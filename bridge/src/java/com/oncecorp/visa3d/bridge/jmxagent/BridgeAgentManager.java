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

package com.oncecorp.visa3d.bridge.jmxagent;

import java.util.Map;
import java.util.Iterator;
import java.util.Properties;

import javax.naming.Context;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx4j.server.MBeanServerImpl;
import mx4j.util.StandardMBeanProxy;
import mx4j.tools.naming.NamingService;
import mx4j.adaptor.rmi.jrmp.JRMPAdaptor;
import mx4j.adaptor.rmi.jrmp.JRMPAdaptorMBean;
import mx4j.adaptor.ssl.SSLAdaptorServerSocketFactoryMBean;

import org.apache.log4j.Logger;

import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

/**
 * <p>Title: BridgeAgentManager </p>
 * <p>Description: This class is responsible for creating
 *                 MBeanServer and registering all the
 *                 identified Mbeans on it in ONCEmpi Bridge
 *                 side. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Jun Shi ( jshi@oncecorp.com)
 * @version 1.0
 */
public class BridgeAgentManager {

    // Keys for Jrmp Adaptor MBean JNDI Name and Properties
	final static String JRMP_ADAPTOR_JNDI               = "JrmpAdaptorJNDI";
	final static String INIT_CONTEXT_FACTORY            = "InitContextFactory";
	final static String BRIDGE_RMIREG_PROVIDER_URL      = "BridgeRmiRegProviderUrl";
	final static String CORE_RMIREG_PROVIDER_URL        = "CoreRmiRegProviderUrl";

    // Varible Definition
    private static Logger logger = DataBridgeLoger.getLogger(
                                 BridgeAgentManager.class.getName());
    private static MBeanServer server = null;

    /**
	 * Convenient method to initialize the Jmx Agent when the Bridge
	 * Server starts, main steps includes:
	 * [1] create MBeanServer with JRMPAdaptor started
	 * [2] register all the identified MBeans
	 *
	 * @param cfgData: HashMap
	 * @return void
	 */
    public static synchronized void initAgent(Map cfgData)
        throws BridgeAgentException {
        logger.debug("Passed in cfgData = ["+cfgData+"]");

		// creating once
		if (server == null) {
		   try {
		       // Make the MBean as the Compliant one
		       // in case its class and interface in different package
		       System.setProperty("mx4j.strict.mbean.interface","no");

		       // create MBean Server
		       server = BridgeAgentManager.createMBServer(cfgData);

		       // register MBeans
               BridgeAgentManager.registerMBeans(server, cfgData);
		   } catch(Exception e) {
		       throw new BridgeAgentException(e);
		   }
		} else {
               logger.debug("Note: BridgeAgent already existing");
		}
    }

    /**
     * Create MBeanServer
     *
     * @param cfgData: Map
	 * @return server: MBeanServer
     */
    private static MBeanServer createMBServer(Map cfgData)
       throws BridgeAgentException {
       try {
           // Create a MBeanServer
           server = (MBeanServer)(new MBeanServerImpl("MBServer:BridgeDomain"));
           logger.debug("[MBServer:BridgeDomain] created");

/* SSL:
           // init SSL connection in server side
           ObjectName ssl = new ObjectName("Adaptor:service=SSLServerSocketFactory");
           server.createMBean("mx4j.adaptor.ssl.SSLAdaptorServerSocketFactory", ssl, null);
           SSLAdaptorServerSocketFactoryMBean factory = (SSLAdaptorServerSocketFactoryMBean)
           StandardMBeanProxy.create(SSLAdaptorServerSocketFactoryMBean.class, server, ssl);
           factory.setKeyStoreName("com/oncecorp/visa3d/bridge/jmxagent/agentstore");
           factory.setKeyStorePassword("changeit");
           factory.setKeyManagerPassword("changeit");
*/
           // get the specified port number from config data
           String url = (String) cfgData.get(BRIDGE_RMIREG_PROVIDER_URL);
           int port = Integer.parseInt(url.substring(url.lastIndexOf(":")+1));
           //logger.debug("RMI Registry Provider URL = ["+url+"]");

           // Create and start the naming service
           try {
               ObjectName naming = new ObjectName("Naming:type=rmiregistry");
               NamingService namingRef = new NamingService();
               namingRef.setPort(port);
               server.registerMBean(namingRef, naming);
               server.invoke(naming, "start", null, null);
               logger.debug("Bridge: Naming Service started");
           } catch (MBeanException me) {
               // logging debug info
               logger.debug("Core Naming Service may already exist...", me);

			   try {
				   // Create the JRMP adaptor
				   ObjectName adaptorObjName = new ObjectName("BridgeAdaptor:protocol=JRMP");
				   server.createMBean("mx4j.adaptor.rmi.jrmp.JRMPAdaptor", adaptorObjName, null);
				   logger.debug("JRMPAdaptor Mbean registered");

				   // build a proxy of this JRMPAdaptorMB
				   JRMPAdaptorMBean mbean = (JRMPAdaptorMBean) StandardMBeanProxy.create(
						   JRMPAdaptorMBean.class, server, adaptorObjName);

				   /**
					* [Gang Wu's Note: June 13, 2003] Check and unbind the jndi name if
					* it's already there.
					*/
				   Properties props = new Properties();
				   props.put(Context.INITIAL_CONTEXT_FACTORY,
							 (String)cfgData.get(INIT_CONTEXT_FACTORY));
				   props.put(Context.PROVIDER_URL,
							 (String)cfgData.get(CORE_RMIREG_PROVIDER_URL));
				   checkAndUnbindName(props,
									  (String)cfgData.get(JRMP_ADAPTOR_JNDI));

				   // Set the JNDI name with which will be registered and specify the JNDI properties,
				   mbean.setJNDIName((String)cfgData.get(JRMP_ADAPTOR_JNDI));
				   mbean.putJNDIProperty(Context.INITIAL_CONTEXT_FACTORY,
						   (String)cfgData.get(INIT_CONTEXT_FACTORY));
				   /** Note: here use core naming service url instead as it already exist */
				   mbean.putJNDIProperty(Context.PROVIDER_URL,
				   (String)cfgData.get(CORE_RMIREG_PROVIDER_URL));

				   // Set the SSL ServerSocket Factory
// SSL:        mbean.setSSLFactory(ssl.toString());
//             logger.debug("MBeanServer SSL Initialization Starting ....");

				   // Registers the JRMP adaptor in JNDI and starts it
				   mbean.start();
				   logger.debug("[MBServer:BridgeDomain] started with JRMPAdaptor Using Core Naming Service"
								+"\n*Naming Service Start At: "
		                        +(String)cfgData.get(CORE_RMIREG_PROVIDER_URL));
			   } catch ( Exception ex )
			   {
				   logger.error("Error happened during setup adaptor MBean, "
								+ "please make sure there is not another bridge instance running "
		                        + " with the same JMX RMI Adaptor name:"
						  + cfgData.get(JRMP_ADAPTOR_JNDI) );
				   throw new BridgeAgentException(ex);
			   }

               // return MBserver referrence
               return server;
           }

           // Create the JRMP adaptor
           ObjectName adaptorObjName = new ObjectName("BridgeAdaptor:protocol=JRMP");
           server.createMBean("mx4j.adaptor.rmi.jrmp.JRMPAdaptor", adaptorObjName, null);
           logger.debug("JRMPAdaptor Mbean registered");

           // build a proxy of this JRMPAdaptorMB
           JRMPAdaptorMBean mbean = (JRMPAdaptorMBean) StandardMBeanProxy.create(
                                         JRMPAdaptorMBean.class, server, adaptorObjName);

           // Set the JNDI name with which will be registered and specify the JNDI properties,
           mbean.setJNDIName((String)cfgData.get(JRMP_ADAPTOR_JNDI));
           mbean.putJNDIProperty(Context.INITIAL_CONTEXT_FACTORY,
                             (String)cfgData.get(INIT_CONTEXT_FACTORY));
           mbean.putJNDIProperty(Context.PROVIDER_URL,
                             (String)cfgData.get(BRIDGE_RMIREG_PROVIDER_URL));

           // Set the SSL ServerSocket Factory
// SSL:    mbean.setSSLFactory(ssl.toString());
//         logger.debug("Local MBeanServer SSL Initialization Starting ....");

           // Registers the JRMP adaptor in JNDI and starts it
           mbean.start();
           logger.debug("[MBServer:BridgeDomain] started with JRMPAdaptor Using Bridge Naming Service"
                        +"\n*Naming Service Start At: "
                        +(String)cfgData.get(BRIDGE_RMIREG_PROVIDER_URL));

           // return MBserver referrence
           return server;
       } catch (Exception e) {
           e.printStackTrace();
           logger.error("Exception occured in createMBServer(cfgData) method", e);
           throw new BridgeAgentException(e);
       }
    }

    /**
     * register MBeans
     *
     * @param  server: MBeanServer
     * @param cfgData: HashMap
	 * @return   void
     */
    private static void registerMBeans(MBeanServer server, Map cfgData)
        throws BridgeAgentException {
            try {
                // get all the key entries from cfgData
                Iterator iterator = cfgData.keySet().iterator();

                // selecting mbean entries and registering it on MBeanServer
                while (iterator.hasNext()) {
                   // get a key entry
                   String key = (String) iterator.next();

                   if (key.length()>11 && key.substring(0,12).equals("Bridge:mbean")) {
                       // retrieve objName and clzName
                       String objName = key;
                       String clzName = (String) cfgData.get(key);

                       // register an MBean in server
                       server.createMBean(clzName, new ObjectName(objName), null);
                       logger.debug("["+objName+"] is registered");
                   }
               }

               // do logging
               logger.debug("Bridge: MBeans registration finished");
      } catch (Exception e) {
          logger.error(
          "Exception occured in registerMBeans(server,cfgData) method", e);
          throw new BridgeAgentException(e);
      }
    }

	/**
	 * Check and unbind the given JNDI name in rmi regersity
	 * @param properties - initial context parameters
	 * @param jndiName - the jndi name
	 */
	public static void checkAndUnbindName( Properties properties, String jndiName )
	{
		try {
			InitialContext context = null;
			if (properties.size() != 0)
			{
				context = new InitialContext(properties);
			}
			else
			{
				context = new InitialContext();
			}

			Object obj = null;
			try
			{
				obj = context.lookup( jndiName );
			} catch ( NamingException e )
			{
				obj = null;
			}

			if ( obj != null )
			{
				logger.debug("JNDI name [" + jndiName + "] is already registered, unbind it.");
				// Unbind the given name
				context.unbind(jndiName);
			}
			else
			{
				logger.debug("JNDI name [" + jndiName + "] is not registered yet.");
			}

		} catch ( NamingException ne )
		{
			logger.debug("Exception happending during checkAndUnbindName with JNDI name ["
						 + jndiName + "].", ne);
		}

	}

}
