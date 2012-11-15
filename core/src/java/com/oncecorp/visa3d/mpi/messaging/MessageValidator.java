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

import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.logging.MPILogger;

import org.apache.log4j.Logger;

/**
 * This class provides a generic message validator that allows both
 * field validation and constraint validation.
 * In it's first release, this class is pretty straighforward but should
 * be extended to include regular expression validation and more expressive
 * constraint validation procedure.
 * 
 * Note: I was tired of Copy&Pasting the same validation code all over the place...
 * 		 Centralization is better....
 * 
 * @author Martin Dufort (mdufort@oncecorp.com)
 * 
 */
public class MessageValidator {

	private static final String EMPTY_ERROR_MESSAGE =
		"The content of this field should be specified.";
	private static final String SIZE1_ERROR_MESSAGE = "Field size must be ";
	private static final String DIGIT_ERROR_MESSAGE =
		"Field value should be digits. But: ";

	/**
	 * Local Log4J logger
	 */
	private static Logger logger =
		MPILogger.getLogger(MessageValidator.class.getName());

	/**
	 * This method performs field validation by ensuring that the supplied
	 * field value is within the length range and it is specified.
	 * @param msgID		Message ID to use for exception creation.
	 * @param value		Value of field to validate
	 * @param fieldName	Name of field being validated
	 * @param minSize	Minimum length size for the field (-1 = no minimum)
	 * @param maxSize	Maximum length size for the field (-1 = no maximum)
	 * @param needed	Field is optional or mandatory
	 */
	static public void validateField(
		String msgID,
		Object value,
		String fieldName,
		int minSize,
		int maxSize,
		boolean needed)
		throws MessagingException {

		// Check for needed field which could be missing
		if ((needed) && (value == null)) {
			generateMissingElementError(msgID, fieldName, value);
		}

		// Check according to object type
		if (value instanceof String) {
			String strValue = (String) value;

			// If the field is mandatory, or if the field is not mandatory but has 
			// a value.
			if ((needed)
				|| (!needed)
				&& ((strValue != null) && (strValue.length() > 0))) {
				// No check for length
				if ((minSize == -1) && (maxSize == -1))
					return;

				// Just check the max length size
				if (minSize == -1) {
					if (strValue.length() > maxSize) {
						generateSizeError(
							msgID,
							fieldName,
							strValue,
							minSize,
							maxSize);
					}
				} else if (maxSize == -1) {
					if (strValue.length() < minSize) {
						generateSizeError(
							msgID,
							fieldName,
							strValue,
							minSize,
							maxSize);
					}
				} else {
					if ((strValue.length() > maxSize)
						|| (strValue.length() < minSize)) {
						generateSizeError(
							msgID,
							fieldName,
							strValue,
							minSize,
							maxSize);
					}
				}
			}
		}
	}

	private static void generateMissingElementError(
		String msgId,
		String fieldName,
		Object value)
		throws MessagingException {
		String emptyMessageLong =
			"Field is missing " + fieldName + ". - " + EMPTY_ERROR_MESSAGE;

		logger.debug(emptyMessageLong);
		throw new MessagingException(
			msgId,
			ErrorCodes.ERROR_CODE_3,
			ErrorCodes.ERROR_MESSAGE_3,
			fieldName,
			EMPTY_ERROR_MESSAGE,
			emptyMessageLong);
	}

	private static void generateSizeError(
		String msgId,
		String fieldName,
		Object value,
		int minSize,
		int maxSize)
		throws MessagingException {

		String errMessage = null;
		if (minSize == -1) {
			errMessage = SIZE1_ERROR_MESSAGE + "<= " + maxSize;
		} else if (maxSize == -1) {
			errMessage = SIZE1_ERROR_MESSAGE + ">= " + minSize;
		} else {
			errMessage =
				SIZE1_ERROR_MESSAGE + ">= " + minSize + " and <= " + maxSize;
		}

		String errMessageLong =
			"Invalid field: " + fieldName + " - " + errMessage;

		// Throw proper exception
		throw new MessagingException(
			msgId,
			ErrorCodes.ERROR_CODE_5,
			ErrorCodes.ERROR_MESSAGE_5,
			fieldName,
			errMessage,
			errMessageLong + " Value: {" + value + "}");
	}

	/**
	 * This method performs field validation by ensuring that the supplied
	 * field value is within the length range and it is specified and all in digits.
	 * @param msgID		Message ID to use for exception creation.
	 * @param value		Value of field to validate
	 * @param fieldName	Name of field being validated
	 * @param minSize	Minimum length size for the field (-1 = no minimum)
	 * @param maxSize	Maximum length size for the field (-1 = no maximum)
	 * @param needed	Field is optional or mandatory
	 */
	public static void validateDigitField(
		String msgID,
		Object value,
		String fieldName,
		int minSize,
		int maxSize,
		boolean needed)
		throws MessagingException {
		//Validate field length first
		validateField(msgID, value, fieldName, minSize, maxSize, needed);

		//Validate digits
		if ((needed) || (value != null)) {
			
			try {
				Long.parseLong((String) value);
			} catch (NumberFormatException nfe) {
					logger.debug(DIGIT_ERROR_MESSAGE + value);
					throw new MessagingException(
						msgID,
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						fieldName,
						DIGIT_ERROR_MESSAGE,
						DIGIT_ERROR_MESSAGE + value);
			}
		}
	}

	public static boolean isValidMerchantID(String id) {
		boolean valid = false;
		int hyphenIndex = id.indexOf("-");
		if (hyphenIndex < 0) {
			// no hyphen found
			if (id.length() <= 15)
				valid = true;
		} else {
			// hyphen found
			String firstPart = null;
			String secondPart = null;
			while (hyphenIndex >= 0) {
				firstPart = id.substring(0, hyphenIndex);
				if (hyphenIndex != (id.length() - 1)) {
					secondPart = id.substring(hyphenIndex + 1);
				} else {
					secondPart = "";
				}

				if ((firstPart.length() <= 15) && (secondPart.length() <= 8))
					valid = true;

				if (hyphenIndex != (id.length() - 1))
					hyphenIndex = id.indexOf("-", hyphenIndex + 1);
				else
					hyphenIndex = -1;
			}
		}

		return valid;
	}

	static public void validateConstraint() throws MessagingException {
	}
}
