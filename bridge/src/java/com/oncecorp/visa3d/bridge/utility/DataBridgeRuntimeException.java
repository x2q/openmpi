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

/**
 * Title:        ONCE MPI Data Bridge
 * Description:  This exception class is the base runtime exception that
 * can be as the super class of the other runtime exception class used in
 * different service.
 * Copyright:    Copyright (c) 2002
 * Company:      Once Corporation
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class DataBridgeRuntimeException extends RuntimeException {

  private Throwable             throwable;

  /**
   * The default constructor.
   */
  public DataBridgeRuntimeException() {
  }

  /**
   * The constructor of the class.
   * @param s The detail message.
   */
  public DataBridgeRuntimeException(String s) {
    super(s);
  }

  /**
   * The constructor of the class.
   * @param throwable The encapsulated throwable.
   */
  public DataBridgeRuntimeException(Throwable throwable) {
    this.throwable = throwable;
  }

  /**
   * The constructor of the class.
   * @param s The detail message.
   * @param throwable The encapsulated throwable.
   */
  public DataBridgeRuntimeException(String s, Throwable throwable) {
    super(s);
    this.throwable = throwable;
  }

  /**
   * This method return the error string of the exception.
   * @return The error message.
   */
  public String getMessage() {
    if ( throwable != null ) {
      return super.getMessage() + '\n' + throwable.getMessage();
    }
    else {
      return super.getMessage();
    }
  }

  /**
   * Creates a localized description of this Throwable.
   * If containes encapsulated throwable object, it returns the encapsulated
   * throwable object's localized description. Otherwise,
   * returns the same result as getMessage().
   * @return The localized description of this exception.
   */
  public String getLocalizedMessage() {
    if ( throwable != null ) {
      return throwable.getLocalizedMessage();
    }
    else {
      return super.getLocalizedMessage();
    }
  }

  /**
   * This method prints this exception and its backtrace to the standard error
   * stream.
   */
  public void printStackTrace() {
    super.printStackTrace();
    if ( throwable != null ) {
      throwable.printStackTrace();
    }
  }

  /**
   * This method returns a short description of this exception object.
   * @return A string representation of this Exception.
   */
  public String toString() {
    if ( throwable != null ) {
      return super.toString() + '\n' + throwable.toString();
    }
    else {
      return super.toString();
    }
  }
}