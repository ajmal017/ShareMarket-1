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

public class ChoppyIndex extends Connection{

	static boolean isCalculateForIndex = false;
   public static void main(String[] args)  {
	      Test t = new Test();
	      ChoppyIndex obv = new ChoppyIndex();
	      java.sql.Connection dbConnection = null;
	      
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where ismargin=1 AND NAME='INFRATEL'");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=false; boolean updateForallDays=true;
	    	  	boolean updateResultTable=false;float diffPerc=0.20f;
	    	  	boolean insertAllDataToResult = true;
	    	  	
	    	  	String iter="60";
	    	  	String path="C:/Puneeth/oldLaptop/puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	if(!isCalculateForIndex){
	    	  		while (rs.next()){
		    	  		name= rs.getString("name");
		    	  		if(!iter.equals("1d"))
		    	  			name =name+"_"+iter+"";
		    	  		System.out.println(name);	
		    	  		obv.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, path+iter+"/choppy/");
		    	  	}
	    	  	}else{
	    	  		obv.LoadData(dbConnection, "NIFTY_50", updateForTodayAndNextDay, updateForallDays, path+iter+"/choppy/");
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
		   String path) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	try {
	  		rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, tradedate FROM `"+name+"` ");
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getString("tradedate"));
		  	}
//		  	System.out.println(date.size());
		  	calculateChoppyIndex(con, name,open, high, low, close, date, updateForTodayAndNextDay, updateForallDays, 
		  			path, 50);
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
   
	void calculateChoppyIndex(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, 
			List<Float> close, List date, boolean updateForTodayAndNextDay, boolean updateForallDays,
			String path, int pastRange) throws IOException, SQLException{
		
		long obv_prev=0, obv=0;
		
		TemporaryTable tmp = new TemporaryTable();
		float h1 = 0, h2=0,h3=0,h4=0,h5=0,h6=0;
		float l1=0, l2=0,l3=0,l4=0,l5=0,l6=0;
		String sql="", tradeDate="";
		
		
		BufferedWriter output = null;
		if(updateForallDays == true){	
			tmp.createTempTable(con, name);
		  	File file = new File(""+path+name+".txt");
	        output = new BufferedWriter(new FileWriter(file));
		}
	  	
        float trueHigh=0f, trueLow=0f, range3=0f, TR=0f, atr=0f;;
        float highestHigh=0f, lowestLow=999999999f, choppyValue=0f;
        String todaysDate = executeCountQuery(con, "select date(now())");
		for (int i=pastRange; i<= date.size()-1; i++){
			TR=0f;
			trueHigh=0f;trueLow=0f;highestHigh=0f;lowestLow=999999999f;
	  		tradeDate = (String) date.get(i);
	  		for (int j=i-pastRange; j<= i; j++){
	  			if(j==0) continue;
		  		trueHigh = Math.max(high.get(j), close.get(j-1));
				trueLow = Math.min(low.get(j), close.get(j-1));
				TR+=(trueHigh - trueLow);
				highestHigh = Math.max(highestHigh, trueHigh);
				lowestLow = Math.min(lowestLow, trueLow);
	  		}
	  		choppyValue = (float) (100 * Math.log((( TR / ( highestHigh - lowestLow ) ) / Math.log(pastRange))));
	  		if(Float.isInfinite(choppyValue)){
	  			continue;
	  		}
	  		if(updateForTodayAndNextDay == true && i== (date.size()-1)){
				sql = "UPDATE `"+name+"` set choppyIndex="+choppyValue+" where  tradedate='"+tradeDate+"'";
//				System.out.println(sql);
				executeSqlQuery(con, sql);
			}else if(updateForallDays == true){
				output.write(tradeDate);
				output.write(","+choppyValue); 
            	output.write("\r\n");
			}
			if(updateForTodayAndNextDay == true && (tradeDate.split(" ")[0]).equalsIgnoreCase(todaysDate)){
				output.write(tradeDate);
				output.write(","+choppyValue); 
            	output.write("\r\n");
			}
		}
		if ( output != null ) output.close();
		
		if(updateForallDays == true ){
			sql = "LOAD DATA LOCAL INFILE '"+path+name+".txt' "+
					 " INTO TABLE "+name+"_Temp "+
					 " FIELDS TERMINATED BY ',' "+
					 " LINES TERMINATED BY '\n'" +
					 " "+
					 " (tradedate, choppyIndex) ";
          
         		executeSqlQuery(con, sql);
         		
         		sql = "UPDATE `"+name+"_Temp` b, `"+name+"` a"+
				" SET a.choppyIndex = b.choppyIndex*1 "+
				" WHERE a.tradedate = b.tradedate ";
//         		System.out.println(sql);
         		executeSqlQuery(con, sql);
         		tmp.dropTempTable(con, name);
		}
		if(isCalculateForIndex){
			System.exit(0);
		}
	}
			
}

