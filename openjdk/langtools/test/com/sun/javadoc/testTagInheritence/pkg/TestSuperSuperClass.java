/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package pkg;

public class TestSuperSuperClass {

    /**
     * Test 23 passes.
     * @param <P> Test 24 passes.
     * @param <Q> Test 25 passes.
     * @param x1 Test 26 passes.
     * @param x2 Test 27 passes.
     * @return Test 28 passes.
     * @throws java.io.IOException Test 29 passes.
     * @throws java.lang.NullPointerException Test 30 passes.
     */
    public <P,Q> String testSuperSuperMethod(int x1, int x2) {
        return null;
    }

    /**
     * @param <P> Test 32 passes.
     * @param x1 Test 34 passes.
     * @return Test 36 passes.
     * @throws java.lang.NullPointerException Test 38 passes.
     */
    public <P,Q> String testSuperSuperMethod2(int x1, int x2) {
        return null;
    }

}
