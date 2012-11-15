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

package com.oncecorp.visa3d.bridge.monitoring;

import java.util.Properties;

/**
 * <p>Title: SecurityServiceMBean </p>
 * <p>Description: Security service related attributes and operations published to JMX server</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public interface SecurityServiceMBean
{
	/**
	 * Reset encryption related properties from management console
	 * The keystore data is encoded by base64.
	 * @param props - security related attributes
	 */
	public void putEncryptionKeyStore( Properties props );

	/**
	 * Retrive the current encryption properties and assemble them into the properties
	 * and send to the msnagement console. The keystore data is encoded by base64.
	 * @return - the current security properties
	 */
	public  Properties retriveEncryptionKeyStore();

	/**
	 * Reset SSL keystore related properties from management console
	 * The keystore data is encoded by base64.
	 * @param props - security related attributes
	 */
	public void putSSLKeyStore( Properties props );

	/**
	 * Retrive the current SSL keystore properties and assemble them into the properties
	 * and send to the msnagement console. The keystore data is encoded by base64.
	 * @return - the current security properties
	 */
	public  Properties retriveSSLKeyStore();

	/**
	 * Reset SSL trust store related properties from management console
	 * The keystore data is encoded by base64.
	 * @param props - security related attributes
	 */
	public void putSSLTrustStore( Properties props );

	/**
	 * Retrive the current SSL trust store properties and assemble them into the properties
	 * and send to the msnagement console. The keystore data is encoded by base64.
	 * @return - the current security properties
	 */
	public  Properties retriveSSLTrustStore();
}



