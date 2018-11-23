package Indicators;

import java.sql.SQLException;

public class TemporaryTable extends Connection{

	public void dropTempTable(java.sql.Connection dbConnection, String symbol){
		String sql = "drop TEMPORARY TABLE "+symbol+"_Temp";
		try {
			executeSqlQuery(dbConnection, sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void createTempTable(java.sql.Connection dbConnection, String symbol){
		String sql = "CREATE TEMPORARY TABLE  "+symbol+"_Temp ( "+
	  			  " Tradedate datetime NOT NULL, "+
	  			  " Open varchar(10) NOT NULL," +
	  			  " High varchar(10) NOT NULL," +
	  			  " Low varchar(10) NOT NULL," +
	  			  " Close varchar(10) NOT NULL," +
	  			  " psar varchar(45) DEFAULT '' ," +
	  			  " dir varchar(10) DEFAULT '' ," +
	  			  " reversal varchar(45) DEFAULT '' ," +
	  			  " ProfitonNextDaySell float DEFAULT 0," +
	  			  " volume bigint(20) DEFAULT NULL," +
	  			  " EMA1 float DEFAULT 0," +
	  			  " EMA2 float DEFAULT 0," +
	  			  " MACD float DEFAULT 0," +
	  			  " HISTOGRAM float DEFAULT 0," +
	  			  " SIG float DEFAULT 0," +
	  			  " MACD_CHANGE_DIR varchar(45) DEFAULT '' ," +
	  			  " Hist_Zero_Cross varchar(45) DEFAULT '' ," +
	  			  " TR float DEFAULT 0," +
	  			  " DM_1_PLUS float DEFAULT 0," +
	  			  " DM_1_Minus float DEFAULT 0," +
	  			  " TR_AVARAGE float DEFAULT 0," +
	  			  " DM_PLUS_AVERAGE float DEFAULT 0," +
	  			  " DM_MINUS_AVERAGE float DEFAULT 0," +
	  			  " DI_PLUS_AVERAGE float DEFAULT 0," +
	  			  " DI_MINUS_AVERAGE float DEFAULT 0," +
	  			  " DI_DIFF float DEFAULT 0," +
	  			  " DI_SUM float DEFAULT 0," +
	  			  " DX float DEFAULT 0," +
	  			  " ADX float DEFAULT 0," +
	  			  " ADX_DM_Crossover varchar(40) DEFAULT '' ," +
	  			  " ADX_Bull_PROFIT_ON_DM_CROSS float DEFAULT 0," +
	  			  " ADX_Bear_PROFIT_ON_DM_CROSS float DEFAULT 0," +
	  			  " ADX_Bull_PROFIT_RUPEES float DEFAULT 0," +
	  			  " ADX_Bear_PROFIT_RUPEES float DEFAULT 0," +
	  			  " ADX_Bullish_Enter varchar(50) DEFAULT '' ," +
	  			  " ADX_Bearish_Enter varchar(50) DEFAULT '' ," +
	  			  " ADX_Bull_Entered_profit float DEFAULT 0," +
	  			  " ADX_Bear_Entered_profit float DEFAULT 0," +
	  			  " ADX_BULL_ENTERED_PROFIT_Rs float DEFAULT 0," +
	  			  " ADX_BEAR_ENTERED_PROFIT_Rs float DEFAULT 0," +
	  			  " WILL_HIGH float DEFAULT 0," +
	  			  " WILL_LOW float DEFAULT 0," +
	  			  " WILL_R float DEFAULT 0," +
	  			  " WILL_BULL_PROFIT float DEFAULT 0," +
	  			  " WILL_BEAR_PROFIT float DEFAULT 0," +
	  			  " WILL_BULL_PROFIT_Rs float DEFAULT 0," +
	  			  " WILL_Bear_PROFIT_Rs float DEFAULT 0," +
	  			  " WILL_Reversal varchar(50) DEFAULT '' ," +
	  			  " MACD_BULL_PROFIT float DEFAULT 0," +
	  			  " MACD_BEAR_PROFIT float DEFAULT 0," +
	  			  " MACD_BULL_PROFIT_Rs float DEFAULT 0," +
	  			  " MACD_Bear_PROFIT_Rs float DEFAULT 0," +
	  			  " Typical_Price float DEFAULT 0," +
	  			  " Typical_Price_Mean float DEFAULT 0," +
	  			  " Mean_Deviation float DEFAULT 0," +
	  			  " CCI float DEFAULT 0," +
	  			  " CCI_Reversal varchar(50) DEFAULT '' ," +
	  			  " CCI_Bull_profit float DEFAULT 0," +
	  			  " CCI_Bull_profit_Rs float DEFAULT 0," +
	  			  " CCI_Bear_profit float DEFAULT 0," +
	  			  " CCI_Bear_profit_Rs float DEFAULT 0," +
	  			  " SMA_ENVELP float DEFAULT 0," +
	  			  " UPPER_ENVELP float DEFAULT 0," +
	  			  " LOWER_ENVELP float DEFAULT 0," +
	  			  " ENVLP_CROSS varchar(50) DEFAULT '' ," +
	  			  " MAEnvelope_BULL_PROFIT float DEFAULT 0," +
	  			  " MAEnvelope_BULL_PROFIT_Rs float DEFAULT 0," +
	  			  " MAEnvelope_Bear_PROFIT float DEFAULT 0," +
	  			  " MAEnvelope_Bear_PROFIT_Rs float DEFAULT 0," +
	  			  " PMO_ROC float DEFAULT 0," +
	  			  " PMO_CUSTOM_ROC_EMA float DEFAULT 0," +
	  			  " PMO_CUSTOM_EMA float DEFAULT 0," +
	  			  " PMO_SIGNAL float DEFAULT 0," +
	  			  " PMO_BULL_PROFIT float DEFAULT 0," +
	  			  " PMO_BEAR_PROFIT float DEFAULT 0," +
	  			  " PMO_REVERSAL varchar(50) DEFAULT '' ," +
	  			  " ATR float DEFAULT 0 ," +
	  			" SuperTrend_UP float DEFAULT 0 ," +
	  			" SuperTrend_DOWN float DEFAULT 0 ," +
	  			" SuperTrend_Reversal varchar(10) DEFAULT '' ," +
	  			" Will_R_5 float DEFAULT 0 ," +
	  			" SuperTrend_UP_Band FLOAT default 0 ," +
	  			" SuperTrend_Down_Band FLOAT default 0 ," +
	  			" SuperTrend FLOAT default 0 ," +
	  			" OBV varchar(100) DEFAULT '' ," +
	  			" ROC varchar(100) DEFAULT '' ," +   
	  			" breakout1 VARCHAR(50) default '' ," +
	  			" breakout2 VARCHAR(50) default '' ," +
	  			" BB_std_dev_20 FLOAT default 0 ," +
	  			" BB_middleBand_20 FLOAT default 0 ," +
	  			" BB_lowerBand_20 FLOAT default 0 ," +
	  			" BB_upperBand_20 FLOAT default 0 ," +
	  			" BollingerBW FLOAT default 0 ," +
	  			
				" pivot varchar(40) default 0 ," +
				" R1 varchar(40) default 0 ," +
				" R2 varchar(40) default 0 ," +
				" R3 varchar(40) default 0 ," +
				" S1 varchar(40) default 0 ," +
				" S2 varchar(40) default 0 ," +
				" S3 varchar(40) default 0 ," +
				
				" Camarilla_L1 varchar(10) default 0 ," +
				" Camarilla_L2 varchar(10) default 0 ," +
				" Camarilla_L3 varchar(10) default 0 ," +
				" Camarilla_L4 varchar(10) default 0 ," +
				" Camarilla_L5 varchar(10) default 0 ," +
				" Camarilla_L6 varchar(10) default 0 ," +
				
				" Camarilla_H1 varchar(10) default 0 ," +
				" Camarilla_H2 varchar(10) default 0 ," +
				" Camarilla_H3 varchar(10) default 0 ," +
				" Camarilla_H4 varchar(10) default 0 ," +
				" Camarilla_H5 varchar(10) default 0 ," +
				" Camarilla_H6 varchar(10) default 0 ," +

				" HA_OPEN varchar(50) default 0 ," +
				" HA_HIGH varchar(50) default 0 ," +
				" HA_LOW varchar(50) default 0 ," +
				" HA_CLOSE varchar(50) default 0 ," +

				" SMA varchar(50) default 0 ," +
				" avg_Volume varchar(50) default '' ," +
				" max_past_high varchar(50) default '' ," +
				" min_past_low varchar(50) default '' ," +
	  			  " PRIMARY KEY (Tradedate)" +
	  			 " ) ENGINE=InnoDB DEFAULT  CHARSET=utf8;";
		try {
			executeSqlQuery(dbConnection, sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
