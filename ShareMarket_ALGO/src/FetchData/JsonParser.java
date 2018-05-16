package FetchData;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Indicators.Connection;
import Indicators.Notifier;

public class JsonParser extends Connection implements Runnable{
	Notifier n = new Notifier();
	public static void main(String []a){
	  	
	  	while(1==1){
	  		Thread  t = new Thread(new JsonParser());
	  		t.start();
	  		try {
				t.sleep(60000*100);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  	}
		
		
	}
	@Override
	public void run() {
		JsonParser j = new JsonParser();
		java.sql.Connection dbConnection=null;
		int incr = 100; int bulkSendCount=100;
		Connection con = new Connection();
	  	dbConnection = con.getDbConnection();
	  	boolean updatePreOpenAsOpenPriceByGoogle=false;
	  	boolean isMarginReq = true;
	  	String count="", join="";
	  	int transactionLimit=1000; float percAppr = .5f;
		try {
			if(isMarginReq==true){
				join = " Inner join margintables m on m.name = s.name ";
			}
			String sql = "select s.name from symbols s "+join+" where volume>"+transactionLimit+" order by s.name asc ";
			count = con.executeCountQuery(dbConnection, "select count(*) from symbols s "+join+" where volume>"+transactionLimit+" order by s.name asc");
//			System.out.println("count="+count);
			try {
				j.getCommaList(dbConnection, Integer.parseInt(count), incr, bulkSendCount, updatePreOpenAsOpenPriceByGoogle,sql);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			notifyCrossedSymbols(dbConnection, transactionLimit, percAppr, isMarginReq);
            //updateReversal(dbConnection, transactionLimit, percAppr);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public String getCommaList(java.sql.Connection dbConnection, int totalCount, int incr, int bulkSendCount, boolean updatePreOpenAsOpenPriceByGoogle
			, String sql) throws SQLException, HttpException, IOException{
		String  commaList="";
		ResultSet rs=null;boolean ret;
		String StrSql=sql;
		for (int i=0; i < totalCount;i=i+incr){
			//inner join margintables m on m.name=s.name
			if(StrSql.equals("")){
				sql = "SELECT s.name FROM symbols s  where s.name not like '%&%' limit "+(i)+", "+bulkSendCount;
			}else{
				sql = StrSql + " limit "+(i)+", "+bulkSendCount;
			}
			
			System.out.println(sql);
			rs = executeSelectSqlQuery(dbConnection, sql);
			String name="", nseName="";
			while (rs.next()){
				name = "nse:"+rs.getString("s.name");
				nseName = nseName + rs.getString("s.name")+",";
				commaList = commaList +name+",";
			}
//			System.out.println(commaList);
			ret = makeCall(dbConnection, commaList, updatePreOpenAsOpenPriceByGoogle);
//			ret = makeCallNSE(dbConnection, nseName);
			
//			if(ret==false){
//				System.out.println("failed on exception");
//			}
			commaList="";sql="";
		}
			
		return commaList;
	}

	
	
	
public boolean makeCall(java.sql.Connection dbConnection, String commaList, boolean updatePreOpenAsOpenPriceByGoogle) throws HttpException, IOException{
		
        String line="";
        String line2="";
        String xmlData="";
        try{
        	String request = "http://api.search.yahoo.com/WebSearchService/V1/webSearch?appid=YahooDemo&query=umbrella&results=10";
    		request = "http://finance.google.com/finance/info?client=ig&q="+commaList+"";
    		
    		
            HttpClient client = new HttpClient();
            GetMethod method = new GetMethod(request);
            
            // Send GET request
            int statusCode = client.executeMethod(method);
            
            if (statusCode != HttpStatus.SC_OK) {
            	System.err.println("Method failed: " + method.getStatusLine());
            }
            InputStream rstream = null;
            
            // Get the response body
            rstream = method.getResponseBodyAsStream();
            Map a;
            
            // Process the response from Yahoo! Web Services
            BufferedReader br = new BufferedReader(new InputStreamReader(rstream));
            
            while ((line = br.readLine()) != null) {
                
                line2 = line2 + line;
                
            }
//            System.out.println(line2);
//            line2 = line2.replace("[", ""); line2 = line2.replace("]", ""); 
            line2 = line2.replace("//", "");
//            System.out.println(line2);
            line2 = "{data:"+line2+"}";
//            System.out.println(line2);
            org.json.JSONObject jsonObject = new org.json.JSONObject(line2);
//            System.out.println(jsonObject.getString("t"));
            JSONArray locArr = jsonObject.getJSONArray("data"); 
            JSONObject o = null;
//            System.out.println(locArr.length());
            String symbol; Float curPrice=0.0f;
            for (int i = 0; i < locArr.length(); i++) {
            	
                o =  (JSONObject) locArr.get(i); 
                symbol = o.getString("t");
                
                curPrice = Float.parseFloat(o.getString("l_cur").replaceAll("Rs.", "").replaceAll(",", ""));
                System.out.println(symbol + " ,"+curPrice);
                if(updatePreOpenAsOpenPriceByGoogle == true){
                	executeSqlQuery(dbConnection, "UPDATE symbols SET openupdatedtime=now(), todaysopen="+curPrice+", LASTPRICE="+curPrice+" where name='"+symbol+"'") ;
                }else{
                	executeSqlQuery(dbConnection, "UPDATE symbols SET LASTPRICE="+curPrice+" where name='"+symbol+"'") ;
                	executeSqlQuery(dbConnection, "UPDATE storereversal SET LASTPRICE="+curPrice+" where name='"+symbol+"'") ;
                }
                
                
             }
           
            br.close();
            if(rstream!=null)
            rstream.close();
        }
        catch(Exception e){
        	System.out.println("sssss");
        	e.printStackTrace();
        	return false;
        }
    	
        return true;
	}


	
	public void notifyCrossedSymbols(java.sql.Connection con, int transactionLimit, float percApprLimit, boolean isMarginReq) throws SQLException{
		ResultSet rs=null;
		String sql="", join="", notifymessage="";
		
		//update reversal in symbols table starts
		
		if(isMarginReq==true){
			join = " Inner join margintables m on m.name = s.name ";
		}
		sql = "UPDATE  SYMBOLS s "+join+
" SET  PSARCrossoverTime =  CASE WHEN ((next_day_psar-lastprice) <0 and lastprice<>0 and todaysopen<next_day_psar and prevdirection=-1 and PsarCrossover='') THEN now() else null END,"
+ "PsarCrossover ="+
" CASE WHEN ((next_day_psar-lastprice) <0 and lastprice<>0 and todaysopen<next_day_psar and prevdirection=-1 and PsarCrossover='') THEN 'BULLISH' else PsarCrossover END";
		executeSqlQuery(con, sql);
		
		sql = "UPDATE  SYMBOLS s "+join+
" SET  PSARCrossoverTime = CASE WHEN ((lastprice-next_day_psar) <0  and lastprice<>0 and todaysopen>next_day_psar and prevdirection=1 and PsarCrossover='') THEN now() else null END, "+
" PsarCrossover ="+
" CASE WHEN ((lastprice-next_day_psar) <0  and lastprice<>0 and todaysopen>next_day_psar and prevdirection=1 and PsarCrossover='') THEN 'BEARISH' else PsarCrossover END";
		executeSqlQuery(con, sql);
		
		//adx
		sql = "UPDATE  SYMBOLS s "+join+
" SET ADXCrossoverTime = CASE WHEN ((last_High-lastprice) <0 and lastprice<>0 and todaysopen<last_high and adx_dm_crossovr='Bull' and ADXCrossover='') THEN now() else null END, "+
" ADXCrossover ="+
" CASE WHEN ((last_High-lastprice) <0 and lastprice<>0 and todaysopen<last_high and adx_dm_crossovr='Bull' and ADXCrossover='') THEN 'BULL' else ADXCrossover END";
		executeSqlQuery(con, sql);
//		
		sql = "UPDATE  SYMBOLS s "+join+
" SET ADXCrossoverTime=CASE WHEN ((lastprice-last_low) <0  and lastprice<>0 and todaysopen>last_low and adx_dm_crossovr='Bear' and ADXCrossover='') THEN now() else null END, "+
" ADXCrossover ="+
" CASE WHEN ((lastprice-last_low) <0  and lastprice<>0 and todaysopen>last_low and adx_dm_crossovr='Bear' and ADXCrossover='') THEN 'BEAR' else ADXCrossover END";
		executeSqlQuery(con, sql);
		
		//macd
		sql = "UPDATE  SYMBOLS s "+join+
" SET MACDCrossoverTime = CASE WHEN ((last_High-lastprice) <0 and lastprice<>0 and todaysopen<last_high and (MACD_DIR='UP' or MACD_HIST_CROSS='Bullish Cross') and MACDCrossover='') THEN now() else null END, "+
" MACDCrossover ="+
" CASE WHEN ((last_High-lastprice) <0 and lastprice<>0 and todaysopen<last_high and (MACD_DIR='UP' or MACD_HIST_CROSS='Bullish Cross') and MACDCrossover='') THEN 'BULL' else MACDCrossover END";
		executeSqlQuery(con, sql);
//				
		sql = "UPDATE  SYMBOLS s "+join+
" SET MACDCrossoverTime=CASE WHEN ((lastprice-last_low) <0  and lastprice<>0 and todaysopen>last_low and (MACD_DIR='DOWN' or MACD_HIST_CROSS='Bearish Cross') and MACDCrossover='') THEN now() else null END,"+
" MACDCrossover ="+
" CASE WHEN ((lastprice-last_low) <0  and lastprice<>0 and todaysopen>last_low and (MACD_DIR='DOWN' or MACD_HIST_CROSS='Bearish Cross') and MACDCrossover='') THEN 'BEAR' else MACDCrossover END";
		executeSqlQuery(con, sql);
		
		//williams
		sql = "UPDATE  SYMBOLS s "+join+
" SET WilliamsCrossoverTime= CASE WHEN ((last_High-lastprice) <0 and lastprice<>0 and todaysopen<last_high and (Williams_Reversal='Bull') and WilliamsCrossover='') THEN now() else null END,"+
" WilliamsCrossover = "+
" CASE WHEN ((last_High-lastprice) <0 and lastprice<>0 and todaysopen<last_high and (Williams_Reversal='Bull') and WilliamsCrossover='') THEN 'BULL' else WilliamsCrossover END";
		executeSqlQuery(con, sql);
//				
		sql = "UPDATE  SYMBOLS s "+join+
" SET WilliamsCrossoverTime = CASE WHEN ((lastprice-last_low) <0  and lastprice<>0 and todaysopen>last_low and (Williams_Reversal='Bear') and WilliamsCrossover='') THEN now() else null END, "+
" WilliamsCrossover ="+
" CASE WHEN ((lastprice-last_low) <0  and lastprice<>0 and todaysopen>last_low and (Williams_Reversal='Bear') and WilliamsCrossover='') THEN 'BEAR' else WilliamsCrossover END";
		executeSqlQuery(con, sql);
						
		//CCI
		sql = "UPDATE  SYMBOLS s "+join+
" SET CCICrossoverTime=CASE WHEN ((last_High-lastprice) <0 and lastprice<>0 and todaysopen<last_high and (CCI_Reverse='Bull') and CCICrossover='') THEN now() else null END,"+
" CCICrossover ="+
" CASE WHEN ((last_High-lastprice) <0 and lastprice<>0 and todaysopen<last_high and (CCI_Reverse='Bull') and CCICrossover='') THEN 'BULL' else CCICrossover END";
		executeSqlQuery(con, sql);
//				
		sql = "UPDATE  SYMBOLS s "+join+
" SET CCICrossoverTime=CASE WHEN ((lastprice-last_low) <0  and lastprice<>0 and todaysopen>last_low and (CCI_Reverse='Bear') and CCICrossover='') THEN now() else null END, "+
" CCICrossover ="+
" CASE WHEN ((lastprice-last_low) <0  and lastprice<>0 and todaysopen>last_low and (CCI_Reverse='Bear') and CCICrossover='') THEN 'BEAR' else CCICrossover END";
		executeSqlQuery(con, sql);
		
		//MA envelope
		sql = "UPDATE  SYMBOLS s "+join+
" SET MaEnvelopeCrossoverTime=CASE WHEN ((last_High-lastprice) <0 and lastprice<>0 and todaysopen<last_high and (MA_ENVLP_Cross='Bull') and MaEnvelopeCrossover='') THEN now() else null END, "+
" MaEnvelopeCrossover ="+
" CASE WHEN ((last_High-lastprice) <0 and lastprice<>0 and todaysopen<last_high and (MA_ENVLP_Cross='Bull') and MaEnvelopeCrossover='') THEN 'BULL' else MaEnvelopeCrossover END";
		executeSqlQuery(con, sql);
//				
		sql = "UPDATE  SYMBOLS s "+join+
" SET MaEnvelopeCrossoverTime= CASE WHEN ((lastprice-last_low) <0  and lastprice<>0 and todaysopen>last_low and (MA_ENVLP_Cross='Bear') and MaEnvelopeCrossover='') THEN now() else null END, "+
" MA_ENVLP_Cross ="+
" CASE WHEN ((lastprice-last_low) <0  and lastprice<>0 and todaysopen>last_low and (MA_ENVLP_Cross='Bear') and MaEnvelopeCrossover='') THEN 'BEAR' else MaEnvelopeCrossover END";
		executeSqlQuery(con, sql);
						
//		System.out.println(sql);
//		/update reversal in symbols table ends
		
		//notification started-----------------------------------------------------------------------------------
		//psar started
		sql = "select s.name, truncate((next_day_psar-lastprice)*100/lastprice,2) as approach, next_day_psar,lastprice, (lastprice >= next_day_psar) as Crossed from symbols as s "+join+" where volume*todaysopen>"+transactionLimit+" "+
				" and prevdirection=-1 and (next_day_psar-lastprice)*100/lastprice <= "+percApprLimit+" and (next_day_psar-lastprice)*100/lastprice>0 and PsarCrossover='' order by volume*todaysopen desc"+
				" ";
//		System.out.println(sql);
		rs = executeSelectSqlQuery(con, sql);
		String notify="", table="<table width=100% border=1>";
		while (rs.next()){
			if(notify.equals("")){
				notify = "<font color=red>PSAR Bull Appr</font>";
			}
			if(rs.getInt("Crossed")==0)
				notify = notify+rs.getString("s.name")+" ["+rs.getString("approach")+"%] TRIG["+rs.getString("next_day_psar")+"] LP["+rs.getString("lastprice")+"], ";
			
			
		}
		rs.close();
		if(!notify.equals("")){
			notifymessage =notifymessage+ table+"<tr><td>"+notify+"</td></tr>";
			notify="";table="";
		}
		
		
		sql = "select s.name, truncate((lastprice-next_day_psar)*100/lastprice,2) as approach, next_day_psar, lastprice, (lastprice <= next_day_psar) as Crossed from symbols as s "+join+"  where volume*todaysopen>"+transactionLimit+" "+
				" and prevdirection=1 and (lastprice-next_day_psar)*100/lastprice <= "+percApprLimit+" and (lastprice-next_day_psar)*100/lastprice > 0 and PsarCrossover='' "+
				"  order by volume*todaysopen desc " ;
		rs = executeSelectSqlQuery(con, sql);
		while (rs.next()){
			if(notify.equals("")){
				notify = "<font color=red>PSAR Bear Appr</font>";
			}
			
			if(rs.getInt("Crossed")==0)
				notify = notify+rs.getString("s.name")+" ["+rs.getString("approach")+"%] TRIG["+rs.getString("next_day_psar")+"] LP["+rs.getString("lastprice")+"], ";
			
		}
		rs.close();
		if(!notify.equals("")){
			notifymessage =notifymessage+ "<tr><td>"+notify+"</td></tr>";
			notify="";table="";
		}
		
		//psar ends
		
		
		//adx notify starts
		
		sql = "select s.name, truncate((Last_high-lastprice)*100/lastprice,2) as approach, lastprice,Last_high,  (lastprice >= Last_high) as Crossed from symbols as s "+join+"  "+
				" where ADXCrossover='' and (ADX_DM_Crossovr='Bull' ) and volume*todaysopen>"+transactionLimit+" and ((Last_high-lastprice)*100/lastprice <= "+percApprLimit+" or "+
				" lastprice >= Last_high) order by volume*todaysopen desc ";
		rs = executeSelectSqlQuery(con, sql);
//		System.out.println(sql);
		while (rs.next()){
			if(notify.equals("")){
				notify = "<font color=red>ADX Bull </font>";
			}
			if(rs.getInt("Crossed")==0)
				notify = notify+rs.getString("s.name")+" ["+rs.getString("approach")+"%] LP["+rs.getString("lastprice")+"] TRIG["+rs.getString("Last_high")+"], ";
			
			notify =notify+"/";
			
		}
		rs.close();
		
		if(!notify.equals("")){
			notifymessage =notifymessage+ "<tr><td>"+notify+"</td></tr>";
			notify="";table="";
		}
		
		sql = "select s.name, truncate((lastprice-Last_Low)*100/lastprice,2) as approach, lastprice,Last_Low,  (lastprice <= Last_Low) as Crossed from symbols as s "+join+"  "+
				" where ADXCrossover='' and (ADX_DM_Crossovr='Bear' ) and volume*todaysopen>"+transactionLimit+" and ((lastprice-Last_Low)*100/lastprice <= "+percApprLimit+" or "+
				" lastprice <= Last_Low) order by volume*todaysopen desc ";
		rs = executeSelectSqlQuery(con, sql);
//		System.out.println(sql);
		while (rs.next()){
			if(notify.equals("")){
				notify = "<font color=red>ADX Bear </font>";
			}
			if(rs.getInt("Crossed")==0)
				notify = notify+rs.getString("s.name")+" ["+rs.getString("approach")+"%] LP["+rs.getString("lastprice")+"] TRIG["+rs.getString("Last_Low")+"], ";
			
			notify =notify+"/";
			
		}
		rs.close();
		

		if(!notify.equals("")){
			notifymessage =notifymessage+ "<tr><td>"+notify+"</td></tr>";
			notify="";table="";
		}

		//adx notify ends
		//Williams notify starts
		sql = "select s.name, truncate((Last_high-lastprice)*100/lastprice,2) as approach, lastprice,Last_high,  (lastprice >= Last_high) as Crossed from symbols as s "+join+"  "+
				" where WilliamsCrossover='' and (Williams_Reversal='Bull' ) and volume*todaysopen>"+transactionLimit+" and ((Last_high-lastprice)*100/lastprice <= "+percApprLimit+" or "+
				" lastprice >= Last_high) order by volume*todaysopen desc ";
		
		
		rs = executeSelectSqlQuery(con, sql);
		while (rs.next()){
			if(notify.equals("")){
				notify = "<font color=red>Williams Bull </font>";
			}
			if(rs.getInt("Crossed")==0)
				notify = notify+rs.getString("s.name")+" ["+rs.getString("approach")+"%] LP["+rs.getString("lastprice")+"] TRIG["+rs.getString("Last_high")+"], ";
			
			notify =notify+"/";
		}
		rs.close();
		
		if(!notify.equals("")){
			notifymessage =notifymessage+ "<tr><td>"+notify+"</td></tr>";
			notify="";table="";
		}
		sql = "select s.name, truncate((lastprice-Last_Low)*100/lastprice,2) as approach, lastprice,Last_Low,  (lastprice <= Last_Low) as Crossed from symbols as s "+join+"  "+
				" where WilliamsCrossover='' and (Williams_Reversal='Bear' ) and volume*todaysopen>"+transactionLimit+" and ((lastprice-Last_Low)*100/lastprice <= "+percApprLimit+" or "+
				" lastprice <= Last_Low) order by volume*todaysopen desc ";
		rs = executeSelectSqlQuery(con, sql);
//		System.out.println(sql);
		while (rs.next()){
			if(notify.equals("")){
				notify = "<font color=red>Williams Bear </font>";
			}
			if(rs.getInt("Crossed")==0)
				notify = notify+rs.getString("s.name")+" ["+rs.getString("approach")+"%] LP["+rs.getString("lastprice")+"] TRIG["+rs.getString("Last_Low")+"], ";
			
			notify =notify+"/";
			
		}
		rs.close();
		
		if(!notify.equals("")){
			notifymessage =notifymessage+ "<tr><td>"+notify+"</td></tr>";
			notify="";table="";
		}
		
		//Williams notify ends
		
		//CCI notify starts
		
		sql = "select s.name, truncate((Last_high-lastprice)*100/lastprice,2) as approach, lastprice,Last_high,  (lastprice >= Last_high) as Crossed from symbols as s "+join+"  "+
				" where CCICrossover='' and (CCI_Reverse='Bull' ) and volume*todaysopen>"+transactionLimit+" and ((Last_high-lastprice)*100/lastprice <= "+percApprLimit+" or "+
				" lastprice >= Last_high) order by volume*todaysopen desc ";
		
		
		rs = executeSelectSqlQuery(con, sql);
		while (rs.next()){
			if(notify.equals("")){
				notify = "<font color=red>CCI Bull </font>";
			}
			if(rs.getInt("Crossed")==0)
				notify = notify+rs.getString("s.name")+" ["+rs.getString("approach")+"%] LP["+rs.getString("lastprice")+"] TRIG["+rs.getString("Last_high")+"], ";
			
			notify =notify+"/";
		}
		rs.close();
		
		if(!notify.equals("")){
			notifymessage =notifymessage+ "<tr><td>"+notify+"</td></tr>";
			notify="";table="";
		}
		sql = "select s.name, truncate((lastprice-Last_Low)*100/lastprice,2) as approach, lastprice,Last_Low,  (lastprice <= Last_Low) as Crossed from symbols as s "+join+"  "+
				" where CCICrossover='' and (CCI_Reverse='Bear' ) and volume*todaysopen>"+transactionLimit+" and ((lastprice-Last_Low)*100/lastprice <= "+percApprLimit+" or "+
				" lastprice <= Last_Low) order by volume*todaysopen desc ";
		rs = executeSelectSqlQuery(con, sql);
//		System.out.println(sql);
		while (rs.next()){
			if(notify.equals("")){
				notify = "<font color=red>CCI Bear </font>";
			}
			if(rs.getInt("Crossed")==0)
				notify = notify+rs.getString("s.name")+" ["+rs.getString("approach")+"%] LP["+rs.getString("lastprice")+"] TRIG["+rs.getString("Last_Low")+"], ";
			
			notify =notify+"/";
			
		}
		rs.close();
		
		if(!notify.equals("")){
			notifymessage =notifymessage+ "<tr><td>"+notify+"</td></tr>";
			notify="";table="";
		}
		//CCI notify ends
		//notify moving average envelope starts
			sql = "select s.name, truncate((Last_high-lastprice)*100/lastprice,2) as approach, lastprice,Last_high,  (lastprice >= Last_high) as Crossed from symbols as s "+join+"  "+
					" where MaEnvelopeCrossover='' and (MA_ENVLP_CROSS='Bull' ) and volume*todaysopen>"+transactionLimit+" and ((Last_high-lastprice)*100/lastprice <= "+percApprLimit+" or "+
					" lastprice >= Last_high) order by volume*todaysopen desc ";
			
//				System.out.println(sql);
			rs = executeSelectSqlQuery(con, sql);
			while (rs.next()){
				if(notify.equals("")){
					notify = "<font color=red>MA ENVLP Bull </font>";
				}
				if(rs.getInt("Crossed")==0)
					notify = notify+rs.getString("s.name")+" ["+rs.getString("approach")+"%] LP["+rs.getString("lastprice")+"] TRIG["+rs.getString("Last_high")+"], ";
				
				notify =notify+"/";
			}
			rs.close();
			
			if(!notify.equals("")){
				notifymessage =notifymessage+ "<tr><td>"+notify+"</td></tr>";
				notify="";table="";
			}
			sql = "select s.name, truncate((lastprice-Last_Low)*100/lastprice,2) as approach, lastprice,Last_Low,  (lastprice <= Last_Low) as Crossed from symbols as s "+join+"  "+
					" where MaEnvelopeCrossover='' and (MA_ENVLP_CROSS='Bear' ) and volume*todaysopen>"+transactionLimit+" and ((lastprice-Last_Low)*100/lastprice <= "+percApprLimit+" or "+
					" lastprice <= Last_Low) order by volume*todaysopen desc ";
			rs = executeSelectSqlQuery(con, sql);
//			System.out.println(sql);
			while (rs.next()){
				if(notify.equals("")){
					notify = "<font color=red>MA ENVLP Bear </font>";
				}
				if(rs.getInt("Crossed")==0)
					notify = notify+rs.getString("s.name")+" ["+rs.getString("approach")+"%] LP["+rs.getString("lastprice")+"] TRIG["+rs.getString("Last_Low")+"], ";
				
				notify =notify+"/";
				
			}
			rs.close();
			if(!notify.equals("")){
				notifymessage =notifymessage+ "<tr><td>"+notify+"</td></tr>";
				notify="";table="";
			}
			//notify moving average envelope ends
				n.alert(notifymessage);
				notify="";table="";
		
	}

}
