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

public class YestCrossPivot extends Connection{

	public void getData( java.sql.Connection con, String name){
		 ResultSet rs=null;
		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	List date = new ArrayList<String>();
		  	try {
				rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, tradedate FROM "+name+"  order by tradedate;");
			  	while (rs.next()){
			  		open.add(rs.getFloat("open"));
			  		high.add(rs.getFloat("high"));
			  		low.add(rs.getFloat("low"));
			  		close.add(rs.getFloat("close"));
			  		date.add(rs.getString("tradedate"));
			  	}
			  	for(int x=0; x<date.size()-1; x++){
			  		if((float)open.get(x+1) < (float)high.get(x) && (float)open.get(x+1) > (float)low.get(x))
			  			calculateYestCross(con, name, (float)high.get(x), (float)low.get(x), (float)high.get(x+1), (float)low.get(x+1), (float)close.get(x+1), date.get(x+1).toString());
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
	public void calculateYestCross(java.sql.Connection con, String  name, float prevHigh, float prevLow,float nextHigh,float nextLow,float nextClose, String nextDate) throws SQLException{
		ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List pivot = new ArrayList<Float>();List r1 = new ArrayList<Float>();List s1 = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	int interval=5;String sql="";
	  	float trig=0f, profitPerc=0f, width=0f;
	  	int iter=15;float target=0.2f;
	  	rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, tradedate, pivot, R1, S1 FROM "+name+"_"+interval+" where date(tradedate)=Date('"+nextDate+"')  order by tradedate;");
	  	while (rs.next()){
	  		open.add(rs.getFloat("open"));
	  		high.add(rs.getFloat("high"));
	  		low.add(rs.getFloat("low"));
	  		close.add(rs.getFloat("close"));
	  		pivot.add(rs.getFloat("pivot"));
	  		r1.add(rs.getFloat("R1"));
	  		s1.add(rs.getFloat("S1"));
	  		date.add(rs.getString("tradedate"));
	  	}
	  	
	  	for(int i=0; i<date.size(); i++){
	  		if(i==0) continue;
//	  		prevHigh < ((float)pivot.get(i)+(width/2)) && prevHigh>(float)pivot.get(i)
	  		width = ((float)r1.get(i) - (float)pivot.get(i));
	  		if((float)high.get(i) > (float)prevHigh && (float)high.get(i-1) < (float)prevHigh && prevHigh > (float)pivot.get(i) && prevHigh < (float)r1.get(i)){
	  			sql = "select high from "+name+"_"+iter+" where date(tradedate)=Date('"+date.get(i)+"') and high>"+prevHigh+" order by tradedate limit 1";
	  			float barHigh = Float.parseFloat(executeCountQuery(con, sql));
	  			sql = "select close from "+name+"_"+iter+" where date(tradedate)=Date('"+date.get(i)+"') and high>"+prevHigh+" order by tradedate limit 1";
	  			
	  			float barClose = Float.parseFloat(executeCountQuery(con, sql));
	  			profitPerc = (barHigh - prevHigh)*100/prevHigh;
	  			if(profitPerc>target) profitPerc=target;
	  			else profitPerc = (barClose - prevHigh)*100/prevHigh;
//	  			profitPerc = (prevHigh-nextClose)*100/prevHigh;
	  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
	  					+ " values ('"+name+"', 'Bull', "+prevHigh+", "+(profitPerc-0.05f)+", "+nextClose+", '"+date.get(i)+"')";
				executeSqlQuery(con, sql);
				break;
	  		}
	  		if((float)low.get(i) < (float)prevLow && (float)low.get(i-1) > (float)prevLow && prevLow < (float)pivot.get(i) && prevLow > (float)s1.get(i) ){
	  			sql = "select low from "+name+"_"+iter+" where date(tradedate)=Date('"+date.get(i)+"') and low<"+prevLow+" order by tradedate limit 1";
	  			float barLow = Float.parseFloat(executeCountQuery(con, sql));
	  			profitPerc = (prevLow-barLow)*100/prevLow;
	  			
	  			sql = "select close from "+name+"_"+iter+" where date(tradedate)=Date('"+date.get(i)+"') and low<"+prevLow+" order by tradedate limit 1";
	  			float barClose = Float.parseFloat(executeCountQuery(con, sql));
	  			if(profitPerc>target) profitPerc=target;
	  			else profitPerc = (prevLow-barClose)*100/prevLow;;
//	  			profitPerc = (nextClose-prevLow)*100/prevLow;
	  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
	  					+ " values ('"+name+"', 'Bear', "+prevLow+", "+(profitPerc-0.05f)+", "+nextClose+", '"+date.get(i)+"')";
				executeSqlQuery(con, sql);
				break;
	  		}
	  	}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      YestCrossPivot pin = new YestCrossPivot();
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
