package Strategies.Adx;

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

public class NoChangeInPrice extends Connection{
	public void getHighDeliveryPercDates( java.sql.Connection con, String name){
		ResultSet rs=null;
		float percentage=80f;
		List perc = new ArrayList<Float>();
	  	List tradedate = new ArrayList<String>();
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
	  	List di_plus = new ArrayList<Float>();
	  	List di_minus = new ArrayList<Float>();
	  	List max_high = new ArrayList<Float>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select * from "+name+"_dp a, "+name+" b where  a.tradedate=b.tradedate;");
			while(rs.next()){
				tradedate.add(rs.getString("tradedate"));
				perc.add(rs.getFloat("perc"));
				tradedQuantity.add(rs.getLong("tradedQuantity"));
				open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		max_high.add(rs.getFloat("max_past_high"));
		  		pivot.add(rs.getFloat("pivot"));
		  		r1.add(rs.getFloat("r1"));
		  		r2.add(rs.getFloat("r2"));
		  		s1.add(rs.getFloat("s1"));
		  		s2.add(rs.getFloat("s2"));
		  		di_plus.add(rs.getFloat("DI_PLUS_AVERAGE"));
		  		di_minus.add(rs.getFloat("DI_MINUS_AVERAGE"));
				tradedQuantity.add(rs.getLong("tradedQuantity"));
			}
			float trig=0f, prev1,prev2,prev3,prev4, width;int count=0;
			for(int i=0; i< tradedate.size()-1; i++){
				count=0;
				if((float)perc.get(i) ==100 && (float)perc.get(i-1) !=100){
					for(int j=i-10; j< i; j++){
						if((float)perc.get(j) !=100){
							count++;
						}else count =0;
					}
				}
				if(count>6){
						trig=(float)open.get(i+1); 
						percProfitAtHighPrice = ((float)high.get(i+5)-trig)*100/trig;
						percProfitAtClosePrice = ((float)close.get(i+5)-trig)*100/trig;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "+percProfitAtHighPrice+", '"+tradedate.get(i+1)+"')";
						executeSqlQuery(con, sql);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      NoChangeInPrice pin = new NoChangeInPrice();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 10000  and todaysopen>2 and id>1075 order by id");
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
