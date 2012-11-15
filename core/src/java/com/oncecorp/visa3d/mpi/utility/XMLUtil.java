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

package com.oncecorp.visa3d.mpi.utility;

import com.ibm.dom.util.XPathCanonicalizer;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

/**
 * Description: Utility class provides convenience methods of XML operations.
 * 
 * @version 0.1 July 08, 2002
 * @author	Alan Zhang
 */
public class XMLUtil {

	/** Namespaces feature id (http://xml.org/sax/features/namespaces). */
	protected static final String NAMESPACES_FEATURE_ID =
		"http://xml.org/sax/features/namespaces";

	/** Validation feature id (http://xml.org/sax/features/validation). */
	protected static final String VALIDATION_FEATURE_ID =
		"http://xml.org/sax/features/validation";

	/** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
	protected static final String SCHEMA_VALIDATION_FEATURE_ID =
		"http://apache.org/xml/features/validation/schema";

	/** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
	protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID =
		"http://apache.org/xml/features/validation/schema-full-checking";

	/**
	 * Create an empty DOM document object. Note: essentially the empty DOM object is not
	 * a valid object. Append root element before doing any real operation against returned
	 * ojbect
	 * 
	 * @return Document object or null
	 */
	public static Document createDocument() {
		try {
			DocumentBuilder builder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			return doc;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Parse XML string into Document object WITHOUT valisation
	 * 
	 * @param xmlStr The XML String to parse
	 * @return Document object or null
	 */
	public static Document createDocument(String xmlStr) throws Exception {
		final boolean VALIDATION_OFF = false;

		//Sanity check
		if ((xmlStr == null) || (xmlStr.length() == 0))
			return null;

		//Create parser			
		DOMParser parser = new DOMParser();

		//Switch validation
		parser.setFeature(VALIDATION_FEATURE_ID, VALIDATION_OFF);
		parser.setErrorHandler(new ValidityErrorHandler());

		//Create underlying stream
		StringReader sr = new StringReader(xmlStr);

		//Parsing
		parser.parse(new InputSource(sr));

		//return Document
		return parser.getDocument();
	}

	/**
	 * Parse XML string into Document object
	 * 
	 * @param xmlStr The XML String to parse
	 * @param validate The flag indicates whether implementing XML Parser validation
	 * @return Document object or null
	 * @link createDocument(String xmlStr)
	 */
	public static Document createDocument(String xmlStr, boolean validate)
		throws Exception {
		/* 
		 * [Martin's Note: Oct 1, 2002 9:12:10 AM]
		 * Validation of XML document with corresponding DTD has been forced off.
		 * This method forces the use of the simpler createDocument(String) with validation off...
		 */
		//return Document
		return createDocument(xmlStr);
	}

	/**
	 * Parse XML string into Document object with Schema Validation
	 * 
	 * @param xmlStr The XML String to parse
	 * @param schemaValidation The flag indicates Schema Validation
	 * @return Document object or null 
	 */
	/** Since we don't have Xerces 2 any more.
	public static Document createDocumentWithSchemaValidation(
		String xmlStr,
		boolean validate)
		throws Exception {
		//Sanity check
		if ((xmlStr == null) || (xmlStr.length() == 0))
			return null;
	
		//Create parser			
		DOMParser parser = new DOMParser();
	
		//Set features
		parser.setFeature(VALIDATION_FEATURE_ID, validate);
		parser.setFeature(NAMESPACES_FEATURE_ID, validate);
		parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, validate);
		parser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, validate);
	
		//Create underlying stream
		StringReader sr = new StringReader(xmlStr);
	
		//Parsing
		parser.parse(new InputSource(sr));
	
		//return Document
		return parser.getDocument();
	}
	*/

	/**
	 * Convert DOM object into a canonicalize XML string 
	 * 
	 * @param doc DOM object to be converted
	 * @return XML String
	 */
	public static String toXmlString(Document doc) {
		try {
			//Create format
			OutputFormat format = new OutputFormat(doc);

			//Create string writer
			StringWriter stringOut = new StringWriter();
			/* 
			 * [Martin's Note: Sep 16, 2002 4:28:22 PM]
			 * Need to use XPAthCanonicaliser because of problem with XML signature
			 * that was not validating properly (inserted lf/ cr etc....)
			 */
			//			//Create serializer
			//			XMLSerializer serial = new XMLSerializer(stringOut, format);
			//
			//			//As a DOM Serializer
			//			serial.asDOMSerializer();
			//
			//			//Serialize DOM
			//			serial.serialize(doc.getDocumentElement());
			XPathCanonicalizer.serializeAll(doc, true, stringOut);

			//Spit out DOM as a String
			return stringOut.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;

	}

	/**
	`	 * Convert DOM object into a XML string indicated either canonical or regular
	 * 
	 * @param doc DOM object to be converted
	 * @param isCanonical Indicate if conversion needs to be canonicalized or not
	 * @return XML String
	 */
	public static String toXmlString(Document doc, boolean isCanonical) {
		try {
			// Want canonical version, use toXMLString(Document) instead
			if (isCanonical) {
				return toXmlString(doc);
			} else {
				// Return XML string using normal XMLSerializer

				//Create format
				OutputFormat format = new OutputFormat(doc);

				//Create string writer
				StringWriter stringOut = new StringWriter();

				//Create serializer
				XMLSerializer serial = new XMLSerializer(stringOut, format);

				//As a DOM Serializer
				serial.asDOMSerializer();

				//Serialize DOM
				serial.serialize(doc.getDocumentElement());

				//Spit out DOM as a String
				return stringOut.toString();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;

	}

	/**
	 * Find first node under given XPath expression. XPath 1.0 is supported. 
	 * XPath namespace prefixes are resolved from the contextNode.
	 * 
	 * If you have a DOM object with structure like:
	 * 	<root>
	 * 		<person>
	 * 			<name>Alan Zhang</name>
	 * 		</person>
	 * 	</root>
	 * 
	 * Then, XPath expression "/root/person/name" will locate the "name" element
	 * under "root/person". 
	 * 
	 * 
	 * @param node The node to start searching from
	 * @param xpathStr A valid XPath string
	 * @return First found node or null
	 */
	public static Node getFirstNodeByXPath(Node node, String xpathStr) {
		try {
			Node n = XPathAPI.selectSingleNode(node, xpathStr);
			if (n != null) {
				return n;
			}
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Find all nodes under given XPath expression. XPath 1.0 is supported. 
	 * XPath namespace prefixes are resolved from the contextNode.
	 * 
	 * @param node The node to start searching from
	 * @param xpathStr A valid XPath string
	 * @return NodeList or null
	 */
	public static NodeList getNodeListByXPath(Node node, String xpathStr) {
		try {
			//Use XPath string to select a nodelist
			NodeList nl = XPathAPI.selectNodeList(node, xpathStr);
			return nl;
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get value of node specified by XPath expression
	 * 
	 * @param node The node to start searching from
	 * @param xpathStr A valid XPath string
	 * @return Node value or null
	 */
	public static String getValueByXPath(Node node, String xpathStr) {
		try {
			//Use XPath string to select a nodelist
			NodeIterator nl = XPathAPI.selectNodeIterator(node, xpathStr);
			Node n;
			if ((n = nl.nextNode()) != null) {
				if (isTextNode(n)) {
					// DOM may have more than one node corresponding to a 
					// single XPath text node.  Coalesce all contiguous text nodes
					// at this level
					StringBuffer sb = new StringBuffer(n.getNodeValue());
					for (Node nn = n.getNextSibling();
						isTextNode(nn);
						nn = nn.getNextSibling()) {
						sb.append(nn.getNodeValue());
					}
					return sb.toString();
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	/** 
	 * Decide if the node is text.
	 * 
	 * @param n Node to check
	 * @return True if node is text; otherwise retur false
	 */
	public static boolean isTextNode(Node n) {
		//Sanity check
		if (n == null)
			return false;

		//Get node type
		short nodeType = n.getNodeType();

		//Check type and return result
		return nodeType == Node.CDATA_SECTION_NODE
			|| nodeType == Node.TEXT_NODE;
	}

	/**
	 * Resolve reserved XML characters
	 *  
	 */
	public static String filterSpecialChars(String in) {
		if (in == null)
			return null;

		StringBuffer sb = new StringBuffer();
		char[] chars = in.toCharArray();

		for (int i = 0; i < chars.length; i++) {

			switch (chars[i]) {
				case '<' :
					sb.append("&lt;");
					break;

				case '>' :
					sb.append("&gt;");
					break;

				case '&' :
					sb.append("&amp;");
					break;

				default :
					sb.append(chars[i]);
			}
		}

		return sb.toString();
	}

	/**
	 * filters NULL value
	 * @param in The string
	 * @return True if string is null
	 */
	public static boolean isNull(String in) {
		if ((in == null) || (in.length() == 0))
			return true;
		else
			return false;
	}

}
