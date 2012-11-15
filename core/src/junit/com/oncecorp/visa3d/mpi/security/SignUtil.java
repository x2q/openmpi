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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ResourceBundle;

import org.w3c.dom.Document;

public class SignUtil {    
	public static void main(String[] args) {

		System.out.println("Usage: java SignUtil [input.xml] [output.xml]");
		System.out.println("Default: IPResInput.xml SignedIPRes.xml");
		try {
			//Get properties needed for XML Signature
			ResourceBundle rb =
				ResourceBundle.getBundle("com.oncecorp.visa3d.mpi.security.SecurityTest");
			String keystorepath = rb.getString("keystorepath");
			String alias = rb.getString("alias");
			String storepass = rb.getString("storepass");
			String keypass = rb.getString("keypass");
			String methodID = rb.getString("methodID");

			String filename = "IPResInput.xml";
			if ((args != null) && (args.length >= 1) && (args[0] != null))
				filename = args[0];

			byte[] xmlBytes = EncCompUtil.getBytesFromFile(new File(filename));

			//Constructs a PARes message
			Document doc = XMLUtil.createDocument(new String(xmlBytes), false);
			
			if (doc != null) {
				System.out.println("Loaded IPRes from " + filename);
			} else {
				System.out.println("IPRes loading failed. Check file name.");
				System.exit(0);
			}
			

			//Sign PARes element
			System.out.println("Signing IPRes message with key alias: "+alias);
			XMLSignatureResult xsr =
				SecurityManager.getInstance().sign(
					keystorepath,
					alias,
					storepass.toCharArray(),
					keypass.toCharArray(),
					methodID,
					doc,
					"/ThreeDSecure/Message/IPRes");
			System.out.println("Finish signing.");

			//Write it back to XML file
			// Prepare the output file
			String outputFilename = "SignedIPRes.xml";
			String verDocStr = XMLUtil.toXmlString(xsr.getSignedDoc());
			if ((args != null) && (args.length >= 2) && (args[1] != null))
				outputFilename = args[1];
				
			FileOutputStream fos = new FileOutputStream(new File(outputFilename));
			fos.write(verDocStr.getBytes());
			fos.flush();
			fos.close();
			
			System.out.println(
				"Singed IPRes message is in SignedIPRes.xml or the file you specified.");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}