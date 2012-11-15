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

package com.oncecorp.visa3d.mpi.publishing;

import com.oncecorp.visa3d.mpi.domain.payment.VEReqMessage;
import com.oncecorp.visa3d.mpi.messaging.ID_Generator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.JUnitHelper;

import junit.framework.TestCase;

/**
 * JUnit test case for PublishingManager
 *
 * @author azhang
 * @version 1.0 29-Nov-02
 */
public class PublishingManagerTest extends TestCase {

	/**
	 * Constructor for PublishingManagerTest.
	 * @param arg0
	 */
	public PublishingManagerTest(String arg0) {
		super(arg0);
	}

	public void testPublish() {

		JUnitHelper.initFromFile();

		// Prepare message to be published
		// Use VEReq as sample message
		VEReqMessage vereq = null;
		try {
			vereq = new VEReqMessage();
			vereq.setId(ID_Generator.getUniqueId());
			vereq.setPan("4510100020003000");
			vereq.setAcqBIN("123456");
			vereq.setMerID("123456789012345");
			vereq.setPassword("password");
		} catch (MessagingException e) {
			System.out.println("Failed to prepare VEReq message.");
			e.printStackTrace();
			fail();
		}

		// Get PublishingManager instance
		PublishingManager pm = PublishingManager.getInstance();
		if (pm == null) {
			System.out.println("Failed to get PublishingManager instance.");
			fail();
		}

		// Publishing
		try {
			pm.publish(vereq);
		} catch (PublishException e) {
			System.out.println("Publishing failed.");
			e.printStackTrace();
			fail();
		}


	}

}
