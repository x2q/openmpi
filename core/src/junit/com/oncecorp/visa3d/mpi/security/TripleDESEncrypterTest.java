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
import com.oncecorp.visa3d.mpi.utility.JUnitHelper;

import junit.framework.TestCase;

/**
 * Test case for TripleDESEncrityper
 *
 * @author azhang
 * @version 1.o Nov 29, 2002
 */
public class TripleDESEncrypterTest extends TestCase {

	/**
	 * Constructor for TripleDESEncrypterTest.
	 * @param arg0
	 */
	public TripleDESEncrypterTest(String arg0) {
		super(arg0);

		JUnitHelper.initFromFile();

		boolean initOK = CoreInitializer.init();
		if (!initOK) {
			System.out.println("Init Core Server failed.");
			fail();
		}

	}

	public void testEncrypter() {
		String text =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<ThreeDSecure>"
				+ "<Message id=\"999\">"
				+ "<PAReq>"
				+ "<version>1.0.1</version>"
				+ "<Merchant>"
				+ "<acqBIN>1271722</acqBIN>"
				+ "<merID>merID</merID>"
				+ "<name>Merchant Name</name>"
				+ "<country>CAN</country>"
				+ "<url>http://merchant.com</url>"
				+ "</Merchant>"
				+ "<Purchase>"
				+ "<xid>This is a 28 byte long value</xid>"
				+ "<date>20020806 10:10:10</date>"
				+ "<amount>123.45</amount>"
				+ "<purchAmount>12345</purchAmount>"
				+ "<currency>124</currency>"
				+ "<exponent>2</exponent>"
				+ "<desc>This is description</desc>"
				+ "<Recur>"
				+ "<frequency>25</frequency>"
				+ "<endRecur>1111</endRecur>"
				+ "</Recur>"
				+ "<install>200</install>"
				+ "</Purchase>"
				+ "<CH>"
				+ "<acctID>acctID1234</acctID>"
				+ "<expiry>0804</expiry>"
				+ "</CH>"
				+ "<Extension id=\"http://www.oncecorp.com/visa3d/ProfileSpec.html\" critical=\"false\">ProfileSupport</Extension>"
				+ "</PAReq>"
				+ "</Message>"
				+ "</ThreeDSecure>";

		// Get encrypter instance
		TripleDESEncrypter encrypter = TripleDESEncrypter.getInstance();
		if (encrypter == null) {
			System.out.println("Failed to get Encrypter instance.");
			fail();
		}

		// Encrypt sample message text
		String[] result = null;
		try {
			result = encrypter.encrypt(text);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		// Check encryption result
		if ((result == null) || (result.length != 2)) {
			System.out.println(
				"encryption result is null or with incorrect length.");
			fail();
		}

		System.out.println("encryption done.");
		System.out.println("encrypted text: " + result[0]);
		System.out.println("IV: " + result[1]);

		// Decryption
		String afterDecryption = null;
		try {
			afterDecryption = encrypter.decrypt(result[0], result[1]);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		// Check decryption result
		if (afterDecryption == null) {
			System.out.println("decryption result is null.");
			fail();
		}

		System.out.println("decryption done.");
		System.out.println("decrypted text: " + afterDecryption);
		if (!afterDecryption.equals(text))
			fail();
	}

}
