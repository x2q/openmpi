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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.ConfigurationException;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorMessage;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.DomToMsgConverter;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageEngine;
import com.oncecorp.visa3d.mpi.messaging.MessageGenerator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.security.SecurityManager;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import com.sun.net.ssl.internal.www.protocol.https.Handler;
import com.sun.net.ssl.HttpsURLConnection;
import com.sun.net.ssl.HostnameVerifier;

import javax.net.ssl.SSLSession;

/**
 * The Communicator class is responsible for sending 3-D
 * Secure messages to external VISA computing entities in order to
 * carry on the authentication request specified by the client.
 *
 * @version $Revision: 37 $
 * @author	Alan Zhang
 */
public class Communicator {
	/**
	 * Local Log4J logger
	 */
	public Logger logger = MPILogger.getLogger(Communicator.class.getName());

	/**
	 * Send message to external entity
	 * @param msg The message String to be sent
	 * @param toEntity The entity URI
	 * @return The response message string
	 */
	public Message send(Message inMsg, String toEntity)
		throws CommunicatorException, MessagingException {
		//Initialize IO stream
		OutputStreamWriter out = null;
		BufferedReader in = null;
		Document resDom = null;
	
	//  Teodora added on March 27 for retry strategy 
	
		int numberOfRetries;
		long timeBetweenRetries;
		// fetch the proper information
		// Try to load configuration parameter NUMBER_OF_RETRIES.
		try { 
			
			numberOfRetries = Integer.parseInt((String) Config.getConfigReference().getConfigData(
						MPIConfigDefinition.NUMBER_OF_RETRIES));
		}

		
		catch (ConfigurationException cfgExcep) {
			// By default, if this property is not configured, it is set to 0.
			numberOfRetries = 0;
		}
		
		// Try to load configuration parameter TIME_BETWEEN_RETRIES.
		if (numberOfRetries != 0) {
			try {
				timeBetweenRetries = Long.parseLong((String) Config.getConfigReference().getConfigData(
						MPIConfigDefinition.TIME_BETWEEN_RETRIES));
			}
			catch (ConfigurationException cfgExcep) {
			// By default, if this property is not configured, it is set to 0.
			timeBetweenRetries = 0;
			}
		}
		else timeBetweenRetries = 0;
		
		
		try {
			this.logger.debug("Sending message to VISA Dir: " + toEntity);

			//Initialize connection
			boolean initialOK =
				SecurityManager.getInstance().initializeConnection();
			if (!initialOK) {
				this.logger.error(
					"Failed to initialize connection parameters.");
				throw new CommunicatorException("Failed to initialze connection parameters.");
			}

            /**
             * The following change done by Gang, to change the HttpConnection to
             * directly use SUN implementation
             */
            /**
             * The following old one is replaced
			//Establish connection to servlet
			URL url = new URL(toEntity);
			URLConnection conn = url.openConnection();
            */
           URL url;
           if ( toEntity != null &&
                toEntity.trim().toLowerCase().startsWith("https:") )
               url = new URL(null, toEntity, new Handler() );
           else
               url = new URL( toEntity );
              
           	
           
            URLConnection conn = null;
            int j;
            
               // TEODORA: try to establish the connection numberOfRetries times with timeBetweenRetries
					for (j = 0; j <= numberOfRetries; j++) {
					
						
						conn=sendOver(url);
												
						if (!conn.equals (null)) {
								logger.debug("Established Connection");
								logger.debug("The number of retries set to ..." + numberOfRetries);
								logger.debug("The number of attempts to connect to directory was " + (++j));
								break;
						}
						
						else {
							logger.debug("Thread will sleep for time between retries..." + timeBetweenRetries);	
							Thread.sleep(timeBetweenRetries);
							logger.debug("Thread woke up.");	
						}	            
               
					}
                    if  (j > numberOfRetries && conn.equals(null)) throw new CommunicatorException("Unsuccessfull connection");      
			

			//Prepare for both input and output
			conn.setDoInput(true);
			conn.setDoOutput(true);

			//Turn off caching
			conn.setUseCaches(false);

			// Disable SSL hostname verification
			((HttpsURLConnection)conn).setHostnameVerifier(
					new HostnameVerifier() {
                    		  public boolean verify(String hostname, SSLSession session) {
                    				System.out.println("Hostname of server " + hostname);
                    				return true;
                    		  }

							  public boolean verify(String hostname, String session) {
                    				return true;
                    		  }

					});

			//Convert inMsg to string
			String msg = inMsg.toString();
			this.logger.debug("Content to VISA Dir: " + msg);

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
			this.logger.debug("Sending finished.");

			//Create input stream
			in =
				new BufferedReader(
					new InputStreamReader(conn.getInputStream()));

			//Read response
			String resStr = in.readLine();
			String line;
			while ((line = in.readLine()) != null) {
				resStr += line;
			}

			//Close input stream
			in.close();
			this.logger.debug(
				"Receiving finished. Content received: " + resStr);

			//Received response should be encoded in "x-www-form-urlencoded" format.
			//Decode it back to String and convert request XML string into DOM with
			//validation turned off

			resDom = XMLUtil.createDocument(resStr);
			this.logger.debug("Received message is converted to Document.");

			if (resDom == null) {
				logger.error("NULL response received by Communicator.");
				throw new MessagingException(
					"UNKNOWN",
					ErrorCodes.ERROR_CODE_1,
					ErrorCodes.ERROR_MESSAGE_1,
					"ThreeDSecure",
					"Root element missing.",
					"Root element missing.");
			}

			//Convert DOM to message
			Message newMsg = new DomToMsgConverter().convert(resDom);

			// Validate structural integrity of the new message
			// If it is not valid then a MessagingException will be thrown
			newMsg.validate();

			//Check version support
			MessageGenerator.checkVersionSupport(newMsg);

			return newMsg;

		} catch (MessagingException excep) {
			this.logger.error(
				"Failed to convert message in Communicator.",
				excep);

			// call MessageGenerator to create an empty ErrorMessage
			ErrorMessage errMsg =
				(ErrorMessage) MessageGenerator.create(
					ErrorMessage.MSG_TYPE,
					ErrorMessage.MSG_VERSION);

			if ((excep.getId() == null)
					|| excep.getId().equalsIgnoreCase("UNKNOWN")) {
				excep.setId(inMsg.getId());
			}

			// set value for this err msg
			errMsg.setId(excep.getId());
			errMsg.setErrorCode(excep.getErrorCode());
			errMsg.setErrorMessage(excep.getErrorMsg());
			errMsg.setErrorDetail(excep.getErrorDetail());
			errMsg.setVendorCode(excep.getVendorCode());

			errMsg = (ErrorMessage) MessageEngine.process(errMsg);

			// Send error to responding entity
			ErrorRequest er = new ErrorRequest();
			er.setToUrl(toEntity);
			er.setMsg(errMsg);

			(new ErrorHandlerThreadManager()).dispatchErrorMessage(er);
			logger.info(
				"Error notification dispatched to ErrorHandlerManager.");

			// throw error back to Merchant as usual
			throw excep;

		} catch (ConnectException ex) {
			//Log the error
			this.logger.error("Can't connect to the server. Please be sure that the server is up and running.");

			//Throws CommunicatorException
			throw new CommunicatorException(ex.getMessage());
			
		} catch (Exception e) {
			//Log the error
			this.logger.error("Communication error with the server. Please be sure that the server is up and running.");

			//Throws CommunicatorException
			throw new CommunicatorException(e.getMessage());
		

		

		} finally {
			//Clean-up
			try {
				if (out != null)
					out.close();
				if (in != null)
					in.close();
			} catch (IOException ioe) {
				this.logger.error("Can't close IO stream", ioe);
			}

			if (resDom != null)
				resDom = null;
		}
	}

		/*
		 * [Teodora's note - March 27, 2003]
		 * Added method for retry strategy for Visa/Master card Directory 
		 *  
		 */

   public URLConnection sendOver(URL url) {
    
    
    	URLConnection conn;
   		try{
			logger.debug("Establishing Connection");
			conn = url.openConnection();
			return  conn;
		} catch (IOException ioe) {
			this.logger.error("IO Exception during establishing connection with Visa/ Master card directory", ioe);
			return null;
		}	
			
		
			
		

    
    }



	public Message receive(String str) throws CommunicatorException {
		try {
			//Convert to DOM document first
			Document dom = XMLUtil.createDocument(str, false);

			//Convert DOM to message
			return (new DomToMsgConverter()).convert(dom);
		} catch (Exception e) {
			this.logger.error("Communication error.", e);
			throw new CommunicatorException("Communication error.");
		}
	}

}