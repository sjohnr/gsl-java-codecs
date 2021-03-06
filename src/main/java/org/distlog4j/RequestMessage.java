/* ============================================================================
 * RequestMessage.java
 * 
 * Generated codec class for RequestMessage
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
package org.distlog4j;

import java.util.*;

import org.zeromq.api.*;
import org.zeromq.api.Message.Frame;

/**
 * RequestMessage class.
 */
public class RequestMessage {
    public static final LogSocket.MessageType MESSAGE_TYPE = LogSocket.MessageType.REQUEST;

    protected Integer sequence;
    protected String fileName;
    protected Integer start;
    protected Integer end;

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
     * Get the fileName field.
     * 
     * @return The fileName field
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set the fileName field.
     * 
     * @param fileName The fileName field
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Get the start field.
     * 
     * @return The start field
     */
    public Integer getStart() {
        return start;
    }

    /**
     * Set the start field.
     * 
     * @param start The start field
     */
    public void setStart(Integer start) {
        this.start = start;
    }

    /**
     * Get the end field.
     * 
     * @return The end field
     */
    public Integer getEnd() {
        return end;
    }

    /**
     * Set the end field.
     * 
     * @param end The end field
     */
    public void setEnd(Integer end) {
        this.end = end;
    }
}
