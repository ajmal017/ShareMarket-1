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

public class IndexSymbols extends Connection{

	String save;
    public IndexSymbols(String j)
    {
        save = j;
    }
     
public void readFile(String fileNameWithPath, String fileName){
	try {
		java.sql.Connection dbConnection=null;
		dbConnection = getDbConnection();
//		create table nifty500(Name varchar(50) not null, PRIMARY KEY (`Name`));
		CsvReader products = new CsvReader(fileNameWithPath);
		String symbol="", open="", high="", low="", close="", volume="", name="", qty="", query="";
		products.readHeaders();String table="";
		if(fileName.equalsIgnoreCase("ind_niftyautolist"))
			table = "AUTO_INDEX";
		else if(fileName.equalsIgnoreCase("ind_niftybanklist"))
			table = "BANKNIFTY_INDEX";
		else if(fileName.equalsIgnoreCase("ind_niftyfinancelist"))
			table = "FINANCIAL_INDEX";
		else if(fileName.equalsIgnoreCase("ind_niftyfmcglist"))
			table = "FMCG_INDEX";
		else if(fileName.equalsIgnoreCase("ind_niftyitlist"))
			table = "IT_INDEX";
		else if(fileName.equalsIgnoreCase("ind_nifty500list"))
			table = "NIFTY_500";
		while (products.readRecord())
		{
			if(products.get("Series").equalsIgnoreCase("EQ")){
				name = products.get("Symbol");
				query = "insert into "+table+"(name) "
						+ "values('"+name+"'"+")";
//				executeSqlQuery(dbConnection, query);
			}
			query = "Update symbols set nifty500=1 where name='"+name+"'";
			executeSqlQuery(dbConnection, query);
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
    	String path="C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\Hist_Data\\index\\";
    	int day=Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		int month = Calendar.getInstance().get(Calendar.MONTH)+1;
		int year = Calendar.getInstance().get(Calendar.YEAR);
    	IndexSymbols d = new IndexSymbols(path);
    	d.readFile(""+path+"ind_nifty500list.csv", "ind_nifty500list");
    }
}
