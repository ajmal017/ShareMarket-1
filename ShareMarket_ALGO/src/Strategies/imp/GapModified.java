package Strategies.imp;

import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.record.DBCellRecord;

import Indicators.Connection;
import Indicators.Test;

public class GapModified extends Connection {
	int sl=3;
	boolean isSL=true;//true
	int optionVolume=1000000;
	static long transaction = 100000000;
	boolean exitNextDayClose = false;float maxLossForNextDayExit=-10f;
	String isEntryAtOpenOr3MinClose = "a.intradayFirst3MinClose";//a.intradayOpen, intradayFirst3MinClose
	boolean isCheckFirstCloseBelowAboveOpen = false;
	String intradayOpen="a.open"; //intradayOpen //comparing with open price of day or intra open
	String closeAtWhatTime="a.intraday3Min3_18_close";
	static boolean isIndexCheck = false;
	static boolean isOtherStrategy = false;
	static int sY=2015;
	static String sM="01";
	static String d1="2015-01-01", d2="2019-12-31"; 
	static String d1T="2016-01-01", d2T="2019-05-31"; 
	
	static float slPerc = -0.4f;
	static float targetPerc = 20f;
	static float hitPercAndWentNegative = 0.5f;
	static float SLWhenhitPercAndWentNegative = -0.0f;
	static String resultTable = "results";//table to insert SL results across all symbols in a day
	static String tableName="williamsresults3";//williamsResults
	static String tableToUpdate="williamsresults2";
	static String dateField="date";
	//table from which get profit and put in result table
	static String tableToDeleteUnwantedRecords="williamsresults2";
	
	boolean isOptionCheck=false;
	float buyAtCheaper = 2f;
	String d = "2014-07-01";
	int daysToAvg=2;
	static float indexPerc=0.2f;
	static float gapWithPrevAvgClose=3f;
	static float capital=80000;
	static String strategyFilter=" ";// and year(a.tradedate)=2011
	
	static int start=2, end=3;
	static boolean isOpen=false;
	
	static boolean isUpdateIntraIntoWilliamsResults = false;
	static boolean isCheckProfitWithSL=false;
	static boolean isCheckWhenItReachedTarget=false;
	static boolean isGetProfitAndEnterContinously = false; static String dateAfterTargetAchieved = "";
	static int targetProfit=200000;
	static boolean isDrawDownCalculate = false;
	static boolean isDeleteUnwantedRecords = false;
	static boolean isCheckBothBuySellProbability = false;
	static boolean isCamarillaCheck = false;
	static float mul=7f;
	static int execute=1;
	static String filter = "  ";//and year(date)=2017 and month(date)>=1 and month(date)<=12
	
	static int maxSymbolsToPlaceOrder=100;
	static float algoPerc=0.8f;
	static int first3MinTransaction = 0;
	static String indexTable="nifty_50";//nifty_auto,nifty_bank,nifty_energy,nifty_finance,nifty_fmcg,nifty_it,nifty_media,nifty_metal,nifty_midcap_50,nifty_pharma,nifty_psu,nifty_realty
	float indexGapDiv=0.6f;
	static String margin;
	long avgTotalTrades = 7000, maxAvgTotalTrades=220000000;
	public float getIntraOpen(java.sql.Connection con, String name, String date, float daysOpen){
		try{
			String sql = "select open from `"+name+"_60` where date(tradedate)=date('" + date + "') limit 1";
			String intraOpen = executeCountQuery(con, sql);
			if(intraOpen != null || !intraOpen.equals("")) {
				if(Math.abs((Float.parseFloat(intraOpen)- daysOpen)*100/daysOpen) >10){
					return daysOpen;
				}else{
					return Float.parseFloat(intraOpen);
				}
			}
			else return daysOpen;
		}catch(NumberFormatException e){
			return daysOpen;
		}
	}
	public void getOnPrevHighLowCompare(java.sql.Connection con, String name, float gapPerc,
			long transaction, float min, float max, float gapLimitPerc, float highLowGap,boolean isOtherStrategy) {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List open = new ArrayList<Float>();List nopen = new ArrayList<Float>();
		List high = new ArrayList<Float>();List nhigh = new ArrayList<Float>();
		List low = new ArrayList<Float>();List nlow = new ArrayList<Float>();
		List close = new ArrayList<Float>();List nclose = new ArrayList<Float>();
		List intraOpen = new ArrayList<Float>();
		List intraFirst3MinClose = new ArrayList<Float>();
		List intraFirstHrClose = new ArrayList<Float>();
		List intraFC = new ArrayList<Float>(); 
		List intraSC = new ArrayList<Float>(); 
		List intraClose = new ArrayList<Float>();
		List intraday3Min3_18_Close = new ArrayList<Float>();
		List intradayAt3_15 = new ArrayList<Float>();
		List intradayHigh = new ArrayList<Float>();
		List intradayLow = new ArrayList<Float>();
		
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			// name= name+"_FUT";
			sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, "
					+ " a.intraFirstMinClose, "+closeAtWhatTime+", a.totalTrades, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ "  and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, n.open,a.intradayHigh, a.intradayLow, n.high, a.totalTrades, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			}
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalTrades"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intradayHigh.add(rs.getFloat("a.intradayHigh"));
				intradayLow.add(rs.getFloat("a.intradayLow"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat(closeAtWhatTime));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			float  div = 2f;
			int iter = 1;
			String out = "";
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;String optionProfit = "0";
			boolean indexFilter=true;
			for (int i = 5; i < tradedate.size(); i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)intraday3Min3_18_Close.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				float trigger = (float) intraOpen.get(i);
//				float trigger = (float) intraFirst3MinClose.get(i);
				if(isIndexCheck){
//					indexFilter = ((float)nopen.get(i) - (float)nclose.get(i-1))*100/(float)nclose.get(i-1) > gapPerc/indexGapDiv;
					indexFilter = ((float)nopen.get(i) - (float)nhigh.get(i-1))*100/(float)nopen.get(i) > gapPerc/indexGapDiv;
				}
				float actualGap=(trigger - (float) high.get(i - 1)) * 100 / trigger;
				float actualGap2=(trigger - (float) close.get(i - 1)) * 100 / trigger;
				if (actualGap > gapPerc
						&& (trigger - (float) close.get(i - 1)) * 100 / trigger < gapLimitPerc && indexFilter)
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger > min && 
							trigger < max && (float) open.get(i - 1) != (float) close.get(i - 1);
					// && (float)open.get(i)>(float)close.get(i-1) ;
					// if(((float)open.get(i-1)==(float)high.get(i-1)))
//					if (((float) open.get(i - 1) - (float) close.get(i - 1)) * 100
//							/ (float) open.get(i - 1) > prevBodyGap && filter)
					// if(((float)high.get(i-1)-(float)close.get(i-1))*100/(float)close.get(i-1)
					// > 2 )
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) > (float)open.get(i)){
							filter=false;
						}
					}
					if(filter==true && !isOtherStrategy)
					{
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float)intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='PE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'BearAvg', " + actualGap2 + ", " + percProfitAtLowPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if(filter==true && isOtherStrategy && ((float)intradayHigh.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						
						percProfitAtHighPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'BearAvg_Other', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraOpen.get(i);
//				trigger = (float) intraFirst3MinClose.get(i);
				if(isIndexCheck){
//					indexFilter = ((float)nclose.get(i-1) - (float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc/indexGapDiv;
					indexFilter = ((float)nlow.get(i-1) - (float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc/indexGapDiv;
				}
				actualGap =((float) low.get(i - 1) - trigger) * 100 / trigger; 
				actualGap2 =((float) close.get(i - 1) - trigger) * 100 / trigger; 
				if (actualGap > gapPerc
						&& ((float) close.get(i - 1) - trigger) * 100 / trigger < gapLimitPerc && indexFilter)
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger > min && 
							trigger < max && (float) open.get(i - 1) != (float) close.get(i - 1);
					// && (float)open.get(i)>(float)close.get(i-1);
					// if(((float)open.get(i-1)==(float)low.get(i-1)))
//					if (((float) close.get(i - 1) - (float) open.get(i - 1)) * 100
//							/ (float) open.get(i - 1) > prevBodyGap && filter)
					// if(((float)close.get(i-1)-(float)low.get(i-1))*100/(float)close.get(i-1)
					// > 2)
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) < (float)open.get(i)){
							filter=false;
						}
					}
					if(filter==true && !isOtherStrategy)
					{
						trigger = ((float) intraOpen.get(i) + (float) intraOpen.get(i) * .1f / 100);
						trigger = (float)intraFirst3MinClose.get(i);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='CE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'BullAvg', " + actualGap2 + ", " + percProfitAtHighPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
						
					}
					if(filter==true  && isOtherStrategy &&  ((float)intraOpen.get(i)-(float)intradayLow.get(i))*100/(float)intradayLow.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'BullAvg_Other', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void gapWithCloseTest(java.sql.Connection con, String name, float gapPerc,
			long transaction,float min, float max, float gapLimitPerc,float highLowGap, boolean isOtherStrategy) {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List open = new ArrayList<Float>();
		List high = new ArrayList<Float>();
		List low = new ArrayList<Float>();
		List intraOpen = new ArrayList<Float>();
		List intraFirst3MinClose = new ArrayList<Float>();
		List intraFirstHrClose = new ArrayList<Float>();
		List intraFC = new ArrayList<Float>(); 
		List intraSC = new ArrayList<Float>(); 
		List intraClose = new ArrayList<Float>();
		List intraday3Min3_18_Close = new ArrayList<Float>();
		List intradayAt3_15 = new ArrayList<Float>();
		List close = new ArrayList<Float>();
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		List nopen = new ArrayList<Float>();
		List nhigh = new ArrayList<Float>();
		List nlow = new ArrayList<Float>();
		List nclose = new ArrayList<Float>();
		List intradayHigh = new ArrayList<Float>();
		List intradayLow = new ArrayList<Float>();
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			// name= name+"_FUT";
			sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, "
					+ " a.intraFirstMinClose, "+closeAtWhatTime+", a.totalTrades, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ "  and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null "
					+ " and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0 and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, n.open,a.intradayHigh, a.intradayLow, n.high, a.totalTrades, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0  and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			}
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalTrades"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intradayHigh.add(rs.getFloat("a.intradayHigh"));
				intradayLow.add(rs.getFloat("a.intradayLow"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat(closeAtWhatTime));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			float div = 2f;
			int iter = 1;
			String out = "";
			boolean indexFilter=true;
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;String optionProfit="0";
			for (int i = 5; i < tradedate.size(); i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)intraday3Min3_18_Close.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				float trigger = (float) intraOpen.get(i);
//				float trigger = (float) intraFirst3MinClose.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nopen.get(i) - (float)nclose.get(i-1))*100/(float)nclose.get(i-1) > gapPerc/indexGapDiv;
					indexFilter = ((float)nopen.get(i) - (float) nclose.get(i - 1)) * 100 / (float)nopen.get(i) > gapPerc/indexGapDiv;
				}
				float actualGap =(trigger - (float) close.get(i - 1)) * 100 / trigger; 
				if (actualGap > gapPerc
						&& actualGap < gapLimitPerc
						&& indexFilter)
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && avgQty < maxAvgTotalTrades && trigger > min && 
							trigger < max  
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					 
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) > (float)open.get(i)){
							filter=false;
						}
					}
					if (filter==true && !isOtherStrategy)
					{
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='PE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear2', " + actualGap + ", " + percProfitAtLowPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if(filter==true && isOtherStrategy && ((float)intradayHigh.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear2_Other', " + trigger + ", " + percProfitAtLowPrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
//				trigger = (float) intraFirst3MinClose.get(i);
				trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nclose.get(i-1) - (float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc/indexGapDiv;
					indexFilter = ((float) nclose.get(i - 1) - (float)nopen.get(i)) * 100 / (float)nopen.get(i) > gapPerc/indexGapDiv;
				}
				actualGap = ((float) close.get(i - 1) - trigger) * 100 / trigger;
				if (actualGap > gapPerc
						&& actualGap < gapLimitPerc
						&& indexFilter)
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && avgQty < maxAvgTotalTrades && trigger > min && 
							trigger < max  
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) < (float)open.get(i)){
							filter=false;
						}
					}
					if (filter==true && !isOtherStrategy)
					{
						trigger = ((float) intraOpen.get(i) + (float) intraOpen.get(i) * .1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='CE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull2', " + actualGap + ", " + percProfitAtHighPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if(filter==true && isOtherStrategy && ((float)intraOpen.get(i)-(float)intradayLow.get(i))*100/(float)intradayLow.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull2_Other', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void checkGap(java.sql.Connection con, String name) throws SQLException {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> intradayOpen = new ArrayList<Float>();
		
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Float> intraDayFirst3MinClose = new ArrayList<Float>();
		List<Float> intraDay3Min09_18_Close = new ArrayList<Float>();
		List<Float> intraDay3_18Close = new ArrayList<Float>();
		List<Long> totalQty = new ArrayList<Long>();
		List<Long> totalTrades = new ArrayList<Long>();
		List<Long> volume = new ArrayList<Long>();
		List nopen = new ArrayList<Float>();
		List nhigh = new ArrayList<Float>();
		List nlow = new ArrayList<Float>();
		List nclose = new ArrayList<Float>();
		float intraHigh, intraLow;
		float gap=0.8f, sl=-1f;int div=20;
		boolean isBodyCompare=true, justGapStrategy=false;
		String openOrIntra="a.open";//intradayOpen
		float total=0f;
		try{
			String sql = "select a.tradedate,a.open,a.volume,a.high,a.low,a.totalTrades,"+openOrIntra+",a.intraday3Min3_18_close,a.intradayFirst3MinClose,"
					+ "a.close,a.TotalQty from `"+name+"` as a where tradedate>'2015-01-01'"
							+ " and intradayFirst3MinClose<>0 and intraday3Min3_18_close<>0"
							+ " and intradayHigh<>0 and intradayLow<>0 and "+openOrIntra+"<>0";
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, a.TotalQty,a.volume,a.totalTrades,"
					+ "n.open,a.intradayHigh, a.intradayLow, n.high, a.totalTrades, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where (n.tradedate) >= '"+d1+"' and (n.tradedate)<='"+d2+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 "
					+ " and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0  and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			}
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat(openOrIntra));
				high.add(rs.getFloat("high"));
				low.add(rs.getFloat("low"));
				close.add(rs.getFloat("close"));
				intraDayFirst3MinClose.add(rs.getFloat("intradayFirst3MinClose"));
				intraDay3_18Close.add(rs.getFloat("intraday3Min3_18_close"));
				totalQty.add(rs.getLong("TotalQty"));
				volume.add(rs.getLong("volume"));
				totalTrades.add(rs.getLong("totalTrades"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			long sum=0;
			boolean indexFilter =false;
			for(int i=7; i< tradedate.size()-1; i++)
			{
				boolean isQtyGood = true;
				isQtyGood = totalTrades.get(i-1)>5000 && volume.get(i-1)>100000000;
				if(isIndexCheck){
					indexFilter = ((float)nclose.get(i-1) - (float)nopen.get(i))*100/(float)nopen.get(i) > gap/indexGapDiv;
					indexFilter = ((float) nclose.get(i - 1) - (float)nopen.get(i)) * 100 / (float)nopen.get(i) > gap/2;
				}
				if((close.get(i-1)-open.get(i))*100/open.get(i) > gap && isQtyGood && indexFilter)
				{
					sql="select volume from `"+name+"_3` where tradedate='"+tradedate.get(i).toString().split(" ")[0]+" 09:15:00'";
					float intraVolume=Long.parseLong(executeCountQuery(con, sql));
					float volAvg=(totalQty.get(i-1)+totalQty.get(i-2)+totalQty.get(i-3)+totalQty.get(i-4)+
							totalQty.get(i-5)+totalQty.get(i-6)+totalQty.get(i-7))/7;
					if( intraVolume> volAvg/div || justGapStrategy)
					{
						sql="select count(*) from `"+name+"_3` where low=open and tradedate='"+tradedate.get(i).toString().split(" ")[0]+" 09:15:00'";
						if(isBodyCompare){
							sql="select count(*) from `"+name+"_3` where close>open and tradedate='"+tradedate.get(i).toString().split(" ")[0]+" 09:15:00'";
						}
							
						if(Float.parseFloat(executeCountQuery(con, sql)) >0 || justGapStrategy)
						{
							total= (intraDay3_18Close.get(i)-intraDayFirst3MinClose.get(i))*100/intraDayFirst3MinClose.get(i);
							
							sql="select min(low) from `"+name+"_3` where tradedate>='"+tradedate.get(i).toString().split(" ")[0]+" 09:18:00' "
									+ " and tradedate<='"+tradedate.get(i).toString().split(" ")[0]+" 15:18:00'";
							intraLow = Float.parseFloat(executeCountQuery(con, sql));
							
							if((intraLow-intraDayFirst3MinClose.get(i))*100/intraDayFirst3MinClose.get(i)< sl
									|| total<=sl){
								total=sl;
							}
							sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + name + "', 'Bull', " + total + ", " + total + ", "
									+ total + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
						}
					}
				}
				if(isIndexCheck){
					indexFilter = ((float)nopen.get(i) - (float)nclose.get(i-1))*100/(float)nclose.get(i-1) > gap/indexGapDiv;
					indexFilter = ((float)nopen.get(i) - (float) nclose.get(i - 1)) * 100 / (float)nopen.get(i) > gap/2;
				}
				if((open.get(i)-close.get(i-1))*100/open.get(i) > gap && isQtyGood && indexFilter)
				{
					sql="select volume from `"+name+"_3` where tradedate='"+tradedate.get(i).toString().split(" ")[0]+" 09:15:00' ";
					float intraVolume=Long.parseLong(executeCountQuery(con, sql));
					float volAvg=(totalQty.get(i-1)+totalQty.get(i-2)+totalQty.get(i-3)+totalQty.get(i-4)+
							totalQty.get(i-5)+totalQty.get(i-6)+totalQty.get(i-7))/7;
					if( intraVolume> volAvg/div || justGapStrategy)
					{
						sql="select count(*) from `"+name+"_3` where high=open and tradedate='"+tradedate.get(i).toString().split(" ")[0]+" 09:15:00'";
						if(isBodyCompare){
							sql="select count(*) from `"+name+"_3` where close<open and tradedate='"+tradedate.get(i).toString().split(" ")[0]+" 09:15:00'";
						}
						if(Float.parseFloat(executeCountQuery(con, sql)) >0 || justGapStrategy)
						{
							total= (intraDayFirst3MinClose.get(i)-intraDay3_18Close.get(i))*100/intraDayFirst3MinClose.get(i);
							sql="select max(high) from `"+name+"_3` where tradedate>='"+tradedate.get(i).toString().split(" ")[0]+" 09:18:00' "
									+ " and tradedate<='"+tradedate.get(i).toString().split(" ")[0]+" 15:18:00' ";
							
							intraHigh = Float.parseFloat(executeCountQuery(con, sql));
							if((intraDayFirst3MinClose.get(i)-intraHigh)*100/intraDayFirst3MinClose.get(i)< sl
									|| total<=sl){
								total=sl;
							}
							sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + name + "', 'Bear', " + total + ", " + total + ", "
									+ total + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
						}
					}
				}
			}
			System.out.println("Total: "+total);
		} catch (Exception e) {
		}
	}
	
	public void gapWithInPrevDayRange(java.sql.Connection con, String name, float gapPerc,
			long transaction,float min, float max, float gapLimitPerc,float highLowGap, boolean isOtherStrategy) {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> intraOpen = new ArrayList<Float>();
		List intraFirst3MinClose = new ArrayList<Float>();
		List intraFirstHrClose = new ArrayList<Float>();
		List intraFC = new ArrayList<Float>(); 
		List intraSC = new ArrayList<Float>(); 
		List intraClose = new ArrayList<Float>();
		List intraday3Min3_18_Close = new ArrayList<Float>();
		List intradayAt3_15 = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		List nopen = new ArrayList<Float>();
		List nhigh = new ArrayList<Float>();
		List nlow = new ArrayList<Float>();
		List nclose = new ArrayList<Float>();
		List intradayHigh = new ArrayList<Float>();
		List intradayLow = new ArrayList<Float>();
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			// name= name+"_FUT";
			sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, "
					+ " a.intraFirstMinClose, "+closeAtWhatTime+", a.totalTrades, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where (a.tradedate) >= '"+d1+"' and (a.tradedate)<='"+d2+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 "
					+ "  and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null "
					+ " and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0 and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, n.open,a.intradayHigh, a.intradayLow, n.high, a.totalTrades, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where (n.tradedate) >= '"+d1+"' and (n.tradedate)<='"+d2+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 "
					+ " and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0  and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			}
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalTrades"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intradayHigh.add(rs.getFloat("a.intradayHigh"));
				intradayLow.add(rs.getFloat("a.intradayLow"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat(closeAtWhatTime));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			float div = 2f;
			int iter = 1;
			String out = "";
			boolean indexFilter=true;
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;String optionProfit="0";
			for (int i = 5; i < tradedate.size(); i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)intraday3Min3_18_Close.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				float trigger = (float) intraOpen.get(i);
//				float trigger = (float) intraFirst3MinClose.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nopen.get(i) - (float)nclose.get(i-1))*100/(float)nclose.get(i-1) > gapPerc/indexGapDiv;
					indexFilter = ((float)nopen.get(i) - (float) nclose.get(i - 1)) * 100 / (float)nopen.get(i) > gapPerc/indexGapDiv;
				}
				float actualGap =(trigger - (float) close.get(i - 1)) * 100 / trigger; 
				if (actualGap > gapPerc
						&& indexFilter)
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && avgQty < maxAvgTotalTrades && trigger > min && 
							trigger < max  
							;
					 
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) > (float)open.get(i)){
							filter=false;
						}
					}
					if (filter==true && !isOtherStrategy)
					{
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='PE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear2', " + actualGap + ", " + percProfitAtLowPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if(filter==true && isOtherStrategy && ((float)intradayHigh.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear2_Other', " + trigger + ", " + percProfitAtLowPrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
//				trigger = (float) intraFirst3MinClose.get(i);
				trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nclose.get(i-1) - (float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc/indexGapDiv;
					indexFilter = ((float) nclose.get(i - 1) - (float)nopen.get(i)) * 100 / (float)nopen.get(i) > gapPerc/indexGapDiv;
				}
				actualGap = ((float) close.get(i - 1) - trigger) * 100 / trigger;
				if (actualGap > gapPerc
						&& indexFilter)
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && avgQty < maxAvgTotalTrades && trigger > min && 
							trigger < max;
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) < (float)open.get(i)){
							filter=false;
						}
					}
					if (filter==true && !isOtherStrategy)
					{
						trigger = ((float) intraOpen.get(i) + (float) intraOpen.get(i) * .1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='CE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull2', " + actualGap + ", " + percProfitAtHighPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if(filter==true && isOtherStrategy && ((float)intraOpen.get(i)-(float)intradayLow.get(i))*100/(float)intradayLow.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull2_Other', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void buyAtEveryCheaperPerc(java.sql.Connection con, String name, float gapPerc,
			long transaction,float min, float max, float gapLimitPerc,float highLowGap, boolean isOtherStrategy) {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> intraOpen = new ArrayList<Float>();
		List<Float> intraFirst3MinClose = new ArrayList<Float>();
		List<Float> intraFirstHrClose = new ArrayList<Float>();
		List<Float> intraFC = new ArrayList<Float>(); 
		List<Float> intraSC = new ArrayList<Float>(); 
		List<Float> intraClose = new ArrayList<Float>();
		List<Float> intraday3Min3_18_Close = new ArrayList<Float>();
		List<Float> intradayAt3_15 = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		List<Float> nopen = new ArrayList<Float>();
		List<Float> nhigh = new ArrayList<Float>();
		List<Float> nlow = new ArrayList<Float>();
		List<Float> nclose = new ArrayList<Float>();
		List<Float> intradayHigh = new ArrayList<Float>();
		List<Float> intradayLow = new ArrayList<Float>();
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			// name= name+"_FUT";
			sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, "
					+ " a.intraFirstMinClose, "+closeAtWhatTime+", a.totalTrades, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ "  and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, n.open,a.intradayHigh, a.intradayLow, n.high, a.totalTrades, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			}
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalTrades"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intradayHigh.add(rs.getFloat("a.intradayHigh"));
				intradayLow.add(rs.getFloat("a.intradayLow"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat(closeAtWhatTime));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			float div = 2f;
			int iter = 1;
			String out = "";
			boolean indexFilter=true;
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;String optionProfit="0";
			for (int i = 5; i < tradedate.size(); i++) {
				float topHight = (intradayHigh.get(i) - intraOpen.get(i))*100/intraOpen.get(i);
				float prevBody = (open.get(i-1)-close.get(i-1))*100/open.get(i-1);
				int quotient = (int) (topHight/buyAtCheaper);
				for (int j=1; j<= quotient ; j++){
					trig =  intraOpen.get(i) + (intraOpen.get(i)*buyAtCheaper*j/100);
					float profit = (trig-intraday3Min3_18_Close.get(i))*100/trig;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bear', " + trig + ", " + profit + ", "
							+ profit + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
				
				float bottomHight = (intraOpen.get(i)-intradayLow.get(i))*100/intraOpen.get(i);
				quotient = (int) (bottomHight/buyAtCheaper);
				prevBody = (close.get(i-1)-open.get(i-1))*100/open.get(i-1);
				for (int j=1; j<= quotient; j++){
					trig =  intraOpen.get(i) - (intraOpen.get(i)*buyAtCheaper*j/100);
					float profit = (intraday3Min3_18_Close.get(i)-trig)*100/trig;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bull', " + trig + ", " + profit + ", "
							+ profit + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void gapOptionsCurrencyBuySellBoth(java.sql.Connection con, String name, float gapPerc,
			long transaction,float min, float max, float gapLimitPerc,float highLowGap, boolean isOtherStrategy) {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List<Float> copen = new ArrayList<Float>();
		List<Float> chigh = new ArrayList<Float>();
		List<Float> clow = new ArrayList<Float>();
		List<Float> intraOpen = new ArrayList<Float>();
		List intraFirst3MinClose = new ArrayList<Float>();
		List intraFirstHrClose = new ArrayList<Float>();
		List intraFC = new ArrayList<Float>(); 
		List intraSC = new ArrayList<Float>(); 
		List intraClose = new ArrayList<Float>();
		List intraday3Min3_18_Close = new ArrayList<Float>();
		List intradayAt3_15 = new ArrayList<Float>();
		List<Float> cclose = new ArrayList<Float>();
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		List<Float> popen = new ArrayList<Float>();
		List<Float> phigh = new ArrayList<Float>();
		List<Float> plow = new ArrayList<Float>();
		List<Float> pclose = new ArrayList<Float>();
		List intradayHigh = new ArrayList<Float>();
		List intradayLow = new ArrayList<Float>();
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			String d = "2018-07-27";
//			d = "2018-07-12";
			String table1="USDINRAUG69CE_3", table2="USDINRAUG69PE_3";
//			String table1="NIFTY11400CE_3", table2="NIFTY11000PE_3";
			// name= name+"_FUT";
			sql = "select C.tradedate,c.open, c.high, c.low, c.close, p.open,p.high,p.low,p.close from  "+table1+" P, "+table2+" C where c.tradedate=p.tradedate and c.tradedate>='"+d+"'";
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("c.tradedate"));
				copen.add(rs.getFloat("c.open"));
				chigh.add(rs.getFloat("c.high"));
				clow.add(rs.getFloat("c.low"));
				cclose.add(rs.getFloat("c.close"));
				
				popen.add(rs.getFloat("p.open"));
				phigh.add(rs.getFloat("p.high"));
				plow.add(rs.getFloat("p.low"));
				pclose.add(rs.getFloat("p.close"));
			}
			float trig1 = 0f, trig2=0f, prev2, prev3, prev4, width;
			float div = 2f;
			int iter = 1;
			String out = "";
			float t1=0f, t2=0f;boolean isEntryGot=false;
			boolean indexFilter=true;
			boolean isEntered=false;
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;
			int enterBefore=16;
			String entered="";
			for (int i = 5; i < tradedate.size(); i++) {
				String timePart=tradedate.get(i).toString().split(" ")[1];
				boolean time = Integer.parseInt(timePart.split(":")[0])<enterBefore && !isEntered
						&& (chigh.get(i)-clow.get(i))*100/clow.get(i)>3 && 
						(phigh.get(i)-plow.get(i))*100/plow.get(i)>3;
				if(time && (Math.abs(cclose.get(i)-copen.get(i))*100/copen.get(i) > 
				Math.abs(pclose.get(i)-popen.get(i))*100/popen.get(i)*2 || 
				Math.abs(pclose.get(i)-popen.get(i))*100/popen.get(i) > 
				Math.abs(cclose.get(i)-copen.get(i))*100/copen.get(i)*2)
				 )
//				if(tradedate.get(i).toString().contains("09:15:00"))		
				{
					isEntered=true;
					trig1 =copen.get(i+1);
					trig2 =popen.get(i+1);
					entered=tradedate.get(i).toString();
				}
//				if(tradedate.get(i).toString().contains("15:00:00") && isEntered)
				if(isEntered)
				{
					entered=tradedate.get(i).toString();
					float cprofit = (cclose.get(i+1)-trig1)*100/trig1;
					float pprofit = (pclose.get(i+1)-trig2)*100/trig2;
					float profit = cprofit+pprofit;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bull2', " + cprofit + ", " + profit + ", "
							+ pprofit + ", '" + entered + "')";
					executeSqlQuery(con, sql);
//					isEntered=false;
				}
				if(tradedate.get(i).toString().contains(""+enterBefore+":00:00")){
					isEntered=false;
				}
			}
			
			/*for (int i = 5; i < tradedate.size(); i++) {
				String timePart=tradedate.get(i).toString().split(" ")[1];
				if(tradedate.get(i).toString().contains("09:15:00"))		
				{
					isEntered=true;
					trig1 =copen.get(i);
					trig2 =popen.get(i);
					entered=tradedate.get(i).toString();
				}
				if(isEntered)
				{
					
					float cprofit = (cclose.get(i)-trig1)*100/trig1;
					float pprofit = (pclose.get(i)-trig2)*100/trig2;
					float profit = cprofit+pprofit;
					if(Math.abs(profit) > 10 && !isEntryGot && (cprofit<0 && pprofit>0)){
						t1 =  copen.get(i+1);t2 =  popen.get(i+1);
						isEntryGot=true;
						entered=tradedate.get(i).toString();
					}
					if(isEntryGot){
						cprofit = (cclose.get(i+1)-t1)*100/t1;
						pprofit = (pclose.get(i+1)-t2)*100/t2;
						profit = cprofit+pprofit;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull2', " + cprofit + ", " + profit + ", "
								+ pprofit + ", '" + entered + "')";
						executeSqlQuery(con, sql);
					}
				}
				if(tradedate.get(i).toString().contains("15:27:00")){
					isEntered=false;
					isEntryGot=false;
				}
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public float getValue(float value, float aboveWhat){
		int val=((int)(value*aboveWhat/100)*10000);
		if(val<25){
			return 25f/10000;
		}else if(val>25 && val<50){
			return  50f/10000;
		}else if(val>50 && val<75){
			return 75f/10000;
		}else if(val>75 && val<100){
			return 100f/10000;
		}
		return 0;
	}
	
	public void gapCurrencyRangeCross(java.sql.Connection con, String name, float gapPerc,
			long transaction,float min, float max, float gapLimitPerc,float highLowGap, boolean isOtherStrategy) {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Float> popen = new ArrayList<Float>();
		List<Float> phigh = new ArrayList<Float>();
		List<Float> plow = new ArrayList<Float>();
		List<Float> pclose = new ArrayList<Float>();
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		/*List<String> optionsName = Arrays.asList("USDINRAUG69CE_3","USDINRAUG69PE_3","USDINRAUG70CE_3",
				"USDINRAUG70PE_3","USDINRAUG68CE_3", "USDINRAUG68PE_3","USDINRAUG68_5CE_3","USDINRAUG68_5PE_3");*/
		List<String> optionsName = Arrays.asList("CRUDEOIL_SEP_FUT_60");//EURINRAUGFUT_30,USDINRAUGFUT_30 
		for(String optName: optionsName)
		{
			List<String> allDates = new ArrayList<>();
			int avgVolume = 3000;
			try {
				allDates.clear();
				String d = "2018-05-01";
				String table1=optName;
				rs = executeSelectSqlQuery(con, "select date(tradedate) as d from "+table1+" group by date(tradedate) having avg(volume)>"+avgVolume);
				while(rs.next()){
					allDates.add(rs.getString("d"));
				}
//				float aboveWhat=1f, bodyWidth=5;
				
				float aboveWhat=0.005f, bodyWidth=0.01f, noOfLots=10f, targetTicks=2f, lotSize=100f;
				for(String dat: allDates){
					tradedate.clear();open.clear();high.clear();low.clear();close.clear();
					popen.clear();phigh.clear();plow.clear();pclose.clear();
					sql = "select C.tradedate,c.open, c.high, c.low, c.close from  "+table1+" c "
							+ " where date(c.tradedate)='"+dat+"'";
					rs = executeSelectSqlQuery(con, sql);
					while (rs.next()) {
						tradedate.add(rs.getString("c.tradedate"));
						open.add(rs.getFloat("c.open"));
						high.add(rs.getFloat("c.high"));
						low.add(rs.getFloat("c.low"));
						close.add(rs.getFloat("c.close"));
					}
					for(int i=0; i< tradedate.size()-1; i++){
						float width=(close.get(i) - open.get(i))*100/open.get(i);
						float prevHight = high.get(i)+getValue(high.get(i), aboveWhat);
						prevHight= high.get(i)+1;
						if(high.get(i+1) >= prevHight && open.get(i+1)<prevHight)
						{
							float profit = (high.get(i+1)-prevHight)*lotSize*noOfLots;
							float profitClose = (close.get(i+1)-prevHight)*lotSize*noOfLots;
							float p=((close.get(i+1)-0.0000f)-prevHight)*lotSize*noOfLots;
							if((high.get(i+1)-prevHight) >= targetTicks && isSL){
								p=((prevHight+targetTicks)-prevHight)*lotSize*noOfLots;
							}
							p = p-Math.abs(60*noOfLots);
							sql = "insert into psarresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + table1 + "', 'Bull', " + p + ", " + profit + ", "
									+ profitClose + ", '" + tradedate.get(i+1) + "')";
							executeSqlQuery(con, sql);
						}
						width=(open.get(i) - close.get(i))*100/close.get(i);
						prevHight = low.get(i)-getValue(low.get(i), aboveWhat);
						prevHight= low.get(i)-1;
						if(low.get(i+1) <= prevHight && open.get(i+1)>prevHight)
						{
							float profit = (prevHight-low.get(i+1))*lotSize*noOfLots;
							float profitClose = (prevHight-close.get(i+1))*lotSize*noOfLots;
							float p=(prevHight-(close.get(i+1)+0.0000f))*lotSize*noOfLots;
							if(prevHight-low.get(i+1) >= targetTicks && isSL){
								p=(prevHight-(prevHight-targetTicks))*lotSize*noOfLots;
							}
							p = p-Math.abs(60*noOfLots);
							sql = "insert into psarresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + table1 + "', 'Bear', " + p + ", " + profit + ", "
									+ profitClose + ", '" + tradedate.get(i+1) + "')";
							executeSqlQuery(con, sql);
						}
					}
					/*boolean filter=false;float wid=0.02f;
					for(int i=0; i< tradedate.size()-1; i++){
						if((high.get(i)- open.get(i)) > wid){
							float profit = (close.get(i)-(open.get(i)+wid))*1000*lotSize;
							profit = profit-Math.abs(profit*9/100);
//							profit = (close.get(i)-(open.get(i)+wid))*100/close.get(i);
							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + table1 + "', 'Bull', " + profit + ", " + (open.get(i)+wid) + ", "
									+ close.get(i) + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
						}
						if((open.get(i)- low.get(i)) > wid){
							float profit = ((open.get(i)-wid)-close.get(i))*1000*lotSize;
							profit = profit-Math.abs(profit*9/100);
//							profit =((open.get(i)-wid)-close.get(i))*100/close.get(i);
							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + table1 + "', 'Bear', " + profit + ", " + (open.get(i)-wid) + ", "
									+ close.get(i) + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
						}
					}*/
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void getOpenLowSameOrOpenHighSame(java.sql.Connection con, String name, float gapPerc, float prevBodyGap,
			long transaction, float highLowGap) {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List open = new ArrayList<Float>();
		List high = new ArrayList<Float>();
		List low = new ArrayList<Float>();
		List intraOpen = new ArrayList<Float>();
		List intraFirst3MinClose = new ArrayList<Float>();
		List intraFirstHrClose = new ArrayList<Float>();
		List intraFC = new ArrayList<Float>(); 
		List intraSC = new ArrayList<Float>(); 
		List intraClose = new ArrayList<Float>();
		List intraday3Min3_18_Close = new ArrayList<Float>();
		List intradayAt3_15 = new ArrayList<Float>();
		List close = new ArrayList<Float>();
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			// name= name+"_FUT";
			sql = "select a.volume," + "a.open, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and a.intraday3Min3_18_Close is not null and a.intradayClose is not null and a.intradayOpen is not null ";
			
//			sql = "select a.volume," + "a.open, a.high,a.intradayOpen, a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
//					+ "` as a where a.tradedate >= '2018-01-01'";
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalTrades"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat("a.intraday3Min3_18_Close"));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			float gapLimitPerc = 10f, div = 2f;
			int iter = 1;
			String out = "";
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;
			for (int i = 5; i < tradedate.size(); i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)intraClose.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				float trigger = (float) intraOpen.get(i);
				boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger > 10 && 
						trigger < 5000 ;
				float tri = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
//				tri = ((float)high.get(i-1) - (float)low.get(i-1))*3 + (float)intraOpen.get(i);
				if(filter==true && (float)high.get(i) > tri )
				{
					trigger = tri;
					percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', '"+closeAtWhatTime+"', " + trigger + ", " + percProfitAtLowPrice + ", "
							+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
				tri = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
//				tri =  (float)intraOpen.get(i) - ((float)high.get(i-1) - (float)low.get(i-1))*3;
				if(filter==true && (float)low.get(i) < tri )
				{
					trigger = tri;
					percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', '"+closeAtWhatTime+"', " + trigger + ", " + percProfitAtHighPrice + ", "
							+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	public void getOnPrevCloseCompare(java.sql.Connection con, String name, float gapPerc, float prevBodyGap,
			long transaction,float min, float max, float gapLimitPerc, float highLowGap,boolean isOtherStrategy) {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List open = new ArrayList<Float>();
		List high = new ArrayList<Float>();
		List low = new ArrayList<Float>();
		List intraOpen = new ArrayList<Float>();
		List intraFirst3MinClose = new ArrayList<Float>();
		List intraFirstHrClose = new ArrayList<Float>();
		List intraFC = new ArrayList<Float>(); 
		List intraSC = new ArrayList<Float>(); 
		List intraClose = new ArrayList<Float>();
		List intraday3Min3_18_Close = new ArrayList<Float>();
		List intradayAt3_15 = new ArrayList<Float>();
		List close = new ArrayList<Float>();
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		List nopen = new ArrayList<Float>();
		List nhigh = new ArrayList<Float>();
		List nlow = new ArrayList<Float>();
		List nclose = new ArrayList<Float>();
		List intradayHigh = new ArrayList<Float>();
		List intradayLow = new ArrayList<Float>();
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			// name= name+"_FUT";
			sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, "
					+ " a.intraFirstMinClose, "+closeAtWhatTime+", a.totalTrades, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0 and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, n.open,a.intradayHigh, a.intradayLow, n.high, a.totalTrades, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0 and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			}
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalTrades"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intradayHigh.add(rs.getFloat("a.intradayHigh"));
				intradayLow.add(rs.getFloat("a.intradayLow"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat(closeAtWhatTime));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			float div = 2f;
			int iter = 1;
			String out = "";
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;String optionProfit="0";
			for (int i = 5; i < tradedate.size(); i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)intraday3Min3_18_Close.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				float trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nopen.get(i) - (float)nclose.get(i-1))*100/(float)nclose.get(i-1) > gapPerc/indexGapDiv;
				}
				float actualGap =(trigger - (float) close.get(i - 1)) * 100 / trigger; 
				if (actualGap > gapPerc
						&& actualGap < gapLimitPerc && indexFilter) 
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger > min && 
							trigger < max && (float) open.get(i - 1) != (float) close.get(i - 1);
					// && (float)open.get(i)>(float)close.get(i-1) ;
					// if(((float)open.get(i-1)==(float)high.get(i-1)))
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) > (float)open.get(i)){
							filter=false;
						}
					}
					if (((float) open.get(i - 1) - (float) close.get(i - 1)) * 100
							/ (float) close.get(i - 1) > prevBodyGap && filter && !isOtherStrategy)
					// if(((float)high.get(i-1)-(float)close.get(i-1))*100/(float)close.get(i-1)
					// > 2 )
					{
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='PE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear3', " + actualGap + ", " + percProfitAtLowPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if (((float) open.get(i - 1) - (float) close.get(i - 1)) * 100
							/ (float) open.get(i - 1) > prevBodyGap && filter
							&& isOtherStrategy && ((float)intradayHigh.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear3_Other', " + trigger + ", " + percProfitAtLowPrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nclose.get(i-1) - (float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc/indexGapDiv;
				}
				actualGap =((float) close.get(i - 1) - trigger) * 100 / trigger; 
				if ( actualGap > gapPerc
						&& actualGap < gapLimitPerc && indexFilter) 
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger > min && 
							trigger < max && (float) open.get(i - 1) != (float) close.get(i - 1);
					// && (float)open.get(i)>(float)close.get(i-1);
					// if(((float)open.get(i-1)==(float)low.get(i-1)))
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) < (float)open.get(i)){
							filter=false;
						}
					}
					if (((float) close.get(i - 1) - (float) open.get(i - 1)) * 100
							/ (float) open.get(i - 1) > prevBodyGap && filter && !isOtherStrategy)
					// if(((float)close.get(i-1)-(float)low.get(i-1))*100/(float)close.get(i-1)
					// > 2)
					{
						trigger = ((float) intraOpen.get(i) + (float) intraOpen.get(i) * .1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='CE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull3', " + actualGap + ", " + percProfitAtHighPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if (((float) close.get(i - 1) - (float) open.get(i - 1)) * 100
							/ (float) open.get(i - 1) > prevBodyGap && filter
							&& isOtherStrategy && ((float)intraOpen.get(i)-(float)intradayLow.get(i))*100/(float)intradayLow.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull3_Other', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getOnPrevBody_TodayOpenBelowAboveBody(java.sql.Connection con, String name, float gapPerc, float prevBodyGap,
			long transaction,float min, float max, float gapLimitPerc, float highLowGap,boolean isOtherStrategy) {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> intraOpen = new ArrayList<Float>();
		List<Float> intraFirst3MinClose = new ArrayList<Float>();
		List<Float> intraFirstHrClose = new ArrayList<Float>();
		List<Float> intraFC = new ArrayList<Float>(); 
		List<Float> intraSC = new ArrayList<Float>(); 
		List<Float> intraClose = new ArrayList<Float>();
		List<Float> intraday3Min3_18_Close = new ArrayList<Float>();
		List<Float> intradayAt3_15 = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		List<Float> nopen = new ArrayList<Float>();
		List<Float> nhigh = new ArrayList<Float>();
		List<Float> nlow = new ArrayList<Float>();
		List<Float> nclose = new ArrayList<Float>();
		List<Float> intradayHigh = new ArrayList<Float>();
		List<Float> intradayLow = new ArrayList<Float>();
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			// name= name+"_FUT";
			sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, "
					+ " a.intraFirstMinClose, "+closeAtWhatTime+", a.totalTrades, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0 and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, n.open,a.intradayHigh, a.intradayLow, n.high, a.totalTrades, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0 and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			}
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalTrades"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intradayHigh.add(rs.getFloat("a.intradayHigh"));
				intradayLow.add(rs.getFloat("a.intradayLow"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat(closeAtWhatTime));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			float div = 2f;
			int iter = 1;
			String out = "";
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;String optionProfit="0";
			for (int i = 5; i < tradedate.size(); i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)intraday3Min3_18_Close.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				float trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nopen.get(i) - (float)nclose.get(i-1))*100/(float)nclose.get(i-1) > gapPerc/indexGapDiv;
				}
				float actualGap =(trigger - close.get(i - 1)) * 100 / trigger; 
				float prevBody =((open.get(i-1)) -  close.get(i - 1)) * 100 /  open.get(i - 1); 
				if (prevBody > gapPerc
						&& actualGap < gapLimitPerc && indexFilter) 
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger > min && 
							trigger < max && (float) open.get(i - 1) != (float) close.get(i - 1);
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) > (float)open.get(i)){
							filter=false;
						}
					}
					if (open.get(i) > open.get(i-1) && filter && !isOtherStrategy)
					{
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='PE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear7', " + actualGap + ", " + percProfitAtLowPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nclose.get(i-1) - (float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc/indexGapDiv;
				}
				actualGap =((float) close.get(i - 1) - trigger) * 100 / trigger; 
				prevBody =((close.get(i-1)) -  open.get(i - 1)) * 100 /  open.get(i - 1); 
				if (prevBody > gapPerc
						&& actualGap < gapLimitPerc && indexFilter) 
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger > min && 
							trigger < max && (float) open.get(i - 1) != (float) close.get(i - 1);
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) < (float)open.get(i)){
							filter=false;
						}
					}
					if (open.get(i) < open.get(i-1) && filter && !isOtherStrategy)
					{
						trigger = ((float) intraOpen.get(i) + (float) intraOpen.get(i) * .1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='CE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull7', " + actualGap + ", " + percProfitAtHighPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getData(java.sql.Connection con, String name, float gapPerc,long transaction,
			float min, float max, float gapLimitPerc, float highLowGap,boolean isOtherStrategy){
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List open = new ArrayList<Float>();
		List high = new ArrayList<Float>();
		List low = new ArrayList<Float>();
		List intraOpen = new ArrayList<Float>();
		List intraFirst3MinClose = new ArrayList<Float>();
		List intraFirstHrClose = new ArrayList<Float>();
		List intraFC = new ArrayList<Float>(); 
		List intraSC = new ArrayList<Float>(); 
		List intraClose = new ArrayList<Float>();
		List intraday3Min3_18_Close = new ArrayList<Float>();
		List intradayAt3_15 = new ArrayList<Float>();
		List close = new ArrayList<Float>();
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		List nopen = new ArrayList<Float>();
		List nhigh = new ArrayList<Float>();
		List nlow = new ArrayList<Float>();
		List nclose = new ArrayList<Float>();
		List intradayHigh = new ArrayList<Float>();
		List intradayLow = new ArrayList<Float>();
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, "
					+ " a.intraFirstMinClose, "+closeAtWhatTime+", a.totalTrades, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ "  and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0 and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, n.open,a.intradayHigh, a.intradayLow, n.high, a.totalTrades, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0 and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			}
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalTrades"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intradayHigh.add(rs.getFloat("a.intradayHigh"));
				intradayLow.add(rs.getFloat("a.intradayLow"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat(closeAtWhatTime));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			
			int iter = 1;
			String out = "";
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;String optionProfit="0";
			for (int i = 5; i < tradedate.size(); i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)intraday3Min3_18_Close.get(i);
				
				float trigger = (float) intraOpen.get(i);
//				float trigger = (float) intraFirst3MinClose.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				if(isIndexCheck){
					indexFilter = ((float)nopen.get(i) - (float)nclose.get(i-1))*100/(float)nclose.get(i-1) > gapPerc/indexGapDiv;
					float fib = (float) nhigh.get(i - 1) - ((float) nhigh.get(i - 1) - (float) nlow.get(i - 1)) / 2;
					indexFilter = (float)nopen.get(i)>fib;
				}
				float actualGap=(trigger - (float) close.get(i - 1)) * 100 / trigger;
				if (actualGap > gapPerc
						&& actualGap < gapLimitPerc && indexFilter) {
					
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) > (float)open.get(i)){
							filter=false;
						}
					}
					float fib = (float) high.get(i - 1) - ((float) high.get(i - 1) - (float) low.get(i - 1)) / 2;
					if (trigger > fib && filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='PE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear4', " + actualGap + ", " + percProfitAtLowPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if ((float) intraOpen.get(i) > fib && isOtherStrategy && filter && ((float)intradayHigh.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap) {
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear4_Other', " + trigger + ", " + percProfitAtLowPrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nclose.get(i-1) - (float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc/indexGapDiv;
					float fib = (float) nlow.get(i - 1) + ((float) nhigh.get(i - 1) - (float) nlow.get(i - 1)) / 2;
					indexFilter = (float)nopen.get(i)<fib;
				}
				actualGap = ((float) close.get(i - 1) - trigger) * 100 / trigger;
				if (actualGap > gapPerc
						&& actualGap < gapLimitPerc && indexFilter) {
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) < (float)open.get(i)){
							filter=false;
						}
					}
					float fib = (float) low.get(i - 1) + ((float) high.get(i - 1) - (float) low.get(i - 1)) / 2;
					if (trigger < fib && filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='CE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull4', " + actualGap + ", " + percProfitAtHighPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if ((float) intraOpen.get(i) < fib && isOtherStrategy && filter
							&& ((float)intraOpen.get(i)-(float)intradayLow.get(i))*100/(float)intradayLow.get(i) > highLowGap) {
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull4_Other', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void gapCompareToPrevLowHighReverse(java.sql.Connection con, String name, float gapPerc,long transaction,
			float min, float max, float gapLimitPerc, float highLowGap,boolean isOtherStrategy){
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> intraOpen = new ArrayList<Float>();
		List<Float> intraFirst3MinClose = new ArrayList<Float>();
		List<Float> intraFirstHrClose = new ArrayList<Float>();
		List<Float> intraFC = new ArrayList<Float>(); 
		List<Float> intraSC = new ArrayList<Float>(); 
		List<Float> intraClose = new ArrayList<Float>();
		List<Float> intraday3Min3_18_Close = new ArrayList<Float>();
		List<Float> intradayAt3_15 = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		List<Float> nopen = new ArrayList<Float>();
		List<Float> nhigh = new ArrayList<Float>();
		List<Float> nlow = new ArrayList<Float>();
		List<Float> nclose = new ArrayList<Float>();
		List<Float> intradayHigh = new ArrayList<Float>();
		List<Float> intradayLow = new ArrayList<Float>();
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, "
					+ " a.intraFirstMinClose, "+closeAtWhatTime+", a.totalTrades, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 "
					+ "  and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0 and a.intradayHigh<>0 and a.intradayLow<>0 and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0  and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, n.open,a.intradayHigh, a.intradayLow, n.high, a.totalTrades, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 "
					+ " and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0 and a.intradayHigh<>0 and a.intradayLow<>0 and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0  and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			}
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalTrades"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intradayHigh.add(rs.getFloat("a.intradayHigh"));
				intradayLow.add(rs.getFloat("a.intradayLow"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat(closeAtWhatTime));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			
			int iter = 1;
			String out = "";
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;String optionProfit="0";
			for(int k=start; k<= end; k++)
			{
				daysToAvg = k;
			
			for (int i = 20; i < tradedate.size(); i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)intraday3Min3_18_Close.get(i);
				
				float trigger = (float) intraOpen.get(i);
//				float trigger = (float) intraFirst3MinClose.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				if(isIndexCheck){
					indexFilter = ((float)nopen.get(i) - (float)nclose.get(i-1))*100/(float)nclose.get(i-1) > indexPerc;
				}
				float temp=0f;
				for(int j=1; j<= daysToAvg; j++){
					if(isOpen){
						temp+=open.get(i-j);
					}else{
						temp+=close.get(i-j);
					}
				}
				float avgClose = temp/daysToAvg;
				float g=(trigger - avgClose) * 100 / trigger;
				float g2=(trigger - close.get(i-1)) * 100 / trigger;
//				float g=(trigger - (float) close.get(i - 1)) * 100 / trigger;
				if ( g > gapPerc && g < gapLimitPerc && indexFilter) {
					
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
//					filter = filter && open.get(i-1) > close.get(i-1) && open.get(i) > open.get(i-1);
					if (filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) {
							percProfitAtLowPrice = sl*-1;
						}
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='PE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							if(StringUtils.isNumeric(optionProfit)){
								optionProfit = String.valueOf(Math.floor(Double.parseDouble(optionProfit)));
							}else continue;
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear5_Avg', '" + g2 + "', " + percProfitAtLowPrice + ", "
								+ g2 + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if (filter && isOtherStrategy && ((float)intradayHigh.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear5_Avg_Other', " + trigger + ", " + percProfitAtLowPrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nclose.get(i-1) - (float)nopen.get(i))*100/(float)nopen.get(i) > indexPerc;
				}
//				g = ((float) close.get(i - 1) - trigger) * 100 / trigger;
				g = (avgClose - trigger) * 100 / trigger;
				g2=(close.get(i-1)-trigger) * 100 / trigger;
				if (g > gapPerc && g < gapLimitPerc && indexFilter) {
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
//					filter = filter && open.get(i-1) < close.get(i-1) && open.get(i) < open.get(i-1);
					if (filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='CE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
							if(StringUtils.isNumeric(optionProfit)){
								optionProfit = String.valueOf(Math.floor(Double.parseDouble(optionProfit)));
							}else continue;
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull5_Avg', '" + g2 + "', " + percProfitAtHighPrice + ", "
								+ g2 + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if (filter && isOtherStrategy && ((float)intraOpen.get(i)-(float)intradayLow.get(i))*100/(float)intradayLow.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull5_Avg_Other', " + g + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void checkCamarilla(java.sql.Connection con, String name, float gapPerc,long transaction,
			float min, float max, float gapLimitPerc, float highLowGap,boolean isOtherStrategy){
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		
		List<Float> h1 = new ArrayList<Float>();
		List<Float> h2 = new ArrayList<Float>();
		List<Float> h3 = new ArrayList<Float>();
		List<Float> h4 = new ArrayList<Float>();
		List<Float> h5 = new ArrayList<Float>();
		List<Float> l1 = new ArrayList<Float>();
		List<Float> l2 = new ArrayList<Float>();
		List<Float> l3 = new ArrayList<Float>();
		List<Float> l4 = new ArrayList<Float>();
		List<Float> l5 = new ArrayList<Float>();
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			name="NIFTY_50";
			
			sql = " select * from "+name+" a";
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				h1.add(rs.getFloat("a.camarilla_h1"));
				h2.add(rs.getFloat("a.camarilla_h2"));
				h3.add(rs.getFloat("a.camarilla_h3"));
				h4.add(rs.getFloat("a.camarilla_h4"));
				h5.add(rs.getFloat("a.camarilla_h5"));
				
				l1.add(rs.getFloat("a.camarilla_l1"));
				l2.add(rs.getFloat("a.camarilla_l2"));
				l3.add(rs.getFloat("a.camarilla_l3"));
				l4.add(rs.getFloat("a.camarilla_l4"));
				l5.add(rs.getFloat("a.camarilla_l5"));
			}
			for(int i=0; i<tradedate.size(); i++){
				if(open.get(i) > h3.get(i) && open.get(i) < h4.get(i)
						&& high.get(i) > h4.get(i)){
					float profit = (close.get(i)-h4.get(i))*100/h4.get(i);
					float profitAtHigh = (high.get(i)-h4.get(i))*100/h4.get(i);
					sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bull', " + profitAtHigh + ", " + profit + ", "
							+ profitAtHigh + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
				if(open.get(i) < l3.get(i) && open.get(i) > l4.get(i)
						&& low.get(i) < l4.get(i)){
					float profit = (l4.get(i)-close.get(i))*100/close.get(i);
					float profitAtHigh = (l4.get(i)-low.get(i))*100/low.get(i);
					sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bear', " + profitAtHigh + ", " + profit + ", "
							+ profitAtHigh + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
			}
			System.exit(1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void gapCompareAvgPrevCloseDaily(java.sql.Connection con, String name, float gapPerc,long transaction,
			float min, float max, float gapLimitPerc, float highLowGap,boolean isOtherStrategy){
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		List<Float> nopen = new ArrayList<Float>();
		List<Float> nhigh = new ArrayList<Float>();
		List<Float> nlow = new ArrayList<Float>();
		List<Float> nclose = new ArrayList<Float>();
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, "
					+ " a.intraFirstMinClose, "+closeAtWhatTime+", a.totalTrades, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' "+strategyFilter+"";
			
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, n.open,a.intradayHigh, a.intradayLow, n.high, a.totalTrades, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where n.tradedate >= '"+d+"' "+strategyFilter+" and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' ";
			}
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalTrades"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			int iter = 1;
			String out = "";
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;String optionProfit="0";
			for(int k=start; k<end ; k++)
			{
				daysToAvg = k;
			
			for (int i = end; i < tradedate.size(); i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)close.get(i);
				
				float trigger = (float) open.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				if(isIndexCheck){
					indexFilter = ((float)nopen.get(i) - (float)nclose.get(i-1))*100/(float)nclose.get(i-1) > 0.2f;
				}
				float temp=0f;
				for(int j=1; j<= daysToAvg; j++){
					if(isOpen){
						temp+=open.get(i-j);
					}else{
						temp+=close.get(i-j);
					}
				}
				float avgClose = temp/daysToAvg;
				float g=(trigger - avgClose) * 100 / trigger;
//				float g=(trigger - (float) close.get(i - 1)) * 100 / trigger;
				if ( g > gapPerc && g < gapLimitPerc && indexFilter) {
					
					boolean filter = avgVol > transaction && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					if (filter && !isOtherStrategy) {
						trigger = (float) open.get(i);
						percProfitAtLowPrice = (trigger - (float) close.get(i)) * 100 / (float) trigger;
						stopLoss = ((float) high.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) {
							percProfitAtLowPrice = sl*-1;
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear4', '" + optionProfit + "', " + percProfitAtLowPrice + ", "
								+ g + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) open.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nclose.get(i-1) - (float)nopen.get(i))*100/(float)nopen.get(i) > 0.2f;
				}
//				g = ((float) close.get(i - 1) - trigger) * 100 / trigger;
				g = (avgClose - trigger) * 100 / trigger;
				if (g > gapPerc && g < gapLimitPerc && indexFilter) {
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					if (filter && !isOtherStrategy) {
						trigger = (float) open.get(i);
						percProfitAtHighPrice = ((float) close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) low.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull4', '" + optionProfit + "', " + percProfitAtHighPrice + ", "
								+ g + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void gapUpDownNiftyReverse(java.sql.Connection con, String name, float gapPerc,long transaction,
			float min, float max, float gapLimitPerc, float highLowGap,boolean isOtherStrategy){
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			sql = "select a.open, a.high, a.low, a.close, a.tradedate from NIFTY_50 a where a.tradedate >= '"+d+"'";
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			int iter = 1;
			String out = "";
			for(int i=1; i< tradedate.size(); i++){
				float gap = (open.get(i)-close.get(i-1))*100/close.get(i-1);
				if( gap > gapPerc){
					float profit = (open.get(i) - close.get(i))*100/close.get(i);
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bear_Nifty', '" + profit + "', " + profit + ", "
							+ gap + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
				gap = (close.get(i-1)-open.get(i))*100/open.get(i);
				if( gap > gapPerc){
					float profit = (close.get(i) - open.get(i))*100/open.get(i);
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bull_Nifty', '" + profit + "', " + profit + ", "
							+ gap + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void crossingPrevCandleFully(java.sql.Connection con, String name, float gapPerc,long transaction,
			float min, float max, float gapLimitPerc, float highLowGap,boolean isOtherStrategy){
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> intraOpen = new ArrayList<Float>();
		List<Float> intraFirst3MinClose = new ArrayList<Float>();
		List<Float> intraFirstHrClose = new ArrayList<Float>();
		List<Float> intraFC = new ArrayList<Float>(); 
		List<Float> intraSC = new ArrayList<Float>(); 
		List<Float> intraClose = new ArrayList<Float>();
		List<Float> intraday3Min3_18_Close = new ArrayList<Float>();
		List<Float> intradayAt3_15 = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Long> totalVol = new ArrayList<Long>();
		List<Long> intraday3Min09_15_Volume = new ArrayList<Long>();
		List<Long> totalQty = new ArrayList<Long>();
		List<Float> nopen = new ArrayList<Float>();
		List<Float> nhigh = new ArrayList<Float>();
		List<Float> nlow = new ArrayList<Float>();
		List<Float> nclose = new ArrayList<Float>();
		List<Float> intradayHigh = new ArrayList<Float>();
		List<Float> intradayLow = new ArrayList<Float>();
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			sql = "select a.volume," + "a.open, a.intraday3Min09_15_Volume, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, "
					+ " a.intraFirstMinClose, "+closeAtWhatTime+", a.totalTrades, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 "
					+ "  and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null"
					+ "";
			
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, a.intraday3Min09_15_Volume, n.open,a.intradayHigh, a.intradayLow, n.high, a.totalTrades, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			}
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalTrades"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intradayHigh.add(rs.getFloat("a.intradayHigh"));
				intradayLow.add(rs.getFloat("a.intradayLow"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat(closeAtWhatTime));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
				intraday3Min09_15_Volume.add(rs.getLong("a.intraday3Min09_15_Volume"));
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			
			int iter = 1;
			String out = "";
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;String optionProfit="0";
			for (int i = 5; i < tradedate.size()-1; i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)intraday3Min3_18_Close.get(i);
				
				float trigger = (float) intraOpen.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				if(isIndexCheck){
					indexFilter = ((float)nopen.get(i) - (float)nclose.get(i-1))*100/(float)nclose.get(i-1) > 0.3f;
				}
				if (Math.abs(trigger-close.get(i-1))*100/trigger < 0.1 && indexFilter && 
						intraday3Min09_15_Volume.get(i) > totalVol.get(i-1)/2 && intraFirst3MinClose.get(i)<intraOpen.get(i)) {
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
							;
					if (filter && !isOtherStrategy) {
						trigger = (float) intraday3Min3_18_Close.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i+1)) * 100 / (float) trigger;
						stopLoss = ((float) intradayHigh.get(i+1)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='PE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear4', " + trigger + ", " + percProfitAtLowPrice + ", "
								+ optionProfit + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nclose.get(i-1) - (float)nopen.get(i))*100/(float)nopen.get(i) > 0.3f;
				}
				if (Math.abs(trigger-close.get(i-1))*100/trigger < 0.1 && indexFilter && 
						intraday3Min09_15_Volume.get(i) > totalVol.get(i-1)/2 && intraFirst3MinClose.get(i)>intraOpen.get(i))
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min;
					if (filter && !isOtherStrategy) {
						trigger = (float) intraday3Min3_18_Close.get(i);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i+1) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i+1)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='CE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull4', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ optionProfit + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getChoppyCandle(java.sql.Connection con, String name, float gapPerc,long transaction,
			float min, float max, float gapLimitPerc, float highLowGap,boolean isOtherStrategy){
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		
		List<Float> close = new ArrayList<Float>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			name = name+"_60";
			
			sql = "select a.open, a.high, a.low, a.close,a.tradedate from "+name+" a where a.tradedate >= '"+d+"' order by a.tradedate";
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			
			int iter = 1;
			String out = "";
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7, target=2f, gapPer=0.5f;
			boolean filter=false;float wid=0.5f;
			for(int i=0; i< tradedate.size()-1; i++){
				if((high.get(i)- open.get(i))*100/open.get(i) > wid){
					trig = open.get(i) + (open.get(i)*wid)/100;
					float profit = (close.get(i)-trig)*100/trig;
//					profit = (close.get(i)-(open.get(i)+wid))*100/close.get(i);
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bull', " + profit + ", " + profit + ", "
							+ profit + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
				if((open.get(i)- low.get(i))*100/open.get(i) > wid){
					trig = open.get(i) - (open.get(i)*wid)/100;
					float profit = (trig-close.get(i))*100/trig;
//					profit =((open.get(i)-wid)-close.get(i))*100/close.get(i);
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bear', " + profit + ", " + profit + ", "
							+ profit + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getNiftyTest(java.sql.Connection con, String name, float gapPerc,float prevDayBody,long transaction,
			float min, float max, float highLowGap){
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List open = new ArrayList<Float>();
		List high = new ArrayList<Float>();
		List low = new ArrayList<Float>();
		List intraOpen = new ArrayList<Float>();
		List intraFirst3MinClose = new ArrayList<Float>();
		List intraFirstHrClose = new ArrayList<Float>();
		List intraFC = new ArrayList<Float>(); 
		List intraSC = new ArrayList<Float>(); 
		List intraClose = new ArrayList<Float>();
		List intraday3Min3_18_Close = new ArrayList<Float>();
		List intradayAt3_15 = new ArrayList<Float>();
		List close = new ArrayList<Float>();
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		List nopen = new ArrayList<Float>();
		List nhigh = new ArrayList<Float>();
		List nlow = new ArrayList<Float>();
		List nclose = new ArrayList<Float>();
		List intradayHigh = new ArrayList<Float>();
		List intradayLow = new ArrayList<Float>();
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.totalTrades,a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0 and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null ";
			
//			if(isIndexCheck)
			{
				sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, n.open, n.high, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.totalTrades,a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ "  and "+closeAtWhatTime+" is not null and "+closeAtWhatTime+" <>0 and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
			}
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalTrades"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intradayHigh.add(rs.getFloat("a.intradayHigh"));
				intradayLow.add(rs.getFloat("a.intradayLow"));
				intraday3Min3_18_Close.add(rs.getFloat(closeAtWhatTime));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
//				if(isIndexCheck)
				{
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			
			int iter = 1;
			String out = "";
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;String optionProfit="0";
			for (int i = 5; i < tradedate.size(); i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)intraday3Min3_18_Close.get(i);
				float trigger = (float) intraOpen.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				if(isIndexCheck){
					indexFilter = (float)intraOpen.get(i) > (float)high.get(i-1);
				}
				float actualGap=(trigger - (float) close.get(i - 1)) * 100 / trigger;
				if (((float)nopen.get(i)-(float)nclose.get(i-1))*100/(float)nopen.get(i) > gapPerc) {
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
							&& indexFilter ;//&& ((float)nopen.get(i-1)-(float)nclose.get(i-1))*100/(float)nopen.get(i-1) > prevDayBody;
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) > (float)open.get(i)){
							filter=false;
						}
					}
					if (filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='PE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear_nifty', " + actualGap + ", " + percProfitAtLowPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if(filter==true && isOtherStrategy && ((float)intradayHigh.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtHighPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear_nifty_Other', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = (float)intraOpen.get(i) < (float)low.get(i-1);
				}
				actualGap = ((float) close.get(i - 1) - trigger) * 100 / trigger;
				if (((float)nclose.get(i-1)-(float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc) {
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
						 && indexFilter ;//&& ((float)nclose.get(i-1)-(float)nopen.get(i-1))*100/(float)nopen.get(i-1) > prevDayBody;
					if(isCheckFirstCloseBelowAboveOpen){
						if((float)intraFirst3MinClose.get(i) < (float)open.get(i)){
							filter=false;
						}
					}
					if (filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						if(isOptionCheck){
							sql="select (close-open)*100/open from "+name+"_OPT where tradedate='"+tradedate.get(i)+"' and type='CE' and open*OPEN_INTEREST> "+optionVolume+" " 
									+" order by convert(OPEN_INTEREST, unsigned int) desc limit 1";
							optionProfit = executeCountQuery(con, sql);
						}
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull_nifty', " + actualGap + ", " + percProfitAtHighPrice + ", "
								+ avgVol + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if(filter==true && isOtherStrategy && ((float)intraOpen.get(i)-(float)intradayLow.get(i))*100/(float)intradayLow.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull_nifty_Other', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getIndexTest(java.sql.Connection con, String name, float gapPerc,float prevDayBody,long transaction,
			float min, float max){
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List open = new ArrayList<Float>();
		List high = new ArrayList<Float>();
		List low = new ArrayList<Float>();
		List intraOpen = new ArrayList<Float>();
		List intraFirst3MinClose = new ArrayList<Float>();
		List intraFirstHrClose = new ArrayList<Float>();
		List intraFC = new ArrayList<Float>(); 
		List intraSC = new ArrayList<Float>(); 
		List intraClose = new ArrayList<Float>();
		List intraday3Min3_18_Close = new ArrayList<Float>();
		List intradayAt3_15 = new ArrayList<Float>();
		List close = new ArrayList<Float>();
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		List nopen = new ArrayList<Float>();
		List nhigh = new ArrayList<Float>();
		List nlow = new ArrayList<Float>();
		List nclose = new ArrayList<Float>();
		List intradayHigh = new ArrayList<Float>();
		List intradayLow = new ArrayList<Float>();
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtLowPrice = 0f;
		try {
			
			sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.totalTrades,a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and a.intraday3Min3_18_Close is not null  and a.intradayHigh<>0 and a.intradayLow<>0  and a.intradayOpen is not null ";
			
//			if(isIndexCheck)
			{
				sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, n.open, n.high, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.totalTrades,a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ "  and a.intraday3Min3_18_Close is not null and a.intradayHigh<>0 and a.intradayLow<>0 and a.intradayOpen is not null";
			}
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalTrades"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intradayHigh.add(rs.getFloat("a.intradayHigh"));
				intradayLow.add(rs.getFloat("a.intradayLow"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat(closeAtWhatTime));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
//				if(isIndexCheck)
				{
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			
			int iter = 1;
			String out = "";
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;
			for (int i = 5; i < tradedate.size(); i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)intraday3Min3_18_Close.get(i);
				float trigger = (float) intraOpen.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				if(isIndexCheck){
					indexFilter = (float)open.get(i) > (float)high.get(i-1);
				}
				if (((float)nopen.get(i)-(float)nclose.get(i-1))*100/(float)nopen.get(i) > gapPerc) {
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
							&& indexFilter ;//&& ((float)nopen.get(i-1)-(float)nclose.get(i-1))*100/(float)nopen.get(i-1) > prevDayBody;
					if (filter) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', '"+closeAtWhatTime+"', " + trigger + ", " + percProfitAtLowPrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = (float)open.get(i) < (float)low.get(i-1);
				}
				if (((float)nclose.get(i-1)-(float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc) {
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
						 && indexFilter ;//&& ((float)nclose.get(i-1)-(float)nopen.get(i-1))*100/(float)nopen.get(i-1) > prevDayBody;
					if (filter) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) low.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', '"+closeAtWhatTime+"', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	public String isOpenPriceTouched(java.sql.Connection con, String name, String date, String dir, float trig) {
		String sql = "";
		int interval = 5;
		String isOpenPriceTouched = "";
		if (dir.equalsIgnoreCase("Bull")) {
			sql = "select tradedate from " + name + "_" + interval + " where high>" + trig
					+ " order by tradedate limit 1";
			String trigDate = executeCountQuery(con, sql);
			sql = "select case when (low < (open-OPEN*0.5/100)) then 1 else 0 end from " + name + "_" + interval
					+ " where tradedate > '" + trigDate + "' and date(tradedate) = '" + date + "'";
			isOpenPriceTouched = executeCountQuery(con, sql);
		}
		return isOpenPriceTouched;
	}
	
	public void calcProfit(java.sql.Connection con, float capital, float multiplier, int year, int month, boolean isDrawDownCalculate, String filter) throws SQLException {
		ResultSet rs= null;
		float totalCap = capital*multiplier;
		List<Float> profit = new ArrayList<Float>();
		List tradedate = new ArrayList<String>();
		float cumProfit=0f;String sql="";
		
		if(isDrawDownCalculate==true){
			rs = executeSelectSqlQuery(con, "select avg(profitPerc) as profit, "
					+ " sum(triggerPrice),"+dateField+" date from "+tableName+" where year("+dateField+")="+year+" and month("+dateField+")="+month+" group by date("+dateField+") order by "+dateField+" asc");
		}else{
//			rs = executeSelectSqlQuery(con, "select sum(profitPerc)/count(*) as profit,count(*), "
//					+ " sum(triggerPrice),date from williamsResults where 1=1 "+filter+" group by date order by date asc");
			rs = executeSelectSqlQuery(con, "select avg(profitPerc) as profit, "
					+ " sum(triggerPrice),"+dateField+" as date from "+tableName+" where 1=1 "+filter+" "
					+ " group by date("+dateField+") order by "+dateField+" asc");
		}
		 
		while(rs.next()){
			profit.add(rs.getFloat("profit"));
			tradedate.add(rs.getString("date"));
		}
		for (int i=0; i< profit.size(); i++){
			sql="select avg(profitPerc) from results_2 where startdate=date('"+tradedate.get(i)+"') group by date(startdate)";
			String difProfit = executeCountQuery(con, sql);
			if(!StringUtils.isEmpty(difProfit)){
//				profit.set(i, Float.parseFloat(difProfit));
			}
			cumProfit = totalCap*(float)profit.get(i)/100;
			capital = (float) ((capital+cumProfit)-(totalCap*2*0.0313/100));
			totalCap = capital*multiplier;
			
			sql = "Insert into psarResults(date, profitPerc, profitRupees) values ('"+tradedate.get(i)+"', "
					+ "'"+capital+"', '"+(float)profit.get(i)+"')";
			executeSqlQuery(con, sql);
			if(isGetProfitAndEnterContinously && capital >=targetProfit){
				break;
			}
		}
	}

	public static void main(String[] args) {
		/*List<String> list = Arrays.asList("nifty_psu",
				"nifty_realty");*/
		List<String> list = Arrays.asList("nifty_auto","nifty_bank","nifty_energy","nifty_finance","nifty_fmcg",
				"nifty_it","nifty_media","nifty_metal","nifty_midcap_50","nifty_pharma","nifty_psu",
				"nifty_realty");
		d1="2015-01-01"; d2="2028-12-31"; 
		d1T="2015-01-01"; d2T="2028-12-31";
		isIndexCheck=true;
//		for (String n: list)
		{
//			indexTable = n;
			System.out.println(indexTable);
		
		Test t = new Test();
		GapModified pin = new GapModified();
		java.sql.Connection dbConnection = null;
		boolean updateSymbolsTableData = true;
		boolean updateAllData = true;
		try {

			Connection con = new Connection();
			dbConnection = con.getDbConnection();
			ResultSet rs = null;
			String sql = "";
			String name = "";
//			pin.checkProfit(dbConnection);
			
			boolean updateForTodayAndNextDay = true;
			boolean updateForallDays = true;
			boolean updateResultTable = false;
			boolean isIntraDayData = false;
			boolean insertAllDataToResult = false;
			String iter = "1d";
			String path = "C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
			float gapLimitPerc=20f, highLowGap=3f;
			float min=20, max=5000;
			
			if(isUpdateIntraIntoWilliamsResults){
				pin.updateAllTimeToWilliamsResultsTable(dbConnection);
				return;
			}
			if(isCheckProfitWithSL){
				pin.getProfitWithSL(dbConnection);
				pin.executeSqlQuery(dbConnection, "update "+resultTable+" set date=startdate");
				return;
			} 
			List<String> timeList = new ArrayList<>();
			rs = con.executeSelectSqlQuery(dbConnection, "select time(tradedate) as intraTime from sbin_3 where date(tradedate) = '2018-7-12' and time(tradedate)<='15:18:00' and time(tradedate)>='09:18:00';");
	   	  	 while(rs.next()){
	   	  		 String s = rs.getString("intraTime");
	   	  		 s = s.split(":")[0]+"_"+s.split(":")[1];
	   	  		timeList.add("intraday3Min"+s+"_Close");
	   	  	 }
//	   	  		pin.gapOptionsCurrencyBuySellBoth(dbConnection, name, 1.4f, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy);
	   	
//	   	  	 pin.gapCurrencyRangeCross(dbConnection, name, 1.4f, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy);
//	   	  and "+indexTable+"=1
	   	  	 //pin.gapUpDownNiftyReverse(dbConnection, name, 0f, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);
	   	  	sql = "SELECT s.name, s.margin FROM symbols s where totalTrades >= 500 and ismargin=1 and name !='VAKRANGEE' and name!='PCJEWELLER'"
					+ " and name!='PHILIPCARB'  order by convert(totalTrades, SIGNED INTEGER) desc";
//	   	  	sql="select s.name from williamsresults s where date>='"+d1T+"' and date<='"+d2T+"' group by name having avg(profitPerc)>0 order by avg(profitPerc) desc";
			rs = con.executeSelectSqlQuery(dbConnection, sql);
   	  	 	
			if(isDeleteUnwantedRecords){
				pin.deleteUnWantedRecords(dbConnection);
			}
			if(isCheckBothBuySellProbability){
				pin.checkBothBuySellProbability(dbConnection);
			}
			
			if(1==execute)
			{
				while (rs.next()) {
					name = rs.getString("s.name");
//					margin=rs.getString("s.margin");
					System.out.println(name);
					if (!iter.equals("1d"))
						name = name + "_" + iter + "";
					/*for (int i=0; i< timeList.size(); i++)
					{
						pin.closeAtWhatTime = timeList.get(i);
						pin.getOnPrevHighLowCompare(dbConnection, name, algoPerc, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy); //0.8, 0.67%, 3888---
						pin.gapWithCloseTest(dbConnection, name, algoPerc, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy); // 2, 0.75%, 4469--
						pin.getOnPrevCloseCompare(dbConnection, name, algoPerc, -15.0f, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy); //2,-5,0.7%, 4068---
						pin.getData(dbConnection, name, algoPerc, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);//1.8,0.71%-5307---
						pin.getNiftyTest(dbConnection, name, 0.5f, 0.5f, transaction, min, max, highLowGap);
						pin.gapCompareToPrevLowHighReverse(dbConnection, name, gapWithPrevAvgClose, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);
					}*/
//					if(isCamarillaCheck){
//						pin.checkCamarilla(dbConnection, name, gapWithPrevAvgClose, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);
//					}
					pin.getOnPrevHighLowCompare(dbConnection, name, algoPerc, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy); //0.8, 0.67%, 3888---
					pin.gapWithCloseTest(dbConnection, name, algoPerc, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy); // 2, 0.75%, 4469--
					pin.getOnPrevCloseCompare(dbConnection, name, algoPerc, -15.0f, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy); //2,-5,0.7%, 4068---
					pin.getData(dbConnection, name, algoPerc, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);//1.8,0.71%-5307---
					pin.getNiftyTest(dbConnection, name, 0.8f, 0.5f, transaction, min, max, highLowGap);
//					pin.gapCompareToPrevLowHighReverse(dbConnection, name, gapWithPrevAvgClose, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);
					
//					pin.gapWithInPrevDayRange(dbConnection, name, algoPerc, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);
					
//					pin.getOnPrevBody_TodayOpenBelowAboveBody(dbConnection, name, 0.5f, -15.0f, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy);
					
//					pin.gapCompareAvgPrevCloseDaily(dbConnection, name, gapWithPrevAvgClose, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);
					
//					pin.crossingPrevCandleFully(dbConnection, name, 0.6f, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);
					
//					pin.buyAtEveryCheaperPerc(dbConnection, name, 2f, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);
//					pin.getChoppyCandle(dbConnection, name, 2f, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);
				}
			}
			pin.executeSqlQuery(dbConnection, "delete from psarresults");
			if(isDrawDownCalculate==true){
				for(int y=sY; y<=2018; y++){
					for(int m=1; m<=12; m++){
						pin.calcProfit(dbConnection, capital, mul, y, m , isDrawDownCalculate, "");
					}
				}
			}else{
				if(isCheckWhenItReachedTarget){
					pin.executeSqlQuery(dbConnection, "truncate table "+resultTable);
					pin.executeSqlQuery(dbConnection, "truncate table psarresults");
					for(int y=sY; y<=2019; y++){
						for(int m=1; m<=12; m++)
						{	
							if(y==2015 && m==1) continue;
							if(y==2019 && m>4) break;
							filter=" and "+dateField+">='"+y+"-"+m+"-01'";
							pin.calcProfit(dbConnection, capital, mul, 2015, 2 , isDrawDownCalculate, filter);
							sql = "select date(date) from psarresults order by date asc limit 1";
							String date = "'"+pin.executeCountQuery(dbConnection, sql)+"'";
							sql="insert into "+resultTable+"(duration,startdate) values((select dateDiff(date,"+date+") from psarresults where profitPerc >"+targetProfit+" order by date limit 1), "
									+ " '"+y+"-"+m+"-01')";
							pin.executeSqlQuery(dbConnection, sql);
							pin.executeSqlQuery(dbConnection, "truncate table psarresults");
						}
					}
				}else if (isGetProfitAndEnterContinously){
					pin.executeSqlQuery(dbConnection, "truncate table "+resultTable);
					pin.executeSqlQuery(dbConnection, "truncate table psarresults");
					boolean isDone = false;
					String d="'2015-02-11'";
					while(!isDone){
						filter=" and "+dateField+">="+d;
						pin.calcProfit(dbConnection, capital, mul, 2015, 2 , isDrawDownCalculate, filter);
						sql = "select date(date) from psarresults order by date asc limit 1";
						String date = "'"+pin.executeCountQuery(dbConnection, sql)+"'";
						sql = "select date from psarresults where profitPerc >"+targetProfit+" order by date limit 1";
						String dateWhenAchieved = "'"+pin.executeCountQuery(dbConnection, sql)+"'";
						if(dateWhenAchieved.equals("''")) {
							isDone=true;
							break;
						}
						sql="insert into "+resultTable+"(duration,startdate) values( (select dateDiff("+dateWhenAchieved+","+d+")), "
								+ " "+d+")";
						pin.executeSqlQuery(dbConnection, sql);
						sql = "select DATE_ADD(date("+dateWhenAchieved+"),  interval 1 day)";
						d = "'"+pin.executeCountQuery(dbConnection, sql)+"'";
						pin.executeSqlQuery(dbConnection, "truncate table psarresults");
					}
				}
				else{
					filter=" and "+dateField+">='"+sY+"-"+sM+"-01'";
					pin.calcProfit(dbConnection, capital, mul, sY, 2 , isDrawDownCalculate, filter);
				}
			}
		}

		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dbConnection != null)
				try {
					dbConnection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		}
	}
	
	public void checkProfit(java.sql.Connection dbConnection) throws SQLException{
		List<Float> profit = new ArrayList<>();
		List<String> date = new ArrayList<>();
		ResultSet rs = null;
		float targetProfit = 7f;
		List<String> intraTime = new ArrayList<>();
		String sql ="select sum(profitRupees)/count(*) profit, date, reversal from williamsresults group by date, reversal";
		rs = executeSelectSqlQuery(dbConnection, sql);
		while(rs.next()){
			profit.add(rs.getFloat("profit"));
			date.add(rs.getString("date"));
			intraTime.add(rs.getString("reversal"));
		}
		float actualProfit=0f;
		for(int i=0; i< date.size(); i++){
			if(profit.get(i) > targetProfit){
				actualProfit = targetProfit;
				String count = executeCountQuery(dbConnection, "select count(*) from psarresults where date(date)='" + date.get(i) + "'");
				if(Integer.parseInt(count)==0){
					sql = "insert into psarresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('AtTarget', '"+closeAtWhatTime+"', " + actualProfit + ", " + actualProfit + ", "
							+ actualProfit + ", '" + date.get(i) + "')";
					executeSqlQuery(dbConnection, sql);
				}
			}else if(!date.get(i).equals(date.get(i+1))){
				actualProfit = profit.get(i);
				String count = executeCountQuery(dbConnection, "select count(*) from psarresults where date(date)='" + date.get(i) + "'");
				if(Integer.parseInt(count)==0){
					sql = "insert into psarresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('AtClose', '"+closeAtWhatTime+"', " + actualProfit + ", " + actualProfit + ", "
							+ actualProfit + ", '" + date.get(i) + "')";
					executeSqlQuery(dbConnection, sql);
				}
			}
		}
	}
	public void deleteUnWantedRecords(java.sql.Connection dbConnection) throws SQLException{
		List<String> date = new ArrayList<>();
		List<String> name = new ArrayList<>();
		ResultSet rs = null;
		float targetProfit = 7f;
		
		List<String> intraTime = new ArrayList<>();
		String sql ="select name, date,avg(triggerprice),triggerprice from "+tableToDeleteUnwantedRecords+"  group by date(date) having count(distinct(name)) > "+maxSymbolsToPlaceOrder;
		rs = executeSelectSqlQuery(dbConnection, sql);
		while(rs.next()){
			date.add(rs.getString("date"));
		}
		float actualProfit=0f;
		for(int i=0; i< date.size(); i++){
			sql = "select count(distinct(name)) from "+tableToDeleteUnwantedRecords+"  where date(date)=date('"+date.get(i)+"')";
			if(Integer.parseInt(executeCountQuery(dbConnection, sql)) > maxSymbolsToPlaceOrder){
				sql ="delete from "+tableToDeleteUnwantedRecords+" where name in (select name from (select name from "+tableToDeleteUnwantedRecords+"  "
						+ " where date(date)=date('"+date.get(i)+"') group by date(date),name order by triggerprice desc limit "+maxSymbolsToPlaceOrder+",1000) w) and "
						+ " date='"+date.get(i)+"'";
				executeSqlQuery(dbConnection, sql);
			}
		}
	}
	
	public void checkBothBuySellProbability(java.sql.Connection dbConnection) throws SQLException{
		List<String> date = new ArrayList<>();
		List<String> name = new ArrayList<>();
		ResultSet rs = null;
		float targetProfit = 7f;
		
		List<String> intraTime = new ArrayList<>();
		String sql ="select name, date,avg(triggerprice),triggerprice from williamsresults  group by date ";
		rs = executeSelectSqlQuery(dbConnection, sql);
		while(rs.next()){
			date.add(rs.getString("date"));
		}
		float actualProfit=0f;
		for(int i=0; i< date.size(); i++){
			System.out.println(date.get(i));
			sql = "select count(distinct(name)) from williamsresults  where date='"+date.get(i)+"'";
			int totalCountForDay = Integer.parseInt(executeCountQuery(dbConnection, sql));
			int bullExpectedCount = totalCountForDay/2;
			int bearExpectedCount = totalCountForDay/2;
			sql = "select count(distinct(name)) from williamsresults  where date='"+date.get(i)+"' and reversal like '%Bull%'";
			int bullActualDailyCount = Integer.parseInt(executeCountQuery(dbConnection, sql));
			sql = "select count(distinct(name)) from williamsresults  where date='"+date.get(i)+"' and reversal like '%Bear%'";
			int bearActualDailyCount = Integer.parseInt(executeCountQuery(dbConnection, sql));
			
			if(bullActualDailyCount+bearActualDailyCount < maxSymbolsToPlaceOrder){
				sql="select distinct(name) as N from williamsresults where date='"+date.get(i)+"'"+ 
					" and reversal like '%Bull%' group by name order by triggerprice desc limit "+bullExpectedCount;
				rs = executeSelectSqlQuery(dbConnection, sql);
				while (rs.next()){
					sql="Insert into williamsresults_New (select * from williamsresults where date='"+date.get(i)+"' "
							+ " and reversal like '%Bull%' and name = '"+rs.getString("N")+"')";
//					System.out.println(sql);
					executeSqlQuery(dbConnection, sql);
				}
				
				sql="select count(distinct(name)) as N from williamsresults where date='"+date.get(i)+"'"+ 
						" and reversal like '%Bear%' group by name order by triggerprice desc limit "+bearExpectedCount;
				rs = executeSelectSqlQuery(dbConnection, sql);
				while (rs.next()){
					sql="Insert into williamsresults_New (select * from williamsresults where date='"+date.get(i)+"' "
							+ " and reversal like '%Bear%' and name = '"+rs.getString("N")+"')";
//					System.out.println(sql);
					executeSqlQuery(dbConnection, sql);
				}
			}
		}
	}
	
	public void updateAllTimeToWilliamsResultsTable(java.sql.Connection dbConnection) throws SQLException{
		List<String> timeList = new ArrayList<>();
		List<String> symbolsList = new ArrayList<>();
		ResultSet rs=null;
		Connection con = new Connection();
		rs = con.executeSelectSqlQuery(dbConnection,
				"select time(tradedate) as intraTime from sbin_3 where date(tradedate) = '2018-7-12'");
		while (rs.next()) {
			String s = rs.getString("intraTime");
			s = s.split(":")[0] + "_" + s.split(":")[1];
			timeList.add(s);
		}
		rs = con.executeSelectSqlQuery(dbConnection, "select distinct(name) N from "+tableToUpdate);
		while(rs.next()){
			symbolsList.add(rs.getString("N"));
		}
		for(int j=0; j< symbolsList.size(); j++){
			System.out.println(symbolsList.get(j));
			String col="";
			StringBuilder sb = new StringBuilder();
			sb.append("update "+tableToUpdate+" w, "+symbolsList.get(j)+" s set ");
			for(int i=0; i< timeList.size(); i++){
				col = "intraday3Min"+timeList.get(i)+"_Close";
				sb.append("w."+col+"=s."+col);
				if(i!=timeList.size()-1){
					sb.append(",");
				}
	  		}
			sb.append(" where w.name='"+symbolsList.get(j)+"' ");
			sb.append(" and date(s.tradedate)=date(w.date)");
			con.executeSqlQuery(dbConnection, sb.toString());
		}
	}
	public void getProfitWithSL(java.sql.Connection dbConnection) throws SQLException{
		List<String> timeList = new ArrayList<>();
		List<String> symbolsList = new ArrayList<>();
		List<String> dateList = new ArrayList<>();
		ResultSet rs=null;
		Connection con = new Connection();
		rs = con.executeSelectSqlQuery(dbConnection,
				"select time(tradedate) as intraTime from sbin_3 where date(tradedate) = '2018-7-12'");
		while (rs.next()) {
			String s = rs.getString("intraTime");
			s = s.split(":")[0] + "_" + s.split(":")[1];
			timeList.add(s);
		}
		rs = con.executeSelectSqlQuery(dbConnection, "select distinct(date) d from  "+tableName
				+ "  order by date;");//where date='2015-03-20'
		while(rs.next()){
			dateList.add(rs.getString("d"));
		}
		String col="",sql="",timeEnter="intraday3Min09_15_Close", timeSqOff="intraday3Min15_18_Close";
		float slPercTemp=slPerc;
		for(int i=0; i< dateList.size(); i++){
			System.out.println(dateList.get(i));
			slPerc = slPercTemp;
			for(int j=2; j< timeList.size(); j++){
				
				col = "intraday3Min"+timeList.get(j)+"_Close";
				sql="select avg(case when (reversal like '%Bull%') then (case when ("+col+"-"+timeEnter+")*100/"+timeEnter+" < -3 then -3 else ("+col+"-"+timeEnter+")*100/"+timeEnter+" end) else "+
" (case when ("+timeEnter+"-"+col+")*100/"+timeEnter+" < -3 then -3 else ("+timeEnter+"-"+col+")*100/"+timeEnter+" end ) end) as P from "+tableName+" "+ 
" where "+col+" is not null and "+timeEnter+" is not null and date='"+dateList.get(i)+"' ";
				String profit = con.executeCountQuery(dbConnection, sql);
				if(profit=="" || profit==null){
					break;
				}
				if(Float.parseFloat(profit)> hitPercAndWentNegative){
					slPerc = SLWhenhitPercAndWentNegative;
				}
				if(Float.parseFloat(profit)> targetPerc){
					sql="insert into "+resultTable+" values('"+profit+"', date('"+dateList.get(i)+"'),'"+profit+"','1','')";
					con.executeSqlQuery(dbConnection, sql);
					break;
				}
				if(Float.parseFloat(profit)< slPerc){
//					sql="insert into "+resultTable+" values('"+slPerc+"', date('"+dateList.get(i)+"'),'"+slPerc+"','1','')";
					sql="insert into "+resultTable+" values('"+profit+"', date('"+dateList.get(i)+"'),'"+profit+"','1','')";
					con.executeSqlQuery(dbConnection, sql);
					break;
				}else if(timeList.get(j).equals("15_18")){
					
					sql="select avg(case when (reversal like '%Bull%') then (case when profitPerc < -3 then -3 else ("+timeSqOff+"-"+timeEnter+")*100/"+timeEnter+" end) else "+
" (case when profitPerc < -3 then -3 else ("+timeEnter+"-"+timeSqOff+")*100/"+timeEnter+" end ) end) as P from "+tableName+""+ 
" where "+timeSqOff+" is not null and "+timeEnter+" is not null and date='"+dateList.get(i)+"' ";
					profit = con.executeCountQuery(dbConnection, sql);
					sql="insert into "+resultTable+" values('"+profit+"', date('"+dateList.get(i)+"'),'"+profit+"','1','')";
					con.executeSqlQuery(dbConnection, sql);
					break;
				}
			}
		}
	}
}
