/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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

/* @test
   @summary Test SoftReceiver send method */

import javax.sound.midi.*;
import javax.sound.sampled.*;

import com.sun.media.sound.*;

public class Send_ActiveSense {

    private static void assertEquals(Object a, Object b) throws Exception
    {
        if(!a.equals(b))
            throw new RuntimeException("assertEquals fails!");
    }

    private static void assertTrue(boolean value) throws Exception
    {
        if(!value)
            throw new RuntimeException("assertTrue fails!");
    }

    public static void sendActiveSens(Receiver r) throws Exception
    {
        ShortMessage smsg = new ShortMessage();
        smsg.setMessage(ShortMessage.ACTIVE_SENSING);
        r.send(smsg, -1);
    }

    public static void main(String[] args) throws Exception {
        SoftTestUtils soft = new SoftTestUtils();
        MidiChannel channel = soft.synth.getChannels()[0];
        Receiver receiver = soft.synth.getReceiver();

        sendActiveSens(receiver);

        // 1. Check if notes are keept active
        //    if send active sens every 200-300 msec

        sendActiveSens(receiver);
        channel.noteOn(60, 64);
        assertTrue(soft.findVoice(0,60) != null);
        for (int i = 0; i < 10; i++) {
            soft.read(0.2); // read 200 msec
            sendActiveSens(receiver);
            assertTrue(soft.findVoice(0,60) != null);
        }
        // 2. Now we stop send active sense message
        //    and the note should be killed off
        soft.read(2);
        assertTrue(soft.findVoice(0,60) == null);



        soft.close();
    }
}
