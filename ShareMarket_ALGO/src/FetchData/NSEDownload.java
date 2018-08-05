package FetchData;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.csvreader.*;
import com.mysql.jdbc.DatabaseMetaData;

import Indicators.Connection;

public class NSEDownload extends Connection{

	String save;
	List notExist =new ArrayList<>();
    public NSEDownload(String j)
    {
        save = j;
    }
 
    public void downloadZipFile(int year, int month, int day) {
        String saveTo = save;
        try {
        	String monthName="", fileName="", dayZeroConcat="", monthZeroConcat="";
//    		int day=Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
//    		int month = Calendar.getInstance().get(Calendar.MONTH)+1;
//    		int year = Calendar.getInstance().get(Calendar.YEAR);
    		if(day<10) dayZeroConcat = "0"+day; else dayZeroConcat=Integer.toString(day) ;
    		if(month<10) monthZeroConcat = "0"+month; else monthZeroConcat=Integer.toString(month);
    		
    		monthName = MonthName(month);
    		fileName = "cm"+dayZeroConcat+monthName+year+"bhav.csv";
    		String path="https://nseindia.com/content/historical/EQUITIES/"+year+"/"+monthName+"/"+fileName+".zip";
            URL url = new URL(path);
            URLConnection conn = url.openConnection();
            InputStream in = conn.getInputStream();
            FileOutputStream out = new FileOutputStream(saveTo + ""+year+"-"+month+"-"+day+".zip");
            byte[] b = new byte[1024];
            int count;
            while ((count = in.read(b)) >= 0) {
                out.write(b, 0, count);
            }
            out.flush(); out.close(); in.close();                   
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void unzipFile(String filePath, String path){
        
        FileInputStream fis = null;
        ZipInputStream zipIs = null;
        ZipEntry zEntry = null;
        try {
            fis = new FileInputStream(filePath);
            zipIs = new ZipInputStream(new BufferedInputStream(fis));
            while((zEntry = zipIs.getNextEntry()) != null){
                try{
                    byte[] tmp = new byte[4*1024];
                    FileOutputStream fos = null;
                    String opFilePath = ""+path+zEntry.getName();
                    fos = new FileOutputStream(opFilePath);
                    int size = 0;
                    while((size = zipIs.read(tmp)) != -1){
                        fos.write(tmp, 0 , size);
                    }
                    fos.flush();
                    fos.close();
                } catch(Exception ex){
                     
                }
            }
            zipIs.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
        }
    }
public void readFile(int year, int month, int day, String path, boolean isUpdate) throws SQLException{
	try {
		java.sql.Connection dbConnection=null;
		dbConnection = getDbConnection();
		String monthName="", fileName="", date="", query="";
		
		String dayZeroConcat="", monthZeroConcat="";
		if(day<10) dayZeroConcat = "0"+day; else dayZeroConcat=Integer.toString(day) ;
		if(month<10) monthZeroConcat = "0"+month; else monthZeroConcat=Integer.toString(month);
		
		monthName = MonthName(month);
		date = year+"-"+month+"-"+day; 
		fileName = "cm"+dayZeroConcat+monthName+year+"bhav.csv";
		CsvReader products = new CsvReader(""+path+fileName+"");
		String symbol="", open="", high="", low="", close="", volume="", qty="",series="", totalTrades="";
		products.readHeaders();
		String tableExist="1";
		
		while (products.readRecord())
		{
			series = products.get("SERIES");
			if(series.equalsIgnoreCase("EQ") || series.equalsIgnoreCase("BE")){
				symbol = products.get("SYMBOL");
//				if(notExist.size() == 0){
////					System.out.println("entered");
//					tableExist = IstableExist(dbConnection, symbol, "sharemarketnew");
//				}else{
////					System.out.println("entered");
//				}
//				 && isDataNotExist(symbol)
				try{
					if(tableExist.equalsIgnoreCase("1")){
						
						open = products.get("OPEN");
						high = products.get("HIGH");
						low = products.get("LOW");
						close = products.get("CLOSE"); 
						volume = products.get("TOTTRDVAL");
						volume = volume.replaceAll(",", "");
						qty = products.get("TOTTRDQTY");
						qty = qty.replaceAll(",", "");
						totalTrades = products.get("TOTALTRADES");
						if(isUpdate==false){
							query = "insert into `"+symbol+"`(tradedate, open, high, low, close, volume,TotalQty,totalTrades) "
									+ "values('"+date+" 00:00:00'"+","+open+","+high+","+low+","+close+", '"+volume+"', '"+qty+"', '"+totalTrades+"')";
							executeSqlQuery(dbConnection, query);
						}else{
							System.out.println(symbol);
							query = "UPDATE SYMBOLS SET todaysopen="+open+", todayslow="+low+", todayshigh="+high+", todaysclose="+close+","
									+ "yestOpen="+open+", yestLow="+low+", yestHigh="+high+", yestClose="+close+", "
									+ "volume=(SELECT AVG(items.volume) as avgVol FROM (SELECT t.volume FROM `"+symbol+"` t ORDER BY t.tradedate desc LIMIT 5) items), "
											+ " avgQuantity = (SELECT AVG(items.totalQty) as avgQty FROM (SELECT t.totalQty FROM `"+symbol+"` t ORDER BY t.tradedate desc LIMIT 5) items), "
													+ "totalTrades = (SELECT AVG(items.totalTrades) as avgTrades FROM (SELECT t.totalTrades FROM `"+symbol+"` t ORDER BY t.tradedate desc LIMIT 50) items), lastprice="+close+" where name='"+symbol+"'";
							executeSqlQuery(dbConnection, query);	
						}
						
//						query = "UPDATE "+symbol+" SET TotalQty='"+qty+"' where tradedate='"+date+" 00:00:00'";
//						executeSqlQuery(dbConnection, query);
//						System.out.println(query);
					}else{
//						notExist.add(symbol);
//						System.out.println(symbol);
					}
				}catch(SQLException e){
					
				}
			}
		}
		products.close();
	} 
	catch (FileNotFoundException e) {
//		e.printStackTrace();
	} catch (IOException e) {
//		e.printStackTrace();
	}
}
	public String IstableExist(java.sql.Connection dbConnection, String name, String schema) throws SQLException{
		String sql="SELECT count(*) "+
            " FROM INFORMATION_SCHEMA.TABLES "+
           " WHERE table_schema = '"+schema+"' "+
            " AND table_name LIKE `"+name+"`";
		return executeCountQuery(dbConnection, sql);
	}
    public static void main(String []a){
    	java.sql.Connection dbConnection=null;
    	Connection con = new Connection();
    	dbConnection = con.getDbConnection();
    	ResultSet rs=null;String date="";
    	int day=0, month=0, year=0;
    	String path="C:\\Puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\";
    	String unzipPath="C:\\Puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\UNZIP\\";
    	String sql="select * from "+
"(select adddate('1970-01-01',t4.i*10000 + t3.i*1000 + t2.i*100 + t1.i*10 + t0.i) selected_date from "+
 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t0,"+
 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t1,"+
 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t2,"+
 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t3,"+
 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t4) v"+
" where selected_date between '2018-08-03' and '2018-08-03'";
    	try {
			rs = con.executeSelectSqlQuery(dbConnection, sql);
			boolean isUpdate=true;
			NSEDownload downl = new NSEDownload(path);
			while(rs.next()){
				date = rs.getString("selected_date");
				String[] parts = date.split("-");
				year = Integer.parseInt(parts[0]);
				month = Integer.parseInt(parts[1]);
				day = Integer.parseInt(parts[2]);
				downl.downloadZipFile(year, month, day);
				downl.unzipFile(""+path+""+year+"-"+month+"-"+day+".zip", unzipPath);
				downl.readFile(year, month, day, unzipPath, isUpdate);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
    }
    
    public boolean isDataNotExist(String namePassed){
    	List<String> list= new ArrayList();
    	list.add("ABCAPITAL");list.add("TIFIN");list.add("STARCEMENT");list.add("SPTL");list.add("SCHAEFFLER");
    	list.add("SIS");list.add("SBILIFE");list.add("RNAVAL");list.add("RHFL");list.add("MCDOWELL-N");list.add("MASFIN");
    	list.add("M&MFIN");list.add("M&M");list.add("L&TFH");list.add("J&KBANK");list.add("IL&FSTRANS");list.add("IEX");
    	list.add("ICICIGI");list.add("HUDCO");list.add("HCL-INSYS");list.add("GODREJAGRO");list.add("GICRE");list.add("GET&D");
    	list.add("ERIS");list.add("DIXON");list.add("DCAL");list.add("COX&KINGS");list.add("COCHINSHIP");list.add("CDSL");
    	list.add("BSE");list.add("BAJAJ-AUTO");list.add("AUBANK");
    	for (String name: list){
    		if(name.equalsIgnoreCase(namePassed)){
    			return true;
    		}
    	}
    	return false;
    }
}
