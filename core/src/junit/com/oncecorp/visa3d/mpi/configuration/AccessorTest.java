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

import com.oncecorp.visa3d.mpi.utility.JUnitHelper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit for validation of proper retrieval of configuration information from
 * Accessor Component
 *
 * @version $Revision: 4 $
 * @author 	Martin Dufort (mdufort@oncecorp.com)
 */
public class AccessorTest extends TestCase {
	// Constructor
	public AccessorTest(String name) {
		super(name);
	}

	protected void setup() throws Exception {
		//		boolean initOK = CoreInitializer.init();
		//		if (!initOK) {
		//			System.out.println("Init Core Server failed.");
		//			fail();
		//		}
	}

	/**
	 * Test the retrieval of Logging configuration data from the database
	 */
	public void testLogConfigFromDB() {
		// Setup parameters to retrieve from database
		setupDBParams(true); // true = Use database accessor

		// Check Consistency and proper definition
		checkLogConsistency();
	}

	/**
	 * Test the retrieval of the Logging configuration data from a file
	 */
	public void testLogConfigFromFile() {
		// Setup parameters to retrieve from database
		setupDBParams(false); // true = Use database accessor

		// Check Consistency and proper definition
		checkLogConsistency();
	}

	/**
	 * Test the retrieval of Core Server configuration data from the database
	 */
	public void testCoreConfigFromDB() {
		// Setup parameters to retrieve from database
		setupDBParams(true); // true = Use database accessor

		// Check Consistency and proper definition
		checkCoreConsistency();
	}

	/**
	 * Test the retrieval of Core Server configuration data from the XML file
	 */
	public void testCoreConfigFromFile() {
		// Disable access from database
		setupDBParams(false); // false = Use file accessor

		// Check Consistency and proper definition
		checkCoreConsistency();
	}

	/**
	 * Test the retrieval of Core Server configuration data from the database
	 */
	public void testMerchantConfigFromDB() {
		// Setup parameters to retrieve from database
		setupDBParams(true); // true = Use database accessor

		// Check Consistency and proper definition
		checkMerchantConsistency();
	}

	/**
	 * Test the retrieval of Core Server configuration data from the XML file
	 */
	public void testMerchantConfigFromFile() {
		// Disable access from database
		setupDBParams(false); // false = Use file accessor

		// Check Consistency and proper definition
		checkMerchantConsistency();
	}

	/**
	 * Change some configuration items and ensure they are properly persisted
	 */
	public void testChangeConfigDB() {
		// Change configuration items stored in database
		setupDBParams(true);

		// Retrieve all configuration data, make some changes and verify if everything
		// is working properly
		HashMap cfg = null;
		try {
			cfg = (HashMap)Config.getConfigReference().getCoreConfigData();
			cfg.put("NewElement", "NewValue");

			// Get accessor and store the modified data component
			ConfigAccessor ca = ConfigAccessorGenerator.getAccessor(ConfigAccessorType.ACCESSOR_TYPE_MPI);
			ca.setConfigData(cfg);

			// Now retrieve the configuration and compare the values
			HashMap newCfg = (HashMap) Config.getConfigReference().getCoreConfigData();

			// Ensure new structure as exactly one more element
			assertEquals(cfg.size()+1, newCfg.size());
		}
		catch (ConfigurationException e) {
			fail("Unable to retrieve the Core Component configuration data");
		}

	}

	/**
	 * This unit test the ability of the Merchant DAO to properly stored a set of
	 * properties read from the Merchant Configuration XML file. The second part of
	 * the test reads the Merchant DB and writes to the Merchant Configuration XML file.
	 *
	 */
	public void testStoreMerchant2DB() {
		try {
			// Load configuration data from the XML file
			setupDBParams(false);
			HashMap map = checkMerchantConsistency();

			// Stores it in the database
			setupDBParams(true);
			Config.getConfigReference().setMerchantConfigData(map, MerchantConfigAccessor.FROM_DAO);

			// Reload it and compare with the values loaded from the file
			// to ensure correct integrity
			HashMap newMap = Config.getConfigReference().getMerchantConfigData();

			// Ensure both location has the same number of elements
			assertEquals(map.size(), newMap.size());
		}
		catch (ConfigurationException e) {
			// #todo# Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testStoreMerchant2File() {

	}

	/**
	 * Check the consistency of the configuration parameters
	 */
	private void checkCoreConsistency() {
		// Retrieve configuration from Database and ensure consistency
		try {
			HashMap configData = Config.getConfigReference().getCoreConfigData();
			Iterator it = configData.keySet().iterator();
			while (it.hasNext()) {
				String key = (String)it.next();
				CoreConfigInfo element = (CoreConfigInfo)configData.get(key);

				// Ensure that the key and configuration name are the same
				if (!key.equalsIgnoreCase(element.getName())) {
					fail("Inconsistent definition. Key must be equal to Configuration Item name for " + key);
				}

				// Ensure that all elements contains a value as well
				if (element.getValue() == null || element.getValue().length() == 0) {
					fail("Value of configuration parameter " + key + " is either null or blank");
				}
			}
		}
		catch (ClassCastException ccExcep) {
			ccExcep.printStackTrace();
			fail("Entries in Core Configuration are not of 'CoreConfigInfo' type");
		}
		catch (ConfigurationException e) {
			e.printStackTrace();
			fail("Configuration exception while retrieving configuration data");
		}
	}

	/**
	 * Check the consistency of the configuration parameters
	 */
	private void checkLogConsistency() {
		// Retrieve configuration from Database and ensure consistency
		Properties props = Config.getConfigReference().getLog4jConfigData();
		if (props == null || props.size() == 0) {
			fail("Log4J properties not correctly specified");
		}
	}

	/**
	 * Check the consistency of the configuration parameters
	 * @return  The loaded Merchant Information map
	 */
	private HashMap checkMerchantConsistency() {
		HashMap map = null;
		try {
			// Retrieve configuration from Database and ensure consistency
			map = Config.getConfigReference().getMerchantConfigData();
			if (map == null || map.size() == 0) {
				fail("Merchant Configuration data not correctly specified");
				return null;
			}
		}
		catch (ConfigurationException e) {
			e.printStackTrace();
			fail("Configuration exception while retrieving configuration data");
		}

		return map;
	}

	/**
	 * Setup the proper database parameters to unit test the configuration from
	 * a valid database instance
	 */
	private void setupDBParams(boolean useDb) {
		Properties props = JUnitHelper.getPropertiesFromFile("UnitTest.properties");
		if (!useDb) {
			JUnitHelper.initFromFile();
		}
		else {
			JUnitHelper.initFromDB();
		}

	}

	/**
	 * Test suite definition
	 * @return	Test suite for the configuration package
	 */
	public static Test suite() {
		return new TestSuite("Configuration Suite");
		/**
		 * [Gang Wu's note: 31 May, 2003] This test suit is temporary masked, as
		 * it's dangerous to update the configuration data. There is a bug to update
		 * merchant database as plain password store to the database.
		 */
		/**
		TestSuite suite = new TestSuite("Configuration Suite");

		// Test Log4j configuration accessor
		suite.addTest(new AccessorTest("testLogConfigFromDB"));
		suite.addTest(new AccessorTest("testLogConfigFromFile"));

		// Test Core Configuration accessor
		suite.addTest(new AccessorTest("testCoreConfigFromDB"));
		suite.addTest(new AccessorTest("testCoreConfigFromFile"));
		suite.addTest(new AccessorTest("testChangeConfigDB"));

		// Test Merchant configuration accessor
		suite.addTest(new AccessorTest("testMerchantConfigFromFile"));
		suite.addTest(new AccessorTest("testMerchantConfigFromDB"));
		suite.addTest(new AccessorTest("testStoreMerchant2DB"));
		suite.addTest(new AccessorTest("testStoreMerchant2File"));
		return suite;
		*/
	}
}
