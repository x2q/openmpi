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

import org.apache.log4j.Logger;

import com.oncecorp.visa3d.mpi.intf.payment.PaymentVerifReqMessage;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.MessageGenerator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * Class description
 *
 * @author Alan Zhang
 * @version 1.0 20-Feb-03
 */
public class HTMLVerifReqMessage extends HTMLMessage implements HTMLFieldDefinition {

	private String tdsId;
	private String tdsVersion;
	private String tdsMerchantID;
	private String tdsMerchantTermURL;
	private String tdsMerchantData;
	private String tdsTransactionID;
	private String tdsCardNumber;
	private String tdsCardExpiry;
	private String tdsPurchaseDate;
	private String tdsDisplayAmount;
	private String tdsPurchaseAmount;
	private String tdsCurrencyCode;
	private String tdsPurchaseDesc;
	private String tdsPurchaseInfo;
	private String tdsBrowserCat;
	private String tdsBrowserAccept;
	private String tdsBrowserAgent;
	private String tdsPurchaseInstall;
	private String tdsPurchaseFrequency;
	private String tdsPurchaseEndRecur;
	private String ucafEnabled;

	protected Logger logger =
		MPILogger.getLogger(HTMLVerifReqMessage.class.getName());

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

		if (XMLUtil.isNull(getTdsCardNumber())) {
			logger.error(
				"Field TDS_cardNumber should be provided. Current value: "
					+ getTdsCardNumber());
			return false;
		}

		if (XMLUtil.isNull(getTdsCardExpiry())) {
			logger.error(
				"Field TDS_cardExpiry should be provided. Current value: "
					+ getTdsCardExpiry());
			return false;
		}

		if (XMLUtil.isNull(getTdsPurchaseDate())) {
			logger.error(
				"Field TDS_purchaseDate should be provided. Current value: "
					+ getTdsPurchaseDate());
			return false;
		}

		if (XMLUtil.isNull(getTdsDisplayAmount())) {
			logger.error(
				"Field TDS_displayAmount should be provided. Current value: "
					+ getTdsDisplayAmount());
			return false;
		}

		if (XMLUtil.isNull(getTdsPurchaseAmount())) {
			logger.error(
				"Field TDS_purchaseAmount should be provided. Current value: "
					+ getTdsPurchaseAmount());
			return false;
		}

		return true;
	}

	/**
	 * Transforms HTMLVerifReqMessage to PaymentVerifReqMessage
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#transform()
	 */
	public Object transform( ) throws MessagingException {

        logger.debug("Enter HTMLVerifReqMessage transform.");
        PaymentVerifReqMessage pvreq =
                null;
        try {
            String msgVersion = PaymentVerifReqMessage.MSG_VERSION;
            if ( getTdsVersion() != null && !getTdsVersion().trim().equals("") )
                msgVersion = getTdsVersion();

            // Instantiates PaymentVerifReqMessage
            pvreq = (PaymentVerifReqMessage) MessageGenerator.create(PaymentVerifReqMessage.MSG_TYPE, msgVersion);

            // Transforms fields
            pvreq.setId(getTdsId());
            pvreq.setMerchantID(getTdsMerchantID());
            pvreq.setMerchantTermURL(getTdsMerchantTermURL());
            pvreq.setMerchantData(getTdsMerchantData());
            pvreq.setTransactionID(getTdsTransactionID());
            pvreq.setPan(getTdsCardNumber());
            pvreq.setPanExpiry(getTdsCardExpiry());
            pvreq.setPurchaseDate(getTdsPurchaseDate());
            pvreq.setPurchaseAmount(getTdsDisplayAmount());
            pvreq.setPurchasePurchAmount(getTdsPurchaseAmount());
            pvreq.setPurchaseCurrency(getTdsCurrencyCode());
            pvreq.setPurchaseDesc(getTdsPurchaseDesc());
            pvreq.setPurchaseInfo(getTdsPurchaseInfo());
            pvreq.setBrowserCategory(getTdsBrowserCat());
            pvreq.setBrowserAccept(getTdsBrowserAccept());
            pvreq.setBrowserAgent(getTdsBrowserAgent());
            pvreq.setPurchaseInstall( getTdsPurchaseInstall() );
            pvreq.setPurchaseEndRecur( getTdsPurchaseEndRecur() );
            pvreq.setPurchaseFrequency( getTdsPurchaseFrequency() );

            logger.debug("Exit HTMLVerifReqMessage transform.");
        }
        catch (MessagingException e) {
            // Set the proper id and throw back
            e.setId(getTdsId());
            throw e;
        }

        return pvreq;
	}

	/**
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#populate(Object)
	 */
	public void populate(Object o) {
        if ( o == null || !( o instanceof HttpServletRequest ) )
        {
            logger.error( "The html content is null or not a valida servlet request.");
            return;
        }
        logger.debug("Enter HTMLVerifReqMessage populate.");
        HttpServletRequest request = (HttpServletRequest) o;

        setTdsId( request.getParameter( TDS_ID ) );
		setTdsVersion( request.getParameter( TDS_VERSION ) );
        setTdsMerchantID( request.getParameter(TDS_MERCHANT_ID));
        setTdsMerchantTermURL(request.getParameter(TDS_MERCHANT_TERM_URL));
        setTdsMerchantData(request.getParameter(TDS_MERCHANT_DATA));
        setTdsTransactionID(request.getParameter(TDS_TRANSACTION_ID));
        setTdsCardNumber(request.getParameter(TDS_CARD_NUMBER));
        setTdsCardExpiry(request.getParameter(TDS_CARD_EXPIRY));
        setTdsPurchaseDate(request.getParameter(TDS_PURCHASE_DATE));
        setTdsDisplayAmount(request.getParameter(TDS_DISPLAY_AMOUNT));
        setTdsPurchaseAmount(request.getParameter(TDS_PURCHASE_AMOUNT));
        setTdsCurrencyCode(request.getParameter(TDS_CURRENCY_CODE));
        setTdsPurchaseDesc(request.getParameter(TDS_PURCHASE_DETDS));
        setTdsPurchaseInfo(request.getParameter(TDS_PURCHASE_INFO));
		setTdsPurchaseFrequency(request.getParameter(TDS_PURCHASE_FREQUENCY));
		setTdsPurchaseEndRecur(request.getParameter(TDS_PURCHASE_END_RECUR));
		setTdsPurchaseInstall(request.getParameter(TDS_PURCHASE_INSTALL));
        setTdsBrowserCat(request.getParameter(TDS_BROWSER_CAT));
        setTdsBrowserAccept(request.getParameter(TDS_BROWSER_ACCEPT));
        setTdsBrowserAgent(request.getParameter(TDS_BROWSER_AGENT));
        setUcafEnabled(request.getParameter(UCAF_ENABLED));
        logger.debug("Exit HTMLVerifReqMessage populate.");

	}

	/**
	 * Returns the tdsBrowserAccept.
	 * @return String
	 */
	public String getTdsBrowserAccept() {
		return tdsBrowserAccept;
	}

	/**
	 * Returns the tdsBrowserAgent.
	 * @return String
	 */
	public String getTdsBrowserAgent() {
		return tdsBrowserAgent;
	}

	/**
	 * Returns the tdsBrowserCat.
	 * @return String
	 */
	public String getTdsBrowserCat() {
		return tdsBrowserCat;
	}

	/**
	 * Returns the tdsCardExpiry.
	 * @return String
	 */
	public String getTdsCardExpiry() {
		return tdsCardExpiry;
	}

	/**
	 * Returns the tdsCardNumber.
	 * @return String
	 */
	public String getTdsCardNumber() {
		return tdsCardNumber;
	}

	/**
	 * Returns the tdsCurrencyCode.
	 * @return String
	 */
	public String getTdsCurrencyCode() {
		return tdsCurrencyCode;
	}

	/**
	 * Returns the tdsDisplayAmount.
	 * @return String
	 */
	public String getTdsDisplayAmount() {
		return tdsDisplayAmount;
	}

	/**
	 * Returns the tdsId.
	 * @return String
	 */
	public String getTdsId() {
		return tdsId;
	}

	/**
	 * Returns the tdsMerchantData.
	 * @return String
	 */
	public String getTdsMerchantData() {
		return tdsMerchantData;
	}

	/**
	 * Returns the tdsMerchantID.
	 * @return String
	 */
	public String getTdsMerchantID() {
		return tdsMerchantID;
	}

	/**
	 * Returns the tdsMerchantTermURL.
	 * @return String
	 */
	public String getTdsMerchantTermURL() {
		return tdsMerchantTermURL;
	}

	/**
	 * Returns the tdsPurchaseAmount.
	 * @return String
	 */
	public String getTdsPurchaseAmount() {
		return tdsPurchaseAmount;
	}

	/**
	 * Returns the tdsPurchaseDate.
	 * @return String
	 */
	public String getTdsPurchaseDate() {
		return tdsPurchaseDate;
	}

	/**
	 * Returns the tdsPurchaseDesc.
	 * @return String
	 */
	public String getTdsPurchaseDesc() {
		return tdsPurchaseDesc;
	}

	/**
	 * Returns the tdsPurchaseInfo.
	 * @return String
	 */
	public String getTdsPurchaseInfo() {
		return tdsPurchaseInfo;
	}

	/**
	 * Returns the tdsTransactionID.
	 * @return String
	 */
	public String getTdsTransactionID() {
		return tdsTransactionID;
	}

	/**
	 * Returns the ucafEnabled.
	 * @return String
	 */
	public String getUcafEnabled() {
		return ucafEnabled;
	}

	/**
	 * Sets the tdsBrowserAccept.
	 * @param tdsBrowserAccept The tdsBrowserAccept to set
	 */
	public void setTdsBrowserAccept(String tdsBrowserAccept) {
		this.tdsBrowserAccept = tdsBrowserAccept;
	}

	/**
	 * Sets the tdsBrowserAgent.
	 * @param tdsBrowserAgent The tdsBrowserAgent to set
	 */
	public void setTdsBrowserAgent(String tdsBrowserAgent) {
		this.tdsBrowserAgent = tdsBrowserAgent;
	}

	/**
	 * Sets the tdsBrowserCat.
	 * @param tdsBrowserCat The tdsBrowserCat to set
	 */
	public void setTdsBrowserCat(String tdsBrowserCat) {
		this.tdsBrowserCat = tdsBrowserCat;
	}

	/**
	 * Sets the tdsCardExpiry.
	 * @param tdsCardExpiry The tdsCardExpiry to set
	 */
	public void setTdsCardExpiry(String tdsCardExpiry) {
		this.tdsCardExpiry = tdsCardExpiry;
	}

	/**
	 * Sets the tdsCardNumber.
	 * @param tdsCardNumber The tdsCardNumber to set
	 */
	public void setTdsCardNumber(String tdsCardNumber) {
		this.tdsCardNumber = tdsCardNumber;
	}

	/**
	 * Sets the tdsCurrencyCode.
	 * @param tdsCurrencyCode The tdsCurrencyCode to set
	 */
	public void setTdsCurrencyCode(String tdsCurrencyCode) {
		this.tdsCurrencyCode = tdsCurrencyCode;
	}

	/**
	 * Sets the tdsDisplayAmount.
	 * @param tdsDisplayAmount The tdsDisplayAmount to set
	 */
	public void setTdsDisplayAmount(String tdsDisplayAmount) {
		this.tdsDisplayAmount = tdsDisplayAmount;
	}

	/**
	 * Sets the tdsId.
	 * @param tdsId The tdsId to set
	 */
	public void setTdsId(String tdsId) {
		this.tdsId = tdsId;
	}

	/**
	 * Sets the tdsMerchantData.
	 * @param tdsMerchantData The tdsMerchantData to set
	 */
	public void setTdsMerchantData(String tdsMerchantData) {
		this.tdsMerchantData = tdsMerchantData;
	}

	/**
	 * Sets the tdsMerchantID.
	 * @param tdsMerchantID The tdsMerchantID to set
	 */
	public void setTdsMerchantID(String tdsMerchantID) {
		this.tdsMerchantID = tdsMerchantID;
	}

	/**
	 * Sets the tdsMerchantTermURL.
	 * @param tdsMerchantTermURL The tdsMerchantTermURL to set
	 */
	public void setTdsMerchantTermURL(String tdsMerchantTermURL) {
		this.tdsMerchantTermURL = tdsMerchantTermURL;
	}

	/**
	 * Sets the tdsPurchaseAmount.
	 * @param tdsPurchaseAmount The tdsPurchaseAmount to set
	 */
	public void setTdsPurchaseAmount(String tdsPurchaseAmount) {
		this.tdsPurchaseAmount = tdsPurchaseAmount;
	}

	/**
	 * Sets the tdsPurchaseDate.
	 * @param tdsPurchaseDate The tdsPurchaseDate to set
	 */
	public void setTdsPurchaseDate(String tdsPurchaseDate) {
		this.tdsPurchaseDate = tdsPurchaseDate;
	}

	/**
	 * Sets the tdsPurchaseDesc.
	 * @param tdsPurchaseDesc The tdsPurchaseDesc to set
	 */
	public void setTdsPurchaseDesc(String tdsPurchaseDesc) {
		this.tdsPurchaseDesc = tdsPurchaseDesc;
	}

	/**
	 * Sets the tdsPurchaseInfo.
	 * @param tdsPurchaseInfo The tdsPurchaseInfo to set
	 */
	public void setTdsPurchaseInfo(String tdsPurchaseInfo) {
		this.tdsPurchaseInfo = tdsPurchaseInfo;
	}

	/**
	 * Sets the tdsTransactionID.
	 * @param tdsTransactionID The tdsTransactionID to set
	 */
	public void setTdsTransactionID(String tdsTransactionID) {
		this.tdsTransactionID = tdsTransactionID;
	}

	/**
	 * Sets the ucafEnabled.
	 * @param ucafEnabled The ucafEnabled to set
	 */
	public void setUcafEnabled(String ucafEnabled) {
		this.ucafEnabled = ucafEnabled;
	}



	/**
	*
	* @return the String value of tdsPurchaseInstall.
	*/
	public String getTdsPurchaseInstall(){
		return tdsPurchaseInstall;
	}

	/**
	*
	* @param aTdsPurchaseInstall - the new value for tdsPurchaseInstall
	*/
	public void setTdsPurchaseInstall(String aTdsPurchaseInstall){
		tdsPurchaseInstall = aTdsPurchaseInstall;
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
	* @return the String value of tdsPurchaseFrequency.
	*/
	public String getTdsPurchaseFrequency(){
		return tdsPurchaseFrequency;
	}

	/**
	*
	* @param aTdsPurchaseFrequency - the new value for tdsPurchaseFrequency
	*/
	public void setTdsPurchaseFrequency(String aTdsPurchaseFrequency){
		tdsPurchaseFrequency = aTdsPurchaseFrequency;
	}


	/**
	*
	* @return the String value of tdsPurchaseEndRecur.
	*/
	public String getTdsPurchaseEndRecur(){
		return tdsPurchaseEndRecur;
	}

	/**
	*
	* @param aTdsPurchaseEndRecur - the new value for tdsPurchaseEndRecur
	*/
	public void setTdsPurchaseEndRecur(String aTdsPurchaseEndRecur){
		tdsPurchaseEndRecur = aTdsPurchaseEndRecur;
	}



}
