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

package com.oncecorp.visa3d.bridge.monitoring;

import java.util.Properties;
/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This is the MBean interface that provides the operation
 * methods for operation the merchant info database.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public interface MerchantInfoMBean {
  /**
   * This method inserts a new merchant row into the merchant info table.
   * @param bean The bean that contains the merchant data.
   * @return <tt>true</tt> if inserting success, <tt>false</tt> otherwise.
   */
  public String create(Properties prop);
  /**
   * This method updates the nerchant info row.
   * @param bean The bean that contains the merchant data.
   * @return <tt>true</tt> if updating success, <tt>false</tt> otherwise.
   */
  public String update(Properties prop);
  /**
   * This method deletes a merchant info row from the merchant info database.
   * @param id The deleted merchant id.
   * @return <tt>true</tt> if deleting success, <tt>false</tt> otherwise.
   */
  public String delete(String id);
  /**
   * This method retrieves a merchant info data from the database.
   * @param id The retrieved merchant id.
   * @return The Properties contains the merchant info data.
   */
  public Properties retrieve(String id);
  /**
   * This method returns all merchant info data.
   * @return The Properties array.  Every element contains a merchant info data.
   */
  public Properties[] retrieveAll();
}