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
// import javax.net.ssl.HttpsURLConnection; ?

/**
 * Description: This thread is resposible for sending error message
 * back to VisaDir or ACS when MPI receives invalid VERes or PARes message
 *
 * @version 0.1 Oct 18, 2002
 * @author Jerome Mourits
 */
public class ErrorHandlerThread extends Thread{

	private Logger logger = MPILogger.getLogger(ErrorHandlerThread.class.getName());
	private Vector toBeSent = new Vector();

	private static int interval;

	public void run() {

		setInterval(ErrorHandlerThreadManager.getErrorThreadSleepInterval());

		//this is not correct should be changed later
		//- only shut down when the server is shutdown
		while (true){

			if (ErrorHandlerThreadManager.getErrorVector().size() > 0) {
				synchronized (ErrorHandlerThreadManager.lock) {
					toBeSent = new Vector(ErrorHandlerThreadManager.getErrorVector());
					ErrorHandlerThreadManager.setErrorVector(new Vector());
				}
			}

			if (toBeSent.size() > 0) {
				logger.debug("Process error requests: " + toBeSent.size());
				for (int i=0; i<toBeSent.size(); i++) {
					logger.debug("process error request " + i + "...");
					ErrorRequest er = (ErrorRequest) toBeSent.get(i);
					send(er.getToUrl(), er.getMsg());
					logger.debug("process error request " + i + " finished.");
				}
				toBeSent = new Vector();
			} else {
				logger.debug("No ErrorRequest to be processed.");
			}

			try {
				sleep(getInterval());
			} catch (InterruptedException e) {
				logger.debug("ErrorHandlerThread interrupted by other thread.", e);
			}
		}
	}


    // start sending to targetUrl ....
    private void send(String targetUrl, Message sentMsg) {
		// do logging
		logger.debug("ErrorHandlerThread is called");

		//Initialize IO stream
		OutputStreamWriter out = null;

		try {
			//Precheck id
			if ((sentMsg.getId() != null) && (sentMsg.getId().equals("UNKNOWN")) ) {
				sentMsg.setId(ID_Generator.getUniqueId());
				logger.debug("UNKNOWN id replaced with new id: " + sentMsg.getId());
			}

            logger.debug("Sending message to targetUrl: " + targetUrl);

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
			conn.setRequestProperty("Content-Type", "application/xml; charset=\"utf-8\"");

			//Set content length
			conn.setRequestProperty("Content-Length", Integer.toString(msg.length()));

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
                  logger.debug("Error Message (id ="+sentMsg.getId()
                              +") is sent to targetUrl: ["+targetUrl
                              +"] successfully - Time At: ["
                              +timestamp.toString()+"]");
               } else {
                  // do logging
                  logger.error("Error Message (id ="+sentMsg.getId()
                              +") cannot be sent back to  targetUrl: ["+targetUrl
                              +"] - Time At: ["
                              +timestamp.toString()+"]");
               }
            } else {
               logger.error("NO HTTP connection was set up");
            }
		} catch (Exception e) {
			logger.error("Failed to send error message to targetUrl, Reason: "+e.getMessage(), e);
		} finally {
			try {
			if (out != null) out.close();
			} catch (Exception e) {
				logger.error("Couldn't close outputstream in ErrorHandlerThread finally block.", e);
			}
		}
	}
	/**
	 * Returns the interval.
	 * @return int
	 */
	public static int getInterval() {
		return interval;
	}

	/**
	 * Sets the interval.
	 * @param interval The interval to set
	 */
	public static void setInterval(int interval) {
		ErrorHandlerThread.interval = interval;
	}

}
