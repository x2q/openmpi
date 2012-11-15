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

package com.oncecorp.visa3d.bridge.configure;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.io.File;

import com.oncecorp.visa3d.bridge.configure.FileHandler;
import com.oncecorp.visa3d.bridge.beans.DataBridgeBean;
import com.oncecorp.visa3d.bridge.beans.BeansHelper;

/**
 * <p>Title: ConfigurationUnitTest</p>
 * <p>Description: Setup all unit case needed by configuration service. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */


public class ConfigurationUnitTest  extends TestCase
{

    public ConfigurationUnitTest( String name )
    {
        super(name);
    }

    /**
     * Test load configuration data from file
     */
    public void testFileHandler()
    {
        FileHandler fh = new FileHandler();

        System.out.println("If you got exception, Please put mpi-databridge.xml file under your current working directory");
        System.out.println("Your current working directory is: " + System.getProperty("user.dir") );

        File file = new File( fh.getFileParentPath( "mpi-databridge.xml",
                              this.getClass())
                              + File.separator + "mpi-databridge.xml" );
        System.out.println("file path is:" + file.getAbsolutePath() );
        assertTrue( file.exists() );
        Document doc = fh.load( file.getAbsolutePath() );
        DataBridgeBean dbb = BeansHelper.databridgeFromXml( null, doc );
        assertNotNull( dbb );
        assertNotNull( dbb.getAuditingService() );
        assertNotNull( dbb.getListeningService() );
        assertNotNull( dbb.getListeningService().getProperties() );

        file = new File( fh.getFileParentPath( "mpi-databridge.xml")
                              + File.separator + "mpi-databridge.xml" );
        assertTrue( file.exists() );

        dbb.getAuditingService().setMailPasswd("TestPassword");
        dbb.getListeningService().setTimeServiceInterval(8888);

        fh.save( "testmpi-databridge.xml", dbb.toXml() );
        doc = fh.load( "testmpi-databridge.xml" );
        dbb = BeansHelper.databridgeFromXml( null, doc );

        assertNotNull( dbb );
        assertNotNull( dbb.getAuditingService() );
        assertNotNull( dbb.getListeningService() );
        assertEquals( dbb.getAuditingService().getMailPasswd(), "TestPassword" );
        assertEquals( dbb.getListeningService().getTimeServiceInterval(), 8888 );

    }

    /**
     *
     * @return -  Test suit for listening service.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Configuration Service Unit Test Suite");
        suite.addTest(new ConfigurationUnitTest("testFileHandler"));

        return suite;
    }


}