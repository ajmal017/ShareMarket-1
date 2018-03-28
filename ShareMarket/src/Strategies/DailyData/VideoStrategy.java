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

public class VideoStrategy extends Connection{
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
	  	List cci = new ArrayList<Float>();
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
		  		cci.add(rs.getFloat("CCI"));
			}
			float trig=0f, prev1,prev2,prev3,prev4, width;float gapPerc=3f, div=3;
			for(int i=1; i< tradedate.size(); i++){
//				if(((float)open.get(i)-(float)high.get(i-1))*100/(float)high.get(i-1) > gapPerc && (float)cci.get(i-1) > 100){
//					float trigger = ((float)open.get(i)-(float)high.get(i-1))/div;
//					trigger = ((float)open.get(i)+trigger);
//					if((float)high.get(i) > trigger){
//						percProfitAtHighPrice = ((float)high.get(i)-trigger)*100/(float)trigger;
//						percProfitAtClosePrice = ((float)close.get(i)-trigger)*100/(float)trigger;
//						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
//			  					+ " values ('"+name+"', 'Bull2', "+trigger+", "+percProfitAtHighPrice+", "+percProfitAtClosePrice+", '"+tradedate.get(i)+"')";
//						executeSqlQuery(con, sql);
//					}
//				}
				float diff = (float)high.get(i)-(float)open.get(i);
				if(((float)open.get(i)-(float)high.get(i-1))*100/(float)high.get(i-1) > gapPerc ){
					float trigger = ((float)open.get(i)-(float)high.get(i-1))/div;
					trigger = ((float)open.get(i)-trigger);
					if((float)low.get(i) < trigger){
						percProfitAtClosePrice = (trigger - (float)low.get(i))*100/(float)trigger;
//						percProfitAtClosePrice = (trigger - Math.min((float)low.get(i), (float)open.get(i+1)))*100/(float)trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bear', "+trigger+", "+percProfitAtClosePrice+", "+diff+", '"+tradedate.get(i)+"')";
						executeSqlQuery(con, sql);
					}
				}
//				if(((float)low.get(i-1)-(float)open.get(i))*100/(float)open.get(i) > gapPerc && (float)cci.get(i-1) < -100){
//					float trigger = ((float)low.get(i-1)-(float)open.get(i))/div;
//					trigger = ((float)open.get(i)-trigger);
//					if((float)low.get(i) < trigger){
//						percProfitAtHighPrice = (trigger - (float)low.get(i))*100/(float)trigger;
//						percProfitAtClosePrice = (trigger - (float)close.get(i))*100/(float)trigger;
//						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
//			  					+ " values ('"+name+"', 'Bear2', "+trigger+", "+percProfitAtHighPrice+", "+percProfitAtClosePrice+", '"+tradedate.get(i)+"')";
//						executeSqlQuery(con, sql);
//					}
//				}
				diff = (float)open.get(i)-(float)low.get(i);
				if(((float)low.get(i-1)-(float)open.get(i))*100/(float)open.get(i) > gapPerc){
					float trigger = ((float)low.get(i-1)-(float)open.get(i))/div;
					trigger = ((float)open.get(i)+trigger);
					if((float)high.get(i) > trigger){
						percProfitAtClosePrice = ((float)high.get(i)-trigger)*100/(float)trigger;
//						percProfitAtClosePrice = (Math.max((float)high.get(i), (float)open.get(i+1))-trigger)*100/(float)trigger;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bull', "+trigger+", "+percProfitAtClosePrice+", "+diff+", '"+tradedate.get(i)+"')";
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
	      VideoStrategy pin = new VideoStrategy();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	String sql="";
	    	  	sql =  "SELECT s.name FROM symbols s where volume > 100000000 and todaysopen>6 order by volume desc";
	    	  	sql =  "SELECT name FROM symbols where name in ("
	    	  			+ "'ACC','AMBUJACEM','AXISBANK','BHARTIARTL','BHEL','BPCL','CAIRN','CIPLA','DLF','DRREDDY',"
	    	  			+ "'GAIL','GRASIM','HCLTECH','HDFC','HDFCBANK','HEROHONDA','HINDALCO','HINDUNILVR','ICICIBANK','',"
	    	  			+ "'','','','','','','','','','',"
	    	  			+ "'','','','','','','','','','',"
	    	  			+ "'','','','','','','','','','',"
	    	  			+ "'','','','','','','','','','')";
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
