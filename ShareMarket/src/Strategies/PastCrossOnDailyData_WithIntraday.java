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

public class PastCrossOnDailyData_WithIntraday extends Connection{

	public void getData( java.sql.Connection con, String name){
		 ResultSet rs=null;
		  	List open = new ArrayList<Float>();	List openI = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();	List highI = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();	List lowI = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();	List closeI = new ArrayList<Float>();
		  	List max_high = new ArrayList<Float>();	List dateI = new ArrayList<String>();
		  	List min_low = new ArrayList<Float>();	List pivotI = new ArrayList<Float>();
		  	List cci = new ArrayList<Float>();		List r1I = new ArrayList<Float>();
		  	List volume = new ArrayList<Long>();	List s1I = new ArrayList<Float>();
		  	List avgVolume = new ArrayList<Long>();
		  	List date = new ArrayList<String>();
		  	int interval=5;
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
			  	float profitPerc=0f, target=2, trigger=0;;
			  	String sql="";int check=0;
			  	float diffToTrig=0f;
			  	for(int x=0; x<date.size()-1; x++){
			  		check=0;openI.clear();highI.clear();lowI.clear();closeI.clear();pivotI.clear();r1I.clear();s1I.clear();dateI.clear();
			  		if(x<3) continue;
			  			float widthPerc = ((float)open.get(x+1) - (float)low.get(x))*100/((float)high.get(x) - (float)low.get(x));
			  			float diff = ((float)close.get(x) - (float)open.get(x+1))*100/(float)open.get(x+1);
			  			diffToTrig = ((float)max_high.get(x) - (float)low.get(x+1))*100/(float)low.get(x+1);
			  			
				  		if( (float)high.get(x+1) > (float)max_high.get(x) && diff<20 &&  (float)open.get(x+1) < (float)max_high.get(x) && (float)max_high.get(x)!=0
				  				){
				  			sql = "select tradedate, pivot, r1,s1, open, high,low,close from "+name+"_"+interval+" where date(tradedate)=date('"+date.get(x+1)+"') order by tradedate";
				  			rs = executeSelectSqlQuery(con, sql);
				  			while(rs.next()){
				  				
				  				openI.add(rs.getFloat("open"));
						  		highI.add(rs.getFloat("high"));
						  		lowI.add(rs.getFloat("low"));
						  		closeI.add(rs.getFloat("close"));
						  		pivotI.add(rs.getFloat("pivot"));
						  		r1I.add(rs.getFloat("r1"));
						  		s1I.add(rs.getFloat("s1"));
						  		dateI.add(rs.getString("tradedate"));
				  			}
				  			trigger=(float)max_high.get(x);
				  			for(int j=0;j<dateI.size(); j++){
				  				if( (float)high.get(j) > (float)max_high.get(x) ){
				  					check=1;
				  					break;
				  				}
//				  				if( (float)close.get(j) > (float)max_high.get(x) ){
//				  					trigger = (float)close.get(j);
//				  					check=1;
//				  					break;
//				  				}
				  			}
				  			profitPerc = ((float)close.get(x+1) - trigger)*100/trigger;
//				  			profitPerc = ((float)close.get(x+1) - (float)max_high.get(x))*100/(float)max_high.get(x);
//				  			profitPerc = ((float)close.get(x+1) - (float)open.get(x+1))*100/(float)open.get(x+1);
//				  			if(profitPerc>target){
//				  				profitPerc=target;
//				  			}else profitPerc = ((float)close.get(x+1) - (float)max_high.get(x))*100/(float)max_high.get(x);
				  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
				  					+ " values ('"+name+"', 'Bull', "+(float)max_high.get(x)+", "+(profitPerc-.05f)+", "+(float)high.get(x)+", '"+date.get(x+1)+"')";
							if(check==1)
								executeSqlQuery(con, sql);
				  		}
				  		diffToTrig = ((float)high.get(x+1) - (float)min_low.get(x))*100/(float)high.get(x+1);
				  		if( (float)low.get(x+1) < (float)min_low.get(x)  && diff<20 && (float)open.get(x+1) > (float)min_low.get(x) && (float)min_low.get(x)!=0
				  				&& (float)cci.get(x)<-100){
				  			profitPerc = ((float)min_low.get(x)-(float)low.get(x+1))*100/(float)min_low.get(x);
//				  			profitPerc = ((float)min_low.get(x)-(float)close.get(x+1))*100/(float)min_low.get(x);
//				  			profitPerc = ((float)open.get(x+1)-(float)close.get(x+1))*100/(float)open.get(x+1);
//				  			if(profitPerc>target){
//				  				profitPerc=target;
//				  			}else profitPerc = ((float)min_low.get(x)-(float)close.get(x+1))*100/(float)min_low.get(x);
				  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
				  					+ " values ('"+name+"', 'Bear', "+(float)min_low.get(x)+", "+(profitPerc-.05f)+", "+(float)low.get(x)+", '"+date.get(x+1)+"')";
//							executeSqlQuery(con, sql);
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
	      PastCrossOnDailyData_WithIntraday pin = new PastCrossOnDailyData_WithIntraday();
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
