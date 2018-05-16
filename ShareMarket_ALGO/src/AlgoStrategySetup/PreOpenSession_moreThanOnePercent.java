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

public class PreOpenSession_moreThanOnePercent extends Connection {
	
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
       executeSqlQuery(dbConnection, "Update symbols set todaysopen = 0, PreOpen=0");
       for (int i = 0; i < locArr.size(); i++) {
       	
           o =  (org.json.simple.JSONObject) locArr.get(i); 
           symbol = (String) o.get("symbol");
           openPrice = Float.parseFloat(((String) o.get("iep")).replaceAll("Rs.", "").replaceAll(",", ""));
           try {
			executeSqlQuery(dbConnection, "UPDATE symbols SET openupdatedtime=now(), todaysopen="+openPrice+", LASTPRICE="+openPrice+",PreOpen="+openPrice+" where name='"+symbol+"'") ;
			executeSqlQuery(dbConnection, "UPDATE storereversal SET lastPrice="+openPrice+" where name='"+symbol+"'") ;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
       double minLimit = 2, maxLimit=10, prevBodyGap=1f;
       long transaction = 1000000l;//5000000l;
       int minOpenPrice=10;
       String bullFilter = "(yestClose-yestOpen)*100/yestOpen > "+prevBodyGap+" and "
       		+ "(yestClose-PreOpen)*100/PreOpen > "+minLimit+" and (yestClose-PreOpen)*100/PreOpen < "+maxLimit;
       String bearFilter = "(yestOpen-yestClose)*100/yestOpen > "+prevBodyGap+" and "
          		+ "(PreOpen-yestClose)*100/yestClose > "+minLimit+" and (PreOpen-yestClose)*100/yestClose < "+maxLimit;
       sql = "select todaysopen, PreOpen,"+bullFilter+" as 'Bull', "+bearFilter+" as 'Bear', s.name, s.zerodha_id from symbols s "
       		+ "where  volume > "+transaction+" and s.name not like '%&%' and PreOpen > "+minOpenPrice+" and "
       		+ " ("+bullFilter+" OR "+bearFilter+") and totalTrades > 2000 order by convert(PreOpen, SIGNED INTEGER)";
       System.out.println(sql);
       rs = executeSelectSqlQuery(dbConnection, sql);
       StringBuilder token = new StringBuilder();
	    StringBuilder instrument = new StringBuilder();
       String app = "\"";
       String dir="";
       float requiredAmountToTrade=0f;
       while(rs.next()){
    	   if(rs.isFirst()) {
    		   token.append("var symbols = [");
    	   }
    	   dir=(rs.getString("Bull").equals("1"))?"Bull": "Bear"; 
    	   token.append("["+app+rs.getString("s.name")+app+","+rs.getString("zerodha_id")+","+rs.getString("PreOpen")+","
    	   		+ "'"+dir+"']");
    	   if(rs.isLast()) {
    		   token.append("];");
    	   }else{
    		   token.append(",");
    	   }
    	   requiredAmountToTrade = requiredAmountToTrade+Float.parseFloat(rs.getString("PreOpen"));
       }
		try (BufferedReader br = new BufferedReader(new FileReader("C:/puneeth/OldLaptop/Puneeth/SHARE_MARKET/Zerodha/instruments"))) {
		    String line, out="";
		    List<String> array = new ArrayList<>();
		    while ((line = br.readLine()) != null) {
		    	if((line.contains("EQ,NSE,NSE") || line.contains("EQ,BSE,BSE")))
		    	{
		    		String arr[] = line.split(",");
		    		out = arr[2]+"_"+arr[0]+"";
		    		array.add(out);
//		    		executeSqlQuery(dbConnection, "Update symbols set zerodha_id='"+arr[0]+"' where name='"+arr[2]+"'");
		    	}
		    }
	       for(int i=0; i< array.size(); i++){
	    	   if(i==0){
	    		   instrument.append("var instrument = [");
	    	   }
	    	   instrument.append("["+app+array.get(i).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[0]+app+","+app+array.get(i).split("_")[1]
	    			   +"_"+app+"]");
	    	   if(i==array.size()-1){
	    		   instrument.append("];");
	    	   }else{
	    		   instrument.append(",");
	    	   }
	       }
		}
       System.out.println(token);
       System.out.println("");
       System.out.println(requiredAmountToTrade);
//       System.out.println("");
//       System.out.println(instrument);
       fileReader.close();

	}
	public String getPrevHighLow(java.sql.Connection dbConnection, String name) throws SQLException{
		String sql = "select concat('[\""+name+"\", \"',high,'_',low,'\"]') from "+name+" order by tradedate desc limit 1";
		return executeCountQuery(dbConnection, sql);
	}
	public void updatePreOpenPrice(java.sql.Connection dbConnection) throws SQLException{
		try {
			PreOpenSessionReadAndUpdate(dbConnection, "C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\all.json");
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
		PreOpenSession_moreThanOnePercent preopen = new PreOpenSession_moreThanOnePercent();
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
