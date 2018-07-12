package Strategies.Supertrend;

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

public class WeeklyHighLowAsResSupport extends Connection{
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
	  	List max_high = new ArrayList<Float>();
	  	List min_low = new ArrayList<Float>();
	  	List up = new ArrayList<Float>();List down = new ArrayList<Float>();List supertrendReversal = new ArrayList<String>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select tradedate, open, high,low,close,supertrend_Reversal*1 as supertrend_Reversal,"
					+ " supertrend_up_band, supertrend_down_band from "+name+" ");
			System.out.println(name);
			while(rs.next()){
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		up.add(rs.getFloat("supertrend_up_band"));
		  		down.add(rs.getFloat("supertrend_down_band"));
		  		supertrendReversal.add(rs.getString("supertrend_Reversal"));
			}
			float trig=0f;String tradedDate="", exitDate="";
			int count = 0;
			for(int i=2; i< tradedate.size(); i++){
				if(((float)high.get(i)-(float)close.get(i))*100/(float)close.get(i) > 4 && (float)close.get(i)>(float)open.get(i)){
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited) "
		  					+ " values ('"+name+"', 'Bear', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "
		  							+ ""+percProfitAtClosePrice+", '"+tradedate.get(i+1)+"', '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
				if(((float)close.get(i)-(float)close.get(i))*100/(float)close.get(i) > 4 && (float)close.get(i)>(float)open.get(i)){
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited) "
		  					+ " values ('"+name+"', 'Bear', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "
		  							+ ""+percProfitAtClosePrice+", '"+tradedate.get(i+1)+"', '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void getLongBodyPrevWeek( java.sql.Connection con, String name){
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
	  	List max_high = new ArrayList<Float>();
	  	List min_low = new ArrayList<Float>();
	  	List up = new ArrayList<Float>();List down = new ArrayList<Float>();List supertrendReversal = new ArrayList<String>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select tradedate, open, high,low,close from "+name+" where year(tradedate)>=2015");
			System.out.println(name);
			while(rs.next()){
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
			}
			float trig=0f;String tradedDate="", exitDate="";
			int count = 0;
			String dailyName = name.split("_")[0];
			float shadowPerc=1f, bodyPerc=10f;
			for(int i=0; i< tradedate.size()-1; i++){
				String resOpen = executeCountQuery(con, "select intradayOpen from "+dailyName+" where tradedate>'"+tradedate.get(i)+"' "
						+ " order by tradedate limit 1");
				String resClose = executeCountQuery(con, "select intraday3Min3_18_Close from "+dailyName+" where tradedate>'"+tradedate.get(i)+"' "
						+ " order by tradedate limit 1");
				if(resOpen == null || resOpen=="" || resClose==null || resClose==""){
					continue;
				}
				float dailyOpen = Float.parseFloat(resOpen);
				float dailyClose = Float.parseFloat(resClose);
				if(((float)high.get(i)-(float)close.get(i))*100/(float)close.get(i) < shadowPerc && 
						((float)close.get(i) - (float)open.get(i))*100/(float)open.get(i) > bodyPerc){
					
//					percProfitAtClosePrice = ((float)close.get(i+1) - (float)open.get(i+1))*100/(float)open.get(i+1);
					percProfitAtClosePrice = (dailyClose - dailyOpen)*100/dailyOpen;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited) "
		  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "
		  							+ ""+percProfitAtClosePrice+", '"+tradedate.get(i+1)+"', '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
				
				if(((float)close.get(i)-(float)low.get(i))*100/(float)low.get(i) < shadowPerc && 
						((float)open.get(i) - (float)close.get(i))*100/(float)close.get(i) > bodyPerc){
//					percProfitAtClosePrice = ((float)open.get(i+1) - (float)close.get(i+1))*100/(float)close.get(i+1);
					percProfitAtClosePrice = (dailyOpen - dailyClose)*100/dailyClose;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited) "
		  					+ " values ('"+name+"', 'Bear', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "
		  							+ ""+percProfitAtClosePrice+", '"+tradedate.get(i+1)+"', '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      WeeklyHighLowAsResSupport pin = new WeeklyHighLowAsResSupport();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 5000000 order by volume desc");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
	    	  	boolean updateResultTable=false;boolean isIntraDayData=false;
	    	  	boolean insertAllDataToResult = false;
	    	  	String iter="7d";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("s.name");
//	    	  		System.out.println(name);
	    	  		if(!iter.equals("1d"))
	    	  			name =name+"_"+iter+"";
	    	  		pin.getLongBodyPrevWeek(dbConnection, name);
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
