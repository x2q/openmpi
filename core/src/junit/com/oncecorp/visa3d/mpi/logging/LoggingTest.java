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

package com.oncecorp.visa3d.mpi.logging;

import com.oncecorp.visa3d.mpi.configuration.ConfigAccessor;
import com.oncecorp.visa3d.mpi.configuration.ConfigAccessorGenerator;
import com.oncecorp.visa3d.mpi.configuration.ConfigAccessorType;
import com.oncecorp.visa3d.mpi.utility.JUnitHelper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import java.util.Properties;

/**
 * Description: JUnit test case for Visa 3-D Secure(TM)
 * Merchant Plug-In Logging Component
 *
 * @version 0.1 July 02, 2002
 * @author	Alan Zhang
 */
public class LoggingTest extends TestCase {
	public LoggingTest(String name) {
		super(name);
	}

	public void testLogger() {
		//Check isConfigured flag before getting logger
		Assert.assertEquals(false, MPILogger.isConfigured());
		Logger logger1 = MPILogger.getLogger("com.oncecorp.visa3d.mpi.junit.Logger1");
		logger1.info("I've got logger successfully.");

		//Check isConfigured flag again before getting second logger
		Assert.assertEquals(true, MPILogger.isConfigured());
		Logger logger2 = MPILogger.getLogger("com.oncecorp.visa3d.mpi.junit.Logger2");
		logger2.info("I've got logger successfully too.");

	}

	public void testLoggingConfigAccessor() {
		//Get accessor
		ConfigAccessor accessor =
			ConfigAccessorGenerator.getAccessor(ConfigAccessorType.ACCESSOR_TYPE_LOGGING);

		//Load properties
		Properties ps = (Properties) accessor.getConfigData();

		/**
		//Make changes
		String original = ps.getProperty("log4j.appender.R.MaxFileSize");
		ps.setProperty("log4j.appender.R.MaxFileSize", "999KB");

		//Store properties
		accessor.setConfigData(ps);

		//Reload properties
		ps = (Properties) accessor.getConfigData();
		*/

		//Verifying
		//This value is changed everytime so use exist verify
		//Assert.assertEquals("500KB", ps.getProperty("log4j.appender.R.MaxFileSize"));
		Assert.assertNotNull( ps.getProperty("log4j.appender.R.MaxFileSize"));

		//Undo changes
		//ps.setProperty("log4j.appender.R.MaxFileSize", original);
		//accessor.setConfigData(ps);
	}

	public static Test suite() {

		JUnitHelper.initFromFile();

		TestSuite suite = new TestSuite("Logging suite");
		suite.addTest(new LoggingTest("testLogger"));
		suite.addTest(new LoggingTest("testLoggingConfigAccessor"));
		return suite;
	}

}