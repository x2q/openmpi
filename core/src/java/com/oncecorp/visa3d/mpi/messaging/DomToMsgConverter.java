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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.xerces.validators.schema.XUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oncecorp.visa3d.mpi.domain.CardRange;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.meta.BindingMetaInfo;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageMetaInfo;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageUID;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

/**
 * DOM Document to Message converter. This class enables the conversion of
 * XML DOM object to an associated Java Message instances with all the proper
 * attributes and properties correctly initialized.
 *
 * The content of the MessageDefinition.xml file found within the package gives the
 * proper direction as to the mechanism and structure of each message. XPath
 * expressions are used to properly extract XML element and method reflection and
 * invocation are then executed to set all the proper attributes.
 *
 * @version $Revision: 53 $
 * @author	Alan Zhang
 *
 * Patented optimization strategies by
 * @author Martin Dufort (mdufort@oncecorp.com)
 */
public class DomToMsgConverter {
	/**
	 * Definition of all the binding types
	 */
	public final static String BINDING_TYPE_SIMPLE = "simple";
	public final static String BINDING_TYPE_COMPLEX = "complex";
	public final static String BINDING_TYPE_PROFILE = "profile";
	public final static String BINDING_TYPE_EXTENSION = "extension";
	public final static String BINDING_TYPE_ATTRVALUE = "attr";
	public final static String BINDING_TYPE_PROFILE_REQUEST = "profileRequest";
	public final static String BINDING_TYPE_SIGNATURE = "signature";
	public final static String BINDING_TYPE_CR = "cr";

	/**
	 * Local Log4J logger
	 */
	protected static Logger logger = MPILogger.getLogger(DomToMsgConverter.class.getName());

	/**
	 * Caching of method instances to imporve performance
	 */
	private static HashMap messageMethodCache = new HashMap();

	/**
	 * VISA 3-D message root
	 */
	protected final static String THREED_SECURE = "ThreeDSecure";

	/**
	 * MPI Interface message root
	 */
	protected final static String MPI_INTERFACE = "MPI_Interface";

	/**
	 * Element version
	 */
	protected final static String MSG_VERSION = "version/text()";

	/**
	 * MPI Message Attribute id
	 */
	protected final static String MPI_MSG_ID = "id";

	/**
	 * ThreeDSecure Message Attribute id
	 */
	protected final static String THREED_MSG_ID = "id";

	/**
	 * Message Extension attribute: critical
	 */
	protected final static String EXTENSION_CRITICAL = "critical";

	/**
	 * Message Signature tag name
	 */
	protected final static String SIGNATURE_TAG_NAME = "Signature";

	/**
	 * ProfileName element
	 */
	protected final static String PROFILE_NAME = "profileName/text()";

	/**
	 * ProfileScope element
	 */
	protected final static String PROFILE_SCOPE = "profileData/profileScope";

	/**
	 * Profile scopeid attribute
	 */
	protected final static String PROFILE_SCOPE_ID = "scopeid";

	/**
	 * ProfileDataItem element
	 */
	protected final static String PROFILE_DATA_ITEM = "profileDataItem";

	/**
	 * Profile uid attribute
	 */
	protected final static String PROFILE_UID = "uid";

	/**
	 * ProfileRequestItem element
	 */
	protected final static String PROFILE_REQUEST_ITEM = "profileRequestItem";

	/**
	 * Defined 3D-Secure message elements
	 */
	protected static Vector MSG_ELEMENTS;

	/**
	 * Mapping of defined message elements
	 * <ul>
	 * <li>key: element</li>
	 * <li>value: defined elements for key</li>
	 * </ul>
	 */
	protected static HashMap MSG_MAPPING;

	static {
		//Initialize MSG_ELEMENTS and MSG_MAPPING
		MSG_ELEMENTS = new Vector();
		MSG_ELEMENTS.add("CRReq");
		MSG_ELEMENTS.add("CRRes");
		MSG_ELEMENTS.add("VEReq");
		MSG_ELEMENTS.add("VERes");
		MSG_ELEMENTS.add("PAReq");
		MSG_ELEMENTS.add("PARes");
		MSG_ELEMENTS.add("Error");

		//Only CRRes, VERes, PARes, Error element mapping defined
		MSG_MAPPING = new HashMap();

		Vector temp = new Vector();
		temp.add("version");
		temp.add("CR");
		temp.add("serialNumber");
		temp.add("IReq");
		MSG_MAPPING.put("CRRes", temp);

		temp = new Vector();
		temp.add("version");
		temp.add("CH");
		temp.add("url");
		temp.add("protocol");
		temp.add("IReq");
		temp.add("Extension");
		MSG_MAPPING.put("VERes", temp);

		temp = new Vector();
		temp.add("version");
		temp.add("Merchant");
		temp.add("Purchase");
		temp.add("pan");
		temp.add("TX");
		temp.add("IReq");
		temp.add("Extension");
		MSG_MAPPING.put("PARes", temp);

		temp = new Vector();
		temp.add("version");
		temp.add("errorCode");
		temp.add("errorMessage");
		temp.add("errorDetail");
		temp.add("vendorCode");
		MSG_MAPPING.put("Error", temp);
	}

	/**
	 * Convert DOM document to MPI message.
	 */
	public Message convert(Document doc) throws Exception {

		//Prepare message id, type & version
		String msgId = null;
		String msgType = null;
		String msgVersion = null;
		Element root = null, e = null;

		if (doc == null) {
			throw new MessagingException(
				"UNKNOWN",
				ErrorCodes.ERROR_CODE_1,
				ErrorCodes.ERROR_MESSAGE_1,
				"ThreeDSecure",
				"Root element missing.",
				"Root element missing.");
		}

		//Get message root
		root = doc.getDocumentElement();

		//Get root name
		String name = root.getTagName();
		logger.debug("Message root element: " + name);

		try {
			if (name.equalsIgnoreCase(DomToMsgConverter.THREED_SECURE)) {
				//Get 'Message' element
				e = (Element) XMLUtil.getFirstNodeByXPath(root, "Message");
				if (e == null) {
					logger.error("Message element missing.");
					throw new MessagingException(
						"UNKNOWN",
						ErrorCodes.ERROR_CODE_3,
						ErrorCodes.ERROR_MESSAGE_3,
						"Message",
						"Message element missing.",
						"Message element missing..");
				}

				//Get message id
				if (e.getAttributeNode(THREED_MSG_ID) == null) {
					//id attribute missing
					logger.error("Message missing id attribute.");
					throw new MessagingException(
						"UNKNOWN",
						ErrorCodes.ERROR_CODE_3,
						ErrorCodes.ERROR_MESSAGE_3,
						"Message.id",
						"Message missing id attribute.",
						"Message missing id attribute.");
				}

				msgId = e.getAttribute(THREED_MSG_ID);
				if ((msgId == null) || (msgId.length() == 0)) {
					//no value
					throw new MessagingException(
						"UNKNOWN",
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"Message.id",
						"Message has id attribute with no value.",
						"Message has id attribute with no value.");
				}

				//No checking for undefined elements
				//checkUndefinedElements(e);

				//Get message type element
				//e = XUtil.getFirstChildElement(e);

				//Check first child of Message element
				e = findDefinedMessageElement(msgId, e);

				//Get message type
				msgType = e.getTagName();

				//Get message version
				msgVersion = XMLUtil.getValueByXPath(e, MSG_VERSION);
				if (msgVersion == null) {
					//id attribute missing
					logger.error("Message missing version element.");
					throw new MessagingException(
						msgId,
						ErrorCodes.ERROR_CODE_3,
						ErrorCodes.ERROR_MESSAGE_3,
						"version",
						"version element missing.",
						"version element missing.");

				}
				else {
					if (msgVersion.length() == 0) {
						//no value
						throw new MessagingException(
							msgId,
							ErrorCodes.ERROR_CODE_5,
							ErrorCodes.ERROR_MESSAGE_5,
							"version",
							"version element has no value.",
							"version element has no value.");
					}
				}

			}
			else if (name.equalsIgnoreCase(DomToMsgConverter.MPI_INTERFACE)) {
				//Get message type element
				e = XUtil.getFirstChildElement(root);

				//Get message id
				msgId = e.getAttribute(MPI_MSG_ID);
				if ((msgId == null) || (msgId.length() == 0)) {
					//no value
					throw new MessagingException(
						"UNKNOWN",
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"id",
						"MPI_Interface message has id attribute with no value.",
						"MPI_Interface message has id attribute with no value.");
				}

				//Get message type
				msgType = e.getTagName();

				//Get message version
				msgVersion = XMLUtil.getValueByXPath(e, MSG_VERSION);
				if (msgVersion == null) {
					//id attribute missing
					logger.error("Message missing version element.");
					throw new MessagingException(
						msgId,
						ErrorCodes.ERROR_CODE_3,
						ErrorCodes.ERROR_MESSAGE_3,
						"version",
						"version element missing.",
						"version element missing.");

				}
				else {
					if (msgVersion.length() == 0) {
						//no value
						throw new MessagingException(
							msgId,
							ErrorCodes.ERROR_CODE_5,
							ErrorCodes.ERROR_MESSAGE_5,
							"version",
							"version element has no value.",
							"version element has no value.");
					}
				}
			}
			else {
				logger.error("Root element of message is not recognized.");
				throw new MessagingException(
					"UNKNOWN",
					ErrorCodes.ERROR_CODE_1,
					ErrorCodes.ERROR_MESSAGE_1,
					name,
					"Root element:" + name + "is not recognized.",
					"Root element:" + name + "is not recognized.");
			}
		}
		catch (NullPointerException npe) {
			logger.error("Element missed.", npe);
			throw new MessagingException(
				msgId,
				ErrorCodes.ERROR_CODE_3,
				ErrorCodes.ERROR_MESSAGE_3,
				"Element missed. Message id: " + msgId + ", type: " + msgType + ": version: " + msgVersion,
				"Element missed",
				npe.getMessage());
		}

		//Logging
		logger.debug("Message ID: " + msgId);
		logger.debug("Message Type: " + msgType);
		logger.debug("Message Version: " + msgVersion);

		return makeConversion(doc, msgId, msgType, msgVersion);
	}

	/**
	 * This method performs the actual conversion from DOM to the Java instance.
	 *
	 * @param doc			The XML document we are converting from
	 * @param msgId			The messageID of this request
	 * @param msgType		The message type associated with this conversion
	 * @param msgVersion	The version that we must used, if supported
	 * @return Message		Return the appropriate Java instance message
	 * @throws MessagingException
	 */
	private Message makeConversion(Document doc, String msgId, String msgType, String msgVersion)
		throws MessagingException {
		// Check support for specific message type/version
		MessageUID msgUID = new MessageUID(msgType, msgVersion);
		if (!MessageMetaInfo.isVersionSupported(msgUID)) {
			throw new MessagingException(
				msgId,
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"version",
				"Version: " + msgVersion + " is not supported by this software release.",
				"[MessageType:"
					+ msgType
					+ "] Version: "
					+ msgVersion
					+ " is not supported by this software release.)");
		}

		// Get message binding
		BindingMetaInfo binding = MessageMetaInfo.getBindingInfo(msgUID);
		String[] paths = binding.getPaths();
		String[] attributes = binding.getAttributes();
		String[] types = binding.getTypes();
		String[] javatypes = binding.getJavatypes();

		// Check binding info
		if ((paths == null) || (attributes == null) || (paths.length != attributes.length)) {
			logger.error("Message binding error.");
			throw new MessagingException("Binding error.");
		}

		//Get new message
		Message msg = MessageGenerator.create(msgType, msgVersion);

		//Set id first
		msg.setId(msgId);

		//Get underlying message class
		Class msgClass = msg.getClass();

		// Check if we have already cached the proper method information
		HashMap methodCache = (HashMap) messageMethodCache.get(msgUID);
		if (methodCache == null) {
			// No caching info. Saving it for next time
			methodCache = new HashMap();
			for (int i = 0; i < paths.length; i++) {
				String pathKey = paths[i];
				Method setterMethod = extractMethod(msgClass, paths[i], attributes[i], types[i], javatypes[i]);
				if (setterMethod == null) {
					// If method was not found, we cannot continue
					logger.error("Unable to located proper setter method information.");
					throw new MessagingException("Unable to located proper setter method information.");
				}

				// Add the proper setter method for the corresponding path
				methodCache.put(pathKey, setterMethod);
			}

			// Assigned all method caches to proper methodCache structure
			messageMethodCache.put(msgUID, methodCache);
		}


		//Set message attribute values
		Method setterMethod = null;
		Object[] arguments;
		for (int i = 0; i < paths.length; i++) {
			try {
				logger.debug("Entry[" + i + "]: ");

				//Get argument value
				Object value = getValue(doc, paths[i], types[i]);
				logger.debug("  value: " + value);

				// First fetch the proper method from the cache area
				setterMethod = (Method) methodCache.get(paths[i]);

				// then invoke method
				arguments = new Object[] { value };
				setterMethod.invoke(msg, arguments);
			}
			catch (NullPointerException npe) {
				logger.error("Element missed.", npe);
				throw new MessagingException(
					msgId,
					ErrorCodes.ERROR_CODE_3,
					ErrorCodes.ERROR_MESSAGE_3,
					"Element missed. Message id: "
						+ msgId
						+ ", type: "
						+ msgType
						+ ": version: "
						+ msgVersion,
					"Element missed",
					npe.getMessage());
			}
			catch (MessagingException me) {
				logger.error("MessagingException caught by Converter: ", me);
				throw new MessagingException(
					msgId,
					me.getErrorCode(),
					me.getErrorMsg(),
					me.getErrorDetail(),
					me.getVendorCode(),
					me.getMessage());
			}
			catch (InvocationTargetException ite) {
				if (ite.getTargetException() instanceof MessagingException) {
					MessagingException me = (MessagingException) ite.getTargetException();
					logger.error("Relecting invocation target exception: ", me);
					throw new MessagingException(
						msgId,
						me.getErrorCode(),
						me.getErrorMsg(),
						me.getErrorDetail(),
						me.getVendorCode(),
						me.getMessage());
				}
				else {
					logger.error("Unexpected Converting Error.", ite);
					throw new MessagingException(ite.getMessage());
				}
			}
			catch (Exception exception) {
				logger.error("Unexpected Converting Error.", exception);
				throw new MessagingException(exception.getMessage());
			}
		}

		//We are done.
		return msg;
	}

	/**
	 * Convenient method to get setter method name
	 */
	protected String getSetterName(String attr) {
		char[] charArray = attr.toCharArray();
		charArray[0] = Character.toUpperCase(charArray[0]);
		return ("set" + String.valueOf(charArray));
	}

	private Method extractMethod(Class msgClass, String path, String attribute, String type, String javaType) {
		Method setterMethod = null;
		try {

			//Logging
			logger.debug("  path: " + path);
			logger.debug("  attr: " + attribute);
			logger.debug("  type: " + type);
			logger.debug("  javatype: " + javaType);

			//Set up parameter type
			Class[] parameterTypes = new Class[] { getJavatypeClass(javaType)};

			//Get method
			setterMethod = msgClass.getMethod(getSetterName(attribute), parameterTypes);
		}
		catch (Exception e) {
			return null;
		}

		return setterMethod;
	}

	/**
	 * Convenient method to get setter attribute value
	 *
	 * @param doc The message document
	 * @param xpath The XPath to retrieve element value or nodelist
	 * @param type The binding type. Either "simple" or "complex"
	 * @return The attribute value.
	 */
	protected Object getValue(Document doc, String xpath, String type) throws Exception {
		//Check binding type
		if (type.equalsIgnoreCase(BINDING_TYPE_SIMPLE)) {
			return getSimpleValue(doc, xpath);

		}
		else if (type.equalsIgnoreCase(BINDING_TYPE_COMPLEX)) {
			return getComplexValue(doc, xpath);

		}
		else if (type.equalsIgnoreCase(BINDING_TYPE_PROFILE)) {
			return getProfileValue(doc, xpath);

		}
		else if (type.equalsIgnoreCase(BINDING_TYPE_EXTENSION)) {
			return getExtensionValue(doc, xpath);

		}
		else if (type.equalsIgnoreCase(BINDING_TYPE_ATTRVALUE)) {
			return getAttrValue(doc, xpath);

		}
		else if (type.equalsIgnoreCase(BINDING_TYPE_PROFILE_REQUEST)) {
			return getProfileRequestValue(doc, xpath);

		}
		else if (type.equalsIgnoreCase(BINDING_TYPE_SIGNATURE)) {
			return getSignatureValue(doc, xpath);

		}
		else if (type.equalsIgnoreCase(BINDING_TYPE_CR)) {
			return getCrValue(doc, xpath);

		}
		else {
			logger.error("Unknown binding type.");
			throw new MessagingException("Unknown binding type.");
		}
	}

	/**
	 * Convenient method to get simple type attribute value
	 *
	 * @param doc The message document
	 * @param xpath The XPath to retrieve element value or nodelist
	 * @return The attribute value.
	 */
	private Object getSimpleValue(Document doc, String xpath) throws Exception {
		//Simple type: Get value
		return XMLUtil.getValueByXPath(doc, xpath);
	}

	/**
	 * Convenient method to get complex type attribute value
	 *
	 * @param doc The message document
	 * @param xpath The XPath to retrieve element value or nodelist
	 * @return The String[] object
	 */
	private Object getComplexValue(Document doc, String xpath) throws Exception {
		//Complex type: multiple values could be existed.
		NodeList nl = XMLUtil.getNodeListByXPath(doc, xpath);

		if (nl.getLength() == 0)
			return null;

		//Prepare container
		String[] value = new String[nl.getLength()];

		//Get all the values
		for (int i = 0; i < value.length; i++) {
			value[i] = XMLUtil.getValueByXPath(nl.item(i), "text()");
		}

		//return result
		return value;
	}

	/**
	 * Convenient method to get profile type attribute value
	 *
	 * @param doc The message document
	 * @param xpath The XPath to retrieve element value or nodelist
	 * @return The Profile[] object.
	 */
	private Object getProfileValue(Document doc, String xpath) throws Exception {
		//Profile type: need return an array of type Profile
		NodeList nl = XMLUtil.getNodeListByXPath(doc, xpath);

		if (nl.getLength() == 0)
			return null;

		//Prepare container
		Profile[] value = new Profile[nl.getLength()];
		logger.debug("    Profile no.: " + value.length);

		//Get all the profile values
		for (int i = 0; i < nl.getLength(); i++) {
			//Get Profile element
			Element profileElement = (Element) nl.item(i);

			//Prepare individual profile object
			Profile profile = new Profile();

			//Set attribute values
			profile.setProfileName(XMLUtil.getValueByXPath(profileElement, PROFILE_NAME));

			//Get profile scope elements
			NodeList profileScopes = XMLUtil.getNodeListByXPath(profileElement, PROFILE_SCOPE);
			logger.debug("    Profile[" + i + "] scope no.: " + profileScopes.getLength());

			//Prepare prepare data container (key: scopeid, value: data item collection)
			HashMap profileData = new HashMap();

			//Set profile data
			for (int j = 0; j < profileScopes.getLength(); j++) {
				//Get individual profileScope element
				Element profileScope = (Element) profileScopes.item(j);

				//Get all data item elements
				NodeList profileDataItems = XMLUtil.getNodeListByXPath(profileScope, PROFILE_DATA_ITEM);

				//Prepare data itme container (key: uid, value: data item value)
				HashMap dataItems = new HashMap();
				logger.debug(
					"    Profile[" + i + "] scope[" + j + "] Item no.: " + profileDataItems.getLength());

				//Set data item values
				for (int k = 0; k < profileDataItems.getLength(); k++) {
					//Get individual data item element
					Element profileDataItem = (Element) profileDataItems.item(k);

					//add data item value to container
					dataItems.put(
						profileDataItem.getAttribute(PROFILE_UID),
						XMLUtil.getValueByXPath(profileDataItem, "text()"));
				}

				//Add data item collection to contatiner
				profileData.put(profileScope.getAttribute(PROFILE_SCOPE_ID), dataItems);
			}

			//Set profile data to profile object
			profile.setProfileData(profileData);

			//Add profile to container
			value[i] = profile;
		}

		//return result
		return value;
	}

	/**
	 * Convenient method to get extension type attribute value
	 *
	 * @param doc The message document
	 * @param xpath The XPath to retrieve element value or nodelist
	 * @return The Extension[] object.
	 */
	private Object getExtensionValue(Document doc, String xpath) throws Exception {
		//Extension type: need return an array of type Extension
		NodeList nl = XMLUtil.getNodeListByXPath(doc, xpath);

		if (nl.getLength() == 0)
			return null;

		//Prepare container
		Extension[] value = new Extension[nl.getLength()];
		logger.debug("    Extension no.: " + value.length);

		//Get all the values
		for (int i = 0; i < value.length; i++) {
			//Get extension element
			Element extElement = (Element) nl.item(i);

			//Prepare Extension object
			Extension ext = new Extension();

			//id attribute missing
			if (extElement.getAttributeNode(THREED_MSG_ID) == null) {
				logger.error("Extension missing id attribute.");
				throw new MessagingException(
					"UNKNOWN",
					ErrorCodes.ERROR_CODE_3,
					ErrorCodes.ERROR_MESSAGE_3,
					"Extension.id",
					"Extension missing id attribute.",
					"Extension missing id attribute.");
			}
            // Set the extension ID
			ext.setExtID(extElement.getAttribute(THREED_MSG_ID));

			// critical attribute missing
			if (extElement.getAttributeNode(EXTENSION_CRITICAL) == null) {
                // Correction for BugID:648
                // From page 88 of VISA spec: a missing critical attribute is equivalent to
                // false critical extension. However a critical extension with no value should
				// no be OK
                ext.setCritical("false");
			}
            else {
                // Critical attribute is specified so we use that value
                ext.setCritical(extElement.getAttribute(EXTENSION_CRITICAL));
            }
			// Set attribute values
			ext.setExtValue(XMLUtil.getValueByXPath(extElement, "text()"));

			// Set extension object in container
			value[i] = ext;
		}

		//return result
		return value;
	}

	/**
	 * Convenient method to get attr type attribute value
	 *
	 * @param doc The message document
	 * @param xpath The XPath to retrieve element value or nodelist
	 * @return The attribute value.
	 */
	private Object getAttrValue(Document doc, String xpath) throws Exception {
		//attrValue type: Get value
		Node node = XMLUtil.getFirstNodeByXPath(doc, xpath);

		if (node == null)
			return null;
		else
			return node.getNodeValue();
	}

	/**
	 * Convenient method to get profileRequest type attribute value
	 *
	 * @param doc The message document
	 * @param xpath The XPath to retrieve element value or nodelist
	 * @return The ProfileRequestScope[] object
	 */
	private Object getProfileRequestValue(Document doc, String xpath) throws Exception {
		//Profile request type: need return an array of type ProfileRequestScope
		NodeList nl = XMLUtil.getNodeListByXPath(doc, xpath);

		if (nl.getLength() == 0)
			return null;

		//Prepare container
		ProfileRequestScope[] value = new ProfileRequestScope[nl.getLength()];
		logger.debug("    Profile request scope no.: " + value.length);

		//Get all the values
		for (int i = 0; i < value.length; i++) {
			//Get profileRequestScope element
			Element scopeElement = (Element) nl.item(i);

			//Prepare ProfileRequestScope object
			ProfileRequestScope prs = new ProfileRequestScope();

			//Set attribute values
			prs.setScopeid(scopeElement.getAttribute(PROFILE_SCOPE_ID));

			//Get all profileRequestItem elements
			NodeList itemElements = XMLUtil.getNodeListByXPath(scopeElement, PROFILE_REQUEST_ITEM);

			//Prepare container
			String[] items = new String[itemElements.getLength()];
			logger.debug("      Scope[" + i + "] ID: " + prs.getScopeid() + " Item no.: " + items.length);

			//Set all profileRequestItem value
			for (int j = 0; j < items.length; j++) {
				//Get individual profileRequestItem element
				Element itemElement = (Element) itemElements.item(j);

				//Get id attribute value
				items[j] = itemElement.getAttribute(PROFILE_UID);
			}

			//Set item values to profileRequestScope
			prs.setRequestItem(items);

			//Set profileRequestScope object in container
			value[i] = prs;
		}

		//return result
		return value;
	}

	/**
	 * Convenient method to get signature type attribute value
	 *
	 * @param doc The message document
	 * @param xpath The XPath to retrieve element value or nodelist
	 * @return The Signature element.
	 */
	private Object getSignatureValue(Document doc, String xpath) throws Exception {
		//Get signature parent element
		Element parent = (Element) XMLUtil.getFirstNodeByXPath(doc, xpath);

		//Get signature element
		NodeList nl = parent.getElementsByTagName(SIGNATURE_TAG_NAME);
		if (nl.getLength() == 0)
			return null;

		Element signature = (Element) nl.item(0);

		//Create wrap document
		Document doc2 = XMLUtil.createDocument();

		//Import signature to wrap document
		doc2.appendChild(doc2.importNode(signature, true));

		//return String who represents this document
		return XMLUtil.toXmlString(doc2);
	}

	/**
	 * Convenient method to get profileRequest type attribute value
	 *
	 * @param doc The message document
	 * @param xpath The XPath to retrieve element value or nodelist
	 * @return The CardRange[] object
	 */
	private Object getCrValue(Document doc, String xpath) throws Exception {
		//Cr type: need return an array of type CardRange
		NodeList nl = XMLUtil.getNodeListByXPath(doc, xpath);

		if (nl.getLength() == 0)
			return null;

		//Prepare container
		CardRange[] crs = new CardRange[nl.getLength()];
		logger.debug("    Number of Card Ranges: " + crs.length);

		for (int i = 0; i < crs.length; i++) {
			Element crElement = (Element) nl.item(i);
			CardRange cr = new CardRange();

			String begin = XMLUtil.getValueByXPath(crElement, "begin/text()");
			String end = XMLUtil.getValueByXPath(crElement, "end/text()");
			String action = XMLUtil.getValueByXPath(crElement, "action/text()");

			// Validate the size of the begin card number provided
			if ((begin == null) || (begin.length() > 19) || (begin.length() < 13)) {
				logger.error("CR.begin is invalid. Value: " + begin);
				throw new MessagingException(
					"UNKNOWN",
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"CR.begin",
					"Invalid number size.",
					"Invalid number size.");
			}

			// Validate the size of the end card number provided
			if ((end == null) || (end.length() > 19) || (end.length() < 13)) {
				logger.error("CR.begin is invalid. Value: " + end);
				throw new MessagingException(
					"UNKNOWN",
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"CR.end",
					"Invalid number size.",
					"Invalid number size.");
			}

			// Validate to ensure the begin and end card number are of the same length
			if (begin.length() != end.length()) {
				logger.error("Card range not of same size. Begin size: " + begin.length() + " End size: " + end.length());
				throw new MessagingException(
					"UNKNOWN",
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"CR.begin, CR.end",
					"Begin card not same length as End card",
					"Begin card not same length as End card.");
			}

			// Validate the action associate with the card range provided
			if ((action == null) || !(action.equalsIgnoreCase("A") || action.equalsIgnoreCase("D"))) {
				logger.error("CR.action is invalid. Value: " + action);
				throw new MessagingException(
					"UNKNOWN",
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"CR.action",
					"Invalid action.",
					"Invalid action.");
			}

			try {
				cr.setBegin(Long.valueOf(begin));
			}
			catch (NumberFormatException nfe) {
				logger.error("CR.begin is invalid. Value: " + begin);
				throw new MessagingException(
					"UNKNOWN",
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"CR.begin",
					"Invalid number format.",
					"Invalid number format.");
			}

			try {
				cr.setEnd(Long.valueOf(end));
			}
			catch (NumberFormatException nfe) {
				logger.error("CR.end is invalid. Value: " + end);
				throw new MessagingException(
					"UNKNOWN",
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"CR.end",
					"Invalid number format.",
					"Invalid number format.");

			}

			cr.setAction(action);

			logger.debug(
				"      CR "
					+ i
					+ ":\n"
					+ "        begin: "
					+ begin
					+ "\n"
					+ "        end: "
					+ end
					+ "\n"
					+ "        action: "
					+ action);

			crs[i] = cr;
		}

		return crs;
	}

	/**
	 * check undefined elements in received messages. It checks if there is any
	 * undefined element under Message element.
	 *
	 * In turn, it checks elements under CRRes, VERes, PARes and Error elements.
	 *
	 * @param e The Message element
	 */
//	private void checkUndefinedElements(Element e) throws MessagingException {
//		try {
//			logger.debug("Checking undefined elements under Message element...");
//			Element element = XUtil.getFirstChildElement(e);
//			if (element == null) {
//				logger.error("Element missing under Message element.");
//				return;
//			}
//
//			String nodeName = element.getTagName();
//
//			if (!MSG_ELEMENTS.contains(nodeName)) {
//				logger.error("Undefined element found under Message element: " + nodeName);
//				throw new MessagingException(
//					"UNKNOWN",
//					ErrorCodes.ERROR_CODE_2,
//					ErrorCodes.ERROR_MESSAGE_2,
//					nodeName,
//					"Undefined element check failed.",
//					"Undefined element found under Message element: " + nodeName);
//			}
//
//			logger.debug("No undefined elements found under Message element.");
//
//			if ((nodeName.equals("CRRes"))
//				|| (nodeName.equals("VERes"))
//				|| (nodeName.equals("PARes"))
//				|| (nodeName.equals("Error"))) {
//				Vector validElements = (Vector) MSG_MAPPING.get(nodeName);
//				Node node = XUtil.getFirstChildElement(element);
//
//				while (node != null) {
//					if (validElements.contains(node.getNodeName()))
//						node = XUtil.getNextSiblingElement(node);
//					else {
//						logger.error(
//							"Undefined element found under " + nodeName + " element: " + node.getNodeName());
//						throw new MessagingException(
//							"UNKNOWN",
//							ErrorCodes.ERROR_CODE_2,
//							ErrorCodes.ERROR_MESSAGE_2,
//							nodeName,
//							"Undefined element check failed.",
//							"Undefined element found under " + nodeName + " element: " + node.getNodeName());
//					}
//				}
//
//				logger.debug("No undefined elements found under " + nodeName + " element.");
//			}
//		}
//		catch (Exception exc) {
//			logger.error("Exception caught during Undefined elements checking.", exc);
//			throw new MessagingException(exc);
//		}
//	}

	private Class getJavatypeClass(String javatype) throws Exception {
		try {
			//Filter array
			int inx = javatype.indexOf("[]");

			if (inx > 0) {
				//Array class
				String arraytype = javatype.substring(0, inx);
				Object result = Array.newInstance(Class.forName(arraytype), 0);
				return result.getClass();
			}
			else {
				//Not array class
				return Class.forName(javatype);
			}
		}
		catch (Exception e) {
			logger.error("Failed to get javatype class: " + javatype, e);
			throw new NullPointerException("Failed to get javatype class: " + javatype);
		}
	}

	private Element findDefinedMessageElement(String id, Element msg) throws Exception {
		//Get Message element's first child
		Element e = XUtil.getFirstChildElement(msg);

		if (e == null) {
			logger.error("The first element under Message is not defined message element.");
			throw new MessagingException(
				id,
				ErrorCodes.ERROR_CODE_3,
				ErrorCodes.ERROR_MESSAGE_3,
				"Message element",
				"Message element missign.",
				"Message element missing.");

		}

		//check element name
		String tagName = e.getTagName();
		if (MSG_ELEMENTS.contains(tagName)) {
			return e;
		}
		else {
			logger.error("The first element under Message is not defined message element.");
			throw new MessagingException(
				id,
				ErrorCodes.ERROR_CODE_2,
				ErrorCodes.ERROR_MESSAGE_2,
				tagName,
				"No defined message element found.",
				"No defined message element found.");
		}
	}
}