package Strategies.Adx;

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

public class AdxAtExtreme extends Connection{

	   public static void main(String[] args)  {
		      Test t = new Test();
		      AdxAtExtreme range = new AdxAtExtreme();
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
		   List pivot = new ArrayList<Float>();
		  	List r1 = new ArrayList<Float>();List s1 = new ArrayList<Float>();
		  	List r2 = new ArrayList<Float>();List s2 = new ArrayList<Float>();
		  	List macd = new ArrayList<Float>();List signal = new ArrayList<Float>();
		  	List adx = new ArrayList<Float>();
		  	List di_plus = new ArrayList<Float>();
		  	List di_minus = new ArrayList<Float>();
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
			  		macd.add(rs.getFloat("macd"));
			  		signal.add(rs.getFloat("sig"));
			  		adx.add(rs.getFloat("adx"));
			  		di_plus.add(rs.getFloat("DI_PLUS_AVERAGE"));
			  		di_minus.add(rs.getFloat("DI_MINUS_AVERAGE"));
			  	}
			  	
			  	String tableName = "williamsresults";
			  	float filter=0f, filterPerc = 2f, rangeHigh=0f, rangeLow=0f;
			  	Float profitPerc=0.0f, profitRupees=0.0f;
			  	float range=0f;int x=0; float p=1f, l=-2f, triggerPrice=0f, dailyRange=0f, stopLossPrice=0f, stopLoss=3f;
			  	String tradeDate="";String daysHigh="", daysLow="",daysClose="", exitPrice=""; 
			  	String daysOpen="";int maxVolume=0;
			  	float percProfitAtHighPrice, percProfitAtClosePrice;
				 
			  	for(int i=1; i< date.size()-1; i++){
					if((float)adx.get(i) > 50 && (float)adx.get(i+1) < (float)adx.get(i) && 
							(float)di_minus.get(i) > (float)di_plus.get(i) && (float)close.get(i+1) > (float)open.get(i+1) && 
							((float)high.get(i)-(float)open.get(0))*100/(float)open.get(0) > 3){
						triggerPrice=(float)close.get(i+1); 
						sql = "select max(high) from "+name+"_5 where tradedate >'"+date.get(i+1)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
						daysHigh = executeCountQuery(con, sql);
          				sql = "select close from "+name+"_5 where tradedate = concat(Date('"+date.get(i)+"'),' 15:15:00')";
          				daysClose = executeCountQuery(con, sql);
						percProfitAtHighPrice = (Float.parseFloat(daysHigh)-triggerPrice)*100/triggerPrice;
						percProfitAtClosePrice = (Float.parseFloat(daysClose)-triggerPrice)*100/triggerPrice;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "+percProfitAtHighPrice+", '"+date.get(i+1)+"')";
						executeSqlQuery(con, sql);
						break;
					}
					if((float)adx.get(i) > 50 && (float)adx.get(i+1) < (float)adx.get(i) && 
							(float)di_minus.get(i) < (float)di_plus.get(i) && (float)close.get(i+1) < (float)open.get(i+1) && 
							((float)open.get(0)-(float)low.get(i))*100/(float)open.get(0) > 3){
						triggerPrice=(float)close.get(i+1); 
						sql = "select min(low) from "+name+"_5 where tradedate >'"+date.get(i+1)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
						daysLow = executeCountQuery(con, sql);
          				sql = "select close from "+name+"_5 where tradedate = concat(Date('"+date.get(i)+"'),' 15:15:00')";
          				daysClose = executeCountQuery(con, sql);
						percProfitAtHighPrice = (triggerPrice-Float.parseFloat(daysLow))*100/triggerPrice;
						percProfitAtClosePrice = (triggerPrice-Float.parseFloat(daysClose))*100/triggerPrice;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bear', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "+percProfitAtHighPrice+", '"+date.get(i+1)+"')";
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
