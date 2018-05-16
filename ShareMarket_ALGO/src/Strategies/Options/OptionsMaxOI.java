package Strategies.Options;

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

public class OptionsMaxOI extends Connection{
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
	  	List type = new ArrayList<String>();
	  	List strike = new ArrayList<Float>();
	  	List r2 = new ArrayList<Float>();
	  	List s1 = new ArrayList<Float>();
	  	List s2 = new ArrayList<Float>();
	  	List max_high = new ArrayList<Float>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			sql="select tradedate,strike_price, open_interest,type from "+name+"_OPT s inner join " +
					" (select max(open_interest*1) as id,tradedate d from "+name+"_OPT group by (date(tradedate))) maxi "+
					" on s.open_interest*1=maxi.id*1 and s.tradedate=maxi.d order by s.tradedate ";
//			System.out.println(sql);
			rs = executeSelectSqlQuery(con, sql);
			while(rs.next()){
				tradedate.add(rs.getString("tradedate"));
				strike.add(rs.getFloat("strike_price"));
				type.add(rs.getString("type"));
			}
			float trig=0f, prev1,prev2,prev3,prev4, width;float gapPerc=1f, div=3;
			for(int i=5; i< tradedate.size(); i++){
				if(type.get(i).toString().equals("CE")){
					if((float)strike.get(i)==(float)strike.get(i-1) && (float)strike.get(i)==(float)strike.get(i-2)
							&& (float)strike.get(i)==(float)strike.get(i-3) && (float)strike.get(i)==(float)strike.get(i-4)
									&& (float)strike.get(i)==(float)strike.get(i-5)
							){
						trig = (float) ((float)strike.get(i)-((float)strike.get(i)*0.2/100));
						sql = "select (high-"+trig+")*100/"+trig+" from  "+name+" where tradedate='"+tradedate.get(i)+"'";
						float diffToApproach = Float.parseFloat(executeCountQuery(con, sql));
						sql="select max(high) from "+name+" where tradedate > date_add('"+tradedate.get(i)+"', interval -3 day) and tradedate <= '"+tradedate.get(i)+"'";
						float maxHigh=Float.parseFloat(executeCountQuery(con, sql));
						if(maxHigh< (float)trig){
//							sql = "select datediff(tradedate, '"+tradedate.get(i)+"') from "+name+" where high > "+trig+" and "
//									+ " open < "+trig+" and tradedate > '"+tradedate.get(i)+"' limit 1";
//							String diff = executeCountQuery(con, sql);
////							diff="0";
//							sql = "select (high-"+trig+")*100/"+trig+" from "+name+" where high > "+trig+" and "
//									+ " open < "+trig+" and tradedate > '"+tradedate.get(i)+"' limit 1";
//							String profit = executeCountQuery(con, sql);
//							
//							sql = "select tradedate from "+name+" where high > "+trig+" and "
//									+ " open < "+trig+" and tradedate > '"+tradedate.get(i)+"' limit 1";
//							String crossedDate = executeCountQuery(con, sql);
							
//							if(Float.parseFloat(profit)<0.5){
//								sql = "select (high-"+trig+")*100/"+trig+" from "+name+" where tradedate = '"+crossedDate+"'";
//								profit = executeCountQuery(con, sql);
//							}
							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
				  					+ " values ('"+name+"', 'Bull', "+strike.get(i)+", "+strike.get(i)+", "+strike.get(i)+", '"+tradedate.get(i)+"')";
							executeSqlQuery(con, sql);
						}
						
					}
				}
				if(type.get(i).toString().equals("PE")){
					if((float)strike.get(i)==(float)strike.get(i-1) && (float)strike.get(i)==(float)strike.get(i-2)
							&& (float)strike.get(i)==(float)strike.get(i-3) && (float)strike.get(i)==(float)strike.get(i-4)
									&& (float)strike.get(i)==(float)strike.get(i-5)
							){
						trig = (float) ((float)strike.get(i)-((float)strike.get(i)*0.2/100));
						sql = "select ("+trig+"-low)*100/"+trig+" from  "+name+" where tradedate='"+tradedate.get(i)+"'";
						float diffToApproach = Float.parseFloat(executeCountQuery(con, sql));
						sql="select min(low) from "+name+" where tradedate > date_add('"+tradedate.get(i)+"', interval -3 day) and tradedate <= '"+tradedate.get(i)+"'";
						float minLow=Float.parseFloat(executeCountQuery(con, sql));
//						if(minLow< (float)trig)
						{
//							sql = "select datediff(tradedate, '"+tradedate.get(i)+"') from "+name+" where high > "+trig+" and "
//									+ " open < "+trig+" and tradedate > '"+tradedate.get(i)+"' limit 1";
//							String diff = executeCountQuery(con, sql);
////							diff="0";
//							sql = "select (high-"+trig+")*100/"+trig+" from "+name+" where high > "+trig+" and "
//									+ " open < "+trig+" and tradedate > '"+tradedate.get(i)+"' limit 1";
//							String profit = executeCountQuery(con, sql);
//							
//							sql = "select tradedate from "+name+" where high > "+trig+" and "
//									+ " open < "+trig+" and tradedate > '"+tradedate.get(i)+"' limit 1";
//							String crossedDate = executeCountQuery(con, sql);
							
//							if(Float.parseFloat(profit)<0.5){
//								sql = "select (high-"+trig+")*100/"+trig+" from "+name+" where tradedate = '"+crossedDate+"'";
//								profit = executeCountQuery(con, sql);
//							}
							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
				  					+ " values ('"+name+"', 'Bear', "+strike.get(i)+", "+strike.get(i)+", "+strike.get(i)+", '"+tradedate.get(i)+"')";
							executeSqlQuery(con, sql);
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
	      OptionsMaxOI pin = new OptionsMaxOI();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	String sql="";
	    	  	sql =  "SELECT s.name FROM symbols s where volume > 100000000 and todaysopen>6 order by volume desc";
	    	  	sql =  "SELECT s.name FROM symbols s, margintables m where s.name=m.name and s.name<>'TV18BRDCST'";
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
