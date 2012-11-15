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
import com.oncecorp.visa3d.mpi.domain.payment.CurrencyCode;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageValidator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.Base64Conversion;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implement message class for PARes. The PaymentAuthRes (Payment
 * Authentication Response) message is sent by the Merchant Plug-in to response
 * the PaymentAuthReq regardless of whether authentication is successful.
 *
 * @version $Revision: 20 $
 * @author	Alan Zhang
 */
public class PaymentAuthResMessage extends Message {

	// Error messages
	private static final String INVALIDATE_ERRMSG =
		"Invalid  format. Must be yyyyMMdd HH:mm:ss.";
	/**
	 * Message Type
	 */
	public final static String MSG_TYPE = "PaymentAuthRes";

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

	private static final String PAMNT_ERRMSG = "Null, size is 0.";
	private static final String PAMNT_ERRMSG_LONG =
		"Invalid field: [PaymentAuthRes.purchaseAmount] - " + PAMNT_ERRMSG;

	/**
	 * XML Message structure
	 */
	private static final String PAYMENTAUTHRES_XML_STRING =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<MPI_Interface>"
			+ "<PaymentAuthRes>"
			+ "<version/>"
			+ "<merchantID/>"
			+ "<purchaseDate/>"
			+ "<purchaseAmount/>"
			+ "<authDate/>"
			+ "<transactionID/>"
			+ "<currency/>"
			+ "<exponent/>"
			+ "<status/>"
			+ "</PaymentAuthRes>"
			+ "</MPI_Interface>";

	private static final String PCURR_ERRMSG = "Null, size is 0.";
	private static final String PCURR_ERRMSG_LONG =
		"Invalid field: [PaymentAuthRes.currency] - " + PCURR_ERRMSG;

	private static final String PCURR2_ERRMSG = "Invalid currency code.";
	private static final String PCURR2_ERRMSG_LONG =
		"Invalid field: [PaymentAuthRes.currency] - " + PCURR2_ERRMSG;

	private static final String PEXP_ERRMSG = "Null, size is 0.";
	private static final String PEXP_ERRMSG_LONG =
		"Invalid field: [PaymentAuthRes.exponent] - " + PEXP_ERRMSG;
	private static final String TXDATE_ERRMSG_LONG =
		"Invalid field: [PaymentAuthRes.purchaseDate] - " + INVALIDATE_ERRMSG;

	private static final String TXSTATUS_ERRMSG = "Null, size is 0.";
	private static final String TXSTATUS_ERRMSG_LONG =
		"Invalid field: [PaymentAuthRes.status] - " + TXSTATUS_ERRMSG;
	private static final String TXTIME_ERRMSG_LONG =
		"Invalid field: [PaymentAuthRes.authDate] - " + INVALIDATE_ERRMSG;
	private String authDate;
	private String cavv;
	private String cavvAlgorithm;
	private String currency;
	private String eci;
	private String exponent;
	private String invalidDetail;
	private String invalidReq;

	/**
	 * Local Log4J logger
	 */
	private Logger logger =
		MPILogger.getLogger(PaymentAuthResMessage.class.getName());
	private String merchantData;

	// Message Structure
	private String merchantID;
	private String merchantURL;
	private String purchaseAmount;
	private String purchaseDate;
	private String status;
	private String transactionID;
	private String vendorCode;

	/**
	 * Constructor
	 */
	public PaymentAuthResMessage() throws MessagingException {
		//Set message type
		setType(MSG_TYPE);

		//Set message version
		setVersion(MSG_VERSION);
	}

	/**
	 * Convert the value of the CAVV or the XID in base64 format into a new format
	 * according to the properly set option in the configuration
	 * 
	 * @param in					Value in base64 format that needs to be converted
	 * @param formattingOption 	Option name that determines which conversion to apply
	 * @return	Converted value according to the configuration option
	 */
	private String convertValue(String in, String formattingOption) {
		String format;

		// Check if value is available and not empty
		if (in == null || in.length() == 0) {
			return in;
		}

		// Try the conversion now...
		try {
			format = (String) Config.getConfigReference().getConfigData(formattingOption);
			return Base64Conversion.convert(in, format);
		}
		catch (ConfigurationException cfgExcep) {
			// Unable to find the proper configuration item so we send it as is
			return in;
		}
	}

	/**
	 * Gets the signatrure authorization
	 * @return Returns a String
	 */
	public String getAuthDate() {
		return authDate;
	}

	/**
	 * Gets the cavv
	 * @return Returns a String
	 */
	public String getCavv() {
		return cavv;
	}

	/**
	 * Gets the cavvAlgorithm
	 * @return Returns a String
	 */
	public String getCavvAlgorithm() {
		return cavvAlgorithm;
	}

	/**
	 * Gets the currency
	 * @return Returns a String
	 */
	public String getCurrency() {
		return currency;
	}

	/**
	 * Gets the eci
	 * @return Returns a String
	 */
	public String getEci() {
		return eci;
	}

	/**
	 * Gets the exponent
	 * @return Returns a String
	 */
	public String getExponent() {
		return exponent;
	}

	/**
	 * Gets the invalidDetail
	 * @return Returns a String
	 */
	public String getInvalidDetail() {
		return invalidDetail;
	}

	/**
	 * Gets the invalidReq
	 * @return Returns a String
	 */
	public String getInvalidReq() {
		return invalidReq;
	}

	/**
	 * Returns the merchantData.
	 * @return String
	 */
	public String getMerchantData() {
		return merchantData;
	}

	/**
	 * Returns the merchantID.
	 * @return String
	 */
	public String getMerchantID() {
		return merchantID;
	}

	/**
	 * @return String
	 */
	public String getMerchantURL() {
		return merchantURL;
	}

	/**
	 * Gets the purchAmount
	 * @return Returns a String
	 */
	public String getPurchaseAmount() {
		return purchaseAmount;
	}

	/**
	 * Gets the purchase
	 * @return Returns a String
	 */
	public String getPurchaseDate() {
		return purchaseDate;
	}

	/**
	 * Gets the status
	 * @return Returns a String
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Returns the transactionId.
	 * @return String
	 */
	public String getTransactionID() {
		return transactionID;
	}

	/**
	 * Gets the vendorCode
	 * @return Returns a String
	 */
	public String getVendorCode() {
		return vendorCode;
	}
	/**
	 * Sets the signature authorization
	 * @param in The time to set
	 */
	public void setAuthDate(String in) throws MessagingException {
		//Check Field: Transaction signature  & time
		// This attribute is equivalent to PARes.TX.time
		MessageValidator.validateField(
			getId(),
			in,
			"[PaymentAuthRes.authDate]",
			17,
			17,
			true);

		//Check format
		try {
			// Create formatter
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			sdf.setLenient(false);

			// Verify data format
			sdf.parse(in);

			//Release formatter
			sdf = null;
		} catch (ParseException pe) {
			this.logger.error(TXTIME_ERRMSG_LONG + " Value: {" + in + "}");
			throw new MessagingException(
				"UNKNOWN",
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentAuthRes.authDate]",
				INVALIDATE_ERRMSG,
				TXTIME_ERRMSG_LONG + " Value: {" + in + "}");
		}
		this.authDate = in;
	}
	/**
	 * Sets the cavv
	 * @param in The cavv to set
	 */
	public void setCavv(String cavv) throws MessagingException {
		//Check Field: Transaction Status
		// This field is equivalent to PARes.TX.cavv
		MessageValidator.validateField(
			getId(),
			cavv,
			"[PaymentAuthRes.cavv]",
			28,
			28,
			false);

		/*
		 * [Martin's Note: 24-Apr-03 4:39:41 PM]
		 * According to ENH-006, we need to be able to support sending the CAVV value using
		 * different representations. Those representation being:
		 * 	- Base64
		 *  - Binary
		 *  - Binhex
		 * 	- Asciihes
		 * So the setter here will look at the current representation wanted and properly convert
		 * the result into that representation.
		 */
		this.cavv = convertValue(cavv, MPIConfigDefinition.CAVV_FORMATTING);
	}
	/**
	 * Sets the cavvAlgorithm
	 * @param in The cavvAlgorithm to set
	 */
	public void setCavvAlgorithm(String in) throws MessagingException {
		MessageValidator.validateDigitField(
			getId(),
			in,
			"[PaymentAuthRes.algorithm]",
			1,
			-1,
			false);
		this.cavvAlgorithm = in;
	}
	/**
	 * Sets the currency
	 * @param in The currency to set
	 */
	public void setCurrency(String in) throws MessagingException {
		//Check Field: Purchase Currency
		// This field is equivalent to PARes.Purchase.currency
		MessageValidator.validateDigitField(
			getId(),
			in,
			"[PaymentAuthRes.currency]",
			1,
			3,
			true);

		// Validate currency using currency code table
		if (!CurrencyCode.isCodeValid(in)) {
			this.logger.error(PCURR2_ERRMSG_LONG + " Value: {" + in + "}");
			throw new MessagingException(
				"UNKNOWN",
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentAuthRes.currency]",
				PCURR2_ERRMSG,
				PCURR2_ERRMSG_LONG + " Value: {" + in + "}");
		}

		this.currency = in;
	}
	/**
	 * Sets the eci
	 * @param in The eci to set
	 */
	public void setEci(String in) throws MessagingException {
		// This field is equivalent to PARes.TX.eci
		MessageValidator.validateDigitField(
			getId(),
			in,
			"[PaymentAuthRes.eci]",
			2,
			2,
			false);
		this.eci = in;
	}
	/**
	 * Sets the exponent
	 * @param in The exponent to set
	 */
	public void setExponent(String in) throws MessagingException {
		//Check Field: Purchase Exponent
		// This field is equivalent to PARes.Purchase.exponent
		MessageValidator.validateDigitField(
			getId(),
			in,
			"[PaymentAuthRes.exponent]",
			1,
			3,
			true);
		this.exponent = in;
	}

	/**
	 * Sets the id. Overrides super.setId()
	 *
	 * @param in The id
	 */
	public void setId(String in) throws MessagingException {
		//Check Field: id
		MessageValidator.validateField(
			"UNKNOWN",
			in,
			"[PaymentAuthRes.id]",
			1,
			-1,
			true);
		super.setId(in);
	}
	/**
	 * Sets the invalidDetail
	 * @param invalidDetail The invalidDetail to set
	 */
	public void setInvalidDetail(String invalidDetail)
		throws MessagingException {
		MessageValidator.validateField(
			getId(),
			invalidDetail,
			"[PaymentAuthRes.invalidDetail]",
			1,
			-1,
			false);
		this.invalidDetail = invalidDetail;
	}
	/**
	 * Sets the invalidReq
	 * @param invalidReq The invalidReq to set
	 */
	public void setInvalidReq(String invalidReq) throws MessagingException {
		MessageValidator.validateDigitField(
			getId(),
			invalidReq,
			"[PaymentAuthRes.invalidReq]",
			1,
			3,
			false);
		this.invalidReq = invalidReq;
	}

	/**
	 * Sets the merchantData.
	 * @param merchantData The merchantData to set
	 */
	public void setMerchantData(String merchantData) {
		this.merchantData = merchantData;
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
			"[PaymentAuthRes.merchantID]",
			1,
			24,
			true);
		this.merchantID = merchantID;
	}

	/**
	 * Sets the merchantURL.
	 * @param merchantURL The merchantURL to set
	 */
	public void setMerchantURL(String merchantURL) {
		this.merchantURL = merchantURL;
	}
	/**
	 * Sets the purchAmount
	 * @param in The purchAmount to set
	 */
	public void setPurchaseAmount(String in) throws MessagingException {
		//Check Field: Purchase Amount
		// This field is equivalent to PARes.Purchase.purchAmount
		MessageValidator.validateDigitField(
			getId(),
			in,
			"[PaymentAuthRes.purchaseAmount]",
			1,
			12,
			true);
		this.purchaseAmount = in;
	}
	/**
	 * Sets the
	 * @param in The  to set
	 */
	public void setPurchaseDate(String in) throws MessagingException {
		//Check Field: Purchase Date & Time
		// This attribute is equivalent to PARes.Purchase.
		MessageValidator.validateField(
			getId(),
			in,
			"[PaymentAuthRes.purchaseDate]",
			17,
			17,
			true);

		//Check  format
		try {
			//Create formatter
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			sdf.setLenient(false);

			//Verify data format
			sdf.parse(in);

			//Release formatter
			sdf = null;
		} catch (ParseException pe) {
			this.logger.error(TXDATE_ERRMSG_LONG + " Value: {" + in + "}");
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentAuthRes.purchaseDate]",
				INVALIDATE_ERRMSG,
				TXDATE_ERRMSG_LONG + " Value: {" + in + "}");
		}
		this.purchaseDate = in;
	}
	/**
	 * Sets the status
	 * @param in The status to set
	 */
	public void setStatus(String in) throws MessagingException {
		//Check Field: Transaction Status
		// This field is equivalent to PARes.TX.status
		MessageValidator.validateField(
			getId(),
			in,
			"[PaymentAuthRes.status]",
			1,
			1,
			true);
		this.status = in;
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
			"[PaymentAuthRes.transactionID]",
			28,
			28,
			true);

		/*
		 * [Martin's Note: 24-Apr-03 4:39:41 PM]
		 * According to ENH-006, we need to be able to support sending the CAVV value using
		 * different representations. Those representation being:
		 * 	- Base64
		 *  - Binary
		 *  - Binhex
		 * 	- Asciihes
		 * So the setter here will look at the current representation wanted and properly convert
		 * the result into that representation.
		 */
		this.transactionID = convertValue(transactionId, MPIConfigDefinition.XID_FORMATTING);
	}
	/**
	 * Sets the vendorCode
	 * @param vendorCode The vendorCode to set
	 */
	public void setVendorCode(String vendorCode) throws MessagingException {
		MessageValidator.validateField(
			getId(),
			vendorCode,
			"[PaymentAuthRes.vendorCode]",
			1,
			256,
			false);
		this.vendorCode = vendorCode;
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
					+ "<PaymentAuthRes id=\"");

			sb.append(XMLUtil.filterSpecialChars(getId()));
			sb.append("\"><version>");
			sb.append(XMLUtil.filterSpecialChars(getVersion()));
			sb.append("</version><merchantID>");
			sb.append(XMLUtil.filterSpecialChars(getMerchantID()));
			sb.append("</merchantID><purchaseDate>");
			sb.append(XMLUtil.filterSpecialChars(getPurchaseDate()));
			sb.append("</purchaseDate><purchaseAmount>");
			sb.append(XMLUtil.filterSpecialChars(getPurchaseAmount()));
			sb.append("</purchaseAmount><authDate>");
			sb.append(XMLUtil.filterSpecialChars(getAuthDate()));
			sb.append("</authDate><transactionID>");
			sb.append(XMLUtil.filterSpecialChars(getTransactionID()));
			sb.append("</transactionID><currency>");
			sb.append(XMLUtil.filterSpecialChars(getCurrency()));
			sb.append("</currency><exponent>");
			sb.append(XMLUtil.filterSpecialChars(getExponent()));
			sb.append("</exponent><status>");
			sb.append(XMLUtil.filterSpecialChars(getStatus()));
			sb.append("</status>");

			// add Cardholder Authentication Verification Value (Optional)
			if (getCavv() != null) {
				sb.append("<cavv>");
				sb.append(XMLUtil.filterSpecialChars(getCavv()));
				sb.append("</cavv>");
			}

			//Set Electronic Commerce Indicator (Optional)
			if (getEci() != null) {
				sb.append("<eci>");
				sb.append(XMLUtil.filterSpecialChars(getEci()));
				sb.append("</eci>");
			}

			//Set CAVV Algorithm (Optional)
			if (getCavvAlgorithm() != null) {
				sb.append("<algorithm>");
				sb.append(XMLUtil.filterSpecialChars(getCavvAlgorithm()));
				sb.append("</algorithm>");
			}

			//Set Invalid Request Code (Optional)
			if (getInvalidReq() != null) {
				sb.append("<invalidReq>");
				sb.append(XMLUtil.filterSpecialChars(getInvalidReq()));
				sb.append("</invalidReq>");

				//Set Invalid Request Detail (Optional)
				if (getInvalidDetail() != null) {
					sb.append("<invalidDetail>");
					sb.append(XMLUtil.filterSpecialChars(getInvalidDetail()));
					sb.append("</invalidDetail>");
				}

				//Set Vendor Code (Optional)
				if (getVendorCode() != null) {
					sb.append("<vendorCode>");
					sb.append(XMLUtil.filterSpecialChars(getVendorCode()));
					sb.append("</vendorCode>");
				}
			}

			sb.append("</PaymentAuthRes></MPI_Interface>");

			return sb.toString();
		} catch (Exception e) {
			logger.error(
				"Failed to convert PaymentAuthRes message to XML string.",
				e);
			return null;
		}

	}

	/**
	 * XMLSerializable interface method.
	 * This method consturcts a DOM document which represents this message.
	 * @return DOM document object
	 */
	public Document toXML() {
		try {
			//XML String used to create PaymentAuthRes message document
			//Create document template
			Document dom =
				XMLUtil.createDocument(PAYMENTAUTHRES_XML_STRING, false);

			//Sanity check
			if (dom == null) {
				this.logger.error("Failed to create document template.");
				return null;
			}

			//Shared attributes
			Element element;

			//Add message id attribute (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthRes");
			element.setAttribute("id", getId());

			//Set version (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthRes/version");
			element.appendChild(dom.createTextNode(getVersion()));

			//Set merchantID (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthRes/merchantID");
			element.appendChild(dom.createTextNode(getMerchantID()));

			//Set Purchase  & time (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthRes/purchaseDate");
			element.appendChild(dom.createTextNode(getPurchaseDate()));

			//Set Purchase amount (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthRes/purchaseAmount");
			element.appendChild(dom.createTextNode(getPurchaseAmount()));

			//Set Transaction signature Date & Time (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthRes/authDate");
			element.appendChild(dom.createTextNode(getAuthDate()));

			//Set TransactionId (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthRes/transactionID");
			element.appendChild(dom.createTextNode(getTransactionID()));

			//Set Purchase currency (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthRes/currency");
			element.appendChild(dom.createTextNode(getCurrency()));

			//Set Purchase exponent (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthRes/exponent");
			element.appendChild(dom.createTextNode(getExponent()));

			//Set Transaction Status (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthRes/status");
			element.appendChild(dom.createTextNode(getStatus()));

			//Set Cardholder Authentication Verification Value (Optional)
			if (getCavv() != null) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentAuthRes");
				Element child = dom.createElement("cavv");
				child.appendChild(dom.createTextNode(getCavv()));
				element.appendChild(child);
			}

			//Set Electronic Commerce Indicator (Optional)
			if (getEci() != null) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentAuthRes");
				Element child = dom.createElement("eci");
				child.appendChild(dom.createTextNode(getEci()));
				element.appendChild(child);
			}

			//Set CAVV Algorithm (Optional)
			if (getCavvAlgorithm() != null) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentAuthRes");
				Element child = dom.createElement("algorithm");
				child.appendChild(dom.createTextNode(getCavvAlgorithm()));
				element.appendChild(child);
			}

			//Set Invalid Request Code (Optional)
			if (getInvalidReq() != null) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentAuthRes");
				Element child = dom.createElement("invalidReq");
				child.appendChild(dom.createTextNode(getInvalidReq()));
				element.appendChild(child);

				//Set Invalid Request Detail (Optional)
				if (getInvalidDetail() != null) {
					child = dom.createElement("invalidDetail");
					child.appendChild(dom.createTextNode(getInvalidDetail()));
					element.appendChild(child);
				}

				//Set Vendor Code (Optional)
				if (getVendorCode() != null) {
					child = dom.createElement("vendorCode");
					child.appendChild(dom.createTextNode(getVendorCode()));
					element.appendChild(child);
				}
			}

			return dom;
		} catch (Exception e) {
			//Catch any problem that prevents us from creating DOM document
			this.logger.error(
				"Failed to convert PaymentAuthRes message to DOM document.",
				e);
			return null;
		}
	}

	/**
	 * Overrides super class validate()
	 */
	public boolean validate() throws MessagingException {
		//Check mandatory fields
		MessageValidator.validateField(
			"UNKNOWN",
			getId(),
			"[PaymentAuthRes.id]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getMerchantID(),
			"[PaymentAuthRes.merchantID]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getPurchaseDate(),
			"[PaymentAuthRes.purchaseDate]",
			1,
			-1,
			true);
		MessageValidator.validateDigitField(
			getId(),
			getPurchaseAmount(),
			"[PaymentAuthRes.purchaseAmount]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getAuthDate(),
			"[PaymentAuthRes.authDate]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getTransactionID(),
			"[PaymentAuthRes.transactionID]",
			1,
			-1,
			true);
		MessageValidator.validateDigitField(
			getId(),
			getCurrency(),
			"[PaymentAuthRes.currency]",
			1,
			-1,
			true);
		MessageValidator.validateDigitField(
			getId(),
			getExponent(),
			"[PaymentAuthRes.exponent]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getStatus(),
			"[PaymentAuthRes.status]",
			1,
			-1,
			true);

		//Check Field: Transaction Status
		/*
		 * [Martin's Note: Nov 1, 2002 12:46:40 PM]
		 * Added support for TX.status = "A" as per 1.0.2 protocol
		 */
		if ((getStatus().equalsIgnoreCase("y"))
			|| (getStatus().equalsIgnoreCase("a"))) {
			//Status is 'Y' or 'A', then Cavv, eci, CavvAlgorithm become mandatory
			//Check Field: Cardholder Authentication Verification Value
			MessageValidator.validateField(
				getId(),
				getCavv(),
				"[PaymentAuthRes.cavv]",
				1,
				-1,
				true);
			MessageValidator.validateDigitField(
				getId(),
				getEci(),
				"[PaymentAuthRes.eci]",
				1,
				-1,
				true);
			MessageValidator.validateDigitField(
				getId(),
				getCavvAlgorithm(),
				"[PaymentAuthRes.algorithm]",
				1,
				-1,
				true);
		}

		return true;
	}

}