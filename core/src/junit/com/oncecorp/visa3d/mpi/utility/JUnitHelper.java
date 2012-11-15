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

package com.oncecorp.visa3d.mpi.utility;

import java.io.FileInputStream;
import java.util.Properties;

import com.oncecorp.visa3d.mpi.persistence.ConfigDAO;
import com.oncecorp.visa3d.mpi.persistence.Log4jDAO;
import com.oncecorp.visa3d.mpi.persistence.MerchantDAO;

/**
 * Helper class for Junit test case
 * <p>Title: JUnitHelper</p>
 * <p>Description: For Test purpose </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
public class JUnitHelper
{

	public JUnitHelper()
	{
	}

	/**
	 * Extract initialize parameters from property file
	 * @param name - property file name
	 * @return - A property list
	 */
	public static Properties getPropertiesFromFile ( String name )
	{
		Properties props = new Properties();
		try {
			props.load( new FileInputStream(name) );
		} catch ( Exception e )
		{
			System.err.println("Unable to retrieve the initialize parameters.");
			e.printStackTrace();
		}

		return props;
	}

	public static void initFromDB()
	{
		Properties props = getPropertiesFromFile("UnitTest.properties");
		// Setup the proper attributes to access the database
		// for the Core Config data
		ConfigDAO.setJdbcDriver( props.getProperty("JdbcDriver") );
		ConfigDAO.setDbUrl(props.getProperty("DbUrl") );
		ConfigDAO.setDbUsername( props.getProperty("DbUsername") );
		ConfigDAO.setDbPassword( props.getProperty("DbPassword") );
		ConfigDAO.setDbSchema( props.getProperty("DbSchema") );
		ConfigDAO.setDbTable( props.getProperty("DbTable") );

		// for the Log4J Config data
		Log4jDAO.setJdbcDriver( props.getProperty("Log4jJdbcDriver") );
		Log4jDAO.setDbUrl( props.getProperty("Log4jDbUrl") );
		Log4jDAO.setDbUsername( props.getProperty("Log4jDbUsername") );
		Log4jDAO.setDbPassword( props.getProperty("Log4jDbPassword") );
		Log4jDAO.setDbSchema( props.getProperty("Log4jDbSchema") );
		Log4jDAO.setDbTable( props.getProperty("Log4jDbTable") );

		// for the Merchant Config data
		MerchantDAO.setJdbcDriver( props.getProperty("MerchantJdbcDriver") );
		MerchantDAO.setDbUrl( props.getProperty("MerchantDbUrl") );
		MerchantDAO.setDbUsername( props.getProperty("MerchantDbUsername") );
		MerchantDAO.setDbPassword( props.getProperty("MerchantDbPassword") );
		MerchantDAO.setDbSchema( props.getProperty("MerchantDbSchema") );
		MerchantDAO.setDbTable( props.getProperty("MerchantDbTable") );

	}

	public static void initFromFile()
	{
		Properties props = getPropertiesFromFile("UnitTest.properties");
		// Set driver to null to prevent usage of database
		ConfigDAO.setJdbcDriver(null);
		Log4jDAO.setJdbcDriver(null);

		// Set the proper Java properties for the location of the
		// configuration files as a backup if the database is not used
		System.setProperty("coreConfigLocation",  props.getProperty("coreConfigLocation") );
		System.setProperty("coreConfigName",  props.getProperty("coreConfigName") );

		System.setProperty("merchantConfigLocation",  props.getProperty("merchantConfigLocation") );
		System.setProperty("merchantConfigName",  props.getProperty("merchantConfigName") );

		System.setProperty("log4JConfigLocation",  props.getProperty("log4JConfigLocation") );
		System.setProperty("log4JConfigName",  props.getProperty("log4JConfigName") );
	}

}