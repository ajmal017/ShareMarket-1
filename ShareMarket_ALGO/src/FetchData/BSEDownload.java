package FetchData;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.csvreader.*;
import com.mysql.jdbc.DatabaseMetaData;

import Indicators.Connection;

public class BSEDownload extends Connection {

	String save;
	List notExist = new ArrayList<>();

	public BSEDownload(String j) {
		save = j;
	}

	public void downloadZipFile(int year, int month, int day) {
		String saveTo = save;
		try {
			String monthName = "", fileName = "", dayZeroConcat = "", monthZeroConcat = "";
			year = 18;
			if (day < 10)
				dayZeroConcat = "0" + day;
			else
				dayZeroConcat = Integer.toString(day);
			if (month < 10)
				monthZeroConcat = "0" + month;
			else
				monthZeroConcat = Integer.toString(month);

			fileName = "EQ" + dayZeroConcat + monthZeroConcat + year + "_CSV";
			String path = "http://www.bseindia.com/download/BhavCopy/Equity/" + fileName + ".zip";
			URL url = new URL(path);
			System.out.println(path);
			URLConnection conn = url.openConnection();
			InputStream in = conn.getInputStream();
			year = 2017;
			FileOutputStream out = new FileOutputStream(saveTo + "" + year + "-" + month + "-" + day + ".zip");
			byte[] b = new byte[1024];
			int count;
			
//			URL website = new URL(path);
//			Path path2 = new Path(saveTo);
//			try (InputStream in = website.openStream()) {
//			    Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
//			}
			
			while ((count = in.read(b)) >= 0) {
				out.write(b, 0, count);
			}
			out.flush();
			out.close();
			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void unzipFile(String filePath, String path) {

		FileInputStream fis = null;
		ZipInputStream zipIs = null;
		ZipEntry zEntry = null;
		try {
			fis = new FileInputStream(filePath);
			zipIs = new ZipInputStream(new BufferedInputStream(fis));
			while ((zEntry = zipIs.getNextEntry()) != null) {
				try {
					byte[] tmp = new byte[4 * 1024];
					FileOutputStream fos = null;
					String opFilePath = "" + path + zEntry.getName();
					System.out.println("Extracting file to " + opFilePath);
					fos = new FileOutputStream(opFilePath);
					int size = 0;
					while ((size = zipIs.read(tmp)) != -1) {
						fos.write(tmp, 0, size);
					}
					fos.flush();
					fos.close();
				} catch (Exception ex) {

				}
			}
			zipIs.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Map getSymbolsNameFromZerodhaInstrument() {

		Map map = new HashMap<>();
		try (BufferedReader br = new BufferedReader(
				new FileReader("C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\instruments"))) {
			String line, out = "";
			List<String> tradableSymbols = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				if (line.contains("EQ") && line.contains("BSE")) {
					String arr[] = line.split(",");
					out = arr[2] + "_" + arr[0] + "_" + arr[8];
					map.put(arr[1], arr[2]);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return map;
	}

	public void createtables(String name, java.sql.Connection con) {
		String sql = "CREATE TABLE  `sharemarketnew`.`" + name + "` ( " + "`TradeDate` datetime NOT NULL, "
				+ "`Open` varchar(10) NOT NULL, " + "`High` varchar(10) NOT NULL, " + "`Low` varchar(10) NOT NULL, "
				+ "`Close` varchar(10) NOT NULL, " + "`psar` varchar(45) DEFAULT '', " + "`dir` varchar(5) DEFAULT '', "
				+ "`reversal` varchar(45) DEFAULT '', " + "`ProfitonNextDaySell` float DEFAULT '0', "
				+ "`volume` bigint(20) DEFAULT NULL, " + "`EMA1` float DEFAULT '0', " + "`EMA2` float DEFAULT '0', "
				+ "`MACD` float DEFAULT '0', " + "`SIG` float DEFAULT '0', " + "`HISTOGRAM` float DEFAULT '0', "
				+ "`MACD_CHANGE_DIR` varchar(45) DEFAULT '', " + "`Hist_Zero_Cross` varchar(45) DEFAULT '', "
				+ "`TR` float DEFAULT '0', " + "`DM_1_PLUS` float DEFAULT '0', " + "`DM_1_Minus` float DEFAULT '0', "
				+ "`TR_AVARAGE` float DEFAULT '0', " + "`DM_PLUS_AVERAGE` float DEFAULT '0', "
				+ "`DM_MINUS_AVERAGE` float DEFAULT '0', " + "`DI_PLUS_AVERAGE` float DEFAULT '0', "
				+ "`DI_MINUS_AVERAGE` float DEFAULT '0', " + "`DI_DIFF` float DEFAULT '0', "
				+ "`DI_SUM` float DEFAULT '0', " + "`DX` float DEFAULT '0', " + "`ADX` float DEFAULT '0', "
				+ "`ADX_DM_Crossover` varchar(40) DEFAULT '', " + "`ADX_Bull_PROFIT_ON_DM_CROSS` float DEFAULT '0', "
				+ "`ADX_Bear_PROFIT_ON_DM_CROSS` float DEFAULT '0', " + "`ADX_Bull_PROFIT_RUPEES` float DEFAULT '0', "
				+ "`ADX_Bear_PROFIT_RUPEES` float DEFAULT '0', " + "`ADX_Bullish_Enter` varchar(50) DEFAULT '', "
				+ "`ADX_Bearish_Enter` varchar(50) DEFAULT '', " + "`ADX_Bull_Entered_profit` float DEFAULT '0', "
				+ "`ADX_Bear_Entered_profit` float DEFAULT '0', " + "`ADX_BULL_ENTERED_PROFIT_Rs` float DEFAULT '0', "
				+ "`ADX_BEAR_ENTERED_PROFIT_Rs` float DEFAULT '0', " + "`WILL_HIGH` float DEFAULT '0', "
				+ "`WILL_LOW` float DEFAULT '0', " + "`WILL_R` float DEFAULT '0', "
				+ "`WILL_BULL_PROFIT` float DEFAULT '0', " + "`WILL_BEAR_PROFIT` float DEFAULT '0', "
				+ "`WILL_BULL_PROFIT_Rs` float DEFAULT '0', " + "`WILL_Bear_PROFIT_Rs` float DEFAULT '0', "
				+ "`WILL_Reversal` varchar(50) DEFAULT '', " + "`MACD_BULL_PROFIT` float DEFAULT '0', "
				+ "`MACD_BEAR_PROFIT` float DEFAULT '0', " + "`MACD_BULL_PROFIT_Rs` float DEFAULT '0', "
				+ "`MACD_Bear_PROFIT_Rs` float DEFAULT '0', " + "`Typical_Price` float DEFAULT '0', "
				+ "`Typical_Price_Mean` float DEFAULT '0', " + "`Mean_Deviation` float DEFAULT '0', "
				+ "`CCI` float DEFAULT '0', " + "`CCI_Reversal` varchar(50) DEFAULT '', "
				+ "`CCI_Bull_profit` float DEFAULT '0', " + "`CCI_Bull_profit_Rs` float DEFAULT '0', "
				+ "`CCI_Bear_profit` float DEFAULT '0', " + "`CCI_Bear_profit_Rs` float DEFAULT '0', "
				+ "`SMA_ENVELP` float DEFAULT '0', " + "`UPPER_ENVELP` float DEFAULT '0', "
				+ "`LOWER_ENVELP` float DEFAULT '0', " + "`ENVLP_CROSS` varchar(50) DEFAULT '', "
				+ "`MAEnvelope_BULL_PROFIT` float DEFAULT '0', " + "`MAEnvelope_BULL_PROFIT_Rs` float DEFAULT '0', "
				+ "`MAEnvelope_Bear_PROFIT` float DEFAULT '0', " + "`MAEnvelope_Bear_PROFIT_Rs` float DEFAULT '0', "
				+ "`PMO_ROC` float DEFAULT '0', " + "`PMO_CUSTOM_ROC_EMA` float DEFAULT '0', "
				+ "`PMO_CUSTOM_EMA` float DEFAULT '0', " + "`PMO_SIGNAL` float DEFAULT '0', "
				+ "`PMO_BULL_PROFIT` float DEFAULT '0', " + "`PMO_BEAR_PROFIT` float DEFAULT '0', "
				+ "`PMO_REVERSAL` varchar(50) DEFAULT '', " + "`ATR` float DEFAULT '0', "
				+ "`SuperTrend_UP` float DEFAULT '0', " + "`SuperTrend_Down` float DEFAULT '0', "
				+ "`SuperTrend_Reversal` varchar(10) DEFAULT '', " + "`Will_R_5` float DEFAULT '0', "
				+ "`SuperTrend_UP_Band` float DEFAULT '0', " + "`SuperTrend_Down_Band` float DEFAULT '0', "
				+ "`SuperTrend` float DEFAULT '0', " + "`OBV` varchar(100) DEFAULT '', "
				+ "`TotalQty` varchar(100) DEFAULT '', " + "`roc` varchar(100) DEFAULT '', "
				+ "`breakout1` varchar(50) DEFAULT '', " + "`breakout2` varchar(50) DEFAULT '', "
				+ "`hist_dir` varchar(50) DEFAULT '', " + "`pivot` varchar(40) DEFAULT NULL, "
				+ "`R1` varchar(40) DEFAULT NULL, " + "`R2` varchar(40) DEFAULT NULL, "
				+ "`R3` varchar(40) DEFAULT NULL, " + "`S1` varchar(40) DEFAULT NULL, "
				+ "`S2` varchar(40) DEFAULT NULL, " + "`S3` varchar(40) DEFAULT NULL, "
				+ "`avg_Volume` varchar(50) DEFAULT NULL, " + "`max_past_high` varchar(50) DEFAULT NULL, "
				+ "`min_past_low` varchar(50) DEFAULT NULL, " + "`SMA` varchar(50) DEFAULT NULL, "
				+ "`BB_std_dev_20` varchar(50) DEFAULT NULL, " + "`BB_middleBand_20` varchar(50) DEFAULT NULL, "
				+ "`BB_lowerBand_20` varchar(50) DEFAULT NULL, " + "`BB_upperBand_20` varchar(50) DEFAULT NULL, "
				+ "`HA_OPEN` varchar(50) DEFAULT NULL, " + "`HA_HIGH` varchar(50) DEFAULT NULL, "
				+ "`HA_LOW` varchar(50) DEFAULT NULL, " + "`HA_CLOSE` varchar(50) DEFAULT NULL, "
				+ "`BollingerBW` varchar(50) DEFAULT NULL, " + "PRIMARY KEY (`TradeDate`) "
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		try {
			System.out.println(sql);
			executeSqlQuery(con, sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readFile(int year, int yearForDb, int month, int day, String path, boolean isUpdate, Map<String, String> map) {
		try {
			java.sql.Connection dbConnection = null;
			dbConnection = getDbConnection();
			String monthName = "", fileName = "", date = "", query = "";

			String dayZeroConcat = "", monthZeroConcat = "";
			if (day < 10)
				dayZeroConcat = "0" + day;
			else
				dayZeroConcat = Integer.toString(day);
			if (month < 10)
				monthZeroConcat = "0" + month;
			else
				monthZeroConcat = Integer.toString(month);
			date = year + "-" + month + "-" + day;
			fileName = "EQ" + dayZeroConcat + monthZeroConcat + year + ".csv";
			CsvReader products = new CsvReader("" + path + fileName + "");
			String symbol = "", type, code, open = "", high = "", low = "", close = "", volume = "", qty = "",
					series = "", symName = "";
			products.readHeaders();
			while (products.readRecord()) {
				{
					symbol = products.get("SC_GROUP");
					type = products.get("SC_TYPE");
					code = products.get("SC_CODE");
					if (!type.trim().equalsIgnoreCase("Z")) {
						for (Map.Entry entry : map.entrySet()) {
							if (entry.getKey().equals(code)) {
								symName = (String) entry.getValue();
								
								break;

							}
						}
						
//						createtables(symName, dbConnection);
						open = products.get("OPEN");
						high = products.get("HIGH");
						low = products.get("LOW");
						close = products.get("CLOSE");
						volume = products.get("NET_TURNOV");
						volume = volume.replaceAll(",", "");
						qty = products.get("NO_OF_SHRS");
						qty = qty.replaceAll(",", "");
//						String sql = "insert into symbols(name) values" + "('" + symName + "')";
//						String max = executeCountQuery(dbConnection, "select max(id) from symbols");
//						sql = "update symbols set id=" + max + "+1 where name='" + symName + "' and id=0";
//						try {
//							System.out.println(sql);
//							executeSqlQuery(dbConnection, sql);
//						} catch (SQLException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
						 if(isUpdate==false){
							 date = yearForDb + "-" + month + "-" + day;
							 query = "insert into "+symName+"(tradedate, open, high, low, close, volume,TotalQty) "
							 + "values('"+date+" 00:00:00'"+","+open+","+high+","+low+","+close+",'"+volume+"', '"+qty+"')";
							 try {
								 System.out.println(query);
								 executeSqlQuery(dbConnection, query);
							 } catch (SQLException e) {
								 // TODO Auto-generated catch block
								 e.printStackTrace();
							 }
						 }else{
							 try {
								 query = "select close from "+symName+" order by tradedate desc limit 1";
								 String lastPrice = executeCountQuery(dbConnection,query);
								 query="Update symbols set lastPrice="+lastPrice+" where name='"+symName+"'";
								 executeSqlQuery(dbConnection, query);
								 query = "update "+symName+" set TotalQty="+qty+" where date(tradedate)=date('"+date+"')";
								 executeSqlQuery(dbConnection, query);
							 } catch (SQLException e) {
								 // TODO Auto-generated catch block
								 e.printStackTrace();
							 }
						 }
					}
				}
			}

			products.close();

		}

		catch (FileNotFoundException e) {
			 e.printStackTrace();
		} catch (IOException e) {
			 e.printStackTrace();
		}
	}

	public static void main(String[] a) {
		java.sql.Connection dbConnection = null;
		Connection con = new Connection();
		dbConnection = con.getDbConnection();
		ResultSet rs = null;
		String date = "";
		int day = 0, month = 0, year = 18, yearForDb = 2018;
		String path = "C:\\Puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\BSE\\";
		String unzipPath = "C:\\Puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\BSE\\UNZIP\\";
		String sql = "select * from "
				+ "(select adddate('1970-01-01',t4.i*10000 + t3.i*1000 + t2.i*100 + t1.i*10 + t0.i) selected_date from "
				+ "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t0,"
				+ "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t1,"
				+ "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t2,"
				+ "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t3,"
				+ "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t4) v"
				+ " where selected_date between '"+yearForDb+"-03-27' and '"+yearForDb+"-03-27'";
		try {
			// System.out.println(sql);
			rs = con.executeSelectSqlQuery(dbConnection, sql);
			boolean isUpdate = false;
			BSEDownload downl = new BSEDownload(path);
			Map<String, String> map = downl.getSymbolsNameFromZerodhaInstrument();
			while (rs.next()) {
				date = rs.getString("selected_date");
				String[] parts = date.split("-");
				year = Integer.parseInt(parts[0]);
				month = Integer.parseInt(parts[1]);
				day = Integer.parseInt(parts[2]);
//				downl.downloadZipFile(year, month, day);
//				downl.unzipFile("" + path + "" + year + "-" + month + "-" + day + ".zip", unzipPath);
				year=18;
				downl.readFile(year, yearForDb, month, day, unzipPath, isUpdate, map);

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}
}
