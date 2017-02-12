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
import org.zeromq.api.Message.Frame;

/**
 * LogsMessage class.
 */
public class LogsMessage {
    public static final LogSocket.MessageType MESSAGE_TYPE = LogSocket.MessageType.LOGS;

    protected Integer sequence;
    protected Map<String, String> headers;
    protected String ip;
    protected Integer port;
    protected String fileName;
    protected Integer lineNum;
    protected List<String> messages;

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
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Get the port field.
     * 
     * @return The port field
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Set the port field.
     * 
     * @param port The port field
     */
    public void setPort(Integer port) {
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
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Get the lineNum field.
     * 
     * @return The lineNum field
     */
    public Integer getLineNum() {
        return lineNum;
    }

    /**
     * Set the lineNum field.
     * 
     * @param lineNum The lineNum field
     */
    public void setLineNum(Integer lineNum) {
        this.lineNum = lineNum;
    }

    /**
     * Get the list of messages strings.
     * 
     * @return The messages strings
     */
    public List<String> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }

    /**
     * Append a value to the messages field.
     *
     * @param value The value
     */
    public void addMessage(String value) {
        getMessages().add(value);
    }

    /**
     * Set the list of messages strings.
     * 
     * @param messages The messages collection
     */
    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
