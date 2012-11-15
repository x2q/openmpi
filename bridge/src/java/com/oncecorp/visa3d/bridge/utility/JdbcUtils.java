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

package com.oncecorp.visa3d.bridge.utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;

/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This is the utility class that provides the methods for
 * JDBC connection.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class JdbcUtils {

  public JdbcUtils() {
  }

  /**
   * This method create and returns the client data source object.  In this class,
   * we use "Jakarta-commons-release" to create the data source object.
   * @param driverClass The JDBC driver class name.
   * @param jdbcURL The database URL.
   * @param jdbcUID The database user id.
   * @param jdbcPW The database password.
   * @param maxConns maximum connections
   * @param timeOut  connection timeout
   * @return The data Source object.
   */
  public static DataSource createPoolingDataSoruce(
		  String driverClass, String jdbcURL, String jdbcUID, String jdbcPW,
          int maxConns, int timeOut	)
  {
	  try {
		  Class.forName( driverClass );
		  BasicDataSource bds = new BasicDataSource();
		  bds.setDriverClassName( driverClass );
		  bds.setMaxActive( maxConns );
		  bds.setMaxWait( timeOut );
		  bds.setUrl( jdbcURL );
		  bds.setUsername( jdbcUID );
		  bds.setPassword( jdbcPW );

		  System.out.println("Use dbcp pool with datasource = "
						+ bds );
		  return bds;
	  }
	  catch (Exception e)
	  {
		  System.out.println("Data Source creating failed. -- "
					   + driverClass + ", " +  jdbcURL + ", "
					   + jdbcUID + ", " +  jdbcPW + ", ==" + e.getMessage() );
		  return null;
	  }
  }

  /**
   *
   * @param src - data source instance
   */
  public static void closeDataSource(  DataSource  src )
  {
	  try {
		  if ( src != null && src instanceof BasicDataSource )
			   ( (BasicDataSource)src).close();
	  } catch (Exception e) {
		  System.out.println( " Close pooling connection pool failed == "
							 + e.getMessage() );
	  }

  }

  /**
   * This method converts the java date into the long that point to the beginning
   * of the day.
   * @param date The converted date object.
   * @param startEnd <tt>true</tt> day start time, <tt>false</tt> day end time.
   * @return The long that point to the beginning or end of the day.
   */
  public static long dayValue(Date date, boolean startEnd) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR, startEnd ? 0 : 23);
    calendar.set(Calendar.MINUTE, startEnd ? 0 : 59);
    calendar.set(Calendar.SECOND, startEnd ? 0 : 59);
    calendar.set(Calendar.MILLISECOND, startEnd ? 0 : 999);
    return calendar.getTime().getTime();
  }

  /**
   * This method converts the java date object into the <tt>long</tt> type
   * value that includes the day and time.
   * @param date The java date object.
   * @return The <tt>long</tt> type value.
   */
  public static long dateValue(Date date) {
    return date.getTime();
  }

  /**
   * This method extracts string represented date value into <tt>long</tt>
   * type value.
   * @param dateString The string represents the date.
   * @param dateFormat The format pattern string.  The format syntax see the
   * API document of java.text.SimpleDateFormat.
   * @return The <tt>long</tt> type value.
   */
  public static long extractDateValue( String dateString, String dateFormat ) {
    SimpleDateFormat format = new SimpleDateFormat(dateFormat);
    try {
      Date date = format.parse(dateString);
      return date.getTime();
    }
    catch (Exception e) {
      return -1L;
    }
  }
}