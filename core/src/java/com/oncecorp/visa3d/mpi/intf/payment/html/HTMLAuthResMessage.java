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

package com.oncecorp.visa3d.mpi.intf.payment.html;

import com.oncecorp.visa3d.mpi.intf.payment.PaymentAuthResMessage;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentCacheObject;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentMessageCacheManager;
import com.oncecorp.visa3d.mpi.intf.payment.UniqueWindowIDGenerator;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import org.apache.log4j.Logger;

/**
 * Class description
 *
 * @author Alan Zhang
 * @version 1.0 20-Feb-03
 */
public class HTMLAuthResMessage extends HTMLMessage implements HTMLFieldDefinition {

	private String tdsId;
	private String tdsMerchantID;
	private String tdsMerchantData;
	private String tdsAuthDate;
	private String tdsTransactionID;
	private String tdsCurrency;
	private String tdsExponent;
	private String tdsStatus;
	private String tdsEci;
	private String tdsCavv;
	private String tdsAlgorithm;
	private String tdsInvalidReq;
	private String ucafAuthenticationData;
	private String ucafPaymentCardNumber;
    private String merchantURL;
	private String tdsType;
	private String tdsVersion;
	private String tdsPurchaseAmount;
	private String tdsPurchaseDate;
	private String tdsVendorCode;
	private String tdsInvalidDetails;

	protected Logger logger =
		MPILogger.getLogger(HTMLAuthResMessage.class.getName());

	/**
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#isValid()
	 */
	public boolean isValid() {
		// mandatory fields check
		if (XMLUtil.isNull(getTdsId())) {
			logger.error(
				"Field TDS_id should be provided. Current value: "
					+ getTdsId());
			return false;
		}

		if (XMLUtil.isNull(getTdsMerchantID())) {
			logger.error(
				"Field TDS_merchantID should be provided. Current value: "
					+ getTdsMerchantID());
			return false;
		}

		if (XMLUtil.isNull(getTdsMerchantData())) {
			logger.error(
				"Field TDS_merchantData should be provided. Current value: "
					+ getTdsMerchantData());
			return false;
		}

		if (XMLUtil.isNull(getTdsAuthDate())) {
			logger.error(
				"Field TDS_authDate should be provided. Current value: "
					+ getTdsAuthDate());
			return false;
		}

		if (XMLUtil.isNull(getTdsTransactionID())) {
			logger.error(
				"Field TDS_transactionID should be provided. Current value: "
					+ getTdsTransactionID());
			return false;
		}

		if (XMLUtil.isNull(getTdsCurrency())) {
			logger.error(
				"Field TDS_currency should be provided. Current value: "
					+ getTdsCurrency());
			return false;
		}

		if (XMLUtil.isNull(getTdsExponent())) {
			logger.error(
				"Field TDS_exponent should be provided. Current value: "
					+ getTdsExponent());
			return false;
		}

		if (XMLUtil.isNull(getTdsStatus())) {
			logger.error(
				"Field TDS_status should be provided. Current value: "
					+ getTdsStatus());
			return false;
		}

		return true;
	}

	/**
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#transform
	 */
	public Object transform( ) {
        logger.debug("Enter HTMLAuthResMessage transform.");

        boolean statusFlag = true;

        StringBuffer sb = new StringBuffer("<html>\n");
        sb.append("  <head><title>3-D Secure Authentication</title></head>\n");
        sb.append("  <body onload = \"OnLoadEvent();\" >\n");
		/**
		 * [Gang Wu's Note: June 10, 2003] The Window will close and the result
		 * will display on the merchant main windw.
		 */
		/*
        sb.append("    <center>\n");
        sb.append("      <h1>RESULTS PAGE</h1>\n");
        sb.append("      Your online purchase was\n");

        String status = getTdsStatus();
        if ( status == null || !status.equalsIgnoreCase("Y") )
        {
            sb.append("NOT ");
            statusFlag = false;
        }

        sb.append("      properly authenticated.<p/>\n");
        sb.append("      Confirmation number:\n");
        sb.append("      " + new String(Base64.decode(tdsTransactionID)) + "<br><br>\n");
        sb.append("      CAVV: " + tdsCavv + "\n<br>");
        sb.append("      ECI: " + tdsEci + "<p/>\n");
        sb.append("      </center>\n");
		*/
        sb.append( generateHiddenFields( statusFlag ) );
		sb.append( "<noscript>"
				  + "		<br>"
	              + "		<br>"
		          + "		<center>"
		          + "		<h1>Processing your 3-D Secure Transactions</h1>"
		          + "		<h2>JavaScript is currently disabled or is not supported by your browser.</h2>"
		          + "		<h3>Please click on the Submit button to continue the processing of your 3-D Secure transaction </h3>"
		          + "		<input type=\"submit\" value=\"Submit\">"
		          + "		</center>"
		          + "</noscript>");
//        sb.append("      <input type=\"submit\" name=\"submit\" value=\"Return to Merchant Site\">\n");
        sb.append("     </form>\n");
		sb.append( "<SCRIPT LANGUAGE=\"JavaScript\">"
		            + "function OnLoadEvent()"
		            + "{"
			  + "document.AuthResReturn.target = \"mainWindow"
	          + UniqueWindowIDGenerator.getWindowID( getTdsId() ) + "\";"
			        + "setTimeout('window.close()',500);"
		            + "document.AuthResReturn.submit();"
		            + "}"
		            + "</SCRIPT>");

        sb.append("  </body>\n");
        sb.append("</html>\n");

        return sb.toString();
	}

    /**
     * Add Master secure code hidden fields to the HTML form which will be finally
     * delivered to the Merchant application.
     * @param statusFlag - the status flag
     * @return - the HTML form string which contains all hidden fields definition.
     */
    public String generateHiddenFields( boolean statusFlag )
    {
        StringBuffer sb = new StringBuffer();

        sb.append("    <form name=\"AuthResReturn\" action=\"");
        sb.append( getMerchantURL() );
        sb.append("\" method=\"post\" >\n");

        // TDS_id
		printHiddenFields( sb, TDS_ID, getTdsId() );

        // TDS_MERCHANT_ID
		printHiddenFields( sb, TDS_MERCHANT_ID, getTdsMerchantID() );

        // TDS_MERCHANT_DATA
		printHiddenFields( sb, TDS_MERCHANT_DATA, getTdsMerchantData() );

        // TDS_AUTH_DATE
		printHiddenFields( sb, TDS_AUTH_DATE, getTdsAuthDate() );

        // TDS_TRANSACTION_ID
		printHiddenFields( sb, TDS_TRANSACTION_ID, getTdsTransactionID() );

        // TDS_CURRENCY
		printHiddenFields( sb, TDS_CURRENCY, getTdsCurrency() );

        // TDS_EXPONENT
		printHiddenFields( sb, TDS_EXPONENT, getTdsExponent() );

        // TDS_STATUS
		printHiddenFields( sb, TDS_STATUS, getTdsStatus() );

		// TDS_TYPE
		printHiddenFields( sb, TDS_TYPE, getTdsType() );

		// TDS_VERSION
		printHiddenFields( sb, TDS_VERSION, getTdsVersion() );

		// TDS_VENDOR_CODE
		printHiddenFields( sb, TDS_VENDOR_CODE, getTdsVendorCode() );

		// TDS_PURCHASE_AMOUNT
		printHiddenFields( sb, TDS_PURCHASE_AMOUNT, getTdsPurchaseAmount() );

		// TDS_PURCHASE_DATE
		printHiddenFields( sb, TDS_PURCHASE_DATE, getTdsPurchaseDate() );

        if ( statusFlag )
        {
            // TDS_ECI
			printHiddenFields( sb, TDS_ECI, getTdsEci() );

            // TDS_CAVV
			printHiddenFields( sb, TDS_CAVV, getTdsCavv() );

            // TDS_ALGORITHM
			printHiddenFields( sb, TDS_ALGORITHM, getTdsAlgorithm() );

            // UCAF_AUTHENTICATION_DATA
			printHiddenFields( sb, UCAF_AUTHENTICATION_DATA, getUcafAuthenticationData() );

            // UCAF_PAYMENT_CARD_NUMBER
			printHiddenFields( sb, UCAF_PAYMENT_CARD_NUMBER, getUcafPaymentCardNumber() );

        }
        else
        {
            // TDS_ERROR_CODE
			printHiddenFields( sb, TDS_INVALID_REQ, getTdsInvalidReq() );

			// TDS_ERROR_MESSAGE
			printHiddenFields( sb, TDS_INVALID_DETAILS, getTdsInvalidDetails() );

        }

        return sb.toString();
    }

	/**
	 * Populates fields with given PaymentAuthResMessage
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#populate(Object)
	 */
	public void populate(Object o) {
		// sanity check
		if ((o == null) || !(o instanceof PaymentAuthResMessage)) {
			logger.error("HTMLAuthResMessage could be only populated with a PaymentAuthResMessage.");
			return;
		}

        logger.debug("Enter HTMLAuthResMessage populate.");
		PaymentAuthResMessage authRes = (PaymentAuthResMessage) o;
		setTdsId(authRes.getId());
		setTdsMerchantID(authRes.getMerchantID());
		setTdsMerchantData(authRes.getMerchantData());
		setTdsAuthDate(authRes.getAuthDate());
		setTdsTransactionID(authRes.getTransactionID());
		setTdsCurrency(authRes.getCurrency());
		setTdsExponent(authRes.getExponent());
		setTdsStatus(authRes.getStatus());
		setTdsEci(authRes.getEci());
		setTdsCavv(authRes.getCavv());
		setTdsAlgorithm(authRes.getCavvAlgorithm());
		setTdsInvalidReq(authRes.getInvalidReq());
		setUcafAuthenticationData(authRes.getCavv());
        setMerchantURL( authRes.getMerchantURL() );

		setTdsType( authRes.getType() );
		setTdsVersion( authRes.getVersion() );
		setTdsPurchaseAmount( authRes.getPurchaseAmount() );
		setTdsPurchaseDate( authRes.getPurchaseDate() );
		setTdsVendorCode( authRes.getVendorCode() );
		setTdsInvalidDetails( authRes.getInvalidDetail() );

        // the Ucaf_Payment_Card_Number would be set in HTMLAuthenticatorServlet

        /*
         * [Martin's Note - June 27, 2003 - 11:29
         * I don't see why we should populate the card number in the HTMLAuthenticatorServlet.
         * This should actually be done here but the problem is that the creditcard number is only
         * available in the VEReq message in the PaymentCacheObject.
         */

        // Retrieve cached item from cache manager
        Object item = (new PaymentMessageCacheManager()).getCachedItem(authRes.getId());
        String pan;
        if (item == null) {
            pan = "";
        }
        else {
            // retrieves cardnumber from VEReq message
            pan  = ((PaymentCacheObject) item).getVereqMsg().getPan();
        }

        // Format it and set it according to the SecureCode rule
        setUcafPaymentCardNumber(maskSecureCodePan(pan));
	}

    /**
     * Mask the Pan number according to the SecureCode rule
     * @param   pan PAN/Credit card number from the VEReq message
     * @return  Masked PAN number
     */
    private String maskSecureCodePan(String pan) {
        String maskedPan = "00000";
        if (pan != null && pan.length() >= 5) {                  // Ensure the PAN is AT least 5 long....
            int startPos = pan.length() - 5;
            maskedPan = pan.substring(startPos, startPos + 5);   // Just grab the last 5 digits of the card
        }

        return maskedPan;
    }

    /**
	 * Returns the tdsAlgorithm.
	 * @return String
	 */
	public String getTdsAlgorithm() {
		return tdsAlgorithm;
	}

	/**
	 * Returns the tdsAuthDate.
	 * @return String
	 */
	public String getTdsAuthDate() {
		return tdsAuthDate;
	}

	/**
	 * Returns the tdsCavv.
	 * @return String
	 */
	public String getTdsCavv() {
		return tdsCavv;
	}

	/**
	 * Returns the tdsCurrency.
	 * @return String
	 */
	public String getTdsCurrency() {
		return tdsCurrency;
	}

	/**
	 * Returns the tdsEci.
	 * @return String
	 */
	public String getTdsEci() {
		return tdsEci;
	}

	/**
	 * Returns the tdsInvalidReq.
	 * @return String
	 */
	public String getTdsInvalidReq() {
		return tdsInvalidReq;
	}

	/**
	 * Returns the tdsExponent.
	 * @return String
	 */
	public String getTdsExponent() {
		return tdsExponent;
	}

	/**
	 * Returns the tdsId.
	 * @return String
	 */
	public String getTdsId() {
		return tdsId;
	}

	/**
	 * Returns the tdsMerchantID.
	 * @return String
	 */
	public String getTdsMerchantID() {
		return tdsMerchantID;
	}

	/**
	 * Returns the tdsStatus.
	 * @return String
	 */
	public String getTdsStatus() {
		return tdsStatus;
	}

	/**
	 * Returns the tdsTransactionID.
	 * @return String
	 */
	public String getTdsTransactionID() {
		return tdsTransactionID;
	}

	/**
	 * Returns the ucafAuthenticationData.
	 * @return String
	 */
	public String getUcafAuthenticationData() {
		return ucafAuthenticationData;
	}

	/**
	 * Returns the ucafPaymentCardNumber.
	 * @return String
	 */
	public String getUcafPaymentCardNumber() {
		return ucafPaymentCardNumber;
	}

	/**
	 * Sets the tdsAlgorithm.
	 * @param tdsAlgorithm The tdsAlgorithm to set
	 */
	public void setTdsAlgorithm(String tdsAlgorithm) {
		this.tdsAlgorithm = tdsAlgorithm;
	}

	/**
	 * Sets the tdsAuthDate.
	 * @param tdsAuthDate The tdsAuthDate to set
	 */
	public void setTdsAuthDate(String tdsAuthDate) {
		this.tdsAuthDate = tdsAuthDate;
	}

	/**
	 * Sets the tdsCavv.
	 * @param tdsCavv The tdsCavv to set
	 */
	public void setTdsCavv(String tdsCavv) {
		this.tdsCavv = tdsCavv;
	}

	/**
	 * Sets the tdsCurrency.
	 * @param tdsCurrency The tdsCurrency to set
	 */
	public void setTdsCurrency(String tdsCurrency) {
		this.tdsCurrency = tdsCurrency;
	}

	/**
	 * Sets the tdsEci.
	 * @param tdsEci The tdsEci to set
	 */
	public void setTdsEci(String tdsEci) {
		this.tdsEci = tdsEci;
	}

	/**
	 * Sets the tdsInvalidReq.
	 * @param tdsInvalidReq The tdsInvalidReq to set
	 */
	public void setTdsInvalidReq(String tdsInvalidReq) {
		this.tdsInvalidReq = tdsInvalidReq;
	}

	/**
	 * Sets the tdsExponent.
	 * @param tdsExponent The tdsExponent to set
	 */
	public void setTdsExponent(String tdsExponent) {
		this.tdsExponent = tdsExponent;
	}

	/**
	 * Sets the tdsId.
	 * @param tdsId The tdsId to set
	 */
	public void setTdsId(String tdsId) {
		this.tdsId = tdsId;
	}

	/**
	 * Sets the tdsMerchantID.
	 * @param tdsMerchantID The tdsMerchantID to set
	 */
	public void setTdsMerchantID(String tdsMerchantID) {
		this.tdsMerchantID = tdsMerchantID;
	}

	/**
	 * Sets the tdsStatus.
	 * @param tdsStatus The tdsStatus to set
	 */
	public void setTdsStatus(String tdsStatus) {
		this.tdsStatus = tdsStatus;
	}

	/**
	 * Sets the tdsTransactionID.
	 * @param tdsTransactionID The tdsTransactionID to set
	 */
	public void setTdsTransactionID(String tdsTransactionID) {
		this.tdsTransactionID = tdsTransactionID;
	}

	/**
	 * Sets the ucafAuthenticationData.
	 * @param ucafAuthenticationData The ucafAuthenticationData to set
	 */
	public void setUcafAuthenticationData(String ucafAuthenticationData) {
		this.ucafAuthenticationData = ucafAuthenticationData;
	}

	/**
	 * Sets the ucafPaymentCardNumber.
	 * @param ucafPaymentCardNumber The ucafPaymentCardNumber to set
	 */
	public void setUcafPaymentCardNumber(String ucafPaymentCardNumber) {
		this.ucafPaymentCardNumber = ucafPaymentCardNumber;
	}

	/**
	 * Returns the tdsMerchantData.
	 * @return String
	 */
	public String getTdsMerchantData() {
		return tdsMerchantData;
	}

	/**
	 * Sets the tdsMerchantData.
	 * @param tdsMerchantData The tdsMerchantData to set
	 */
	public void setTdsMerchantData(String tdsMerchantData) {
		this.tdsMerchantData = tdsMerchantData;
	}

    /**
    *
    * @return the String value of merchantURL.
    */
    public String getMerchantURL(){
        return merchantURL;
    }

    /**
    *
    * @param aMerchantURL - the new value for merchantURL
    */
    public void setMerchantURL(String aMerchantURL){
        merchantURL = aMerchantURL;
    }


	/**
	*
	* @return the String value of tdsType.
	*/
	public String getTdsType(){
		return tdsType;
	}

	/**
	*
	* @param aTdsType - the new value for tdsType
	*/
	public void setTdsType(String aTdsType){
		tdsType = aTdsType;
	}


	/**
	*
	* @return the String value of tdsVersion.
	*/
	public String getTdsVersion(){
		return tdsVersion;
	}

	/**
	*
	* @param aTdsVersion - the new value for tdsVersion
	*/
	public void setTdsVersion(String aTdsVersion){
		tdsVersion = aTdsVersion;
	}


	/**
	*
	* @return the String value of tdsPurchaseAmount.
	*/
	public String getTdsPurchaseAmount(){
		return tdsPurchaseAmount;
	}

	/**
	*
	* @param aTdsPurchaseAmount - the new value for tdsPurchaseAmount
	*/
	public void setTdsPurchaseAmount(String aTdsPurchaseAmount){
		tdsPurchaseAmount = aTdsPurchaseAmount;
	}


	/**
	*
	* @return the String value of tdsPurchaseDate.
	*/
	public String getTdsPurchaseDate(){
		return tdsPurchaseDate;
	}

	/**
	*
	* @param aTdsPurchaseDate - the new value for tdsPurchaseDate
	*/
	public void setTdsPurchaseDate(String aTdsPurchaseDate){
		tdsPurchaseDate = aTdsPurchaseDate;
	}


	/**
	*
	* @return the String value of tdsVendorCode.
	*/
	public String getTdsVendorCode(){
		return tdsVendorCode;
	}

	/**
	*
	* @param aTdsVerdorCode - the new value for tdsVendorCode
	*/
	public void setTdsVendorCode(String aTdsVerdorCode){
		tdsVendorCode = aTdsVerdorCode;
	}


	/**
	*
	* @return the String value of tdsInvalidDetails.
	*/
	public String getTdsInvalidDetails(){
		return tdsInvalidDetails;
	}

	/**
	*
	* @param aTdsInvalidDetails - the new value for tdsInvalidDetails
	*/
	public void setTdsInvalidDetails(String aTdsInvalidDetails){
		tdsInvalidDetails = aTdsInvalidDetails;
	}


}
