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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import org.apache.log4j.Logger;

import com.ibm.xml.dsig.util.Base64;

/**
 * Provides support for Triple-DES encryption.
 * This class is from mpi core and modified the config and logger mechanism by Gang Wu
 *
 * Add databridge related configuration functions by Gang Wu
 *
 * @version 0.1 Nov 28, 2002
 * @author	Alan Zhang
 */
public class TripleDESEncrypter {

    private static Logger m_logger = DataBridgeLoger.getLogger(
            TripleDESEncrypter.class.getName() );

    public final static String CIPHER_TEXT_IV_DELIMITER  =         "___";

	final static String CIPHER_TRANS = "DESede/CBC/PKCS5Padding";
	final static String KEY_GEN_TRANS = "DESede";

	private static SecretKey secretKey;
	private static TripleDESEncrypter encrypter;

    public final static String JCE_PROVIDER =  "JCEProvider";
    public final static String KEY_STORE_LOCATION = "KeystoreLocation";
    public final static String KEY_STORE_PASSWORD = "KeystorePwd";
    public final static String RAW_KEY_ALIAS = "RawkeyAlias";
    public final static String RAW_KEY_PASSWORD = "RawkeyPwd";

	private static String m_jceName = null;
	private static KeyStore m_keyStore = null;
	private static String  m_keyStroreFileName = null;

    private static Map m_props = null;

	/**
	 * Constructor
	 */
	private TripleDESEncrypter() {
        try {
            // Retreive Triple-DES configuration data
            String jceProvider = (String)m_props.get( JCE_PROVIDER );
            String keystoreLocation = (String)m_props.get( KEY_STORE_LOCATION );
            String keystorePwd = (String)m_props.get( KEY_STORE_PASSWORD );
            String rawkeyAlias = (String)m_props.get( RAW_KEY_ALIAS );
            String rawkeyPwd = (String)m_props.get( RAW_KEY_PASSWORD );

//            m_logger.debug("jceProvider=" + jceProvider);
//            m_logger.debug("keystoreLocation=" + keystoreLocation);
//            m_logger.debug("keystorePwd=" + keystorePwd);
//            m_logger.debug("rawkeyAlias=" + rawkeyAlias);
//            m_logger.debug("RAW_KEY_PASSWORD=" + rawkeyPwd);

            // Add JCE Provider
			if ( ( m_jceName == null || !m_jceName.equals( jceProvider ) )
				&& jceProvider != null )
			{
				Security.addProvider(
						(Provider) Class.forName(jceProvider).newInstance());
				m_jceName = jceProvider;
			}

            // Load keystore
            m_keyStore = loadKeystore(keystoreLocation, keystorePwd);
			m_keyStroreFileName = keystoreLocation;

            // Get private key
            Key key = getPrivateKey( m_keyStore, rawkeyAlias, rawkeyPwd);

            // Generate secret key
            setSecretKey(genSecretKey(key));

            m_logger.info("Triple-DES Encrypter init finished.");

        } catch (Exception e) {
            m_logger.error("Failed to init TripleDESEncrypter.", e);
        }
	}

    /**
     * Initialize the encrypter with the configuration data
     * @param props -  properties name/value pairs
     */
    public synchronized static void initialize( Map props )
    {
        m_props = props;
        encrypter = new TripleDESEncrypter();
    }

	/**
	 * Get singleton reference of this class
	 */
	public synchronized static TripleDESEncrypter getInstance() {
		if (encrypter == null) {
			encrypter = new TripleDESEncrypter();
		}

		return encrypter;
	}

	/**
	 * Get private key from keystore
	 *
	 * @param alias The key alias
	 * @param pwd The key password
	 * @return The private key
	 */
	public PrivateKey getPrivateKey(
		KeyStore keystore,
		String alias,
		String pwd) {
		try {
			// Get private key
			return (PrivateKey) keystore.getKey(alias, pwd.toCharArray());
		} catch (Exception e) {
			m_logger.error("Failed to get private key: " + alias, e);
			return null;
		}
	}

	/**
	 * Get public key from keystore
	 *
	 * @param alias The key alias
	 * @param pwd The key password
	 * @return The public key
	 */
	public PublicKey getPublicKey(
		KeyStore keystore,
		String alias,
		String pwd) {
		try {
			// Get private key
			Key key = keystore.getKey(alias, pwd.toCharArray());
			if (key instanceof PrivateKey) {
				// Get certificate of public key
				java.security.cert.Certificate cert =
					keystore.getCertificate(alias);

				// Get public key
				return cert.getPublicKey();
			} else {
				return null;
			}
		} catch (Exception e) {
			m_logger.error("Failed to get public key: " + alias, e);
			return null;
		}
	}

	/**
	 * load keystore
	 *
	 * @param location The keystore location
	 * @param pwd The keystore password
	 * @return The keystore
	 */
	private KeyStore loadKeystore(String location, String pwd) {
		KeyStore keystore = null;
		FileInputStream is = null;
		try {
			// Load the keystore in the user's home directory
			File file = new File(location);
			is = new FileInputStream(file);
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, pwd.toCharArray());
			is.close();
		} catch (Exception e) {
			m_logger.error("Failed to load keystore from: " + location, e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException ioe) {
				m_logger.error(
					"Failed to close FileInputStream after loading keystore.");
			}
		}

		return keystore;
	}

	/**
	 * Generate secret key
	 *
	 * @param key The private key
	 * @return The secret key
	 */
	private SecretKey genSecretKey(Key key) throws Exception {
		byte[] rawkey = key.getEncoded();
        /*
		System.out.println(
			"Raw key (encoded): "
				+ com.ibm.xml.dsig.util.Base64.encode(rawkey));
        */

		DESedeKeySpec keyspec = new DESedeKeySpec(rawkey);
		SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
		SecretKey secretKey = keyfactory.generateSecret(keyspec);
		return secretKey;
	}

	/**
	 * Encrypt data
	 *
	 * @param msg The data to be encrypted
	 * @return The encrypted text and it's initialization vector
	 */
	public String[] encrypt(String msg) throws Exception {

		Cipher cipher = Cipher.getInstance(CIPHER_TRANS);
		cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());

		// get the initialization vector
		byte[] iv = cipher.getIV();
		byte[] buffer = cipher.doFinal(msg.getBytes());

		String[] result = new String[2];
		result[0] = Base64.encode(buffer);
		result[1] = Base64.encode(iv);

		m_logger.info("Encryption finished.");
//		m_logger.debug("Result: " + result[0]);
		return result;
	}

	/**
	 * Decrypt data
	 *
	 * @param msg The data to be decrypted
	 * @param ivStr The initialization vector
	 * @return The decrypted text
	 */
	public String decrypt(String msg, String ivStr) throws Exception {
		Cipher cipher = Cipher.getInstance(CIPHER_TRANS);

		IvParameterSpec ivp = new IvParameterSpec(Base64.decode(ivStr));
		cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), ivp);

		String result = new String(cipher.doFinal(Base64.decode(msg)));
        m_logger.debug("Decryption finished.");
//		m_logger.debug("Decryption result: " + result);
		return result;
	}

    /**
     * Decrypt data
     *
     * @param msg The data to be decrypted, which include cipher text and IV
     * @return The decrypted text
     */
    public String decrypt( String msg ) throws Exception
    {

        if ( msg == null || msg.indexOf( CIPHER_TEXT_IV_DELIMITER ) == -1 )
        {
            m_logger.warn(" Not combined encrypt string, return orignal string.");
            return msg;
        }
        int place = msg.indexOf( CIPHER_TEXT_IV_DELIMITER );
        String ctext = msg.substring(0, place);
        String ivStr = msg.substring( place + CIPHER_TEXT_IV_DELIMITER.length() );
        Cipher cipher = Cipher.getInstance(CIPHER_TRANS);

        IvParameterSpec ivp = new IvParameterSpec(Base64.decode(ivStr));
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), ivp);

        String result = new String(cipher.doFinal(Base64.decode(ctext)));
        m_logger.debug("Decryption finished.");
//		m_logger.debug("Decryption result: " + result);
        return result;
    }

	/**
	 * Returns the secretKey.
	 * @return SecretKey
	 */
	public synchronized static SecretKey getSecretKey() {
		return secretKey;
	}

	/**
	 * Sets the secretKey.
	 * @param secretKey The secretKey to set
	 */
	public synchronized static void setSecretKey(SecretKey secretKey) {
		TripleDESEncrypter.secretKey = secretKey;
	}

	/**
	 *
	 * @return - the security setting's
	 */
	public static Map getSettings()
	{
		return m_props;
	}

	/**
	 *
	 * @return - key store instance
	 */
	public synchronized static KeyStore getKeyStore()
	{
		return m_keyStore;
	}

	/**
	 *
	 * @return - key store file name
	 */
	public static String getKeyStoreFileName()
	{
		return m_keyStroreFileName;
	}
}
