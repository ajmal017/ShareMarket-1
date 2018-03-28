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

public class YestCrossWithMoreVolume extends Connection{

	public void getData( java.sql.Connection con, String name){
		 ResultSet rs=null;
		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	List volume = new ArrayList<Long>();
		  	List date = new ArrayList<String>();
		  	try {
				rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, totalqty , tradedate FROM "+name+"  order by tradedate;");
			  	while (rs.next()){
			  		open.add(rs.getFloat("open"));
			  		high.add(rs.getFloat("high"));
			  		low.add(rs.getFloat("low"));
			  		close.add(rs.getFloat("close"));
			  		volume.add(rs.getLong("totalqty"));
			  		date.add(rs.getString("tradedate"));
			  	}
			  	for(int x=0; x<date.size()-1; x++){
			  		if((float)open.get(x+1) < (float)high.get(x) && (float)open.get(x+1) > (float)low.get(x))
			  			calculateYestCross(con, name, (Long)volume.get(x), (float)high.get(x), (float)low.get(x), (float)high.get(x+1), (float)low.get(x+1), (float)close.get(x+1), date.get(x+1).toString());
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
	public void calculateYestCross(java.sql.Connection con, String  name, long prevVolume, float prevHigh, float prevLow,float nextHigh,float nextLow,float nextClose, String nextDate) throws SQLException{
		ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List pivot = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	int interval=60;String sql="";
	  	float trig=0f, profitPerc=0f;
	  	rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, tradedate, pivot FROM "+name+"_"+interval+" where date(tradedate)=Date('"+nextDate+"')  order by tradedate;");
	  	while (rs.next()){
	  		open.add(rs.getFloat("open"));
	  		high.add(rs.getFloat("high"));
	  		low.add(rs.getFloat("low"));
	  		close.add(rs.getFloat("close"));
	  		pivot.add(rs.getFloat("pivot"));
	  		date.add(rs.getString("tradedate"));
	  	}
	  	float daysOpen=0;
	  	float daysHigh,daysLow;
	  	for(int i=0; i<date.size(); i++){
	  		if(i<1) continue;
	  		sql="select open from "+name+" where date(tradedate)=Date('"+nextDate+"') ";
	  		daysOpen = Float.parseFloat(executeCountQuery(con, sql));
	  		float widthPerc = (daysOpen - prevLow)*100/(prevHigh - prevLow);
	  		if((float)high.get(i) > (float)prevHigh && (float)high.get(i-1) < (float)prevHigh ){
	  			sql="select high from "+name+" where date(tradedate)=Date('"+nextDate+"') ";
		  		daysHigh = Float.parseFloat(executeCountQuery(con, sql));
	  			profitPerc = (nextClose - prevHigh)*100/prevHigh;
	  			profitPerc = (daysHigh - prevHigh)*100/prevHigh;
	  			if(profitPerc>2.5){
	  				profitPerc=2.5f;
	  			}else profitPerc = (nextClose - prevHigh)*100/prevHigh;
	  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
	  					+ " values ('"+name+"', 'Bull', "+prevHigh+", "+(profitPerc-.05f)+", "+nextClose+", '"+date.get(i)+"')";
				executeSqlQuery(con, sql);
				break;
	  		}
	  		if((float)low.get(i) < (float)prevLow && (float)low.get(i-1) > (float)prevLow){
	  			sql="select low from "+name+" where date(tradedate)=Date('"+nextDate+"') ";
		  		daysLow = Float.parseFloat(executeCountQuery(con, sql));
	  			profitPerc = (prevLow-nextClose)*100/prevLow;
	  			profitPerc = (prevLow-daysLow)*100/prevLow;
	  			if(profitPerc>2.5){
	  				profitPerc=2.5f;
	  			}else profitPerc = (prevLow-nextClose)*100/prevLow;
	  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
	  					+ " values ('"+name+"', 'Bear', "+prevLow+", "+(profitPerc-.05f)+", "+nextClose+", '"+date.get(i)+"')";
				executeSqlQuery(con, sql);
				break;
	  		}
	  	}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      YestCrossWithMoreVolume pin = new YestCrossWithMoreVolume();
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
