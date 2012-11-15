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

package com.oncecorp.visa3d.bridge.startup;

import java.util.Hashtable;
import javax.naming.Context;
import javax.sql.DataSource;

import com.oncecorp.visa3d.bridge.utility.JdbcUtils;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

/**
 * <p>Title: StartupProxy </p>
 * <p>Description: A proxy for dedicated application server related functions</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com)
 * @version 1.0
 */

public class StartupProxy
{
	public final static String  MERCHANT_JDBC = "merchant.dao.jdbc.driver";
	public final static String  MERCHANT_URL = "merchant.dao.db.url";
	public final static String  MERCHANT_USER_NAME = "merchant.dao.db.username";
	public final static String  MERCHANT_PASSWD = "merchant.dao.db.password";
	public final static String  MERCHANT_SCHEMA = "merchant.dao.db.schema";
	public final static String  MERCHANT_TABLE = "merchant.dao.db.table";
	public final static String  MERCHANT_TABLE_DEFAULT = "merchant";

    protected  static boolean serverContext = true;
    protected  static Hashtable initParameters = null;
    protected  static IRegisterDataSoruce dataSourceHandler = null;

	protected  static DataSource  m_merchantDataSource = null;
	protected  static boolean m_merchantSetFlag = false;

    /**
     * Default constructor
     */
    public StartupProxy()
    {
    }

    /**
     *
     * @return true if serverContext is set to true.
     */
    public static boolean isServerContext(){
        return serverContext;
    }

    /**
     *
     * @param aServerContext - the new value for serverContext
     */
    public static void setServerContext(boolean aServerContext){
        serverContext = aServerContext;
    }


    /**
     *
     * @return the Hashtable value of initParameters.
     */
    public static Hashtable getInitParameters(){
        return initParameters;
    }

    /**
     *
     * @param aInitParameters - the new value for initParameters
     */
    public static void setInitParameters(Hashtable aInitParameters){
        initParameters = aInitParameters;
    }

    /**
    *
    * @return the IRegisterDataSoruce value of dataSourceHandler.
    */
    public static IRegisterDataSoruce getDataSourceHandler(){
        return dataSourceHandler;
    }

    /**
    *
    * @param aDataSourceHandler - the new value for dataSourceHandler
    */
    public static void setDataSourceHandler(IRegisterDataSoruce aDataSourceHandler){
        dataSourceHandler = aDataSourceHandler;
    }

    /**
     *
     * @return - whether current application server support data source registered
     */
    public static boolean canRegisterDataSource()
    {
        return ( dataSourceHandler != null );
    }

    /**
     * Create and register a new data source
     * @param cpJNDIName - data source JNDI name
     * @param driverName - JDBC driver name
     * @param url - JDBC url
     * @param user - database user
     * @param passwd - database user's password
     * @throws Exception
     */
    public static void createDataSource( String cpJNDIName, String driverName,
                                  String url, String user,
                                  String passwd) throws Exception
    {
        if ( canRegisterDataSource() )
            dataSourceHandler.createDataSource( cpJNDIName, driverName,
                             url, user, passwd );
    }


    /**
     * Remove the registered data source
     * @param name - data source JNDI name
     */
    public static void removeDataSource( String name )
    {
        if ( canRegisterDataSource() )
            dataSourceHandler.removeDataSource( name );
    }

    /**
     * Get initial JNDI context
     * @return - JNDI context
     */
    public static Context getContext()
    {
        return JndiContextProxy.getContext();
    }

	/**
	 * Close all of the pool database connections
	 */
	public void finalize()
	{
		JdbcUtils.closeDataSource( m_merchantDataSource );
	}


	/**
	 * Use JNDI context first, if it's not there, find from the
	 * initparameters and use common connection pool to setup
	 * data source
	 * @param jndi - merchant JNDI name
	 * @return - merchant datasource
	 */
	synchronized public static DataSource getMerchantDataSource( String jndi )
	{
		DataBridgeLoger.getLogger( StartupProxy.class ).debug(
				"Enter StartupProxy:getMerchantDataSource()" );

		if ( m_merchantSetFlag  )
	         return m_merchantDataSource;

		m_merchantDataSource =  getJndiDataSource( jndi );

		if ( m_merchantDataSource == null )
		{
		     m_merchantDataSource = JdbcUtils.createPoolingDataSoruce(
					 (String)initParameters.get(MERCHANT_JDBC),
					 (String)initParameters.get(MERCHANT_URL),
					 (String)initParameters.get(MERCHANT_USER_NAME),
					 (String)initParameters.get(MERCHANT_PASSWD),
					  5, 5 );
		}

		m_merchantSetFlag = true;

		DataBridgeLoger.getLogger( StartupProxy.class ).debug(
				"Exit StartupProxy:getMerchantDataSource() with "
				+ m_merchantDataSource );

		return m_merchantDataSource;
	}

	/**
	 * Use JNDI context first, if it's not there, find from the
	 * initparameters and use common connection pool to setup
	 * data source
	 * @param jndi - merchant JNDI name
	 * @return - merchant datasource
	 */
	public static DataSource getJndiDataSource( String jndi )
	{
		DataSource rds = null;

		try {
			rds =  (DataSource)(getContext().lookup(jndi));
		} catch ( Exception e )
		{
			rds = null;
			DataBridgeLoger.getLogger( StartupProxy.class ).debug(
					"Get Jndi data source " + jndi + " not defined in server context.");
		}

		return rds;
	}

	/**
	 * Use JNDI context first, if it's not there, use driver class
	 * @param driverClass The JDBC driver class name.
	 * @param jdbcURL The database URL.
	 * @param jdbcUID The database user id.
	 * @param jdbcPW The database password.
	 * @param maxConns maximum connections
	 * @param timeOut  connection timeout
	 * @return - merchant datasource
	 */
	public static DataSource getGenericDataSource( String jndi,
		String driverClass, String jdbcURL, String jdbcUID, String jdbcPW	)
	{
		DataBridgeLoger.getLogger( StartupProxy.class ).debug(
				"Enter StartupProxy:getGenericDataSource()" );

		DataSource rds = getJndiDataSource( jndi );

		if ( rds == null )
		{
			 rds = JdbcUtils.createPoolingDataSoruce(
					 driverClass, jdbcURL, jdbcUID, jdbcPW, 1, 5 );
		}

		DataBridgeLoger.getLogger( StartupProxy.class ).debug(
				"Exit StartupProxy:getGenericDataSource() with "
				+ rds.toString() );

		return rds;
	}

	/**
	 *
	 * @return - the table name from the init parameters or default one if not set
	 */
	public static String getTableName()
	{
		if ( initParameters.containsKey( MERCHANT_TABLE ) )
	        return (String)initParameters.get( MERCHANT_TABLE );
        else
		    return MERCHANT_TABLE_DEFAULT;
	}

}