/*
 * Copyright (c) 2002, 2015, Oracle and/or its affiliates. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License, version 2.0, as published by the
 * Free Software Foundation.
 *
 * This program is also distributed with certain software (including but not
 * limited to OpenSSL) that is licensed under separate terms, as designated in a
 * particular file or component or in included license documentation. The
 * authors of MySQL hereby grant you an additional permission to link the
 * program and your derivative works with the separately licensed software that
 * they have included with MySQL.
 *
 * Without limiting anything contained in the foregoing, this file, which is
 * part of MySQL Connector/J, is also subject to the Universal FOSS Exception,
 * version 1.0, a copy of which can be found at
 * http://oss.oracle.com/licenses/universal-foss-exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License, version 2.0,
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.mysql.cj.jdbc;

import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.mysql.cj.api.jdbc.ClientInfoProvider;

/**
 * An implementation of ClientInfoProvider that exposes the client info as a comment prepended to all statements issued by the driver.
 * 
 * Client information is <i>never</i> read from the server with this implementation, it is always cached locally.
 */

public class CommentClientInfoProvider implements ClientInfoProvider {
    private Properties clientInfo;

    public synchronized void initialize(java.sql.Connection conn, Properties configurationProps) throws SQLException {
        this.clientInfo = new Properties();
    }

    public synchronized void destroy() throws SQLException {
        this.clientInfo = null;
    }

    public synchronized Properties getClientInfo(java.sql.Connection conn) throws SQLException {
        return this.clientInfo;
    }

    public synchronized String getClientInfo(java.sql.Connection conn, String name) throws SQLException {
        return this.clientInfo.getProperty(name);
    }

    public synchronized void setClientInfo(java.sql.Connection conn, Properties properties) throws SQLClientInfoException {
        this.clientInfo = new Properties();

        Enumeration<?> propNames = properties.propertyNames();

        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();

            this.clientInfo.put(name, properties.getProperty(name));
        }

        setComment(conn);
    }

    public synchronized void setClientInfo(java.sql.Connection conn, String name, String value) throws SQLClientInfoException {
        this.clientInfo.setProperty(name, value);
        setComment(conn);
    }

    private synchronized void setComment(java.sql.Connection conn) {
        StringBuilder commentBuf = new StringBuilder();
        Iterator<Entry<Object, Object>> elements = this.clientInfo.entrySet().iterator();

        while (elements.hasNext()) {
            if (commentBuf.length() > 0) {
                commentBuf.append(", ");
            }

            Map.Entry<?, ?> entry = elements.next();
            commentBuf.append("" + entry.getKey());
            commentBuf.append("=");
            commentBuf.append("" + entry.getValue());
        }

        ((com.mysql.cj.api.jdbc.JdbcConnection) conn).setStatementComment(commentBuf.toString());
    }
}
