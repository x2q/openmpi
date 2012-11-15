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

* (C) Copyright Once Corp. 2002 All rights reserved.
*
* The program is provided "as is" without any warranty express or
* implied, including the warranty of non-infringement and the implied
* warranties of merchantability and fitness for a particular purpose.
* Once Corporation will not be liable for any damages suffered by 
* you as a result of using the Program. 
* In no event will Once Corporation be liable for any special, indirect
* or consequential damages or lost profits even if Once Corporation 
* has been advised of the possibility of their occurrence. Once 
* Corporation will not be liable for any third party claims against you.
* 
* ---------------------------------------------
* $Header: /dev/oncempi/core-v11/src/junit/com/oncecorp/visa3d/mpi/intf/payment/PaymentIntfTest.java 5     7/02/03 4:52p Azhang $
* 
*/

package com.oncecorp.visa3d.mpi.intf.payment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.oncecorp.visa3d.mpi.CoreInitializer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
* Description: JUnit test case for ONCEmpi Payment interface component
* 
* @version 0.1 Oct 15, 2002
* @author Alan Zhang
*/
public class PaymentIntfTest extends TestCase {

	public PaymentIntfTest(String name) {
		super(name);
	}

	/**
	* This test creates 15 threads in three loops to simulate concurrent access 
	* to CacheManager. The access type (read or write) is decided randomly. Either 
	* the ID and Item. 
	* 
	* Manually result checking required. No assertion methods.
	*/

	public void testCacheManager() {
				
		Random random = new Random();

		try {
			for (int i=0; i<15; ) {
				SingleCacheThread thread1 = new SingleCacheThread("ID-" + random.nextInt(), "Item-" + random.nextInt(), random.nextBoolean());
				SingleCacheThread thread2 = new SingleCacheThread("ID-" + random.nextInt(), "Item-" + random.nextInt(), random.nextBoolean());
				SingleCacheThread thread3 = new SingleCacheThread("ID-" + random.nextInt(), "Item-" + random.nextInt(), random.nextBoolean());
				SingleCacheThread thread4 = new SingleCacheThread("ID-" + random.nextInt(), "Item-" + random.nextInt(), random.nextBoolean());
				SingleCacheThread thread5 = new SingleCacheThread("ID-" + random.nextInt(), "Item-" + random.nextInt(), random.nextBoolean());
				
				i+=5;
				
				thread1.start();
				thread2.start();
				thread3.start();
				thread4.start();
				thread5.start();
				
				System.out.println("Concurrent read all CACHE content: ");
				displayMapContent(PaymentMessageCacheManager.getMessageCache());
				
				System.out.println("Sleep for 30 seconds (roughly)...\n");
				Thread.sleep(30000);
			}
			
			HashMap cache = (PaymentMessageCacheManager.getMessageCache());
			System.out.println("--- Final Content of CACHE: ---");
			displayMapContent(cache);
			
			HashMap trace = (PaymentMessageCacheManager.getTrace());
			System.out.println("--- Final Content of TRACE: ---");
			displayMapContent(trace);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public static Test suite() {
		boolean initOK = CoreInitializer.init();
		if (!initOK) {
			System.out.println("Init Core Server failed.");
			fail();
		}
		
		System.out.println("This test suit takes about 2 mins to finish.");

		TestSuite suite = new TestSuite("Intf.Payment suite");
		suite.addTest(new PaymentIntfTest("testCacheManager"));
		return suite;
	}
	
	/**
	 * Convenient method to display HashMap content
	 * 
	 * @param map The HashMap
	 */
	private void displayMapContent(HashMap map) {
		Iterator keys = map.keySet().iterator();
		while (keys.hasNext()) {
			Object key = keys.next();
			System.out.println("Key: " + key + ", Value: " + map.get(key));
		}
		System.out.println("--- Content display completed. ---");
	}
}
