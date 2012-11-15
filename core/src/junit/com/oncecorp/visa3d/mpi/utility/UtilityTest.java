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

import com.ibm.xml.dsig.util.Base64;

import com.oncecorp.visa3d.mpi.CoreInitializer;
import com.oncecorp.visa3d.mpi.domain.payment.VEReqMessage;
import com.oncecorp.visa3d.mpi.messaging.MessageGenerator;

import java.util.ArrayList;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Description: JUnit test case for Visa 3-D Secure(TM) 
 * Merchant Plug-In Utility Component
 * 
 * @version 0.1 July 07, 2002
 * @author	Alan Zhang
 */
public class UtilityTest extends TestCase {
	public final static String BASE64_STRING 	= "Test string for Base64 encoding.";
	public final static byte[] BASE64_BYTES 		= BASE64_STRING.getBytes();

	public final static String COMP_STRING 		= "Test string for ZLibCompressor testing. ";
	public final static byte[] COMP_BYTES 		= COMP_STRING.getBytes();

	/**
	 * Constructor
	 */
	public UtilityTest(String name) {
		super(name);
	}

	/**
	 * Test String Base64 encoding
	 */
	public void testStringEncoding() {
		Assert.assertEquals(
			BASE64_STRING,
			//Base64Codec.decodeToString(Base64Codec.encodeString(BASE64_STRING)));
			new String(Base64.decode( Base64.encode(BASE64_STRING.getBytes()))));
	}

	/**
	 * Test byte array Base64 encoding
	 */
	public void testByteEncoding() {
		//byte[] toVer = Base64Codec.decode(Base64Codec.encodeBytes(BASE64_BYTES));
		byte[] toVer = Base64.decode(Base64.encode(BASE64_BYTES));
		Assert.assertEquals(BASE64_BYTES.length, toVer.length);

		for (int i = 0; i < BASE64_BYTES.length; i++)
			Assert.assertEquals(BASE64_BYTES[i], toVer[i]);
	}

	/**
	 * Test byte array --> byte array compression
	 */
	public void testByteToByteCompress() {
		byte[] result = ZLibCompressor.compress(COMP_BYTES);
		Assert.assertEquals(true, ZLibCompressor.verify(COMP_BYTES, result));
		System.out.println("byte[] to byte[] compression OK.");
	}

	/**
	 * Test byte array --> byte array decompression
	 */
	public void testByteToByteDecompress() {
		byte[] oriCompBytes = ZLibCompressor.compress(COMP_BYTES);
		byte[] result = ZLibDecompressor.decompress(oriCompBytes);
		Assert.assertEquals(true, ZLibDecompressor.verify(oriCompBytes, result));
		System.out.println("byte[] to byte[] decompression OK.");
	}

	/**
	 * Test Compession --> Encoding --> Decoding --> Decompression
	 */
	public void testWorkFlow() {
		String theString = null;
		try {
			/* 
		 	 * [Martin's Note: Sep 17, 2002 9:44:07 AM]
		     * The switch to the new XPathCanonicalizer, validates the XML message against the DTD
		     * before serializing it. Thus creating an invalid XML document and trying to canonicalize
		     * it will result in a NullPointerException for the XPathCanonicalizer function.
		     * All Messages needs to be well-formed before being toXMLString()...
		     */

			//Get empty PAReq message
			VEReqMessage msg = (VEReqMessage) MessageGenerator.create(VEReqMessage.MSG_TYPE, VEReqMessage.MSG_VERSION);
			
			// Set some attribute within that message
			msg.setId("120");
			msg.setPan("4500000100020003");
			msg.setMerID("3456");
			msg.setAcqBIN("9987");
			msg.setPassword("Password");
			
			Document msgDom = msg.toXML();
			theString = XMLUtil.toXmlString(msgDom);
			System.out.println("VEReq: " + theString);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		byte[] compressed = ZLibCompressor.compress(theString);
		Assert.assertTrue(ZLibCompressor.verify(theString.getBytes(), compressed));

		//String encoded = new String(Base64Codec.encodeBytes(compressed));
		//byte[] decoded = Base64Codec.decode(encoded);
		String encoded = new String(Base64.encode(compressed));
		
		//Add URLEncoding/URLDecoding
		String urlDecoded = java.net.URLDecoder.decode(java.net.URLEncoder.encode(encoded));
		
		byte[] decoded = Base64.decode(urlDecoded);

		String decompressed = ZLibDecompressor.decompressToString(decoded);
		Assert.assertTrue(ZLibDecompressor.verify(decoded, decompressed.getBytes()));

		Assert.assertEquals(theString, decompressed);

	}

	/**
	 * Test credit card validation functions
	 */
	public void testCardValidation() {
		CardValidator cv = new CardValidator();
		
		// Test invalid card number
		ArrayList acceptedTypes = new ArrayList();
		acceptedTypes.add(CardValidator.ACCEPT_ALL);
		
		Assert.assertEquals(false, cv.isValid("4566102320321829", acceptedTypes)); 
		
		// Test valid VISA card number
		acceptedTypes.clear();
		acceptedTypes.add(CardValidator.ACCEPT_VISA);
		
		Assert.assertTrue( cv.isValid("4810105050101020", acceptedTypes) ); 
		
		// Test valid MC card number
		acceptedTypes.clear();
		acceptedTypes.add(CardValidator.ACCEPT_MC);
		
		Assert.assertTrue( cv.isValid("5417109041200010", acceptedTypes) ); 
		
		// Test invalid unknown type
		acceptedTypes.clear();
		acceptedTypes.add("freaky new card");
		
		Assert.assertEquals(false, cv.isValid("6011102320321829", acceptedTypes));
		Assert.assertEquals("We don't accept Discover/Novus cards.",cv.getError()); 
		
		// Test unknown first 4 digits
		acceptedTypes.clear();
		acceptedTypes.add(CardValidator.ACCEPT_ALL);
		
		Assert.assertEquals(false, cv.isValid("7830123847563", acceptedTypes));
		Assert.assertEquals("First four digits, 7830, indicate we don't accept that type of card.",cv.getError()); 
		
		/* 
		 * For the PoC, testing of other types of card than VISA is not necessary. But to truly test this
		 * CardValidator, all types of cards should be tested 
		 */
		
		// Test empty / null parameters
		Assert.assertEquals(false, cv.isValid("4566102320321829", null)); 
		Assert.assertEquals("Invalid accepted credit cards supplied.",cv.getError()); 

		acceptedTypes.clear();
		acceptedTypes.add(CardValidator.ACCEPT_VISA);
		Assert.assertEquals(false, cv.isValid(null, acceptedTypes)); 
		Assert.assertEquals("The credit card number is not formed properly.",cv.getError()); 

		Assert.assertEquals(false, cv.isValid(null, null));
		Assert.assertEquals("The credit card number is not formed properly.",cv.getError()); 

	}



	/**
	 * Test XMLUtil
	 */
	public void testXMLUtil() {
		//Create a DOM object with structure: 
		//	<root>
		//		<person id="111">
		//			<name>Alan Zhang</name>
		//			<desc>Description of myself</desc>
		//		</person>
		//		<person id="222">
		//			<name>Chunyu Zhang</name>
		//			<desc>Description of myself again</desc>
		//		</person>
		//	</root>
		Document doc = XMLUtil.createDocument();
		Element root = doc.createElement("root");
		Element person = doc.createElement("person");
		person.setAttribute("id", "111");
		Element name = doc.createElement("name");
		Element desc = doc.createElement("desc");
		name.appendChild(doc.createTextNode("Alan Zhang"));
		desc.appendChild(doc.createTextNode("Description of myself"));
		person.appendChild(name);
		person.appendChild(desc);
		root.appendChild(person);

		person = doc.createElement("person");
		person.setAttribute("id", "222");
		name = doc.createElement("name");
		desc = doc.createElement("desc");
		name.appendChild(doc.createTextNode("Chunyu Zhang"));
		desc.appendChild(doc.createTextNode("Description of myself again"));
		person.appendChild(name);
		person.appendChild(desc);
		root.appendChild(person);
		doc.appendChild(root);

		//System.out.println();
		//System.out.println( XMLUtil.toXmlString( doc ) );

		//Test getValueByXPath(). XPath: /root/person/name/text()
		String myName = XMLUtil.getValueByXPath(root, "/root/person[@id=\"111\"]/name/text()");
		Assert.assertEquals("Alan Zhang", myName);

		//Test getValueByXPath(). XPath: /root/person/name/text()
		myName = XMLUtil.getValueByXPath(root, "/root/person[@id=\"222\"]/name/text()");
		Assert.assertEquals("Chunyu Zhang", myName);

		//Test getFirstNodeByXPath(). XPath: /root/person/desc
		Node node = XMLUtil.getFirstNodeByXPath(root, "/root/person/desc");
		Assert.assertEquals(Node.ELEMENT_NODE, node.getNodeType());

		//Test isTextNode()
		Node text = node.getFirstChild();
		Assert.assertEquals(true, XMLUtil.isTextNode(text));
		Assert.assertEquals("Description of myself", text.getNodeValue());

		//Test getNodeListByXPath. XPath: /root/person
		NodeList nl = XMLUtil.getNodeListByXPath(root, "/root/person");
		Assert.assertEquals(2, nl.getLength());

		//Test createDocument(String xmlStr)
		String xmlStr = XMLUtil.toXmlString(doc);
		Document newDoc = null;
		try {
			newDoc = XMLUtil.createDocument(xmlStr, false);
			Assert.assertEquals(xmlStr, XMLUtil.toXmlString(newDoc));
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		//Test DTD validation
		try {
			xmlStr =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<!DOCTYPE MPI_Interface SYSTEM \"http://demo.oncecorp.com/mpi/MPIInterfaceWithProfile.dtd\">"
					+ "<MPI_Interface>"
					+ "<MPIError id=\"UNKNOWN\">"
					+ "<version>1.0</version>"
					+ "<errorCode>"
					+ "500"
					+ "</errorCode>"
					+ "<errorMessage>"
					+ "Sample Error Message"
					+ "</errorMessage>"
					+ "</MPIError>"
					+ "</MPI_Interface>";

			newDoc = XMLUtil.createDocument(xmlStr, true);
			Assert.assertTrue(newDoc != null);
			System.out.println(XMLUtil.toXmlString(newDoc));
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		//Test DTD validation (with invalid XML string)
		try {
				xmlStr =
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<!DOCTYPE MPI_Interface SYSTEM \"http://demo.oncecorp.com/mpi/MPIInterfaceWithProfile.dtd\">"
						+ "<MPI_Interface>"
						+ "<MPIError>" //without id attribute
	+"<version>1.0</version>"
		+ "<errorCode>"
		+ "500"
		+ "</errorCode>"
		+ "<errorMessage>"
		+ "Error message goes here."
		+ "</errorMessage>"
		+ "</MPIError>"
		+ "</MPI_Interface>";

			newDoc = XMLUtil.createDocument(xmlStr, true);

			//if no exception thrown
			fail();

		}
		catch (Exception e) {
			Assert.assertTrue(e instanceof org.xml.sax.SAXException);
			System.out.println("SAXException info: " + e.getMessage());
		}

		//Test Schema validation
		//Deprecated
		/**
		try {
			xmlStr = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<MPI_Interface xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:noNamespaceSchemaLocation=\"http://demo.oncecorp.com/MPI/MPIInterface.xsd\">"
				+ "<PaymentVerifReq ID=\"1234\">"
				+ "<merchantTermURL/>"
		 		+ "<merchantData/>"
				+ "<pan/>"
				+ "<panExpiry/>"
				+ "<purchaseDate/>"
				+ "<purchasePurchAmount/>"
				+ "<purchaseCurrency/>"
				+ "</PaymentVerifReq>"
				+ "</MPI_Interface>";
		
			newDoc = XMLUtil.createDocumentWithSchemaValidation(xmlStr, true);
			Assert.assertTrue(newDoc != null);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		//Test Schema validation (with invalid XML string)
		try {
			xmlStr = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<MPI_Interface xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:noNamespaceSchemaLocation=\"http://demo.oncecorp.com/MPI/MPIInterface.xsd\">"
				+ "<PaymentVerifReq ID=\"1234\">"
				+ "<merchantTermURL/>"
		 		+ "<merchantData/>"
				+ "<pan>" //pan tag not terminated
				+ "<panExpiry/>"
				+ "<purchaseDate/>"
				+ "<purchasePurchAmount/>"
				+ "<purchaseCurrency/>"
				+ "</PaymentVerifReq>"
				+ "</MPI_Interface>";
		
			newDoc = XMLUtil.createDocumentWithSchemaValidation(xmlStr, true);
		
			//If no exception thrown
			fail();
		} catch (Exception e) {
			Assert.assertTrue(e instanceof org.xml.sax.SAXParseException);
		}
		*/
	}

	/**
	 * Test credit card validation functions
	 */
	public void testURLValidator() {
		final String URL_1 = "http://mpi.oncecorp.com:9080/MPIWEB/makeit.htm";
		final String URL_2 = "https://mpi.oncecorp.com:9443/test";
		final String URL_3 = "https:/";
		final String URL_4 = "whatis this thing";
		final String URL_5 = "";
		final String URL_6 = null;
		
		// Those are valid URL
		assertTrue(URLValidator.isValid(URL_1));
		assertTrue(URLValidator.isValid(URL_2));
		assertTrue(URLValidator.isValid(URL_3));
		
		// Starting from here, all the URL are invalid
		assertTrue(! URLValidator.isValid(URL_4));
		assertTrue(! URLValidator.isValid(URL_5));
		assertTrue(! URLValidator.isValid(URL_6));
	}
	
	/**
	 * Test the conversion routine from Base64 to 
	 * AsciiHex
	 */
	public void testBase64toAsciiHex() {
		String EXPECTED_RESULT = "54686520717569636B2062726F776E20666F78206A756D706564206F76657220746865206C617A7920646F672E20313233343536373839302021402324255E262A28295F2B7C5C3D2D7E60";
		
		// Create a string that will be encoded
		String baseString = "The quick brown fox jumped over the lazy dog. 1234567890 !@#$%^&*()_+|\\=-~`";
		
		String result = Base64Codec.encodeString(baseString);	
		String asciiRes = Base64Conversion.convert(result, Base64Conversion.TO_ASCIIHEX);
		
		if (! asciiRes.equalsIgnoreCase(EXPECTED_RESULT)) {
			fail("Expected result of asciiHex conversion did not work properly");
		}
	}
	
	/**
	 * Test the conversion routines from Base64 to 
	 * Binary
	 */
	public void testBase64toBinary() {
	}
	
	
	/**
	 * Test suite for the utility package
	 * @return 	TestSuite to be executed
	 */
	public static Test suite() {
		boolean initOK = CoreInitializer.init();
		if (!initOK) {
			System.out.println("Init Core Server failed.");
			fail();
		}

		TestSuite suite = new TestSuite("Utility suite");
		suite.addTest(new UtilityTest("testStringEncoding"));
		suite.addTest(new UtilityTest("testByteEncoding"));
		suite.addTest(new UtilityTest("testByteToByteCompress"));
		suite.addTest(new UtilityTest("testByteToByteDecompress"));
		suite.addTest(new UtilityTest("testWorkFlow"));
		suite.addTest(new UtilityTest("testBase64toAsciiHex"));
		suite.addTest(new UtilityTest("testBase64toBinary"));
		suite.addTest(new UtilityTest("testCardValidation"));
		suite.addTest(new UtilityTest("testURLValidator"));

		// suite.addTest(new UtilityTest("testXMLUtil"));

		return suite;
	}
}