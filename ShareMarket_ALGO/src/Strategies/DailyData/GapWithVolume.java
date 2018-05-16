package Strategies.DailyData;

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

public class GapWithVolume extends Connection{
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
	  	List volume = new ArrayList<Long>();
	  	List ema1 = new ArrayList<Float>();
	  	List ema2 = new ArrayList<Float>();
	  	List lowF = new ArrayList<Float>();
	  	List closeF = new ArrayList<Float>();
	  	List max_high = new ArrayList<Float>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;float gapPerc=0.5f;
		try{
			GapWithVolume gap = new GapWithVolume();
			rs = executeSelectSqlQuery(con, "select * from  "+name+"_60 as a ");
			while(rs.next()){
				tradedate.add(rs.getString("a.tradedate"));
		  		open.add(rs.getFloat("a.open"));
		  		high.add(rs.getFloat("a.high"));
		  		low.add(rs.getFloat("a.low"));
		  		ema1.add(rs.getFloat("a.ema1"));
		  		ema2.add(rs.getFloat("a.ema2"));
		  		close.add(rs.getFloat("a.close"));
			}
			int count=0;float profit=0f;
			for(int i=1; i< tradedate.size(); i++){
				if((float)open.get(i) > (float)ema1.get(i) && (float)close.get(i) < (float)ema1.get(i)
						&& ((float)open.get(i) - (float)close.get(i))*100/(float)open.get(i) > 1){
					for(int j=i-40; j< i; j++){
						if((float)low.get(j) > (float)ema1.get(j)){
							count++;
						}else count=0;
					}
					if(count > 20){
						profit = ((float)close.get(i) -(float)low.get(i+1))*100/(float)close.get(i);
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bear', "+low.get(i)+", "+profit+", "+profit+", '"+tradedate.get(i)+"')";
						executeSqlQuery(con, sql);
					}
				}
				
				if((float)open.get(i) < (float)ema1.get(i) && (float)close.get(i) > (float)ema1.get(i)
						&& ((float)close.get(i) - (float)open.get(i))*100/(float)open.get(i) > 1){
					for(int j=i-40; j< i; j++){
						if((float)high.get(j) < (float)ema1.get(j)){
							count++;
						}else count=0;
					}
					if(count > 20){
						profit = ((float)high.get(i+1) -(float)close.get(i))*100/(float)close.get(i);
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bull', "+low.get(i)+", "+profit+", "+profit+", '"+tradedate.get(i)+"')";
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
	      GapWithVolume pin = new GapWithVolume();
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
