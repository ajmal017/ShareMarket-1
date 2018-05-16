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

public class GapIntraCheckWithSL extends Connection{
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
	  	GapIntraCheckWithSL gap = new GapIntraCheckWithSL();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select * from  "+name+" as a where tradedate >= '2016-02-01' and tradedate <> '2016-06-24'");
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
				if(((float)open.get(i)-(float)high.get(i-1))*100/(float)high.get(i-1) > gapPerc ){
					float trigger = ((float)open.get(i)-(float)high.get(i-1))/div;
					trigger = ((float)open.get(i)-trigger);
					exitPrice = (float) (trigger-(trigger*targetPerc/100));
					isOpenPriceTouched(con, name, tradedate.get(i).toString(), "bear", trigger, exitPrice, 
							(float)open.get(i), (float)close.get(i), targetPerc, (float)high.get(i), (float)low.get(i));
				}
				if(((float)low.get(i-1)-(float)open.get(i))*100/(float)open.get(i) > gapPerc){
					float trigger = ((float)low.get(i-1)-(float)open.get(i))/div;
					trigger = ((float)open.get(i)+trigger);
					exitPrice = (float) (trigger+(trigger*targetPerc/100));
					isOpenPriceTouched(con, name, tradedate.get(i).toString(), "bull", trigger, exitPrice, 
							(float)open.get(i), (float)close.get(i), targetPerc, (float)high.get(i), (float)low.get(i));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public String isOpenPriceTouched(java.sql.Connection con, String name, String date, String dir, float trig,
			float exitPrice, float SL, float closePrice, float targetPerc, float daysHigh, float daysLow) throws SQLException{
		String sql = "";int interval=5;String isOpenPriceTouched="";
		List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List tradedate = new ArrayList<String>();
	  	ResultSet rs=null;
	  	rs = executeSelectSqlQuery(con, "select * from  "+name+"_"+interval+" as a where date(tradedate) = date('"+date+"')");
		while(rs.next()){
			tradedate.add(rs.getString("a.tradedate"));
	  		open.add(rs.getFloat("a.open"));
	  		high.add(rs.getFloat("a.high"));
	  		low.add(rs.getFloat("a.low"));
	  		close.add(rs.getFloat("a.close"));
		}
		boolean entered = false;
		float MoreSLPerc=0.5f;float max=0f, min=100000;
		for(int i=0; i< 20; i++){
			max = Math.max(max, (float)high.get(i));
			min = Math.min(min, (float)low.get(i));
			if((float)low.get(i) < trig && (float)open.get(i) > trig && dir.equalsIgnoreCase("Bear")
					&& entered==false){
				SL = max;
				SL = SL + (SL*MoreSLPerc/100);
				for(int j=i; j< tradedate.size(); j++){

					if((float)high.get(j) > SL){
						float loss = (trig-SL)*100/trig;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'BearLoss_"+SL+"', "+trig+", "+exitPrice+", "+loss+", '"+tradedate.get(j)+"')";
						executeSqlQuery(con, sql);
						loss = (daysHigh-SL)*100/SL;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Opp_"+SL+"', "+trig+", "+exitPrice+", "+loss+", '"+tradedate.get(j)+"')";
//						executeSqlQuery(con, sql);
						entered=true;
						break;
					}
					if((float)low.get(j) < exitPrice){
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bear_"+SL+"', "+trig+", "+exitPrice+", "+targetPerc+", '"+tradedate.get(j)+"')";
						executeSqlQuery(con, sql);
						entered=true;
						break;
					}
				}
				if(entered==false && i== tradedate.size()-1){
					float loss = (trig-closePrice)*100/trig;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'BearAtClose_"+SL+"', "+trig+", "+exitPrice+", "+loss+", '"+tradedate.get(i)+"')";
					executeSqlQuery(con, sql);
				}
			}
			if((float)high.get(i) > trig && (float)open.get(i) < trig && dir.equalsIgnoreCase("Bull")
					&& entered==false){
				SL = min;
				SL = SL - (SL*MoreSLPerc/100);
				for(int j=i; j< tradedate.size(); j++){

					if((float)low.get(j) < SL){
						float loss = (SL-trig)*100/trig;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'BullLoss_"+SL+"', "+trig+", "+exitPrice+", "+loss+", '"+tradedate.get(j)+"')";
						executeSqlQuery(con, sql);
						loss = (SL-daysLow)*100/SL;
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Opp_"+SL+"', "+trig+", "+exitPrice+", "+loss+", '"+tradedate.get(j)+"')";
//						executeSqlQuery(con, sql);
						entered=true;
						break;
					}
					if((float)high.get(j) > exitPrice){
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bull_"+SL+"', "+trig+", "+exitPrice+", "+targetPerc+", '"+tradedate.get(j)+"')";
						executeSqlQuery(con, sql);
						entered=true;
						break;
					}
				}
				if(entered==false && i== tradedate.size()-1){
					float loss = (closePrice-trig)*100/trig;
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
		  					+ " values ('"+name+"', 'BullAtClose_"+SL+"', "+trig+", "+exitPrice+", "+loss+", '"+tradedate.get(i)+"')";
					executeSqlQuery(con, sql);
				}
			}
			if(entered==true) break;
			
		}
		return isOpenPriceTouched;
	}
   public static void main(String[] args)  {
	      Test t = new Test();
	      GapIntraCheckWithSL pin = new GapIntraCheckWithSL();
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
