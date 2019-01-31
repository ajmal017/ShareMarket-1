package FetchData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mysql.jdbc.DatabaseMetaData;

import Indicators.Connection;
import Indicators.Test;
import Strategies.imp.GapModified;

public class BinaryOptionsFetch extends Connection {
	
	static String path="C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\Intraday\\binary\\";
	static String fileName="AUD_USD";
	static int dur=5;
	public static void main(String[] args) throws IOException, ParseException, SQLException, java.text.ParseException {
		Connection con = new Connection();
		java.sql.Connection dbConnection = null;
		dbConnection = con.getDbConnection();
		BinaryOptionsFetch bo = new BinaryOptionsFetch();
		
		bo.fetchIntraDataZerodha(dbConnection, path, fileName, dur);
	}
	public void fetchIntraDataZerodha(java.sql.Connection dbConnection, 
			String path, String fileName, int dur) throws IOException, ParseException, SQLException, java.text.ParseException{
		JSONParser parser = new JSONParser();
		String sql="";
	       
		FileReader fileReader = new FileReader(path+fileName+"_"+dur+".JSON"); 
		org.json.simple.JSONArray locArr = (org.json.simple.JSONArray) parser.parse(fileReader);
		String open="",high="",low="",close="", date="";
		String a[];
       boolean filter =false;
       
	   Map<String, String> map = new HashMap<>();
	   Map<String, String> mapAll = new LinkedHashMap();
	   float max=0f, min=0f;
       for (int i = 0; i < locArr.size(); i++) {
    	   org.json.simple.JSONObject each = (org.json.simple.JSONObject) locArr.get(i);
//    	   arr= locArr.get(i).toString().replace("[", "");
//    	   arr = arr.replace("]", "");
//    	   a = arr.split(",");
//    	   String rep = a[0].replace("\"","");
    	   if(!each.get("Date").toString().trim().equals("")){
    		   date = each.get("Date")+""+each.get("time");
    		   String finalDate =date.split(" ")[2]+"-"+TextMonth(date.split(" ")[0].toUpperCase())+"-"
    		   +date.split(" ")[1]+" "+date.split(" ")[3]+":00";
    		   sql ="Insert into "+fileName+"_BO_"+dur+"(tradedate, open,high,low,close)"
    		   		+ "values ('"+finalDate+"',"+each.get("open")+", "+each.get("high")+", "
    		   				+ ""+each.get("low")+", "+each.get("close")+")";
        	   executeSqlQuery(dbConnection, sql);
    	   }
       }
       fileReader.close();
	}
}
