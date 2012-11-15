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

import weblogic.common.T3StartupDef;
import weblogic.common.T3ServicesDef;

import org.apache.log4j.Logger;

import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.configure.ConfigurationManager;

import java.util.Hashtable;

/**
 * <p>Title: WeblogicStartup</p>
 * <p>Description: Register the databridge component into Weblogic start up processing.</p>
 * The initial parameters for Weblogic is:
 * <ul>
 * <li>fromFile: if configuration from data source, value=<code>false</code>; otherwise value=<code>true</code>
 * <li>fileName: if configuration from file, set value=<code>configuration file absolute path</code>
 * <li>dbsource: if configuration from data source, set value = <code>data source JNDI name</code>
 * <li>tableName: data base table that contain configuration data. default is <code>bridge_config</code>
 * <li>serverName: databridge running server name.
 * </ul>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class WeblogicStartup implements T3StartupDef
{
    private T3ServicesDef m_services;

    /**
     * Default constructor
     */
    public WeblogicStartup()
    {
    }

    /**
     * Startup function called by Weblogic application server.
     * @param name - startup instance name
     * @param args - the properties defined in the startup deployment
     * @return - the register result
     * @throws Exception
     */
    public String startup( String name, Hashtable args ) throws Exception
    {
        DataBridgeLoger.getLogger( this.getClass() ).debug( "Start databridge component.");
        StartupProxy.setServerContext( true );
        String serverName = "";
        if ( args.containsKey("serverName") )
            serverName = (String) args.get("serverName");
        ConfigurationManager.getInstance().initialize( args );
        WeblogicDataSourceManager wdsm = new WeblogicDataSourceManager( serverName );
        StartupProxy.setDataSourceHandler( wdsm );
        DataBridgeLoger.getLogger( this.getClass() ).debug( "Databridge component is ready for business.");
        return "ok";
    }

    /**
     * Set service method
     * @param services - Weblogic service
     */
    public void setServices(T3ServicesDef services) {
        this.m_services = services;
    }

}