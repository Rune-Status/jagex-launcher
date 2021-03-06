/*
 * Copyright (c) 2007 Sun Microsystems, Inc.  All Rights Reserved.
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

template <class T> inline void ParScanWeakRefClosure::do_oop_work(T* p) {
  assert (!oopDesc::is_null(*p), "null weak reference?");
  oop obj = oopDesc::load_decode_heap_oop_not_null(p);
  // weak references are sometimes scanned twice; must check
  // that to-space doesn't already contain this object
  if ((HeapWord*)obj < _boundary && !_g->to()->is_in_reserved(obj)) {
    // we need to ensure that it is copied (see comment in
    // ParScanClosure::do_oop_work).
    klassOop objK = obj->klass();
    markOop m = obj->mark();
    oop new_obj;
    if (m->is_marked()) { // Contains forwarding pointer.
      new_obj = ParNewGeneration::real_forwardee(obj);
    } else {
      size_t obj_sz = obj->size_given_klass(objK->klass_part());
      new_obj = ((ParNewGeneration*)_g)->copy_to_survivor_space(_par_scan_state,
                                                                obj, obj_sz, m);
    }
    oopDesc::encode_store_heap_oop_not_null(p, new_obj);
  }
}

inline void ParScanWeakRefClosure::do_oop_nv(oop* p)       { ParScanWeakRefClosure::do_oop_work(p); }
inline void ParScanWeakRefClosure::do_oop_nv(narrowOop* p) { ParScanWeakRefClosure::do_oop_work(p); }

template <class T> inline void ParScanClosure::par_do_barrier(T* p) {
  assert(generation()->is_in_reserved(p), "expected ref in generation");
  assert(!oopDesc::is_null(*p), "expected non-null object");
  oop obj = oopDesc::load_decode_heap_oop_not_null(p);
  // If p points to a younger generation, mark the card.
  if ((HeapWord*)obj < gen_boundary()) {
    rs()->write_ref_field_gc_par(p, obj);
  }
}

template <class T>
inline void ParScanClosure::do_oop_work(T* p,
                                        bool gc_barrier,
                                        bool root_scan) {
  assert((!Universe::heap()->is_in_reserved(p) ||
          generation()->is_in_reserved(p))
         && (generation()->level() == 0 || gc_barrier),
         "The gen must be right, and we must be doing the barrier "
         "in older generations.");
  T heap_oop = oopDesc::load_heap_oop(p);
  if (!oopDesc::is_null(heap_oop)) {
    oop obj = oopDesc::decode_heap_oop_not_null(heap_oop);
    if ((HeapWord*)obj < _boundary) {
      assert(!_g->to()->is_in_reserved(obj), "Scanning field twice?");
      // OK, we need to ensure that it is copied.
      // We read the klass and mark in this order, so that we can reliably
      // get the size of the object: if the mark we read is not a
      // forwarding pointer, then the klass is valid: the klass is only
      // overwritten with an overflow next pointer after the object is
      // forwarded.
      klassOop objK = obj->klass();
      markOop m = obj->mark();
      oop new_obj;
      if (m->is_marked()) { // Contains forwarding pointer.
        new_obj = ParNewGeneration::real_forwardee(obj);
        oopDesc::encode_store_heap_oop_not_null(p, new_obj);
      } else {
        size_t obj_sz = obj->size_given_klass(objK->klass_part());
        new_obj = _g->copy_to_survivor_space(_par_scan_state, obj, obj_sz, m);
        oopDesc::encode_store_heap_oop_not_null(p, new_obj);
        if (root_scan) {
          // This may have pushed an object.  If we have a root
          // category with a lot of roots, can't let the queue get too
          // full:
          (void)_par_scan_state->trim_queues(10 * ParallelGCThreads);
        }
      }
      if (gc_barrier) {
        // Now call parent closure
        par_do_barrier(p);
      }
    }
  }
}

inline void ParScanWithBarrierClosure::do_oop_nv(oop* p)       { ParScanClosure::do_oop_work(p, true, false); }
inline void ParScanWithBarrierClosure::do_oop_nv(narrowOop* p) { ParScanClosure::do_oop_work(p, true, false); }

inline void ParScanWithoutBarrierClosure::do_oop_nv(oop* p)       { ParScanClosure::do_oop_work(p, false, false); }
inline void ParScanWithoutBarrierClosure::do_oop_nv(narrowOop* p) { ParScanClosure::do_oop_work(p, false, false); }
