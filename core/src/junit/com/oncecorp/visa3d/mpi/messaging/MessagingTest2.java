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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;

import com.oncecorp.visa3d.mpi.CoreInitializer;
import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.configuration.MerchantMetaInfo;
import com.oncecorp.visa3d.mpi.domain.payment.CurrencyCode;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorMessage;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorToMPIErrorTransformer;
import com.oncecorp.visa3d.mpi.domain.payment.PAReqMessage;
import com.oncecorp.visa3d.mpi.domain.payment.VEReqMessage;
import com.oncecorp.visa3d.mpi.domain.payment.VEResMessage;
import com.oncecorp.visa3d.mpi.domain.payment.VEResToPAReqTransformer;
import com.oncecorp.visa3d.mpi.domain.payment.VEResToPaymentVerifResTransformer;
import com.oncecorp.visa3d.mpi.intf.payment.MPIErrorMessage;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentVerifReqMessage;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentVerifReqToPAReqTransformer;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentVerifReqToVEReqTransformer;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentVerifResMessage;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;
import com.oncecorp.visa3d.mpi.utility.JUnitHelper;

/**
 * Description: JUnit test case for Visa 3-D Secure(TM)
 * Merchant Plug-In Messaging Component
 *
 * Tests: VEReqMessage
 * 		  VEResMessage
 *        ErrorMessage
 *        MPIErrorMessage
 *        PaymentVerifReqMessage
 *        PaymentVerifResMessage
 * 		  PaymentVerifReqToVEReqTransformer
 *        VEResToPAReqTransformer
 *        ErrorToMPIErrorTransformer
 *        VEResToPaymentVerifResTransformer
 *
 * @version 0.1 Aug 07, 2002
 * @author	Jun Shi
 */

public class MessagingTest2 extends TestCase {

	// PaymentVerifReq xml String
	protected static String pvreqXmlStr =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<MPI_Interface>"
			+ "<PaymentVerifReq id=\"001\">"
			+ "<version>1.0</version>"
			+ "<merchantID>testMerchant1</merchantID>"
			+ "<merchantTermURL>http://www.buy.com</merchantTermURL>"
			+ "<merchantData>merchantData</merchantData>"
			+ "<pan>123456789012345</pan>"
			+ "<panExpiry>0912</panExpiry>"
			+ "<purchaseDate>20020101 12:12:12</purchaseDate>"
			+ "<purchaseAmount>$123.45</purchaseAmount>"
			+ "<purchasePurchAmount>12345</purchasePurchAmount>"
			+ "<purchaseCurrency>124</purchaseCurrency>"
			+ "<purchaseDesc>This is purchaseDesc</purchaseDesc>"
			+ "<browserCategory>0</browserCategory>"
			+ "<browserAccept>aaa</browserAccept>"
			+ "<browserAgent>bbb</browserAgent>"
			+ "<PurchaseRecur>"
			+ "<frequency>25</frequency>"
			+ "<endRecur>20050404</endRecur>"
			+ "</PurchaseRecur>"
			+ "<purchaseInstall>200</purchaseInstall>"
			+ "</PaymentVerifReq> "
			+ "</MPI_Interface>";

	// VERes Case0: enrolled = "Y" (positive test)
	protected static String veresXmlStr0 =
		"<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
			+ "<ThreeDSecure>"
			+ "<Message id=\"999\">"
			+ "<VERes>"
			+ "<version>1.0.1</version>"
			+ "<CH>"
			+ "<enrolled>Y</enrolled>"
			+ "<acctID>12345678</acctID>"
			+ "</CH>"
			+ "<url>http://visa.acs.com</url>"
			+ "<protocol>ThreeDSecure</protocol>"
			+ "<IReq> "
			+ "<iReqCode></iReqCode>"
			+ "<iReqDetail></iReqDetail>"
			+ "<vendorCode></vendorCode>"
			+ "</IReq>"
			+ "</VERes>"
			+ "</Message>"
			+ "</ThreeDSecure>";

	// VERes Case2: enrolled = "Y" (negative test)
	protected static String veresXmlStr2 =
		"<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
			+ "<ThreeDSecure>"
			+ "<Message id=\"999\">"
			+ "<VERes>"
			+ "<version>1.0.1</version>"
			+ "<CH>"
			+ "<enrolled>Y</enrolled>"
			+ "<acctID>12345678</acctID>"
			+ "</CH>"
			+ "<url></url>"
			+ "<protocol>ThreeDSecure</protocol>"
			+ "<IReq> "
			+ "<iReqCode>50</iReqCode>"
			+ "<iReqDetail>aaaaa</iReqDetail>"
			+ "<vendorCode>aaa</vendorCode>"
			+ "</IReq>"
			+ "</VERes>"
			+ "</Message>"
			+ "</ThreeDSecure>";

	// VERes Case1: enrolled = "N" (positive test)
	protected static String veresXmlStr1 =
		"<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
			+ "<ThreeDSecure>"
			+ "<Message id=\"999\">"
			+ "<VERes>"
			+ "<version>1.0.1</version>"
			+ "<CH>"
			+ "<enrolled>N</enrolled>"
			+ "<acctID></acctID>"
			+ "</CH>"
			+ "<url></url>"
			+ "<IReq> "
			+ "<iReqCode>50</iReqCode>"
			+ "<iReqDetail>iReq Detail here</iReqDetail>"
			+ "<vendorCode>char less than 256</vendorCode>"
			+ "</IReq>"
			+ "</VERes>"
			+ "</Message>"
			+ "</ThreeDSecure>";

	// VERes Case3: enrolled = "N" (nagetive test)
	protected static String veresXmlStr3 =
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
			+ "<iReqCode></iReqCode>"
			+ "<iReqDetail>iReq Detail here</iReqDetail>"
			+ "<vendorCode>char less than 256</vendorCode>"
			+ "</IReq>"
			+ "</VERes>"
			+ "</Message>"
			+ "</ThreeDSecure>";

	protected static String errXmlStr =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<ThreeDSecure>"
			+ "<Message id=\"0002\">"
			+ "<Error>"
			+ "<version>1.0.1</version>"
			+ "<errorCode>5</errorCode>"
			+ "<errorMessage>here is errMsg</errorMessage>"
			+ "<errorDetail>here is errDetails</errorDetail>"
			+ "<vendorCode>this is optional field</vendorCode>"
			+ "</Error>"
			+ "</Message>"
			+ "</ThreeDSecure>";

	public MessagingTest2(String name) {
		super(name);
	}

	/**
	 * Test VEReqMessage
	 * Methods tested: setters, toXML(), validate()
	 */
	public void testVEReq() {
		//Get empty VEReq message
		VEReqMessage vereq_msg = null;
		try {
			vereq_msg =
				(VEReqMessage) MessageGenerator.create("VEReq", "1.0.1");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Testing setters validation

		//Test setId()
		try {
			//invalid value
			vereq_msg.setId("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			vereq_msg.setId("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPan()
		try {
			//invalid value
			vereq_msg.setPan("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value 13-19 digits
			vereq_msg.setPan("4500100020003000");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setAcqBIN()
		try {
			//invalid value (>11 digits)
			vereq_msg.setAcqBIN("1234567890123");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value (<11 digits)
			vereq_msg.setAcqBIN("12345678");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setMerID()
		try {
			//invalid value
			vereq_msg.setMerID("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			vereq_msg.setMerID("<24 digits");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPassword()
		try {
			//invalid value (8 chars if not null)
			vereq_msg.setPassword("1234567890");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			vereq_msg.setPassword("12345678");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setDeviceCategory()
		try {
			//invalid value (1 chars if not null)
			vereq_msg.setDeviceCategory("12");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			vereq_msg.setDeviceCategory("1");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//----------- Test validate() method ------------
		try {
			vereq_msg.validate();
		} catch (MessagingException e) {
			Assert.fail();
		}

		//----------- Test toXML() method ---------------
		try {
			Document doc = vereq_msg.toXML();
			String vereqStr = XMLUtil.toXmlString(doc);

			// check document validation
			doc = XMLUtil.createDocument(vereqStr, false);

			Assert.assertTrue(doc != null);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test VEResMessage
	 * Methods tested: setters, toXML(), validate()
	 */
	public void testVERes() {

		VEResMessage veres_msg0 = null;
		VEResMessage veres_msg1 = null;

		VEResMessage veres_msg2 = null;
		VEResMessage veres_msg3 = null;

		try {
			/** Testing VERes Case0: enrolled = "Y" (positive) */

			// create an empty VERes
			veres_msg0 =
				(VEResMessage) MessageGenerator.create("VERes", "1.0.1");

			//Create VERes DOM Document
			Document doc0 = XMLUtil.createDocument(veresXmlStr0, false);

			//Convert document to messsage
			VEResMessage veres0 =
				(VEResMessage) (new DomToMsgConverter()).convert(doc0);

			// populate fields
			veres_msg0.setId(veres0.getId());
			veres_msg0.setEnrolled(veres0.getEnrolled());
			veres_msg0.setAcctID(veres0.getAcctID());
			veres_msg0.setUrl(veres0.getUrl());
			veres_msg0.setProtocol(veres0.getProtocol());
			veres_msg0.setIReqCode(veres0.getIReqCode());
			veres_msg0.setIReqDetail(veres0.getIReqDetail());
			veres_msg0.setVendorCode(veres0.getVendorCode());

			//----------- Test validate() method for Case 0 ------------
			Assert.assertTrue(veres_msg0.validate());

			//----------- Test toXML() method --------------------------
			Document dom0 = veres_msg0.toXML();
			String veresStr0 = XMLUtil.toXmlString(dom0);
			// check document validation
			dom0 = XMLUtil.createDocument(veresStr0, false);
			Assert.assertTrue(dom0 != null);

			/** Testing VERes Case2: enrolled = "Y" (negative) */

			// create an empty VERes
			veres_msg2 =
				(VEResMessage) MessageGenerator.create("VERes", "1.0.1");

			//Create VERes DOM Document
			Document doc2 = XMLUtil.createDocument(veresXmlStr2, false);

			//Convert document to messsage
			VEResMessage veres2 =
				(VEResMessage) (new DomToMsgConverter()).convert(doc2);

			// populate fields
			veres_msg2.setId(veres2.getId());
			veres_msg2.setEnrolled(veres2.getEnrolled());
			veres_msg2.setAcctID(veres2.getAcctID());
			veres_msg2.setUrl(veres0.getUrl());
			veres_msg2.setProtocol(veres2.getProtocol());
			veres_msg2.setIReqCode(veres2.getIReqCode());
			veres_msg2.setIReqDetail(veres2.getIReqDetail());
			veres_msg2.setVendorCode(veres2.getVendorCode());

			//----------- Test validate() method for Case 2 ------------
			try {
				veres_msg2.validate();
				// Should not validate properly because we have both enrolled = "Y" and
				// iReqCode specified
				Assert.fail();
			} catch (MessagingException e) {
				// Good validation if we reach this area
			}

			/** Testing VERes Case1: enrolled = "N" (positive) */

			// create an empty VERes
			veres_msg1 =
				(VEResMessage) MessageGenerator.create("VERes", "1.0.1");

			//Create VERes DOM Document
			Document doc1 = XMLUtil.createDocument(veresXmlStr1, false);

			//Convert document to messsage
			VEResMessage veres1 =
				(VEResMessage) (new DomToMsgConverter()).convert(doc1);

			// populate fields
			veres_msg1.setId(veres1.getId());
			veres_msg1.setEnrolled(veres1.getEnrolled());
			veres_msg1.setAcctID(veres1.getAcctID());
			veres_msg1.setUrl(veres1.getUrl());
			veres_msg1.setProtocol(veres1.getProtocol());
			veres_msg1.setIReqCode(veres1.getIReqCode());
			veres_msg1.setIReqDetail(veres1.getIReqDetail());
			veres_msg1.setVendorCode(veres1.getVendorCode());

			//----------- Test validate() method for Case 1 ------------
			Assert.assertTrue(veres_msg1.validate());

			//----------- Test toXML() method --------------------------
			Document dom1 = veres_msg1.toXML();
			String veresStr1 = XMLUtil.toXmlString(dom1);
			// check document validation
			dom1 = XMLUtil.createDocument(veresStr1, false);
			Assert.assertTrue(dom1 != null);

			/** Testing VERes Case3: enrolled = "N" (positive) */

			// create an empty VERes
			veres_msg3 =
				(VEResMessage) MessageGenerator.create("VERes", "1.0.1");

			//Create VERes DOM Document
			Document doc3 = XMLUtil.createDocument(veresXmlStr3, false);

			//Convert document to messsage
			VEResMessage veres3 =
				(VEResMessage) (new DomToMsgConverter()).convert(doc3);

			// populate fields
			veres_msg3.setId(veres3.getId());
			veres_msg3.setEnrolled(veres3.getEnrolled());
			veres_msg3.setAcctID(veres3.getAcctID());
			veres_msg3.setUrl(veres3.getUrl());
			veres_msg3.setProtocol(veres3.getProtocol());
			veres_msg3.setIReqCode(veres3.getIReqCode());
			veres_msg3.setIReqDetail(veres3.getIReqDetail());
			veres_msg3.setVendorCode(veres3.getVendorCode());

			//----------- Test validate() method for Case 3 ------------
			try {
				veres_msg3.validate();
				Assert.fail();
			} catch (MessagingException e) {
				// validation should fail because of invalid IReq settings
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	/**
	 * Test ErrorMessage
	 * Methods tested: setters, toXML(), validate()
	 */
	public void testError() {
		//Get empty MPIError message
		ErrorMessage err = null;
		try {
			err = (ErrorMessage) MessageGenerator.create("Error", "1.0.1");
		} catch (Exception e) {
			e.printStackTrace();
		}

		/** Testing setters validation */

		//Test setId()
		try {
			//invalid value
			err.setId("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			err.setId("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setErrorCode()
		try {
			//invalid value (char >1)
			err.setErrorCode("123");

		} catch (MessagingException me) {
			fail();
		}

		try {
			//valid value
			err.setErrorCode("1");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setErrorMessage()
		try {
			//invalid value
			err.setErrorMessage("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			err.setErrorMessage("err Message here");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setErrorDetail()
		try {
			//invalid value
			err.setErrorDetail("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			err.setErrorDetail("err Detail here");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setVendorCode(): it is Optional and < 256 if not null
		try {
			//invalid value > 256
			err.setVendorCode(
				"This is a very very very very very "
					+ " very very very very very very very very very"
					+ " very very very very very very very very very"
					+ " very very very very very very very very very"
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
			err.setVendorCode("");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//----------- Test validate() method ------------
		try {
			err.validate();
		} catch (MessagingException e) {
			Assert.fail();
		}

		//----------- Test toXML() method ---------------
		try {
			Document doc = err.toXML();
			String errStr = XMLUtil.toXmlString(doc);

			// check document validation
			doc = XMLUtil.createDocument(errStr, false);

			Assert.assertTrue(doc != null);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test MPIErrorMessage
	 * Methods tested: setters, toXML(), validate()
	 */
	public void testMPIError() {

		//Get empty MPIError message
		MPIErrorMessage mpiEr = null;
		try {
			mpiEr =
				(MPIErrorMessage) MessageGenerator.create("MPIError", "1.0");
		} catch (Exception e) {
			e.printStackTrace();
		}

		/** Testing setters validation */
		//Test setId()
		try {
			//invalid value
			mpiEr.setId("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}
		try {
			//valid value
			mpiEr.setId("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setErrorCode()
		try {
			//invalid value
			mpiEr.setErrorCode("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}
		try {
			//valid value
			mpiEr.setErrorCode("888");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setErrorMessage()
		try {
			//invalid value
			mpiEr.setErrorMessage("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}
		try {
			//valid value
			mpiEr.setErrorMessage("here is mpi err msg");

		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setVendorCode(): it is Optional and < 256 if not null
		try {
			//invalid value > 256
			mpiEr.setVendorCode(
				"This is a very very very very very "
					+ " very very very very very very very very very"
					+ " very very very very very very very very very"
					+ " very very very very very very very very very"
					+ " very very very very very very very very very"
					+ " very very very very very very very very very"
					+ " very very very very very very very very very"
					+ " very very very very very very very very very"
					+ " very very very very very very very very very"
					+ " very very very very very very very very very"
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
			mpiEr.setVendorCode("");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setMerchantID()
		try {
			//invalid value
			mpiEr.setMerchantID("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}
		try {
			//valid value
			mpiEr.setMerchantID("testMerchant2");

		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//----------- Test validate() method ------------
		try {
			mpiEr.validate();
		} catch (MessagingException e) {
			// That was supposed to be a valid message
			Assert.fail();

		}

		//----------- Test toXML() method ---------------
		try {
			Document doc = mpiEr.toXML();
			String mpiErrStr = XMLUtil.toXmlString(doc);

			// check document validation
			doc = XMLUtil.createDocument(mpiErrStr, false);

			Assert.assertTrue(doc != null);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test PaymentVerifReqMessage
	 * Methods tested: setters, toXML(), validate()
	 */
	public void testPaymentVerifReq() {
		//Get empty PaymentVerifReq message
		PaymentVerifReqMessage pvreq = null;
		try {
			pvreq =
				(PaymentVerifReqMessage) MessageGenerator.create(
					"PaymentVerifReq",
					"1.0");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//----------- Test setter validation ------------
		//Test setId()
		try {
			//invalid value
			pvreq.setId(null);

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvreq.setId("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setMerchantTermURL()
		try {
			//invalid value
			pvreq.setMerchantTermURL("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvreq.setMerchantTermURL("http://merchant.com");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setMerchantData()
		try {
			//Valid value
			pvreq.setMerchantData(null);
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		try {
			//valid value
			pvreq.setMerchantData("Merchant Data here");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPan()
		try {
			//invalid value
			pvreq.setPan("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvreq.setPan("4500100020003000");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPanExpiry()
		try {
			//invalid value
			pvreq.setPanExpiry("9930");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvreq.setPanExpiry("0408");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurDate()
		try {
			//invalid value
			pvreq.setPurchaseDate("2002abcd 12:12:12");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvreq.setPurchaseDate("20020807 12:12:12");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurchaseAmount()
		try {
			//invalid value - more than 20
			pvreq.setPurchaseAmount("1234567890123456789012");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvreq.setPurchaseAmount("$123.45");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurPurchaseAmount()
		try {
			//invalid value
			pvreq.setPurchasePurchAmount("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvreq.setPurchasePurchAmount("123456");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurCurrency()
		try {
			//invalid value - more than 3 digits
			pvreq.setPurchaseCurrency("12345");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvreq.setPurchaseCurrency("124");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurDesc()
		try {
			//invalid value - more than 125
			pvreq.setPurchaseDesc(
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
			pvreq.setPurchaseDesc("Now it's a good one.");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setBrowserCategory()
		try {
			//invalid value - more than 1 digit
			pvreq.setBrowserCategory("123");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvreq.setBrowserCategory("0");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurRecurFrequency()
		try {
			//invalid value - more than 2 digits
			pvreq.setPurchaseFrequency("99999");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvreq.setPurchaseFrequency("25");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurRecurExpiry()
		try {
			//invalid value
			pvreq.setPurchaseEndRecur("20080909aaa");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvreq.setPurchaseEndRecur("20040505");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test setPurInstall()
		try {
			//invalid value - more than 3 digits
			pvreq.setPurchaseInstall("1234");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvreq.setPurchaseInstall("60");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		try {
			pvreq.setMerchantID("testMerchant1");
		} catch (MessagingException e) {
			e.printStackTrace();
			fail();
		}

		//----------- Test validate() method ------------
		try {
			pvreq.validate();
		} catch (MessagingException e) {
			Assert.fail();
		}

		//----------- Test toXML() method ---------------
		try {

			Document doc = pvreq.toXML();
			String pvreqStr = XMLUtil.toXmlString(doc);

			// check document validation
			doc = XMLUtil.createDocument(pvreqStr, false);

			Assert.assertTrue(doc != null);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test PaymentVerifResMessage
	 * Methods tested: setters, toXML(), validate()
	 */
	public void testPaymentVerifRes() {
		//Get empty PaymentVerifRes message
		PaymentVerifResMessage pvres = null;
		try {
			pvres =
				(PaymentVerifResMessage) MessageGenerator.create(
					"PaymentVerifRes",
					"1.0");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//----------- Test setter validation ------------
		//Test setId()
		try {
			//invalid value
			pvres.setId("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvres.setId("12345");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//Test enrolled
		try {
			//invalid value
			pvres.setEnrolled("A");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		//Test setMerchantID()
		try {
			//invalid value
			pvres.setMerchantID("");

			//If passed, bug detected
			fail();
		} catch (MessagingException me) {
			//Good, we expect this.
		}

		try {
			//valid value
			pvres.setMerchantID("testMerchant2");
		} catch (MessagingException me) {
			//if any exception
			me.printStackTrace();
			fail();
		}

		//////////// conflict fields test /////////////////
		//Test enrolled(Y): (negative test)
		try {
			// init value
			pvres.setId("001");
			pvres.setEnrolled("Y");

			//invalid value
			pvres.setHtmlForm("");
			try {
				pvres.validate();
				Assert.fail();
			} catch (MessagingException e) {
			}
		} catch (Exception e) {
			//if any exception
			e.printStackTrace();
			fail();
		}

		//Test enrolled(Y): (positive test)
		try {
			// init value
			pvres.setId("002");
			pvres.setEnrolled("Y");

			//valid value
			pvres.setHtmlForm("htmlForm is here");
			boolean isValid = pvres.validate();

			if (!isValid) {
				try {
					throw new MessagingException("fail");
				} catch (Exception e) {
					//If passed, bug detected
					fail();
				}
			}
		} catch (Exception e) {
			//if any exception
			e.printStackTrace();
			fail();
		}

		//Test enrolled(N): (negative test)
		try {
			// init value
			pvres.setId("001");
			pvres.setEnrolled("N");

			//invalid value
			pvres.setHtmlForm("should be null");
			try {
				pvres.validate();
				Assert.fail();
			} catch (MessagingException e) {

			}
		} catch (Exception e) {
			//if any exception
			e.printStackTrace();
			fail();
		}

		//Test enrolled(N): (positive test)
		try {
			// init value
			pvres.setId("004");
			pvres.setEnrolled("N");

			//valid value
			pvres.setHtmlForm("");
			pvres.setInvalidReq("50");
			pvres.setInvalidDetails("iReq Details here");
			pvres.setVendorCode("This is optional field");
			boolean isValid = pvres.validate();

			if (!isValid) {
				try {
					throw new MessagingException("fail");
				} catch (Exception e) {
					//If passed, bug detected
					fail();
				}
			}
		} catch (Exception e) {
			//if any exception
			e.printStackTrace();
			fail();
		}

		//Test enrolled(U): (positive test)
		try {
			// init value
			pvres.setId("005");
			pvres.setEnrolled("U");

			//valid value
			pvres.setHtmlForm("");
			pvres.setInvalidReq("");
			pvres.setInvalidDetails("");
			pvres.setVendorCode("");
			boolean isValid = pvres.validate();

			if (!isValid) {
				try {
					throw new MessagingException("fail");
				} catch (Exception e) {
					//If passed, bug detected
					fail();
				}
			}
		} catch (Exception e) {
			//if any exception
			e.printStackTrace();
			fail();
		}

		//----------- Test toXML() method ---------------
		try {
			Document doc = pvres.toXML();

			String pvresStr = XMLUtil.toXmlString(doc);

			// check document validation
			doc = XMLUtil.createDocument(pvresStr, false);

			Assert.assertTrue(doc != null);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	/**
	 * Test PaymentVerifReqToVEReqTransformer
	 */
	public void testPaymentVerifReqToVEReqTransformer() {
		try {
			//Create PaymentVerifReq DOM Document
			Document doc = XMLUtil.createDocument(pvreqXmlStr, false);

			//Convert document to messsage
			PaymentVerifReqMessage pvreq =
				(PaymentVerifReqMessage) (new DomToMsgConverter()).convert(doc);

			//Get empty VEReq messsage
			VEReqMessage vereq =
				(VEReqMessage) MessageGenerator.create("VEReq", "1.0.1");

			//Set id since they have different IDs
			vereq.setId("123");

			//Transform PaymentVerifReq To VEReq
			vereq =
				(VEReqMessage)
					(new PaymentVerifReqToVEReqTransformer()).transform(
					pvreq,
					vereq);

			/** fetch other vereq required data from configuration component */

			// get Config referrence
			Config cfg = Config.getConfigReference();

			// Get merchant meta info
			MerchantMetaInfo mmi =
				(MerchantMetaInfo) cfg.getMerchantMetaInfo(vereq.getMerID());

			System.out.println("### merchantID is:" + vereq.getMerID());
			// Set acquirer BIN
			vereq.setAcqBIN(mmi.getAcquirerBIN());

			// Set password
			vereq.setPassword(mmi.getMerchantPassword());

			// Release no-longer used references
			cfg = null;

			// Check result
			Assert.assertTrue(vereq.validate());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test PaymentVerifReqToPAReqTransformer and VEResToPAReqTransformer
	 */
	public void testPaymentVerifReqToPAReq_and_VEResToPAReqTransformer() {
		try {
			//Create PaymentVerifReq DOM Document
			Document doc1 = XMLUtil.createDocument(pvreqXmlStr, false);

			//Convert document to messsage
			PaymentVerifReqMessage pvreq =
				(PaymentVerifReqMessage) (new DomToMsgConverter()).convert(
					doc1);

			//Create VERes DOM Document
			Document doc2 = XMLUtil.createDocument(veresXmlStr0, false);

			//Convert document to messsage
			VEResMessage veres =
				(VEResMessage) (new DomToMsgConverter()).convert(doc2);

			//Get empty PAReq messsage
			PAReqMessage pareq =
				(PAReqMessage) MessageGenerator.create("PAReq", "1.0.1");

			//Set id since they have different IDs
			pareq.setId(veres.getId());

			//Transform VERes To PAReq
			pareq =
				(PAReqMessage) (new VEResToPAReqTransformer()).transform(
					veres,
					pareq);

			//Transform PaymentVerifReq To PAReq
			pareq =
				(PAReqMessage)
					(new PaymentVerifReqToPAReqTransformer()).transform(
					pvreq,
					pareq);

			/** fetch data from config */

			// Get Config reference
			Config config = Config.getConfigReference();

			//Get merchant meta info
			MerchantMetaInfo mmi =
				(MerchantMetaInfo) config.getMerchantMetaInfo(pareq.getMerID());

			System.out.println("### merchant id is:[" + pareq.getMerID() + "]" );

			//Set acquirer BIN
			pareq.setMerAcqBIN(mmi.getAcquirerBIN());

			//Set Merchant Name
			pareq.setMerName(mmi.getMerchantName());

			//Set Merchant Country Code
			pareq.setMerCountry(mmi.getMerchantCountryCode());

			//Set Merchant URL
			/*
				 * [Martin's Note: Sep 20, 2002 7:40:11 AM]
				 * Merchant URL is no longer in the configuration file since it is always supplied by the
				 * invoking application. So we remove the lookup for it in this test case
				 */
			//pareq.setMerURL("http://www.buy.com");

			//Check purchase currency. If none, use default value
			if ((pareq.getPurCurrency() == null)
				|| (pareq.getPurCurrency().trim().length() == 0)) {
				// There is no currency specified
				pareq.setPurCurrency(mmi.getMerchantPurchaseCurrency());
			}

			//Set exponent
			pareq.setPurExponent(
				CurrencyCode.getExponentForCurrency(pareq.getPurCurrency()));

			//Check merchant URL. If none, use default value
			if ((pareq.getMerURL() == null)
				|| (pareq.getMerURL().trim().length() == 0)) {
				// There is no merchant URL specified
				pareq.setMerURL(mmi.getMerchantURL());
			}
			// Release Config reference
			config = null;

			//Check result
			Assert.assertTrue(pareq.validate());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test ErrorToMPIErrorTransformer
	 */
	public void testErrorToMPIErrorTransformer() {
		try {
			//Create Error DOM Document
			Document doc = XMLUtil.createDocument(errXmlStr, false);

			//Convert document to messsage
			ErrorMessage err =
				(ErrorMessage) (new DomToMsgConverter()).convert(doc);

			//Get empty MPIError messsage
			MPIErrorMessage mpiErr =
				(MPIErrorMessage) MessageGenerator.create("MPIError", "1.0");

			//Set id since they have different IDs
			mpiErr.setId("123");

			//Transform PaymentVerifReq To VEReq
			mpiErr =
				(MPIErrorMessage) (new ErrorToMPIErrorTransformer()).transform(
					err,
					mpiErr);

			//Check result
			Assert.assertTrue(mpiErr.validate());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test VEResToPaymentVerifResTransformer
	 */
	public void testVEResToPaymentVerifResTransformer() {
		try {
			//Create VERes DOM Document
			Document doc = XMLUtil.createDocument(veresXmlStr1, false);

			//Convert document to messsage
			VEResMessage veres =
				(VEResMessage) (new DomToMsgConverter()).convert(doc);

			//Get empty MPIError messsage
			PaymentVerifResMessage pvres =
				(PaymentVerifResMessage) MessageGenerator.create(
					"PaymentVerifRes",
					"1.0");

			//Set id since they have different IDs
			pvres.setId("123");
			pvres.setMerchantID("testMerchant2");

			//Transform PaymentVerifReq To VEReq
			pvres =
				(PaymentVerifResMessage)
					(new VEResToPaymentVerifResTransformer()).transform(
					veres,
					pvres);

			//Check result
			Assert.assertTrue(pvres.validate());
		} catch (Exception e) {
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

		TestSuite suite = new TestSuite("Messaging suite #2");
		suite.addTest(new MessagingTest2("testVEReq"));
		suite.addTest(new MessagingTest2("testVERes"));
		suite.addTest(new MessagingTest2("testError"));
		suite.addTest(new MessagingTest2("testMPIError"));
		suite.addTest(new MessagingTest2("testPaymentVerifReq"));
		suite.addTest(new MessagingTest2("testPaymentVerifRes"));
		suite.addTest(
			new MessagingTest2("testPaymentVerifReqToVEReqTransformer"));
		suite.addTest(
			new MessagingTest2("testPaymentVerifReqToPAReq_and_VEResToPAReqTransformer"));
		suite.addTest(new MessagingTest2("testErrorToMPIErrorTransformer"));
		suite.addTest(
			new MessagingTest2("testVEResToPaymentVerifResTransformer"));
		return suite;
	}
}