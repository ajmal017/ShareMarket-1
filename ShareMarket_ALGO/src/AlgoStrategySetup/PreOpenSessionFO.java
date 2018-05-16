package AlgoStrategySetup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import FetchData.JsonParser;
import Indicators.Connection;

public class PreOpenSessionFO extends Connection {
	
	public void PreOpenSessionReadAndUpdate(java.sql.Connection dbConnection, String path) throws IOException, ParseException, SQLException{
		JSONParser parser = new JSONParser();
		String sql="";
	       
		FileReader fileReader = new FileReader(path); 
		org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(fileReader);
		org.json.simple.JSONArray locArr = (org.json.simple.JSONArray) json.get("data");
		org.json.simple.JSONObject o = null;
       String symbol; Float openPrice=0.0f;
       List<String> marginSymbols = new ArrayList<>();
       ResultSet rs = null;
       executeSqlQuery(dbConnection, "Update symbols set todaysopen = 0");
       sql= " update symbols set CCICrossover='', MACDCrossover='', MaEnvelopeCrossover='', ADXCrossover='', WilliamsCrossover='', PsarCrossover='',"
	       		+ " CCICrossoverTime =null, MACDCrossoverTime =null, MaEnvelopeCrossoverTime =null, ADXCrossoverTime =null, WilliamsCrossoverTime =null, PSARCrossoverTime=null";
	       executeSqlQuery(dbConnection, sql);
       for (int i = 0; i < locArr.size(); i++) {
       	
           o =  (org.json.simple.JSONObject) locArr.get(i); 
           symbol = (String) o.get("symbol");
           openPrice = Float.parseFloat(((String) o.get("iep")).replaceAll("Rs.", "").replaceAll(",", ""));
           try {
			executeSqlQuery(dbConnection, "UPDATE symbols SET openupdatedtime=now(), todaysopen="+openPrice+", LASTPRICE="+openPrice+" where name='"+symbol+"'") ;
			executeSqlQuery(dbConnection, "UPDATE storereversal SET lastPrice="+openPrice+" where name='"+symbol+"'") ;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
//       and m.fut_avg_volume*m.fut_lot_size*m.fut_open > 2000000000
		rs = executeSelectSqlQuery(dbConnection,
				"select todaysopen, s.name, zerodha_id "
				+ "from symbols s, margintables m where m.name=s.name "
						+ "  and s.name not like '%&%' and abs(todaysopen-LastPrice)*100/LastPrice <= 0.5");
		StringBuilder preopen = new StringBuilder();
		String app = "\"";
		List<String> eqSymbols = new ArrayList<>();
		while (rs.next()) {
			eqSymbols.add(rs.getString("s.name"));
			if (rs.isFirst()) {
				preopen.append("var preopen = [");
			}
			preopen.append("[" + app + rs.getString("s.name") + app + "," + rs.getString("todaysopen") + "]");
			if (rs.isLast()) {
				preopen.append("];");
			} else {
				preopen.append(",");
			}
		}
        
       StringBuilder prevHighLow = new StringBuilder();
	    StringBuilder instrument = new StringBuilder();
       
		try (BufferedReader br = new BufferedReader(new FileReader("C:\\Puneeth\\SHARE_MARKET\\Zerodha\\instruments"))) {
		    String line, out="";
		    List<String> array = new ArrayList<>();
		    List<String> tradableSymbols = new ArrayList<>();
		    while ((line = br.readLine()) != null) {
		    	if(line.contains("NFO-FUT") && line.contains("MAR") && !line.contains("&")){
		    		String arr[] = line.split(",");
		    		out = arr[2]+"_"+arr[0]+"_"+arr[8];
		    		array.add(out);
		    	}
		    }
		    String name="";
		    for(int i=0; i< eqSymbols.size(); i++){
		    	for(int j=0; j< array.size(); j++){
		    		name = array.get(j).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[0];
		    		if(name.equalsIgnoreCase(eqSymbols.get(i))){
		    			tradableSymbols.add(array.get(j));
		    			break;
		    		}
		    	}
		    }
//		    System.out.println(tradableSymbols.size());
//		    System.out.println(eqSymbols.size());
	       for(int i=0; i< tradableSymbols.size(); i++){
	    	   if(i==0){
	    		   prevHighLow.append("var sym_HighLow = [");
	    		   instrument.append("var instrument = [");
	    	   }
	    	   
 	    	   name = tradableSymbols.get(i).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[0];
 	    	   sql = "update "+name+"_FUT set lotsize='"+tradableSymbols.get(i).split("_")[2]+"'";
	    	   executeSqlQuery(dbConnection, sql);
	    	   prevHighLow.append(getPrevHighLow(dbConnection, name));
	    	   instrument.append("["+app+name+app+","+app+tradableSymbols.get(i).split("_")[1]
	    			   +"_"+tradableSymbols.get(i).split("_")[2]+app+","+app+tradableSymbols.get(i).split("_")[0]+app+"]");
	    	   if(i==tradableSymbols.size()-1){
	    		   prevHighLow.append("];");
	    		   instrument.append("];");
	    	   }else{
	    		   prevHighLow.append(",");
	    		   instrument.append(",");
	    	   }
	       }
		}
		System.out.println("");
       System.out.println(preopen);
//       System.out.println("");
//       System.out.println(prevHighLow);
       System.out.println("");
       System.out.println(instrument);
       fileReader.close();

	}
	public String getPrevHighLow(java.sql.Connection dbConnection, String name){
		String sql = "select concat('[\""+name+"\", \"',high,'_',low,'\"]') from "+name+"_FUT order by tradedate desc limit 1,1";
		return executeCountQuery(dbConnection, sql);
	}
	public void updatePreOpenPrice(java.sql.Connection dbConnection) throws SQLException{
		try {
			PreOpenSessionReadAndUpdate(dbConnection, "C:\\Puneeth\\SHARE_MARKET\\all.json");
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
		PreOpenSessionFO preopen = new PreOpenSessionFO();
		JsonParser js = new JsonParser();
		String count="", sql="";
		int transactionLimit=5000000; float percAppr = 1;
		boolean isMarginReq = true;
		try {
			preopen.updatePreOpenPrice(dbConnection);
			
//			count = con.executeCountQuery(dbConnection, "select count(*) from symbols where volume*todaysopen>"+transactionLimit+" order by volume desc");
//			System.out.println(count);
//			sql = "select s.name from symbols s where volume*todaysopen>"+transactionLimit+" order by volume desc ";
//			js.getCommaList(dbConnection, Integer.parseInt(count), incr, bulkSendCount, updatePreOpenAsOpenPriceByGoogle, sql);
//			js.notifyCrossedSymbols(dbConnection, transactionLimit, percAppr, isMarginReq);
			
		} 
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		catch (HttpException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		finally{
			if(dbConnection !=null) dbConnection=null;
		}

		
	}
}
