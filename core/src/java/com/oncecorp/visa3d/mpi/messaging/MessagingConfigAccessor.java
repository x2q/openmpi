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

import com.oncecorp.visa3d.mpi.configuration.ConfigAccessor;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.meta.BindingMetaInfo;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageMetaInfo;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageUID;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Description: This class implements ConfigAccessor interface to actually
 * load/update Messaging Mapping data from/to file/CMP for caller's
 * request
 *
 * @version 0.1 July 20, 2002
 * @author	Jun Shi
 */

public class MessagingConfigAccessor implements ConfigAccessor {

	// init a flag
	private static boolean initialized = false;

	// init mapping repository
	private static HashMap allConfigData;

	// local Log4J logger
	private static Logger logger = MPILogger.getLogger(MessagingConfigAccessor.class.getName());

	/** ConvertionBindings Definition */
	private static final String MESSAGE_DEFINITION_FILE	= "MessageDefinition.xml";

	// Element: Binding
	private final static String MESSAGE_TYPE_XMLATTR 	= "type";
	private final static String MESSAGE_VERSION_XMLATTR = "version";

	private final static String ENTRY_PATH 		= "path";
	private final static String ENTRY_ATTRIBUTE = "attribute";
	private final static String ENTRY_TYPE		= "type";
	private final static String ENTRY_JAVATYPE	= "javatype";

	private final static String MESSAGE_DEFINITION_XMLPATH 	= "/messageDefinition/message";
	private final static String MESSAGE_BINDING_XMLPATH 	= "./bindings/entry";
	private final static String MESSAGE_TRANSFORMER_XMLPATH = "./transformers/transform";
	private final static String MESSAGE_EXTENSION_XMLPATH 	= "./extensions/extension";

	/**
	 * [MessagingConfigAccessor] implements two public methods in [ConfigAccessor]
	 * interface, i.e. [.getConfigData] and [.setConfigData]. Current version,
	 * [MessagingConfigAccessor.getConfigData] loads data from [MessageDefinition.xml]
	 * and stores both "MessageMapping" and "ProcessorMapping"  etc. information in
	 * "allConfigData:HashMap" and returns as Serializable.
	 *
	 * @param none
	 * @return Serializable (allConfigData)
	 */
	public Serializable getConfigData() {
		/** loading once */
		if (! initialized) {
			logger.debug("First Time initial starting ...");
			return initialize();
		}
		else {
			logger.debug("Initial already done before, Just Return allConfigData...");
			return allConfigData;
		}
	}

	private HashMap initialize() throws FactoryConfigurationError {
		// init mapping repository
		allConfigData = new HashMap();

		// loading the message definition file
		// The class loading mechanisn in Weblogic and in Tomcat is different
		// so we have to try two strategies to load that configuration file
		InputStream is = Thread.currentThread().getContextClassLoader()
				 .getResourceAsStream(MESSAGE_DEFINITION_FILE);

		// Try with the second strategy
		if (is == null) {
			is = this.getClass().getResourceAsStream(MESSAGE_DEFINITION_FILE);
		}

		// Still not working then we need to exit from here....
		if (is == null) {
			logger.debug("Unable to find " + MESSAGE_DEFINITION_FILE);
			throw new RuntimeException("Unable to read the message definition file as a resource");
		}
		else {
			logger.debug("[MessageDefinition.xml] is loaded properly");
		}

		/** create DOM object for [MessageDefinition.xml] */
		Element root = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);

			// do logging
			logger.debug("Building DOM of [MessageDefinition.xml] completed!!");

			// get root element
			root = doc.getDocumentElement();
		}
		catch (Exception e) {
			logger.error("[.getConfigData] loading [MessageDefinition.xml] failed!!", e);
			e.printStackTrace();
		}

		/** loading Message Mapping */
		NodeList msgDef = XMLUtil.getNodeListByXPath(root, MESSAGE_DEFINITION_XMLPATH);
		if ((msgDef == null) || (msgDef.getLength() == 0)) {
			logger.debug("NO Message Mapping Information Found!!!");
		}

		// Load message definition
		Element currentMsgDef;
		for (int i = 0; i < msgDef.getLength(); i++) {
			MessageMetaInfo metaInfo = new MessageMetaInfo();
			currentMsgDef = (Element) msgDef.item(i);

			// Retrieve message definition items
			String name = currentMsgDef.getAttribute("name");
			String version = currentMsgDef.getAttribute("version");
			String impl = currentMsgDef.getAttribute("impl");
			String processor = currentMsgDef.getAttribute("processor");
			String protocol = currentMsgDef.getAttribute("protocol");
			metaInfo.setName(name);
			metaInfo.setSupportedVersion(parseSupportedVersion(version));
			metaInfo.setImpl(impl);
			metaInfo.setProcessor(processor);
			metaInfo.setProtocol(protocol);

			// Load transformer definition
			loadTransformerDefinition(currentMsgDef, metaInfo);

			// Load binding definition
			loadBindingDefinition(currentMsgDef, metaInfo);

			// Load extension definitions
			loadExtensionDefinition(currentMsgDef, metaInfo);

			// Now we stored a metainfo object per message name/version
			String msgName = metaInfo.getName();
			ArrayList versions = metaInfo.getSupportedVersion();
			for (int j = 0; j < versions.size(); j++) {
				String msgVersion = (String) versions.get(j);

				allConfigData.put(new MessageUID(msgName, msgVersion), metaInfo);
			}
		}

	   // set flag
       initialized = true;

       //do logging
       logger.debug("Messaging Definition loading finished");

	   // return to caller
	   return allConfigData;
	}

	/**
	 * Parse version string and extract all supported versions.
	 * @param version		All supported versions separated by commas
	 * @return ArrayList	Each item is the version number
	 */
	private ArrayList parseSupportedVersion(String version) {
		StringTokenizer tokenizer = new StringTokenizer(version, ", ");
		ArrayList versions = new ArrayList();

		// Extract all version numbers separated by ';'
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token != null) {
				versions.add(token);
			}
		}
		logger.debug("Version supported information:" + versions.toString());
		return versions;
	}

	private void loadExtensionDefinition(Element root, MessageMetaInfo metaInfo) {
	}

	private void loadBindingDefinition(Element supportElement, MessageMetaInfo metaInfo) {
		NodeList nl;
		Element current;
		BindingMetaInfo bInfo = null;

		/** loading ConvertionBindings Mappings */
		nl = XMLUtil.getNodeListByXPath(supportElement, MESSAGE_BINDING_XMLPATH);

		if ((nl == null) || (nl.getLength() == 0)) {
			logger.debug("NO binding information Found!!!");
		}

		try {
			bInfo = this.getDomToMessageMapping(nl);
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.debug("loading ConvertionBindings Mapping Info Exception !!");
		}
		metaInfo.setBindings(bInfo);
	}

	private void loadTransformerDefinition(Element messageElement, MessageMetaInfo metaInfo) {
		NodeList nl;
		Element current;
		HashMap transformers = new HashMap();

		// Loading Transformer Mapping info
		nl = XMLUtil.getNodeListByXPath(messageElement, MESSAGE_TRANSFORMER_XMLPATH);
		if ((nl == null) || (nl.getLength() == 0)) {
			logger.debug("NO Transformer Mapping Information Found!!!");
		}

		for (int i = 0; i < nl.getLength(); i++) {
			current = (Element) nl.item(i);

			String toMessage = current.getAttribute("to");
			String toVersion = current.getAttribute("version");
			String impl = current.getAttribute("impl");

			// Create messageUID and use it as key to insert proper
			// transformer implementation
			MessageUID uid = new MessageUID(toMessage, toVersion);
			transformers.put(uid, impl);
		}
		// Update meta info with new transformer structure
		metaInfo.setTransformers(transformers);
	}

	// Actual loading ConvertionBindings method */
	private BindingMetaInfo getDomToMessageMapping(NodeList nl) throws Exception {
		//Instantial mapping HashMap
		HashMap domToMessageMapping = new HashMap();
		int nbEntry = nl.getLength();

		//Prepare convertion binding
		BindingMetaInfo bInfo = new BindingMetaInfo();
		String[] paths = new String[nbEntry];
		String[] attributes = new String[nbEntry];
		String[] types = new String[nbEntry];
		String[] javatypes = new String[nbEntry];

		//Get bindings
		for (int i = 0; i < nl.getLength(); i++) {
			//Get individual binding
			Element entry = (Element) nl.item(i);

			//Get attribute: path
			paths[i] = entry.getAttribute(ENTRY_PATH);

			//Get attribute: attribute
			attributes[i] = entry.getAttribute(ENTRY_ATTRIBUTE);

			//Get attribute: type
			types[i] = entry.getAttribute(ENTRY_TYPE);

			//Get attribute: javatype
			javatypes[i] = entry.getAttribute(ENTRY_JAVATYPE);
		}

		//Set up BindingMetaInfo
		bInfo.setPaths(paths);
		bInfo.setAttributes(attributes);
		bInfo.setTypes(types);
		bInfo.setJavatypes(javatypes);

		return bInfo;
	}

	// To be implemented
	public boolean setConfigData(Serializable data) {
		logger.debug("[MessagingConfigAccessor.setConfigData] called");
		boolean isSuccess = true;
		return isSuccess;
	}


	/**
	 * Retrieve all messageMetaInfo defined for a specific protocol. In order to retrieve all messages defined
	 * use "ALL" as the parameter value.
	 *
	 * @param aProtocol	Protocol can be either "3DSecure", "MPI_Interface" or "ALL"
	 * @return Collection
	 */
	public Vector getAllMessageInfo(String aProtocol) throws MessagingException {
		// Check for null protocol which is invalid
		if (aProtocol == null) {
			throw new MessagingException("Null parameter supplied for getAllMessageInfo()");
		}

		Vector resultingSet = new Vector();
		Iterator it = ((HashMap) getConfigData()).keySet().iterator();
		while (it.hasNext()) {
			MessageUID uid = (MessageUID) it.next();
			MessageMetaInfo info = (MessageMetaInfo) allConfigData.get(uid);
			if (aProtocol.equalsIgnoreCase(MessageMetaInfo.MESSAGE_PROTOCOL_ALL) ||
				info.getProtocol().equalsIgnoreCase(aProtocol)) {
				resultingSet.add(uid);
			}
		}
		return resultingSet;
	}
}