package Strategies.IntraDay;

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

public class Stagnant extends Connection{

	   public static void main(String[] args)  {
		      Test t = new Test();
		      Stagnant range = new Stagnant();
		      java.sql.Connection dbConnection = null;
		      boolean updateSymbolsTableData=true; boolean updateAllData=true;
		      try{
		    	  
		    	  Connection con = new Connection();
		    	  	dbConnection = con.getDbConnection();
		    	  	ResultSet rs=null;
		    	  	String sql = "SELECT s.name FROM symbols s where volume > 100000000";
//		    	  	sql = "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 100000 and s.name<>'Mindtree' order by volume desc ";
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
		   String sql="Select tradedate from "+name+" where tradedate>'2016-02-01'";
		   ResultSet rs=null;
		   rs = executeSelectSqlQuery(con, sql);

		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	List date = new ArrayList<String>();
		  	List volume = new ArrayList<Integer>();
		   while (rs.next()){
			   UpdatePinResultsRange2(con, name, rs.getString("tradedate"), open,high,low,close,date,volume);
		   }
		   rs.close();
	   }
	   
	   
	   public void UpdatePinResultsRange2(java.sql.Connection con, String name,String dailyDate, List open, List high, List low, List close, List date, List volume) throws SQLException {
		   ResultSet rs=null;
		  	String sql="";String interval="1";
		  	List<Float> ema2 = new ArrayList<>();
		  	try {
		  		if(!interval.equals("")){
		  			sql="select * from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:00:00') and tradedate <= concat(Date('"+dailyDate+"'),' 15:00:00')";
		  		}else{
		  			sql="select * from "+name+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:00:00') and tradedate <= concat(Date('"+dailyDate+"'),' 15:00:00')";
		  		}
		  		
				rs = executeSelectSqlQuery(con, sql);
				open.clear();high.clear();low.clear();close.clear();volume.clear();date.clear();
			  	while (rs.next()){
			  		open.add(rs.getFloat("open"));
			  		high.add(rs.getFloat("high"));
			  		low.add(rs.getFloat("low"));
			  		close.add(rs.getFloat("close"));
			  		ema2.add(rs.getFloat("ema2"));
			  		date.add(rs.getString("tradedate"));
			  		volume.add(rs.getInt("volume"));
			  	}
			  	float highDay=0f, lowday=100000f;
			  	String tableName = "williamsresults";int count=0;
			  	float dayslow, daysHigh;
				 float profitBullPerc=0f;float range=0f, profitBearPerc=0f;
			  		for (int i=0; i< date.size()-1; i++){
			  			range = Math.abs((float)ema2.get(i) - (float)ema2.get(i+1))*100/(float)ema2.get(i);
			  			
			  			if(range < 0.004){
			  				count++;
			  			}else count=0;
			  			if(count > 100){
			  				count=0;
			  				sql = "select min(low) from "+name+"_"+interval+" where tradedate <=concat(Date('"+date.get(i)+"'),' 15:30:00') and tradedate > '"+date.get(i)+"'";
	          				dayslow = Float.parseFloat(executeCountQuery(con, sql));
	          				sql = "select max(high) from "+name+"_"+interval+" where tradedate <=concat(Date('"+date.get(i)+"'),' 15:30:00') and tradedate > '"+date.get(i)+"'";
	          				daysHigh = Float.parseFloat(executeCountQuery(con, sql));
	          				profitBullPerc = (daysHigh - (float)close.get(i))*100/(float)close.get(i);
	          				profitBearPerc = ((float)close.get(i)-dayslow)*100/(float)close.get(i);
			  				sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	             					+ " values ('"+name+"', 'Bull', 0, "+profitBearPerc+", "+profitBullPerc+", '"+date.get(i)+"', '"+date.get(i)+"', '1')";
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
