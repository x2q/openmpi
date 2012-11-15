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

package com.oncecorp.visa3d.bridge.utility;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;


/**
 * <p>Title: UtilityUnitTest</p>
 * <p>Description: Setup all unit case needed by utility service. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */


public class UtilityUnitTest  extends TestCase
{

    public UtilityUnitTest( String name )
    {
        super(name);
    }

    /**
     * Test main XMLUtils's methods
     */
    public void testXMLUtils()
    {
        //Test validation with wrong xml
        String xmlStr =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE MPI_Interface SYSTEM \"http://demo.oncecorp.com/mpi/MPIInterfaceWithProfile.dtd\">"
                + "<MPI_Interface>"
                + "<MPIError>" //without id attribute
                +"<version>1.0</version>"
                + "<errorCode>"
                + "500"
                + "</errorCode>"
                + "<errorMessage>"
                + "Error message goes here."
                + "</errorMessage>"
                + "</MPIError>"
                + "</MPI_Interface>";
        Document doc = XMLUtils.getDocumentFromString( xmlStr, true );
        assertNull( doc );

        //Test validation with right xml
        xmlStr =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE MPI_Interface SYSTEM \"http://demo.oncecorp.com/mpi/MPIInterfaceWithProfile.dtd\">"
                + "<MPI_Interface>"
                + "<MPIError id=\"001\">"
                +"<version>1.0</version>"
                + "<errorCode>"
                + "500"
                + "</errorCode>"
                + "<errorMessage>"
                + "Error message goes here."
                + "</errorMessage>"
                + "</MPIError>"
                + "</MPI_Interface>";
        doc = XMLUtils.getDocumentFromString( xmlStr, true );
        assertNotNull( doc );

        // Test XPath
        Element item = XMLUtils.findXPathElement( doc.getDocumentElement(),
                "/MPI_Interface/MPIError/errorCode" );
        assertNotNull( item );
        assertEquals( "500", XMLUtils.getText(item) );

        // Test set node text, toString, etc
        XMLUtils.setNodeText( item, "600" );
        xmlStr = XMLUtils.toXmlString( doc );
        assertNotNull( xmlStr );
        doc = XMLUtils.getDocumentFromString( xmlStr );

        item = XMLUtils.findXPathElement( doc.getDocumentElement(),
                "/MPI_Interface/MPIError" );

        // Test relative xpath
        Element sitem = XMLUtils.findXPathElement( item, "errorCode" );
        assertNotNull( sitem );
        assertEquals( "600", XMLUtils.getText(sitem) );

        assertNotNull( sitem );

        // Test toRelativePath
        sitem = XMLUtils.findXPathElement( item,
                "MPI_Interface/MPIError/errorCode" );
        assertNotNull( sitem );
        assertEquals( "600", XMLUtils.getText(sitem) );

        sitem = XMLUtils.findXPathElement( item,
                "MPIError/errorCode" );
        assertNotNull( sitem );
        assertEquals( "600", XMLUtils.getText(sitem) );

        sitem = XMLUtils.findXPathElement( item,
                "/MPI_Interface/MPIError/errorCode" );
        assertNotNull( sitem );
        assertEquals( "600", XMLUtils.getText(sitem) );

    }

    /**
     *
     * @return -  Test suit for listening service.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Utility Service Unit Test Suite");
        suite.addTest(new UtilityUnitTest("testXMLUtils"));

        return suite;
    }


}