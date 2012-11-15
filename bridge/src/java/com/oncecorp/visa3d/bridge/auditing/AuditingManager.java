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

import  java.sql.Connection;
import  java.sql.DatabaseMetaData;
import  java.sql.Statement;
import  java.sql.ResultSet;
import  java.util.ArrayList;
import  java.util.Collections;
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.List;
import  java.util.Map;
import  java.util.Vector;
import  java.util.Properties;
import  java.util.Set;
import  java.util.TreeSet;
import  javax.sql.DataSource;

import  org.w3c.dom.Document;
import  org.apache.log4j.Logger;

import  com.oncecorp.visa3d.bridge.beans.AuditingServiceBean;
import  com.oncecorp.visa3d.bridge.beans.AuditingListenerBean;
import  com.oncecorp.visa3d.bridge.beans.MerchantInfoBean;
import  com.oncecorp.visa3d.bridge.configure.FileHandler;
import  com.oncecorp.visa3d.bridge.listening.ListeningManager;
import  com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import  com.oncecorp.visa3d.bridge.beans.BeansHelper;
import  com.oncecorp.visa3d.bridge.utility.ConfigureConstants;
import  com.oncecorp.visa3d.bridge.utility.JdbcUtils;

/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This class handles the logger instances and statistics
 * instance.  This class also provides the public methods that are calles
 * by other parts of the data bridge such as listening service, configuring
 * service and monitoring service.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @authot Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
public class AuditingManager {
    private static Logger log4j = DataBridgeLoger.getLogger(AuditingManager.class);
    private final static AuditingManager THIS_INSTANCE = new AuditingManager();

    /**
     * This map stores database current record number.
     */
    private Map recordNumberMap;
    private Map merchantInfoBeanMap;
    private Map loggerMap;
    /**
     * This map stores the merchant/logger pairs.
     */
    private Map merchantLoggerMap;
    private StatisticsHolder mainStatisticsHolder;
    private AuditingServiceBean bean;
    private long startTime;

    /**
     * The default constructor. This constructor is modified to private so getting
     * instance must call getInstance() method.
     */
    private AuditingManager () {
        startTime = System.currentTimeMillis();
        loggerMap = Collections.synchronizedMap(new HashMap());
        merchantLoggerMap = Collections.synchronizedMap(new HashMap());
        recordNumberMap = Collections.synchronizedMap(new HashMap());
        merchantInfoBeanMap = Collections.synchronizedMap(new HashMap());
        bean = new AuditingServiceBean();
        //initMerchantInfoMap();
        createStatisticsCounters();
    }

    /**
     * This method creates the statistics counters.
     */
    private void createStatisticsCounters () {
        log4j.debug("start createStatisticsCounters.");
        StatusCounter statusCounter = null;
        mainStatisticsHolder = new StatisticsHolder("");
        // add all counters holder.
        mainStatisticsHolder.add(new BaseCounter(PropertiesConstants.ALL));
        // add status counters holder.
        StatisticsHolder statusHolder = new StatisticsHolder(PropertiesConstants.STATUS);
        // create and add "Authenicated" counter.
        statusCounter = new StatusCounter(PropertiesConstants.AUTHENTICATED);
        statusCounter.addConditionPair("PaymentAuthRes", "Y");
        statusCounter.addConditionPair("PaymentAuthRes", "A");
        statusCounter.addConditionPair("ProfileAuthRes", "Y");
        statusCounter.addConditionPair("PARes", "Y");
        statusCounter.addConditionPair("PARes", "A");
        statusCounter.addConditionPair("IPRes", "Y");
        statusHolder.add(statusCounter);
        // create and add "Not Authenicated" counter.
        statusCounter = new StatusCounter(PropertiesConstants.NOT_AUTHENTICATED);
        statusCounter.addConditionPair("PaymentAuthRes", "N");
        statusCounter.addConditionPair("ProfileAuthRes", "N");
        statusCounter.addConditionPair("PARes", "N");
        statusCounter.addConditionPair("IPRes", "N");
        statusHolder.add(statusCounter);
        // create and add "Enrolled" counter.
        statusCounter = new StatusCounter(PropertiesConstants.ENROLLED);
        statusCounter.addConditionPair("PaymentVerifRes", "Y");
        statusCounter.addConditionPair("ProfileVerifRes", "Y");
        statusCounter.addConditionPair("VERes", "Y");
        statusHolder.add(statusCounter);
        // create and add "Not Enrolled" counter.
        statusCounter = new StatusCounter(PropertiesConstants.NOT_ENROLLED);
        statusCounter.addConditionPair("PaymentVerifRes", "N");
        statusCounter.addConditionPair("ProfileVerifRes", "N");
        statusCounter.addConditionPair("VERes", "N");
        statusHolder.add(statusCounter);
        // create and add "Unknow" counter.
        statusCounter = new StatusCounter(PropertiesConstants.UNKNOW);
        statusCounter.addConditionPair("PaymentVerifRes", "U");
        statusCounter.addConditionPair("PaymentAuthRes", "U");
        statusCounter.addConditionPair("ProfileVerifRes", "U");
        statusCounter.addConditionPair("ProfileAuthRes", "U");
        statusCounter.addConditionPair("VERes", "U");
        statusCounter.addConditionPair("PARes", "U");
        statusCounter.addConditionPair("IPRes", "U");
        statusHolder.add(statusCounter);
        //
        mainStatisticsHolder.add(statusHolder);
        // add the merchant counters holder.
        mainStatisticsHolder.add(new MerchantStatisticsHolder(PropertiesConstants.MERCHANT));

        // add the performance counter.
        mainStatisticsHolder.add(new PerformanceCounter(this));
        log4j.debug("end createStatisticsCounters.");
    }

    /**
     * This method returns the instance of this class.  Using this method to
     * guarentee only one instance in the JVM.
     * @return The instance of this class.
     */
    public static AuditingManager getInstance () {
        //if ( thisInstance == null ) {
        //  thisInstance = new AuditingManager();
        //}
        return  THIS_INSTANCE;
    }

    /**
     * This method adds a new auditing logger to the manager.
     * @param newBean The new logger's bean object.
     * @result <tt>true</tt> the new logger added, <tt>false</tt> otherwise.
     */
    public synchronized boolean addAuditingLogger (AuditingListenerBean newBean) {
        if (newBean == null || containsLogger(newBean.getId())) {
            log4j.error("The logger id \"" + newBean.getId() + "\" is duplicate.");
            return  false;
        }
        bean.addListener(newBean);
        loggerMap.put(newBean.getId(), new AuditingLogger(newBean));
        generateMerchantLoggerMap();
        return  true;
    }

    /**
     * This method removes an existed logger from the manager.
     * @param loggerId The logger id.
     */
    public synchronized boolean removeAuditingLogger (String loggerId) {
        if (!loggerMap.containsKey(loggerId)) {
            log4j.error("The logger id \"" + loggerId + "\" does not exists.");
            return  false;
        }
        bean.removeListener(loggerId);
        loggerMap.remove(loggerId);
        generateMerchantLoggerMap();
        return  true;
    }

    /**
     * This method updates the existed logger.
     * @param updateBean The bean that contains the updating data.
     * @return <tt>true</tt> if any data is changed, <tt>false</tt> otherwise.
     */
    public synchronized boolean updateAuditingLogger (AuditingListenerBean updateBean) {
        if (updateBean != null && containsLogger(updateBean.getId()) && getLogger(updateBean.getId()).update(updateBean)) {
            log4j.debug("update done.");
            generateMerchantLoggerMap();
            return  true;
        }
        else {
            log4j.warn("update not done.");
            return  false;
        }
    }

    /**
     * This method returns <tt>true</tt> if the logger is contained in this manager.
     * @param loggerID The logger id.
     * @return check result.
     */
    public boolean containsLogger (String loggerID) {
        return  loggerMap.containsKey(loggerID) && loggerMap.get(loggerID) !=
                null;
    }

    /**
     * This method checks if the given merchant id exsits in the merchant info
     * bean map.
     * @param merchantId The merchant id.
     * @return The checking result.
     */
    public boolean containsMerchantInLoggers (String merchantId) {
        return  merchantLoggerMap.containsValue(merchantId);
        //return merchantInfoBeanMap.containsKey(merchantId);
    }

    /**
     * This method returns the auditing logger object with the given logger id.
     * @param loggerId the logger id.
     * @return The auditing logger object.
     */
    public AuditingLogger getLogger (String loggerID) {
        return  (AuditingLogger)loggerMap.get(loggerID);
    }

    /**
     * This method returns the auditing logger object from the merchant id.
     * @param merchantId The metchant id.
     * @return The auditing logger object.
     */
    public AuditingLogger getLoggerFromMerchant (String merchantId) {
        return  getLogger((String)merchantLoggerMap.get(merchantId));
    }

    /**
     * This method returns the available logger id list.
     * @return The string array that contains the available logger id list.
     */
    public String[] getAvailableLoggerId () {
        List idList = new ArrayList(loggerMap.keySet());
        Collections.sort(idList);
        String[] result = new String[idList.size()];
        idList.toArray(result);
        return  result;
    }

    /**
     * This method counts the message based on the message properties.
     * @param - message properties
     */
    public void count( Properties props )
    {
            mainStatisticsHolder.count( props );
    }

    /**
     * This method returns the particular statistics counting current value.  This
     * method is called by MBean to display the current counting value on the
     * management console.
     * @param counterFullName The full counter's name.  The examples are as the
     * following:<br>
     * <tt>status.Authenticated</tt><br>
     * <tt>status.NotAuthenticated</tt><br>
     * and so on.
     * @param msgType  - The message type.
     * @param msgVersion - The message version.
     * @return The current counting value.
     */
    public Map getCountingValue (String counterFullName,
                                 String msgType, String msgVersion ) {
        MessageCountable counter = mainStatisticsHolder.getSubCounter(counterFullName);
        if (counter != null) {
            return  counter.getCountingValue(counterFullName,
                    msgType, msgVersion );
        }
        else {
            return  Collections.synchronizedMap( new HashMap() );
        }
    }

    /**
     * This method updates the Auditing Service configuration.
     * @param updateBean The bean that contains the updating data.
     * @return <tt>true</tt> if the Auditing Service configuration has been
     * updated, <tt>false</tt> otherwise.
     */
    public synchronized boolean update (AuditingServiceBean updateBean) {
        if (updateBean == null) {
            log4j.warn("Update Bean is null.");
            return  false;
        }
        boolean result = false;
        if (bean.getSamplingTime() != updateBean.getSamplingTime()) {
            bean.setSamplingTime(updateBean.getSamplingTime());
            getPerformanceCounter().setSamplingTime(updateBean.getSamplingTime());
            log4j.debug("samplingTime updated.");
            result = true;
        }
        if (!bean.getMerchantSchema().equals(updateBean.getMerchantSchema())) {
            bean.setMerchantSchema(updateBean.getMerchantSchema());
            log4j.debug("merchantSchema updated.");
            result = true;
        }
        if (bean.isMailAuth() != updateBean.isMailAuth()) {
            bean.setMailAuth(updateBean.isMailAuth());
            log4j.debug("mailAuth updated.");
            result = true;
        }
        if (!bean.getMailUsername().equals(updateBean.getMailUsername())) {
            bean.setMailUsername(updateBean.getMailUsername());
            log4j.debug("mailUsername updated.");
            result = true;
        }
        if (!bean.getMailPasswd().equals(updateBean.getMailPasswd())) {
            bean.setMailPasswd(updateBean.getMailPasswd());
            log4j.debug("mailPasswd updated.");
            result = true;
        }
        if (!bean.getMailInet().equals(updateBean.getMailInet())) {
            bean.setMailInet(updateBean.getMailInet());
            log4j.debug("mailInet updated.");
            result = true;
        }
        boolean loggerUpdateFlag = false;
        Vector removeList = new Vector();
        String key;
        for (Iterator i = bean.getListeners().keySet().iterator(); i.hasNext();) {
            key = (String)i.next();
            if (updateBean.getListeners().containsKey(key)) {
                AuditingLogger logger = getLogger(key);
                AuditingListenerBean subUpdateBean = updateBean.getListener(key);
                loggerUpdateFlag = logger.update(subUpdateBean) || loggerUpdateFlag;
                log4j.debug("handler \"" + key + "\" updated.");
            }
            else {
                removeList.add(key);
            }
        }
        log4j.debug("Begin remove the handlers.");
        for (Iterator lt = removeList.iterator(); lt.hasNext();) {
            key = (String)lt.next();
            removeAuditingLogger(key);
            log4j.debug("handler \"" + key + "\" removed.");
            loggerUpdateFlag = true;
        }
        for (Iterator i = updateBean.getListeners().keySet().iterator(); i.hasNext();) {
            key = (String)i.next();
            if (!bean.getListeners().containsKey(key)) {
                AuditingListenerBean subUpdateBean = updateBean.getListener(key);
                loggerUpdateFlag = addAuditingLogger(subUpdateBean) || loggerUpdateFlag;
                log4j.debug("handler \"" + key + "\" added.");
            }
        }
        if (loggerUpdateFlag) {
            generateMerchantLoggerMap();
        }
        result = result || loggerUpdateFlag;
        if (result) {
            flushCounters();
        }
        //bean = updateBean;
        return  result;
    }

    /**
     * This method flushes the all statistics counters.  This method is called by
     * MBean.
     */
    public void flushCounters () {
        mainStatisticsHolder.flush();
    }

    /**
     * This method returns the auditing server's bean.
     * @return The auditing server's bean.
     */
    public AuditingServiceBean getBean () {
        return  bean;
    }

    /**
     * This method sets the auditing server's bean.
     * @param bean The auditing server's bean.
     */
    public void setBean (AuditingServiceBean bean) {
        update(bean);
        this.bean = bean;
        initMerchantInfoMap();
        initCurrentNumber();
    }

    /**
     * This method generates merchant/logger map.
     */
    private void generateMerchantLoggerMap () {
        synchronized (merchantLoggerMap) {
            merchantLoggerMap.clear();
            for (Iterator i = bean.getListeners().keySet().iterator(); i.hasNext();) {
                String logId = (String)i.next();
                AuditingListenerBean subBean = bean.getListener(logId);
                for (Iterator j = subBean.getMerchantIds().iterator(); j.hasNext();) {
                    String merchantId = (String)j.next();
                    merchantLoggerMap.put(merchantId, logId);
                }
            }
        }
    }

    /**
     * put your documentation comment here
     * @return
     */
    public PerformanceCounter getPerformanceCounter () {
        return  (PerformanceCounter)mainStatisticsHolder.getSubCounter(PropertiesConstants.PERFORMANCE);
    }

    /**
     * This method save the mail template file.
     */
    public void saveMailTemplate () {
        Map listenerMap = bean.getListeners();
        for (Iterator i = listenerMap.keySet().iterator(); i.hasNext();) {
            String key = (String)i.next();
            getLogger(key).saveMailTemplate();
        }
    }

    /**
     * This method update the configuration when remove a merchant.
     * @param merchantId The removed merchant id.
     */
    public void removeMerchantFromLogger (String merchantId) {
        log4j.debug("start remove merchant from auditing service -- " + merchantId);
        String loggerId = (String)merchantLoggerMap.remove(merchantId);
        AuditingLogger logger = getLogger(loggerId);
        if (logger == null) {
            log4j.warn("logger does not exist.  for merchant -- " + merchantId);
            return;
        }
        AuditingListenerBean listenerBean = logger.getBean();
        List merchantList = listenerBean.getMerchantIds();
        merchantList.remove(merchantId);
        // update the listener.
        ListeningManager.getInstance().updateAuditingListener(listenerBean);
        log4j.debug("merchant [" + merchantId + "] remove form logger [" +
                logger.getId() + "].");
    }

    /**
     * This method returns the auditing server start time.
     * @return The auditing server start time.
     */
    public long getStartTime () {
        return  startTime;
    }

    /**
     * This method generates the current record number of all the auditing logger
     * database table.
     */
    public void initCurrentNumber () {
        recordNumberMap = Collections.synchronizedMap( new HashMap() );
        for (Iterator i = merchantInfoBeanMap.values().iterator(); i.hasNext();) {
            MerchantInfoBean infoBean = (MerchantInfoBean)i.next();
            initCurrentNumber(infoBean);
        }
    }

    /**
     * This method initializes the current number of the particular auditing logger
     * database.
     * @param infoBean The merchant info bean object.
     */
    public void initCurrentNumber (MerchantInfoBean infoBean) {
        String id = infoBean.getId();
        if ( recordNumberMap.containsKey(id) ) {
            log4j.debug("contains key [" + id + "] -- return.");
            return;
        }

        DataSource datasource = AuditingUtils.checkAndGetDataSource(infoBean);
        String schema = infoBean.getSchemaName();
        if (datasource == null) {
            log4j.error("Data Source does not exist.  initCurrentNumber() exit.");
            return  ;
        }

        int rint = 0;
        log4j.debug("start initCurrentNumber(" + datasource + ", " + schema );
        Connection con = null;
        try {
            con = datasource.getConnection();
            log4j.debug("getConnection.");
            checkAndCreateTables( con, schema );
			Statement stmt = null;
			ResultSet rs = null;
            try {
                String sql = "SELECT COUNT(*) FROM ";
                if ( schema != null && schema.trim().length() != 0 )
                {
                    sql += schema + "." + AuditingUtils.MESSAGE_TABLE_NAME;
                }
                else {
                    sql += AuditingUtils.MESSAGE_TABLE_NAME;
                }
                log4j.debug("SQL = " + sql);
                stmt = con.createStatement();
                rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    rint = rs.getInt(1);
                }
                else {
                    rint = 0;
                }
            } catch (Exception e) {
                log4j.error(
                        datasource + "." + schema + " -- get current number failed.",
                        e);
                rint = -1;
            } finally
			{
				if ( rs != null )
				{
				   try {
					   rs.close();
				   } catch ( Exception e )
				   {
				   }
				}
	            if ( stmt != null )
				{
				   try {
					   stmt.close();
				   } catch ( Exception e )
				   {
				   }
				}
			}
        } catch (Exception e)
        {
            log4j.error("Connection getting and table checking and creating failed.",
                    e);
            rint = -1;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    log4j.error("connection close failed.", e);
                }
            }

			JdbcUtils.closeDataSource( datasource );
            log4j.debug("end initCurrentNumber(" + datasource + ", " + schema
                        + ") " + rint);
        }

        recordNumberMap.put( id, new Integer( rint ) );
    }

    /**
     * This method checks the existence of the tables.  If the table is not existed,
     * create a new one.
     * @param con The database connection.
     * @param schemaName The database schema name.
     * @return <tt>false</tt> if the database does not support.
     * @throws Exception Throws if creating failed.
     */
    private boolean checkAndCreateTables (Connection con, String schemaName) throws Exception {
        log4j.debug("start checkAndCreateTables(Connection, String).");
        Set tableSet = Collections.synchronizedSet(new TreeSet());
        DatabaseMetaData meta = con.getMetaData();

        /**
         * Check which database is used
         */
        ResultSet metaRs = meta.getTables(null, schemaName == null || schemaName.trim().equals("") ?
                null : schemaName.toUpperCase(), null, new String[] {
            "TABLE"
        });
        while (metaRs.next()) {
            String tableName = metaRs.getString("TABLE_NAME");
            if (tableName != null) {
//                log4j.debug("Found exist table \"" + tableName + "\".");
                tableSet.add(tableName.toLowerCase());
            }
        }
        metaRs.close();
        if (!tableSet.contains( AuditingUtils.MESSAGE_TABLE_NAME )) {
            String tblscript = AuditingUtils.getMessageTableScript(
                    meta.getDatabaseProductName());
            log4j.debug("Creating table \"" + AuditingUtils.MESSAGE_TABLE_NAME + "\".");
            StringBuffer sql = new StringBuffer("CREATE TABLE ");
            if (schemaName != null && schemaName.trim().length() != 0) {
                sql.append(schemaName);
                sql.append(".");
            }
            sql.append( AuditingUtils.MESSAGE_TABLE_NAME );
            sql.append( tblscript );
            DataBridgeLoger.getLogger(this).debug("SQL = " + sql);
            Statement stmt = con.createStatement();
            stmt.executeUpdate(sql.toString());
            log4j.debug("\"" + AuditingUtils.MESSAGE_TABLE_NAME + "\" table created.");
            stmt.close();
        }
        return  true;
    }

    /**
     * This method returns the int that store the current number.
     * @param merchantBean - merchant info instance
     * @return The int that store the current number.
     */
    public int getCurrentRecordNumber (MerchantInfoBean merchantBean, boolean incFlag )
	{
		String id = merchantBean.getId();
        if ( !recordNumberMap.containsKey(id) ) {
            initCurrentNumber( merchantBean );
        }
        Integer num = (Integer)recordNumberMap.get( id );
        if ( num == null )
            return 0;
        else
        {
            if ( !incFlag )
                return num.intValue();
            else
            {
                int nvalue = num.intValue() + 1;
                recordNumberMap.put( id, new Integer( nvalue ) );
                return nvalue;
            }
        }
    }

    /**
     * This method returns the int that store the current number.
     * @param merchantBean The merchant info bean object.
     * @return The int array that store the current number.
     */
    public int getCurrentRecordNumber (MerchantInfoBean merchantBean) {
        return  getCurrentRecordNumber( merchantBean, false);
    }

    /**
     * This method returns the int that store the current number.
     * @param merchantBean The merchant info bean object.
     * @return The int array that store the current number.
     */
    public int increaseRecordNumber (MerchantInfoBean merchantBean) {
        return  getCurrentRecordNumber( merchantBean, true );
    }

    /**
     * This method sets the merchant info bean into the map.
     * @param infoBean The bean to be set.
     */
    public void setMerchantInfoBean (MerchantInfoBean infoBean) {
        log4j.debug("setMerchantInfoBean -- " + infoBean.getId());
        merchantInfoBeanMap.put(infoBean.getId(), infoBean);
        initCurrentNumber(infoBean);
    }

    /**
     * This method returns the merchant info bean.
     * @param merchantId The merchant id.
     * @return The merchant info bean.
     */
    public MerchantInfoBean getMerchantInfoBean (String merchantId) {
        log4j.debug("getMerchantInfoBean -- " + merchantId);
        return  (MerchantInfoBean)merchantInfoBeanMap.get(merchantId);
    }

    /**
     * This method removes the merchant info bean from the map.
     * @param merchantId The merchant id.
     */
    public void removeMerchantInfoBean (String merchantId) {
        log4j.debug("removeMerchantInfoBean -- " + merchantId);
        merchantInfoBeanMap.remove(merchantId);
        removeMerchantFromLogger(merchantId);
    }

    /**
     * This method put all merchant info data into the map.
     */
    public void initMerchantInfoMap () {
        MerchantInfo merchantMBean = new MerchantInfo();
        Properties[] propsList = merchantMBean.retrieveAll();
        if (propsList == null) {
            log4j.warn("merchant list retrieving failed.");
            // set all logger handlers "initializedFlag" to <false>.
            for (Iterator i = loggerMap.values().iterator(); i.hasNext();) {
                ((AuditingLogger)i.next()).setInitialized(false);
            }
            return;
        }
        synchronized (merchantInfoBeanMap) {
            merchantInfoBeanMap.clear();
            for (int i = 0; i < propsList.length; i++) {
                MerchantInfoBean infoBean = new MerchantInfoBean();
                infoBean.fromProperties(propsList[i]);
                merchantInfoBeanMap.put(infoBean.getId(), infoBean);
            }
        }
        log4j.debug("initMerchantInfoMap() done.");
    }

    /**
     * This method generates the data source key.
     * @param jndi The jndi name.
     * @param schema The schema name.
     * @return The key.
     */
    private String getDataSourceKey (String jndi, String schema) {
        return  jndi + ConfigureConstants.MESSAGE_KEY_DELIMITER + schema.toUpperCase();
    }

    /**
     * This method generates the data source key.
     * @param infoBean The merchant info bean object.
     * @return The key.
     */
    private String getDataSourceKey (MerchantInfoBean infoBean) {
        return  getDataSourceKey(infoBean.getDataSourceJndi(), infoBean.getSchemaName());
    }

    /**
     * This method remove data source from the application.
     * @param jndi
     */
    public void removeDataSource (String jndi) {
        AuditingUtils.removeDataSource(jndi);
        for (Iterator i = recordNumberMap.keySet().iterator(); i.hasNext();) {
            String key = (String)i.next();
            log4j.debug("key = " + key);
            if (key.startsWith(jndi + ConfigureConstants.MESSAGE_KEY_DELIMITER)) {
                i.remove();
                log4j.debug("key [" + key + "] removed.");
            }
        }
    }

    /**
     *
     * @return - return main statistics holder
     */
    public StatisticsHolder getMainStatisticsHolder()
    {
        return mainStatisticsHolder;
    }
}



