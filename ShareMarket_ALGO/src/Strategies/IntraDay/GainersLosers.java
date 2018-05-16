package Strategies.IntraDay;

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

public class GainersLosers extends Connection{

	java.sql.Connection dbConnection = null;
	public GainersLosers() {
		Connection con = new Connection();
	  	dbConnection = con.getDbConnection();
	}
	   public static void main(String[] args)  {
		      Test t = new Test();
		      GainersLosers range = new GainersLosers();
		      
		      boolean updateSymbolsTableData=true; boolean updateAllData=true;
		      try{
		    	  
		    	    Connection con = new Connection();
		    	  	ResultSet rs=null;
		    	  	String sql = "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 100000 "
		    	  			+ "and s.name<>'Mindtree' and s.name<>'IOC' and s.name<>'GRANULES' order by volume desc ";
//		    	  	sql = "SELECT s.name FROM symbols s where volume > 10000000 order by volume desc ";
		    	  	rs = con.executeSelectSqlQuery(range.dbConnection, sql);
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
		    	  		range.getdate(range.dbConnection, name);
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
		      }
	   }
	   
	   public void getdate(java.sql.Connection con, String name) throws SQLException, InterruptedException{
		   String sql="Select * from "+name+" where tradedate> '2016-02-01'";
		   ResultSet rs=null;
		   rs = executeSelectSqlQuery(con, sql);

		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	List date = new ArrayList<String>();
		  	List volume = new ArrayList<Integer>();
		   while (rs.next()){
			   open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getString("tradedate"));
		   }
		   float gap=0.5f;
		   for(int i=1; i< date.size(); i++){
			   UpdatePinResultsRange2(con, name, date.get(i).toString(), (float)close.get(i-1),60);
		   }
	   }
	   
	   
	   public void UpdatePinResultsRange2(java.sql.Connection con, String name,String dailyDate, float prevClose, int interval){
		   ResultSet rs=null;
		  	String sql="";
		  	List<Float> ema2 = new ArrayList<>();
		  	try {
		  		float diff=0f;
				List open = new ArrayList<Float>();
			  	List high = new ArrayList<Float>();
			  	List low = new ArrayList<Float>();
			  	List close = new ArrayList<Float>();
			  	List date = new ArrayList<String>();
			  	sql="Select * from "+name+"_60 where date(tradedate) = date('"+dailyDate+"')";
				rs = executeSelectSqlQuery(con, sql);
				while(rs.next()){
					open.add(rs.getFloat("open"));
			  		high.add(rs.getFloat("high"));
			  		low.add(rs.getFloat("low"));
			  		close.add(rs.getFloat("close"));
			  		date.add(rs.getString("tradedate"));
				}
				float intraLow=100000f, intraHigh=0f;
				for(int i=0; i< date.size()-1; i++){
					intraLow = Math.min(intraLow, (float)low.get(i));
					intraHigh = Math.max(intraHigh, (float)high.get(i));
					if(i==1)
						diff = ((float)close.get(i) - prevClose)*100/prevClose;
					else diff=0;
					float trig = intraLow+(intraHigh-intraLow)/4;
					float profit = ((float)close.get(date.size()-1) - trig)*100/trig;
					if(diff > 2 ){
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+name+"', 'Bull', "+profit+", "+trig+", "+diff+", '"+dailyDate+"')";
						executeSqlQuery(con, sql);
						break;
					}
				}
		  	}
		  	catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  	catch(Exception e){
		    	  e.printStackTrace();
		    }
	   }
}
