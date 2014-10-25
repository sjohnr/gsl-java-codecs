/* ============================================================================
 * LogsMessage.java
 * 
 * Generated codec class for LogsMessage
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
 * LogsMessage codec.
 */
public class LogsMessage {
    public static final LogSocket.MessageType MESSAGE_TYPE = LogSocket.MessageType.LOGS;

    protected long sequence;
    protected Map<String, String> headers;
    protected String ip;
    protected int port;
    protected String fileName;
    protected long lineNum;
    protected String message;

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
     * Get the ip field.
     * 
     * @return The ip field
     */
    public String getIp() {
        return ip;
    }

    /**
     * Set the ip field.
     * 
     * @param ip The ip field
     */
    public void setIp(String format, Object... args) {
        //  Format into newly allocated string
        ip = String.format(format, args);
    }

    /**
     * Get the port field.
     * 
     * @return The port field
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the port field.
     * 
     * @param port The port field
     */
    public void setPort(int port) {
        this.port = port;
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
     * Get the lineNum field.
     * 
     * @return The lineNum field
     */
    public long getLineNum() {
        return lineNum;
    }

    /**
     * Set the lineNum field.
     * 
     * @param lineNum The lineNum field
     */
    public void setLineNum(long lineNum) {
        this.lineNum = lineNum;
    }

    /**
     * Get the message field.
     * 
     * @return The message field
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message field.
     * 
     * @param message The message field
     */
    public void setMessage(String format, Object... args) {
        //  Format into newly allocated string
        message = String.format(format, args);
    }
}
