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

public class PinBar extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      PinBar pin = new PinBar();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 100000000 and s.name<>'Mindtree'  order by volume desc ");
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
	   String sql="Select tradedate from "+name+"";
	   ResultSet rs=null;
	   rs = executeSelectSqlQuery(con, sql);
	   while (rs.next()){
//		   UpdatePinResultsADX(con, name, rs.getString("tradedate"), updateSymbolsTableData, updateAllData, updateResultTable, isIntraDayData, insertAllDataToResult);
		   UpdatePinResultsRange(con, name, rs.getString("tradedate"), updateSymbolsTableData, updateAllData, updateResultTable, isIntraDayData, insertAllDataToResult);
	   }
//	   UpdatePinResults(con, name, "2016-05-25 00:00:00", updateSymbolsTableData, updateAllData, updateResultTable, isIntraDayData, insertAllDataToResult);
   }
   
   public void UpdatePinResultsRange(java.sql.Connection con, String name,String dailyDate,  boolean updateSymbolsTableData, 
		   boolean updateAllData, boolean updateResultTable, boolean isIntraDayData, boolean insertAllDataToResult) {
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
	  	List atr = new ArrayList<Float>();
//	  	List volume = new ArrayList<String>();
	  	String sql="";String interval="5";
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
		  		date.add(rs.getString("tradedate"));
		  		macd.add(rs.getFloat("macd"));
		  		cci.add(rs.getFloat("cci"));
		  		adx.add(rs.getFloat("adx"));
		  		dm_plus.add(rs.getFloat("DM_PLUS_AVERAGE"));
		  		dm_minus.add(rs.getFloat("DM_MINUS_AVERAGE"));
    	  		sig.add(rs.getFloat("sig"));
    	  		hist.add(rs.getFloat("histogram"));
    	  		atr.add(rs.getFloat("atr"));
		  	}
		  	
		  	String tableName = "williamsresults";
		  	float filter=0f, filterPerc = 2f, rangeHigh=0f, rangeLow=0f;
		  	Float profitPerc=0.0f, profitRupees=0.0f;
		  	float range=0f;int x=0; float p=1f, l=-2f, triggerPrice=0f, dailyRange=0f, stopLossPrice=0f, stopLoss=3f;
		  	String tradeDate="";boolean check=false; String dayshigh="", dayslow="",daysclose=""; boolean gotentered=false;
		  	String daysOpen="";
		  	sql = "select max(high) as rangehigh, min(low) as rangelow from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:10:00') and tradedate <= concat(Date('"+dailyDate+"'),' 09:40:00')";
			 rs = executeSelectSqlQuery(con, sql);
			 while (rs.next()){
				 rangeHigh = Float.parseFloat(rs.getString("rangehigh"));
				rangeLow = Float.parseFloat(rs.getString("rangelow"));
			 }
			 daysOpen = executeCountQuery(con, "select open from "+name+" where tradedate=Date('"+dailyDate+"')");
		  	if(updateAllData == true){
		  		for (int i=0; i< date.size(); i++){
		  				if(isIntraDayData==true)
		    	  			tradeDate = (String) date.get(i).toString();
		    	  		else
		    	  			tradeDate = date.get(i)+"";
//		  				triggerPrice = (float)rangeHigh;
		  				triggerPrice = (rangeHigh + (rangeHigh*0.0f/100.00f));
		  				dailyRange = (rangeHigh-(float)rangeLow)*100/(float)rangeLow;
		  				Float targetProfit = 2f;
						if((float)high.get(i) > triggerPrice &&  (float)rangeLow > Float.parseFloat(daysOpen)){
		  					sql = "select max(high) from "+name+"_"+interval+" where tradedate >='"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
	          				dayshigh = executeCountQuery(con, sql);
//	          				triggerPrice = (float)close.get(i);
	          				sql = "select close from "+name+"_15 where tradedate = concat(Date('"+date.get(i)+"'),' 15:15:00')";
	          				daysclose = executeCountQuery(con, sql);
	          				
	          				profitPerc = (Float.parseFloat(daysclose) - triggerPrice)*100/triggerPrice;
		  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	             					+ " values ('"+name+"', 'Bull', "+(float)rangeHigh+", "+(profitPerc-0.05f)+", "+daysclose+", '"+date.get(i)+"', '"+date.get(i+1)+"', '"+dailyRange+"')";
//		             			System.out.println(sql);
	              			executeSqlQuery(con, sql);gotentered=false;
	              			break;
		  				}
		  				triggerPrice = (rangeLow - (rangeLow*0.0f/100.00f));
		  				if((float)low.get(i) < triggerPrice  && (float)rangeHigh < Float.parseFloat(daysOpen)){
		  					
		  					sql = "select min(low) from "+name+"_"+interval+" where tradedate >='"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
	          				dayslow = executeCountQuery(con, sql);

	          				sql = "select close from "+name+"_15 where tradedate = concat(Date('"+date.get(i)+"'),' 15:15:00')";
	          				daysclose = executeCountQuery(con, sql);
		  					profitPerc = (triggerPrice-Float.parseFloat(daysclose))*100/triggerPrice;
		  					
		  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	             					+ " values ('"+name+"', 'Bear', "+(float)rangeLow+", "+(profitPerc-0.05f)+", "+daysclose+", '"+date.get(i)+"', '"+date.get(i+1)+"', '"+dailyRange+"')";
//		             			System.out.println(sql);
	              			executeSqlQuery(con, sql);gotentered=false;
	              			break;
		  				}
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
   }
   public void UpdatePinResultsADX(java.sql.Connection con, String name,String dailyDate,  boolean updateSymbolsTableData, 
		   boolean updateAllData, boolean updateResultTable, boolean isIntraDayData, boolean insertAllDataToResult) {
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
//	  	List volume = new ArrayList<String>();
	  	String sql="";String interval="10";
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
		  		date.add(rs.getString("tradedate"));
		  		macd.add(rs.getFloat("macd"));
		  		cci.add(rs.getFloat("cci"));
		  		adx.add(rs.getFloat("adx"));
		  		dm_plus.add(rs.getFloat("DM_PLUS_AVERAGE"));
		  		dm_minus.add(rs.getFloat("DM_MINUS_AVERAGE"));
		  		
//		  		volume.add(rs.getString("volume"));
    	  		sig.add(rs.getFloat("sig"));
    	  		hist.add(rs.getFloat("histogram"));
//    	  		hist_dir.add(rs.getString("hist_dir"));
		  	}
		  	
		  	String tableName = "williamsresults";
		  	float filter=0f, filterPerc = 2f;
		  	Float profitPerc=0.0f, profitRupees=0.0f;
		  	float range=0f;int x=0; float p=1f, l=-2f;
		  	String tradeDate="";boolean check=false; String dayshigh="", dayslow="",daysclose=""; boolean gotentered=false;
		  	if(updateAllData == true){
		  		for (int i=0; i< date.size(); i++){
		  			if(i>1){
		  				if(isIntraDayData==true)
		    	  			tradeDate = (String) date.get(i).toString();
		    	  		else
		    	  			tradeDate = date.get(i)+"";
		  				profitPerc = ((float)close.get(i) - (float)open.get(i))*100/(float)open.get(i);
		  				profitRupees =((float)close.get(i) - (float)open.get(i))*100/(float)open.get(i); 
		  				float triggerPrice = ((float)high.get(i) + ((float)high.get(i)*0.15f/100.00f));
		  				if((float)adx.get(i) < 10 && (float)dm_plus.get(i) > (float)dm_minus.get(i) && (float)adx.get(i) >(float)adx.get(i-1) && ((float)dm_plus.get(i) - (float)dm_minus.get(i)) > 1)
		  				{

		  					sql = "select max(high) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
	          				dayshigh = executeCountQuery(con, sql);
	          				triggerPrice = (float)close.get(i);
	          				daysclose = executeCountQuery(con, "select close from "+name+" where tradedate=Date('"+date.get(i)+"')");
		  					profitPerc = (Float.parseFloat(dayshigh) - triggerPrice)*100/triggerPrice;
		  					
		  					profitPerc = (Float.parseFloat(daysclose) - triggerPrice)*100/triggerPrice;
		  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	             					+ " values ('"+name+"', 'Bull', "+(float)cci.get(i+1)+", "+(profitPerc-0.05f)+", "+profitRupees+", '"+date.get(i+1)+"', '"+date.get(i+1)+"', 1)";
//		             			System.out.println(sql);
	              			executeSqlQuery(con, sql);gotentered=false;
	              			break;
		  				}
		  				if((float)adx.get(i) < 10 && (float)dm_plus.get(i) < (float)dm_minus.get(i) && (float)adx.get(i) >(float)adx.get(i-1) && ((float)dm_minus.get(i) - (float)dm_plus.get(i)) > 1)
		  				
		  				{
		  					
		  					sql = "select min(low) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
	          				dayslow = executeCountQuery(con, sql);
	          				triggerPrice = (float)close.get(i);
	          				daysclose = executeCountQuery(con, "select close from "+name+" where tradedate=Date('"+date.get(i)+"')");
		  					profitPerc = (triggerPrice-Float.parseFloat(daysclose))*100/triggerPrice;
		  					
		  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	             					+ " values ('"+name+"', 'Bear', "+(float)cci.get(i+1)+", "+(profitPerc-0.05f)+", "+profitRupees+", '"+date.get(i+1)+"', '"+date.get(i+1)+"', 1)";
//		             			System.out.println(sql);
	              			executeSqlQuery(con, sql);gotentered=false;
	              			break;
		  				}
		  			}
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
   }

   public void UpdatePinResults(java.sql.Connection con, String name,String dailyDate,  boolean updateSymbolsTableData, 
		   boolean updateAllData, boolean updateResultTable, boolean isIntraDayData, boolean insertAllDataToResult) {
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
//	  	List volume = new ArrayList<String>();
	  	String sql="";String interval="5";
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
		  		date.add(rs.getString("tradedate"));
		  		macd.add(rs.getFloat("macd"));
		  		cci.add(rs.getFloat("cci"));
		  		adx.add(rs.getFloat("adx"));
		  		dm_plus.add(rs.getFloat("DM_PLUS_AVERAGE"));
		  		dm_minus.add(rs.getFloat("DM_MINUS_AVERAGE"));
		  		
//		  		volume.add(rs.getString("volume"));
    	  		sig.add(rs.getFloat("sig"));
    	  		hist.add(rs.getFloat("histogram"));
//    	  		hist_dir.add(rs.getString("hist_dir"));
		  	}
		  	
		  	String tableName = "williamsresults";
		  	float filter=0f, filterPerc = 2f;
		  	Float profitPerc=0.0f, profitRupees=0.0f;
		  	float range=0f;int x=0; float p=1f, l=-2f;
		  	String tradeDate="";boolean check=false; String dayshigh="", dayslow="",daysclose=""; boolean gotentered=false;
		  	if(updateAllData == true){
		  		for (int i=0; i< date.size(); i++){
		  			if(i>1){
		  				if(isIntraDayData==true)
		    	  			tradeDate = (String) date.get(i).toString();
		    	  		else
		    	  			tradeDate = date.get(i)+"";
		  				profitPerc = ((float)close.get(i) - (float)open.get(i))*100/(float)open.get(i);
		  				profitRupees =((float)close.get(i) - (float)open.get(i))*100/(float)open.get(i); 
		  				float triggerPrice = ((float)high.get(i) + ((float)high.get(i)*0.15f/100.00f));
		  				if((float)macd.get(i) > 1 && (float)macd.get(i) < (float)macd.get(i) && (float)dm_plus.get(i) >(float)dm_plus.get(i-1) && (float)dm_plus.get(i-1) < 1){
		  					sql = "select adx from "+name+"_15 where tradedate='"+date.get(i)+"'";
		  					if(Float.parseFloat(executeCountQuery(con, sql)) > 50)
		  						break;
		  					sql = "select max(high) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
	          				dayshigh = executeCountQuery(con, sql);
	          				triggerPrice = (float)open.get(i+1);
	          				daysclose = executeCountQuery(con, "select close from "+name+" where tradedate=Date('"+date.get(i)+"')");
		  					profitPerc = (Float.parseFloat(dayshigh) - triggerPrice)*100/triggerPrice;
		  					
		  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	             					+ " values ('"+name+"', 'Bull', "+(float)cci.get(i+1)+", "+(profitPerc-0.05f)+", "+profitRupees+", '"+date.get(i+1)+"', '"+date.get(i+1)+"', 1)";
//		             			System.out.println(sql);
//	              			executeSqlQuery(con, sql);gotentered=false;
	              			break;
		  				}
		  				if((float)macd.get(i) > 8 && (float)macd.get(i+1) < (float)macd.get(i) && (float)macd.get(i) > (float)macd.get(i-1)){
		  					
		  					sql = "select min(low) from "+name+"_"+interval+" where tradedate >'"+date.get(i+1)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
	          				dayslow = executeCountQuery(con, sql);
	          				triggerPrice = (float)close.get(i+1);
	          				daysclose = executeCountQuery(con, "select close from "+name+" where tradedate=Date('"+date.get(i)+"')");
		  					profitPerc = (triggerPrice-Float.parseFloat(dayslow))*100/triggerPrice;
		  					
		  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	             					+ " values ('"+name+"', 'Bear', "+(float)cci.get(i+1)+", "+(profitPerc-0.05f)+", "+profitRupees+", '"+date.get(i+1)+"', '"+date.get(i+1)+"', 1)";
//		             			System.out.println(sql);
	              			executeSqlQuery(con, sql);gotentered=false;
	              			break;
		  				}
		  			}
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
   }

   public void UpdatePinResultsOld(java.sql.Connection con, String name,String dailyDate,  boolean updateSymbolsTableData, 
		   boolean updateAllData, boolean updateResultTable, boolean isIntraDayData, boolean insertAllDataToResult) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	List hist_dir = new ArrayList<String>();
	  	List macd = new ArrayList<Float>();
	  	List sig = new ArrayList<Float>();
	  	List hist = new ArrayList<Float>();List cci = new ArrayList<Float>();
//	  	List volume = new ArrayList<String>();
	  	String sql="";String interval="5";
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
		  		date.add(rs.getString("tradedate"));
		  		macd.add(rs.getFloat("macd"));
		  		cci.add(rs.getFloat("cci"));
		  		
//		  		volume.add(rs.getString("volume"));
    	  		sig.add(rs.getFloat("sig"));
    	  		hist.add(rs.getFloat("histogram"));
//    	  		hist_dir.add(rs.getString("hist_dir"));
		  	}
		  	String tableName = "williamsresults";
		  	float filter=0f, filterPerc = 2f;
		  	Float profitPerc=0.0f, profitRupees=0.0f;
		  	float range=0f;int x=0; float p=1f, l=-2f;
		  	String tradeDate="";boolean check=false; String dayshigh="", daysclose=""; boolean gotentered=false;
		  	if(updateAllData == true){
		  		for (int i=0; i< date.size(); i++){
		  			if(i>3){
		  				if(isIntraDayData==true)
		    	  			tradeDate = (String) date.get(i).toString();
		    	  		else
		    	  			tradeDate = date.get(i)+"";
		  				profitPerc = ((float)close.get(i) - (float)open.get(i))*100/(float)open.get(i);
		  				profitRupees =((float)close.get(i) - (float)open.get(i))*100/(float)open.get(i); 
		  				float triggerPrice = ((float)high.get(i) + ((float)high.get(i)*0.15f/100.00f));
		  				if((float)cci.get(i) > 100 && (float)cci.get(i-1) < 100 && (float)high.get(i+1) >triggerPrice ){
		  					
		  					sql = "select max(high) from "+name+"_"+interval+" where tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:15:00')";
	          				dayshigh = executeCountQuery(con, sql);
	          				daysclose = executeCountQuery(con, "select close from "+name+" where tradedate=Date('"+date.get(i)+"')");
		  					profitPerc = (Float.parseFloat(dayshigh) - triggerPrice)*100/triggerPrice;
		  					
		  					for(int j=i+1; j< date.size()-1; j++){
	          					
	          					if(((float) high.get(j) - (float) triggerPrice)*100/(float) triggerPrice >= p){
	          						profitPerc = ((float) high.get(j) - (float) triggerPrice)*100/(float) triggerPrice;
	          						profitPerc = p;
//	          						dateexited = tradedate.get(j).toString();
	          						gotentered = true;
	          						break;
	          					}else if (((float) low.get(j+1) - (float) triggerPrice)*100/(float) triggerPrice <= l){
	          						profitPerc = ((float) low.get(j+1) - (float) triggerPrice)*100/(float) triggerPrice;
	          						profitPerc = l;
//	          						dateexited = tradedate.get(j+1).toString();
	          						gotentered = true;
	          						break;
	          					}	
	          				}
//		  					if(gotentered==false)
//		  						profitPerc = (Float.parseFloat(daysclose) - triggerPrice)*100/triggerPrice;
		  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	             					+ " values ('"+name+"', 'Bull', "+(float)cci.get(i+1)+", "+(profitPerc-0.05f)+", "+profitRupees+", '"+date.get(i+1)+"', '"+date.get(i+1)+"', 1)";
//		             			System.out.println(sql);
	              			executeSqlQuery(con, sql);gotentered=false;
	              			break;
		  				}
		  				profitRupees =((float) open.get(i) -  (float) close.get(i))*100/(float) open.get(i);
		  				if((float)cci.get(i) < -95 && (float)cci.get(i) > -100 && (float)open.get(i) > (float)close.get(i) && profitRupees > 1){
		  					profitPerc = ((float) open.get(i+1) -  (float) low.get(i+1))*100/(float) open.get(i+1);
		  					
		  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	             					+ " values ('"+name+"', 'Bear', "+(float)cci.get(i+1)+", "+(profitPerc-0.05f)+", "+profitRupees+", '"+date.get(i+1)+"', '"+date.get(i+1)+"', 1)";
//		             			System.out.println(sql);
//	              			executeSqlQuery(con, sql);
	              			break;
		  				}
		  				if( ((float)macd.get(i-2)-(float)sig.get(i-2)) > 0.2 && ((float)macd.get(i)-(float)sig.get(i)) > 0.2 && (float)hist.get(i-1) > 0 && (float)hist.get(i-1) < 0.1 && (float)cci.get(i) > 100 && (float)macd.get(i) > 0  && 
		  						hist_dir.get(i).toString().equalsIgnoreCase("green") && hist_dir.get(i-1).toString().equalsIgnoreCase("red") ){
		  					profitPerc = ((float) high.get(i+1) -  (float) open.get(i+1))*100/(float) open.get(i+1);
//		  					if(profitPerc > p) profitPerc = p; else profitPerc = ((float) close.get(i+1) -  (float) open.get(i+1))*100/(float) open.get(i+1);
		  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	             					+ " values ('"+name+"', 'Bull', "+(float) open.get(i+1)+", "+(profitPerc-0.05f)+", "+profitRupees+", '"+date.get(i+1)+"', '"+date.get(i+1)+"', 1)";
//		             			System.out.println(sql);
//	              			executeSqlQuery(con, sql);
		  				}
		  				
		  				if( ((float)sig.get(i-2)*-1 - (float)macd.get(i-2)*-1) > 0.2 && ((float)sig.get(i)*-1 - (float)macd.get(i)*-1) > 0.2 && (float)hist.get(i-1) < 0 && (float)hist.get(i-1) > -0.1 && (float)cci.get(i) < -100 && (float)macd.get(i) < 0  && 
		  						hist_dir.get(i).toString().equalsIgnoreCase("red") && hist_dir.get(i-1).toString().equalsIgnoreCase("green") 
		  						&& (float)macd.get(i-1) < (float)sig.get(i-1)){
		  					profitPerc = ((float) open.get(i+1) -  (float) low.get(i+1))*100/(float) open.get(i+1);
//		  					if(profitPerc > p) profitPerc = p; else profitPerc = ((float) open.get(i+1) -  (float) close.get(i+1))*100/(float) open.get(i+1);
		  					sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	             					+ " values ('"+name+"', 'Bear', "+(float) open.get(i+1)+", "+(profitPerc-0.05f)+", "+profitRupees+", '"+date.get(i+1)+"', '"+date.get(i+1)+"', 1)";
//		             			System.out.println(sql);
//	              			executeSqlQuery(con, sql);
		  				}
		  			}
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
   }

}

