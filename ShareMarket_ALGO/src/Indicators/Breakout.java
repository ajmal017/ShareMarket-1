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

public class Breakout extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      Breakout brk = new Breakout();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT name FROM symbols ");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;float diffPerc=0.20f;
	    	  	boolean updateResultTable=false, isIntraDayData=false;
	    	  	boolean insertAllDataToResult = false;
	    	  	String iter="1d";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		System.out.println(name);	
	    	  		brk.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/envelope/"+iter+"/");
//	    	  		brk.UpdateENVELOPEResults(dbConnection, name, updateSymbolsTableData, updateAllData, 
//	    	  				updateResultTable, diffPerc, isIntraDayData, insertAllDataToResult);
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
//		  	System.out.println(date.size());
		  	calculateBreakout(con, name,open, high, low, close, date,  updateForTodayAndNextDay, updateForallDays, isIntraDayData, path);
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
   
  
   
   
	void calculateBreakout(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, List<Float> close, List date, 
			boolean updateForTodayAndNextDay, boolean updateForallDays, boolean isIntraDayData, String path) throws IOException, SQLException{
		
		String sql="";
		TemporaryTable tmp = new TemporaryTable();
		BufferedWriter output = null;
		String todaysDate = executeCountQuery(con, "select date(now())");
		if(updateForallDays == true || isIntraDayData == true){			

		  	tmp.createTempTable(con, name);
		  	File file = new File(""+path+name+".txt");
	        output = new BufferedWriter(new FileWriter(file));
		}
        float brk1 = 0f, brk2=0f;
		for (int i=0; i<= date.size()-1; i++){
			if (i>0){
				brk1 = ((float)high.get(i-1) - (float)low.get(i-1))*0.45f;
				brk2 = ((float)high.get(i-1) - (float)low.get(i-1))*0.75f;
			}
			
			if(updateForTodayAndNextDay == true && i== (date.size()-1)){
				sql = "UPDATE "+name+" set breakout1='"+brk1+"',breakout2='"+brk2+"' where  tradedate='"+date.get(i).toString()+"'";
//				System.out.println(sql);
				executeSqlQuery(con, sql);
			}else if(updateForallDays == true){
				
				output.write(date.get(i).toString());	output.write(","+brk1);	output.write(","+brk2);	
            	output.write("\r\n");
			}
			if(updateForTodayAndNextDay == true && (date.get(i).toString().split(" ")[0]).equalsIgnoreCase(todaysDate) && isIntraDayData == true){
				output.write(date.get(i).toString());	output.write(","+brk1);	output.write(","+brk2);	
            	output.write("\r\n");
			}
			
		}
		
		if ( output != null ) output.close();
		if(updateForallDays == true || isIntraDayData == true){			
			
			
			
			sql = "LOAD DATA LOCAL INFILE '"+path+name+".txt' "+
					 " INTO TABLE "+name+"_Temp "+
					 " FIELDS TERMINATED BY ',' "+
					 " LINES TERMINATED BY '\n'" +
					 " "+
					 " (tradedate, breakout1, breakout2 ) ";
           
          		executeSqlQuery(con, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.breakout1 = b.breakout1*1, a.breakout2 = b.breakout2*1 "+
				" WHERE a.tradedate = b.tradedate ";
//          		System.out.println(sql);
          		executeSqlQuery(con, sql);
          		tmp.dropTempTable(con, name);
		}
	}
			
}

