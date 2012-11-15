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

package com.oncecorp.visa3d.mpi.controller;

import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.ConfigurationException;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.ID_Generator;
import com.oncecorp.visa3d.mpi.messaging.Message;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sun.net.ssl.internal.www.protocol.https.Handler;
import com.sun.net.ssl.internal.www.protocol.https.HttpsURLConnection;


public class ErrorHandlerThreadManager {

	// List of dispatch modes and default interval
	private static final int DEFAULT_DISPATCH_INTERVAL 	= 5000;
	private static final String DIRECT_DISPATCH_MODE 	= "Direct";
	private static final String DELAY_DISPATCH_MODE 	= "Delay";

	/**
	 * Local Log4J logger
	 */
	private static Logger logger = MPILogger.getLogger(Communicator.class.getName());
	private static String dispatchMode;
	private static int errorThreadSleepInterval;

	private static Thread errorThread;
	private static Vector errorVector = new Vector();

	public static byte[] lock = { 0 };

	public void dispatchErrorMessage(ErrorRequest req) {
		//Load config attributes if not loaded
		if (getDispatchMode() == null) {
			try {
				Config cfg = Config.getConfigReference();
				String mode =
					(String) cfg.getConfigData(
						MPIConfigDefinition.ERROR_DISPATCH_MODE);
				String interval =
					(String) cfg.getConfigData(
						MPIConfigDefinition.ErrorThreadSleepInterval);

				if (mode != null) {
					setDispatchMode(mode);
					logger.debug("Dispatch mode set to: " + getDispatchMode());
				} else {
					setDispatchMode(DIRECT_DISPATCH_MODE);
					logger.error(
						"No dispatch mode value found. Set to default:" + DIRECT_DISPATCH_MODE);
				}

				if (interval != null) {
					try {
						setErrorThreadSleepInterval(Integer.parseInt(interval));
						logger.debug(
							"Error thread sleep interval set to: "
								+ getErrorThreadSleepInterval());
					} catch (NumberFormatException ne) {
						setErrorThreadSleepInterval(DEFAULT_DISPATCH_INTERVAL);
						logger.error(
							"Invalid value for error thread sleep interval. Set to default",
							ne);
					}
				}
				else {
					setErrorThreadSleepInterval(DEFAULT_DISPATCH_INTERVAL);
					logger.error(
						"No error thread sleep interval value found. Set to default");
				}

			} catch (ConfigurationException e) {
				logger.error(
					"Failed to load config attributes for error thread manager",
					e);
				setDispatchMode(DIRECT_DISPATCH_MODE);
				setErrorThreadSleepInterval(DEFAULT_DISPATCH_INTERVAL);
				logger.debug(
					"Error thread manager dispatch mode & thread sleep interval set to default.");
			}
		}

		//Decide routine by checking dispatch mode
		if (getDispatchMode().equals(DIRECT_DISPATCH_MODE)) {
			logger.debug("Error dispatch mode:" + DIRECT_DISPATCH_MODE);
			try {
				send(req.getToUrl(), req.getMsg());
			} catch (Exception e) {
				logger.error(
					"Failed to send error notification in direct mode.",
					e);
			}
		} else {
			logger.debug("Error dispatch mode: " + getDispatchMode());
			if ((errorThread == null) || (!errorThread.isAlive())) {
				createNewErrorThread();
				logger.debug("ErrorHandlerThread created.");
			}

			synchronized (lock) {
				errorVector.add(req);
				logger.debug("New ErrorRequest added.");
			}
		}

	}

	private void send(String targetUrl, Message sentMsg) {
		// do logging
		logger.debug("ErrorHandlerThread is called");

		//Initialize IO stream
		OutputStreamWriter out = null;

		try {
			//Precheck id
			if ((sentMsg.getId() != null)
				&& (sentMsg.getId().equals("UNKNOWN"))) {
				sentMsg.setId(ID_Generator.getUniqueId());
				logger.debug(
					"UNKNOWN id replaced with new id: " + sentMsg.getId());
			}

			logger.debug("Sending message to: " + targetUrl);

            /**
             * The following change done by Gang, to change the HttpConnection to
             * directly use SUN implementation
             */
            /**
             * The following old one is replaced
            //Establish connection to servlet
            URL url = new URL(targetUrl);
            */
            //Establish connection to servlet
            URL url;
            if ( targetUrl != null &&
                 targetUrl.trim().toLowerCase().startsWith("https:") )
                url = new URL(null, targetUrl, new Handler() );
            else
                url = new URL( targetUrl );
			URLConnection conn = url.openConnection();
			logger.debug("connection setup finished.");

			//Prepare for both input and output
			conn.setDoInput(true);
			conn.setDoOutput(true);

			//Turn off caching
			conn.setUseCaches(false);

			//Convert inMsg to string
			String msg = sentMsg.toString();
			logger.debug("Content to targetUrl: " + msg);

			//Set content type to MPI message
			conn.setRequestProperty(
				"Content-Type",
				"application/xml; charset=\"utf-8\"");

			//Set content length
			conn.setRequestProperty(
				"Content-Length",
				Integer.toString(msg.length()));

			//Create output stream
			out = new OutputStreamWriter(conn.getOutputStream());

			//Write message as POST data
			//out.write(URLEncoder.encode(msg));
			out.write(msg);

			//Flush it
			out.flush();

			//Close output stream
			out.close();
			logger.debug("Sending finished.");

			// prepare for logging
			Date timestamp = Calendar.getInstance().getTime();

            /** Old code
			// check the response
			if (conn instanceof HttpURLConnection) {
				// get Response Code
				HttpURLConnection ht = (HttpURLConnection) conn;
				int i = ht.getResponseCode();
            */
           // check the response
           if ( (conn instanceof HttpURLConnection)
                || ( conn instanceof HttpsURLConnection ) ) {

              int i;
              // get Response Code
              if ( conn instanceof HttpURLConnection )
              {
                  HttpURLConnection ht = (HttpURLConnection) conn;
                  i = ht.getResponseCode();
              }
              else
              {
                  HttpsURLConnection hts = (HttpsURLConnection) conn;
                  i = hts.getResponseCode();
              }


				// logging handling
				if (i == 200) {
					// do logging
					logger.debug(
						"Error Message (id ="
							+ sentMsg.getId()
							+ ") is sent to targetUrl: ["
							+ targetUrl
							+ "] successfully - Time At: ["
							+ timestamp.toString()
							+ "]");
				} else {
					// do logging
					logger.error(
						"Error Message (id ="
							+ sentMsg.getId()
							+ ") cannot be sent back to  targetUrl: ["
							+ targetUrl
							+ "] - Time At: ["
							+ timestamp.toString()
							+ "]");
				}
			} else {
				logger.error("NO HTTP connection was set up");
			}
		} catch (Exception e) {
			logger.error(
				"Failed to send error message to targetUrl, Reason: "
					+ e.getMessage(),
				e);
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception e) {
				logger.error(
					"Couldn't close outputstream in ErrorHandlerThread finally block.",
					e);
			}
		}
	}

	private void createNewErrorThread() {
		errorThread = new ErrorHandlerThread();
		logger.info("Error Handler Thread instantiated");
		errorThread.start();
	}

	/**
	 * Returns the errorVector.
	 * @return Vector
	 */
	public static Vector getErrorVector() {
		return errorVector;
	}

	/**
	 * Sets the errorVector.
	 * @param errorVector The errorVector to set
	 */
	public static void setErrorVector(Vector errorVector) {
		ErrorHandlerThreadManager.errorVector = errorVector;
	}

	/**
	 * Returns the dispatchMode.
	 * @return String
	 */
	public static String getDispatchMode() {
		return dispatchMode;
	}

	/**
	 * Returns the errorThreadSleepInteval.
	 * @return int
	 */
	public static int getErrorThreadSleepInterval() {
		return errorThreadSleepInterval;
	}

	/**
	 * Sets the dispatchMode.
	 * @param dispatchMode The dispatchMode to set
	 */
	public static void setDispatchMode(String dispatchMode) {
		// Validate dispatch mode to make sure it is valid before setting it
		if (dispatchMode.equalsIgnoreCase(DIRECT_DISPATCH_MODE) ||
			dispatchMode.equalsIgnoreCase(DELAY_DISPATCH_MODE)) {
			ErrorHandlerThreadManager.dispatchMode = dispatchMode;
			}
		else {
			// Invalid dispatch mode specified. use DIRECT by default
			logger.error("Invalid dispatch mode specified. Using default '" + DIRECT_DISPATCH_MODE + "' mode");
			ErrorHandlerThreadManager.dispatchMode = DIRECT_DISPATCH_MODE;
		}
	}

	/**
	 * Sets the errorThreadSleepInteval.
	 * @param errorThreadSleepInteval The errorThreadSleepInteval to set
	 */
	public static void setErrorThreadSleepInterval(int errorThreadSleepInteval) {
		ErrorHandlerThreadManager.errorThreadSleepInterval =
			errorThreadSleepInteval;
	}

}
