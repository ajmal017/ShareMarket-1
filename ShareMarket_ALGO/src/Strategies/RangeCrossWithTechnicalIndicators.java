package Strategies;

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

public class RangeCrossWithTechnicalIndicators extends Connection{

	   public static void main(String[] args)  {
		      Test t = new Test();
		      RangeCrossWithTechnicalIndicators range = new RangeCrossWithTechnicalIndicators();
		      java.sql.Connection dbConnection = null;
		      boolean updateSymbolsTableData=true; boolean updateAllData=true;
		      try{
		    	  
		    	  Connection con = new Connection();
		    	  	dbConnection = con.getDbConnection();
		    	  	ResultSet rs=null;
		    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 100000 and s.name<>'Mindtree' and s.name='jindalstel' order by volume desc ");
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
		    	  		range.getdate(dbConnection, name);
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
	   
	   public void getdate(java.sql.Connection con, String name) throws SQLException{
		   String sql="Select b.tradedate,tradedQuantity, perc,coalesce((case when reversal='BullToday' then 'Bull'  when macd_change_dir='UP' then 'Bull' when hist_zero_cross='Bullish Cross' "+
		" then 'Bull' when adx_dm_crossover='Bull' then 'Bull' when adx_bullish_enter='Yes' then 'Bull' when will_reversal='Bull' then 'Bull' "+
		" when cci_reversal='Bull' then 'Bull' when envlp_cross='Bull' then 'Bull' end),'') as Bull, "+
		" coalesce((case when reversal='BearToday' then 'Bear'  when macd_change_dir='DOWN' then 'Bear' when hist_zero_cross='Bearish Cross' "+
		" then 'Bear' when adx_dm_crossover='Bear' then 'Bear' when adx_bearish_enter='Yes' then 'Bear' when will_reversal='Bear' then 'Bear' "+
		" when cci_reversal='Bear' then 'Bear' when envlp_cross='Bear' then 'Bear' end),'') as Bear "
		+ " from "+name+" b, "+name+"_dp dp where b.tradedate=dp.tradedate ";
		   ResultSet rs=null;
		   rs = executeSelectSqlQuery(con, sql);

		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	List date = new ArrayList<String>();
		  	List dailyDate = new ArrayList<String>();
		  	List bull = new ArrayList<String>();
		  	List bear = new ArrayList<String>();
		  	List perc = new ArrayList<Float>();
		  	List tradedQuantity = new ArrayList<Long>();
		  	List volume = new ArrayList<Integer>();
		   while (rs.next()){
			   dailyDate.add(rs.getString("b.tradedate"));
			   bull.add(rs.getString("Bull"));
			   bear.add(rs.getString("Bear"));
			   tradedQuantity.add(rs.getLong("tradedQuantity"));
			   perc.add(rs.getFloat("perc"));
			   
		   }
		   for(int i=1; i<dailyDate.size()-1; i++){
			   if((bull.get(i).toString().equals("Bull") || bear.get(i).toString().equals("Bear")) )
				   UpdatePinResultsRange(con, name, dailyDate.get(i+1).toString(), bull.get(i).toString(), bear.get(i).toString(),open,high,low,close,date,volume);
		   }
		   rs.close();
	   }
	   
	   public void UpdatePinResultsRange(java.sql.Connection con, String name,String dailyDate, String bull, String bear, List open, List high, List low, List close, List date, List volume) throws SQLException {
		   ResultSet rs=null;
		   List pivot = new ArrayList<Float>();
		  	List r1 = new ArrayList<Float>();List s1 = new ArrayList<Float>();
		  	List r2 = new ArrayList<Float>();List s2 = new ArrayList<Float>();
		  	String sql="";String interval="5";
		  	try {
		  		if(!interval.equals("")){
		  			sql="select * from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:10:00') and tradedate <= concat(Date('"+dailyDate+"'),' 15:00:00')";
		  		}else{
		  			sql="select * from "+name+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:10:00') and tradedate <= concat(Date('"+dailyDate+"'),' 15:00:00')";
		  		}
		  		
				rs = executeSelectSqlQuery(con, sql);
				open.clear();high.clear();low.clear();close.clear();volume.clear();date.clear();
			  	while (rs.next()){
			  		open.add(rs.getFloat("open"));
			  		high.add(rs.getFloat("high"));
			  		low.add(rs.getFloat("low"));
			  		close.add(rs.getFloat("close"));
			  		date.add(rs.getString("tradedate"));
			  		volume.add(rs.getInt("volume"));
			  		pivot.add(rs.getFloat("pivot"));
			  		r1.add(rs.getFloat("R1"));
			  		s1.add(rs.getFloat("S1"));
			  		r2.add(rs.getFloat("R2"));
			  		s2.add(rs.getFloat("S2"));
			  	}
			  	
			  	String tableName = "williamsresults";
			  	float filter=0f, filterPerc = 2f, rangeHigh=0f, rangeLow=0f;
			  	Float profitPerc=0.0f, profitRupees=0.0f;
			  	float range=0f;int x=0; float p=1f, l=-2f, triggerPrice=0f, dailyRange=0f, stopLossPrice=0f, stopLoss=3f;
			  	String tradeDate="";String dayshigh="", dayslow="",daysclose=""; 
			  	String daysOpen="";
			  	
				 sql = "select coalesce(max(high),0) as rangehigh from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:10:00') and tradedate <= concat(Date('"+dailyDate+"'),' 09:40:00')";
				 rangeHigh = Float.parseFloat(executeCountQuery(con, sql));
				 sql = "select coalesce(min(low),0) as rangelow from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:10:00') and tradedate <= concat(Date('"+dailyDate+"'),' 09:40:00')";
				 rangeLow = Float.parseFloat(executeCountQuery(con, sql));
				 
				 float pivotRange=0f;
				 daysOpen = executeCountQuery(con, "select open from "+name+" where tradedate=Date('"+dailyDate+"')");
			  		for (int i=2; i< date.size()-1; i++){
			  			if(rangeHigh==0 || rangeLow==0) break;
			  				tradeDate = (String) date.get(i).toString();
			  				triggerPrice = (rangeHigh + (rangeHigh*0.0f/100.00f));
			  				dailyRange = (rangeHigh-(float)rangeLow)*100/(float)rangeLow;
			  				pivotRange = ((float)r1.get(i) - (float)pivot.get(i))*100/(float)pivot.get(i);
			  				Float targetProfit = 2f;
//			  				 rangeHigh > (float)pivot.get(i) && rangeHigh < (float)r1.get(i)
							if((float)high.get(i) > triggerPrice && (float)high.get(i-1) < triggerPrice  && bull.equals("Bull")){
			  					sql = "select max(high) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				dayshigh = executeCountQuery(con, sql);
		          				sql = "select close from "+name+"_5 where tradedate = concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				daysclose = executeCountQuery(con, sql);
		          				triggerPrice = (float)close.get(i);
		          				profitPerc = (Float.parseFloat(dayshigh) - triggerPrice)*100/triggerPrice;
		          				if(profitPerc > dailyRange/2){
		          					profitPerc = dailyRange/2;
		          				}else{
		          					profitPerc = (Float.parseFloat(daysclose) - triggerPrice)*100/triggerPrice;
		          				}
		          				profitPerc = (Float.parseFloat(daysclose) - triggerPrice)*100/triggerPrice;
		          				profitRupees = (Float.parseFloat(dayshigh) - triggerPrice)*100/triggerPrice;
			  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
		             					+ " values ('"+name+"', 'Bull', "+(float)r1.get(i)+", "+(profitPerc-0.05f)+", "+(profitRupees-0.05f)+", '"+date.get(i)+"', '"+date.get(i)+"', '"+dailyRange+"')";
		              			executeSqlQuery(con, sql);
		              			break;
			  				}
			  				triggerPrice = (rangeLow - (rangeLow*0.0f/100.00f));
//			  				&& dailyRange >2 && (int)volume.get(i)> (int)volume.get(i-1)*2
			  				pivotRange = ((float)pivot.get(i) - (float)s1.get(i))*100/(float)s1.get(i);
			  				if((float)low.get(i) < triggerPrice && (float)low.get(i-1) > triggerPrice  && bear.equals("Bear")){
			  					
			  					sql = "select min(low) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				dayslow = executeCountQuery(con, sql);
		          				triggerPrice = (float)close.get(i);
		          				sql = "select close from "+name+"_5 where tradedate = concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				daysclose = executeCountQuery(con, sql);
		          				profitPerc = (triggerPrice-Float.parseFloat(dayslow))*100/triggerPrice;
		          				if(profitPerc > dailyRange/2){
		          					profitPerc = dailyRange/2;
		          				}else{
		          					profitPerc = (triggerPrice-Float.parseFloat(daysclose))*100/triggerPrice;
		          				}
		          				profitPerc = (triggerPrice-Float.parseFloat(daysclose))*100/triggerPrice;
		          				profitRupees = (triggerPrice-Float.parseFloat(dayslow))*100/triggerPrice;
			  					
//			  					
			  					
			  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
		             					+ " values ('"+name+"', 'Bear', "+ (float)s1.get(i)+", "+(profitPerc-0.05f)+", "+(profitRupees-0.05f)+", '"+date.get(i)+"', '"+date.get(i)+"', '"+dailyRange+"')";
//			             			System.out.println(sql);
		              			executeSqlQuery(con, sql);
		              			break;
			  				}
			  		}
			  	  	
			  	if(rs!=null) rs.close();
		  	}
		  	catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  	catch(Exception e){
		    	  e.printStackTrace();
		    }
		  	finally{
		  		open.clear();high.clear();low.clear();close.clear();date.clear();
		  		rs.close();
		  	}
	   }
}
