package FetchData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Indicators.Connection;

public class Fetch_Old_Historical_Intra_Stocks_ForOnlyUpdateInDaily extends Connection {

	public static void main(String[] a) throws IOException, SQLException {
		java.sql.Connection dbConnection = null;
		Fetch_Old_Historical_Intra_Stocks_ForOnlyUpdateInDaily fetch = new Fetch_Old_Historical_Intra_Stocks_ForOnlyUpdateInDaily();
		Connection con = new Connection();
		dbConnection = con.getDbConnection();
		ResultSet rs = con.executeSelectSqlQuery(dbConnection,
				"SELECT s.name FROM symbols s where isMargin=1 "
						+ " order by volume asc");
		List<String> listOfSymbols = new ArrayList<>();
		while(rs.next()){
			listOfSymbols.add(rs.getString("s.name"));
		}
		String n="";
		for(String name: listOfSymbols){
			for(int i=0; i<1; i++){
				if(i==0)n=name;else n= ""+name+" ("+i+")";
//				String path = "C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\pastintraday\\2018\\NOV\\"+name+".txt";
				String path = "C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\intraday\\1\\2019\\3-13\\"+n+".JSON";
				String table = name;
				String count = "", sql = "";
				try {
					System.out.println(table);
					fetch.updateInDailyFromZerodha(dbConnection, path, table);
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	public void fetchData(java.sql.Connection dbConnection, String path, String table)
			throws IOException, SQLException {
		File file = new File(path);

		BufferedReader br = new BufferedReader(new FileReader(file));
		String st, sql, date, dateT, time, open, high, low, close, volume;
		while ((st = br.readLine()) != null) {
			System.out.println(st);
			dateT = st.split(",")[1];
			open = st.split(",")[3];
			high = st.split(",")[4];
			low = st.split(",")[5];
			close = st.split(",")[6];
			volume = st.split(",")[7];
			time = st.split(",")[2] + ":00";
			date = dateT.substring(0, 4) + "-" + dateT.substring(4, 6) + "-" + dateT.substring(6) + " " + time;
			sql = "Insert into " + table + "(tradedate, open,high, low, close,volume) values" + "('" + date + "',"
					+ open + "," + high + "," + low + "," + close + "," + volume + ")";
			executeSqlQuery(dbConnection, sql);
		}
	}
	
	public void updateInDailyNotFromZerodha(java.sql.Connection dbConnection, String path, String table)
	{
		File file = new File(path);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String st, sql, date, dateT, time, open, high, low, close, volume;
			while ((st = br.readLine()) != null) {
				dateT = st.split(",")[1];
				open = st.split(",")[3];
				high = st.split(",")[4];
				low = st.split(",")[5];
				close = st.split(",")[6];
				volume = st.split(",")[7];
				time = st.split(",")[2] + ":00";
				date = dateT.substring(0, 4) + "-" + dateT.substring(4, 6) + "-" + dateT.substring(6) + " " + time;
				if(time.equals("09:16:00")){
					sql="update `"+table+"` set intraFirstMinOpen="+open+",intraFirstMinHigh="+high+","
							+ "intraFirstMinLow="+low+", intraFirstMinClose="+close+", intraFirstMinVolume="+volume+" "
							+ " where date(tradedate)=date('"+date+"')";
					executeSqlQuery(dbConnection, sql);
				}
				if(time.equals("09:17:00")){
					sql="update `"+table+"` set intraSecondMinClose="+close+", intraSecondMinVolume="+volume+" "
							+ " where date(tradedate)=date('"+date+"')";
					executeSqlQuery(dbConnection, sql);
				}
			}
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void updateInDailyFromZerodha(java.sql.Connection dbConnection, String path, String table) throws IOException, ParseException
	{
		File file = new File(path);
		JSONParser parser = new JSONParser();
		FileReader fileReader = new FileReader(path); 
		org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(fileReader);
		org.json.simple.JSONObject locObj = (org.json.simple.JSONObject) json.get("data");
		org.json.simple.JSONArray locArr = (org.json.simple.JSONArray) locObj.get("candles");
		String arr="",a[];
		String st, sql, date, dateT, time, open, high, low, close, volume;
		try {
			for (int i = 0; i < locArr.size(); i++) {
				arr= locArr.get(i).toString().replace("[", "");
	    	   arr = arr.replace("]", "");
	    	   a = arr.split(",");
	    	   String rep = a[0].replace("\"","");
	    	   dateT = a[0];open = a[1];high = a[2];low = a[3];close = a[4];volume = a[5];
	    	   
	    	   /*String tradeDate = rep.replace("T", " ").replace("+0530", "");
				sql = "Insert into `" + table + "_1`(tradedate, open,high, low, close,volume) values" + "('" + tradeDate + "',"
						+ open + "," + high + "," + low + "," + close + "," + volume + ")";
				executeSqlQuery(dbConnection, sql);*/
				
				if(a[0].contains("09:15:00")){
	    		   dateT = executeCountQuery(dbConnection, "select date("+dateT+")");
	    		   sql="update `"+table+"` set intraFirstMinOpen="+open+",intraFirstMinHigh="+high+","
							+ "intraFirstMinLow="+low+", intraFirstMinClose="+close+", intraFirstMinVolume="+volume+" "
							+ " where date(tradedate)=date('"+dateT+"')";
					executeSqlQuery(dbConnection, sql);
	    	   }
	    	   if(a[0].contains("09:16:00")){
	    		   dateT = executeCountQuery(dbConnection, "select date("+dateT+")");
					sql="update `"+table+"` set intraSecondMinClose="+close+", intraSecondMinVolume="+volume+" "
							+ " where date(tradedate)=date('"+dateT+"')";
					executeSqlQuery(dbConnection, sql);
	    	   }
	    	   if(a[0].contains("15:18:00")){
	    		   dateT = executeCountQuery(dbConnection, "select date("+dateT+")");
					sql="update `"+table+"` set intraday3Min3_18_close="+close+" "
							+ " where date(tradedate)=date('"+dateT+"')";
					executeSqlQuery(dbConnection, sql);
	    	   }
	    	   if(a[0].contains("15:29:00")){
	    		   dateT = executeCountQuery(dbConnection, "select date("+dateT+")");
					sql="update `"+table+"` set intradayClose="+close+" "
							+ " where date(tradedate)=date('"+dateT+"')";
					executeSqlQuery(dbConnection, sql);
	    	   }
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
