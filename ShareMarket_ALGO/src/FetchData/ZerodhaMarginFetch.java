package FetchData;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.csvreader.*;

import Indicators.Connection;

public class ZerodhaMarginFetch extends Connection {

	String save;

	public ZerodhaMarginFetch(String j) {
		save = j;
	}

	public ZerodhaMarginFetch() {
	};

	String path="C:\\Puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\margin.json";

	public void getMarginFromZerodha() {
		try {
			URL url = new URL("https://api.kite.trade/margins/equity");
			System.out.println(url);
			URLConnection conn = url.openConnection();
			InputStream in = conn.getInputStream();
			FileOutputStream out = new FileOutputStream(path);
            byte[] b = new byte[1024];
            int count;
            while ((count = in.read(b)) >= 0) {
                out.write(b, 0, count);
            }
            out.flush(); out.close(); in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readMarginFile() throws IOException, ParseException{
		JSONParser parser = new JSONParser();
		String sql="";
		java.sql.Connection dbConnection = null;
		Connection con = new Connection();
		dbConnection = getDbConnection();   
		FileReader fileReader = new FileReader(path); 
		
		org.json.simple.JSONArray json = (org.json.simple.JSONArray) parser.parse(fileReader);
		for (int i=0; i<json.size(); i++){
			org.json.simple.JSONObject obj = (org.json.simple.JSONObject)json.get(i);
			String name = obj.get("tradingsymbol").toString();
			String margin = obj.get("mis_multiplier").toString();
			try {
				con.executeSqlQuery(dbConnection, "Update symbols set margin='"+margin+"' where name='"+name+"'");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] a) throws IOException, ParseException {
		int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		ZerodhaMarginFetch downloadFile = new ZerodhaMarginFetch();
		downloadFile.getMarginFromZerodha();
		downloadFile.readMarginFile();
	}
}
