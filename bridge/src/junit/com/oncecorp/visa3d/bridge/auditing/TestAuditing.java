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

package com.oncecorp.visa3d.bridge.auditing;

import com.oncecorp.visa3d.bridge.beans.AuditingListenerBean;
import com.oncecorp.visa3d.bridge.beans.AuditingServiceBean;
import com.oncecorp.visa3d.bridge.beans.DataBridgeBean;
import com.oncecorp.visa3d.bridge.beans.MerchantInfoBean;
import com.oncecorp.visa3d.bridge.configure.AuditingConfig;
import com.oncecorp.visa3d.bridge.configure.ConfigurationManager;
import com.oncecorp.visa3d.bridge.utility.XMLUtils;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import java.util.Hashtable;
//import javax.naming.Context;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class TestAuditing extends TestCase {

//  private Hashtable      props;
  private Document       configDoc;
  private DataBridgeBean dbBean;
  private Auditing       auditing;

  public TestAuditing(String name) {
    super(name);
  }

  protected void setUp() {
    init();
  }

  protected void tearDown() {
    configDoc = null;
  }

  private void init() {
    // configure the auditing service.
    try {
      configDoc = DocumentBuilderFactory.
        newInstance().newDocumentBuilder().
        parse(AuditingServiceBean.class.getResourceAsStream("mpi-databridge.xml"));

      dbBean = new DataBridgeBean();
      dbBean.fromXml(XMLUtils.toXmlString(configDoc));
      AuditingConfig ac = new AuditingConfig();
      ac.config(dbBean);
    }
    catch (Exception e) {

    }

    ConfigurationManager.getInstance().setAutoSave(false);

    // create auditing MBean object.
    auditing = new Auditing();
  }

  private static final String NEW_HANDLER_ID = "handler-N";
  private static final String NEW_MERCHANT_ID = "Merchant-N";

  private void addNewLogger() {
    AuditingListenerBean lBean = new AuditingListenerBean();
    lBean.setId(NEW_HANDLER_ID);
    lBean.setMaxRows(1000);
    lBean.setThreshold(700);
    lBean.setMailNotify(true);
    lBean.addMerchant(NEW_MERCHANT_ID);
    lBean.setSender("info@oncecorp.com");
    lBean.setTo("yge@oncecorp.com");
    lBean.setEmailTemplate("TemplateHandler1.xml");
    auditing.addAuditingLogger(lBean.toXml());
  }

  private static final String            TEST_LOGGER_ID = "handler1";

  private AuditingListenerBean createOriginalBean() {
    String xml_0 = AuditingManager.getInstance().getLogger(TEST_LOGGER_ID).getBean().toXml();
    AuditingListenerBean lBean = new AuditingListenerBean();
    lBean.fromXml(xml_0);
    return lBean;
  }

  public void testUpdateMaxRows() {
    AuditingListenerBean lBean = createOriginalBean();
    lBean.setMaxRows(500);
    String xml = lBean.toXml();
    auditing.updateAuditingLogger(xml);
    assertEquals(500, AuditingManager.getInstance().getLogger(TEST_LOGGER_ID).getBean().getMaxRows());
  }

  public void testUpdateThreshold() {
    AuditingListenerBean lBean = createOriginalBean();
    lBean.setThreshold(400);
    String xml = lBean.toXml();
    auditing.updateAuditingLogger(xml);
    assertEquals(400, AuditingManager.getInstance().getLogger(TEST_LOGGER_ID).getBean().getThreshold());
  }

  public void testUpdateNotifyTrue() {
    AuditingListenerBean lBean = createOriginalBean();
    lBean.setMailNotify(true);
    String xml = lBean.toXml();
    auditing.updateAuditingLogger(xml);
    assertTrue(AuditingManager.getInstance().getLogger(TEST_LOGGER_ID).getBean().isMailNotify());
  }

  public void testUpdateNotifyFalse() {
    AuditingListenerBean lBean = createOriginalBean();
    lBean.setMailNotify(false);
    String xml = lBean.toXml();
    auditing.updateAuditingLogger(xml);
    assertTrue(!AuditingManager.getInstance().getLogger(TEST_LOGGER_ID).getBean().isMailNotify());
  }

  public void testUpdatePeriod() {
    AuditingListenerBean lBean = createOriginalBean();
    lBean.setPeriod("12:00:00");
    String xml = lBean.toXml();
    auditing.updateAuditingLogger(xml);
    assertTrue(AuditingManager.getInstance().getLogger(TEST_LOGGER_ID).getBean().getPeriod().equals("12:00:00"));
  }

  public void testUpdateMailSender() {
    AuditingListenerBean lBean = createOriginalBean();
    lBean.setSender("yge@oncecorp.com");
    String xml = lBean.toXml();
    auditing.updateAuditingLogger(xml);
    assertTrue(AuditingManager.getInstance().getLogger(TEST_LOGGER_ID).getBean().getSender().equals("yge@oncecorp.com"));
  }

  public void testUpdateMailTo() {
    AuditingListenerBean lBean = createOriginalBean();
    lBean.setTo("yge@oncecorp.com");
    String xml = lBean.toXml();
    auditing.updateAuditingLogger(xml);
    assertTrue(AuditingManager.getInstance().getLogger(TEST_LOGGER_ID).getBean().getTo().equals("yge@oncecorp.com"));
  }

  public void testUpdateMailCc() {
    AuditingListenerBean lBean = createOriginalBean();
    lBean.setCc("yge@oncecorp.com");
    String xml = lBean.toXml();
    auditing.updateAuditingLogger(xml);
    assertTrue(AuditingManager.getInstance().getLogger(TEST_LOGGER_ID).getBean().getCc().equals("yge@oncecorp.com"));
  }

  public void testDeleteMerchant() {
    MerchantInfo merchantMBean = new MerchantInfo();
    merchantMBean.delete(NEW_MERCHANT_ID);

    MerchantInfoBean merchant = new MerchantInfoBean();
    merchant.setId(NEW_MERCHANT_ID);
    merchant.setDataSourceJndi(NEW_MERCHANT_ID);
    merchant.setJdbcDriverName("com.mysql.jdbc.Driver");
    merchant.setDatabaseUrl("jdbc:mysql://localhost/DataBridge");
    merchant.setDatabaseUserName("");
    merchant.setDatabasePassword("");
    merchantMBean.create(merchant);

    merchantMBean.delete(NEW_MERCHANT_ID);
    assertNull(merchantMBean.retrieve(NEW_MERCHANT_ID));
  }

  public static Test suite() {
    TestSuite suite = new TestSuite("Auditing Unit Test Suite");
    suite.addTest(new TestAuditing("testUpdateMaxRows"));
    suite.addTest(new TestAuditing("testUpdateThreshold"));
    suite.addTest(new TestAuditing("testUpdateNotifyTrue"));
    suite.addTest(new TestAuditing("testUpdateNotifyFalse"));
    suite.addTest(new TestAuditing("testUpdatePeriod"));
    suite.addTest(new TestAuditing("testUpdateMailSender"));
    suite.addTest(new TestAuditing("testUpdateMailTo"));
    suite.addTest(new TestAuditing("testUpdateMailCc"));
    suite.addTest(new TestAuditing("testDeleteMerchant"));

    return suite;
  }

  public static void main(String[] args) {
    DataBridgeLoger.initConfigFromFile();
    junit.textui.TestRunner.run(suite());
  }
}