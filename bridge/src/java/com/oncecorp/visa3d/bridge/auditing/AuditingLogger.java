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


package  com.oncecorp.visa3d.bridge.auditing;

import  com.oncecorp.visa3d.bridge.beans.AuditingListenerBean;
import  com.oncecorp.visa3d.bridge.beans.MerchantInfoBean;
import  com.oncecorp.visa3d.bridge.configure.FileHandler;
import  com.oncecorp.visa3d.bridge.listening.ListeningUtils;
import  com.oncecorp.visa3d.bridge.listening.MPIMessageListener;
import  com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import  com.oncecorp.visa3d.bridge.utility.XMLUtils;
import  com.oncecorp.visa3d.bridge.utility.JdbcUtils;
import  com.oncecorp.visa3d.bridge.utility.Utils;
import  com.oncecorp.visa3d.bridge.listening.MessageColumnDataExtract;
import  java.io.ByteArrayInputStream;
import  java.sql.Connection;
import  java.sql.DatabaseMetaData;
import  java.sql.PreparedStatement;
import  java.sql.ResultSet;
import  java.sql.Statement;
import  java.util.Collections;
import  java.util.Iterator;
import  java.util.List;
import  java.util.Properties;
import  java.util.Set;
import  java.util.TreeSet;
import  javax.sql.DataSource;
import  org.w3c.dom.Document;
import  org.apache.log4j.Logger;


/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This class write the message into the database.  It also
 * provides the methods to set the configuration.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
public class AuditingLogger
        implements MPIMessageListener {
    private AuditingListenerBean bean = new AuditingListenerBean();
    //  private int[]                   currentNumber;
    //  private String                  jndi;
    //  private DataSource              datasource;
    private String templateName;
    private String subject;
    private String body;
    //  private String                  schemaName;
    private boolean initializedFlag = true;
    private static Logger log4j = DataBridgeLoger.getLogger(AuditingLogger.class);

    /**
     * The constructor of the class.
     * @param bean The bean that hold the data.
     */
    public AuditingLogger (AuditingListenerBean bean) {
        //currentNumber = AuditingUtils.createIntArray();
        this.bean.setId(bean.getId());
        setBean(bean);
    }

    /**
     * This method returns the logger id;
     * @return The logger id.
     */
    public String getId () {
        return  bean.getId();
    }

    /**
     * This method updates the current auditing logger.
     * @param updateBean The bean object that contains the updating data.
     * @return <tt>true</tt> if any data is changed, <tt>false</tt> otherwise.
     */
    public synchronized boolean update (AuditingListenerBean updateBean) {
        if (updateBean == null || !getId().equals(updateBean.getId())) {
            initializedFlag = false;
            log4j.warn("Bean is not valid.");
            return  false;
        }
        log4j.debug("start doing update logger.");
        boolean result = false;
        if (!ListeningUtils.merchantListEqual(bean.getMerchantIds(), updateBean.getMerchantIds())) {
            List oldMerchants = bean.getMerchantIds();
            List newMerchants = updateBean.getMerchantIds();
            for (Iterator i = newMerchants.iterator(); i.hasNext();) {
                String merchantId = (String)i.next();
                if (!AuditingManager.getInstance().containsMerchantInLoggers(merchantId)) {
                    // this merchant is the new one.
                    continue;
                }
                AuditingLogger thatLogger = AuditingManager.getInstance().getLoggerFromMerchant(merchantId);
                if (thatLogger == null) {
                    continue;
                }
                if (thatLogger.getId().equals(getId())) {
                    // this merchant exists in the same logger.
                    continue;
                }
                log4j.warn("duplicate merchant id -- \"" + merchantId + "\".");
                i.remove();
            }
            bean.setMerchantIds(newMerchants);
            //bean.setMerchantIds(updateBean.getMerchantIds());
            log4j.debug(updateBean.getId() + " merchant updated.");
            result = true;
        }
        if (!ListeningUtils.messageMapEqual(bean.getMessages(), updateBean.getMessages(),
                true)) {
            bean.setMessages(updateBean.getMessages());
            log4j.debug(updateBean.getId() + " message updated.");
            result = true;
        }
        if (!bean.getStatus().equals(updateBean.getStatus())) {
            bean.setStatus(updateBean.getStatus());
            log4j.debug(updateBean.getId() + " status updated.");
            result = true;
        }
        if (bean.getMaxRows() != updateBean.getMaxRows()) {
            bean.setMaxRows(updateBean.getMaxRows());
            log4j.debug(updateBean.getId() + " maxRows updated.");
            result = true;
        }
        if (bean.getThreshold() != updateBean.getThreshold()) {
            bean.setThreshold(updateBean.getThreshold());
            log4j.debug(updateBean.getId() + " threshold updated.");
            result = true;
        }
        if (bean.isMailNotify() != updateBean.isMailNotify()) {
            bean.setMailNotify(updateBean.isMailNotify());
            log4j.debug(updateBean.getId() + " mailNotify updated.");
            result = true;
        }
        if (!bean.getPeriod().equals(updateBean.getPeriod())) {
            bean.setPeriod(updateBean.getPeriod());
            log4j.debug(updateBean.getId() + " period updated.");
            result = true;
        }
        if (!bean.getEmailTemplate().equals(updateBean.getEmailTemplate())) {
            bean.setEmailTemplate(updateBean.getEmailTemplate());
            log4j.debug(updateBean.getId() + " mailTemplate updated.");
            setMailTemplate();
            result = true;
        }
        if (!bean.getSender().equals(updateBean.getSender())) {
            bean.setSender(updateBean.getSender());
            log4j.debug(updateBean.getId() + " sender updated.");
            result = true;
        }
        if (!bean.getTo().equals(updateBean.getTo())) {
            bean.setTo(updateBean.getTo());
            log4j.debug(updateBean.getId() + " to updated.");
            result = true;
        }
        if (!bean.getCc().equals(updateBean.getCc())) {
            bean.setCc(updateBean.getCc());
            log4j.debug(updateBean.getId() + " cc updated.");
            result = true;
        }
        bean = updateBean;
        AuditingManager.getInstance().getBean().addListener(updateBean);
        return  result;
    }

    /**
     * This method returns the data bean.
     * @return The bean.
     */
    public AuditingListenerBean getBean () {
        return  bean;
    }

    /**
     * This method sets the bean to the logger.
     * @param bean The data bean.
     */
    public synchronized void setBean (AuditingListenerBean bean) {
        update(bean);
    }

    /**
     * This method writes the message into the log database.
     * @param prope The peoperties object that contains the message's additional
     * data.
     * @param msg  The XML Document object that contains the message's content.
     * @return <tt>true</tt> if message handle success or ignore, <tt>false</tt>
     * otherwise.
     */
    public boolean handleMsg (Properties props, Document msg)
    {
        String merchantId = props.getProperty(ListeningUtils.JMS_MERCHANT_PROPERTY);
        MerchantInfoBean merchantInfoBean = AuditingManager.getInstance().getMerchantInfoBean(merchantId);
        if (merchantInfoBean == null) {
            log4j.error("merchant [" + merchantId + "] not support.  Message is rollbacked.");
            return  false;
        }
        DataSource datasource = AuditingUtils.checkAndGetDataSource(merchantInfoBean);
        if (datasource == null) {
            log4j.error("Data Source for [" + merchantId + "] does not exist.  Message is rollbacked.");
            return  false;
        }
        String schemaName = merchantInfoBean.getSchemaName();
        String messageType = props.getProperty(ListeningUtils.JMS_MESSAGE_TYPE);
        String msgVersion = props.getProperty(ListeningUtils.JMS_MESSAGE_VERSION);
        if ( ! AuditingUtils.isDefined( messageType, msgVersion ) )
        {
            log4j.error(" Message [" + messageType + "] [" + msgVersion
                       + "] is ignored.");
            return true;
        }
        String messageId = props.getProperty(MessageColumnDataExtract.MESSAGE_ID);
		if ( messageId == null || messageId.trim().equals("") )
		{
			log4j.error(" Message [" + messageType + "] [" + msgVersion
					   + "] is not correctly defined, ignore it.");
			return true;
		}

        StringBuffer sql = new StringBuffer("INSERT INTO ");
        if (schemaName != null && schemaName.trim().length() != 0) {
            sql.append(schemaName);
            sql.append(".");
        }
        sql.append(AuditingUtils.MESSAGE_TABLE_NAME);
        sql.append(AuditingUtils.SAVE_MESSAGES_SQL);
        log4j.debug("SQL = " + sql);
        synchronized (this) {
            Connection con = null;
            PreparedStatement pstmt = null;
            try {
                con = datasource.getConnection();
                pstmt = con.prepareStatement(sql.toString());
                pstmt.setString(1, messageId);
                pstmt.setString(2, merchantId);
                pstmt.setString(3, Utils.convertNullString2Empty(props.get(ListeningUtils.JMS_MESSAGE_TIMESTAMP)));
                pstmt.setString(4, XMLUtils.toXmlString(msg));
                pstmt.setString(5, messageType);
                pstmt.setString(6, Utils.convertNullString2Empty( msgVersion ) );
                pstmt.setString(7, Utils.convertNullString2Empty(props.get(ListeningUtils.JMS_MESSAGE_PROTOCOL)));
                pstmt.setString(8, Utils.convertNullString2Empty(props.get(MessageColumnDataExtract.MESSAGE_STATUS)));
                pstmt.setString(9, Utils.convertNullString2Empty(props.get(MessageColumnDataExtract.CARD_NUMBER)));
                pstmt.setString(10, Utils.convertNullString2Empty(props.get(MessageColumnDataExtract.CARD_NUMBER_FLAG)));
                pstmt.setString(11, Utils.convertNullString2Empty(props.get(MessageColumnDataExtract.TRANSACTION_ID)));
                //        pstmt.setBinaryStream(4, new ByteArrayInputStream(bytes), bytes.length);
                int result = pstmt.executeUpdate();
                if (result == 1) {
                    DataBridgeLoger.getLogger(this).debug(merchantId + ", "
                            + messageType + ", " + messageId + " -- message insert success.");
                    // increase the current number.
                    int num = AuditingManager.getInstance().increaseRecordNumber(merchantInfoBean);
                    log4j.debug(merchantId + ", " + messageType + " current number is "
                            + num);
                    // check size and send mail.
                    if (num == bean.getThreshold() && bean.isMailNotify()) {
                        // send notification mail.
                        AuditingUtils.sendMail(this, merchantInfoBean.getDataSourceJndi(),
                                merchantInfoBean.getSchemaName(), messageType,
                                num);
                        log4j.debug("sending notification mail.");
                    }
                    else if (num == bean.getMaxRows() && bean.isMailNotify()) {
                        // send wraning mail.
                        AuditingUtils.sendMail(this, merchantInfoBean.getDataSourceJndi(),
                                merchantInfoBean.getSchemaName(), messageType,
                                num);
                        log4j.debug("sending wraning mail.");
                    }
                    // statistics the message.
                    log4j.debug("statistics the message.");
                    AuditingManager.getInstance().count( props );
                }
                else {
                    log4j.warn(merchantId + ", " + messageType + ", " + messageId
                            + " -- message can't insert.");
                    return  false;
                }
                return  true;
            } catch (Exception e) {
                log4j.error(bean.getId() + ", " + messageType + ", " + messageId
                        + " -- message insert error. This message is ignored.",
                        e);
                return  true;
            } finally {
                try {
                    if (pstmt != null) {
                        pstmt.close();
                    }
                } catch (Exception e) {
                    log4j.error("statement closing failed.", e);
                }
                try {
                    if (con != null) {
                        con.close();
                    }
                } catch (Exception e) {
                    log4j.error("connection closing failed.", e);
                }

				JdbcUtils.closeDataSource( datasource );

            }
        }       // end of synchronized.
    }

    /**
     * This method sets the mail template text.
     */
    private void setMailTemplate () {
        if (templateName == null || !templateName.equals(bean.getEmailTemplate())) {
            templateName = bean.getEmailTemplate();
            Document doc = new FileHandler().load(templateName);
            if (doc != null) {
                subject = XMLUtils.getFirstElementText(doc, "subject");
                body = XMLUtils.getFirstElementText(doc, "body");
            }
            else {
                subject = null;
                body = null;
            }
        }
    }

    /**
     * This method returns the mail subject text.
     * @return The mail subject text.
     */
    public String getSubject () {
        return  subject == null ? "" : subject;
    }

    /**
     * This method sets the mail subject text.
     * @param subject The mail subject text.
     */
    public void setSubject (String subject) {
        this.subject = subject;
    }

    /**
     * This method returns the mail body template text.
     * @return The mail body template text.
     */
    public String getBody () {
        return  body == null ? "" : body;
    }

    /**
     * This method sets the mail body template text.
     * @param body The mail body template text.
     */
    public void setBody (String body) {
        this.body = body;
    }

    /**
     * This method returns the data source's jndi name.
     * @return The jndi name.
     */
    //  public String getJndi() {
    //    return this.jndi;
    //  }
    /**
     * This method save the mail template file.
     */
    public void saveMailTemplate () {
        FileHandler fileHandler = new FileHandler();
        String xml = "<emailTemplate>\r\n" + "<subject>" + getSubject() + "</subject>\r\n"
                + "<body>" + getBody() + "</body>\r\n" + "</emailTemplate>\r\n";
        fileHandler.save(bean.getEmailTemplate(), xml);
    }

    /**
     * This method does configuration using xml string.
     * @param xml The xml string.
     */
    public void config (String xml) {
        AuditingListenerBean bean = new AuditingListenerBean();
        bean.fromXml(xml);
        setBean(bean);
    }

    /**
     * put your documentation comment here
     * @param initializedFlag
     */
    void setInitialized (boolean initializedFlag) {
        this.initializedFlag = initializedFlag;
    }

    /**
     * @return whether the plug-in has been intialized
     */
    public boolean isInitialized () {
        return  initializedFlag;
    }

    /**
     * This method does nothing in this class.
     */
    public void start () {
        bean.setStatus(Utils.LISTENTING_START);
    }

    /**
     * This method does nothing in this class.
     */
    public void stop () {
        bean.setStatus( Utils.LISTENING_STOP );
    }

    /**
     * This method returns the merchant id.
     * @return
     */
    public String getDescription () {
        return  "The id of the handler is " + bean.getId();
    }

    /**
     * Register plugin
     */
    public void register () {
    //bean.setStatus(ListeningUtils.REGISTER);
    }

    /**
     * Unregister plugin
     */
    public void unregister () {
    //bean.setStatus(ListeningUtils.UNREGISTER);
    }

    /**
     * This method returns the logger id.
     * @return The logger id.
     */
    public String getName () {
        return  bean.getId();
    }

    /**
     * This method is not used in this class.
     * @return - whether the plugin will accept filter change
     */
    public boolean acceptFilterChange () {
        return  true;
    }

    /**
     * This method is not used in this class.
     * @return - whether the plugin will accept dynamic filter change
     */
    public boolean acceptDynamicFilterChange () {
        return  true;
    }

    /**
     * This method is not used in this class.
     * @param props - contains merchant ids and message type-version list
     */
    public void setFilter (Properties props) {}

    /**
     * This method is not used in this class.
     * @return - whether the plugin will accept any filter
     */
    public boolean acceptFilter () {
        return  true;
    }
}



