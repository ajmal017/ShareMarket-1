package Strategies.Weekly;

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

public class Engulfing extends Connection {
	public void getData(java.sql.Connection con, String name) {
		ResultSet rs = null;
		float percentage = 80f;

		List tradedate = new ArrayList<String>();
		List tradedQuantity = new ArrayList<Long>();
		List open = new ArrayList<Float>();
		List high = new ArrayList<Float>();
		List low = new ArrayList<Float>();
		List close = new ArrayList<Float>();
		String date = "", sql = "";
		float percProfitAtHighPrice = 0f, percProfitAtClosePrice = 0f, percProfitAtLowPrice = 0f;
		try {
			rs = executeSelectSqlQuery(con, "select * from " + name + "_7d");
			while (rs.next()) {
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
				high.add(rs.getFloat("high"));
				low.add(rs.getFloat("low"));
				close.add(rs.getFloat("close"));
			}
			float trig = 0f;
			String tradedDate = "", exitDate = "";
			int count = 0;
			float range = 0f, prevRange = 0f; float perc=0f;
			boolean isTargetDayAchieved = true;float diff=0f;
			for (int i = 2; i < tradedate.size(); i++) {
//				diff = ((float)close.get(i-2)-(float)low.get(i-6))*100/(float)low.get(i-6);
				if((float)close.get(i) < (float)open.get(i-1) && (float)open.get(i) > (float)close.get(i-1)
						&& (float)low.get(i+1) < (float)low.get(i) && (float)open.get(i+1) > (float)low.get(i)){
//					perc = ((float) open.get(i+1) - (float)low.get(i+1)) * 100 / (float) open.get(i+1);
//					perc = ((float) low.get(i) - (float)low.get(i+1)) * 100 / (float) low.get(i);
//					sql = "insert into williamsresults " 
//							+ "(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
//							+ " values ('" + name + "', 'Bear', " + trig + ", " + perc + ", " + trig + ", '"
//							+ tradedate.get(i) + "', '" + tradedate.get(i) + "', '1')";
//					executeSqlQuery(con, sql);
					calcData(con, name, tradedate.get(i+1).toString(), tradedate.get(i).toString(),(float) open.get(i+1),
							(float) high.get(i+1), (float) low.get(i+1), (float) close.get(i+1), (float) high.get(i),
							(float) low.get(i));
				}
//				diff = ((float)high.get(i-6)-(float)close.get(i-2))*100/(float)close.get(i-2);
				if((float)close.get(i) > (float)open.get(i-1) && (float)open.get(i) < (float)close.get(i-1)
						&& (float)high.get(i+1) > (float)high.get(i) && (float)open.get(i+1) < (float)high.get(i)){
//					perc = ((float) high.get(i+1) - (float)open.get(i+1)) * 100 / (float) open.get(i+1);
//					perc = ((float) high.get(i+1) - (float)high.get(i)) * 100 / (float) high.get(i);
//					sql = "insert into williamsresults " 
//							+ "(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
//							+ " values ('" + name + "', 'Bull', " + trig + ", " + perc + ", " + trig + ", '"
//							+ tradedate.get(i) + "', '" + tradedate.get(i) + "', '1')";
//					executeSqlQuery(con, sql);
					calcData(con, name, tradedate.get(i+1).toString(), tradedate.get(i).toString(),(float) open.get(i+1),
							(float) high.get(i+1), (float) low.get(i+1), (float) close.get(i+1), (float) high.get(i),
							(float) low.get(i));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void calcData(java.sql.Connection con, String name, String weekEnd, String weekStart, float open, float high,
			float low, float close, float prevWeekHigh, float prevWeekLow) throws SQLException {
		String sql = "select d.open, d.high,d.low,d.close,d.tradedate from " + name + " d "
				+ " where d.tradedate >'" + weekStart + "' and d.tradedate <='" + weekEnd
				+ "'";
		ResultSet rs = null;
		List<Float> open1 = new ArrayList<>();
		List<Float> high1 = new ArrayList<>();
		List<Float> low1 = new ArrayList<>();
		List<Float> close1 = new ArrayList<>();
		List<Long> openInterest = new ArrayList<>();
		List<String> date1 = new ArrayList<>();
		float range = (prevWeekHigh - prevWeekLow);
		float perc = 0f;
		float triggerRange = range * .433f;
		float range2 = range * .766f;
		float range3 = range * 1.355f;
//		triggerRange = range3;
		try {
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				date1.add(rs.getString("d.tradedate"));
				open1.add(rs.getFloat("d.open"));
				high1.add(rs.getFloat("d.high"));
				low1.add(rs.getFloat("d.low"));
				close1.add(rs.getFloat("d.close"));
			}
			float trig = 0f;
			for (int i = 0; i < date1.size(); i++) {
				if ((float) high1.get(i) > prevWeekHigh) {
					perc = ((float) high1.get(i) - prevWeekHigh) * 100 / prevWeekHigh;
					perc = (Math.max((float) high1.get(i), (float) high1.get(i+1)) - prevWeekHigh) * 100 / prevWeekHigh;
					sql = "insert into williamsresults " 
							+ "(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
							+ " values ('" + name + "', 'Bull', " + prevWeekHigh + ", " + perc + ", " + prevWeekHigh + ", '"
							+ date1.get(i) + "', '" + date1.get(i) + "', '1')";
					executeSqlQuery(con, sql);
					break;
				}
				if ((float) low1.get(i) < prevWeekLow) { 
					perc = (prevWeekLow - Math.min((float) low1.get(i), (float) low1.get(i+1))) * 100 / prevWeekLow;
					sql = "insert into williamsresults "
							+ "(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
							+ " values ('" + name + "', 'Bear', " + prevWeekLow + ", " + perc + ", " + prevWeekLow + ", '"
							+ date1.get(i) + "', '" + date1.get(i) + "', '1')";
					executeSqlQuery(con, sql);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	public static void main(String[] args) {
		Test t = new Test();
		Engulfing pin = new Engulfing();
		java.sql.Connection dbConnection = null;
		boolean updateSymbolsTableData = true;
		boolean updateAllData = true;
		try {

			Connection con = new Connection();
			dbConnection = con.getDbConnection();
			ResultSet rs = null;
			rs = con.executeSelectSqlQuery(dbConnection,
					"SELECT s.name FROM symbols s where volume > 100000000 order by volume desc");
			String name = "";
			boolean updateForTodayAndNextDay = true;
			boolean updateForallDays = true;
			boolean updateResultTable = false;
			boolean isIntraDayData = false;
			boolean insertAllDataToResult = false;
			String iter = "1d";
			String path = "C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
			while (rs.next()) {
				name = rs.getString("s.name");
				System.out.println(name);
				if (!iter.equals("1d"))
					name = name + "_" + iter + "";
				pin.getData(dbConnection, name);
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
