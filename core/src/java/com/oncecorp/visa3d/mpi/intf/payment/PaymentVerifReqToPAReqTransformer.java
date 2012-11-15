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

package com.oncecorp.visa3d.mpi.intf.payment;

import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.ConfigurationException;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.controller.AuthenticatorSession;
import com.oncecorp.visa3d.mpi.domain.payment.PAReqMessage;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.ID_Generator;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageTransformer;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;

import org.apache.log4j.Logger;

import com.ibm.xml.dsig.util.Base64;

/**
 * Description: Implement transformer class for transformation:
 * PaymentVerifReq Message to PAReq Message.
 * 
 * @version 0.1 Aug 9, 2002
 * @author	Jun Shi
 */

public class PaymentVerifReqToPAReqTransformer implements MessageTransformer {

	//  PaymentVerifReqToPAReqTransformer type
	public final static String TRANSFORMER_TYPE = "PaymentVerifReqToPAReq";

	// Local Log4J logger
	private Logger logger =
		MPILogger.getLogger(PaymentVerifReqToPAReqTransformer.class.getName());

	/**
	 * Implement MessageTransformer interface method: transform(fromMsg, toMsg)
	 *
	 * @param PaymentVerifReqMessage and an empty PAReqMessage
	 * @return a partly populated PAReqMessage
	 */
	public Message transform(Message fromMessage, Message toMessage)
		throws MessagingException {
		//Sanity check
		if (!(fromMessage instanceof PaymentVerifReqMessage)
			|| !(toMessage instanceof PAReqMessage)) {
			this.logger.error(
				"PaymentVerifReqToPAReqTransformer:"
					+ "Incompatible messsage passed in.");
			throw new MessagingException(
				"PaymentVerifReqToPAReqTransformer:"
					+ "Incompatible messsage passed in.");
		}

		/** type casting */
		PaymentVerifReqMessage pvreqMsg = (PaymentVerifReqMessage) fromMessage;
		PAReqMessage preqMsg = (PAReqMessage) toMessage;

		/** Initialize attributes by using passed-in PaymentVerifReqMessage */

		//Set merchant ID
		preqMsg.setMerID(pvreqMsg.getMerchantID());

		//Set merchant URL
		/*
		 * [Alan's Note: Dec 09, 2002]
		 * If URL provided, then set it. Otherwise PaymentVerifyReqProcessor would use 
		 * default URL from db.
		 */
		if ((pvreqMsg.getMerchantTermURL() != null)
			&& (pvreqMsg.getMerchantTermURL().trim().length() != 0)) {
			preqMsg.setMerURL(pvreqMsg.getMerchantTermURL());
		}
		
		/* 
		 * [Martin's Note: Feb 25, 2003 5:21:08 PM]
		 * For the HTML interface, we force the ACS popup to post the final PaRes
		 * directly to the MPI for validation. We cen fetch the MPI URL from the configuration object
		 */
		 final String currentInterface = AuthenticatorSession.instance().getPaymentInterface();
		 if (currentInterface.equalsIgnoreCase(AuthenticatorSession.HTML_INTERFACE)) {
		 	logger.debug("Retrieving the MPICore URL to replace the provided merchantURL.");
		 	try { 
				String serverURL = (String) Config.getConfigReference().getConfigData(MPIConfigDefinition.CORE_SERVER_URL);
				if (serverURL == null || serverURL.length() == 0) {
					throw new MessagingException("The Core Server URL is badly formed");
				}
				else {
					// We can redirect to the Core Server
					logger.debug("Bypassing Merchant URL so MPI will receive PARes response");
					preqMsg.setMerURL(serverURL);
				}
		 	}
		 	catch (ConfigurationException cfgExcep) {
		 		// Unable to retrieve that configuration value 
				throw new MessagingException("The URL for the Core Server is not specified" + cfgExcep.getMessage());
		 	}
		 	
		 }

		//Set Transaction ID
		/*
		 * [Alan's Note - Dec 11, 2002]
		 * Merchant provided transaction ID is identical with message ID
		 * 
		 * [Alan's Note - Jan 20, 2003]
		 * If merchant provided transactionId in PaymentVerifReq message, then copy it 
		 * over; otherwise we generate a Unique ID instead.
		 */
		if (pvreqMsg.getTransactionID() != null) {
			preqMsg.setPurXid(pvreqMsg.getTransactionID());
		} else {
			preqMsg.setPurXid(
				Base64.encode(ID_Generator.getUniqueId(20).getBytes()));
		}

		//Set Pruchase Date & Time
		preqMsg.setPurDate(pvreqMsg.getPurchaseDate());

		//Set Display Amount
		preqMsg.setPurDispAmount(pvreqMsg.getPurchaseAmount());

		//Set Purchase Amount
		preqMsg.setPurAmount(pvreqMsg.getPurchasePurchAmount());

		/** set Currency and Exponent */
		/* 
			* [Martin's Note: Oct 21, 2002 2:52:31 PM]
			* We can either force the use of currency code for each invocation or
			* use the default currency specified in the configuration file if there is none.
			*/
		/*
		 * [Alan's Note: Dec 09, 2002]
		 * If currency provided, then set it. Otherwise PaymentVerifyReqProcessor would use 
		 * default currency from db.
		 * 
		 * The exponent will be set by the processor as well.
		 */
		if ((pvreqMsg.getPurchaseCurrency() != null)
			&& (pvreqMsg.getPurchaseAmount().trim().length() != 0)) {
			// Currency is specified so use it
			preqMsg.setPurCurrency(pvreqMsg.getPurchaseCurrency());
		}

		//Set Order Description
		preqMsg.setPurDesc(pvreqMsg.getPurchaseDesc());

		//Set Recurring Frequency
		preqMsg.setPurRecurFrequency(pvreqMsg.getPurchaseFrequency());

		//Set Recurring Expiry
		preqMsg.setPurRecurExpiry(pvreqMsg.getPurchaseEndRecur());

		//Set Installment Payment
		preqMsg.setPurInstall(pvreqMsg.getPurchaseInstall());

		//Set CH expiry date
		preqMsg.setChExpiry(pvreqMsg.getPanExpiry());

		//Do logging
		this.logger.debug("PAReq Message initialization completed.");

		//return a partly populated PAReqMessage
		return preqMsg;

	}
}