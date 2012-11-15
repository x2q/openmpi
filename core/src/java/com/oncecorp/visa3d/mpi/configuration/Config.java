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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This class implements Singleton pattern and can be called by 
 * any other entities. It will load configurable data from database or 
 * file and store it appropriately.
 * 
 * @author Alan Zhang
 * @author Martin Dufort (mdufort@onceocorp.com)
 * @version $Revision: 22 $
 */
public final class Config {

	/* ---------  Static definition --------------- */
	// Singleton instance
	private static Config config = null;

	// Local log4j logger
	// private static Logger logger = MPILogger.getLogger(Config.class.getName());

	/* --------- Member attributes -------------- */
	// Core configuration data. 
	// HashMap containing: key 	 = configuration name
	//					   value = CoreConfigInfo
	private HashMap coreConfigData = null;

	// Merchant configuration data. 
	// HashMap containing: key 	 = merchantID
	//					   value = MerchantMetaInfo
	private HashMap merchantConfigData = null;

	// Log4J configuration data
	private Properties log4jConfigData = null;
	
	/* ------------ Method definitions ------------ */

	/**  
	 * Default construction for Config object
	 */
	private Config() {
		super();
	}

	/**
	 * Return a reference to the global configuration object 
	 */
	public synchronized static Config getConfigReference() {
		// Check if the configuration object has already been created
		if (config == null) {
			// No then create a new configuration object and initialize it.
			config = new Config();
		}

		// Return singleton object to caller
		return config;
	}

	/**
	 * Reload the configuration information from the available repositories
	 */
	public static void reload() throws ConfigurationException {
		// Force reload on the next access
		getConfigReference().merchantConfigData 	= null;
		getConfigReference().coreConfigData 		= null;
		getConfigReference().log4jConfigData 		= null;
	}

	/** 
	 * Load core config data
	 * 
	 * @param 	String (ConfigDataName: could be a dataName or a dataSetName)
	 * @return 	Serializable           (could be a String or a HashMap)
	 */
	public Serializable getConfigData(String ConfigDataName)
		throws ConfigurationException {

		// Return a String for data or an HashMap for dataSet
		CoreConfigInfo cfg = (CoreConfigInfo) getCoreConfigData().get(ConfigDataName);
		
		// Return the value of that configuration element if it exists
		if (cfg != null) {			
			return cfg.getValue();
		}
		else {
			return null;
		}
	}

	/**
	 * Retrieve config data with given prefix
	 */
	public Map getConfigDataWithPrefix(String prefix) throws ConfigurationException {
		if (prefix == null)
			return null;

		Map map = new Hashtable();
		Iterator iterator = getCoreConfigData().keySet().iterator();
		String key = null;
		while (iterator.hasNext()) {
			key = (String)iterator.next();
			if (key.startsWith(prefix)) {
				map.put(key.substring(prefix.length()), getConfigData(key));
			}
		}

		return map;
	}

	/**
	 * Get merchant meta info
	 * @param id The merchant ID
	 * @return The MerchantMetaInfo
	 */
	public MerchantMetaInfo getMerchantMetaInfo(String id)
		throws ConfigurationException {


		if (getMerchantConfigData().get(id) != null)
			return (MerchantMetaInfo)getMerchantConfigData().get(id);
		else
			return null;
	}

	/**
	 * Get first merchant in MerchantMetaInfo hashmap for Card Range Monitor that
	 * supports the supplied protocol type.
	 * 
	 * @param aProtocolType	Protocol type the merchant needs to support
	 * @return The merchantID
	 */
	public String getFirstMerchantID(String aProtocolType)
		throws ConfigurationException {
		byte checkProtocol = 0;
		String foundID = null;

		// Retrieve proper byte to check for protocol
		if (aProtocolType.equalsIgnoreCase(MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE)) {
			checkProtocol = MerchantMetaInfo.MERCHANT_SUPPORT_VBV;
		}
		else if (aProtocolType.equalsIgnoreCase(MPIConfigDefinition.PROTOCOL_MASTERCARD_SECURE_CODE)) {
			checkProtocol = MerchantMetaInfo.MERCHANT_SUPPORT_SECURECODE;
		}

		Set set = getMerchantConfigData().keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			String key = (String)it.next();
			// Retrieve the merchant information
			MerchantMetaInfo element = (MerchantMetaInfo)getMerchantConfigData().get(key);

			// Test if the merchant supports the necessary protocol 
			if ((element.getProtocolSupport() & checkProtocol) != 0) {
				foundID = element.getMerchantID();
			}
		}

		return foundID;
	}

	/**
	 * Returns the merchantConfigData.
	 * @return HashMap	HashMap of all the MerchantConfiguration instances
	 */
	public HashMap getMerchantConfigData() throws ConfigurationException {
		if (merchantConfigData == null) {
			// Retrieve the proper accessor for the merchant and load the configuration
			ConfigAccessor accessor = ConfigAccessorGenerator.getAccessor(ConfigAccessorType.ACCESSOR_TYPE_MERCHANT);
			merchantConfigData = (HashMap)accessor.getConfigData();
		}
		
		return config.merchantConfigData;
	}

	/**
	 * Returns the coreConfigData.
	 * @return HashMap
	 */
	public HashMap getCoreConfigData() throws ConfigurationException {
		if (coreConfigData == null) {
			// Retrieve the proper accessor for the MPI and load the configuration
			ConfigAccessor accessor = ConfigAccessorGenerator.getAccessor(ConfigAccessorType.ACCESSOR_TYPE_MPI);
			coreConfigData = (HashMap)accessor.getConfigData();
		}
		
		return config.coreConfigData;
	}
	
	/**
	 * Enables the storage of the Core Configuration data
	 * 
	 * @return
	 */
	public boolean setCoreConfigData(HashMap data) throws ConfigurationException {
		// Retrieve the proper accessor for the MPI and load the configuration
		ConfigAccessor accessor = ConfigAccessorGenerator.getAccessor(ConfigAccessorType.ACCESSOR_TYPE_MPI);
		return accessor.setConfigData(data);
	}

	/**
	 * Enables the storage of the Log4J Configuration data
	 * 
	 * @return
	 */
	public boolean setLog4jConfigData(Properties props) throws ConfigurationException {
		// Retrieve the proper accessor for the MPI and load the configuration
		ConfigAccessor accessor = ConfigAccessorGenerator.getAccessor(ConfigAccessorType.ACCESSOR_TYPE_LOGGING);
		return accessor.setConfigData(props);
	}

	/**
	 * Enables the storage of the Merchant Configuration data
	 * 
	 * @return Execution indicator for storage operation
	 */
	public boolean setMerchantConfigData(HashMap data) throws ConfigurationException {
		// Retrieve the proper accessor for the MPI and load the configuration
		ConfigAccessor accessor = ConfigAccessorGenerator.getAccessor(ConfigAccessorType.ACCESSOR_TYPE_MERCHANT);
		return accessor.setConfigData(data);
	}

	/**
	 * Enables the storage of the Merchant Configuration data to a specific location
	 * 
	 * @return Execution indicator for storage operation
	 */
	public boolean setMerchantConfigData(HashMap data, byte location) throws ConfigurationException {
		// Retrieve the proper accessor for the MPI and load the configuration
		MerchantConfigAccessor accessor = (MerchantConfigAccessor) ConfigAccessorGenerator.getAccessor(ConfigAccessorType.ACCESSOR_TYPE_MERCHANT);
		accessor.overrideLocation(location);
		return accessor.setConfigData(data);
	}

	/**
	 * Returns the config.
	 * @return 			Config
	 * @deprecated		Please directly use the getConfigReference() method instead
	 */
	public static Config getConfig() {
		return config;
	}

	/**
	 * Sets the config.
	 * @param 		config The config to set
	 * @deprecated	This setter should never be called externally
	 */
	public static void setConfig(Config config) {
		Config.config = config;
	}

	/**
	 * Returns a set of URL
	 * @param type The protocol type, either "VISA 3D-Secure" or "MasterCard SecureCode"
	 * @return The URLs of directory
	 */
	public String[] getDirectoryURLs(String type) throws ConfigurationException {
		// sanity check
		if (type == null)
			return null;

		String[] urls = new String[3];
		// checks type
		if (type.equals(MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE)) {
			urls[0] = (String)getConfigData(MPIConfigDefinition.VISA_DIR_URL_1);
			urls[1] = (String)getConfigData(MPIConfigDefinition.VISA_DIR_URL_2);
			urls[2] = (String)getConfigData(MPIConfigDefinition.VISA_DIR_URL_3);
			return urls;
		}
		else if (type.equals(MPIConfigDefinition.PROTOCOL_MASTERCARD_SECURE_CODE)) {
			urls[0] = (String)getConfigData(MPIConfigDefinition.MCard_DIR_URL_1);
			urls[1] = (String)getConfigData(MPIConfigDefinition.MCard_DIR_URL_2);
			urls[2] = (String)getConfigData(MPIConfigDefinition.MCard_DIR_URL_3);
			return urls;
		}
		else {
			return null;
		}
	}

	/**
	 * Returns the log4jConfigData.
	 * @return Properties
	 */
	public Properties getLog4jConfigData() {
		if (log4jConfigData == null) {
			// Retrieve the proper accessor for the MPI and load the configuration
			ConfigAccessor accessor = ConfigAccessorGenerator.getAccessor(ConfigAccessorType.ACCESSOR_TYPE_LOGGING);
			log4jConfigData = (Properties)accessor.getConfigData();
		}

		// Return a String for data or an HashMap for dataSet 
		return log4jConfigData;
	}

}