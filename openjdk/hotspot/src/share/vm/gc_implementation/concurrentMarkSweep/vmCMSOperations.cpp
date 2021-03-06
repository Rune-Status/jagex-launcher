/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */
# include "incls/_precompiled.incl"
# include "incls/_vmCMSOperations.cpp.incl"

HS_DTRACE_PROBE_DECL(hs_private, cms__initmark__begin);
HS_DTRACE_PROBE_DECL(hs_private, cms__initmark__end);

HS_DTRACE_PROBE_DECL(hs_private, cms__remark__begin);
HS_DTRACE_PROBE_DECL(hs_private, cms__remark__end);

//////////////////////////////////////////////////////////
// Methods in abstract class VM_CMS_Operation
//////////////////////////////////////////////////////////
void VM_CMS_Operation::acquire_pending_list_lock() {
  // The caller may block while communicating
  // with the SLT thread in order to acquire/release the PLL.
  ConcurrentMarkSweepThread::slt()->
    manipulatePLL(SurrogateLockerThread::acquirePLL);
}

void VM_CMS_Operation::release_and_notify_pending_list_lock() {
  // The caller may block while communicating
  // with the SLT thread in order to acquire/release the PLL.
  ConcurrentMarkSweepThread::slt()->
    manipulatePLL(SurrogateLockerThread::releaseAndNotifyPLL);
}

void VM_CMS_Operation::verify_before_gc() {
  if (VerifyBeforeGC &&
      GenCollectedHeap::heap()->total_collections() >= VerifyGCStartAt) {
    HandleMark hm;
    FreelistLocker x(_collector);
    MutexLockerEx  y(_collector->bitMapLock(), Mutex::_no_safepoint_check_flag);
    Universe::heap()->prepare_for_verify();
    Universe::verify(true);
  }
}

void VM_CMS_Operation::verify_after_gc() {
  if (VerifyAfterGC &&
      GenCollectedHeap::heap()->total_collections() >= VerifyGCStartAt) {
    HandleMark hm;
    FreelistLocker x(_collector);
    MutexLockerEx  y(_collector->bitMapLock(), Mutex::_no_safepoint_check_flag);
    Universe::verify(true);
  }
}

bool VM_CMS_Operation::lost_race() const {
  if (CMSCollector::abstract_state() == CMSCollector::Idling) {
    // We lost a race to a foreground collection
    // -- there's nothing to do
    return true;
  }
  assert(CMSCollector::abstract_state() == legal_state(),
         "Inconsistent collector state?");
  return false;
}

bool VM_CMS_Operation::doit_prologue() {
  assert(Thread::current()->is_ConcurrentGC_thread(), "just checking");
  assert(!CMSCollector::foregroundGCShouldWait(), "Possible deadlock");
  assert(!ConcurrentMarkSweepThread::cms_thread_has_cms_token(),
         "Possible deadlock");

  if (needs_pll()) {
    acquire_pending_list_lock();
  }
  // Get the Heap_lock after the pending_list_lock.
  Heap_lock->lock();
  if (lost_race()) {
    assert(_prologue_succeeded == false, "Initialized in c'tor");
    Heap_lock->unlock();
    if (needs_pll()) {
      release_and_notify_pending_list_lock();
    }
  } else {
    _prologue_succeeded = true;
  }
  return _prologue_succeeded;
}

void VM_CMS_Operation::doit_epilogue() {
  assert(Thread::current()->is_ConcurrentGC_thread(), "just checking");
  assert(!CMSCollector::foregroundGCShouldWait(), "Possible deadlock");
  assert(!ConcurrentMarkSweepThread::cms_thread_has_cms_token(),
         "Possible deadlock");

  // Release the Heap_lock first.
  Heap_lock->unlock();
  if (needs_pll()) {
    release_and_notify_pending_list_lock();
  }
}

//////////////////////////////////////////////////////////
// Methods in class VM_CMS_Initial_Mark
//////////////////////////////////////////////////////////
void VM_CMS_Initial_Mark::doit() {
  if (lost_race()) {
    // Nothing to do.
    return;
  }
  HS_DTRACE_PROBE(hs_private, cms__initmark__begin);

  GenCollectedHeap* gch = GenCollectedHeap::heap();
  GCCauseSetter gccs(gch, GCCause::_cms_initial_mark);

  VM_CMS_Operation::verify_before_gc();

  IsGCActiveMark x; // stop-world GC active
  _collector->do_CMS_operation(CMSCollector::CMS_op_checkpointRootsInitial);

  VM_CMS_Operation::verify_after_gc();
  HS_DTRACE_PROBE(hs_private, cms__initmark__end);
}

//////////////////////////////////////////////////////////
// Methods in class VM_CMS_Final_Remark_Operation
//////////////////////////////////////////////////////////
void VM_CMS_Final_Remark::doit() {
  if (lost_race()) {
    // Nothing to do.
    return;
  }
  HS_DTRACE_PROBE(hs_private, cms__remark__begin);

  GenCollectedHeap* gch = GenCollectedHeap::heap();
  GCCauseSetter gccs(gch, GCCause::_cms_final_remark);

  VM_CMS_Operation::verify_before_gc();

  IsGCActiveMark x; // stop-world GC active
  _collector->do_CMS_operation(CMSCollector::CMS_op_checkpointRootsFinal);

  VM_CMS_Operation::verify_after_gc();
  HS_DTRACE_PROBE(hs_private, cms__remark__end);
}

// VM operation to invoke a concurrent collection of a
// GenCollectedHeap heap.
void VM_GenCollectFullConcurrent::doit() {
  assert(Thread::current()->is_VM_thread(), "Should be VM thread");

  GenCollectedHeap* gch = GenCollectedHeap::heap();
  if (_gc_count_before == gch->total_collections()) {
    // The "full" of do_full_collection call below "forces"
    // a collection; the second arg, 0, below ensures that
    // only the young gen is collected. XXX In the future,
    // we'll probably need to have something in this interface
    // to say do this only if we are sure we will not bail
    // out to a full collection in this attempt, but that's
    // for the future.
    assert(SafepointSynchronize::is_at_safepoint(),
      "We can only be executing this arm of if at a safepoint");
    GCCauseSetter gccs(gch, _gc_cause);
    gch->do_full_collection(gch->must_clear_all_soft_refs(),
                            0 /* collect only youngest gen */);
  } // Else no need for a foreground young gc
  assert((_gc_count_before < gch->total_collections()) ||
         (GC_locker::is_active() /* gc may have been skipped */
          && (_gc_count_before == gch->total_collections())),
         "total_collections() should be monotonically increasing");

  MutexLockerEx x(FullGCCount_lock, Mutex::_no_safepoint_check_flag);
  if (gch->total_full_collections() == _full_gc_count_before) {
    // Disable iCMS until the full collection is done.
    CMSCollector::disable_icms();
    // In case CMS thread was in icms_wait(), wake it up.
    CMSCollector::start_icms();
    // Nudge the CMS thread to start a concurrent collection
    CMSCollector::request_full_gc(_full_gc_count_before);
  } else {
    FullGCCount_lock->notify_all();  // Inform the Java thread its work is done
  }
}

bool VM_GenCollectFullConcurrent::evaluate_at_safepoint() const {
  Thread* thr = Thread::current();
  assert(thr != NULL, "Unexpected tid");
  if (!thr->is_Java_thread()) {
    assert(thr->is_VM_thread(), "Expected to be evaluated by VM thread");
    GenCollectedHeap* gch = GenCollectedHeap::heap();
    if (_gc_count_before != gch->total_collections()) {
      // No need to do a young gc, we'll just nudge the CMS thread
      // in the doit() method above, to be executed soon.
      assert(_gc_count_before < gch->total_collections(),
             "total_collections() should be monotnically increasing");
      return false;  // no need for foreground young gc
    }
  }
  return true;       // may still need foreground young gc
}


void VM_GenCollectFullConcurrent::doit_epilogue() {
  Thread* thr = Thread::current();
  assert(thr->is_Java_thread(), "just checking");
  JavaThread* jt = (JavaThread*)thr;
  // Release the Heap_lock first.
  Heap_lock->unlock();
  release_and_notify_pending_list_lock();

  // It is fine to test whether completed collections has
  // exceeded our request count without locking because
  // the completion count is monotonically increasing;
  // this will break for very long-running apps when the
  // count overflows and wraps around. XXX fix me !!!
  // e.g. at the rate of 1 full gc per ms, this could
  // overflow in about 1000 years.
  GenCollectedHeap* gch = GenCollectedHeap::heap();
  if (gch->total_full_collections_completed() <= _full_gc_count_before) {
    // Now, wait for witnessing concurrent gc cycle to complete,
    // but do so in native mode, because we want to lock the
    // FullGCEvent_lock, which may be needed by the VM thread
    // or by the CMS thread, so we do not want to be suspended
    // while holding that lock.
    ThreadToNativeFromVM native(jt);
    MutexLockerEx ml(FullGCCount_lock, Mutex::_no_safepoint_check_flag);
    // Either a concurrent or a stop-world full gc is sufficient
    // witness to our request.
    while (gch->total_full_collections_completed() <= _full_gc_count_before) {
      FullGCCount_lock->wait(Mutex::_no_safepoint_check_flag);
    }
  }
  // Enable iCMS back.
  CMSCollector::enable_icms();
}
