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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.oncecorp.visa3d.mpi.domain.payment.CurrencyCode;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageValidator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.URLValidator;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

/**
 * Description: Implement message class for PaymentVerifReq. The PaymentVerifReq
 * message is sent by Merchant Application (via Controller Component) when user
 * click "buy". It encapsulates both purchase and user information such as "pan",
 * "purchase amount" and "user browser info" etc. 
 *  
 * @version $Revision: 37 $
 * @author	Jun Shi
 */

public class PaymentVerifReqMessage extends Message {

	// Message type
	public final static String MSG_TYPE = "PaymentVerifReq";

	/**
	 * Message Version
	 */
	// public final static String MSG_VERSION = "1.0";
	/* 
	 * [Martin's Note: Dec 12, 2002 1:06:27 PM]
	 * 
	 * Moved to version 1.1 as per new interface definition
	 */
	public final static String MSG_VERSION = "1.1";
	
	/**
	 * XML Message structure
	 */
	private static final String PAYMENTVERIFREQ_XML_STRING =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<MPI_Interface>"
			+ "<PaymentVerifReq>"
			+ "<version/>"
			+ "<merchantID/>"
			+ "<pan/>"
			+ "<panExpiry/>"
			+ "<purchaseDate/>"
			+ "<purchasePurchAmount/>"
			+ "</PaymentVerifReq>"
			+ "</MPI_Interface>";

	// Error messages

	private static final String TERMURL2_ERRMSG =
		"URL Specification is not valid";
	private static final String TERMURL2_ERRMSG_LONG =
		"Invalid field: [PaymentVerifReq.merchantTermURL] - " + TERMURL2_ERRMSG;

	private static final String PANEXP_ERRMSG = "Credit card is expired.";
	private static final String PANEXP_ERRMSG_LONG =
		"Invalid field: [PaymentVerifReq.panExpiry] - " + PANEXP_ERRMSG;

	private static final String PANEXP2_ERRMSG =
		"Invalid data format of Card Expiry Date.";
	private static final String PANEXP2_ERRMSG_LONG =
		"Invalid field: [PaymentVerifReq.panExpiry] - " + PANEXP2_ERRMSG;

	private static final String PURDATE2_ERRMSG =
		"Invalid data format of Purchase Date.";
	private static final String PURDATE2_ERRMSG_LONG =
		"Invalid field: [PaymentVerifReq.purchaseDate] - " + PURDATE2_ERRMSG;

	private static final String PURCURR2_ERRMSG = "Invalid currency code used.";
	private static final String PURCURR2_ERRMSG_LONG =
		"Invalid field: [PaymentVerifReq.purchaseCurrency] - "
			+ PURCURR2_ERRMSG;

	private static final String PENDR2_ERRMSG =
		"Invalid data format. Must be YYYYMMDD.";
	private static final String PENDR2_ERRMSG_LONG =
		"Invalid field: [PaymentVerifReq.PurchaseRecur.endRecur] - "
			+ PENDR2_ERRMSG;

	private static final String PENDR3_ERRMSG =
		"End Recurrence date is not in the future.";
	private static final String PENDR3_ERRMSG_LONG =
		"Invalid field: [PaymentVerifReq.PurchaseRecur.endRecur] - "
			+ PENDR3_ERRMSG;

	private static final String PENDR4_ERRMSG =
		"End Recurrence date is less than PAN expiry date.";
	private static final String PENDR4_ERRMSG_LONG =
		"Invalid field: [PaymentVerifReq.PurchaseRecur.endRecur] - "
			+ PENDR3_ERRMSG;

	private static final String COEXIST1_ERRMSG =
		"[PurchaseRecur.frequency, PurchaseRecur.endRecur] must both be specified";
	private static final String COEXIST1_ERRMSG_LONG =
		"Invalid message: [PaymentVerifReq] - " + COEXIST1_ERRMSG;

	private static final String DEVICE_ERRMSG =
		"[browserCategory] must be 0 for PC";
	private static final String DEVICE_ERRMSG_LONG =
		"Invalid message: [PaymentVerifReq] - " + DEVICE_ERRMSG;

	private static final String MERCHANT_ERRMSG =
		"[merchantID] must be up to 24 characters with up to 15 byte alphanumeric Card Acceptor ID"
			+ " and optionally followed by a hypher and an up to 8 byte alphanumeric Card Acceptor Terminal ID";
	private static final String MERCHANT_ERRMSG_LONG =
		"Invalid message: [PaymentVerifReq] - " + MERCHANT_ERRMSG;

	private static final String PURCHAMOUNT_ERRMSG =
		"[purchasePurchAmount] must not be 0.";
	private static final String PURCHAMOUNT_ERRMSG_LONG =
		"Invalid message: [PaymentVerifReq] - " + PURCHAMOUNT_ERRMSG;

	private static final String PURCHAMOUNT2_ERRMSG =
		"[purchasePurchAmount] must be numeric.";
	private static final String PURCHAMOUNT2_ERRMSG_LONG =
		"Invalid message: [PaymentVerifReq] - " + PURCHAMOUNT2_ERRMSG;

	/**
	 * PaymentVerifReq Message Fields
	 */
	private String merchantTermURL; // Optional
	private String merchantData; // Mandatory
	private String transactionID; // Optional
	private String pan; // Mandatory
	private String panExpiry; // Mandatory
	private String purchaseDate; // Mandatory
	private String purchaseAmount; // Mandatory
	private String purchasePurchAmount; // Mandatory
	private String purchaseCurrency; // Optional
	private String purchaseDesc; // Optional
	private String browserCategory; // Optional
	private String browserAccept; // Optional
	private String browserAgent; // Optional
	private String purchaseFrequency; // Conditional (if Recur is not null)
	private String purchaseEndRecur; // Conditional (if Recur is not null)
	private String purchaseInstall; // Optional

	private String merchantID;
	private String purchaseInfo;

	// Local Log4J logger
	private Logger logger =
		MPILogger.getLogger(PaymentVerifReqMessage.class.getName());

	/** Constructor: set type and version for this object */
	public PaymentVerifReqMessage() throws MessagingException {
		//Set message type
		setType(MSG_TYPE);

		//Set messsage version
		setVersion(MSG_VERSION);
	}

	/** 
	 * [Getter and Setter methods]
	 * [Setters] validates PaymentVerifReq message fields to make sure all mandatory 
	 * fields presented and with correct sizes. A MessagingException will be 
	 * thrown in case any invalid field exists.
	 */

	// override super class to do id validation for this msg
	public void setId(String id) throws MessagingException {
		/** validate input id */
		MessageValidator.validateField(
			getId(),
			id,
			"[Message.id]",
			-1,
			-1,
			true);
		super.setId(id);
	}

	public String getMerchantTermURL() {
		return merchantTermURL;
	}

	public void setMerchantTermURL(String merchantTermURL)
		throws MessagingException {
		//Check Field: merchantTermURL
		/*
		 * [Alan's Note: Dec 09, 2002]
		 * MerchantURL is optional now.
		 */
		MessageValidator.validateField(
			getId(),
			merchantTermURL,
			"[PaymentVerifReq.merchantTermURL]",
			1,
			255,
			false);

		if (merchantTermURL != null) {
			// Verify URL
			if (!URLValidator.isValid(merchantTermURL)) {
				logger.error(
					TERMURL2_ERRMSG_LONG + " Value: {" + merchantTermURL + "}");
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[PaymentVerifReq.merchantTermURL]",
					TERMURL2_ERRMSG,
					TERMURL2_ERRMSG_LONG + " Value: {" + merchantTermURL + "}");
			}
		}

		this.merchantTermURL = merchantTermURL;
	}

	public String getMerchantData() {
		return merchantData;
	}
	public void setMerchantData(String merchantData)
		throws MessagingException {
		/* 
		 * [Martin's Note: Oct 22, 2002 9:54:20 AM]
		 * It is possible that the MerchantData field could be empty. So 
		 * we should not inforce validation of this field as mandatory
		 */

		//Check Field: merchantData
		if (merchantData == null) {
			this.merchantData = "";
		} else {
			this.merchantData = merchantData;
		}
	}

	public String getPan() {
		return pan;
	}
	public void setPan(String pan) throws MessagingException {
		//Check Field: pan
		MessageValidator.validateDigitField(
			getId(),
			pan,
			"[PaymentVerifReq.pan]",
			13,
			19,
			true);
		this.pan = pan;
	}

	public String getPanExpiry() {
		return panExpiry;
	}
	public void setPanExpiry(String panExpiry) throws MessagingException {
		//Check Field: pan expiry date
		MessageValidator.validateDigitField(
			getId(),
			panExpiry,
			"[PaymentVerifReq.panExpiry]",
			4,
			4,
			true);

		//Check date format
		SimpleDateFormat sdf = new SimpleDateFormat("yyMM");
		GregorianCalendar cardDate = new GregorianCalendar();
		try {
			//Create formatter
			sdf.setLenient(false);

			//Verify data format
			cardDate.setTime(sdf.parse(panExpiry));
		} catch (ParseException pe) {
			logger.error(PANEXP2_ERRMSG_LONG + " Value: {" + panExpiry + "}");
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentVerifReq.panExpiry]",
				PANEXP2_ERRMSG,
				PANEXP2_ERRMSG_LONG + " Value: {" + panExpiry + "}");
		}

		// Validate that the card expiry date is still valid
		cardDate.set(
			java.util.Calendar.DAY_OF_MONTH,
			cardDate.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));

		GregorianCalendar now = new GregorianCalendar();
		if (now.getTime().after(cardDate.getTime())) {
			logger.error(PANEXP_ERRMSG_LONG + " Value: {" + panExpiry + "}");
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentVerifReq.panExpiry]",
				PANEXP_ERRMSG,
				PANEXP_ERRMSG_LONG + " Value: {" + panExpiry + "}");
		}

		this.panExpiry = panExpiry;
	}

	public String getPurchaseDate() {
		return purchaseDate;
	}
	public void setPurchaseDate(String purchaseDate)
		throws MessagingException {
		//Check Field: Purchase Date & Time
		MessageValidator.validateField(
			getId(),
			purchaseDate,
			"[PaymentVerifReq.purchaseDate]",
			1,
			17,
			true);

		//Check date format
		try {
			// Create formatter
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			sdf.setLenient(false);

			// Verify data format
			sdf.parse(purchaseDate);

			// Release formatter
			sdf = null;
		} catch (ParseException pe) {
			logger.error(
				PURDATE2_ERRMSG_LONG + " Value: {" + purchaseDate + "}");
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentVerifReq.purchaseDate]",
				PURDATE2_ERRMSG,
				PURDATE2_ERRMSG_LONG + " Value: {" + purchaseDate + "}");
		}

		this.purchaseDate = purchaseDate;
	}

	public String getPurchaseAmount() {
		return purchaseAmount;
	}
	public void setPurchaseAmount(String purchaseAmount)
		throws MessagingException {
		//Check Field: Purchase Amount (Mandatory)
		MessageValidator.validateField(
			getId(),
			purchaseAmount,
			"[PaymentVerifReq.purchaseAmount]",
			1,
			20,
			true);
		this.purchaseAmount = purchaseAmount;
	}

	public String getPurchasePurchAmount() {
		return purchasePurchAmount;
	}
	public void setPurchasePurchAmount(String purchasePurchAmount)
		throws MessagingException {
		//Check Field: Purchase Purch Amount
		MessageValidator.validateDigitField(
			getId(),
			purchasePurchAmount,
			"[PaymentVerifReq.purchasePurchAmount]",
			1,
			12,
			true);

		// check the amount with zero value
		try {
			if (Integer.parseInt(purchasePurchAmount) == 0) {
				logger.error(
					PURCHAMOUNT_ERRMSG_LONG
						+ " Value: {"
						+ purchasePurchAmount
						+ "}");
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[PaymentVerifReq.purchasePurchAmount]",
					PURCHAMOUNT_ERRMSG,
					PURCHAMOUNT_ERRMSG_LONG
						+ " Value: {"
						+ purchasePurchAmount
						+ "}");
			}
		} catch (NumberFormatException ne) {
			logger.error(
				PURCHAMOUNT2_ERRMSG_LONG
					+ " Value: {"
					+ purchasePurchAmount
					+ "}",
				ne);
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentVerifReq.purchasePurchAmount]",
				PURCHAMOUNT2_ERRMSG,
				PURCHAMOUNT2_ERRMSG_LONG
					+ " Value: {"
					+ purchasePurchAmount
					+ "}");
		}

		this.purchasePurchAmount = purchasePurchAmount;
	}

	public String getPurchaseCurrency() {
		return purchaseCurrency;
	}
	public void setPurchaseCurrency(String purchaseCurrency)
		throws MessagingException {
		//Check Field: Purchase Currency
		/*
		 * [Alan's Note: Dec 09, 2002]
		 * Currency is optional now.
		 */
		MessageValidator.validateDigitField(
			getId(),
			purchaseCurrency,
			"[PaymentVerifReq.purchaseCurrency]",
			1,
			3,
			false);

		// Only validate currency using currency code table if it is 
		// specified in the message
		if ((purchaseCurrency != null)
			&& (!CurrencyCode.isCodeValid(purchaseCurrency))) {
			logger.error(
				PURCURR2_ERRMSG_LONG + " Value: {" + purchaseCurrency + "}");
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentVerifReq.purchaseCurrency]",
				PURCURR2_ERRMSG,
				PURCURR2_ERRMSG_LONG + " Value: {" + purchaseCurrency + "}");

		}
		this.purchaseCurrency = purchaseCurrency;
	}
	public String getPurchaseDesc() {
		return purchaseDesc;
	}
	public void setPurchaseDesc(String purchaseDesc)
		throws MessagingException {
		//Check Field: Purchase Description (Optional)
		MessageValidator.validateField(
			getId(),
			purchaseDesc,
			"[PaymentVerifReq.purchaseDesc]",
			1,
			125,
			false);
		this.purchaseDesc = purchaseDesc;
	}

	public String getBrowserCategory() {
		return browserCategory;
	}

	public void setBrowserCategory(String in) throws MessagingException {
		//Check Field: Browser_deviceCategory (must be 0 for PC)
		if ((in != null) && (in.length() != 0)) {
			try {
				int number = Integer.parseInt(in);
				if (number < 0) {
					this.logger.error(DEVICE_ERRMSG + " Value: {" + in + "}");
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"[PaymentVerifReq.browserCategory]",
						DEVICE_ERRMSG,
						DEVICE_ERRMSG_LONG + " Value: {" + in + "}");

				}
			} catch (NumberFormatException nfe) {
				this.logger.error(DEVICE_ERRMSG + " Value: {" + in + "}");
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[PaymentVerifReq.browserCategory",
					DEVICE_ERRMSG,
					DEVICE_ERRMSG_LONG + " Value: {" + in + "}");
			}

			if (!in.equals("0")) {
				logger.error(
					DEVICE_ERRMSG_LONG + " Value: {" + in + "}");
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_60,
					ErrorCodes.ERROR_MESSAGE_60,
					"[PaymentVerifReq.browserCategory]",
					ErrorCodes.ERROR_MESSAGE_60,
					ErrorCodes.ERROR_MESSAGE_60 + " Value: {" + in + "}");

			}
		}

		this.browserCategory = in;
	}

	public String getBrowserAccept() {
		return browserAccept;
	}
	public void setBrowserAccept(String browserAccept)
		throws MessagingException {
		this.browserAccept = browserAccept;
	}

	public String getBrowserAgent() {
		return browserAgent;
	}
	public void setBrowserAgent(String browserAgent)
		throws MessagingException {
		this.browserAgent = browserAgent;
	}

	public String getPurchaseFrequency() {
		return purchaseFrequency;
	}
	public void setPurchaseFrequency(String in) throws MessagingException {
		//Check Field: Purchase Recurring - Frequency
		MessageValidator.validateDigitField(
			getId(),
			in,
			"[PaymentVerifReq.PurchaseRecur.frequency]",
			1,
			4,
			false);
		this.purchaseFrequency = in;
	}

	public String getPurchaseEndRecur() {
		return purchaseEndRecur;
	}
	public void setPurchaseEndRecur(String in) throws MessagingException {
		//Check Field: Purchase Recurring
		MessageValidator.validateField(
			getId(),
			in,
			"[PaymentVerifReq.PurchaseRecur.endRecur]",
			8,
			8,
			false);

		//Since Purchase Recurring is not mandatory, only validate date format if it's not empty
		if ((in == null) || (in.length() == 0))
			return;

		// Validate that the End Recurrence date (format YYYYMMDD)
		// is a future date
		Date eRecurDate = null;
		try {
			//Create formatter
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMDD");
			sdf.setLenient(false);

			//Verify data format
			eRecurDate = sdf.parse(in);

			//Release formatter
			sdf = null;
		} catch (ParseException pe) {
			logger.error(PENDR2_ERRMSG_LONG + " Value: {" + in + "}");
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentVerifReq.PurchaseRecur.endRecur]",
				PENDR2_ERRMSG,
				PENDR2_ERRMSG_LONG + " Value: {" + in + "}");
		}

		// Validate that end-recurrence date is specified as a future date
		Date now = new Date(System.currentTimeMillis());
		if (eRecurDate.compareTo(now) < 0) {
			// End Recur is less than today.
			logger.error(PENDR3_ERRMSG_LONG + " Value: {" + eRecurDate + "}");
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentVerifReq.PurchaseRecur.endRecur]",
				PENDR3_ERRMSG,
				PENDR3_ERRMSG_LONG + " Value: {" + eRecurDate + "}");
		}

		this.purchaseEndRecur = in;
	}

	public String getPurchaseInstall() {
		return purchaseInstall;
	}
	public void setPurchaseInstall(String purchaseInstall)
		throws MessagingException {
		//Check Field: Purchase Install (Optional)
		MessageValidator.validateDigitField(
			getId(),
			purchaseInstall,
			"[PaymentVerifReq.purchaseInstall]",
			1,
			3,
			false);
		this.purchaseInstall = purchaseInstall;
	}

	// validate dependent fields 
	public boolean validate() throws MessagingException {
		// Mandatory Check
		/*
		 * [Alan's Note: Dec 09, 2002]
		 * MerchantURL & Currency are optional now.
		 */

		/* 
		* [Martin's Note: Dec 12, 2002 2:18:06 PM]
		* So they are removed from the following tests because
		* we only check to ensure mandatory fields are specified
		*/
		MessageValidator.validateField(
			"UNKNOWN",
			id,
			"[Message.id]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getMerchantID(),
			"[PaymentVerifyReq.Merchant.merchantID]",
			1,
			24,
			true);

		MessageValidator.validateDigitField(
			getId(),
			pan,
			"[PaymentVerifReq.pan]",
			13,
			19,
			true);
		MessageValidator.validateDigitField(
			getId(),
			panExpiry,
			"[PaymentVerifReq.panExpiry]",
			4,
			4,
			true);
		MessageValidator.validateField(
			getId(),
			purchaseDate,
			"[PaymentVerifReq.purchaseDate]",
			1,
			17,
			true);
		MessageValidator.validateDigitField(
			getId(),
			purchasePurchAmount,
			"[PaymentVerifReq.purchasePurchAmount]",
			1,
			12,
			true);
		MessageValidator.validateField(
			getId(),
			purchaseAmount,
			"[PaymentVerifReq.purchaseAmount]",
			1,
			20,
			true);

		//Check Fields: Purchase Recurring Frequence & endRecur
		if ((getPurchaseFrequency() != null)
			&& (getPurchaseFrequency().length() != 0)) {
			if (getPurchaseEndRecur() == null) {
				logger.error(COEXIST1_ERRMSG_LONG);
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_3,
					ErrorCodes.ERROR_MESSAGE_3,
					"[PaymentVerifReq.PurchaseRecur.endRecur]",
					COEXIST1_ERRMSG,
					COEXIST1_ERRMSG_LONG);
			} else {
				if (getPurchaseEndRecur().length() == 0) {
					logger.error(COEXIST1_ERRMSG_LONG);
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"[PaymentVerifReq.PurchaseRecur.endRecur]",
						COEXIST1_ERRMSG,
						COEXIST1_ERRMSG_LONG);
				}
			}
		}

		if ((getPurchaseEndRecur() != null)
			&& (getPurchaseEndRecur().length() != 0)) {
			if (getPurchaseFrequency() == null) {
				logger.error(COEXIST1_ERRMSG_LONG);
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_3,
					ErrorCodes.ERROR_MESSAGE_3,
					"[PaymentVerifReq.PurchaseRecur.frequency]",
					COEXIST1_ERRMSG,
					COEXIST1_ERRMSG_LONG);
			} else {
				if (getPurchaseFrequency().length() == 0) {
					logger.error(COEXIST1_ERRMSG_LONG);
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"[PaymentVerifReq.PurchaseRecur.frequency]",
						COEXIST1_ERRMSG,
						COEXIST1_ERRMSG_LONG);
				}
			}
		}

		// check panExpiry & endRecur
		if ((getPurchaseEndRecur() != null)
			&& (getPurchaseEndRecur().length() != 0)) {
			try {
				Date endRecurDate =
					(new SimpleDateFormat("yyyyMMdd")).parse(
						getPurchaseEndRecur());
				Date panExpiryDate =
					(new SimpleDateFormat("yyMM")).parse(getPanExpiry());

				if (endRecurDate.compareTo(panExpiryDate) > 0) {
					logger.error(
						PENDR4_ERRMSG_LONG
							+ " Value: {"
							+ getPurchaseEndRecur()
							+ "}");
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"[PaymentVerifReq.PurchaseRecur.endRecur]",
						PENDR4_ERRMSG,
						PENDR4_ERRMSG_LONG
							+ " Value: {"
							+ getPurchaseEndRecur()
							+ "}");

				}
			} catch (ParseException e) {
				logger.error(
					PENDR2_ERRMSG_LONG
						+ " Value: {"
						+ getPanExpiry()
						+ ", "
						+ getPurchaseEndRecur()
						+ "}");
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[PaymentVerifReq.PurchaseRecur.endRecur]",
					PENDR2_ERRMSG,
					PENDR2_ERRMSG_LONG
						+ " Value: {"
						+ getPanExpiry()
						+ ", "
						+ getPurchaseEndRecur()
						+ "}");
			}
		}

		return true;
	}

	/**
	 * XMLSerializable interface method: toXML()
	 * This method consturcts a DOM document which represents this message.
	 * 
	 * @return DOM document object
	 */
	public Document toXML() {
		try {
			//Create document template
			Document dom =
				XMLUtil.createDocument(PAYMENTVERIFREQ_XML_STRING, false);
			//Sanity check
			if (dom == null) {
				logger.error("Failed to create document template.");
				return null;
			} //Shared attributes
			Element element;
			//Add message id attribute (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentVerifReq");
			element.setAttribute("id", getId());
			//Set version (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentVerifReq/version");
			element.appendChild(dom.createTextNode(getVersion()));
			//Set merchantID (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentVerifReq/merchantID");
			element.appendChild(dom.createTextNode(getMerchantID()));
			//Set merchantTermURL (Optional)
			if ((getMerchantTermURL() != null)
				&& (getMerchantTermURL().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifReq");
				Element child = dom.createElement("merchantTermURL");
				child.appendChild(dom.createTextNode(getMerchantTermURL()));
				element.appendChild(child);
			}

			//Set merchantData (Optional)
			/*
			 * [Alan's Note - 2003.01.20]
			 * merchantData is no longer a mandatory field. 
			 */
			if ((getMerchantData() != null)
				&& (getMerchantData().trim().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifReq");

				Element child = dom.createElement("merchantData");
				child.appendChild(dom.createTextNode(getMerchantData()));
				element.appendChild(child);
			}

			//Set transactionId (Optional)
			if ((getTransactionID() != null)
				&& (getTransactionID().trim().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifReq");

				Element child = dom.createElement("transactionID");
				child.appendChild(dom.createTextNode(getTransactionID()));
				element.appendChild(child);
			}

			//Set pan (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentVerifReq/pan");
			element.appendChild(dom.createTextNode(getPan()));
			//Set panExpiry (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentVerifReq/panExpiry");
			element.appendChild(dom.createTextNode(getPanExpiry()));
			//Set purchaseDate (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentVerifReq/purchaseDate");
			element.appendChild(dom.createTextNode(getPurchaseDate()));
			//Set purchaseAmount (Optional)
			if ((purchaseAmount != null) && (purchaseAmount.length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifReq");
				Element child = dom.createElement("purchaseAmount");
				child.appendChild(dom.createTextNode(getPurchaseAmount()));
				element.appendChild(child);
			} //Set purchasePurchAmount (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentVerifReq/purchasePurchAmount");
			element.appendChild(dom.createTextNode(getPurchasePurchAmount()));
			//Set purchaseCurrency (Optional)
			if ((getPurchaseCurrency() != null)
				&& (getPurchaseCurrency().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifReq");
				Element child = dom.createElement("purchaseCurrency");
				child.appendChild(dom.createTextNode(getPurchaseCurrency()));
				element.appendChild(child);
			} //Set purchaseDesc (Optional)
			if ((getPurchaseDesc() != null)
				&& (getPurchaseDesc().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifReq");
				Element child = dom.createElement("purchaseDesc");
				child.appendChild(dom.createTextNode(getPurchaseDesc()));
				element.appendChild(child);
			} //Set purchaseInfo (Optional)
			if ((getPurchaseInfo() != null)
				&& (getPurchaseInfo().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifReq");
				Element child = dom.createElement("purchaseInfo");
				child.appendChild(dom.createTextNode(getPurchaseInfo()));
				element.appendChild(child);
			} //Set browserCategory (Optional)
			if ((getBrowserCategory() != null)
				&& (getBrowserCategory().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifReq");
				Element child = dom.createElement("browserCategory");
				child.appendChild(dom.createTextNode(getBrowserCategory()));
				element.appendChild(child);
			} //Set browserAccept (Optional)
			if ((getBrowserAccept() != null)
				&& (getBrowserAccept().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifReq");
				Element child = dom.createElement("browserAccept");
				child.appendChild(dom.createTextNode(getBrowserAccept()));
				element.appendChild(child);
			} //Set browserAgent (Optional)
			if ((getBrowserAgent() != null)
				&& (getBrowserAgent().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifReq");
				Element child = dom.createElement("browserAgent");
				child.appendChild(dom.createTextNode(getBrowserAgent()));
				element.appendChild(child);
			} //Set Purchase Recurring (Optional)
			if ((getPurchaseFrequency() != null)
				|| (getPurchaseEndRecur() != null)) {
				if ((getPurchaseFrequency() != null)
					&& (getPurchaseEndRecur() != null)) {
					element =
						(Element) XMLUtil.getFirstNodeByXPath(
							dom,
							"/MPI_Interface/PaymentVerifReq");
					Element child = dom.createElement("PurchaseRecur");
					Element frequencyElement = dom.createElement("frequency");
					Element endRecurElement = dom.createElement("endRecur");
					frequencyElement.appendChild(
						dom.createTextNode(getPurchaseFrequency()));
					endRecurElement.appendChild(
						dom.createTextNode(getPurchaseEndRecur()));
					child.appendChild(frequencyElement);
					child.appendChild(endRecurElement);
					element.appendChild(child);
				} else {
					logger.error(
						"Failed to create PaymentVerifReq message document."
							+ "[Recurring frequency & expiry should be co-existed.]");
					return null;
				}
			} //Set purchaseInstall (Optional)
			if ((getPurchaseInstall() != null)
				&& (getPurchaseInstall().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifReq");
				Element child = dom.createElement("purchaseInstall");
				child.appendChild(dom.createTextNode(getPurchaseInstall()));
				element.appendChild(child);
			} // do logging
			logger.debug("[PaymentVerifReqMessage.toXML] completed!");
			return dom;
		} catch (Exception e) {
			logger.error(
				"Failed to convert PaymentVerifReq message to DOM document.",
				e);
			return null;
		}

	}

	/**
	 * Represents this message as XML string
	 *
	 * @return The XML string
	 */
	public String toString() {
		try {
			StringBuffer sb = new StringBuffer();

			sb.append(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<MPI_Interface>"
					+ "<PaymentVerifReq id=\"");

			sb.append(XMLUtil.filterSpecialChars(getId()));
			sb.append("\"><version>");
			sb.append(XMLUtil.filterSpecialChars(getVersion()));
			sb.append("</version><merchantID>");
			sb.append(XMLUtil.filterSpecialChars(getMerchantID()));
			sb.append("</merchantID>");

			// add merchantTermURL (Optional)
			if ((getMerchantTermURL() != null)
				&& (getMerchantTermURL().length() != 0)) {
				sb.append("<merchantTermURL>");
				sb.append(XMLUtil.filterSpecialChars(getMerchantTermURL()));
				sb.append("</merchantTermURL>");
			}

			// add merchantData (Optional)
			if ((getMerchantData() != null)
				&& (getMerchantData().trim().length() != 0)) {
				sb.append("<merchantData>");
				sb.append(XMLUtil.filterSpecialChars(getMerchantData()));
				sb.append("</merchantData>");
			}

			//Set transactionId (Optional)
			if ((getTransactionID() != null)
				&& (getTransactionID().trim().length() != 0)) {
				sb.append("<transactionID>");
				sb.append(XMLUtil.filterSpecialChars(getTransactionID()));
				sb.append("</transactionID>");
			}

			sb.append("<pan>");
			sb.append(XMLUtil.filterSpecialChars(getPan()));
			sb.append("</pan><panExpiry>");
			sb.append(XMLUtil.filterSpecialChars(getPanExpiry()));
			sb.append("</panExpiry><purchaseDate>");
			sb.append(XMLUtil.filterSpecialChars(getPurchaseDate()));
			sb.append("</purchaseDate>");

			// add purchaseAmount (Optional)
			if ((purchaseAmount != null) && (purchaseAmount.length() != 0)) {
				sb.append("<purchaseAmount>");
				sb.append(XMLUtil.filterSpecialChars(getPurchaseAmount()));
				sb.append("</purchaseAmount>");
			}

			sb.append("<purchasePurchAmount>");
			sb.append(XMLUtil.filterSpecialChars(getPurchasePurchAmount()));
			sb.append("</purchasePurchAmount>");

			// add purchaseCurrency (Optional)
			if ((getPurchaseCurrency() != null)
				&& (getPurchaseCurrency().length() != 0)) {
				sb.append("<purchaseCurrency>");
				sb.append(XMLUtil.filterSpecialChars(getPurchaseCurrency()));
				sb.append("</purchaseCurrency>");
			}

			// add purchaseDesc (Optional)
			if ((getPurchaseDesc() != null)
				&& (getPurchaseDesc().length() != 0)) {
				sb.append("<purchaseDesc>");
				sb.append(XMLUtil.filterSpecialChars(getPurchaseDesc()));
				sb.append("</purchaseDesc>");
			}

			// add purchaseInfo (Optional)
			if ((getPurchaseInfo() != null)
				&& (getPurchaseInfo().length() != 0)) {
				sb.append("<purchaseInfo>");
				sb.append(XMLUtil.filterSpecialChars(getPurchaseInfo()));
				sb.append("</purchaseInfo>");
			}

			// add browserCategory (Optional)
			if ((getBrowserCategory() != null)
				&& (getBrowserCategory().length() != 0)) {
				sb.append("<browserCategory>");
				sb.append(XMLUtil.filterSpecialChars(getBrowserCategory()));
				sb.append("</browserCategory>");
			}

			// add browserAccept (Optional)
			if ((getBrowserAccept() != null)
				&& (getBrowserAccept().length() != 0)) {
				sb.append("<browserAccept>");
				sb.append(XMLUtil.filterSpecialChars(getBrowserAccept()));
				sb.append("</browserAccept>");
			}

			// add browserAgent (Optional)
			if ((getBrowserAgent() != null)
				&& (getBrowserAgent().length() != 0)) {
				sb.append("<browserAgent>");
				sb.append(XMLUtil.filterSpecialChars(getBrowserAgent()));
				sb.append("</browserAgent>");
			}

			// add Purchase Recurring (Optional)
			if ((getPurchaseFrequency() != null)
				|| (getPurchaseEndRecur() != null)) {
				if ((getPurchaseFrequency() != null)
					&& (getPurchaseEndRecur() != null)) {
					sb.append("<PurchaseRecur><frequency>");
					sb.append(
						XMLUtil.filterSpecialChars(getPurchaseFrequency()));
					sb.append("</frequency><endRecur>");
					sb.append(
						XMLUtil.filterSpecialChars(getPurchaseEndRecur()));
					sb.append("</endRecur></PurchaseRecur>");
				} else {
					logger.error(
						"Failed to create PaymentVerifReq message document."
							+ "[Recurring frequency & endRecur should be co-existed.]");
					return null;
				}
			}

			// add purchaseInstall (Optional)
			if ((getPurchaseInstall() != null)
				&& (getPurchaseInstall().length() != 0)) {
				sb.append("<purchaseInstall>");
				sb.append(XMLUtil.filterSpecialChars(getPurchaseInstall()));
				sb.append("</purchaseInstall>");
			}

			sb.append("</PaymentVerifReq></MPI_Interface>");

			return sb.toString();
		} catch (Exception e) {
			logger.error(
				"Failed to convert PaymentVerifReq message to XML string.",
				e);
			return null;
		}

	}

	/**
	 * Returns the merchantID.
	 * @return String
	 */
	public String getMerchantID() {
		return merchantID;
	}

	/**
	 * Returns the purchaseInfo.
	 * @return String
	 */
	public String getPurchaseInfo() {
		return purchaseInfo;
	}

	/**
	 * Sets the merchantID.
	 * @param merchantID The merchantID to set
	 */
	public void setMerchantID(String merchantID) throws MessagingException {
		//Check Field: Merchant ID
		MessageValidator.validateField(
			getId(),
			merchantID,
			"[PaymentVerifyReq.merchantID]",
			1,
			24,
			true);

		// check format
		if (!MessageValidator.isValidMerchantID(merchantID)) {
			logger.error(MERCHANT_ERRMSG_LONG);
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentVerifReq.merchantID]",
				MERCHANT_ERRMSG,
				MERCHANT_ERRMSG_LONG);
		}

		this.merchantID = merchantID;
	}

	/**
	 * Sets the purchaseInfo.
	 * @param purchaseInfo The purchaseInfo to set
	 */
	public void setPurchaseInfo(String purchaseInfo) {
		this.purchaseInfo = purchaseInfo;
	}

	/**
	 * Returns the transactionId.
	 * @return String
	 */
	public String getTransactionID() {
		return transactionID;
	}

	/**
	 * Sets the transactionId.
	 * @param transactionId The transactionId to set
	 */
	public void setTransactionID(String transactionId)
		throws MessagingException {
		/** validate input transactionId */
		MessageValidator.validateField(
			getId(),
			transactionId,
			"[PaymentVerifReq.transactionID]",
			28,
			28,
			false);
		this.transactionID = transactionId;
	}

}
