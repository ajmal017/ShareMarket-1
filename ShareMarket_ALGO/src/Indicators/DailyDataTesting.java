package Indicators;

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

public class DailyDataTesting extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      DailyDataTesting pin = new DailyDataTesting();
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
	   String sql="Select tradedate, open, high,low,close, will_high, will_low from "+name+" where tradedate >='2016-02-01'";
	   float filter=1f, shadowWidth=3f;
	   String daysOpen, daysHigh, daysLow, daysClose;
	   ResultSet rs=null;float pinbar=0f, profitPerc=0f; String tableName = "williamsresults";
	   List open = new ArrayList<Float>();List high = new ArrayList<Float>();List low = new ArrayList<Float>();List close = new ArrayList<Float>();
	   List date = new ArrayList<>(); List will_high = new ArrayList<Float>(); List will_low = new ArrayList<Float>();
	   List close_I = new ArrayList<Float>(); List high_I = new ArrayList<Float>();List low_I = new ArrayList<Float>(); 
	   int interval=5; List date_I = new ArrayList<Float>(); String exitDate="";
	   rs = executeSelectSqlQuery(con, sql);
	   while (rs.next()){
		   open.add(rs.getFloat("open"));
		   high.add(rs.getFloat("high"));
		   low.add(rs.getFloat("low"));
		   close.add(rs.getFloat("close"));
		   will_high.add(rs.getFloat("will_high"));
		   will_low.add(rs.getFloat("will_low"));
		   date.add(rs.getString("tradedate"));
	   }
	   
	   int conditon=0, test=0, past=5;
	   
	   for (int i=0; i< date.size(); i++){
		   conditon=0;test=0;
		   date_I.clear();low_I.clear();high_I.clear();close_I.clear();
		   if(i<15) continue;
			   if((float)high.get(i) > (float)will_high.get(i-1) && (float)open.get(i) < (float)will_high.get(i-1)) {
				   for (int j=i-past; j<i; j++){
					   if((float)will_high.get(j) != (float)will_high.get(i-1)){
						   conditon=1;
					   }
				   }
				   if(conditon==0){
					   sql ="select tradedate, close,high,low from "+name+"_"+interval+" where  date(tradedate) = Date('"+date.get(i).toString()+"')";
//					   System.out.println(sql);
					   rs = executeSelectSqlQuery(con, sql);
					   while (rs.next()){
						   close_I.add(rs.getFloat("close"));
						   high_I.add(rs.getFloat("high"));
						   low_I.add(rs.getFloat("low"));
						   date_I.add(rs.getString("tradedate")); 
					   }
					   for (int j=0; j< close_I.size(); j++){
						   if((float)high_I.get(j) > (float)will_high.get(i-1)){
//							   profitPerc = ((float)close_I.get(j) -(float)will_high.get(i-1))*100/(float)will_high.get(i-1);
							   exitDate = date_I.get(j).toString();
							   if((float)close_I.get(j) > (float)will_high.get(i-1)){
//								   profitPerc = ((float)close.get(i) -(float)will_high.get(i-1))*100/(float)will_high.get(i-1);
								   profitPerc = ((float)close.get(i) -(float)close_I.get(j))*100/(float)close_I.get(j);
								   test=1;
							   }
							   break;
						   }
					   }
//					   profitPerc = ((float)close.get(i) -(float)will_high.get(i-1))*100/(float)will_high.get(i-1);
					   if(close_I.size() ==0) continue;
					   sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
            					+ " values ('"+name+"', 'bull', "+(float)will_high.get(i-1)+", "+(profitPerc-0.05f)+", "+profitPerc+", '"+date.get(i)+"', '"+exitDate+"', "+test+")";
             			executeSqlQuery(con, sql);
				   }
			   }
			   if((float)low.get(i) < (float)will_low.get(i-1) && (float)open.get(i) > (float)will_low.get(i-1)){
				   for (int j=i-past; j<i; j++){
					   if((float)will_low.get(j) != (float)will_low.get(i-1)){
						   conditon=1;
					   }
				   }
				   if(conditon==0){
					   sql ="select tradedate, close,high,low from "+name+"_"+interval+" where  date(tradedate) = Date('"+date.get(i).toString()+"')";
//					   System.out.println(sql);
					   rs = executeSelectSqlQuery(con, sql);
					   while (rs.next()){
						   close_I.add(rs.getFloat("close"));
						   high_I.add(rs.getFloat("high"));
						   low_I.add(rs.getFloat("low"));
						   date_I.add(rs.getString("tradedate")); 
					   }
					   for (int j=0; j< close_I.size(); j++){
						   if((float)low_I.get(j) < (float)will_low.get(i-1)){
//							   profitPerc = ((float)will_low.get(i-1) -(float)close_I.get(j))*100/(float)will_low.get(i-1);
							   exitDate = date_I.get(j).toString();
							   if((float)close_I.get(j) < (float)will_low.get(i-1)){
								   profitPerc = ((float)will_low.get(i-1) -(float)close.get(i))*100/(float)will_low.get(i-1);
								   profitPerc = ((float)close_I.get(j) - (float)close.get(i))*100/(float)close_I.get(j);
								   test=1;
							   }
							   break;
						   }
					   }
					   if(close_I.size() ==0) continue;
//					   profitPerc = ((float)will_low.get(i-1) -(float)close.get(i))*100/(float)will_low.get(i-1);
					   sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
            					+ " values ('"+name+"', 'bear', "+(float)will_low.get(i-1)+", "+(profitPerc-0.05f)+", "+profitPerc+", '"+date.get(i)+"', '"+date.get(i)+"', "+test+")";
             			executeSqlQuery(con, sql);
				   }
			   }
			   
	   }
   }
   
   public void getdatePinBar(java.sql.Connection con, String name, boolean updateSymbolsTableData, 
		   boolean updateAllData, boolean updateResultTable, boolean isIntraDayData, boolean insertAllDataToResult) throws SQLException{
	   String sql="Select tradedate, open, high,low,close from "+name+"";
	   float filter=1f, shadowWidth=3f;
	   String daysOpen, daysHigh, daysLow, daysClose;
	   ResultSet rs=null;float pinbar=0f, profitPerc=0f; String tableName = "williamsresults";
	   List open = new ArrayList<Float>();List high = new ArrayList<Float>();List low = new ArrayList<Float>();List close = new ArrayList<Float>();List date = new ArrayList<>();
	   rs = executeSelectSqlQuery(con, sql);
	   while (rs.next()){
		   open.add(rs.getFloat("open"));
		   high.add(rs.getFloat("high"));
		   low.add(rs.getFloat("low"));
		   close.add(rs.getFloat("close"));
		   date.add(rs.getString("tradedate"));
	   }
	   
	   for (int x=0; x< date.size()-2; x++){
		   if (x>=3 ){
			   float barWidth =0f;
			   
			   if(((float) high.get(x) - (float)open.get(x)) > ((float) open.get(x) - (float)close.get(x))*shadowWidth && (float)close.get(x) < (float)open.get(x) && (float)low.get(x+1) < (float)close.get(x) &&
					   ((float)close.get(x) - (float)low.get(x)) <= ((float) open.get(x) - (float)close.get(x))){
				   barWidth= ((float)open.get(x) - (float)close.get(x))*100/(float)open.get(x);
				   if((float) high.get(x-2) > (float) high.get(x-3) && (float) high.get(x-1) > (float) high.get(x-2) && (float) high.get(x) >(float) high.get(x-1) && barWidth > filter){
					   daysClose = executeCountQuery(con, "select close from "+name+" where date(tradedate)=Date('"+date.get(x).toString()+"') order by tradedate desc limit 1");
					   if((float)open.get(x+1) > (float)close.get(x))
//						   profitPerc = ((float)close.get(x) - (float)open.get(x+2))*100/(float)close.get(x);
						   profitPerc = ((float)close.get(x) - (float)close.get(x+1))*100/(float)close.get(x);
//						   profitPerc = ((float)close.get(x) - (float)low.get(x+1))*100/(float)close.get(x);
					   else if ((float)open.get(x+1) < (float)close.get(x))
//						   profitPerc = ((float)open.get(x+1) - (float)open.get(x+2))*100/(float)open.get(x+1);
						   profitPerc = ((float)open.get(x+1) - (float)close.get(x+1))*100/(float)open.get(x+1);
//						   profitPerc = ((float)open.get(x+1) - (float)low.get(x+1))*100/(float)open.get(x+1);
					   profitPerc = ((float)open.get(x+2) - (float)low.get(x+2))*100/(float)open.get(x+2);
	  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
            					+ " values ('"+name+"', 'bear', "+(float)open.get(x)+", "+(profitPerc-0.05f)+", "+profitPerc+", '"+date.get(x+1)+"', '"+date.get(x+1)+"', 1)";
             			executeSqlQuery(con, sql);
				   }
			   }
			   if(((float) open.get(x) - (float)low.get(x)) > ((float) close.get(x) - (float)open.get(x))*shadowWidth && (float)close.get(x) > (float)open.get(x) && (float)high.get(x+1) > (float)close.get(x) &&
					   ((float)high.get(x) - (float)close.get(x)) <= ((float) close.get(x) - (float)open.get(x))){
				   barWidth= ((float)close.get(x) - (float)open.get(x))*100/(float)open.get(x);
				   if((float) low.get(x-2) < (float) low.get(x-3) && (float) low.get(x-1) < (float) low.get(x-2) && (float) low.get(x) <(float) low.get(x-1) && barWidth> filter){
					   daysClose = executeCountQuery(con, "select close from "+name+" where date(tradedate)=Date('"+date.get(x).toString()+"') order by tradedate desc limit 1");
//					   profitPerc = ((float)close.get(x+1) - (float)close.get(x))*100/(float)close.get(x);
					   
					   if((float)open.get(x+1) < (float)close.get(x))
//						   profitPerc = ((float)open.get(x+2) - (float)close.get(x))*100/(float)close.get(x);
						   profitPerc = ((float)close.get(x+1) - (float)close.get(x))*100/(float)close.get(x);
//						   profitPerc = ((float)high.get(x+1) - (float)close.get(x))*100/(float)close.get(x);
					   else if ((float)open.get(x+1) > (float)close.get(x))
//						   profitPerc = ((float)open.get(x+2) - (float)open.get(x+1))*100/(float)open.get(x+1);
						   profitPerc = ((float)close.get(x+1) - (float)open.get(x+1))*100/(float)open.get(x+1);
//						   profitPerc = ((float)high.get(x+1) - (float)open.get(x+1))*100/(float)open.get(x+1);
					   profitPerc = ((float)high.get(x+2) - (float)open.get(x+2))*100/(float)open.get(x+2);
	  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
            					+ " values ('"+name+"', 'Bull', "+(float)open.get(x)+", "+(profitPerc-0.05f)+", "+profitPerc+", '"+date.get(x+1)+"', '"+date.get(x+1)+"', 1)";
             			executeSqlQuery(con, sql);
				   }
			   }
		   }
//			   UpdatePinResults(con, name, date.get(x).toString(), Float.parseFloat(high.get(x-1).toString()), Float.parseFloat(low.get(x-1).toString()));
	   }
   }
   
   public void UpdatePinResults(java.sql.Connection con, String name, String dailyDate, float prevHigh, float prevLow) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	List hist_dir = new ArrayList<String>();
	  	List macd = new ArrayList<Float>();
	  	List sig = new ArrayList<Float>();List adx = new ArrayList<Float>();List dm_plus = new ArrayList<Float>();List dm_minus = new ArrayList<Float>();
	  	List hist = new ArrayList<Float>();List cci = new ArrayList<Float>();
	  	String sql="";String interval="30";
	  	try {
	  		if(!interval.equals("")){
	  			sql="select * from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:10:00') and tradedate <= concat(Date('"+dailyDate+"'),' 15:00:00')";
	  		}else{
	  			sql="select * from "+name+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:10:00') and tradedate <= concat(Date('"+dailyDate+"'),' 15:00:00')";
	  		}
	  		
			rs = executeSelectSqlQuery(con, sql);
		 
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getFloat("tradedate"));
		  	}
		  	String tableName = "williamsresults";
		  	Float profitPerc=0.0f, profitRupees=0.0f;
		  	String tradeDate="";boolean check=false; String dayshigh="", dayslow="",daysclose=""; boolean gotentered=false;
	  		for (int i=0; i< date.size(); i++)
	  		{
	  			if(i>1){
	    	  		tradeDate = (String) date.get(i).toString();
	  				if((float)adx.get(i) < 10 && (float)dm_plus.get(i) > (float)dm_minus.get(i) && (float)adx.get(i) >(float)adx.get(i-1) )
	  				{
	  					sql = "select max(high) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
          				dayshigh = executeCountQuery(con, sql);
          				daysclose = executeCountQuery(con, "select close from "+name+" where tradedate=Date('"+date.get(i)+"')");
//	  					profitPerc = (Float.parseFloat(dayshigh) - triggerPrice)*100/triggerPrice;
//	  					profitPerc = (Float.parseFloat(daysclose) - triggerPrice)*100/triggerPrice;
	  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
             					+ " values ('"+name+"', 'Bull', "+(float)cci.get(i+1)+", "+(profitPerc-0.05f)+", "+profitRupees+", '"+date.get(i+1)+"', '"+date.get(i+1)+"', 1)";
              			executeSqlQuery(con, sql);gotentered=false;
              			break;
	  				}
	  				if((float)adx.get(i) < 10 && (float)dm_plus.get(i) < (float)dm_minus.get(i) && (float)adx.get(i) >(float)adx.get(i-1) )
	  				{
	  					sql = "select min(low) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
          				dayslow = executeCountQuery(con, sql);
          				daysclose = executeCountQuery(con, "select close from "+name+" where tradedate=Date('"+date.get(i)+"')");
	  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
             					+ " values ('"+name+"', 'Bear', "+(float)cci.get(i+1)+", "+(profitPerc-0.05f)+", "+profitRupees+", '"+date.get(i+1)+"', '"+date.get(i+1)+"', 1)";
              			executeSqlQuery(con, sql);gotentered=false;
              			break;
	  				}
	  			}
	  		}
		  	if(rs!=null) rs.close();
	  	}
	  	catch (SQLException e) {
			e.printStackTrace();
		}
	  	catch(Exception e){
	    	  e.printStackTrace();
	    }
   }
}

