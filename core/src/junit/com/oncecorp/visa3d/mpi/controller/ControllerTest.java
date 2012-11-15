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

package com.oncecorp.visa3d.mpi.controller;

import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageGenerator;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;
import com.oncecorp.visa3d.mpi.utility.JUnitHelper;
import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.security.SecurityManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ResourceBundle;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Description: JUnit test case for Visa 3-D Secure(TM)
 * Merchant Plug-In Controller Component
 *
 * @version 0.1 July 17, 2002
 * @author	Alan Zhang
 */
public class ControllerTest extends TestCase {
	public ControllerTest(String name) {
		super(name);
	}

	public void testAuthServlet() {
		try {
			//Get AuthenticatorServlet URL
			/**
			 * [Gang's Note: 29 May, 2003] Get the value from the configuration file
			 */
			/**
			ResourceBundle rb =
				ResourceBundle.getBundle("com.oncecorp.visa3d.mpi.controller.controller");
			String servletURL = rb.getString("servlet_url");
			*/
			String servletURL = (String) Config.getConfigReference().
					   getConfigData(MPIConfigDefinition.CORE_SERVER_URL);

			//Establish connection to servlet
			URL url = new URL(servletURL);
			URLConnection conn = url.openConnection();

			//Prepare for both input and output
			conn.setDoInput(true);
			conn.setDoOutput(true);

			//Turn off caching
			conn.setUseCaches(false);

			//Set content type to MPI message
			conn.setRequestProperty("Content-Type", "application/xml;charset=\"utf-8\"");

			//Create PaymentVerifyReq request.
			String reqStr =
				URLEncoder.encode(
					"<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
						+ "<!DOCTYPE MPI_Interface SYSTEM \"http://demo.oncecorp.com/MPI/MPIInterfaceWithProfile.dtd\">"
						+ "<MPI_Interface>"
						+ "<PaymentVerifReq id=\"417823180326\">"
						+ "<version>1.0</version>"
						+ "<merchantTermURL>http://192.168.0.30:8080/TestApplication/doTransaction.jsp</merchantTermURL>"
						+ "<merchantData>>I don&apos;t know what this is</merchantData>"
						+ "<pan>4563218880030</pan>"
						+ "<panExpiry>1201</panExpiry>"
						+ "<purchaseDate>20020821 10:56:22</purchaseDate>"
						+ "<purchaseAmount>$23.99</purchaseAmount>"
						+ "<purchasePurchAmount>2399</purchasePurchAmount>"
						+ "<purchaseCurrency>124</purchaseCurrency>"
						+ "<purchaseDesc>This is purchaseDesc</purchaseDesc>"
						+ "<browserCategory></browserCategory>"
						+ "<browserAccept></browserAccept>"
						+ "<browserAgent></browserAgent>"
						+ "<purchaseRecur>"
						+ "<purchaseFrequency></purchaseFrequency>"
						+ "<purchaseEndRecur></purchaseEndRecur>"
						+ "</purchaseRecur>"
						+ "<purchaseInstall>200</purchaseInstall>"
						+ "</PaymentVerifReq>"
						+ "</MPI_Interface>");

			//Set content length
			conn.setRequestProperty("Content-Length", Integer.toString(reqStr.length()));

			//Write the request as post data
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			out.write(reqStr);
			out.flush();
			out.close();

			//Read response
			BufferedReader in =
				new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String resStr = in.readLine();
			String line;
			while ((line = in.readLine()) != null) {
				resStr += line;
			}
			in.close();
			resStr = java.net.URLDecoder.decode(resStr);

			//Check response
			Assert.assertEquals(false, (resStr == null));
			System.out.println("Response: " + resStr);

			//Check response content
			org.w3c.dom.Document dom = XMLUtil.createDocument(resStr, true);
			System.out.println(
				"Error code: "
					+ XMLUtil.getValueByXPath(dom, "/MPI_Interface/MPIError/errorCode/text()"));
			System.out.println(
				"Error message: "
					+ XMLUtil.getValueByXPath(dom, "/MPI_Interface/MPIError/errorMessage/text()"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testCommunicator() throws CommunicatorException {
		//Get AuthenticatorServlet URL
		/**
		 * [Gang's Note: 29 May, 2003] Get the value from the configuration file
		 */
		/*
		ResourceBundle rb =
			ResourceBundle.getBundle("com.oncecorp.visa3d.mpi.controller.controller");
		String entityURL = rb.getString("entity_url");
		*/
	   //Create dummy request.
	   try {
		   String entityURL = (String) Config.getConfigReference().
					   getConfigData( MPIConfigDefinition.VISA_DIR_URL_1 );

		   Communicator ctr = new Communicator();

			Message reqMsg = MessageGenerator.create("VEReq", "1.0.1");
			Message resMsg = ctr.send(reqMsg, entityURL);
			Assert.assertEquals(true, (resMsg != null));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public static Test suite() {

		JUnitHelper.initFromFile();


		SecurityManager.getInstance().initializeConnection();

 		TestSuite suite = new TestSuite("Controller suite");
		suite.addTest(new ControllerTest("testAuthServlet"));
		suite.addTest(new ControllerTest("testCommunicator"));
		return suite;
	}

}