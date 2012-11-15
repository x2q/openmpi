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

import com.oncecorp.visa3d.bridge.utility.DataBridgeRuntimeException;
/**
 * Title:        ONCE MPI Data Bridge
 * Description:  This exception class handle all exception occurs in the
 * auditing service.  It can ecapsulate the other exception which originally
 * happen.
 * Copyright:    Copyright (c) 2002
 * Company:      Once Corporation
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class AuditingException extends DataBridgeRuntimeException {

  /**
   * The default constructor.
   */
  public AuditingException() {
  }

  /**
   * The constructor of the class.
   * @param s The detail message.
   */
  public AuditingException(String s) {
    super(s);
  }

  /**
   * The constructor of the class.
   * @param throwable The encapsulated throwable.
   */
  public AuditingException(Throwable throwable) {
    super(throwable);
  }

  /**
   * The constructor of the class.
   * @param s The detail message.
   * @param throwable The encapsulated throwable.
   */
  public AuditingException(String s, Throwable throwable) {
    super(s, throwable);
  }
}