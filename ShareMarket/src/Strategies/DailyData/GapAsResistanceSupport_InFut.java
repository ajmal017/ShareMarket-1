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

public class GapAsResistanceSupport_InFut extends Connection{
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
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select * from  "+name+"_FUT as a");
			while(rs.next()){
				tradedate.add(rs.getString("a.tradedate"));
		  		open.add(rs.getFloat("a.open"));
		  		high.add(rs.getFloat("a.high"));
		  		low.add(rs.getFloat("a.low"));
		  		close.add(rs.getFloat("a.close"));
			}
			float trig=0f, prev1,prev2,prev3,prev4, width;float gapPerc=1f, div=3;int iter=1;String out="";
			float stopLossPerc=1f, targetPerc=1f;int count=0;
			for(int i=1; i< tradedate.size(); i++){
				count=0;
				if(((float)open.get(i)-(float)high.get(i-1))*100/(float)high.get(i-1) > gapPerc
						&& ((float)low.get(i) - (float)high.get(i-1))*100/(float)high.get(i-1) > gapPerc/3){
					for(int j=i+1; j< tradedate.size(); j++){
						count++;
						/*if((float)low.get(j) < (float)low.get(i) && (float)close.get(j)>(float)low.get(i) && (float)open.get(j)>(float)low.get(i)){
							percProfitAtHighPrice = ((float)high.get(j+1)-(float)open.get(j+1))*100/(float)open.get(j+1);
							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noOfCandles) "
				  					+ " values ('"+name+"', 'Bull2', "+percProfitAtHighPrice+", "+percProfitAtHighPrice+", "+percProfitAtClosePrice+", "
				  							+ "'"+tradedate.get(i)+"', '"+tradedate.get(j+1)+"', '"+count+"')";
							executeSqlQuery(con, sql);
							break;
						}*/
//						&& (float)open.get(j+1)>(float)close.get(j)
						if((float)high.get(j) > (float)high.get(i) && (float)close.get(j) < (float)high.get(i) && (float)open.get(j)<(float)high.get(i)
								&& ((float)high.get(i)-(float)close.get(j))*100/(float)close.get(j) > 0.5 ){
//							percProfitAtHighPrice = ((float)open.get(j+1)-Math.min((float)low.get(j+1), (float)low.get(j+1)))*100/(float)open.get(j+1);
							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noOfCandles) "
				  					+ " values ('"+name+"', 'Bear', "+percProfitAtHighPrice+", "+percProfitAtHighPrice+", "+percProfitAtClosePrice+", "
				  							+ "'"+tradedate.get(i)+"', '"+tradedate.get(j)+"', '"+count+"')";
							executeSqlQuery(con, sql);
							break;
						}
					}
				}
				if(((float)low.get(i-1)-(float)open.get(i))*100/(float)open.get(i) > gapPerc && 
						((float)low.get(i-1) - (float)high.get(i))*100/(float)high.get(i) > gapPerc/3){
					for(int j=i+1; j< tradedate.size(); j++){
						count++;
						/*if((float)high.get(j) > (float)high.get(i) && (float)close.get(j) < (float)high.get(i) && (float)open.get(j) < (float)high.get(i)){
							percProfitAtHighPrice = ((float)open.get(j+1)-(float)low.get(j+1))*100/(float)open.get(j+1);
							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noOfCandles) "
				  					+ " values ('"+name+"', 'Bear2', "+percProfitAtHighPrice+", "+percProfitAtHighPrice+", "+percProfitAtClosePrice+", "
				  							+ "'"+tradedate.get(i)+"', '"+tradedate.get(j+1)+"', '"+count+"')";
							executeSqlQuery(con, sql);
							break;
						}*/
//						put this filter on both side and uncomment profitAtHighPrice (float)open.get(j+1)<(float)close.get(j)
						if((float)low.get(j) < (float)low.get(i) && (float)close.get(j) > (float)low.get(i) && (float)open.get(j) > (float)low.get(i)
								&& ((float)close.get(j)-(float)low.get(i))*100/(float)low.get(i) > 0.5 ){
//							percProfitAtHighPrice = (Math.max((float)high.get(j+1), (float)high.get(j+1))-(float)open.get(j+1))*100/(float)open.get(j+1);
							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noOfCandles) "
				  					+ " values ('"+name+"', 'Bull', "+percProfitAtHighPrice+", "+percProfitAtHighPrice+", "+percProfitAtClosePrice+", "
				  							+ "'"+tradedate.get(i)+"', '"+tradedate.get(j)+"', '"+count+"')";
							executeSqlQuery(con, sql);
							break;
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      GapAsResistanceSupport_InFut pin = new GapAsResistanceSupport_InFut();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	String sql="";
	    	  	sql =  "SELECT s.name FROM symbols s where volume > 50000000 and todaysopen>6 order by volume desc";
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
