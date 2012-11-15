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

import com.oncecorp.visa3d.mpi.configuration.CoreConfigInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

/**
 * DAO implementation class for Core Config Data
 * 
 * @author Alan Zhang
 * @version $Revision: 10 $
 */
public class ConfigDAO implements SimpleDAO {

	private static String jdbcDriver;
	private static String dbUrl;
	private static String dbUsername;
	private static String dbPassword;
	private static String dbSchema;
	private static String dbTable;

	/**
	 * Insert config data entries
	 * @param obj The Vector of config meta
	 */
	public void create(Object obj) throws PersistentException {
		Connection conn = null;

		try {
			Vector data = (Vector) obj;

			// get JDBC connection
			conn = getConnection();

			// prepare SQL statement
			PreparedStatement stmt =
				conn.prepareStatement(
					DAODefinition.CONFIG_CREATE_STMT1
						+ getDbSchema()
						+ "."
						+ getDbTable()
						+ DAODefinition.CONFIG_CREATE_STMT2);

			for (int i = 0; i < data.size(); i++) {
				CoreConfigInfo cInfo = (CoreConfigInfo) data.get(i);

				stmt.setString(1, cInfo.getName());
				stmt.setString(2, cInfo.getValue());
				stmt.setString(3, cInfo.getDescription());
				stmt.setString(4, cInfo.getUnitDescription());

				stmt.executeUpdate();
			}

			// clean up
			stmt.close();
			conn.close();
		} catch (ClassCastException cce) {
			System.err.println("Argument type casting error. The type of argument s/b ConfigMeta." + cce);
			cce.printStackTrace();
			throw new PersistentException(cce.getMessage());
		} catch (SQLException sqle) {
			System.err.println("Failed to insert config data");
			sqle.printStackTrace();
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

	/**
	 * Retrieve MPI configuration data
	 * @param obj Null to retrieve all config data or config data type
	 * @return The Vector contains ConfigMeta objects
	 */
	public Object retrieve(Object obj) throws PersistentException {
		Connection conn = null;
		try {
			// get JDBC connection
			conn = getConnection();

			// prepare SQL statement
			PreparedStatement stmt = null;
			stmt = conn.prepareStatement(
						DAODefinition.CONFIG_RETRIEVE_ALL_STMT
							+ getDbSchema()
							+ "."
							+ getDbTable());
			ResultSet rs = stmt.executeQuery();

			// Convert result into vector
			Vector data = new Vector();
			while (rs.next()) {
				CoreConfigInfo info = new CoreConfigInfo();
				info.setName(rs.getString("NAME"));
				info.setValue(rs.getString("VALUE"));
				info.setDescription(rs.getString("DESCRIPTION"));
				info.setUnitDescription(rs.getString("UNIT_DESCRIPTION"));
				data.add(info);
			}

			if (data.size() == 0) {
				// No rows in database
			 	throw new PersistentException("Database Table for Core Configuration is empty...");
			}
			
			// clean up
			stmt.close();
			conn.close();

			return data;
		} catch (ClassCastException cce) {
			System.out.println(
				"Argument type casting error. The type of argument s/b java.lang.String.");
			throw new PersistentException(cce.getMessage());
		} catch (SQLException sqle) {
			System.out.println("Failed to retrieve config data" + sqle.getMessage());
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

	/**
	 * Update config data entry
	 * @param obj The Vector of config meta
	 */
	public void update(Object obj) throws PersistentException {
		Connection conn = null;

		try {
			Vector data = (Vector) obj;

			// get JDBC connection
			conn = getConnection();

			// prepare SQL statement
			PreparedStatement stmt =
				conn.prepareStatement(
					DAODefinition.CONFIG_UPDATE_STMT1
						+ getDbSchema()
						+ "."
						+ getDbTable()
						+ DAODefinition.CONFIG_UPDATE_STMT2);

			for (int i = 0; i < data.size(); i++) {
				CoreConfigInfo info = (CoreConfigInfo) data.get(i);

				stmt.setString(1, info.getValue());
				stmt.setString(2, info.getDescription());
				stmt.setString(3, info.getUnitDescription());
				
				// Set proper primary key to use for update
				stmt.setString(4, info.getName());
				stmt.executeUpdate();
			}

			// clean up
			stmt.close();
			conn.close();
		} catch (ClassCastException cce) {
			System.out.println(
				"Argument type casting error. The type of argument s/b ConfigMeta.");
			throw new PersistentException(cce.getMessage());
		} catch (SQLException sqle) {
			System.out.println("Failed to update config data");
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

	/**
	 * Delete config data
	 * @param obj The config data type
	 */
	public void delete(Object obj) throws PersistentException {
		Connection conn = null;

		try {
			String name = (String) obj;
			System.out.println("Delete config data: " + name);

			// get JDBC connection
			conn = getConnection();
			PreparedStatement stmt =
			conn.prepareStatement(
				DAODefinition.CONFIG_DELETE_TYPE_STMT1
					+ getDbSchema()
					+ "."
					+ getDbTable());
			stmt.executeUpdate();

			// Clean up
			stmt.close();
			conn.close();
		} catch (ClassCastException cce) {
			System.out.println(
				"Argument type casting error. The type of argument s/b java.lang.String.");
			throw new PersistentException(cce.getMessage());
		} catch (SQLException sqle) {
			System.out.println("Failed to delete config data");
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

	private Connection getConnection() throws PersistentException {
		/*
		if (getJdbcDriver() == null) {
			init();
		}
		*/

		try {
			Class.forName(getJdbcDriver());
		} catch (ClassNotFoundException cnfExcep) {
			System.out.println("JDBC Driver Class Not Found: " + getJdbcDriver() + cnfExcep);
			throw new PersistentException("Database driver not available");
		}
		catch (NullPointerException npeExcep) {
			throw new PersistentException("No database driver classname specified");
		}
		
		// Able to get the connection driver so try it out...		
		try {
			return DriverManager.getConnection(
				getDbUrl(),
				getDbUsername(),
				getDbPassword());

		} catch (SQLException ex) {
			System.out.println(
				"Failed to initialize JDBC connection to "
					+ getDbUrl()
					+ " by account "
					+ getDbUsername()
					+ "/"
					+ getDbPassword());
			// ex.printStackTrace();
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
		ConfigDAO.dbPassword = dbPassword;
	}

	/**
	 * Sets the dbUrl.
	 * @param dbUrl The dbUrl to set
	 */
	public static void setDbUrl(String dbUrl) {
		ConfigDAO.dbUrl = dbUrl;
	}

	/**
	 * Sets the dbUsername.
	 * @param dbUsername The dbUsername to set
	 */
	public static void setDbUsername(String dbUsername) {
		ConfigDAO.dbUsername = dbUsername;
	}

	/**
	 * Sets the jdbcDriver.
	 * @param jdbcDriver The jdbcDriver to set
	 */
	public static void setJdbcDriver(String jdbcDriver) {
		ConfigDAO.jdbcDriver = jdbcDriver;
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
		ConfigDAO.dbSchema = dbSchema;
	}

	/**
	 * Sets the dbTable.
	 * @param dbTable The dbTable to set
	 */
	public static void setDbTable(String dbTable) {
		ConfigDAO.dbTable = dbTable;
	}

}
