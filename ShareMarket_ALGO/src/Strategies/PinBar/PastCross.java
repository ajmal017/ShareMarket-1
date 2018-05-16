package Strategies.PinBar;

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

public class PastCross extends Connection{
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
	  	List volume = new ArrayList<Long>();
	  	List max_high = new ArrayList<Float>();
	      PastCross pin = new PastCross();
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
		  		max_high.add(rs.getFloat("max_past_high"));
		  		pivot.add(rs.getFloat("pivot"));
		  		r1.add(rs.getFloat("r1"));
		  		r2.add(rs.getFloat("r2"));
		  		s1.add(rs.getFloat("s1"));
		  		s2.add(rs.getFloat("s2"));
		  		volume.add(rs.getLong("volume"));
			}
			float trig=0f;boolean isValid=true;String res="";
			for(int i=1; i< tradedate.size()-1; i++){
				sql = "select concat(DATEdiff(tradedate, '"+tradedate.get(i)+"'),'_', tradedate, '_',high) from "+name+" where tradedate>'"+tradedate.get(i)+"' "
						+ " and high> "+high.get(i)+" and close < "+close.get(i)+" "
						+ " and (high-close)*100/close >1 order by tradedate";
				res = executeCountQuery(con,sql);
//				System.out.println(sql);
				if(!res.equals("")){
					if(Integer.parseInt(res.split("_")[0]) > 20){
						boolean check = pin.checkForResistanceSupport(con, name, tradedate.get(i).toString(), (float)high.get(i), res);
						
						String result = executeCountQuery(con, "select count(*) from "+name+" where tradedate>'"+tradedate.get(i)+"' and high> "+high.get(i)+" and "+
								" tradedate< '"+res.split("_")[1]+"'");
						if(Integer.parseInt(result) ==0 && check==true){
							percProfitAtClosePrice = Float.parseFloat(executeCountQuery(con, "select (open-low)*100/open from "+name+ " where tradedate > '"+res.split("_")[1]+"'"
									+ " order by tradedate limit 1"));
							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
				  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", '"+res.split("_")[1]+"')";
							executeSqlQuery(con, sql);
						}
						
					}
				}
				
				/*float gap = ((float)open.get(i)-(float)high.get(i-1))*100/(float)high.get(i-1);
				if((long)volume.get(i) >  (long)volume.get(i-1)*10){
					if(gap>2){
						percProfitAtClosePrice = ((float)high.get(i+1)-(float)open.get(i+1))*100/(float)open.get(i+1);
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", '"+tradedate.get(i+1)+"')";
						executeSqlQuery(con, sql);
					}
					gap = ((float)low.get(i-1)-(float)open.get(i))*100/(float)open.get(i);
					if(gap>2){
						percProfitAtClosePrice = ((float)open.get(i+1)-(float)low.get(i+1))*100/(float)open.get(i+1);
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bear', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", '"+tradedate.get(i+1)+"')";
						executeSqlQuery(con, sql);
					}
				}*/
				
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public boolean checkForResistanceSupport(java.sql.Connection con, String name, String date, float high, String result){
		int days = Integer.parseInt(result.split("_")[0]);
		String prevDayWhenCrossed = result.split("_")[1];
		String prevHighWhenCrossed = result.split("_")[2];
		String res="", sql="";boolean check=true;
		sql = "select concat(DATEdiff('"+date+"',tradedate),'_', tradedate,'_',high) from "+name+" where tradedate<'"+date+"' "
				+ " and high> "+high+" order by tradedate desc limit 1";
		res = executeCountQuery(con,sql);
//		System.out.println(sql);
		if(res.equals("")) return true;
		if(Integer.parseInt(res.split("_")[0]) > days/4){
			return true;
		}
		return false;
	}
   public static void main(String[] args)  {
	      Test t = new Test();
	      PastCross pin = new PastCross();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 100000000 and todaysopen>6 order by volume desc");
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
