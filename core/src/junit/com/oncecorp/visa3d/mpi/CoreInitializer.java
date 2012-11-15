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

package com.oncecorp.visa3d.mpi;

import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.ConfigurationException;
import com.oncecorp.visa3d.mpi.configuration.MerchantMetaInfo;
import com.oncecorp.visa3d.mpi.logging.MPILogger;

/**
 * MPI Core Server initializer for JUnit test suit.
 * 
 * @author Alan Zhang
 * @version $Revision: 3 $
 */
public class CoreInitializer {

	private static HashMap coreConfig;
	private static HashMap merchantConfig;
	private static Properties log4jConfig;

	private static boolean initialized;

	static {
		// init core config HashMap
		coreConfig = new HashMap();
		coreConfig.put("CardRangeCacheExpiryTime", "14400");
		coreConfig.put("CardRangeMonitorSleepInterval", "14400");
		coreConfig.put(
			"Core:mbean=Configurator",
			"com.oncecorp.visa3d.mpi.configuration.Configurator");
		coreConfig.put(
			"Core:mbean=ExecutionControl",
			"com.oncecorp.visa3d.mpi.controller.ExecutionControl");
		coreConfig.put(
			"Core:mbean=PerformanceMonitor",
			"com.oncecorp.visa3d.mpi.messaging.PerformanceMonitor");
		coreConfig.put("ErrorDispatchMode", "Direct");
		coreConfig.put("ErrorThreadSleepInterval", "5000");
		coreConfig.put(
			"HttpsProtocolHandler",
			"com.sun.net.ssl.internal.www.protocol");
		coreConfig.put("InitCRMonitorUntilFirstRequest", "false");
		coreConfig.put(
			"InitContextFactory",
			"com.sun.jndi.rmi.registry.RegistryContextFactory");
		coreConfig.put("JCEProvider", "com.sun.crypto.provider.SunJCE");
		coreConfig.put("JMS.weblogic.jndi.createIntermediateContexts", "true");
		coreConfig.put("JMSConnectionFactoryJndi", "databridge-tcf");
		coreConfig.put("JMSEncryptionMode", "true");
		coreConfig.put("JMSProviderURL", "t3://localhost:7001");
		coreConfig.put("JMSSecurityCredentials", "oncecorp");
		coreConfig.put("JMSSecurityPrincipal", "system");
		coreConfig.put("JMSTopicJndi", "databridge-topic");
		coreConfig.put("JSSEProvider", "com.sun.net.ssl.internal.ssl.Provider");
		coreConfig.put("JrmpAdaptorJNDI", "Core:JRMPAdaptor");
		coreConfig.put("MerchantPurchaseCurrency", "124");
		coreConfig.put("PerformanceMetricsSamplingTime", "60");
		coreConfig.put("RmiRegProviderUrl", "rmi://lego.oncecorp.com:1099");
		coreConfig.put(
			"SSLClientKeystore",
			"/opt/WebSphere/AppServer/etc/mpi.jks");
		coreConfig.put("SSLClientKeystorePassword", "changeit");
		coreConfig.put("TransactionCacheExpiryInterval", "5");
		coreConfig.put(
			"TripleDESKeystoreLocation",
			"/home/azhang/release/DESede.keystore");
		coreConfig.put("TripleDESKeystorePwd", "changeit");
		coreConfig.put("TripleDESRawkeyAlias", "testkey");
		coreConfig.put("TripleDESRawkeyPwd", "changeit");
		coreConfig.put(
			"VisaDirectoryURL",
			"http://192.168.0.151:7800/LoadServer");

		// init log4j config HashMap
		log4jConfig = new Properties();
		log4jConfig.put(
			"log4j.appender.stdout",
			"org.apache.log4j.ConsoleAppender");
		log4jConfig.put(
			"log4j.appender.stdout.layout",
			"org.apache.log4j.PatternLayout");
		log4jConfig.put(
			"log4j.appender.stdout.layout.ConversionPattern",
			"%-6r[%10.10t] %d %-5p %40.40c %x - %m\n");
		log4jConfig.put(
			"log4j.logger.com.oncecorp.visa3d.mpi.messaging",
			"DEBUG");
		log4jConfig.put("log4j.rootLogger", "DEBUG,stdout");

		// init merchant config HashMap
		merchantConfig = new HashMap();
		MerchantMetaInfo mmi = new MerchantMetaInfo();
		mmi.setAcquirerBIN("11111111111");
		mmi.setMerchantCountryCode("124");
		mmi.setMerchantID("12AB,cd/34-EF -g,5/H-67");
		mmi.setMerchantName("Generic Merchant");
		mmi.setMerchantPassword("12345678");
		mmi.setMerchantPurchaseCurrency("124");
		mmi.setMerchantURL("http;//merchant.com");
		merchantConfig.put(mmi.getMerchantID(), mmi);
	}

	public static boolean init() {

		// #todo# Look to see why the initializer is not in the JUnit package
		
//		if (!initialized) {
//			try {
//				// init Log4j first
//				Logger.getLogger(CoreInitializer.class.getName());
//				MPILogger.reconfig(log4jConfig);
//				MPILogger.setConfigured(true);
//
//				// init core & merchant config
//				Config.getConfigReference().setCoreConfigData(coreConfig);
//				Config.getConfigReference().setMerchantConfigData(merchantConfig);
//				// Config.setConfig(new Config());
//			} catch (ConfigurationException e) {
//				System.out.println(
//					"CoreInitializer had problem to init Core Server.");
//				e.printStackTrace();
//				return false;
//			}
//		}

		return true;
	}

}
