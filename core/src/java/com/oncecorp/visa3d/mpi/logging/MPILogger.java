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
import com.oncecorp.visa3d.mpi.configuration.ConfigAccessorGenerator;
import com.oncecorp.visa3d.mpi.configuration.ConfigAccessorType;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Description: This is the gateway class which provides access point to Log4J framwork.
 * Other MPI components should call static method getLogger() to get a local logger.
 * Suggested that other components pass in their fully qualified class name as logger name.
 *
 * For example:
 * <pre>
 * 		org.apache.log4j.Logger logger
 * 				= MPILogger.getLogger( this.class.getName() );
 * </pre>
 *
 *
 * @version 0.1 July 02, 2002
 * @author	Alan Zhang
 */
public class MPILogger {
	/**
	 * Singleton flag attribute
	 */
	private static boolean configured = false;

	/**
	 * Get the appropriate Log4J Logger
	 * @param in Logger name
	 * @return Logger Log4J logger object. If any initialization problem occurred, return null.
	 */
	public static Logger getLogger(String in) {
		//Get logger first.
		Logger logger = Logger.getLogger(in);

		//Configure Logging system if not done yet.
		if (!configured) {
			try {
				System.out.println("Configuring MPILogger...");
				config();
			} catch (MPILoggerInitException mpie) {
				return getDefaultLogger();
			}
		}

		//Return logger
		return logger;

	}

    /**
     * Return a default logger for MPI Core
     * @return Logger Log4J logger ojbect. (Default properties)
     */
    private synchronized static Logger getDefaultLogger()
    { Properties ps = new Properties();

      ps.setProperty("log4j.rootCategory", "DEBUG, R");
      ps.setProperty("log4j.appender.R", "org.apache.log4j.ConsoleAppender");
      ps.setProperty("log4j.appender.R.layout", "org.apache.log4j.PatternLayout");
      ps.setProperty("log4j.appender.R.layout.ConversionPattern", "%d{dd MMM yyyy HH:mm:ss,SSS} %-5p [%t] (%F:%L) - %m\n");
      PropertyConfigurator.configure(ps);

      configured = true;

      return Logger.getLogger(MPILogger.class);
    }

	/**
	 * Configure Log4J environment attributes.
	 * This method will be updated when Persistent Component ready. More detail explanation will be added
	 * at that moment.
	 *
	 * @exception MPILoggerInitException Customized exception to indicate MPILogging initial exceptions.
	 */
	private synchronized static void config() throws MPILoggerInitException {
		//Get config accessor
		ConfigAccessor accessor =
			ConfigAccessorGenerator.getAccessor(ConfigAccessorType.ACCESSOR_TYPE_LOGGING);

		//Get config data
		Properties ps = null;
		try {
			ps = (Properties) accessor.getConfigData();
		} catch (ClassCastException cce) {
			throw new MPILoggerInitException("Failed to get config data.");
		}

    System.out.println("Loaded LOG4J properties: " + ps);

		//Configure Log4J environment
		/**
		 * [Gang's note: May 28, 2003]
		 * Catch the exception that caused at runtime config, and if error
		 * happened forcing using default logger.
		 */
	    try {
			PropertyConfigurator.configure(ps);
	    } catch ( Exception e )
		{
			throw new MPILoggerInitException("Failed to config logger property.");
		}

		//Set singleton flag
		configured = true;
	}

	/**
	 * Convenience method to check Log4J environment status
	 *
	 * @return boolean If configured, return true; otherwise return false.
	 */
	public static boolean isConfigured() {
		//return flag status
		return configured;
	}

	/**
	 * Reconfigure Logger properties
	 * @param props The new properties
	 */
	public static void reconfig(Properties props) {
		PropertyConfigurator.configure(props);
	}
	/**
	 * Sets the configured.
	 * @param configured The configured to set
	 */
	public static void setConfigured(boolean configured) {
		MPILogger.configured = configured;
	}

}
