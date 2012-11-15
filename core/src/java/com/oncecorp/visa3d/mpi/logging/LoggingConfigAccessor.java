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

package com.oncecorp.visa3d.mpi.logging;

import com.oncecorp.visa3d.mpi.configuration.ConfigAccessor;
import com.oncecorp.visa3d.mpi.persistence.Log4jDAO;
import com.oncecorp.visa3d.mpi.persistence.PersistentException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.Vector;

/**
 * Description: The config accessor for Logging Component. The accessor
 * tries to locate local properties file first. It the file not exist, then
 * the accessor looks up ConfigData Entity Bean and try to pull out config
 * data from persistent repository.
 * 
 * 
 * @version 0.1 July 17, 2002
 * @author	Alan Zhang
 */
public class LoggingConfigAccessor implements ConfigAccessor {
	// Constant Definitions
	private static final String LOG4J_LOCATION_PROPERTY   = "log4JConfigLocation";
	private static final String LOG4J_CONFIGNAME_PROPERTY = "log4JConfigName";

	// Constant for loading location
	private static final byte FROM_DAO = 1;
	private static final byte FROM_FILE = 2;
	
	// Loading location
	private static byte loadingLocation;

	/**
	 * Implement ConfigAccessor interface method.
	 * Get MPI logging configuration properties.
	 */
	public Serializable getConfigData() {
		Properties ps = null;

		//Get from DAO
		try {
			ps = getFromDAO();
			System.out.println("Loaded Log4j properties from DAO.");
			loadingLocation = FROM_DAO;
			return ps;
		} catch (PersistentException pe) {
			System.out.println(
				"Failed to load Log4j properties from DAO. Try local file...");
		}

		//Look for local properties file
		try {
			ps = getFromFile();

			//We got all we need.
			System.out.println("Loaded Log4j properties from file.");
			loadingLocation = FROM_FILE;
			return ps;
		} catch (Exception e) {
			System.out.print(
				"Loading of Log4j properties failed (no file or access denied): ");
			System.out.println(e.getMessage());
		}
		return null;
	}

	/**
	 * Load log4j properties from DAO
	 * @return The Properties
	 */
	private Properties getFromDAO() throws PersistentException {
		Log4jDAO dao = new Log4jDAO();
		Properties props = (Properties) dao.retrieve(null);

		return props;
	}

	/**
	 * Convenient method to get properties from local file
	 */
	private Properties getFromFile() throws Exception {
		FileInputStream fis = null;
		String configFileName;

		String location = System.getProperty(LOG4J_LOCATION_PROPERTY);
		String filename = System.getProperty(LOG4J_CONFIGNAME_PROPERTY);
		if (location == null || filename == null) {
			throw new PersistentException("Configuration properties not properly defined");
		} 
		
		
		// Loading Log4jConfig.properties from the supplied location
		try {
			configFileName = location + filename;
			fis = new FileInputStream(configFileName);
		}
		catch (FileNotFoundException fnfExcep) {
			throw new RuntimeException("Invalid configuration exception");
		}

		//Read all properties from file input stream
		Properties props = new Properties();
		props.load(fis);

		fis.close();
		System.out.println("Log4J properties: " + props.toString());
		return props;
	}

	/**
	 * Store Log4J logging properties through DAO
	 * @param The log4j properties
	 */
	public boolean setConfigData(Serializable obj) {
		//Check data type
		if (!(obj instanceof Properties)) {
			System.out.println("Unknown data type. Failed to save log4j properties.");
			return false;
		}

		Properties props = (Properties)obj;
		if (loadingLocation == FROM_DAO) {
			return storeToDB(props);
		}
		else {
			return storeToFile(props);
		}
	}

	/**
	 * @param obj
	 * @return
	 */
	private boolean storeToFile(Properties props) {
		FileOutputStream fos = null;
		try {
			// Get resouce location
			String filename = System.getProperty(LOG4J_LOCATION_PROPERTY) + 
							  System.getProperty(LOG4J_CONFIGNAME_PROPERTY);
			File log4jFile = new File(filename);

			// Delete current file;
			log4jFile.delete();
			
			// Create new file with current configuration
			log4jFile.createNewFile();
	
			// Create output stream
			fos = new FileOutputStream(log4jFile);
		
			// Store properties
			props.store(fos, null);
			
			System.out.println("Log4j config data updated in file.");
		} catch (FileNotFoundException fnfExcep) {
			
		} catch (IOException ioExcep) {

		} finally {
			if (fos != null)
				try {
					fos.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		return true;
	}

	private boolean storeToDB(Properties props) {
		try {
			Log4jDAO dao = new Log4jDAO();
			Vector data = new Vector();
		
			// Delete all current log4j config data entries
			dao.delete(null);
		
			// Create new set of config data
			dao.create(props);

			System.out.println("Log4j config data updated in DB.");
			return true;
		
		} 
		catch (PersistentException pe) {
			System.out.println("Failed to save Log4j properites through DAO.");
			pe.printStackTrace();
			return false;
		}
	}


}