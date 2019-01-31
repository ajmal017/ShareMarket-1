package FetchData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.httpclient.HttpException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Indicators.Connection;

public class Fetch_Old_Historical_Intra extends Connection {

	public static void main(String[] a) throws IOException, SQLException {
		java.sql.Connection dbConnection = null;
		Fetch_Old_Historical_Intra fetch = new Fetch_Old_Historical_Intra();
		Connection con = new Connection();
		dbConnection = con.getDbConnection();
		String count = "", sql = "";
		try {
			String path = "C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\pastintraday\\2012\\BANKNIFTY_F1_2012_2016.txt";
			String table = "BANKNIFTY_50_1";
			fetch.fetchData(dbConnection, path, table);
		} finally {
			if (dbConnection != null)
				dbConnection = null;
		}
	}

	public void fetchData(java.sql.Connection dbConnection, String path, String table)
			throws IOException, SQLException {
		File file = new File(path);

		BufferedReader br = new BufferedReader(new FileReader(file));
		String st, sql, date, dateT, time, open, high, low, close, volume;
		while ((st = br.readLine()) != null) {
			System.out.println(st);
			dateT = st.split(",")[1];
			open = st.split(",")[3];
			high = st.split(",")[4];
			low = st.split(",")[5];
			close = st.split(",")[6];
			volume = st.split(",")[7];
			time = st.split(",")[2] + ":00";
			date = dateT.substring(0, 4) + "-" + dateT.substring(4, 6) + "-" + dateT.substring(6) + " " + time;
			sql = "Insert into " + table + "(tradedate, open,high, low, close,volume) values" + "('" + date + "',"
					+ open + "," + high + "," + low + "," + close + "," + volume + ")";
			executeSqlQuery(dbConnection, sql);
		}
	}
}
