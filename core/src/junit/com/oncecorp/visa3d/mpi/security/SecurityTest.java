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

import com.oncecorp.visa3d.mpi.CoreInitializer;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;
import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.utility.JUnitHelper;

import java.util.ResourceBundle;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;

/**
 * Description: JUnit test case for Visa 3-D Secure(TM)
 * Merchant Plug-In Security Component
 *
 * @version 0.1 July 29, 2002
 * @author	Alan Zhang
 */
public class SecurityTest extends TestCase {

	/**
	 * Constructor
	 */
	public SecurityTest(String name) {
		super(name);
	}

	/**
	 * Test XMLSignature
	 */
	public void testXMLSignature() {
		try {
			//Get properties needed for XML Signature
			/**
			 * [Gang's Note: 29 May, 2003] Use Triple3DES keystore
			 */
			/**
			ResourceBundle rb =
				ResourceBundle.getBundle("com.oncecorp.visa3d.mpi.security.SecurityTest");
			String keystorepath = rb.getString("keystorepath");
			String alias = rb.getString("alias");
			String storepass = rb.getString("storepass");
			String keypass = rb.getString("keypass");
			String methodID = rb.getString("methodID");
			*/
			String keystorepath = (String) Config.getConfigReference().
					   getConfigData( MPIConfigDefinition.TRIPLE_DES_KEYSTORE_LOCATION );
			String alias = (String) Config.getConfigReference().
					   getConfigData( MPIConfigDefinition.TRIPLE_DES_RAWKEY_ALIAS );
			String storepass = (String) Config.getConfigReference().
					   getConfigData( MPIConfigDefinition.TRIPLE_DES_KEYSTORE_PWD );
			String keypass = (String) Config.getConfigReference().
					   getConfigData( MPIConfigDefinition.TRIPLE_DES_RAWKEY_PWD );
			String methodID = "DSA";

			//Constructs a PARes message
			String xmlStr =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<ThreeDSecure xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
					+ "xsi:noNamespaceSchemaLocation=\"http://demo.oncecorp.com/MPI/ThreeDSecure.xsd\">"
					+ "<Message id=\"Msg123\">"
					+ "<PARes id=\"PARes0\">"
					+ "<version>1.0.1</version>"
					+ "<Merchant>"
					+ "<acqBIN>BIN123</acqBIN>"
					+ "<merID>MERID123</merID>"
					+ "</Merchant>"
					+ "<Purchase>"
					+ "<xid>XID123</xid>"
					+ "<date>20020729 12:12:12</date>"
					+ "<purchAmount>12345</purchAmount>"
					+ "<currency>CA</currency>"
					+ "<exponent>2</exponent>"
					+ "</Purchase>"
					+ "<pan>12345</pan>"
					+ "<TX>"
					+ "<time>20020729 13:13:13</time>"
					+ "<status>Y</status>"
					+ "</TX>"
					+ "</PARes>"
					+ "</Message>"
					+ "</ThreeDSecure>";

			/*
			String xmlStr =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<!DOCTYPE ThreeDSecure SYSTEM \"http://demo.oncecorp.com/mpi/ThreeDSecureWithProfile.dtd\">"
					+ "<ThreeDSecure>"
					+ "<Message id=\"577808640633\">"
					+ "<IPRes id=\"IPRes\">"
					+ "<version>1.0.1</version>"
					+ "<Merchant><acqBIN>123456790</acqBIN><merID>1234-5678</merID></Merchant>"
					+ "<pan>4563218880030</pan>"
					+ "<Profile>"
					+ "<profileName>OttoProfile</profileName>"
					+ "<profileData>"
					+ "<profileScope scopeid=\"ScopeB\">"
					+ "<profileDataItem uid=\"106\">b_elem106</profileDataItem>"
					+ "<profileDataItem uid=\"100\">b_elem100</profileDataItem>"
					+ "</profileScope>"
					+ "</profileData>"
					+ "</Profile>"
					+ "<TX><time/><status>Y</status></TX></IPRes></Message></ThreeDSecure>";
			*/

			//Create PARes message document
			Document doc = XMLUtil.createDocument(xmlStr, false);

			//Sign PARes element
			XMLSignatureResult xsr =
				SecurityManager.getInstance().sign(
					keystorepath,
					alias,
					storepass.toCharArray(),
					keypass.toCharArray(),
					methodID,
					doc,
					"/ThreeDSecure/Message/PARes");
			Assert.assertTrue(xsr.getSignedDoc() != null);

			//Simulates DOM--String--DOM processing
			Document verDoc =
				XMLUtil.createDocument(XMLUtil.toXmlString(xsr.getSignedDoc()), false);

			//Verify signature
			xsr = XMLSignature.verify(verDoc, keystorepath, storepass.toCharArray());
			Assert.assertTrue(xsr.getVerified());

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	/**
	 * Test MPIEncrypter
	 */
    public void testMPIEncrypter(){
	   try {

	            // Tested String
		        String originalStr = "KeepInSecret";

	            // Encrypt
	            String encrypted = MPIEncrypter.encrypt(originalStr);

	            // Decrypt
	            String decrypted = MPIEncrypter.decrypt(encrypted);

		        // verify encryption result
		        Assert.assertTrue(encrypted != null);
		        Assert.assertTrue(decrypted.equals(originalStr));

	       }catch (Exception e) {
				e.printStackTrace();
				fail();
		   }
	}

	public static Test suite() {

		JUnitHelper.initFromFile();

		boolean initOK = CoreInitializer.init();
		if (!initOK) {
			System.out.println("Init Core Server failed.");
			fail();
		}

		TestSuite suite = new TestSuite("Security suite");
		suite.addTest(new SecurityTest("testXMLSignature"));
        suite.addTest(new SecurityTest("testMPIEncrypter"));

		return suite;
	}

}