package Strategies.imp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.poi.hssf.record.DBCellRecord;

import Indicators.Connection;
import Indicators.Test;

public class GapModified extends Connection {
	int sl=30;
	boolean isSL=false;
	boolean exitNextDayClose = false;float maxLossForNextDayExit=-10f;
	String isEntryAtOpenOr3MinClose = "a.intradayFirst3MinClose";//a.intradayOpen, intradayFirst3MinClose
	String intradayOpen="a.open"; //intradayOpen //comparing with open price of day or intra open
	boolean isIndexCheck = false;
	int indexGapDiv=6;
	static String margin;
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
		
		List totalVol = new ArrayList<Long>();
		List totalQty = new ArrayList<Long>();
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtClosePrice = 0f, percProfitAtLowPrice = 0f;
		try {
			String d = "2015-02-02";
			// name= name+"_FUT";
			sql = "select a.volume," + "a.open, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ "  and a.intraday3Min3_18_Close is not null and a.intradayOpen is not null";
			
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, n.open, n.high, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, nifty_50 n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
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
				totalQty.add(rs.getLong("a.totalQty"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat("a.intraday3Min3_18_Close"));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			long avgMinQty = 0l;
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
					indexFilter = ((float)nopen.get(i) - (float)nclose.get(i-1))*100/(float)nclose.get(i-1) > gapPerc/indexGapDiv;
				}
				if ((trigger - (float) high.get(i - 1)) * 100 / trigger > gapPerc
						&& (trigger - (float) close.get(i - 1)) * 100 / trigger < gapLimitPerc && indexFilter)
				{
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > min && 
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
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) high.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear1', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if(filter==true && isOtherStrategy && ((float)high.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / trigger;
						percProfitAtHighPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear1', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraOpen.get(i);
//				trigger = (float) intraFirst3MinClose.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nclose.get(i-1) - (float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc/indexGapDiv;
				}
				if (((float) low.get(i - 1) - trigger) * 100 / trigger > gapPerc
						&& ((float) close.get(i - 1) - trigger) * 100 / trigger < gapLimitPerc && indexFilter)
				{
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > min && 
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
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) low.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull1', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
						
					}
					if(filter==true  && isOtherStrategy &&  ((float)intraOpen.get(i)-(float)low.get(i))*100/(float)low.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = ((float) low.get(i) - trigger) * 100 / (float) trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull1', " + trigger + ", " + percProfitAtClosePrice + ", "
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
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtClosePrice = 0f, percProfitAtLowPrice = 0f;
		try {
			String d = "2015-02-02";
			// name= name+"_FUT";
			sql = "select a.volume," + "a.open, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and a.intraday3Min3_18_Close is not null  and a.intradayOpen is not null";
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, n.open, n.high, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, nifty_50 n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
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
				totalQty.add(rs.getLong("a.totalQty"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat("a.intraday3Min3_18_Close"));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			long avgMinQty = 0l;
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
				}
				if ((trigger - (float) close.get(i - 1)) * 100 / trigger > gapPerc
						&& (trigger - (float) close.get(i - 1)) * 100 / trigger < gapLimitPerc
						&& indexFilter)
				{
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > min && 
							trigger < max  
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					 
					if (filter==true && !isOtherStrategy)
					{
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) high.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear2', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if(filter==true && isOtherStrategy && ((float)high.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap)
					{
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = (trigger - (float) high.get(i)) * 100 / trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear2', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
//				trigger = (float) intraFirst3MinClose.get(i);
				trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = ((float)nclose.get(i-1) - (float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc/indexGapDiv;
				}
				if (((float) close.get(i - 1) - trigger) * 100 / trigger > gapPerc
						&& ((float) close.get(i - 1) - trigger) * 100 / trigger < gapLimitPerc
						&& indexFilter)
				{
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > min && 
							trigger < max
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					if (filter==true && !isOtherStrategy)
					{
						trigger = ((float) intraOpen.get(i) + (float) intraOpen.get(i) * .1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) low.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull2', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if(filter==true && isOtherStrategy && ((float)intraOpen.get(i)-(float)low.get(i))*100/(float)low.get(i) > highLowGap)
					{
						trigger = ((float) intraOpen.get(i) + (float) intraOpen.get(i) * .1f / 100);
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
//						percProfitAtHighPrice = ((float) intraFirst3MinClose.get(i+1) - trigger) * 100 / (float) trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull2', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		float percProfitAtHighPrice = 0f, percProfitAtClosePrice = 0f, percProfitAtLowPrice = 0f;
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
				totalQty.add(rs.getLong("a.totalQty"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat("a.intraday3Min3_18_Close"));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
			}
			long avgMinQty = 0l;
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
				boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > 10 && 
						trigger < 5000 ;
				float tri = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
//				tri = ((float)high.get(i-1) - (float)low.get(i-1))*3 + (float)intraOpen.get(i);
				if(filter==true && (float)high.get(i) > tri )
				{
					trigger = tri;
					percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / trigger;
					percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bear', " + trigger + ", " + percProfitAtClosePrice + ", "
							+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
				tri = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
//				tri =  (float)intraOpen.get(i) - ((float)high.get(i-1) - (float)low.get(i-1))*3;
				if(filter==true && (float)low.get(i) < tri )
				{
					trigger = tri;
					percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
					percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bull', " + trigger + ", " + percProfitAtClosePrice + ", "
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
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtClosePrice = 0f, percProfitAtLowPrice = 0f;
		try {
			String d = "2015-02-02";
			// name= name+"_FUT";
			sql = "select a.volume," + "a.open, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and a.intraday3Min3_18_Close is not null  and a.intradayOpen is not null";
			
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, n.open, n.high, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, nifty_50 n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
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
				totalQty.add(rs.getLong("a.totalQty"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat("a.intraday3Min3_18_Close"));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			long avgMinQty = 0l;
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
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > min && 
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
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = ((float) high.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear3', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if (((float) open.get(i - 1) - (float) close.get(i - 1)) * 100
							/ (float) open.get(i - 1) > prevBodyGap && filter
							&& isOtherStrategy && ((float)high.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = (trigger - (float) high.get(i)) * 100 / trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear3', " + trigger + ", " + percProfitAtClosePrice + ", "
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
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > min && 
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
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) low.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull3', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if (((float) close.get(i - 1) - (float) open.get(i - 1)) * 100
							/ (float) open.get(i - 1) > prevBodyGap && filter
							&& isOtherStrategy && ((float)intraOpen.get(i)-(float)low.get(i))*100/(float)low.get(i) > highLowGap)
					{
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
//						percProfitAtHighPrice = ((float) intraFirst3MinClose.get(i+1) - trigger) * 100 / (float) trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull3', " + trigger + ", " + percProfitAtClosePrice + ", "
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
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtClosePrice = 0f, percProfitAtLowPrice = 0f;
		try {
			String d = "2015-02-02";
			sql = "select a.volume," + "a.open, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and a.intraday3Min3_18_Close is not null  and a.intradayOpen is not null ";
			
			if(isIndexCheck){
				sql = "select a.volume," + "a.open, n.open, n.high, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, nifty_50 n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
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
				totalQty.add(rs.getLong("a.totalQty"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat("a.intraday3Min3_18_Close"));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
				if(isIndexCheck){
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			long avgMinQty = 0l;
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
				}
				if ((trigger - (float) close.get(i - 1)) * 100 / trigger > gapPerc
						&& (trigger - (float) close.get(i - 1)) * 100
								/ trigger < gapLimitPerc && indexFilter) {
					
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					
					float fib = (float) high.get(i - 1) - ((float) high.get(i - 1) - (float) low.get(i - 1)) / 2;
					if (trigger > fib && filter && !isOtherStrategy) {
//						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / (float) trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = ((float) high.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear4', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if ((float) intraOpen.get(i) > fib && isOtherStrategy && filter && ((float)high.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap) {
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / (float) trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = (trigger - (float) high.get(i)) * 100 / (float) trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear4', " + trigger + ", " + percProfitAtClosePrice + ", "
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
								/ trigger < gapLimitPerc && indexFilter) {
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					float fib = (float) low.get(i - 1) + ((float) high.get(i - 1) - (float) low.get(i - 1)) / 2;
					if (trigger < fib && filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) low.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull4', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if ((float) intraOpen.get(i) < fib && isOtherStrategy && filter
							&& ((float)intraOpen.get(i)-(float)low.get(i))*100/(float)low.get(i) > highLowGap) {
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull4', " + trigger + ", " + percProfitAtClosePrice + ", "
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
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtClosePrice = 0f, percProfitAtLowPrice = 0f;
		try {
			String d = "2015-02-02";
			sql = "select a.volume," + "a.open, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and a.intraday3Min3_18_Close is not null  and a.intradayOpen is not null ";
			
//			if(isIndexCheck)
			{
				sql = "select a.volume," + "a.open, n.open, n.high, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, nifty_50 n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
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
				totalQty.add(rs.getLong("a.totalQty"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat("a.intraday3Min3_18_Close"));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
//				if(isIndexCheck)
				{
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			long avgMinQty = 0l;
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
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1) && indexFilter;
					if (filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / (float) trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = ((float) high.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear4', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if (isOtherStrategy && filter && ((float)high.get(i)-(float)intraOpen.get(i))*100/(float)intraOpen.get(i) > highLowGap) {
						trigger = (float)intraOpen.get(i) + ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / (float) trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = (trigger - (float) high.get(i)) * 100 / (float) trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear4', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = (float)open.get(i) < (float)low.get(i-1);
				}
				if (((float)nclose.get(i-1)-(float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc) {
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1) && indexFilter;
					if (filter && !isOtherStrategy) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) low.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull4', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
					if (isOtherStrategy && filter
							&& ((float)intraOpen.get(i)-(float)low.get(i))*100/(float)low.get(i) > highLowGap) {
						trigger = (float)intraOpen.get(i) - ((float)intraOpen.get(i)*highLowGap/100);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull4', " + trigger + ", " + percProfitAtClosePrice + ", "
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
		boolean indexFilter=true;
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtClosePrice = 0f, percProfitAtLowPrice = 0f;
		try {
			String d = "2015-02-02";
			sql = "select a.volume," + "a.open, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a where a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
					+ " a.intradayFirst3MinClose !=0 and abs((a.intradayOpen-a.open)*100/a.open) < 5 "
					+ " and a.intraday3Min3_18_Close is not null  and a.intradayOpen is not null ";
			
//			if(isIndexCheck)
			{
				sql = "select a.volume," + "a.open, n.open, n.high, n.low,n.close, a.intradayAt3_15,a.high,a.intraday3Min3_18_Close, a.intradayOpen, a.intradayFirst3MinClose, a.intraFirstMinClose, "
					+ " a.intradayFirstHrClose, a.intradaySecond3MinClose, a.intraSecondMinClose,a.intradayClose, a.low,a.close,a.tradedate,a.totalQty from  `" + name
					+ "` as a, nifty_50 n where n.tradedate >= '"+d+"' and date(n.tradedate)=date(a.tradedate) and a.tradedate >= '"+d+"' and a.intradayFirst3MinClose is not null and "
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
				totalQty.add(rs.getLong("a.totalQty"));
				intraOpen.add(rs.getFloat(intradayOpen));
				intraFirst3MinClose.add(rs.getFloat(isEntryAtOpenOr3MinClose));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat("a.intraday3Min3_18_Close"));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
//				if(isIndexCheck)
				{
					nopen.add(rs.getFloat("n.open"));
					nhigh.add(rs.getFloat("n.high"));
					nlow.add(rs.getFloat("n.low"));
					nclose.add(rs.getFloat("n.close"));
				}
			}
			long avgMinQty = 0l;
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
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger < max && trigger > min
							&& indexFilter ;//&& ((float)nopen.get(i-1)-(float)nclose.get(i-1))*100/(float)nopen.get(i-1) > prevDayBody;
					if (filter) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / (float) trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = ((float) high.get(i)-trigger) * 100 / trigger;
						if(stopLoss > sl && isSL) percProfitAtLowPrice = sl*-1;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear4', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraOpen.get(i);
				if(isIndexCheck){
					indexFilter = (float)open.get(i) < (float)low.get(i-1);
				}
				if (((float)nclose.get(i-1)-(float)nopen.get(i))*100/(float)nopen.get(i) > gapPerc) {
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger < max && trigger > min
						 && indexFilter ;//&& ((float)nclose.get(i-1)-(float)nopen.get(i-1))*100/(float)nopen.get(i-1) > prevDayBody;
					if (filter) {
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = (trigger-(float) low.get(i)) * 100 / (float) trigger;
						if(stopLoss > sl && isSL) percProfitAtHighPrice = sl*-1;
						
						if(percProfitAtHighPrice < maxLossForNextDayExit && exitNextDayClose){
							percProfitAtHighPrice = (float)intraFirst3MinClose.get(i+1);
						}
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull4', " + trigger + ", " + percProfitAtClosePrice + ", "
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
//			 sql = "SELECT s.name FROM symbols s where nifty500=0 and volume >'5000000' order by convert(totalTrades, SIGNED INTEGER) desc";
//			 sql = "SELECT s.name FROM symbols s where nifty500=1 and volume > '5000000' order by convert(totalTrades, SIGNED INTEGER) desc";
//			or isNonMarginSymbolToBeConsidered='1'
			sql = "SELECT s.name, s.margin FROM symbols s where volume > '5000000' and isMargin=1 and name !='VAKRANGEE' and name!='PCJEWELLER'"
					+ " order by convert(totalTrades, SIGNED INTEGER) desc";
			rs = con.executeSelectSqlQuery(dbConnection, sql);
			String name = "";
			boolean updateForTodayAndNextDay = true;
			boolean updateForallDays = true;
			boolean updateResultTable = false;
			boolean isIntraDayData = false;
			boolean insertAllDataToResult = false;float capital=100000f;boolean isDrawDownCalculate = false;
			String iter = "1d";
			String path = "C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
			float gapLimitPerc=7f, highLowGap=5;
			long transaction = 50000000;float min=20, max=2000;
			boolean isOtherStrategy = false;
			if(1==0)
			{
				while (rs.next()) {
					name = rs.getString("s.name");
					margin=rs.getString("s.margin");
					System.out.println(name);
					if (!iter.equals("1d"))
						name = name + "_" + iter + "";
					pin.getOnPrevHighLowCompare(dbConnection, name, 1.4f, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy); //0.8, 0.67%, 3888---
					pin.gapWithCloseTest(dbConnection, name, 1.4f, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy); // 2, 0.75%, 4469--
					pin.getOnPrevCloseCompare(dbConnection, name, 1.4f, -15.0f, transaction, min, max,gapLimitPerc,highLowGap,isOtherStrategy); //2,-5,0.7%, 4068---
					pin.getData(dbConnection, name, 1.4f, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);//1.8,0.71%-5307---
//					pin.getOnPrevHighLowCompare(dbConnection, name, 0f, 0.3f, transaction); //
//					pin.gapWithCloseTest(dbConnection, name, 1.5f, 0.4f, transaction); //
//					pin.gapWithCloseTest(dbConnection, name, 2.0f, 0.4f, transaction); //0.86, 5762
//					 pin.getOnPrevCloseCompare(dbConnection, name, 1.5f, 0.5f, transaction,min, max,gapLimitPerc,highLowGap,isOtherStrategy); //
//					 pin.getOnPrevCloseCompare(dbConnection, name, 1.5f, 0.4f, transaction,min, max,gapLimitPerc,highLowGap,isOtherStrategy); //
//					 pin.getOnPrevCloseCompare(dbConnection, name, 2.25f, 0.2f, transaction,min, max,gapLimitPerc,highLowGap,isOtherStrategy); //
//					 pin.getData(dbConnection, name, 2.4f, transaction,2f); //
//					 pin.getData(dbConnection, name, 3f,2,  transaction);//1, 4293
//					 pin.getData(dbConnection, name, 2f, 5, transaction);// 1.02, 4293
//					pin.getData(dbConnection, name, 3f, 10, transaction);// 1.02, 4293
//					pin.getData(dbConnection, name, 2.5f, 4, transaction);//
//					pin.getData(dbConnection, name, 1.8f, transaction);//
//					pin.getData(dbConnection, name, 2.5f, transaction, 2);// 
//					pin.getData(dbConnection, name, 2f, transaction, 2);//
//					pin.getTest(dbConnection, name, 1f, transaction, min, max,gapLimitPerc, highLowGap,isOtherStrategy);//
					pin.getNiftyTest(dbConnection, name, 0.8f, 0.5f, transaction, min, max);
//					pin.getOpenLowSameOrOpenHighSame(dbConnection, name, 4f, 5f, transaction,9);//
				}
			}
			String filter = " ";//and year(date)=2018
			isDrawDownCalculate=true;
			if(isDrawDownCalculate==true){
				for(int y=2015; y<=2018; y++){
					for(int m=1; m<=12; m++){
						pin.calcProfit(dbConnection, capital, 3f, y, m , isDrawDownCalculate, "");
					}
				}
			}else{
				pin.calcProfit(dbConnection, capital, 3f, 2015, 2 , isDrawDownCalculate, filter);
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
