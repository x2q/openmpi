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

package com.oncecorp.visa3d.mpi.utility;

import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;

/**
 * <p>Title: Utils</p>
 * <p>Description: Help functions</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
public class Utils
{
    public final static String KEY_DELIMITER   =     "___";

    /**
     * protocol support
     */
    public final static byte   NONE_SUPPORT          =    0x00;
    public final static byte   VISA_SUPPORT          =    0x01;
    public final static byte   MASTER_SUPPORT        =    0x02;
    public final static byte   ALL_SUPPORT           =    0x7f;

    public Utils()
    {
    }

    /**
     * Generate a message mapping key from type and version
     * @param type - messages type
     * @param version - message version
     * @return - combined message key
     */
    public static String getMessageMappingKey( String type, String version )
    {
        if ( type == null )
            return "";

        if ( version == null )
            version = "";

        return type.trim() + KEY_DELIMITER + version.trim();
    }

    /**
     * Extract type part from type and version binder key
     * @param key - join key
     * @return - type part of the key
     */
    public static String extractTypeFromKey( String key )
    {
        if ( key == null || key.indexOf( KEY_DELIMITER ) == -1 )
            return key;

        return key.substring(0, key.indexOf( KEY_DELIMITER ) );
    }

    /**
     * Extract version part from type and version binder key
     * @param key - join key
     * @return - version part of the key
     */
    public static String extractVersionFromKey( String key )
    {
        if ( key == null || key.indexOf( KEY_DELIMITER ) == -1 )
            return "";

        return key.substring(key.indexOf( KEY_DELIMITER )
                             + KEY_DELIMITER.length() );
    }

    /**
     * Transform message protocol string to byte
     * @param protocolString - protocol string
     * @return - protocol represent byte
     */
    public static byte toProtocolByte( String protocolString )
    {
        if ( protocolString == null || protocolString.trim().equals("") )
            return 0;
        else {
            byte value = 0x0;

            protocolString = protocolString.trim();

            if ( protocolString.equalsIgnoreCase(
                    MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE) )
                value |= VISA_SUPPORT;
            if ( protocolString.equalsIgnoreCase(
                    MPIConfigDefinition.PROTOCOL_MASTERCARD_SECURE_CODE)  )
                value |= MASTER_SUPPORT;

            return value;
        }

    }
}