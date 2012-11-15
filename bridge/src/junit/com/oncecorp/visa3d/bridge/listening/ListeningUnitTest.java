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

package com.oncecorp.visa3d.bridge.listening;


import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

import com.oncecorp.visa3d.bridge.beans.MessageFieldBean;
import com.oncecorp.visa3d.bridge.beans.ListeningMessageBean;
import com.oncecorp.visa3d.bridge.beans.BeansHelper;
import com.oncecorp.visa3d.bridge.security.TripleDESEncrypter;
import com.oncecorp.visa3d.bridge.utility.XMLUtils;
import com.oncecorp.visa3d.bridge.beans.MessageMappingBean;
import com.oncecorp.visa3d.bridge.beans.MessageDefinitionBean;
import com.oncecorp.visa3d.bridge.beans.FieldDefinitionBean;
import com.oncecorp.visa3d.bridge.listening.ListeningUtils;
import com.oncecorp.visa3d.bridge.configure.FileHandler;

/**
 * <p>Title: ListeningUnitTest</p>
 * <p>Description: Setup all unit case needed by listening service. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class ListeningUnitTest extends TestCase
{

    public ListeningUnitTest( String name )
    {
        super(name);
    }

    /**
     * Test generate selector string for message filter.
     */
    public void testGetSelectorString()
    {
        ListeningThread lt = new ListeningThread();
        ListeningMessageBean msgBean;

        // Initialize messsage definition table
        MessageFieldsFilter.initMessageTable(
                new FileHandler().getFileParentPath("mpi-messages.xml",
                this.getClass()) );

        ArrayList merchantIDs = new ArrayList();
        HashMap messages = new HashMap();

        /**
         * Test without message type, only merchantIDs
         */
        merchantIDs.add("Merchant1");
        merchantIDs.add("Merchant2");
        merchantIDs.add("Merchant3");

        lt.setMerchantIDs( merchantIDs );
        lt.setMessagesMap( messages );
        assertEquals("MerchantID in ('Merchant1', 'Merchant2', 'Merchant3')",
                     lt.getSelectorString().trim() );
        System.out.println( lt.getSelectorString().trim() );


        /**
         * Test only with message types.
         */
        merchantIDs = new ArrayList();
        messages = new HashMap();

        msgBean = new ListeningMessageBean("PAReq", "1.0.1");
        BeansHelper.addMessage( messages, msgBean );
        msgBean = new ListeningMessageBean("PAReq", "2.0.1");
        BeansHelper.addMessage( messages, msgBean );
        msgBean = new ListeningMessageBean("PARes", "2.0.1");
        BeansHelper.addMessage( messages, msgBean );

        lt.setMerchantIDs( merchantIDs );
        lt.setMessagesMap( messages );
        assertTrue( lt.getSelectorString().trim().indexOf(
                "(MessageType = 'PAReq' AND MessageVersion = '2.0.1')" ) != -1 );
        assertTrue( lt.getSelectorString().trim().indexOf(
                "(MessageType = 'PAReq' AND MessageVersion = '1.0.1')" ) != -1 );
        assertTrue( lt.getSelectorString().trim().indexOf(
                "(MessageType = 'PARes' AND MessageVersion = '2.0.1')" ) != -1 );
        System.out.println( lt.getSelectorString().trim() );

        /**
         * Test with merchant ID's and message types.
         */
        merchantIDs = new ArrayList();
        messages = new HashMap();

        merchantIDs.add("Merchant1");
        merchantIDs.add("Merchant2");
        merchantIDs.add("Merchant3");

        msgBean = new ListeningMessageBean("PAReq", "1.0.1");
        BeansHelper.addMessage( messages, msgBean );
        msgBean = new ListeningMessageBean("PAReq", "2.0.1");
        BeansHelper.addMessage( messages, msgBean );
        msgBean = new ListeningMessageBean("PARes", "2.0.1");
        BeansHelper.addMessage( messages, msgBean );

        lt.setMerchantIDs( merchantIDs );
        lt.setMessagesMap( messages );
        assertTrue( lt.getSelectorString().trim().startsWith("MerchantID in ('Merchant1', 'Merchant2', 'Merchant3') AND ") );
        assertTrue( lt.getSelectorString().trim().indexOf(
                "(MessageType = 'PAReq' AND MessageVersion = '2.0.1')" ) != -1 );
        assertTrue( lt.getSelectorString().trim().indexOf(
                "(MessageType = 'PAReq' AND MessageVersion = '1.0.1')" ) != -1 );
        assertTrue( lt.getSelectorString().trim().indexOf(
                "(MessageType = 'PARes' AND MessageVersion = '2.0.1')" ) != -1 );
        System.out.println( lt.getSelectorString().trim() );

        /**
         * Test with merchant ID's and message types, where some message type is
         * embeded by other message type, check selector string is optimised.
         */
        merchantIDs = new ArrayList();
        messages = new HashMap();

        merchantIDs.add("Merchant1");
        merchantIDs.add("Merchant2");
        merchantIDs.add("Merchant3");

        msgBean = new ListeningMessageBean("PAReq", "1.0.1");
        BeansHelper.addMessage( messages, msgBean );
        msgBean = new ListeningMessageBean("PAReq", "2.0.1");
        BeansHelper.addMessage( messages, msgBean );
        msgBean = new ListeningMessageBean("PARes", "2.0.1");
        BeansHelper.addMessage( messages, msgBean );
        msgBean = new ListeningMessageBean("PARes", "");
        BeansHelper.addMessage( messages, msgBean );

        lt.setMerchantIDs( merchantIDs );
        lt.setMessagesMap( messages );
        assertTrue( lt.getSelectorString().trim().startsWith("MerchantID in ('Merchant1', 'Merchant2', 'Merchant3') AND ") );
        assertTrue( lt.getSelectorString().trim().indexOf(
                "(MessageType = 'PAReq' AND MessageVersion = '2.0.1')" ) != -1 );
        assertTrue( lt.getSelectorString().trim().indexOf(
                "(MessageType = 'PAReq' AND MessageVersion = '1.0.1')" ) != -1 );
        assertTrue( lt.getSelectorString().trim().indexOf(
                "MessageType in ('PARes')" ) != -1 );
        System.out.println( lt.getSelectorString().trim() );
    }

    /**
     * Test transform message functionaties
     */
    public void testTransformMessage()
    {
        String xmlMsg =    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<MPI_Interface>"
        + "<PaymentVerifReq id=\"001\">"
        + "<version>1.0</version>"
        + "<merchantTermURL>http://merchant.com/obcapp</merchantTermURL>"
        + "<merchantData>merchantData</merchantData>"
        + "<pan>123456789012345</pan>"
        + "<panExpiry>0912</panExpiry>"
        + "<purchaseDate>20020101 11:12:12</purchaseDate>"
        + "<purchaseAmount>123.45</purchaseAmount>"
        + "<purchasePurchAmount>12345</purchasePurchAmount>"
        + "<purchaseCurrency>124</purchaseCurrency>"
        + "<purchaseDesc>This is purchaseDesc</purchaseDesc>"
        + "<browserCategory>0</browserCategory>"
        + "<browserAccept>aaa</browserAccept>"
        + "<browserAgent>bbb</browserAgent>"
        + "<purchaseRecur>"
        + "<purchaseFrequency>25000.345</purchaseFrequency>"
        + "<purchaseEndRecur>20100718</purchaseEndRecur>"
        + "</purchaseRecur>"
        + "<purchaseInstall>200</purchaseInstall>"
        + "</PaymentVerifReq> "
        + "</MPI_Interface>";

        // Initialize messsage definition table
        MessageFieldsFilter.initMessageTable(
                new FileHandler().getFileParentPath("mpi-messages.xml",
                this.getClass()) );

        // Initialize TripleDESEncrypter
        HashMap props = new HashMap();
        String path = FileHandler.getFileParentPath("local.keystore", this.getClass())
                      + File.separator + "local.keystore";
        props.put(TripleDESEncrypter.KEY_STORE_LOCATION, path);
        props.put(TripleDESEncrypter.KEY_STORE_PASSWORD, "changeit");
        props.put(TripleDESEncrypter.RAW_KEY_ALIAS, "testkey");
        props.put(TripleDESEncrypter.RAW_KEY_PASSWORD, "changeit");
        props.put(TripleDESEncrypter.JCE_PROVIDER, "com.sun.crypto.provider.SunJCE");

        TripleDESEncrypter.initialize( props );

        //Initialize Message configuration
        HashMap msgMap = new HashMap();
        ListeningMessageBean lmb = new ListeningMessageBean("PaymentVerifReq", "1.0");
        ArrayList list = new ArrayList();
        list.add( new MessageFieldBean("merchantTermURL", "false", "{The merchant URL is {0} }") );
        list.add( new MessageFieldBean("purchaseDate", "false", "{date,yyyy.MM.dd G 'at' HH:mm:ss z}") );
        list.add( new MessageFieldBean("browserCategory", "true", null) );
        list.add( new MessageFieldBean("purchaseRecur/purchaseFrequency", "true",
                                       "{number,$#,###.##}") );
        lmb.setFields( list );
        String key = BeansHelper.getMessageMappingKey("PaymentVerifReq", "1.0");
        msgMap.put(key, lmb );

        ListeningThread lt = new ListeningThread();
        lt.setMessagesMap( msgMap );
		Document fromdoc = XMLUtils.getDocumentFromString( xmlMsg );
        Document doc = lt.transformMsg("PaymentVerifReq", "1.0", fromdoc, new Properties() );
        System.out.print( XMLUtils.toXmlString( doc ) );

        Element item = doc.getDocumentElement();
        Element titem = XMLUtils.findXPathElement( item, "/MPI_Interface/PaymentVerifReq/merchantTermURL");
        assertEquals( XMLUtils.getText( titem ), "The merchant URL is http://merchant.com/obcapp " );
        titem = XMLUtils.findXPathElement( item, "/MPI_Interface/PaymentVerifReq/purchaseDate");
        assertEquals( XMLUtils.getText( titem ), "2002.01.01 AD at 11:12:12 EST" );
        titem = XMLUtils.findXPathElement( item, "/MPI_Interface/PaymentVerifReq/browserCategory");
        String ctext = "";
        try {
            ctext = TripleDESEncrypter.getInstance().decrypt( XMLUtils.getText( titem ) );
        } catch ( Exception e )
        {
            ctext = "";
        }
        assertEquals( ctext, "0" );
        titem = XMLUtils.findXPathElement( item, "/MPI_Interface/PaymentVerifReq/purchaseRecur/purchaseFrequency");
        ctext = "";
        try {
            ctext = TripleDESEncrypter.getInstance().decrypt( XMLUtils.getText( titem ) );
        } catch ( Exception e )
        {
            ctext = "";
        }
        assertEquals( ctext, "$25,000.34" );
        titem = XMLUtils.findXPathElement( item, "/MPI_Interface/PaymentVerifReq/pan");
        ctext = "";
        try {
            ctext = TripleDESEncrypter.getInstance().decrypt( XMLUtils.getText( titem ) );
        } catch ( Exception e )
        {
            ctext = "";
        }
        assertEquals( ctext, "123456789012345" );
        titem = XMLUtils.findXPathElement( item, "/MPI_Interface/PaymentVerifReq/panExpiry");
        assertEquals(  XMLUtils.getText( titem ) , "0912" );
        titem = XMLUtils.findXPathElement( item, "/MPI_Interface/PaymentVerifReq/browserAgent");
        assertNull( titem );

        MessageFieldBean mfb = new MessageFieldBean("pan", "false", "Y{0}#{4}");
        list.add( mfb );
        lt.setMessagesMap( msgMap );
        doc = lt.transformMsg("PaymentVerifReq", "1.0",
							  XMLUtils.getDocumentFromString( xmlMsg ), new Properties() );
        System.out.print( XMLUtils.toXmlString( doc ) );
        item = doc.getDocumentElement();
        titem = XMLUtils.findXPathElement( item, "/MPI_Interface/PaymentVerifReq/pan");
        assertEquals( XMLUtils.getText( titem ) , "YYYYYYYYYYY2345" );

        list.remove( mfb );
        mfb = new MessageFieldBean("pan", "false", null);
        list.add( mfb );
        lt.setMessagesMap( msgMap );
        doc = lt.transformMsg("PaymentVerifReq", "1.0",
							  XMLUtils.getDocumentFromString( xmlMsg ) , new Properties());
        System.out.print( XMLUtils.toXmlString( doc ) );
        item = doc.getDocumentElement();
        titem = XMLUtils.findXPathElement( item, "/MPI_Interface/PaymentVerifReq/pan");
        ctext = "";
        try {
            ctext = TripleDESEncrypter.getInstance().decrypt( XMLUtils.getText( titem ) );
        } catch ( Exception e )
        {
            ctext = "";
        }
        assertEquals( ctext, "123456789012345" );

    }

    /**
     * Test message field filter functionalities
     */
    public void testMessageFieldFilter()
    {
        // Initialize messsage definition table
        MessageFieldsFilter.initMessageTable(
                new FileHandler().getFileParentPath("mpi-messages.xml",
                this.getClass()) );
        MessageMappingBean bean = MessageFieldsFilter.getMessageMappingBean();
        System.out.print( "\r\n\r\n" + bean.toXml() );

        MessageMappingBean bean2 = BeansHelper.messageMappingFromXml( null, bean.toXml() );

        System.out.print(  "\r\n\r\n" + bean2.toXml() );

        Map map1 = bean.getMessages();
        Map map2 = bean2.getMessages();
        List list1 = bean.getFieldsList("PaymentVerifReq", "1.0");
        List list2 = bean2.getFieldsList("PaymentVerifReq", "1.0");
        assertEquals( map1.size(), map2.size() );
        //assertEquals( list1.size(), 19);
        assertEquals( list1.size(), list2.size() );

        MessageDefinitionBean mdb = bean.getMessageDefinition("PaymentVerifReq", "1.0");
        assertEquals( mdb.getXpath(), "/MPI_Interface/PaymentVerifReq");
        assertEquals( mdb.getType(), "PaymentVerifReq");
        assertEquals( mdb.getVersion(), "1.0");

        FieldDefinitionBean fdb = mdb.getFieldFromXpath("pan");
        assertTrue( fdb.isMandatory() );
        assertTrue( fdb.isMustEncryption() );
        fdb = mdb.getFieldFromXpath("PurchaseRecur/endRecur");
        assertTrue( !fdb.isMandatory() );
        assertTrue( !fdb.isMustEncryption() );

        mdb = bean2.getMessageDefinition("PaymentVerifReq", "1.0");
        assertEquals( mdb.getXpath(), "/MPI_Interface/PaymentVerifReq");
        assertEquals( mdb.getType(), "PaymentVerifReq");
        assertEquals( mdb.getVersion(), "1.0");

        fdb = mdb.getFieldFromXpath("pan");
        assertTrue( fdb.isMandatory() );
        assertTrue( fdb.isMustEncryption() );
        fdb = mdb.getFieldFromXpath("PurchaseRecur/endRecur");
        assertTrue( !fdb.isMandatory() );
        assertTrue( !fdb.isMustEncryption() );

        String xmlMsg =         "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<ThreeDSecure>"
            + "<Message id = \"999\">"
            + "<PARes id=\"#PA2157205452125\">"
            + "<version>1.0.1</version>"
            + "<Merchant>"
            + "<acqBIN>199876</acqBIN>"
            + "<merID>merID</merID>"
            + "</Merchant>"
            + "<Purchase>"
            + "<xid>This is a 28 byte long value</xid>"
            + "<date>20020806 10:10:10</date>"
            + "<purchAmount>123456.789</purchAmount>"
            + "<currency>0124456789</currency>"
            + "<exponent>2</exponent>"
            + "<desc>Just for Test</desc>"
            + "</Purchase>"
            + "<pan>0000000000000000</pan>"
            + "<TX>"
            + "<time>20020806 11:11:11</time>"
            + "<status>N</status>"
            + "<eci>eci</eci>"
            + "</TX>"
            + "</PARes>"
            + "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"
            + "<SignedInfo>"
            + "<CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>"
            + "<SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/>"
            + "<Reference URI=\"#PA2157205452125\">"
            + "<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>"
            + "<DigestValue>/EBXwBFRS5idR7aYOgvMsQHL6sY=</DigestValue>"
            + "</Reference>"
            + "</SignedInfo>"
            + "<SignatureValue>b/Khejqq1CZViUXaC/G7emdO2CxJkwKSwCkM2pns0TVO6PFdQxWdmilWoTvjIbotrSRPXeWBZ7P1xIwFDoIJZauHiZLZSfvzK5wUcQZ98E6kXNrzTqnLRYRX7zkLH5UUXJJ/bfBWoNUzZ5obxr2OpA6Gs+vXIA0RKacbS4ARZnM=</SignatureValue>"
            + "<KeyInfo>"
            + "<X509Data xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"
            + "<X509Certificate>MIICsDCCAhmgAwIBAgIIRnP8mDwESl0wDQYJKoZIhvcNAQEFBQAwYDELMAkGA1UEBhMCVVMxEDAOBgNVBAoTB0NhcmFkYXMxLDAqBgNVBAsTIzMtRCBTZWN1cmUgQ29tcGxpYW5jZSBUZXN0IEZhY2lsaXR5MREwDwYDVQQDEwhDVEhfUk9PVDAeFw0wMjA0MjIyMDU0MDRaFw0wMzA0MjIyMDU0MDRaMIHCMQswCQYDVQQGEwJDWTEQMA4GA1UECBMHTmljb3NpYTEQMA4GA1UEBxMHTmljb3NpYTEjMCEGA1UEChMaQ2FyZCBUZWNoIFNlcnZpY2VzIExpbWl0ZWQxITAfBgNVBAsTGFdlYiBTb2x1dGlvbnMgRGVwYXJ0bWVudDEpMCcGCSqGSIb3DQEJARYadGhlb2Rvcm9zLnNhdnZpZGVzQGN0bC5jb20xHDAaBgNVBAMTE0NUTCBTRU5UUlkgMS4wIFNpZ24wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAOsHniQ0r0wyCLk1lm6Egh/TAhp0hU+8oxMc1IqG4t9+p0ZFK1HgH94wm2yLcXolgXgJxhGkX2iBczRmbP9uPi7pRObjcaw8BfGxZrrQb3f1sOf1yP7B9HFpIavstMaERU/KJKiDliIagyKv2Itm4NwMoyP1G6LtAQ9kewaSi5bNAgMBAAGjEDAOMAwGA1UdEwEB/wQCMAAwDQYJKoZIhvcNAQEFBQADgYEAZdYMZ+p02r6oDnnZKIajtI5zxYfIcIiTZsKSMYd4QZe9TzEC02z9uca4MpJgl50i5DPFky+YSXD9OOSVDWBW2fp4fF657x8pfx+TktYrMU2pQZp1tDp+gNIZ5K5bxrt0wXGDLkUaTg+XwEtLRCI8ltQqRy0Iy8msPIdSW408oEQ=</X509Certificate>"
            + "</X509Data>"
            + "</KeyInfo>"
            + "</Signature>"
            + "</Message>"
            + "</ThreeDSecure>";

        Document doc = XMLUtils.getDocumentFromString( xmlMsg );

        // Test Extract Fields
        ArrayList list = new ArrayList();
        MessageFieldsFilter.extractFields("PARes", "1.0.1", null, doc );

        Element item = doc.getDocumentElement();
        Element titem = XMLUtils.findXPathElement( item, "/ThreeDSecure/PARes/TX/status");
        assertEquals( XMLUtils.getText( titem ), "N" );
        titem = XMLUtils.findXPathElement( item, "TX/eci");
        assertEquals( XMLUtils.getText( titem ), "eci" );
        titem = XMLUtils.findXPathElement( item, "/ThreeDSecure/Signature/SignedInfo");
        assertNotNull( titem );

        doc = XMLUtils.getDocumentFromString( xmlMsg );
        item = doc.getDocumentElement();
        list = new ArrayList();
        list.add( new MessageFieldBean("Purchase/desc", "false", null) );
        MessageFieldsFilter.extractFields("PARes", "1.0.1", list, doc );
        titem = XMLUtils.findXPathElement( item, "Purchase/desc");
        assertEquals( XMLUtils.getText( titem ), "Just for Test" );
        titem = XMLUtils.findXPathElement( item, "TX/eci");
        assertNull( titem );
        titem = XMLUtils.findXPathElement( item, "pan");
        assertEquals( XMLUtils.getText( titem ), "0000000000000000" );

        // Test Encryption
        doc = XMLUtils.getDocumentFromString( xmlMsg );
        item = doc.getDocumentElement();
        list = new ArrayList();
        list.add( new MessageFieldBean("Purchase/desc", "true", null) );
        MessageFieldsFilter.encryptFields("PARes", "1.0.1", list, doc );
        titem = XMLUtils.findXPathElement( item, "Purchase/desc");

        String ctext = "";
        try {
            ctext = TripleDESEncrypter.getInstance().decrypt( XMLUtils.getText( titem ) );
        } catch ( Exception e )
        {
            ctext = "";
        }
        assertEquals(  ctext, "Just for Test" );

        titem = XMLUtils.findXPathElement( item, "pan");

        ctext = "";
        try {
            ctext = TripleDESEncrypter.getInstance().decrypt( XMLUtils.getText( titem ) );
        } catch ( Exception e )
        {
            ctext = "";
        }
        assertEquals(  ctext, "0000000000000000" );

        // Test masking
        doc = XMLUtils.getDocumentFromString( xmlMsg );
        item = doc.getDocumentElement();
        list = new ArrayList();
        list.add( new MessageFieldBean("Merchant/merID", "false", "{The merchant identify is {0} }") );
        list.add( new MessageFieldBean("Purchase/date", "false", "{date,yyyy.MM.dd G 'at' HH:mm:ss z}") );
        list.add( new MessageFieldBean("Purchase/purchAmount", "false",
                                       "{number,$#,###.##}") );
        list.add( new MessageFieldBean("pan", "false", "Y{0}#{4}") );
        list.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        list.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        MessageFieldsFilter.maskFields("PARes", "1.0.1", list, doc );

        titem = XMLUtils.findXPathElement( item, "Merchant/merID");
        assertEquals( XMLUtils.getText( titem ), "The merchant identify is merID " );
        titem = XMLUtils.findXPathElement( item, "Purchase/date");
        assertEquals( XMLUtils.getText( titem ), "2002.08.06 AD at 10:10:10 EDT" );
        titem = XMLUtils.findXPathElement( item, "Purchase/purchAmount");
        assertEquals( XMLUtils.getText( titem ), "$123,456.79" );
        titem = XMLUtils.findXPathElement( item, "pan");
        assertEquals( XMLUtils.getText( titem ), "YYYYYYYYYYYY0000" );
        titem = XMLUtils.findXPathElement( item, "Purchase/currency");
        assertEquals( XMLUtils.getText( titem ), "AA24XXX78Y" );
        titem = XMLUtils.findXPathElement( item, "Merchant/acqBIN");
        assertEquals( XMLUtils.getText( titem ), "1998XX" );
    }

    /**
     * Test listening Utility functionaliaties
     */
    public void testListeningUtils()
    {
        // Test Merchant List Equal
        ArrayList list1 = new ArrayList();
        list1.add("M1");
        list1.add("M2");
        list1.add("M3");
        ArrayList list2 = new ArrayList();
        list2.add("M1");
        list2.add("M3");
        list2.add("M2");
        assertTrue( ListeningUtils.merchantListEqual( list1, list2 ) );

        list1 = new ArrayList();
        list1.add("M1");
        list1.add("M2");
        list1.add("M3");
        list2 = new ArrayList();
        list2.add("M1");
        assertTrue( !ListeningUtils.merchantListEqual( list1, list2 ) );

        list1 = new ArrayList();
        list1.add("M1");
        list1.add("M2");
        list1.add("M3");
        list2 = new ArrayList();
        list2.add("M1");
        list2.add("M4");
        list2.add("M3");
        assertTrue( !ListeningUtils.merchantListEqual( list1, list2 ) );

        list1 = new ArrayList();
        list1.add("M1");
        list1.add("M2");
        list1.add("M3");
        assertTrue( !ListeningUtils.merchantListEqual( list1, null ) );

        assertTrue( ListeningUtils.merchantListEqual( null, null ) );

        list1 = new ArrayList();
        list1.add("M1");
        list1.add("M2");
        list1.add("M3");
        list2 = new ArrayList();
        list2.add("M1");
        assertTrue( !ListeningUtils.merchantListEqual( list1, list2 ) );

        // Test Field List Equal
        list1 = new ArrayList();
        list1.add( new MessageFieldBean("pan", "false", "Y{0}#{4}") );
        list1.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        list1.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        list2 = new ArrayList();
        list2.add( new MessageFieldBean("pan", "false", "Y{0}#{4}") );
        list2.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        list2.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        assertTrue( ListeningUtils.fieldsListEqual( list1, list2 ) );

        list1 = new ArrayList();
        list1.add( new MessageFieldBean("pan", "false", "Y{0}#{4}") );
        list1.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        list1.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        list2 = new ArrayList();
        list2.add( new MessageFieldBean("pan", "true", "Y{0}#{4}") );
        list2.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        list2.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        assertTrue( !ListeningUtils.fieldsListEqual( list1, list2 ) );

        list1 = new ArrayList();
        list1.add( new MessageFieldBean("pan", "false", "Y{0}#{4}") );
        list1.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        list1.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        list2 = new ArrayList();
        list2.add( new MessageFieldBean("pan", "false", "Y{0}#{3}") );
        list2.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        list2.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        assertTrue( !ListeningUtils.fieldsListEqual( list1, list2 ) );

        list1 = new ArrayList();
        list1.add( new MessageFieldBean("pan", "false", "Y{0}#{4}") );
        list1.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        list1.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        list2 = new ArrayList();
        list2.add( new MessageFieldBean("pan", "false", "Y{0}#{4}") );
        list2.add( new MessageFieldBean("Purchase/purchAmount", "false", "#{4}X{0}") );
        list2.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        assertTrue( !ListeningUtils.fieldsListEqual( list1, list2 ) );

        list1 = new ArrayList();
        list1.add( new MessageFieldBean("pan", "false", "Y{0}#{4}") );
        list1.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        list1.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        assertTrue( !ListeningUtils.fieldsListEqual( list1, null ) );

        assertTrue( ListeningUtils.fieldsListEqual( null, null ) );

        list1 = new ArrayList();
        list1.add( new MessageFieldBean("pan", "false", "Y{0}#{4}") );
        list1.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        list1.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        list2 = new ArrayList();
        list2.add( new MessageFieldBean("pan", "false", "Y{0}#{4}") );
        assertTrue( !ListeningUtils.fieldsListEqual( list1, list2 ) );

        // Test MessageMapEqual
        ListeningMessageBean lmb;
        HashMap msgMap1 = new HashMap();
        lmb = new ListeningMessageBean("PARes", "1.0");
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PAReq", "1.0");
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PaymentVerifReq", "1.0.2");
        BeansHelper.addMessage( msgMap1, lmb );
        HashMap msgMap2 = new HashMap();
        lmb = new ListeningMessageBean("PARes", "1.0");
        BeansHelper.addMessage( msgMap2, lmb );
        lmb = new ListeningMessageBean("PaymentVerifReq", "1.0.2");
        BeansHelper.addMessage( msgMap2, lmb );
        lmb = new ListeningMessageBean("PAReq", "1.0");
        BeansHelper.addMessage( msgMap2, lmb );
        assertTrue( ListeningUtils.messageMapEqual( msgMap1, msgMap2, false) );

        msgMap1 = new HashMap();
        lmb = new ListeningMessageBean("PARes", "1.0");
        list1 = new ArrayList();
        list1.add( new MessageFieldBean("pan", "false", "Y{0}#{4}") );
        list1.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        list1.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        lmb.setFields( list1 );
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PAReq", "1.0");
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PaymentVerifReq", "1.0.2");
        BeansHelper.addMessage( msgMap1, lmb );
        msgMap2 = new HashMap();
        lmb = new ListeningMessageBean("PARes", "1.0");
        list2 = new ArrayList();
        list2.add( new MessageFieldBean("pan", "false", "Y{0}#{4}") );
        list2.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        list2.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        lmb.setFields( list2 );
        BeansHelper.addMessage( msgMap2, lmb );
        lmb = new ListeningMessageBean("PaymentVerifReq", "1.0.2");
        BeansHelper.addMessage( msgMap2, lmb );
        lmb = new ListeningMessageBean("PAReq", "1.0");
        BeansHelper.addMessage( msgMap2, lmb );
        assertTrue( ListeningUtils.messageMapEqual( msgMap1, msgMap2, true) );

        msgMap1 = new HashMap();
        lmb = new ListeningMessageBean("PARes", "1.0");
        list1 = new ArrayList();
        list1.add( new MessageFieldBean("pan", "true", "Y{0}#{4}") );
        list1.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        list1.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        lmb.setFields( list1 );
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PAReq", "1.0");
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PaymentVerifReq", "1.0.2");
        BeansHelper.addMessage( msgMap1, lmb );
        msgMap2 = new HashMap();
        lmb = new ListeningMessageBean("PARes", "1.0");
        list2 = new ArrayList();
        list2.add( new MessageFieldBean("pan", "false", "Y{0}#{4}") );
        list2.add( new MessageFieldBean("Merchant/acqBIN", "false", "#{4}X{0}") );
        list2.add( new MessageFieldBean("Purchase/currency", "false", "A{2}#{2}X{0}#{2}Y{1}") );
        lmb.setFields( list2 );
        BeansHelper.addMessage( msgMap2, lmb );
        lmb = new ListeningMessageBean("PaymentVerifReq", "1.0.2");
        BeansHelper.addMessage( msgMap2, lmb );
        lmb = new ListeningMessageBean("PAReq", "1.0");
        BeansHelper.addMessage( msgMap2, lmb );
        assertTrue( !ListeningUtils.messageMapEqual( msgMap1, msgMap2, true) );

        msgMap1 = new HashMap();
        lmb = new ListeningMessageBean("PARes", "1.0");
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PAReq", "1.0");
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PaymentVerifReq", "1.0.2");
        BeansHelper.addMessage( msgMap1, lmb );
        msgMap2 = new HashMap();
        lmb = new ListeningMessageBean("PARes", "1.0");
        BeansHelper.addMessage( msgMap2, lmb );
        lmb = new ListeningMessageBean("PaymentVerifReq", "1.0.1");
        BeansHelper.addMessage( msgMap2, lmb );
        lmb = new ListeningMessageBean("PAReq", "1.0");
        BeansHelper.addMessage( msgMap2, lmb );
        assertTrue( !ListeningUtils.messageMapEqual( msgMap1, msgMap2, false) );

        msgMap1 = new HashMap();
        lmb = new ListeningMessageBean("PARes", "1.0");
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PAReq", "1.0");
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PaymentVerifReq", "1.0.2");
        BeansHelper.addMessage( msgMap1, lmb );
        msgMap2 = new HashMap();
        lmb = new ListeningMessageBean("PARes", "1.0");
        BeansHelper.addMessage( msgMap2, lmb );
        lmb = new ListeningMessageBean("PaymentVerifRes", "1.0.2");
        BeansHelper.addMessage( msgMap2, lmb );
        lmb = new ListeningMessageBean("PAReq", "1.0");
        BeansHelper.addMessage( msgMap2, lmb );
        assertTrue( !ListeningUtils.messageMapEqual( msgMap1, msgMap2, false) );

        msgMap1 = new HashMap();
        lmb = new ListeningMessageBean("PARes", "1.0");
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PAReq", "1.0");
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PaymentVerifReq", "1.0.2");
        BeansHelper.addMessage( msgMap1, lmb );
        msgMap2 = new HashMap();
        lmb = new ListeningMessageBean("PARes", "1.0");
        BeansHelper.addMessage( msgMap2, lmb );
        lmb = new ListeningMessageBean("PAReq", "1.0");
        BeansHelper.addMessage( msgMap2, lmb );
        assertTrue( !ListeningUtils.messageMapEqual( msgMap1, msgMap2, false) );

        msgMap1 = new HashMap();
        lmb = new ListeningMessageBean("PARes", "1.0");
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PAReq", "1.0");
        BeansHelper.addMessage( msgMap1, lmb );
        lmb = new ListeningMessageBean("PaymentVerifReq", "1.0.2");
        BeansHelper.addMessage( msgMap1, lmb );
        assertTrue( !ListeningUtils.messageMapEqual( msgMap1, null, false) );

        assertTrue( ListeningUtils.messageMapEqual( null, null, false) );

    }

    /**
     *
     * @return -  Test suit for listening service.
     */
    public static Test suite() {
        System.out.println("If you got exception, Please put local.keystore and mpi-messages.xml files under your current working directory");
        System.out.println("Your current working directory is: " + System.getProperty("user.dir") );

        TestSuite suite = new TestSuite("Listening Service Unit Test Suite");
        suite.addTest(new ListeningUnitTest("testGetSelectorString"));
        suite.addTest(new ListeningUnitTest("testTransformMessage"));
        suite.addTest(new ListeningUnitTest("testMessageFieldFilter"));
        suite.addTest(new ListeningUnitTest("testListeningUtils"));

        return suite;
    }

}
