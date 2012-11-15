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
 * Description: The class contains DOM document to MPI message
 * convertion binding information.
 * 
 * @version 0.1 Aug 01, 2002
 * @author	Alan Zhang
 */

public class BindingMetaInfo {
	/**
	 * The array contains all the XPath expressions which will be used
	 * to get element values.
	 */
	protected String[] paths;

	/**
	 * The array contains all binding attributes of individual message
	 */
	protected String[] attributes;
	
	/**
	 * The binding type
	 */
	protected String[] types;

	/**
	 * The binding javatype
	 */
	protected String[] javatypes;

	/**
	 * Gets the paths
	 * @return Returns a String[]
	 */
	public String[] getPaths() {
		return paths;
	}

	/**
	 * Sets the paths
	 * @param paths The paths to set
	 */
	public void setPaths(String[] paths) {
		this.paths = paths;
	}

	/**
	 * Gets the attributes
	 * @return Returns a String[]
	 */
	public String[] getAttributes() {
		return attributes;
	}

	/**
	 * Sets the attributes
	 * @param attributes The attributes to set
	 */
	public void setAttributes(String[] attributes) {
		this.attributes = attributes;
	}

	/**
	 * Gets the types
	 * @return Returns a String[]
	 */
	public String[] getTypes() {
		return types;
	}
	/**
	 * Sets the types
	 * @param types The types to set
	 */
	public void setTypes(String[] types) {
		this.types = types;
	}


	/**
	 * Returns the javatypes.
	 * @return String[]
	 */
	public String[] getJavatypes() {
		return javatypes;
	}

	/**
	 * Sets the javatypes.
	 * @param javatypes The javatypes to set
	 */
	public void setJavatypes(String[] javatypes) {
		this.javatypes = javatypes;
	}

}