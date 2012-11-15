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
import com.oncecorp.visa3d.mpi.persistence.ConfigDAO;
import com.oncecorp.visa3d.mpi.persistence.PersistentException;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of the ConfigAccessor interface to properly load and
 * save the configuration data associated with the MPI configuration type.
 *
 * @version $Revision: 35 $
 * @author 	Martin Dufort (mdufort@oncecorp.com)
 */
public class MPIConfigAccessor implements ConfigAccessor {

	// local Log4J logger
	private Logger logger = MPILogger.getLogger(MPIConfigAccessor.class.getName());

	// Constant Definitions
	private static final String MPI_LOCATION_PROPERTY = "coreConfigLocation";
	private static final String MPI_CONFIGNAME_PROPERTY = "coreConfigName";
	private static final String DATA_XMLPATH = "/coreConfiguration/configurationItem";
	private static final String DATA_NAME_XMLATTR = "name";
	private static final String DATA_VALUE_XMLATTR = "value";
	private static final String DATA_DESC_XMLATTR = "description";
	private static final String DATA_UDESC_XMLATTR = "unitDescription";

	// Constant for loading location
	private static final byte FROM_DAO = 1;
	private static final byte FROM_FILE = 2;

	// Loading location
	private static byte loadingLocation;

	/**
	 * Return the configuration data, associated to this accessor, as a Serializable item
	 *
	 * @return 	Configuration data
	 */
	public Serializable getConfigData() {
		return loadCoreConfig();
	}

	/**
	 * Load the configuration from either the DAO or the file for this configuration
	 * item
	 *
	 * @return Merchant Configuration information
	 */
	private HashMap loadCoreConfig() {
		HashMap coreConfigData = null;

		//get from DAO
		try {
			coreConfigData = getFromDAO();
			logger.debug("Core config data loaded from DAO.");
			loadingLocation = FROM_DAO;
			return coreConfigData;
		}
		catch (PersistentException pe) {
			logger.warn("Failed to load Core configuration from DAO. Now trying to load from file...");
		}

		try {
			//get from MPIConfig.xml file
			if (coreConfigData == null) {
				coreConfigData = getFromFile();
				loadingLocation = FROM_FILE;
			}
		}
		catch (PersistentException e) {
			logger.error("Loading of MPI properties failed (no file or access denied): ", e);
		}

		// return to caller
		return coreConfigData;
	}

	/**
	 * Load Core config data from DAO
	 */
	private HashMap getFromDAO() throws PersistentException {
		ConfigDAO dao = new ConfigDAO();
		Vector data = (Vector) dao.retrieve(null);

		// Retrieve information from database and construct the
		// appropriate HashMap needed to store
		HashMap coreConfigData = new HashMap();
		for (int i = 0; i < data.size(); i++) {
			CoreConfigInfo info = (CoreConfigInfo) data.get(i);
			coreConfigData.put(info.getName(), info);
		}

		return coreConfigData;
	}

	/**
	 * Load Core config data from XML file
	 * @return The HashMap
	 */
	private HashMap getFromFile() throws PersistentException {
		// Initialize mapping repository
		HashMap coreConfigData = new HashMap();
		FileInputStream fis = null;
		String configFileName = "";

		// Loading MPIConfig.xml from the supplied location
		String location = System.getProperty(MPI_LOCATION_PROPERTY);
		String filename = System.getProperty(MPI_CONFIGNAME_PROPERTY);
		if (location == null || filename == null) {
			throw new PersistentException("Configuration properties not properly defined");
		}

		try {
			configFileName = location + filename;
			fis = new FileInputStream(configFileName);
		}
		catch (FileNotFoundException fnfExcep) {
			// The configuration file is not found. Nothing much we can do.
			logger.fatal(
				"Unable to retrieve the core configuration file ["
	            + configFileName + "]. The file does not exist.",
	            fnfExcep );
			throw new RuntimeException("Invalid configuration exception");
		}


		if (fis == null) {
			logger.debug("Unable to find " + configFileName);
		}
		else {
			logger.debug(configFileName + " is loaded properly");
		}

		// Initialize Core Component configuration from the XML Document
		coreConfigData = initFromXML(fis);

		// Properly close the file inputstream
		try {
			if (fis != null) fis.close();
		}
		catch (Exception e) {}

		logger.debug("MPI config data loaded from file.");
		return coreConfigData;
	}

	/**
	 * Initialize the Core Configuration from the XML document.
	 * Return a populated HashMap with the configuration retrieved
	 * from this parsing activity.
	 *
	 * @param coreConfigData
	 * @param fis
	 */
	private HashMap initFromXML(FileInputStream fis) {
		HashMap coreConfigData = new HashMap();

		// Create DOM object for [MessagingConfig.xml]
		Element root = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(fis);

			// do logging
			logger.debug("Building DOM of MPIConfig.xml file completed!!");

			// get root element
			root = doc.getDocumentElement();
		}
		catch (Exception e) {
			logger.error("loading of MPIConfig.xml file failed!!", e);
			e.printStackTrace();
		}

		// loading "data" from the XML configuration file
		NodeList nl = XMLUtil.getNodeListByXPath(root, DATA_XMLPATH);
		if (nl == null || nl.getLength() == 0) {
			logger.error("No configuration information specified in the specified file");
		}
		else {
			// Create the configuration elements
			Element current;
			for (int i = 0; i < nl.getLength(); i++) {
				current = (Element) nl.item(i);

				// For each configuration element found, we create a corresponfing
				// CoreConfigInfo instance and we populate it accordingly
				CoreConfigInfo info = new CoreConfigInfo();
				info.setName(current.getAttribute(DATA_NAME_XMLATTR));

				// Retrieve the other values defined as sub-elements
				NodeList childNodes = current.getChildNodes();
				for (int j = 0; j < childNodes.getLength(); j++) {
					Node n = childNodes.item(j);

					if (n.getNodeName().equalsIgnoreCase(DATA_VALUE_XMLATTR)) {
						Node n2 = n.getFirstChild();
						if (n2 != null) info.setValue(n2.getNodeValue()); else info.setValue("");
					}

					if (n.getNodeName().equalsIgnoreCase(DATA_DESC_XMLATTR)) {
						Node n2 = n.getFirstChild();
						if (n2 != null) info.setDescription(n2.getNodeValue()); else info.setDescription("");
					}

					if (n.getNodeName().equalsIgnoreCase(DATA_UDESC_XMLATTR)) {
						Node n2 = n.getFirstChild();
						if (n2 != null) info.setUnitDescription(n2.getNodeValue()); else info.setUnitDescription("");
					}
				}

				coreConfigData.put(info.getName(), info);
			}
		}

		return coreConfigData;
	}

	/**
	 * Set Core config data through DAO
	 * @param The HashMap of config data
	 */
	public boolean setConfigData(Serializable obj) {
		//Check data type
		if (!(obj instanceof HashMap)) {
			logger.error("Unknown data type. Failed to save Core config data.");
			return false;
		}

		if (loadingLocation == FROM_DAO) {
			return saveToDB(obj);
		}
		else {
			return saveToFile(obj);
		}

	}

	/**
	 * Save the core configuration to the database in the CORECONFIG table
	 * @param obj
	 * @return
	 */
	private boolean saveToDB(Serializable obj) {

		ConfigDAO dao = new ConfigDAO();
		Vector data = new Vector();
		Vector inkeys = new Vector();

		/*
		 * [Martin's Note: 22-Apr-03 12:43:14 PM]
		 *
		 * This update strategy is NOT very good because if an error occurs after
		 * the deletion of all the Core Config information, then we are left
		 * with an empty database and possibly with an unusable installation....
		 *
		 * We NEED TO correct this...
		 * [Gang's Note: 27-May-03]
		 * Compare the save data with the database current data and decide to
		 * update, add or delete the items.
		 */
		// Delete all current Core Config data entries
		//dao.delete(null);

		// Transform CoreConfig hashmap into vector for proper insertion
		HashMap configData = (HashMap) obj;
		Iterator iterator = configData.values().iterator();
		while (iterator.hasNext())
		{
			CoreConfigInfo info = (CoreConfigInfo) iterator.next();
			data.add(info);
			inkeys.add( info.getName() );
		}

		// Insert config data
		//dao.create(data);

		Vector dbData = null;
		try {
			dbData = (Vector) dao.retrieve(null);
		}
		catch (PersistentException pe) {
			logger.error("Failed to retrieve Core properites through DAO.", pe);
			dbData = null;
		}

		Vector addData = new Vector();
		Vector deleteData = new Vector();
		Vector updataData = new Vector();

		if ( dbData == null || dbData.size() < 1 )
		{
			// Now the database contain no item
			try {
				dao.create( data );
			}
			catch (PersistentException pe) {
				logger.error("Failed to add all Core properites through DAO.", pe);
			}
		}
		else
		{
			Vector dbkeys = new Vector();
			CoreConfigInfo info;
			String key;
			for ( Iterator lt = dbData.iterator(); lt.hasNext(); )
			{
				info = (CoreConfigInfo) lt.next();
				key = info.getName();
				if ( !inkeys.contains( key ) )
				{
					// The database item not include in the new config data set
					// Add it to the delete vector
					deleteData.add( info );
				}
				else
				{
					dbkeys.add( key );
				}
			}

			for ( Iterator lt = data.iterator(); lt.hasNext(); )
			{
				info = (CoreConfigInfo) lt.next();
				if ( dbkeys.contains( info.getName() ) )
				{
					// Update the database item
					updataData.add( info );
				}
				else
				{
					// New item that need to be added
					addData.add( info );
				}
			}

			if ( updataData.size() > 0 )
			{
				try {
					dao.update( updataData );
					logger.debug("Core config data updated "
								 + updataData.size() + " items.");
				}
				catch (PersistentException pe) {
					logger.error("Failed to update core config data.", pe);
				}
			}

			if ( addData.size() > 0 )
			{
				try {
					dao.create( addData );
					logger.debug("Core config data add "
								 + addData.size() + " items.");
				}
				catch (PersistentException pe) {
					logger.error("Failed to add core config data.", pe);
				}
			}

			if ( deleteData.size() > 0 )
			{
				try {
					dao.delete( deleteData );
					logger.debug("Core config data delete "
								 + deleteData.size() + " items.");
				}
				catch (PersistentException pe) {
					logger.error("Failed to delete core config data.", pe);
				}
			}
		}

		logger.debug("Core config data saveTODB finished.");
		return true;

	}

	/**
	 * Save the core configuration to the database in the CORECONFIG table
	 * @param obj
	 * @return
	 */
	private boolean saveToFile(Serializable obj) {
		// #todo# Need to implement the file persistence for Core Config
		return false;
	}
}