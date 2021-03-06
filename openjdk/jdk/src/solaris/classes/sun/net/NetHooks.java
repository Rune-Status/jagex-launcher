/*
 * Copyright 2009 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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

package sun.net;

import java.net.InetAddress;
import java.io.FileDescriptor;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.security.action.GetPropertyAction;

/**
 * Defines static methods to be invoked prior to binding or connecting TCP sockets.
 */

public final class NetHooks {

    /**
     * A provider with hooks to allow sockets be converted prior to binding or
     * connecting a TCP socket.
     *
     * <p> Concrete implementations of this class should define a zero-argument
     * constructor and implement the abstract methods specified below.
     */
    public static abstract class Provider {
        /**
         * Initializes a new instance of this class.
         */
        protected Provider() {}

        /**
         * Invoked prior to binding a TCP socket.
         */
        public abstract void implBeforeTcpBind(FileDescriptor fdObj,
                                               InetAddress address,
                                               int port)
            throws IOException;

        /**
         * Invoked prior to connecting an unbound TCP socket.
         */
        public abstract void implBeforeTcpConnect(FileDescriptor fdObj,
                                                 InetAddress address,
                                                 int port)
            throws IOException;
    }

    /**
     * For now, we load the SDP provider on Solaris. In the future this may
     * be changed to use the ServiceLoader facility to allow the deployment of
     * other providers.
     */
    private static Provider loadProvider(final String cn) {
        return AccessController
            .doPrivileged(new PrivilegedAction<Provider>() {
                @Override public Provider run() {
                    Class<Provider> c;
                    try {
                        c = (Class<Provider>)Class.forName(cn, true, null);
                    } catch (ClassNotFoundException x) {
                        return null;
                    }
                    try {
                        return c.newInstance();
                    } catch (IllegalAccessException x) {
                        throw new AssertionError(x);
                    } catch (InstantiationException x) {
                        throw new AssertionError(x);
                    }
            }});
    }
    private static final Provider provider = AccessController
        .doPrivileged(new GetPropertyAction("os.name")).equals("SunOS") ?
            loadProvider("sun.net.spi.SdpProvider") : null;

    /**
     * Invoke prior to binding a TCP socket.
     */
    public static void beforeTcpBind(FileDescriptor fdObj,
                                     InetAddress address,
                                     int port)
        throws IOException
    {
        if (provider != null)
            provider.implBeforeTcpBind(fdObj, address, port);
    }

    /**
     * Invoke prior to connecting an unbound TCP socket.
     */
    public static void beforeTcpConnect(FileDescriptor fdObj,
                                        InetAddress address,
                                        int port)
        throws IOException
    {
        if (provider != null)
            provider.implBeforeTcpConnect(fdObj, address, port);
    }
}
