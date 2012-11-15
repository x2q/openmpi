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

package com.oncecorp.visa3d.mpi.security;

import com.sun.crypto.provider.SunJCE; 
import javax.crypto.KeyGenerator; 
import javax.crypto.SecretKey;
import java.security.Security;
import java.io.*;

/**
 * Description: 
 * 
 * @version 0.1 Aug 19, 2002
 * @author	Jun Shi
 */

public class MPIKeyGenerator { 

    // Constructor
    private MPIKeyGenerator()
    {
       try 
       { 
          //Explicitly add SunJCE provider 
          Security.addProvider(new SunJCE());
          
          // Generate a 3DES key - put Algorithm named [DESede] or [TripleDES]
          KeyGenerator keyGen = KeyGenerator.getInstance("DESede");
          SecretKey key = keyGen.generateKey();
          
          // location of the generated key
          File f1 = new File ("C:\\keystore");
          File f2 = new File (f1, "Key.store");
          
          OutputStream outputFile = new FileOutputStream(f2);
          ObjectOutputStream cout = new ObjectOutputStream( outputFile ); 
          cout.writeObject(key); 
          cout.close(); 
       }
       catch (Exception e) 
       { 
          e.printStackTrace();
       }
    } 

//////////////////// Testing ///////////////
    public static void main(String [] args)
    {
       new MPIKeyGenerator(); 
    }

} 


