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

public class LostLot extends Connection{
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
	  	List tradedateF = new ArrayList<String>();
	  	List openF = new ArrayList<Float>();
	  	List highF = new ArrayList<Float>();
	  	List lowF = new ArrayList<Float>();
	  	List closeF = new ArrayList<Float>();
	  	List max_high = new ArrayList<Float>();
	  	LostLot gap = new LostLot();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
//			name= name+"_FUT";
			sql = "select * from  "+name+" as a where a.tradedate>='2016-01-01' ";
//			sql = sql + " and avg_volume*lotsize*open > 2000000000";
			rs = executeSelectSqlQuery(con, sql);
			while(rs.next()){
				tradedate.add(rs.getString("a.tradedate"));
		  		open.add(rs.getFloat("a.open"));
		  		high.add(rs.getFloat("a.high"));
		  		low.add(rs.getFloat("a.low"));
		  		close.add(rs.getFloat("a.close"));
			}
			float trig=0f, prev1,prev2,prev3,prev4, width;float gapPerc=2f,gapLimitPerc=5f, div=4f;int iter=1;String out="";
			float stopLossPerc=1f, targetPerc=4f;
			for(int i=1; i< tradedate.size()-1; i++){
				float diff =0f, stopLoss=0f;
				float profit = ((float)high.get(i+1)-(float)open.get(i+1))*100/(float)open.get(i+1);
				float profitAtclose = ((float)close.get(i+1)-(float)open.get(i+1))*100/(float)open.get(i+1);
				if(((float)close.get(i)-(float)open.get(i))*100/(float)close.get(i) > 5){
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'BULL', "+profit+", "+profitAtclose+", "+diff+", '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
				profit = ((float)open.get(i+1)-(float)low.get(i+1))*100/(float)open.get(i+1);
				profitAtclose = ((float)open.get(i+1)-(float)close.get(i+1))*100/(float)open.get(i+1);
				if(((float)open.get(i)-(float)close.get(i))*100/(float)close.get(i) > 5){
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bear', "+profit+", "+profitAtclose+", "+diff+", '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      LostLot pin = new LostLot();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	String sql="";
	    	  	sql =  "SELECT s.name FROM symbols s where volume > 50000000 and todaysopen>6 order by volume desc";
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
