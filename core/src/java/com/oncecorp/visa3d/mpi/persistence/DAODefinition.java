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

package com.oncecorp.visa3d.mpi.persistence;

/**
 * Class description
 * 
 * @author Alan Zhang
 * @version $Revision: 15 $
 */
public interface DAODefinition {

	/**
	 * DAO initial properties file location
	 */
	public final static String DAO_INIT_FILE 				= "com.oncecorp.visa3d.mpi.persistence.DAOInit";

	/**
	 * Config DAO config data keys
	 */
	public final static String CONFIG_DAO_JDBC_DRIVER 		= "config.dao.jdbc.driver";
	public final static String CONFIG_DAO_DB_URL 			= "config.dao.db.url";
	public final static String CONFIG_DAO_DB_USERNAME 		= "config.dao.db.username";
	public final static String CONFIG_DAO_DB_PASSWORD 		= "config.dao.db.password";
	public final static String CONFIG_DAO_DB_SCHEMA 		= "config.dao.db.schema";
	public final static String CONFIG_DAO_DB_TABLE 			= "config.dao.db.table";

	/**
	 * LOG4J DAO config data keys
	 */
	public final static String LOG4J_DAO_JDBC_DRIVER 		= "log4j.dao.jdbc.driver";
	public final static String LOG4J_DAO_DB_URL 			= "log4j.dao.db.url";
	public final static String LOG4J_DAO_DB_USERNAME 		= "log4j.dao.db.username";
	public final static String LOG4J_DAO_DB_PASSWORD 		= "log4j.dao.db.password";
	public final static String LOG4J_DAO_DB_SCHEMA 			= "log4j.dao.db.schema";
	public final static String LOG4J_DAO_DB_TABLE 			= "log4j.dao.db.table";

	/**
	 * Merchant DAO config data keys
	 */
	public final static String MERCHANT_DAO_JDBC_DRIVER 	= "merchant.dao.jdbc.driver";
	public final static String MERCHANT_DAO_DB_URL 			= "merchant.dao.db.url";
	public final static String MERCHANT_DAO_DB_USERNAME 	= "merchant.dao.db.username";
	public final static String MERCHANT_DAO_DB_PASSWORD 	= "merchant.dao.db.password";
	public final static String MERCHANT_DAO_DB_SCHEMA 		= "merchant.dao.db.schema";
	public final static String MERCHANT_DAO_DB_TABLE 		= "merchant.dao.db.table";

	/**
	 * Config data query
	 */
	public final static String CONFIG_RETRIEVE_ALL_STMT =
		"SELECT NAME, VALUE,  DESCRIPTION, UNIT_DESCRIPTION FROM ";

	public final static String CONFIG_UPDATE_STMT1 =
		"UPDATE ";
	public final static String CONFIG_UPDATE_STMT2 =
		" SET VALUE=?, DESCRIPTION=?, UNIT_DESCRIPTION=? WHERE NAME=?";

	public final static String CONFIG_DELETE_TYPE_STMT1 =
		"DELETE FROM ";
	public final static String CONFIG_DELETE_TYPE_STMT2 =
		" WHERE NAME=?";

	public final static String CONFIG_CREATE_STMT1 =
		"INSERT INTO ";
	public final static String CONFIG_CREATE_STMT2 =
		" (NAME, VALUE, DESCRIPTION, UNIT_DESCRIPTION) VALUES (?, ?, ?, ?)";

	/**
	 * Log4J data query
	 */
	public final static String LOG4J_RETRIEVE_ALL_STMT =
		"SELECT NAME, PROPERTY FROM ";

	public final static String LOG4J_UPDATE_STMT1 =
		"UPDATE ";
	public final static String LOG4J_UPDATE_STMT2 =
		" SET PROPERTY=? WHERE NAME=?";

	public final static String LOG4J_DELETE_TYPE_STMT1 =
		"DELETE FROM ";
	public final static String LOG4J_DELETE_TYPE_STMT2 =
		" WHERE NAME=?";

	public final static String LOG4J_CREATE_STMT1 =
		"INSERT INTO ";
	public final static String LOG4J_CREATE_STMT2 =
		" (NAME, PROPERTY) VALUES (?, ?)";

	/**
	 * Merchant data query
	 */
	public final static String MERCHANT_RETRIEVE_SINGLE_STMT1 =
		"SELECT MERCHANT_ID, NAME, MERCHANT_PASSWORD, MERCHANT_URL, COUNTRYCODE, PURCHASECURRENCY, " +
		"ACQUIRERBIN, PROTOCOL_SUPPORT, LICENSING_KEY, KEY_EXPIRY_DATE"
			+ " FROM ";
	public final static String MERCHANT_RETRIEVE_SINGLE_STMT2 =
		" WHERE MERCHANT_ID=?";

	public final static String MERCHANT_RETRIEVE_ALL_STMT =
		"SELECT MERCHANT_ID, NAME, MERCHANT_PASSWORD, MERCHANT_URL, COUNTRYCODE, PURCHASECURRENCY, ACQUIRERBIN, "+
		"PROTOCOL_SUPPORT, LICENSING_KEY, KEY_EXPIRY_DATE"
			+ " FROM ";
			
	public final static String MERCHANT_CREATE_STMT1 =
		"INSERT INTO ";
	public final static String MERCHANT_CREATE_STMT2 =
		" (MERCHANT_ID, NAME, MERCHANT_PASSWORD, MERCHANT_URL, COUNTRYCODE, PURCHASECURRENCY, ACQUIRERBIN, "+
		"PROTOCOL_SUPPORT, LICENSING_KEY, KEY_EXPIRY_DATE) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	public final static String MERCHANT_UPDATE_STMT1 =
		"UPDATE ";
	public final static String MERCHANT_UPDATE_STMT2 =
		" SET NAME=?,MERCHANT_PASSWORD=?,MERCHANT_URL=?,COUNTRYCODE=?,PURCHASECURRENCY=?,ACQUIRERBIN=?,"+
		"PROTOCOL_SUPPORT=?,LICENSING_KEY=?,KEY_EXPIRY_DATE=? WHERE MERCHANT_ID=?";
				
	public final static String MERCHANT_DELETE_ALL_STMT1 =
		"DELETE FROM ";
		
	public final static String MERCHANT_DELETE_ONE_STMT1 = 
		"DELETE FROM ";
		
	public final static String MERCHANT_DELETE_ONE_STMT2 = 
		" WHERE MERCHANT_ID=?";

}
