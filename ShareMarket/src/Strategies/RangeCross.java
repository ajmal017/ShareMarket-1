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

public class RangeCross extends Connection{

	   public static void main(String[] args)  {
		      Test t = new Test();
		      RangeCross range = new RangeCross();
		      java.sql.Connection dbConnection = null;
		      boolean updateSymbolsTableData=true; boolean updateAllData=true;
		      try{
		    	  
		    	  Connection con = new Connection();
		    	  	dbConnection = con.getDbConnection();
		    	  	ResultSet rs=null;
		    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 10000 and s.name<>'Mindtree' and s.name='jindalstel'  order by volume desc ");
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
		   String sql="Select tradedate from "+name+"";
		   ResultSet rs=null;
		   rs = executeSelectSqlQuery(con, sql);

		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	List date = new ArrayList<String>();
		  	List volume = new ArrayList<Integer>();
		   while (rs.next()){
			   UpdatePinResultsRange(con, name, rs.getString("tradedate"), open,high,low,close,date,volume);
		   }
		   rs.close();
	   }
	   
	   public void UpdatePinResultsRange(java.sql.Connection con, String name,String dailyDate, List open, List high, List low, List close, List date, List volume) throws SQLException {
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
			  	}
			  	
			  	String tableName = "williamsresults";
			  	float filter=0f, filterPerc = 2f, rangeHigh=0f, rangeLow=0f;
			  	Float profitPerc=0.0f, profitRupees=0.0f;
			  	float range=0f;int x=0; float p=1f, l=-2f, triggerPrice=0f, dailyRange=0f, stopLossPrice=0f, stopLoss=3f;
			  	String tradeDate="";String dayshigh="", dayslow="",daysclose=""; 
			  	String daysOpen="";
			  	sql = "select coalesce(max(high),0) as rangehigh, coalesce(min(low),0) as rangelow from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:10:00') and tradedate <= concat(Date('"+dailyDate+"'),' 09:40:00')";
				 rs = executeSelectSqlQuery(con, sql);
				 while (rs.next()){
					 rangeHigh = Float.parseFloat(rs.getString("rangehigh"));
					rangeLow = Float.parseFloat(rs.getString("rangelow"));
				 }
				 daysOpen = executeCountQuery(con, "select open from "+name+" where tradedate=Date('"+dailyDate+"')");
			  		for (int i=2; i< date.size()-1; i++){
			  			if(rangeHigh==0 || rangeLow==0) break;
			  				tradeDate = (String) date.get(i).toString();
			  				triggerPrice = (rangeHigh + (rangeHigh*0.0f/100.00f));
			  				dailyRange = (rangeHigh-(float)rangeLow)*100/(float)rangeLow;
			  				Float targetProfit = 2f;
							if((float)high.get(i) > triggerPrice && (float)high.get(i-1) < triggerPrice && (float)triggerPrice > (float)pivot.get(i) && (float)triggerPrice < (float)r1.get(i)){
			  					sql = "select max(high) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				dayshigh = executeCountQuery(con, sql);
		          				sql = "select close from "+name+"_15 where tradedate = concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				daysclose = executeCountQuery(con, sql);
		          				triggerPrice = (float)close.get(i);
		          				profitPerc = (Float.parseFloat(daysclose) - triggerPrice)*100/triggerPrice;
//		          				profitPerc = (Float.parseFloat(dayshigh) - triggerPrice)*100/triggerPrice;
			  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
		             					+ " values ('"+name+"', 'Bull', "+(float)triggerPrice+", "+(profitPerc-0.05f)+", "+Float.parseFloat(daysclose)+", '"+date.get(i)+"', '"+date.get(i+1)+"', '"+dailyRange+"')";
		              			executeSqlQuery(con, sql);
		              			break;
			  				}
			  				triggerPrice = (rangeLow - (rangeLow*0.0f/100.00f));
//			  				&& dailyRange >2 && (int)volume.get(i)> (int)volume.get(i-1)*2
			  				if((float)low.get(i) < triggerPrice && (float)low.get(i-1) > triggerPrice && (float)triggerPrice < (float)pivot.get(i) && (float)triggerPrice > (float)s1.get(i)){
			  					
			  					sql = "select min(low) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				dayslow = executeCountQuery(con, sql);
		          				triggerPrice = (float)close.get(i);
		          				sql = "select close from "+name+"_15 where tradedate = concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				daysclose = executeCountQuery(con, sql);
			  					profitPerc = (triggerPrice-Float.parseFloat(daysclose))*100/triggerPrice;
//			  					profitPerc = (triggerPrice-Float.parseFloat(dayslow))*100/triggerPrice;
			  					
			  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
		             					+ " values ('"+name+"', 'Bear', "+(float)triggerPrice+", "+(profitPerc-0.05f)+", "+Float.parseFloat(daysclose)+", '"+date.get(i)+"', '"+date.get(i+1)+"', '"+dailyRange+"')";
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