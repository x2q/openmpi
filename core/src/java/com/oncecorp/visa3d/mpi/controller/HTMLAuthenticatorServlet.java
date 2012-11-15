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

import com.oncecorp.visa3d.mpi.logging.MPILogger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Description: The Servlet who receives requests from external entities.
 * This servlet is designated to process enrollment verification requests
 * and authentication requests for which submitted by HTML hidden fields.
 *
 * A set of intermedia messages are created to represent HTML hidden fields
 * and will be transformed into ONCEmpi interface messages or HTML form.
 *
 * @version 0.1 Feb 24, 2003
 * @author	Alan Zhang
 */
public class HTMLAuthenticatorServlet extends HttpServlet {
	/**
	 * Local Log4J logger
	 */
	protected Logger logger =
		MPILogger.getLogger(HTMLAuthenticatorServlet.class.getName());

	/**
	 * Servlet initialization.
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
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

		String resStr = null;

		//Initialize IO Stream
		OutputStreamWriter responseOutputStream = null;

		try {

			//Authenticate request
			resStr = HTMLAuthenticatorImpl.authenticate(request);

			this.logger.debug(
				"Response message received from HTMLAuthenticator: " + resStr);

			//Get output stream
			responseOutputStream =
				new OutputStreamWriter(response.getOutputStream());
//			responseOutputStream.write(URLEncoder.encode(resStr));
            responseOutputStream.write(resStr);
			responseOutputStream.flush();

		} catch (Exception e) {
			this.logger.error("Unexpected exception caught.", e);
		} finally {
			//Clean-up
			try {
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
			"<head><title>VISA 3-D Secure(TM) Merchant Plug-In HTML Authenticator Server</title></head>");
		out.println("<body>");
		out.println("<p>HTML Authenticator Server is running</p>");
		out.println(
			"<p>Current Time at Server: "
				+ new java.util.Date().toString()
				+ "</p>");
		out.println("</body>");
		out.println("</html>");
	}

}