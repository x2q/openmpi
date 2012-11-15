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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * This DAO allows access to the common log4j properties that will be shared among
 * all verified@ONCE components. It handles and return a Properties object to simply
 * the interaction with the actual Log4J component
 * 
 * @author Martin Dufort (mdufort@oncecorp.com)
 * @version $Revision: 1 $
 * @since 1.2
 */
public class Log4jDAO implements SimpleDAO {
	
	private static String jdbcDriver;
	private static String dbUrl;
	private static String dbUsername;
	private static String dbPassword;
	private static String dbSchema;
	private static String dbTable;

	/*
	 * @see com.oncecorp.visa3d.mpi.persistence.SimpleDAO#create(java.lang.Object)
	 */
	public void create(Object o) throws PersistentException {
		Connection conn = null;

		try {
			Properties data = (Properties) o;

			// get JDBC connection
			conn = getConnection();

			// prepare SQL statement
			PreparedStatement stmt =
				conn.prepareStatement(
					DAODefinition.LOG4J_CREATE_STMT1
						+ getDbSchema()
						+ "."
						+ getDbTable()
						+ DAODefinition.LOG4J_CREATE_STMT2);

			// Update table with all elements contained in the Properties object
			Enumeration enum = data.keys();
			while (enum.hasMoreElements()) {
				String key = (String) enum.nextElement();
				String value = (String) data.get(key);

				System.out.println(
					"Insert Log4J data: \n"
						+ "  Name: "
						+ key
						+ "\n  Property: "
						+ value);

				stmt.setString(1, key);
				stmt.setString(2, value);

				stmt.executeUpdate();
			}

			// Clean up
			stmt.close();
			conn.close();
		} 
		catch (ClassCastException cce) {
			System.out.println(
				"The Log4JDao can only handle a Properties object.");
			throw new PersistentException(cce.getMessage());
		} 
		catch (SQLException sqle) {
			System.out.println("Failed to insert Log4J data");
			sqle.printStackTrace();
			throw new PersistentException(sqle.getMessage());
		} 
		finally {
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

	/* (non-Javadoc)
	 * @see com.oncecorp.visa3d.mpi.persistence.SimpleDAO#retrieve(java.lang.Object)
	 * 
	 */
	public Object retrieve(Object o) throws PersistentException {
		Connection conn = null;

		try {
			// Get JDBC connection
			conn = getConnection();
			System.out.println("Got db connection to retrieve log4j configuration data.");

			// Prepare SQL statement
			PreparedStatement stmt = null;

			// Retrieve all Log4J Configuration parameters from the DB
			stmt = conn.prepareStatement(
					DAODefinition.LOG4J_RETRIEVE_ALL_STMT
							+ getDbSchema()
							+ "."
							+ getDbTable());
			
			ResultSet rs = stmt.executeQuery();

			// Read each row and insert into Properties object
			Properties data = new Properties();
			while (rs.next()) {
				data.put(rs.getString("NAME"), rs.getString("PROPERTY"));
			}

			if (data.size() == 0) {
				// No rows in database
				throw new PersistentException("Database Table for Log4J Configuration is empty...");
			}
			// Clean up
			stmt.close();
			conn.close();

			return data;
		} 
		catch (ClassCastException cce) {
			System.out.println(
				"Argument type casting error.  java.lang.String.");
			throw new PersistentException(cce.getMessage());
		} 
		catch (SQLException sqle) {
			System.out.println("Failed to retrieve config data" + sqle.getMessage());
			throw new PersistentException(sqle.getMessage());
		} 
		finally {
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

	/* (non-Javadoc)
	 * @see com.oncecorp.visa3d.mpi.persistence.SimpleDAO#update(java.lang.Object)
	 */
	public void update(Object o) throws PersistentException {
		// #todo# Auto-generated method stub

	}

	/**
	 * @see com.oncecorp.visa3d.mpi.persistence.SimpleDAO#delete(java.lang.Object)
	 */
	public void delete(Object o) throws PersistentException {
		Connection conn = null;

		try {
			// Get JDBC connection
			conn = getConnection();
			System.out.println("Got db connection to delete log4j configuration data.");

			// Prepare SQL statement
			PreparedStatement stmt = null;

			// Retrieve all Log4J Configuration parameters from the DB
			stmt = conn.prepareStatement(
					DAODefinition.LOG4J_DELETE_TYPE_STMT1
							+ getDbSchema()
							+ "."
							+ getDbTable());
			
			stmt.executeUpdate();

			// Clean up
			stmt.close();
			conn.close();

		} 
		catch (SQLException sqle) {
			System.out.println("Failed to delete log4j config data" + sqle.getMessage());
			throw new PersistentException(sqle.getMessage());
		} 
		finally {
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
//			System.out.println("JDBC Driver Class Not Found: " + getJdbcDriver());
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
	 * @return
	 */
	public static String getDbSchema() {
		return dbSchema;
	}

	/**
	 * @return
	 */
	public static String getDbTable() {
		return dbTable;
	}

	/**
	 * @return
	 */
	public static String getDbUrl() {
		return dbUrl;
	}

	/**
	 * @return
	 */
	public static String getDbUsername() {
		return dbUsername;
	}

	/**
	 * @return
	 */
	public static String getJdbcDriver() {
		return jdbcDriver;
	}

	/**
	 * @param string
	 */
	public static void setDbSchema(String string) {
		dbSchema = string;
	}

	/**
	 * @param string
	 */
	public static void setDbTable(String string) {
		dbTable = string;
	}

	/**
	 * @param string
	 */
	public static void setDbUrl(String string) {
		dbUrl = string;
	}

	/**
	 * @param string
	 */
	public static void setDbUsername(String string) {
		dbUsername = string;
	}

	/**
	 * @param string
	 */
	public static void setJdbcDriver(String string) {
		jdbcDriver = string;
	}

	/**
	 * @return
	 */
	public static String getDbPassword() {
		return dbPassword;
	}

	/**
	 * @param string
	 */
	public static void setDbPassword(String string) {
		dbPassword = string;
	}

}
