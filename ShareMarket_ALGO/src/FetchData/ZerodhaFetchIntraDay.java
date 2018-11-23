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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

public class ZerodhaFetchIntraDay extends Connection {
	static List<String> listOfMissingSymbols = new ArrayList<>();
	boolean isInsertIntoIntra = false;//to insert intraday data to _<duration> table
	boolean isUpdateAll3MinIntraIntoDaily = true;//to update all intraday data into daily table
	boolean isUpdateOnlyIntraMaxMinFirst3MinVolume=true;//to update only max, min and first 3 min volume
	static boolean isUpdateDailyFromFile=true;
	
	public void moveFiles() throws IOException{
		String targetDir = "C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\Intraday\\3\\ALL\\";
		File dir1 = new File("C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\Intraday\\3\\");
		if(dir1.isDirectory()) {
		    File[] content = dir1.listFiles();
		    
		    for(int i = 0; i < content.length; i++) {
		        if(content[i].isDirectory()){
		        	System.out.println(content[i].getPath());
		        	if(content[i].getPath().contains("ALL") || content[i].getPath().contains("bulk")){
		        		continue;
		        	}
		        	File[] subDirFiles = new File(content[i].getPath()).listFiles();
		        	for(int j = 0; j < subDirFiles.length; j++) {
		        		String fileName = Paths.get(subDirFiles[j].getPath()).getFileName().toString();
		        		if(new File(targetDir+""+fileName).exists()){
		        			if(fileName.contains(".json")){
		        				if(fileName.contains("(")){
			        				int num = Integer.parseInt((fileName.split(".json")[0]).split(" ")[1].replace("(","").replace(")", ""));
				        			num++;
				        			boolean isFileExist=true;
				        			if(new File(targetDir+""+fileName.split(" ")[0].trim()+" ("+num+").json").exists()){
				        				while(isFileExist){
				        					num++;
				        					if(!new File(targetDir+""+fileName.split(" ")[0].trim()+" ("+num+").json").exists()){
				        						isFileExist=false;
				        					}
				        				}
				        			}
				        			fileName= fileName.split(" ")[0].trim()+" ("+num+").json";
			        			}else{
			        				int num=1;
			        				fileName = fileName.split(".json")[0]+" ("+num+").json";
			        				boolean isFileExist=true;
				        			if(new File(targetDir+""+fileName.split(" ")[0].trim()+" ("+num+").json").exists()){
				        				while(isFileExist){
				        					num++;
				        					if(!new File(targetDir+""+fileName.split(" ")[0].trim()+" ("+num+").json").exists()){
				        						isFileExist=false;
				        					}
				        				}
				        			}
				        			fileName= fileName.split(" ")[0].trim()+" ("+num+").json";
			        			}
		        			}
		        		}
		        		if(fileName.contains(".json")){
		        			Path temp = Files.move
			            	        (Paths.get(subDirFiles[j].getPath()), 
			            	        Paths.get(targetDir+""+fileName));
		        		}
		        	}
		        }
		    }
		}
	}
	public void fetchIntraDataZerodha(java.sql.Connection dbConnection, String pathToSaveBulkFile, String path, 
			String name, int duration) throws IOException, ParseException, SQLException{
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
	   Map<String, String> map = new HashMap<>();
	   Map<String, String> mapAll = new LinkedHashMap();
	   float max=0f, min=0f;
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
        	   if(isUpdateDailyFromFile){
        		   String date = rep.replace("T", " ").replace("+0530", "");
            	   if(time.equalsIgnoreCase("09:15:00")){
            		   map.put("intradayFirst3MinClose", a[4]);
            		   map.put("intradayOpen", a[1]);
            	   }else if(time.equalsIgnoreCase("09:18:00")){
            		   map.put("intradaySecond3MinClose", a[4]);
            	   }else if(time.equalsIgnoreCase("15:18:00")){
            		   map.put("intraday3Min3_18_Close", a[4]);
                	   if(map.size()==4){
                		   updateIntradayThreeMinFromFile(date, name, map);
                	   }
            	   }
        	   }
        	    
        	   if(isUpdateAll3MinIntraIntoDaily){
        		   getAll3MinMap(time, a[4], mapAll);
        		   if(time.equalsIgnoreCase("15:27:00")){
        			   String date = rep.replace("T", " ").replace("+0530", "");
        			   updateIntradayThreeMinForDailyAllFromFile(date, name, mapAll);
        			   mapAll.clear();
        		   }
        	   }
        	   if(isUpdateOnlyIntraMaxMinFirst3MinVolume)
        	   {
        		   if(time.equalsIgnoreCase("09:15:00"))
        		   {
        			   min=Float.parseFloat(a[3]);
        		   }
        		   if(Integer.parseInt(time.split(":")[0]) < 15){
    				   max= Math.max(max, Float.parseFloat(a[2]));
            		   min= Math.min(min, Float.parseFloat(a[3]));
    			   }
    			   String date = rep.replace("T", " ").replace("+0530", "");
    			   if(time.equalsIgnoreCase("15:27:00")){
        			   sql="update `"+name+"` as daily set "+
        						" intradayHigh =  '"+max+"', intradayLow='"+min+"'"+
        						" where date(daily.tradedate)=date('"+date+"')";
        			   executeSqlQuery(dbConnection, sql);
        			   max=0;min=0;
        		   }
    			   if(time.equalsIgnoreCase("09:15:00")){
    				   sql="update `"+name+"` as daily set "+
       						" intraday3Min09_15_Volume =  '"+a[5]+"'"+
       						" where date(daily.tradedate)=date('"+date+"')";
    				   executeSqlQuery(dbConnection, sql);
    			   }
        	   }
        	   
           }
    	   filter=true;
    	   if(filter==true && isInsertIntoIntra){
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
	
	public void getAll3MinMap(String time, String close, Map<String, String> mapAll){
		String s = "intraday3Min"+time.split(":")[0]+"_"+time.split(":")[1]+"_Close";
		mapAll.put(s, close);
	}
	public void updateIntraDayData(java.sql.Connection dbConnection, String name, int duration, String year, 
			boolean isMultipleJsonInsert, String path) throws SQLException{
		try {
			String pathToSaveBulkFile = "C:/puneeth/OldLaptop/Puneeth/SHARE_MARKET/Hist_Data/Intraday/"+duration+"/bulk";
			for(int i=0; i< 30; i++){
				try {
					if(isMultipleJsonInsert){
						if(i==0){
							fetchIntraDataZerodha(dbConnection, pathToSaveBulkFile, path+".json", name, duration);
						}else{
							fetchIntraDataZerodha(dbConnection, pathToSaveBulkFile, path+" ("+i+")"+".json", name, duration);
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				if(!isMultipleJsonInsert)
				{
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
	
	public List<String> getSymbolsWithZerodhaId(java.sql.Connection dbConnection, boolean isForZerodhaFetchInJS) throws SQLException{
		String sql = "SELECT s.zerodha_id, s.name FROM symbols s where volume>5000000 and s.zerodha_id is not null and s.isMargin=1"
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
	
	public boolean isDataExist(java.sql.Connection dbConnection, String symbol) throws SQLException{
		Connection con = new Connection();
		String count = con.executeCountQuery(dbConnection, "select count(*) from `"+symbol+"` where intradayHigh <> 0");
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
						" and date(daily.tradedate)>='"+startDate+"' and date(intra.tradedate)>='"+startDate+"' and time(intra.tradedate)='15:18:00') "+
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
	
	public void updateIntradayThreeMinFromFile(String date, String name, Map<String, String> map) {
		java.sql.Connection dbConnection = null;
		try {
			int dur=3;
			Connection con = new Connection();
			dbConnection = con.getDbConnection();
			ResultSet rs = null;
			String sql = "";
			sql="update `"+name+"` as daily set "+
				" daily.intradayFirst3MinClose=  "+map.get("intradayFirst3MinClose")+","+
				" daily.intradaySecond3MinClose=  "+map.get("intradaySecond3MinClose")+","+
				" daily.intraday3Min3_18_Close=  "+map.get("intraday3Min3_18_Close")+","+
				" daily.intradayOpen=  "+map.get("intradayOpen")+" "+
				" where date(daily.tradedate)=date('"+date+"')";
			executeSqlQuery(dbConnection, sql);
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
	

	public void updateIntradayThreeMinForDailyAllFromFile(String date, String name, Map<String, String> map) {
		java.sql.Connection dbConnection = null;
		try {
			int dur=3;
			Connection con = new Connection();
			dbConnection = con.getDbConnection();
			ResultSet rs = null;
			String sql = "";
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> entry: map.entrySet()){
				sb.append(" daily."+entry.getKey()+"="+entry.getValue()+" ");
				if(!entry.getKey().contains("15_27")){
					sb.append(",");
				}
			}
			sql="update `"+name+"` as daily set "+
					" "+sb.toString()+" "+
					" where date(daily.tradedate)=date('"+date+"')";
			executeSqlQuery(dbConnection, sql);
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
		String startDate="2018-08-21";
		boolean isMultipleJsonInsert=false, isForZerodhaFetchInJS=false;
		
//		preopen.moveFiles();
		try {
			List<String> list = preopen.getSymbolsWithZerodhaId(dbConnection,isForZerodhaFetchInJS);
			if(isForZerodhaFetchInJS==false){
				for (String name: list){
					String path = "C:/puneeth/OldLaptop/Puneeth/SHARE_MARKET/Hist_Data/Intraday/"+duration+"/"+startDate+"/"+name.split("_")[1];
//					if(!preopen.isTableExist(dbConnection, name+"_60")) System.out.println(name); 
//					if(!preopen.isDataExist(dbConnection, name.split("_")[1]))
					{
						preopen.updateIntraDayData(dbConnection, name.split("_")[1], duration, startDate, isMultipleJsonInsert, path);
					}
				}
				if(isUpdateDailyFromFile){
					preopen.updateIntradayThreeMin(startDate);
				}
			}
		} 
		finally{
			if(dbConnection !=null) dbConnection=null;
		}		
	}
}
