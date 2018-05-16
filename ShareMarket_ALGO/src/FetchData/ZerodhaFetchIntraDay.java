package FetchData;

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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mysql.jdbc.DatabaseMetaData;

import Indicators.Connection;
import Indicators.Test;
import Strategies.imp.GapModified;

public class ZerodhaFetchIntraDay extends Connection {
	static List<String> listOfMissingSymbols = new ArrayList<>();
	public void fetchIntraDataZerodha(java.sql.Connection dbConnection, String pathToSaveBulkFile, String path, String name, int duration) throws IOException, ParseException, SQLException{
		JSONParser parser = new JSONParser();
		String sql="";
	       
		FileReader fileReader = new FileReader(path); 
		org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(fileReader);
		org.json.simple.JSONObject locObj = (org.json.simple.JSONObject) json.get("data");
		org.json.simple.JSONArray locArr = (org.json.simple.JSONArray) locObj.get("candles");
		org.json.simple.JSONObject o = null;
		String open,high,low,close, arr;
		String a[];
       String symbol; Float openPrice=0.0f;
       boolean filter =false;
       File file = new File(pathToSaveBulkFile+"/"+name+".txt");
       BufferedWriter output = new BufferedWriter(new FileWriter(file,true));
       
       System.out.println(name);
       for (int i = 0; i < locArr.size(); i++) {
    	   arr= locArr.get(i).toString().replace("[", "");
    	   arr = arr.replace("]", "");
    	   a = arr.split(",");
    	   String rep = a[0].replace("\"","");
    	   String time = executeCountQuery(dbConnection, "select time("+a[0].toString().replace("T", " ").replace("+0530", "")+")");
    	   if(duration == 1){
        	   filter = time.equalsIgnoreCase("09:15:00") || time.equalsIgnoreCase("09:16:00");
           }else if (duration==3){
        	   filter = time.equalsIgnoreCase("09:15:00") || time.equalsIgnoreCase("09:18:00") || time.equalsIgnoreCase("15:18:00");
           }
    	   if(filter==true){
    		   	output.write(rep.replace("T", " ").replace("+0530", ""));
	           	output.write(","+a[1]);
	           	output.write(","+a[2]);
	           	output.write(","+a[3]);
	           	output.write(","+a[4]);
	           	output.write(","+a[5]+"\r\n");
    	   }
       }
	   if ( output != null ) output.close();
       fileReader.close();
	}
	public void updateIntraDayData(java.sql.Connection dbConnection, String name, int duration, String year, boolean isMultipleJsonInsert, String path) throws SQLException{
		try {
			String pathToSaveBulkFile = "C:/puneeth/OldLaptop/Puneeth/SHARE_MARKET/Hist_Data/Intraday/"+duration+"/bulk";
			for(int i=1; i< 2; i++){
				try {
					if(isMultipleJsonInsert){
						fetchIntraDataZerodha(dbConnection, pathToSaveBulkFile, path+" ("+i+")"+".json", name, duration);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				if(!isMultipleJsonInsert){
					fetchIntraDataZerodha(dbConnection, pathToSaveBulkFile, path+".json", name, duration);
				}
//				fetchIntraDataZerodha(dbConnection, pathToSaveBulkFile, path+".json", name, duration);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String sql = "LOAD DATA LOCAL INFILE '"+pathToSaveBulkFile+"/"+name+".txt'"+" "+
					 " INTO TABLE "+name+"_"+duration+" "+
					 " FIELDS TERMINATED BY ',' "+
					 " LINES TERMINATED BY '\n'" +
					 " "+
					 " (tradedate, open,high, low, close, volume) ";
         	executeSqlQuery(dbConnection, sql);
		} catch (ParseException e) {
			listOfMissingSymbols.add("'"+name+"'");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String []a) throws IOException, SQLException{
		java.sql.Connection dbConnection=null;
		Connection con = new Connection();
		dbConnection = con.getDbConnection();
		int incr = 10; int bulkSendCount=10;
		boolean updatePreOpenAsOpenPriceByGoogle=true;
		ZerodhaFetchIntraDay preopen = new ZerodhaFetchIntraDay();
		JsonParser js = new JsonParser();
		String count="", sql="";
		int transactionLimit=5000000; float percAppr = 1;
		boolean isMarginReq = true;int duration=3; 
		String startDate="2018-05-06";boolean isMultipleJsonInsert=false, isForZerodhaFetchInJS=false;
		try {
			List<String> list = preopen.getSymbolsWithZerodhaId(dbConnection,isForZerodhaFetchInJS);
			if(isForZerodhaFetchInJS==false){
				for (String name: list){
					String path = "C:/puneeth/OldLaptop/Puneeth/SHARE_MARKET/Hist_Data/Intraday/"+duration+"/"+startDate+"/"+name.split("_")[1];
//					if(!preopen.isTableExist(dbConnection, name+"_60")) System.out.println(name); 
//					if(!preopen.isDataExist(dbConnection, name.split("_")[1], duration, year))
					{
//						preopen.updateIntraDayData(dbConnection, name.split("_")[1], duration, startDate, isMultipleJsonInsert, path);
					}
				}
//				System.out.println(listOfMissingSymbols);
//				System.out.println(list);
//				preopen.updateIntradayOpen(duration);
//				preopen.updateIntradayOpenOneMin(duration);
				preopen.updateIntradayThreeMin(startDate);
			}
		} 
		finally{
			if(dbConnection !=null) dbConnection=null;
		}		
	}
	
	public List<String> getSymbolsWithZerodhaId(java.sql.Connection dbConnection, boolean isForZerodhaFetchInJS) throws SQLException{
		String sql = "SELECT s.zerodha_id, s.name FROM symbols s where volume>5000000 and s.zerodha_id is not null "
				+ " ";
		Connection con = new Connection();
		ResultSet rs = con.executeSelectSqlQuery(dbConnection, sql);
		List<String> listOfZerodhaIds = new ArrayList<>();
		while(rs.next()){
			if(isForZerodhaFetchInJS==true){
				listOfZerodhaIds.add("'"+rs.getString("s.zerodha_id")+"_"+rs.getString("s.name")+"'");//for JS
			}else{
				listOfZerodhaIds.add(""+rs.getString("s.zerodha_id")+"_"+rs.getString("s.name")+"");//For insert
			}
		}
		System.out.println(listOfZerodhaIds);
		return listOfZerodhaIds;
	}
	
	public boolean isDataExist(java.sql.Connection dbConnection, String symbol, int duration, String year) throws SQLException{
		Connection con = new Connection();
		String count = con.executeCountQuery(dbConnection, "select count(*) from `"+symbol+"_"+duration+"` where year(tradedate)="+year);
		return Long.parseLong(count)>0? true: false;
	}
	
	public void updateIntradayOpen(int dur) {
		java.sql.Connection dbConnection = null;
		try {
			Connection con = new Connection();
			dbConnection = con.getDbConnection();
			ResultSet rs = null;
			String sql = "";
			sql = "SELECT s.name FROM symbols s where volume > '5000000' order by convert(totalTrades, SIGNED INTEGER) desc";
			rs = con.executeSelectSqlQuery(dbConnection, sql);
			String name = "";
			String iter = "1d";
			String date="2015-02-02";
			while (rs.next()) {
				name = rs.getString("s.name");
				System.out.println(name);
				/*sql="update `"+name+"` as daily set "+
" daily.intradayOpen= (select open from `"+name+"_"+dur+"` as intra where date(intra.tradedate)=date(daily.tradedate) "+ 
" and date(daily.tradedate)>='"+date+"' and date(intra.tradedate)>='"+date+"' limit 1) "+
" where daily.tradedate>='"+date+"'";
				executeSqlQuery(dbConnection, sql);
				sql="update `"+name+"` as daily set "+
				" daily.intradayClose= (select open from `"+name+"_"+dur+"` as intra where date(intra.tradedate)=date(daily.tradedate) "+ 
				" and date(daily.tradedate)>='"+date+"' and date(intra.tradedate)>='"+date+"' order by intra.tradedate desc limit 1) "+
				" where daily.tradedate>='"+date+"'";
				executeSqlQuery(dbConnection, sql);
				sql="update `"+name+"` as daily set "+
					" daily.intradayFirstHrClose= (select close from `"+name+"_"+dur+"` as intra where date(intra.tradedate)=date(daily.tradedate) "+ 
					" and date(daily.tradedate)>='"+date+"' and date(intra.tradedate)>='"+date+"' limit 1) "+
					" where daily.tradedate>='"+date+"'";
				executeSqlQuery(dbConnection, sql);*/
				sql="update `"+name+"` as daily set "+
						" daily.intradayAt3_15= (select close from `"+name+"_"+dur+"` as intra where date(intra.tradedate)=date(daily.tradedate) "+ 
						" and date(daily.tradedate)>='"+date+"' and date(intra.tradedate)>='"+date+"' and time(intra.tradedate)='14:15:00') "+
						" where daily.tradedate>='"+date+"' and daily.intradayAt3_15 is null";
				executeSqlQuery(dbConnection, sql);
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
	
	public void updateIntradayThreeMin(String startDate) {
		java.sql.Connection dbConnection = null;
		try {
			int dur=3;
			Connection con = new Connection();
			dbConnection = con.getDbConnection();
			ResultSet rs = null;
			String sql = "";
			sql = "SELECT s.name FROM symbols s where volume > '5000000' order by convert(totalTrades, SIGNED INTEGER) desc";
			rs = con.executeSelectSqlQuery(dbConnection, sql);
			String name = "";
			while (rs.next()) {
				name = rs.getString("s.name");
				System.out.println(name);
				sql="update `"+name+"` as daily set "+
					" daily.intradayFirst3MinClose= (select close from `"+name+"_"+dur+"` as intra where date(intra.tradedate)=date(daily.tradedate) "+ 
					" and date(daily.tradedate)>='"+startDate+"' and date(intra.tradedate)>='"+startDate+"' limit 1) "+
					" where date(daily.tradedate)>='"+startDate+"'";
				executeSqlQuery(dbConnection, sql);
				sql="update `"+name+"` as daily set "+
						" daily.intradaySecond3MinClose= (select close from `"+name+"_"+dur+"` as intra where date(intra.tradedate)=date(daily.tradedate) "+ 
						" and date(daily.tradedate)>='"+startDate+"' and date(intra.tradedate)>='"+startDate+"' limit 1,1) "+
						" where date(daily.tradedate)>='"+startDate+"'";
				executeSqlQuery(dbConnection, sql);
				sql="update `"+name+"` as daily set "+
						" daily.intraday3Min3_18_Close= (select close from `"+name+"_"+dur+"` as intra where date(intra.tradedate)=date(daily.tradedate) "+ 
						" and date(daily.tradedate)>='"+startDate+"' and date(intra.tradedate)>='"+startDate+"' limit 2,1) "+
						" where date(daily.tradedate)>='"+startDate+"'";
				executeSqlQuery(dbConnection, sql);
				sql="update `"+name+"` as daily set "+
						" daily.intradayOpen= (select open from `"+name+"_"+dur+"` as intra where date(intra.tradedate)=date(daily.tradedate) "+ 
						" and date(daily.tradedate)>='"+startDate+"' and date(intra.tradedate)>='"+startDate+"' limit 1) "+
						" where daily.tradedate>='"+startDate+"'";
				executeSqlQuery(dbConnection, sql);
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
	public void updateIntradayOpenOneMin(int dur) {
		java.sql.Connection dbConnection = null;
		try {
			Connection con = new Connection();
			dbConnection = con.getDbConnection();
			ResultSet rs = null;
			String sql = "";
			sql = "SELECT s.name FROM symbols s where volume > '5000000' order by convert(totalTrades, SIGNED INTEGER) desc";
			rs = con.executeSelectSqlQuery(dbConnection, sql);
			String name = "";
			String iter = "1d";
			String date="2018-03-20";
			while (rs.next()) {
				name = rs.getString("s.name");
				System.out.println(name);
				sql="update `"+name+"` as daily set "+
" daily.intraFirstMinClose= (select close from `"+name+"_"+dur+"` as intra where date(intra.tradedate)=date(daily.tradedate) "+ 
" and date(daily.tradedate)>='"+date+"' and date(intra.tradedate)>='"+date+"' limit 1) "+
" where daily.tradedate>='"+date+"'";
				executeSqlQuery(dbConnection, sql);
				sql="update `"+name+"` as daily set "+
						" daily.intraSecondMinClose= (select close from `"+name+"_"+dur+"` as intra where date(intra.tradedate)=date(daily.tradedate) "+ 
						" and date(daily.tradedate)>='"+date+"' and date(intra.tradedate)>='"+date+"' limit 1,1) "+
						" where daily.tradedate>='"+date+"'";
				executeSqlQuery(dbConnection, sql);
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
