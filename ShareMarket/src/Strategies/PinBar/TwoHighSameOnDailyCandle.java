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

public class TwoHighSameOnDailyCandle extends Connection{
	public void getHighDeliveryPercDates( java.sql.Connection con, String name){
		ResultSet rs=null;
		float percentage=80f;
	  	List tradedate = new ArrayList<String>();
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
			rs = executeSelectSqlQuery(con, "select * from "+name+"");
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
			float trig=0f;
			
			for(int i=5; i< tradedate.size()-1; i++){
				float diff = ((float)low.get(i-5) - (float)close.get(i))*100/(float)low.get(i-5);
				if((float)high.get(i)==(float)high.get(i-1) && (float)close.get(i-1)>(float)open.get(i-1) && (float)close.get(i)<(float)open.get(i)
						&& ((float)high.get(i)>(float)r1.get(i) && (float)close.get(i)<(float)r1.get(i) || 
								(float)high.get(i)>(float)r2.get(i) && (float)close.get(i)<(float)r2.get(i) ||
								(float)high.get(i)>(float)pivot.get(i) && (float)close.get(i)<(float)pivot.get(i))
								&& (float)low.get(i+1) < (float)low.get(i)
								&& diff > 5){
					percProfitAtClosePrice = ((float)low.get(i)-(float)low.get(i+1))*100/(float)low.get(i);
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bear', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
				diff = ((float)high.get(i-5) - (float)close.get(i))*100/(float)high.get(i-5);
				if((float)low.get(i)==(float)low.get(i-1) && (float)close.get(i-1)<(float)open.get(i-1) && (float)close.get(i)>(float)open.get(i)
						&& ((float)low.get(i)<(float)s1.get(i) && (float)close.get(i)>(float)s1.get(i) || 
								(float)low.get(i)<(float)s2.get(i) && (float)close.get(i)>(float)s2.get(i) ||
								(float)low.get(i)<(float)pivot.get(i) && (float)close.get(i)>(float)pivot.get(i))
								&& (float)high.get(i+1) > (float)high.get(i)
								&& diff> 5){
					percProfitAtClosePrice = ((float)high.get(i+1)-(float)high.get(i))*100/(float)high.get(i);
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      TwoHighSameOnDailyCandle pin = new TwoHighSameOnDailyCandle();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 100000000 and todaysopen>6 order by volume desc");
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
