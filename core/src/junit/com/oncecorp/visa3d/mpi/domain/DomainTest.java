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

* (C) Copyright Once Corp. 2002 All rights reserved.
*
* The program is provided "as is" without any warranty express or
* implied, including the warranty of non-infringement and the implied
* warranties of merchantability and fitness for a particular purpose.
* Once Corporation will not be liable for any damages suffered by
* you as a result of using the Program.
* In no event will Once Corporation be liable for any special, indirect
* or consequential damages or lost profits even if Once Corporation
* has been advised of the possibility of their occurrence. Once
* Corporation will not be liable for any third party claims against you.
*
* ---------------------------------------------
* $Header: /dev/oncempi/core-v12/src/junit/com/oncecorp/visa3d/mpi/domain/DomainTest.java 5     5/29/03 3:32p Gwu $
*
*/

package com.oncecorp.visa3d.mpi.domain;

import com.oncecorp.visa3d.mpi.CoreInitializer;
import com.oncecorp.visa3d.mpi.domain.payment.CRReqMessage;
import com.oncecorp.visa3d.mpi.domain.payment.CRResMessage;
import com.oncecorp.visa3d.mpi.messaging.DomToMsgConverter;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageEngine;
import com.oncecorp.visa3d.mpi.messaging.MessageGenerator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
* Description: JUnit test case for ONCEmpi Payment interface component
*
* @version 0.1 Oct 18, 2002
* @author Alan Zhang
*/
public class DomainTest extends TestCase {

	public DomainTest(String name) {
		super(name);
	}

	/**
	* Test CRReq message implementation and their processors. It tests
	* DomToMsgConverter's conversion part of these two messages.
	*/
	public void testCRReq() {
		try {
			//Positive test
			Message newMsg =
				MessageGenerator.create(
					CRReqMessage.MSG_TYPE,
					CRReqMessage.MSG_VERSION);
			if (newMsg == null)
				fail();
			if (!(newMsg instanceof CRReqMessage))
				fail();

			CRReqMessage crreq = (CRReqMessage) newMsg;
			crreq.setId("crreq#123");
			crreq.setMerchantAcqBIN("acqbin#123");
			crreq.setMerchantID("merid#123");
			crreq.setMerchantPassword("password");
			crreq.setSerialNumber("123");

			if (!crreq.validate())
				fail();

			String crreqStr = XMLUtil.toXmlString(crreq.toXML());
			System.out.println("CRReq message: " + crreqStr);

			Message regen = new DomToMsgConverter().convert(crreq.toXML());
			if (regen == null)
				fail();
			if (!(regen instanceof CRReqMessage))
				fail();
			if (!regen.validate())
				fail();

			MessageEngine.process(regen);

			//Negative test
			String temp = crreq.getId();
			try {
				crreq.setId(null);
				fail();
			} catch (MessagingException me) {
			}
			crreq.setId(temp);

			temp = crreq.getMerchantAcqBIN();
			try {
				crreq.setMerchantAcqBIN(null);
				fail();
			} catch (MessagingException me) {
			}
			crreq.setMerchantAcqBIN(temp);

			temp = crreq.getMerchantID();
			try {
				crreq.setMerchantID(null);
				fail();
			} catch (MessagingException me) {
			}
			crreq.setMerchantID(temp);

			temp = crreq.getMerchantPassword();
			try {
				crreq.setMerchantPassword("wronglength");
				fail();
			} catch (MessagingException me) {
			}
			crreq.setMerchantPassword(temp);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	* Test CRRes message implementation and their processors. It tests
	* DomToMsgConverter's conversion part of these two messages.
	*/
	public void testCRRes() {
		try {
			//Positive test
			Message newMsg =
				MessageGenerator.create(
					CRResMessage.MSG_TYPE,
					CRResMessage.MSG_VERSION);
			if (newMsg == null)
				fail();
			if (!(newMsg instanceof CRResMessage))
				fail();

			CRResMessage crres = (CRResMessage) newMsg;
			crres.setId("CRRes#123");
			crres.setSerialNumber("123");
			crres.setProtocol( MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE );

			CardRange[] crs = new CardRange[3];
			CardRange cr1 =
				new CardRange(
					Long.valueOf("4505100020003000"),
					Long.valueOf("4505100020004000"),
					"A");
			CardRange cr2 =
				new CardRange(
					Long.valueOf("4505100020005000"),
					Long.valueOf("4505100020006000"),
					"A");
			CardRange cr3 =
				new CardRange(
					Long.valueOf("4505100020007000"),
					Long.valueOf("4505100020008000"),
					"A");
			crs[0] = cr1;
			crs[1] = cr2;
			crs[2] = cr3;
			crres.setCr(crs);

			if (!crres.validate())
				fail();

			String crresStr = XMLUtil.toXmlString(crres.toXML());
			System.out.println("CRRes message: " + crresStr);

			Message regen = new DomToMsgConverter().convert(crres.toXML());
			/**
			 *  [Gang's Note: 29 May, 2003] As protocol is not part of CR message.
			 *  It's just used for internal operation. We missed them during
			 *  toXML and convert methods, so we need add them mannually.
			 */
			((CRResMessage)regen).setProtocol( MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE );
			System.out.println(
				"CRRes message after conversion: "
					+ XMLUtil.toXmlString(regen.toXML()));
			if (regen == null)
				fail();
			if (!(regen instanceof CRResMessage))
				fail();
			if (!regen.validate())
				fail();

			System.out.println("Process CRRes... " + ((CRResMessage)regen).getProtocol() );
			MessageEngine.process(regen);

			boolean enrolled =
				(new CardRangeManager()).verifyCard("4505100020003015");
			if (!enrolled)
				fail();

			enrolled = (new CardRangeManager()).verifyCard("4505100020007305");
			if (!enrolled)
				fail();

			enrolled = (new CardRangeManager()).verifyCard("4505100020004050");
			if (enrolled)
				fail();

			enrolled = (new CardRangeManager()).verifyCard("4505100020002000");
			if (enrolled)
				fail();

			enrolled = (new CardRangeManager()).verifyCard("4505100020009000");
			if (enrolled)
				fail();

			CRResMessage crresUpdate = (CRResMessage) newMsg;
			crresUpdate.setId("CRRes#123");
			crresUpdate.setSerialNumber("999");

			CardRange[] crsUpdate = new CardRange[3];
			CardRange cr1Update =
				new CardRange(
					Long.valueOf("4505100020003000"),
					Long.valueOf("4505100020004000"),
					"D");
			CardRange cr2Update =
				new CardRange(
					Long.valueOf("4505100020005000"),
					Long.valueOf("4505100020006000"),
					"D");
			CardRange cr3Update =
				new CardRange(
					Long.valueOf("4505100099997000"),
					Long.valueOf("4505100099998000"),
					"A");
			crsUpdate[0] = cr1Update;
			crsUpdate[1] = cr2Update;
			crsUpdate[2] = cr3Update;
			crresUpdate.setCr(crsUpdate);

			if (!crresUpdate.validate())
				fail();

			MessageEngine.process(crresUpdate);

			enrolled = (new CardRangeManager()).verifyCard("4505100020003050");
			if (enrolled)
				fail();

			enrolled = (new CardRangeManager()).verifyCard("4505100020005050");
			if (enrolled)
				fail();

			enrolled = (new CardRangeManager()).verifyCard("4505100020007050");
			if (!enrolled)
				fail();

			enrolled = (new CardRangeManager()).verifyCard("4505100099997050");
			if (!enrolled)
				fail();

			//Negative test
			String temp = crres.getId();
			try {
				crres.setId(null);
				fail();
			} catch (MessagingException me) {
			}
			crres.setId(temp);

			//Exclusive test: iReqCode & CR
			crres.setIreqCode("51");
			try {
				if (crres.validate())
					fail();
			} catch (Exception e) {
				// good
			}
			crres.setIreqCode(null);

			//Bad CR test
			cr3 = new CardRange(Long.valueOf("123"), Long.valueOf("321"), null);
			crs[2] = cr3;
			crres.setCr(crs);
			try {
				if (crres.validate())
					fail();
			} catch (Exception e) {
				// good
			}

			cr3 = new CardRange(Long.valueOf("123"), null, "A");
			crs[2] = cr3;
			crres.setCr(crs);
			try {
				if (crres.validate())
					fail();
			} catch (Exception e) {
				// good
			}

			cr3 = new CardRange(null, Long.valueOf("321"), "D");
			crs[2] = cr3;
			crres.setCr(crs);
			try {
				if (crres.validate())
					fail();
			} catch (Exception e) {
				// good
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public static Test suite() {
		boolean initOK = CoreInitializer.init();
		if (!initOK) {
			System.out.println("Init Core Server failed.");
			fail();
		}

		TestSuite suite = new TestSuite("Domain suite");
		suite.addTest(new DomainTest("testCRReq"));
		suite.addTest(new DomainTest("testCRRes"));
		return suite;
	}

}
