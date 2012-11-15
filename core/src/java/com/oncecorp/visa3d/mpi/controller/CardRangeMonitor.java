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
import com.oncecorp.visa3d.mpi.configuration.MerchantMetaInfo;
import com.oncecorp.visa3d.mpi.domain.CardRangeManager;
import com.oncecorp.visa3d.mpi.domain.payment.CRReqMessage;
import com.oncecorp.visa3d.mpi.domain.payment.CRResMessage;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorMessage;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.ID_Generator;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageEngine;
import com.oncecorp.visa3d.mpi.messaging.MessageGenerator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import java.util.Calendar;

import org.apache.log4j.Logger;

/**
* The card range monitor runs in its own thread. It's instantiated by 
* AuthenticatorServlet and is reponsible for refreshing local card range 
* cache by consulting Visa Directory. 
* 
* A CRReq message would be sent to Visa Directory to request a copy of 
* enrolled card range or card range updates.
* 
* A CRRes message is expected to be received as response of CRReq message. The 
* CRRes message may contains most recent card range updates.
* 
* @version 0.1 Oct 17, 2002
* @author Alan Zhang
*/
public class CardRangeMonitor extends Thread {

	public final static String THREAD_NAME = "CardRangeMonitor";

	/**
	 * Local log4j Logger
	 */
	private Logger logger = MPILogger.getLogger(CardRangeMonitor.class.getName());

	/**
	 * Monitor thread sleep interval
	 */
	private static long sleepInterval;

	/**
	 * Cache expiry interval
	 */
	private static long cacheExpiryInterval;

	/**
	 * Last refresh time for each card range
	 */
	private static long lastVBVRefreshTime;
	private static long lastSCRefreshTime;

	/**
	 * Default constructor
	 */
	public CardRangeMonitor() {
		super(THREAD_NAME);

		// Set default env value
		setSleepInterval(5 * 60 * 1000);		// Default time is 5 mins
		setCacheExpiryInterval(5 * 60 * 1000);	// Default is 5 mins
		
		// Set last refresh time for both protocol
		setLastRefreshTime(
			MPIConfigDefinition.PROTOCOL_MASTERCARD_SECURE_CODE,
			Calendar.getInstance().getTime().getTime());
		setLastRefreshTime(
			MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE,
			Calendar.getInstance().getTime().getTime());
	}

	/**
	 * Constructor
	 */
	public CardRangeMonitor(long sleepInt, long cacheExpiryInt) {
		super(THREAD_NAME);

		logger.debug(
			"Initializing CardRangeMonitor. [Sleep interval: "
				+ sleepInt
				+ " mins, Cache expiry: "
				+ cacheExpiryInt
				+ " mins]");

		//Convert mins to milliseconds
		setSleepInterval(sleepInt * 60 * 1000);
		setCacheExpiryInterval(cacheExpiryInt * 60 * 1000);

		// Set last refresh time for both protocol
		setLastRefreshTime(
			MPIConfigDefinition.PROTOCOL_MASTERCARD_SECURE_CODE,
			Calendar.getInstance().getTime().getTime());
		setLastRefreshTime(
			MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE,
			Calendar.getInstance().getTime().getTime());
	}

	/**
	 * Override run() method 
	 */
	public void run() {
		getCardRanges();

		try {
			while (true) {
				logger.debug("CardRangeMonitor sleeps for " + getSleepInterval() + " milliseconds...");
				Thread.sleep(getSleepInterval());

				getCardRanges();
			}
		}
		catch (InterruptedException ie) {
			logger.debug("CardRangeMonitor thread is interrupted by other thread.");
		}
	}

	/**
	 * Get the card ranges for both authentication protocol if they are
	 * configured as supporting that functionality
	 */
	public void getCardRanges() {
		// Check which protocol is configured for credit card range and use it to 
		// fetch the proper information
		Boolean scSupport, vbvSupport;

		logger.debug("Card Range Monitor is verifying if card ranges retrieval is needed");
		
		// Try to load configuration parameter about SecureCode Card Range support.
		try {
			scSupport = new Boolean((String) Config.getConfigReference().getConfigData(
						MPIConfigDefinition.SECURECODE_RANGE_SUPPORT));
		}
		catch (ConfigurationException cfgExcep) {
			// By default, if these properties are not configured, then we do not support.
			scSupport = Boolean.FALSE;
		}
		

		// Try to load configuration parameter about VbV Card Range support.
		try {
			vbvSupport = new Boolean((String) Config.getConfigReference().getConfigData(
						 MPIConfigDefinition.VBV_RANGE_SUPPORT));
		}
		catch (ConfigurationException cfgExcep) {
			// By default, if these properties are not configured, then we do not support.
			vbvSupport = Boolean.FALSE;
		}

		// Load the credit card range for each supported protocol
		if (scSupport.booleanValue()) {
			logger.debug("Trying to fetch card ranges for SecureCode");
			getCardRangeForType(MPIConfigDefinition.PROTOCOL_MASTERCARD_SECURE_CODE);
		}
		if (vbvSupport.booleanValue()) {
			logger.debug("Trying to fetch card ranges for VbV");
			getCardRangeForType(MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE);
		}
	}

	/**
	 * Acquires a copy of card ranges or new updates for a specific protocol
	 * Work flow is:
	 * <ul>
	 * <li>Creates a new CRReq message</li>
	 * <li>Sends CRReq message to Visa Directory</li>
	 * <li>Receives CRRes message from Visa Directory which contains card ranges</li>
	 * <li>Applies card ranges or updates to buffered cache</li>
	 * <li>Refreshes main cache</li>
	 * <li>Set serialNumber for future use</li>
	 * </ul>
	 * 
	 * @param aProtocolType	Protocol for which we need to acquire the card range
	 */
	private void getCardRangeForType(String aProtocolType) {
		logger.debug("Retrieving card range for " + aProtocolType);
		
		// Check if we need to load the range for this protocol
		long rightnow = Calendar.getInstance().getTime().getTime();
		long elapsed = rightnow - getLastRefreshTime(aProtocolType);
		boolean loaded = CardRangeManager.isCardRangeLoaded(aProtocolType);
		logger.debug("Card range cache status --- Elapsed time: " + elapsed + ", Loaded: " + loaded);

		if ((elapsed > getCacheExpiryInterval()) || !loaded) {
			logger.debug(
				loaded
					? "Card Range cache expired. Start retrieving new updates..."
					: "Card Range cache not loaded. Start retrieving new card range...");

			try {
				// list current cached card ranges
				if (CardRangeManager.getBufferedBegins(aProtocolType) != null) {
					Object[] begins = CardRangeManager.getBufferedBegins(aProtocolType).toArray();
					Object[] ends = CardRangeManager.getBufferedEnds(aProtocolType).toArray();
					if (begins != null) {
						logger.debug("Current ranges:");
						for (int i = 0; i < begins.length; i++) {
							logger.debug(
								"  " + ((Long) begins[i]).toString() + " ~ " + ((Long) ends[i]).toString());
						}
					}
				}

				Config cfg = Config.getConfigReference();
				logger.debug("Got Config reference.");

				CRReqMessage crreq = getCRReqMessage(aProtocolType, cfg);

				logger.debug("CRReq message created. Sending to VisaDir...");
				Message response = null;
				try {
					response = sendToDirectory(aProtocolType, cfg, crreq);
					
					/* 
					 * [Martin's Note: Mar 3, 2003 3:30:44 PM]
					 * The response we are getting is losing protocol information 
					 * in the processing part because we are processing each message as
					 * an atomic unit. We need to add that protocol information here in
					 * order to properly update the right card range.
					 * 
					 */
					if (response instanceof CRResMessage) {
						CRResMessage crres = (CRResMessage) response;
						crres.setProtocol(aProtocolType);
					}
					
						
				}
				catch (Exception e) {
					logger.error("Exception caught during CR acquiring. ", e);
					response = null;
				}

				if (needsResetCRCache(crreq, response, cfg)) {
					resetCache(aProtocolType);
					logger.info("CR cache reset finished.");
					return;
				}
				else {
					MessageEngine.process(response);
					setLastRefreshTime(aProtocolType, Calendar.getInstance().getTime().getTime());
					logger.debug("lastRefreshTime reset to : " + getLastRefreshTime(aProtocolType));
				}

			}
			catch (MessagingException msge) {
				logger.error(
					"MessagingException caught during card range refreshing."
						+ " (Local problem, no Error message sent to visa dir.");
				logger.error(
					"Error code: "
						+ msge.getErrorCode()
						+ ", ErrorMessage: "
						+ msge.getErrorMsg()
						+ ", VendorCode: "
						+ msge.getVendorCode()
						+ ", Message: "
						+ msge.getMessage()
						+ ", LocalizedMessage: "
						+ msge.getLocalizedMessage());
				resetCache(aProtocolType);
				logger.info("CR cache reset finished.");
			}
			catch (Exception e) {
				logger.error(
					"Unknown exception caught during card range refreshing. CR cache remains unchanged.");
			}
		}
	}

	private void logErrorMessage(Message response) {
		ErrorMessage err = (ErrorMessage) response;
		logger.error(
			"Error message received as reponse of CRReq. "
				+ "Code: "
				+ err.getErrorCode()
				+ ", Detail: "
				+ err.getErrorDetail()
				+ ", VendorCode: "
				+ err.getVendorCode()
				+ ", Message: "
				+ err.getErrorMessage());
	}

	private Message sendToDirectory(String aProtocolType, Config cfg, CRReqMessage crreq)
		throws CommunicatorException, MessagingException, ConfigurationException {

		/*
		 * [Alan's note - Feb 24, 2003]
		 * Added multiple directory URL support. 
		 * Also added protocol support for both VISA 3D-Secure and MasterCard SecureCode
		 * 
		 */
		String[] urls = cfg.getDirectoryURLs(aProtocolType);
		
				
		Message response = null;
		for (int i = 0; i < urls.length; i++) {
			if (!XMLUtil.isNull(urls[i])) {
				try {
						
							response = (new Communicator()).send(crreq, urls[i]);
	
							if (response != null) {
									logger.debug("Got card range from " + urls[i]);
									return response;
							}
													
						
					 }
				
					
				
				catch (CommunicatorException ce) {
					logger.error("Failed to get card range from " + urls[i]);
				}
			}	
			
		}
			
		
		return response;
	}

	private CRReqMessage getCRReqMessage(String aProtocolType, Config cfg)
		throws MessagingException, ConfigurationException {
		CRReqMessage crreq =
			(CRReqMessage) MessageGenerator.create(CRReqMessage.MSG_TYPE, CRReqMessage.MSG_VERSION);
		logger.debug("CRReq generated by MessageGenerator.");

		crreq.setId(ID_Generator.getUniqueId());

		String merID = cfg.getFirstMerchantID(aProtocolType);
		MerchantMetaInfo mmi = (MerchantMetaInfo) cfg.getMerchantMetaInfo(merID);
		
		if (mmi != null) {
			crreq.setMerchantAcqBIN(mmi.getAcquirerBIN());
			crreq.setMerchantID(mmi.getMerchantID());
			crreq.setMerchantPassword(mmi.getMerchantPassword());
			String sn = CardRangeManager.getSerialNumber(aProtocolType);
			if ((sn != null) && (sn.length() != 0)) {
				logger.debug("Set previoius serialNumber: " + sn);
				crreq.setSerialNumber(sn);
			}
			
			logger.debug("CRReq message value set.");
		}
		else {
			// No merchant associated to that protocol
			logger.fatal("Unable to retrieve a merchant associated to the protocol" + aProtocolType);
		}

		// MessageEngine.setThreadLocalMerchantID(crreq.getMerchantID());
		// crreq = (CRReqMessage) MessageEngine.process(crreq);
		return crreq;
	}

	private void resetCache(String aProtocolType) {
		CardRangeManager.setSerialNumber(aProtocolType, null);
		CardRangeManager.setBufferedBegins(aProtocolType, null);
		CardRangeManager.setBufferedEnds(aProtocolType, null);
		CardRangeManager.setCardRangeLoaded(aProtocolType, false);
		(new CardRangeManager()).refresh(aProtocolType);
		logger.debug("CR has been reset.");
	}

	private boolean needsResetCRCache(CRReqMessage crreq, Message response, Config cfg) {
		if (response == null) {
			return false;
		}

		if (response instanceof ErrorMessage) {
			//Log error info and try again
			logErrorMessage(response);
			return true;
		}

		if (response instanceof CRResMessage) {
			CRResMessage crres = (CRResMessage) response;
			logger.debug("CRRes message received as response of CRReq: " + crres.toString());

			try {
				crres.validate();
			}
			catch (MessagingException me) {
				logger.error("CRRes is invalid.");
				try {
					// call MessageGenerator to create an empty ErrorMessage
					ErrorMessage errMsg =
						(ErrorMessage) MessageGenerator.create(
							ErrorMessage.MSG_TYPE,
							ErrorMessage.MSG_VERSION);

					// set value for this err msg			
					errMsg.setId(crres.getId());
					errMsg.setErrorCode(me.getErrorCode());
					errMsg.setErrorMessage(me.getErrorMsg());
					errMsg.setErrorDetail(me.getErrorDetail());
					errMsg.setVendorCode(me.getVendorCode());

					// Send error to responding entity
					ErrorRequest er = new ErrorRequest();
					er.setToUrl((String) cfg.getConfigData(MPIConfigDefinition.VISA_DIR_URL_1));
					er.setMsg(errMsg);

					(new ErrorHandlerThreadManager()).dispatchErrorMessage(er);
					logger.info("Error notification dispatched to ErrorHandlerManager.");
				}
				catch (Exception e) {
					logger.error(
						"Exception caught during invalid CRRes process. "
							+ "The Error message could not be sent to visa dir.",
						e);
				}

				return false;
			}

			if (!response.getId().equals(crreq.getId())) {
				logger.error("CRRes ID not matched with CRReq.");

				try {
					// call MessageGenerator to create an empty ErrorMessage
					ErrorMessage errMsg =
						(ErrorMessage) MessageGenerator.create(
							ErrorMessage.MSG_TYPE,
							ErrorMessage.MSG_VERSION);

					// set value for this err msg			
					errMsg.setId(crres.getId());
					errMsg.setErrorCode(ErrorCodes.ERROR_CODE_5);
					errMsg.setErrorMessage(ErrorCodes.ERROR_MESSAGE_5);
					errMsg.setErrorDetail("CRRes message ID not match with CRReq.");
					errMsg.setVendorCode("CRRes message ID not match with CRReq.");

					// Send error to responding entity
					ErrorRequest er = new ErrorRequest();
					er.setToUrl((String) cfg.getConfigData(MPIConfigDefinition.VISA_DIR_URL_1));
					er.setMsg(errMsg);

					(new ErrorHandlerThreadManager()).dispatchErrorMessage(er);
					logger.info("Error notification dispatched to ErrorHandlerManager.");

				}
				catch (Exception e) {
					logger.error(
						"Exception caught during invalid CRRes process. "
							+ "The Error message could not be sent to visa dir.",
						e);
				}

				return false;
			}

			if ((crres.getIreqCode() != null) && (crres.getIreqCode().length() != 0)) {
				logger.error(
					"CRRes message is valid but contains iReqCode: "
						+ crres.getIreqCode()
						+ ", iReqDetail: "
						+ crres.getIreqDetail()
						+ ", vendorCode: "
						+ crres.getVendorCode());
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the cacheExpiryInterval.
	 * @return long
	 */
	public static long getCacheExpiryInterval() {
		return cacheExpiryInterval;
	}

	/**
	 * Returns the lastRefreshTime.
	 * @return long
	 */
	public static long getLastRefreshTime(String aProtocolType) {
		if (aProtocolType.equalsIgnoreCase(MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE)) {
			return lastVBVRefreshTime;
		}
		else if (aProtocolType.equalsIgnoreCase(MPIConfigDefinition.PROTOCOL_MASTERCARD_SECURE_CODE)) {
			return lastSCRefreshTime;
		}
		else {
			return 0;
		}
	}

	/**
	 * Returns the sleepInterval.
	 * @return long
	 */
	public static long getSleepInterval() {
		return sleepInterval;
	}

	/**
	 * Sets the cacheExpiryInterval.
	 * @param cacheExpiryInterval The cacheExpiryInterval to set
	 */
	public static void setCacheExpiryInterval(long cacheExpiryInterval) {
		CardRangeMonitor.cacheExpiryInterval = cacheExpiryInterval;
	}

	/**
	 * Sets the lastRefreshTime.
	 * @param lastRefreshTime The lastRefreshTime to set
	 */
	public static void setLastRefreshTime(String aProtocolType, long lastRefreshTime) {
		if (aProtocolType.equalsIgnoreCase(MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE)) {
			CardRangeMonitor.lastVBVRefreshTime = lastRefreshTime;
		}
		else if (aProtocolType.equalsIgnoreCase(MPIConfigDefinition.PROTOCOL_MASTERCARD_SECURE_CODE)) {
			CardRangeMonitor.lastSCRefreshTime = lastRefreshTime;
		}
	}

	/**
	 * Sets the sleepInterval.
	 * @param sleepInterval The sleepInterval to set
	 */
	public static void setSleepInterval(long sleepInterval) {
		CardRangeMonitor.sleepInterval = sleepInterval;
	}

}
