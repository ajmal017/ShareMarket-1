package Indicators;

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

public class MaxMinOfPast extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      MaxMinOfPast avg = new MaxMinOfPast();
	      java.sql.Connection dbConnection = null;
	      try{
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT name FROM symbols where volume>100000");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
	    	  	boolean updateSymbolsTableData=true; boolean updateAllData=true;
	    	  	boolean updateResultTable=false; boolean isIntraDayData=false;
	    	  	boolean insertAllDataToResult = false;
	    	  	String iter="1d";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		if(!iter.equals("1d"))
	    	  			name =name+"_"+iter+"";
	    	  		System.out.println(name);	
	    	  		avg.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/macd/"+iter+"/");
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
   
   public void LoadData(java.sql.Connection con, String name, boolean updateForTodayAndNextDay, 
		   boolean updateForallDays, boolean isIntraDayData, String path) {
	   ResultSet rs=null;
	  	List volume = new ArrayList<Long>();
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	try {
			rs = executeSelectSqlQuery(con, "SELECT open,high,low,close,totalqty, volume, tradedate FROM "+name+"  ");
		 
		  	while (rs.next()){
		  		volume.add(rs.getLong("totalqty"));
		  		date.add(rs.getString("tradedate"));
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  	}
		  	calculateMaxMin(con, name,open,high,low,close, volume, date, 100, updateForTodayAndNextDay, 
		  			updateForallDays, isIntraDayData, path);
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
   
   	void calculateMaxMin(java.sql.Connection con, String name,List<Float> open,List<Float> high,List<Float> low,
   			List<Float> close,List<Long> volume,List<String> date, int range,boolean updateForTodayAndNextDay, boolean updateForallDays, boolean isIntraDayData, String path) throws IOException, SQLException{
   		long avg=0,sum=0;
   		String sql="";
   		TemporaryTable tmp = new TemporaryTable();
		BufferedWriter output = null;
		String todaysDate = executeCountQuery(con, "select date(now())");
		if(updateForallDays == true || isIntraDayData == true){			
			tmp.createTempTable(con, name);
		  	File file = new File(""+path+name+".txt");
	        output = new BufferedWriter(new FileWriter(file));
		}
   		range--;
   		float highT,lowT;int k=0;
   		for (int i=0; i<date.size(); i++){
			if(i>= range){
				highT=0f;lowT=0f;k=0;
				for(int j=i-range; k<=range; j++){
					k++;
					highT = Math.max(highT, high.get(j));
					if(k==1){
						lowT = low.get(j);
					}else{
						lowT = Math.min(lowT, low.get(j));
					}
				}
				if(updateForTodayAndNextDay == true && i== (volume.size()-1)){
					sql = "UPDATE "+name+" set max_past_high="+highT+", min_past_low="+lowT+" where tradedate='"+date.get(i)+"'";
//					System.out.println(sql);
					executeSqlQuery(con, sql);
				}else if(updateForallDays == true){
					output.write(date.get(i));	output.write(","+highT);output.write(","+lowT);
                	output.write("\r\n");
				}
			}
   		}
		
		if ( output != null ) output.close();
		if(updateForallDays == true || isIntraDayData == true){			
			
			sql = "LOAD DATA LOCAL INFILE '"+path+name+".txt' "+
					 " INTO TABLE "+name+"_Temp "+
					 " FIELDS TERMINATED BY ',' "+
					 " LINES TERMINATED BY '\n'" +
					 " "+
					 " (tradedate, max_past_high,min_past_low) ";
           
          		executeSqlQuery(con, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.max_past_high = b.max_past_high*1, a.min_past_low = b.min_past_low*1 "+
				" WHERE a.tradedate = b.tradedate ";
//          		System.out.println(sql);
          		executeSqlQuery(con, sql);
          		tmp.dropTempTable(con, name);
		}
	}
			
}

