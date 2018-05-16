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

public class SuperTrend extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      SuperTrend superTrend = new SuperTrend();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s order by volume desc limit 500");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
	    	  	boolean updateResultTable=true;float diffPerc=0.20f;boolean isIntraDayData=true;
	    	  	String iter="1d";
	    	  	int multiplier = 3;
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		if(!iter.equals("1d"))
	    	  			name =name+"_"+iter+"";
	    	  		
	    	  		System.out.println(name);	
	    	  		superTrend.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, 
	    	  				isIntraDayData, path+"/adx/"+iter+"/", multiplier);
//	    	  		superTrend.UpdateADXResults(dbConnection, name, updateSymbolsTableData, updateAllData, updateResultTable, 
//	    	  				diffPerc, isIntraDayData);
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
		   boolean isIntraDayData, String path, int multiplier) {
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
		  	calculateSuperTrend(con, name,open, high, low, close, date,  14, updateForTodayAndNextDay, updateForallDays, 
		  			isIntraDayData, path, multiplier);
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
   
  
   
   
	void calculateSuperTrend(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, 
			List<Float> close, List date, int Range, boolean updateForTodayAndNextDay, boolean updateForallDays,
			boolean isIntraDayData, String path, int multiplier) throws IOException, SQLException{
		
		
		TemporaryTable tmp = new TemporaryTable();
		Float TR=0.0f, range1=0.0f, range2=0.0f, range3=0.0f, DMPlus=0.0f, DMMinus=0.0f;
		Float TRSum=0.0f, ATR=0.0f;float supertrend_up=0f, supertrend_down=0f, up_band=0f, down_band=0f,
				up_band_prev=0f, down_band_prev=0f, supertrend=0f, supertrend_prev=0f;
		String sql="", tradeDate="";
		Range--;int j=1;
		
		BufferedWriter output = null;
	  	tmp.createTempTable(con, name);
	  	File file = new File(""+path+name+".txt");
        output = new BufferedWriter(new FileWriter(file));
        String dir="", direction="";
		for (int i=0; i<= date.size()-1; i++){
			if(isIntraDayData==true)
	  			tradeDate = (String) date.get(i).toString();
	  		else
	  			tradeDate = (String) date.get(i);
			
			if(i > 0){
				range1 = (float)high.get(i) - (float)low.get(i);
				range2 = Math.abs((float)high.get(i) - (float)close.get(i-1));
				range3 = Math.abs((float)low.get(i) - (float)close.get(i-1));
				
				TR = Math.max(range1, Math.max(range2, range3));
				TRSum = TRSum + TR;
				
				if(i==Range+1){
					ATR = TRSum/14.00f;
				}else if (i>Range+1){
					ATR = (ATR*13+TR)/14;
				}
				if(i>=Range+1){
					supertrend_up = ((float)high.get(i) + (float)low.get(i)) / 2.00f + (multiplier * ATR);
					supertrend_down = ((float)high.get(i) + (float)low.get(i)) / 2.00f - (multiplier * ATR);
					up_band_prev = up_band;
					if(supertrend_up<up_band || close.get(i-1) > up_band )
						up_band = supertrend_up;
					else
						up_band = up_band;
					
					down_band_prev = down_band;
					if(supertrend_down>down_band || close.get(i-1) < down_band )
						down_band = supertrend_down;
					else
						down_band = down_band;
					
					supertrend_prev = supertrend;
	                if(supertrend==up_band_prev && close.get(i)<=up_band)	{
	                	supertrend = up_band;
	                }else{
	                	if(supertrend==up_band_prev && close.get(i)>=up_band){
	                		supertrend = down_band;
	                	}else{
	                		if(supertrend==down_band_prev && close.get(i)>=down_band){
	                			supertrend = down_band;
	                		}else{
	                			if(supertrend==down_band_prev && close.get(i)<=down_band){
	                				supertrend = up_band;
	                			}
	                		}
	                	}
	                }
	                if(close.get(i) > supertrend)
	                	direction = "1";
	                else
	                	direction = "-1";
				}
					
				
//				System.out.println(up_band); 
				if(updateForTodayAndNextDay == true && i== (date.size()-1)){
					sql = "UPDATE "+name+" set ATR="+ATR+", SuperTrend_UP="+supertrend_up+", SuperTrend_Down="+supertrend_down+" "
							+ " , SuperTrend_UP_Band="+up_band+","
							+ " SuperTrend_Down_Band="+down_band+", SuperTrend="+supertrend+", SuperTrend_Reversal='"+direction+"'*1 where tradedate='"+tradeDate+"'";
//					System.out.println(sql);
					executeSqlQuery(con, sql);
				}else if(updateForallDays == true){
					output.write(tradeDate);output.write(","+ATR);	output.write(","+supertrend_up);output.write(","+supertrend_down);
					output.write(","+up_band);	output.write(","+down_band);output.write(","+supertrend);output.write(","+direction);
                	output.write("\r\n");
				}
			}
		}
		if ( output != null ) output.close();
		if(updateForallDays == true){
			
			
			sql = "LOAD DATA LOCAL INFILE '"+path+name+".txt' "+
					 " INTO TABLE "+name+"_Temp "+
					 " FIELDS TERMINATED BY ',' "+
					 " LINES TERMINATED BY '\n'" +
					 " "+
					 " (tradedate, ATR, SuperTrend_UP, SuperTrend_Down, SuperTrend_UP_Band, SuperTrend_Down_Band, SuperTrend, SuperTrend_Reversal)";
           
          		executeSqlQuery(con, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.ATR = b.ATR, a.SuperTrend_UP = b.SuperTrend_UP, a.SuperTrend_Down = b.SuperTrend_Down, a.SuperTrend_UP_Band = b.SuperTrend_UP_Band ,"
				+ " a.SuperTrend_Down_Band = b.SuperTrend_Down_Band, a.SuperTrend = b.SuperTrend, a.SuperTrend_Reversal = b.SuperTrend_Reversal "+
				" WHERE a.tradedate = b.tradedate ";
//          		System.out.println(sql);
          		executeSqlQuery(con, sql);
          		tmp.dropTempTable(con, name);
		}
	}
			
}

