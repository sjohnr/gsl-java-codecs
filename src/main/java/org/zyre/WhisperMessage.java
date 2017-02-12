/* ============================================================================
 * WhisperMessage.java
 * 
 * Generated codec class for WhisperMessage
 * ----------------------------------------------------------------------------
 * This is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by   
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.                                      
 *                                                                      
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of           
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU     
 * Lesser General Public License for more details.                      
 *                                                                      
 * You should have received a copy of the GNU Lesser General Public     
 * License along with this program. If not, see                         
 * http://www.gnu.org/licenses.                                         
 * ============================================================================
 */
package org.zyre;

import java.util.*;

import org.zeromq.api.*;
import org.zeromq.api.Message.Frame;

/**
 * WhisperMessage class.
 */
public class WhisperMessage {
    public static final ZreSocket.MessageType MESSAGE_TYPE = ZreSocket.MessageType.WHISPER;

    protected Integer sequence;
    protected Frame content = Message.EMPTY_FRAME;

    /**
     * Get the sequence field.
     * 
     * @return The sequence field
     */
    public Integer getSequence() {
        return sequence;
    }

    /**
     * Set the sequence field.
     * 
     * @param sequence The sequence field
     */
    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    /**
     * Get the content field.
     * 
     * @return The content field
     */
    public Frame getContent() {
        return content;
    }

    /**
     * Set the content field, and takes ownership of supplied frame.
     * 
     * @param frame The new content frame
     */
    public void setContent(Frame frame) {
        content = frame;
    }
}
