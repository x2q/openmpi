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

package com.oncecorp.visa3d.mpi.configuration;

/**
 * Description: This interface is a common definition mapping of the configuration values
 * needed to properly adapt the behavior of the Core Server
 *
 * @version $Revision: 41 $
 * @author	Jun Shi
 */
public interface MPIConfigDefinition {
	/*
	 * [Alan's note - Feb 23, 2003]
	 * Added below two types to support both VISA 3D-Secure and MasterCard SecureCode
	 * protocol
	 */
	final String PROTOCOL_VISA_3D_SECURE 			= "VISA 3D-Secure";
	final String PROTOCOL_MASTERCARD_SECURE_CODE 	= "MasterCard SecureCode";

	/**
	 * Key for VISA directory URL
	 */
	final String VISA_DIR_URL_1 = "VisaDirectoryURL.1";
	final String VISA_DIR_URL_2 = "VisaDirectoryURL.2";
	final String VISA_DIR_URL_3 = "VisaDirectoryURL.3";

	/**
	 * Key for MasterCard directory URL
	 */
	final String MCard_DIR_URL_1 = "MCardDirectoryURL.1";
	final String MCard_DIR_URL_2 = "MCardDirectoryURL.2";
	final String MCard_DIR_URL_3 = "MCardDirectoryURL.3";

	/**
	 * Key for Card range cache expired time
	 */
	final String CR_CACHE_EXPIRED = "CardRangeCacheExpiryTime";

	/**
	 * Configuration item that indicates if we should support card ranges
	 * This is a configurable item per authentication protocol
	 */
	final String SECURECODE_RANGE_SUPPORT 	= "SecureCodeRangeSupport";
	final String VBV_RANGE_SUPPORT			= "VerifiedByVisaRangeSupport";

	/**
	 * TEODORA: Configuration items that indicate startegy
	 * for accessing the Visa/Master card directory -
	 * the number of retries and the time between retries.
	 */
	final String NUMBER_OF_RETRIES 		= "NumberOfRetriesForDirectory";
	final String TIME_BETWEEN_RETRIES 		= "TimeBetweenRetriesForDirectory";

	/**
	 * CAVV and XID Formatting option configuration parameter
	 * This parameter could have the following values:
	 * 	- Base64
	 *  - Binary
	 *  - Binhex
	 *  - AsciiHex
	 */
	final String CAVV_FORMATTING 			= "CAVVFormattingOption";
	final String XID_FORMATTING			= "XIDFormattingOption";

	/**
	 * Key for default merchant purchase currency code
	 */
	final String MERCHANT_PURCHASE_CURRENCY = "MerchantPurchaseCurrency";

	/**
	 * Key for https proxy host
	 */
	final String HTTP_PROXY_HOST = "https.proxyHost";

	/**
	 * Key for https proxy port
	 */
	final String HTTP_PROXY_PORT = "https.proxyPort";

	/** Mapping For MPI Configurable DataSet Name */
	final String MERCHANT_ID_CARDACCEPTORID = "CardAcceptorId";
	final String MERCHANT_ID_CARDACCEPTORTERMINALID = "CardAcceptorTerminalId";

	/**
	 * Key for System property: java.protocol.handler.pkgs
	 */
	final String HTTPS_PROTOCOL_HANDLER = "HttpsProtocolHandler";

	/**
	 * Key for Security provider
	 */
	final String HTTPS_SECURITY_PROVIDER = "JSSEProvider";

	/**
	 * Key for SSL client keystore
	 */
	final String SSL_CLIENT_KEYSTORE = "SSLClientKeystore";

	/**
	 * Key for SSL client keystore password
	 */
	final String SSL_CLIENT_KEYSTORE_PW = "SSLClientKeystorePassword";

	/**
	 * Key for SSL Truststore
	 */
	final String SSL_TRUSTSTORE = "SSLTrustKeystore";

	/**
	 * Key for SSL truststore password
	 */
	final String SSL_TRUSTSTORE_PW = "SSLTrustKeystorePassword";

	/**
	 * Key for Https proxy host
	 */
	final String HTTPS_PROXY_HOST = "HttpsProxyHost";

	/**
	 * Key for Https proxy port
	 */
	final String HTTPS_PROXY_PORT = "HttpsProxyPort";

	/**
	 * Key for VEReq/PAReq message expiry time period
	 */
	final String CACHE_EXPIRY_INTERVAL = "TransactionCacheExpiryInterval";

	/**
	 * Key for CR Cache expiry interval
	 */
	final String CR_MONITOR_SLEEP_INTERVAL = "CardRangeMonitorSleepInterval";

	/**
	 * Key for WaitUntilFirstRequest
	 */
	final String WAIT_UNTIL_FIRST_REQUEST = "InitCRMonitorUntilFirstRequest";

	/**
	 * Key for ErrorDispatchMode
	 */
	final String ERROR_DISPATCH_MODE = "ErrorDispatchMode";

	/**
	 * Key for ErrorThreadSleepInterval
	 */
	final String ErrorThreadSleepInterval = "ErrorThreadSleepInterval";

	/**
	 * Key for JMS connection factory JNDI name
	 */
	final String JMS_CONNECTION_FACTORY_JNDI = "JMSConnectionFactoryJndi";

	/**
	 * Key for JMS Topic JNDI name
	 */
	final String JMS_TOPIC_JNDI = "JMSTopicJndi";

	/**
	 * Key for JMS message encryption mode
	 */
	final String JMS_ENCRYPTION_MODE = "JMSEncryptionMode";

	/**
	 * Key for JMS Naming Service mode
	 */
	final String JMS_NAMING_SERVICE_DEFAULT_MODE = "JMSNamingServiceDefaultMode";

	/**
	 * Key for JMS Initial Context Factory
	 */
	final String JMS_INITIAL_CONTEXT_FACTORY = "JMSInitialContextFactory";

	/**
	 * Key for JMS Naming Service Provider URL
	 */
	final String JMS_NAMING_SERVICE_PROVIDER_URL = "JMSProviderURL";

	/**
	 * Key for JMS Naming Service Security Principal
	 */
	final String JMS_NAMING_SERVICE_SECURITY_PRINCIPAL = "JMSSecurityPrincipal";

	/**
	 * Key for JMS Naming Service Security Credentials
	 */
	final String JMS_NAMING_SERVICE_SECURITY_CREDENTIALS = "JMSSecurityCredentials";

	/**
	 * Key for Triple-DES keystore location
	 */
	final String TRIPLE_DES_KEYSTORE_LOCATION = "TripleDESKeystoreLocation";

	/**
	 * Key for Triple-DES keystore password
	 */
	final String TRIPLE_DES_KEYSTORE_PWD = "TripleDESKeystorePwd";

	/**
	 * Key for Triple-DES rawkey alias
	 */
	final String TRIPLE_DES_RAWKEY_ALIAS = "TripleDESRawkeyAlias";

	/**
	 * Key for Triple-DES rawkey password
	 */
	final String TRIPLE_DES_RAWKEY_PWD = "TripleDESRawkeyPwd";

	/**
	 * Key for JCE provider
	 */
	final String JCE_PROVIDER = "JCEProvider";

	/**
	 * Key for Performance monitor sampling time
	 */
	final String PERFOMANCE_MONITOR_SAMPLING_TIME = "PerformanceMetricsSamplingTime";

    /**
	 * Key for Jrmp Adaptor MBean JNDI Name
	 */
	final String JRMP_ADAPTOR_JNDI     = "JrmpAdaptorJNDI";

	/**
	 * Key for Initial Context Factory clazz name
	 */
	final String INIT_CONTEXT_FACTORY = "InitContextFactory";

	/**
	 * Key for rmiregistry provider URL for core
	 */
	final String CORE_RMIREG_PROVIDER_URL  = "CoreRmiRegProviderUrl";

	/**
	 * Key for rmiregistry provider URL for bridge
	 */
	final String BRIDGE_RMIREG_PROVIDER_URL  = "BridgeRmiRegProviderUrl";

	/**
	 * Key for CoreServerURL
	 */
	final String CORE_SERVER_URL = "CoreServerURL";
}