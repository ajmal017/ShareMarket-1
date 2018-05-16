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

public class EnterOnCrossingOneOf_3_values extends Connection{

	   public static void main(String[] args)  {
		      Test t = new Test();
		      EnterOnCrossingOneOf_3_values range = new EnterOnCrossingOneOf_3_values();
		      java.sql.Connection dbConnection = null;
		      boolean updateSymbolsTableData=true; boolean updateAllData=true;
		      try{
		    	  
		    	  Connection con = new Connection();
		    	  	dbConnection = con.getDbConnection();
		    	  	ResultSet rs=null;
		    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 10000000 and s.name<>'Mindtree' order by volume desc ");
		    	  	String name="";
		    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
		    	  	boolean updateResultTable=false;boolean isIntraDayData=false;
		    	  	boolean insertAllDataToResult = false;
		    	  	String iter="1d";
		    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
		    	  	while (rs.next()){
		    	  		name= rs.getString("s.name");
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
		   String sql="Select tradedate, (high-low) as dailyRange from "+name+" where tradedate>'2016-02-01'";
		   System.out.println(sql);
		   ResultSet rs=null;
		   rs = executeSelectSqlQuery(con, sql);

		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	List date = new ArrayList<String>();
		  	List volume = new ArrayList<Integer>();
		  	List range = new ArrayList<Float>();
		   while (rs.next()){
			   date.add(rs.getString("tradedate")); 
			   range.add(rs.getString("dailyRange")); 
		   }
		   for (int i=1; i< date.size(); i++){
			   UpdatePinResultsRange(con, name, date.get(i).toString(),Float.parseFloat(range.get(i-1).toString()));
		   }
		   
		   rs.close();
	   }
	   
	   public void UpdatePinResultsRange(java.sql.Connection con, String name,String dailyDate, float range) throws SQLException {
		   ResultSet rs=null;
		  	String sql="";String interval="5";
		  	try {
			  	String tableName = "williamsresults";
			  	String tradeDate="";String dayshigh="", dayslow="",daysclose=""; 
			  	float rangeHigh1 =0f, rangeLow1=0f;float rangeHigh2 =0f, rangeLow2=0f;
			  	float triggerRange = range*.433f;float range2 = range*.766f;float range3 = range*1.355f;
			  	triggerRange = range3;
			  	float profit=0f;
			 sql = "select coalesce(max(high),0) as rangehigh from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:10:00') "
			 		+ "and tradedate <= concat(Date('"+dailyDate+"'),' 10:00:00')";
			 rangeHigh1 = Float.parseFloat(executeCountQuery(con, sql));
			 sql = "select coalesce(min(low),0) as rangelow from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:10:00') "
			 		+ "and tradedate <= concat(Date('"+dailyDate+"'),' 10:00:00')";
			 rangeLow1 = Float.parseFloat(executeCountQuery(con, sql));
			 
			 sql = "select coalesce(max(high),0) as rangehigh from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 10:00:00') "
				 		+ "and tradedate <= concat(Date('"+dailyDate+"'),' 15:15:00')";
			 rangeHigh2 = Float.parseFloat(executeCountQuery(con, sql));
			 sql = "select coalesce(min(low),0) as rangelow from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 10:00:00') "
			 		+ "and tradedate <= concat(Date('"+dailyDate+"'),' 15:15:00')";
			 rangeLow2 = Float.parseFloat(executeCountQuery(con, sql));
				 
			 if(rangeHigh2 > (rangeLow1 + triggerRange) && rangeHigh1 < (rangeLow1 + triggerRange)){
				 profit = (rangeHigh2 - (rangeLow1 + triggerRange))*100/(rangeLow1 + triggerRange);
				 sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
      					+ " values ('"+name+"', 'Bull', "+(rangeLow1 + triggerRange)+", "+profit+", "+(rangeLow1 + triggerRange)+", '"+dailyDate+"', "
      							+ "'"+dailyDate+"', '1')";
       				executeSqlQuery(con, sql);
			 }
			 
			 if(rangeLow2 < (rangeHigh1 - triggerRange) && rangeLow1 > (rangeHigh1 - triggerRange)){
				 profit = ((rangeHigh1 - triggerRange) - rangeLow2)*100/(rangeHigh1 - triggerRange);
				 sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
      					+ " values ('"+name+"', 'Bear', "+(rangeHigh1 - triggerRange)+", "+profit+", "+(rangeHigh1 - triggerRange)+", '"+dailyDate+"', "
      							+ "'"+dailyDate+"', '1')";
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
		  		if(rs!=null)
		  		rs.close();
		  	}
	   }
}
