/*
 * Copyright 2001 Sun Microsystems, Inc.  All Rights Reserved.
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
 * @bug 4446035
 * @summary Simple test of DatagramSocket connection consistency
 * @library ..
 */

import java.net.*;
import java.nio.*;
import java.nio.channels.*;


public class IsConnected {
    public static void main(String argv[]) throws Exception {
        InetSocketAddress isa = new InetSocketAddress(
            InetAddress.getByName(TestUtil.HOST), 13);
        DatagramChannel dc = DatagramChannel.open();
        dc.configureBlocking(true);
        dc.connect(isa);
        if  (!dc.isConnected())
            throw new RuntimeException("channel.isConnected inconsistent");
        if (!dc.socket().isConnected())
            throw new RuntimeException("socket.isConnected inconsistent");
        dc.close();
    }
}
