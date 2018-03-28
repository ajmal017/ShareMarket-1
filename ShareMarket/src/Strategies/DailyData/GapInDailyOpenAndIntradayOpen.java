package Strategies.DailyData;

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

public class GapInDailyOpenAndIntradayOpen extends Connection{
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
	  	List tradedateF = new ArrayList<String>();
	  	List openF = new ArrayList<Float>();
	  	List highF = new ArrayList<Float>();
	  	List lowF = new ArrayList<Float>();
	  	List closeF = new ArrayList<Float>();
	  	List max_high = new ArrayList<Float>();
	  	GapInDailyOpenAndIntradayOpen gap = new GapInDailyOpenAndIntradayOpen();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select * from  "+name+" as a where tradedate >= '2017-01-01' and tradedate <> '2016-06-24'");
			while(rs.next()){
				tradedate.add(rs.getString("a.tradedate"));
		  		open.add(rs.getFloat("a.open"));
		  		high.add(rs.getFloat("a.high"));
		  		low.add(rs.getFloat("a.low"));
		  		close.add(rs.getFloat("a.close"));
			}
			float trig=0f, prev1,prev2,prev3,prev4, width;float gapPerc=1f, div=10;int iter=1;String out="";
			float targetPerc=0.2f;
			for(int i=1; i< tradedate.size(); i++){
				float exitPrice =0f, stopLoss=0f;
				isOpenPriceTouched(con, name, tradedate.get(i).toString(),(float)open.get(i), 
						(float)high.get(i), (float)low.get(i), (float)close.get(i));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public String isOpenPriceTouched(java.sql.Connection con, String name, String date, float daysOpen,
			float daysHigh, float daysLow, float daysClose) throws SQLException{
		String sql = "";int interval=60;String isOpenPriceTouched="";
		List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List tradedate = new ArrayList<String>();
	  	ResultSet rs=null;
	  	rs = executeSelectSqlQuery(con, "select * from  "+name+"_"+interval+" as a where date(tradedate) = date('"+date+"') limit 1");
		while(rs.next()){
			tradedate.add(rs.getString("a.tradedate"));
	  		open.add(rs.getFloat("a.open"));
	  		high.add(rs.getFloat("a.high"));
	  		low.add(rs.getFloat("a.low"));
	  		close.add(rs.getFloat("a.close"));
		}
		boolean entered = false;
		float MoreSLPerc=0.3f;float max=0f, min=100000, diff=0f, profit, profitAtClose, diffCutoff=1f;
		for(int i=0; i< open.size(); i++){
			diff = (daysOpen-(float)open.get(0))*100/(float)open.get(0);
			if(diff>diffCutoff && diff<20){
				profit = (daysHigh-(float)open.get(0))*100/(float)open.get(0);
				profitAtClose = (daysClose-(float)open.get(0))*100/(float)open.get(0);
				sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
	  					+ " values ('"+name+"', 'Bull', "+(float)open.get(0)+", "+profit+", "+profitAtClose+", '"+tradedate.get(i)+"')";
				executeSqlQuery(con, sql);
				break;
			}
			diff = ((float)open.get(0)-daysOpen)*100/(float)open.get(0);
			if(diff>diffCutoff && diff<20){
				profit = ((float)open.get(0)-daysLow)*100/(float)open.get(0);
				profitAtClose = ((float)open.get(0)-daysClose)*100/(float)open.get(0);
				sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
	  					+ " values ('"+name+"', 'Bear', "+(float)open.get(0)+", "+profit+", "+profitAtClose+", '"+tradedate.get(i)+"')";
				executeSqlQuery(con, sql);
				break;
			}
		}
		return isOpenPriceTouched;
	}
   public static void main(String[] args)  {
	      Test t = new Test();
	      GapInDailyOpenAndIntradayOpen pin = new GapInDailyOpenAndIntradayOpen();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	String sql="";
	    	  	sql =  "SELECT s.name FROM symbols s where volume > 100000000 and lastprice > 6 order by volume ";
//	    	  	sql =  "SELECT s.name FROM symbols s, margintables m where s.name=m.name and s.name <> 'MINDTREE' order by id";
	    	  	rs = con.executeSelectSqlQuery(dbConnection, sql);
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
