package Strategies;

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

public class PastCrossOnDailyData_EarlyEntry extends Connection{

	public void getData( java.sql.Connection con, String name){
		 ResultSet rs=null;
		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	List max_high = new ArrayList<Float>();
		  	List min_low = new ArrayList<Float>();
		  	List cci = new ArrayList<Float>();
		  	List volume = new ArrayList<Long>();
		  	List avgVolume = new ArrayList<Long>();
		  	List date = new ArrayList<String>();
		  	try {
				rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, totalqty , avg_volume, tradedate,cci, max_past_high, min_past_low FROM "+name+"  order by tradedate;");
			  	while (rs.next()){
			  		open.add(rs.getFloat("open"));
			  		high.add(rs.getFloat("high"));
			  		low.add(rs.getFloat("low"));
			  		close.add(rs.getFloat("close"));
			  		max_high.add(rs.getFloat("max_past_high"));
			  		min_low.add(rs.getFloat("min_past_low"));
			  		cci.add(rs.getFloat("cci"));
			  		volume.add(rs.getLong("totalqty"));
			  		avgVolume.add(rs.getLong("avg_volume"));
			  		date.add(rs.getString("tradedate"));
			  	}
			  	float profitPerc=0f, target=2, triggerPrice=0f;
			  	String sql="";
			  	float diffToTrig=0f;
			  	for(int x=0; x<date.size()-1; x++){
			  		if(x<3) continue;
			  			float widthPerc = ((float)open.get(x+1) - (float)low.get(x))*100/((float)high.get(x) - (float)low.get(x));
			  			float diff = ((float)close.get(x) - (float)open.get(x+1))*100/(float)open.get(x+1);
			  			diffToTrig = ((float)max_high.get(x) - (float)low.get(x+1))*100/(float)low.get(x+1);
			  			triggerPrice = ((float)max_high.get(x) - ((float)max_high.get(x)*0.0f/100.00f));
				  		if( (float)high.get(x+1) > triggerPrice && diff<20 &&  (float)open.get(x+1) < triggerPrice && (float)max_high.get(x)!=0 && (float)cci.get(x)>100){
				  			profitPerc = ((float)high.get(x+1) - triggerPrice)*100/triggerPrice;
//				  			profitPerc = ((float)close.get(x+1) - (float)max_high.get(x))*100/(float)max_high.get(x);
//				  			profitPerc = ((float)close.get(x+1) - (float)open.get(x+1))*100/(float)open.get(x+1);
//				  			if(profitPerc>target){
//				  				profitPerc=target;
//				  			}else profitPerc = ((float)close.get(x+1) - (float)max_high.get(x))*100/(float)max_high.get(x);
				  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
				  					+ " values ('"+name+"', 'Bull', "+(float)max_high.get(x)+", "+(profitPerc-.05f)+", "+(float)high.get(x)+", '"+date.get(x+1)+"')";
							executeSqlQuery(con, sql);
				  		}
				  		triggerPrice = ((float)min_low.get(x) + ((float)min_low.get(x)*0.0f/100.00f));
				  		diffToTrig = ((float)high.get(x+1) - (float)min_low.get(x))*100/(float)high.get(x+1);
				  		if( (float)low.get(x+1) < triggerPrice  && diff<20 && (float)open.get(x+1) > triggerPrice && (float)min_low.get(x)!=0 && (float)cci.get(x)<-100){
				  			profitPerc = (triggerPrice-(float)low.get(x+1))*100/triggerPrice;
//				  			profitPerc = ((float)min_low.get(x)-(float)close.get(x+1))*100/(float)min_low.get(x);
//				  			profitPerc = ((float)open.get(x+1)-(float)close.get(x+1))*100/(float)open.get(x+1);
//				  			if(profitPerc>target){
//				  				profitPerc=target;
//				  			}else profitPerc = ((float)min_low.get(x)-(float)close.get(x+1))*100/(float)min_low.get(x);
				  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
				  					+ " values ('"+name+"', 'Bear', "+(float)min_low.get(x)+", "+(profitPerc-.05f)+", "+(float)low.get(x)+", '"+date.get(x+1)+"')";
							executeSqlQuery(con, sql);
				  		}
//			  		}
			  	}
			  	if(rs!=null) rs.close();
		  	}
		  	catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  	catch(Exception e){
		    	  e.printStackTrace();
		    }
	}


   public static void main(String[] args)  {
	      Test t = new Test();
	      PastCrossOnDailyData_EarlyEntry pin = new PastCrossOnDailyData_EarlyEntry();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 100000000 and s.name<>'Mindtree'  order by volume desc");
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
