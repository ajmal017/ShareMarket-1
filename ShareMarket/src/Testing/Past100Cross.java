package Testing;

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

public class Past100Cross extends Connection{

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
				rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, totalqty , avg_volume, tradedate,cci, max_past_high, min_past_low FROM "+name+"  order by tradedate desc limit 1;");
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
			  	float profitPerc=0f, target=2;
			  	String sql="";
			  	
			  	for(int x= date.size()-1; x== date.size()-1; x++){
			  		float upPerc=((float)max_high.get(x)-(float)high.get(x))*100/(float)high.get(x);
			  		float downPerc=((float)low.get(x)-(float)min_low.get(x))*100/(float)low.get(x);
			  		if(upPerc>0 ){
			  			sql = "insert into Past100DaysCross(name, date, approach, dir, lastHigh, triggerPrice) "
			  					+ " values ('"+name+"', '"+date.get(x)+"', "+(upPerc)+", 'Upward', "+(float)high.get(x)+", "+(float)max_high.get(x)+")";
						executeSqlQuery(con, sql);
			  		}
			  		
			  		if(downPerc>0){
			  			sql = "insert into Past100DaysCross(name, date, approach, dir, lastLow, triggerPrice) "
			  					+ " values ('"+name+"', '"+date.get(x)+"', "+(downPerc)+", 'Downward', "+(float)low.get(x)+", "+(float)min_low.get(x)+")";
						executeSqlQuery(con, sql);
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
	      Past100Cross pin = new Past100Cross();
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
//	    	  	String sql ="CREATE TABLE Past100DaysCross "
//            			+ "(date datetime NOT NULL, name varchar(30) default '', approach float default 0, "
//            			+ "dir varchar(10) default '', lastHigh float default 0,lastLow float default 0, triggerPrice float default 0) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
//            	System.out.println(sql);
//	    	  	sql="drop table Past100DaysCross";
//            	con.executeSqlQuery(dbConnection, sql);
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("s.name");
//	    	  		System.out.println(name);
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
