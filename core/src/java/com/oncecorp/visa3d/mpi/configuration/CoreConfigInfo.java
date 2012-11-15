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

package com.oncecorp.visa3d.mpi.configuration;

import java.io.Serializable;

/**
 * Defines the structure of the configuration attributes needed by the Core
 * Server to properly execute it's functionality. This class has migrated
 * from the simpler, less expressive Hashmap implementation found in earlier
 * releases. This allows configuration values validation and management action
 * checking like delete, display & modify.
 * 
 * Each instance of this class is connected, using the unique Configuration Name, 
 * to the corresponding Validator object that provides basic validation functions.
 * 
 * @since 	1.2
 * @version $Revision: 1 $
 * @author 	Martin Dufort (mdufort@oncecorp.com)
 */
public class CoreConfigInfo implements Serializable, Comparable {
	/* Serializable versioning information */
	/* generated using: serialver com.oncecorp.visa3d.mpi.configuration.MerchantMetaInfo */ 
	static final long serialVersionUID = 1000L;
	
	/* --------- Static attributes ---------- */
	private static String NAMESPACE	= "coreConfig:";
	
	// General description of this configuration item
	private String description;
	
	/* --------- Member attributes ---------- */
	// Configuration name
	private String name;
	
	// Description of value units for this configuration item
	private String unitDescription;
	
	// Configuration value
	private String value;

	/**
	 * Default constructor. Initialize and nullify all member attributes
	 */
	public CoreConfigInfo() {
		this.name 				= null;
		this.value				= null;
		this.description		= null;
		this.unitDescription	= null;
	}
	
	/**
	 * Copy constructor. Initialize a new instance of this object
	 * from another instance.
	 * 
	 * @param copyFrom	Object to use to initialize this new instance
	 */
	public CoreConfigInfo(CoreConfigInfo copyFrom) {
		name 			= copyFrom.name;
		value			= copyFrom.value;
		description 	= copyFrom.description;
		unitDescription = copyFrom.unitDescription;
	}
	
	/**
	 * Implement the comparable interface so objects of this class
	 * can be inserted and efficiently retrieved from set-like collections
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object anObject) {
		CoreConfigInfo info = (CoreConfigInfo) anObject;
		if (! (info instanceof CoreConfigInfo)) {
			throw new ClassCastException("Unable to cast object for proper comparison");
		}
		
		// Start comparison with name and continue with value if both names are equal
		int compare = this.name.compareTo(info.getName());
		if (compare == 0) {
			return this.value.compareTo(info.getValue()); 
		}
		else {
			return compare;
		}
		
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object anObject) {
		if (! (anObject instanceof CoreConfigInfo)) {
			return false;
		}
		
		CoreConfigInfo compareTo = (CoreConfigInfo) anObject;	
		if ((this.name.equalsIgnoreCase(compareTo.getName()) &&
			(this.value.equalsIgnoreCase(compareTo.getValue())))) {
			return true;
		}
		else {
			return false;
		}	
	}
	
	/**
	 * Returns the description.
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the unitDescription.
	 * @return String
	 */
	public String getUnitDescription() {
		return unitDescription;
	}

	/**
	 * Returns the value.
	 * @return String
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the description.
	 * @param description The description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the unitDescription.
	 * @param unitDescription The unitDescription to set
	 */
	public void setUnitDescription(String unitDescription) {
		this.unitDescription = unitDescription;
	}

	/**
	 * Sets the value.
	 * @param value The value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
