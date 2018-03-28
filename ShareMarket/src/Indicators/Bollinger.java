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

public class Bollinger extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      Bollinger bb = new Bollinger();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume*lastprice > 10000000000 and "
		    	  			+ " s.name not like '%-%' and s.name not like '%&%'  order by id");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
	    	  	boolean updateResultTable=false;boolean isIntraDayData=false;
	    	  	boolean insertAllDataToResult = false;
	    	  	List dailyDate=new ArrayList<String>();
	    	  	String iter="60";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("s.name");
	    	  		if(!iter.equals("1d"))
	    	  			name =name+"_"+iter+"";
	    	  		System.out.println(name);	
	    	  		bb.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData,path+"/"+iter+"/");
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
   public List passDate(java.sql.Connection con, String name) throws SQLException{
	   ResultSet rs=null;
	   List date = new ArrayList<String>();
	   String sql = "select distinct(date(tradedate)) as date from "+name;
	   rs = executeSelectSqlQuery(con, sql);
	   while (rs.next()){
		   date.add(rs.getString("date"));
	   }
	   return date;
   }
   
   
   public void LoadData(java.sql.Connection con, String name, boolean updateForTodayAndNextDay, boolean updateForallDays,
		   boolean isIntraDayData, String path) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	try {
			rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, tradedate, supertrend_reversal FROM "+name+"  order by tradedate;");
		 
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getString("tradedate"));
		  	}
//		  	System.out.println(date.size());
		  	calculateBolingerBand(con, name,open, high, low, close, date,  50, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path);
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
 
   
	void calculateBolingerBand(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, List<Float> close, List date, 
			int bolingerPeriod,
			boolean updateForTodayAndNextDay, boolean updateForallDays, boolean isIntraDayData, String path) throws IOException, SQLException{
		Float highT=0.0f, lowT=0f, will_R=0f, willTemp=0f;
		bolingerPeriod--;int k=0;String sql="",tradeDate="";
		int bolPeriod = bolingerPeriod;
		String todaysDate = executeCountQuery(con, "select date(now())");
		TemporaryTable tmp = new TemporaryTable();
		BufferedWriter output = null;
		if(updateForallDays == true || isIntraDayData == true){			
			tmp.createTempTable(con, name);
		  	File file = new File(""+path+name+".txt");
	        output = new BufferedWriter(new FileWriter(file));	
		}
	  	
        float sumOfField=0f, sma=0f, squaredSum=0f, squaredSumAvg=0f, std_dev=0f, upper=0f,lower=0f;
        float bandWidth=0f;;
		for (int i=0; i<= date.size()-1; i++){
			tradeDate = (String) date.get(i).toString();
			
			if(i >= bolingerPeriod){
				for (int j=Math.abs(i-bolingerPeriod); j<= i; j++){
					sumOfField = sumOfField + (float)close.get(j);
				}
				sma = sumOfField / (bolingerPeriod+1);
				sumOfField=0f;
				for (int j=Math.abs(i-bolingerPeriod); j<= i; j++){
					squaredSum  = (float) (squaredSum + Math.pow(((float)close.get(j) - sma), 2));
				}
				squaredSumAvg = squaredSum / (bolingerPeriod+1);
				squaredSum=0f;
				std_dev = (float) Math.sqrt(squaredSumAvg);
				upper = sma + (std_dev*2);
				lower = sma - (std_dev*2);
				bandWidth = ((upper - lower)/sma)*100;
			}
			
			
			if(updateForTodayAndNextDay == true && i== (date.size()-1)){
				sql = "UPDATE "+name+" set BB_std_dev_20="+std_dev+", BollingerBW='"+bandWidth+"',BB_middleBand_20="+sma+", BB_lowerBand_20="+lower+""
						+ ", BB_upperBand_20="+upper+" where  tradedate='"+tradeDate+"'";
//				System.out.println(sql);
				executeSqlQuery(con, sql);
			}else if(updateForallDays == true){
				output.write(tradeDate);	output.write(","+std_dev);	output.write(","+sma);	output.write(","+lower);
				output.write(","+upper);output.write(","+bandWidth); 
            	output.write("\r\n");
			}
			if(updateForTodayAndNextDay == true && (tradeDate.split(" ")[0]).equalsIgnoreCase(todaysDate) && isIntraDayData == true){
				output.write(tradeDate);	output.write(","+std_dev);	output.write(","+sma);	output.write(","+lower);
				output.write(","+upper); output.write(","+bandWidth);
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
					 " (tradedate, BB_std_dev_20, BB_middleBand_20, BB_lowerBand_20, BB_upperBand_20, BollingerBW) ";
           
          		executeSqlQuery(con, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET  a.BB_std_dev_20 = b.BB_std_dev_20, a.BB_middleBand_20=b.BB_middleBand_20, a.BB_lowerBand_20 = b.BB_lowerBand_20,"
				+ " a.BB_upperBand_20 = b.BB_upperBand_20, a.BollingerBW = b.BollingerBW "+
				" WHERE a.tradedate = b.tradedate ";
//          		System.out.println(sql);
          		executeSqlQuery(con, sql);
          		tmp.dropTempTable(con, name);
		}
	}
			
}

