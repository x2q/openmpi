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

/**
 * This identity class is use to store and retrieve message meta-data information 
 * that is specified in the MessagingConfig.xml file. All messages are thus uniquely
 * identified by the {name, version} tuple which is embedded in the present class.
 * Since this class implements the Comparable interface, it can be used transparently as
 * a key in Set type of objects.
 *
 * @version $Revision: 1 $
 * @author mdufort
 */
public class MessageUID implements Comparable {

	private String name;
	private String version;
	
	/**
	 * Constructor for MessageUID.
	 * Default constructor is not accessible
	 */
	private MessageUID() {
		super();
	}

	/** 
	 * Construct a valid MessageUID using the message name
	 * and version...Those should be in the format
	 * name 	:== "PARes"
	 * version 	:== "1.0.1"
	 */
	public MessageUID(String aName, String aVersion) {
		super();
		this.name 		= aName;
		this.version 	= aVersion;
	}	
	

	/**
	 * Getter for message version number
	 * @return Version number of message
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * Getter for message typename
	 * @return Name type of message
	 */
	public String getName() {
		return this.name;
	}

	/** 
	 * Implements the hashCode function to be used in hash-type collections.
	 * To obtain hashcode, we concatenate the message type and message 
	 * version together into a new temporary string and we return the 
	 * hashCode value of that new string.
	 * 
	 * @return Hashcode for MessageUID instance
	 */
	public int hashCode() {
		return (getName()+getVersion()).hashCode();
	}
	
	/**
	 * Civilized version of the toString conversion method.
	 * Output intelligent string describing MessageUID information
	 */
	public String toString() {
		return "[MessageUID:"+ this.name + "/" + this.version + "]";
	}

	/**
	 * Implementation of the Comparable interface. This is needed is 
	 * order to use this class in Set-related collections.
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object anObject) {
		// First compare the message name. 
		MessageUID msg2Compare = (MessageUID) anObject;
		int compareResult = this.name.compareTo(msg2Compare.getName());
		if (compareResult != 0) return compareResult;
		
		// If message name is equal then we we need to compare the version number
		compareResult = this.version.compareTo(msg2Compare.getVersion());
		return compareResult;			
	}

	/**
	 * Returns true only if both the name and the version are equal.
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object anObject) {
		if (! (anObject instanceof MessageUID)) return false;
		MessageUID msg2Compare = (MessageUID) anObject;

		return (this.name.equals(msg2Compare.getName()) && 
				this.version.equals(msg2Compare.getVersion()));
	}
}
