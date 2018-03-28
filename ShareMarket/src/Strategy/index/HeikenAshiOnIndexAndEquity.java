package Strategy.index;

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

public class HeikenAshiOnIndexAndEquity extends Connection{
	public void getEquityPerformance( java.sql.Connection dbConnection, String index, String date, String dir){
		ResultSet rs=null;
	  	List tradedate = new ArrayList<String>();
	  	List indexSymbols = new ArrayList<String>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
		String sql="";
		float percProfitAtHighPrice=0f, percProfitAtClosePrice=0f, percProfitAtLowPrice=0f;
		try{
			sql = "select * from "+index+"_index";
			rs = executeSelectSqlQuery(dbConnection, sql);
			while(rs.next()){
				indexSymbols.add(rs.getString("name"));
			}
			for (int i=0; i<indexSymbols.size(); i++){
				try{
					String sym = indexSymbols.get(i).toString();
					if(dir.equalsIgnoreCase("UP") && !sym.contains("-") && !sym.contains("&")){
						sql = "select (high-open)*100/open from "+indexSymbols.get(i)+" where tradedate = date('"+date+"')";
						percProfitAtHighPrice = Float.parseFloat(executeCountQuery(dbConnection, sql));
						sql = "select (close-open)*100/open from "+indexSymbols.get(i)+" where tradedate = date('"+date+"')";
						percProfitAtClosePrice = Float.parseFloat(executeCountQuery(dbConnection, sql));
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+index+"', 'Bull', "+percProfitAtHighPrice+", "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", '"+date+"')";
						executeSqlQuery(dbConnection, sql);
					}
					if(dir.equalsIgnoreCase("DOWN") && !sym.contains("-") && !sym.contains("&")){
						sql = "select (open-low)*100/open from "+indexSymbols.get(i)+" where tradedate = date('"+date+"')";
						percProfitAtLowPrice = Float.parseFloat(executeCountQuery(dbConnection, sql));
						sql = "select (open-close)*100/open from "+indexSymbols.get(i)+" where tradedate = date('"+date+"')";
						percProfitAtClosePrice = Float.parseFloat(executeCountQuery(dbConnection, sql));
						
						sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
			  					+ " values ('"+index+"', 'Bear', "+percProfitAtLowPrice+", "+percProfitAtClosePrice+", "+percProfitAtClosePrice+", '"+date+"')";
						executeSqlQuery(dbConnection, sql);
					}
				}
				catch(SQLException e){
					e.printStackTrace();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

   public static void main(String[] args)  {
	      Test t = new Test();
	      List tradedate = new ArrayList<String>();
		  	List profit = new ArrayList<Float>();
		  	List highha = new ArrayList<Float>();
		  	List openha = new ArrayList<Float>();
		  	List lowha = new ArrayList<Float>();
		  	List closeha = new ArrayList<Float>();
		  	
		  	List high = new ArrayList<Float>();
		  	List open = new ArrayList<Float>();
		  	List low = new ArrayList<Float>();
		  	List close = new ArrayList<Float>();
	      HeikenAshiOnIndexAndEquity pin = new HeikenAshiOnIndexAndEquity();
	      java.sql.Connection dbConnection = null;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	String sql="";
	    	  	sql =  "SELECT * from  indices";
	    	  	rs = con.executeSelectSqlQuery(dbConnection, sql);
	    	  	List<String> indices = new ArrayList<>();
	    	  	while (rs.next()){
	    	  		indices.add(rs.getString("indexname"));
	    	  	}
	    	  	for(int x=0; x< 1; x++){
	    	  		String name=indices.get(x);
		    	  	sql =  "SELECT * from  "+name;
		    	  	rs = con.executeSelectSqlQuery(dbConnection, sql);
		    	  	
		    	  	String iter="1d";
		    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
		    	  	while (rs.next()){
		    	  		tradedate.add(rs.getString("tradedate"));
		    	  		openha.add(rs.getFloat("HA_OPEN"));
		    	  		highha.add(rs.getFloat("HA_HIGH"));
		    	  		lowha.add(rs.getFloat("HA_LOW"));
		    	  		closeha.add(rs.getFloat("HA_CLOSE"));
		    	  		
		    	  		open.add(rs.getFloat("OPEN"));
		    	  		high.add(rs.getFloat("HIGH"));
		    	  		low.add(rs.getFloat("LOW"));
		    	  		close.add(rs.getFloat("CLOSE"));
		    	  	}
		    	  	float profitAtHighLow=0f, profitAtClose=0f;
		    	  	for(int i=2; i< tradedate.size()-1; i++){
		    	  		if((float)openha.get(i) ==(float)lowha.get(i)){
//		    	  			profitAtHighLow = ((float)high.get(i+1) - (float)open.get(i+1))*100/(float)open.get(i+1);
//		    	  			profitAtClose = ((float)close.get(i+1) - (float)open.get(i+1))*100/(float)open.get(i+1);
//		    	  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
//		    	  					+ " values ('"+name+"', 'Bull', "+profitAtClose+", "+profitAtHighLow+", "+profitAtHighLow+", '"+tradedate.get(i+1)+"')";
//		    				con.executeSqlQuery(dbConnection, sql);
		    				pin.getEquityPerformance(dbConnection, name, tradedate.get(i+1).toString(), "UP"); 
		    	  		}
		    	  		if((float)openha.get(i) ==(float)highha.get(i)){
//		    	  			profitAtHighLow = ((float)open.get(i+1) - (float)low.get(i+1))*100/(float)open.get(i+1);
//		    	  			profitAtClose = ((float)open.get(i+1) - (float)close.get(i+1))*100/(float)open.get(i+1);
//		    	  			sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
//		    	  					+ " values ('"+name+"', 'Bear', "+profitAtClose+", "+profitAtHighLow+", "+profitAtHighLow+", '"+tradedate.get(i+1)+"')";
//		    				con.executeSqlQuery(dbConnection, sql);
		    				pin.getEquityPerformance(dbConnection, name, tradedate.get(i+1).toString(), "DOWN");
		    	  		}
		    	  	}
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
}
