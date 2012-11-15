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

package com.oncecorp.visa3d.mpi.utility;

import com.oncecorp.visa3d.mpi.logging.MPILogger;

import org.apache.log4j.Logger;

/**
 * This class is a utility class that provides Base64 conversion to several 
 * different format, namely:
 * 	- Binary
 * 	- Binhex
 * 	- Asciihex
 * 
 * This conversion is used with the PARes.CAVV and the PARes.xid values to 
 * accomodate merchant application that must submit those values using a 
 * different format than the standard "base64" one.
 * 
 * @author Martin Dufort (mdufort@oncecorp.com)
 */
public class Base64Conversion {
	/**
	 * Local Log4J logger
	 */
	private static Logger logger = MPILogger.getLogger(Base64Conversion.class.getName());
	
	/* Definition of the available conversion functions */
	public static final String TO_BASE64	= "ToBase64";
	public static final String TO_BINARY 	= "ToBinary";
	public static final String TO_BINHEX 	= "ToBinHex";
	public static final String TO_ASCIIHEX	= "ToAsciiHex";
	
	/**
	 * Convert a Base64 value into one of the other available formats
	 * @param in		Value to be converted
	 * @param format	Format to convert the value to 
	 * @return			Properly converted value
	 */
	public static String convert(String in, String format) throws IllegalArgumentException {
		logger.debug("Converting Base64 value: " + in + " to new format: " + format);	
	
		// If the format is unknown or we are requesting a Base64 format, then we
		// return the value as is...
		if ((format == null) || 
			(format.equalsIgnoreCase(TO_BASE64))) {
			return in;
		}
		
		// Decode value first		
		byte[] res = Base64Codec.decode(in);
		if (res == null) {
			// Unable to decode, probabling garbled characters
			return null;
		}
		
		// Conversion to a binary format ???
		if (format.equalsIgnoreCase(TO_BINARY)) {
			// Binary format is just returning the decoded value as a String
			return toBinary(res);
		}
//		
// ***** Not Supported for now!!!! *****
//		else if (format.equalsIgnoreCase(TO_BINHEX)) {
//			// To Binary hex format
//			/* 
//			 * [Martin's Note: 21-May-03 11:14:40 AM]
//			 * 
//			 * not currently supported. return the value as B64
//			 */
//			/* return toBinhex(res); */
//			return in;			
//		}
		else if (format.equalsIgnoreCase(TO_ASCIIHEX)) {
			// To ASCII Hex format
			return toAsciiHex(res);
		}
		else {
			// Unknown format wanted. we send back as original
			logger.debug("Unknown conversion format " + format + " specified for Base64 Conversion.\nReturning original value");
			return in;
			// throw new IllegalArgumentException("Unknown conversion format " + format + " specified for Base64 Conversion.");
		}
	}
	
	/**
	 * Return the binary version of the Base64 value. Since the 
	 * input is already base64 decoded we just create a String with the 
	 * supplied byte array and return it as our result.
	 * 
	 * @param value	Value to be converted
	 * @return			String represnting the binary representation 
	 */
	private static String toBinary(byte[] value) {
		return new String(value);
	}
	
	/**
	 * Return the binhex version of the base64 value supplied. This conversion
	 * operation is based on the algorithm found in Python 2.2.1 library and is 
	 * and adaption of it. Please refer to: 
	 * <href>http://www.python.org/doc/current/lib/module-binascii.html</href> 
	 * for more information
	 * 
	 * @param value	Value to be converted
	 * @return			String representing the binhex representation
	 */
	private static String toBinhex(byte[] value) {
		/* 
		 * [Martin's Note: 25-Apr-03 9:16:00 AM]
		 * 
		 * The only implementation of the BinHex algorithm I found is related to the 
		 * Macintosh and published by Peter Lewis. I'm not sure we should support that 
		 * conversion format because it is not readily available. The implementation is 
		 * postponed for now.......
		 * 
		 * Come and see me if you have a problem with this....
		 */
		return null;
	}
		
	/**
	 * Return the hexadecimal representation of the binary data. Every byte of data 
	 * is converted into the corresponding 2-digit hex representation. The resulting 
	 * string is therefore twice as long as the length of data
	 * 
	 * @param value	Value to be converted
	 * @return			String representing the ASCIIHex version of the value
	 */	
	private static String toAsciiHex(byte[] value) {
		// Define the hexadecimal alphabet here...
		char[] hexAlphabet = {	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
								'A', 'B', 'C', 'D', 'E', 'F' };
								
		StringBuffer hexResult = new StringBuffer();							   	
		for (int idx = 0; idx < value.length; idx++) {
			// All values are made up of 2 characters, so check both.
			int currentValue 	= value[idx];
			int bigEndian 		= currentValue >> 4;
			int smallEndian 	= currentValue - (bigEndian * 16);
			
			// Add both hex values to our string
			hexResult.append(hexAlphabet[bigEndian]);	
			hexResult.append(hexAlphabet[smallEndian]);
		}
		
		// Return our string result containing the hex value
		return hexResult.toString();
	}
	
}
