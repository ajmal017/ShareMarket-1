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

public class MovingAvgEnvelope extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      MovingAvgEnvelope env = new MovingAvgEnvelope();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT name FROM symbols");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=false;float diffPerc=0.20f;
	    	  	boolean updateResultTable=false, isIntraDayData=false;
	    	  	boolean insertAllDataToResult = false;
	    	  	String iter="5";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		System.out.println(name);	
	    	  		env.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/envelope/"+iter+"/");
	    	  		env.UpdateENVELOPEResults(dbConnection, name, updateSymbolsTableData, updateAllData, 
	    	  				updateResultTable, diffPerc, isIntraDayData, insertAllDataToResult);
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
   
   
   public void UpdateENVELOPEResults(java.sql.Connection con, String name, boolean updateSymbolsTableData, 
		   boolean updateAllData, boolean updateResultTable, Float diffperc, boolean isIntraDayData, boolean insertAllDataToResult) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	List upper = new ArrayList<Float>();
	  	List lower = new ArrayList<Float>();
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
		  		upper.add(rs.getFloat("UPPER_ENVELP"));
		  		lower.add(rs.getFloat("LOWER_ENVELP"));
    	  		di_plus.add(rs.getFloat("di_plus_average"));
    	  		di_minus.add(rs.getFloat("di_minus_average"));
		  	}
		  	
		  	String envlope_Change_DIR="";
		  	String tradeDate="";
		  	Float profit=0.0f, profitRupees=0.0f;
		  	float filter=0f, filterPerc = 2f;
		  	Float trigger=0f;
		  	if(updateAllData == true){
		  		for (int i=0; i< date.size(); i++){
		  			if(isIntraDayData==true)
	    	  			tradeDate = (String) date.get(i).toString();
	    	  		else
	    	  			tradeDate = date.get(i)+"";
		  			if(i>1){
		  				envlope_Change_DIR = "";
		  				if((float)close.get(i) > (float)upper.get(i) && (float)close.get(i-1) < (float)upper.get(i-1)){
		  					envlope_Change_DIR = "Bull";
				  		}
		  				if((float)close.get(i) < (float)lower.get(i) && (float)close.get(i-1) > (float)lower.get(i-1)){
		  					envlope_Change_DIR = "Bear";
				  		}
		  				if(!envlope_Change_DIR.equals("")){
//		  					Will_Change_DIR="";
		  					sql = "UPDATE "+name+" set ENVLP_CROSS='"+envlope_Change_DIR+"' WHERE  TRADEDATE='"+tradeDate+"'";
		  					executeSqlQuery(con, sql);
		  				}
		  				boolean check=false;
		  				if(envlope_Change_DIR.equalsIgnoreCase("Bull") || insertAllDataToResult == true){
		  					trigger = ((float)high.get(i) - ((float)high.get(i)*diffperc/100.00f));
			  				if(i < date.size()-1){
			  					if(!name.contains("_")){
		  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(),date.get(i).toString(), (float)high.get(i), "Bull","Envelope");
		    	  				}
			  					if((float) high.get(i+1) > (float) trigger){
				  					if((float)open.get(i+1) > (float) trigger){
				  						check=true;
				  						profit = ((float) high.get(i+1) - (float)open.get(i+1))*100/(float)open.get(i+1);
				  						profitRupees = (float) high.get(i+1) - (float)open.get(i+1);
				  					}else{
				  						profit = ((float) high.get(i+1) - (float)trigger)*100/(float)trigger;
				  						profitRupees = (float) high.get(i+1) - (float)trigger;
				  					}
				  					if(((float)high.get(i+1) < (float) open.get(i+1))) {
				  						profit=0.0f;
				  						profitRupees=0.0f;
				  					}
				  					sql = "update "+name+" set MAEnvelope_BULL_PROFIT="+profit+", "
				  							+ "MAEnvelope_BULL_PROFIT_Rs="+profitRupees+" WHERE  TRADEDATE='"+date.get(i+1)+"';";
						  			executeSqlQuery(con, sql);
//						  			System.out.println(sql);
						  			filter = ((float)open.get(i) - (float)low.get(i))*100/(float)open.get(i);
						  			if(check == false){
						  				if(updateResultTable==true  && isIntraDayData==false && filter<filterPerc){
						  					sql = "insert into maenveloperesults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								  					+ " values ('"+name+"', 'Bull', "+trigger+", "+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
						  					if((insertAllDataToResult == true || i==date.size()-2) )
						  					executeSqlQuery(con, sql);
						  				}
						  				
						  			}
				  				}
			  				}
			  			}
		  				if(envlope_Change_DIR.equalsIgnoreCase("Bear") || insertAllDataToResult == true){
		  					trigger = ((float)low.get(i) + ((float)low.get(i)*diffperc/100.00f));
			  				if(i < date.size()-1){
			  					if(!name.contains("_")){
		  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(), date.get(i).toString(),(float)low.get(i), "Bear","Envelope");
		    	  				}
			  					if((float) low.get(i+1) < (float) trigger){
				  					if((float)open.get(i+1) < (float) trigger){
				  						check=true;
				  						profit = ((float) open.get(i+1) - (float)low.get(i+1))*100/(float)open.get(i+1);
				  						profitRupees = (float) open.get(i+1) - (float)low.get(i+1);
				  					}else{
				  						profit = ((float) trigger - (float)low.get(i+1))*100/(float)trigger;
				  						profitRupees = (float) trigger - (float)low.get(i+1);
				  					}
				  					if(((float)low.get(i+1) > (float) open.get(i+1))) {
				  						profit=0.0f;
				  						profitRupees=0.0f;
				  					}
				  					sql = "update "+name+" set MAEnvelope_Bear_PROFIT="+profit+", "
				  							+ "MAEnvelope_Bear_PROFIT_Rs="+profitRupees+" WHERE TRADEDATE='"+date.get(i+1)+"';";
//				  					System.out.println(sql);
						  			executeSqlQuery(con, sql);
						  			filter = ((float)high.get(i) - (float)open.get(i))*100/(float)open.get(i);
						  			if(check == false){
						  				if(updateResultTable==true  && isIntraDayData==false && filter<filterPerc){
						  					sql = "insert into maenveloperesults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								  					+ " values ('"+name+"', 'Bear', "+trigger+", "+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
						  					if((insertAllDataToResult == true || i==date.size()-2))
						  					executeSqlQuery(con, sql);
						  				}
						  				
						  			}
				  				}
			  				}
			  			}
		  			}
		  		}
		  	}
		  	
		  	if(updateSymbolsTableData == true){
		  		sql = "update symbols set MAvgEnvelope_BULL_PROFIT= "+
				  		"(select coalesce(SUM(MAEnvelope_BULL_PROFIT),0) "+
				  		" from "+name+" where (MAEnvelope_BULL_PROFIT<>0 or ENVLP_CROSS='Bull') and tradedate>='2015-01-01') "+
				  		" where name='"+name+"'";
				  	executeSqlQuery(con, sql);
				  	
				sql = "update symbols set MAvgEnvelope_Bear_PROFIT= "+
					  		"(select coalesce(SUM(MAEnvelope_BEAR_PROFIT),0) "+
					  		" from "+name+" where (MAEnvelope_BEAR_PROFIT<>0 or ENVLP_CROSS='Bear') and tradedate>='2015-01-01') "+
					  		" where name='"+name+"'";
					  	executeSqlQuery(con, sql);
					  	
			  	sql="select coalesce(ENVLP_CROSS,'') ENVLP_CROSS from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET MA_ENVLP_Cross=("+sql+") WHERE NAME='"+name+"'";
				executeSqlQuery(con, sql);
				
				sql="select coalesce(UPPER_ENVELP,0) UPPER_ENVELP from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET MA_UPPER_ENVELOPE=("+sql+") WHERE NAME='"+name+"'";
				executeSqlQuery(con, sql);
				
				sql="select coalesce(LOWER_ENVELP,0) LOWER_ENVELP from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET MA_LOWER_ENVELOPE=("+sql+") WHERE NAME='"+name+"'";
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
		  	calculateEnvelope(con, name,open, high, low, close, date, 50, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path);
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
   
  
   
   
	void calculateEnvelope(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, List<Float> close, List date, int envRange,
			boolean updateForTodayAndNextDay, boolean updateForallDays, boolean isIntraDayData, String path) throws IOException, SQLException{
		Float highT=0.0f, lowT=0f, will_R=0f;
		envRange--;int k=0;String sql="", tradeDate="";;
		Float maTemp=0.0f, ma=0.0f, upper=0.0f, lower=0.0f;
		
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
			if(i>= envRange){
				k=0;maTemp=0.0f;
				for(int j=i-envRange; k<=envRange; j++){
					k++;
					maTemp+= close.get(j);
				}
				ma = maTemp/(envRange+1.00f);
				
				
				upper = ma+(ma*0.05f);
				lower = ma-(ma*0.05f);
//				System.out.println(ma+" "+date.get(i)+" ,"+upper+","+lower);
			}
			
			if(updateForTodayAndNextDay == true && i== (date.size()-1)){
				sql = "UPDATE "+name+" set SMA_ENVELP="+ma+",UPPER_ENVELP="+upper+","
						+ "LOWER_ENVELP="+lower+" where  tradedate='"+tradeDate+"'";
//				System.out.println(sql);
				executeSqlQuery(con, sql);
			}else if(updateForallDays == true){
				
				output.write(tradeDate);	output.write(","+ma);	output.write(","+upper);	output.write(","+lower);
            	output.write("\r\n");
			}
			if(updateForTodayAndNextDay == true && (tradeDate.split(" ")[0]).equalsIgnoreCase(todaysDate) && isIntraDayData == true){
				output.write(tradeDate);	output.write(","+ma);	output.write(","+upper);	output.write(","+lower);
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
					 " (tradedate, SMA_ENVELP, UPPER_ENVELP, LOWER_ENVELP) ";
           
          		executeSqlQuery(con, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.SMA_ENVELP = b.SMA_ENVELP, a.UPPER_ENVELP = b.UPPER_ENVELP, a.LOWER_ENVELP = b.LOWER_ENVELP  "+
				" WHERE a.tradedate = b.tradedate ";
//          		System.out.println(sql);
          		executeSqlQuery(con, sql);
          		tmp.dropTempTable(con, name);
		}
	}
			
}

