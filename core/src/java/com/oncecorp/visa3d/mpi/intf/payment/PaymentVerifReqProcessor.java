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
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.configuration.MerchantMetaInfo;
import com.oncecorp.visa3d.mpi.controller.AuthenticatorSession;
import com.oncecorp.visa3d.mpi.controller.Communicator;
import com.oncecorp.visa3d.mpi.controller.CommunicatorException;
import com.oncecorp.visa3d.mpi.controller.ErrorHandlerThreadManager;
import com.oncecorp.visa3d.mpi.controller.ErrorRequest;
import com.oncecorp.visa3d.mpi.domain.CardRangeManager;
import com.oncecorp.visa3d.mpi.domain.payment.CurrencyCode;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorMessage;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorToMPIErrorTransformer;
import com.oncecorp.visa3d.mpi.domain.payment.PAReqMessage;
import com.oncecorp.visa3d.mpi.domain.payment.VEReqMessage;
import com.oncecorp.visa3d.mpi.domain.payment.VEResMessage;
import com.oncecorp.visa3d.mpi.domain.payment.VEResToPAReqTransformer;
import com.oncecorp.visa3d.mpi.domain.payment.VEResToPaymentVerifResTransformer;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.ComplexMessageProcessor;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageEngine;
import com.oncecorp.visa3d.mpi.messaging.MessageGenerator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageMetaInfo;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageUID;
import com.oncecorp.visa3d.mpi.utility.ZLibCompressor;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;


import org.apache.log4j.Logger;

import com.ibm.xml.dsig.util.Base64;

/**
 * Description: Implement processor class for Message: PaymentVerifReq.
 * This processor is a special one which implements two process methods.
 * One is in [MessageProcessor.process(msg)], it does the same job like
 * some other processors to process its own msgand return itself.
 * Another implemented method is in
 * [ComplexMessageProcessor.processComplex(msg)], which actually controls
 * the whole Messaging flow. The final returned Msg can be controlled by
 * designer based on the business requirement. Currently, processComplex
 * method returns "PaymentVerifResMessage" back to MessageEngine after
 * doing a bunch of computing as supposed. A MessagingException will be
 * thrown in case any invalid field exists or process failed.
 *
 * @version 0.1 Aug 03, 2002
 * @author	Jun Shi
 */

public class PaymentVerifReqProcessor implements ComplexMessageProcessor {
	/*
	 * Changed made by Alan Zhang (Oct 16, 2002):
	 * 1. Changed naming convention on some variables. No '_' char occurred.
	 * 2. Split processComplex() method into multiple methods.
	 * 3. Added Caching function.
	 */

	// PaymentVerifReqProcessor type
	public final static String PROCESSOR_TYPE = "PaymentVerifReq";

	// Local Log4J logger
	private Logger logger =
		MPILogger.getLogger(PaymentVerifReqProcessor.class.getName());

	/**
	 * implement [MessageProcessor.process(msg)]
	 *
	 * @param  PaymentVerifReqMessage
	 * @return PaymentVerifReqMessage
	 */
	public Message process(Message inMsg) throws MessagingException {
		//Sanity check
		if (!(inMsg instanceof PaymentVerifReqMessage)) {
			this.logger.error(
				"PaymentVerifReqProcessor can only process PaymentVerifReqMessage."
					+ "Incompatible messsage passed in.");
			throw new MessagingException("PaymentVerifReqProcessor: Incompatible messsage passed in");
		}

		// Set ThreadLocalMerchantID
		AuthenticatorSession.instance().setMerchantID(
			((PaymentVerifReqMessage) inMsg).getMerchantID());
		logger.debug(
			"ThreadLocalMerchantID set: "
				+ AuthenticatorSession.instance().getMerchantID());

		// Set ThreadLocalProtocolType
		AuthenticatorSession.instance().setProtocol(
			getProtocolType(((PaymentVerifReqMessage) inMsg).getPan()));
		logger.debug(
			"ThreadLocalProtocolType set: "
				+ AuthenticatorSession.instance().getProtocol());

		return inMsg;
	}

	/**
	 * Implement [ComplexMessageProcessor.processComplex(msg)], which
	 * actually controls the messaging flow.
	 *
	 * @param  PaymentVerifReqMessage
	 * @return PaymentVerifResMessage
	 */

	public Message processComplex(Message inMessage)
		throws MessagingException {
		// init a referrence
		PaymentVerifReqMessage pvreqMsg = null;

		// init a referrence for Config
		Config cfg = null;

		// Sanity Check
		if (inMessage instanceof PaymentVerifReqMessage) {
			// type casting
			pvreqMsg = (PaymentVerifReqMessage) inMessage;
		} else {
			this.logger.error("[.processComplex] incompatible msg passed in");
			throw new MessagingException("[.processComplex] incompatible msg passed in");
		}

		return processPaymentVerifMessage(inMessage, pvreqMsg);

	}

	private Message processPaymentVerifMessage(
		Message inMessage,
		PaymentVerifReqMessage pvreqMsg)
		throws MessagingException {

		//Verify PAN against cache
		logger.debug("Verifying PAN in cached card range if range available...");
		boolean isInRange =
			(new CardRangeManager()).verifyCard(pvreqMsg.getPan());

		if (!isInRange) {
			logger.debug(
				"Card is not in cached range. Sending back PaymentVerifyRes message with ENROLLED as 'N'.");
			return preparePaymentVerifResAsCardNotInRange(pvreqMsg);
		} else {
			logger.debug("Need to send VEReq to confirm card is enrolled");
		}

		// Generate ID for VEReqMessage and used by other Visa Message within the same transaction
		// String msgID = ID_Generator.getUniqueId();
		/*
		 * [Martin's Note: Nov 20, 2002 10:36:33 AM]
		 * In order to keep the complete transaction intact we should use the id supplied
		 * in the PaymentVerifReq message. So all messages will be assigned that original ID.
		 * For the PaymentAuthReq, the client will be responsible for suppliying the same ID
		 * used for the PaymentVerifReq
		 */

		//Prepare VEReq message
		String msgID = inMessage.getId();
		VEReqMessage vreqMsg = prepareVEReqMessage(pvreqMsg, msgID);

		//Send VEReq message to Visa Directory
		Message resMsg = sendToVDir(vreqMsg);

		/** Process response msg recieved from Visa Directory (via Communicator) */
		if (resMsg instanceof VEResMessage)
			return processVEResMessage(pvreqMsg, msgID, resMsg, vreqMsg);
		else // resMsg is not VEResMessage
			{
			if (resMsg instanceof ErrorMessage) {
				/**
				 * After sending VEReq to Visa Dirctory, in case returned msg is an
				 * Error Message, MessageEngine will create MPIErrorMessage and send
				 * it back to caller instead of Visa ErrorMessage.
				 *
				 */

				// Type casting
				ErrorMessage errMsg = (ErrorMessage) resMsg;

				// call Engine to process this ErrorMessage
				errMsg = (ErrorMessage) MessageEngine.process(errMsg);

				//check if ErrorMessage(id) match VEReqMessage(id)
				if (!(errMsg.getId().equals(msgID))) {
					this.logger.error("Error(id) is NOT matching VEReq(id)");
					throw new MessagingException("Error(id) is NOT matching VEReq(id)");
				}

				// call MessageGenerator to create an empty MPIErrorMessage
				MPIErrorMessage mpiErrMsg =
					(MPIErrorMessage) MessageGenerator.create(
						MPIErrorMessage.MSG_TYPE,
						MPIErrorMessage.MSG_VERSION);

				// set id for MPIErrorMessage
				mpiErrMsg.setId(msgID);

				// call Engine to get ErrorToMPIErrorTransformer
				MessageUID uid =
					new MessageUID(
						ErrorMessage.MSG_TYPE,
						ErrorMessage.MSG_VERSION);
				ErrorToMPIErrorTransformer errorToMpiErrorTransformer =
					(
						ErrorToMPIErrorTransformer) MessageMetaInfo
							.getTransformer(
						uid,
						MPIErrorMessage.MSG_TYPE,
						MPIErrorMessage.MSG_VERSION);

				// do transforming
				mpiErrMsg =
					(MPIErrorMessage) errorToMpiErrorTransformer.transform(
						errMsg,
						mpiErrMsg);

				// call Engine to process this MPIErrorMessage
				mpiErrMsg = (MPIErrorMessage) MessageEngine.process(mpiErrMsg);

				// returned MPIErrorMessage
				return mpiErrMsg;

			}

			/*
			 * In case Visa Directory returned msg is an unknown message with neither
			 * "VERes" nor "Error" type message
			 */
			throw new MessagingException(
				"Returned Message from Visa Directory"
					+ "is neither VERes nor Error type.");
		}
	}

	private Message processVEResMessage(
		PaymentVerifReqMessage pvreqMsg,
		String msgID,
		Message resMsg,
		VEReqMessage vreqMsg)
		throws MessagingException {
		{
			// Type casting
			VEResMessage vresMsg = (VEResMessage) resMsg;

			// call Engine to process this VEResMessage
			vresMsg = (VEResMessage) MessageEngine.process(vresMsg);

			//check if VEResMessage(id) match VEReqMessage(id)
			if (!(vresMsg.getId().equals(msgID))) {
				this.logger.error("VERes(id) is NOT matching VEReq(id)");

				// call MessageGenerator to create an empty ErrorMessage
				ErrorMessage errMsg =
					(ErrorMessage) MessageGenerator.create(
						ErrorMessage.MSG_TYPE,
						ErrorMessage.MSG_VERSION);

				// set value for this err msg
				errMsg.setId(vresMsg.getId());
				errMsg.setErrorCode(ErrorCodes.ERROR_CODE_5);
				errMsg.setErrorMessage(ErrorCodes.ERROR_MESSAGE_5);
				errMsg.setErrorDetail("VEReq.id, VERes.id");
				errMsg.setVendorCode("VERes.id not matching VEReq.id.");

				errMsg = (ErrorMessage) MessageEngine.process(errMsg);

				try {
					String toURL =
						(String) Config.getConfigReference().getConfigData(
							MPIConfigDefinition.VISA_DIR_URL_1);

					// Send error to responding entity
					ErrorRequest er = new ErrorRequest();
					er.setToUrl(toURL);
					er.setMsg(errMsg);

					(new ErrorHandlerThreadManager()).dispatchErrorMessage(er);
					logger.info(
						"Error notification dispatched to ErrorHandlerManager.");
				} catch (Exception e) {
					this.logger.error(
						"Failed to send notification to VISA Dir due to failure getting VISA Dir URL from Config.",
						e);
				}

				/*
				 * Change made by Alan [Dec 17, 2002]
				 * throws more specific exception
				 */
				throw new MessagingException(
					vreqMsg.getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"VERes.id",
					"VERes(id) is NOT matching VEReq(id)",
					"VERes(id) is NOT matching VEReq(id)");

			}

			/*
			 * [Martin's Note: Sep 20, 2002 3:55:11 PM]
			 * In order to support backcompatible messages, the version of the PAReq that we need to
			 * create must be the same one returned in the VERes message. So the next chunk of code
			 * will have to be updated to ensure that
			 * 1) we support the PAReq message for that version
			 * 2) that all transforms are available
			 *
			 * If not, then an <Error> will have to be send to the VDir and also propate an <MPIError>
			 * to the client
			 */
			if (vresMsg.getEnrolled().equalsIgnoreCase("Y")) {
				/*
				 * [Alan's Note: Feb 19, 2003]
				 * Temp solution.
				 * In VISA 3D-Secure Spec v1.0.2, the acctID in VERes must not reveal the PAN, so we do
				 * extra check here. This check should be avoid in future implemetation as it ties
				 * processor to particular version.
				 */
				if ((vresMsg.getVersion().equals(VEResMessage.MSG_VERSION))
					&& (vresMsg.getAcctID().equals(vreqMsg.getPan()))) {
					this.logger.error(
						"VERes.acctID must not be identical with VEReq.pan (v1.0.2).");

					// call MessageGenerator to create an empty ErrorMessage
					ErrorMessage errMsg =
						(ErrorMessage) MessageGenerator.create(
							ErrorMessage.MSG_TYPE,
							ErrorMessage.MSG_VERSION);

					// set value for this err msg
					errMsg.setId(vresMsg.getId());
					errMsg.setErrorCode(ErrorCodes.ERROR_CODE_5);
					errMsg.setErrorMessage(ErrorCodes.ERROR_MESSAGE_5);
					errMsg.setErrorDetail("VEReq.pan, VERes.acctID");
					errMsg.setVendorCode(
						"VERes.acctID must not be identical with VEReq.pan.");

					errMsg = (ErrorMessage) MessageEngine.process(errMsg);

					try {
						String toURL =
							(String) Config.getConfigReference().getConfigData(
								MPIConfigDefinition.VISA_DIR_URL_1);

						// Send error to responding entity
						ErrorRequest er = new ErrorRequest();
						er.setToUrl(toURL);
						er.setMsg(errMsg);

						(new ErrorHandlerThreadManager()).dispatchErrorMessage(
							er);
						logger.info(
							"Error notification dispatched to ErrorHandlerManager.");
					} catch (Exception e) {
						this.logger.error(
							"Failed to send notification to VISA Dir due to failure getting VISA Dir URL from Config.",
							e);
					}

					throw new MessagingException(
						vreqMsg.getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"VERes.acctID",
						"VERes.acctID must not be identical with VEReq.pan",
						"VERes.acctID must not be identical with VEReq.pan");

				}

				// call MessageGenerator to create an empty PAReqMessage
				PAReqMessage preqMsg = null;

				preqMsg =
					(PAReqMessage) MessageGenerator.create(
						PAReqMessage.MSG_TYPE,
						vresMsg.getVersion());
				//To align with Error Code 6 treatment
				//PAReqMessage.MSG_VERSION);

				// set id for PAReq
				preqMsg.setId(vresMsg.getId());

				// call Engine to get VEResToPAReqTransformer
				MessageUID uid =
					new MessageUID(
						VEResMessage.MSG_TYPE,
						VEResMessage.MSG_VERSION);
				VEResToPAReqTransformer veresToPAReqTransformer =
					(VEResToPAReqTransformer) MessageMetaInfo.getTransformer(
						uid,
						PAReqMessage.MSG_TYPE,
						PAReqMessage.MSG_VERSION);

				// do transforming
				preqMsg =
					(PAReqMessage) veresToPAReqTransformer.transform(
						vresMsg,
						preqMsg);

				// call Engine to get PaymentVerifReqToPAReqTransformer
				uid =
					new MessageUID(
						PaymentVerifReqMessage.MSG_TYPE,
						PaymentVerifReqMessage.MSG_VERSION);
				PaymentVerifReqToPAReqTransformer pvreqToPAReqTransformer =
					(
						PaymentVerifReqToPAReqTransformer) MessageMetaInfo
							.getTransformer(
						uid,
						PAReqMessage.MSG_TYPE,
						PAReqMessage.MSG_VERSION);

				// do transforming
				preqMsg =
					(PAReqMessage) pvreqToPAReqTransformer.transform(
						pvreqMsg,
						preqMsg);

				/**
				 * set other PAReqMessage values fetched from Configuration Component
				 */
				try {
					/** fetch data from config */

					// Get Config reference
					Config cfg = Config.getConfigReference();

					//Get merchant meta info
					MerchantMetaInfo mmi =
						(MerchantMetaInfo) cfg.getMerchantMetaInfo(
							preqMsg.getMerID());

					//Set acquirer BIN
					preqMsg.setMerAcqBIN(mmi.getAcquirerBIN());

					//Set Merchant Name
					preqMsg.setMerName(mmi.getMerchantName());

					//Set Merchant Country Code
					preqMsg.setMerCountry(mmi.getMerchantCountryCode());

					//Check purchase currency. If none, use default value
					if ((preqMsg.getPurCurrency() == null)
						|| (preqMsg.getPurCurrency().trim().length() == 0)) {
						// There is no currency specified
						preqMsg.setPurCurrency(
							mmi.getMerchantPurchaseCurrency());
					}

					//Set exponent
					preqMsg.setPurExponent(
						CurrencyCode.getExponentForCurrency(
							preqMsg.getPurCurrency()));

					//Check merchant URL. If none, use default value
					if ((preqMsg.getMerURL() == null)
						|| (preqMsg.getMerURL().trim().length() == 0)) {
						// There is no merchant URL specified
						logger.debug("Setting the Merchant URL to the value in the Merchant");
						preqMsg.setMerURL(mmi.getMerchantURL());
					}

					// Release Config reference
					cfg = null;

				} catch (Exception e) {
					logger.debug(
						"Failed to get merchant meta info from Config.",
						e);
					/*
					 * Change made by Alan [Dec 17, 2002]
					 * throws more specific exception
					 */
					throw new MessagingException(
						vreqMsg.getId(),
						MessageEngine.UNEXPECTED_EXCEPTION_CODE,
						MessageEngine.GENERIC_ERROR_MESSAGE,
						"Failed to get merchant meta info from Config.",
						"Failed to get merchant meta info from Config.",
						e.getMessage());
				}

				// call Engine to process this PAReqMessage
				preqMsg = (PAReqMessage) MessageEngine.process(preqMsg);

				/**
				 * prepare htmlForm data and create PaymentVerifResMessage
				 */

				// XML Serializable
				// Document dom = preqMsg.toXML();

				// convert this DOM to XML String
				// String pareqXmlString = XMLUtil.toXmlString(dom);
				String pareqXmlString = preqMsg.toString();

				// compress PAReq
				byte[] compressedPAReq =
					ZLibCompressor.compress(pareqXmlString);

				// base64 encode PAReq
				//String encodedPAReq     = Base64Codec.encodeBytes(compressedPAReq);
				//String encodedPAReq     = Base64.encodeBytes(compressedPAReq);
				String encodedPAReq = Base64.encode(compressedPAReq);

				// fetch other htmlForm needed data from PaymentVerifReqMessage
				String termURL = preqMsg.getMerURL();

				// fetch other htmlForm needed data from VEResMessage
				String acsURL = vresMsg.getUrl();

				String winId = UniqueWindowIDGenerator.getWindowID( pvreqMsg.getId() );

				/*
				 * [Martin's Note: Oct 22, 2002 8:08:12 AM]
				 * In version 1.0.2 of the spec, there is a
				 * provision for browser with no javascript
				 * support. That special code has been included
				 * in the form below...
				 */
				// create htmlForm String
				String htmlForm =
					"<html>\r\n"
						+ "<head>\r\n"
						+ "<title>PAReq Authentication</title>\r\n"
						+ "</head>\r\n"
						+ "<body onload = \"OnLoadEvent();\" >\r\n"
						+ "<form name=\"downloadForm\" action=\"" + acsURL
						+ "\" method=\"POST\">\r\n"
						+ "<noscript>\r\n"
						+ "		<br>\r\n"
						+ "		<br>\r\n"
						+ "		<center>\r\n"
						+ "		<h1>Processing your 3-D Secure Transactions</h1>\r\n"
						+ "		<h2>JavaScript is currently disabled or is not supported by your browser.</h2>\r\n"
						+ "		<h3>Please click on the Submit button to continue the processing of your 3-D Secure transaction </h3>\r\n"
						+ "		<input type=\"submit\" value=\"Submit\">\r\n"
						+ "		</center>\r\n"
						+ "</noscript>\r\n"
						+ "<input type=\"hidden\" name=\"PaReq\" value=\"" 	+ encodedPAReq 	+ "\">\r\n"
						+ "<input type=\"hidden\" name=\"TermUrl\" value=\"" + termURL + "\">\r\n"
						+ "<input type=\"hidden\" name=\"MD\" value=\"" + pvreqMsg.getId() + "\">\r\n"
						+ "</form>\r\n"
						+ "<SCRIPT LANGUAGE=\"JavaScript\">\r\n"
						+ "function OnLoadEvent()\r\n"
						+ "{\r\n"
// [Gang Wu's Note: June 10, 2000] Name should be changed no matter it exists or not
						+ "window.name = \"mainWindow" + winId + "\";\r\n"
//						+ "if (window.name == null || window.name == \"\") window.name = \"mainWindow"
//				        + winId + "\";"
						+ "childwin = window.open (\"about:blank\", \"popupName" + winId + "\","
						+ "\"height=400, width=390, status=yes, dependent=no, scrollbars=yes, resizable=no\");\r\n"
						+ "document.downloadForm.target = \"popupName" + winId + "\";\r\n"
						+ "document.downloadForm.submit();\r\n"
						+ "}\r\n"
						+ "</SCRIPT>\r\n"
						+ "</body>\r\n"
						+ "</html>\r\n";

				// call MessageGenerator to create an empty  PaymentVerifResMessage
				PaymentVerifResMessage pvresMsg =
					(PaymentVerifResMessage) MessageGenerator.create(
						PaymentVerifResMessage.MSG_TYPE,
						PaymentVerifResMessage.MSG_VERSION);

				// set value for PaymentVerifResMessage
				pvresMsg.setId(pvreqMsg.getId());
				pvresMsg.setMerchantID(pvreqMsg.getMerchantID());
				pvresMsg.setEnrolled("Y");
				pvresMsg.setHtmlForm(htmlForm);

				// call Engine to process this PaymentVerifResMessage
				pvresMsg =
					(PaymentVerifResMessage) MessageEngine.process(pvresMsg);

				/*
				 * Before sending back PaymentVerifRes message, save VEReq / PAReq
				 * messages in Cache
				 */
				/*
				 * [Alan's note - Feb 21, 2003]
				 * A new cache object is created to represent cache unit. The next line of
				 * code should be replaced.
				 *
				 *	Object[] item =
				 *		new Object[] { vreqMsg, preqMsg, vresMsg.getUrl()};
				 *
				 * The MD field in HTML form is replaced by message id, the real MD value is
				 * stored in cached item.
				 */

				PaymentCacheObject item = new PaymentCacheObject();
				item.setVereqMsg(vreqMsg);
				item.setPareqMsg(preqMsg);
				item.setAcsUrl(vresMsg.getUrl());
				item.setMerchantData(pvreqMsg.getMerchantData());
				item.setProtocolType(getProtocolType(pvreqMsg.getPan()));
				item.setMerchantURL(pvreqMsg.getMerchantTermURL());

				(new PaymentMessageCacheManager()).addItem(msgID, item);

				// returned PaymmentVerifResMessage
				return pvresMsg;
			} else // VEResMessage back, but enrolled is equal to "N" or "U"
				return preparePaymentVerifResAsNotEnrolled(pvreqMsg, vresMsg);
		}
	}

	private Message preparePaymentVerifResAsNotEnrolled(
		PaymentVerifReqMessage pvreqMsg,
		VEResMessage vresMsg)
		throws MessagingException {
		{
			// call MessageGenerator to create an empty  PaymentVerifResMessage
			PaymentVerifResMessage pvresMsg =
				(PaymentVerifResMessage) MessageGenerator.create(
					PaymentVerifResMessage.MSG_TYPE,
					PaymentVerifResMessage.MSG_VERSION);

			// set id for PaymentVerifRes
			pvresMsg.setId(pvreqMsg.getId());

			// set merchant ID
			pvresMsg.setMerchantID(pvreqMsg.getMerchantID());

			// call Engine to get VEResToPaymentVerifResTransformer
			MessageUID uid =
				new MessageUID(VEResMessage.MSG_TYPE, VEResMessage.MSG_VERSION);
			VEResToPaymentVerifResTransformer veresToPVResTransformer =
				(
					VEResToPaymentVerifResTransformer) MessageMetaInfo
						.getTransformer(
					uid,
					PaymentVerifResMessage.MSG_TYPE,
					PaymentVerifResMessage.MSG_VERSION);

			// do transforming
			pvresMsg =
				(PaymentVerifResMessage) veresToPVResTransformer.transform(
					vresMsg,
					pvresMsg);

			// call Engine to process this PaymentVerifResMessage
			pvresMsg = (PaymentVerifResMessage) MessageEngine.process(pvresMsg);

			// returned PaymaneVerifResMessage
			return pvresMsg;
		}
	}

	private Message preparePaymentVerifResAsCardNotInRange(PaymentVerifReqMessage pvreqMsg)
		throws MessagingException {
		{
			// call MessageGenerator to create an empty  PaymentVerifResMessage
			PaymentVerifResMessage pvresMsg =
				(PaymentVerifResMessage) MessageGenerator.create(
					PaymentVerifResMessage.MSG_TYPE,
					PaymentVerifResMessage.MSG_VERSION);

			// set value for PaymentVerifRes
			pvresMsg.setId(pvreqMsg.getId());
			pvresMsg.setMerchantID(pvreqMsg.getMerchantID());
			pvresMsg.setEnrolled("N");
			pvresMsg.setInvalidReq("000");
			pvresMsg.setInvalidDetails("PAN is not in cached card range.");

			// call Engine to process this PaymentVerifResMessage
			pvresMsg = (PaymentVerifResMessage) MessageEngine.process(pvresMsg);

			// returned PaymaneVerifResMessage
			return pvresMsg;
		}
	}

	private VEReqMessage prepareVEReqMessage(
		PaymentVerifReqMessage pvreqMsg,
		String msgID)
		throws MessagingException {
		VEReqMessage vreqMsg;
		// call MessageGenerator to create an empty VEReqMessage
		vreqMsg =
			(VEReqMessage) MessageGenerator.create(
				VEReqMessage.MSG_TYPE,
				VEReqMessage.MSG_VERSION);

		// set VEReqMessage id
		vreqMsg.setId(msgID);

		// call Engine to get PaymentVerifReqToVEReqTransformer
		MessageUID uid =
			new MessageUID(
				PaymentVerifReqMessage.MSG_TYPE,
				PaymentVerifReqMessage.MSG_VERSION);
		PaymentVerifReqToVEReqTransformer pvreqToVEReqTransformer =
			(PaymentVerifReqToVEReqTransformer) MessageMetaInfo.getTransformer(
				uid,
				VEReqMessage.MSG_TYPE,
				VEReqMessage.MSG_VERSION);

		// do transforming
		vreqMsg =
			(VEReqMessage) pvreqToVEReqTransformer.transform(pvreqMsg, vreqMsg);

		/** set other VEReqMessage values from Configuration Component */
		try {
			// get Config referrence
			Config cfg = Config.getConfigReference();

			// Get MerchantMetaInfo
			MerchantMetaInfo mmi =
				(MerchantMetaInfo) cfg.getMerchantMetaInfo(vreqMsg.getMerID());
			if (mmi == null) {
				logger.error("Unsupported merchant: " + vreqMsg.getMerID());
				throw new MessagingException(
					vreqMsg.getId(),
					ErrorCodes.ERROR_CODE_80,
					ErrorCodes.ERROR_MESSAGE_80,
					"Merchant ID supplied in PaymentVerifReq message is not supported.["
						+ vreqMsg.getMerID()
						+ "]",
					"Merchant ID supplied in PaymentVerifReq message is not supported.["
						+ vreqMsg.getMerID()
						+ "]",
					"Unsupported merchant");
			}

			// Set acquirer BIN
			vreqMsg.setAcqBIN(mmi.getAcquirerBIN());

			// Set password
			vreqMsg.setPassword(mmi.getMerchantPassword());

			// Release no-longer used references
			cfg = null;
		} catch (MessagingException me) {
			throw me;
		} catch (Exception e) {
			logger.debug("Config Exception caught.", e);
			/*
			 * Change made by Alan [Dec 17, 2002]
			 * throws more specific exception
			 */
			throw new MessagingException(
				vreqMsg.getId(),
				MessageEngine.UNEXPECTED_EXCEPTION_CODE,
				MessageEngine.GENERIC_ERROR_MESSAGE,
				"Failed to get config data.",
				"Failed to get config data.",
				e.getMessage());
		}

		// call Engine to process this VEReqMessage
		vreqMsg = (VEReqMessage) MessageEngine.process(vreqMsg);
		return vreqMsg;
	}

	private Message sendToVDir(VEReqMessage vreqMsg) throws MessagingException {

		String[] urls = null;
		try {
			// We construct the list of URLS needed according to the card number
			// VISA: We grab [VisaDirectoryURL.1, VisaDirectoryURL.2, VisaDirectoryURL.3]
			// MasterCard: We grab [MCardDirectoryURL.1, MCardDirectoryURL.2, MCardDirectoryURL.3]
			Config cfg = Config.getConfigReference();
			urls = cfg.getDirectoryURLs(getProtocolType(vreqMsg.getPan()));

			// release referrence
			cfg = null;
		}
		catch (Exception e) {
			this.logger.error(
				"Error occurred when getting Directory information URLs", e);

			if (e instanceof MessagingException)
				throw (MessagingException) e;
			else
				throw new MessagingException(
					vreqMsg.getId(),
					MessageEngine.UNEXPECTED_EXCEPTION_CODE,
					MessageEngine.GENERIC_ERROR_MESSAGE,
					"Unexpected exception caught during communication to with directory.",
					"Unexpected exception caught during communication to with directory.",
					e.getMessage());
		}

		/**
		 * send VEReqMessage (msg) to Visa Directory via Communicator and
		 * get VEResMessage or ErrorMessage (msg) back
		 */
		try {
			for (int i = 0; i < urls.length; i++) {
				if (! XMLUtil.isNull(urls[i])) {
					try {
						return new Communicator().send(vreqMsg, urls[i]);
					}
					catch (CommunicatorException ce) {
						logger.error("Unable to send message to directory: " + urls[i]);
						logger.error("\tError message is : " + ce.getMessage());
					}
				}
			}
		} catch (Exception e) {
			this.logger.error("Error occurred in sentToVDir(): ", e);

			if (e instanceof MessagingException)
				throw (MessagingException) e;
			else
				throw new MessagingException(
					vreqMsg.getId(),
					MessageEngine.UNEXPECTED_EXCEPTION_CODE,
					MessageEngine.GENERIC_ERROR_MESSAGE,
					"Unexpected exception caught during communication to directory.",
					"Unexpected exception caught during communication to directory.",
					e.getMessage());
		}

		//
		// If we get here, that means we were unable to reach any configured directories
		//
		throw new MessagingException(
			vreqMsg.getId(),
			MessageEngine.UNEXPECTED_EXCEPTION_CODE,
			MessageEngine.GENERIC_ERROR_MESSAGE,
			"Unable to reach configured directories to send request to...",
			"Unable to reach configured directories to send request to...",
			null);
	}

	/**
	 * Convenient method to define protocol type by checking PAN number
	 * @param pan The PAN number
	 * @return The protocol type
	 */
	private String getProtocolType(String pan) {
		if (pan == null)
			return null;

		if (pan.startsWith("4"))
			return MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE;
		else if (pan.startsWith("5"))
			return MPIConfigDefinition.PROTOCOL_MASTERCARD_SECURE_CODE;
		else
			return null;
	}
}
