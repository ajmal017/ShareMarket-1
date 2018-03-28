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

public class HeikenAshi extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      HeikenAshi ha = new HeikenAshi();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 50000000 and todaysopen>6 order by volume desc");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;float diffPerc=0.20f;
	    	  	boolean updateResultTable=false, isIntraDayData=false;;
	    	  	boolean insertAllDataToResult = false; boolean fastProcess=false;
	    	  	String iter="30";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		if(!name.equals("1d")){
	    	  			name = name + "_"+iter;
	    	  		}
	    	  		System.out.println(name);	
	    	  		ha.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/"+iter+"/");
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
	   ResultSet rs=null, rs2=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	try {
			rs = executeSelectSqlQuery(con, "SELECT open,high,low,close,tradedate,date(tradedate) as Date FROM "+name+"  order by tradedate;");
		 
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));		  		
		  		date.add(rs.getString("tradedate"));
		  	}
		  	if(rs!=null) rs=null; if(rs2!=null) rs2=null;
		  	calculateHeikenAshi(con, name,open, high, low, close, date, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path);
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
   
  
   
   
	void calculateHeikenAshi(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, List<Float> close, List date,
			boolean updateForTodayAndNextDay, boolean updateForallDays, boolean isIntraDayData, String path) throws IOException, SQLException{
		String sql="",tradeDate="";;
		
		TemporaryTable tmp = new TemporaryTable();
		BufferedWriter output = null;
		String todaysDate = executeCountQuery(con, "select date(now())");
		if(updateForallDays == true || isIntraDayData == true){			
		  	tmp.createTempTable(con, name);
		  	File file = new File(""+path+name+".txt");
	        output = new BufferedWriter(new FileWriter(file));	
		}
        float ha_open=0f, ha_high=0f, ha_low=0f, ha_close=0f, ha_temp_forOpen=0f;
		for (int i=0; i<= date.size()-1; i++){
			tradeDate = date.get(i).toString();
			if(i==0){
				ha_close = ((float)open.get(i) + (float)high.get(i) + (float)low.get(i) +(float)close.get(i))/4;
				ha_open = ((float)open.get(i) +(float)close.get(i))/2;
				ha_high = (float)high.get(i);
				ha_low = (float)low.get(i);
				ha_temp_forOpen = (ha_open +ha_close)/2;
			}else{
				ha_open = ha_temp_forOpen;
				ha_close = ((float)open.get(i) + (float)high.get(i) + (float)low.get(i) +(float)close.get(i))/4;
				ha_high = Math.max((float)high.get(i), (Math.max(ha_close, ha_open)));
				ha_low = Math.min((float)low.get(i), (Math.min(ha_close, ha_open)));
				ha_temp_forOpen = (ha_open +ha_close)/2;
			}
			
			
			if(updateForTodayAndNextDay == true && i== (date.size()-1)){
				sql = "UPDATE "+name+" set HA_OPEN="+ha_open+",ha_high="+ha_high+","
						+ "ha_low="+ha_low+",ha_close="+ha_close+" where  tradedate='"+tradeDate+"'";
//				System.out.println(sql);
				executeSqlQuery(con, sql);
			}else if(updateForallDays == true){
				output.write(tradeDate);output.write(","+ha_open);output.write(","+ha_high);output.write(","+ha_low);
				output.write(","+ha_close); 
            	output.write("\r\n");
			}
			if(updateForTodayAndNextDay == true && (tradeDate.split(" ")[0]).equalsIgnoreCase(todaysDate) && isIntraDayData == true){
				output.write(tradeDate);output.write(","+ha_open);output.write(","+ha_high);output.write(","+ha_low);
				output.write(","+ha_close); 
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
					 " (tradedate, HA_OPEN, HA_HIGH, HA_LOW, HA_CLOSE) ";
           
          		executeSqlQuery(con, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.HA_OPEN = b.HA_OPEN*1, a.HA_HIGH = b.HA_HIGH*1, a.HA_LOW = b.HA_LOW*1, a.HA_CLOSE = b.HA_CLOSE*1  "+
				" WHERE a.tradedate = b.tradedate ";
//          		System.out.println(sql);
          		executeSqlQuery(con, sql);
          		tmp.dropTempTable(con, name);
		}
	}
			
}

