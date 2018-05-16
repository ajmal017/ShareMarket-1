package FetchData;

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

import Indicators.CCI;
import Indicators.Connection;
import Indicators.Test;

public class GoogleIntraday extends Connection{
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
//            		System.out.println(line);
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
//            		System.out.println(line);
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
//            			System.out.println(epoch+" a value");
            		}else{
//            			System.out.println(Long.parseLong(dateTemp));
            			if(Long.parseLong(dateTemp) > 100000)
            				dateTemp = "0";
            			if(Integer.parseInt(date)  > (Long.parseLong(dateTemp)+1)  &&
            					!(Integer.parseInt(date)  > (Integer.parseInt(dateTemp)+comp))){
            				for(int y=1; y < (Integer.parseInt(date)-Integer.parseInt(dateTemp)); y++){
            					epoch = Long.parseLong(prevDate)+Long.parseLong(iter)*60*((Long.parseLong(dateTemp)+y));
//            					System.out.println(epoch+" Loop, close="+closeTemp);
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
//           		System.out.println(sql);
           		sql = "select max(high) from "+symbol+"_"+iter+" where date(tradedate) = "
           				+ "(select date(tradedate) from "+symbol+"_"+iter+" order by tradedate desc limit 1) ";
           		executeSqlQuery(dbConnection, "UPDATE storereversal set max_high = ("+sql+") where name='"+symbol+"'");
           		sql = "select min(low) from "+symbol+"_"+iter+" where date(tradedate) = "
           				+ "(select date(tradedate) from "+symbol+"_"+iter+" order by tradedate desc limit 1) ";
           		executeSqlQuery(dbConnection, "UPDATE storereversal set min_low = ("+sql+") where name='"+symbol+"'");
           		
//           		System.out.println(sql);
//           		boolean success = (new File
//           	         (""+path+symbol+"_"+iter+".txt")).delete();
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
		      Test t = new Test();
		      CCI cci = new CCI();
		      java.sql.Connection dbConnection = null;
		      String epochString = "1459146275";
		      long epoch = Long.parseLong( epochString );
		      Date expiry = new Date( epoch * 1000 );
		      int noOfDataRangeToBeInserted = 4 ;
		      String iteration = "";
		      for (int count=4;count<=noOfDataRangeToBeInserted; count++){
		    	  try{

		    	  		GoogleIntraday g = new GoogleIntraday();
			    	  Connection con = new Connection();
			    	  	dbConnection = con.getDbConnection();
			    	  	ResultSet rs=null;
			    	  	String sql="SELECT s.name FROM symbols s where volume*lastprice > 1000000000 "
		    	  			+ " and s.name not like '%-%' and s.name not like '%&%'  order by id";
//			    	  	sql="select * from symbols s, margintables m where m.name=s.name and s.name not like '%-%' and s.name not like '%&%'";
			    	  	rs = con.executeSelectSqlQuery(dbConnection, sql);
			    	  	String name="";
			    	  	if(count==1) iteration = "5";
			    	  	else if(count==2) iteration = "10";
			    	  	else if(count==3) iteration = "15";
			    	  	else if(count==4) iteration = "30";
			    	  	else if(count==5) iteration = "60";
			    	  	else if(count==6) iteration = "3";
			    	  	else if(count==7) iteration = "1";
			    	  	String range="100"; //days
			    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/"+iteration+"/";
			    	  	while (rs.next()){
			    	  		name= rs.getString("s.name");
			    			g.makeCall(dbConnection, name.toUpperCase(), iteration, range, path);
			    	  		System.out.println(name+"_"+iteration);
			    	  		Thread.sleep(300);
			    	  		//g.getSqlQueries().clear();
//			    	  		g.readFile(path + "\\"+name+".csv");
			    	  	}
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
}
