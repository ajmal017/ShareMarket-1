package Strategies.imp;

import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.poi.hssf.record.DBCellRecord;

import Indicators.Connection;
import Indicators.Test;

public class GapModified extends Connection {
	int sl=18;
	boolean isSL=true;
	boolean exitNextDayClose = false;float maxLossForNextDayExit=-10f;
	String isEntryAtOpenOr3MinClose = "a.intradayFirst3MinClose";//a.intradayOpen, intradayFirst3MinClose
	String intradayOpen="a.intradayOpen"; //intradayOpen //comparing with open price of day or intra open
	String closeAtWhatTime="a.intraday3Min3_18_close";
	boolean isIndexCheck = false;
	static boolean isOtherStrategy = false;
	static int first3MinTransaction = 0;
	static String indexTable="nifty_50";//nifty_auto,nifty_bank,nifty_energy,nifty_finance,nifty_fmcg,nifty_it,nifty_media,nifty_metal,nifty_midcap_50,nifty_pharma,nifty_psu,nifty_realty
	float indexGapDiv=0.9f;
	static String margin;
	long avgTotalTrades = 7000l;
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
			String d = "2015-02-02";
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
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;
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
				if ((trigger - (float) high.get(i - 1)) * 100 / trigger > gapPerc
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
					if(filter==true && !isOtherStrategy)
					{
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float)intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear1', " + trigger + ", " + percProfitAtLowPrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if(filter==true && isOtherStrategy && ((float)intradayHigh.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						
						percProfitAtHighPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear1_Other', " + trigger + ", " + percProfitAtHighPrice + ", "
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
				if (((float) low.get(i - 1) - trigger) * 100 / trigger > gapPerc
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
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull1', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
						
					}
					if(filter==true  && isOtherStrategy &&  ((float)intraOpen.get(i)-(float)intradayLow.get(i))*100/(float)intradayLow.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull1_Other', " + trigger + ", " + percProfitAtHighPrice + ", "
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
			String d = "2015-02-02";
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
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;
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
				if ((trigger - (float) close.get(i - 1)) * 100 / trigger > gapPerc
						&& (trigger - (float) close.get(i - 1)) * 100 / trigger < gapLimitPerc
						&& indexFilter)
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger > min && 
							trigger < max  
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					 
					if (filter==true && !isOtherStrategy)
					{
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear2', " + trigger + ", " + percProfitAtLowPrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
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
				if (((float) close.get(i - 1) - trigger) * 100 / trigger > gapPerc
						&& ((float) close.get(i - 1) - trigger) * 100 / trigger < gapLimitPerc
						&& indexFilter)
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger > min && 
							trigger < max
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
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
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull2', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
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
		List<String> optionsName = Arrays.asList("USDINRAUGFUT_5");
		for(String optName: optionsName)
		{
			List<String> allDates = new ArrayList<>();
			int avgVolume = 1000;
			try {
				allDates.clear();
				String d = "2018-05-01";
				String table1=optName;
				rs = executeSelectSqlQuery(con, "select date(tradedate) as d from "+table1+" group by date(tradedate) having avg(volume)>"+avgVolume);
				while(rs.next()){
					allDates.add(rs.getString("d"));
				}
//				float aboveWhat=1f, bodyWidth=5;
				float aboveWhat=0.005f, bodyWidth=0.005f;
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
					for(int i=0; i< tradedate.size()-13; i++){
						float width=(close.get(i) - open.get(i))*100/open.get(i);
						float prevHight = high.get(i)+(high.get(i)*aboveWhat/100);
						if(width > bodyWidth && high.get(i+1) >= prevHight)
						{
							float profit = (high.get(i+1)-prevHight)*100/prevHight;
							float profitClose = (close.get(i+1)-prevHight)*100/prevHight;
							float p=(close.get(i+1)-prevHight)*1000*65;
							p = (float) (p-(p*6/100));
							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + table1 + "', 'Bull', " + p + ", " + prevHight + ", "
									+ close.get(i+1) + ", '" + tradedate.get(i+1) + "')";
							executeSqlQuery(con, sql);
						}
						width=(open.get(i) - close.get(i))*100/close.get(i);
						prevHight = low.get(i)-(low.get(i)*aboveWhat/100);
						if(width > bodyWidth && low.get(i+1) <= prevHight){
							float profit = (prevHight-low.get(i+1))*100/prevHight;
							float profitClose = (prevHight-close.get(i+1))*100/prevHight;
							float p=(prevHight-close.get(i+1))*1000*65;
							p = (float) (p-(p*6/100));
							
							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + table1 + "', 'Bear', " + p + ", " + prevHight + ", "
									+ close.get(i+1) + ", '" + tradedate.get(i+1) + "')";
							executeSqlQuery(con, sql);
						}
					}
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
			String d = "2015-02-02";
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
			String d = "2015-02-02";
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
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;
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
				if ((trigger - (float) close.get(i - 1)) * 100 / trigger > gapPerc
						&& (trigger - (float) close.get(i - 1)) * 100
								/trigger < gapLimitPerc && indexFilter) 
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger > min && 
							trigger < max && (float) open.get(i - 1) != (float) close.get(i - 1);
					// && (float)open.get(i)>(float)close.get(i-1) ;
					// if(((float)open.get(i-1)==(float)high.get(i-1)))
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
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear3', " + trigger + ", " + percProfitAtLowPrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
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
				if (((float) close.get(i - 1) - trigger) * 100 / trigger > gapPerc
						&& ((float) close.get(i - 1) - trigger) * 100
								/ trigger < gapLimitPerc && indexFilter) 
				{
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger > min && 
							trigger < max && (float) open.get(i - 1) != (float) close.get(i - 1);
					// && (float)open.get(i)>(float)close.get(i-1);
					// if(((float)open.get(i-1)==(float)low.get(i-1)))
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
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull3', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
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
			String d = "2015-02-02";
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
			
			int iter = 1;
			String out = "";
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;
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
				if ((trigger - (float) close.get(i - 1)) * 100 / trigger > gapPerc
						&& (trigger - (float) close.get(i - 1)) * 100
								/ trigger < gapLimitPerc && indexFilter) {
					
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					
					float fib = (float) high.get(i - 1) - ((float) high.get(i - 1) - (float) low.get(i - 1)) / 2;
					if (trigger > fib && filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear4', " + trigger + ", " + percProfitAtLowPrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
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
				if (((float) close.get(i - 1) - trigger) * 100 / trigger > gapPerc
						&& ((float) close.get(i - 1) - trigger) * 100
								/ trigger < gapLimitPerc && indexFilter) {
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					float fib = (float) low.get(i - 1) + ((float) high.get(i - 1) - (float) low.get(i - 1)) / 2;
					if (trigger < fib && filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull4', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
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
	
	public void getTest(java.sql.Connection con, String name, float gapPerc,long transaction,
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
			String d = "2015-02-02";
			sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and a.intraday3Min3_18_Close is not null  and a.intradayOpen is not null ";
			
//			if(isIndexCheck)
			{
				sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow,n.open, n.high, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ "  and a.intraday3Min3_18_Close is not null and a.intradayOpen is not null";
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
				intraday3Min3_18_Close.add(rs.getFloat("a.intraday3Min3_18_Close"));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				intradayHigh.add(rs.getFloat("a.intradayHigh"));
				intradayLow.add(rs.getFloat("a.intradayLow"));
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
				if (((float)nopen.get(i)-(float)nclose.get(i-1))*100/(float)nclose.get(i-1) > gapPerc) {
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1) && indexFilter;
					if (filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', '"+closeAtWhatTime+"', " + trigger + ", " + percProfitAtLowPrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if (isOtherStrategy && filter && ((float)high.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap) {
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = (trigger - (float) high.get(i)) * 100 / (float) trigger;
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
							&& (float) open.get(i - 1) != (float) close.get(i - 1) && indexFilter;
					if (filter && !isOtherStrategy) {
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
					if (isOtherStrategy && filter
							&& ((float)intraOpen.get(i)-(float)low.get(i))*100/(float)low.get(i) > highLowGap) {
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
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
			String d = "2015-02-02";
			sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.totalTrades,a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null ";
			
//			if(isIndexCheck)
			{
				sql = "select a.volume," + "a.open, a.intradayHigh, a.intradayLow, n.open, n.high, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, "+closeAtWhatTime+", a.totalTrades,a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, "+indexTable+" n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ "  and a.intradayHigh<>0 and a.intradayLow<>0 and a.intraday3Min3_18_Close is not null and intraday3Min09_15_Volume is not null and intraday3Min09_15_Volume*a.open >"+first3MinTransaction+" and a.intradayOpen is not null";
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
					if (filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = ((float) intradayHigh.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear_nifty', " + trigger + ", " + percProfitAtLowPrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
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
					indexFilter = (float)open.get(i) < (float)low.get(i-1);
				}
				if (((float)nclose.get(i-1)-(float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc) {
					boolean filter = avgVol > transaction && avgQty > avgTotalTrades && trigger < max && trigger > min
						 && indexFilter ;//&& ((float)nclose.get(i-1)-(float)nopen.get(i-1))*100/(float)nopen.get(i-1) > prevDayBody;
					if (filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) intradayLow.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull_nifty', " + trigger + ", " + percProfitAtHighPrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
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
			String d = "2015-02-02";
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
		List profit = new ArrayList<Float>();
		List tradedate = new ArrayList<String>();
		float cumProfit=0f;String sql="";
		if(isDrawDownCalculate==true){
			rs = executeSelectSqlQuery(con, "select sum(profitRupees)/count(*) as profit,count(*), "
					+ " sum(triggerPrice),date from williamsResults where year(date)="+year+" and month(date)="+month+" group by date order by date asc");
		}else{
			rs = executeSelectSqlQuery(con, "select sum(profitRupees)/count(*) as profit,count(*), "
					+ " sum(triggerPrice),date from williamsResults where 1=1 "+filter+" group by date order by date asc");
		}
		 
		while(rs.next()){
			profit.add(rs.getFloat("profit"));
			tradedate.add(rs.getString("date"));
		}
		for (int i=0; i< profit.size(); i++){
			cumProfit = totalCap*(float)profit.get(i)/100;
			capital = capital+cumProfit;
			totalCap = capital*multiplier;
			sql = "Insert into psarResults(date, profitPerc, profitRupees) values ('"+tradedate.get(i)+"', '"+capital+"', '"+(float)profit.get(i)+"')";
			executeSqlQuery(con, sql);
		}
	}

	public static void main(String[] args) {
		List<String> list = Arrays.asList("nifty_auto","nifty_bank","nifty_energy","nifty_finance","nifty_fmcg",
				"nifty_it","nifty_media","nifty_metal","nifty_midcap_50","nifty_pharma","nifty_psu",
				"nifty_realty");
//		for (String n: list)
		{
//			indexTable = n;
		
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
			boolean insertAllDataToResult = false;float capital=100000f;boolean isDrawDownCalculate = false;
			String iter = "1d";
			String path = "C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
			float gapLimitPerc=7f, highLowGap=6;
			long transaction = 10000000;float min=20, max=5000;
			
			
			List<String> timeList = new ArrayList<>();
			rs = con.executeSelectSqlQuery(dbConnection, "select time(tradedate) as intraTime from sbin_3 where date(tradedate) = '2018-7-12' and time(tradedate)<='15:18:00' and time(tradedate)>='09:18:00';");
	   	  	 while(rs.next()){
	   	  		 String s = rs.getString("intraTime");
	   	  		 s = s.split(":")[0]+"_"+s.split(":")[1];
	   	  		timeList.add("intraday3Min"+s+"_Close");
	   	  	 }
//	   	  		pin.gapOptionsCurrencyBuySellBoth(dbConnection, name, 1.4f, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy);
	   	
	   	  	 pin.gapCurrencyRangeCross(dbConnection, name, 1.4f, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy);
//	   	  and "+indexTable+"=1
	   	  	sql = "SELECT s.name, s.margin FROM symbols s where totalTrades >= 500 and isMargin=1 and name !='VAKRANGEE' and name!='PCJEWELLER'"
					+ " and name!='PHILIPCARB' order by convert(totalTrades, SIGNED INTEGER) desc";
			rs = con.executeSelectSqlQuery(dbConnection, sql);
   	  	 
			if(1==0)
			{
				while (rs.next()) {
					name = rs.getString("s.name");
					margin=rs.getString("s.margin");
					System.out.println(name);
					if (!iter.equals("1d"))
						name = name + "_" + iter + "";
					/*for (int i=0; i< timeList.size(); i++){
						pin.closeAtWhatTime = timeList.get(i);
						pin.getOnPrevHighLowCompare(dbConnection, name, 1.5f, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy); //0.8, 0.67%, 3888---
						pin.gapWithCloseTest(dbConnection, name, 1.5f, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy); // 2, 0.75%, 4469--
						pin.getOnPrevCloseCompare(dbConnection, name, 1.5f, -15.0f, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy); //2,-5,0.7%, 4068---
						pin.getData(dbConnection, name, 1.5f, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);//1.8,0.71%-5307---
						pin.getNiftyTest(dbConnection, name, 0.9f, 0.5f, transaction, min, max);
					}*/
					
					pin.getOnPrevHighLowCompare(dbConnection, name, 1.4f, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy); //0.8, 0.67%, 3888---
					pin.gapWithCloseTest(dbConnection, name, 1.4f, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy); // 2, 0.75%, 4469--
					pin.getOnPrevCloseCompare(dbConnection, name, 1.4f, -15.0f, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy); //2,-5,0.7%, 4068---
					pin.getData(dbConnection, name, 1.4f, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);//1.8,0.71%-5307---
					pin.getNiftyTest(dbConnection, name, 0.8f, 0.5f, transaction, min, max, highLowGap);
//					pin.getIndexTest(dbConnection, name, 1f, 0.5f, transaction, min, max);
				}
			}
			String filter = " ";//and year(date)=2018
			isDrawDownCalculate=false;
			if(isDrawDownCalculate==true){
				for(int y=2015; y<=2018; y++){
					for(int m=1; m<=12; m++){
						pin.calcProfit(dbConnection, capital, 1.5f, y, m , isDrawDownCalculate, "");
					}
				}
			}else{
				//pin.calcProfit(dbConnection, capital, 1.5f, 2015, 2 , isDrawDownCalculate, filter);
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
}
