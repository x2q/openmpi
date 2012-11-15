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

package com.oncecorp.visa3d.mpi.monitoring;

import java.util.HashMap;
import java.util.Properties;

/**
 * MBean interface for Configurator
 * 
 * @author azhang
 * @version 1.0 6-Dec-02
 */
public interface ConfiguratorMBean {

	/**
	 * Load core config data
	 */
	public HashMap loadCoreConfigData();

	/**
	 * Save core config data
	 */
	public String saveCoreConfigData(HashMap data);

	/**
	 * Refresh merchant config data to synchronize with changes
	 * done by Data Bridge
	 */
	public HashMap refreshMerchantConfigData();

	/**
	 * Load log4j config data
	 */
	public Properties loadLog4jConfigData();
	
	/**
	 * Save log4j config data
	 * @param props The new properties
	 */
	public String saveLog4jConfigData(Properties props);


}
