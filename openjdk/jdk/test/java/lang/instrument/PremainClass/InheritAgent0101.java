/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 */

/**
 * @test
 * @bug 6289149
 * @summary test config (0,1,0,1): inherited 1-arg and declared 1-arg in agent class
 * @author Daniel D. Daugherty, Sun Microsystems
 *
 * @run shell ../MakeJAR3.sh InheritAgent0101
 * @run main/othervm -javaagent:InheritAgent0101.jar DummyMain
 */

import java.lang.instrument.*;

class InheritAgent0101 extends InheritAgent0101Super {

    //
    // This agent has a single argument premain() method which
    // is the one that should be called.
    //
    public static void premain (String agentArgs) {
        System.out.println("Hello from Single-Arg InheritAgent0101!");
    }

    // This agent does NOT have a double argument premain() method.
}

class InheritAgent0101Super {

    //
    // This agent has a single argument premain() method which
    // is NOT the one that should be called.
    //
    public static void premain (String agentArgs) {
        System.out.println("Hello from Single-Arg InheritAgent0101Super!");
        throw new Error("ERROR: THIS AGENT SHOULD NOT HAVE BEEN CALLED.");
    }

    // This agent does NOT have a double argument premain() method.
}
