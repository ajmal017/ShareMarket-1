package FetchData;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.poi.util.IOUtils;

import Indicators.Connection;

public class NSEDeliveryPosition extends Connection{

	private final String USER_AGENT = "Mozilla/5.0";

	public static void main(String[] args) throws Exception {
		ResultSet rs=null;
		Connection con = new Connection();
		java.sql.Connection dbC=null;
    	dbC = con.getDbConnection();
    	String date="", fileName="",dateToInsert="";
    	int day=0,month=0,year=0;
    	String dayZeroConcat="",monthZeroConcat="",yearZeroConcat="";
		NSEDeliveryPosition http = new NSEDeliveryPosition();
		String sql="select selected_date from "+
				"(select adddate('1970-01-01',t4.i*10000 + t3.i*1000 + t2.i*100 + t1.i*10 + t0.i) selected_date from "+
				 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t0,"+
				 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t1,"+
				 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t2,"+
				 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t3,"+
				 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t4) v"+
				" where selected_date between '2017-03-24' and '2017-04-23'";
		rs = con.executeSelectSqlQuery(dbC, sql);
		while(rs.next()){
			date = rs.getString("selected_date");
			System.out.println(date);
			String[] parts = date.split("-");
			year = Integer.parseInt(parts[0]);
			month = Integer.parseInt(parts[1]);
			day = Integer.parseInt(parts[2]);
			if(day<10) dayZeroConcat = "0"+day; else dayZeroConcat=Integer.toString(day) ;
			if(month<10) monthZeroConcat = "0"+month; else monthZeroConcat=Integer.toString(month);
			fileName = "MTO_"+dayZeroConcat+monthZeroConcat+year+".DAT";
			dateToInsert = year+"-"+month+"-"+day; 
			http.sendGet(dbC, fileName, dateToInsert);
		}
	}

	// HTTP GET request
	private void sendGet(java.sql.Connection dbC, String fileName, String dateToInsert)  {
		try{

			String url = "https://nseindia.com/archives/equities/mto/"+fileName;

			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			int count=0;
			String a[]=null, name="",tradedQuantity="", deliveryQuantity="", perc="",sql="";
			while ((inputLine = in.readLine()) != null) {
				count++;
				a = inputLine.split(",");
				if(count>4){
					if(a[3].equalsIgnoreCase("EQ")){
						 name = a[2];
						 deliveryQuantity=a[5];
						 tradedQuantity = a[4];
						 perc = a[6];
//						 System.out.println(name+","+deliveryQuantity+","+tradedQuantity+","+perc);
						 sql = "insert into "+name+"_DP(tradedate, tradedQuantity, deliveryQuantity, perc) "
									+ "values('"+dateToInsert+" 00:00:00'"+",'"+tradedQuantity+"','"+deliveryQuantity+"','"+perc+"')";
						 executeSqlQuery(dbC, sql);
					}
					 
				}
				response.append(inputLine);
			}
			in.close();

		}
		catch(SQLException e){
			e.printStackTrace();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// HTTP POST request
	private void sendPost() throws Exception {

		String url = "https://selfsolve.apple.com/wcResults.do";
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		System.out.println(response.toString());

	}

}