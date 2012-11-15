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

import com.oncecorp.visa3d.bridge.beans.StatusCategoryID;
/**
 * Title:        ONCE MPI Data Bridge
 * Description:  This interface defines the property name countants that are
 * used in the auditing service.
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Once Corporation
 * @author yge@oncecorp.com
 * @version 1.0
 */

public interface  PropertiesConstants {
  /**
   * All kinds of the message type.
   */
  public static final String          ALL_TYPE              = "AllType";
  public static final String          VE_REQ                = "VEReq";
  public static final String          VE_RES                = "VERes";
  public static final String          PA_REQ                = "PAReq";
  public static final String          PA_RES                = "PARes";
  public static final String          IP_REQ                = "IPReq";
  public static final String          IP_RES                = "IPRes";
  public static final String          ERROR                 = "Error";
  public static final String          PAYMENT_VERIF_REQ     = "PaymentVerifReq";
  public static final String          PAYMENT_VERIF_RES     = "PaymentVerifRes";
  public static final String          PAYMENT_AUTH_REQ      = "PaymentAuthReq";
  public static final String          PAYMENT_AUTH_RES      = "PaymentAuthRes";
  public static final String          MPI_ERROR             = "MPIError";
  public static final String          PROFILE_VERIF_REQ     = "ProfileVerifReq";
  public static final String          PROFILE_VERIF_RES     = "ProfileVerifRes";
  public static final String          PROFILE_AUTH_REQ      = "ProfileAuthReq";
  public static final String          PROFILE_AUTH_RES      = "ProfileAuthRes";

  // The logger properties name.
  /*
  public static final String          CURRENT_NUMBER       = "CurrentNumber";
  public static final String          THRESHOLD_NUMBER     = "ThresholdNumber";
  public static final String          MAXIMUM_NUMBER       = "MaximumNumber";
  public static final String          MAIL_SENDER          = "MailSender";
  public static final String          MAIL_TO              = "MailTo";
  public static final String          MAIL_CC              = "MailCc";
  public static final String          MAIL_SUBJECT         = "MailSubject";
  */

  // The status category name.
  /**
   * statistics per message type.
   */
  public static final String          ALL                  = "All";
  /**
   * statistics per message status.
   */
  public static final String          STATUS               = "Status";
  /**
   * statistics per merchant.
   */
  public static final String          MERCHANT             = "Merchant";
  /**
   * performance metrics.
   */
  public static final String          PERFORMANCE          = "Performance";

  // The status statistics items name.
  /**
   * Authenticated status.
   */
  public static final String          AUTHENTICATED        = "Authenticated";
  /**
   * Not Authenticated status.
   */
  public static final String          NOT_AUTHENTICATED    = "Not Authenticated";
  /**
   * Enrolled status.
   */
  public static final String          ENROLLED             = "Enrolled";
  /**
   * Not Enrolled status.
   */
  public static final String          NOT_ENROLLED         = "Not Enrolled";
  /**
   * Unknow status.
   */
  public static final String          UNKNOW               = "Unknown";

  public static final StatusCategoryID[]  STATUS_CATEGORY_ID_LIST = {
    new StatusCategoryID(AUTHENTICATED, PAYMENT_AUTH_RES),
//    new StatusCategoryID(AUTHENTICATED, PROFILE_AUTH_RES),
    new StatusCategoryID(AUTHENTICATED, PA_RES),
//    new StatusCategoryID(AUTHENTICATED, IP_RES),
    new StatusCategoryID(NOT_AUTHENTICATED, PAYMENT_AUTH_RES),
//    new StatusCategoryID(NOT_AUTHENTICATED, PROFILE_AUTH_RES),
    new StatusCategoryID(NOT_AUTHENTICATED, PA_RES),
//    new StatusCategoryID(NOT_AUTHENTICATED, IP_RES),
    new StatusCategoryID(ENROLLED, PAYMENT_VERIF_RES),
//    new StatusCategoryID(ENROLLED, PROFILE_VERIF_RES),
    new StatusCategoryID(ENROLLED, VE_RES),
    new StatusCategoryID(NOT_ENROLLED, PAYMENT_VERIF_RES),
//    new StatusCategoryID(NOT_ENROLLED, PROFILE_VERIF_RES),
    new StatusCategoryID(NOT_ENROLLED, VE_RES),
    new StatusCategoryID(UNKNOW, PAYMENT_VERIF_RES),
    new StatusCategoryID(UNKNOW, PAYMENT_AUTH_RES),
//    new StatusCategoryID(UNKNOW, PROFILE_VERIF_RES),
//    new StatusCategoryID(UNKNOW, PROFILE_AUTH_RES),
    new StatusCategoryID(UNKNOW, PA_RES),
    new StatusCategoryID(UNKNOW, VE_RES),
//    new StatusCategoryID(UNKNOW, IP_RES)
  };

/*  public static final String[]          STATUS_LIST = {
    AUTHENTICATED,
    NOT_AUTHENTICATED,
    ENROLLED,
    NOT_ENROLLED,
    UNKNOW
  };*/
  // The performance statistics name.
  //public static final String          BOTH_PEAK_AVG_NUMBER = "BothPeakAverageNumber";
  /**
   * Peak TPS.
   */
  public static final String          PEAK_NUMBER          = "PeakNumber";
  /**
   * Average TPS.
   */
  public static final String          AVERAGE_NUMBER       = "AverageNumber";

  /**
   * jndi name of merchant database.
   */
  //public static final String          MERCHANT_DATABASE_JNDI = "MySQLMerchant";
}