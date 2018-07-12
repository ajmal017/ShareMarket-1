package Strategies.IntraDay;

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

public class FirstHourPinGapStrategy extends Connection{

	java.sql.Connection dbConnection = null;
	public FirstHourPinGapStrategy() {
		Connection con = new Connection();
	  	dbConnection = con.getDbConnection();
	}
	   public static void main(String[] args)  {
		      Test t = new Test();
		      FirstHourPinGapStrategy range = new FirstHourPinGapStrategy();
		      try{
		    	  
		    	    Connection con = new Connection();
		    	  	ResultSet rs=null;
		    	  	String sql = "";
		    	  	sql = "SELECT s.name FROM symbols s where volume > 50000000 order by volume desc ";
		    	  	rs = con.executeSelectSqlQuery(range.dbConnection, sql);
		    	  	String name="";
		    	  	String iter="1d";
		    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
		    	  	while (rs.next()){
		    	  		name= rs.getString("s.name");
		    	  		System.out.println(name);
		    	  		if(!iter.equals("1d"))
		    	  			name =name+"_"+iter+"";
		    	  		range.getdate(range.dbConnection, name);
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
		      }
	   }
	   
	   public void getdate(java.sql.Connection con, String name) throws SQLException, InterruptedException{
		   String sql="Select * from `"+name+"` where tradedate> '2015-01-01' and "
		   		+ "intraday3Min3_18_Close is not null && intradayOpen is not null && abs(intradayOpen-open)*100/open < 5";
		   ResultSet rs=null;
		   rs = executeSelectSqlQuery(con, sql);

		  	List open = new ArrayList<Float>();
		  	List high = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
		  	List date = new ArrayList<String>();
		  	List volume = new ArrayList<Integer>();
		   while (rs.next()){
			   open.add(rs.getFloat("intradayopen"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("intraday3Min3_18_Close"));
		  		date.add(rs.getString("tradedate"));
		   }
		   float gap=0.5f;float shadowPerc = 4f;
		   for(int i=1; i< date.size(); i++){
			   float o=(float)open.get(i);float h=(float)high.get(i);
			   float l=(float)low.get(i);float c=(float)close.get(i);
			   boolean check = (float)open.get(i-1) > (float)close.get(i-1) &&
					   ((float)open.get(i-1) - (float)close.get(i-1))*100/(float)close.get(i-1) > 2;
			   if((h-o)*100/o > shadowPerc){
				   float trig = o+(o*shadowPerc)/100;
				   float profit = (trig-c)*100/trig;
	  				sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
        					+ " values ('"+name+"', 'Bear', "+trig+", 0, "+profit+", '"+date.get(i)+"', '"+date.get(i)+"', '1')";
         			executeSqlQuery(con, sql);
			   }
			   check = (float)open.get(i-1) < (float)close.get(i-1) &&
					   ((float)close.get(i-1) - (float)open.get(i-1))*100/(float)open.get(i-1) > 2;
			   if((o-l)*100/o > shadowPerc){
				   float trig = o-(o*shadowPerc)/100;
				   float profit = (c-trig)*100/trig;
	  				sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
        					+ " values ('"+name+"', 'Bull', "+trig+", 0, "+profit+", '"+date.get(i)+"', '"+date.get(i)+"', '1')";
         			executeSqlQuery(con, sql);
			   }
		   }
	   }
}
