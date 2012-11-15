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

package com.oncecorp.visa3d.mpi.intf.payment;

/**
* Support class of Single Thread to access PaymentMessageCacheManager
* 
* @version 0.1 Oct 15, 2002
* @author Alan Zhang
*/
public class SingleCacheThread extends Thread {
	private String id;
	private Object item;
	private boolean addAction;

	public SingleCacheThread() {
		super();
	}

	public SingleCacheThread(String id, Object item, boolean action) {
		super();
		this.id = id;
		this.item = item;
		this.addAction = action;
	}

	public void run() {
		System.out.println(
			"Thread: " + id + ", Item: " + item + ", Add action: " + addAction);
		try {
			if (addAction) {
				(new PaymentMessageCacheManager()).addItem(id, item);
			} else {
				System.out.println(
					"Item "
						+ id
						+ ": "
						+ (new PaymentMessageCacheManager()).getCachedItem(id));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
