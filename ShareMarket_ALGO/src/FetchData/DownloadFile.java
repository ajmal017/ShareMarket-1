package FetchData;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.csvreader.*;

import Indicators.Connection;

public class DownloadFile extends Connection{

	String save;
    public DownloadFile(String j)
    {
        save = j;
    }
    public DownloadFile(){};
 
    public void downloadZipFile() {
        String saveTo = save;
        try {
        	String monthName="", fileName="", dayZeroConcat="", monthZeroConcat="";
    		int day=Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    		int month = Calendar.getInstance().get(Calendar.MONTH)+1;
    		int year = Calendar.getInstance().get(Calendar.YEAR);
    		if(day<10) dayZeroConcat = "0"+day; else dayZeroConcat=Integer.toString(day) ;
    		if(month<10) monthZeroConcat = "0"+month; else monthZeroConcat=Integer.toString(month);
    		monthName = MonthName(month);
    		fileName = "cm"+dayZeroConcat+monthName+year+"bhav.csv";
    		
            URL url = new URL("https://www.nseindia.com/content/historical/EQUITIES/"+year+"/"+monthName+"/"+fileName+".zip");
            System.out.println(url);
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
    
    public void downloadIndexFile(int day, int month, int year) {
        String saveTo = save;
        try {
        	String monthName="", fileName="", dayZeroConcat="", monthZeroConcat="";
    		if(day<10) dayZeroConcat = "0"+day; else dayZeroConcat=Integer.toString(day) ;
    		if(month<10) monthZeroConcat = "0"+month; else monthZeroConcat=Integer.toString(month);
    		monthName = MonthName(month);
    		fileName = "ind_close_all_"+dayZeroConcat+monthZeroConcat+year+".csv";
    		
            URL url = new URL("https://www.nseindia.com/content/indices/"+fileName);
            System.out.println(url);
            URLConnection conn = url.openConnection();
            InputStream in = conn.getInputStream();
            
            FileOutputStream out = new FileOutputStream(saveTo + ""+year+"-"+month+"-"+day+".csv");
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
public void readFile(int year, int month, int day, String path){
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
		String symbol="", open="", high="", low="", close="", volume="", series="", qty="",totalTrades="";
		products.readHeaders();
		
		while (products.readRecord())
		{
			series = products.get("SERIES");
			if(series.equalsIgnoreCase("EQ") || series.equalsIgnoreCase("BE")){

				symbol = products.get("SYMBOL");
//				query = "insert into symbols(name) "
//						+ "values('"+symbol+"')";
//				executeSqlQuery(dbConnection, query);
				
				System.out.println(symbol);
				open = products.get("OPEN");
				high = products.get("HIGH");
				low = products.get("LOW");
				close = products.get("CLOSE"); 
				volume = products.get("TOTTRDVAL");
				qty = products.get("TOTTRDQTY");
				totalTrades = products.get("TOTALTRADES");
				qty = qty.replaceAll(",", "");
				volume = volume.replaceAll(",", "");
				query = "insert into "+symbol+"(tradedate, open, high, low, close, volume, TotalQty, totalTrades) "
						+ "values('"+date+" 00:00:00'"+","+open+","+high+","+low+","+close+", '"+volume+"','"+qty+"', '"+totalTrades+"')";
				executeSqlQuery(dbConnection, query);
				query = "UPDATE SYMBOLS SET todaysopen="+open+", todayslow="+low+", todayshigh="+high+", todaysclose="+close+","
						+ "yestOpen="+open+", yestLow="+low+", yestHigh="+high+", yestClose="+close+", "
						+ "volume=(SELECT AVG(items.volume) as avgVol FROM (SELECT t.volume FROM "+symbol+" t ORDER BY t.tradedate desc LIMIT 5) items), "
								+ " avgQuantity = (SELECT AVG(items.totalQty) as avgQty FROM (SELECT t.totalQty FROM "+symbol+" t ORDER BY t.tradedate desc LIMIT 7) items), "
										+ "totalTrades = (SELECT AVG(items.totalTrades) as avgTrades FROM (SELECT t.totalTrades FROM "+symbol+" t ORDER BY t.tradedate desc LIMIT 50) items), lastprice="+close+" where name='"+symbol+"'";
				executeSqlQuery(dbConnection, query);	
			}	
					
		}

		products.close();
		
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

public void readIndexFile(int day, int month, int year, String path){
	try {
		java.sql.Connection dbConnection=null;
		dbConnection = getDbConnection();
		String monthName="", fileName="", date="", query="";
		
		String dayZeroConcat="", monthZeroConcat="";
		if(day<10) dayZeroConcat = "0"+day; else dayZeroConcat=Integer.toString(day) ;
		if(month<10) monthZeroConcat = "0"+month; else monthZeroConcat=Integer.toString(month);
		
		monthName = MonthName(month);
		date = year+"-"+month+"-"+day; 
		fileName = date+".csv";
		CsvReader products = new CsvReader(""+path+fileName+"");
		String symbol="", open="", high="", low="", close="", volume="", series="", qty="",totalTrades="";
		products.readHeaders();
		
		while (products.readRecord())
		{
			if(products.get("Index Name").toString().equalsIgnoreCase("Nifty 50")){
				open = products.get("Open Index Value").toString();
				high = products.get("High Index Value").toString();
				low = products.get("Low Index Value").toString();
				close = products.get("Closing Index Value").toString();
				
				query = "insert into nifty_50(tradedate, open, high, low, close) "
						+ "values('"+date+" 00:00:00'"+","+open+","+high+","+low+","+close+")";
				executeSqlQuery(dbConnection, query);
			}
		}

		products.close();
		
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
    public static void main(String []a){
    	int day=Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		int month = Calendar.getInstance().get(Calendar.MONTH)+1;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		DownloadFile downloadFile = new DownloadFile();
//		int dayStart=1, dayEnd = 27; month=7;
//		downloadFile.downloadIndexData(dayStart, dayEnd, month, year);
//		day=3;month=8;year=2018;
		downloadFile.downloadIndexData(day, day, month, year);
		downloadFile.downloadEqData(day, month, year);
		
    }
    public void downloadIndexData(int dayStart, int dayEnd, int month, int year){
    	String indexSavePath="C:\\Puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\index\\";
    	for(int i=dayStart; i<= dayEnd; i++){
    		System.out.println(dayStart+":"+dayEnd);
    		DownloadFile indexDownload = new DownloadFile(indexSavePath);
        	indexDownload.downloadIndexFile(i,month,year);
        	indexDownload.readIndexFile(i, month, year, indexSavePath);
    	}
    }
    public void downloadEqData(int day, int month, int year){
    	String path="C:\\Puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\";
    	String unzipPath="C:\\Puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\UNZIP\\";
    	
    	DownloadFile d = new DownloadFile(path);
    	d.downloadZipFile();
//    	day=30;month=5;year=2018;
//    	month++;
    	d.unzipFile(""+path+""+year+"-"+month+"-"+day+".zip", unzipPath);
    	d.readFile(year, month, day, unzipPath);
    }
}
