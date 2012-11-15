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

package com.oncecorp.visa3d.mpi.domain.payment;

import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.controller.ErrorHandlerThreadManager;
import com.oncecorp.visa3d.mpi.controller.ErrorRequest;
import com.oncecorp.visa3d.mpi.domain.CardRange;
import com.oncecorp.visa3d.mpi.domain.CardRangeManager;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageGenerator;
import com.oncecorp.visa3d.mpi.messaging.MessageProcessor;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;

import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * CRRes message Processor. Wheneven MessageEngine processes a CRRes message, this 
 * processor would be instantiated and the process() method would be called.
 * 
 * @version 0.1 Oct 17, 2002
 * @author	Alan Zhang
 */
public class CRResProcessor implements MessageProcessor {
	/**
	 * PAResProcessor type
	 */
	public final static String PROCESSOR_TYPE = "CRRes";

	/**
	 * Local Log4J logger
	 */
	public Logger logger = MPILogger.getLogger(CRResProcessor.class.getName());

	/**
	 * This method initializes or updates Card Range cache. 
	 */
	public Message process(Message inMsg) throws MessagingException {
		//Sanity check
		if (!(inMsg instanceof CRResMessage)) {
			this.logger.error(
				"CRResProcessor can only process CRResMessage. Incompatible message passed in.");
			throw new MessagingException("Incompatible message type.");
		}

		CRResMessage crres = (CRResMessage) inMsg;
		String crresProtocol = crres.getProtocol();	
		
		//Get cached serialNumber
		String preSerialNumber = CardRangeManager.getSerialNumber(crresProtocol);
			
		if ((preSerialNumber == null)
			|| (!preSerialNumber.equals(crres.getSerialNumber()))) {
			logger.debug(
				"CR needs to be initialized or updated. [pre-/current- SerialNumber: "
					+ preSerialNumber
					+ " / "
					+ crres.getSerialNumber()
					+ "]");

			//Cache current serialNumber
			CardRangeManager.setSerialNumber(crresProtocol, crres.getSerialNumber());

			CardRange[] crs = crres.getCr();
			TreeSet bufferedBegins = CardRangeManager.getBufferedBegins(crresProtocol);
			TreeSet bufferedEnds = CardRangeManager.getBufferedEnds(crresProtocol);

			if (bufferedBegins == null) {
				logger.debug("Instantiate buffered cache");
				bufferedBegins = new TreeSet();
				bufferedEnds = new TreeSet();
			}

			//Initialize/Update buffered card range
			for (int i = 0; i < crs.length; i++) {
				CardRange cr = crs[i];
				if (cr.getAction().equalsIgnoreCase("A")) {
					logger.debug(
						"Add new range: "
							+ cr.getBegin().toString()
							+ " ~ "
							+ cr.getEnd().toString());
					bufferedBegins.add(cr.getBegin());
					bufferedEnds.add(cr.getEnd());
				} else {
					if (cr.getAction().equalsIgnoreCase("D")) {
						logger.debug(
							"Remove range: "
								+ cr.getBegin().toString()
								+ " ~ "
								+ cr.getEnd().toString());

						if ((bufferedBegins.contains(cr.getBegin()))
							&& (bufferedEnds.contains(cr.getEnd()))) {
							bufferedBegins.remove(cr.getBegin());
							bufferedEnds.remove(cr.getEnd());
						} else {
							logger.error(
								"The range to be removed is not exist in current cache.");
							handleError(crres, "CR.action");
							return inMsg;
						}
					} else {
						logger.error(
							"Unknown action: "
								+ cr.getAction()
								+ " for range: "
								+ cr.getBegin().toString()
								+ " ~ "
								+ cr.getEnd().toString());
						handleError(crres, "CR.action");
						return inMsg;
					}
				}
			}

			logger.debug("Buffered cache updated.");
			CardRangeManager.setBufferedBegins(crresProtocol, bufferedBegins);
			CardRangeManager.setBufferedEnds(crresProtocol, bufferedEnds);

			logger.debug("Set cardRangeLoaded to false before refreshing.");
			CardRangeManager.setCardRangeLoaded(crresProtocol, false);

			//Refresh main cache
			 (new CardRangeManager()).refresh(crresProtocol);
			logger.debug("CR refreshed.");

			logger.debug("Set cardRangeLoaded to true.");
			CardRangeManager.setCardRangeLoaded(crresProtocol, true);
		}

		//return message itself
		return inMsg;
	}

	private void handleError(Message aMessage, String error) {
		CRResMessage crres = (CRResMessage) aMessage;
		String crresProtocol = crres.getProtocol();
		try {
			logger.info("Reseting CR...");
			//Reset cache first
			CardRangeManager.setSerialNumber(crresProtocol, null);
			CardRangeManager.setBufferedBegins(crresProtocol, null);
			CardRangeManager.setBufferedEnds(crresProtocol, null);
			CardRangeManager.setCardRangeLoaded(crresProtocol, false);
			(new CardRangeManager()).refresh(crresProtocol);
			logger.info("CR has been reset.");

			//Send notification error message to visa dir
			logger.info("Sending notification to visa dir...");
			Config cfg = Config.getConfigReference();

			// call MessageGenerator to create an empty ErrorMessage
			ErrorMessage errMsg =
				(ErrorMessage) MessageGenerator.create(
					ErrorMessage.MSG_TYPE,
					ErrorMessage.MSG_VERSION);

			// set value for this err msg			
			errMsg.setId(crres.getId());
			errMsg.setErrorCode(ErrorCodes.ERROR_CODE_5);
			errMsg.setErrorMessage(ErrorCodes.ERROR_MESSAGE_5);
			errMsg.setErrorDetail(error);
			errMsg.setVendorCode(error);

			// Send error to responding entity
			ErrorRequest er = new ErrorRequest();
			er.setToUrl(
				(String) cfg.getConfigData(MPIConfigDefinition.VISA_DIR_URL_1));
			er.setMsg(errMsg);

			(new ErrorHandlerThreadManager()).dispatchErrorMessage(er);
			logger.info(
				"Error notification dispatched to ErrorHandlerManager.");
		} catch (Exception e) {
			logger.error(
				"Exception caught during invalid CRRes process. "
					+ "The Error message could not be sent to visa dir.",
				e);
		}

	}
}