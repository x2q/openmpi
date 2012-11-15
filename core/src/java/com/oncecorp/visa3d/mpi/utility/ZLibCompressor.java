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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.log4j.Logger;


/**
 * Description: This class is based on java.util.zip.Defalter. It provides support 
 * for general-purpose compression using popular ZLIB library. The result of 
 * compression is fully compliant with ZLIB data format as defined in RFC 1950. 
 * The "DEFAULT_COMPRESSION" level is used as specified in 3-D Secure(TM) 
 * Protocol Specification.
 * 
 * @version 0.1 July 04, 2002
 * @author	Alan Zhang
 */
public class ZLibCompressor {


	/** Encoding for string conversion */
	//private final static String DEFAULT_STRING_ENCODING = "ISO-8859-1";


	/**
	 * Local Log4J logger
	 */
	private static Logger logger =
		MPILogger.getLogger(ZLibCompressor.class.getName());


	/**
	 * Compress byte array. 
	 * 
	 * @param in The byte array to compress
	 * @return Compressed byte array or null (if compression failed)
	 */
	public static byte[] compress(byte[] in) {
		//Sanity check
		if (in == null)
			return null;


		try {
			//Underlying ByteArrayOutputStream to output compressed byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();


			//Deflater with DEFAULT_COMPRESSION level and DEFLATED method (the only one supported)
			Deflater def = new Deflater(Deflater.DEFAULT_COMPRESSION);


			//Filter output stream to compress data
			DeflaterOutputStream dos = new DeflaterOutputStream(baos, def);


			//Write data to output stream to be compressed
			dos.write(in, 0, in.length);


			//Finishes writing compressed data to the output stream without closing the underlying stream
			dos.finish();


			//Convert to byte array
			byte[] b = baos.toByteArray();


			//Close output stream
			dos.close();


			//return compressed byte array
			return b;
		} catch (IOException ioe) {
			//Log error message and exception
			logger.error("Compression Error.", ioe);


			return null;
		}
	}


	/**
	 * Compress String object
	 * 
	 * @param in The String to compress
	 * @return Compressed byte array or null (if compression failed)
	 */
	public static byte[] compress(String in) {


		//Sanity check
		if (in == null)
			return null;


		return (compress(in.getBytes()));


		/**
		try {
			return (compress(in.getBytes(DEFAULT_STRING_ENCODING)));
		} catch (UnsupportedEncodingException uee) {
			//Log error message and exception
			logger.error("String to Byte Array conversion Error.", uee);


			return null;
		}
		*/
	}


	/**
	 * Verify compression result
	 * 
	 * @param aBytes The byte array before compression
	 * @param cBytes The byte array after compression
	 * @return If the result of decompression of cBytes is identical with aBytes, return true. Otherwise, return false.
	 */
	public static boolean verify(byte[] aBytes, byte[] cBytes) {
		//Sanity check
		if ((aBytes == null) || (cBytes == null))
			return false;


		//Decompress cBytes into byte array dBytes
		byte[] dBytes = ZLibDecompressor.decompress(cBytes);


		//Check length
		if (aBytes.length != dBytes.length)
			return false;


		//Check each character
		for (int i = 0; i < aBytes.length; i++) {
			if (aBytes[i] != dBytes[i])
				return false;
		}


		//Compression verified
		return true;


	}


}