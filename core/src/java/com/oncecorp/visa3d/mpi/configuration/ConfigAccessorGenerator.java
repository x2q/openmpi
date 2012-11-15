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

/**
 * This is the factory class which provides access point to other MPI 
 * components to get a individual ConfigAccessor. Suggested that
 * other components pass in their fully qualified Accessor type name. 
 * 
 *
 * @version $Revision: 13 $
 * @author Martin Dufort (mdufort@oncecorp.com)
 */
public class ConfigAccessorGenerator {
	/**
	 * Return a properly initialized ConfigAccessor instance from 
	 * the provided type. That type must be valid and will usually
	 * be specified from the defined types in the ConfigAccessorType
	 * object
	 * 
	 * @param 	String 		Type of accessor wanted
	 * @return 	An instance of the ConfigAccessor interface
	 */
	public static ConfigAccessor getAccessor(String accessorType) {
		ConfigAccessor cfgAccessor = null;
		try {
			String clzName = ConfigAccessorType.getAccessorClassName(accessorType);
			cfgAccessor = (ConfigAccessor) Class.forName(clzName).newInstance();
		}
		catch (ConfigurationException cfgExcep) {
			cfgExcep.printStackTrace();
		}
		catch (InstantiationException iExcep) {
			iExcep.printStackTrace();
		}
		catch (IllegalAccessException iaExcep) {
			iaExcep.printStackTrace();
		}
		catch (ClassNotFoundException cnfExcep) {
			cnfExcep.printStackTrace();
		}

		return cfgAccessor;
	}
}