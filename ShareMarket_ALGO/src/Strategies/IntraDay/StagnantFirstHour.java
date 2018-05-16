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

public class StagnantFirstHour extends Connection{

	   public static void main(String[] args)  {
		      Test t = new Test();
		      StagnantFirstHour range = new StagnantFirstHour();
		      java.sql.Connection dbConnection = null;
		      boolean updateSymbolsTableData=true; boolean updateAllData=true;
		      try{
		    	  
		    	  Connection con = new Connection();
		    	  	dbConnection = con.getDbConnection();
		    	  	ResultSet rs=null;
		    	  	String sql = "SELECT s.name FROM symbols s where volume > 100000000 and s.name not like '%&%'";
//		    	  	sql = "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 100000 and s.name<>'Mindtree' order by volume desc ";
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
		   String sql="Select * from "+name+" where tradedate>'2016-02-01'";
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
		   for(int i=0; i< date.size(); i++){

			   boolean isvalid = UpdatePinResultsRange2(con, name, date.get(i).toString());
			   if(isvalid==true){
				   if((float)high.get(i+1) > (float)high.get(i) && ((float)high.get(i)-(float)low.get(i))*100/(float)low.get(i) < 1){
					   float profitBullPerc= ((float)high.get(i+1) - (float)high.get(i))*100/(float)high.get(i);
					   sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
		    					+ " values ('"+name+"', 'Bull', 0, "+profitBullPerc+", "+profitBullPerc+", '"+date.get(i)+"', '"+date.get(i)+"', '1')";
					   executeSqlQuery(con, sql);
				   }
			   }
			   
		   }
		   rs.close();
	   }
	   
	   
	   public boolean UpdatePinResultsRange2(java.sql.Connection con, String name,String dailyDate){
		   ResultSet rs=null;
		  	String sql="";String interval="15";
		  	List<Float> ema2I = new ArrayList<>();
		  	List openI = new ArrayList<Float>();
		  	List highI = new ArrayList<Float>();
		  	List lowI = new ArrayList<Float>();
		  	List closeI = new ArrayList<Float>();
		  	List dateI = new ArrayList<String>();
		  	List volumeI = new ArrayList<Integer>();
		  	boolean isStagnant=false;
		  	try {
		  		if(!interval.equals("")){
		  			sql="select * from "+name+"_"+interval+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:00:00') and tradedate <= concat(Date('"+dailyDate+"'),' 15:00:00')";
		  		}else{
		  			sql="select * from "+name+" where tradedate >=concat(Date('"+dailyDate+" '),' 9:00:00') and tradedate <= concat(Date('"+dailyDate+"'),' 15:00:00')";
		  		}
		  		
				rs = executeSelectSqlQuery(con, sql);
			  	while (rs.next()){
			  		openI.add(rs.getFloat("open"));
			  		highI.add(rs.getFloat("high"));
			  		lowI.add(rs.getFloat("low"));
			  		closeI.add(rs.getFloat("close"));
			  		ema2I.add(rs.getFloat("ema2"));
			  		dateI.add(rs.getString("tradedate"));
			  		volumeI.add(rs.getInt("volume"));
			  	}
			  	float highDay=0f, lowday=100000f;
			  	String tableName = "williamsresults";int count=0;
			  	float dayslow, daysHigh;
				 float profitBullPerc=0f;float range=0f, profitBearPerc=0f;
			  		for (int i=0; i< dateI.size()-1; i++){
			  			range = Math.abs((float)ema2I.get(i) - (float)ema2I.get(i+1))*100/(float)ema2I.get(i);
			  			
			  			if(range < 0.01){
			  				count++;
			  			}
			  			if(count > 20){
			  				count=0;
			  				isStagnant=true;
	              			break;
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
		  		openI.clear();highI.clear();lowI.clear();closeI.clear();dateI.clear();
		  	}
		  	return isStagnant;
	   }
}
