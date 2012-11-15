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

/**
 * Description: JUnit test case for Visa 3-D Secure(TM)
 * Merchant Plug-In Messaging Component
 *
 * Tests: PAReqMessage
 * 		  PAResMessage
 *        PaymentAuthReqMessage
 *        PaymentAuthResMessage
 * 		  DomToMsgConverter
 * 		  PAResToPaymentAuthResTransformer
 *
 * @version 0.1 Aug 06, 2002
 * @author	Alan Zhang
 */

package com.oncecorp.visa3d.mpi.messaging;

import com.ibm.xml.dsig.util.Base64;
import com.oncecorp.visa3d.mpi.CoreInitializer;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorMessage;
import com.oncecorp.visa3d.mpi.domain.payment.PAReqMessage;
import com.oncecorp.visa3d.mpi.domain.payment.PAResMessage;
import com.oncecorp.visa3d.mpi.domain.payment.PAResToPaymentAuthResTransformer;
import com.oncecorp.visa3d.mpi.domain.payment.VEResMessage;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentAuthReqMessage;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentAuthResMessage;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentVerifReqMessage;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageMetaInfo;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageUID;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;
import com.oncecorp.visa3d.mpi.utility.ZLibCompressor;
import com.oncecorp.visa3d.mpi.utility.JUnitHelper;

import java.util.Iterator;
import java.util.Vector;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;

public class MessagingTest extends TestCase {
	protected static String pvreqXmlStr =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<MPI_Interface>"
			+ "<PaymentVerifReq id=\"001\">"
			+ "<version>1.0</version>"
			+ "<merchantID>merchant-001</merchantID>"
			+ "<merchantTermURL>http://merchant.com/obcapp</merchantTermURL>"
			+ "<merchantData>merchantData</merchantData>"
			+ "<pan>123456789012345</pan>"
			+ "<panExpiry>0912</panExpiry>"
			+ "<purchaseDate>20020101 12:12:12</purchaseDate>"
			+ "<purchaseAmount>123.45</purchaseAmount>"
			+ "<purchasePurchAmount>12345</purchasePurchAmount>"
			+ "<purchaseCurrency>124</purchaseCurrency>"
			+ "<purchaseDesc>This is purchaseDesc</purchaseDesc>"
			+ "<purchaseInfo>This is purchaseInfo</purchaseInfo>"
			+ "<browserCategory>0</browserCategory>"
			+ "<browserAccept>aaa</browserAccept>"
			+ "<browserAgent>bbb</browserAgent>"
			+ "<PurchaseRecur>"
			+ "<frequency>25</frequency>"
			+ "<endRecur>20080718</endRecur>"
			+ "</PurchaseRecur>"
			+ "<purchaseInstall>200</purchaseInstall>"
			+ "</PaymentVerifReq> "
			+ "</MPI_Interface>";

	protected static String veresXmlStr =
		"<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
			+ "<ThreeDSecure>"
			+ "<Message id=\"999\">"
			+ "<VERes>"
			+ "<version>1.0.1</version>"
			+ "<CH>"
			+ "<enrolled>N</enrolled>"
			+ "<acctID>12345678</acctID>"
			+ "</CH>"
			+ "<url>http://visa.acs.com</url>"
			+ "<protocol>ThreeDSecure</protocol>"
			+ "<IReq> "
			+ "<iReqCode>50</iReqCode>"
			+ "<iReqDetail>iReqDetail here</iReqDetail>"
			+ "<vendorCode>char less than 256</vendorCode>"
			+ "</IReq>"
			+ "</VERes>"
			+ "</Message>"
			+ "</ThreeDSecure>";

	protected static String pareqXmlStr =
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

	protected static String paresXmlStr =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<ThreeDSecure>"
			+ "<Message id = \"999\">"
			+ "<PARes id=\"#PA2157205452125\">"
			+ "<version>1.0.1</version>"
			+ "<Merchant>"
			+ "<acqBIN>199876</acqBIN>"
			+ "<merID>merID</merID>"
			+ "</Merchant>"
			+ "<Purchase>"
			+ "<xid>This is a 28 byte long value</xid>"
			+ "<date>20020806 10:10:10</date>"
			+ "<purchAmount>99999</purchAmount>"
			+ "<currency>124</currency>"
			+ "<exponent>2</exponent>"
			+ "</Purchase>"
			+ "<pan>0000000000000000</pan>"
			+ "<TX>"
			+ "<time>20020806 11:11:11</time>"
			+ "<status>N</status>"
			+ "</TX>"
			+ "</PARes>"
			+ "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"
			+ "<SignedInfo>"
			+ "<CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>"
			+ "<SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/>"
			+ "<Reference URI=\"#PA2157205452125\">"
			+ "<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>"
			+ "<DigestValue>/EBXwBFRS5idR7aYOgvMsQHL6sY=</DigestValue>"
			+ "</Reference>"
			+ "</SignedInfo>"
			+ "<SignatureValue>b/Khejqq1CZViUXaC/G7emdO2CxJkwKSwCkM2pns0TVO6PFdQxWdmilWoTvjIbotrSRPXeWBZ7P1xIwFDoIJZauHiZLZSfvzK5wUcQZ98E6kXNrzTqnLRYRX7zkLH5UUXJJ/bfBWoNUzZ5obxr2OpA6Gs+vXIA0RKacbS4ARZnM=</SignatureValue>"
			+ "<KeyInfo>"
			+ "<X509Data xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"
			+ "<X509Certificate>MIICsDCCAhmgAwIBAgIIRnP8mDwESl0wDQYJKoZIhvcNAQEFBQAwYDELMAkGA1UEBhMCVVMxEDAOBgNVBAoTB0NhcmFkYXMxLDAqBgNVBAsTIzMtRCBTZWN1cmUgQ29tcGxpYW5jZSBUZXN0IEZhY2lsaXR5MREwDwYDVQQDEwhDVEhfUk9PVDAeFw0wMjA0MjIyMDU0MDRaFw0wMzA0MjIyMDU0MDRaMIHCMQswCQYDVQQGEwJDWTEQMA4GA1UECBMHTmljb3NpYTEQMA4GA1UEBxMHTmljb3NpYTEjMCEGA1UEChMaQ2FyZCBUZWNoIFNlcnZpY2VzIExpbWl0ZWQxITAfBgNVBAsTGFdlYiBTb2x1dGlvbnMgRGVwYXJ0bWVudDEpMCcGCSqGSIb3DQEJARYadGhlb2Rvcm9zLnNhdnZpZGVzQGN0bC5jb20xHDAaBgNVBAMTE0NUTCBTRU5UUlkgMS4wIFNpZ24wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAOsHniQ0r0wyCLk1lm6Egh/TAhp0hU+8oxMc1IqG4t9+p0ZFK1HgH94wm2yLcXolgXgJxhGkX2iBczRmbP9uPi7pRObjcaw8BfGxZrrQb3f1sOf1yP7B9HFpIavstMaERU/KJKiDliIagyKv2Itm4NwMoyP1G6LtAQ9kewaSi5bNAgMBAAGjEDAOMAwGA1UdEwEB/wQCMAAwDQYJKoZIhvcNAQEFBQADgYEAZdYMZ+p02r6oDnnZKIajtI5zxYfIcIiTZsKSMYd4QZe9TzEC02z9uca4MpJgl50i5DPFky+YSXD9OOSVDWBW2fp4fF657x8pfx+TktYrMU2pQZp1tDp+gNIZ5K5bxrt0wXGDLkUaTg+XwEtLRCI8ltQqRy0Iy8msPIdSW408oEQ=</X509Certificate>"
			+ "</X509Data>"
			+ "</KeyInfo>"
			+ "</Signature>"
			+ "</Message>"
			+ "</ThreeDSecure>";

	protected static String signatureStr =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"
			+ "<SignedInfo>"
			+ "<CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>"
			+ "<SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/>"
			+ "<Reference URI=\"#PA2157205452125\">"
			+ "<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>"
			+ "<DigestValue>/EBXwBFRS5idR7aYOgvMsQHL6sY=</DigestValue>"
			+ "</Reference>"
			+ "</SignedInfo>"
			+ "<SignatureValue>b/Khejqq1CZViUXaC/G7emdO2CxJkwKSwCkM2pns0TVO6PFdQxWdmilWoTvjIbotrSRPXeWBZ7P1xIwFDoIJZauHiZLZSfvzK5wUcQZ98E6kXNrzTqnLRYRX7zkLH5UUXJJ/bfBWoNUzZ5obxr2OpA6Gs+vXIA0RKacbS4ARZnM=</SignatureValue>"
			+ "<KeyInfo>"
			+ "<X509Data xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"
			+ "<X509Certificate>MIICsDCCAhmgAwIBAgIIRnP8mDwESl0wDQYJKoZIhvcNAQEFBQAwYDELMAkGA1UEBhMCVVMxEDAOBgNVBAoTB0NhcmFkYXMxLDAqBgNVBAsTIzMtRCBTZWN1cmUgQ29tcGxpYW5jZSBUZXN0IEZhY2lsaXR5MREwDwYDVQQDEwhDVEhfUk9PVDAeFw0wMjA0MjIyMDU0MDRaFw0wMzA0MjIyMDU0MDRaMIHCMQswCQYDVQQGEwJDWTEQMA4GA1UECBMHTmljb3NpYTEQMA4GA1UEBxMHTmljb3NpYTEjMCEGA1UEChMaQ2FyZCBUZWNoIFNlcnZpY2VzIExpbWl0ZWQxITAfBgNVBAsTGFdlYiBTb2x1dGlvbnMgRGVwYXJ0bWVudDEpMCcGCSqGSIb3DQEJARYadGhlb2Rvcm9zLnNhdnZpZGVzQGN0bC5jb20xHDAaBgNVBAMTE0NUTCBTRU5UUlkgMS4wIFNpZ24wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAOsHniQ0r0wyCLk1lm6Egh/TAhp0hU+8oxMc1IqG4t9+p0ZFK1HgH94wm2yLcXolgXgJxhGkX2iBczRmbP9uPi7pRObjcaw8BfGxZrrQb3f1sOf1yP7B9HFpIavstMaERU/KJKiDliIagyKv2Itm4NwMoyP1G6LtAQ9kewaSi5bNAgMBAAGjEDAOMAwGA1UdEwEB/wQCMAAwDQYJKoZIhvcNAQEFBQADgYEAZdYMZ+p02r6oDnnZKIajtI5zxYfIcIiTZsKSMYd4QZe9TzEC02z9uca4MpJgl50i5DPFky+YSXD9OOSVDWBW2fp4fF657x8pfx+TktYrMU2pQZp1tDp+gNIZ5K5bxrt0wXGDLkUaTg+XwEtLRCI8ltQqRy0Iy8msPIdSW408oEQ=</X509Certificate>"
			+ "</X509Data>"
			+ "</KeyInfo>"
			+ "</Signature>";

	protected static String authReqXmlStr1 =
		"<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
			+ "<MPI_Interface>"
			+ "<PaymentAuthReq id=\"001\">"
			+ "<version>1.0</version>"
			+ "<merchantID>merchant-001</merchantID>"
			+ "<paymentAuthMsg>";

	protected static String authReqXmlStr2 =
		"</paymentAuthMsg>" + "</PaymentAuthReq>" + "</MPI_Interface>";

	protected static String authResXmlStr =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<MPI_Interface>"
			+ "<PaymentAuthRes id=\"001\">"
			+ "<version>1.0</version>"
			+ "<acqBIN>199876</acqBIN>"
			+ "<merchantID>merID1234</merchantID>"
			+ "<transactionID>TranID1234</transactionID>"
			+ "<date>20020807 12:12:12</date>"
			+ "<time>20020807 11:11:11</time>"
			+ "<purchAmount>12345</purchAmount>"
			+ "<currency>124</currency>"
			+ "<exponent>2</exponent>"
			+ "<authPAN>123456789012</authPAN>"
			+ "<status>N</status>"
			+ "</PaymentAuthRes>"
			+ "</MPI_Interface>";

	protected static String errorXmlStr =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<ThreeDSecure>"
			+ "<Message id=\"999\">"
			+ "<Error>"
			+ "<version>1.0.1</version>"
			+ "<errorCode>1</errorCode>"
			+ "<errorMessage>Error Message</errorMessage>"
			+ "<errorDetail>Error Detail</errorDetail>"
			+ "</Error>"
			+ "</Message>"
			+ "</ThreeDSecure>";

	public MessagingTest(String name) {
		super(name);
	}

	/**
	 * Test DomToMsgConverter
	 * Methods tested: convert()
	 *
	 * Currently there are five Conversion Bindings avaiable.
	 * We would test them one by one.
	 */
	public void testConverter() {
		try {
			//----------- Test PaymentVerifReq conversion ---------------
			//Create PaymentVerifReq DOM document
			Document doc = XMLUtil.createDocument(pvreqXmlStr, false);

			//Convert document to messsage
			Message msg = (new DomToMsgConverter()).convert(doc);

			//Check result
			Assert.assertTrue("true", (msg instanceof PaymentVerifReqMessage));

			//Check content
			PaymentVerifReqMessage pvreqMsg = (PaymentVerifReqMessage) msg;

			Assert.assertEquals("001", pvreqMsg.getId());
			Assert.assertEquals("1.0", pvreqMsg.getVersion());
			Assert.assertEquals("http://merchant.com/obcapp", pvreqMsg.getMerchantTermURL());
			Assert.assertEquals("merchantData", pvreqMsg.getMerchantData());
			Assert.assertEquals("123456789012345", pvreqMsg.getPan());
			Assert.assertEquals("0912", pvreqMsg.getPanExpiry());
			Assert.assertEquals("20020101 12:12:12", pvreqMsg.getPurchaseDate());
			Assert.assertEquals("123.45", pvreqMsg.getPurchaseAmount());
			Assert.assertEquals("12345", pvreqMsg.getPurchasePurchAmount());
			Assert.assertEquals("124", pvreqMsg.getPurchaseCurrency());
			Assert.assertEquals("This is purchaseDesc", pvreqMsg.getPurchaseDesc());
			Assert.assertEquals("0", pvreqMsg.getBrowserCategory());
			Assert.assertEquals("aaa", pvreqMsg.getBrowserAccept());
			Assert.assertEquals("bbb", pvreqMsg.getBrowserAgent());
			Assert.assertEquals("25", pvreqMsg.getPurchaseFrequency());
			Assert.assertEquals("20080718", pvreqMsg.getPurchaseEndRecur());
			Assert.assertEquals("200", pvreqMsg.getPurchaseInstall());
			Assert.assertTrue(pvreqMsg.validate());

			//---------------Test PaymentAuthReq conversion ----------------
			//Concat PaymentAuthReq XML strings
			String xmlStr =
				authReqXmlStr1
					//+ Base64Codec.encodeBytes(ZLibCompressor.compress(paresXmlStr))
					+ Base64.encode(ZLibCompressor.compress(paresXmlStr))
					+ authReqXmlStr2;

			//Create PaymentVerifReq DOM document
			doc = XMLUtil.createDocument(xmlStr, false);

			//Convert document to messsage
			msg = (new DomToMsgConverter()).convert(doc);

			//Check result
			Assert.assertTrue("true", (msg instanceof PaymentAuthReqMessage));

			//Check content
			PaymentAuthReqMessage authReqMsg = (PaymentAuthReqMessage) msg;

			Assert.assertEquals("001", authReqMsg.getId());
			Assert.assertEquals("1.0", authReqMsg.getVersion());
			Assert.assertEquals(
				//Base64Codec.encodeBytes(ZLibCompressor.compress(paresXmlStr)),
				Base64.encode(ZLibCompressor.compress(paresXmlStr)),
				authReqMsg.getPaymentAuthMsg());
			Assert.assertTrue(authReqMsg.validate());

			//---------------Test VERes Conversion --------------------------
			//Create VERes DOM Document
			doc = XMLUtil.createDocument(veresXmlStr, false);

			//Convert document to messsage
			msg = (new DomToMsgConverter()).convert(doc);

			//Check message type
			Assert.assertTrue((msg instanceof VEResMessage));

			//Check content
			VEResMessage veresMsg = (VEResMessage) msg;

			// Adding extension to VERes
			Extension ext1 = new Extension();
			ext1.setExtID("com.oncecorp.extID");
			ext1.setExtValue("extra extension info");
			ext1.setCritical("false");
			veresMsg.setExtension(new Extension[] {ext1});

			Assert.assertTrue(veresMsg.validate());
			// Print out the XML string of this new message
			System.out.println("-- Start: Printing the VERes message XML version --");
			System.out.println( XMLUtil.toXmlString(veresMsg.toXML()) );
			System.out.println("== End: Printing the VERes message XML version ==");

			// Asserting all message attributes values
			Assert.assertEquals("999", veresMsg.getId());
			Assert.assertEquals("1.0.1", veresMsg.getVersion());
			Assert.assertEquals("N", veresMsg.getEnrolled());
			Assert.assertEquals("12345678", veresMsg.getAcctID());
			Assert.assertEquals("http://visa.acs.com", veresMsg.getUrl());
			String[] protocol = veresMsg.getProtocol();
			Assert.assertEquals(1, protocol.length);
			Assert.assertEquals("ThreeDSecure", protocol[0]);
			Assert.assertEquals("50", veresMsg.getIReqCode());
			Assert.assertEquals("iReqDetail here", veresMsg.getIReqDetail());
			Assert.assertEquals("char less than 256", veresMsg.getVendorCode());

			// Validating the Extension portion
			Extension[] resultExt = veresMsg.getExtension();
			Assert.assertEquals("com.oncecorp.extID",resultExt[0].getExtID());
			Assert.assertEquals("extra extension info",resultExt[0].getExtValue());
			Assert.assertEquals("false",resultExt[0].getCritical());



			//---------------Test PAReq Conversion --------------------------
			//Create PAReq DOM Document
			doc = XMLUtil.createDocument(pareqXmlStr, false);

			//Convert document to messsage
			msg = (new DomToMsgConverter()).convert(doc);

			//Check result
			Assert.assertTrue("true", (msg instanceof PAReqMessage));

			//Check content
			PAReqMessage pareqMsg = (PAReqMessage) msg;

			System.out.println(pareqMsg.toString());
			System.out.println(XMLUtil.toXmlString(doc));

			Assert.assertTrue(pareqMsg.validate());

			//---------------Test PARes Conversion --------------------------
			//Create PARes DOM Document
			doc = XMLUtil.createDocument(paresXmlStr, false);

			//Convert document to messsage
			msg = (new DomToMsgConverter()).convert(doc);

			//Check result
			Assert.assertTrue("true", (msg instanceof PAResMessage));

			//Check content
			PAResMessage paresMsg = (PAResMessage) msg;

			Assert.assertEquals("999", paresMsg.getId());
			Assert.assertEquals("#PA2157205452125", paresMsg.getRefId());
			Assert.assertEquals("1.0.1", paresMsg.getVersion());
			Assert.assertEquals("199876", paresMsg.getMerAcqBIN());
			Assert.assertEquals("merID", paresMsg.getMerID());
			Assert.assertEquals("This is a 28 byte long value", paresMsg.getPurXid());
			Assert.assertEquals("20020806 10:10:10", paresMsg.getPurDate());
			Assert.assertEquals("99999", paresMsg.getPurAmount());
			Assert.assertEquals("124", paresMsg.getPurCurrency());
			Assert.assertEquals("2", paresMsg.getPurExponent());
			Assert.assertEquals("0000000000000000", paresMsg.getPan());
			Assert.assertEquals("20020806 11:11:11", paresMsg.getTxTime());
			Assert.assertEquals("N", paresMsg.getTxStatus());

			Assert.assertTrue(paresMsg.validate());

			//---------------Test Error Conversion --------------------------
			//Create Error DOM Document
			doc = XMLUtil.createDocument(errorXmlStr, false);

			//Convert document to messsage
			msg = (new DomToMsgConverter()).convert(doc);

			//Check result
			Assert.assertTrue("true", (msg instanceof ErrorMessage));

			//Check content
			ErrorMessage errorMsg = (ErrorMessage) msg;

			Assert.assertEquals("999", errorMsg.getId());
			Assert.assertEquals("1.0.1", errorMsg.getVersion());
			Assert.assertEquals("1", errorMsg.getErrorCode());
			Assert.assertEquals("Error Message", errorMsg.getErrorMessage());
			Assert.assertEquals("Error Detail", errorMsg.getErrorDetail());
			Assert.assertTrue(errorMsg.validate());

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test PAReqMessage
	 * Methods tested: setters, toXML(), validate()
	 */
	public void testPAReq() {
		//Get empty PAReq message
		PAReqMessage pareq = null;
		try {
			pareq = (PAReqMessage) MessageGenerator.create("PAReq", "1.0.1");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//----------- Test setter validation ------------
		//Test setId()
		try {
			//invalid value
			pareq.setId("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setId("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setMerAcqBin()
		try {
			//invalid value
			pareq.setMerAcqBIN("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setMerAcqBIN("199872");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setMerCountry()
		try {
			//invalid value
			pareq.setMerCountry("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setMerCountry("CAN");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setMerURL()
		try {
			//invalid value
			pareq.setMerURL("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setMerURL("http://merchant.com");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurDate()
		try {
			//invalid value
			pareq.setPurDate("2002abcd 12:12:12");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setPurDate("20020807 12:12:12");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurXid()
		try {
			//invalid value
			pareq.setPurXid("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setPurXid("TranID1234");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurDispAmount()
		try {
			//invalid value
			pareq.setPurDispAmount("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setPurDispAmount("$123.45");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurAmount()
		try {
			//invalid value
			pareq.setPurAmount("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setPurAmount("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurCurrency()
		try {
			//invalid value
			pareq.setPurCurrency("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setPurCurrency("124");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurExponent()
		try {
			//invalid value
			pareq.setPurExponent("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setPurExponent("2");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurDesc()
		try {
			//invalid value
			pareq.setPurDesc(
				"This is a very very very very very"
					+ " very very very very very very very very very"
					+ " very very very very very very very very very"
					+ " very very very very very very very very very"
					+ " long string.");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setPurDesc("Now it's a good one.");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurRecurFrequency()
		try {
			//invalid value
			pareq.setPurRecurFrequency("1111");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setPurRecurFrequency("25");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurRecurExpiry()
		try {
			//invalid value
			pareq.setPurRecurExpiry("123456789");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setPurRecurExpiry("1234");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setMerName()
		try {
			//invalid value
			pareq.setMerName("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setMerName("Merchant Name");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurInstall()
		try {
			//invalid value
			pareq.setPurInstall("1234567890123");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setPurInstall("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setChAcctID()
		try {
			//invalid value
			pareq.setChAcctID("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setChAcctID("4505xxxxxxxxxxxx");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setChExpiry()
		try {
			//invalid value
			pareq.setChExpiry("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setChExpiry("0408");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setMerId()
		try {
			//invalid value
			pareq.setMerID("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pareq.setMerID("MerID123");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//----------- Test validate() method ------------
		try {
			pareq.validate();
		}
		catch(MessagingException e) {
			Assert.fail();
		}

		//----------- Test toString() method ------------
		System.out.println(pareq.toString());

		//----------- Test toXML() method ---------------
		try {
			Document doc = pareq.toXML();
			Assert.assertTrue(doc != null);

			doc = XMLUtil.createDocument(XMLUtil.toXmlString(doc), false);
			Assert.assertTrue((new DomToMsgConverter()).convert(doc).validate());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test PAResMessage
	 * Methods tested: setters toXML(), validate()
	 */
	public void testPARes() {
		//Get empty PARes message
		PAResMessage pares = null;
		try {
			pares = (PAResMessage) MessageGenerator.create("PARes", "1.0.1");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//----------- Test setter validation ------------
		//Test setId()
		try {
			//invalid value
			pares.setId("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setId("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Set reference id
		try {
			//valid value
			pares.setRefId("PARes12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setMerAcqBIN()
		try {
			//invalid value
			pares.setMerAcqBIN("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setMerAcqBIN("19987263");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setMerID()
		try {
			//invalid value
			pares.setMerID("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setMerID("MerID123");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurXid()
		try {
			//invalid value
			pares.setPurXid("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setPurXid("This is a 28 byte long value");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurDate()
		try {
			//invalid value
			pares.setPurDate("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setPurDate("20020807 12:23:34");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurAmount()
		try {
			//invalid value
			pares.setPurAmount("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setPurAmount("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurExponent()
		try {
			//invalid value
			pares.setPurExponent("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setPurExponent("2");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPan()
		try {
			//invalid value
			pares.setPan("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setPan("4898782737462722");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setTxTime()
		try {
			//invalid value
			pares.setTxTime("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setTxTime("20020807 12:12:23");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setTxStatus()
		try {
			//invalid value
			pares.setTxStatus("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setTxStatus("Y");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setTxCavv()
		try {
			//invalid value
			pares.setTxCavv("123456789012345678901");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value is 28 byte long
			pares.setTxCavv("This is a 28 byte long value");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setTxEci()
		try {
			//invalid value
			pares.setTxEci("1234567890123");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value is exactly two decimal digits
			pares.setTxEci("12");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setTxCavvAlgorithm()
		try {
			//invalid value
			pares.setTxCavvAlgorithm("-12");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setTxCavvAlgorithm("1");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setIreqCode()
		try {
			//invalid value
			pares.setIreqCode("123");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setIreqCode("50");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setIreqDetail()
		try {
			//valid value
			pares.setIreqDetail("Detail goes here");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurcurrency()
		try {
			//invalid value
			pares.setPurCurrency("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setPurCurrency("124");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setSignature()
		try {
			//invalid value
			pares.setSignature(null);

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pares.setSignature(signatureStr);
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//----------- Test validate() method ------------
		try {
			pares.validate();
		}
		catch (MessagingException e) {
			Assert.fail();
		}

		//----------- Test toXML() method ---------------
		try {
			Document doc = pares.toXML();
			Assert.assertTrue(doc != null);

			//Validate message
			doc = XMLUtil.createDocument(XMLUtil.toXmlString(doc), false);
			Assert.assertTrue((new DomToMsgConverter()).convert(doc).validate());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test PaymentAuthReqMessage
	 * Methods tested: setters toXML(), validate()
	 */
	public void testPaymentAuthReq() {
		//Get empty PaymentAuthReq message
		PaymentAuthReqMessage authReq = null;
		try {
			authReq =
				(PaymentAuthReqMessage) MessageGenerator.create("PaymentAuthReq", "1.0");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//----------- Test setter validation ------------
		//Test setId()
		try {
			//invalid value
			authReq.setId(null);

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			authReq.setId("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPaymentAuthMsg
		try {
			//invalid value
			authReq.setPaymentAuthMsg("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			authReq.setPaymentAuthMsg("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		try {
			authReq.setMerchantID("merchant-001");
		} catch (MessagingException e) {
			e.printStackTrace();
			fail();
		}

		//----------- Test validate() method ------------
		try {
			authReq.validate();
		}
		catch (MessagingException e) {
			// this message was supposed to be valid
			Assert.fail();
		}

		//----------- Test toXML() method ---------------
		try {
			Document doc = authReq.toXML();
			Assert.assertTrue(doc != null);

			doc = XMLUtil.createDocument(XMLUtil.toXmlString(doc), false);
			Assert.assertTrue((new DomToMsgConverter()).convert(doc).validate());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test PaymentAuthResMessage
	 * Methods tested: setters toXML(), validate()
	 */
	public void testPaymentAuthRes() {
		//Get empty PaymentAuthRes message
		PaymentAuthResMessage authRes = null;
		try {
			authRes =
				(PaymentAuthResMessage) MessageGenerator.create("PaymentAuthRes", "1.0");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//----------- Test setter validation ------------
		//Test setId()
		try {
			//invalid value
			authRes.setId("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			authRes.setId("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setMerchantID()
		try {
			//invalid value
			authRes.setMerchantID("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			authRes.setMerchantID("11111111111");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setDate()
		try {
			//invalid value
			authRes.setPurchaseDate("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			authRes.setPurchaseDate("20020807 12:12:12");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setTime()
		try {
			//invalid value
			authRes.setAuthDate("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			authRes.setAuthDate("20020807 11:11:11");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurchAmount()
		try {
			//invalid value
			authRes.setPurchaseAmount("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			authRes.setPurchaseAmount("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setCurrency()
		try {
			//invalid value
			authRes.setCurrency("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			authRes.setCurrency("124");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setExponent()
		try {
			//invalid value
			authRes.setExponent("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			authRes.setExponent("2");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setStatus()
		try {
			//invalid value
			authRes.setStatus("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			authRes.setStatus("Y");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setCavv()
		try {
			//valid value
			authRes.setCavv("CAVV-VALUE is 28 long0123456"); // Must be 28 char long
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setEci()
		try {
			//valid value
			authRes.setEci("12");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setCavvAlgorithm()
		try {
			//valid value
			authRes.setCavvAlgorithm("2");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setTransactionID()
		try {
			//valid value
			authRes.setTransactionID("CTH-314021629821352124304290");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//----------- Test validate() method ------------
		try {
			authRes.validate();
		}
		catch (MessagingException e) {
			// this message was supposed to be valid
			Assert.fail();
		}

		//----------- Test toXML() method ---------------
		try {
			Document doc = authRes.toXML();
			Assert.assertTrue(doc != null);

			doc = XMLUtil.createDocument(XMLUtil.toXmlString(doc), false);
			Assert.assertTrue((new DomToMsgConverter()).convert(doc).validate());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test PAResToPaymentAuthResTransformer
	 */
	public void testPAResToPaymentAuthResTransformer() {
		try {
			//Create PARes DOM Document
			Document doc = XMLUtil.createDocument(paresXmlStr, false);

			//Convert document to messsage
			PAResMessage pares = (PAResMessage) (new DomToMsgConverter()).convert(doc);

			//Get empty PaymentAuthRes messsage
			PaymentAuthResMessage authRes =
				(PaymentAuthResMessage) MessageGenerator.create("PaymentAuthRes", "1.0");

			//Set id since they have different IDs
			authRes.setId("123");
			authRes.setTransactionID("CTH-314021629821352124304290");

			//Transform PARes to PaymentAuthRes
			authRes =
				(PaymentAuthResMessage) (new PAResToPaymentAuthResTransformer()).transform(
					pares,
					authRes);

			//Check result
			Assert.assertTrue(authRes.validate());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	/**
	 * Test ID_Generator
	 */
	public void testIDGenerator() {
		// Generate 1000 id and make sure they are unique
		// be verifying them against the array that stores
		// all the previous ones.
	    final int NB_ID_TO_GENERATE = 1000;
		String storedID[] = new String[NB_ID_TO_GENERATE];

		// test for default id generating
		for (int i = 0; i < NB_ID_TO_GENERATE; i++) {
			String id = ID_Generator.getUniqueId();
			for (int j = 0; j < i; j++) {
				if (storedID[j].equals(id)) {
					String errorMsg = "Non unique id found at position:" + j + " Value:" +
									   storedID[j] + " with iteration #: " + i + " Value: " + id;
					System.out.println(errorMsg);
					Assert.fail(errorMsg);
				}
			}
			storedID[i] = id;
		}

	    // test for specified id length generating
	    for (int i = 0; i < NB_ID_TO_GENERATE; i++) {
			String id = null;

			try {
			    // specify a id length
			    id = ID_Generator.getUniqueId(32);
			    Assert.assertEquals(32, id.length());
			} catch (Exception e){
			  e.printStackTrace();
			}

			for (int j = 0; j < i; j++) {
				if (storedID[j].equals(id)) {
					String errorMsg = "Non unique id found at position:" + j + " Value:" +
									   storedID[j] + " with iteration #: " + i + " Value: " + id;
					System.out.println(errorMsg);
					Assert.fail(errorMsg);
				}
			}
			storedID[i] = id;
		}

	}

	/**
	 * This test validate that the method returning the proper UID for a specific messaging protocol
	 * is behaving properly.
	 *
	 */
	public void testMessageProtocol() {
		try {
			// Test the 3D Secure protocol messages
			// Make sure we get all the 3DSecure messages
			Vector vector = new MessagingConfigAccessor().getAllMessageInfo(MessageMetaInfo.MESSAGE_PROTOCOL_3DSECURE);
			for (Iterator iter = vector.iterator(); iter.hasNext();) {
				MessageUID element = (MessageUID) iter.next();
				if ( (! element.getName().equalsIgnoreCase("CRReq")) &&
					 (! element.getName().equalsIgnoreCase("CRRes")) &&
					 (! element.getName().equalsIgnoreCase("VEReq")) &&
					 (! element.getName().equalsIgnoreCase("VERes")) &&
					 (! element.getName().equalsIgnoreCase("PAReq")) &&
					 (! element.getName().equalsIgnoreCase("PARes")) &&
					 (! element.getName().equalsIgnoreCase("IPReq")) &&
					 (! element.getName().equalsIgnoreCase("IPRes")) &&
					 (! element.getName().equalsIgnoreCase("Error"))) {
					 fail("Invalid message returned for 3DSecure: " + element.getName());
				}
			}

			// Test the MPI_interface protocol messages
			// Make sure we get all the MPI_Interface messages
			vector = new MessagingConfigAccessor().getAllMessageInfo(MessageMetaInfo.MESSAGE_PROTOCOL_MPI);
			for (Iterator iter2 = vector.iterator(); iter2.hasNext();) {
				MessageUID element = (MessageUID) iter2.next();
				if ( (! element.getName().equalsIgnoreCase("PaymentVerifReq")) &&
					 (! element.getName().equalsIgnoreCase("PaymentVerifRes")) &&
					 (! element.getName().equalsIgnoreCase("PaymentAuthReq")) &&
					 (! element.getName().equalsIgnoreCase("PaymentAuthRes")) &&
					 (! element.getName().equalsIgnoreCase("ProfileVerifReq")) &&
					 (! element.getName().equalsIgnoreCase("ProfileVerifRes")) &&
					 (! element.getName().equalsIgnoreCase("ProfileAuthReq"))  &&
					 (! element.getName().equalsIgnoreCase("ProfileAuthRes")) &&
					 (! element.getName().equalsIgnoreCase("MPIError"))) {
					 fail("Invalid message returned for MPI_Interface: " + element.getName());
				}
			}

			// Test the all protocol messages
			vector = new MessagingConfigAccessor().getAllMessageInfo(MessageMetaInfo.MESSAGE_PROTOCOL_ALL);
			if (vector.size() == 0) fail("No messages returned when trying to fetch all protocols");
		}
		catch (MessagingException e) {
			// If exception throwned for these good cases then we have a problem
			fail("Unexpected exception thrown while invoking getAllMessageInfo()");
		}

		// Test invalid protocol
		try {
			Vector v = new MessagingConfigAccessor().getAllMessageInfo("4DSecure");
			if (v.size() != 0) fail("Messages returned for invalid protocol");

			new MessagingConfigAccessor().getAllMessageInfo(null);
			fail("Null parameter in getAllMessageInfo not detected properly");
		}
		catch (MessagingException e) {
			// Throw exception for invalid parameter. That's OK
		}

	}

	public static Test suite() {

		JUnitHelper.initFromFile();

		boolean initOK = CoreInitializer.init();
		if (!initOK) {
			System.out.println("Init Core Server failed.");
			fail();
		}

		TestSuite suite = new TestSuite("Messaging suite");
		suite.addTest(new MessagingTest("testConverter"));
		suite.addTest(new MessagingTest("testPAReq"));
		suite.addTest(new MessagingTest("testPARes"));
		suite.addTest(new MessagingTest("testPaymentAuthReq"));
		suite.addTest(new MessagingTest("testPaymentAuthRes"));
		suite.addTest(new MessagingTest("testPAResToPaymentAuthResTransformer"));
		suite.addTest(new MessagingTest("testIDGenerator"));
		suite.addTest(new MessagingTest("testMessageProtocol"));
		return suite;
	}

}