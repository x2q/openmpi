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

import java.util.HashMap;

/**
 * This class contains all the extension segment supported for a specific message.
 * This information is stored within a hashmap that is indexed by the extension id
 * The inner class ExtensionMetaData holds the information for a single instance
 * of extension
 * 
 * @version $Revision: 5 $
 * @author Martin Dufort (mdufort@oncecorp.com)
 */

public class ExtensionMetaInfo {
	private HashMap extensionList = new HashMap();	// The list of all extensions for this message
									                // Key: extensionID	Value: extensionMetaData

	public ExtensionMetaInfo() {
	}

	/**
	 * Add a new extension definition for this associated message definition
	 */	
	public void addExtension(String anID, String aName, String aProcessor) {
		ItemUID uid  	= new ItemUID(anID, aName);
		Item item 		= new Item(uid, aProcessor);

		// Add an entry with the following characteristics
		// Key: extensionID Value: ExtensionMetaInfoItem
		extensionList.put(uid, item);	
	}
	
	/**
	 * Clear all the extensions defined for this message
	 */
	public void clearAll() {
		extensionList.clear();
	}
	
	/**
	 * Retrieve the processor class name for a specific extension ID/name
	 */
	public String getProcessor(String anID, String aName) {
		// Try to retrieve the processor associated with a specific ID and name
		Item item;
		ItemUID uid = new ItemUID(anID, aName);
		if ( (item = (Item) extensionList.get(uid)) != null ) {
			// We return the processor name
			return item.getProcessor();
		}
		
		// No processor found, we return NULL
		return null;		 	
	}

	/** 
	 * Check if a particular extension is supported for the associated message definition
	 */
	public boolean isSupported(String anID, String aName) {
		ItemUID uid = new ItemUID(anID, aName);
		return ( extensionList.get(uid) != null );
	}
	

	/**
	 * The Item inner class defined the processor information associated
	 * with a specific extension UID
	 */	
	private class Item {
		ItemUID uid;			// UID for this extension
		String processor;		// The processor associated with the extension

		Item(ItemUID aUID, String aProcessor) {
			this.uid		 	= aUID;
			this.processor 		= aProcessor;
		}

		String getExtensionID() 	{ return this.uid.getID(); }
		String getExtensionName() 	{ return this.uid.getName(); }
		String getProcessor() 		{ return this.processor; }	
	}		

	/**
	 * The Item UID inner class defines the extension primary which consist
	 * of the extension ID and the extension name
	 */
	private class ItemUID {
		String extensionID;			// The extension ID defining this extension
		String extensionName;		// The extension name
	
		ItemUID(String anID, String aName) {
			this.extensionID 	= anID;
			this.extensionName  = aName;
		}
		
		String getID() 		{ return this.extensionID; }
		String getName() 	{ return this.extensionName; }
		
		/** 
		 * Override the hashcode function to enable a better distribution
		 * when used with hashed collections
		 */
		public int hashCode() {
			// Return the hashcode for the concatenation of ID and Name
			return (getID()+getName()).hashCode();
		}
	}
}
