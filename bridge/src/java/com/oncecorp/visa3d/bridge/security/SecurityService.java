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

package com.oncecorp.visa3d.bridge.security;

import java.util.Date;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.security.KeyStore;
import java.text.SimpleDateFormat;

import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import org.apache.log4j.Logger;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

import com.oncecorp.visa3d.bridge.monitoring.SecurityServiceMBean;

/**
 * <p>Title: SecurityService </p>
 * <p>Description: Provides security related MBean functions</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
public class SecurityService implements SecurityServiceMBean
{

	private static Logger m_logger = DataBridgeLoger.getLogger(
			SecurityService.class.getName() );

	private static BASE64Encoder m_base64Encoder = new BASE64Encoder();
	private static BASE64Decoder m_base64Decoder = new BASE64Decoder();

	public final static String            KEY_STORE_DATA   = "KeyStore.Data";
	public final static String            KEY_STORE_PASSWORD  = "KeyStore.Password";

	/**
	 * Default constructor
	 */
    public SecurityService()
    {
    }

	/**
	 * Reset encryption related properties from management console
	 * The keystore data is encoded by base64.
	 * @param props - security related attributes
	 */
	public void putEncryptionKeyStore( Properties props )
	{
		String fname = TripleDESEncrypter.getKeyStoreFileName();
		String data = props.getProperty( KEY_STORE_DATA );
		String passwd = props.getProperty( KEY_STORE_PASSWORD );
		try {
			saveKeystore2File( data, fname, passwd );
			TripleDESEncrypter.initialize( props );
		} catch ( Exception e )
		{
			m_logger.error( "Save encryption key store exception:", e );
		}
	}

	/**
	 * Retrive the current encryption properties and assemble them into the properties
	 * and send to the msnagement console. The keystore data is encoded by base64.
	 * @return - the current security properties
	 */
	public  Properties retriveEncryptionKeyStore()
	{
		Properties props = new Properties();
		Map settings = TripleDESEncrypter.getSettings();
		String key;
		for ( Iterator lt = settings.keySet().iterator(); lt.hasNext(); )
		{
			key = (String) lt.next();
			props.put( key, settings.get( key ) );
		}

		props.setProperty( KEY_STORE_PASSWORD,
						  TripleDESEncrypter.KEY_STORE_PASSWORD );
		KeyStore kstore = TripleDESEncrypter.getKeyStore();
		String passwd = props.getProperty( KEY_STORE_PASSWORD );

		String data = getKeystoreBytes( kstore, passwd );

		if ( data != null )
	        props.setProperty( KEY_STORE_DATA, data );
        else
			props.setProperty( KEY_STORE_DATA, "" );

		return props;
	}

	/**
	 * Reset SSL keystore related properties from management console
	 * The keystore data is encoded by base64.
	 * @param props - security related attributes
	 */
	public void putSSLKeyStore( Properties props )
	{
		m_logger.warn("Not used yet.");
	}

	/**
	 * Retrive the current SSL keystore properties and assemble them into the properties
	 * and send to the msnagement console. The keystore data is encoded by base64.
	 * @return - the current security properties
	 */
	public  Properties retriveSSLKeyStore()
	{
		m_logger.warn("Not used yet.");
		return null;
	}

	/**
	 * Reset SSL trust store related properties from management console
	 * The keystore data is encoded by base64.
	 * @param props - security related attributes
	 */
	public void putSSLTrustStore( Properties props )
	{
		m_logger.warn("Not used yet.");
	}

	/**
	 * Retrive the current SSL trust store properties and assemble them into the properties
	 * and send to the msnagement console. The keystore data is encoded by base64.
	 * @return - the current security properties
	 */
	public  Properties retriveSSLTrustStore()
	{
		m_logger.warn("Not used yet.");
		return null;
	}

	/**
	 * Get Base64 Encoded string of a given keystore byte array
	 * @param kstore - keystore instance
	 * @param password - keystore password
	 * @return - key store byte arrays or null if any exception happened
	 */
	public static String getKeystoreBytes(KeyStore kstore,
			String password )
	{
		char[] pwout = password.toCharArray();
		if (pwout.length == 0)
		{
			pwout = null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			kstore.store(out, pwout);
		} catch ( Exception e )
		{
			m_logger.error("Get keystore bytes exception:", e);
			return null;
		}

		try {
			out.flush();
			byte[] bytes = out.toByteArray();
			out.close();
			System.out.println("get keystore bytes ["
					+ bytes.length + "] successfully.\n");
			return m_base64Encoder.encode( bytes );
		} catch ( Exception e )
		{
			m_logger.error("Get keystore bytes IO exception:", e);
			return null;
		}
	}

	/**
	 * Save a given keystore data into the file, the old keystore file is also
	 * saved by time.
	 * @param data - base 64 encoded keystore data
	 * @param fname - output file name
	 * @param password - keystore password
	 * @throws OCMException
	 */
	 public static void saveKeystore2File( String data,
									 String fname, String password )
			 throws Exception
	 {
		 char[] jksPassword = password.toCharArray();

		 KeyStore jksKeyStore = null;
		 jksKeyStore = KeyStore.getInstance( "JKS" );

		 //Load the keystore
		 ByteArrayInputStream jksInputStream =
				 new ByteArrayInputStream( m_base64Decoder.decodeBuffer( data ) );
		 jksKeyStore.load( jksInputStream, jksPassword );

		 char[] pwout = password.toCharArray();
		 if (pwout.length == 0) {
			 pwout = null;
		 }
		 File file = new File( fname );
		 if ( file.exists() )
		 {
			 //Save the old file
			 String apath = file.getAbsolutePath();
			 File tmpDir = new File( apath );
			 String ppath = tmpDir.getParent();
			 String oname = file.getName();

			 SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

			 String tmpFile = ppath + File.separator
				   + sdf.format( new Date() ) + "_" + oname;

			 m_logger.debug( "Save the old keystore file to: [" +  tmpFile + "]" );
			 file.renameTo( new File ( tmpFile ) );
		 }
		 FileOutputStream out = new FileOutputStream( fname );
		 jksKeyStore.store(out, pwout);
		 out.close();
	 }

}