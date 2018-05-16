package Indicators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.poi.util.IOUtils;
import org.apache.xmlbeans.impl.common.ReaderInputStream;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import FetchData.GoogleIntraday;

public class Strategy_OpenAndChangesAreSame extends Connection  implements Runnable{
	public List sqlQueries = new ArrayList<String>();
	
	

	public List getSqlQueries() {
		return sqlQueries;
	}
	

	public void setSqlQueries(List sqlQueries) {
		this.sqlQueries = sqlQueries;
	}
	
	   public static void main(String[] args)  {
//		   Strategy1 s = new Strategy1();
//		   s.run();
		   while(1==1){
		  		Thread  t = new Thread(new Strategy_OpenAndChangesAreSame());
		  		t.start();
		  		try {
					t.sleep(60000*15);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		  	}
	   }


	   @Override
	public void run() {
		java.sql.Connection dbConnection = null;
	      int noOfDataRangeToBeInserted = 1 ;int percChange=3;
	      String iterMain = "", iterSub="5", notifyMessage="";
	      List symbols = new ArrayList<String>(); List changedSymbols = new ArrayList<String>();
	      List changedDate = new ArrayList<>();List dir = new ArrayList<String>();Notifier n = new Notifier();
	      for (int count=1;count<=noOfDataRangeToBeInserted; count++){
	    	  try{

	    	  		GoogleIntraday g = new GoogleIntraday();
		    	  	dbConnection = getDbConnection();
		    	  	ResultSet rs=null;
		    	  	String sql = "select s.name from symbols s , margintables m where m.name=s.name and volume > 100000000 order by volume desc";
		    	  	rs = executeSelectSqlQuery(dbConnection, sql);
		    	  	String name="";
		    	  	if(count==1) iterMain = "5";
		    	  	String range="1";
		    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/"+iterMain+"/";
		    	  	while (rs.next()){
		    	  		name= rs.getString("s.name");
		    	  		if(!name.contains("&"))
		    	  			symbols.add(name);
		    	  		System.out.println(name);
		    	  		
//		    			g.makeCall(dbConnection, name.toUpperCase(), iterMain, range, path);
//		    			g.makeCall(dbConnection, name.toUpperCase(), iterSub, range, path);
		    	  	}
		    	  	String open = "", daysClose,daysHigh, daysLow, direction="", notify;
		    	  	notify="<table border=1><tr><td><font color=red><b><u>Results</u></b></font></td></tr>";
		    	  	for(int i=0; i< symbols.size(); i++){
	    	  			name = symbols.get(i).toString();
	    	  			sql="select open from "+name+"_"+iterMain+" where date(tradedate)=date(now()) order by tradedate limit 1 ";
	    	  			open = executeCountQuery(dbConnection, sql);
						if(open.equals("")) continue;
						
	    	  			sql = "select coalesce (case when min(low)="+open+" then 'GoForBull' when max(high)="+open+" then 'GoForBear' end,'')"
	    	  					+ " as result from "+name+"_"+iterMain+" where date(tradedate)=date(now()) and tradedate < concat(Date(date(now())),' 09:40:00')";
	    	  			direction = executeCountQuery(dbConnection, sql);
	    	  			if(!direction.equals("")){
	    	  				notify+="<tr><td> "+name+": ";
							 notify+= " <font color=red>"+direction+"</font>";
							 notify+="</td></tr>";
	    	  			}
		    	  	}
		    	  	notify+="</table>";
		    	  	
		    	  	notifyMessage = "<table><tr>";
		    	  	notifyMessage += "<td>"+notify+"</td>";
		    	  	notifyMessage += "</tr></table>";
		    	  	n.alert(notifyMessage);
		      }
			  	
		      catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		      catch(Exception e){
					e.printStackTrace();
				}
	      }
		
	}
	   
	 
	 public String notifyCross(java.sql.Connection dbConnection, List symbols, String iteration, List changedDate){
		 String name="", notify=""; ResultSet rs=null;
		 notify="<table border=1><tr><td><font color=red><b><u>Results</u></b></font></td></tr>";
		 try {
			 for(int i=0; i< symbols.size(); i++){
				 name = symbols.get(i).toString();
					 String sql="select tradedate,(case when macd > sig then 'GoForBull' "+
							 " when macd < sig then 'GoForBear' end) TradeDir  from "+name+"_"+iteration+" where date(tradedate)=date(now()) and tradedate>'"+changedDate.get(i)+"'";
					 
						rs = executeSelectSqlQuery(dbConnection, sql);
					
					 
					 while(rs.next()){
						 if(rs.getString("TradeDir") != null){
							 notify+="<tr><td> "+name+":"+rs.getString("tradedate");
							 notify+= " <font color=red>"+rs.getString("TradeDir")+"</font>";
							 notify+="</td></tr>";
							 break;
						 }
					 }
			 }
		 }
		 catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		 
		 notify+="</table>";
		 
		return notify;
		 
	 }
	   
	
	public void updateAll(java.sql.Connection dbConnection, String name, String iteration) throws SQLException{
		PSAR psar = new PSAR();ADX adx = new ADX(); MACD macd = new MACD();WILLIAMS williams = new WILLIAMS();
	    CCI cci = new CCI();MovingAvgEnvelope ma = new MovingAvgEnvelope();SuperTrend superTrend= new SuperTrend();
	    RangeCompare range = new RangeCompare();PriceRateOfChange roc = new PriceRateOfChange();OnBalanceVolume obv = new OnBalanceVolume();
	    
	    try{
	    	  
	    	  	ResultSet rs=null;
	    	  	boolean updateForTodayAndNextDay=true;boolean updateForallDays=true;
	    	  	boolean updateSymbolsTableData=false; boolean updateAllData=true;
	    	  	boolean updateResultTable=false;boolean isIntraDayData=true;boolean insertAllDataToResult = false;
	    	  	Float diffPerc=0.00f;String iter=iteration;boolean fastProcess=true;
	    	  	if(!iter.equals("1d"))
    	  			name =name+"_"+iter+"";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";

	    	  	macd.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/macd/"+iter+"/");
//    	  		macd.UpdateMACDResults(dbConnection, name, updateSymbolsTableData, updateAllData, updateResultTable, isIntraDayData, insertAllDataToResult);


	      }
		  	catch(Exception e){
				e.printStackTrace();
			}
	    
	}
}
