/*
 * Copyright 2000-2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.jvm.hotspot.gc_interface;

/** Mimics the enums in the VM under CollectedHeap::Name */

public class CollectedHeapName {
  private String name;

  private CollectedHeapName(String name) { this.name = name; }

  public static final CollectedHeapName ABSTRACT = new CollectedHeapName("abstract");
  public static final CollectedHeapName SHARED_HEAP = new CollectedHeapName("SharedHeap");
  public static final CollectedHeapName GEN_COLLECTED_HEAP = new CollectedHeapName("GenCollectedHeap");
  public static final CollectedHeapName PARALLEL_SCAVENGE_HEAP = new CollectedHeapName("ParallelScavengeHeap");

  public String toString() {
    return name;
  }
}
