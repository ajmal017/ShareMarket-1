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

public class RangeCompare extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      RangeCompare adx = new RangeCompare();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 1000000000 order by volume desc ");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=false;
	    	  	boolean updateResultTable=true;float diffPerc=0.20f;boolean isIntraDayData=false;
	    	  	boolean insertAllDataToResult = true;
	    	  	String iter="1d";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		System.out.println(name);	
//	    	  		adx.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/adx/"+iter+"/");
	    	  		adx.UpdateRangeResults(dbConnection, name, updateSymbolsTableData, updateAllData, updateResultTable, 
	    	  				diffPerc, isIntraDayData, insertAllDataToResult);
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
   
   
   public void UpdateRangeResults(java.sql.Connection con, String name, boolean updateSymbolsTableData, boolean updateAllData, 
		   boolean updateResultTable, Float diffperc, boolean isIntraDayData, boolean insertAllDataToResult) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
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
		  	}
		  	
		  	String reversal="";
		  	Float profit=0.0f, profitRupees=0.0f;
		  	Float trigger=0.0f;
		  	float filter=0f, filterPerc=.1f;;
		  	String tradeDate="";
		  	if(updateAllData == true){
		  		for (int i=0; i< date.size(); i++){
		  			if(isIntraDayData==true)
	    	  			tradeDate = (String) date.get(i).toString();
	    	  		else
	    	  			tradeDate = date.get(i)+"";
			  		if(i>1 && i < date.size()-1){
			  			filter = ((float)high.get(i) - (float)open.get(i))*100/(float)open.get(i);
			  			if(filter<filterPerc){
			  				if(!name.contains("_")){
		  						will.calculateIntraDayCross(con, name, date.get(i+1).toString(),date.get(i).toString(), (float)low.get(i), "Bear","Range");
	    	  				}
			  				if(((float)open.get(i+1) > (float)low.get(i)) && (float)low.get(i+1) < (float)low.get(i)
			  						){
			  					profit = ((float)low.get(i) - (float)low.get(i+1))*100/(float)low.get(i);
			  					profitRupees = ((float)low.get(i) - (float)low.get(i+1));
			  					trigger = (float)low.get(i);
			  					reversal = "Bear";
			  					
			  					
			  					if(updateResultTable==true  && isIntraDayData==false){
				  					sql = "insert into rangeresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
						  					+ " values ('"+name+"', 'Bear', "+trigger+","+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
				  					if((insertAllDataToResult == true || i==date.size()-2) )
				  						executeSqlQuery(con, sql);
				  				}
			  					
			  				}
			  			}
			  			
			  			filter = ((float)open.get(i) - (float)low.get(i))*100/(float)open.get(i);
			  			if(filter<filterPerc){
			  				if(!name.contains("_")){
		  						will.calculateIntraDayCross(con, name, date.get(i+1).toString(), date.get(i).toString(),(float)high.get(i), "Bull","Range");
	    	  				}
			  				if(((float)open.get(i+1) < (float)high.get(i)) && (float)high.get(i+1) > (float)high.get(i)
			  						){
			  					profit = ((float)high.get(i+1) - (float)high.get(i))*100/(float)high.get(i);
			  					profitRupees = ((float)high.get(i+1) - (float)high.get(i));
			  					trigger = (float)high.get(i);
			  					reversal = "Bull";
			  					
			  					
			  					
			  					if(updateResultTable==true  && isIntraDayData==false){
				  					sql = "insert into rangeresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
						  					+ " values ('"+name+"', 'Bull', "+trigger+","+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
				  					if((insertAllDataToResult == true || i==date.size()-2) )
				  						executeSqlQuery(con, sql);
				  				}
			  					
			  				}
			  			}
			  		}
		  		}
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
   
   public void LoadData(java.sql.Connection con, String name, boolean updateForTodayAndNextDay, boolean updateForallDays,
		   boolean isIntraDayData, String path) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	try {
			rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, tradedate FROM "+name+"   order by tradedate;");
		 
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getString("tradedate"));
		  	}
//		  	System.out.println(date.size());
		  	calculateADX(con, name,open, high, low, close, date,  14, updateForTodayAndNextDay, updateForallDays, 
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
   
  
   
   
	void calculateADX(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, 
			List<Float> close, List date, int adxRange, boolean updateForTodayAndNextDay, boolean updateForallDays,
			boolean isIntraDayData, String path) throws IOException, SQLException{
		
		
		TemporaryTable tmp = new TemporaryTable();
		Float TR=0.0f, range1=0.0f, range2=0.0f, range3=0.0f, DMPlus=0.0f, DMMinus=0.0f;
		Float TRSum=0.0f, TRAvarage=0.0f, DMPLusSum=0.0f, DMMinusSum=0.0f, DMPlusAvar=0.0f, DMMinusAvar=0.0f;
		Float DIPlusAver=0.0f, DIMinusAver=0.0f, DIDiff=0.0f, DISum=0.0f, DX=0.0f, ADX=0.0f, DX_Sum=0.0f;
		String sql="", tradeDate="";
		adxRange--;int j=1;
		
		BufferedWriter output = null;
	  	tmp.createTempTable(con, name);
	  	File file = new File(""+path+name+".txt");
        output = new BufferedWriter(new FileWriter(file));
        
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
				
				if( ((float) high.get(i) - (float)high.get(i-1)) > ((float) low.get(i-1) - (float)low.get(i))){
					DMPlus = Math.max(((float) high.get(i) - (float)high.get(i-1)), 0);
				}else DMPlus=0.0f;
				DMPLusSum = DMPLusSum + DMPlus;
				
				
				if( ((float) high.get(i) - (float)high.get(i-1)) < ((float) low.get(i-1) - (float)low.get(i))){
					DMMinus = Math.max(((float) low.get(i-1) - (float)low.get(i)), 0);
				}else DMMinus=0.0f;
				
				DMMinusSum = DMMinusSum + DMMinus;
//				System.out.println("DMMinus = "+DMMinus);
				
				
				if(i==adxRange+1){
					TRAvarage = TRSum;
					DMPlusAvar = DMPLusSum;
					DMMinusAvar = DMMinusSum;
				}else if (i>adxRange+1){
					TRAvarage = TRAvarage-(TRAvarage/(adxRange+1.00f)) + TR;
					DMPlusAvar = DMPlusAvar-(DMPlusAvar/(adxRange+1.00f)) + DMPlus;
					DMMinusAvar = DMMinusAvar-(DMMinusAvar/(adxRange+1.00f)) + DMMinus;
				}
				if(i>=(adxRange+1)){
					DIPlusAver = 100.00f * DMPlusAvar/TRAvarage;
					DIMinusAver = 100.00f * DMMinusAvar/TRAvarage;
					DIDiff = Math.abs((float)DIPlusAver - (float)DIMinusAver);
					DISum = (float)DIPlusAver + (float)DIMinusAver;
					DX = 100.00f * DIDiff/DISum;
//					System.out.println("DX="+DX);
					DX_Sum = DX_Sum + DX;
//					System.out.println("ADX="+ADX+ " DXSUM="+DX_Sum+"");
					if(j==(adxRange)){
						ADX = DX_Sum/(adxRange);
					}else if(j>(adxRange)){
						ADX = ((ADX*13.00f) + DX)/14.00f;
					}
					if(updateForTodayAndNextDay == true && i== (date.size()-1)){
						sql = "UPDATE "+name+" set TR="+TR+", DM_1_PLUS="+DMPlus+", DM_1_MINUS="+DMMinus+""
								+ ", TR_AVARAGE="+TRAvarage+", DM_PLUS_AVERAGE="+DMPlusAvar+", DM_MINUS_AVERAGE="
								+ ""+DMMinusAvar+", DI_PLUS_AVERAGE="+DIPlusAver+", DI_MINUS_AVERAGE="+DIMinusAver+
								", DI_DIFF="+DIDiff+", DI_SUM="+DISum+", DX="+DX+", ADX="+ADX+" where ADX=0 and tradedate='"+tradeDate+"'";
//						System.out.println(sql);
					}else if(updateForallDays == true){
						output.write(tradeDate);	output.write(","+TR);	output.write(","+DMPlus);	output.write(","+DMMinus);
						output.write(","+TRAvarage);	output.write(","+DMPlusAvar);	output.write(","+DMMinusAvar);	output.write(","+DIPlusAver);
						output.write(","+DIMinusAver); output.write(","+DIDiff); output.write(","+DISum); output.write(","+DX); output.write(","+ADX);
                    	output.write("\r\n");
                    	
					}
						
						
						try {
							
							if(!sql.equals(""))
							executeSqlQuery(con, sql);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					j++;
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
					 " (tradedate, TR, DM_1_PLUS, DM_1_MINUS, TR_AVARAGE, DM_PLUS_AVERAGE, DM_MINUS_AVERAGE, DI_PLUS_AVERAGE, DI_MINUS_AVERAGE, DI_DIFF, "
					 + " DI_SUM, DX, ADX) ";
           
          		executeSqlQuery(con, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.TR = b.TR, a.DM_1_PLUS = b.DM_1_PLUS, a.DM_1_MINUS = b.DM_1_MINUS, a.TR_AVARAGE = b.TR_AVARAGE, a.DM_PLUS_AVERAGE=b.DM_PLUS_AVERAGE,"
				+ "  a.DM_MINUS_AVERAGE = b.DM_MINUS_AVERAGE, a.DI_PLUS_AVERAGE = b.DI_PLUS_AVERAGE, a.DI_MINUS_AVERAGE = b.DI_MINUS_AVERAGE, "
				+ " a.DI_DIFF = b.DI_DIFF, a.DI_SUM = b.DI_SUM, a.DX = b.DX, a.ADX = b.ADX  "+
				" WHERE a.tradedate = b.tradedate ";
//          		System.out.println(sql);
          		executeSqlQuery(con, sql);
          		tmp.dropTempTable(con, name);
		}
	}
			
}

