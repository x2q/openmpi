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

package com.oncecorp.visa3d.mpi.security;

import com.oncecorp.visa3d.mpi.utility.XMLUtil;
import com.oncecorp.visa3d.mpi.utility.ZLibCompressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;

import com.ibm.xml.dsig.util.Base64;

public class EncCompUtil {

	public static void main(String[] args) {
		System.out.println("Usage: java SignUtil [input.xml] [output.enc]");
		System.out.println("Default: SignedIPResInput.xml EncodedIPRes.enc");
		try {
			String filename = "SignedIPRes.xml";
			if ((args != null) && (args.length >= 1) && (args[0] != null))
				filename = args[0];

			byte[] xmlBytes = getBytesFromFile(new File(filename));

			//Constructs a PARes message
			Document doc = XMLUtil.createDocument(new String(xmlBytes), false);
			
			if (doc != null) {
				System.out.println("Loaded IPRes from " + filename);
			} else {
				System.out.println("IPRes loading failed. Check file name.");
				System.exit(0);
			}
			
			String theString = XMLUtil.toXmlString(doc);

			byte[] compressed = ZLibCompressor.compress(theString);
			//byte[] encoded = Base64Codec.encodeBytes(compressed).getBytes();
			byte[] encoded = Base64.encode(compressed).getBytes();
			System.out.println("Finish encoding/compression.");
			
			String outputFilename = "EncodedIPRes.enc";
			if ((args != null) && (args.length >= 2) && (args[1] != null))
				outputFilename = args[1];
			FileOutputStream fos = new FileOutputStream(new File(outputFilename));
			fos.write(encoded);
			fos.flush();
			fos.close();
			System.out.println("Encoded/Compressed IPRes message in EncodedIPRes.enc or the file you specified.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}