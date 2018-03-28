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

public class YestCrossOnDailyDataOnWilliams extends Connection{

	public void getData( java.sql.Connection con, String name){
		 ResultSet rs=null;
		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	List will_h = new ArrayList<Float>();
		  	List will_l = new ArrayList<Float>();
		  	List cci = new ArrayList<Float>();
		  	List volume = new ArrayList<Long>();
		  	List avgVolume = new ArrayList<Long>();
		  	List date = new ArrayList<String>();
		  	try {
				rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, totalqty , avg_volume, tradedate,cci, will_high, will_low FROM "+name+"  order by tradedate;");
			  	while (rs.next()){
			  		open.add(rs.getFloat("open"));
			  		high.add(rs.getFloat("high"));
			  		low.add(rs.getFloat("low"));
			  		close.add(rs.getFloat("close"));
			  		will_h.add(rs.getFloat("will_high"));
			  		will_l.add(rs.getFloat("will_low"));
			  		cci.add(rs.getFloat("cci"));
			  		volume.add(rs.getLong("totalqty"));
			  		avgVolume.add(rs.getLong("avg_volume"));
			  		date.add(rs.getString("tradedate"));
			  	}
			  	float profitPerc=0f;
			  	String sql="";
			  	for(int x=0; x<date.size()-1; x++){
			  		if(x<6) continue;
			  		
			  		if((float)open.get(x+1) < (float)will_h.get(x) && (float)open.get(x+1) > (float)will_l.get(x)){
			  			float widthPerc = ((float)open.get(x+1) - (float)low.get(x))*100/((float)high.get(x) - (float)low.get(x));
				  		if( (float)high.get(x+1) > (float)will_h.get(x) && (float)cci.get(x) <100 && (float)cci.get(x) >50 && (long)volume.get(x) > (long)avgVolume.get(x)){
//				  			profitPerc = ((float)high.get(x+1) - (float)will_h.get(x))*100/(float)will_h.get(x);
				  			profitPerc = ((float)close.get(x+1) - (float)will_h.get(x))*100/(float)will_h.get(x);
//				  			if((float)low.get(x+1) < (float)low.get(x)){
//				  				profitPerc = ((float)low.get(x) - (float)will_h.get(x))*100/(float)will_h.get(x);
//				  			}
//				  			profitPerc = ((float)high.get(x+1) - (float)high.get(x))*100/(float)high.get(x);
//				  			if(profitPerc>3){
//				  				profitPerc=3f;
//				  			}else profitPerc = ((float)close.get(x+1) - (float)will_h.get(x))*100/(float)will_h.get(x);
				  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
				  					+ " values ('"+name+"', 'Bull', "+(float)high.get(x)+", "+(profitPerc-.05f)+", "+(float)high.get(x)+", '"+date.get(x+1)+"')";
							executeSqlQuery(con, sql);
				  		}
				  		if( (float)low.get(x+1) < (float)will_l.get(x) && (float)cci.get(x) >-100 && (float)cci.get(x) <-50 && (long)volume.get(x) > (long)avgVolume.get(x)){
//				  			profitPerc = ((float)will_l.get(x)-(float)low.get(x+1))*100/(float)(float)will_l.get(x);
				  			profitPerc = ((float)will_l.get(x)-(float)close.get(x+1))*100/(float)(float)will_l.get(x);
//				  			profitPerc = ((float)low.get(x)-(float)low.get(x+1))*100/(float)(float)low.get(x);
//				  			if(profitPerc>3){
//				  				profitPerc=3f;
//				  			}else profitPerc = ((float)will_l.get(x)-(float)close.get(x+1))*100/(float)(float)will_l.get(x);
//				  			if((float)high.get(x+1) > (float)high.get(x)){
//				  				profitPerc = ((float)will_l.get(x)-(float)high.get(x))*100/(float)(float)will_l.get(x);
//				  			}
				  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
				  					+ " values ('"+name+"', 'Bear', "+(float)low.get(x)+", "+(profitPerc-.05f)+", "+(float)low.get(x)+", '"+date.get(x+1)+"')";
							executeSqlQuery(con, sql);
				  		}
			  		}
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
	      YestCrossOnDailyDataOnWilliams pin = new YestCrossOnDailyDataOnWilliams();
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
