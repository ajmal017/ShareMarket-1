package Strategies.AboveMA;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import Indicators.Connection;
import Indicators.Test;

public class PriceAboveMA_UsingCCI extends Connection {
	float total=0;

	static boolean isBodyCompare=false, justGapStrategy=false;
	static String openOrIntra="intraFirstMinOpen";//intraFirstMinOpen
	static String closeOrIntraClose="intraday3Min3_18_close";
	static String date1="2019-01-01", date2="2019-01-01";
	static float gap=0.5f, sl=-1.5f;int div=100;
	public void getDataNiftyBankNifty(java.sql.Connection con, String name, String ndate, float nopen, float nhigh,
			float nlow, float nclose) {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();

		String date = "", sql = "";
		String time = "13:00:00", time2 = "15:30:00";
		try {
			rs = executeSelectSqlQuery(con,
					"select tradedate,open,high,low,close " + "  from `" + name + "` "
							+ "where date(tradedate) = date('" + ndate + "') and time(tradedate) > '" + time + "'"
							+ " and time(tradedate) <= '" + time2 + "' order by tradedate");
			while (rs.next()) {
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
				high.add(rs.getFloat("high"));
				low.add(rs.getFloat("low"));
				close.add(rs.getFloat("close"));
			}
			float trig = 0f;
			boolean isSL = false;
			float profit = 0f, target = 100f, gap = 0f;
			int point = 100;
			int count = 0, addPoint = 100;
			float highest = 0f, lowest = 9999999f, closeCandle = 0f;
			sql = "select max(high) from `" + name + "` where date(tradedate) = date('" + ndate + "')"
					+ " and time(tradedate) <= '" + time + "'";
			highest = Float.parseFloat(executeCountQuery(con, sql));
			sql = "select min(low) from `" + name + "` where date(tradedate) = date('" + ndate + "')"
					+ " and time(tradedate) <= '" + time + "'";
			lowest = Float.parseFloat(executeCountQuery(con, sql));
			int sl = -3;
			boolean isHighHit = false, isLowHit = false;
			for (int i = 0; i < tradedate.size() - 1; i++) {
				if (high.get(i) > highest && !isHighHit) {
					isHighHit = true;
					for (int j = i + 1; j < tradedate.size() - 13; j++) {
						if ((low.get(j) - highest) < sl) {
							isSL = true;
							profit = sl;
							sql = "insert into williamsresults2(name, reversal, profitPerc,profitRupees,triggerPrice, date, dateexited) "
									+ " values ('" + name + "', 'BullSL', " + profit + ", " + highest + ", " + ""
									+ (highest - close.get(i)) + ", '" + tradedate.get(j) + "', '" + tradedate.get(j)
									+ "')";
							executeSqlQuery(con, sql);
							return;
						}
					}
					if (!isSL) {
						profit = (close.get(tradedate.size() - 13) - highest);
						if (profit < sl)
							profit = sl;
						sql = "insert into williamsresults2(name, reversal, profitPerc,profitRupees,triggerPrice, date, dateexited) "
								+ " values ('" + name + "', 'Bull', " + profit + ", " + highest + ", " + ""
								+ (highest - close.get(i)) + ", '" + tradedate.get(i) + "', '" + tradedate.get(i)
								+ "')";
						executeSqlQuery(con, sql);
						break;
					}
				}
				if (low.get(i) < lowest && !isLowHit) {
					isLowHit = true;
					for (int j = i + 1; j < tradedate.size() - 13; j++) {
						if ((lowest - high.get(j)) < sl) {
							isSL = true;
							profit = sl;
							sql = "insert into williamsresults2(name, reversal, profitPerc,profitRupees,triggerPrice, date, dateexited) "
									+ " values ('" + name + "', 'BearSL', " + profit + ", " + lowest + ", " + ""
									+ (close.get(i) - lowest) + ", '" + tradedate.get(j) + "', '" + tradedate.get(j)
									+ "')";
							executeSqlQuery(con, sql);
							return;
						}
					}
					if (!isSL) {
						profit = (lowest - close.get(tradedate.size() - 13));
						if (profit < sl)
							profit = sl;
						sql = "insert into williamsresults2(name, reversal, profitPerc,profitRupees,triggerPrice, date, dateexited) "
								+ " values ('" + name + "', 'Bear', " + profit + ", " + lowest + ", " + ""
								+ (close.get(i) - lowest) + ", '" + tradedate.get(i) + "', '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getDataSymbols(java.sql.Connection con, String name, String ndate, float nopen, float nhigh, float nlow,
			float nclose) {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();

		String date = "", sql = "";
		String time = "09:15:00", time2 = "15:18:00";
		try {
			float o = Float.parseFloat(executeCountQuery(con,
					"select close " + "  from `" + name + "` " + "where date(tradedate) = date('" + ndate
							+ "') and time(tradedate) > '" + time + "'" + " and time(tradedate) <= '" + time2
							+ "' order by tradedate limit 1"));

			float c = Float.parseFloat(executeCountQuery(con,
					"select close " + "  from `" + name + "` " + "where date(tradedate) = date('" + ndate
							+ "') and time(tradedate) > '" + time + "'" + " and time(tradedate) <= '" + time2
							+ "' order by tradedate desc limit 1"));
			float profit = c - o;
			sql = "insert into williamsresults_new(name, reversal, profitPerc,profitRupees,triggerPrice, date, dateexited) "
					+ " values ('" + name + "', 'BullSL', " + profit + ", " + o + ", " + "" + c + ", '" + ndate + "', '"
					+ ndate + "')";
			executeSqlQuery(con, sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getDataCCI(java.sql.Connection con, String name, String ndate, float nopen, float nhigh, float nlow,
			float nclose) {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Float> cci = new ArrayList<Float>();

		String date = "", sql = "";
		String time = "09:15:00", time2 = "15:30:00";
		try {
			rs = executeSelectSqlQuery(con,
					"select tradedate,open,high,low,close,cci " + "  from `" + name + "` "
							+ "where date(tradedate) = date('" + ndate + "') and time(tradedate) > '" + time + "'"
							+ " and time(tradedate) <= '" + time2 + "' order by tradedate");
			while (rs.next()) {
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
				high.add(rs.getFloat("high"));
				low.add(rs.getFloat("low"));
				close.add(rs.getFloat("close"));
				cci.add(rs.getFloat("cci"));
			}
			float trig = 0f;
			boolean isSL = false;
			float profit = 0f, target = 100f, gap = 0f;
			nhigh = nhigh + 2;
			nlow = nlow - 2;
			int point = 100;
			int count = 0, addPoint = 100;
			int sl = -5;
			for (int i = 1; i < tradedate.size() - 13; i++) {
				if (cci.get(i) > 100 && cci.get(i - 1) < 100) {
					profit = (close.get(i + 10) - open.get(i + 1));
					sql = "insert into williamsresults2(name, reversal, profitPerc,profitRupees,triggerPrice, date, dateexited) "
							+ " values ('" + name + "', 'BullSL', " + profit + ", " + profit + ", " + "" + low.get(i)
							+ ", '" + tradedate.get(i) + "', '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getDataHourCrossTest(java.sql.Connection con, String name, String ndate, float nopen, float nhigh,
			float nlow, float nclose) throws SQLException {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();

		String date = "", sql = "";
		try {
			rs = executeSelectSqlQuery(con, "select tradedate,open,high,low,close " + "  from `" + name + "` "
					+ "where date(tradedate) = date('" + ndate + "') order by tradedate");
			while (rs.next()) {
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
				high.add(rs.getFloat("high"));
				low.add(rs.getFloat("low"));
				close.add(rs.getFloat("close"));
			}
			float trig = 0f;
			boolean isSL = false;
			float profit = 0f, target = 100f, gap = 0f;
			nhigh = nhigh + 2;

			nlow = nlow - 2;
			int point = 100;
			int count = 0, addPoint = 100;
			float profitAtClose = 0f, lowest = 9999999f, closeCandle = 0f;
			int sl = -5;
			int dif = 100;
			for (int i = 1; i < tradedate.size() - 1; i++) {
				float width = (open.get(i - 1) + close.get(i - 1)) / 2;
				if (open.get(i) < width && high.get(i) > width) {
					profit = high.get(i) - width;
					profitAtClose = close.get(i) - (high.get(i));
					sql = "insert into williamsresults2(name, reversal, profitPerc,profitRupees,triggerPrice, date, dateexited) "
							+ " values ('" + name + "', 'BullSL', " + profitAtClose + ", " + profit + ", " + ""
							+ high.get(i - 1) + ", '" + tradedate.get(i) + "', '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
				if (open.get(i) > width && low.get(i) < width) {
					profit = width - low.get(i);
					profitAtClose = (width - close.get(i));
					sql = "insert into williamsresults2(name, reversal, profitPerc,profitRupees,triggerPrice, date, dateexited) "
							+ " values ('" + name + "', 'BearSL', " + profitAtClose + ", " + profit + ", " + ""
							+ low.get(i - 1) + ", '" + tradedate.get(i) + "', '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testProfit(java.sql.Connection con) throws SQLException {
		String y = "2014", m = "01";
		String d = "" + y + "-" + m + "-01";
		executeSqlQuery(con, "truncate table psarresults");
		String sql = "select * from williamsresults2 where name like '%bank%' and date>='" + d + "'" + " order by date";
		ResultSet rs = executeSelectSqlQuery(con, sql);
		List<Float> profitInPoints = new ArrayList<>();
		List<Float> profitRupees = new ArrayList<>();
		List<String> profitDate = new ArrayList<>();
		while (rs.next()) {
			profitInPoints.add(rs.getFloat("profitPerc"));
			profitRupees.add(rs.getFloat("profitRupees"));
			profitDate.add(rs.getString("date"));
		}
		float initCapital = 20000f;
		int lot = 40;
		int margin = 66;

		for (int i = 0; i < profitInPoints.size(); i++) {
			float thatDaysPrice = Float.parseFloat(executeCountQuery(con,
					"select close from bankNIFTY_50_1 " + " where tradedate='" + profitDate.get(i) + "'"));
			float p = (int) (initCapital / (thatDaysPrice * lot / margin)) * profitInPoints.get(i) * lot;
			if (initCapital > 0)
				initCapital = initCapital - initCapital * 1 / 100;
			else
				initCapital = initCapital - (initCapital * 1 / 100 * -1);
			initCapital += p;
			sql = "Insert into psarResults(date, profitPerc, profitRupees) values " + "('" + profitDate.get(i) + "', '"
					+ initCapital + "', '" + profitInPoints.get(i) + "')";
			executeSqlQuery(con, sql);
		}
	}

	public void checkCross(java.sql.Connection con, String name) throws SQLException {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Long> volume = new ArrayList<Long>();
		try{
			String sql = "select tradedate,open,high,low,close,volume from `"+name+"_3` where tradedate>'2018-05-01'";
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
				high.add(rs.getFloat("high"));
				low.add(rs.getFloat("low"));
				close.add(rs.getFloat("close"));
				volume.add(rs.getLong("volume"));
			}
			long sum=0;
			for(int i=100; i< tradedate.size(); i++)
			{
				sum=0;
				for(int j=i-100; j< i; j++){
					sum+=volume.get(j);
				}
				if(volume.get(i) > sum){
					System.out.println(name+","+tradedate.get(i)+", diff="+(close.get(i)-close.get(i+1))*100/close.get(i));
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void checkGap3Min(java.sql.Connection con, String name) throws SQLException {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> intradayOpen = new ArrayList<Float>();
		gap=0.8f;
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Float> intraDayFirst3MinClose = new ArrayList<Float>();
		List<Float> intraDay3_18Close = new ArrayList<Float>();
		List<Long> totalQty = new ArrayList<Long>();
		List<Long> totalTrades = new ArrayList<Long>();
		List<Long> volume = new ArrayList<Long>();
		float intraHigh, intraLow;
		int div=100;
		boolean isBodyCompare=false, justGapStrategy=false;
		String openOrIntra="intradayOpen";//intradayOpen
		try{
			String sql = "select tradedate,open,volume,high,low,totalTrades,intradayOpen,"+openOrIntra+",intraday3Min3_18_close,"
					+ " intradayFirst3MinClose,intraFirstMinClose, "
					+ "close,TotalQty from `"+name+"` where tradedate>'2015-01-01'"
							+ " and intradayFirst3MinClose<>0 and intraday3Min3_18_close<>0"
							+ " and intradayHigh<>0 and intradayLow<>0 and "+openOrIntra+"<>0";
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
			}
			long sum=0;
			for(int i=7; i< tradedate.size()-1; i++)
			{
				boolean isQtyGood = true;
				
				float avgVol = ((long) volume.get(i - 1) + (long) volume.get(i - 2) + (long) volume.get(i - 3)
						+ (long) volume.get(i - 4) + (long) volume.get(i - 5)) / 5;
				float avgTotalTrades = ((long) totalTrades.get(i - 1) + (long) totalTrades.get(i - 2) + (long) totalTrades.get(i - 3)
						+ (long) totalTrades.get(i - 4) + (long) totalTrades.get(i - 5)) / 5;
				isQtyGood = avgTotalTrades>7000 && avgVol>100000000;
				float actualGap=(close.get(i-1)-open.get(i))*100/open.get(i);
				if( actualGap > gap && isQtyGood)
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
									+ " values ('" + name + "', 'Bull', " + actualGap + ", " + total + ", "
									+ total + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
						}
					}
				}
				actualGap =(open.get(i)-close.get(i-1))*100/open.get(i); 
				if( actualGap> gap && isQtyGood)
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
									+ " values ('" + name + "', 'Bear', " + actualGap + ", " + total + ", "
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

	public void checkGap1Min(java.sql.Connection con, String name) {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> intradayOpen = new ArrayList<Float>();
		
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Float> intradayClose = new ArrayList<Float>();
		List<Float> intraDayFirst3MinClose = new ArrayList<Float>();
		
		List<Float> intraFirstMinOpen = new ArrayList<Float>();
		List<Float> intraFirstMinHigh = new ArrayList<Float>();
		List<Float> intraFirstMinLow = new ArrayList<Float>();
		List<Float> intraFirstMinClose = new ArrayList<Float>();
		List<Float> intraSecondMinClose = new ArrayList<Float>();
		
		List<Float> intraDay3Min09_18_Close = new ArrayList<Float>();
		List<Float> intraDay3_18Close = new ArrayList<Float>();
		List<Float> intraAfter09_16_High = new ArrayList<Float>();
		List<Float> intraAfter09_16_Low = new ArrayList<Float>();
		
		List<Long> totalQty = new ArrayList<Long>();
		List<Long> totalTrades = new ArrayList<Long>();
		List<Long> volume = new ArrayList<Long>();
		List<Long> intraFirstMinVolume = new ArrayList<Long>();
		float intraHigh=0f, intraLow=0f;
		String dir="";
		
		float slPercToCompareBeyondOpen=0.05f;
		try{
			String sql = "select tradedate,open,volume,high,low,intraFirstMinVolume,intraFirstMinOpen,intraAfter09_16_High,intraAfter09_16_Low,"
					+ "intraFirstMinHigh,intraFirstMinLow,intraAfter09_15_High,intraAfter09_15_Low,totalTrades,intradayOpen,"+openOrIntra+",intraday3Min3_18_close,"
					+ " intradayFirst3MinClose,intraFirstMinClose, intradayClose, intraSecondMinClose,intraday3Min09_18_close,"
					+ "close,TotalQty from `"+name+"` where tradedate>='"+date1+"' and tradedate<'"+date2+"' "
							+ " and intraday3Min3_18_close<>0 and intraAfter09_15_Low<>0  and intraAfter09_15_High<>0 "
							+ " and intraFirstMinVolume<> 0 and intraFirstMinOpen<>0 and "
//							+ " abs(intraFirstMinClose-intraday3Min3_18_close)*100/intraday3Min3_18_close < 30 and "
							+ " abs(intraFirstMinOpen-open)*100/open < 5 "
							+ " and "+openOrIntra+"<>0 and open>20 and open<30000 and intradayClose<>0 ";
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat(openOrIntra));
				high.add(rs.getFloat("high"));
				low.add(rs.getFloat("low"));
				close.add(rs.getFloat(closeOrIntraClose));
				intradayClose.add(rs.getFloat("intradayClose"));
				intraDayFirst3MinClose.add(rs.getFloat("intradayFirst3MinClose"));
				intraFirstMinOpen.add(rs.getFloat("intraFirstMinOpen"));
				intraFirstMinHigh.add(rs.getFloat("intraFirstMinHigh"));
				intraFirstMinLow.add(rs.getFloat("intraFirstMinLow"));
				intraFirstMinClose.add(rs.getFloat("intraFirstMinClose"));
				intraSecondMinClose.add(rs.getFloat("intraFirstMinClose"));//intraFirstMinClose
				intraDay3_18Close.add(rs.getFloat("intraday3Min3_18_close"));
				intraAfter09_16_High.add(rs.getFloat("intraAfter09_15_High"));//intraAfter09_16_High
				intraAfter09_16_Low.add(rs.getFloat("intraAfter09_15_Low"));//intraAfter09_16_Low
				totalQty.add(rs.getLong("TotalQty"));
				volume.add(rs.getLong("volume"));
				totalTrades.add(rs.getLong("totalTrades"));
				intraFirstMinVolume.add(rs.getLong("intraFirstMinVolume"));
			}
			long sum=0;
			for(int i=7; i< 0; i++)
//			for(int i=7; i< tradedate.size()-1; i++)
			{
				float avgVol = ((long) volume.get(i - 1) + (long) volume.get(i - 2) + (long) volume.get(i - 3)
						+ (long) volume.get(i - 4) + (long) volume.get(i - 5)) / 5;
				float avgTotalTrades = ((long) totalTrades.get(i - 1) + (long) totalTrades.get(i - 2) + (long) totalTrades.get(i - 3)
						+ (long) totalTrades.get(i - 4) + (long) totalTrades.get(i - 5)) / 5;
				boolean isQtyGood = avgTotalTrades>7000 && avgVol>100000000;
				float intraVolume=intraFirstMinVolume.get(i);
				float volAvg=0f;
				volAvg = (totalQty.get(i-1)+totalQty.get(i-2)+totalQty.get(i-3)+totalQty.get(i-4)+
						totalQty.get(i-5)+totalQty.get(i-6)+totalQty.get(i-7))/7;
				if(isQtyGood){
					
					sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bear', " + total + ", " + total + ", "
							+ total + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
			}
			for(int i=7; i< tradedate.size(); i++)
//			for(int i=7; i< 0; i++)
			{
				boolean isQtyGood = true;
				
				float avgVol = ((long) volume.get(i - 1) + (long) volume.get(i - 2) + (long) volume.get(i - 3)
						+ (long) volume.get(i - 4) + (long) volume.get(i - 5)) / 5;
				float avgTotalTrades = ((long) totalTrades.get(i - 1) + (long) totalTrades.get(i - 2) + (long) totalTrades.get(i - 3)
						+ (long) totalTrades.get(i - 4) + (long) totalTrades.get(i - 5)) / 5;
				isQtyGood = avgTotalTrades>7000 && avgVol>100000000;
				float actualGap=0f;
//				float avgClose = (close.get(i-1)+close.get(i-2))/2;
				float avgClose = close.get(i-1); //intraDay3_18Close
				actualGap=(avgClose-open.get(i))*100/open.get(i);
				if( actualGap > gap && isQtyGood)
				{
					float intraVolume=intraFirstMinVolume.get(i);
					float volAvg=0f;
					volAvg = (totalQty.get(i-1)+totalQty.get(i-2)+totalQty.get(i-3)+totalQty.get(i-4)+
							totalQty.get(i-5)+totalQty.get(i-6)+totalQty.get(i-7))/7;
					if( intraVolume> volAvg/div || justGapStrategy)
					{
						boolean filter = Float.compare(intraFirstMinLow.get(i), intraFirstMinOpen.get(i))==0;
						if(isBodyCompare){
							filter = intraFirstMinClose.get(i) > intraFirstMinOpen.get(i);
						}
							
						if(filter || justGapStrategy)
						{
							dir = "Bull_"+openOrIntra+"_"+closeOrIntraClose;
							total= (intraDay3_18Close.get(i)-intraSecondMinClose.get(i))*100/intraSecondMinClose.get(i);
							intraLow = intraAfter09_16_Low.get(i);
							intraHigh = intraAfter09_16_High.get(i);
							
							if((intraLow-intraSecondMinClose.get(i))*100/intraSecondMinClose.get(i)< sl
									|| total<=sl){
								total=sl;
							}
							/*if(intraHigh >= intraSecondMinClose.get(i) && Float.compare(total, sl)==0){
								total= (intraDay3_18Close.get(i)-intraSecondMinClose.get(i))*100/intraSecondMinClose.get(i);
								if(total<=sl) total=sl*1.5f;
								else total=total+sl;
								dir=dir+"_Again";
							}*/
							if(intraLow < intraFirstMinLow.get(i)){
//								total = (intraFirstMinLow.get(i)-intraSecondMinClose.get(i))*100/intraSecondMinClose.get(i);
							}
							/*if(Float.compare(total, sl) ==0){
								float trig = intraSecondMinClose.get(i) - (intraSecondMinClose.get(i)*(sl-0.10f)*-1)/100;
								if(intraLow <= trig){
									if((intraLow-trig)*100/trig<= sl){
										total=sl*2;
									}else{
										total= (intraDay3_18Close.get(i)-trig)*100/trig + sl;
									}
								}
							}*/
							
							/*if((intraHigh-intraSecondMinClose.get(i))*100/intraSecondMinClose.get(i) > sl*3*-1){
								total = Float.compare(total, sl) ==0 ? (sl*3*-1)+sl : sl*3*-1;
							}*/
							sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + name + "', '"+dir+"', " + actualGap + ", " + total + ", "
									+ intraSecondMinClose.get(i) + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
							
							if(close.get(i-1)>open.get(i-1) && open.get(i) < close.get(i-1) 
									&& (close.get(i-1)-open.get(i-1))*10/open.get(i-1) > 0.5){
								float max = (intraHigh - intraFirstMinClose.get(i))*100/intraFirstMinClose.get(i);
								sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
										+ " values ('" + name + "', '"+dir+"_1', " + actualGap + ", " + total + ", "
										+ max + ", '" + tradedate.get(i) + "')";
//								executeSqlQuery(con, sql);
							}
							float fib = (float) low.get(i - 1) + ((float) high.get(i - 1) - (float) low.get(i - 1)) / 2;
							if(open.get(i) < fib){
								sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
										+ " values ('" + name + "', '"+dir+"_2', " + actualGap + ", " + total + ", "
										+ intraSecondMinClose.get(i) + ", '" + tradedate.get(i) + "')";
//								executeSqlQuery(con, sql);
							}
						}
					}
				}
				
				actualGap =(open.get(i)-avgClose)*100/open.get(i);
				if( actualGap> gap && isQtyGood)
				{
					float intraVolume=intraFirstMinVolume.get(i);
					float volAvg=(totalQty.get(i-1)+totalQty.get(i-2)+totalQty.get(i-3)+totalQty.get(i-4)+
							totalQty.get(i-5)+totalQty.get(i-6)+totalQty.get(i-7))/7;
					if( intraVolume> volAvg/div || justGapStrategy)
					{
						boolean filter = Float.compare(intraFirstMinHigh.get(i), intraFirstMinOpen.get(i))==0;
						if(isBodyCompare){
							filter = intraFirstMinClose.get(i) < intraFirstMinOpen.get(i);
						}
						if(filter || justGapStrategy)
						{
							dir = "Bear_"+openOrIntra+"_"+closeOrIntraClose;
							total= (intraSecondMinClose.get(i)-intraDay3_18Close.get(i))*100/intraSecondMinClose.get(i);
							intraHigh = intraAfter09_16_High.get(i);
							intraLow = intraAfter09_16_Low.get(i);
							
							if((intraSecondMinClose.get(i)-intraHigh)*100/intraSecondMinClose.get(i)< sl
									|| total<=sl){
								total=sl;
							}
							/*if(intraLow <= intraSecondMinClose.get(i) && Float.compare(total, sl)==0){
								total= (intraSecondMinClose.get(i)-intraDay3_18Close.get(i))*100/intraSecondMinClose.get(i);
								if(total<=sl) total=sl*1.5f;
								else total=total+sl;
								dir=dir+"_Again";
							}*/
							if(intraHigh > intraFirstMinHigh.get(i)){
//								total = (intraSecondMinClose.get(i)-intraFirstMinHigh.get(i))*100/intraSecondMinClose.get(i);
							}
							/*if(Float.compare(total, sl) ==0){
								float trig = intraSecondMinClose.get(i) + (intraSecondMinClose.get(i)*(sl-0.10f)*-1)/100;
								if(intraHigh >= trig ){
									if((trig-intraHigh)*100/trig<= sl){
										total=sl*2;
									}else{
										total= (trig-intraDay3_18Close.get(i))*100/trig + sl;
									}
								}
							}*/
							/*if((intraSecondMinClose.get(i)-intraLow)*100/intraSecondMinClose.get(i) > sl*3*-1){
								total = Float.compare(total, sl) ==0 ? (sl*3*-1)+sl : sl*3*-1;
							}*/
							sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + name + "', '"+dir+"', " + actualGap + ", " + total + ", "
									+ intraSecondMinClose.get(i) + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
							
							if(close.get(i-1)<open.get(i-1) && open.get(i) > close.get(i-1)
									&& (open.get(i-1)-close.get(i-1))*10/open.get(i-1) > 0.5){
								float max = (intraFirstMinClose.get(i) - intraLow)*100/intraFirstMinClose.get(i);
								sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
										+ " values ('" + name + "', '"+dir+"_1', " + actualGap + ", " + total + ", "
										+ max + ", '" + tradedate.get(i) + "')";
//								executeSqlQuery(con, sql);
							}
							float fib = (float) high.get(i - 1) - ((float) high.get(i - 1) - (float) low.get(i - 1)) / 2;
							if(open.get(i) > fib){
								sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
										+ " values ('" + name + "', '"+dir+"_2', " + actualGap + ", " + total + ", "
										+ intraSecondMinClose.get(i) + ", '" + tradedate.get(i) + "')";
//								executeSqlQuery(con, sql);
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void highVolume(java.sql.Connection con,String name) throws SQLException{
		ResultSet rs = null;
		sl=-1.5f;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		String sql = "select tradedate,open,volume,high,low, close from `"+name+"` order by tradedate";
		rs = executeSelectSqlQuery(con, sql);
		while (rs.next()) {
			tradedate.add(rs.getString("tradedate"));
			open.add(rs.getFloat("open"));
			high.add(rs.getFloat("high"));
			low.add(rs.getFloat("low"));
			close.add(rs.getFloat("close"));
		}
		float trigger=0f, target = 5f;
		boolean isGot = false;
		float enter=3;
		for(int i=1; i< tradedate.size()-1; i++){
			if((open.get(i) - low.get(i))*100/low.get(i) > enter){
				trigger = open.get(i) - (open.get(i)*enter/100);
				/*for(int j=i+1; j< tradedate.size(); j++)
				{
					if((high.get(j) - trigger)*100/trigger > target){
						sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull', " + (j-i) + ", " + target + ", "
								+ trigger + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
						isGot=true;
						break;
					}
				}*/
				if(high.get(i+1) >high.get(i)){
					total = (high.get(i)-close.get(i+1))*100/high.get(i);
					sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bear', " + 1 + ", " + total + ", "
							+ trigger + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
			}
			
			if((high.get(i) - open.get(i))*100/open.get(i) > enter){
				trigger = open.get(i) + (open.get(i)*enter/100);
				/*for(int j=i+1; j< tradedate.size(); j++)
				{
					if((high.get(j) - trigger)*100/trigger > target){
						sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull', " + (j-i) + ", " + target + ", "
								+ trigger + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
						isGot=true;
						break;
					}
				}*/
				if(low.get(i+1) <low.get(i)){
					total = (close.get(i+1)-low.get(i))*100/low.get(i);
					sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
							+ " values ('" + name + "', 'Bull', " + 1 + ", " + total + ", "
							+ trigger + ", '" + tradedate.get(i) + "')";
					executeSqlQuery(con, sql);
				}
			}
		}
	}
	public void hourlyCross(java.sql.Connection con,String name) throws SQLException{
		List<String> tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Float> cci = new ArrayList<Float>();
		String sql = "select tradedate,replace(open,'-','') open,replace(high,'-','') high,replace(low,'-','') low, replace(close,'-','') close,cci from `"+name+"_3` "
				+ " where tradedate<>'0000-00-00 00:00:00' and (tradedate)>='2019-03-10' order by tradedate";
		ResultSet rs;
		try {
			rs = executeSelectSqlQuery(con, sql);
			if(rs!=null){
				while (rs.next()) {
					try{
						tradedate.add(rs.getString("tradedate"));
						open.add(rs.getFloat("open"));
						high.add(rs.getFloat("high"));
						low.add(rs.getFloat("low"));
						close.add(rs.getFloat("close"));
					}
					catch(Exception e){
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			float target=10f, trigger=0f;
			float slPerc=2f;float h=0,l=0, width=0;
			String d="";boolean bullHit=false,bearHit=false,enter=false;
			float g1temp=0;
			float max=0,min=0;
			float opp = 0f;
			
			for(int i=4; i< tradedate.size()-3; i++){
				float g1 = Math.abs(close.get(i-1)-open.get(i))*100/close.get(i-1);
				if(tradedate.get(i).contains("09:15:00")){
					sql="SELECT avg(volume)*1 from 	`"+name+"` where tradedate < '"+tradedate.get(i)+"'"
							+ " and tradedate > '"+tradedate.get(i)+"' - INTERVAL 5 DAY;";
					String avg = executeCountQuery(con, sql);
					if(avg != null){
						Float avgVolume = Float.parseFloat(avg);
						if(avgVolume < 100000000) continue;
					}else continue;
					
					
					h=high.get(i);l=low.get(i);
					h = (float) (h-(h*0.0/100));
					l = (float) (l+(l*0.0/100));
					d=tradedate.get(i).split(" ")[0];
					bullHit=false;bearHit=false;
					width = (h-l)*100/l;
					if((h-l)*100/l >20){
						enter = false;
					}
					else{
						enter = true;
					}
					max=h; min=l;
					try{
						sql = "select open from `"+name+"` where date(tradedate)=date('"+tradedate.get(i)+"')";
						float dayOpen = Float.parseFloat(executeCountQuery(con, sql));
						sql = "select close from `"+name+"` where date(tradedate)=date('"+tradedate.get(i-1)+"')";
						float dayClose = Float.parseFloat(executeCountQuery(con, sql));
						g1 = Math.abs(dayClose-dayOpen)*100/dayClose;
						g1temp=g1;
						if(g1 < gap){
							enter=false;bullHit=false;bearHit=false;
						}else{
							enter=true;
						}
					}catch(Exception e){
						enter=false;bullHit=false;bearHit=false;
					}
					continue;
				}
				if(tradedate.get(i).contains(d) && enter){
					max = Math.max(max, high.get(i));
					min = Math.min(min, low.get(i));
					if(high.get(i) > h && enter ){
						bullHit=true;bearHit=false;
					}

					if(low.get(i) < l && enter ){
						bearHit=true;bullHit=false;
					}
					if(bearHit){
						if(high.get(i+1) > h ){
							total=(l-h)*100/h;
							sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + name + "', 'BearC_SL', " + g1temp + ", " + total + ", "
									+ g1temp + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
							bearHit=false;enter = false;
						}
						if(bearHit && tradedate.get(i).contains("15:18:00")){
							total=(l-close.get(i))*100/l;
							float  p =(l-min)*100/l;
							sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + name + "', 'BearC', " + g1temp + ", " + total + ", "
									+ g1temp + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
							bearHit=false;enter = false;
						}
					}

					if(bullHit){
						if(low.get(i+1) < l ){
							total=(l-h)*100/h;
							sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + name + "', 'BullC_SL', " + g1temp + ", " + total + ", "
									+ g1temp + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
							bullHit=false;enter = false;
						}
						if(bullHit && tradedate.get(i).contains("15:18:00")){
							total=(close.get(i) - h)*100/h;
							float p = (max-h)*100/h;
							sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + name + "', 'BullC', " + g1temp + ", " + total + ", "
									+ g1temp + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
							bullHit=false;enter = false;
						}
					}
				}
			}
	}
	
	public void firstThreeMinClose(java.sql.Connection con,String name) throws SQLException{
		List<String> tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Float> cci = new ArrayList<Float>();
		gap = 1f;
		String sql = "select tradedate,replace(open,'-','') open,replace(high,'-','') high,replace(low,'-','') low, replace(close,'-','') close,cci from `"+name+"_3` "
				+ " where tradedate<>'0000-00-00 00:00:00' and year(tradedate)=2019 order by tradedate";
		ResultSet rs;
		try {
			rs = executeSelectSqlQuery(con, sql);
			if(rs!=null){
				while (rs.next()) {
					try{
						tradedate.add(rs.getString("tradedate"));
						open.add(rs.getFloat("open"));
						high.add(rs.getFloat("high"));
						low.add(rs.getFloat("low"));
						close.add(rs.getFloat("close"));
					}
					catch(Exception e){
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			float target=10f, trigger=0f;
			float slPerc=2f;float h=0,l=0, width=0;
			String d="";boolean bullHit=false,bearHit=false,enter=false;
			float g1temp=0;
			float max=0,min=0;
			float opp = 0f;
			
			for(int i=4; i< tradedate.size()-3; i++){
				float g1 = Math.abs(close.get(i-1)-open.get(i))*100/close.get(i-1);
				if(tradedate.get(i).contains("09:15:00") && g1>gap){
					sql="SELECT avg(volume)*1 from 	`"+name+"` where tradedate < '"+tradedate.get(i)+"'"
							+ " and tradedate > '"+tradedate.get(i)+"' - INTERVAL 5 DAY;";
					String avg = executeCountQuery(con, sql);
					if(avg != null){
						Float avgVolume = Float.parseFloat(avg);
						if(avgVolume < 100000000) continue;
					}else continue;
					
					g1temp=g1;
					h=high.get(i);l=low.get(i);
					h = (float) (h-(h*0.0/100));
					l = (float) (l+(l*0.0/100));
					d=tradedate.get(i).split(" ")[0];
					bullHit=false;bearHit=false;
					width = (h-l)*100/l;
					if((h-l)*100/l >20){
						enter = false;
					}
					else{
						enter = true;
					}
					max=h; min=l;
					trigger = close.get(i);
					if(close.get(i) > open.get(i) && enter ){
						bullHit=true;bearHit=false;
					}

					if(close.get(i) < open.get(i) && enter ){
						bearHit=true;bullHit=false;
					}
					continue;
				}
				if(tradedate.get(i).contains(d) && enter){
					max = Math.max(max, high.get(i));
					min = Math.min(min, low.get(i));
					
					if(bearHit){
						if(high.get(i+1) > h ){
							total=(trigger-h)*100/h;
							sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + name + "', 'BearC_SL', " + g1temp + ", " + total + ", "
									+ g1temp + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
							bearHit=false;enter = false;
						}
						if(bearHit && tradedate.get(i).contains("15:18:00")){
							total=(trigger-close.get(i))*100/trigger;
							sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + name + "', 'BearC', " + g1temp + ", " + total + ", "
									+ g1temp + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
							bearHit=false;enter = false;
						}
					}

					if(bullHit){
						if(low.get(i+1) < l ){
							total=(l-trigger)*100/trigger;
							sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + name + "', 'BullC_SL', " + g1temp + ", " + total + ", "
									+ g1temp + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
							bullHit=false;enter = false;
						}
						if(bullHit && tradedate.get(i).contains("15:18:00")){
							total=(close.get(i) - trigger)*100/trigger;
							sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									+ " values ('" + name + "', 'BullC', " + g1temp + ", " + total + ", "
									+ g1temp + ", '" + tradedate.get(i) + "')";
							executeSqlQuery(con, sql);
							bullHit=false;enter = false;
						}
					}
				}
			}
	}
	
	public void tooMuchChangeAndReversal(java.sql.Connection con,String name) throws SQLException{
		List<String> tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Float> cci = new ArrayList<Float>();
		String sql = "select tradedate,open,volume,high,low, close,cci from `"+name+"_3` "
				+ " where tradedate<>'0000-00-00 00:00:00' order by tradedate";
		ResultSet rs;
		try {
			rs = executeSelectSqlQuery(con, sql);
			if(rs!=null){
				while (rs.next()) {
					tradedate.add(rs.getString("tradedate"));
					open.add(rs.getFloat("open"));
					high.add(rs.getFloat("high"));
					low.add(rs.getFloat("low"));
					close.add(rs.getFloat("close"));
					cci.add(rs.getFloat("cci"));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		float trigger = 0f, target = 10f;
		float slPerc = 2f;
		float h = 0, l = 0;
		String d = "";
		boolean bullHit = false, bearHit = false, enter = false;
		float g1temp = 0;
		float max = 0, min = 0;
		for (int i = 2; i < tradedate.size() - 1; i++) {
			if(tradedate.get(i).toString().contains("09:15:00")){
				for(int j=i; j< i+10; j++){
					
				}
			}
		}
	}
	public void checkCCI(java.sql.Connection con,String name) throws SQLException{
		List<String> tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Float> cci = new ArrayList<Float>();
		String sql = "select tradedate,open,volume,high,low, close,cci from `"+name+"_60` order by tradedate";
		ResultSet rs = executeSelectSqlQuery(con, sql);
		while (rs.next()) {
			tradedate.add(rs.getString("tradedate"));
			open.add(rs.getFloat("open"));
			high.add(rs.getFloat("high"));
			low.add(rs.getFloat("low"));
			close.add(rs.getFloat("close"));
			cci.add(rs.getFloat("cci"));
		}
		float trigger=0f,target=10f;
		float slPerc=2f;
		int wait = 50;
		for(int i=1; i<tradedate.size()-wait;i++){
			if(cci.get(i) > 100 && cci.get(i-1) < 100){
				trigger = close.get(i);
				for(int j=i+1; j<i+wait;j++){

					if((trigger-low.get(j))*100/trigger > slPerc){
						sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull_SL', " + slPerc*-1 + ", " + slPerc*-1 + ", "
								+ (j-i) + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
						break;
					}
					if((high.get(j)-trigger)*100/trigger > target){
						sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull', " + target + ", " + target + ", "
								+ (j-i) + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
						break;
					}
					if(j==i+wait-2){
						float profit=(close.get(j) - trigger)*100/trigger;
						sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bull_NotHit_Any', " + profit + ", " + profit + ", "
								+ wait + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
						break;
					}
				}
			}
			if(cci.get(i) < -100 && cci.get(i-1) > -100){
				trigger = close.get(i);
				for(int j=i+1; j<i+wait;j++){

					if((high.get(j)-trigger)*100/trigger > slPerc){
						sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear_SL', " + slPerc*-1 + ", " + slPerc*-1 + ", "
								+ (j-i) + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
						break;
					}
					if((trigger-low.get(j))*100/trigger > target){
						sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear', " + target + ", " + target + ", "
								+ (j-i) + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
						break;
					}
					if(j==i+wait-2){
						float profit=(trigger-close.get(j))*100/trigger;
						sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('" + name + "', 'Bear_NotHit_Any', " + profit + ", " + profit + ", "
								+ wait + ", '" + tradedate.get(i) + "')";
						executeSqlQuery(con, sql);
						break;
					}
				}
			}
		}
		/*for(int i=1; i<tradedate.size()-100;i++){
			if(cci.get(i) > 100 && cci.get(i-1) < 100){
				trigger = close.get(i);
				float profit = (close.get(i+10)-trigger)*100/trigger;
				sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
						+ " values ('" + name + "', 'Bull', " + profit + ", " + profit + ", "
						+ (i-i) + ", '" + tradedate.get(i) + "')";
				executeSqlQuery(con, sql);
			}
			if(cci.get(i) < -100 && cci.get(i-1) > -100){
				trigger = close.get(i);
				float profit =(trigger-close.get(i+10))*100/trigger;
				sql = "insert into williamsresults2(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
						+ " values ('" + name + "', 'Bear', " + profit + ", " + profit + ", "
						+ (i-i) + ", '" + tradedate.get(i) + "')";
				executeSqlQuery(con, sql);
			}
		}*/
	}
	
	public void checkSupportResistance1Day(java.sql.Connection con, String name) throws SQLException {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> intradayOpen = new ArrayList<Float>();
		
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		float intraHigh, intraLow;
		float gap=2f, sl=-1f;int div=100;
		boolean isBodyCompare=true, justGapStrategy=true;
		boolean isCompareTouchingOpenAsSL=false;
		float slPercToCompareBeyondOpen=0.05f;
		String openOrIntra="open";//intradayOpen
		try{
			String sql = "select tradedate,open,volume,high,low, close from `"+name+"`_60 order by tradedate";
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat(openOrIntra));
				high.add(rs.getFloat("high"));
				low.add(rs.getFloat("low"));
				close.add(rs.getFloat("close"));
			}
			long sum=0;
			float highest=0f;
			for(int i=20; i< tradedate.size(); i++)
			{
				boolean valid=true;
				for(int j=i-20; j<i; j++){
					highest = Math.max(highest, high.get(j));
				}
				if(high.get(i) > highest){
					for(int j=i-10; j<i; j++){
						if(high.get(j) >= highest){
							valid = false;
						}
					}
					if(valid && low.get(i+1) > highest && low.get(i+2) > highest && low.get(i+3) > highest
							&& low.get(i+4) > highest && low.get(i+5) > highest && low.get(i+6) > highest){
						sql = "Insert into psarResults(name,date, reversal,profitPerc, profitRupees) "
								+ "values " + "('"+name+"', '" + tradedate.get(i) + "','Bull','"
								+ total + "', '" + total + "')";
						executeSqlQuery(con, sql);
					}
				}
			}
		} catch (Exception e) {
		}
	}

	public void checkPrevDayRangeCrossWithVolume(java.sql.Connection con, String name) throws SQLException {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Float> intraOpen = new ArrayList<Float>();
		List<Float> intraDayFirst3MinClose = new ArrayList<Float>();
		List<Float> intraDay3_18Close = new ArrayList<Float>();
		List<Long> volume = new ArrayList<Long>();
		float gap=0.3f, sl=-20f;
		try{
			String sql = "select tradedate,open,high,low,intradayOpen, intradayFirst3MinClose,"
					+ "intraday3Min3_18_close, close,TotalQty,intradayHigh,intradayLow from `"+name+"` where tradedate>'2015-01-01'"
							+ " and intradayFirst3MinClose<>0 and intraday3Min3_18_close<>0"
							+ " and intradayHigh<>0 and intradayLow<>0 ";
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
				high.add(rs.getFloat("high"));
				low.add(rs.getFloat("low"));
				close.add(rs.getFloat("close"));
				intraOpen.add(rs.getFloat("intradayOpen"));
				intraDayFirst3MinClose.add(rs.getFloat("intradayFirst3MinClose"));
				intraDay3_18Close.add(rs.getFloat("intraday3Min3_18_close"));
				volume.add(rs.getLong("TotalQty"));
			}
			long sum=0;
			
			for(int i=1; i< tradedate.size()-1; i++)
			{
				float prevH = (float) (high.get(i-1)-(high.get(i-1)*0/100));
				sql="select volume from `"+name+"_3` where high>"+high.get(i-1)+" "
						+ " and date(tradedate)='"+tradedate.get(i).toString().split(" ")[0]+"' order by tradedate limit 1";
//				float vol = Long.parseLong(executeCountQuery(con, sql));
				if(high.get(i) > prevH && open.get(i) < prevH
						&& Math.abs(open.get(i)-intraOpen.get(i))*100/intraOpen.get(i) < 10
						)
				{
					total= (intraDay3_18Close.get(i)-prevH)*100/prevH;
					sql = "Insert into psarResults(name,date, reversal,profitPerc, profitRupees) "
							+ "values " + "('"+name+"', '" + tradedate.get(i) + "','Bull','"
							+ total + "', '" + total + "')";
					executeSqlQuery(con, sql);
				}
				float prevL = (float) (low.get(i-1)+(low.get(i-1)*0.0/100));
				sql="select volume from `"+name+"_3` where low<"+low.get(i-1)+" "
						+ " and date(tradedate)='"+tradedate.get(i).toString().split(" ")[0]+"' order by tradedate limit 1";
//				vol = Long.parseLong(executeCountQuery(con, sql));
				if(low.get(i)< prevL && open.get(i) > prevL
						&& Math.abs(open.get(i)-intraOpen.get(i))*100/intraOpen.get(i) < 10
						)
				{
					total= (prevL-intraDay3_18Close.get(i))*100/prevL;
					sql = "Insert into psarResults(name,date, reversal,profitPerc, profitRupees) "
							+ "values " + "('"+name+"', '" + tradedate.get(i) + "','Bear','"
							+ total + "', '" + total + "')";
					executeSqlQuery(con, sql);
				}
			}
			System.out.println("Total: "+total);
		} catch (Exception e) {
		}
	}
	
	public void checkSplitProfit(java.sql.Connection con, String name) throws SQLException {
		ResultSet rs = null;
		List tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Float> intraOpen = new ArrayList<Float>();
		List<Float> intraDayFirst3MinClose = new ArrayList<Float>();
		List<Float> intraDay3_18Close = new ArrayList<Float>();
		List<Long> volume = new ArrayList<Long>();
		float gap=0.3f, sl=-20f;
		try{
			String sql = "select tradedate,open,high,low,close,totalQty from `"+name+"` order by tradedate asc";
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
				high.add(rs.getFloat("high"));
				low.add(rs.getFloat("low"));
				close.add(rs.getFloat("close"));
				volume.add(rs.getLong("totalQty"));
			}
			long sum=0;
			
			for(int i=5; i< tradedate.size()-1000; i++)
			{
				long avgQty = (volume.get(i-1)+volume.get(i-2)+volume.get(i-3)+volume.get(i-4)+volume.get(i-5))/5;
				if((open.get(i) - open.get(i-1))*100/open.get(i-1) >20 && avgQty*close.get(i-1) > 10000000){
					total= (close.get(i+1000)-open.get(i))*100/open.get(i);
					sql = "Insert into psarResults(name,date, reversal,profitPerc, profitRupees) "
							+ "values " + "('"+name+"', '" + tradedate.get(i) + "','Bull','"
							+ total + "', '" + total + "')";
					executeSqlQuery(con, sql);
				}
			}
			System.out.println("Total: "+total);
		} catch (Exception e) {
		}
	}

	public static void main(String[] args) {
		Test t = new Test();
		PriceAboveMA_UsingCCI pin = new PriceAboveMA_UsingCCI();
		java.sql.Connection dbConnection = null;
		List<String> tradedate = new ArrayList<String>();
		List<Float> open = new ArrayList<Float>();
		List<Float> high = new ArrayList<Float>();
		List<Float> low = new ArrayList<Float>();
		List<Float> close = new ArrayList<Float>();
		List<Float> intraOpen = new ArrayList<Float>();
		List<Float> intraFirst3MinClose = new ArrayList<Float>();
		List<Float> intraHigh = new ArrayList<Float>();
		List<Float> intraLow = new ArrayList<Float>();
		List<Float> intraday3Min3_18_close = new ArrayList<Float>();
		float target = 0.5f;
		try {

			Connection con = new Connection();
			dbConnection = con.getDbConnection();
			ResultSet rs = null;
			/*rs = con.executeSelectSqlQuery(dbConnection,
					"SELECT s.name FROM symbols s where " + " ismargin=1 and volume > 100000000"
							+ " and totaltrades>7000 order by volume desc");*/
			
			rs = con.executeSelectSqlQuery(dbConnection,
					"SELECT s.name FROM symbols s where ismargin=1  "
							+ " order by volume desc ");
			
			String name = "";
			boolean updateForTodayAndNextDay = true;
			boolean updateForallDays = true;
			boolean updateResultTable = false;
			boolean isIntraDayData = false;
			boolean insertAllDataToResult = false;
			String iter = "3";
			String touched = "";
			String path = "C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
			List<String> listOfNames = new ArrayList<>();
//			pin.executeSqlQuery(dbConnection, "truncate table williamsresults2");
			float total = 0f;
			while (rs.next()) {
				name = rs.getString("s.name");
				listOfNames.add(name);
			}
//			pin.clubBothResults(dbConnection);
			for (String n : listOfNames) {
				System.out.println(n);
//				pin.checkPrevDayRangeCrossWithVolume(dbConnection, n);
//				pin.checkSplitProfit(dbConnection, n);
//				pin.checkGap3Min(dbConnection, n);
				
//				pin.checkCCI(dbConnection, n);
//				gap = 1.2f;
//				pin.hourlyCross(dbConnection, n);
//				pin.firstThreeMinClose(dbConnection, n);
				
//				pin.checkSupportResistance1Day(dbConnection, n);
//				pin.runAll(dbConnection, n);
//				pin.highVolume(dbConnection, n);
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
	
	public void clubBothResults(java.sql.Connection dbConnection) throws SQLException
	{
		List<String> date = new ArrayList<String>();
		List<Float> profit = new ArrayList();
		String sql="select avg(profitPerc) profit,date from williamsresults3 group by date(date) order by date";
		ResultSet rs = executeSelectSqlQuery(dbConnection, sql);
		while(rs.next()){
			date.add(rs.getString("date"));
			profit.add(rs.getFloat("profit"));
		}
		executeSqlQuery(dbConnection, "truncate table results_2");
		float capital = 1000000;
		for(int i=0; i< date.size();i++){
			String p = executeCountQuery(dbConnection, "select avg(profitPerc) from williamsresults5 where "
					+ " date(date)=date('"+date.get(i)+"')");
			if(p!=null){
//				BigDecimal value = BigDecimal.valueOf(Float.parseFloat(p)+profit.get(i)).setScale(3, RoundingMode.UP);
				float p2 = Float.parseFloat(p);
				float p1 = profit.get(i);
				float totalProfitForDay = (p1*(capital*50/100)/100) + (p2*(capital*50/100)/100);
				float totalProfitPercForDay = (totalProfitForDay/capital) *100;
				sql="insert into results_2(profitPerc,startdate) values('"+totalProfitPercForDay+"',"
						+ " date('"+date.get(i)+"'))";
				executeSqlQuery(dbConnection, sql);
			}else{
				float p1 = profit.get(i);
				float totalProfitForDay = (p1*(capital*100/100)/100);
				float totalProfitPercForDay = (totalProfitForDay/capital) *100;
				sql="insert into results_2(profitPerc,startdate) values('"+totalProfitPercForDay+"',"
						+ " date('"+date.get(i)+"'))";
				executeSqlQuery(dbConnection, sql);
			}
		}
		
	}
	public void runAll(java.sql.Connection dbConnection, String n) throws SQLException
	{
		PriceAboveMA_UsingCCI pin = new PriceAboveMA_UsingCCI();
		openOrIntra="intraFirstMinOpen";
		closeOrIntraClose="intraday3Min3_18_close";
		date1="2015-01-01";date2="2026-01-01";
		sl=-1.5f;int div=100;
		
		Map<String, String> map = new HashMap<String, String>();
/*		map.put("open_1", "close");
		map.put("intraFirstMinOpen_1", "close");
		map.put("open_2", "intradayClose");
		map.put("intraFirstMinOpen_2", "intradayClose");
		map.put("open_3", "intraday3Min3_18_close");
		map.put("intraFirstMinOpen_3", "intraday3Min3_18_close");*/
		
		map.put("open_1", "close");
		map.put("open_2", "intradayClose");
		map.put("open_3", "intraday3Min3_18_close");
		
		
		for(Entry<String, String> set : map.entrySet()){
//			System.out.println(set);
			String key = set.getKey().split("_")[0];
			/*openOrIntra=key; closeOrIntraClose=set.getValue();gap=0.5f;isBodyCompare=false;justGapStrategy=false;
			pin.checkGap1Min(dbConnection, n);
			openOrIntra=key; closeOrIntraClose=set.getValue();gap=0.5f;isBodyCompare=true;justGapStrategy=false;
			pin.checkGap1Min(dbConnection, n);
			openOrIntra=key; closeOrIntraClose=set.getValue();gap=1.2f;isBodyCompare=true;justGapStrategy=true;
			pin.checkGap1Min(dbConnection, n);
			openOrIntra=key; closeOrIntraClose=set.getValue();gap=2f;isBodyCompare=true;justGapStrategy=true;
			pin.checkGap1Min(dbConnection, n);*/
			/*openOrIntra=key; closeOrIntraClose=set.getValue();gap=0.5f;isBodyCompare=false;justGapStrategy=false;
			pin.checkGap1Min(dbConnection, n);
			openOrIntra=key; closeOrIntraClose=set.getValue();gap=0.5f;isBodyCompare=true;justGapStrategy=false;
			pin.checkGap1Min(dbConnection, n);*/
			
			/*openOrIntra=key; closeOrIntraClose
			 * =set.getValue();gap=0.5f;isBodyCompare=false;justGapStrategy=false;
			pin.checkGap1Min(dbConnection, n);
			openOrIntra=key; closeOrIntraClose=set.getValue();gap=0.5f;isBodyCompare=true;justGapStrategy=false;
			pin.checkGap1Min(dbConnection, n);*/
			for(float i=0.6f; i<=.8f; i=i+0.30f){
				openOrIntra=key; closeOrIntraClose=set.getValue();gap=i;isBodyCompare=true;justGapStrategy=true;
				pin.checkGap1Min(dbConnection, n);
			}
//			pin.checkGap1Min(dbConnection, n);
		}
	}
	//changed to intraFirstMinClose from intraSecondMinClose, check----
}
