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

package com.oncecorp.visa3d.mpi.messaging;

import com.oncecorp.visa3d.mpi.domain.payment.CRReqMessage;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.meta.ExtensionMetaInfo;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageMetaInfo;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageUID;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Mapping Class of MPI message Extension element   
 *   
 * @version $Revision: 26 $
 * @author	Jun Shi
 * @author 	Alan Zhang
 */
public class Extension implements Serializable {
	/**
	 * Extension message structure
	 */
	private String extID;
	private String critical;
	private String extValue;
	private Serializable extData;

	/**
	 * Validation error messages
	 */
	private static final String CRITICAL_ERRMSG = "Size is zero(0), or value not equal to 'true' or 'false'";
	private static final String CRITICAL_ERRMSG_LONG = "Invalid field. [Extension.critical] - " + CRITICAL_ERRMSG;;

	/**
	 * Local Log4J logger
	 */
	private transient Logger logger =
		MPILogger.getLogger(CRReqMessage.class.getName());

	public Extension() {
		super();
	}

	/**
	 * Convenient method to create corresponding DOM element of
	 * Extension
	 */
	public Element toXML() throws Exception {
		//Create empty document
		Document doc = XMLUtil.createDocument();

		//Prepare extension element
		Element extensionElement = doc.createElement("Extension");

		//Set attribute value
		extensionElement.setAttribute("id", getExtID());
		extensionElement.setAttribute("critical", getCritical());

		//Set element value
		extensionElement.appendChild(doc.createTextNode(getExtValue()));
		
		//return result
		return extensionElement;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<Extension id=\"");
		sb.append(XMLUtil.filterSpecialChars(getExtID()));
		sb.append("\" critical=\"");
		sb.append(XMLUtil.filterSpecialChars(getCritical()));
		sb.append("\">");
		sb.append(XMLUtil.filterSpecialChars(getExtValue()));
		sb.append("</Extension>");
		
		return sb.toString();
	}

	/**
	 * Gets the extID
	 * @return Returns a String
	 */
	public String getExtID() {
		return extID;
	}
	/**
	 * Sets the extID
	 * @param extID The extID to set
	 */
	public void setExtID(String extID) throws MessagingException {
		// Validate proper extension id specification
		MessageValidator.validateField("UNKNOWN", extID, "[Extension.id]", 1, -1, true);
		this.extID = extID;
	}

	/**
	 * Gets the critical
	 * @return Returns a String
	 */
	public String getCritical() {
		return critical;
	}
	/**
	 * Sets the critical
	 * @param critical The critical to set
	 */
	public void setCritical(String critical) throws MessagingException {
		//Validate proper specification for critical attribute
		MessageValidator.validateField("UNKNOWN", critical, "[Extension.critical]", 4, 5, true);
		if ((! critical.equalsIgnoreCase("true")) &&
			(! critical.equalsIgnoreCase("false"))) {
				this.logger.error(CRITICAL_ERRMSG_LONG);
				throw new MessagingException(
					"UNKNOWN",
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[Extension.critical]",
					CRITICAL_ERRMSG,
					CRITICAL_ERRMSG_LONG + "Value: {" + critical + "}");
		}
		
		this.critical = critical;
	}

	/** 
	 * Indicate id extension is critical or not
	 * @return	true=Extension is critical, false=Extension is NOT critical
	 */
	public boolean isCritical() {
		return (getCritical().equalsIgnoreCase("true"));
	}
	
	/**
	 * Gets the extValue
	 * @return Returns a String
	 */
	public String getExtValue() {
		return extValue;
	}
	/**
	 * Sets the extValue
	 * @param extValue The extValue to set
	 */
	public void setExtValue(String extValue) {
		this.extValue = extValue;
	}

	public boolean validate() throws MessagingException {
		logger.debug("Validating current CRReq message...");

		//Check all the mandatory field existence
		MessageValidator.validateField("UNKNOWN", getExtID(), 			"[Extension.extID]", 		1, -1, true);
		MessageValidator.validateField("UNKNOWN", getCritical(), 		"[Extension.critical]", 	1, -1, true);
		
		return true;
	}
	
	
	/**
	 * Check the support of the extension for this particulay message. Support
	 * information is retrieved from the MessageDefinition information file
	 * @param aMsgType		Type of the message
	 * @param aMsgVersion	Version of the message
	 * @param extension		The extension needed to be tested for support
	 * @return boolean		True = Extension supported, False = Extension not supported.
	 */
	public static boolean isSupported(String aMsgType, String aMsgVersion, Extension extension) {
		// Fetch proper meta-information about extension definition for this message
		boolean supported = false;
		MessageUID uid = new MessageUID(aMsgType, aMsgVersion);

		// Retrieve extension metainfo and check for support
		ExtensionMetaInfo meta = MessageMetaInfo.getExtensionInfo(uid);
		if (meta != null) {
			supported = meta.isSupported(extension.getExtID(), extension.getExtValue());
		}			
		
		// If critical and not supported by us then this is NOT supported.
		// Everything else is OK
		if (extension.isCritical()) {
			if (supported) {
				return true;		
			}
			else {
				return false;
			}
		}
		else {
			// Since it is not critical, then it is supported by default
			return true;
		}
	}
}