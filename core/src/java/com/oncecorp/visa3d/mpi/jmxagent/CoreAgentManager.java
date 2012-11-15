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

package com.oncecorp.visa3d.mpi.jmxagent;

import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.logging.MPILogger;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx4j.adaptor.rmi.jrmp.JRMPAdaptor;
import mx4j.server.MBeanServerImpl;
import mx4j.tools.naming.NamingService;

import org.apache.log4j.Logger;

/**
 * This class is responsible for creating MBeanServer and registering
 * all the identified Mbeans on the MBeanServer in ONCEmpi Core.
 *
 * @author Jun Shi (jshi@oncecorp.com)
 * @author Martin Dufort (mdufort@oncecorp.com)
 * @version $Revision: 33 $
 */
public class CoreAgentManager {

	// Varible Definition
	private static Logger logger = MPILogger.getLogger(CoreAgentManager.class.getName());
	private static MBeanServer server;

	/**
	 * Convenient method to initialize the Jmx Agent when the Core Server starts, main
	 * steps includes:
	 * [1] load configurable data
	 * [2] create MBeanServer with JRMPAdaptor started
	 * [3] register all the identified MBeans
	 *
	 * @param none
	 * @return void
	 */
	public static synchronized void initAgent() throws CoreAgentException {
		// creating once
		if (server == null) {
			try {
				// Make the MBean as the Compliant one
				// in case its class and interface in different package
				System.setProperty("mx4j.strict.mbean.interface", "no");

				// create MBean Server
				server = CoreAgentManager.createMBServer();

				// register MBeans
				CoreAgentManager.registerMBeans(server);
				logger.info("JMX Core Component Service properly started");
			}
			catch (Exception e) {
				logger.debug("Expection while starting JMX Core Component Service", e);
				throw new CoreAgentException(e);
			}
		}
	}

	/**
	 * Create MBeanServer
	 *
	 * @param cfgData: HashMap
	 * @return server: MBeanServer
	 */
	private static MBeanServer createMBServer() throws CoreAgentException {
		try {
			/*
				* [Martin's Note: 6-May-03 11:04:57 AM]
				* Using MBeanServerImpl directly instead of factory
				* to force usage of Mx4J in the WebLogic environment. If we
				* use the factory, the default SUN JMX will be loaded and
				* that will cause conflict with our mbeans
				*
				* // server = MBeanServerFactory.createMBeanServer();
				*/
			server = (MBeanServer) (new MBeanServerImpl("MBServer:CoreDomain"));
			logger.debug("[MBServer:CoreDomain] created");

			/* SSL:
			           // init SSL connection in server side
			           ObjectName ssl = new ObjectName("Adaptor:service=SSLServerSocketFactory");
			           server.createMBean("mx4j.adaptor.ssl.SSLAdaptorServerSocketFactory", ssl, null);
			           SSLAdaptorServerSocketFactoryMBean factory = (SSLAdaptorServerSocketFactoryMBean)
			           StandardMBeanProxy.create(SSLAdaptorServerSocketFactoryMBean.class, server, ssl);
			           factory.setKeyStoreName("com/oncecorp/visa3d/mpi/jmxagent/agentstore");
			           factory.setKeyStorePassword("changeit");
			           factory.setKeyManagerPassword("changeit");
			*/

			// get the specified port number from config data
			String url = (String)Config.getConfigReference().getConfigData(MPIConfigDefinition.CORE_RMIREG_PROVIDER_URL);
			int port = Integer.parseInt(url.substring(url.lastIndexOf(":") + 1));
			logger.debug("RMI Registry Provider URL = " + url + " port: " + port);

			// Create and start the naming service
			startRegistry(port);

			// Create the JRMP adaptor
			ObjectName adaptorObjName = new ObjectName("CoreAdaptor:protocol=JRMP");
			JRMPAdaptor jrmpTransport = new JRMPAdaptor();

			// Retrieve all the properties we need to start our JMX agent object
			String jrmpAdaptor = (String)Config.getConfigReference().getConfigData(MPIConfigDefinition.JRMP_ADAPTOR_JNDI);
			String contextFactory = (String)Config.getConfigReference().getConfigData(MPIConfigDefinition.INIT_CONTEXT_FACTORY);
			String providerURL = (String)Config.getConfigReference().getConfigData(MPIConfigDefinition.CORE_RMIREG_PROVIDER_URL);

			logger.debug((jrmpAdaptor == null) ? "unspecified adaptor name" : "adaptor = " + jrmpAdaptor);
			logger.debug((contextFactory == null) ? "unspecified context factory" : "Factory = " + contextFactory);
			logger.debug((providerURL == null) ? "unspecified provider URL" : "providelURL = " + providerURL);

			/**
			 * [Gang Wu's Note: June 13, 2003] Check and unbind the jndi name if
			 * it's already there.
			 */
			Properties props = new Properties();
			props.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory );
			props.put(Context.PROVIDER_URL, providerURL );
			checkAndUnbindName(props, jrmpAdaptor );

			// Set the JNDI name with which will be registered and specify for the JNDI properties
			jrmpTransport.setJNDIName(jrmpAdaptor);
			jrmpTransport.putJNDIProperty(Context.PROVIDER_URL, providerURL);
			jrmpTransport.putJNDIProperty(Context.INITIAL_CONTEXT_FACTORY, contextFactory);

			// Set the SSL ServerSocket Factory
			// SSL:    mbean.setSSLFactory(ssl.toString());
			//         logger.debug("SSL Initialization Starting ....");

			// Register our JRMP adaptor
			server.registerMBean(jrmpTransport, adaptorObjName);
			logger.debug("Registered jrmpTransport as an MBean in the MBeanServer");

			// and start it

			jrmpTransport.start();
			logger.debug("MBServer:CoreDomain started with JRMPAdaptor Using Core Naming Service.");

			// return MBserver referrence
			return server;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new CoreAgentException(e);
		}
	}

	/**
	 * Starting a MBean RMI Registry component with the Core Server. If is possible
	 * that this action will failed if any RMI registry (i.e. a central one) is
	 * already running. In that case, we will use it directly.
	 *
	 * @param port		Port to start registry on
	 */
	private static void startRegistry(int port) {
		try {
			ObjectName naming = new ObjectName("Naming:type=rmiregistry");
			NamingService namingRef = new NamingService();
			namingRef.setPort(port);
			namingRef.start();

			server.registerMBean(namingRef, naming);
			server.invoke(naming, "start", null, null);
			logger.debug("Core: Naming Service started");
		}
		catch (Exception e) {
			// Unable to start the registry, already one running, using that instance instead !!!!
			logger.debug("Another naming service is already running, using it instead. Message: " + e.getMessage());
		}
	}

	/**
	 * register MBeans
	 *
	 * @param  server: MBeanServer
	 * @param cfgData: HashMap
	 * @return   void
	 */
	private static void registerMBeans(MBeanServer server) throws CoreAgentException {
		try {
			// get all the key entries from cfgData
			Map map = Config.getConfigReference().getConfigDataWithPrefix("Core:mbean");
			Iterator it = map.keySet().iterator();

			// selecting mbean entries and registering it on MBeanServer
			while (it.hasNext()) {
				// get a key entry
				String key = "Core:mbean" + (String)it.next();
				String clzName = (String)Config.getConfigReference().getConfigData(key);

				// register an MBean
				server.registerMBean(Class.forName(clzName).newInstance(), new ObjectName(key));
				// do logging
				logger.debug("[" + key + "] is registered");
			}

			// do logging
			logger.debug("Core: MBeans registration finished");
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new CoreAgentException(e);
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
