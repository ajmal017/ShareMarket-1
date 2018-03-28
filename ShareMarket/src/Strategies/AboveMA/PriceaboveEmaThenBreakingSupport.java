package Strategies.AboveMA;

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

public class PriceaboveEmaThenBreakingSupport extends Connection{
	public void getData( java.sql.Connection con, String name){
		ResultSet rs=null;
		float percentage=80f;
		
	  	List tradedate = new ArrayList<String>();
	  	List perc = new ArrayList<Float>();
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
	  	List volume = new ArrayList<Long>();
	  	List min_low = new ArrayList<Float>();
	  	List ema2 = new ArrayList<Float>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select * from "+name+" ");
			while(rs.next()){
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		ema2.add(rs.getFloat("ema2"));
		  		volume.add(rs.getLong("volume"));
			}
			float trig=0f;String tradedDate="", exitDate="";
			int count = 0;boolean isVolEligible=true;
			for(int i=50; i< tradedate.size(); i++){
				isVolEligible=true;
				float lost = ((float)open.get(i) - (float)close.get(i))*100/(float)close.get(i);
				float percAboveBelowMA = ((float)ema2.get(i) - (float)close.get(i))*100/(float)close.get(i);
//				float percMA = ((float)ema2.get(i) - (float)close.get(i))*100/(float)close.get(i);
				if(lost>5 && (float)open.get(i) > (float)ema2.get(i) && (float)close.get(i) < (float)ema2.get(i)
						&& (float)close.get(i-1) > (float)ema2.get(i-1) && percAboveBelowMA > 1){
					for(int j=i-30; j< i; j++){
						if((float)close.get(j) > ((float)ema2.get(j)) && (float)ema2.get(j) !=0){
							count++;
						}else count=0;
					}
					for(int j=i-10; j< i; j++){
						if((long)volume.get(j) > (long)volume.get(i)){
							isVolEligible = false;
						}
					}
					if(count>29 && isVolEligible==true){
						percProfitAtClosePrice = ((float)open.get(i+1) - (float)low.get(i+1))*100/(float)open.get(i+1);
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited) "
			  					+ " values ('"+name+"', 'Bear', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "
			  							+ ""+percProfitAtClosePrice+", '"+tradedate.get(i+1)+"', '"+tradedate.get(i+1)+"')";
						executeSqlQuery(con, sql);
					}
				}
				
				float gained = ((float)close.get(i) - (float)open.get(i))*100/(float)open.get(i);
				percAboveBelowMA = ((float)close.get(i) - (float)ema2.get(i))*100/(float)close.get(i);
				if(gained>5 && (float)open.get(i) < (float)ema2.get(i) && (float)close.get(i) > (float)ema2.get(i)
						&& (float)close.get(i-1) < (float)ema2.get(i-1) && percAboveBelowMA>1){
					for(int j=i-30; j< i; j++){
						if((float)close.get(j) < ((float)ema2.get(j)) && (float)ema2.get(j) !=0){
							count++;
						}else count=0;
					}
					for(int j=i-10; j< i; j++){
						if((long)volume.get(j) > (long)volume.get(i)){
							isVolEligible = false;
						}
					}
					if(count>29 && isVolEligible==true){
						percProfitAtClosePrice = ((float)high.get(i+1) - (float)open.get(i+1))*100/(float)open.get(i+1);
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited) "
			  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "
			  							+ ""+percProfitAtClosePrice+", '"+tradedate.get(i+1)+"', '"+tradedate.get(i+1)+"')";
						executeSqlQuery(con, sql);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      PriceaboveEmaThenBreakingSupport pin = new PriceaboveEmaThenBreakingSupport();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 100000000 order by volume desc");
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
	    	  		pin.getData(dbConnection, name);
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
