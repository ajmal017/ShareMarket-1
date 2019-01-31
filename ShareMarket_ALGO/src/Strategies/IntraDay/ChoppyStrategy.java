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

public class ChoppyStrategy extends Connection{
	public void getData( java.sql.Connection con, String name){
		ResultSet rs=null;
		float percentage=80f;
		
	  	List tradedate = new ArrayList<String>();
	  	List perc = new ArrayList<Float>();
	  	List tradedQuantity = new ArrayList<Long>();
	  	List<Float> open = new ArrayList<Float>();
	  	List<Float> high = new ArrayList<Float>();
	  	List<Float> low = new ArrayList<Float>();
	  	List<Float> close = new ArrayList<Float>();
	  	List pivot = new ArrayList<Float>();
	  	List r2 = new ArrayList<Float>();
	  	List s1 = new ArrayList<Float>();
	  	List s2 = new ArrayList<Float>();
	  	List max_high = new ArrayList<Float>();
	  	List min_low = new ArrayList<Float>();
	  	List<Float> choppyIndex = new ArrayList<Float>();
	  	List<Float> intraday3Min3_18_close = new ArrayList<Float>();
	  	List<Float> intradayFirst3MinClose = new ArrayList<Float>();
	  	List intradayHigh = new ArrayList<Float>();
	  	List intradayLow = new ArrayList<Float>();
	  	
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select tradedate,open,high,low,close,intradayFirst3MinClose,intraday3Min3_18_close "
					+ " from `"+name+"` ");
			while(rs.next()){
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
//		  		intradayFirst3MinClose.add(rs.getFloat("intradayFirst3MinClose"));
//		  		intraday3Min3_18_close.add(rs.getFloat("intraday3Min3_18_close"));
			}
			float trig=0f;String tradedDate="", exitDate="";
			int count = 0, count2=0;boolean isChoppy=false;
			for(int i=1; i< tradedate.size()-3; i++){
				isChoppy=false;
				/*if((high.get(i)-low.get(i))*100/low.get(i) < 0.3)
				{
					for (int j=i+1; j<=i+20 ; j++){
						if(high.get(j) < high.get(i) && low.get(j)>low.get(i)){
							isChoppy=true;
						}else{
							isChoppy=false;
						}
					}
				}*/
				if(high.get(i) >high.get(i-1) && close.get(i)<high.get(i-1) && 
						high.get(i+1) >Math.min(high.get(i-1), high.get(i)) && close.get(i+1)<Math.min(high.get(i-1), high.get(i))
						&& close.get(i+2) > Math.min(high.get(i-1), high.get(i))){
					float profit=(high.get(i+3)-open.get(i+3))*100/open.get(i+3);
					float profitAtClose=(close.get(i+3)-open.get(i+3))*100/open.get(i+3);
					sql = "insert into psarresults(name, reversal, profitPerc,profitRupees,triggerPrice, date) "
		  					+ " values ('"+name+"', 'Bull', "+profit+", "+profitAtClose+", "
		  							+ ""+profitAtClose+", '"+tradedate.get(i+3)+"')";
					executeSqlQuery(con, sql);
				}
				if(low.get(i) <low.get(i-1) && close.get(i)>low.get(i-1) && 
						low.get(i+1) <Math.max(low.get(i-1), low.get(i)) && close.get(i+1)>Math.max(low.get(i-1), low.get(i))
						&& close.get(i+2) < Math.max(low.get(i-1), low.get(i))){
					float profit=(open.get(i+3)-low.get(i+3))*100/open.get(i+3);
					float profitAtClose=(open.get(i+3)-close.get(i+3))*100/open.get(i+3);
					sql = "insert into psarresults(name, reversal, profitPerc,profitRupees,triggerPrice, date) "
		  					+ " values ('"+name+"', 'Bear', "+profit+", "+profitAtClose+", "
		  							+ ""+profitAtClose+", '"+tradedate.get(i+3)+"')";
					executeSqlQuery(con, sql);
				}
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      ChoppyStrategy pin = new ChoppyStrategy();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where "
	    	  			+ " ismargin=1 order by volume desc");
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
