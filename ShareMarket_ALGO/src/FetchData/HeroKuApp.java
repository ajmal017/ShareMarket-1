package FetchData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.JSONArray;
import org.json.JSONObject;

import Indicators.Connection;
import Indicators.Test;

public class HeroKuApp implements Runnable {
	public static void main(String a[]){
		Thread  t = new Thread(new HeroKuApp());
		t.start();
	}
Connection con = new Connection();
	public void run(){

		java.sql.Connection dbConnection = null;
		Connection con = new Connection();
		dbConnection = con.getDbConnection();
		int incr = 10;int bulkSendCount=10;
		int totCount=256;//1508
		String updateOnlyMargin="yes";
		try {
			getCommaList(dbConnection, totCount, incr, bulkSendCount, updateOnlyMargin);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void  updateTodayDataForSymbol(java.sql.Connection dbConnection,String symbol, String date, String open,String high,
			String low, String close, String volume, String updateonlySymbols ){
		String query="";
		open = open.replaceAll(",", "");
		high = high.replaceAll(",", "");
		low = low.replaceAll(",", "");
		close = close.replaceAll(",", "");
		close = close.replaceAll("-", "0.0");
		boolean checkForError=false;
		if(!updateonlySymbols.equalsIgnoreCase("updateOnlySymbolsForToday")){
			query = "insert into "+symbol+"(tradedate, open, high, low, close) values('"+date+" 00:00:00'"+","+open+","+high+","+low+","+close+")";
//			System.out.println(query);
			try {
				con.executeSqlQuery(dbConnection, query);
			} catch (SQLException e) {
				checkForError = true;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(checkForError== false && (updateonlySymbols.equalsIgnoreCase("updateOnlySymbolsForToday"))){
			query = "UPDATE SYMBOLS SET LastUpdated = '"+date+"', todaysopen="+open+", todayslow="+low+","
					+ " todayshigh="+high+", todaysclose="+close+", psar=next_day_psar, volume='"+volume+"' where name='"+symbol+"'";
//			System.out.println(query);
			try {
				con.executeSqlQuery(dbConnection, query);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public String getCommaList(java.sql.Connection dbConnection, int totalCount, int incr, int bulkSendCount
			, String updateOnlyMargin) throws SQLException, HttpException, IOException, InterruptedException{
		String sql="", join="", commaList="";
		ResultSet rs=null;
		boolean checkFailure;
        String request = "";
        Thread t;
		
//		System.out.println(request);
        HttpClient client = new HttpClient();
        GetMethod method = null;
//        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
//        		new DefaultHttpMethodRetryHandler(3, false));
        
		for (int i=0; i < totalCount;i=i+incr){
			if(updateOnlyMargin.equalsIgnoreCase("yes")){
				join = "INNER JOIN MARGINTABLES M ON M.NAME=S.NAME ";
			}
			sql = "SELECT S.name FROM symbols S "+join+" where S.name not like '%&%' and S.name not like 'max' limit "+(i)+", "+bulkSendCount;
//			System.out.println(sql);
			
			rs = con.executeSelectSqlQuery(dbConnection, sql);
			while (rs.next()){
				commaList = commaList +rs.getString("S.name")+",";
			}
//			System.out.println(commaList);
			
			request = "http://live-nse.herokuapp.com/?symbol="+commaList;
			method =  new GetMethod(request);
			System.out.println(request);
			checkFailure = makeCall(dbConnection, commaList, client, method);
			if(checkFailure ==false){
				String a[];
				a = commaList.split(",");
				for (int j=0; j< (a.length); j++){
					request = "http://live-nse.herokuapp.com/?symbol="+a[j];
					method =  new GetMethod(request);
					makeCall(dbConnection, commaList, client, method);
					Thread.sleep(3000);
					method.releaseConnection();
				}
			}
			commaList="";
			method.releaseConnection();
			Thread.sleep(3000);
		}
			
		return commaList;
	}
	
	
	
public boolean makeCall(java.sql.Connection dbConnection,String commaList, HttpClient client, GetMethod method) throws HttpException, IOException{
		
        String line="";
        String line2="";
        String xmlData="";
        String open="", high="", low="", close="", volume="",date="", sql="";
        Test t = new Test();
        try{
//        	getCommaList(dbConnection);
        	
    		
    		
            
//            client.wait(1);
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
            org.json.JSONObject jsonObject = new org.json.JSONObject(line2);

            date = jsonObject.getString("tradedDate");
            String day = date.substring(0, 2);
            String month = date.substring(2, 5);
            month = con.TextMonth(month);
            String year = date.substring(5, 9);
            
            date = year+"-"+month+"-"+day;
            
            JSONArray locArr = jsonObject.getJSONArray("data"); 
            JSONObject o = null;
            JSONArray arr = null;
            String symbol="";
//            System.out.println(locArr.toString());
            int count=0;
            for (int i = 0; i < locArr.length(); i++) {
            	
               o =  (JSONObject) locArr.get(i); 
               open =  o.getString("open").replaceAll(",", "");
               high = o.getString("dayHigh").replaceAll(",", "");
               low = o.getString("dayLow").replaceAll(",", "");
               close = o.getString("closePrice").replaceAll(",", "");
               symbol = o.getString("symbol");
               volume = o.getString("totalTradedVolume");
               updateTodayDataForSymbol(dbConnection,symbol, date, open, high, low, close, volume, "updateOnlySymbolsForToday" );
               count++;
               System.out.println(symbol);
            }
//            System.out.println(count);
            
//           t.CreateOrUpdateTimeStamp(dbConnection, commaList, date, open, high, low, close);
            br.close();
            if(rstream!=null)
            rstream.close();
        }
        catch(Exception e){
//        	System.out.println("sssss");
        	e.printStackTrace();
        	return false;
        }
        finally{

            
        }
    	
        return true;
	}
}
