package AlgoStrategySetup;

import java.io.BufferedReader;
import java.io.FileOutputStream;
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
		ResultSet rs = null;
	       
		FileReader fileReader = new FileReader(path); 
		org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(fileReader);
		org.json.simple.JSONArray locArr = (org.json.simple.JSONArray) json.get("data");
		org.json.simple.JSONObject o = null;
       String symbol; Float openPrice=0.0f;
       List<String> marginSymbols = new ArrayList<>();
       
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
		System.out.println(preRequisites.getLimitStart()+","+preRequisites.getLimitEnd());
       sql = "select concat(open,'_',high,'_', low, '_', close) as NiftyPrevDayData from NIFTY_50 "
       		+ " order by tradedate desc limit 1";
       String niftyPrevDay = executeCountQuery(dbConnection, sql);
       sql = "select name, zerodha_id, avgClosePrev2day, date(lastUpdated) lastUpdated, avgQuantity, avgClosePrev3day,yestOpen,yestHigh, yestLow, yestClose, preOpen, ROUND((yestClose-preOpen)*100/preOpen,2) GapDownPerc,"
       		+ " ROUND((preOpen-yestClose)*100/yestClose,2) GapUpPerc from symbols s "
       		+ " where totalTrades >= 7000 and volume > 100000000 and isMargin=1 "
       		+ " order by volume desc limit "+preRequisites.getLimitStart()+","+preRequisites.getLimitEnd();
       rs = executeSelectSqlQuery(dbConnection, sql);
       StringBuilder token = new StringBuilder();
       token.append("var niftyData = {PrevOpen: ").append(niftyPrevDay.split("_")[0]);
       token.append(", PrevHigh: ").append(niftyPrevDay.split("_")[1]);
       token.append(", PrevLow: ").append(niftyPrevDay.split("_")[2]);
       token.append(", PrevClose: ").append(niftyPrevDay.split("_")[3]);
       token.append(", Open: ").append("''").append("}; ");
       token.append("var indexGapDiv=parseFloat(0.6); ");
       token.append("var isIndexCheck=true; var isIncludeOnlyGapCheck=true; ");
       token.append("var qtyDivisible=200, gapCheckFirst3MinBodyWithGoodVolume1 = 0.5, gapWithOpenLowOpenHighSameWithGoodVolume1=0.5; ");
       
       StringBuilder globalObject = new StringBuilder();
	    StringBuilder instrument = new StringBuilder();
       String app = "\"";
       float requiredAmountToTrade=0f;
       while(rs.next()){
    	   if(rs.isFirst()) {
    		   token.append("var symbols = [");
    	   }
    	   String n = rs.getString("name").replace("&", "%26");
    	   token.append("["+app+n+app+","+rs.getString("zerodha_id")+","+rs.getString("yestOpen")+","
    	   		+ rs.getString("yestHigh")+", "+rs.getString("yestLow")+", "+rs.getString("yestClose")+", "
   				+ ""+rs.getString("preOpen")+","+rs.getString("GapUpPerc")+","+rs.getString("GapDownPerc")+","
   				+ ""+rs.getString("avgClosePrev2day")+","+rs.getString("avgClosePrev3day")+","
   						+ ""+rs.getString("avgQuantity")+", '"+rs.getString("lastUpdated")+"']");
    	   if(rs.isLast()) {
    		   token.append("];");
    	   }else{
    		   token.append(",");
    	   }
    	   requiredAmountToTrade = requiredAmountToTrade+Float.parseFloat(rs.getString("yestClose"));
       }
       globalObject.append("var typeOfOrder='LIMIT';");
       globalObject.append("var boVarietyType='bo';");
       globalObject.append("var squareoffPerc=parseFloat(40);");
       globalObject.append("var stopLossPerc=parseFloat(3);");
       globalObject.append("var stopLossPercFromCircuit=parseFloat(2);");
       globalObject.append("var whichTab='"+preRequisites.getWhichTab()+"';");
       globalObject.append("var varietyType='bo';");
       globalObject.append("var invest=100000, qty=1;");
       globalObject.append("var entryPercFromOpen=parseFloat(0.02);");
       globalObject.append("var hour="+preRequisites.getHour()+", minute="+preRequisites.getMinute()+", second="+preRequisites.getSeconds()+";");
       globalObject.append("var gapCutoff=parseFloat(50);");
       globalObject.append("var getOnPrevHighLowCompare1=parseFloat(0.9);");
       globalObject.append("var gapWithCloseTest1=parseFloat(0.9);");
       globalObject.append("var getOnPrevCloseCompare1=parseFloat(0.9), getOnPrevCloseCompare2=parseFloat(-15);");
       globalObject.append("var getData1=parseFloat(0.9);");
       globalObject.append("var getOnNiftyPrevCloseGapCheck1=parseFloat(0.5);");
       globalObject.append("var gapCompareAvgPrevClose1=parseFloat(3);");
       globalObject.append("var gapCompareAvgPrevClose2=parseFloat(0.2);");
       globalObject.append("var isMultipleTab = false; ");
       
       globalObject.append("var otherStrategyObjects = [], availableAtCheaperPricePercFromOpen = parseInt(3); ");
       globalObject.append("var min=parseFloat(20), max=parseFloat(30000);");
       globalObject.append("var csrfToken='"+preRequisites.getCsrfToken()+"';");
       globalObject.append("var accessToken='"+preRequisites.getCsrfToken()+"';");
       
       globalObject.append("var isPlaceOrder=true;var globalResponse='';");
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
       
       FileOutputStream out = new FileOutputStream("C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\prereq.txt");
       out.write(globalObject.toString().getBytes());
       out.close();
//       System.out.println(requiredAmountToTrade);
       
//       System.out.println("");
//       System.out.println(instrument);
//       fileReader.close();
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
		Selenium sel = new Selenium();
		int transactionLimit=5000000; float percAppr = 1;
		PreRequisites preRequisites = new PreRequisites();
		preRequisites.setCsrfToken("ipUVRGI7cN3MC7GzUe6dcURpJT3twvB9");
		preRequisites.setUpstox_access_token("368369043ca0628917dca28c976735681fa3eacf");
		preRequisites.setHour(sel.entryHour);
		preRequisites.setMinute(sel.entryMinute);preRequisites.setSeconds(sel.entrySecond);
		preRequisites.setMarginMultiplier(sel.marginMultiplier);
		preRequisites.setLimitStart(0);
		preRequisites.setLimitEnd(1000);
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
