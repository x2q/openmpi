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

import com.oncecorp.visa3d.mpi.domain.payment.PAReqMessage;
import com.oncecorp.visa3d.mpi.domain.payment.VEReqMessage;

/**
 * This class represents an cache unit for Payment Transaction.
 * 
 * @author Alan Zhang
 * @version 1.0 21-Feb-03
 */
public class PaymentCacheObject {

	private VEReqMessage vereqMsg;
	private PAReqMessage pareqMsg;
	private String acsUrl;
	private String merchantData;
	private String protocolType;
	private String merchantURL;			// Merchant URL as specified in the VerifReq request or from the DB


	/**
	 * Returns the acsUrl.
	 * @return String
	 */
	public String getAcsUrl() {
		return acsUrl;
	}

	/**
	 * Returns the merchantData.
	 * @return String
	 */
	public String getMerchantData() {
		return merchantData;
	}

	/**
	 * Returns the pareqMsg.
	 * @return PAReqMessage
	 */
	public PAReqMessage getPareqMsg() {
		return pareqMsg;
	}

	/**
	 * Returns the protocolType.
	 * @return String
	 */
	public String getProtocolType() {
		return protocolType;
	}

	/**
	 * Returns the vereqMsg.
	 * @return VEReqMessage
	 */
	public VEReqMessage getVereqMsg() {
		return vereqMsg;
	}

	/**
	 * Sets the acsUrl.
	 * @param acsUrl The acsUrl to set
	 */
	public void setAcsUrl(String acsUrl) {
		this.acsUrl = acsUrl;
	}

	/**
	 * Sets the merchantData.
	 * @param merchantData The merchantData to set
	 */
	public void setMerchantData(String merchantData) {
		this.merchantData = merchantData;
	}

	/**
	 * Sets the pareqMsg.
	 * @param pareqMsg The pareqMsg to set
	 */
	public void setPareqMsg(PAReqMessage pareqMsg) {
		this.pareqMsg = pareqMsg;
	}

	/**
	 * Sets the protocolType.
	 * @param protocolType The protocolType to set
	 */
	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType;
	}

	/**
	 * Sets the vereqMsg.
	 * @param vereqMsg The vereqMsg to set
	 */
	public void setVereqMsg(VEReqMessage vereqMsg) {
		this.vereqMsg = vereqMsg;
	}

	/**
	 * @return String
	 */
	public String getMerchantURL() {
		return merchantURL;
	}

	/**
	 * Sets the merchantURL.
	 * @param merchantURL The merchantURL to set
	 */
	public void setMerchantURL(String merchantURL) {
		this.merchantURL = merchantURL;
	}

}
