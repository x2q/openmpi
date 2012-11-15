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

package com.oncecorp.visa3d.bridge.beans;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.oncecorp.visa3d.bridge.utility.JUnitHelper;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

/**
 * <p>Title: </p>
 * <p>Description: This class is used for doing MerchantInfoBean unit test.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class TestMerchantInfoBean extends TestCase {

  public TestMerchantInfoBean(String name) {
    super(name);
  }

  public void testFromProperties() {
    Properties props = new Properties();
    props.setProperty(MerchantInfoBean.ID, "001");
    props.setProperty(MerchantInfoBean.NAME, "name");
    props.setProperty(MerchantInfoBean.DATA_SOURCE_JNDI, "jndi");
    props.setProperty(MerchantInfoBean.JDBC_DRIVER_NAME, "jdbc");
    props.setProperty(MerchantInfoBean.DATABASE_URL, "database url");
    props.setProperty(MerchantInfoBean.DATABASE_USER_NAME, "database username");
    props.setProperty(MerchantInfoBean.DATABASE_PASSWORD, "database password");
    props.setProperty(MerchantInfoBean.SCHEMA_NAME, "database schema");
    props.setProperty(MerchantInfoBean.MERCHANT_PASSWORD, "merchant password");
    props.setProperty(MerchantInfoBean.MERCHANT_URL, "merchant url");
    props.setProperty(MerchantInfoBean.COUNTRY_CODE, "USA");
    props.setProperty(MerchantInfoBean.PURCHASE_CURRENCY, "USD");
    props.setProperty(MerchantInfoBean.PROTOCOL_SUPPORT, "3");
    MerchantInfoBean bean = new MerchantInfoBean();
    bean.fromProperties(props);

    assertTrue(JUnitHelper.isEqual(bean.getId(), "001"));
    assertTrue(JUnitHelper.isEqual(bean.getName(), "name"));
    assertTrue(JUnitHelper.isEqual(bean.getDataSourceJndi(), "jndi"));
    assertTrue(JUnitHelper.isEqual(bean.getJdbcDriverName(), "jdbc"));
    assertTrue(JUnitHelper.isEqual(bean.getDatabaseUrl(), "database url"));
    assertTrue(JUnitHelper.isEqual(bean.getDatabaseUserName(), "database username"));
    assertTrue(JUnitHelper.isEqual(bean.getDatabasePassword(), "database password"));
    assertTrue(JUnitHelper.isEqual(bean.getSchemaName(), "database schema"));
    assertTrue(JUnitHelper.isEqual(bean.getMerchantPassword(), "merchant password"));
    assertTrue(JUnitHelper.isEqual(bean.getMerchantUrl(), "merchant url"));
    assertTrue(JUnitHelper.isEqual(bean.getCountryCode(), "USA"));
    assertTrue(JUnitHelper.isEqual(bean.getPurchaseCurrency(), "USD"));
    assertTrue(JUnitHelper.isEqual(""+bean.getProtocolSupport(), "3"));
  }

  public void testToProperties() {
    MerchantInfoBean bean = new MerchantInfoBean();
    bean.setId("001");
    bean.setName("name");
    bean.setDataSourceJndi("jndi");
    bean.setJdbcDriverName("jdbc");
    bean.setDatabaseUrl("database url");
    bean.setDatabaseUserName("database username");
    bean.setDatabasePassword("database password");
    bean.setSchemaName("database schema");
    bean.setMerchantPassword("merchant password");
    bean.setMerchantUrl("merchant url");
    bean.setCountryCode("USA");
    bean.setPurchaseCurrency("USD");
    bean.setProtocolSupport((byte)3);
    Properties props = bean.toProperties();

    assertTrue(JUnitHelper.isEqual(props.getProperty(MerchantInfoBean.ID), "001"));
    assertTrue(JUnitHelper.isEqual(props.getProperty(MerchantInfoBean.NAME), "name"));
    assertTrue(JUnitHelper.isEqual(props.getProperty(MerchantInfoBean.DATA_SOURCE_JNDI), "jndi"));
    assertTrue(JUnitHelper.isEqual(props.getProperty(MerchantInfoBean.JDBC_DRIVER_NAME), "jdbc"));
    assertTrue(JUnitHelper.isEqual(props.getProperty(MerchantInfoBean.DATABASE_URL), "database url"));
    assertTrue(JUnitHelper.isEqual(props.getProperty(MerchantInfoBean.DATABASE_USER_NAME), "database username"));
    assertTrue(JUnitHelper.isEqual(props.getProperty(MerchantInfoBean.DATABASE_PASSWORD), "database password"));
    assertTrue(JUnitHelper.isEqual(props.getProperty(MerchantInfoBean.SCHEMA_NAME), "database schema"));
    assertTrue(JUnitHelper.isEqual(props.getProperty(MerchantInfoBean.MERCHANT_PASSWORD), "merchant password"));
    assertTrue(JUnitHelper.isEqual(props.getProperty(MerchantInfoBean.MERCHANT_URL), "merchant url"));
    assertTrue(JUnitHelper.isEqual(props.getProperty(MerchantInfoBean.COUNTRY_CODE), "USA"));
    assertTrue(JUnitHelper.isEqual(props.getProperty(MerchantInfoBean.PURCHASE_CURRENCY), "USD"));
    assertTrue(JUnitHelper.isEqual(props.getProperty(MerchantInfoBean.PROTOCOL_SUPPORT), "3"));
  }

  public static Test suite() {
    //return new TestSuite(TestMerchantInfoBean.class);
    TestSuite suite = new TestSuite("Merchant Info Bean Unit Test Suite");
    suite.addTest(new TestMerchantInfoBean("testFromProperties"));
    suite.addTest(new TestMerchantInfoBean("testToProperties"));

    return suite;
  }

  public static void main(String[] args) {
    DataBridgeLoger.initConfigFromFile();
    junit.textui.TestRunner.run(suite());
  }
}