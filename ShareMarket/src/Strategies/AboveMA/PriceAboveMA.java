package Strategies.AboveMA;

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

public class PriceAboveMA extends Connection{
	public void getData( java.sql.Connection con, String name){
		ResultSet rs=null;
		float percentage=80f;
		
	  	List tradedate = new ArrayList<String>();
	  	List perc = new ArrayList<Float>();
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
	  	List max_high = new ArrayList<Float>();
	  	List min_low = new ArrayList<Float>();
	  	List ema2 = new ArrayList<Float>();
		String date="",sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			rs = executeSelectSqlQuery(con, "select * from "+name+" ");
			while(rs.next()){
				tradedate.add(rs.getString("tradedate"));
				open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		ema2.add(rs.getFloat("ema2"));
			}
			float trig=0f;String tradedDate="", exitDate="";
			int count = 0;
			for(int i=0; i< tradedate.size()-1; i++){
				float target = (float)ema2.get(i)*1/100;tradedDate=""; exitDate="";
				target=0;
				if((float)low.get(i) > ((float)ema2.get(i)+target) && (float)ema2.get(i) !=0){
					count++;
				}else count=0;
				boolean entered = false;
				float diff = ((float)low.get(i)-(float)ema2.get(i))*100/(float)ema2.get(i);
				if(count > 10){
					for(int k=i-10; k<= i; k++){
						if(((float)low.get(k)-(float)ema2.get(k-1))*100/(float)ema2.get(k-1) > 15){
							entered = true;break;
						}
					}
					if(entered == false) continue;
					for(int j=i; j< i+10; j++){
						int exitAfter=10;
						if((float)low.get(j)<(float)ema2.get(j-1) && ((float)open.get(j) > ((float)open.get(j-1) - (float)open.get(j-1)*20/100))){
							percProfitAtClosePrice = ((float)close.get(j+exitAfter) - (float)ema2.get(j-1))*100/(float)ema2.get(j-1);
//							percProfitAtClosePrice = ((float)close.get(j+1) - (float)open.get(j+1))*100/(float)open.get(j+1);
							if(((float)close.get(j+exitAfter) > ((float)ema2.get(j-1) - (float)ema2.get(j-1)*20/100))){
								tradedDate = tradedate.get(j).toString();
								exitDate = tradedate.get(j+exitAfter).toString();
							}
							break;
						}
					}
					//if(entered) i=i+10;
//					for(int j=i; j< tradedate.size()-1; j++){
//						if((float)low.get(j) < (float)ema2.get(j)){
//							percProfitAtClosePrice = ((float)ema2.get(j) - (float)open.get(i))*100/(float)open.get(i);
//							sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited) "
//				  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "
//				  							+ ""+percProfitAtHighPrice+", '"+tradedate.get(i)+"', '"+tradedate.get(j)+"')";
//							executeSqlQuery(con, sql);
//							break;
//						}
//					}
					count=0;
//					percProfitAtClosePrice = ((float)close.get(i+50) - (float)open.get(i))*100/(float)open.get(i);
					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited) "
		  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "
		  							+ ""+percProfitAtHighPrice+", '"+tradedDate+"', '"+exitDate+"')";
//					percProfitAtClosePrice = ((float)close.get(i) - (float)open.get(i))*100/(float)open.get(i);
//					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited) "
//		  					+ " values ('"+name+"', 'Bull', "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", "
//		  							+ ""+percProfitAtHighPrice+", '"+tradedate.get(i)+"', '"+tradedate.get(i)+"')";
					if(tradedDate!="" && exitDate!="")
					executeSqlQuery(con, sql);
					
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      PriceAboveMA pin = new PriceAboveMA();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 100000000 order by volume desc");
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
