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

public class PivotFor1DayData extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      PivotFor1DayData pivot = new PivotFor1DayData();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 100000000 order by volume desc");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
	    	  	boolean updateResultTable=true;float diffPerc=0.20f;boolean isIntraDayData=false;
	    	  	String iter="1d";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		if(!iter.equals("1d"))
	    	  			name =name+"_"+iter+"";
	    	  		
	    	  		System.out.println(name);	
	    	  		pivot.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, 
	    	  				isIntraDayData, path+"/adx/"+iter+"/");
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
	  	List date = new ArrayList<String>();
	  	try {
			rs = executeSelectSqlQuery(con, "select tradedate from "+name+" group by year(tradedate),month(tradedate);");
		  	while (rs.next()){
		  		date.add(rs.getString("tradedate"));
		  	}
		  	calculatePivot(con, name,date, updateForTodayAndNextDay, updateForallDays, 
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
   
  
   
   
	void calculatePivot(java.sql.Connection con, String name, List date, boolean updateForTodayAndNextDay, boolean updateForallDays,
			boolean isIntraDayData, String path) throws IOException, SQLException{
		String sql="", tradeDate="";
        String dir="", direction="", prevDate="";
        int count=0,nextMonth=0, nextYear=0;
        ResultSet rs=null;float pivot=0f, r1=0f, r2=0f, r3=0f, s1=0f, s2=0f, s3=0f;
        float maxMonth=0f,minMonth=0f,closeMonth=0f;
		for (int i=0; i<= date.size()-1; i++){
			String split[]=date.get(i).toString().split("-");
			sql="select max(high+0) from "+name+" where month(tradedate)="+split[1]+" and year(tradedate)="+split[0]+"";
			maxMonth = Float.parseFloat(executeCountQuery(con, sql));
			sql="select min(low+0) from "+name+" where month(tradedate)="+split[1]+" and year(tradedate)="+split[0]+"";
			minMonth = Float.parseFloat(executeCountQuery(con, sql));
			sql="select close from "+name+" where month(tradedate)="+split[1]+" and year(tradedate)="+split[0]+" order by tradedate desc limit 1";
			closeMonth = Float.parseFloat(executeCountQuery(con, sql));
			pivot = (maxMonth + minMonth + closeMonth)/3;
			r1 = 2*pivot-minMonth;
			s1 = 2*pivot-maxMonth;
			r2 = pivot + (r1 - s1);
			s2 = pivot - (r1 - s1);
			r3 = maxMonth + 2*(pivot-minMonth);
			s3 = minMonth - 2*(maxMonth-pivot);
			
			if(Integer.parseInt(split[1]) ==12) {
				nextMonth=01;
				nextYear = Integer.parseInt(split[0])+1;
			}else{
				nextMonth=Integer.parseInt(split[1])+1;
				nextYear = Integer.parseInt(split[0]);
			}
			sql = "UPDATE "+name+" "+
					" SET pivot = "+pivot+",R1 = "+r1+", R2 = "+r2+", R3 = "+r3+", S1 = "+s1+", S2 = "+s2+", S3 = "+s3+""+
					" WHERE month(tradedate)="+nextMonth+" and year(tradedate)="+nextYear;
	          		executeSqlQuery(con, sql);
		}
	}
}

