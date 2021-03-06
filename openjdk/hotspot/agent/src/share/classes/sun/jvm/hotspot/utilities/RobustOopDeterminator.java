/*
 * Copyright 2000-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.jvm.hotspot.utilities;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

/** This class determines to the best of its ability, and in a
    reasonably robust fashion, whether a given pointer is an intact
    oop or not. It does this by checking the integrity of the
    metaclass hierarchy. This is only intended for use in the
    debugging system. It may provide more resilience to unexpected VM
    states than the ObjectHeap code. */

public class RobustOopDeterminator {
  private static OopField klassField;

  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }

  private static void initialize(TypeDataBase db) {
    Type type = db.lookupType("oopDesc");

    if (VM.getVM().isCompressedOopsEnabled()) {
      klassField = type.getNarrowOopField("_metadata._compressed_klass");
    } else {
      klassField = type.getOopField("_metadata._klass");
    }
  }

  public static boolean oopLooksValid(OopHandle oop) {
    if (oop == null) {
      return false;
    }
    if (!VM.getVM().getUniverse().isIn(oop)) {
      return false;
    }
    try {
      for (int i = 0; i < 4; ++i) {
        OopHandle next = klassField.getValue(oop);
        if (next == null) {
          return false;
        }
        if (next.equals(oop)) {
          return true;
        }
        oop = next;
      }
      return false;
    }
    catch (AddressException e) {
      return false;
    }
  }
}
