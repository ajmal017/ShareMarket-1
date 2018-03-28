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

public class FirstMinGapEntry extends Connection{

	   public static void main(String[] args)  {
		      Test t = new Test();
		      FirstMinGapEntry range = new FirstMinGapEntry();
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
		   String sql="Select * from "+name+" where tradedate>'2016-02-01'";
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
		   float gap=3f;
		   for(int i=1; i< date.size(); i++){
			   if(((float)open.get(i)-(float)high.get(i-1))*100/(float)high.get(i-1) >gap){
				   UpdatePinResultsRange2(con, name, date.get(i).toString(), (float)high.get(i), (float)low.get(i), "upper");
			   }
			   if(((float)low.get(i-1)-(float)open.get(i))*100/(float)low.get(i-1) >gap){
				   UpdatePinResultsRange2(con, name, date.get(i).toString(), (float)high.get(i), (float)low.get(i), "lower");
			   }
		   }
		   rs.close();
	   }
	   
	   
	   public void UpdatePinResultsRange2(java.sql.Connection con, String name,String dailyDate, float high, float low,String gapDir){
		   ResultSet rs=null;
		  	String sql="";String interval="5";
		  	List<Float> ema2 = new ArrayList<>();
		  	try {
		  		
			  	float highDay=0f, lowday=100000f;
			  	String tableName = "williamsresults";int count=0;
			  	float rangeLow, rangeHigh;
				 float profitBullPerc=0f;float range=0f, profitBearPerc=0f;
				 float entry=0f;
//				 sql = "select high from "+name+"_"+interval+" where date(tradedate) = date('"+dailyDate+"') limit 1";
//				 rangeHigh = Float.parseFloat(executeCountQuery(con, sql));
//				 sql = "select low from "+name+"_"+interval+" where date(tradedate) = date('"+dailyDate+"') limit 1";
//				 rangeLow = Float.parseFloat(executeCountQuery(con, sql));
				 if(gapDir.equals("lower")){
					 sql = "select case when(open>high-(high-low)/2 and close>high-(high-low)/2) then 'GoForBull' else '' end from "+name+"_"+interval+" where "+
							 "date(tradedate) = date('"+dailyDate+"') limit 1";
					 if(executeCountQuery(con, sql).equals("GoForBull")){
						 entry = Float.parseFloat(executeCountQuery(con, "select close from "+name+"_"+interval+" where date(tradedate) = date('"+dailyDate+"') limit 1"));
						 profitBullPerc = (high - entry)*100/entry;
			  				sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
		         					+ " values ('"+name+"', 'Bull', 0, 0, "+profitBullPerc+", '"+dailyDate+"', '"+dailyDate+"', '1')";
		          			executeSqlQuery(con, sql);
					 }
				 }
				 
				 if(gapDir.equals("upper")){
					 sql = "select case when(open<low+(high-low)/2 and close<low+(high-low)/2) then 'GoForBear' else '' end from "+name+"_"+interval+" where "+
							 "date(tradedate) = date('"+dailyDate+"') limit 1";
					 if(executeCountQuery(con, sql).equals("GoForBear")){
						 entry = Float.parseFloat(executeCountQuery(con, "select close from "+name+"_"+interval+" where date(tradedate) = date('"+dailyDate+"') limit 1"));
						 profitBearPerc = (entry-low)*100/entry;
			  				sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
		         					+ " values ('"+name+"', 'Bear', 0, 0, "+profitBearPerc+", '"+dailyDate+"', '"+dailyDate+"', '1')";
		          			executeSqlQuery(con, sql);
					 }
				 }
				 
			  	/*if(high > rangeHigh && gapDir.equals("lower") && (rangeHigh-rangeLow)*100/rangeLow < 1){
			  		profitBullPerc = (high - rangeHigh)*100/rangeHigh;
	  				sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
         					+ " values ('"+name+"', 'Bull', 0, 0, "+profitBullPerc+", '"+dailyDate+"', '"+dailyDate+"', '1')";
          			executeSqlQuery(con, sql);
			  	}
			  	if(low<rangeLow && gapDir.equals("upper") && (rangeHigh-rangeLow)*100/rangeLow < 1){
			  		profitBearPerc = (rangeLow - low)*100/rangeLow;
	  				sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
         					+ " values ('"+name+"', 'Bear', 0, 0, "+profitBearPerc+", '"+dailyDate+"', '"+dailyDate+"', '1')";
          			executeSqlQuery(con, sql);
			  	}*/
			  	  	
			  	if(rs!=null) rs.close();
		  	}
		  	catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  	catch(Exception e){
		    	  e.printStackTrace();
		    }
	   }
}
