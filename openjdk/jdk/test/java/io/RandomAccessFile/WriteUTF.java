/*
 * Copyright 1997 Sun Microsystems, Inc.  All Rights Reserved.
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

/* @test
   @bug 4018515
   @summary Make sure that writeUTF throws a UTFDataFormatException when the
            output string is too long
 */

import java.io.*;


public class WriteUTF {

    public static void main(String[] args) throws IOException {

        RandomAccessFile f;
        File fn = new File("x.WriteUTF");

        String s = "\uffff";
        for (int i = 0; i < 16; i++)
            s += s;
        System.err.println("String length " + s.length());

        try {
            f = new RandomAccessFile(fn, "rw");
            try {
                f.writeUTF(s);
            }
            catch (UTFDataFormatException x) {
                return;
            }
            throw new RuntimeException("UTFDataFormatException not thrown");
        }
        finally {
            fn.delete();
        }

    }

}
