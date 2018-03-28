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

public class GapFuturesAndEquity_DiffInOpen extends Connection{
	public void getHighDeliveryPercDates( java.sql.Connection con, String name){
		ResultSet rs=null;
		float percentage=80f;
		List perc = new ArrayList<Float>();
	  	List tradedate = new ArrayList<String>();
	  	List tradedateF = new ArrayList<String>();
	  	List tradedQuantity = new ArrayList<Long>();
	  	List openF = new ArrayList<Float>();
	  	List highF = new ArrayList<Float>();
	  	List lowF = new ArrayList<Float>();
	  	List closeF = new ArrayList<Float>();

	  	List open = new ArrayList<Float>();
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
			rs = executeSelectSqlQuery(con, "select * from "+name+" a,"+name+"_FUT b where a.tradedate = b.tradedate");
			while(rs.next()){
				tradedate.add(rs.getString("a.tradedate"));
				tradedateF.add(rs.getString("b.tradedate"));

				open.add(rs.getFloat("a.open"));
		  		high.add(rs.getFloat("a.high"));
		  		low.add(rs.getFloat("a.low"));
		  		close.add(rs.getFloat("a.close"));
		  		
				openF.add(rs.getFloat("b.open"));
		  		highF.add(rs.getFloat("b.high"));
		  		lowF.add(rs.getFloat("b.low"));
		  		closeF.add(rs.getFloat("b.close"));
			}
			float target=2f, trigPerc=0f;;
			float trig=0f, prev1,prev2,prev3,prev4, width;float gapPerc=1f, div=3, gap;
			for(int i=1; i< tradedate.size(); i++){
				trig = (float)openF.get(i) - ((float)openF.get(i)-(float)open.get(i))/3;
				gap=((float)openF.get(i)-(float)open.get(i))*100/(float)open.get(i);
				if(gap > gapPerc ) 
				{
					float profitF = ((float)openF.get(i)-(float)lowF.get(i))*100/(float)openF.get(i);
					float profitEquity = ((float)high.get(i)-(float)open.get(i))*100/(float)open.get(i);
//					if(profitF>target) profitF=target;
//					else profitF = ((float)openF.get(i)-(float)closeF.get(i))*100/(float)openF.get(i);
//					
//					if(profitEquity> target) profitEquity=target;
//					else profitEquity =  ((float)close.get(i)-(float)open.get(i))*100/(float)open.get(i);
					trig = (float) ((float)open.get(i)-((float)open.get(i)*trigPerc/100));
					profitEquity =  ((float)close.get(i)-trig)*100/trig;
//					if((float)low.get(i)>trig)  profitEquity=999;
					trig = (float) ((float)openF.get(i)+((float)openF.get(i)*trigPerc/100));
					profitF = (trig-(float)closeF.get(i))*100/trig;
//					if((float)highF.get(i)<trig)  profitF=999;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bear', "+profitF+", "+profitEquity+", "+gap+", '"+tradedate.get(i)+"')";
					executeSqlQuery(con, sql);
				}
				trig = (float)openF.get(i) + ((float)open.get(i)-(float)openF.get(i))/3;
				gap=((float)open.get(i)-(float)openF.get(i))*100/(float)open.get(i);
				if(gap > gapPerc)  
				{
					float profitEquity = ((float)open.get(i)-(float)low.get(i))*100/(float)open.get(i);
					float profitF = ((float)highF.get(i)-(float)openF.get(i))*100/(float)openF.get(i);
//					if(profitEquity>target) profitEquity=target;
//					else profitEquity = ((float)open.get(i)-(float)close.get(i))*100/(float)open.get(i);
//					if(profitF>target) profitF=target;
//					else profitF = ((float)closeF.get(i)-(float)openF.get(i))*100/(float)openF.get(i);
					trig = (float) ((float)openF.get(i)-((float)openF.get(i)*trigPerc/100));
					profitF = ((float)closeF.get(i)-trig)*100/trig;
//					if((float)lowF.get(i)>trig)  profitF=999;
					trig = (float) ((float)open.get(i)+((float)open.get(i)*trigPerc/100));
					profitEquity = (trig-(float)close.get(i))*100/trig;
//					if((float)high.get(i)<trig)  profitEquity=999;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'Bull', "+profitF+", "+profitEquity+", "+gap+", '"+tradedate.get(i)+"')";
					executeSqlQuery(con, sql);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      GapFuturesAndEquity_DiffInOpen pin = new GapFuturesAndEquity_DiffInOpen();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	String sql="";
	    	  	sql =  "SELECT s.name FROM symbols s where volume > 100000000 and todaysopen>6 order by volume desc";
	    	  	sql =  "SELECT s.name FROM symbols s, margintables m where s.name=m.name and s.name<>'COALINDIA'";
//	    	  			+ " and fut_avg_volume*fut_lot_size*fut_open > 1000000000";
	    	  	System.out.println(sql);
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
