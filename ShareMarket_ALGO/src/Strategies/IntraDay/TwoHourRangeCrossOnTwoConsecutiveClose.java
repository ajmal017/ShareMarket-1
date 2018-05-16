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

public class TwoHourRangeCrossOnTwoConsecutiveClose extends Connection{

	   public static void main(String[] args)  {
		      Test t = new Test();
		      TwoHourRangeCrossOnTwoConsecutiveClose range = new TwoHourRangeCrossOnTwoConsecutiveClose();
		      java.sql.Connection dbConnection = null;
		      boolean updateSymbolsTableData=true; boolean updateAllData=true;
		      try{
		    	  
		    	  Connection con = new Connection();
		    	  	dbConnection = con.getDbConnection();
		    	  	ResultSet rs=null;
		    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 100000 and s.name<>'Mindtree' order by volume desc ");
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
			   UpdatePinResultsRange(con, name, rs.getString("tradedate"), open,high,low,close,date,volume);
		   }
		   rs.close();
	   }
	   
	   public void UpdatePinResultsRange(java.sql.Connection con, String name,String dailyDate, List open, List high, List low, List close, List date, List volume) throws SQLException {
		   ResultSet rs=null;
		  	String sql="";String interval="5";
		  	try {
		  		if(!interval.equals("")){
		  			sql="select * from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:00:00') and tradedate <= concat(Date('"+dailyDate+"'),' 13:00:00')";
		  		}else{
		  			sql="select * from "+name+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:00:00') and tradedate <= concat(Date('"+dailyDate+"'),' 13:00:00')";
		  		}
		  		
				rs = executeSelectSqlQuery(con, sql);
				List<Float> ema1 = new ArrayList<>();List<Float> ema2 = new ArrayList<>(); 
				open.clear();high.clear();low.clear();close.clear();volume.clear();date.clear();
			  	while (rs.next()){
			  		open.add(rs.getFloat("open"));
			  		high.add(rs.getFloat("high"));
			  		low.add(rs.getFloat("low"));
			  		close.add(rs.getFloat("close"));
			  		ema1.add(rs.getFloat("ema1"));
			  		ema2.add(rs.getFloat("ema2"));
			  		date.add(rs.getString("tradedate"));
			  		volume.add(rs.getInt("volume"));
			  	}
			  	
			  	String tableName = "williamsresults";
			  	float filter=0f, filterPerc = 2f, rangeHigh1=0f, rangeLow1=0f, rangeHigh2=0f, rangeLow2=0f;
			  	Float profitPerc=0.0f, profitRupees=0.0f;
			  	float range=0f;int x=0; float p=1f, l=-2f, triggerPrice=0f, dailyRange=0f, stopLossPrice=0f, stopLoss=3f;
			  	String tradeDate="";String dayshigh="", dayslow="",daysclose=""; 
			  	String daysOpen="", daysHigh="", daysLow="", daysClose="", openInterest = "";
				 boolean bullMet = false, bearMet=false;
			  		for (int i=0; i< date.size()-1; i++){
			  			if((float)ema1.get(i) > (float)ema2.get(i)){
			  				bullMet = true;
			  			}else {
			  				bullMet = false;
			  				break;
			  			}
			  			
			  			if((float)ema1.get(i) < (float)ema2.get(i)){
			  				bearMet = true;
			  			}else {
			  				bearMet = false;
			  				break;
			  			}
			  		}
			  		if(bullMet ==true){
			  			bullMet = false;
		  				sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
             					+ " values ('"+name+"', 'Bull', "+profitPerc+", "+profitPerc+", "+profitPerc+", '"+date.get(0)+"', '"+date.get(0)+"', '"+dailyRange+"')";
              			executeSqlQuery(con, sql);
		  			}
		  			if(bearMet==true){
		  				bearMet = false;
		  				sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
             					+ " values ('"+name+"', 'Bear', "+profitPerc+", "+profitPerc+", "+profitPerc+", '"+date.get(0)+"', '"+date.get(0)+"', '"+dailyRange+"')";
              			executeSqlQuery(con, sql);
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
