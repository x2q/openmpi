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

import java.util.Vector;

import org.apache.log4j.Logger;

import com.oncecorp.visa3d.mpi.configuration.ConfigAccessorGenerator;
import com.oncecorp.visa3d.mpi.configuration.ConfigAccessorType;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorMessage;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageMetaInfo;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageUID;

/**
 * Description: This class is used for generating a specific Message in
 * terms of type, version
 *  
 * @version 0.1 Aug 03, 2002
 * @author	Jun Shi
 */

public class MessageGenerator {

	// Local Log4J logger
	private static Logger logger =
		MPILogger.getLogger(MessageGenerator.class.getName());

	private static String overridenVersion = null;
	private static Vector visa3DSecureMessageUIDs;

	static {
		MessagingConfigAccessor accessor =
			(MessagingConfigAccessor) ConfigAccessorGenerator.getAccessor(
				ConfigAccessorType.ACCESSOR_TYPE_MESSAGING);
		try {
			visa3DSecureMessageUIDs =
				accessor.getAllMessageInfo(
					MessageMetaInfo.MESSAGE_PROTOCOL_3DSECURE);
		} catch (MessagingException e) {
			logger.error(
				"Failed to load 3DSecure messaging UIDs in MessageGenerator.",
				e);
		}
	}

	/** MessageGenerator Constructor */
	private MessageGenerator() {
	}

	/** 
	 * static method - create (type, version) return a specific Message
	 * according to submitted type and version.
	 * 
	 * @param type    - String
	 * @param version - String
	 * @return Message
	 */
	public static Message create(String type, String version)
		throws MessagingException {
		Message msg = null;
		String msgImpl = null;
		try {
            MessageUID uid = new MessageUID(type, version);
            if (!MessageMetaInfo.isVersionSupported(uid)) {
                 throw new MessagingException(
                     "unknown",
                     ErrorCodes.ERROR_CODE_5,
                     ErrorCodes.ERROR_MESSAGE_5,
                     "version",
                     "Version: " + version + " is not supported by this software release.",
                     "[MessageType:"
                         + type
                         + "] Version: "
                         + version
                         + " is not supported by this software release.)");
             }

            // Get implementation for that UID
 			msgImpl = MessageMetaInfo.getImpl(uid);
			if (msgImpl == null) {
				logger.error("Message Impl Not Found!!");
				throw new MessagingException("Message Impl Not Found!!");
			}

			// instantiate msgImpl class
			msg = (Message) Class.forName(msgImpl).newInstance();

			// By default the message is initialized to the latest version of the protocol it
			// is supporting. If it is supporting more than one version, we ensure than the 
			// requested version is set in this message instance....
			if (visa3DSecureMessageUIDs
				.contains(new MessageUID(msg.getType(), msg.getVersion()))) {
				if (getOverridenVersion() != null) {
					msg.setVersion(getOverridenVersion());
				} else {
					msg.setVersion(version);
				}
			} else {
				msg.setVersion(version);
			}

			// do logging
			logger.debug("create [" + msg.getClass() + "] completed !!");
		} catch (ClassCastException e1) {
			// Wrong type stored in the mapping file
			logger.fatal(
				"Wrong type stored in the mapping file. Unable to load message definition");
			throw new MessagingException("Wrong type stored in the mapping file. Unable to load message definition");
		} catch (Exception e) {
			logger.error(
				"Fatal Error: Message Impl Not Found or cannot instantiate",
				e);
			throw new MessagingException(
				"MessageGenerator:"
					+ " Message Impl Not Found or Cannot Instantiate");
		}

		// return created empty msg to caller
		return msg;
	}

	public static void checkVersionSupport(Message msg) {
		try {
			if ((msg != null) && (msg instanceof ErrorMessage)) {
				if (((ErrorMessage) msg).getErrorCode().equals("6")) {
					setOverridenVersion(((ErrorMessage) msg).getErrorDetail());
					logger.info(
						"Current support version is forced to: "
							+ getOverridenVersion());
				}
			}
		} catch (Exception e) {
			logger.debug("Error in checking Version Support.", e);
		}
	}

	/**
	 * Returns the currentVersion.
	 * @return String
	 */
	public static String getOverridenVersion() {
		return overridenVersion;
	}

	/**
	 * Sets the currentVersion.
	 * @param currentVersion The currentVersion to set
	 */
	public static void setOverridenVersion(String currentVersion) {
		MessageGenerator.overridenVersion = currentVersion;
	}

}