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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.oncecorp.visa3d.bridge.configure.FileHandler;

import java.util.HashMap;
import java.io.File;

/**
 * <p>Title: SecurityUnitTest</p>
 * <p>Description: Setup all unit case needed by security service. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */


public class SecurityUnitTest  extends TestCase
{

    public SecurityUnitTest( String name )
    {
        super(name);
    }

    /**
     * Test TripleDESEncrypter's main methods
     */
    public void testTripleDESEncrypter()
    {
        String text =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<ThreeDSecure>"
                + "<Message id=\"999\">"
                + "<PAReq>"
                + "<version>1.0.1</version>"
                + "<Merchant>"
                + "<acqBIN>1271722</acqBIN>"
                + "<merID>merID</merID>"
                + "<name>Merchant Name</name>"
                + "<country>CAN</country>"
                + "<url>http://merchant.com</url>"
                + "</Merchant>"
                + "<Purchase>"
                + "<xid>This is a 28 byte long value</xid>"
                + "<date>20020806 10:10:10</date>"
                + "<amount>123.45</amount>"
                + "<purchAmount>12345</purchAmount>"
                + "<currency>124</currency>"
                + "<exponent>2</exponent>"
                + "<desc>This is description</desc>"
                + "<Recur>"
                + "<frequency>25</frequency>"
                + "<endRecur>1111</endRecur>"
                + "</Recur>"
                + "<install>200</install>"
                + "</Purchase>"
                + "<CH>"
                + "<acctID>acctID1234</acctID>"
                + "<expiry>0804</expiry>"
                + "</CH>"
                + "<Extension id=\"http://www.oncecorp.com/visa3d/ProfileSpec.html\" critical=\"false\">ProfileSupport</Extension>"
                + "</PAReq>"
                + "</Message>"
                + "</ThreeDSecure>";

        HashMap props = new HashMap();
        String path = FileHandler.getFileParentPath("local.keystore", this.getClass())
                      + File.separator + "local.keystore";
        props.put(TripleDESEncrypter.KEY_STORE_LOCATION, path);
        props.put(TripleDESEncrypter.KEY_STORE_PASSWORD, "changeit");
        props.put(TripleDESEncrypter.RAW_KEY_ALIAS, "testkey");
        props.put(TripleDESEncrypter.RAW_KEY_PASSWORD, "changeit");
        props.put(TripleDESEncrypter.JCE_PROVIDER, "com.sun.crypto.provider.SunJCE");

        TripleDESEncrypter.initialize( props );

        // Get encrypter instance
        TripleDESEncrypter encrypter = TripleDESEncrypter.getInstance();
        assertNotNull( encrypter );

        // Encrypt sample message text
        String[] result = null;
        try {
            result = encrypter.encrypt(text);
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }

        assertNotNull( result );
        assertEquals( result.length, 2 );

        System.out.println("encryption done.");
        System.out.println("encrypted text: " + result[0]);
        System.out.println("IV: " + result[1]);

        // Decryption
        String afterDecryption = null;
        try {
            afterDecryption = encrypter.decrypt(result[0], result[1]);
        } catch (Exception e) {
            e.printStackTrace();
            afterDecryption = null;
        }

        assertNotNull( afterDecryption );

        System.out.println("decryption done.");
        System.out.println("decrypted text: " + afterDecryption);

        assertEquals( afterDecryption, text );

        // Test decrypt with combined cipther text
        try {
            afterDecryption = encrypter.decrypt( result[0]
                    + TripleDESEncrypter.CIPHER_TEXT_IV_DELIMITER + result[1] );
        } catch (Exception e) {
            e.printStackTrace();
            afterDecryption = null;
        }

        assertNotNull( afterDecryption );

        System.out.println("decryption done.");
        System.out.println("decrypted text: " + afterDecryption);

        assertEquals( afterDecryption, text );

    }

    /**
     *
     * @return -  Test suit for listening service.
     */
    public static Test suite() {
        System.out.println("If you got exception, Please put local.keystore file under your current working directory");
       System.out.println("Your current working directory is: " + System.getProperty("user.dir") );
       TestSuite suite = new TestSuite("Security Service Unit Test Suite");
        suite.addTest(new SecurityUnitTest("testTripleDESEncrypter"));

        return suite;
    }


}