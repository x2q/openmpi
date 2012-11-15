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

package com.oncecorp.visa3d.mpi.security;

import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;

import org.apache.log4j.Logger;
import java.security.Provider;

/**
 * Description: The ConnectionConfiguration class is a convenient class
 * that allows the specification of System.properties information to
 * properly setup the parameters for HTTPS connections.
 *
 * @version 0.1 July 26, 2002
 * @author	Alan Zhang
 */
public class ConnectionConfigurator {
	/**
	 * Local Log4J logger
	 */
	public Logger logger =
		MPILogger.getLogger(ConnectionConfigurator.class.getName());

	/**
	 * Flag
	 */
	public final static boolean SUCCESS = true;
	public final static boolean FAIL = false;

	public boolean setConnectionParameters() {
		try {
			//Get Config reference
			Config config = Config.getConfigReference();

			//Add https protocol handler
			String handler = (String) config.getConfigData(MPIConfigDefinition.HTTPS_PROTOCOL_HANDLER);
			logger.debug("Add handler: " + handler);
			System.setProperty(
				"java.protocol.handler.pkgs",
				handler);

			//Add JSSE provider
			String provider =
				(String) config.getConfigData(MPIConfigDefinition.HTTPS_SECURITY_PROVIDER);
			logger.debug("Add provider: " + provider);
			java.security.Security.addProvider(
				(Provider) Class.forName(provider).newInstance());

//			System.setProperty("javax.net.debug", "all"); // debug


			//Add keystore
			String ks = (String) config.getConfigData(MPIConfigDefinition.SSL_CLIENT_KEYSTORE);
			String kspw = (String) config.getConfigData(MPIConfigDefinition.SSL_CLIENT_KEYSTORE_PW);
			System.setProperty("javax.net.ssl.keyStore", ks);
			System.setProperty("javax.net.ssl.keyStorePassword", kspw);

			//Add truststore
  		    String tks = (String) config.getConfigData(MPIConfigDefinition.SSL_TRUSTSTORE);
		    String tkspw = (String) config.getConfigData(MPIConfigDefinition.SSL_TRUSTSTORE_PW);
			if ( tks != null && !tks.trim().equals("") )
			{
				System.setProperty("javax.net.ssl.trustStore", tks);
				System.setProperty("javax.net.ssl.trustStorePassword", tkspw);
			}

		//If provided, add HTTPS Proxy server properties
			String proxyHost =
				(String) config.getConfigData(MPIConfigDefinition.HTTPS_PROXY_HOST);
			String proxyPort =
				(String) config.getConfigData(MPIConfigDefinition.HTTPS_PROXY_PORT);

			if (proxyHost != null) {
				logger.debug("Set Https proxy host: " + proxyHost);
				System.setProperty("https.proxyHost", proxyHost);
			}

			if (proxyPort != null) {
				logger.debug("Set Https proxy port: " + proxyPort);
				System.setProperty("https.proxyPort", proxyPort);
			}

			//Do logging
			this.logger.info("ConnectionConfigurator setParameters completed.");

			return SUCCESS;
		} catch (Exception e) {
			this.logger.error("Failed to set connection parameter", e);
			return FAIL;
		}
	}

}