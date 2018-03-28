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

public class MonthlyDataStrategy extends Connection {
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
			rs = executeSelectSqlQuery(con, "select tradedate, open*1 as open, high*1 as high, low*1 low,close*1 close from " + name + "_1M");
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
			int i=0;
			for (i = 1; i < tradedate.size(); i++) {
//				sql = "SELECT   DATE_FORMAT(DATE_ADD('"+tradedate.get(i-1)+"', INTERVAL 1 MONTH), '%Y-%m-01') AS "+
//						" FirstDayOfNextMonth ";
//				String monthStart = executeCountQuery(con, sql);
//				sql = "SELECT  LAST_DAY(DATE_ADD('"+tradedate.get(i-1)+"', INTERVAL 1 MONTH)) AS "+
//			            " LastDayOfNextMonth ";
//				String monthEnd = executeCountQuery(con, sql);
//				calcData(con, name, monthStart, monthEnd,(float) open.get(i-1),
//						(float) high.get(i-1), (float) low.get(i-1), (float) close.get(i-1));
				
				calcMonthlyDoji(con, name, (float) open.get(i-1),
						(float) high.get(i-1), (float) low.get(i-1),(float) close.get(i-1), tradedate.get(i).toString());
			}
//			sql = "SELECT   DATE_FORMAT(DATE_ADD('"+tradedate.get(i-1)+"', INTERVAL 1 MONTH), '%Y-%m-01') AS "+
//					" FirstDayOfNextMonth ";
//			String monthStart = executeCountQuery(con, sql);
//			sql = "SELECT  LAST_DAY(DATE_ADD('"+tradedate.get(i-1)+"', INTERVAL 1 MONTH)) AS "+
//		            " LastDayOfNextMonth ";
//			String monthEnd = executeCountQuery(con, sql);
//			calcData(con, name, monthStart, monthEnd,(float) open.get(i-1),
//					(float) high.get(i-1), (float) low.get(i-1), (float) close.get(i-1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void calcMonthlyDoji(java.sql.Connection con, String name, float open, float high,
			float low, float close, String date) throws SQLException {
		String sql = "";
//		System.out.println(sql);
		ResultSet rs = null;
		try {
			float diff = (close> open) ? (high-close)*100/close : (close - low)*100/low;
			if(diff > 10){
				sql = "insert into williamsresults " 
						+ "(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
						+ " values ('" + name + "', 'BullBear', " + diff + ", " + diff + ", " + diff + ", '"
						+ date + "', '" + date + "', '1')";
				executeSqlQuery(con, sql);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
		}
	}
	public void calcData(java.sql.Connection con, String name, String monthStart, String monthEnd, float open, float high,
			float low, float close) throws SQLException {
		String sql = "select d.open*1 open, d.high*1 high,d.low*1 low,d.close*1 close,d.tradedate from " + name + " d "
				+ " where d.tradedate >='" + monthStart + "' and d.tradedate <='" + monthEnd
				+ "'";
//		System.out.println(sql);
		ResultSet rs = null;
		List<Float> open1 = new ArrayList<>();
		List<Float> high1 = new ArrayList<>();
		List<Float> low1 = new ArrayList<>();
		List<Float> close1 = new ArrayList<>();
		List<Long> openInterest = new ArrayList<>();
		List<String> date1 = new ArrayList<>();
		try {
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				date1.add(rs.getString("d.tradedate"));
				open1.add(rs.getFloat("open"));
				high1.add(rs.getFloat("high"));
				low1.add(rs.getFloat("low"));
				close1.add(rs.getFloat("close"));
			}
			float trig = 0f,trigLow=0f, perc=0f, perc2;
			trig = high+high * 0/100;
			trigLow = low-low * 0/100;
			for (int i = 0; i < date1.size(); i++) {
				float volatility = (close-open)*100/open;
				volatility = (open>close) ? (high-open)*100/open : (high-close)*100/close;
//				float range = (trig-(float)open1.get(i))*100/(float)open1.get(i);
				if ((float) high1.get(i) > trig  && volatility > 20 && (float)open1.get(i)< trig) {
					perc = (trig - (float) close1.get(i)) * 100 / trig;
					perc2 = (close1.get(i) -  trig) * 100 / trig;
					sql = "insert into williamsresults " 
							+ "(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
							+ " values ('" + name + "', 'Bear', " + perc + ", " + perc + ", " + perc2 + ", '"
							+ date1.get(i) + "', '" + date1.get(i) + "', '1')";
					executeSqlQuery(con, sql);
					break;
				}
//				volatility = (open-close)*100/open; 
//				volatility = (open>close) ? (close-low)*100/close : (open-low)*100/low;
//				range = ((float)open1.get(i)-trigLow)*100/(float)open1.get(i);
//				if ((float) low1.get(i) < trigLow && range > 5) {
//					perc = (close1.get(i) -  trigLow) * 100 / trigLow;
//					perc2 = (trigLow - (float) close1.get(i)) * 100 / trigLow;
//					sql = "insert into williamsresults "
//							+ "(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
//							+ " values ('" + name + "', 'Bull', " + perc + ", " + perc + ", " + perc2 + ", '"
//							+ date1.get(i) + "', '" + date1.get(i) + "', '1')";
//					executeSqlQuery(con, sql);
//					break;
//				}
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
		MonthlyDataStrategy pin = new MonthlyDataStrategy();
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
