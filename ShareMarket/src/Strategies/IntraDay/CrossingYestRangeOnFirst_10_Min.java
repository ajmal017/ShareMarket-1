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

public class CrossingYestRangeOnFirst_10_Min extends Connection{

	java.sql.Connection dbConnection = null;
	public CrossingYestRangeOnFirst_10_Min() {
		Connection con = new Connection();
	  	dbConnection = con.getDbConnection();
	}
	   public static void main(String[] args)  {
		      Test t = new Test();
		      CrossingYestRangeOnFirst_10_Min range = new CrossingYestRangeOnFirst_10_Min();
		      
		      boolean updateSymbolsTableData=true; boolean updateAllData=true;
		      try{
		    	  
		    	    Connection con = new Connection();
		    	  	ResultSet rs=null;
		    	  	String sql = "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 100000 "
		    	  			+ "and s.name<>'Mindtree' and s.name<>'IOC' and s.name<>'GRANULES' order by volume desc ";
//		    	  	sql = "SELECT s.name FROM symbols s where volume > 10000000 order by volume desc ";
		    	  	rs = con.executeSelectSqlQuery(range.dbConnection, sql);
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
		    	  		range.getdate(range.dbConnection, name);
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
		      }
	   }
	   
	   public void getdate(java.sql.Connection con, String name) throws SQLException, InterruptedException{
		   String sql="Select * from "+name+" where tradedate> '2017-02-01'";
		   ResultSet rs=null;
		   rs = executeSelectSqlQuery(con, sql);

		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	List date = new ArrayList<String>();
		  	List volume = new ArrayList<Integer>();
		   while (rs.next()){
			   open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getString("tradedate"));
		   }
		   float gap=0.5f;
		   for(int i=1; i< date.size(); i++){
			   UpdatePinResultsRange(con, name, date.get(i).toString(), (float)high.get(i-1), (float)low.get(i-1), (float)close.get(i));
		   }
	   }
	   
	   
	   public void UpdatePinResultsRange(java.sql.Connection con, String name,String dailyDate, float prevHigh, float prevLow, float daysClose) throws SQLException {
		   ResultSet rs=null;
		  	String sql="";String interval="5";
		  	try {
		  		if(!interval.equals("")){
		  			sql="select * from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:00:00') and tradedate <= concat(Date('"+dailyDate+"'),' 15:00:00')";
		  		}else{
		  			sql="select * from "+name+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:00:00') and tradedate <= concat(Date('"+dailyDate+"'),' 15:00:00')";
		  		}
		  		
		  		List open = new ArrayList<Float>();
			  	List high = new ArrayList<Float>();
			  	List low = new ArrayList<Float>();
			  	List close = new ArrayList<Float>();
			  	List date = new ArrayList<String>();
			  	
				rs = executeSelectSqlQuery(con, sql);
				open.clear();high.clear();low.clear();close.clear();date.clear();
			  	while (rs.next()){
			  		open.add(rs.getFloat("open"));
			  		high.add(rs.getFloat("high"));
			  		low.add(rs.getFloat("low"));
			  		close.add(rs.getFloat("close"));
			  		date.add(rs.getString("tradedate"));
			  	}
			  	float highDay=0f, lowday=100000f;
			  	String tableName = "williamsresults";int count=0;
				 float profitPerc=0f;float range=0f;
			  		for (int i=0; i< date.size()-1; i++){
			  			float intraRange = ((float)high.get(0)-(float)low.get(0))*100/(float)low.get(0);
			  			if((float)open.get(0) < prevHigh && (float)close.get(0) > prevHigh){
			  				sql = "select coalesce(max(high),0) as rangehigh from "+name+"_"+interval+" where tradedate > '"+date.get(0)+"'"
							 		+ " and tradedate <= concat(Date('"+dailyDate+"'),' 15:15:00')";
			  				float rangeHigh = Float.parseFloat(executeCountQuery(con, sql));
			  				profitPerc = (rangeHigh-(float)open.get(i+1)) *100/(float)open.get(i+1);
			  				float profitOnCLose = (daysClose-(float)open.get(i+1)) *100/(float)open.get(i+1);
			  				sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	             					+ " values ('"+name+"', 'Bull', "+profitPerc+", "+profitOnCLose+", "+profitPerc+", '"+date.get(i)+"', '"+date.get(i)+"', '1')";
	              			executeSqlQuery(con, sql);
	              			break;
			  			}else break;
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
		  		rs.close();
		  	}
	   }
}
