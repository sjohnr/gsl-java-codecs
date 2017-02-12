/* ============================================================================
 * JoinMessage.java
 * 
 * Generated codec class for JoinMessage
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
 * JoinMessage class.
 */
public class JoinMessage {
    public static final ZreSocket.MessageType MESSAGE_TYPE = ZreSocket.MessageType.JOIN;

    protected Integer sequence;
    protected String group;
    protected Integer status;

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
     * Get the group field.
     * 
     * @return The group field
     */
    public String getGroup() {
        return group;
    }

    /**
     * Set the group field.
     * 
     * @param group The group field
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Get the status field.
     * 
     * @return The status field
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Set the status field.
     * 
     * @param status The status field
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
}
