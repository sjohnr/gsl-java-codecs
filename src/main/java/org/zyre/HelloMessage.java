/* ============================================================================
 * HelloMessage.java
 * 
 * Generated codec class for HelloMessage
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
 * HelloMessage class.
 */
public class HelloMessage {
    public static final ZreSocket.MessageType MESSAGE_TYPE = ZreSocket.MessageType.HELLO;

    protected Integer sequence;
    protected String ipAddress;
    protected Integer mailbox;
    protected List<String> groups;
    protected Integer status;
    protected Map<String, String> headers;

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
     * Get the ipAddress field.
     * 
     * @return The ipAddress field
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Set the ipAddress field.
     * 
     * @param ipAddress The ipAddress field
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Get the mailbox field.
     * 
     * @return The mailbox field
     */
    public Integer getMailbox() {
        return mailbox;
    }

    /**
     * Set the mailbox field.
     * 
     * @param mailbox The mailbox field
     */
    public void setMailbox(Integer mailbox) {
        this.mailbox = mailbox;
    }

    /**
     * Get the list of groups strings.
     * 
     * @return The groups strings
     */
    public List<String> getGroups() {
        if (groups == null) {
            groups = new ArrayList<>();
        }
        return groups;
    }

    /**
     * Append a value to the groups field.
     *
     * @param value The value
     */
    public void addGroup(String value) {
        getGroups().add(value);
    }

    /**
     * Set the list of groups strings.
     * 
     * @param groups The groups collection
     */
    public void setGroups(List<String> groups) {
        this.groups = groups;
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

    /**
     * Get the the headers dictionary.
     * 
     * @return The headers dictionary
     */
    public Map<String, String> getHeaders() {
        if (headers == null) {
            headers = new HashMap<>();
        }
        return headers;
    }

    /**
     * Get a value in the headers dictionary as a string.
     * 
     * @param key The dictionary key
     * @param defaultValue The default value if the key does not exist
     */
    public String getHeader(String key, String defaultValue) {
        String value = defaultValue;
        if (headers != null) {
            value = headers.getOrDefault(key, defaultValue);
        }
        return value;
    }

    /**
     * Get a value in the headers dictionary as a long.
     * 
     * @param key The dictionary key
     * @param defaultValue The default value if the key does not exist
     */
    public long getHeader(String key, long defaultValue) {
        long value = defaultValue;
        if (headers != null && headers.containsKey(key)) {
            value = Long.parseLong(headers.get(key));
        }
        return value;
    }

    /**
     * Get a value in the headers dictionary as a long.
     *
     * @param key The dictionary key
     * @param defaultValue The default value if the key does not exist
     */
    public int getHeader(String key, int defaultValue) {
        int value = defaultValue;
        if (headers != null && headers.containsKey(key)) {
            value = Integer.parseInt(headers.get(key));
        }
        return value;
    }

    /**
     * Set a value in the headers dictionary.
     *
     * @param key The dictionary key
     * @param value The value
     */
    public void putHeader(String key, String value) {
        getHeaders().put(key, value);
    }

    /**
     * Set a value in the headers dictionary.
     * 
     * @param key The dictionary key
     * @param value The value
     */
    public void putHeader(String key, int value) {
        getHeaders().put(key, String.valueOf(value));
    }

    /**
     * Set a value in the headers dictionary.
     * 
     * @param key The dictionary key
     * @param value The value
     */
    public void putHeader(String key, long value) {
        getHeaders().put(key, String.valueOf(value));
    }

    /**
     * Set the headers dictionary.
     * 
     * @param headers The new headers dictionary
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
