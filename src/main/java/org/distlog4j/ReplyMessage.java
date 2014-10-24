/* ============================================================================
 * ReplyMessage.java
 * 
 * Generated codec class for ReplyMessage
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
 * ReplyMessage codec.
 */
public class ReplyMessage {
    public static final int MESSAGE_ID = 3;

    protected long sequence;
    protected Map<String, String> headers;
    protected List<String> messages;

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
     * Get the the headers dictionary.
     * 
     * @return The headers dictionary
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Get a value in the headers dictionary as a string.
     * 
     * @param key The dictionary key
     * @param defaultValue The default value if the key does not exist
     */
    public String getHeader(String key, String defaultValue) {
        String value = null;
        if (headers != null)
            value = headers.get(key);
        if (value == null)
            value = defaultValue;

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
        String string = null;
        if (headers != null)
            string = headers.get(key);
        if (string != null)
            value = Long.parseLong(string);

        return value;
    }

    /**
     * Get a value in the headers dictionary as a long.
     * 
     * @param key The dictionary key
     * @param defaultValue The default value if the key does not exist
     */
    public long getHeader(String key, int defaultValue) {
        int value = defaultValue;
        String string = null;
        if (headers != null)
            string = headers.get(key);
        if (string != null)
            value = Integer.parseInt(string);

        return value;
    }

    /**
     * Set a value in the headers dictionary.
     * 
     * @param key The dictionary key
     * @param format The string format
     * @param args The arguments used to build the string
     */
    public void putHeader(String key, String format, Object... args) {
        //  Format string into buffer
        String string = String.format(format, args);

        //  Store string in hash table
        if (headers == null)
            headers = new HashMap<String, String>();
        headers.put(key, string);
    }

    /**
     * Set a value in the headers dictionary.
     * 
     * @param key The dictionary key
     * @param value The value
     */
    public void putHeader(String key, int value) {
        //  Store string in hash table
        if (headers == null)
            headers = new HashMap<String, String>();
        headers.put(key, String.valueOf(value));
    }

    /**
     * Set a value in the headers dictionary.
     * 
     * @param key The dictionary key
     * @param value The value
     */
    public void putHeader(String key, long value) {
        //  Store string in hash table
        if (headers == null)
            headers = new HashMap<String, String>();
        headers.put(key, String.valueOf(value));
    }

    /**
     * Set the headers dictionary.
     * 
     * @param value The new headers dictionary
     */
    public void setHeaders(Map<String, String> value) {
        if (value != null)
            headers = new HashMap<String, String>(value); 
        else
            headers = value;
    }

    /**
     * Get the list of messages strings.
     * 
     * @return The messages strings
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * Iterate through the messages field, and append a messages value.
     * 
     * @param format The string format
     * @param args The arguments used to build the string
     */
    public void addMessage(String format, Object... args) {
        //  Format into newly allocated string
        String string = String.format(format, args);

        //  Attach string to list
        if (messages == null)
            messages = new ArrayList<String>();
        messages.add(string);
    }

    /**
     * Set the list of messages strings.
     * 
     * @param value The collection of strings
     */
    public void setMessages(Collection<String> value) {
        messages = new ArrayList<String>(value);
    }
}
