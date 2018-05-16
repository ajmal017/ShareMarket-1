package FetchData;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.httpclient.HttpException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Indicators.Connection;

public class ZerodhaAddSymbolId extends Connection {
	
	public void PreOpenSessionReadAndUpdate(java.sql.Connection dbConnection, String path) throws IOException, ParseException, SQLException{
		JSONParser parser = new JSONParser();
		String sql="";
	       
		FileReader fileReader = new FileReader(path); 
		org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(fileReader);
		org.json.simple.JSONObject locObj = (org.json.simple.JSONObject) json.get("data");
		org.json.simple.JSONArray locArr = (org.json.simple.JSONArray) locObj.get("items");
//		org.json.simple.JSONArray items = (org.json.simple.JSONArray) locArr.get("items");
		org.json.simple.JSONObject o = null;
       String symbol="", token=""; Float openPrice=0.0f;
       
//       sql= " update symbols set CCICrossover='', MACDCrossover='', MaEnvelopeCrossover='', ADXCrossover='', WilliamsCrossover='', PsarCrossover='',"
//	       		+ " CCICrossoverTime =null, MACDCrossoverTime =null, MaEnvelopeCrossoverTime =null, ADXCrossoverTime =null, WilliamsCrossoverTime =null, PSARCrossoverTime=null";
//	       executeSqlQuery(dbConnection, sql);
		for (int i = 0; i < locArr.size(); i++) {
			o = (org.json.simple.JSONObject) locArr.get(i);
			symbol = (String) o.get("tradingsymbol");
			token = (String) o.get("token");
			System.out.println(symbol);
//			try {
//				executeSqlQuery(dbConnection, "UPDATE symbols SET openupdatedtime=now(), todaysopen=" + openPrice
//						+ ", LASTPRICE=" + openPrice + " where name='" + symbol + "'");
//				executeSqlQuery(dbConnection,
//						"UPDATE storereversal SET lastPrice=" + openPrice + " where name='" + symbol + "'");
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
       fileReader.close();

	}
	public void updatePreOpenPrice(java.sql.Connection dbConnection) throws SQLException{
		try {
			PreOpenSessionReadAndUpdate(dbConnection, "C:\\Puneeth\\SHARE_MARKET\\Zerodha\\Zerodha_MARKETWATCH_1.json");
			JsonParser j = new JsonParser();
			int transactionLimit=5000000; float percAppr = 1;
			boolean isMarginReq = true;
//			j.updateReversal(dbConnection, transactionLimit, percAppr, isMarginReq);
//			j.notifyCrossedSymbols(dbConnection, transactionLimit, percAppr, isMarginReq);
			
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
		ZerodhaAddSymbolId preopen = new ZerodhaAddSymbolId();
		JsonParser js = new JsonParser();
		String count="", sql="";
		int transactionLimit=5000000; float percAppr = 1;
		boolean isMarginReq = true;
		try {
			preopen.updatePreOpenPrice(dbConnection);
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
