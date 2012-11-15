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

package com.oncecorp.visa3d.bridge.utility;

/**
 * <p>Title: XMLUtils</p>
 * <p>Description: Provides XML related functions using JAXP </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import java.util.StringTokenizer;

public class XMLUtils
{
    public final static String WEB_PREFIX = "/WEB-INF/onceconfig/";

    /**
     * Default constructor
     */
    public XMLUtils()
    {
    }

    /**
     * @param vflag - validation flag
     */
    private final static DocumentBuilder getDocumentBuilder( boolean vflag )
        throws ParserConfigurationException, UnsupportedEncodingException
    {
        /**
         * XML JAXP factory instance
         */
        DocumentBuilderFactory m_jaxpFactory =
                    DocumentBuilderFactory.newInstance();

        /**
         * Document builder instance
         */
        DocumentBuilder m_db = null;

        m_jaxpFactory.setValidating(vflag);
        m_jaxpFactory.setNamespaceAware(false);
        m_db = m_jaxpFactory.newDocumentBuilder();
        m_db.setErrorHandler(XmlErrorHandler.getInstance());
        return m_db;

    }

    /**
     * Get XML Document from a url
     * @param url - the xml file's URL
     * @return - the XML Document of the given url
     */
    public final static Document getDocument( String url )
    {
        return getDocument( url, false );
    }

    /**
     * Get XML Document from a url
     * @param url - the xml file's URL
     * @param vflag - validation flag
     * @return - the XML Document of the given url
     */
    public final static Document getDocument( String url, boolean vflag )
    {
        try {
            return getDocumentBuilder( vflag ).parse( url );
        } catch ( Exception e )
        {
            System.err.println("Exception in [XMLUtils.getDocument] " + e.getMessage() );
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get XML Document from a url
     * @param url - the xml file's URL
     * @return - the XML Document of the given url
     */
    public final static Document getDocumentWithException( String url )
        throws ParserConfigurationException, UnsupportedEncodingException,
                IOException, SAXException
    {
        return getDocumentBuilder( false ).parse( url );
    }

    /**
     * Get XML Document from a url
     * @param url - the xml file's URL
     * @param vflag - validation flag
     * @return - the XML Document of the given url
     */
    public final static Document getDocumentWithException( String url, boolean vflag )
        throws ParserConfigurationException, UnsupportedEncodingException,
                IOException, SAXException
    {
        return getDocumentBuilder( vflag ).parse( url );
    }

    /**
     * Get XML Document from an input source
     * @param is - input source
     * @return - the XML document
     */
    public final static Document getDocument( InputSource is )
    {
        return getDocument( is, false );
    }

    /**
     * Get XML document from an input source
     * @param is - input source
     * @param vflag - validation flag
     * @return - the XML document
     */
    public final static Document getDocument( InputSource is, boolean vflag )
    {
        try {
            is.setSystemId("");
            return getDocumentBuilder( vflag ).parse( is );
        } catch ( Exception e )
        {
            System.err.println("Exception in [getDocument] " + e.getMessage() );
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Error handler to report errors and warnings
     */
    private static class XmlErrorHandler implements ErrorHandler
    {
        /** Error handler output goes here */
        private PrintWriter out;
        private static XmlErrorHandler handler;

        private XmlErrorHandler()
        {
            this.out = new PrintWriter(System.err);
        }

        public static XmlErrorHandler getInstance() {
          if ( handler == null ) {
            handler = new XmlErrorHandler();
          }
          return handler;
        }

        /**
         * Returns a string describing parse exception details
         * @param title - the tile
         * @param spe - the exception instance
         */
        private String getParseExceptionInfo(String title, SAXParseException spe)
        {
            StringBuffer sb = new StringBuffer( title );
            sb.append( spe.getMessage() );
            sb.append( " " );

            String systemId = spe.getSystemId();
            if (systemId == null)
            {
                systemId = "null";
            }
            sb.append( "URI = [" );
            sb.append( systemId );
            sb.append("] ");
            sb.append(" at line [" );
            sb.append( spe.getLineNumber() );
            sb.append("], column is [");
            sb.append( spe.getColumnNumber() );
            sb.append( "] " );
            return sb.toString();
        }

        /**
         * The following methods are standard SAX ErrorHandler methods.
         * See SAX documentation for more info.
         * @param spe - exception instance
         */
        public void warning(SAXParseException spe) throws SAXException {
            out.println( getParseExceptionInfo("Warning: ", spe));
        }

        /**
         * The following methods are standard SAX ErrorHandler methods.
         * See SAX documentation for more info.
         * @param spe - exception instance
         */
        public void error(SAXParseException spe) throws SAXException {
            throw new SAXException( getParseExceptionInfo("Error: ", spe) );
        }

        /**
         * The following methods are standard SAX ErrorHandler methods.
         * See SAX documentation for more info.
         * @param spe - exception instance
         */
        public void fatalError(SAXParseException spe) throws SAXException {
            throw new SAXException( getParseExceptionInfo("Fatal Error: ", spe) );
        }
    }

    /**
     * Looks for a text child node and returns its value.
     *
     * @param tag - XML element
     * @return - the text String of the tag
     */
    public static String getText(final Element tag)
    {
        if ( tag == null )
            return null;

        NodeList     lst = tag.getChildNodes();
        StringBuffer buf = new StringBuffer();

        for ( int Index = 0, Cnt = lst.getLength(); Index < Cnt; Index++ )
        {
            if ( lst.item(Index).getNodeType() == Node.ENTITY_REFERENCE_NODE)
            {
                buf.append(lst.item(Index).getChildNodes().item(0).getNodeValue());
            }
            if ( (lst.item(Index).getNodeType() == Node.TEXT_NODE) ||
               (lst.item(Index).getNodeType() == Node.CDATA_SECTION_NODE) )
            {
               buf.append( lst.item(Index).getNodeValue() );
            }
        }

        if ( buf.length() == 0 )
            return null;
        else
            return buf.toString();
    }

    /**
     * Get XML document from XML string
     * @param xml - XML String
     * @return - a XML document
     */
    public static Document getDocumentFromString( String xml)
    {
        return getDocumentFromString( xml, false );
    }

    /**
     * Get XML document from XML string
     * @param xml - XML String
     * @param vflag - validation flag
     * @return - a XML document
     */
    public static Document getDocumentFromString( String xml, boolean vflag)
    {
        StringReader sr = new StringReader( xml );
        return XMLUtils.getDocument( new InputSource( sr ), vflag );
    }

    /**
     * This method converts the DOM document into the XML String that can be
     * saved to the file or database.
     * @param doc Document object.
     * @return XML string.
     */
    public static String toXmlString(Document doc) {
      return new String(toXmlBytes(doc));
    }

    /**
     * This method returns the xml byte array from the DOM document.
     * @param doc The xml document.
     * @return The byte array contain the xml data.
     */
    public static byte[] toXmlBytes(Document doc) {
      try {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputStream);
        transformer.transform(source, result);
        return outputStream.toByteArray();
      }
      catch (Exception e) {
        throw new DataBridgeRuntimeException("Document to XML string converting error.", e);
      }
    }

    /**
     * This method returns the child element with the particular name.
     * @param parentElement The parent element.
     * @param tagName The child element name.
     * @return The child element.
     */
    public static Element getChildElement(Element parentElement, String tagName) {
      NodeList nodes = parentElement.getElementsByTagName(tagName);
      if ( nodes != null && nodes.getLength() != 0 ) {
        return (Element)nodes.item(0);
      }
      else {
        return null;
      }
    }

    /**
     * This method returns the child element text.
     * @param parentElement The parent element.
     * @param tagName The child element name.
     * @return The text contained in the child element.
     */
    public static String getChildElementText(Element parentElement, String tagName) {
      Element element = getChildElement(parentElement, tagName);
      if ( element != null ) {
        return getText(element);
      }
      else {
        return null;
      }
    }

    /**
     * This method returns the first element from the document.
     * @param document The document.
     * @param tagName The element name.
     * @return The element object.
     */
    public static Element getFirstElement(Document document, String tagName) {
      NodeList nodes = document.getElementsByTagName(tagName);
      if ( nodes != null && nodes.getLength() != 0 ) {
        return (Element)nodes.item(0);
      }
      else {
        return null;
      }
    }

    /**
     * This method returns the first element text from the document.
     * @param document The document.
     * @param tagName The element name.
     * @return The text contained in the first element.
     */
    public static String getFirstElementText(Document document, String tagName) {
      Element element = getFirstElement(document, tagName);
      if ( element != null ) {
        return getText(element);
      }
      else {
        return null;
      }
    }

    /**
     * Find the xpath element under the given root. As the xpath requirement is very
     * simple, so we avoid using XPath API
     * @param root - the root element that search begins
     * @param xpath - the path from the root
     * @return - the xpath defined element under the given root.
     */
    public static Element findXPathElement( Element root, String xpath )
    {
        if ( root == null )
            return null;
        if ( xpath == null || xpath.trim().equals("") )
            return root;
        xpath = toRelativePath( xpath, root.getNodeName() );
        StringTokenizer st = new StringTokenizer( xpath, "/", false );
        String sitem;
        Element item =  root ;
        NodeList list;

        boolean first = true;
        while ( st.hasMoreTokens() )
        {
            sitem = st.nextToken();
            if ( first && sitem.equals( item.getNodeName() ) )
            {
                first = false;
            }
            else
            {
                list = item.getElementsByTagName( sitem );
                if ( list.getLength() < 1 )
                    return null;
                item = (Element) ( list.item(0) );
            }
        }

        return item;
    }

    /**
    * Sets the Node n's text value to that of s.
    * @param Node - the node whose text will be set.
    * If that node isn't a text node. It will set the first childs that is a text node.
    * The rest will be ignored
    * @param String - s the text that will be set to the node.
    */
    public static void setNodeText(Node n, String s)
    {
        if( n == null || s == null || s.length() == 0)
        {
            System.err.println("An argument is null");
            return;
        }

        if ( n.getNodeType() == Node.TEXT_NODE )
        {
            n.setNodeValue(s);
            return;
        }

        NodeList nl = n.getChildNodes();
        for( int i = 0; i < nl.getLength(); i++ )
        {
            Node cn = nl.item(i);
            if( cn == null )
                continue;
            if( cn.getNodeType() == Node.TEXT_NODE ){
                cn.setNodeValue(s);
                break;
            }
        }
    }

    /**
     * Cut xpath to relative path beginning from the node
     * @param xpath - xpath definition
     * @param node - beginning node
     * @return - the cutted xpath
     */
    private static String toRelativePath( String xpath, String node )
    {
        if ( node == null || node.trim().equals("") )
            return xpath;

        if ( xpath.equals( node ) )
            return xpath;
        else if ( xpath.startsWith( node ) )
            return xpath;
        else if (  xpath.startsWith( "/" + xpath ) )
            return xpath.substring(1);
        else
        {
            int place = xpath.indexOf( "/" + node + "/" );
            if ( place != -1 )
            {
                return xpath.substring( place + 1 );
            }
        }

        return xpath;
    }

/*
    public static void main(String[] args) throws Exception {
      Document doc = DocumentBuilderFactory.
        newInstance().newDocumentBuilder().parse(XMLUtils.class.getResourceAsStream("web.xml"));
      System.out.println(toXmlString(doc));
    }
*/
}