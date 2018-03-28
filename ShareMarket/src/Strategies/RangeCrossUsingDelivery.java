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

public class RangeCrossUsingDelivery extends Connection{

	   public static void main(String[] args)  {
		      Test t = new Test();
		      RangeCrossUsingDelivery range = new RangeCrossUsingDelivery();
		      java.sql.Connection dbConnection = null;
		      boolean updateSymbolsTableData=true; boolean updateAllData=true;
		      try{
		    	  
		    	  Connection con = new Connection();
		    	  	dbConnection = con.getDbConnection();
		    	  	ResultSet rs=null;
		    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 100000 and s.name<>'Mindtree'  order by volume desc ");
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
		   String sql="Select a.tradedate, perc, atr from "+name+"_dp a, "+name+" b where  a.tradedate=b.tradedate;";
		   ResultSet rs=null;
		   rs = executeSelectSqlQuery(con, sql);

		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	List date = new ArrayList<String>();
		  	List volume = new ArrayList<Integer>();
		   while (rs.next()){
			   UpdatePinResultsRange(con, name, rs.getString("a.tradedate"), rs.getFloat("perc"), rs.getFloat("atr"), open,high,low,close,date,volume);
		   }
		   open.clear();high.clear();low.clear();close.clear();volume.clear();date.clear();
		   rs.close();
	   }
	   
	   public void UpdatePinResultsRange(java.sql.Connection con, String name,String dailyDate, float perc, float atr, List open, List high, List low, List close, List date, List volume) throws SQLException {
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
				open.clear();high.clear();low.clear();close.clear();volume.clear();date.clear();volume.clear();pivot.clear();r1.clear();s1.clear();
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
			  	}
			  	
			  	String tableName = "williamsresults";
			  	float filter=0f, filterPerc = 2f, rangeHigh=0f, rangeLow=0f;
				float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
			  	float range=0f;int x=0; float p=1f, l=-2f, triggerPrice=0f, dailyRangePerc=0f, stopLossPrice=0f, stopLoss=3f;
			  	String tradeDate="";String dayshigh="", dayslow="",daysclose=""; 
			  	String daysOpen="";
			  	
				 sql = "select coalesce(max(high+0),0) as rangehigh from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:10:00') and tradedate <= concat(Date('"+dailyDate+"'),' 09:40:00')";
				 rangeHigh = Float.parseFloat(executeCountQuery(con,sql));
				 sql = "select coalesce(min(low+0),0) as rangelow from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:10:00') and tradedate <= concat(Date('"+dailyDate+"'),' 09:40:00')";
				 rangeLow = Float.parseFloat(executeCountQuery(con,sql));
				 float dailyRange=0f;
				 daysOpen = executeCountQuery(con, "select open from "+name+" where tradedate=Date('"+dailyDate+"')");
			  		for (int i=2; i< date.size()-1; i++){
						float diff = Math.abs((rangeHigh-rangeLow));
			  			if(rangeHigh==0 || rangeLow==0) break;
			  				tradeDate = (String) date.get(i).toString();
			  				triggerPrice = (rangeHigh + (rangeHigh*0.2f/100.00f));
			  				dailyRangePerc = (rangeHigh-(float)rangeLow)*100/(float)rangeLow;
			  				dailyRange = (rangeHigh-(float)rangeLow);
			  				Float targetProfit = 2f;
//							&& (float)triggerPrice > (float)pivot.get(i) && (float)triggerPrice < (float)r1.get(i)
							if((float)high.get(i) > triggerPrice && (float)high.get(i-1) < triggerPrice && (float)triggerPrice > (float)pivot.get(i) && (float)triggerPrice < (float)r1.get(i)
									 ){
			  					sql = "select max(high) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				dayshigh = executeCountQuery(con, sql);
		          				sql = "select close from "+name+"_15 where tradedate = concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				daysclose = executeCountQuery(con, sql);
		          				triggerPrice = (float)close.get(i);
		          				percProfitAtClosePrice = (Float.parseFloat(daysclose) - triggerPrice)*100/triggerPrice;
		          				percProfitAtHighPrice = (Float.parseFloat(dayshigh) - triggerPrice)*100/triggerPrice;
			  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
		             					+ " values ('"+name+"', 'Bull', "+triggerPrice+", "+(percProfitAtClosePrice-0.05f)+", "+(percProfitAtHighPrice-0.05f)+", '"+date.get(i)+"', '"+date.get(i+1)+"', '"+dailyRangePerc+"')";
		              			executeSqlQuery(con, sql);
		              			break;
			  				}
			  				triggerPrice = (rangeLow - (rangeLow*0.2f/100.00f));
//			  				&& dailyRange >2 && (int)volume.get(i)> (int)volume.get(i-1)*2
			  				if((float)low.get(i) < triggerPrice && (float)low.get(i-1) > triggerPrice && (float)triggerPrice < (float)pivot.get(i) && (float)triggerPrice > (float)s1.get(i)
			  						){
			  					
			  					sql = "select min(low) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				dayslow = executeCountQuery(con, sql);
		          				triggerPrice = (float)close.get(i);
		          				sql = "select close from "+name+"_15 where tradedate = concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				daysclose = executeCountQuery(con, sql);
			  					
			  					percProfitAtClosePrice = (triggerPrice - Float.parseFloat(daysclose))*100/triggerPrice;
		          				percProfitAtLowPrice = (triggerPrice - Float.parseFloat(dayslow))*100/triggerPrice;
			  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
		             					+ " values ('"+name+"', 'Bear', "+triggerPrice+", "+(percProfitAtClosePrice-0.05f)+", "+(percProfitAtLowPrice-0.05f)+", '"+date.get(i)+"', '"+date.get(i+1)+"', '"+dailyRangePerc+"')";
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
		  		open.clear();high.clear();low.clear();close.clear();volume.clear();date.clear();volume.clear();pivot.clear();r1.clear();s1.clear();
		  		rs.close();
		  	}
	   }
}
