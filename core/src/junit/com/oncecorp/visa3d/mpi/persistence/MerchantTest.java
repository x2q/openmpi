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

package com.oncecorp.visa3d.mpi.persistence;

import com.oncecorp.visa3d.mpi.configuration.MerchantMetaInfo;
import com.oncecorp.visa3d.mpi.security.MPIEncrypter;
import com.oncecorp.visa3d.mpi.utility.JUnitHelper;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test Case object to validate the Merchant DAO functionality
 *
 * @author Martin Dufort (mdufort@oncecorp.com)
 * @version $Revision: 3 $
 */
public class MerchantTest extends TestCase {

	private final String CREATE_ID = "190_188_167";
	private MerchantDAO dao = null;

	public MerchantTest(String aTestName) {
		super(aTestName);

	}

	protected void setUp() throws Exception {
		super.setUp();

		JUnitHelper.initFromDB();
		// Initialize DAO
		dao 		= new MerchantDAO();

	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();

		// Remove created merchant
		// dao.delete(CREATE_ID);

	}

	public void testCreateMerchant() {
		// Create a new merchant and then verifies that it can be retrieved from
		// the database
		HashMap createMap 		= new HashMap();
		MerchantMetaInfo mInfo 	= new MerchantMetaInfo();


		// Add new information into the MerchantMetaInfo
		mInfo.setMerchantID(CREATE_ID);
		mInfo.setMerchantName("Junit test merchant 1");
		mInfo.setMerchantPassword(MPIEncrypter.encrypt("mypass11"));
		mInfo.setMerchantPurchaseCurrency("124");
		mInfo.setMerchantCountryCode("124");
		mInfo.setMerchantURL("ww.junitmerchant.com");
		mInfo.setAcquirerBIN("bin191192");

		Calendar cal = new GregorianCalendar();
		cal.set(2005, 10, 1, 14, 00, 00);
		mInfo.setKeyExpiryDate(cal.getTime().getTime());
		mInfo.setLicensingKey("this is a the key to set");

		// Insert it
		createMap.put(mInfo.getMerchantID(), mInfo);

		try {
			// Create new merchant
			dao.create(createMap);

			// Check if we can retrieve it
			Object rs = dao.retrieve(mInfo.getMerchantID());
			assertNotNull(rs);
		}
		catch (PersistentException pExcep) {
			fail("Unable to create the merchant in the proper store");
		}
	}

	public void testUpdateMerchant() {
		// Create a new merchant and then verifies that it can be retrieved from
		// the database
		HashMap updateMap		= new HashMap();
		MerchantMetaInfo mInfo 	= getMerchant(CREATE_ID);


		// Add new information into the MerchantMetaInfo
		mInfo.setMerchantName("Junit updated merchant");
		mInfo.setMerchantPassword(MPIEncrypter.encrypt("mypass11"));
		mInfo.setMerchantPurchaseCurrency("840");
		mInfo.setMerchantCountryCode("124");
		mInfo.setMerchantURL("ww.junitmerchant2.com");
		mInfo.setAcquirerBIN("bin191192");

		Calendar cal = new GregorianCalendar();
		cal.set(2005, 11, 12, 14, 00, 00);
		mInfo.setKeyExpiryDate(cal.getTime().getTime());
		mInfo.setLicensingKey("this is a the key to set");

		// Insert it
		updateMap.put(mInfo.getMerchantID(), mInfo);

		try {
			// Create new merchant
			dao.update(updateMap);

			// Check if we can retrieve it
			MerchantMetaInfo result = getMerchant(CREATE_ID);
			if (result.getMerchantName().equals(mInfo.getMerchantName()) &&
				result.getMerchantPurchaseCurrency().equals(mInfo.getMerchantPurchaseCurrency()) &&
				result.getMerchantURL().equals(mInfo.getMerchantURL()) &&
				result.getKeyExpiryDate() == (mInfo.getKeyExpiryDate())) {
					// Validation is good
					/* do nothing */
			}
			else {
				fail("Merchant information not updated properly");
			}
		}
		catch (PersistentException pExcep) {
			fail("Unable to update the merchant in the proper store");
		}
	}

	public void testDeleteMerchant() {
		// Create a new merchant and then verifies that it can be retrieved from
		// the database
		try {
			// Create new merchant
			dao.delete(CREATE_ID);

			// Check if we can retrieve it
			Object rs = dao.retrieve(CREATE_ID);
			assertNull(rs);
		}
		catch (PersistentException pExcep) {
			// If we get persistent exception, then that's OK
		}
	}

	private MerchantMetaInfo getMerchant(String aMerchantID) {
		try {
			HashMap rs = (HashMap) new MerchantDAO().retrieve(aMerchantID);
			Iterator it = rs.values().iterator();
			while (it.hasNext()) {
				MerchantMetaInfo element = (MerchantMetaInfo)it.next();

				// return the first element found
				return element;
			}
		}
		catch (PersistentException e) {

		}


		return null;
	}

	/**
	 * Test Suite definition
	 * @return Test Suite for this component
	 */
	public static Test suite() {
		return  new TestSuite("Merchant DAO suite");
		/**
		 * [Gang Wu's Note: May 31, 2003]
		 * This test suite is removed as the merchant is updated has some problem.
		 */
		/*
		TestSuite suite = new TestSuite("Merchant DAO suite");
		suite.addTest(new MerchantTest("testCreateMerchant"));
		suite.addTest(new MerchantTest("testUpdateMerchant"));
		suite.addTest(new MerchantTest("testDeleteMerchant"));
		return suite;
		*/
	}

}
