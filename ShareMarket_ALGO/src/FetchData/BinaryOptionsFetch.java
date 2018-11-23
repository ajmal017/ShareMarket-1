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
import java.util.ArrayList;
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
	
	public static void main(String[] args) {
		Connection con = new Connection();
		java.sql.Connection dbConnection = null;
		dbConnection = con.getDbConnection();
		BinaryOptionsFetch bo = new BinaryOptionsFetch();
		bo.fetchIntraDataZerodha(dbConnection, path);
	}
	public void fetchIntraDataZerodha(java.sql.Connection dbConnection, String path) throws IOException, ParseException, SQLException{
		JSONParser parser = new JSONParser();
		String sql="";
	       
		FileReader fileReader = new FileReader(path); 
		org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(fileReader);
		org.json.simple.JSONObject locObj = (org.json.simple.JSONObject) json.get("data");
		org.json.simple.JSONArray locArr = (org.json.simple.JSONArray) locObj.get("candles");
		org.json.simple.JSONObject o = null;
		String open,high,low,close, arr;
		String a[];
       boolean filter =false;
       
	   Map<String, String> map = new HashMap<>();
	   Map<String, String> mapAll = new LinkedHashMap();
	   float max=0f, min=0f;
       for (int i = 0; i < locArr.size(); i++) {
    	   arr= locArr.get(i).toString().replace("[", "");
    	   arr = arr.replace("]", "");
    	   a = arr.split(",");
    	   String rep = a[0].replace("\"","");
    	   
       }
	   if ( output != null ) output.close();
       fileReader.close();
	}
}
