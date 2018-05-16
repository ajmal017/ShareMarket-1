package Strategies.Futures;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.poi.hssf.record.DBCellRecord;

import Indicators.Connection;
import Indicators.Test;

public class GapFuturesAndEquity_DiffGap extends Connection{
	public void getHighDeliveryPercDates( java.sql.Connection con, String name){
		ResultSet rs=null;
		float percentage=80f;
		List perc = new ArrayList<Float>();
	  	List tradedate = new ArrayList<String>();
	  	List tradedateF = new ArrayList<String>();
	  	List tradedQuantity = new ArrayList<Long>();
	  	List openF = new ArrayList<Float>();
	  	List highF = new ArrayList<Float>();
	  	List lowF = new ArrayList<Float>();
	  	List closeF = new ArrayList<Float>();

	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List cci = new ArrayList<Float>();
	  	List r1 = new ArrayList<Float>();
	  	List r2 = new ArrayList<Float>();
	  	List s1 = new ArrayList<Float>();
	  	List s2 = new ArrayList<Float>();
	  	List max_high = new ArrayList<Float>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select * from "+name+" a,"+name+"_FUT b where a.tradedate = b.tradedate"
					+ " ");
			while(rs.next()){
				tradedate.add(rs.getString("a.tradedate"));
				tradedateF.add(rs.getString("b.tradedate"));

				open.add(rs.getFloat("a.open"));
		  		high.add(rs.getFloat("a.high"));
		  		low.add(rs.getFloat("a.low"));
		  		close.add(rs.getFloat("a.close"));
		  		
				openF.add(rs.getFloat("b.open"));
		  		highF.add(rs.getFloat("b.high"));
		  		lowF.add(rs.getFloat("b.low"));
		  		closeF.add(rs.getFloat("b.close"));
			}
			name = name+"_FUT";
			float trig=0f, prev1,prev2,prev3,prev4, width;float gapPerc=0.5f, div=6;
			float targetPerc=0.2f;int check=0;
			float trigger=0f, profitF=0f, profitClose;
			for(int i=1; i< tradedate.size(); i++){
				trigger = ((float)openF.get(i)-(float)highF.get(i-1))/div;
				trigger = ((float)openF.get(i)-trigger);
				if(((float)openF.get(i)-(float)highF.get(i-1))*100/(float)highF.get(i-1) > gapPerc 
						&& Math.abs((float)open.get(i) - (float)close.get(i-1))*100/(float)close.get(i-1) < 0.5
						&& (float)lowF.get(i)< trigger){
					profitF = ((float)openF.get(i)-(float)lowF.get(i))*100/(float)openF.get(i);
					profitF = (trigger-(float)lowF.get(i))*100/trigger;
					profitClose = (trigger-(float)closeF.get(i))*100/trigger;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bear_OpenGap', "+profitClose+", "+profitF+", "+trigger+", '"+tradedate.get(i)+"')";
					executeSqlQuery(con, sql);
				}
				trigger = ((float)lowF.get(i-1)-(float)openF.get(i))/div;
				trigger = ((float)openF.get(i)+trigger);
				if(((float)lowF.get(i-1)-(float)openF.get(i))*100/(float)lowF.get(i-1) > gapPerc 
						&& Math.abs((float)open.get(i) - (float)close.get(i-1))*100/(float)close.get(i-1) < 0.5
						&&  (float)highF.get(i) > trigger){
					profitF = ((float)highF.get(i)-(float)openF.get(i))*100/(float)openF.get(i);
					profitF = ((float)highF.get(i)-trigger)*100/trigger;
					profitClose = ((float)closeF.get(i)-trigger)*100/trigger;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bull_OpenGap', "+profitClose+", "+profitF+", "+trigger+", '"+tradedate.get(i)+"')";
					executeSqlQuery(con, sql);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      GapFuturesAndEquity_DiffGap pin = new GapFuturesAndEquity_DiffGap();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	String sql="";
	    	  	sql =  "SELECT s.name FROM symbols s where volume > 100000000 and todaysopen>6 order by volume desc";
	    	  	sql =  "SELECT s.name FROM symbols s, margintables m where s.name=m.name ";
	    	  	rs = con.executeSelectSqlQuery(dbConnection, sql);
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
	    	  	boolean updateResultTable=false;boolean isIntraDayData=false;
	    	  	boolean insertAllDataToResult = false;
	    	  	String iter="1d";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("s.name");
	    	  		System.out.println(name);
	    	  		if(!iter.equals("1d"))
	    	  			name =name+"_"+iter+"";
	    	  		pin.getHighDeliveryPercDates(dbConnection, name);
	    	  	}
	      }
		  	
	      catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      catch(Exception e){
				e.printStackTrace();
			}
	      finally{
	  	  	if(dbConnection!=null)
				try {
					dbConnection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	      }
   }
}
