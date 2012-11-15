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

import com.oncecorp.visa3d.mpi.configuration.MerchantMetaInfo;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.security.MPIEncrypter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * DAO implementation class for Merchant Config Data
 *
 * @author Alan Zhang
 * @version $Revision: 15 $
 */
public class MerchantDAO implements SimpleDAO {

	protected Logger logger = MPILogger.getLogger(MerchantDAO.class.getName());

	private static String jdbcDriver;
	private static String dbUrl;
	private static String dbUsername;
	private static String dbPassword;
	private static String dbSchema;
	private static String dbTable;

	/**
	 * Retrieve Merchant configuration data
	 * @param obj Null to retrieve all config data or merchant ID in a String format
	 * @return The HashMap contains MerchantID / MerchantMetaInfo pairs
	 */
	public Object retrieve(Object obj) throws PersistentException {
		Connection conn = null;

		try {
			// Get JDBC connection
			conn = getConnection();
			System.out.println("Got db connection to retrieve merchant configuration data.");

			// Prepare SQL statement
			PreparedStatement stmt = null;
			if (obj == null) {
				stmt =
					conn.prepareStatement(
						DAODefinition.MERCHANT_RETRIEVE_ALL_STMT
							+ getDbSchema()
							+ "."
							+ getDbTable());
			} else {
				stmt =
					conn.prepareStatement(
						DAODefinition.MERCHANT_RETRIEVE_SINGLE_STMT1
							+ getDbSchema()
							+ "."
							+ getDbTable()
							+ DAODefinition.MERCHANT_RETRIEVE_SINGLE_STMT2);
				stmt.setString(1, (String) obj);
			}

			// Execute the retrieve statement
			ResultSet rs = stmt.executeQuery();

			// Convert result into a Java object
			String decodedPassword, encodedPassword;
			HashMap data = new HashMap();
			while (rs.next()) {
				MerchantMetaInfo meta = new MerchantMetaInfo();
				//check merchant ID
				meta.setMerchantID(rs.getString("MERCHANT_ID"));
				encodedPassword = rs.getString("MERCHANT_PASSWORD");

				try {
					// Check the mandatory attributes for validity
					checkMerchantID(meta.getMerchantID());
					decodedPassword = checkMerchantPassword(encodedPassword);
				}
				catch (PersistentException e) {
					logger.error("Failed to retrieve merchant. Invalid merchant ID: "
							     + meta.getMerchantID()
							     + ". Continue to retrieve next one.");
					continue;
				}

				// Set all the merchant information values
				meta.setMerchantName(rs.getString("NAME"));
				meta.setMerchantPassword(decodedPassword);
				meta.setMerchantURL(rs.getString("MERCHANT_URL"));
				meta.setMerchantCountryCode(rs.getString("COUNTRYCODE"));
				meta.setMerchantPurchaseCurrency(rs.getString("PURCHASECURRENCY"));
				meta.setAcquirerBIN(rs.getString("ACQUIRERBIN"));
				meta.setProtocolSupport(rs.getByte("PROTOCOL_SUPPORT"));
				meta.setLicensingKey(rs.getString("LICENSING_KEY"));
				meta.setKeyExpiryDate(rs.getLong("KEY_EXPIRY_DATE"));

				data.put(meta.getMerchantID(), meta);
				logger.debug(
					"Merchant retrieved: ["
						+ meta.getMerchantID()
						+ ", "
						+ meta.getMerchantName()
						+ ", "
						+ meta.getMerchantPassword()
						+ ", "
						+ meta.getMerchantURL()
						+ ", "
						+ meta.getMerchantCountryCode()
						+ ", "
						+ meta.getMerchantPurchaseCurrency()
						+ ", "
						+ meta.getAcquirerBIN()
						+ ", "
						+ meta.getProtocolSupport()
						+ ", "
						+ meta.getLicensingKey()
						+ ", "
						+ meta.getKeyExpiryDate()
						+ "]");
			}

			if (data.size() == 0) {
				// No rows in database
				throw new PersistentException("Requested Merchant(s) do not exist");
			}
			// Clean up
			stmt.close();
			conn.close();
			logger.debug("Merchant data retrieval finished.");

			return data;
		} catch (ClassCastException cce) {
			System.out.println(
				"Argument type casting error. The type of argument s/b java.lang.String.");
			throw new PersistentException(cce.getMessage());
		} catch (SQLException sqle) {
			System.out.println("Failed to retrieve merchant data");
			throw new PersistentException(sqle.getMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("Failed to close JDBC connection.");
					e.printStackTrace();
				}
			}
		}
	}

	public String checkMerchantPassword(String anEncodedPwd) throws PersistentException {
		/*
		 * [Martin's Note: Feb 27, 2003 3:37:33 PM]
		 * We allow an empty password so we should bypass
		 * decryption if we get a null back
		 *
		 */
		String decPwd;

		if (anEncodedPwd != null) {
			// Non null indicates that a password is present
			try {
				decPwd = MPIEncrypter.decrypt(anEncodedPwd);
				// The password is invalid if it is null after decryption or if the value is not 0 or 8
				if ((decPwd == null) ||
					((decPwd.length() != 0) && (decPwd.length() != 8))) {
					logger.error(
						"Merchant password length is not valid. Length="
							+ decPwd.length() + " Value=" + decPwd);
					throw new PersistentException("Invalid merchant password");
				}
			}
			catch (Exception e) {
				logger.error("Failed to decrypted merchant's password: " + anEncodedPwd);
				throw new PersistentException("Unable to decrypt the merchant's password");
			}
		}
		else {
			// Set the password to nothing
			decPwd = "";
		}
		return decPwd;
	}

	private void checkMerchantID(String id) throws PersistentException {
		if (id == null) {
			logger.error("Invalid Merchant ID: NULL value.");
			throw new PersistentException("Invalid Merchant ID: NULL value.");
		}

		// We need comments about validation rules for Merchant ID because we 
		// do not want to go look elsewhere to figure out the meaningless, 
		// uncommented lines of code that resides in the following sections. 
		//
		// Comments are cheap and should be part of a good programming practice 
		// instead of trying to create the biggest mass of LOC possible.
		//			This public announcement messages was provided free of charge
		//			by: mdufort@oncecorp.com
		
		// ---- Validation rules ----
		// Merchant id can be up to 24 characters.
		// It can, optionally be divided by a hyphen which counts toward the maximum size
		// If not divided, then maximum size is up to 15
		// If divided, 
		//		then first part is up to 15
		//		and last part is up to 8
		// 		maximum cannot be greater than 24
		// ----- End Validation Rules ----
		
		// Check if we have a hyphen
		int pos = id.indexOf("-");
		if (pos == -1) {
			// no dash exist in id, so maximum is 15
			if (id.length() > 15) {
				logger.error("Invalid merchant ID length: " + id);
				throw new PersistentException(
					"Invalid merchant ID length: " + id);
			} else {
				// valid merchant ID
				return;
			}
		}

		// Dash found in id
		boolean valid = false;
		while (pos != -1) {
			if ((id.substring(0, pos).length() <= 15) && 
					((pos == id.length()) || (id.substring(pos + 1).length() <= 8))) {
				valid = true;
			}
			pos = id.indexOf("-", pos + 1);
		}

		if (!valid) {
			logger.error("Invalid merchant ID: " + id);
			throw new PersistentException("Invalid merchant ID: " + id);
		}
	}

	public void create(Object obj) throws PersistentException {
		Connection conn = null;

		try {
			HashMap data = (HashMap) obj;

			// get JDBC connection
			conn = getConnection();

			// prepare SQL statement
			PreparedStatement stmt =
				conn.prepareStatement(
					DAODefinition.MERCHANT_CREATE_STMT1
						+ getDbSchema()
						+ "."
						+ getDbTable()
						+ DAODefinition.MERCHANT_CREATE_STMT2);

			// Update table with all elements contained in the Properties object
			Iterator it = data.values().iterator();
			while (it.hasNext()) {
				MerchantMetaInfo info = (MerchantMetaInfo) it.next();
				stmt.setString(1, info.getMerchantID());
				stmt.setString(2, info.getMerchantName());
				stmt.setString(3, info.getMerchantPassword());
				stmt.setString(4, info.getMerchantURL());
				stmt.setString(5, info.getMerchantCountryCode());
				stmt.setString(6, info.getMerchantPurchaseCurrency());
				stmt.setString(7, info.getAcquirerBIN());
				stmt.setByte(8,   info.getProtocolSupport());
				stmt.setString(9, info.getLicensingKey());
				stmt.setLong(10,   info.getKeyExpiryDate());

				System.out.println(stmt.toString());
				stmt.executeUpdate();
			}

			// Clean up
			stmt.close();
			conn.close();
		}
		catch (ClassCastException cce) {
			logger.error("The MerchantDAO can only handle a HashMap object.");
			throw new PersistentException(cce.getMessage());
		}
		catch (SQLException sqle) {
			logger.error("Failed to insert Merchant Configuration data");
			throw new PersistentException(sqle.getMessage());
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Failed to close JDBC connection.", e);
				}
			}
		}
	}

	public void update(Object obj) throws PersistentException {
		Connection conn = null;

		try {
			HashMap data = (HashMap) obj;
			PreparedStatement stmt = null;

			// get JDBC connection
			conn = getConnection();

			Iterator it = data.values().iterator();
			while (it.hasNext()) {
				// prepare SQL statement
				stmt = conn.prepareStatement(
							DAODefinition.MERCHANT_UPDATE_STMT1
							+ getDbSchema()
							+ "."
							+ getDbTable()
							+ DAODefinition.MERCHANT_UPDATE_STMT2);

				// Update table with all elements contained in the Properties object
				MerchantMetaInfo info = (MerchantMetaInfo) it.next();
				stmt.setString(1, 	info.getMerchantName());
				stmt.setString(2, 	info.getMerchantPassword());
				stmt.setString(3, 	info.getMerchantURL());
				stmt.setString(4, 	info.getMerchantCountryCode());
				stmt.setString(5, 	info.getMerchantPurchaseCurrency());
				stmt.setString(6, 	info.getAcquirerBIN());
				stmt.setByte(7,   	info.getProtocolSupport());
				stmt.setString(8, 	info.getLicensingKey());
				stmt.setLong(9,   	info.getKeyExpiryDate());
				stmt.setString(10, 	info.getMerchantID());		// Setting the primary key to use for update
				stmt.executeUpdate();
			}

			// Clean up
			stmt.close();
			conn.close();
		}
		catch (ClassCastException cce) {
				logger.error("The MerchantDAO can only handle a HashMap object.");
				throw new PersistentException(cce.getMessage());
		}
		catch (SQLException sqle) {
				logger.error("Failed to update Merchant Configuration data");
				throw new PersistentException(sqle.getMessage());
		}
		finally {
				try {
					conn.close();
				}
				catch (SQLException e) {
					logger.error("Failed to close JDBC connection.", e);
				}
		}
	}

	public void delete(Object obj) throws PersistentException {
		Connection conn = null;
		try {
			String merchantID = (String) obj;

			// get JDBC connection
			conn = getConnection();
			PreparedStatement deleteStmt = null;

			// No object specified then we delete all merchants
			if (obj == null) {
				 deleteStmt = conn.prepareStatement(
									DAODefinition.MERCHANT_DELETE_ALL_STMT1
									+ getDbSchema() + "." + getDbTable());
			}
			else {
				// One object specified w
				deleteStmt = conn.prepareStatement(
									DAODefinition.MERCHANT_DELETE_ONE_STMT1
									+ getDbSchema() + "." + getDbTable()
									+ DAODefinition.MERCHANT_DELETE_ONE_STMT2);

			}
			deleteStmt.setString(1, merchantID);
			deleteStmt.executeUpdate();

			// Clean up
			deleteStmt.close();
			conn.close();
		}
		catch (SQLException sqle) {
				logger.error("Failed to delete Merchant Configuration data");
				throw new PersistentException(sqle.getMessage());
			}
			finally {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Failed to close JDBC connection.", e);
				}
			}

	}

	/**
	 * Initialize JDBC connection
	 * @return The connection
	 */
	private Connection getConnection() throws PersistentException {
		/*
		if (getJdbcDriver() == null) {
			init();
		}
		*/
		String jdbcDriver = "";
		try {
			jdbcDriver = getJdbcDriver();
			Class.forName( jdbcDriver );
		} catch (java.lang.ClassNotFoundException e) {
			logger.error("JDBC Driver Class Not Found. " + jdbcDriver, e);
			throw new PersistentException(e.getMessage());
		} catch (Exception e) {
			logger.error("getConnection exception.", e);
			throw new PersistentException(e.getMessage());
		}

		String cdburl = "";
		String cdbuser = "";
		String cdbpasswd = "";
		try {
			cdburl = getDbUrl();
			cdbuser = getDbUsername();
			cdbpasswd = getDbPassword();
			return DriverManager.getConnection( cdburl, cdbuser, cdbpasswd );

		} catch (SQLException ex) {
			logger.error(
				"Failed to initialize JDBC connection to "
					+ cdburl + " by account " + cdbuser + "/" + cdbpasswd, ex);
			throw new PersistentException(ex.getMessage());
		}
	}
	/**
	 * Returns the dbPassword.
	 * @return String
	 */
	public static String getDbPassword() {
		return dbPassword;
	}

	/**
	 * Returns the dbUrl.
	 * @return String
	 */
	public static String getDbUrl() {
		return dbUrl;
	}

	/**
	 * Returns the dbUsername.
	 * @return String
	 */
	public static String getDbUsername() {
		return dbUsername;
	}

	/**
	 * Returns the jdbcDriver.
	 * @return String
	 */
	public static String getJdbcDriver() {
		return jdbcDriver;
	}

	/**
	 * Sets the dbPassword.
	 * @param dbPassword The dbPassword to set
	 */
	public static void setDbPassword(String dbPassword) {
		MerchantDAO.dbPassword = dbPassword;
	}

	/**
	 * Sets the dbUrl.
	 * @param dbUrl The dbUrl to set
	 */
	public static void setDbUrl(String dbUrl) {
		MerchantDAO.dbUrl = dbUrl;
	}

	/**
	 * Sets the dbUsername.
	 * @param dbUsername The dbUsername to set
	 */
	public static void setDbUsername(String dbUsername) {
		MerchantDAO.dbUsername = dbUsername;
	}

	/**
	 * Sets the jdbcDriver.
	 * @param jdbcDriver The jdbcDriver to set
	 */
	public static void setJdbcDriver(String jdbcDriver) {
		MerchantDAO.jdbcDriver = jdbcDriver;
	}

	/**
	 * Returns the dbSchema.
	 * @return String
	 */
	public static String getDbSchema() {
		return dbSchema;
	}

	/**
	 * Returns the dbTable.
	 * @return String
	 */
	public static String getDbTable() {
		return dbTable;
	}

	/**
	 * Sets the dbSchema.
	 * @param dbSchema The dbSchema to set
	 */
	public static void setDbSchema(String dbSchema) {
		MerchantDAO.dbSchema = dbSchema;
	}

	/**
	 * Sets the dbTable.
	 * @param dbTable The dbTable to set
	 */
	public static void setDbTable(String dbTable) {
		MerchantDAO.dbTable = dbTable;
	}

}
