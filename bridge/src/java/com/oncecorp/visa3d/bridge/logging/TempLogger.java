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

package com.oncecorp.visa3d.bridge.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This class is used for test logging classes.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class TempLogger {
  private static String   FILE_NAME = "/bea/TempLogger.log";
  private static boolean status = true;

  public static void log(String msg) {
    //File file = new File(FILE_NAME);
    if ( status ) {
      System.out.println(msg);
      try {
        FileWriter fw = new FileWriter(FILE_NAME, true);
        PrintWriter out = new PrintWriter(fw);
        out.println(msg);
        out.close();
        fw.close();
      }
      catch (Exception e) {
        System.out.println("Exception in TempLogger.log() -- " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  public static void setStatus(boolean flag) {
    status = flag;
  }

  public static void main(String[] args) {
    //log("Test message");
    File flie = new File("TempLogger.log");
    System.out.println("flie.isAbsolute() = " + flie.isAbsolute());
  }
}