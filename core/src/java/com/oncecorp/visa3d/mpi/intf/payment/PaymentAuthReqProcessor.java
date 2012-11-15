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

import com.oncecorp.visa3d.mpi.controller.AuthenticatorSession;
import com.oncecorp.visa3d.mpi.controller.ErrorHandlerThreadManager;
import com.oncecorp.visa3d.mpi.controller.ErrorRequest;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorMessage;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorToMPIErrorTransformer;
import com.oncecorp.visa3d.mpi.domain.payment.PAReqMessage;
import com.oncecorp.visa3d.mpi.domain.payment.PAResMessage;
import com.oncecorp.visa3d.mpi.domain.payment.PAResToPaymentAuthResTransformer;
import com.oncecorp.visa3d.mpi.domain.payment.VEReqMessage;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.ComplexMessageProcessor;
import com.oncecorp.visa3d.mpi.messaging.DomToMsgConverter;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageEngine;
import com.oncecorp.visa3d.mpi.messaging.MessageGenerator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.security.SecurityManager;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;
import com.oncecorp.visa3d.mpi.utility.ZLibDecompressor;
import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.configuration.ConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.ibm.xml.dsig.util.Base64;

/**
 * Description: Implement processor class for Message: PaymentAuthReq.
 * The processor unwraps embedded PARes from ACS and transform it to
 * PaymentAuthRes message which to be sent back to merchant application.
 *
 * @version 0.1 July 24, 2002
 * @author	Alan Zhang
 */
public class PaymentAuthReqProcessor implements ComplexMessageProcessor {
	/**
	 * PaymentAuthReqProcessor type
	 */
	public final static String PROCESSOR_TYPE = "PaymentAuthReq";

	/**
	 * Local Log4J logger
	 */
	public Logger logger =
		MPILogger.getLogger(PaymentAuthReqProcessor.class.getName());

	/**
	 * ACS url cache
	 */
	//private String localAcsUrl = null;

	/*
	 * [Alan's note - Feb 21, 2003]
	 * Cached item contains elements needed to complete the transaction that were
	 * submitted in the first request.
	 */
	private PaymentCacheObject cachedItem;

	/**
	 * Implement MessageProcessor interface method
	 */
	public Message process(Message inMsg) throws MessagingException {
		// Set ThreadLocalMerchantID
		AuthenticatorSession.instance().setMerchantID(((PaymentAuthReqMessage) inMsg).getMerchantID());
		logger.debug(
			"ThreadLocalMerchantID set: "
				+ AuthenticatorSession.instance().getMerchantID());

		// retrieve cached item
		setCachedItem(retrieveCachedItem(inMsg.getId()));

		// set ThreadLocalProtocolType
		AuthenticatorSession.instance().setProtocol(
			getCachedItem().getProtocolType());
		logger.debug(
			"ThreadLocalProtocolType set: "
				+ AuthenticatorSession.instance().getProtocol());

		/*
		 * [Martin's Note: Feb 28, 2003 2:36:02 PM]
		 *
		 * The HTMLAuthenticator servlet still needs information contained in the
		 * PaymentCacheObject held by the Cache manager. So we cannot remove it here.
		 * We will let the both AuthenticatorImpl remove that information. This quite ugly actually!!!
		 * and error prone. A new mechanism must be added.
		 */
		// remove cached item from CacheManager
		// (new PaymentMessageCacheManager()).removeItem(inMsg.getId());


		//return message itself
		return inMsg;
	}

	/**
	 * Implement ComplexMessageProcessor method
	 */
	public Message processComplex(Message inMsg) throws MessagingException {
		//Sanity check
		if (!(inMsg instanceof PaymentAuthReqMessage)) {
			this.logger.error(
				"PaymentAuthReqProcessor can only process PaymentAuthReqMessage. Incompatible messsage passed in.");
			throw new MessagingException("Incompatible message type.");
		}

		//Type casting
		PaymentAuthReqMessage msg = (PaymentAuthReqMessage) inMsg;

		//Get paymentAuthMsg (it's actually a PARes XML String which has been compressed and encoded)
		String authMsg = msg.getPaymentAuthMsg();

		//Decode & decompress PARes XML String
		//byte[] decoded = Base64Codec.decode(authMsg);
		byte[] decoded = Base64.decode(authMsg);
		if (decoded == null) {
			this.logger.error("Base64 Decoding error.");
			throw new MessagingException("Base64 Decoding error.");
		}

		String decompressedStr = ZLibDecompressor.decompressToString(decoded);
		if (decompressedStr == null) {
			this.logger.error("Decompression error.");
			throw new MessagingException("Decompression error.");
		}

		//Convert it to PARes document
		Document authMsgDom = null;
		try {
			authMsgDom = XMLUtil.createDocument(decompressedStr, false);
		} catch (Exception e) {
			this.logger.error(
				"Failed to convert PARes XML String to Document.",
				e);
			throw new MessagingException("PARes XML String parsing error.");
		}

		//Convert document to message
		PAResMessage paresMsg = null;
		try {
			/*
			 * Alan's Note [Nov 28, 2002]
			 * It's possible that we receive a ErrorMessage that wrapped in PaymentAuthMsg element.
			 * So handle this situation here.
			 */
			Message received = (new DomToMsgConverter()).convert(authMsgDom);

			if (received instanceof PAResMessage)
				paresMsg = (PAResMessage) received;
			else {
				if (received instanceof ErrorMessage)
					return transferToMPIError((ErrorMessage) received);
				else {
					logger.error("Unexpected message type: " + received.getType());
					throw new MessagingException(
						received.getId(),
						ErrorCodes.ERROR_CODE_99,
						ErrorCodes.ERROR_MESSAGE_99,
						"Unexpected message received.",
						"Message type: " + received.getType(),
						"Unexpected message recevied in PaymentAuthMsg.");
				}
			}
		}
        catch (MessagingException me) {
			this.logger.error("MessagingException caught: ", me);
			if ((me.getId() == null) || me.getId().equalsIgnoreCase("UNKNOWN")) {
				me.setId(inMsg.getId());
			}

            // calling error handling method
            sendErrorMessageToAcs(me);

            throw new MessagingException(
				me.getId(),
				me.getErrorCode(),
				me.getErrorMsg(),
				me.getErrorDetail(),
				me.getVendorCode(),
				me.getMessage());
		}
        catch (Exception e) {
			this.logger.error("Unexpected error occurred.", e);
			throw new MessagingException("Unexpected error occurred.");
		}

		/*
		 * [Martin's Note: Sep 13, 2002 8:52:38 AM]
		 * We should use the Security Manager access point for validation instead of directly going
		 * with the XMLSignature.verify(method)
		 */

        // Retrieving signing keystore location information
        String keyStorePath = null, storePass = null;
        boolean signatureResult = false;
        try {
            keyStorePath =  (String) Config.getConfigReference().getConfigData(MPIConfigDefinition.SSL_CLIENT_KEYSTORE);
            storePass =     (String) Config.getConfigReference().getConfigData(MPIConfigDefinition.SSL_CLIENT_KEYSTORE_PW);

            logger.debug("Signature pre-checking. PARes: " + decompressedStr);
            logger.debug("Checking XML Signature with keystore: " + keyStorePath + " and key: " + storePass );

            signatureResult = SecurityManager.getInstance().verifySignature(authMsgDom, keyStorePath, storePass.toCharArray());
        }
        catch (ConfigurationException e) {
            logger.fatal("Unable to load Signing keystore configuration information. Unable to authenticate payment requests");
		}
        catch (MessagingException me) {
			logger.error("Invalid signature element detected. Stop process and sending notification...");
			/*
			 * [Alan's note - Feb 11, 2003]
			 * In this case, we need xid.
			 */
			sendErrorMessageToAcs(me);
		}
        catch (Exception e) {
			//Signature verification failed
			this.logger.error("XML signature verification failed.", e);
			this.logger.debug("Try to construct PARes (Signature verification failed).");
		}

		// Process PARes message
		try {
			paresMsg = (PAResMessage) MessageEngine.process(paresMsg);
		} catch (MessagingException me) {
			// If PARes is failed to process, then send notification and delegate exception
			sendErrorMessageToAcs(me);
			throw me;
		}

		// Match PARes message with cached VEReq/PAReq messages
		if (signatureResult != false) {
			try {
				isMatchedWithVEReqAndPAReq(paresMsg);
			}
            catch (MessagingException me) {
				sendErrorMessageToAcs(me);
				throw me;
			}
		}

		// Get empty PaymentAuthRes message
		PaymentAuthResMessage authResMsg =
			(PaymentAuthResMessage) MessageGenerator.create(
				PaymentAuthResMessage.MSG_TYPE,
				PaymentAuthResMessage.MSG_VERSION);

		// Set id first
		authResMsg.setId(inMsg.getId());

		/*
		 * [Alan's Note - Jan 20, 2003]
		 * PaymentAuthRes message has a new mandatory attribute: transactionId. Set it with
		 * the value from cached PAReq message.
		 */
		authResMsg.setTransactionID(getCachedItem().getPareqMsg().getPurXid());

		// We also copy the merchantData and MerchantURL from the cache
		authResMsg.setMerchantData(getCachedItem().getMerchantData());
		authResMsg.setMerchantURL(getCachedItem().getMerchantURL());

		//Transform PARes to PaymentAuthRes
		authResMsg = (PaymentAuthResMessage) (new PAResToPaymentAuthResTransformer()).transform(paresMsg, authResMsg);

		//Check XML signature verification result again
		if (signatureResult == false) {
			this.logger.debug("Resetting PaymentAuthRes status & payment content (since XML Signature verification failed)... ");
			authResMsg.setStatus("U");
			authResMsg.setCavv(null);
			authResMsg.setCavvAlgorithm(null);
			authResMsg.setEci(null);
		}

		return (MessageEngine.process(authResMsg));
	}

	/**
	 * Matches received PARes with cached VEReq/PAReq messages.
	 * It matches message ID first, then matches:
	 * <ul>
	 * <li>VEReq: Cardholder PAN</li>
	 * <li>PAReq: Merchant Acquirer BIN</li>
	 * <li>PAReq: Merchant ID</li>
	 * <li>PAReq: Purchase xid</li>
	 * <li>PAReq: Purchase Date</li>
	 * <li>PAReq: Purchase Amount</li>
	 * <li>PAReq: Purchase Currency</li>
	 * <li>PAReq: Purchase Currency Exponent</li>
	 * </ul>
	 *
	 * @param paresMsg The received PARes message
	 */
	private void isMatchedWithVEReqAndPAReq(PAResMessage paresMsg)
		throws MessagingException {
		try {

			VEReqMessage vereq = getCachedItem().getVereqMsg();
			PAReqMessage pareq = getCachedItem().getPareqMsg();


			if (!paresMsg.getId().equals(vereq.getId())
				|| !paresMsg.getId().equals(pareq.getId())) {
				logger.error(
					"PARes message ID not match with VEReq/PAReq messages' ID."
						+ "PARes ID: "
						+ paresMsg.getId()
						+ "VEReq ID: "
						+ vereq.getId()
						+ "PAReq ID: "
						+ pareq.getId());
				throw new MessagingException(
					paresMsg.getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"VEReq.id, PAReq.id, PARes.id",
					"PARes message ID not match with VEReq/PAReq messages' ID.",
					"PARes message ID not match with VEReq/PAReq messages' ID.");
			}

			//Match with VEReq
			String vereqPan = vereq.getPan();
//			String paresPan = paresMsg.getPan();
			StringBuffer sb = new StringBuffer();
			if (vereqPan.length() > 4) {
				for (int i = 0; i < (vereqPan.length() - 4); i++) {
					sb.append("0");
				}
			}

			/*
			 * [Alan's Note: Feb 19, 2003]
			 * Temp solution
			 * In VISA 3D-Secure Spec v1.0.1, the PARes.pan must be the same as the PAN
			 * supplied in VEReq. Though in v1.0.2, the PARes.pan must include the last
			 * four digits of the PAN supplied in VEReq, preceeded by zeros.
			 * This check should be avoid here as it ties the processor to particular version
			 * check.
			 */
			if (paresMsg.getVersion().equals(PAResMessage.MSG_VERSION)) {
				// version 1.0.2 validation
				String matchPan =
					sb.toString() + vereqPan.substring(vereqPan.length() - 4);

				if ((paresMsg.getTxStatus().equalsIgnoreCase("Y")
					|| paresMsg.getTxStatus().equalsIgnoreCase("A"))
					&& (!paresMsg.getPan().equals(matchPan))
					&& (!paresMsg.getPan().equals(vereq.getPan()))) {
					logger.error(
						"PARes pan ["
							+ paresMsg.getPan()
							+ "] not match with last 4 digits of VEReq pan ["
							+ vereq.getPan()
							+ "].");
					throw new MessagingException(
						paresMsg.getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"VEReq.pan, PARes.pan",
						"PARes.pan not match with VEReq.pan.",
						"PARes.pan not match with VEReq.pan.");
				}
			} else {
				// version 1.0.1 validation
				if (paresMsg.getTxStatus().equalsIgnoreCase("Y")
					&& !paresMsg.getPan().equals(vereq.getPan())) {
					logger.error(
						"PARes pan ["
							+ paresMsg.getPan()
							+ "] not match with of VEReq pan ["
							+ vereq.getPan()
							+ "].");
					throw new MessagingException(
						paresMsg.getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"VEReq.pan, PARes.pan",
						"PARes.pan not match with VEReq.pan.",
						"PARes.pan not match with VEReq.pan.");
				}
			}

			//Match with PAReq
			if (!paresMsg.getMerAcqBIN().equals(pareq.getMerAcqBIN())) {
				logger.error(
					"PARes Merchant Acquirer BIN ["
						+ paresMsg.getMerAcqBIN()
						+ "] not match with PAReq ["
						+ pareq.getMerAcqBIN()
						+ "].");
				throw new MessagingException(
					paresMsg.getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"PAReq.Merchant.acqBIN, PARes.Merchant.acqBIN",
					"PARes.Merchant.acqBIN not match with PAReq.Merchant.acqBIN.",
					"PARes.Merchant.acqBIN not match with PAReq.Merchant.acqBIN.");
			}

			if (!paresMsg.getMerID().equals(pareq.getMerID())) {
				logger.error(
					"PARes Merchant ID ["
						+ paresMsg.getMerID()
						+ "] not match with PAReq ["
						+ pareq.getMerID()
						+ "].");
				throw new MessagingException(
					paresMsg.getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"PAReq.Merchant.merID, PARes.Merchant.merID",
					"PAReq.Merchant.merID not match with PARes.Merchant.merID.",
					"PAReq.Merchant.merID not match with PARes.Merchant.merID.");
			}

			if (!paresMsg.getPurXid().equals(pareq.getPurXid())) {
				logger.error(
					"PARes Purchase xid ["
						+ paresMsg.getPurXid()
						+ "] not match with PAReq ["
						+ pareq.getPurXid()
						+ "].");
				throw new MessagingException(
					paresMsg.getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"PAReq.Purchase.xid, PARes.Purchase.xid",
					"PAReq.Purchase.xid not match with PARes.Purchase.xid.",
					"PAReq.Purchase.xid not match with PARes.Purchase.xid.");
			}

			if (!paresMsg.getPurDate().equals(pareq.getPurDate())) {
				logger.error(
					"PARes Purchase Date ["
						+ paresMsg.getPurDate()
						+ "] not match with PAReq ["
						+ pareq.getPurDate()
						+ "].");
				throw new MessagingException(
					paresMsg.getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"PAReq.Purchase.date, PARes.Purchase.date",
					"PAReq.Purchase.date not match with PARes.Purchase.date.",
					"PAReq.Purchase.date not match with PARes.Purchase.date.");
			}

			if (!paresMsg.getPurAmount().equals(pareq.getPurAmount())) {
				logger.error(
					"PARes Purchase Amount ["
						+ paresMsg.getPurAmount()
						+ "] not match with PAReq ["
						+ pareq.getPurAmount()
						+ "].");
				throw new MessagingException(
					paresMsg.getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"PAReq.Purchase.purchAmount, PARes.Purchase.purchAmount",
					"PAReq.Purchase.purchAmount not match with PARes.Purchase.purchAmount.",
					"PAReq.Purchase.purchAmount not match with PARes.Purchase.purchAmount.");
			}

			if (!paresMsg.getPurCurrency().equals(pareq.getPurCurrency())) {
				logger.error(
					"PARes Purchase Currency ["
						+ paresMsg.getPan()
						+ "] not match with PAReq ["
						+ pareq.getPurCurrency()
						+ "].");
				throw new MessagingException(
					paresMsg.getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"PAReq.Purchase.currency, PARes.Purchase.currency",
					"PAReq.Purchase.currency not match with PARes.Purchase.currency.",
					"PAReq.Purchase.currency not match with PARes.Purchase.currency.");
			}

			if (!paresMsg.getPurExponent().equals(pareq.getPurExponent())) {
				logger.error(
					"PARes Purchase Currency Exponent ["
						+ paresMsg.getPurExponent()
						+ "] not match with PAReq ["
						+ pareq.getPurExponent()
						+ "].");
				throw new MessagingException(
					paresMsg.getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"PAReq.Purchase.exponent, PARes.Purchase.exponent",
					"PAReq.Purchase.exponent not match with PARes.Purchase.exponent.",
					"PAReq.Purchase.exponent not match with PARes.Purchase.exponent.");
			}

		} catch (Exception e) {
			if (e instanceof MessagingException)
				throw ((MessagingException) e);
			else {
				logger.error(
					"Unexpected error occurred during PARes message matching.",
					e);
				throw new MessagingException(
					paresMsg.getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"VERes, PAReq, PARes",
					"PARes not match with VERes/PAReq.",
					"PARes not match with VERes/PAReq.");
			}
		}
	}

	/*
	 * Retrieve Xid from cached PAReq message
	 */
	private PaymentCacheObject retrieveCachedItem(String id)
		throws MessagingException {
		//Retrieve cached item from cache manager
		Object item = (new PaymentMessageCacheManager()).getCachedItem(id);

		if (item == null) {
			logger.error("No cached item found for ID: " + id);
			throw new MessagingException(
				id,
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"PaymentAuthReq.id",
				"No match found for PaymentAuthReq.id: " + id,
				"No match found for PaymentAuthReq.id: " + id);
		}

		return (PaymentCacheObject) item;

	}

	// generate err msg and sent to Acs
	/*
	 * [Alan's note - Feb 12, 2003]
	 * As the fact that after we retrieve cached item, the item would be removed instantly,
	 * we return xid in case further process needs it.
	 */
	private String sendErrorMessageToAcs(MessagingException me) {
		// init variables
		ErrorMessage errMsg = null;
		String acsUrl = null;
		String xid = null;

		// generate error message
		try {
			// call MessageGenerator to create an empty ErrorMessage
			errMsg =
				(ErrorMessage) MessageGenerator.create(
					ErrorMessage.MSG_TYPE,
					ErrorMessage.MSG_VERSION);

			// set value for this err msg
			errMsg.setId(me.getId());
			errMsg.setErrorCode(me.getErrorCode());
			errMsg.setErrorMessage(me.getErrorMsg());
			errMsg.setVendorCode(me.getVendorCode());
			errMsg.setErrorDetail(me.getErrorDetail());

			errMsg = (ErrorMessage) MessageEngine.process(errMsg);
		} catch (Exception e) {
			logger.debug("Failed to generate Error Message");
		}

		 /*
		  * [Alan's note - Feb 21, 2003]
		  * Now we could retrieve ACS URL from cached item
		  */
		acsUrl = getCachedItem().getAcsUrl();

		// Send error to responding entity
		ErrorRequest er = new ErrorRequest();
		er.setToUrl(acsUrl);
		er.setMsg(errMsg);

		(new ErrorHandlerThreadManager()).dispatchErrorMessage(er);
		logger.info("Error notification dispatched to ErrorHandlerManager.");

		return xid;
	}

	/**
	 * Transfer Error Message to MPIErrorMessage
	 * @param errMsg The ErrorMessage
	 */
	private MPIErrorMessage transferToMPIError(ErrorMessage errMsg)
		throws MessagingException {
		MPIErrorMessage mpiErrMsg =
			(MPIErrorMessage) MessageGenerator.create(
				MPIErrorMessage.MSG_TYPE,
				MPIErrorMessage.MSG_VERSION);
		return (MPIErrorMessage) (new ErrorToMPIErrorTransformer()).transform(
			errMsg,
			mpiErrMsg);
	}

	/**
	 * Returns the cachedItem.
	 * @return PaymentCacheObject
	 */
	public PaymentCacheObject getCachedItem() {
		return cachedItem;
	}

	/**
	 * Sets the cachedItem.
	 * @param cachedItem The cachedItem to set
	 */
	public void setCachedItem(PaymentCacheObject cachedItem) {
		this.cachedItem = cachedItem;
	}

}