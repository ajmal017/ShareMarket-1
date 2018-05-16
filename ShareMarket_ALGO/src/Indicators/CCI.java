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

public class CCI extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      CCI cci = new CCI();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT name FROM symbols  ");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;float diffPerc=0.20f;
	    	  	boolean updateResultTable=false, isIntraDayData=false;;
	    	  	boolean insertAllDataToResult = false; boolean fastProcess=false;
	    	  	String iter="1d";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		System.out.println(name);	
	    	  		cci.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/cci/"+iter+"/");
	    	  		cci.UpdateCCIResults(dbConnection, name, updateSymbolsTableData, updateAllData, 
	    	  				updateResultTable, diffPerc, isIntraDayData, insertAllDataToResult, fastProcess);
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

   public void UpdateCCIResults(java.sql.Connection con, String name, boolean updateSymbolsTableData, 
		   boolean updateAllData, boolean updateResultTable, Float diffPerc, boolean isIntraDayData, boolean insertAllDataToResult, boolean fastProcess) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	List cci = new ArrayList<Float>();
	  	List di_plus = new ArrayList<Float>();
	  	List di_minus = new ArrayList<Float>();
	  	WILLIAMS will = new WILLIAMS();
	  	String sql="";
	  	try {
	  		sql="select * from "+name+" where date(tradedate)>='2016-01-19'";
			rs = executeSelectSqlQuery(con, sql);
		 
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getString("tradedate"));
		  		cci.add(rs.getFloat("CCI"));
    	  		di_plus.add(rs.getFloat("di_plus_average"));
    	  		di_minus.add(rs.getFloat("di_minus_average"));
		  	}
		  	float filter=0f, filterPerc = 2f;
		  	String tradeDate="";
		  	String cci_Change_DIR="";
		  	Float trigger=0.0f;
		  	Float profit=0.0f, profitRupees=0.0f;
		  	
		  	if(updateAllData == true){
		  		for (int i=0; i< date.size(); i++){
		  			if(isIntraDayData==true)
	    	  			tradeDate = (String) date.get(i).toString();
	    	  		else
	    	  			tradeDate = date.get(i)+"";
		  			if(i>1){
		  				cci_Change_DIR = "";
		  				if((float)cci.get(i) > 100 && (float)cci.get(i-1) <100){
		  					cci_Change_DIR = "Bull";
				  		}
		  				if((float)cci.get(i) < -100 && (float)cci.get(i-1) >-100){
		  					cci_Change_DIR = "Bear";
				  		}
		  				if(!cci_Change_DIR.equals("")){
//		  					Will_Change_DIR="";
		  					sql = "UPDATE "+name+" set cci_reversal='"+cci_Change_DIR+"' WHERE cci_reversal='' and TRADEDATE='"+tradeDate+"'";
		  					executeSqlQuery(con, sql);
		  					
		  				}
		  				boolean check=false;
		  				if(fastProcess == false){
		  					if(cci_Change_DIR.equalsIgnoreCase("Bull") || insertAllDataToResult == true){
			  					trigger = ((float)high.get(i) - ((float)high.get(i)*diffPerc/100.00f));
				  				if(i < date.size()-1){
				  					if(!name.contains("_")){
			  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(), date.get(i).toString(),(float)high.get(i), "Bull","CCI");
			    	  				}
				  					if((float) high.get(i+1) > (float) trigger){
					  					if((float)open.get(i+1) > (float) trigger){
					  						check=true;
					  						profit = ((float) high.get(i+1) - (float)open.get(i+1))*100/(float)open.get(i+1);
					  						profitRupees = (float) high.get(i+1) - (float)open.get(i+1);
					  					}else{
					  						profit = ((float) high.get(i+1) - (float)trigger)*100/(float)trigger;
					  						profitRupees = (float) high.get(i+1) - (float)trigger;
					  						
//					  						if(!name.contains("_")){
//					  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(), (float)high.get(i), "Bull","CCI");
//					    	  				}
					  					}
					  					if(((float)high.get(i+1) < (float) open.get(i+1))) {
					  						profit=0.0f;
					  						profitRupees=0.0f;
					  					}
					  					sql = "update "+name+" set CCI_BULL_PROFIT="+profit+", "
					  							+ "CCI_BULL_PROFIT_Rs="+profitRupees+" WHERE TRADEDATE='"+date.get(i+1)+"';";
							  			executeSqlQuery(con, sql);
//							  			System.out.println(sql);
							  			filter = ((float)open.get(i) - (float)low.get(i))*100/(float)open.get(i);
							  			if(check == false){
							  				if(updateResultTable==true  && isIntraDayData==false && filter<filterPerc){
							  					sql = "insert into cciresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									  					+ " values ('"+name+"', 'Bull',"+(float)trigger+",  "+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
							  					if((insertAllDataToResult == true || i==date.size()-2) )
							  						executeSqlQuery(con, sql);
							  				}
							  				
							  			}
					  				}
				  				}
				  			}
			  				if(cci_Change_DIR.equalsIgnoreCase("Bear") || insertAllDataToResult == true){
			  					trigger = ((float)low.get(i) + ((float)low.get(i)*diffPerc/100.00f));
				  				if(i < date.size()-1){
				  					if(!name.contains("_")){
			  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(), date.get(i).toString(),(float)low.get(i), "Bear","CCI");
			    	  				}
				  					if((float) low.get(i+1) < (float) trigger){
					  					if((float)open.get(i+1) < (float) trigger){
					  						check=true;
					  						profit = ((float) open.get(i+1) - (float)low.get(i+1))*100/(float)open.get(i+1);
					  						profitRupees = (float) open.get(i+1) - (float)low.get(i+1);
					  					}else{
					  						profit = ((float) trigger - (float)low.get(i+1))*100/(float)trigger;
					  						profitRupees = (float) trigger - (float)low.get(i+1);
					  						
//					  						if(!name.contains("_")){
//					  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(), (float)low.get(i), "Bear","CCI");
//					    	  				}
					  					}
					  					if(((float)low.get(i+1) > (float) open.get(i+1))) {
					  						profit=0.0f;
					  						profitRupees=0.0f;
					  					}
					  					sql = "update "+name+" set CCI_BEAR_PROFIT="+profit+", "
					  							+ "CCI_BEAR_PROFIT_Rs="+profitRupees+" WHERE TRADEDATE='"+date.get(i+1)+"';";
//					  					System.out.println(sql);
							  			executeSqlQuery(con, sql);
							  			filter = ((float)high.get(i) - (float)open.get(i))*100/(float)open.get(i);
							  			if(check == false){
							  				if(updateResultTable==true  && isIntraDayData==false && filter<filterPerc){
							  					sql = "insert into cciresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
									  					+ " values ('"+name+"', 'Bear', "+(float)trigger+", "+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
							  					if((insertAllDataToResult == true || i==date.size()-2) )
							  					executeSqlQuery(con, sql);
							  				}
							  				
							  			}
					  				}
				  				}
				  			}
		  				}
		  				
		  			}
		  		}
		  	}
		  	
		  	if(updateSymbolsTableData == true){
		  		
				sql="select coalesce(CCI_Reversal,'') CCI_Reversal from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET CCI_Reverse=("+sql+") WHERE NAME='"+name+"'";
				executeSqlQuery(con, sql);
		  	} 	
		  	
		  	
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
//		  		if(rs.getString("Date").equalsIgnoreCase("2016-04-18")){
//		  			rs2 = executeSelectSqlQuery(con, "SELECT open,high,low,close,tradedate  FROM "+name.split("_")[0]+"_5  where tradedate >= '2016-04-18' order by tradedate;");
//		  			while (rs2.next()){
//		  				open.add(rs2.getFloat("open"));
//				  		high.add(rs2.getFloat("high"));
//				  		low.add(rs2.getFloat("low"));
//				  		close.add(rs2.getFloat("close"));
//				  		date.add(rs2.getString("tradedate"));
//		  			}
//		  			break;
//		  		}
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		
		  		date.add(rs.getString("tradedate"));
		  	}
//		  	System.out.println(date.size());
		  	if(rs!=null) rs=null; if(rs2!=null) rs2=null;
//		  	System.out.println(date.size());
		  	calculateCCI(con, name,open, high, low, close, date, 200, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path);
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
   
  
   
   
	void calculateCCI(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, List<Float> close, List date, int cciRange,
			boolean updateForTodayAndNextDay, boolean updateForallDays, boolean isIntraDayData, String path) throws IOException, SQLException{
		Float typicalPrice=0.0f, tpTemp=0.0f, tpAvarage=0.0f, meanDeviationTemp=0.0f, meanDeviation=0.0f, cci=0.0f;
		cciRange--;int k=0;String sql="",tradeDate="";;
		
		TemporaryTable tmp = new TemporaryTable();
		BufferedWriter output = null;
		String todaysDate = executeCountQuery(con, "select date(now())");
		if(updateForallDays == true || isIntraDayData == true){			
		  	tmp.createTempTable(con, name);
		  	File file = new File(""+path+name+".txt");
	        output = new BufferedWriter(new FileWriter(file));	
		}
        
		for (int i=0; i<= date.size()-1; i++){
			if(isIntraDayData==true)
	  			tradeDate = (String) date.get(i).toString();
	  		else
	  			tradeDate = date.get(i)+"";
			typicalPrice = (high.get(i) + low.get(i) + close.get(i))/3.00f;
			if(i>= cciRange){
				k=0;tpTemp=0.0f;meanDeviationTemp=0.0f;
				for(int j=i-cciRange; k<=cciRange; j++){
					k++;
					tpTemp+= ((high.get(j) + low.get(j) + close.get(j))/3.00f);
				}
				tpAvarage = tpTemp / (cciRange+1.00f);
				k=0;
				for(int j=i-cciRange; k<=cciRange; j++){
					k++;
					meanDeviationTemp += Math.abs(tpAvarage - ((high.get(j) + low.get(j) + close.get(j))/3.00f)); 
				}
				meanDeviation = meanDeviationTemp / (cciRange+1.00f);
				cci =(typicalPrice-tpAvarage)/(0.015f*meanDeviation);
				if(Float.isNaN(cci))
					cci = 0.0f;
//				System.out.println("TP="+typicalPrice+",TPAVG="+tpAvarage+",DEV="+meanDeviation+",CCI="+cci+",date="+date.get(i));
			}
			
			if(updateForTodayAndNextDay == true && i== (date.size()-1)){
				sql = "UPDATE "+name+" set Typical_Price="+typicalPrice+",Typical_Price_Mean="+tpAvarage+","
						+ "Mean_Deviation="+meanDeviation+",CCI="+cci+" where  tradedate='"+tradeDate+"'";
//				System.out.println(sql);
				executeSqlQuery(con, sql);
			}else if(updateForallDays == true){
				output.write(tradeDate);	output.write(","+typicalPrice);	output.write(","+tpAvarage);	output.write(","+meanDeviation);
				output.write(","+cci); 
            	output.write("\r\n");
			}
			if(updateForTodayAndNextDay == true && (tradeDate.split(" ")[0]).equalsIgnoreCase(todaysDate) && isIntraDayData == true){
				output.write(tradeDate);	output.write(","+typicalPrice);	output.write(","+tpAvarage);	output.write(","+meanDeviation);
				output.write(","+cci); 
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
					 " (tradedate, Typical_Price, Typical_Price_Mean, Mean_Deviation, CCI) ";
           
          		executeSqlQuery(con, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.Typical_Price = b.Typical_Price, a.Typical_Price_Mean = b.Typical_Price_Mean, a.Mean_Deviation = b.Mean_Deviation, a.CCI = b.CCI  "+
				" WHERE a.tradedate = b.tradedate ";
//          		System.out.println(sql);
          		executeSqlQuery(con, sql);
          		tmp.dropTempTable(con, name);
		}
	}
			
}

