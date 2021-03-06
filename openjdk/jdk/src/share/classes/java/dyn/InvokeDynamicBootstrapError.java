/*
 * Copyright 2008-2009 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.dyn;

/**
 * Thrown to indicate that an {@code invokedynamic} instruction has
 * failed to find its bootstrap method, or the bootstrap method has
 * failed to provide a call site with a non-null target.
 * <p>
 * The boostrap method must have been declared during a class's initialization
 * by a call to {@link Linkage#registerBootstrapMethod}.
 *
 * @author John Rose, JSR 292 EG
 */
public class InvokeDynamicBootstrapError extends LinkageError {
    /**
     * Constructs a {@code InvokeDynamicBootstrapError} with no detail message.
     */
    public InvokeDynamicBootstrapError() {
        super();
    }

    /**
     * Constructs a {@code InvokeDynamicBootstrapError} with the specified
     * detail message.
     *
     * @param s the detail message.
     */
    public InvokeDynamicBootstrapError(String s) {
        super(s);
    }

    /**
     * Constructs a {@code InvokeDynamicBootstrapError} with the specified
     * detail message and cause.
     *
     * @param s the detail message.
     * @param cause the cause.
     */
    public InvokeDynamicBootstrapError(String s, Throwable cause) {
        super(s);
        this.initCause(cause);
    }
}
