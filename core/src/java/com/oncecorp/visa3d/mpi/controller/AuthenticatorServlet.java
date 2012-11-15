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

import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.ConfigurationException;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.jmxagent.CoreAgentException;
import com.oncecorp.visa3d.mpi.jmxagent.CoreAgentManager;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.persistence.ConfigDAO;
import com.oncecorp.visa3d.mpi.persistence.DAODefinition;
import com.oncecorp.visa3d.mpi.persistence.Log4jDAO;
import com.oncecorp.visa3d.mpi.persistence.MerchantDAO;
import com.oncecorp.visa3d.mpi.security.SecurityManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import java.util.Enumeration;

/**
 * The Servlet who receives requests from external entities. 
 * This is the J2EE bridge mechanism used by an external system to invoke the MPI functionality 
 * if they are unable to directly access the Authenticator class.
 * 
 * @version 0.1 July 16, 2002
 * @author	Alan Zhang
 */
public class AuthenticatorServlet extends HttpServlet {
	/**
	 * Local Log4J logger
	 */
	protected Logger logger;

	/**
	 * First request trigger
	 */
	private static boolean waitUntilFirstRequest;
	private static String CORE_CONFIG_LOCATION = "CoreConfigLocation";
	private static String JAVA_PROPERTY_PREFIX = "java:";

	/**
	 * Servlet initialization.
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			// Initialize all the internal parameters from the supplied info in the 
			// web descriptor file
			System.out.println("Reading parameters specified in the web.xml descriptor file");
			initParams(config);

			// initialize local logger
			logger = MPILogger.getLogger(AuthenticatorServlet.class.getName());

			// load WaitUntilFirstReqeust property
			String wufr =
				(String) Config.getConfigReference().getConfigData(
					MPIConfigDefinition.WAIT_UNTIL_FIRST_REQUEST);

			if ((wufr != null) && (wufr.equalsIgnoreCase("true"))) {
				logger.debug(
					"CardRangeMonitor initialization waits until first request.");
				AuthenticatorServlet.waitUntilFirstRequest = true;
			} else {
				logger.debug("Start CardRangeMonitor initialization.");
				AuthenticatorServlet.waitUntilFirstRequest = false;
				initCRMonitor();
			}
			
			/*
			 * Then initialize the proper connection parameters
			 */
			boolean initialOK = SecurityManager.getInstance().initializeConnection();
			if (!initialOK) {
				this.logger.error("Failed to initialize connection parameters.");
			}
			
			/* 
			 * [Jun's Note - Dec 04, 2002]
			 * JMX agent initialization
			 */
			CoreAgentManager.initAgent();
			
			/*
			 * Indicate that initialization is OK and we are ready to accept requests
			 */
			System.out.println("- - - - Verified@ONCE Core Server component is open for e-business - - - -");

		} catch (ConfigurationException ce) {
			logger.error(
				"Failed to load config property. WaitUntilFirstRequest property set to 'true' as default.",
				ce);
			AuthenticatorServlet.waitUntilFirstRequest = true;
		} catch (CoreAgentException cae) {
			logger.error("Jmx Agent Starting Error: ", cae);
		}
	}

	/**
	 * Initialize the internal parameters from the supplied information in the Web
	 * descriptor file. 
	 * 
	 * All servlet parameters that are prefixed with the string
	 * <code>java:</code> are assumed to be Java system properties and 
	 * will be automatically created as such by this method.
	 * 
	 * @param config	Servlet configuration object where to get the parameters
	 * @throws ConfigurationException
	 */
	private void initParams(ServletConfig config) throws ConfigurationException {
		// Create all Java property if they are specified
		Enumeration e = config.getInitParameterNames();
		while (e.hasMoreElements()) {
			String element = (String) e.nextElement();
			if (element.toLowerCase().startsWith(JAVA_PROPERTY_PREFIX)) {
				// We got one
				String value = config.getInitParameter(element);
				if (value != null) {
					String propertyName = element.substring(JAVA_PROPERTY_PREFIX.length()); // Remove "java:" before setting it 
					System.setProperty(propertyName, value);
				}
			}
			
		}
		
		// Initialize the Config DAO properties from the servlet init parameters
		ConfigDAO.setJdbcDriver(
			config.getInitParameter(DAODefinition.CONFIG_DAO_JDBC_DRIVER));
		ConfigDAO.setDbUrl(
			config.getInitParameter(DAODefinition.CONFIG_DAO_DB_URL));
		ConfigDAO.setDbUsername(
			config.getInitParameter(DAODefinition.CONFIG_DAO_DB_USERNAME));
		ConfigDAO.setDbPassword(
			config.getInitParameter(DAODefinition.CONFIG_DAO_DB_PASSWORD));
		ConfigDAO.setDbSchema(
			config.getInitParameter(DAODefinition.CONFIG_DAO_DB_SCHEMA));
		ConfigDAO.setDbTable(
			config.getInitParameter(DAODefinition.CONFIG_DAO_DB_TABLE));

		// Initialize the Log4J DAO properties from the servlet init parameters
		Log4jDAO.setJdbcDriver(
			config.getInitParameter(DAODefinition.LOG4J_DAO_JDBC_DRIVER));
		Log4jDAO.setDbUrl(
			config.getInitParameter(DAODefinition.LOG4J_DAO_DB_URL));
		Log4jDAO.setDbUsername(
			config.getInitParameter(DAODefinition.LOG4J_DAO_DB_USERNAME));
		Log4jDAO.setDbPassword(
			config.getInitParameter(DAODefinition.LOG4J_DAO_DB_PASSWORD));
		Log4jDAO.setDbSchema(
			config.getInitParameter(DAODefinition.LOG4J_DAO_DB_SCHEMA));
		Log4jDAO.setDbTable(
			config.getInitParameter(DAODefinition.LOG4J_DAO_DB_TABLE));
		
		// Initialize the Merchant DAO properties from the servlet init parameters
		MerchantDAO.setJdbcDriver(
			config.getInitParameter(DAODefinition.MERCHANT_DAO_JDBC_DRIVER));
		MerchantDAO.setDbUrl(
			config.getInitParameter(DAODefinition.MERCHANT_DAO_DB_URL));
		MerchantDAO.setDbUsername(
			config.getInitParameter(DAODefinition.MERCHANT_DAO_DB_USERNAME));
		MerchantDAO.setDbPassword(
			config.getInitParameter(DAODefinition.MERCHANT_DAO_DB_PASSWORD));
		MerchantDAO.setDbSchema(
			config.getInitParameter(DAODefinition.MERCHANT_DAO_DB_SCHEMA));
		MerchantDAO.setDbTable(
			config.getInitParameter(DAODefinition.MERCHANT_DAO_DB_TABLE));
	
	}

	/**
	 * HTTP POST Request Handler
	 * @param request HTTP Request
	 * @param response HTTP Response
	 */
	public void doPost(
		HttpServletRequest request,
		HttpServletResponse response)
		throws ServletException, IOException {

		//Check CR Monitor 
		if (waitUntilFirstRequest) {
			logger.debug(
				"First request triggers CardRangeMonitor initialization.");
			AuthenticatorServlet.waitUntilFirstRequest = false;
			initCRMonitor();
		}

		//Initialize XML String
		String reqXmlStr = null;
		String resXmlStr = null;

		//Initialize IO Stream
		BufferedReader requestInputStream = null;
		OutputStreamWriter responseOutputStream = null;

		// Read the serialized XML request string from the input stream
		try {
			//Initial input stream
			requestInputStream =
				new BufferedReader(
					new InputStreamReader(request.getInputStream()));
			String reqStr = requestInputStream.readLine();
			String line;
			while ((line = requestInputStream.readLine()) != null) {
				reqStr += line;
			}
			reqXmlStr = URLDecoder.decode(reqStr);

			//Authenticate request
			resXmlStr = AuthenticatorImpl.authenticate(reqXmlStr);

			this.logger.debug(
				"Response message received from Authenticator: " + resXmlStr);

			//Get output stream
			responseOutputStream =
				new OutputStreamWriter(response.getOutputStream());
			responseOutputStream.write(URLEncoder.encode(resXmlStr));
			responseOutputStream.flush();

		} catch (IOException ioe) {
			this.logger.error(
				"Fail to read/write object from/to InputStream/OutputStream.",
				ioe);
			respondError(
				response,
				AuthenticatorCodes.IO_ERROR,
				AuthenticatorCodes.IO_ERROR_MSG);
		} catch (Exception e) {
			this.logger.error("Unexpected exception caught.", e);
			respondError(
				response,
				AuthenticatorCodes.UNEXPECTED_ERROR,
				AuthenticatorCodes.UNEXPECTED_ERROR_MSG);
		} finally {
			//Clean-up
			try {
				if (requestInputStream != null)
					requestInputStream.close();

				if (responseOutputStream != null)
					responseOutputStream.close();
			} catch (Exception e) {
				this.logger.error("Fail to close IO stream.", e);
			}
		}
	}

	/**
	 * HTTP GET Request Handler
	 * All GET requests will be treated as "ping" request.
	 * @param request HTTP Request
	 * @param response HTTP Response
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, java.io.IOException {

		// set content-type header before accessing the Writer
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		// then write the response
		out.println("<html>");
		out.println(
			"<head><title>VISA 3-D Secure(TM) Merchant Plug-In Authenticator Server</title></head>");
		out.println("<body>");
		out.println("<p>Authenticator Server is running</p>");
		out.println(
			"<p>Current Time at Server: "
				+ new java.util.Date().toString()
				+ "</p>");
		out.println("</body>");
		out.println("</html>");
	}

	/**
	 * Error Handler
	 * @param response HTTP Response object
	 * @param code Error code
	 * @param msg Error message
	 */
	public void respondError(
		HttpServletResponse response,
		String code,
		String msg) {
		//Initialize output stream
		OutputStreamWriter osw = null;

		try {
			//Get response XML String
			String resXmlStr = AuthenticatorCodes.getMPIErrorMsg(code, msg);

			//Get output stream
			osw = new OutputStreamWriter(response.getOutputStream());
			osw.write(URLEncoder.encode(resXmlStr));
			osw.flush();

		} catch (Exception e) {
			this.logger.error("Fail to respond error.", e);
		} finally {
			//Clean-up
			try {
				if (osw != null)
					osw.close();
			} catch (IOException ioe) {
				this.logger.error("Fail to close output stream.", ioe);
			}
		}

	}

	private void initCRMonitor() {
		try {
			Config cfg = Config.getConfigReference();
			String sleepInt =
				(String) cfg.getConfigData(
					MPIConfigDefinition.CR_MONITOR_SLEEP_INTERVAL);
			String cacheExp =
				(String) cfg.getConfigData(
					MPIConfigDefinition.CR_CACHE_EXPIRED);
			new CardRangeMonitor(
				Long.parseLong(sleepInt),
				Long.parseLong(cacheExp))
				.start();

		} catch (Exception e) {
			logger.error(
				"Exception caught during CardRangeMonitor initialization. Start monitor with default setup.",
				e);
			new CardRangeMonitor().start();
		}
	}

}