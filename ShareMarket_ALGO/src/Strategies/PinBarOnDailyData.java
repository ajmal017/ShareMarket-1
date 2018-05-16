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

public class PinBarOnDailyData extends Connection{
	public void getHighDeliveryPercDates( java.sql.Connection con, String name){
		ResultSet rs=null;
		float percentage=80f;
	  	List tradedate = new ArrayList<String>();
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List pivot = new ArrayList<Float>();
	  	List r1 = new ArrayList<Float>();
	  	List r2 = new ArrayList<Float>();
	  	List s1 = new ArrayList<Float>();
	  	List s2 = new ArrayList<Float>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select * from "+name+" a, margintables b where b.name='"+name+"'");
			while(rs.next()){
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("a.open"));
		  		high.add(rs.getFloat("a.high"));
		  		low.add(rs.getFloat("a.low"));
		  		close.add(rs.getFloat("a.close"));
		  		pivot.add(rs.getFloat("a.pivot"));
		  		r1.add(rs.getFloat("a.r1"));
		  		r2.add(rs.getFloat("a.r2"));
		  		s1.add(rs.getFloat("a.s1"));
		  		s2.add(rs.getFloat("a.s2"));
			}
			float trig=0f;
			for(int i=0; i< tradedate.size()-1; i++){
				if(i<6) continue;
				float gain = ((float)close.get(i)-(float)open.get(i))*100/(float)open.get(i);
				float lowerShadow=((float)close.get(i) - (float)low.get(i))*100/(float)low.get(i);
				float bullWidth=(((float)close.get(i) - (float)open.get(i))*100/(float)open.get(i));
				float bearWidth=(((float)open.get(i) - (float)close.get(i))*100/(float)open.get(i));
				float upperShadow=((float)high.get(i) - (float)close.get(i))*100/(float)close.get(i);
				float percDropped = ((float)open.get(i-4) - (float)close.get(i))*100/(float)open.get(i-4);
				float percGained = ((float)close.get(i) - (float)open.get(i-4))*100/(float)open.get(i-4);
				if(lowerShadow > bullWidth*3 && (float)low.get(i) < (float)s2.get(i) && bullWidth>1 && percDropped >10 ){
					trig=(float)open.get(i+1); 
					percProfitAtHighPrice = ((float)high.get(i+1)-(float)open.get(i+1))*100/(float)open.get(i+1);
					percProfitAtClosePrice = ((float)close.get(i+1)-(float)open.get(i+1))*100/(float)open.get(i+1);
					
					percProfitAtHighPrice = ((float)high.get(i+1)-trig)*100/trig;
					percProfitAtClosePrice = ((float)close.get(i+3)-trig)*100/trig;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "+percProfitAtHighPrice+", '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
				if(upperShadow > bearWidth*3 && (float)high.get(i) > (float)r2.get(i) && bearWidth>1 && percGained > 10 ){
					trig=(float)open.get(i+1);
					percProfitAtLowPrice = ((float)open.get(i+1)-(float)low.get(i+1))*100/(float)open.get(i+1);
					percProfitAtClosePrice = ((float)open.get(i+1)-(float)close.get(i+1))*100/(float)open.get(i+1);
					
					percProfitAtLowPrice = (trig - (float)low.get(i+1))*100/trig;
					percProfitAtClosePrice = (trig-(float)close.get(i+3))*100/trig;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bear', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "+percProfitAtLowPrice+", '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      PinBarOnDailyData pin = new PinBarOnDailyData();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 100000 and todaysopen>6 order by volume desc");
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
	    	  		pin.getHighDeliveryPercDates(dbConnection, name);
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
}
