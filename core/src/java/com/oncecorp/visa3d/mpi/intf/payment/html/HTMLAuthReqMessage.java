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

import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentAuthReqMessage;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentCacheObject;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentMessageCacheManager;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentVerifReqMessage;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.MessageGenerator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Class description
 *
 * @author Alan Zhang
 * @version 1.0 20-Feb-03
 */
public class HTMLAuthReqMessage extends HTMLMessage implements HTMLFieldDefinition {

	private String paRes;
	private String md;
	private String version;

	protected Logger logger =
		MPILogger.getLogger(HTMLAuthReqMessage.class.getName());

	/**
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#isValid()
	 */
	public boolean isValid() {
		// mandatory fields check
		if (XMLUtil.isNull(getPaRes())) {
			logger.error(
				"Field PaRes should be provided. Current value: " + getPaRes());
			return false;
		}

		if (XMLUtil.isNull(getMd())) {
			logger.error(
				"Field MD should be provided. Current value: " + getMd());
			return false;
		}

		return true;
	}

	/**
	 * Transforms HTMLAuthReqMessage to PaymentAuthReqMessage
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#transform()
	 */
	public Object transform(  ) throws MessagingException {
        PaymentAuthReqMessage authReq = null;
        logger.debug("Enter HTMLAuthReqMessage transform.");

		String msgVersion = PaymentAuthReqMessage.MSG_VERSION;
		if ( getVersion() != null && !getVersion().trim().equals("") )
	        msgVersion = getVersion();

        // Try to instantiate the proper message (type,version) element
        try {
            // Instantiates PaymentVerifReqMessage
            authReq = (PaymentAuthReqMessage) MessageGenerator.create(PaymentAuthReqMessage.MSG_TYPE, msgVersion);

            // Transforms fields
            authReq.setId(getMd());
            authReq.setPaymentAuthMsg(getPaRes());

            // Retrieve cached item from cache manager
            Object item = (new PaymentMessageCacheManager()).getCachedItem(authReq.getId());
            if (item == null) {
                logger.error("No cached item found for ID: " + authReq.getId());
                throw new MessagingException(
                    authReq.getId(),
                    ErrorCodes.ERROR_CODE_5,
                    ErrorCodes.ERROR_MESSAGE_5,
                    "PaymentAuthReq.id",
                    "No match found for PaymentAuthReq.id: " + authReq.getId(),
                    "No match found for PaymentAuthReq.id: " + authReq.getId());
            }

            // Retrieves merchantID
            authReq.setMerchantID( ((PaymentCacheObject) item).getVereqMsg().getMerID() );
            authReq.setMerchantData( ((PaymentCacheObject) item).getMerchantData() );

        }
        catch (MessagingException e) {
            // Add appropriate ID to this error and throw back up
            e.setId(getMd());
            throw e;
        }

        logger.debug("Exit HTMLAuthReqMessage transform.");
        return authReq;
    }

	/**
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#populate(Object)
	 */
	public void populate(Object o) {
        logger.debug("Enter HTMLAuthReqMessage populate.");
        if ( o == null || !( o instanceof HttpServletRequest ) ) {
            logger.error( "The html content is null or not a validation servlet request.");
            return;
        }
        HttpServletRequest request = (HttpServletRequest) o;

        setPaRes( request.getParameter( PARES ) );
        setMd( request.getParameter( MD ) );
		setVersion( request.getParameter( TDS_VERSION ) );
        logger.debug("Exit HTMLAuthReqMessage populate.");
	}

	/**
	 * Returns the md.
	 * @return String
	 */
	public String getMd() {
		return md;
	}

	/**
	 * Returns the paRes.
	 * @return String
	 */
	public String getPaRes() {
		return paRes;
	}

	/**
	 * Sets the md.
	 * @param md The md to set
	 */
	public void setMd(String md) {
		this.md = md;
	}

	/**
	 * Sets the paRes.
	 * @param paRes The paRes to set
	 */
	public void setPaRes(String paRes) {
		this.paRes = paRes;
	}

	/**
	*
	* @return the String value of version.
	*/
	public String getVersion(){
		return version;
	}

	/**
	*
	* @param aVersion - the new value for version
	*/
	public void setVersion(String aVersion){
		version = aVersion;
	}


}
