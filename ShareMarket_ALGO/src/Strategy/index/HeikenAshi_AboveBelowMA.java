package Strategy.index;

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

public class HeikenAshi_AboveBelowMA extends Connection{

	java.sql.Connection dbConnection = null;
	public HeikenAshi_AboveBelowMA() {
		Connection con = new Connection();
	  	dbConnection = con.getDbConnection();
	}
	   public static void main(String[] args)  {
		      Test t = new Test();
		      HeikenAshi_AboveBelowMA range = new HeikenAshi_AboveBelowMA();
		      
		      boolean updateSymbolsTableData=true; boolean updateAllData=true;
		      try{
		    	  
		    	    Connection con = new Connection();
		    	  	ResultSet rs=null;
		    	  	String sql = "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 100000 "
		    	  			+ " order by volume desc ";
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
		   String sql="Select * from "+name+"_30";
		   ResultSet rs=null;
		   rs = executeSelectSqlQuery(con, sql);

		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	
		  	List o = new ArrayList<Float>();
		  	List h = new ArrayList<Float>();
		  	List l = new ArrayList<Float>();
		  	List c = new ArrayList<Float>();
		  	List ema1 = new ArrayList<Float>();
		  	List ema2 = new ArrayList<Float>();
		  	List date = new ArrayList<String>();
		  	List volume = new ArrayList<Integer>();
		   while (rs.next()){
			   open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		o.add(rs.getFloat("HA_open"));
		  		h.add(rs.getFloat("HA_high"));
		  		l.add(rs.getFloat("HA_low"));
		  		c.add(rs.getFloat("HA_close"));
		  		ema1.add(rs.getFloat("ema1"));
		  		ema2.add(rs.getFloat("ema2"));
		  		date.add(rs.getString("tradedate"));
		   }
		   int count=0;float percProfitAtClosePrice=0f;
		   for(int i=100; i< date.size(); i++){
			   count=0;
			   float trig = (float) ((float)ema2.get(i-1) + (float)ema2.get(i-1)*0.2/100);
			   float diff = ((float)open.get(i)-(float)close.get(i))*100/(float)close.get(i);
			   if((float)l.get(i)<=trig && (float)l.get(i-1)>trig){
				   for(int j=i-50; j< i; j++){
					   if((float)c.get(j) > ((float)ema2.get(j)) && (float)ema2.get(j) !=0){
							count++;
						}else count=0;
				   }
				   if(count > 48){
						count=0;
						percProfitAtClosePrice = ((float)high.get(i+1) - (float)open.get(i+1))*100/(float)open.get(i+1);
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited) "
			  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "
			  							+ ""+diff+", '"+date.get(i+1)+"', '"+date.get(i+1)+"')";
						executeSqlQuery(con, sql);
					}
			   }
				
		   }
	   }
	   
	   
	   public void UpdatePinResultsRange2(java.sql.Connection con, String name,String dailyDate, float high, float low,String gapDir, int interval){
		   ResultSet rs=null;
		  	String sql="";
		  	List<Float> ema2 = new ArrayList<>();
		  	try {
		  		
			  	float highDay=0f, lowday=100000f;
			  	String tableName = "williamsresults";int count=0;
			  	float rangeLow, rangeHigh;
				 float profitBullPerc=0f;float range=0f, profitBearPerc=0f;
				 float entry=0f, exitPrice=0f;
				 if(gapDir.equals("lower")){
					 
					 sql = "select case when(open<low+(high-low)/3 and close<low+(high-low)/3 and close<open) then 'GoForBear' else '' end from "+name+"_"+interval+" where "+
							 "date(tradedate) = date('"+dailyDate+"') limit 1";
					 if(executeCountQuery(con, sql).equals("GoForBear")){
						 entry = Float.parseFloat(executeCountQuery(con, "select close from "+name+"_"+interval+" where date(tradedate) = date('"+dailyDate+"') limit 1"));
						 String entryDate = (executeCountQuery(con, "select tradedate from "+name+"_"+interval+" where date(tradedate) = date('"+dailyDate+"') limit 1"));
						 exitPrice = Float.parseFloat(executeCountQuery(con, "select min(low) from "+name+"_"+interval+" "
							 		+ " where date(tradedate)='"+dailyDate+"' and tradedate > '"+entryDate+"'"));
						 profitBearPerc = (entry-exitPrice)*100/entry;
			  				sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
		         					+ " values ('"+name+"', 'Bear2', 0, 0, "+profitBearPerc+", '"+dailyDate+"', '"+dailyDate+"', '1')";
		          			executeSqlQuery(con, sql);
					 }
				 }
				 
				 if(gapDir.equals("upper")){
					 sql = "select case when(open>high-(high-low)/3 and close>high-(high-low)/3 and close>open) then 'GoForBull' else '' end from "+name+"_"+interval+" where "+
							 "date(tradedate) = date('"+dailyDate+"') limit 1";
					 if(executeCountQuery(con, sql).equals("GoForBull")){
						 entry = Float.parseFloat(executeCountQuery(con, "select close from "+name+"_"+interval+" where date(tradedate) = date('"+dailyDate+"') limit 1"));
						 String entryDate = (executeCountQuery(con, "select tradedate from "+name+"_"+interval+" where date(tradedate) = date('"+dailyDate+"') limit 1"));
						 exitPrice = Float.parseFloat(executeCountQuery(con, "select max(high) from "+name+"_"+interval+" "
						 		+ " where date(tradedate)='"+dailyDate+"' and tradedate > '"+entryDate+"'"));
						 profitBullPerc = (exitPrice - entry)*100/entry;
			  				sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
		         					+ " values ('"+name+"', 'Bull2', 0, 0, "+profitBullPerc+", '"+dailyDate+"', '"+dailyDate+"', '1')";
		          			executeSqlQuery(con, sql);
					 }
				 }
				 
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
