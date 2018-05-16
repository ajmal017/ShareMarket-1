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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.poi.util.IOUtils;
import org.apache.xmlbeans.impl.common.ReaderInputStream;
import org.json.simple.JSONObject;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class Zerodha extends Connection{
	public List sqlQueries = new ArrayList<String>();
	
	

	public List getSqlQueries() {
		return sqlQueries;
	}
	

	public void setSqlQueries(List sqlQueries) {
		this.sqlQueries = sqlQueries;
	}
	

	public String makeCall( java.sql.Connection dbConnection  ,
			String symbol) throws HttpException, IOException{
		
        String line="";
        String line2="";
        String xmlData="";
        BufferedWriter output = null;
        
        
        try{
        	
             
             
            
        	String request = "http://api.search.yahoo.com/WebSearchService/V1/webSearch?appid=YahooDemo&query=umbrella&results=10";
    		request = "https://kite.zerodha.com/api/orders";

            HttpClient client = new HttpClient();
            GetMethod method = new GetMethod(request);
            
            // Send GET request
            int statusCode = client.executeMethod(method);
            
            if (statusCode != HttpStatus.SC_OK) {
            	System.err.println("Method failed: " + method.getStatusLine());
            }
            InputStream rstream = null;
            
            JSONObject json = new JSONObject();
            json.put("exchange", "NSE");    
            json.put("tradingsymbol", "VEDL");    
            json.put("transaction_type", "SELL");    
            json.put("order_type", "SL");    
            json.put("quantity", "1");    
            json.put("price", "90");   
            json.put("product", "MIS");   
            json.put("validity", "DAY");   
            json.put("trigger_price", "90.1");   
            json.put("variety", "regular");   
            json.put("request_token", "s8rdobx5pulzvywr485rl8mdxv9r4h2n");   

            CloseableHttpClient httpClient = HttpClientBuilder.create().build();

            try {
                HttpPost request2 = new HttpPost("https://kite.zerodha.com/api/orders");
                StringEntity params = new StringEntity(json.toString());
                request2.addHeader("Content-Type", "application/json");
//                request2.addHeader("Accept", "application/json, text/plain, */*");
                request2.setEntity(params);
                httpClient.execute(request2);
            // handle response here...
            } catch (Exception ex) {
                // handle exception here
            } finally {
                httpClient.close();
            }
            
		      
		   
           
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
	
	   public static void main(String[] args) throws HttpException, IOException  {
		      Test t = new Test(); Zerodha g = new Zerodha();
		      CCI cci = new CCI();
		      java.sql.Connection dbConnection = null;
		      g.makeCall(dbConnection, "VEDL");
		      
		      
	   }
}
