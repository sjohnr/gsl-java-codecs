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

/**
 * RequestMessage codec.
 */
public class RequestMessage {
    public static final int MESSAGE_ID = 2;

    protected long sequence;
    protected String fileName;
    protected long start;
    protected long end;

    /**
     * Get the sequence field.
     * 
     * @return The sequence field
     */
    public long getSequence() {
        return sequence;
    }

    /**
     * Set the sequence field.
     * 
     * @param sequence The sequence field
     */
    public void setSequence(long sequence) {
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
    public void setFileName(String format, Object... args) {
        //  Format into newly allocated string
        fileName = String.format(format, args);
    }

    /**
     * Get the start field.
     * 
     * @return The start field
     */
    public long getStart() {
        return start;
    }

    /**
     * Set the start field.
     * 
     * @param start The start field
     */
    public void setStart(long start) {
        this.start = start;
    }

    /**
     * Get the end field.
     * 
     * @return The end field
     */
    public long getEnd() {
        return end;
    }

    /**
     * Set the end field.
     * 
     * @param end The end field
     */
    public void setEnd(long end) {
        this.end = end;
    }
}
