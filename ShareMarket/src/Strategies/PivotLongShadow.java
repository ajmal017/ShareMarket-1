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

public class PivotLongShadow extends Connection{

	
	public void calculateResistanceSupport(java.sql.Connection con, String  name) throws SQLException{
		ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List pivot = new ArrayList<Float>();
	  	List r1 = new ArrayList<Float>();List s1 = new ArrayList<Float>();
	  	List r2 = new ArrayList<Float>();List s2 = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	List volume = new ArrayList<Long>();
	  	int interval=15;String sql="";
	  	float trig=0f, profitPerc=0f, width=0f;
	  	float target=0.2f;
	  	rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, tradedate,volume, pivot, R1, S1, R2, S2 FROM "+name+"_"+interval+" order by tradedate;");
	  	while (rs.next()){
	  		open.add(rs.getFloat("open"));
	  		high.add(rs.getFloat("high"));
	  		low.add(rs.getFloat("low"));
	  		close.add(rs.getFloat("close"));
	  		pivot.add(rs.getFloat("pivot"));
	  		r1.add(rs.getFloat("R1"));
	  		s1.add(rs.getFloat("S1"));
	  		r2.add(rs.getFloat("R2"));
	  		s2.add(rs.getFloat("S2"));
	  		date.add(rs.getString("tradedate"));
	  		volume.add(rs.getLong("volume"));
	  	}
	  	float upperGap=0f, lowerGap=0f;
	  	float resist = 0f, sup=0f;
	  	for(int i=0; i<date.size(); i++){
	  		if(i==0) continue;
	  		resist = (float) r1.get(i);
	  		sup = (float) s1.get(i);
	  		upperGap = ((float)high.get(i) - resist)*100/resist;
	  		lowerGap = (resist - (float)close.get(i))*100/(float)close.get(i);
	  		if(upperGap > .1 && lowerGap > .5 && (float)high.get(i) > resist && (float)close.get(i-1) < resist && (Long)volume.get(i)> (Long)volume.get(i-1)*1.9){
	  			sql = "select close from "+name+" where date(tradedate)=Date('"+date.get(i)+"') ";
	  			float daysClose = Float.parseFloat(executeCountQuery(con, sql));
	  			sql = "select coalesce(max(high),0) from "+name+"_"+interval+" where  tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:00:00')";
	  			float daysHigh = Float.parseFloat(executeCountQuery(con, sql));
	  			profitPerc = (daysHigh - (float)open.get(i+1))*100/(float)open.get(i+1);
	  			if(profitPerc>2){
	  				profitPerc =2;
	  			}else profitPerc = (daysClose - (float)open.get(i+1))*100/(float)open.get(i+1);
	  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
	  					+ " values ('"+name+"', 'Bull', "+(float)open.get(i+1)+", "+(profitPerc-0.05f)+", "+daysHigh+", '"+date.get(i)+"')";
	  			if(daysHigh!=0)
				executeSqlQuery(con, sql);
				break;
	  		}
	  		
	  		upperGap = ((float)close.get(i) - sup)*100/sup;
	  		lowerGap = (sup - (float)low.get(i))*100/(float)low.get(i);
	  		if(upperGap > .1 && lowerGap > .2 && (float)low.get(i) < sup && (float)close.get(i-1) > sup && (Long)volume.get(i)> (Long)volume.get(i-1)*1.9){
	  			sql = "select close from "+name+" where date(tradedate)=Date('"+date.get(i)+"') ";
	  			float daysClose = Float.parseFloat(executeCountQuery(con, sql));
	  			sql = "select coalesce(min(low),0) from "+name+"_"+interval+" where  tradedate >'"+date.get(i)+"' and tradedate <= concat(Date('"+date.get(i)+"'),' 15:00:00')";
	  			float daysLow = Float.parseFloat(executeCountQuery(con, sql));
	  			profitPerc = ((float)open.get(i+1) - daysLow)*100/(float)open.get(i+1);
	  			
	  			if(profitPerc>2){
	  				profitPerc =2;
	  			}else profitPerc = ((float)open.get(i+1) - daysClose)*100/(float)open.get(i+1);
	  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
	  					+ " values ('"+name+"', 'Bear', "+(float)open.get(i+1)+", "+(profitPerc-0.05f)+", "+daysLow+", '"+date.get(i)+"')";
	  			if(daysLow!=0)
				executeSqlQuery(con, sql);
				break;
	  		}
	  	}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      PivotLongShadow pin = new PivotLongShadow();
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
	    	  		pin.calculateResistanceSupport(dbConnection, name);
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
