package Strategies.Adx;

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

public class AdxBelowBothLinesForLongTime extends Connection{
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
	  	List adx = new ArrayList<Float>();
	  	List di_plus = new ArrayList<Float>();
	  	List di_minus = new ArrayList<Float>();
	  	List max_high = new ArrayList<Float>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select * from "+name+"_5");
			while(rs.next()){
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		pivot.add(rs.getFloat("pivot"));
		  		r1.add(rs.getFloat("r1"));
		  		r2.add(rs.getFloat("r2"));
		  		s1.add(rs.getFloat("s1"));
		  		s2.add(rs.getFloat("s2"));
		  		adx.add(rs.getFloat("adx"));
		  		di_plus.add(rs.getFloat("DI_PLUS_AVERAGE"));
		  		di_minus.add(rs.getFloat("DI_MINUS_AVERAGE"));
			}
			float trig=0f, prev1,prev2,prev3,prev4, width;int count=0;
			String daysLow, daysClose, daysHigh;
			for(int i=50; i< tradedate.size()-1; i++){
				count=0;
				if((float)adx.get(i) <(float)di_plus.get(i) && (float)adx.get(i) < (float)di_minus.get(i) && (float)adx.get(i+1) > (float)di_minus.get(i+1)
						&& (float)adx.get(i+1) < (float)di_plus.get(i+1)){
					for(int j=i-50; j< i; j++){
						if((float)adx.get(j) <(float)di_plus.get(j) && (float)adx.get(j) < (float)di_minus.get(j)){
							count++;
						}else count =0;
					}
				}
				if(count>45){
						trig=(float)close.get(i+1); 
						sql = "select max(high) from "+name+"_5 where tradedate >'"+tradedate.get(i+1)+"' and tradedate <= concat(Date('"+tradedate.get(i)+"'),' 15:15:00')";
						daysHigh = executeCountQuery(con, sql);
          				sql = "select close from "+name+"_5 where tradedate = concat(Date('"+tradedate.get(i)+"'),' 15:15:00')";
          				daysClose = executeCountQuery(con, sql);
						percProfitAtHighPrice = (Float.parseFloat(daysHigh)-trig)*100/trig;
						percProfitAtClosePrice = (Float.parseFloat(daysClose)-trig)*100/trig;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "+percProfitAtHighPrice+", '"+tradedate.get(i+1)+"')";
						executeSqlQuery(con, sql);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      AdxBelowBothLinesForLongTime pin = new AdxBelowBothLinesForLongTime();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 10000 and todaysopen>6 order by volume desc");
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
