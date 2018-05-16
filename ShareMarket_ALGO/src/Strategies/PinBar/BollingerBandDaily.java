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

public class BollingerBandDaily extends Connection{
	public void getHighDeliveryPercDates( java.sql.Connection con, String name){
		ResultSet rs=null;
		float percentage=80f;
	  	List tradedate = new ArrayList<String>();
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List pivot = new ArrayList<Float>();
	  	List upper = new ArrayList<Float>();
	  	List lower = new ArrayList<Float>();
	  	List s1 = new ArrayList<Float>();
	  	List s2 = new ArrayList<Float>();
	  	List volume = new ArrayList<Long>();
	  	List max_high = new ArrayList<Float>();
	      BollingerBandDaily pin = new BollingerBandDaily();
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
		  		upper.add(rs.getFloat("BB_upperBand_20"));
		  		lower.add(rs.getFloat("BB_lowerBand_20"));
		  		volume.add(rs.getLong("volume"));
			}
			float trig=0f;boolean isValid=true;String res="";
			float day1,day2,day3, day4;
			for(int i=3; i< tradedate.size(); i++){
				day1 = ((float)open.get(i)-(float)close.get(i))*100/(float)open.get(i);
				boolean check = ((float)high.get(i)-(float)open.get(i))>Math.abs(((float)open.get(i)-(float)close.get(i)))*2
						&& ((float)close.get(i)-(float)low.get(i))
						< ((float)open.get(i)-(float)close.get(i)) && (float)open.get(i)>(float)close.get(i);
				day2 = ((float)close.get(i-1)-(float)open.get(i-1))*100/(float)open.get(i-1);
				day3 = ((float)close.get(i-2)-(float)open.get(i-2))*100/(float)open.get(i-2);
//				day4 = ((float)close.get(i-3)-(float)open.get(i-3))*100/(float)open.get(i-3);
//				if(day2 >2 && day3 > 2 && (float)close.get(i-1) > (float)upper.get(i-1) && (float)close.get(i-2) > (float)upper.get(i-2)
//						&& day1 > 2 )
				if(day2 >2 && day3 > 2 && (float)close.get(i-1) > (float)upper.get(i-1) && (float)close.get(i-2) > (float)upper.get(i-2)
						&& check==true)
				{
//					percProfitAtLowPrice = ((float)open.get(i+1)-(float)low.get(i+1))*100/(float)open.get(i+1);
//					percProfitAtClosePrice = ((float)open.get(i+1)-(float)close.get(i+1))*100/(float)open.get(i+1);
//					percProfitAtLowPrice = ((float)low.get(i)-(float)low.get(i+1))*100/(float)low.get(i);
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bear', "+percProfitAtLowPrice+", "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", '"+tradedate.get(i)+"')";
					executeSqlQuery(con, sql);
				}
				check = ((float)open.get(i)-(float)low.get(i))>Math.abs(((float)open.get(i)-(float)close.get(i)))*2
						&& ((float)high.get(i)-(float)close.get(i))
						< ((float)close.get(i)-(float)open.get(i)) && (float)open.get(i)<(float)close.get(i);
				
				day1 = ((float)close.get(i)-(float)open.get(i))*100/(float)open.get(i);
				day2 = ((float)open.get(i-1)-(float)close.get(i-1))*100/(float)open.get(i-1);
				day3 = ((float)open.get(i-2)-(float)close.get(i-2))*100/(float)open.get(i-2);
				day4 = ((float)open.get(i-3)-(float)close.get(i-3))*100/(float)open.get(i-3);
				if(day2 >2 && day3 > 2 && (float)close.get(i-1) < (float)lower.get(i-1) && (float)close.get(i-2) < (float)lower.get(i-2)
						&& check==true)
				{
//					percProfitAtHighPrice = ((float)high.get(i+1)-(float)open.get(i+1))*100/(float)open.get(i+1);
//					percProfitAtClosePrice = ((float)close.get(i+1)-(float)open.get(i+1))*100/(float)open.get(i+1);
//					percProfitAtHighPrice = ((float)high.get(i+1)-(float)high.get(i))*100/(float)high.get(i);
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bull', "+percProfitAtHighPrice+", "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", '"+tradedate.get(i)+"')";
					executeSqlQuery(con, sql);
				}
			}
		}
			
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void checkForResistanceSupport(java.sql.Connection con, String name, String result){
		int days = Integer.parseInt(result.split("_")[0]);
		String prevDayWhenCrossed = result.split("_")[1];
		String prevHighWhenCrossed = result.split("_")[2];
		String res="", sql="";boolean check=true;
//		while(check){
			sql = "select concat(DATEdiff('"+prevDayWhenCrossed+"',tradedate),'_', tradedate,'_',high) from "+name+" where tradedate<'"+prevDayWhenCrossed+"' "
					+ " and high> "+prevHighWhenCrossed+" order by tradedate desc limit 1";
			res = executeCountQuery(con,sql);
			System.out.println(sql);
			if(res.equals("")) return;
			if(Integer.parseInt(res.split("_")[0]) > days/2){
				System.out.println(result+","+name+","+res);
			}
			prevDayWhenCrossed = res.split("_")[1];
			prevHighWhenCrossed = res.split("_")[2];
//		}
	}
   public static void main(String[] args)  {
	      Test t = new Test();
	      BollingerBandDaily pin = new BollingerBandDaily();
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
