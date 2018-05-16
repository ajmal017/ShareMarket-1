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

public class GapModifiedTest extends Connection {
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
			long transaction, float min, float max, int year) {
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
					+ "  and a.intraday3Min3_18_Close is not null and a.intradayOpen is not null and year(a.tradedate)="+year+"";
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalQty"));
				intraOpen.add(rs.getFloat("a.intradayOpen"));
				intraFirst3MinClose.add(rs.getFloat("a.intradayFirst3MinClose"));
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
//				float trigger = (float) intraFirst3MinClose.get(i);
				if ((trigger - (float) high.get(i - 1)) * 100 / (float) high.get(i - 1) > gapPerc
						&& (trigger - (float) high.get(i - 1)) * 100 / (float) high.get(i - 1) < gapLimitPerc)
				{
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > min && 
							trigger < max && (float) open.get(i - 1) != (float) close.get(i - 1);
					// && (float)open.get(i)>(float)close.get(i-1) ;
					// if(((float)open.get(i-1)==(float)high.get(i-1)))
//					if (((float) open.get(i - 1) - (float) close.get(i - 1)) * 100
//							/ (float) open.get(i - 1) > prevBodyGap && filter)
					// if(((float)high.get(i-1)-(float)close.get(i-1))*100/(float)close.get(i-1)
					// > 2 )
					if(filter==true)
					{
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float)intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = (trigger - (float) high.get(i)) * 100 / trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraOpen.get(i);
//				trigger = (float) intraFirst3MinClose.get(i);
				if (((float) low.get(i - 1) - trigger) * 100 / trigger > gapPerc
						&& ((float) low.get(i - 1) - trigger) * 100 / trigger < gapLimitPerc)
				{
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > min && 
							trigger < max && (float) open.get(i - 1) != (float) close.get(i - 1);
					// && (float)open.get(i)>(float)close.get(i-1);
					// if(((float)open.get(i-1)==(float)low.get(i-1)))
//					if (((float) close.get(i - 1) - (float) open.get(i - 1)) * 100
//							/ (float) open.get(i - 1) > prevBodyGap && filter)
					// if(((float)close.get(i-1)-(float)low.get(i-1))*100/(float)close.get(i-1)
					// > 2)
					if(filter==true)
					{
						trigger = ((float) intraOpen.get(i) + (float) intraOpen.get(i) * .1f / 100);
						trigger = (float)intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						stopLoss = ((float) low.get(i) - trigger) * 100 / (float) trigger;
						// percProfitAtClosePrice = (stopLoss < SLPerc)? SLPerc
						// : percProfitAtClosePrice;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull', " + trigger + ", " + percProfitAtClosePrice + ", "
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
			long transaction,float min, float max, int year) {
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
					+ " and a.intraday3Min3_18_Close is not null  and a.intradayOpen is not null and year(a.tradedate)="+year+"";
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalQty"));
				intraOpen.add(rs.getFloat("a.intradayOpen"));
				intraFirst3MinClose.add(rs.getFloat("a.intradayFirst3MinClose"));
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
			for (int i = 5; i < tradedate.size()-1; i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)intraClose.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				float trigger = (float) intraOpen.get(i);
//				float trigger = (float) intraFirst3MinClose.get(i);
				if ((trigger - (float) close.get(i - 1)) * 100 / (float) close.get(i - 1) > gapPerc
						&& (trigger - (float) close.get(i - 1)) * 100 / (float) close.get(i - 1) < gapLimitPerc
						)
				{
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > min && 
							trigger < max && (float) close.get(i - 1) < (float)open.get(i - 1) 
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					 
					if (filter==true)
					{
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = (trigger - (float) high.get(i)) * 100 / trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
//				trigger = (float) intraFirst3MinClose.get(i);
				trigger = (float) intraOpen.get(i);
				if (((float) close.get(i - 1) - trigger) * 100 / trigger > gapPerc
						&& ((float) close.get(i - 1) - trigger) * 100 / trigger < gapLimitPerc
						)
				{
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > min && 
							trigger < max && (float) close.get(i - 1) > (float)open.get(i - 1)
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					if (filter==true)
					{
						trigger = ((float) intraOpen.get(i) + (float) intraOpen.get(i) * .1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
//						percProfitAtHighPrice = ((float) intraFirst3MinClose.get(i+1) - trigger) * 100 / (float) trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull', " + trigger + ", " + percProfitAtClosePrice + ", "
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
			long transaction) {
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
				intraOpen.add(rs.getFloat("a.intradayOpen"));
				intraFirst3MinClose.add(rs.getFloat("a.intradayFirst3MinClose"));
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
				float day1, day2, day3;
				day1 = ((float)high.get(i-1)-(float)open.get(i-1))*100/(float)open.get(i-1);
				day2 = ((float)open.get(i-1)-(float)close.get(i-1))*100/(float)open.get(i-1);
				if ((float)open.get(i-1) > (float)close.get(i-1) && day2 < gapPerc && day1 > gapPerc*prevBodyGap)
				{
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > 10 && 
							trigger < 5000 ;
//					if (((float) open.get(i - 1) - (float) close.get(i - 1)) * 100
//							/ (float) open.get(i - 1) > prevBodyGap && filter)
					{
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float)intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				day1 = ((float)open.get(i-1)-(float)low.get(i-1))*100/(float)low.get(i-1);
				day2 = ((float)close.get(i-1)-(float)open.get(i-1))*100/(float)open.get(i-1);
				if ((float)open.get(i-1) < (float)close.get(i-1) && day2 < gapPerc && day1 > gapPerc*prevBodyGap)
				{
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > 10 && 
							trigger < 5000 ;
//					if (((float) close.get(i - 1) - (float) open.get(i - 1)) * 100
//							/ (float) open.get(i - 1) > prevBodyGap && filter)
					{
						trigger = ((float) intraOpen.get(i) + (float) intraOpen.get(i) * .1f / 100);
						trigger = (float)intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	public void getOnPrevCloseCompare(java.sql.Connection con, String name, float gapPerc, float prevBodyGap,
			long transaction,float min, float max, int year) {
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
					+ " and a.intraday3Min3_18_Close is not null  and a.intradayOpen is not null and year(a.tradedate)="+year+"";
			
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("a.tradedate"));
				open.add(rs.getFloat("a.open"));
				high.add(rs.getFloat("a.high"));
				low.add(rs.getFloat("a.low"));
				close.add(rs.getFloat("a.close"));
				totalVol.add(rs.getLong("a.volume"));
				totalQty.add(rs.getLong("a.totalQty"));
				intraOpen.add(rs.getFloat("a.intradayOpen"));
				intraFirst3MinClose.add(rs.getFloat("a.intradayFirst3MinClose"));
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
				if ((trigger - (float) close.get(i - 1)) * 100 / (float) close.get(i - 1) > gapPerc
						&& (trigger - (float) close.get(i - 1)) * 100
								/ (float) close.get(i - 1) < gapLimitPerc) 
				{
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > min && 
							trigger < max && (float) open.get(i - 1) != (float) close.get(i - 1);
					// && (float)open.get(i)>(float)close.get(i-1) ;
					// if(((float)open.get(i-1)==(float)high.get(i-1)))
					if (((float) open.get(i - 1) - (float) close.get(i - 1)) * 100
							/ (float) open.get(i - 1) > prevBodyGap && filter)
					// if(((float)high.get(i-1)-(float)close.get(i-1))*100/(float)close.get(i-1)
					// > 2 )
					{
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / trigger;
						stopLoss = (trigger - (float) high.get(i)) * 100 / trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraOpen.get(i);
				if (((float) close.get(i - 1) - trigger) * 100 / trigger > gapPerc
						&& ((float) close.get(i - 1) - trigger) * 100
								/ trigger < gapLimitPerc) 
				{
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger > min && 
							trigger < max && (float) open.get(i - 1) != (float) close.get(i - 1);
					// && (float)open.get(i)>(float)close.get(i-1);
					// if(((float)open.get(i-1)==(float)low.get(i-1)))
					if (((float) close.get(i - 1) - (float) open.get(i - 1)) * 100
							/ (float) open.get(i - 1) > prevBodyGap && filter)
					// if(((float)close.get(i-1)-(float)low.get(i-1))*100/(float)close.get(i-1)
					// > 2)
					{
						trigger = ((float) intraOpen.get(i) + (float) intraOpen.get(i) * .1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
//						percProfitAtHighPrice = ((float) intraFirst3MinClose.get(i+1) - trigger) * 100 / (float) trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull', " + trigger + ", " + percProfitAtClosePrice + ", "
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
			float min, float max, int year){
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
					+ " and a.intraday3Min3_18_Close is not null  and a.intradayOpen is not null and year(a.tradedate)="+year+"";
			
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
				intraOpen.add(rs.getFloat("a.intradayOpen"));
				intraFirst3MinClose.add(rs.getFloat("a.intradayFirst3MinClose"));
				intraFirstHrClose.add(rs.getFloat("a.intradayFirstHrClose"));
				intraFC.add(rs.getFloat("a.intraFirstMinClose"));
				intraSC.add(rs.getFloat("a.intraSecondMinClose"));
				intraClose.add(rs.getFloat("a.intradayClose"));
				intraday3Min3_18_Close.add(rs.getFloat("a.intraday3Min3_18_Close"));
				intradayAt3_15.add(rs.getFloat("a.intradayAt3_15"));
			}
			long avgMinQty = 0l;
			float trig = 0f, prev1, prev2, prev3, prev4, width;
			float gapLimitPerc = 10f;
			int iter = 1;
			String out = "";
			float stopLossPerc = 1f, targetPerc = 1f, SLPerc = -7;
			for (int i = 5; i < tradedate.size()-1; i++) {
				float diff = 0f, stopLoss = 0f;
				float closeOnSquareoff = (float)intraClose.get(i);
				
				float trigger = (float) intraOpen.get(i);
//				float trigger = (float) intraFirst3MinClose.get(i);
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				if ((trigger - (float) close.get(i - 1)) * 100 / (float) close.get(i - 1) > gapPerc
						&& (trigger - (float) close.get(i - 1)) * 100
								/ (float) close.get(i - 1) < gapLimitPerc) {
					
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1);

					// &&
					// ((float)open.get(i-1)-(float)close.get(i-1))*100/(float)open.get(i-1)
					// > prevBodyGap;
					
					float fib = (float) high.get(i - 1) - ((float) high.get(i - 1) - (float) low.get(i - 1)) / 2;
					if (trigger > fib && filter) {
//						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / (float) trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						stopLoss = (trigger - (float) high.get(i)) * 100 / (float) trigger;
						// percProfitAtClosePrice = (stopLoss < SLPerc)? SLPerc
						// : percProfitAtClosePrice;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				trigger = (float) intraFirst3MinClose.get(i);
				trigger = (float) intraOpen.get(i);
				if (((float) close.get(i - 1) - trigger) * 100 / trigger > gapPerc
						&& ((float) close.get(i - 1) - trigger) * 100
								/ trigger < gapLimitPerc) {
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger < max && trigger > min
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					// &&
					// ((float)close.get(i-1)-(float)open.get(i-1))*100/(float)open.get(i-1)
					// > prevBodyGap;
					//
					float fib = (float) low.get(i - 1) + ((float) high.get(i - 1) - (float) low.get(i - 1)) / 2;
					if (trigger < fib && filter) {
//						trigger = ((float) intraOpen.get(i) + (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float) intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
//						percProfitAtHighPrice = ((float) intraFirst3MinClose.get(i+1) - trigger) * 100 / (float) trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getDataStretegy(java.sql.Connection con, String name, float gapPerc, float prevBodyGap,
			long transaction) {
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
					+ " and a.intraday3Min3_18_Close is not null  and a.intradayOpen is not null ";
			
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
				intraOpen.add(rs.getFloat("a.intradayOpen"));
				intraFirst3MinClose.add(rs.getFloat("a.intradayFirst3MinClose"));
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
				float trigger = (float) intraOpen.get(i);
				
				float avgVol = ((long) totalVol.get(i - 1) + (long) totalVol.get(i - 2) + (long) totalVol.get(i - 3)
						+ (long) totalVol.get(i - 4) + (long) totalVol.get(i - 5)) / 5;
				float avgQty = ((long) totalQty.get(i - 1) + (long) totalQty.get(i - 2) + (long) totalQty.get(i - 3)
						+ (long) totalQty.get(i - 4) + (long) totalQty.get(i - 5)) / 5;
				if ((float)low.get(i-1)==(float)low.get(i-2)) {
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger < 5000
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					if (filter) {
					    trigger = ((float) intraOpen.get(i) + (float) intraOpen.get(i) * 0.1f / 100);
					    trigger = (float)intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (closeOnSquareoff - trigger) * 100 / (float) trigger;
						percProfitAtHighPrice = ((float) intraday3Min3_18_Close.get(i) - trigger) * 100 / (float) trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtHighPrice + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
					}
				}
				
				if ((float)high.get(i-1)==(float)high.get(i-2)) {
					boolean filter = avgVol > transaction && avgQty > avgMinQty && trigger < 5000
							&& (float) open.get(i - 1) != (float) close.get(i - 1);
					if (filter) {
						trigger = ((float) intraOpen.get(i) - (float) intraOpen.get(i) * 0.1f / 100);
						trigger = (float)intraFirst3MinClose.get(i);
						percProfitAtClosePrice = (trigger - closeOnSquareoff) * 100 / (float) trigger;
						percProfitAtLowPrice = (trigger - (float) intraday3Min3_18_Close.get(i)) * 100 / (float) trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear', " + trigger + ", " + percProfitAtClosePrice + ", "
								+ percProfitAtLowPrice + ", '" + tradedate.get(i) + "')";
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
	
	public void calcProfit(java.sql.Connection con, float capital, float multiplier, int year, int month, boolean isDrawDownCalculate) throws SQLException {
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
					+ " sum(triggerPrice),date from williamsResults group by date order by date asc");
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
		GapModifiedTest pin = new GapModifiedTest();
		String sql = "SELECT s.name as symbol FROM symbols s where volume > '5000000' "
				+ " order by convert(totalTrades, SIGNED INTEGER) desc";
		pin.test(2015, sql);
		for (int i=2015; i<2019; i++){
			sql ="select distinct(s.name) symbol from williamsResults w inner join symbols s on s.name=w.name where volume > '5000000' and year(date)="+i+" and isMargin='0' group by s.name having sum(profitRupees)/count(*) > 0.0";
//			pin.test(i+1, sql);
			sql ="select distinct(s.name) symbol from williamsResults w inner join symbols s on s.name=w.name where volume > '5000000' and year(date)="+i+" and isMargin='1' group by s.name having sum(profitRupees)/count(*) > 0.0";
			pin.test(i+1, sql);
		}
	}
	public void test(int year, String sql){
		Test t = new Test();
		GapModifiedTest pin = new GapModifiedTest();
		java.sql.Connection dbConnection = null;
		boolean updateSymbolsTableData = true;
		boolean updateAllData = true;
		try {
			Connection con = new Connection();
			dbConnection = con.getDbConnection();
			ResultSet rs = null;
			rs = con.executeSelectSqlQuery(dbConnection, sql);
			String name = "";
			boolean updateForTodayAndNextDay = true;
			boolean updateForallDays = true;
			boolean updateResultTable = false;
			boolean isIntraDayData = false;
			boolean insertAllDataToResult = false;float capital=100000f;boolean isDrawDownCalculate = false;
			String iter = "1d";
			String path = "C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
			long transaction = 50000000;float min=20, max=5000;
			if(1==1)
			{
				while (rs.next()) {
					name = rs.getString("symbol");
					System.out.println(name);
					if (!iter.equals("1d"))
						name = name + "_" + iter + "";
					pin.getOnPrevHighLowCompare(dbConnection, name, 1.0f, transaction, min, max,year); //
					pin.gapWithCloseTest(dbConnection, name, 3.0f, transaction, min, max,year); // 1.33, 1027--
					pin.getOnPrevCloseCompare(dbConnection, name, 3.0f, -4f, transaction, min, max,year); //1.36, 2013--
					pin.getData(dbConnection, name, 1.5f, transaction, min, max,year);//--
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
