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

import com.oncecorp.visa3d.mpi.intf.payment.PaymentAuthResMessage;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageTransformer;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;

import org.apache.log4j.Logger;

/**
 * Description: PARes message to PaymentAuthRes message transformer implement
 * class. It transforms values of attributes which are common for both messages.
 * 
 * @version 0.1 July 24, 2002
 * @author	Alan Zhang
 */
public class PAResToPaymentAuthResTransformer implements MessageTransformer {

	/**
	 * Local Log4J logger
	 */
	protected Logger logger =
		MPILogger.getLogger(PAResToPaymentAuthResTransformer.class.getName());

	/**
	 * Implement interface method
	 * 
	 * @param inMsg The PARes message
	 * @param outMsg The PaymentAuthRes message
	 */
	public Message transform(Message inMsg, Message outMsg)
		throws MessagingException {
		//Prepare messages
		PAResMessage paresMsg = null;
		PaymentAuthResMessage authResMsg = null;

		try {
			//Type casting
			paresMsg = (PAResMessage) inMsg;
			authResMsg = (PaymentAuthResMessage) outMsg;
		} catch (Exception e) {
			this.logger.error("Type casting error.", e);
			throw new MessagingException("Type casting error");
		}

		//Transform values
		//Attribute: Transaction ID
		authResMsg.setMerchantID(paresMsg.getMerID());

		//Attribute: Purchase Date
		authResMsg.setPurchaseDate(paresMsg.getPurDate());

		//Attribute: Signature Time
		authResMsg.setAuthDate(paresMsg.getTxTime());

		//Attribute: Purchase Amount
		authResMsg.setPurchaseAmount(paresMsg.getPurAmount());

		//Attribute: Purchase Currency
		authResMsg.setCurrency(paresMsg.getPurCurrency());

		//Attribute: Purchase exponent
		authResMsg.setExponent(paresMsg.getPurExponent());

		//Attribute: Order status
		authResMsg.setStatus(paresMsg.getTxStatus());

		//Attribute: Cardholder Authentication Verification Value
		/*
		 * [Alan's Note - Jan 10, 2003]
		 * According to VISA, when TX.status is different than 'Y' and 'A',
		 * the cavv should be ignored. As so the cavv would not be transformed 
		 * to PaymentAuthRes message. Same principle applied for eci & 
		 * cavvAlgorithm as well.
		 */
		if (authResMsg.getStatus().equalsIgnoreCase("Y")
			|| authResMsg.getStatus().equalsIgnoreCase("A")) {
			authResMsg.setCavv(paresMsg.getTxCavv());

			//Attribute: Electronic Commerce Indicator
			authResMsg.setEci(paresMsg.getTxEci());

			//Attribute: CAVV Algorithm
			authResMsg.setCavvAlgorithm(paresMsg.getTxCavvAlgorithm());
		}

		//Attribute: Invalid Request Code
		authResMsg.setInvalidReq(paresMsg.getIreqCode());

		//Attribute: Invalid Request Detail
		authResMsg.setInvalidDetail(paresMsg.getIreqDetail());

		//Attribute: Vendor Code
		authResMsg.setVendorCode(paresMsg.getIreqVendorCode());

		//Logging
		this.logger.debug(
			"PARes to PaymentAuthRes message transformation completed.");

		return authResMsg;
	}

}