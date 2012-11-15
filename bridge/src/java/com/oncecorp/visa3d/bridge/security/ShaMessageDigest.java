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

package com.oncecorp.visa3d.bridge.security;

import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.security.MessageDigest;
import sun.misc.BASE64Encoder;

/**
 * <p>Title: ShaMessageDigest</p>
 * <p>Description: Use Sun JCE Sha cryptographically secure message digests. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
public class ShaMessageDigest
{
    private static MessageDigest m_sha = null;
    private static BASE64Encoder m_base64 = null;

    public ShaMessageDigest()
    {
    }

    /**
     * Generate the message digest of the given string
     * @param text - input text
     * @return - message digest
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String digestString(String text) throws
           IOException, NoSuchAlgorithmException {

        if ( m_sha == null )
        {
            m_sha = MessageDigest.getInstance ("SHA");
            m_base64 = new BASE64Encoder();
        }
        m_sha.reset();
        m_sha.update ( text.getBytes() );

        return new String( m_base64.encode ( m_sha.digest() ) );

    }
}