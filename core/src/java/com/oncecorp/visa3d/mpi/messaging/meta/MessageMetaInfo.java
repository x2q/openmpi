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

package com.oncecorp.visa3d.mpi.messaging.meta;

import com.oncecorp.visa3d.mpi.configuration.ConfigAccessor;
import com.oncecorp.visa3d.mpi.configuration.ConfigAccessorGenerator;
import com.oncecorp.visa3d.mpi.configuration.ConfigAccessorType;
import com.oncecorp.visa3d.mpi.logging.MPILogger;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * This class contains all the definition information associated with a specific
 * message type/version. The information is retrieved from the MessageConfig.xml file.
 * Each message name/version has a corresponding MessageMetaInfo object associated to it.
 *
 * All this information is contained in an hashmap organized like the following:
 *
 * 		Key 		Value
 * 		-----		--------------
 * 		MessageUID	MessageMetaInfo
 *
 * So to retrieve the proper MessageMetaInfo, the client must construct the proper
 * MessageUID object and use it as a key into the overall mapping structure
 *
 * @author mdufort
 */
public class MessageMetaInfo {
	private String name;                    // Name of message
	private ArrayList supportedVersion;     // Version of message
	private String protocol;                // Protocol this message adheres to...
	private String impl;                    // Implementation code to support that message
	private String processor;               // Processor class name to process this object
	private BindingMetaInfo bindings;       // List of bindings for DOM to Message conversion
	private ExtensionMetaInfo extensions;   // List of supported extensions sorted by id
	private HashMap transformers;           // List of supported transformers sorted by supported

	private static HashMap allMetaInfo = null;

	/**
	 * Local Log4J logger
	 */
	private static Logger logger = MPILogger.getLogger(MessageMetaInfo.class.getName());

	public final static String MESSAGE_PROTOCOL_3DSECURE 	= "3DSecure";
	public final static String MESSAGE_PROTOCOL_MPI 		= "MPI_Interface";
	public final static String MESSAGE_PROTOCOL_ALL 		= "all";

	public MessageMetaInfo() {
	}

	/**
	 * Returns the bindings.
	 * @return ArrayList
	 */
	public BindingMetaInfo getBindings() {
		return bindings;
	}

	/**
	 * Returns the extensions.
	 * @return ArrayList
	 */
	public ExtensionMetaInfo getExtensions() {
		return extensions;
	}

	/**
	 * Returns the impl.
	 * @return String
	 */
	public String getImpl() {
		return impl;
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the processor.
	 * @return String
	 */
	public String getProcessor() {
		return processor;
	}

	/**
	 * Returns the transformers.
	 * @return ArrayList
	 */
	public HashMap getTransformers() {
		return transformers;
	}

	/**
	 * Returns the version.
	 * @return String
	 */
	public ArrayList getSupportedVersion() {
		return supportedVersion;
	}

	/**
	 * Sets the bindings.
	 * @param bindings The bindings to set
	 */
	public void setBindings(BindingMetaInfo bindings) {
		this.bindings = bindings;
	}

	/**
	 * Sets the extensions.
	 * @param extensions The extensions to set
	 */
	public void setExtensions(ExtensionMetaInfo extensions) {
		this.extensions = extensions;
	}

	/**
	 * Sets the impl.
	 * @param impl The impl to set
	 */
	public void setImpl(String impl) {
		this.impl = impl;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the processor.
	 * @param processor The processor to set
	 */
	public void setProcessor(String processor) {
		this.processor = processor;
	}

	/**
	 * Sets the transformers.
	 * @param transformers The transformers to set
	 */
	public void setTransformers(HashMap transformers) {
		this.transformers = transformers;
	}

	/**
	 * Sets the version.
	 * @param version The version to set
	 */
	public void setSupportedVersion(ArrayList version) {
		this.supportedVersion = version;
	}

	/**
	 * Method isVersionSupported.
	 * @param msgUID        ID of the message
	 * @return boolean      Do we support this message type & version ?
   	 */
	public static boolean isVersionSupported(MessageUID msgUID) {
		checkMetaInfoLoaded();

		return allMetaInfo.containsKey(msgUID);
	}

	/**
	 * Retrieve the processor for a specific MessageUID
	 */
	private static String getProcessorClz(MessageUID forUID) {
		checkMetaInfoLoaded();

		MessageMetaInfo mInfo = (MessageMetaInfo) allMetaInfo.get(forUID);
		if (mInfo != null) {
			return mInfo.getProcessor();
		}
		return null;
	}
    /**
	 * Return an instance of the processor object
	 * @param uid		UID of message to which we must fetch the transformer
	 */
	public static Object getProcessor(MessageUID uid) {
		try {
			String clz = getProcessorClz(uid);
			if (clz == null) return null;
			return Class.forName(clz).newInstance();
		}
		catch (ClassNotFoundException e1) {
			logger.fatal("Unable to instantiate the processor class for " + uid);
			return null;
		}
		catch (InstantiationException e2) {
			logger.fatal("Unable to instantiate the processor class for " + uid);
			return null;
		}
		catch (IllegalAccessException e2) {
			logger.fatal("Unable to instantiate the processor class for " + uid);
			return null;
		}
	}
	private static String getTransformerClz(MessageUID forUID, String toType, String toVersion) {
		checkMetaInfoLoaded();

		MessageMetaInfo mInfo = (MessageMetaInfo) allMetaInfo.get(forUID);
		if (mInfo != null) {
			MessageUID transformUID = new MessageUID(toType, toVersion);
			Object clz = mInfo.getTransformers().get(transformUID);
			if (clz != null) return (String) clz;
		}
		return null;
	}

    /**
	 * Return an instance of the transformer object
	 * @param uid		UID of message to which we must fetch the transformer
	 * @param toType	Type of message we are transforming to
	 * @param toVersion	Version of message we are transforming to
	 */
	public static Object getTransformer(MessageUID uid, String toType, String toVersion) {
		try {
			String clz = getTransformerClz(uid, toType, toVersion);
			if (clz == null) return null;
			return Class.forName(clz).newInstance();
		}
		catch (ClassNotFoundException e) {
			logger.fatal("Unable to instantiate the transformer class for " + uid);
			return null;
		}
		catch (InstantiationException e) {
			logger.fatal("Unable to instantiate the transformer class for " + uid);
			return null;
		}
		catch (IllegalAccessException e) {
			logger.fatal("Unable to instantiate the transformer class for " + uid);
			return null;
		}
	}


	public static BindingMetaInfo getBindingInfo(MessageUID forUID) {
		checkMetaInfoLoaded();

		MessageMetaInfo mInfo = (MessageMetaInfo) allMetaInfo.get(forUID);
		if (mInfo != null) {
			Object obj = mInfo.getBindings();
			if (obj != null) return (BindingMetaInfo) obj;
		}
		return null;

	}

	public static ExtensionMetaInfo getExtensionInfo(MessageUID forUID) {
		checkMetaInfoLoaded();

		MessageMetaInfo mInfo = (MessageMetaInfo) allMetaInfo.get(forUID);
		if (mInfo != null) {
			Object obj = mInfo.getExtensions();
			if (obj != null) return (ExtensionMetaInfo) obj;
		}
		return null;
	}


	/**
	 * Method checkMetaInfoLoaded.
	 */
	private final static void checkMetaInfoLoaded() {
		if (allMetaInfo == null) {
			ConfigAccessor cfgAcc = ConfigAccessorGenerator.getAccessor(ConfigAccessorType.ACCESSOR_TYPE_MESSAGING);
			allMetaInfo = (HashMap) cfgAcc.getConfigData();
		}
	}

	/**
	 * Return the implementation class string for a specific message type/version
	 * @param messageUID
	 * @return String
	 */
	public static String getImpl(MessageUID messageUID) {
		checkMetaInfoLoaded();

		MessageMetaInfo mInfo = (MessageMetaInfo) allMetaInfo.get(messageUID);
		if (mInfo == null) {
            // The implementation version requested does not exist in the message meta-data list
			logger.error("No implementation class for " + messageUID );
            return null;
		}
		return mInfo.getImpl();
	}

	/**
	 * Returns the protocol.
	 * @return String
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Sets the protocol.
	 * @param protocol The protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

}
