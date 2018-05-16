package Strategies.PinBar;

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

public class SwingDailyDataTooMuchChangesPrevDay extends Connection{
	public void getHighDeliveryPercDates( java.sql.Connection con, String name){
		ResultSet rs=null;
		float percentage=80f;
		List perc = new ArrayList<Float>();
	  	List tradedate = new ArrayList<String>();
	  	List tradedQuantity = new ArrayList<Long>();
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List pivot = new ArrayList<Float>();
	  	List r1 = new ArrayList<Float>();
	  	List r2 = new ArrayList<Float>();
	  	List s1 = new ArrayList<Float>();
	  	List s2 = new ArrayList<Float>();
	  	List max_high = new ArrayList<Float>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select * from "+name+" b ");
			while(rs.next()){
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		max_high.add(rs.getFloat("max_past_high"));
		  		pivot.add(rs.getFloat("pivot"));
		  		r1.add(rs.getFloat("r1"));
		  		r2.add(rs.getFloat("r2"));
		  		s1.add(rs.getFloat("s1"));
		  		s2.add(rs.getFloat("s2"));
			}
			float trig=0f, prev1,prev2,prev3,prev4, width;
			for(int i=1; i< tradedate.size()-1; i++){
				prev1 = ((float)open.get(i-1)-(float)close.get(i-1))*100/(float)open.get(i-1);
				if(prev1 >5 && (float)low.get(i-1) < (float)s2.get(i-1) && (float)close.get(i-1) > (float)s2.get(i-1)){
					trig=(float)open.get(i); 
					percProfitAtHighPrice = ((float)high.get(i)-trig)*100/trig;
					percProfitAtClosePrice = ((float)close.get(i)-trig)*100/trig;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "+percProfitAtHighPrice+", '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
				prev1 = ((float)close.get(i-1)-(float)open.get(i-1))*100/(float)open.get(i-1);
				if(prev1 >5 && (float)high.get(i-1) > (float)r2.get(i-1) && (float)close.get(i-1) < (float)r2.get(i-1)){
					trig=(float)open.get(i);  
					percProfitAtHighPrice = (trig-(float)low.get(i))*100/trig;
					percProfitAtClosePrice = (trig-(float)close.get(i))*100/trig;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bear', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "+percProfitAtHighPrice+", '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      SwingDailyDataTooMuchChangesPrevDay pin = new SwingDailyDataTooMuchChangesPrevDay();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 10000000 and todaysopen>6 order by volume desc");
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
