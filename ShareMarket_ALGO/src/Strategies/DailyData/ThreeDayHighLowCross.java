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

public class ThreeDayHighLowCross extends Connection{
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
	  	ThreeDayHighLowCross gap = new ThreeDayHighLowCross();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select * from  "+name+" as a ");
			while(rs.next()){
				tradedate.add(rs.getString("a.tradedate"));
		  		open.add(rs.getFloat("a.open"));
		  		high.add(rs.getFloat("a.high"));
		  		low.add(rs.getFloat("a.low"));
		  		close.add(rs.getFloat("a.close"));
			}
			float trig=0f, prev1,prev2,prev3,prev4, width;float gapPerc=1f, div=3;int iter=1;String out="";
			float stopLossPerc=1f, targetPerc=.5f;
			/*for(int i=3; i< tradedate.size(); i++){
				float diff =0f, stopLoss=0f;
				if((float)close.get(i) > (float)high.get(i-1) && (float)close.get(i) > (float)high.get(i-2) && (float)close.get(i) > (float)high.get(i-3)
						&& (float)high.get(i+1) > (float)high.get(i) && (float)open.get(i+1) < (float)high.get(i)){
					trig = (float)high.get(i);
					percProfitAtHighPrice = ((float)high.get(i+1) - trig)*100/trig;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bull', "+percProfitAtHighPrice+", "+percProfitAtHighPrice+", "+diff+", '"+tradedate.get(i+1)+"')";
						executeSqlQuery(con, sql);
				}
				if((float)close.get(i) < (float)low.get(i-1) && (float)close.get(i) < (float)low.get(i-2) && (float)close.get(i) < (float)low.get(i-3)
						&& (float)low.get(i+1) < (float)low.get(i) && (float)open.get(i+1) > (float)low.get(i)){
					trig = (float)low.get(i);
					percProfitAtLowPrice = (trig-(float)low.get(i+1))*100/trig;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bear', "+percProfitAtLowPrice+", "+percProfitAtLowPrice+", "+diff+", '"+tradedate.get(i+1)+"')";
						executeSqlQuery(con, sql);
				}
			}*/
			for(int i=3; i< tradedate.size(); i++){
				float diff =0f, stopLoss=0f;
				float gapOfClose = ((float)close.get(i)-(float)open.get(i+1))*100/ (float)close.get(i);
				if((float)close.get(i) > (float)high.get(i-1) && (float)close.get(i) > (float)high.get(i-2) && (float)close.get(i) > (float)high.get(i-3)
						&& gapOfClose>0.5 && (float)open.get(i+1) < (float)high.get(i)){
					trig = (float)open.get(i+1);
					percProfitAtHighPrice = ((float)high.get(i+1) - trig)*100/trig;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bull', "+percProfitAtHighPrice+", "+percProfitAtHighPrice+", "+diff+", '"+tradedate.get(i+1)+"')";
						executeSqlQuery(con, sql);
				}
//				if((float)close.get(i) < (float)low.get(i-1) && (float)close.get(i) < (float)low.get(i-2) && (float)close.get(i) < (float)low.get(i-3)
//						&& (float)low.get(i+1) < (float)low.get(i) && (float)open.get(i+1) > (float)low.get(i)){
//					trig = (float)low.get(i);
//					percProfitAtLowPrice = (trig-(float)low.get(i+1))*100/trig;
//						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
//			  					+ " values ('"+name+"', 'Bear', "+percProfitAtLowPrice+", "+percProfitAtLowPrice+", "+diff+", '"+tradedate.get(i+1)+"')";
//						executeSqlQuery(con, sql);
//				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public String isOpenPriceTouched(java.sql.Connection con, String name, String date, String dir, float trig){
		String sql = "";int interval=5;String isOpenPriceTouched="";
		if(dir.equalsIgnoreCase("Bull")){
			sql = "select tradedate from "+name+"_"+interval+" where high>"+trig+" order by tradedate limit 1";
			String trigDate = executeCountQuery(con, sql);
			sql = "select case when (low < (open-OPEN*0.5/100)) then 1 else 0 end from "+name+"_"+interval+" where tradedate > '"+trigDate+"' and date(tradedate) = '"+date+"'";
			isOpenPriceTouched = executeCountQuery(con, sql);
		}
		return isOpenPriceTouched;
	}
   public static void main(String[] args)  {
	      Test t = new Test();
	      ThreeDayHighLowCross pin = new ThreeDayHighLowCross();
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
