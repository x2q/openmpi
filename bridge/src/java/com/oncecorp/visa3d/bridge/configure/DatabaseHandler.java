/**
 * Copyright 2003, 2004  ONCE Corporation
 *
 * LICENSE:
 * This file is part of BuilditMPI. It may be redistributed and/or modified
 * under the terms of the Common Public License, version 1.0.
 * You should have received a copy of the Common Public License along with this
 * software. See LICENSE.txt for details. Otherwise, you may find it online at:
 *   http://www.oncecorp.com/CPL10/ or http://opensource.org/licenses/cpl.php
 *
 * DISCLAIMER OF WARRANTIES AND LIABILITY:
 * THE SOFTWARE IS PROVIDED "AS IS".  THE AUTHOR MAKES NO REPRESENTATIONS OR
 * WARRANTIES, EITHER EXPRESS OR IMPLIED.  TO THE EXTENT NOT PROHIBITED BY LAW,
 * IN NO EVENT WILL THE AUTHOR BE LIABLE FOR ANY DAMAGES, INCLUDING WITHOUT
 * LIMITATION, LOST REVENUE, PROFITS OR DATA, OR FOR SPECIAL, INDIRECT,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS
 * OF THE THEORY OF LIABILITY, ARISING OUT OF OR RELATED TO ANY FURNISHING,
 * PRACTICING, MODIFYING OR ANY USE OF THE SOFTWARE, EVEN IF THE AUTHOR HAVE
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * -----------------------------------------------------
 * $Id$
 */

package com.oncecorp.visa3d.bridge.configure;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Clob;

import javax.naming.Context;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.oncecorp.visa3d.bridge.utility.XMLUtils;
import com.oncecorp.visa3d.bridge.startup.StartupProxy;

import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

/**
 * <p>Title: DatabaseHandler</p>
 * <p>Description: Provides database related functions for configuration manager</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class DatabaseHandler
{
    private final static String DATA_SOURCE_NAME = "bridgeConfig";

    private final static String LOAD_SQL = " select configXml from #####";
    private final static String UPDDATE_SQL = " update ##### set configXml=?";
    private final static String INSERT_SQL = " insert into ##### (configXml) values(?)";

    private final static String TABLE_TOKEN = "#####";

    private String m_dsName = null;

    private String tableName = null;

    private static Logger m_logger = DataBridgeLoger.getLogger(
            DatabaseHandler.class.getName() );

    /**
     * Default constructor
     */
    public DatabaseHandler()
    {
    }

    /**
     * Load the configuration data from the database
     * @return - the XML DOM document
     */
    public Document load()
    {
        m_logger.debug("Enter load");
        Context ctx = null;
        try {
            ctx = StartupProxy.getContext();
            DataSource ds       = (DataSource) ctx.lookup ( getDataSourceName() );
            Connection conn     = ds.getConnection();

            PreparedStatement stmt = conn.prepareStatement( replaceSQL(LOAD_SQL) );
            ResultSet rs = stmt.executeQuery();

            if ( rs != null && rs.next() )
            {
                InputStream is = rs.getClob(1).getAsciiStream();

                m_logger.debug("Before reading database");
                return XMLUtils.getDocument( new InputSource( is ) );
//                return XMLUtils.getDocumentFromString( rs.getString(1) );
            }

            stmt.close();
            conn.close();
        }  catch (Exception e)
        {
            m_logger.error("During loading configuration from database.", e);
        }
        finally {
             try {ctx.close();}
             catch (Exception e) {  }
        }

        m_logger.error("Exit load.");
        return null;
    }

    /**
     * Save the configuration data to the database
     * @param xml - XML configuration string
     */
    public void save( String xml )
    {

        m_logger.debug("Enter save");
        Context ctx = null;
        try {
            ctx = StartupProxy.getContext();
            DataSource ds       = (DataSource) ctx.lookup ( getDataSourceName() );
            Connection conn     = ds.getConnection();

            PreparedStatement stmt = conn.prepareStatement( replaceSQL(LOAD_SQL) );
            ResultSet rs = stmt.executeQuery();

            PreparedStatement upst;

            if ( rs != null && rs.next() )
            {
                upst = conn.prepareStatement( replaceSQL(UPDDATE_SQL) );
            }
            else
            {
                upst = conn.prepareStatement( replaceSQL(INSERT_SQL) );
            }
            if ( rs != null )
                rs.close();
            stmt.close();

            ByteArrayInputStream bais = new ByteArrayInputStream( xml.getBytes() );
            upst.setAsciiStream(1, bais, xml.length() );

            int num = upst.executeUpdate();
            if ( num == 1 )
                m_logger.debug("Save successfully.");
            else
                m_logger.debug("Save failed with return [" + num + "]");

            upst.close();

            conn.close();
        }  catch (Exception e)
        {
            m_logger.error("During saving configuration from database.");
        }
        finally {
             try {ctx.close();}
             catch (Exception e) {  }
        }

        m_logger.error("Exit save.");
    }

    /**
     * Set the data source name
     * @param name - a data source name
     */
    public void setDataSourceName( String name )
    {
        m_dsName = name;
    }

    /**
     *
     * @return - the data source name
     */
    public String getDataSourceName()
    {
        if ( m_dsName == null || m_dsName.trim().equals("") )
            return DATA_SOURCE_NAME;
        else
            return m_dsName;
    }

    /**
     *
     * @return the String value of tableName.
     */
    public String getTableName(){
        if ( tableName == null || tableName.trim().equals("") )
            return "bridge_config";
        return tableName;
    }

    /**
     *
     * @param aTableName - the new value for tableName
     */
    public void setTableName(String aTableName){
        tableName = aTableName;
    }

    /**
     * Replace the given string's token with the new string. It will search and
     * replace all the occurence of the "#####".
     *
     * @param instr - the given string
     * @return - the string that have replaced the special token
     */
    private String replaceSQL( String instr )
    {
        if ( instr == null )
            return instr;

        String rstr = getTableName();

        StringBuffer strb = new StringBuffer();

        int sb = 0, se = 0;

        while ( true )
        {
            se = instr.indexOf( TABLE_TOKEN, sb );
            if ( se == -1 )
            {
                strb.append( instr.substring(sb) );
                break;
            }
            else
            {
                strb.append( instr.substring(sb, se) );
                strb.append( rstr );
                sb = se + TABLE_TOKEN.length();
            }
        }

        return strb.toString();
    }

}