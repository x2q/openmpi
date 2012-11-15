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

import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.persistence.MerchantDAO;
import com.oncecorp.visa3d.mpi.persistence.PersistentException;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;
import com.oncecorp.visa3d.mpi.security.MPIEncrypter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Defines the accessor that allows the retrieval of merchant configuration information.
 * This accessor first check if the data is avaiable from a configured database. If not
 * then a preconfigured filename and location will be tried.
 *
 */
public class MerchantConfigAccessor implements ConfigAccessor {
	// local Log4J logger
	private Logger logger = MPILogger.getLogger(MerchantConfigAccessor.class.getName());

	// Constant Definitions
	private static final String MERCHANT_LOCATION_PROPERTY		= "merchantConfigLocation";
	private static final String MERCHANT_CONFIG_NAME			= "merchantConfigName";
	private static final String DATA_XMLPATH 					= "/merchantConfiguration/merchantInfo/";
	private static final String DATA_ID_XMLATTR					= "id";
	private static final String DATA_NAME_XMLATTR	 			= "name";
	private static final String DATA_PASSWORD_XMLATTR 			= "password";
	private static final String DATA_MERCHANTURL_XMLATTR		= "merchantURL";
	private static final String DATA_COUNTRYCODE_XMLATTR		= "countryCode";
	private static final String DATA_PURCHASECURRENCY_XMLATTR	= "purchaseCurrency";
	private static final String DATA_ACQUIRERBIN_XMLATTR		= "acquirerBin";
	private static final String DATA_PROTOCOLSUPPORT_XMLATTR	= "protocolSupport";
	private static final String DATA_LICENSINGKEY_XMLATTR		= "licensingKey";
	private static final String DATA_KEYEXPIRYDATE_XMLATTR		= "keyExpiryDate";

	// Constant for loading location
	public static final byte FROM_DAO = 1;
	public static final byte FROM_FILE = 2;

	// Loading location
	private static byte loadingLocation;

	/**
	 * Return the configuration data, associated to this accessor, as a Serializable item
	 *
	 * @return 	Configuration data
	 */
	public Serializable getConfigData() {
		return loadMerchantConfig();
	}

	/**
	 * Load the configuration from either the DAO or the file for this configuration
	 * item
	 *
	 * @return Merchant Configuration information
	 */
	private HashMap loadMerchantConfig() {
		HashMap merchantConfigData = null;

		// Get from DAO
		try {
			merchantConfigData = getFromDAO();
			logger.debug("Merchant config data loaded from DAO.");
			loadingLocation = FROM_DAO;
			return merchantConfigData;
		}
		catch (PersistentException pe) {
			logger.error("Failed to load Merchant configuration from DAO. Now trying to load from file...", pe);
		}

		// Get from MPIConfig.xml file
		if (merchantConfigData == null) {
			merchantConfigData = getFromFile();
			loadingLocation = FROM_FILE;
		}

		// Return to caller
		return merchantConfigData;
	}

	/**
	 * Load Core config data from DAO
	 */
	private HashMap getFromDAO() throws PersistentException {
		return (HashMap) new MerchantDAO().retrieve(null);
	}

	/**
	 * Load merchant config data from XML file
	 * @return The HashMap
	 */
	private HashMap getFromFile() {
		// Initialize mapping repository
		HashMap merchantConfigData = new HashMap();
		FileInputStream fis = null;
		String configFileName;
		boolean errorFlag = false;

		// Loading MPIConfig.xml from the supplied location
		try {
			configFileName = System.getProperty(MERCHANT_LOCATION_PROPERTY) + System.getProperty(MERCHANT_CONFIG_NAME);
			fis = new FileInputStream(configFileName);
		}
		catch (FileNotFoundException fnfExcep) {
			// The configuration file is not found. Nothing much we can do.
			logger.fatal("Unable to retrieve the merchant configuration file. The file does not exist.");
			throw new RuntimeException("Invalid configuration exception");
		}

		if (fis == null) {
			logger.debug("Unable to find " + configFileName);
		} else {
			logger.debug(configFileName + " is loaded properly");
		}

		// Create DOM object for MerchantConfig.xml file
		Element root = null;
		try {
			DocumentBuilderFactory factory 	= DocumentBuilderFactory.newInstance();
			DocumentBuilder builder 		= factory.newDocumentBuilder();
			Document doc 					= builder.parse(fis);

			// Do logging
			logger.debug("Building DOM for MerchantConfiguration file completed.");

			// Get root element
			root = doc.getDocumentElement();
		}
		catch (Exception e) {
			logger.error("loading" + configFileName +" failed!!", e);
			e.printStackTrace();
		}

		// Loading "data" from the XML configuration file
		NodeList nl = XMLUtil.getNodeListByXPath(root, DATA_XMLPATH);
		if (nl == null || nl.getLength() == 0) {
			logger.error("Malformed Merchant configuration XML file");
		}

		// Create the configuration elements
		Element current;
		for (int i = 0; i < nl.getLength(); i++) {
			current = (Element) nl.item(i);

			// For each configuration element found, we create a corresponfing
			// CoreConfigInfo instance and we populate it accordingly
			MerchantMetaInfo info = new MerchantMetaInfo();
			info.setMerchantID(current.getAttribute(DATA_ID_XMLATTR));

			// Retrieve the other values defined as sub-elements
			NodeList childNodes = current.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node n = childNodes.item(j);

				if (n.getNodeName().equalsIgnoreCase(DATA_NAME_XMLATTR)) {
					Node n2 = n.getFirstChild();
					if (n2 != null) info.setMerchantName(n2.getNodeValue()); else info.setMerchantName("");
				}

				if (n.getNodeName().equalsIgnoreCase(DATA_PASSWORD_XMLATTR)) {
					Node n2 = n.getFirstChild();
					if (n2 != null)
					{
						String decPwd = "";
						try {
							decPwd = MPIEncrypter.decrypt(n2.getNodeValue());
							// The password is invalid if it is null after decryption or if the value is not 0 or 8
							if ((decPwd == null) ||
								((decPwd.length() != 0) && (decPwd.length() != 8))) {
								logger.error(
									"Merchant password length is not valid. Length="
										+ decPwd.length() + " Value=" + decPwd);
								errorFlag = true;
								decPwd = "";
							}
						}
						catch (Exception e) {
							logger.error("Failed to decrypted merchant's password. " + n2.getNodeValue());
							errorFlag = true;
							decPwd = "";
						}
		                info.setMerchantPassword( decPwd );
					}
	                else
					{
					    info.setMerchantPassword("");
					}
				}

				if (n.getNodeName().equalsIgnoreCase(DATA_MERCHANTURL_XMLATTR)) {
					Node n2 = n.getFirstChild();
					if (n2 != null) info.setMerchantURL(n2.getNodeValue()); else info.setMerchantURL("");
				}

				if (n.getNodeName().equalsIgnoreCase(DATA_COUNTRYCODE_XMLATTR)) {
					Node n2 = n.getFirstChild();
					if (n2 != null) info.setMerchantCountryCode(n2.getNodeValue()); else info.setMerchantCountryCode("");
				}

				if (n.getNodeName().equalsIgnoreCase(DATA_PURCHASECURRENCY_XMLATTR)) {
					Node n2 = n.getFirstChild();
					if (n2 != null) info.setMerchantPurchaseCurrency(n2.getNodeValue()); else info.setMerchantPurchaseCurrency("");
				}

                if (n.getNodeName().equalsIgnoreCase(DATA_PROTOCOLSUPPORT_XMLATTR)) {
                    Node n2 = n.getFirstChild();
                    byte protocolValue = Byte.decode(n2.getNodeValue()).byteValue();
                    // Supports the Visa 3-D Secure protocol by default if not mentionned in the XML document
                    if (n2 != null) info.setProtocolSupport(protocolValue); else info.setProtocolSupport(new Byte("1").byteValue());
                }

				if (n.getNodeName().equalsIgnoreCase(DATA_ACQUIRERBIN_XMLATTR)) {
					Node n2 = n.getFirstChild();
					if (n2 != null) info.setAcquirerBIN(n2.getNodeValue()); else info.setAcquirerBIN("");
				}

				if (n.getNodeName().equalsIgnoreCase(DATA_LICENSINGKEY_XMLATTR)) {
					Node n2 = n.getFirstChild();
					if (n2 != null) info.setLicensingKey(n2.getNodeValue()); else info.setLicensingKey("");
				}

				if (n.getNodeName().equalsIgnoreCase(DATA_KEYEXPIRYDATE_XMLATTR)) {
					Node n2 = n.getFirstChild();
					if (n2 != null) {
						info.setKeyExpiryDate(new Long(n2.getNodeValue()).longValue());
					}
					else {
						info.setKeyExpiryDate(0);
					}
				}

			}

			logger.debug(
				"Merchant retrieved: ["
					+ info.getMerchantID()
					+ ", "
					+ info.getMerchantName()
					+ ", "
					+ info.getMerchantPassword()
					+ ", "
					+ info.getMerchantURL()
					+ ", "
					+ info.getMerchantCountryCode()
					+ ", "
					+ info.getMerchantPurchaseCurrency()
					+ ", "
					+ info.getAcquirerBIN()
					+ ", "
					+ info.getProtocolSupport()
					+ ", "
					+ info.getLicensingKey()
					+ ", "
					+ info.getKeyExpiryDate()
					+ "]");

			if ( !errorFlag )
	              merchantConfigData.put(info.getMerchantID(), info);
		}

		logger.debug("Merchant config data loaded from file.");
		return merchantConfigData;
	}


	/**
	 * Set Core config data through DAO
	 * @param obj HashMap of config data
	 */
	public boolean setConfigData(Serializable obj) {
		//Check data type
		if (!(obj instanceof HashMap)) {
			logger.error("Unknown data type. Failed to save merchant configuration.");
			return false;
		}

		HashMap props = (HashMap)obj;
		if (loadingLocation == FROM_DAO) {
			return storeToDB(props);
		}
		else if (loadingLocation == FROM_FILE) {
			return storeToFile(props);
		}
		else {
			logger.error("Unknown loading Location provided, cannot store the merchant information.");
			return false;
		}
	}

	/**
	 * Store merchant information to the Database using the properly
	 * configured MerchantDAO object
	 *
 	 * @param props
	 * @return
	 */
	private boolean storeToFile(HashMap props) {
		// #todo# Take hashmap object and write it to the proper XML document file

		return false;
	}

	/**
	 * Store merchant information to the Database using the properly
	 * configured MerchantDAO object
	 *
	 * @param 	map		HashMap containing a list of MerchantConfigInfo in the value set
	 * @return	Status of execution
	 */
	private boolean storeToDB(HashMap map) {
		try {
			new MerchantDAO().update(map);
			return true;
		}
		catch (PersistentException pExcep) {
			logger.error("Unable to update the Merchant Configuration information to the Database");
		}
		return false;
	}

	/**
	 * Override loading location to allow a more flexible implementation
	 * of the Accessor
	 */
	public void overrideLocation(byte newLocation) {
		MerchantConfigAccessor.loadingLocation = newLocation;
	}
}
