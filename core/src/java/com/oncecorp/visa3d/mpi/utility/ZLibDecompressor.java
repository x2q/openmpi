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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Logger;


/**
 * Description: This class is based on java.util.zip.Infalter. It provides 
 * support for general-purpose decompression using popular ZLIB library. 
 * Though ZLibCompressor is designed to compress data at "DEFAULT_COMPRESSION" 
 * level, this class is able to decompress data that compressed at any level.
 * 
 * @version 0.1 July 05, 2002
 * @author	Alan Zhang
 */
public class ZLibDecompressor {


	/** Encoding of String conversion */
	//private final static String DEFAULT_STRING_ENCODING = "ISO-8859-1";
	
	/**
	 * End of input
	 */
	private final static int EOF = -1;


	/**
	 * Local Log4J logger
	 */
	private static Logger logger =
		MPILogger.getLogger(ZLibDecompressor.class.getName());


	/**
	 * Decompress byte array. 
	 * 
	 * @param in The byte array to decompress
	 * @return Decompressed byte array or null (if decompression failed)
	 */
	public static byte[] decompress(byte[] in) {
		//Sanity check
		if (in == null)
			return null;


		try {
			//Underlying input stream
			ByteArrayInputStream bais = new ByteArrayInputStream(in);
			
			//Filter stream for uncompressing data in the "deflate" compression format.
			InflaterInputStream iis = new InflaterInputStream(bais);


			//output stream
			ByteArrayOutputStream baos = new ByteArrayOutputStream();


			int data = -1;
			//Return 0 after EOF has reached, otherwise always return 1
			while (iis.available() != 0) {
				if ((data = iis.read()) != EOF) 
					baos.write(data);
			}


			//Get result
			byte[] result = baos.toByteArray();


			//Close streams
			baos.close();
			iis.close();


			//return result
			return result;
		} catch (IOException ioe) {
			//Log error message and exception
			logger.error("Decompression Error.", ioe);


			return null;
		}
	}


	/**
	 * Decompress byte array into String
	 * 
	 * @param in The byte array to decompress
	 * @return The decompressed String or null (if decompression failed)
	 */
	public static String decompressToString(byte[] in) {


		//Compress into byte array
		byte[] b = decompress(in);


		//result check
		if (b == null)
			return null;
		else {
			return (new String(b));
		}
	}


	/**
	 * Verify decompression result
	 * 
	 * @param aBytes The byte array before decompression
	 * @param dBytes The byte array after decompression
	 * @return If the result of compression of dBytes is identical with aBytes, return true. Otherwise, return false.
	 */
	public static boolean verify(byte[] aBytes, byte[] dBytes) {
		//Sanity check
		if ((aBytes == null) || (dBytes == null))
			return false;


		//Compress dBytes into byte array cBytes
		byte[] cBytes = ZLibCompressor.compress(dBytes);


		//Check length
		if (aBytes.length != cBytes.length)
			return false;


		//Check each byte individually
		for (int i = 0; i < aBytes.length; i++) {
			if (aBytes[i] != cBytes[i])
				return false;
		}


		//Compression verified
		return true;


	}


}