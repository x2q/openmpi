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

import java.util.Set;
import java.util.Properties;
import java.util.Iterator;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import weblogic.jndi.Environment;
import weblogic.management.configuration.JDBCConnectionPoolMBean;
import weblogic.management.runtime.JDBCConnectionPoolRuntimeMBean;
import weblogic.management.configuration.JDBCDataSourceMBean;
import weblogic.management.configuration.ServerMBean;
import weblogic.management.MBeanHome;
import weblogic.management.WebLogicObjectName;
import weblogic.management.configuration.TargetMBean;
import weblogic.management.configuration.DeploymentMBean;

import org.apache.log4j.Logger;

import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.auditing.AuditingManager;
//import com.oncecorp.visa3d.bridge.auditing.PropertiesConstants;

/**
 * <p>Title: WeblogicDataSourceManager </p>
 * <p>Description: Use weblogic JMX functionalities to automatically register
 *  databridge needed datasource.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class WeblogicDataSourceManager implements IRegisterDataSoruce//, PropertiesConstants
{
    private static MBeanHome mbeanHome = null;

    private static String serverName = "";
    private static ServerMBean serverMBean = null;

    private static WeblogicDataSourceManager m_instance = null;

    private static TargetMBean m_targets[] = null;

    /**
     * Singleton private constructor
     */
    protected WeblogicDataSourceManager( String sname )
    {
        serverName = sname;
        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug
                ("Enter WeblogicDataSourceManager with server name =[" + sname + "]");
        try {
            Context ctx = new InitialContext();

            // Look Up the Administration MBeanHome
            mbeanHome = (MBeanHome)ctx.lookup(MBeanHome.ADMIN_JNDI_NAME);

            //Get the Server MBean
            serverMBean = (ServerMBean)mbeanHome.getAdminMBean(serverName, "Server");
            /**
             * Modify for move merchant JNDI form hard-code to config file.
             * Modified by YG at 18/02/2003.
             */
            //m_targets = getJDBCDataSourceBeanFromJNDI( MERCHANT_DATABASE_JNDI ).getTargets();
            m_targets = getJDBCDataSourceBeanFromJNDI(
              AuditingManager.getInstance().getBean().getMerchantJNDI()
            ).getTargets();
            /**
             * End of modify.
             */
        } catch ( Exception e )
        {
            DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).error("Fail to get MBean", e);
        }

        m_instance = this;
        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug("Exit WeblogicDataSourceManager");
    }

    /**
     * Get Singleton instance
     * @return - datasource manager instance
     */
    public static WeblogicDataSourceManager getInstance()
    {
        return m_instance;
    }

    /**
     * Get registered connection pool MBean by name
     * @param name - connection pool name
     * @return - the registed connection pool MBean
     */
    private JDBCConnectionPoolMBean getJDBCConnectionPoolBean( String name )
    {
        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Enter getJDBCConnectionPoolBean" );

        Set setAllMBeans = mbeanHome.getMBeansByType("JDBCConnectionPool",
                mbeanHome.getDomainName());
        Iterator allMBeansIterator = setAllMBeans.iterator();

        JDBCConnectionPoolMBean jdbcMBean = null;

        try{
            while( allMBeansIterator.hasNext() ){
                jdbcMBean = (JDBCConnectionPoolMBean)allMBeansIterator.next();
                if( jdbcMBean.getName().equals(name) )
                    return jdbcMBean;
            }
        }catch( ClassCastException cce ){
            DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).error(
                    "Class Cast Exception:", cce );
        }catch( Exception e ){
            DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).error(
                    " Exception:", e );
        }

        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Exit getJDBCConnectionPoolBean" );

        return null;
    }

    /**
     * Get registered data source MBean by name
     * @param name - a data source name
     * @return - the registed data source MBean
     */
    private JDBCDataSourceMBean getJDBCDataSourceBean( String name )
    {
        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Enter getJDBCDataSourceBean");
        Set setAllMBeans = mbeanHome.getMBeansByType("JDBCDataSource",
                mbeanHome.getDomainName());
        Iterator allMBeansIterator = setAllMBeans.iterator();

        JDBCDataSourceMBean jdbcMBean = null;

        try{
            while( allMBeansIterator.hasNext() ){
                jdbcMBean = (JDBCDataSourceMBean)allMBeansIterator.next();
                if( jdbcMBean.getName().equals(name) )
                    return jdbcMBean;
            }
        }catch( ClassCastException cce ){
            DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).error(
                    "Class Cast Exception:", cce );
        }catch( Exception e ){
            DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).error(
                    " Exception:", e );
        }

        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Exit getJDBCDataSourceBean");
        return null;
    }

    /**
     * Get registered data source MBean by  JNDI name
     * @param name - a data source JNDI name
     * @return - the registed data source MBean
     */
    private JDBCDataSourceMBean getJDBCDataSourceBeanFromJNDI( String jndiName )
    {
        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Enter getJDBCDataSourceBean");
        Set setAllMBeans = mbeanHome.getMBeansByType("JDBCDataSource",
                mbeanHome.getDomainName());
        Iterator allMBeansIterator = setAllMBeans.iterator();

        JDBCDataSourceMBean jdbcMBean = null;
        String str;

        try{
            while( allMBeansIterator.hasNext() ){
                jdbcMBean = (JDBCDataSourceMBean)allMBeansIterator.next();
                str = jdbcMBean.getJNDIName();
                if( str != null && str.equals( jndiName ) )
                    return jdbcMBean;
            }
        }catch( ClassCastException cce ){
            DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).error(
                    "Class Cast Exception:", cce );
        }catch( Exception e ){
            DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).error(
                    " Exception:", e );
        }

        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Exit getJDBCDataSourceBean");
        return null;
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
    public void createDataSource( String cpJNDIName,
           String driverName, String url, String user,
           String passwd) throws Exception
    {
        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Enter createDataSource");

        DataBridgeLoger.getLogger( this ).debug("jndi = " + cpJNDIName);
        DataBridgeLoger.getLogger( this ).debug("driver name = " + driverName);
        DataBridgeLoger.getLogger( this ).debug("url = " + url);
        DataBridgeLoger.getLogger( this ).debug("user = " + user);
        DataBridgeLoger.getLogger( this ).debug("password = " + passwd);

        String cpName = cpJNDIName + "CP";

        // Create ConnectionPool MBean
        JDBCConnectionPoolMBean cpMBean = (JDBCConnectionPoolMBean)mbeanHome.createAdminMBean( cpName,
                "JDBCConnectionPool",     mbeanHome.getDomainName());

        //Set the Connection Pool Properties
        Properties pros = new Properties();
        pros.put("user", user);
//        pros.put("server", serverName);
        pros.put("password", passwd);
        if ( url.indexOf("@") != - 1 )
        {
            int bin = url.indexOf("@");
            int ein = url.indexOf(":", bin);
            String str;
            if ( ein == -1 )
                str = url.substring( bin + 1 );
            else
                str = url.substring( bin + 1, ein );
            pros.put("URL", str);
        }
        // Set DataSource attributes
        cpMBean.setURL(url);
        cpMBean.setDriverName(driverName);
        cpMBean.setProperties(pros);
//        cpMBean.setPassword(passwd);
//        cpMBean.setLoginDelaySeconds(1);
        cpMBean.setInitialCapacity(1);
        cpMBean.setMaxCapacity(50);
        cpMBean.setCapacityIncrement(1);
//        cpMBean.setShrinkingEnabled(true);
//        cpMBean.setShrinkPeriodMinutes(10);
//        cpMBean.setRefreshMinutes(0);

        //Add the Target
        addTargets( cpMBean );

        // Create DataSource  MBean
        JDBCDataSourceMBean dsMBeans = (JDBCDataSourceMBean)mbeanHome.createAdminMBean( cpJNDIName + "DS",
                "JDBCDataSource",  mbeanHome.getDomainName());

        // Set DataSource attributes
        dsMBeans.setJNDIName(cpJNDIName);
        dsMBeans.setPoolName(cpName);

        // Startup datasource
        dsMBeans.addTarget(serverMBean);
        //Add the Target
        addTargets( dsMBeans );

        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Exit createDataSource");
    }

    /**
     * Remove the registered data source
     * @param name - data source JNDI name
     */
    public  void removeDataSource( String name )
    {
        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Enter removeDataSource");
        deleteDataSource( name + "DS" );
        deleteConnectionPool( name + "CP" );
        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Exit removeDataSource");
    }

    /**
     * Add all targets to the MBean
     * @param bean - a MBean instance
     */
    private static void addTargets( DeploymentMBean bean ) throws Exception
    {
        //Add the Target
        if ( m_targets == null || m_targets.length < 1 )
            bean.addTarget(serverMBean);
        else
            bean.setTargets( m_targets );
    }

    /**
     * Remove a connection pool by name
     * @param name - connection pool name
     */
    private void deleteConnectionPool( String name )
    {
        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Enter deleteConnectionPool");
        JDBCConnectionPoolMBean cpMBean = getJDBCConnectionPoolBean( name );
        if ( cpMBean == null )
            return;

        try {
            // Remove dynamically created connection pool from the server
            cpMBean.removeTarget(serverMBean);
            // Remove dynamically created connection pool from the configuration
            mbeanHome.deleteMBean(cpMBean);
        } catch (Exception ex) {
            DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                    "Exception:", ex);
        }
        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Exit deleteConnectionPool");
    }

    /**
     * Remove a registered data source by name
     * @param name - data source name
     */
    private void deleteDataSource( String name )
    {
        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Enter deleteDataSource");
        JDBCDataSourceMBean  dsMBeans = getJDBCDataSourceBean( name );
        if ( dsMBeans == null )
            return;
        try {
            // Remove dynamically created datasource from the server
            dsMBeans.removeTarget(serverMBean);
            // Remove dynamically created datasource from the configuration
            mbeanHome.deleteMBean(dsMBeans);
        } catch (Exception ex) {
            DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                    "Exception:", ex);
        }

        DataBridgeLoger.getLogger( WeblogicDataSourceManager.class ).debug(
                "Exit deleteDataSource");
    }

}