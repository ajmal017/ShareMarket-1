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

public class OnBalanceVolume extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      OnBalanceVolume obv = new OnBalanceVolume();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s ");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=false; boolean updateForallDays=true;
	    	  	boolean updateResultTable=false;float diffPerc=0.20f;boolean isIntraDayData=true;
	    	  	boolean insertAllDataToResult = true;
	    	  	String iter="15";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		if(!iter.equals("1d"))
	    	  			name =name+"_"+iter+"";
	    	  		System.out.println(name);	
	    	  		obv.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/cci/"+iter+"/");
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
   
   
   
   public void LoadData(java.sql.Connection con, String name, boolean updateForTodayAndNextDay, boolean updateForallDays,
		   boolean isIntraDayData, String path) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	List volume = new ArrayList<Long>();
	  	try {
			rs = executeSelectSqlQuery(con, "SELECT open, high, low, close,TotalQty, tradedate FROM "+name+" ");
		 
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getString("tradedate"));
		  		volume.add(rs.getLong("TotalQty"));
		  	}
//		  	System.out.println(date.size());
		  	calculateOBV(con, name,open, high, low, close, date,  volume, updateForTodayAndNextDay, updateForallDays, 
		  			isIntraDayData, path);
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
   
  
   
   
	void calculateOBV(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, 
			List<Float> close, List date, List<Long> volume, boolean updateForTodayAndNextDay, boolean updateForallDays,
			boolean isIntraDayData, String path) throws IOException, SQLException{
		
		long obv_prev=0, obv=0;
		TemporaryTable tmp = new TemporaryTable();
		
		String sql="", tradeDate="";
		
		
		BufferedWriter output = null;
		if(updateForallDays == true || isIntraDayData == true){	
			tmp.createTempTable(con, name);
		  	File file = new File(""+path+name+".txt");
	        output = new BufferedWriter(new FileWriter(file));
		}
	  	
        
        String todaysDate = executeCountQuery(con, "select date(now())");
		for (int i=0; i<= date.size()-1; i++){
			if(isIntraDayData==true)
	  			tradeDate = (String) date.get(i).toString();
	  		else
	  			tradeDate = (String) date.get(i);
			
			if(i==0){
				obv = volume.get(i);
			}else if(i>0){
				if(close.get(i) == close.get(i-1)){
					obv = obv_prev;
				}else if(close.get(i) > close.get(i-1)){
					obv = obv_prev + volume.get(i);
				}else if (close.get(i) < close.get(i-1)){
					obv = obv_prev - volume.get(i);
				}
			}
			if(updateForTodayAndNextDay == true && i== (date.size()-1)){
				output.write(tradeDate);	output.write(","+obv);	
            	output.write("\r\n");
			}else if(updateForallDays == true){
				
				output.write(tradeDate);	output.write(","+obv);	
            	output.write("\r\n");
			}
			if(updateForTodayAndNextDay == true && (tradeDate.split(" ")[0]).equalsIgnoreCase(todaysDate) && isIntraDayData == true){
				output.write(tradeDate);	output.write(","+obv);	
            	output.write("\r\n");
			}
			obv_prev = obv;
		}
		if ( output != null ) output.close();
		
		if(updateForallDays == true || isIntraDayData == true){
			sql = "LOAD DATA LOCAL INFILE '"+path+name+".txt' "+
					 " INTO TABLE "+name+"_Temp "+
					 " FIELDS TERMINATED BY ',' "+
					 " LINES TERMINATED BY '\n'" +
					 " "+
					 " (tradedate, OBV) ";
          
         		executeSqlQuery(con, sql);
         		
         		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.OBV = b.OBV*1  "+
				" WHERE a.tradedate = b.tradedate ";
//         		System.out.println(sql);
         		executeSqlQuery(con, sql);
         		tmp.dropTempTable(con, name);
		}
			
			
		
	}
			
}

