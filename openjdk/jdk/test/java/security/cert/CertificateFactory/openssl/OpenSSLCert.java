/*
 * Copyright (c) 2009 Sun Microsystems, Inc.  All Rights Reserved.
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
/*
 * @test
 * @bug 6535697
 * @summary keytool can be more flexible on format of PEM-encoded
 *  X.509 certificates
 */

import java.io.*;
import java.util.Arrays;
import java.security.cert.CertificateFactory;

public class OpenSSLCert {
    static final String OUTFILE = "6535697.test";

    public static void main(String[] args) throws Exception {
        test("open");
        test("pem");
        test("open", "open");
        test("open", "pem");
        test("pem", "pem");
        test("pem", "open");
        test("open", "pem", "open");
        test("pem", "open", "pem");
    }

    static void test(String... files) throws Exception {
        FileOutputStream fout = new FileOutputStream(OUTFILE);
        for (String file: files) {
            FileInputStream fin = new FileInputStream(
                new File(System.getProperty("test.src", "."), file));
            byte[] buffer = new byte[4096];
            while (true) {
                int len = fin.read(buffer);
                if (len < 0) break;
                fout.write(buffer, 0, len);
            }
            fin.close();
        }
        fout.close();
        System.out.println("Testing " + Arrays.toString(files) + "...");
        if (CertificateFactory.getInstance("X509")
                .generateCertificates(new FileInputStream(OUTFILE))
                .size() != files.length) {
            throw new Exception("Not same number");
        }
    }
}
