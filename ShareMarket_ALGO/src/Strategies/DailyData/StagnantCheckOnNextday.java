package Strategies.DailyData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import FetchData.GoogleIntraday;
import Indicators.CCI;
import Indicators.Connection;
import Indicators.MACD;
import Indicators.Test;
import Indicators.TrayIconDemo;

public class StagnantCheckOnNextday extends Connection {
	List<String> result = new ArrayList<>();

	public List<String> getResult() {
		return result;
	}
	public void setResult(List<String> result) {
		this.result = result;
	}
	public static void main(String[] args) throws LineUnavailableException, InterruptedException {
		boolean isMarketEnded = false;
		StagnantCheckOnNextday live = new StagnantCheckOnNextday();
		live.runCode();
	}

	public void runCode() {
		Test t = new Test();
		CCI cci = new CCI();
		StagnantCheckOnNextday live = new StagnantCheckOnNextday();
		java.sql.Connection dbConnection = null;
		String epochString = "1488095815";
		long epoch = Long.parseLong(epochString);
		Date expiry = new Date(epoch * 1000);
		String DbDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expiry);
		int noOfDataRangeToBeInserted = 3;
		String iteration = "1";
		try {
			boolean updateForTodayAndNextDay = true, updateForallDays = false, isIntraDayData = true;
			GoogleIntraday g = new GoogleIntraday();
			Connection con = new Connection();
			MACD macd = new MACD();
			dbConnection = con.getDbConnection();
			ResultSet rs = null, rs2=null;
			rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 100000000");
			String name = "";
			String range = "100"; // days
			String path = "C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/" + iteration + "/";
			String curDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			List<String> listOfSymbols = new ArrayList<>();
			BufferedWriter output = null;
			File file = new File("C:\\Puneeth\\SHARE_MARKET\\Stagnant\\Stagnant.txt");
	        output = new BufferedWriter(new FileWriter(file));
			while (rs.next()) {
				name = rs.getString("s.name");
				listOfSymbols.add(name);
			}
			for(String sym: listOfSymbols){
				String sql= "select * from "+sym+"  where tradedate >='2016-03-28'";
				rs2 = executeSelectSqlQuery(dbConnection, sql);
				List<Float> open = new ArrayList<>();
				List<Float> high = new ArrayList<>();
				List<Float> low = new ArrayList<>();
				List<Float> close = new ArrayList<>();
				List<String> date = new ArrayList<>();
				while (rs2.next()){
					open.add(rs2.getFloat("open"));
					high.add(rs2.getFloat("high"));
					low.add(rs2.getFloat("low"));
					close.add(rs2.getFloat("close"));
					date.add(rs2.getString("tradedate"));
				}
				for (int i=1; i< date.size(); i++){
					System.out.println(sym);
					float upper = ((float)open.get(i)-(float)close.get(i-1))*100/(float)close.get(i-1);
					float lower = ((float)close.get(i-1)-(float)open.get(i))*100/(float)open.get(i);
					float upProfit = ((float)high.get(i)-(float)open.get(i))*100/(float)open.get(i);
					float lowProfit = ((float)open.get(i)-(float)low.get(i))*100/(float)open.get(i);
					live.getStagnantData(dbConnection, sym+"_1", date.get(i-1), upper, lower, upProfit, lowProfit);
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}

	public boolean getStagnantData(java.sql.Connection con, String name, String dailyDate, float upper, float lower, float upProfit, float lowProfit) {
		ResultSet rs = null;
		List<Float> open = new ArrayList<>();
		List<Float> high = new ArrayList<>();
		List<Float> low = new ArrayList<>();
		List<Float> close = new ArrayList<>();
		List<String> date = new ArrayList<>();
		List<Float> ema2 = new ArrayList<>();
		String sql = "";
		String interval = "1";
		boolean isStagnant=false;
		try {
			sql = "select * from " + name + " where tradedate >=concat(Date('" + dailyDate
					+ " '),' 13:00:00') and tradedate <= concat(Date('" + dailyDate + "'),' 15:30:00')";

			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()) {
				open.add(rs.getFloat("open"));
				high.add(rs.getFloat("high"));
				low.add(rs.getFloat("low"));
				close.add(rs.getFloat("close"));
				ema2.add(rs.getFloat("ema2"));
				date.add(rs.getString("tradedate"));
			}
			int count = 0;
			float range = 0f;
			for (int i = 0; i < date.size() - 1; i++) {
				range = Math.abs((float) ema2.get(i) - (float) ema2.get(i + 1)) * 100 / (float) ema2.get(i);

				if (range < 0.007) {
					count++;
				} else
					count = 0;
				if (count > 100) {
					if(upper>1){
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	         					+ " values ('"+name+"', 'Bear', 0, "+lowProfit+", "+lowProfit+", '"+dailyDate+"', '"+dailyDate+"', '1')";
	          			executeSqlQuery(con, sql);
					}else if (lower>1){
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	         					+ " values ('"+name+"', 'Bull', 0, "+upProfit+", "+upProfit+", '"+dailyDate+"', '"+dailyDate+"', '1')";
	          			executeSqlQuery(con, sql);
					}
	  				
					count = 0;
					break;
				}
			}

			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (Exception e) {
//			e.printStackTrace();
		} finally {
			open.clear();
			high.clear();
			low.clear();
			close.clear();
			date.clear();
			ema2.clear();date.clear();
		}
		return isStagnant;
	}
}
