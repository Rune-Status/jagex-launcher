/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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
   @bug 6888156
   @summary Tests table column of class Icon.class with Synth LAF
   @author Peter Zhelezniakov
   @run main Test6888156
*/

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class Test6888156 {
    private JTable table;
    private Icon ICON = new Icon() {
        @Override public int getIconWidth() {
            return 24;
        }

        @Override public int getIconHeight() {
            return 24;
        }

        @Override public void paintIcon(Component c, Graphics g, int w, int h) {
        }
    };

    public Test6888156() {
        TableModel model = new AbstractTableModel() {
            @Override public int getRowCount() {
                return 3;
            }

            @Override public int getColumnCount() {
                return 2;
            }

            @Override public Object getValueAt(int rowIndex, int columnIndex) {
                return (columnIndex == 1 ? ICON : 4);
            }

            @Override public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 1 ? Icon.class : int.class);
            }
        };
        table = new JTable(model);
    }

    public void test(final LookAndFeel laf) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                try {
                    UIManager.setLookAndFeel(laf);
                } catch (UnsupportedLookAndFeelException e) {
                    System.err.println(laf.getDescription() +
                                       " is unsupported; continuing");
                    return;
                }
                SwingUtilities.updateComponentTreeUI(table);
                table.setSize(100, 100);
                table.paint(
                        new BufferedImage(100, 100, BufferedImage.OPAQUE).
                            getGraphics());
            }
        });
    }

    public static void main(String[] args) throws Exception {
        Test6888156 t = new Test6888156();
        t.test(new javax.swing.plaf.nimbus.NimbusLookAndFeel());
        t.test(new com.sun.java.swing.plaf.gtk.GTKLookAndFeel());
    }
}