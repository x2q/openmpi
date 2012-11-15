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

package com.oncecorp.visa3d.mpi.configuration;

import java.io.Serializable;

/**
 * Class description
 * 
 * @author azhang
 * @version $Revision: 5 $
 */
public class MerchantMetaInfo implements Serializable {
	
	// Internal data structure of a merchant
	private String merchantID;
	private String merchantName;
	private String merchantPassword;
	private String merchantURL;
	private String merchantCountryCode;
	private String merchantPurchaseCurrency;
	private String acquirerBIN;
	private byte protocolSupport;
	
	private String licensingKey = null;
	private long keyExpiryDate = 0;
	
	/**
	 * Bit-wise value that indicate authentication protocol supportes for each merchant. 
	 * The support information is contained in the PROTOCOL_SUPPORT column. 
	 * Bit 0: VbV support information
	 * Bit 1: SecureCode support information
	 */
	public static final byte MERCHANT_SUPPORT_VBV = 1;
	public static final byte MERCHANT_SUPPORT_SECURECODE = 2; 
	
	/**
	 * Returns the merchantCountryCode.
	 * @return String
	 */
	public String getMerchantCountryCode() {
		return merchantCountryCode;
	}

	/**
	 * Returns the merchantID.
	 * @return String
	 */
	public String getMerchantID() {
		return merchantID;
	}

	/**
	 * Returns the merchantName.
	 * @return String
	 */
	public String getMerchantName() {
		return merchantName;
	}

	/**
	 * Returns the merchantPassword.
	 * @return String
	 */
	public String getMerchantPassword() {
		return merchantPassword;
	}

	/**
	 * Returns the merchantPurchaseCurrency.
	 * @return String
	 */
	public String getMerchantPurchaseCurrency() {
		return merchantPurchaseCurrency;
	}

	/**
	 * Returns the merchantURL.
	 * @return String
	 */
	public String getMerchantURL() {
		return merchantURL;
	}

	/**
	 * Sets the merchantCountryCode.
	 * @param merchantCountryCode The merchantCountryCode to set
	 */
	public void setMerchantCountryCode(String merchantCountryCode) {
		this.merchantCountryCode = merchantCountryCode;
	}

	/**
	 * Sets the merchantID.
	 * @param merchantID The merchantID to set
	 */
	public void setMerchantID(String merchantID) {
		this.merchantID = merchantID;
	}

	/**
	 * Sets the merchantName.
	 * @param merchantName The merchantName to set
	 */
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	/**
	 * Sets the merchantPassword.
	 * @param merchantPassword The merchantPassword to set
	 */
	public void setMerchantPassword(String merchantPassword) {
		this.merchantPassword = merchantPassword;
	}

	/**
	 * Sets the merchantPurchaseCurrency.
	 * @param merchantPurchaseCurrency The merchantPurchaseCurrency to set
	 */
	public void setMerchantPurchaseCurrency(String merchantPurchaseCurrency) {
		this.merchantPurchaseCurrency = merchantPurchaseCurrency;
	}

	/**
	 * Sets the merchantURL.
	 * @param merchantURL The merchantURL to set
	 */
	public void setMerchantURL(String merchantURL) {
		this.merchantURL = merchantURL;
	}

	/**
	 * Returns the acquirerBIN.
	 * @return String
	 */
	public String getAcquirerBIN() {
		return acquirerBIN;
	}

	/**
	 * Sets the acquirerBIN.
	 * @param acquirerBIN The acquirerBIN to set
	 */
	public void setAcquirerBIN(String acquirerBIN) {
		this.acquirerBIN = acquirerBIN;
	}

	/**
	 * @return byte
	 */
	public byte getProtocolSupport() {
		return protocolSupport;
	}

	/**
	 * Sets the protocol_support.
	 * @param protocol_support The protocol_support to set
	 */
	public void setProtocolSupport(byte protocolSupport) {
		this.protocolSupport = protocolSupport;
	}

	/**
	 * Returns the keyExpiryDate.
	 * @return Date
	 */
	public long getKeyExpiryDate() {
		return keyExpiryDate;
	}

	/**
	 * Returns the licensingKey.
	 * @return String
	 */
	public String getLicensingKey() {
		return licensingKey;
	}

	/**
	 * Sets the keyExpiryDate.
	 * @param keyExpiryDate The keyExpiryDate to set
	 */
	public void setKeyExpiryDate(long keyExpiryDate) {
		this.keyExpiryDate = keyExpiryDate;
	}

	/**
	 * Sets the licensingKey.
	 * @param licensingKey The licensingKey to set
	 */
	public void setLicensingKey(String licensingKey) {
		this.licensingKey = licensingKey;
	}

}
