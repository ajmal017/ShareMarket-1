package Indicators.Pivot;

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

import Indicators.Connection;
import Indicators.TemporaryTable;
import Indicators.Test;

public class PivotForBelow30Min extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      PivotForBelow30Min pivot = new PivotForBelow30Min();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 100  order by volume desc ");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
	    	  	boolean updateResultTable=true;float diffPerc=0.20f;boolean isIntraDayData=false;
	    	  	String iter="1";
	    	  	String path="C:/Puneeth/OldLaptop/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		if(!iter.equals("1d"))
	    	  			name =name+"_"+iter+"";
	    	  		name="banknifty_50_1";
	    	  		System.out.println(name);	
	    	  		
	    	  		pivot.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, 
	    	  				isIntraDayData, path+"/pivot/"+iter+"/");
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
	  	List volume = new ArrayList<String>();
	  	try {
			rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, tradedate, volume FROM "+name+"  order by tradedate;");
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getString("tradedate"));
		  		volume.add(rs.getLong("volume"));
		  	}
		  	calculatePivot(con, name,open, high, low, close, date, volume, updateForTodayAndNextDay, updateForallDays, 
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
   
  
   
   
	void calculatePivot(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, 
			List<Float> close, List date, List<Integer> volume, boolean updateForTodayAndNextDay, boolean updateForallDays,
			boolean isIntraDayData, String path) throws IOException, SQLException{
		TemporaryTable tmp = new TemporaryTable();
		tmp.createTempTable(con, name);
		String sql="", tradeDate="";
		List openI = new ArrayList<Float>();
	  	List highI = new ArrayList<Float>();
	  	List lowI = new ArrayList<Float>();
	  	List closeI = new ArrayList<Float>();
	  	List dateI = new ArrayList<String>();
	  	List volumeI = new ArrayList<String>();
		BufferedWriter output = null;
		File file = new File(""+path+name+".txt");
        output = new BufferedWriter(new FileWriter(file));
        String dir="", direction="", prevDate="";
        int count=0;
        ResultSet rs=null; float high_t=0f, low_t=0f;float pivot=0f, r1=0f, r2=0f, r3=0f, s1=0f, s2=0f, s3=0f;
		for (int i=0; i<= date.size()-1; i++){
			
			if(i==0) {
				prevDate = date.get(i).toString().split(" ")[0];
				high_t =(float)high.get(i);
				low_t =(float)low.get(i);
			}
			
			if(prevDate.equals(date.get(i).toString().split(" ")[0])){
				high_t = Math.max(high_t, (float)high.get(i));
				low_t = Math.min(low_t, (float)low.get(i));
			}else{
				pivot = (high_t + low_t + (float)close.get(i-1))/3;
				r1 = 2*pivot-low_t;
				s1 = 2*pivot-high_t;
				r2 = pivot + (r1 - s1);
				s2 = pivot - (r1 - s1);
				r3 = high_t + 2*(pivot-low_t);
				s3 = low_t - 2*(high_t-pivot);
				high_t =(float)high.get(i);
				low_t =(float)low.get(i);
//				sql="update "+name+" set pivot='"+pivot+"', r1='"+r1+"', r2='"+r2+"', r3='"+r3+"',s1='"+s1+"',s2='"+s2+"',s3='"+s3+"'"
//						+ " where date(tradedate)=Date('"+date.get(i)+" ')";
//				executeSqlQuery(con, sql);
				if(updateForallDays == true){
					output.write(date.get(i).toString());output.write(","+pivot);output.write(","+r1);output.write(","+r2);output.write(","+r3);
					output.write(","+s1);output.write(","+s2);output.write(","+s3);
                	output.write("\r\n");
				}
			}
			prevDate = date.get(i).toString().split(" ")[0];
		}

		if ( output != null ) output.close();
		if(updateForallDays == true){
			sql = "LOAD DATA LOCAL INFILE '"+path+name+".txt' "+
					 " INTO TABLE "+name+"_Temp "+
					 " FIELDS TERMINATED BY ',' "+
					 " LINES TERMINATED BY '\n'" +
					 " "+
					 " (tradedate, pivot, R1, R2, R3, S1, S2, S3)";
           
          		executeSqlQuery(con, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.pivot = b.pivot,a.R1 = b.R1, a.R2 = b.R2, a.R3 = b.R3, a.S1 = b.S1, a.S2 = b.S2, a.S3 = b.S3 "+
				" WHERE Date(a.tradedate) = Date(b.tradedate) ";
//          		System.out.println(sql);
          		executeSqlQuery(con, sql);
          		tmp.dropTempTable(con, name);
		}
	}
}

