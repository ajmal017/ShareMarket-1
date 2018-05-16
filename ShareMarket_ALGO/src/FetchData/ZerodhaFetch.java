package FetchData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Indicators.Connection;

public class ZerodhaFetch extends Connection {
	
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
    	   sql = "Insert into "+name+"_5(tradedate, open,high, low, close, volume, totalQty) values"
    	   		+ "("+a[0].toString().replace("T", " ").replace("+0530", "")+","+a[1]+","+a[2]+","+a[3]+","+a[4]+","+a[5]+","+a[5]+")";
    	   executeSqlQuery(dbConnection, sql);
       }
       
       fileReader.close();

	}
	public void updateIntraDayData(java.sql.Connection dbConnection) throws SQLException{
		try {
			fetchIntraDataZerodha(dbConnection, "C:\\Puneeth\\SHARE_MARKET\\Zerodha\\JINDALSTEL_4.json", "Jindalstel");
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void makeCall() throws IOException{
		URL url = new URL("https://kitecharts.zerodha.com/api/chart/779521/60minute?public_token=anyUmBXWSkUGZiZoCBSBD4iTt1VsM2y6&user_id=DP3137&api_key=kitefront&access_token=0V0iqoS4dTdD3ZAjrJQqbgNhho6iz7qr&from=2018-04-01&to=2018-04-07&ciqrandom=1523099476446");
//        String query = INSERT_HERE_YOUR_URL_PARAMETERS;

        //make connection
        URLConnection urlc = url.openConnection();

//        //use post mode
//        urlc.setDoOutput(true);
//        urlc.setAllowUserInteraction(false);


        //get result
        BufferedReader br = new BufferedReader(new InputStreamReader(urlc
            .getInputStream()));
        String l = null;
        while ((l=br.readLine())!=null) {
            System.out.println(l);
        }
        br.close();
	}
	public static void main(String []a) throws IOException, SQLException{
		java.sql.Connection dbConnection=null;
		Connection con = new Connection();
		dbConnection = con.getDbConnection();
		int incr = 10; int bulkSendCount=10;
		boolean updatePreOpenAsOpenPriceByGoogle=true;
		ZerodhaFetch preopen = new ZerodhaFetch();
		JsonParser js = new JsonParser();
		String count="", sql="";
		int transactionLimit=5000000; float percAppr = 1;
		boolean isMarginReq = true;
		try {
			preopen.updateIntraDayData(dbConnection);
//			preopen.makeCall();
		} 
		finally{
			if(dbConnection !=null) dbConnection=null;
		}

		
	}
}
