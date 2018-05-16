package Strategies.Range;

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

public class RangeCross_1St30Min_Pin_HangingMan extends Connection{

	   public static void main(String[] args)  {
		      Test t = new Test();
		      RangeCross_1St30Min_Pin_HangingMan range = new RangeCross_1St30Min_Pin_HangingMan();
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
		  	String sql="";String interval="30";
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
			  	Float profitPerc=0.0f, profitRupees=0.0f;
			  	float range=0f;int x=0; float p=1f, l=-2f, triggerPrice=0f;
			  	String tradeDate="";String daysLow="", daysHigh="", dayslow="",daysclose=""; 
			  	String daysOpen="";
			  	
				 float pivotRange=0f, prevRange, upperRange, lowerRange;
				 daysOpen = executeCountQuery(con, "select open from "+name+" where tradedate=Date('"+dailyDate+"')");
			  		for (int i=1; i< date.size()-1; i++){
			  				tradeDate = (String) date.get(i).toString();
			  				range = ((float)close.get(i)-(float)open.get(i))*100/(float)open.get(i);
			  				prevRange = ((float)close.get(i-1)-(float)open.get(i-1))*100/(float)open.get(i-1);
			  				upperRange = ((float)high.get(i)-(float)close.get(i))*100/(float)close.get(i);
			  				lowerRange = ((float)open.get(i)-(float)low.get(i))*100/(float)low.get(i);
							if(range>0.5 && upperRange >range*1.5 && (float)close.get(i) > (float)open.get(i) && lowerRange<range && prevRange>.5){
			  					sql = "select min(low) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				daysLow = executeCountQuery(con, sql);
		          				sql = "select close from "+name+"_15 where tradedate = concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				daysclose = executeCountQuery(con, sql);
		          				triggerPrice = (float)close.get(i);
		          				
		          				
		          				profitPerc = (triggerPrice-Float.parseFloat(daysclose))*100/triggerPrice;
		          				profitRupees = (triggerPrice-Float.parseFloat(daysLow))*100/triggerPrice;
			  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
		             					+ " values ('"+name+"', 'Bear', "+(float)r1.get(i)+", "+(profitPerc-0.05f)+", "+(profitRupees-0.05f)+", '"+date.get(i)+"', '"+date.get(i)+"', '1')";
		              			executeSqlQuery(con, sql);
		              			break;
			  				}
							range = ((float)open.get(i)-(float)close.get(i))*100/(float)open.get(i);
			  				prevRange = ((float)open.get(i-1)-(float)close.get(i-1))*100/(float)open.get(i-1);;
			  				upperRange = ((float)high.get(i)-(float)open.get(i))*100/(float)open.get(i);
			  				lowerRange = ((float)close.get(i)-(float)low.get(i))*100/(float)low.get(i);
							if(range>0.5 && lowerRange >range*1.5 && (float)close.get(i) < (float)open.get(i) && upperRange<range && prevRange>.5){
			  					sql = "select max(high) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				daysHigh = executeCountQuery(con, sql);
		          				sql = "select close from "+name+"_15 where tradedate = concat(Date('"+date.get(i)+"'),' 15:15:00')";
		          				daysclose = executeCountQuery(con, sql);
		          				triggerPrice = (float)close.get(i);
		          				
		          				profitPerc = (Float.parseFloat(daysclose) - triggerPrice)*100/triggerPrice;
		          				profitRupees = (Float.parseFloat(daysHigh)-triggerPrice)*100/triggerPrice;
		          				
			  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
		             					+ " values ('"+name+"', 'Bull', "+(float)r1.get(i)+", "+(profitPerc-0.05f)+", "+(profitRupees-0.05f)+", '"+date.get(i)+"', '"+date.get(i)+"', '1')";
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
