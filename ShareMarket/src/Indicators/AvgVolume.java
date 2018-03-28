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

public class AvgVolume extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      AvgVolume avg = new AvgVolume();
	      java.sql.Connection dbConnection = null;
	      try{
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT name FROM symbols ");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=false;
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
	  	List date = new ArrayList<String>();
	  	try {
			rs = executeSelectSqlQuery(con, "SELECT totalqty, volume, tradedate FROM "+name+"  ");
		 
		  	while (rs.next()){
		  		volume.add(rs.getLong("totalqty"));
		  		date.add(rs.getString("tradedate"));
		  	}
		  	calculateVolumeAvg(con, name, volume, date, 100, updateForTodayAndNextDay, 
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
   
   	void calculateVolumeAvg(java.sql.Connection con, String name, List<Long> volume,List<String> date, int range,
			boolean updateForTodayAndNextDay, boolean updateForallDays, boolean isIntraDayData, String path) throws IOException, SQLException{
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
		for(int i=0; i<volume.size(); i++){
			if(i>=range){
				if(i==range){
					for(int j=i-range; j<=i; j++){
						sum = sum +volume.get(j);
					}
					avg = sum/(range+1);
					sum=0;
				}else{
					for(int j=i-range; j<=i; j++){
						sum = sum +volume.get(j);
					}
					avg = sum/(range+1);
//					System.out.println(avg+","+date.get(i)+","+(i-range)+" to "+range+","+i);
					sum=0;
				}
				if(updateForTodayAndNextDay == true && i== (volume.size()-1)){
					sql = "UPDATE "+name+" set avg_Volume="+avg+" where tradedate='"+date.get(i)+"'";
//					System.out.println(sql);
					executeSqlQuery(con, sql);
					
					sql = "UPDATE symbols set Volume="+avg+" where name='"+name+"'";
					executeSqlQuery(con, sql);
				}else if(updateForallDays == true){
					output.write(date.get(i));	output.write(","+avg);
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
					 " (tradedate, avg_volume) ";
           
          		executeSqlQuery(con, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.avg_volume = b.avg_volume*1 "+
				" WHERE a.tradedate = b.tradedate ";
//          		System.out.println(sql);
          		executeSqlQuery(con, sql);
          		tmp.dropTempTable(con, name);
		}
	}
			
}

