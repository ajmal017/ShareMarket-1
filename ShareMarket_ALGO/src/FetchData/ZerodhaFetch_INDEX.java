package FetchData;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.httpclient.HttpException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Indicators.Connection;

public class ZerodhaFetch_INDEX extends Connection {
	
	public void fetchIntraDataZerodha(java.sql.Connection dbConnection, String path, String name) throws IOException, ParseException, SQLException{
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
       System.out.println(name);
       for (int i = 0; i < locArr.size(); i++) {
    	   arr= locArr.get(i).toString().replace("[", "");
    	   arr = arr.replace("]", "");
    	   a = arr.split(",");
    	   a[0].toString().replace("T", " ").replace("+0530", "");
    	   sql = "Insert into "+name+"(tradedate, open,high, low, close) values"
    	   		+ "("+a[0].toString().replace("T", " ").replace("+0530", "")+","+a[1]+","+a[2]+","+a[3]+","+a[4]+")";
    	   executeSqlQuery(dbConnection, sql);
       }
       
       fileReader.close();

	}
	public void updateIntraDayData(java.sql.Connection dbConnection) throws SQLException{
		try {
			fetchIntraDataZerodha(dbConnection, "C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\index\\Nifty50_3.json", "Nifty_50");
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String []a){
		java.sql.Connection dbConnection=null;
		Connection con = new Connection();
		dbConnection = con.getDbConnection();
		int incr = 10; int bulkSendCount=10;
		boolean updatePreOpenAsOpenPriceByGoogle=true;
		ZerodhaFetch_INDEX preopen = new ZerodhaFetch_INDEX();
		JsonParser js = new JsonParser();
		String count="", sql="";
		int transactionLimit=5000000; float percAppr = 1;
		boolean isMarginReq = true;
		try {
			preopen.updateIntraDayData(dbConnection);
		} 
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(dbConnection !=null) dbConnection=null;
		}		
	}
}
