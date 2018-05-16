package Strategies.AboveMA;

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

public class PriceAboveMA_TouchingMA extends Connection{
	public void getData( java.sql.Connection con, String name){
		ResultSet rs=null;
		float percentage=80f;
		
	  	List tradedate = new ArrayList<String>();
	  	List perc = new ArrayList<Float>();
	  	List tradedQuantity = new ArrayList<Long>();
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List pivot = new ArrayList<Float>();
	  	List r1 = new ArrayList<Float>();
	  	List r2 = new ArrayList<Float>();
	  	List s1 = new ArrayList<Float>();
	  	List s2 = new ArrayList<Float>();
	  	List max_high = new ArrayList<Float>();
	  	List min_low = new ArrayList<Float>();
	  	List ema2 = new ArrayList<Float>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select * from "+name+" ");
			while(rs.next()){
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		ema2.add(rs.getFloat("ema2"));
			}
			float trig=0f;String tradedDate="", exitDate="";
			int count = 0; float ema =0f;
			
			for(int i=0; i< tradedate.size()-1; i++){
				ema = (float) Math.ceil((float)ema2.get(i));
				float target = ema*1f/100;tradedDate=""; exitDate="";
				target=0;
				if((float)low.get(i) > (ema+target) && ema !=0){
					count++;
				}else count=0;
				boolean entered = false;
				boolean lower = ((float)close.get(i)-(float)low.get(i)) > 
				1 * Math.abs((float)open.get(i) - (float)close.get(i));
				if(count > 20 && (float)low.get(i+1) <= ema && (float)open.get(i+1) > ema){
					count=0;
					percProfitAtClosePrice = ((float)close.get(i+10) - (float)open.get(i))*100/(float)open.get(i);
					percProfitAtClosePrice = ((float)close.get(i+10) - (float)ema)*100/(float)ema;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited) "
		  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "
		  							+ ""+percProfitAtHighPrice+", '"+tradedate.get(i+1)+"', '"+tradedate.get(i+1)+"')";
					executeSqlQuery(con, sql);
					
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      PriceAboveMA_TouchingMA pin = new PriceAboveMA_TouchingMA();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 100000000 order by volume desc");
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
	    	  		pin.getData(dbConnection, name);
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
