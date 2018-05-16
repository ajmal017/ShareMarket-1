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
import Indicators.Test;

public class NSEFuturesDownload extends Connection{

	String save;
	List notExist =new ArrayList<>();
    public NSEFuturesDownload(String j)
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
    		fileName = "fo"+dayZeroConcat+monthName+year+"bhav.csv.zip";
    		String path="https://www.nseindia.com/content/historical/DERIVATIVES/"+year+"/"+monthName+"/"+fileName;
            URL url = new URL(path);
            System.out.println(path);
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
                    System.out.println("Extracting file to "+opFilePath);
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
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
public void readFile(int year, int month, int day, String path, boolean isUpdate){
	try {
		java.sql.Connection dbConnection=null;
		dbConnection = getDbConnection();
		String monthName="", fileName="", date="", query="";
		
		String dayZeroConcat="", monthZeroConcat="";
		if(day<10) dayZeroConcat = "0"+day; else dayZeroConcat=Integer.toString(day) ;
		if(month<10) monthZeroConcat = "0"+month; else monthZeroConcat=Integer.toString(month);
		
		monthName = MonthName(month);
		date = year+"-"+month+"-"+day; 
		fileName = "fo"+dayZeroConcat+monthName+year+"bhav.csv";
		CsvReader products = new CsvReader(""+path+fileName+"");
		String symbol="", open="", high="", low="", close="", volume="", qty="",instrument="",
				open_interest ="", contracts="", strikePrice, changeInOI, type;
		products.readHeaders();
		String tableExist="1", expiryDate="";
		Test test = new Test();
		String symbolTemp="";
		while (products.readRecord())
		{
			instrument = products.get("INSTRUMENT");
			if(instrument.equalsIgnoreCase("OPTSTK")){
				open = products.get("OPEN");
				high = products.get("HIGH");
				low = products.get("LOW");
				close = products.get("CLOSE");
				contracts = products.get("CONTRACTS");
				open_interest = products.get("OPEN_INT");
				strikePrice = products.get("STRIKE_PR");
				changeInOI = products.get("CHG_IN_OI");
				type = products.get("OPTION_TYP");
				if(Integer.parseInt(contracts) !=0){
					query = "insert into "+products.get("SYMBOL")+"_OPT"+"(tradedate, open, high, low, close, type,CONTRACTS,OPEN_INTEREST, strike_price, change_IN_OI) "
							+ "values('"+date+" 00:00:00'"+","+open+","+high+","+low+","+close+", '"+type+"',"
									+ "'"+contracts+"', '"+open_interest+"', '"+strikePrice+"', '"+changeInOI+"')";
					executeSqlQuery(dbConnection, query);
				}
//				executeSqlQuery(dbConnection, "delete from "+products.get("SYMBOL")+"_OPT");
//				String sql="ALTER TABLE "+products.get("SYMBOL")+"_OPT ADD CONSTRAINT PK_OPT PRIMARY KEY (tradedate,strike_price)";
//				executeSqlQuery(dbConnection, sql);
//				String sql="ALTER TABLE "+products.get("SYMBOL")+"_OPT ADD column type varchar(20)";
//				executeSqlQuery(dbConnection, sql);
			}
			if(instrument.equalsIgnoreCase("FUTSTK")){
//				expiryDate = products.get("EXPIRY_DT").split("-")[1];
//				if(expiryDate.equalsIgnoreCase(monthName)){
//					continue;
//				}
				symbol = products.get("SYMBOL")+"_FUT";
				symbolTemp = symbol;
				System.out.println(symbol);
//				test.createTable(dbConnection, products.get("SYMBOL")+"_OPT");
//				executeSqlQuery(dbConnection, "delete from "+symbol+"");
//				executeSqlQuery(dbConnection, "insert into FutureStocks values('"+products.get("SYMBOL")+"')");
				if(tableExist.equalsIgnoreCase("1")){
					
					open = products.get("OPEN");
					high = products.get("HIGH");
					low = products.get("LOW");
					close = products.get("CLOSE");
					contracts = products.get("CONTRACTS");
					open_interest = products.get("OPEN_INT");
					strikePrice = products.get("STRIKE_PR");
					changeInOI = products.get("CHG_IN_OI");
					if(isUpdate==false){
						query = "insert into "+symbol+"(tradedate, open, high, low, close, CONTRACTS,OPEN_INTEREST) "
								+ "values('"+date+" 00:00:00'"+","+open+","+high+","+low+","+close+", '"+contracts+"', '"+open_interest+"')";
						executeSqlQuery(dbConnection, query);
						
						query="select lotsize from "+symbol+" where lotsize <>0 order by tradedate desc limit 1";
						String lotsize = executeCountQuery(dbConnection, query);
						
						query = "select avg(contracts) from "+symbol+" where tradedate > Date_ADD(now(),Interval -14 day)";
						String avgVolume = executeCountQuery(dbConnection, query);
						query = "update "+symbol+" set lotsize='"+lotsize+"', avg_volume='"+avgVolume+"' where tradedate='"+date+"'";
						executeSqlQuery(dbConnection, query);
						
						if(Float.parseFloat(open) !=0){
							query = "update margintables set fut_lot_size='"+lotsize+"', fut_avg_volume='"+avgVolume+"',"
									+ "fut_open='"+open+"' where name='"+products.get("SYMBOL")+"'";
							executeSqlQuery(dbConnection, query);
						}
						
					}
					
					
				}	
			}
		}
		products.close();
		
	}
	catch (SQLException e){
		e.printStackTrace();
	}
	catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
}

	public String IstableExist(java.sql.Connection dbConnection, String name, String schema) throws SQLException{
		String sql="SELECT count(*) "+
            " FROM INFORMATION_SCHEMA.TABLES "+
           " WHERE table_schema = '"+schema+"' "+
            " AND table_name LIKE '"+name+"'";
		return executeCountQuery(dbConnection, sql);
	}
    public static void main(String []a){
    	java.sql.Connection dbConnection=null;
    	Connection con = new Connection();
    	dbConnection = con.getDbConnection();
    	ResultSet rs=null;String date="";
    	int day=0, month=0, year=0;
    	String path="C:\\Puneeth\\SHARE_MARKET\\Futures\\";
    	String unzipPath="C:\\Puneeth\\SHARE_MARKET\\Futures\\UNZIP\\";
    	String sql="select * from "+
"(select adddate('1970-01-01',t4.i*10000 + t3.i*1000 + t2.i*100 + t1.i*10 + t0.i) selected_date from "+
 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t0,"+
 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t1,"+
 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t2,"+
 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t3,"+
 "(select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t4) v"+
" where selected_date between '2017-04-21' and '2017-04-26'";
    	try {
//    		System.out.println(sql);
			rs = con.executeSelectSqlQuery(dbConnection, sql);
			boolean isUpdate=false;
			NSEFuturesDownload downl = new NSEFuturesDownload(path);
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
}
