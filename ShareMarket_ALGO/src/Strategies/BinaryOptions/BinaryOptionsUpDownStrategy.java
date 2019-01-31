package Strategies.BinaryOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mysql.jdbc.DatabaseMetaData;

import Indicators.Connection;
import Indicators.Test;
import Strategies.imp.GapModified;

public class BinaryOptionsUpDownStrategy extends Connection {
	
	public static void main(String[] args) throws IOException, ParseException, SQLException, java.text.ParseException {
		Connection con = new Connection();
		java.sql.Connection dbConnection = null;
		dbConnection = con.getDbConnection();
		BinaryOptionsUpDownStrategy bo = new BinaryOptionsUpDownStrategy();
//		bo.testUpDownStrategy(dbConnection);
		bo.testStrategyConsecutiveRedGreen(dbConnection);
		/*String sql = "SELECT s.name, s.margin FROM symbols s where totalTrades >= 30000 and isMargin=1 and name !='VAKRANGEE' and name!='PCJEWELLER'"
				+ " and name!='PHILIPCARB'  order by convert(totalTrades, SIGNED INTEGER) desc";
		ResultSet rs = con.executeSelectSqlQuery(dbConnection, sql);
		while (rs.next())
		{
			bo.testUpDownStrategyStocks(dbConnection, rs.getString("s.name"));
		}*/
	}
	
	public void testUpDownStrategy(java.sql.Connection dbConnection) throws SQLException{
//		EUR_USD_BO_5
		String sql = "select tradedate, open,high,low,close from EUR_USD_BO_5";
		List<String> date = new ArrayList<>();
		List<Float> open = new ArrayList<>();
		List<Float> high = new ArrayList<>();
		List<Float> low = new ArrayList<>();
		List<Float> close = new ArrayList<>();
		ResultSet rs = null;
		rs = executeSelectSqlQuery(dbConnection, sql);
		while(rs.next()){
			date.add(rs.getString("tradedate"));
			open.add(rs.getFloat("open"));
			high.add(rs.getFloat("high"));
			low.add(rs.getFloat("low"));
			close.add(rs.getFloat("close"));
		}
		boolean isUp=true;
		float totalProfit=100;boolean isSequence = false;
		int lossCount=0, max=5,buyOut=90;
		float[] arr = {1, 3, 9, 27, 81, 243, 729, 2187, 6561, 19683,59049,177147,531441};
//		float[] arr = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512,1024,2048,4096};
//		float[] arr = {5, 10, 5, 10,5, 10,5, 10,5, 10,5, 10,5, 10};
//		float[] arr = {1, 2, 3, 6, 12, 24, 1, 2, 3, 6, 12, 24};
//		float[] arr = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
		for (int i=0; i<date.size(); i++)
		{
			float o = open.get(i);float c = close.get(i);
			if(isUp){
				if(c>o){
					totalProfit+= (arr[lossCount]*buyOut/100)*1;
					isUp=true;
					isSequence=false;
					if(lossCount > max){
						System.out.println("Bull success,Loss count reached "+lossCount+","+totalProfit+","+(arr[lossCount]*91/100)*2);
					}
					lossCount=0;
				}else if(c<o){
					totalProfit = totalProfit-arr[lossCount];
					isSequence=true;
					isUp = false;
					if(lossCount==0)
					{
//						System.out.println("Bull loss "+date.get(i)+","+lossCount);
					}
					lossCount++;
				}else if (c==o){
					totalProfit = totalProfit-arr[lossCount];
					isSequence=true;
					isUp = false;
					lossCount++;
					System.out.println("equal");
				}
			}
			else if(!isUp){
				if(o>c){
					totalProfit+= (arr[lossCount]*buyOut/100)*1;
					isUp=true;
					isSequence=false;
					if(lossCount > max){
						System.out.println("Bear success,Loss count reached "+lossCount+","+totalProfit+","+(arr[lossCount]*91/100)*2);
					}
					lossCount=0;
				}else if(o<c){
					totalProfit = totalProfit-arr[lossCount];
					isSequence=true;
					isUp = true;
					if(lossCount==0)
					{
//						System.out.println("Bear loss "+date.get(i)+","+lossCount);
					}
					lossCount++;
				}else if(c==o){
					totalProfit = totalProfit-arr[lossCount];
					isSequence=true;
					isUp = true;
					lossCount++;
					System.out.println("equal");
				}
			}
		}
		System.out.println(totalProfit);
		System.out.println(lossCount);
	}
	
	public void testStrategyConsecutiveRedGreen(java.sql.Connection dbConnection) throws SQLException{
//		EUR_USD_BO_5
		String sql = "select tradedate, open,high,low,close from EUR_USD_BO_5";
		List<String> date = new ArrayList<>();
		List<Float> open = new ArrayList<>();
		List<Float> high = new ArrayList<>();
		List<Float> low = new ArrayList<>();
		List<Float> close = new ArrayList<>();
		ResultSet rs = null;
		rs = executeSelectSqlQuery(dbConnection, sql);
		while(rs.next()){
			date.add(rs.getString("tradedate"));
			open.add(rs.getFloat("open"));
			high.add(rs.getFloat("high"));
			low.add(rs.getFloat("low"));
			close.add(rs.getFloat("close"));
		}
		boolean isUp=true;
		float totalProfit=100;boolean isSequence = false;
		int lossCount=0, max=5,buyOut=90;
		float[] arr = {1, 3, 9, 27, 81, 243, 729, 2187, 6561, 19683,59049,177147,531441};
		for (int i=1; i<date.size()-1; i++)
		{
			float o = open.get(i);float c = close.get(i);
			float o1 = open.get(i-1);float c1 = close.get(i-1);
			if((c-o) > (c1-o1)*2 && c>0 && c1>o1 && (c-o)*100/o >0.05){
				if(close.get(i+1) > open.get(i+1)){
					totalProfit+= 5;
//					System.out.println(totalProfit);
				}else{
					totalProfit= totalProfit-5;
//					System.out.println(totalProfit);
				}
			}
		}
		System.out.println(totalProfit);
		System.out.println(lossCount);
	}
	
	public void testUpDownStrategyStocks(java.sql.Connection dbConnection, String name) {
//		EUR_USD_BO_5
//		open_interest
//		USDINRAUGFUT_30
//		String sql = "select tradedate, open,high,low,close from USDINRAUGFUT ";
//		String sql = "select tradedate, open,high,low,close,open_interest from "+name+"_OPT where open_interest is not null and open<>close";
		String sql = "select tradedate, open,high,low,close from "+name+" ";
		List<String> date = new ArrayList<>();
		List<Float> open = new ArrayList<>();
		List<Float> high = new ArrayList<>();
		List<Float> low = new ArrayList<>();
		List<Float> close = new ArrayList<>();
		List<Long> o_i = new ArrayList<>();
		ResultSet rs = null;
		try {
			System.out.println(name);
			rs = executeSelectSqlQuery(dbConnection, sql);
			if(rs != null)
			{
				while(rs.next()){
					date.add(rs.getString("tradedate"));
					open.add(rs.getFloat("open"));
					high.add(rs.getFloat("high"));
					low.add(rs.getFloat("low"));
					close.add(rs.getFloat("close"));
//					o_i.add(rs.getLong("open_interest"));
				}
			}
			boolean isUp=true;
			float loss=0f, profit=0f, brokerage=0.03f;boolean isSequence = false;
			int lossCount=0, max=5;
			float capital=1000f, totalProfit=capital;
//			float[] arr = {1, 2, 1, 2,1, 2,1, 2,1, 2,1, 2,1, 2,1, 2,1, 2,1, 2,1, 2,1, 2,1, 2,1, 2,1, 2,1, 2,1, 2,1, 2};
//			float[] arr = {5, 10, 15, 5, 10, 15, 5, 10, 15, 5, 10, 15,5, 10, 15,5, 10, 15,5, 10, 15,5, 10, 15,5, 10, 15,5, 10, 15,5, 10, 15};
//			float[] arr = {5, 10, 5, 10,5, 10,5, 10,5, 10,5, 10,5, 10,5, 10,5, 10,5, 10,5, 10,5, 10,5, 10,5, 10,5, 10,5, 10,5, 10};
			float[] arr = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
			for (int i=1; i<date.size(); i++)
			{
				float o = open.get(i);float c = close.get(i);
				/*if(o_i.get(i-1)*o < 10000000)
				{
					continue;
				}*/
				if(isUp){
					if(c>o){
						profit = (c-o)*100/o-brokerage;
						totalProfit = totalProfit+ (capital*arr[lossCount]*profit/100);
						isUp=true;
						isSequence=false;
						if(lossCount > max){
//							System.out.println("Bull success,Loss count reached "+lossCount+","+totalProfit+","+(arr[lossCount]*91/100)*2);
						}
						lossCount=0;
						sql = "insert into psarresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('"+name+"', '', " + totalProfit + ", " + (capital*arr[lossCount]*profit/100) + ", "
								+ capital*arr[lossCount] + ", '" + date.get(i) + "')";
//						executeSqlQuery(dbConnection, sql);
					}else if(c<o){
						profit = ((c-o)*100/o-brokerage)*-1;
						totalProfit = totalProfit-(capital*arr[lossCount]*profit/100);
						isSequence=true;
						isUp = false;
						if(lossCount==0)
						{
//							System.out.println("Bull loss "+date.get(i)+","+lossCount);
						}
						lossCount++;
						sql = "insert into psarresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('"+name+"', '', " + totalProfit + ", " + (-capital*arr[lossCount]*profit/100) + ", "
								+ capital*arr[lossCount] + ", '" + date.get(i) + "')";
//						executeSqlQuery(dbConnection, sql);
					}else if (c==o){
						profit = ((c-o)*100/o-brokerage)*-1;
						totalProfit = totalProfit-(capital*arr[lossCount]*profit/100);
						isSequence=true;
						isUp = false;
						lossCount++;
						sql = "insert into psarresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('"+name+"', '', " + totalProfit + ", " + (-capital*arr[lossCount]*profit/100) + ", "
								+ capital*arr[lossCount] + ", '" + date.get(i) + "')";
//						executeSqlQuery(dbConnection, sql);
					}
				}
				else if(!isUp){
					if(o>c){
						profit = (o-c)*100/o-brokerage;
						totalProfit = totalProfit+ (capital*arr[lossCount]*profit/100);
						isUp=true;
						isSequence=false;
						if(lossCount > max){
//							System.out.println("Bear success,Loss count reached "+lossCount+","+totalProfit+","+(arr[lossCount]*91/100)*2);
						}
						lossCount=0;
						sql = "insert into psarresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('"+name+"', '', " + totalProfit + ", " + (capital*arr[lossCount]*profit/100) + ", "
								+ capital*arr[lossCount] + ", '" + date.get(i) + "')";
//						executeSqlQuery(dbConnection, sql);
					}else if(o<c){
						profit = ((o-c)*100/o-brokerage)*-1;
						totalProfit = totalProfit-(capital*arr[lossCount]*profit/100);
						isSequence=true;
						isUp = true;
						if(lossCount==0)
						{
//							System.out.println("Bear loss "+date.get(i)+","+lossCount);
						}
						lossCount++;
						sql = "insert into psarresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('"+name+"', '', " + totalProfit + ", " + (-capital*arr[lossCount]*profit/100) + ", "
								+ capital*arr[lossCount] + ", '" + date.get(i) + "')";
//						executeSqlQuery(dbConnection, sql);
					}else if(c==o){
						profit = ((o-c)*100/o-brokerage)*-1;
						totalProfit = totalProfit-(capital*arr[lossCount]*profit/100);
						isSequence=true;
						isUp = true;
						lossCount++;
						sql = "insert into psarresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								+ " values ('"+name+"', '', " + totalProfit + ", " + (-capital*arr[lossCount]*profit/100) + ", "
								+ capital*arr[lossCount] + ", '" + date.get(i) + "')";
//						executeSqlQuery(dbConnection, sql);
					}
				}
			}
			sql = "insert into psarresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
					+ " values ('"+name+"', '', " + totalProfit + ", " + totalProfit + ", "
					+ totalProfit + ", '2016-01-01 00:00:00')";
			executeSqlQuery(dbConnection, sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
	}
}
