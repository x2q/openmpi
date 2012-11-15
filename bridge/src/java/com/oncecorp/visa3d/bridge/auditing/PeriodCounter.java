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

import java.util.Timer;
import java.util.TimerTask;


/**
 * Title:        ONCE MPI Data Bridge
 * Description:  This class is the timer task class that will call
 * <tt>PeriodCountable</tt> periodcally.  It used for counting getting peak
 * number.
 * Copyright:    Copyright (c) 2002
 * Company:      Once Corporation
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class PeriodCounter extends TimerTask {

  private Timer                  timer = null;
  private PeriodCountable        counter;

  /**
   * The constructor of the class.
   * @param counter the counting object whose <tt>pickUpPeriodData</tt> will
   * be called periodcally.
   */
  public PeriodCounter(PeriodCountable counter) {
    this(300000L, counter);
  }

  /**
   * The constructor of the class.
   * @param delay The delay and period time.
   * @param counter the counting object whose <tt>pickUpPeriodData</tt> will
   * be called periodcally.
   */
  public PeriodCounter(long delay, PeriodCountable counter) {
    super();
    this.counter = counter;
    timer = new Timer(true);
    timer.schedule(this, delay, delay);
  }

  /**
   * This method will run when the timer fires.
   */
  public void run() {
    counter.pickUpPeriodData();
  }

  /**
   * This metod cancel the timer and this object.
   */
  public boolean cancel() {
    if ( timer != null ) {
      timer.cancel();
    }
    return super.cancel();
  }
}