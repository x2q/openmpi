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

package com.oncecorp.visa3d.bridge.startup;

import java.util.Enumeration;
import java.util.Map;
import java.util.Hashtable;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.PrintWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import javax.naming.Context;

import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.configure.ConfigurationManager;

/**
 * <p>Title: DatabridgeServlet </p>
 * <p>Description: In case the application server doesn't support custom service
 * or startup, use this dummy servlet as databridge entry.</p>
 * The initial parameters for DatabridgeServlet are:
 * <ul>
 * <li>fromFile: if configuration from data source, value=<code>false</code>; otherwise value=<code>true</code>
 * <li>fileName: if configuration from file, set value=<code>configuration file absolute path</code>
 * <li>dbsource: if configuration from data source, set value = <code>data source JNDI name</code>
 * <li>tableName: data base table that contain configuration data. default is <code>bridge_config</code>
 * <li>Context.INITIAL_CONTEXT_FACTORY (="java.naming.factory.initial"), value is context factory class, for example on Weblogic is <code>weblogic.jndi.WLInitialContextFactory</code>
 * <li>Context.PROVIDER_URL (="java.naming.provider.url"), value is JNDI provider name, for example on Weblogic is:<code>t3://localhost:7001</code>
 * <li>Context.SECURITY_PRINCIPAL (="java.naming.security.principal"), value is application server system user name
 * <li>Context.SECURITY_CREDENTIALS (="java.naming.security.credentials"), value is application server system user password
 * <li>some application server related properties, for example on Webloic <code>("weblogic.jndi.createIntermediateContexts", "true")</code>
 * </ul>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */


public class DatabridgeServlet extends HttpServlet
{
    static final private String CONTENT_TYPE = "text/html";

    /**
     * Initialize global variables, start databridge service
     * @throws ServletException
     */
    public void init() throws ServletException
    {
        Hashtable paras = new Hashtable();
        String key, value;
        for ( Enumeration em = getInitParameterNames(); em.hasMoreElements(); )
        {
            key = ( String ) em.nextElement();
            value = getInitParameter( key );
            paras.put( key, value );
        }

        DataBridgeLoger.getLogger( this.getClass() ).debug( "Start databridge component.");
		if ( paras.get( Context.INITIAL_CONTEXT_FACTORY ) != null
			&& paras.get( Context.PROVIDER_URL ) != null )
	           StartupProxy.setServerContext( false );
        else
			StartupProxy.setServerContext( true );
        StartupProxy.setInitParameters( paras );
        ConfigurationManager.getInstance().initialize( paras );
        DataBridgeLoger.getLogger( this.getClass() ).debug( "Databridge component is ready for business.");
    }

    /**
     * Process the HTTP Get request, only show generic message.
     *
     * @param request - Http request
     * @param response - Http response
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>DatabridgeServlet</title></head>");
        out.println("<body>");
        out.println("<p>The servlet has received a GET. This is the reply.</p>");
        out.println("</body></html>");
    }

    /**
     * Process the HTTP Post request, only show generic message.
     * @param request - Http request
     * @param response - Http response
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>DatabridgeServlet</title></head>");
        out.println("<body>");
        out.println("<p>The servlet has received a POST. This is the reply.</p>");
        out.println("</body></html>");
    }

    /**
     * Process the HTTP Put request, do nothing
     * @param request - Http request
     * @param response - Http response
     * @throws ServletException
     * @throws IOException
     */
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    }

    /**
     * Clean up resources
     */
    public void destroy()
    {
    }

}