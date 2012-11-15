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

import com.oncecorp.visa3d.bridge.utility.XMLUtils;
import com.oncecorp.visa3d.bridge.utility.JUnitHelper;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class TestMessageMappingBean extends TestCase {

  public TestMessageMappingBean(String name) {
    super(name);
  }

  public void testFromXml() {
    String type = "CRReq";
    String version = "1.0.1";
    Document doc = JUnitHelper.getDocument(getClass(), "mpi-messages.xml");
    String xml = XMLUtils.toXmlString(doc);
    MessageMappingBean mmbean = new MessageMappingBean();
    mmbean.fromXml(xml);
    Map messages = mmbean.getMessages();

    NodeList nodes = doc.getElementsByTagName("message");
    assertNotNull(nodes);
    for (int i = 0; i < nodes.getLength(); i++) {
      Element msg = (Element)nodes.item(i);
      if ( !type.equals(msg.getAttribute("type")) ) {
        continue;
      }
      String beanKey = BeansHelper.getMessageMappingKey(type, version);
      MessageDefinitionBean mdbean = (MessageDefinitionBean)messages.get(beanKey);
      assertNotNull(mdbean);
      assertTrue(JUnitHelper.isEqual(msg.getAttribute("xpath"), mdbean.getXpath()));
      NodeList fields = msg.getElementsByTagName("field");
      assertNotNull(fields);
      assertEquals(mdbean.getFields().size(), fields.getLength());
      for (int j = 0; j < fields.getLength(); j++) {
        Element fld = (Element)fields.item(j);
        String xpath = XMLUtils.getText(fld);
        assertNotNull(xpath);
        boolean mandatory = new Boolean(fld.getAttribute("mandatory")).booleanValue();
        boolean mustEncryption= new Boolean(fld.getAttribute("mustEncryption")).booleanValue();
        FieldDefinitionBean fdbean = mdbean.getFieldFromXpath(xpath);
        assertNotNull(fdbean);
        assertEquals(fdbean.isMandatory(), mandatory);
        assertEquals(fdbean.isMustEncryption(), mustEncryption);
      }
    }
  }

  public void testToXml() {
    String type = "CRReq";
    String version = "1.0.1";
    String beanKey = BeansHelper.getMessageMappingKey(type, version);

    Document doc = JUnitHelper.getDocument(getClass(), "mpi-messages.xml");
    String xml = XMLUtils.toXmlString(doc);
    MessageMappingBean mmbean = new MessageMappingBean();
    mmbean.fromXml(xml);
    MessageDefinitionBean mdbean = (MessageDefinitionBean)mmbean.getMessages().get(beanKey);

    String xml0 = mmbean.toXml();
    MessageMappingBean mmbean0 = new MessageMappingBean();
    mmbean0.fromXml(xml0);
    MessageDefinitionBean mdbean0 = (MessageDefinitionBean)mmbean0.getMessages().get(beanKey);

    assertEquals(mdbean.getFields().size(), mdbean0.getFields().size());
    for (Iterator i = mdbean.getFields().iterator(); i.hasNext(); ) {
      FieldDefinitionBean fdbean = (FieldDefinitionBean)i.next();
      FieldDefinitionBean fdbean0 = mdbean0.getFieldFromXpath(fdbean.getXpath());
      assertNotNull(fdbean0);
      assertEquals(fdbean.isMandatory(), fdbean0.isMandatory());
      assertEquals(fdbean.isMustEncryption(), fdbean0.isMustEncryption());
    }
  }

  public static Test suite() {
    //return new TestSuite(TestMessageMappingBean.class);
    TestSuite suite = new TestSuite("Message Mapping Bean Unit Test Suite");
    suite.addTest(new TestMessageMappingBean("testFromXml"));
    suite.addTest(new TestMessageMappingBean("testToXml"));

    return suite;
  }

  public static void main(String[] args) {
    DataBridgeLoger.initConfigFromFile();
    junit.textui.TestRunner.run(suite());
  }
}