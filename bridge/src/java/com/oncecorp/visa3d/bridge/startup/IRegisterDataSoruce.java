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

package com.oncecorp.visa3d.bridge.startup;

/**
 * <p>Title: IRegisterDataSoruce </p>
 * <p>Description: A common interface for registering DataSoruce related functions</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com)
 * @version 1.0
 */

public interface IRegisterDataSoruce
{

    /**
     * Create and register a new data source
     * @param cpJNDIName - data source JNDI name
     * @param driverName - JDBC driver name
     * @param url - JDBC url
     * @param user - database user
     * @param passwd - database user's password
     * @throws Exception
     */
    public void createDataSource( String cpJNDIName, String driverName,
                                  String url, String user,
                                  String passwd) throws Exception;


    /**
     * Remove the registered data source
     * @param name - data source JNDI name
     */
    public void removeDataSource( String name );

}