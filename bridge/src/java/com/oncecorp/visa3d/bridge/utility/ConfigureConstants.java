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
 * <p>Title: ConfigureConstants</p>
 * <p>Description: Define configure related constants</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public interface ConfigureConstants
{
    public final static String DATA_BRIDGE_TAG =    "mpidb:databridge";
    public final static String MERCHANT_TAG    =     "mpidb:merchantid";
    public final static String MSG_TAG    =     "mpidb:msg";
    public final static String PLUGIN_FIELD_TAG    =     "mpidb:field";
    public final static String PLUGIN_CHANNEL_TAG    =     "mpidb:listener";

    public final static String PLUGIN_SERVICE_TAG    =     "mpidb:PlugIns";
    public final static String PLUGIN_TAG    =     "mpidb:plugin";
    public final static String CUSTOM_DATA_TAG    =     "mpidb:customData";

    public final static String LISTENING_SERVICE_TAG    =     "mpidb:ListeningService";
    public final static String AUDITING_SERVICE_TAG    =     "mpidb:AuditingService";
    public final static String MONITORING_SERVICE_TAG    =     "mpidb:MonitoringService";
    public final static String LOG_HANDLER_TAG    =     "mpidb:logHandler";

    public final static String JMX_CONFIG_TAG    =     "mpidb:jmxconfig";

    public final static String LISTENING_ATTRIBUTE_TAG    =     "mpidb:listeningAttribute";
    public final static String JMS_INITIAL_CONTEXT_FACTORY = "jms.InitialContextFactory";
    public final static String JMS_PROVIDER_URL = "jms.ProviderURL";
    public final static String JMS_SECURITY_PRINCIPAL = "jms.SecurityPrincipal";
    public final static String JMS_SECURITY_CREDENTIALS = "jms.SecurityCredentials";
    public final static String JMS_CONNECTION_FACTORY_JNDI = "jms.ConnectionFactoryJNDI";
    public final static String JMS_TOPIC_JNDI = "jms.TopicJNDI";
    public final static String TRIPLEDES_PREFIX_TAG    =     "tripleDES.";
    public final static String JMS_PREFIX_TAG    =     "jms.";
    public final static String MESSAGE_KEY_DELIMITER   =     "___";
}