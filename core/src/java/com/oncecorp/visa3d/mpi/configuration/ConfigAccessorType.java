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

import java.util.HashMap;

/**
 * Description: This class is a common definition mapping Config Accessor type
 * and its implemented class name. Also, it provides JNDI name of Config Entity
 * Bean in case caller likes to load/store data via CMP      
 *  
 * @version $Revision: 15 $
 * @author 	Martin Dufort (mdufort@oncecorp.com)
 */
public class ConfigAccessorType {
    public final static String CONFIG_DATA_JNDI         = "mpi/ConfigData";
    public final static String ACCESSOR_TYPE_MPI        = "MPI";
    public final static String ACCESSOR_TYPE_MESSAGING  = "MESSAGING";
    public final static String ACCESSOR_TYPE_LOGGING    = "LOGGING";
    public final static String ACCESSOR_TYPE_MERCHANT	= "MERCHANT";

	// Define and properly intialize the accessor map
	private static HashMap accessorMap = new HashMap();
	static {
		accessorMap.put(ACCESSOR_TYPE_MPI, 		  "com.oncecorp.visa3d.mpi.configuration.MPIConfigAccessor");
		accessorMap.put(ACCESSOR_TYPE_MERCHANT,   "com.oncecorp.visa3d.mpi.configuration.MerchantConfigAccessor");
		accessorMap.put(ACCESSOR_TYPE_LOGGING, 	  "com.oncecorp.visa3d.mpi.logging.LoggingConfigAccessor");
		accessorMap.put(ACCESSOR_TYPE_MESSAGING,  "com.oncecorp.visa3d.mpi.messaging.MessagingConfigAccessor");
	}

    /**
	 * Return the proper classname that will provide accessor functionality 
	 * from the supplied type name
	 *
	 * @param 	String  The type of configuration accessor wanted
	 * @return 	String 	The classname associated with the proper type name
	 */
    public static String getAccessorClassName(String typeName) throws ConfigurationException {
    	if (typeName == null) throw new ConfigurationException("Invalid type name for accessor request");
    	
	    return (String) accessorMap.get(typeName.toUpperCase());
    }
}