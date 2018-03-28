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

public class BolingerBWSqueeze extends Connection{

	   public static void main(String[] args)  {
		      Test t = new Test();
		      BolingerBWSqueeze range = new BolingerBWSqueeze();
		      java.sql.Connection dbConnection = null;
		      boolean updateSymbolsTableData=true; boolean updateAllData=true;
		      try{
		    	  
		    	  Connection con = new Connection();
		    	  	dbConnection = con.getDbConnection();
		    	  	ResultSet rs=null;
		    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume*lastPrice > 30000000000 "
		    	  			+ " and s.name not like '%-%' and s.name not like '%&%' and s.name not like '%granules%' order by volume desc");
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
		    	  		range.getdate(dbConnection, name);
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
	   
	   public void getdate(java.sql.Connection con, String name) throws SQLException{
		  	UpdatePinResultsRange2(con, name);
	   }
	   
	   
	   public void UpdatePinResultsRange2(java.sql.Connection con, String name) {
		   ResultSet rs=null;
		  	String sql="";String interval="60";
		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	List date = new ArrayList<String>();
		  	List bw = new ArrayList<Float>();
		  	try {
		  		if(!interval.equals("1d")){
		  			sql="select * from "+name+"_"+interval+" where tradedate > '2017-04-01'";
		  		}
		  		else{
		  			sql="select * from "+name+" where tradedate > '2016-01-01'";
		  		}
		  		
				rs = executeSelectSqlQuery(con, sql);
			  	while (rs.next()){
			  		open.add(rs.getFloat("open"));
			  		high.add(rs.getFloat("high"));
			  		low.add(rs.getFloat("low"));
			  		close.add(rs.getFloat("close"));
			  		date.add(rs.getString("tradedate"));
			  		bw.add(rs.getFloat("bollingerBw"));
			  	}
			  	float highDay=0f, lowday=100000f;
			  	String tableName = "williamsresults";int count=0;
			  	
				 float profitPerc=0f;float range=0f;
			  		for (int i=15; i< date.size()-1; i++){
//			  			if((float)bw.get(i) > belowWhat && (float)bw.get(i-1) < belowWhat)
			  			{
//			  				for (int j=i-10; j< i-1; j++){
//			  					float rangePerc = Math.abs((float)bw.get(j)-(float)bw.get(j+1))*100/(float)bw.get(j);
//			  					if((float)bw.get(j) < belowWhat && rangePerc<10){
//			  						count++;
//			  					}
//			  				}
			  			}
//		  				if(count > 5)
			  			float rangePerc = Math.abs((float)bw.get(i)-(float)bw.get(i+1))*100/(float)bw.get(i);
			  			float rangePerc2 = Math.abs((float)bw.get(i)-(float)bw.get(i-1))*100/(float)bw.get(i);
			  			float rangePerc3 = Math.abs((float)bw.get(i)-(float)bw.get(i-2))*100/(float)bw.get(i);
			  			float rangePerc4 = Math.abs((float)bw.get(i)-(float)bw.get(i-3))*100/(float)bw.get(i);
			  			float rangePerc5 = Math.abs((float)bw.get(i)-(float)bw.get(i-4))*100/(float)bw.get(i);
			  			float rangePerc6 = Math.abs((float)bw.get(i)-(float)bw.get(i-5))*100/(float)bw.get(i);
			  			float rangePerc7 = Math.abs((float)bw.get(i)-(float)bw.get(i-6))*100/(float)bw.get(i);
			  			float rangePerc8 = Math.abs((float)bw.get(i)-(float)bw.get(i-7))*100/(float)bw.get(i);
			  			float rangePerc9 = Math.abs((float)bw.get(i)-(float)bw.get(i-8))*100/(float)bw.get(i);
			  			float rangePerc10 = Math.abs((float)bw.get(i)-(float)bw.get(i-9))*100/(float)bw.get(i);
			  			float rangePerc11 = Math.abs((float)bw.get(i)-(float)bw.get(i-10))*100/(float)bw.get(i);
			  			float rangePerc12 = Math.abs((float)bw.get(i)-(float)bw.get(i-11))*100/(float)bw.get(i);
			  			float rangePerc13 = Math.abs((float)bw.get(i)-(float)bw.get(i-12))*100/(float)bw.get(i);
			  			float rangePerc14 = Math.abs((float)bw.get(i)-(float)bw.get(i-13))*100/(float)bw.get(i);
			  			float rangePerc15 = Math.abs((float)bw.get(i)-(float)bw.get(i-14))*100/(float)bw.get(i);
//			  			if((float)bw.get(i) <belowWhat && rangePerc <6)
			  			float perc=3f;
			  			float belowWhat=3f;
			  			if(rangePerc <perc && rangePerc2 <perc && rangePerc3 <perc && rangePerc4 <perc
			  					&& rangePerc5 <perc && rangePerc6 <perc  && rangePerc7 <perc && rangePerc8 <perc &&
			  					rangePerc9 <perc && rangePerc10 <perc  && rangePerc11 <perc && rangePerc12 <perc && 
			  					(float)bw.get(i) <belowWhat)
		  				{
		  					count=0;
			  				sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
	             					+ " values ('"+name+"', 'Bull', 0, 0, "+profitPerc+", '"+date.get(i)+"', '"+date.get(i)+"', '1')";
	              			executeSqlQuery(con, sql);
//	              			break;
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
		  	finally{
		  		open.clear();high.clear();low.clear();close.clear();date.clear();
		  	}
	   }
}
