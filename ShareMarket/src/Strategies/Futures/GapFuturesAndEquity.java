package Strategies.Futures;

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

public class GapFuturesAndEquity extends Connection{
	public void getHighDeliveryPercDates( java.sql.Connection con, String name){
		ResultSet rs=null;
		float percentage=80f;
		List perc = new ArrayList<Float>();
	  	List tradedate = new ArrayList<String>();
	  	List tradedQuantity = new ArrayList<Long>();
	  	List profit = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List cci = new ArrayList<Float>();
	  	List r1 = new ArrayList<Float>();
	  	List r2 = new ArrayList<Float>();
	  	List s1 = new ArrayList<Float>();
	  	List s2 = new ArrayList<Float>();
	  	List max_high = new ArrayList<Float>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			/*rs = executeSelectSqlQuery(con, "select (a.high-a.open)*100/a.open as profit, a.tradedate from "+name+" as a,"+name+"_FUT as b where a.tradedate = b.tradedate "+
					" and (a.open-b.open)*100/a.open>2");
			while(rs.next()){
				profit.add(rs.getFloat("profit"));
				tradedate.add(rs.getString("a.tradedate"));
			}
			for(int i=0; i< profit.size(); i++){
				sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
	  					+ " values ('"+name+"', 'Bull', "+profit.get(i)+", "+profit.get(i)+", "+profit.get(i)+", '"+tradedate.get(i)+"')";
				executeSqlQuery(con, sql);
			}
			profit.clear();tradedate.clear();
			rs = executeSelectSqlQuery(con, "select (a.open-a.low)*100/a.open as profit, a.tradedate from "+name+" as a,"+name+"_FUT as b where a.tradedate = b.tradedate "+
					" and (a.open-b.open)*100/a.open>2");
			while(rs.next()){
				profit.add(rs.getFloat("profit"));
				tradedate.add(rs.getString("a.tradedate"));
			}
			for(int i=0; i< profit.size(); i++){
				sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
	  					+ " values ('"+name+"', 'Bear', "+profit.get(i)+", "+profit.get(i)+", "+profit.get(i)+", '"+tradedate.get(i)+"')";
				executeSqlQuery(con, sql);
			}*/
			rs = executeSelectSqlQuery(con, "select (b.high-b.open)*100/b.open as profit, a.tradedate from "+name+" as a,"+name+"_FUT as b where a.tradedate = b.tradedate "+
					" and (a.open-b.open)*100/b.open>2");
			while(rs.next()){
				profit.add(rs.getFloat("profit"));
				tradedate.add(rs.getString("a.tradedate"));
			}
			for(int i=0; i< profit.size(); i++){
				sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
	  					+ " values ('"+name+"', 'Bull', "+profit.get(i)+", "+profit.get(i)+", "+profit.get(i)+", '"+tradedate.get(i)+"')";
				executeSqlQuery(con, sql);
			}
			profit.clear();tradedate.clear();
			rs = executeSelectSqlQuery(con, "select (b.open-b.low)*100/b.open as profit, a.tradedate from "+name+" as a,"+name+"_FUT as b where a.tradedate = b.tradedate "+
					" and (b.open-a.open)*100/b.open>2");
			while(rs.next()){
				profit.add(rs.getFloat("profit"));
				tradedate.add(rs.getString("a.tradedate"));
			}
			for(int i=0; i< profit.size(); i++){
				sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
	  					+ " values ('"+name+"', 'Bear', "+profit.get(i)+", "+profit.get(i)+", "+profit.get(i)+", '"+tradedate.get(i)+"')";
				executeSqlQuery(con, sql);
			}
			profit.clear();tradedate.clear();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      GapFuturesAndEquity pin = new GapFuturesAndEquity();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	String sql="";
	    	  	sql =  "SELECT s.name FROM symbols s where volume > 100000000 and todaysopen>6 order by volume desc";
	    	  	sql =  "SELECT s.name FROM symbols s, margintables m where s.name=m.name ";
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
