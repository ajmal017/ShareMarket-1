package Indicators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.poi.util.IOUtils;
import org.apache.xmlbeans.impl.common.ReaderInputStream;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class Strategy1 extends Connection  implements Runnable{
	public List sqlQueries = new ArrayList<String>();
	
	

	public List getSqlQueries() {
		return sqlQueries;
	}
	

	public void setSqlQueries(List sqlQueries) {
		this.sqlQueries = sqlQueries;
	}
	

	public String makeCall( java.sql.Connection dbConnection  ,
			String symbol, String iter,  String range, String path) throws HttpException, IOException{
		
        String line="";
        String line2="";
        String xmlData="";
        BufferedWriter output = null;
        
        File file = new File(""+path+symbol+"_"+iter+".txt");
        output = new BufferedWriter(new FileWriter(file));
        try{
        	
             
             
            
        	String request = "http://api.search.yahoo.com/WebSearchService/V1/webSearch?appid=YahooDemo&query=umbrella&results=10";
    		request = "http://www.google.com/finance/getprices?q="+symbol+"&x=NSE&i="+Integer.parseInt(iter)*60+"&p="+range+"d&f=d,o,h,l,c,v";

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
            BufferedWriter writer = null;
            int count=1;int x=1;
           String open="", high="", low="", close="", volume="", date="", prevDate="", dateTemp="", closeTemp="";;;
           int dateC=0, closeC=0, highC=0, lowC=0, openC=0, volumeC=0; 
           String epochString = "";
		      long epoch;
		      Date expiry =null;
		      String DbDate ="", sql="";
		      int comp=0;
		      if(Integer.parseInt(iter)==5 || Integer.parseInt(iter)==10) comp=100; 
		      if(Integer.parseInt(iter)==60) comp=15; 
		      if(Integer.parseInt(iter)==30) comp=30; 
		    //  if(iter.equals(5)) comp=100; 
            while ((line = br.readLine()) != null) {
            	
            	if(line.contains("COLUMNS=DATE")){
            		for (String retval: line.split(",")){
            			if(retval.equalsIgnoreCase("COLUMNS=DATE")) dateC=1;
            			if(retval.equalsIgnoreCase("CLOSE")) closeC=2;
            			if(retval.equalsIgnoreCase("HIGH")) highC=3;
            			if(retval.equalsIgnoreCase("LOW")) lowC=4;
            			if(retval.equalsIgnoreCase("OPEN")) openC=5;
            			if(retval.equalsIgnoreCase("VOLUME")) volumeC=6;
                        x++;
                    }
            	}
            	if(count>7){
            		x=1;
            		for (String retval: line.split(",")){
            			if(x==dateC) date=retval;
            			if(x==closeC) close=retval;
            			if(x==highC) high=retval;
            			if(x==lowC) low=retval;
            			if(x==openC) open=retval;
            			if(x==volumeC) volume=retval;
                        x++;
                    }
            		x=1;
            		if(date.contains("a")){
            			prevDate = date.replaceAll("a", "");
            			epoch = Long.parseLong( prevDate );
            		}else{
            			if(Long.parseLong(dateTemp) > 100000)
            				dateTemp = "0";
            			if(Integer.parseInt(date)  > (Long.parseLong(dateTemp)+1)  &&
            					!(Integer.parseInt(date)  > (Integer.parseInt(dateTemp)+comp))){
            				for(int y=1; y < (Integer.parseInt(date)-Integer.parseInt(dateTemp)); y++){
            					epoch = Long.parseLong(prevDate)+Long.parseLong(iter)*60*((Long.parseLong(dateTemp)+y));
            					expiry = new Date( epoch * 1000 );
                            	DbDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expiry);
                            	
                            	output.write(DbDate);
                            	output.write(","+closeTemp);
                            	output.write(","+closeTemp);
                            	output.write(","+closeTemp);
                            	output.write(","+closeTemp);
                            	output.write(",0\r\n");
            				}
            			}
            			epoch = Long.parseLong(prevDate)+Long.parseLong(iter)*60*Long.parseLong(date);
//            			System.out.println(epoch);
            		}
            		
                	expiry = new Date( epoch * 1000 );
                	DbDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expiry);
                	
                	output.write(DbDate);
                	output.write(","+open);
                	output.write(","+high);
                	output.write(","+low);
                	output.write(","+close);
                	output.write(","+volume);
                	output.write(","+volume+"\r\n");

            	}
            	if(date.contains("a"))
            		dateTemp = prevDate;
            	else
            		dateTemp = date;
            	closeTemp = close;
                count++;
            }
            
            if ( output != null ) output.close();
            sql = "LOAD DATA LOCAL INFILE '"+path+symbol+"_"+iter+".txt' "+
					 " INTO TABLE "+symbol+"_"+iter+" "+
					 " FIELDS TERMINATED BY ',' "+
					 " LINES TERMINATED BY '\n'" +
					 " "+
					 " (tradedate, open,high, low, close, volume, totalQty) ";
            
           		executeSqlQuery(dbConnection, sql);
        }
        catch(Exception e){
        	System.out.println("exception");
        	e.printStackTrace();
        }
        finally{
        	
        	if ( output != null ) output.close();
        }
    	
        return line2;
	}
	
	   public static void main(String[] args)  {
//		   Strategy1 s = new Strategy1();
//		   s.run();
		   while(1==1){
		  		Thread  t = new Thread(new Strategy1());
		  		t.start();
		  		try {
					t.sleep(60000*1);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		  	}
	   }


	   @Override
	public void run() {
		java.sql.Connection dbConnection = null;
	      int noOfDataRangeToBeInserted = 1 ;
	      String iteration = "", notifyMessage="";
	      List symbolsBull = new ArrayList<String>();List symbolsBear = new ArrayList<String>();Notifier n = new Notifier();
	      for (int count=1;count<=noOfDataRangeToBeInserted; count++){
	    	  try{

	    	  		Strategy1 g = new Strategy1();
		    	  	dbConnection = getDbConnection();
		    	  	ResultSet rs=null;
		    	  	String sql = frameQuery("Bull");
		    	  	rs = executeSelectSqlQuery(dbConnection, sql);
		    	  	String name="";
		    	  	if(count==1) iteration = "1";
		    	  	String range="1";
		    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/"+iteration+"/";
		    	  	while (rs.next()){
		    	  		name= rs.getString("name");
		    	  		
		    	  		symbolsBull.add(name);
		    			g.makeCall(dbConnection, name.toUpperCase(), iteration, range, path);
		    	  	}
		    	  	for(int i=0; i< symbolsBull.size(); i++){ 
		    	  		updateAll(dbConnection, symbolsBull.get(i).toString(), iteration);
		    	  		System.out.println("Bull= "+symbolsBull.get(i).toString());
		    	  	}
		    	  	sql = frameQuery("Bear");
		    	  	rs = executeSelectSqlQuery(dbConnection, sql);
		    	  	while (rs.next()){
		    	  		name= rs.getString("name");
		    	  		symbolsBear.add(name);
		    			g.makeCall(dbConnection, name.toUpperCase(), iteration, range, path);
		    	  	}
		    	  	for(int i=0; i< symbolsBear.size(); i++){ 
		    	  		updateAll(dbConnection, symbolsBull.get(i).toString(), iteration);
		    	  		System.out.println("Bear= "+symbolsBull.get(i).toString());
		    	  	}
		    	  	notifyMessage = "<table><tr>";
		    	  	notifyMessage += "<td>"+notifyBull(dbConnection, symbolsBull, "Bull", iteration)+"</td>";
		    	  	notifyMessage += "<td>"+notifyBear(dbConnection, symbolsBear, "Bear", iteration)+"</td>";
		    	  	notifyMessage += "</tr></table>";
		    	  	n.alert(notifyMessage);
		      }
			  	
		      catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		      catch(Exception e){
					e.printStackTrace();
				}
	      }
		
	}
	   public String updateStoreReversalTable(java.sql.Connection dbConnection, String name, String iter) throws SQLException{
		  String sql="select open, high, low, close,cci,cci_reversal from "+name+"_"+iter+" where date(tradedate)>=date(now()) order by tradedate desc limit 1 ";
		  ResultSet rs = executeSelectSqlQuery(dbConnection, sql);
		   while (rs.next()){
			   sql = "update storereversal set "
			   		+ "cci_1_open = "+rs.getFloat("open")+", cci_1_high="+rs.getFloat("high")+", cci_1_low="+rs.getFloat("low")+", cci_1_close="+rs.getFloat("close")+", "
			   		+ "cci_1="+rs.getFloat("cci")+", cci_1_reversal='"+rs.getString("cci_reversal")+"' where name='"+name+"'";
			   executeSqlQuery(dbConnection, sql);
		   }
		return sql;
	   }
	   public String notifyBull(java.sql.Connection dbConnection, List<String> symbols,String dir, String iter) throws SQLException{
		   String sql="", name="";ResultSet rs=null;String notify=""; String nameConcat="";
		   if(dir.equalsIgnoreCase("Bull")){
			   for (int i=0; i< symbols.size(); i++){
				   name=symbols.get(i).toString();
				   nameConcat+="'"+name+"'";
				   if(!(i==symbols.size()-1)){
					   nameConcat+=",";
				   }
				   updateStoreReversalTable(dbConnection, name, iter);
			   }
			   sql = "select name, high, (high-cci_1_high) as appr_Rup, (high-cci_1_high)*100/high as appr_Perc, cci_1_close, tradedate,cci_1,cci_1_reversal from "
				   		+ " storereversal where name in ("+nameConcat+") and (high-cci_1_high)*100/high > 0 order by (high-cci_1_high)*100/high ";
				   rs = executeSelectSqlQuery(dbConnection, sql);
				   notify+="<table border=1><tr><td><font color=red><b><u>Bull Results</u></b></font></td></tr>";
				   while (rs.next()){
					   notify+="<tr><td>";
					   notify+= ""+rs.getString("name")+" ["+rs.getString("appr_Perc")+"%] TRIG["+rs.getString("high")+"] LP["+rs.getString("cci_1_close")+"] CCI["+rs.getFloat("cci_1")+"]";
					   if(rs.getString("cci_1_reversal").equalsIgnoreCase("Bull")){
						   notify+= "<font color=red>CCIBull</font>";
					   }
					   notify+="</td></tr>";
				   }
				   notify+="</table>";
				   if(rs!=null)
				   rs.close();
				   nameConcat="";   
		   }
		   return notify;
	   }
	   
	   public String notifyBear(java.sql.Connection dbConnection, List<String> symbols,String dir, String iter) throws SQLException{
		   String sql="",name=""; ResultSet rs=null;  String notify=""; String nameConcat="";
		   if(dir.equalsIgnoreCase("Bear")){
			   for (int i=0; i< symbols.size(); i++){
				   name=symbols.get(i).toString();
				   nameConcat+="'"+name+"'";
				   if(!(i==symbols.size()-1)){
					   nameConcat+=",";
				   }
				   updateStoreReversalTable(dbConnection, name, iter);
			   }
			   sql = "select name, low, (cci_1_low-low) as appr_Rup, (cci_1_low-low)*100/cci_1_low as appr_Perc, cci_1_close, tradedate,cci_1,cci_1_reversal from "
				   		+ " storereversal where name in ("+nameConcat+") and (cci_1_low-low)*100/cci_1_low > 0 order by (cci_1_low-low)*100/cci_1_low ";
				   rs = executeSelectSqlQuery(dbConnection, sql);
				   notify+="<table border=1><tr><td><font color=red><b><u>Bear Results</u></b></font></td></tr>";
				   while (rs.next()){
					   notify+="<tr><td>";
					   notify+= ""+rs.getString("name")+" ["+rs.getString("appr_Perc")+"%] TRIG["+rs.getString("low")+"] LP["+rs.getString("cci_1_close")+"] CCI["+rs.getFloat("cci_1")+"]";
					   if(rs.getString("cci_1_reversal").equalsIgnoreCase("Bear")){
						   notify+= "<font color=red>CCIBear</font>";
					   }
					   notify+="</td></tr>";
				   }
				   notify+="</table>";
				   if(rs!=null)
				   rs.close();
				   nameConcat="";
		   }
		   return notify;
	   }
	   public String frameQuery(String dir){
		   String sql="";String filter="";
		   if(dir.equalsIgnoreCase("Bull")){
			   filter = "'Bull', 'UP', 'BullToday', 'Bullish Cross' ";
		   }else if(dir.equalsIgnoreCase("Bear")){
			   filter = "'Bear', 'DOWN', 'BearToday', 'Bearish Cross' ";
		   }
		   sql = "(select tradedate,name,high from storereversal where cci_reversal in ("+filter+")) union "
		   		+ " (select tradedate,name,high from storereversal where psar_reversal in ("+filter+")) union "
				+"(select tradedate,name,high from storereversal where macd_change_dir in ("+filter+")) union "
				+ " (select tradedate,name,high from storereversal where hist_zero_cross in ("+filter+")) union "
				+"(select tradedate,name,high from storereversal where adx_dm_crossover in ("+filter+")) union "
				+ "(select tradedate,name,high from storereversal where williams_reversal in ("+filter+")) union "
				+"(select tradedate,name,high from storereversal where MAenvelope_reversal in ("+filter+"))";
		   
		return sql;
		   
	   }
	
	public void updateAll(java.sql.Connection dbConnection, String name, String iteration) throws SQLException{
		PSAR psar = new PSAR();ADX adx = new ADX(); MACD macd = new MACD();WILLIAMS williams = new WILLIAMS();
	    CCI cci = new CCI();MovingAvgEnvelope ma = new MovingAvgEnvelope();SuperTrend superTrend= new SuperTrend();
	    RangeCompare range = new RangeCompare();PriceRateOfChange roc = new PriceRateOfChange();OnBalanceVolume obv = new OnBalanceVolume();
	    
	    try{
	    	  
	    	  	ResultSet rs=null;
	    	  	boolean updateForTodayAndNextDay=true;boolean updateForallDays=false;
	    	  	boolean updateSymbolsTableData=false; boolean updateAllData=true;
	    	  	boolean updateResultTable=false;boolean isIntraDayData=true;boolean insertAllDataToResult = false;
	    	  	Float diffPerc=0.00f;String iter=iteration;boolean fastProcess=true;
	    	  	if(!iter.equals("1d"))
    	  			name =name+"_"+iter+"";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";

    	  		

    	  		cci.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData,path+"/cci/"+iter+"/");
    	  		cci.UpdateCCIResults(dbConnection, name, updateSymbolsTableData, updateAllData, 
    	  				updateResultTable, diffPerc, isIntraDayData, insertAllDataToResult, fastProcess);
    	  		
//    	  		obv.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/obv/"+iter+"/");
//    	  		//roc
//    	  		roc.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/obv/"+iter+"/");


	      }
		  	catch(Exception e){
				e.printStackTrace();
			}
	    
	}
}
