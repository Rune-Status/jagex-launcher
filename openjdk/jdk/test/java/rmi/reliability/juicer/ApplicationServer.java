/*
 * Copyright 2003-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * The ApplicationServer class provides the other server side of the "juicer"
 * stress test of RMI.
 */
public class ApplicationServer implements Runnable {

    /** number of remote Apple objects to export */
    private static final Logger logger = Logger.getLogger("reliability.orange");
    private static final int LOOKUP_ATTEMPTS = 5;
    private static final int DEFAULT_NUMAPPLES = 10;
    private static final String DEFAULT_REGISTRYHOST = "localhost";
    private final int numApples;
    private final String registryHost;
    private final Apple[] apples;
    private AppleUser user;

    ApplicationServer() {
        this(DEFAULT_NUMAPPLES, DEFAULT_REGISTRYHOST);
    }

    ApplicationServer(int numApples, String registryHost) {
        this.numApples = numApples;
        this.registryHost = registryHost;
        apples = new Apple[numApples];
    }

    /*
     * On initialization, export remote objects and register
     * them with server.
     */
    public void run() {
        try {
            int i = 0;

            /*
             * Locate apple user object in registry.  The lookup will
             * occur until it is successful or fails LOOKUP_ATTEMPTS times.
             * These repeated attempts allow the ApplicationServer
             * to be started before the AppleUserImpl.
             */
            Exception exc = null;
            for (i = 0; i < LOOKUP_ATTEMPTS; i++) {
                try {
                    Registry registry = LocateRegistry.getRegistry(
                        registryHost, 2006);
                    user = (AppleUser) registry.lookup("AppleUser");
                    user.startTest();
                    break; //successfully obtained AppleUser
                } catch (Exception e) {
                    exc = e;
                    Thread.sleep(10000); //sleep 10 seconds and try again
                }
            }
            if (user == null) {
                logger.log(Level.SEVERE, "Failed to lookup AppleUser:", exc);
                return;
            }

            /*
             * Create and export apple implementations.
             */
            try {
                for (i = 0; i < numApples; i++) {
                    apples[i] = new AppleImpl("AppleImpl #" + (i + 1));
                }
            } catch (RemoteException e) {
                logger.log(Level.SEVERE,
                    "Failed to create AppleImpl #" + (i + 1) + ":", e);
                user.reportException(e);
                return;
            }

            /*
             * Hand apple objects to apple user.
             */
            try {
                for (i = 0; i < numApples; i++) {
                    user.useApple(apples[i]);
                }
            } catch (RemoteException e) {
                logger.log(Level.SEVERE,
                    "Failed to register callbacks for " + apples[i] + ":", e);
                user.reportException(e);
                return;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected exception:", e);
        }
    }

    private static void usage() {
        System.err.println("Usage: ApplicationServer [-numApples <numApples>]");
        System.err.println("                         [-registryHost <host>]");
        System.err.println("  numApples  The number of apples (threads) to use.");
        System.err.println("             The default is 10 apples.");
        System.err.println("  host       The host running rmiregistry " +
                                         "which contains AppleUser.");
        System.err.println("             The default is \"localhost\".");
        System.err.println();
    }

    public static void main(String[] args) {
        int num = DEFAULT_NUMAPPLES;
        String host = DEFAULT_REGISTRYHOST;

        // parse command line args
        try {
            for (int i = 0; i < args.length ; i++ ) {
                String arg = args[i];
                if (arg.equals("-numApples")) {
                    i++;
                    num = Integer.parseInt(args[i]);
                } else if (arg.equals("-registryHost")) {
                    i++;
                    host = args[i];
                } else {
                    usage();
                }
            }
        } catch (Throwable t) {
            usage();
            throw new RuntimeException("TEST FAILED: Bad argument");
        }

        // start the client server
        Thread server = new Thread(new ApplicationServer(num,host));
        server.start();
        // main should exit once all exported remote objects are gc'd
    }
}
