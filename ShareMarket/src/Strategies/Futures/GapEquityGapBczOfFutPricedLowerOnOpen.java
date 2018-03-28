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

public class GapEquityGapBczOfFutPricedLowerOnOpen extends Connection{
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
			rs = executeSelectSqlQuery(con, "select * from "+name+" a");
			while(rs.next()){
				tradedate.add(rs.getString("a.tradedate"));

				open.add(rs.getFloat("a.open"));
		  		high.add(rs.getFloat("a.high"));
		  		low.add(rs.getFloat("a.low"));
		  		close.add(rs.getFloat("a.close"));

//				tradedateF.add(rs.getString("b.tradedate"));
//				openF.add(rs.getFloat("b.open"));
//		  		highF.add(rs.getFloat("b.high"));
//		  		lowF.add(rs.getFloat("b.low"));
//		  		closeF.add(rs.getFloat("b.close"));
			}
			float trig=0f, prev1,prev2,prev3,prev4, width;float gapPerc=1f, div=3;
			float targetPerc=0.2f;int check=0;
			float trigger=0f, profitF=0f;int index=0;
			for(int i=1; i< tradedate.size(); i++){
				index=0;
				if(((float)open.get(i)-(float)high.get(i-1))*100/(float)open.get(i) > 3
						&& ((float)close.get(i)-(float)open.get(i))*100/(float)open.get(i) > 1){
					for(int j=i+1; j< tradedate.size(); j++){
						if((float)low.get(j) < (float)low.get(i) && index==0){
							index=j;
						}
						float range=((float)open.get(j)-(float)close.get(j))*100/(float)close.get(j);
						if((float)low.get(j) < (float)low.get(i)  && range>1 && (float)close.get(j) > (float)low.get(i) 
								&& j==index){
//							profitF = ((float)low.get(i)-Math.min((float)low.get(j), (float)low.get(j+1)))*100/(float)low.get(i);
							profitF = ((float)high.get(j+1)-(float)open.get(j+1))*100/(float)open.get(j+1);
							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
				  					+ " values ('"+name+"', 'Bull', "+profitF+", "+profitF+", "+profitF+", '"+tradedate.get(j)+"')";
							executeSqlQuery(con, sql);		
							break;
						}
					}
				}
//				if(((float)low.get(i-1)-(float)open.get(i))*100/(float)open.get(i) > 3
//						&& ((float)open.get(i)-(float)close.get(i))*100/(float)open.get(i) > 1){
//					for(int j=i+1; j< tradedate.size(); j++){
//						if((float)high.get(j) > (float)high.get(i) && index==0){
//							index=j;
//						}
//						float gapWithOpen=((float)high.get(i)-(float)open.get(j))*100/(float)open.get(j);
//						if((float)high.get(j) > (float)high.get(i) && gapWithOpen>0
//								&& j==index){
//							profitF = (Math.max((float)high.get(j), (float)high.get(j+1))-(float)high.get(i))*100/(float)high.get(i);
//							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
//				  					+ " values ('"+name+"', 'Bull', "+profitF+", "+profitF+", "+profitF+", '"+tradedate.get(j)+"')";
//							executeSqlQuery(con, sql);		
//							break;
//						}
//					}				
//				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      GapEquityGapBczOfFutPricedLowerOnOpen pin = new GapEquityGapBczOfFutPricedLowerOnOpen();
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
