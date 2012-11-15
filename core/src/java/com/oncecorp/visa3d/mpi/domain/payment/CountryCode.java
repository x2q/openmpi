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

package com.oncecorp.visa3d.mpi.domain.payment;


import java.util.HashMap;

/**
 * This class provides validation for ISO3166 Country Codes
 */
public class CountryCode {
    private static HashMap countryCodeList = new HashMap();

    static {
        countryCodeList.put("004", "AFG");
        countryCodeList.put("008", "ALB");
        countryCodeList.put("010", "ATA");
        countryCodeList.put("012", "DZA");
        countryCodeList.put("016", "ASM");
        countryCodeList.put("020", "AND");
        countryCodeList.put("024", "AGO");
        countryCodeList.put("028", "ATG");
        countryCodeList.put("031", "AZE");
        countryCodeList.put("032", "ARG");
        countryCodeList.put("036", "AUS");
        countryCodeList.put("040", "AUT");
        countryCodeList.put("044", "BHS");
        countryCodeList.put("048", "BHR");
        countryCodeList.put("050", "BGD");
        countryCodeList.put("051", "ARM");
        countryCodeList.put("052", "BRB");
        countryCodeList.put("056", "BEL");
        countryCodeList.put("060", "BMU");
        countryCodeList.put("064", "BTN");
        countryCodeList.put("068", "BOL");
        countryCodeList.put("070", "BIH");
        countryCodeList.put("072", "BWA");
        countryCodeList.put("074", "BVT");
        countryCodeList.put("076", "BRA");
        countryCodeList.put("084", "BLZ");
        countryCodeList.put("086", "IOT");
        countryCodeList.put("090", "SLB");
        countryCodeList.put("092", "VGB");
        countryCodeList.put("096", "BRN");
        countryCodeList.put("100", "BGR");
        countryCodeList.put("104", "MMR");
        countryCodeList.put("108", "BDI");
        countryCodeList.put("112", "BLR");
        countryCodeList.put("116", "KHM");
        countryCodeList.put("120", "CMR");
        countryCodeList.put("124", "CAN");
        countryCodeList.put("132", "CPV");
        countryCodeList.put("136", "CYM");
        countryCodeList.put("140", "CAF");
        countryCodeList.put("144", "LKA");
        countryCodeList.put("148", "TCD");
        countryCodeList.put("152", "CHL");
        countryCodeList.put("156", "CHN");
        countryCodeList.put("158", "TWN");
        countryCodeList.put("162", "CXR");
        countryCodeList.put("166", "CCK");
        countryCodeList.put("170", "COL");
        countryCodeList.put("174", "COM");
        countryCodeList.put("175", "MYT");
        countryCodeList.put("178", "COG");
        countryCodeList.put("180", "ZAR");
        countryCodeList.put("184", "COK");
        countryCodeList.put("188", "CRI");
        countryCodeList.put("191", "HRV");
        countryCodeList.put("192", "CUB");
        countryCodeList.put("196", "CYP");
        countryCodeList.put("203", "CZE");
        countryCodeList.put("204", "BEN");
        countryCodeList.put("208", "DNK");
        countryCodeList.put("212", "DMA");
        countryCodeList.put("214", "DOM");
        countryCodeList.put("218", "ECU");
        countryCodeList.put("222", "SLV");
        countryCodeList.put("226", "GNQ");
        countryCodeList.put("231", "ETH");
        countryCodeList.put("232", "ERI");
        countryCodeList.put("233", "EST");
        countryCodeList.put("234", "FRO");
        countryCodeList.put("238", "FLK");
        countryCodeList.put("239", "SGS");
        countryCodeList.put("242", "FJI");
        countryCodeList.put("246", "FIN");
        countryCodeList.put("249", "FXX");
        countryCodeList.put("250", "FRA");
        countryCodeList.put("254", "GUF");
        countryCodeList.put("258", "PYF");
        countryCodeList.put("260", "ATF");
        countryCodeList.put("262", "DJI");
        countryCodeList.put("266", "GAB");
        countryCodeList.put("268", "GEO");
        countryCodeList.put("270", "GMB");
        countryCodeList.put("276", "DEU");
        countryCodeList.put("288", "GHA");
        countryCodeList.put("292", "GIB");
        countryCodeList.put("296", "KIR");
        countryCodeList.put("300", "GRC");
        countryCodeList.put("304", "GRL");
        countryCodeList.put("308", "GRD");
        countryCodeList.put("312", "GLP");
        countryCodeList.put("316", "GUM");
        countryCodeList.put("320", "GTM");
        countryCodeList.put("324", "GIN");
        countryCodeList.put("328", "GUY");
        countryCodeList.put("332", "HTI");
        countryCodeList.put("334", "HMD");
        countryCodeList.put("336", "VAT");
        countryCodeList.put("340", "HND");
        countryCodeList.put("344", "HKG");
        countryCodeList.put("348", "HUN");
        countryCodeList.put("352", "ISL");
        countryCodeList.put("356", "IND");
        countryCodeList.put("360", "IDN");
        countryCodeList.put("364", "IRN");
        countryCodeList.put("368", "IRQ");
        countryCodeList.put("372", "IRL");
        countryCodeList.put("376", "ISR");
        countryCodeList.put("380", "ITA");
        countryCodeList.put("384", "CIV");
        countryCodeList.put("388", "JAM");
        countryCodeList.put("392", "JPN");
        countryCodeList.put("398", "KAZ");
        countryCodeList.put("400", "JOR");
        countryCodeList.put("404", "KEN");
        countryCodeList.put("408", "PRK");
        countryCodeList.put("410", "KOR");
        countryCodeList.put("414", "KWT");
        countryCodeList.put("417", "KGZ");
        countryCodeList.put("418", "LAO");
        countryCodeList.put("422", "LBN");
        countryCodeList.put("426", "LSO");
        countryCodeList.put("428", "LVA");
        countryCodeList.put("430", "LBR");
        countryCodeList.put("434", "LBY");
        countryCodeList.put("438", "LIE");
        countryCodeList.put("440", "LTU");
        countryCodeList.put("442", "LUX");
        countryCodeList.put("446", "MAC");
        countryCodeList.put("450", "MDG");
        countryCodeList.put("454", "MWI");
        countryCodeList.put("458", "MYS");
        countryCodeList.put("462", "MDV");
        countryCodeList.put("466", "MLI");
        countryCodeList.put("470", "MLT");
        countryCodeList.put("474", "MTQ");
        countryCodeList.put("478", "MRT");
        countryCodeList.put("480", "MUS");
        countryCodeList.put("484", "MEX");
        countryCodeList.put("492", "MCO");
        countryCodeList.put("496", "MNG");
        countryCodeList.put("498", "MDA");
        countryCodeList.put("500", "MSR");
        countryCodeList.put("504", "MAR");
        countryCodeList.put("508", "MOZ");
        countryCodeList.put("512", "OMN");
        countryCodeList.put("516", "NAM");
        countryCodeList.put("520", "NRU");
        countryCodeList.put("524", "NPL");
        countryCodeList.put("528", "NLD");
        countryCodeList.put("530", "ANT");
        countryCodeList.put("533", "ABW");
        countryCodeList.put("540", "NCL");
        countryCodeList.put("548", "VUT");
        countryCodeList.put("554", "NZL");
        countryCodeList.put("558", "NIC");
        countryCodeList.put("562", "NER");
        countryCodeList.put("566", "NGA");
        countryCodeList.put("570", "NIU");
        countryCodeList.put("574", "NFK");
        countryCodeList.put("578", "NOR");
        countryCodeList.put("580", "MNP");
        countryCodeList.put("581", "UMI");
        countryCodeList.put("583", "FSM");
        countryCodeList.put("584", "MHL");
        countryCodeList.put("585", "PLW");
        countryCodeList.put("586", "PAK");
        countryCodeList.put("591", "PAN");
        countryCodeList.put("598", "PNG");
        countryCodeList.put("600", "PRY");
        countryCodeList.put("604", "PER");
        countryCodeList.put("608", "PHL");
        countryCodeList.put("612", "PCN");
        countryCodeList.put("616", "POL");
        countryCodeList.put("620", "PRT");
        countryCodeList.put("624", "GNB");
        countryCodeList.put("626", "TMP");
        countryCodeList.put("630", "PRI");
        countryCodeList.put("634", "QAT");
        countryCodeList.put("638", "REU");
        countryCodeList.put("642", "ROM");
        countryCodeList.put("643", "RUS");
        countryCodeList.put("646", "RWA");
        countryCodeList.put("654", "SHN");
        countryCodeList.put("659", "KNA");
        countryCodeList.put("660", "AIA");
        countryCodeList.put("662", "LCA");
        countryCodeList.put("666", "SPM");
        countryCodeList.put("670", "VCT");
        countryCodeList.put("674", "SMR");
        countryCodeList.put("678", "STP");
        countryCodeList.put("682", "SAU");
        countryCodeList.put("686", "SEN");
        countryCodeList.put("690", "SYC");
        countryCodeList.put("694", "SLE");
        countryCodeList.put("702", "SGP");
        countryCodeList.put("703", "SVK");
        countryCodeList.put("704", "VNM");
        countryCodeList.put("705", "SVN");
        countryCodeList.put("706", "SOM");
        countryCodeList.put("710", "ZAF");
        countryCodeList.put("716", "ZWE");
        countryCodeList.put("724", "ESP");
        countryCodeList.put("732", "ESH");
        countryCodeList.put("736", "SDN");
        countryCodeList.put("740", "SUR");
        countryCodeList.put("744", "SJM");
        countryCodeList.put("748", "SWZ");
        countryCodeList.put("752", "SWE");
        countryCodeList.put("756", "CHE");
        countryCodeList.put("760", "SYR");
        countryCodeList.put("762", "TJK");
        countryCodeList.put("764", "THA");
        countryCodeList.put("768", "TGO");
        countryCodeList.put("772", "TKL");
        countryCodeList.put("776", "TON");
        countryCodeList.put("780", "TTO");
        countryCodeList.put("784", "ARE");
        countryCodeList.put("788", "TUN");
        countryCodeList.put("792", "TUR");
        countryCodeList.put("795", "TKM");
        countryCodeList.put("796", "TCA");
        countryCodeList.put("798", "TUV");
        countryCodeList.put("800", "UGA");
        countryCodeList.put("804", "UKR");
        countryCodeList.put("807", "MKD");
        countryCodeList.put("818", "EGY");
        countryCodeList.put("826", "GBR");
        countryCodeList.put("834", "TZA");
        countryCodeList.put("840", "USA");
        countryCodeList.put("850", "VIR");
        countryCodeList.put("854", "BFA");
        countryCodeList.put("858", "URY");
        countryCodeList.put("860", "UZB");
        countryCodeList.put("862", "VEN");
        countryCodeList.put("876", "WLF");
        countryCodeList.put("882", "WSM");
        countryCodeList.put("887", "YEM");
        countryCodeList.put("891", "YUG");
        countryCodeList.put("894", "ZMB");
    }


    public static final boolean isvalid(String code) {
        return countryCodeList.containsKey(code);
    }
  }
