package FetchData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

public class QuandlRest {

	public String makeCall(String symbol) throws HttpException, IOException{
		
    	String startDate="2016-01-01";
        String endDate="2016-02-18";
        String line="";
        String line2="";
        String xmlData="";
        try{
        	String request = "http://api.search.yahoo.com/WebSearchService/V1/webSearch?appid=YahooDemo&query=umbrella&results=10";
    		request = "https://www.quandl.com/api/v3/datasets/NSE/"+symbol+".xml?start_date="+startDate+"&end_date="+endDate+"&api_key=opPi9VLj1tySD2UJ5zZX";
    		
    		
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
            
            // Process the response from Yahoo! Web Services
            BufferedReader br = new BufferedReader(new InputStreamReader(rstream));
            
            while ((line = br.readLine()) != null) {
                
                line2 = line2 + line;
                
            }
            
            br.close();
            if(rstream!=null)
            rstream.close();
        }
        catch(Exception e){
        	System.out.println("sssss");
        	e.printStackTrace();
        }
    	
        return line2;
	}
}
