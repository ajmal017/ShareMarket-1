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

public class HeikenAshi_OnDaily extends Connection{
	public void getHighDeliveryPercDates( java.sql.Connection con, String name){
		ResultSet rs=null;
		float percentage=80f;
	  	List tradedate = new ArrayList<String>();
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List openHA = new ArrayList<Float>();
	  	List highHA = new ArrayList<Float>();
	  	List lowHA = new ArrayList<Float>();
	  	List closeHA = new ArrayList<Float>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select * from "+name+"");
			while(rs.next()){
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		
		  		openHA.add(rs.getFloat("HA_open"));
		  		highHA.add(rs.getFloat("HA_high"));
		  		lowHA.add(rs.getFloat("HA_low"));
		  		closeHA.add(rs.getFloat("HA_close"));
			}
			float trig=0f;boolean isValid=true;float gap=0.5f;
			for(int i=10; i< tradedate.size()-1; i++){
				if((float)openHA.get(i-1)==(float)highHA.get(i-1) && (float)openHA.get(i-2)==(float)lowHA.get(i-2)
						&& (float)openHA.get(i-3)==(float)lowHA.get(i-3) && (float)openHA.get(i-4)==(float)lowHA.get(i-4)
						){
					trig = (float)open.get(i);
					percProfitAtLowPrice = (trig - (float)low.get(i))*100/(float)open.get(i);
					percProfitAtClosePrice = (trig - (float)close.get(i))*100/(float)open.get(i);
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bear', "+percProfitAtLowPrice+", "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
				
				if((float)openHA.get(i-1)==(float)lowHA.get(i-1) && (float)openHA.get(i-2)==(float)highHA.get(i-2)
						&& (float)openHA.get(i-3)==(float)highHA.get(i-3) && (float)openHA.get(i-4)==(float)highHA.get(i-4)
						){
					trig = (float)open.get(i);
					percProfitAtLowPrice = (trig - (float)low.get(i))*100/(float)open.get(i);
					percProfitAtClosePrice = (trig - (float)close.get(i))*100/(float)open.get(i);
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bull', "+percProfitAtLowPrice+", "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      HeikenAshi_OnDaily pin = new HeikenAshi_OnDaily();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 50000000 and isMargin='1' order by volume desc");
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
