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

import AlgoStrategySetup.Model.PreRequisites;
import FetchData.JsonParser;
import Indicators.Connection;

public class PreOpenSession_TodaysOpenRespectToYestClose extends Connection {
	
	public StringBuilder PreOpenSessionReadAndUpdate(java.sql.Connection dbConnection, String path,
			PreRequisites preRequisites) throws IOException, ParseException, SQLException{
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
       sql = "select name, zerodha_id, yestOpen,yestHigh, yestLow, yestClose, preOpen from symbols s "
       		+ "where volume>50000000 and name !='VAKRANGEE' and name!='PCJEWELLER' and name!='LIQUIDBEES' "
       		+ " and isMargin='1' order by volume desc ";
       rs = executeSelectSqlQuery(dbConnection, sql);
       StringBuilder token = new StringBuilder();
       StringBuilder globalObject = new StringBuilder();
	    StringBuilder instrument = new StringBuilder();
       String app = "\"";
       float requiredAmountToTrade=0f;
       while(rs.next()){
    	   if(rs.isFirst()) {
    		   token.append("var symbols = [");
    	   }
    	   token.append("["+app+rs.getString("name")+app+","+rs.getString("zerodha_id")+","+rs.getString("yestOpen")+","
    	   		+ rs.getString("yestHigh")+", "+rs.getString("yestLow")+", "+rs.getString("yestClose")+", "+rs.getString("preOpen")+"]");
    	   if(rs.isLast()) {
    		   token.append("];");
    	   }else{
    		   token.append(",");
    	   }
    	   requiredAmountToTrade = requiredAmountToTrade+Float.parseFloat(rs.getString("yestClose"));
       }
       globalObject.append("var typeOfOrder='LIMIT';");
       globalObject.append("var varietyType='regular';");
       globalObject.append("var invest=100000, qty=1;");
       globalObject.append("var entryPercFromOpen=parseFloat(0.05);");
       globalObject.append("var hour="+preRequisites.getHour()+", minute="+preRequisites.getMinute()+", second="+preRequisites.getSeconds()+";");
       globalObject.append("var gapCutoff=parseFloat(9);");
       globalObject.append("var getOnPrevHighLowCompare1=parseFloat(0.9);");
       globalObject.append("var gapWithCloseTest1=parseFloat(1.5);");
       globalObject.append("var getOnPrevCloseCompare1=parseFloat(1.5), getOnPrevCloseCompare2=parseFloat(-15);");
       globalObject.append("var getData1=parseFloat(1.5);"); 
       globalObject.append("var getOnNiftyPrevCloseGapCheck1=parseFloat(0.9);");
       
       globalObject.append("var min=parseFloat(20), max=parseFloat(5000);");
       globalObject.append("var csrfToken='"+preRequisites.getCsrfToken()+"';");
       globalObject.append("var accessToken='OBlu2jwfss9L7yMguMt5lwR3h46denJO';");
       
       globalObject.append("var isPlaceOrder=false;var globalResponse='';");
       globalObject.append("var upstoxAccessToken='"+preRequisites.getUpstox_access_token()+"';");
       globalObject.append("var upstoxApiKey='dPMbue9lq7abjTPCeuJ0Y8tYNEXdwKDd3OQiashl';");
       globalObject.append("var marginMultipler='"+preRequisites.getMarginMultiplier()+"';");
       globalObject.append("var niftyOpen=0, niftyLastClose=0, eqExchange='nse_eq', indexExchange='nse_index';");
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
       System.out.println(globalObject);
       globalObject.append(token);
       System.out.println("");
//       System.out.println(requiredAmountToTrade);
       
//       System.out.println("");
//       System.out.println(instrument);
       fileReader.close();
       return globalObject;
	}
	public String getPrevHighLow(java.sql.Connection dbConnection, String name) throws SQLException{
		String sql = "select concat('[\""+name+"\", \"',high,'_',low,'\"]') from "+name+" order by tradedate desc limit 1";
		return executeCountQuery(dbConnection, sql);
	}
	public StringBuilder updatePreOpenPrice(java.sql.Connection dbConnection, PreRequisites preRequisites) throws SQLException{
		try {
			return PreOpenSessionReadAndUpdate(dbConnection, "C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\all.json", preRequisites);
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static void main(String []a){
		java.sql.Connection dbConnection=null;
		Connection con = new Connection();
		dbConnection = con.getDbConnection();
		int incr = 10; int bulkSendCount=10;
		boolean updatePreOpenAsOpenPriceByGoogle=true;
		PreOpenSession_TodaysOpenRespectToYestClose preopen = new PreOpenSession_TodaysOpenRespectToYestClose();
		JsonParser js = new JsonParser();
		String count="", sql="";
		int transactionLimit=5000000; float percAppr = 1;
		PreRequisites preRequisites = new PreRequisites();
		preRequisites.setCsrfToken("OBlu2jwfss9L7yMguMt5lwR3h46denJO");
		boolean isMarginReq = true;
		try {
			preopen.updatePreOpenPrice(dbConnection, preRequisites);
			
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
