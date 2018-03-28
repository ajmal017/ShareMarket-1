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

public class CCI_MULTI extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      CCI_MULTI pin = new CCI_MULTI();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 100000000 and s.name<>'Mindtree'  order by volume desc");
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
	    	  		pin.getdate(dbConnection, name, updateSymbolsTableData, updateAllData, updateResultTable, isIntraDayData, insertAllDataToResult);
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
   
   public void getdate(java.sql.Connection con, String name, boolean updateSymbolsTableData, 
		   boolean updateAllData, boolean updateResultTable, boolean isIntraDayData, boolean insertAllDataToResult) throws SQLException{
	   String sql="Select tradedate,cci, open, high,low,close, will_high, will_low from "+name+" where tradedate >='2016-02-01'";
	   float filter=1f, shadowWidth=3f, daysHigh=0f, daysLow=0f;
	   String daysOpen, daysClose;
	   ResultSet rs=null;float pinbar=0f, profitPerc=0f; String tableName = "williamsresults";
	   List open = new ArrayList<Float>();List high = new ArrayList<Float>();List low = new ArrayList<Float>();List close = new ArrayList<Float>();
	   List date = new ArrayList<>(); List will_high = new ArrayList<Float>(); List will_low = new ArrayList<Float>(); List cci = new ArrayList<Float>();
	   List close_I = new ArrayList<Float>(); List high_I = new ArrayList<Float>();List low_I = new ArrayList<Float>();List cci_I = new ArrayList<Float>(); List open_I = new ArrayList<Float>();  
	   int interval=5; List date_I = new ArrayList<Float>(); String exitDate="";
	   rs = executeSelectSqlQuery(con, sql);
	   while (rs.next()){
		   open.add(rs.getFloat("open"));
		   high.add(rs.getFloat("high"));
		   low.add(rs.getFloat("low"));
		   close.add(rs.getFloat("close"));
		   will_high.add(rs.getFloat("will_high"));
		   will_low.add(rs.getFloat("will_low"));
		   cci.add(rs.getFloat("cci"));
		   date.add(rs.getString("tradedate"));
	   }
	   float targetProfit=1f;
	   int conditon=0, test=0, past=5;
	   float range, target=5f;
	   for (int i=0; i< date.size(); i++){
		   conditon=0;test=0;
		   date_I.clear();low_I.clear();high_I.clear();close_I.clear();cci_I.clear();
		   if(i<1) continue;
		   range = ((float)close.get(i)-(float)open.get(i))*100/(float)open.get(i);
			   if((float)cci.get(i) > 100 && (float)cci.get(i-1) < 100) {
					   sql ="select tradedate, close,high,low, open, cci from "+name+"_"+interval+" where  date(tradedate) = Date('"+date.get(i+1).toString()+"') order by tradedate";
//					   System.out.println(sql);
					   rs = executeSelectSqlQuery(con, sql);
					   while (rs.next()){
						   close_I.add(rs.getFloat("close"));
						   high_I.add(rs.getFloat("high"));
						   low_I.add(rs.getFloat("low"));
						   cci_I.add(rs.getFloat("cci"));
						   open_I.add(rs.getFloat("open"));
						   date_I.add(rs.getString("tradedate")); 
					   }
					   float trig=0f;String enteredTime="";
					   for (int x=1; x< date_I.size(); x++){
						   if( (float)cci_I.get(x-1) < -100 && (float)cci_I.get(x) > -100 ){
							   trig = (float)close_I.get(x);
							   enteredTime = date_I.get(x).toString();
							   sql = "select max(high) from "+name+"_"+interval+" where tradedate >'"+date_I.get(x)+"' and tradedate <= concat(Date('"+date_I.get(x)+"'),' 15:15:00')";
							   daysHigh = Float.parseFloat(executeCountQuery(con, sql));
							   break;
						   }
					   }
//					   profitPerc = ((float)high.get(i+1) -(float)open.get(i+1))*100/(float)open.get(i+1);
					   profitPerc = (daysHigh -(float)trig)*100/trig;
//					   profitPerc = ((float)high.get(i+1) -(float)high.get(i))*100/(float)high.get(i);
//					   if(profitPerc<targetProfit)
//						   profitPerc = ((float)close.get(i+1) -(float)trig)*100/trig;
//					   else profitPerc=targetProfit;
					   sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
            					+ " values ('"+name+"', 'bull', "+(float)(profitPerc-0.05f)+", "+(profitPerc-0.05f)+", "+profitPerc+", '"+enteredTime+"', '"+enteredTime+"', "+test+")";
             			if(!enteredTime.equals(""))
             				executeSqlQuery(con, sql);
			   }
			   range = ((float)open.get(i)-(float)close.get(i))*100/(float)open.get(i);
			   if((float)cci.get(i) < -100 && (float)cci.get(i-1) > -100 ) {
					   sql ="select tradedate, close,high,low, open, cci from "+name+"_"+interval+" where  date(tradedate) = Date('"+date.get(i+1).toString()+"') order by tradedate";
//					   System.out.println(sql);
					   rs = executeSelectSqlQuery(con, sql);
					   while (rs.next()){
						   close_I.add(rs.getFloat("close"));
						   high_I.add(rs.getFloat("high"));
						   low_I.add(rs.getFloat("low"));
						   cci_I.add(rs.getFloat("cci"));
						   open_I.add(rs.getFloat("open"));
						   date_I.add(rs.getString("tradedate")); 
					   }
					   float trig=0f;String enteredTime="";
					   for (int x=1; x< date_I.size()-1; x++){
						   if( (float)cci_I.get(x-1) > 100 && (float)cci_I.get(x) < 100 ){
							   trig = (float)close_I.get(x);
							   enteredTime = date_I.get(x).toString();
							   sql = "select min(low) from "+name+"_"+interval+" where tradedate >'"+date_I.get(x)+"' and tradedate <= concat(Date('"+date_I.get(x)+"'),' 15:15:00')";
							   daysLow = Float.parseFloat(executeCountQuery(con, sql));
							   break;
						   }
					   }
//					   profitPerc = ((float)open.get(i+1) -(float)low.get(i+1))*100/(float)open.get(i+1);
					   profitPerc = (trig -daysLow)*100/trig;
//					   profitPerc = ((float)low.get(i) -(float)low.get(i+1))*100/(float)low.get(i);
//					   if(profitPerc<targetProfit)
//						   profitPerc = (trig -(float)close.get(i+1))*100/trig;
//					   else profitPerc=targetProfit;
					   sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
            					+ " values ('"+name+"', 'bear', "+(float)(profitPerc-0.05f)+", "+(profitPerc-0.05f)+", "+profitPerc+", '"+enteredTime+"', '"+enteredTime+"', "+test+")";
					   if(!enteredTime.equals(""))
						   executeSqlQuery(con, sql);
			   }
	   }
   }
}
