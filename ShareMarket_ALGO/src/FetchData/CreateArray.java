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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.csvreader.*;
import com.mysql.jdbc.BufferRow;
import com.mysql.jdbc.DatabaseMetaData;

import Indicators.Connection;

public class CreateArray extends Connection{

	String save;
	List notExist =new ArrayList<>();
    public CreateArray(String j)
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
		fileName = "MarginList_WithZerodhaId2.txt";
		BufferedReader br =null;
		FileReader fr=null;
		fr=new FileReader(""+path+fileName+"");
		br = new BufferedReader(fr);
		String s, out="";;
		while((s=br.readLine()) !=null){
			out = out + "["+s +"],";
		} 
		System.out.println(out);
		CsvReader products = new CsvReader(""+path+fileName+"");
		String symbol="", open="", high="", low="", close="", volume="", qty="",series="";
		products.readHeaders();
		String tableExist="1";
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
            " AND table_name LIKE '"+name+"'";
		return executeCountQuery(dbConnection, sql);
	}
    public static void main(String []a){
    	java.sql.Connection dbConnection=null;
    	Connection con = new Connection();
    	dbConnection = con.getDbConnection();
    	ResultSet rs=null;String date="";
    	int day=0, month=0, year=0;
    	String path="C:\\Puneeth\\SHARE_MARKET\\Daily\\";
    	String unzipPath="C:\\Puneeth\\SHARE_MARKET\\Zerodha\\";
    		CreateArray c = new CreateArray("a");
			c.readFile(year, month, day, unzipPath, true);
		
    }
}
