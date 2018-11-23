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

import org.apache.poi.hssf.record.DBCellRecord;

public class ADX extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      ADX adx = new ADX();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT name FROM symbols where totaltrades>5000");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
	    	  	boolean updateResultTable=true;float diffPerc=0.20f;boolean isIntraDayData=true;
	    	  	boolean insertAllDataToResult = false;
	    	  	String iter="60";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		if(!name.equals("1d")){
	    	  			name+="_"+iter;
	    	  		}
	    	  		System.out.println(name);	
	    	  		adx.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/adx/"+iter+"/");
//	    	  		adx.UpdateADXResults(dbConnection, name, updateSymbolsTableData, updateAllData, updateResultTable, 
//	    	  				diffPerc, isIntraDayData, insertAllDataToResult);
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
   
  
   public void UpdateADXResults(java.sql.Connection con, String name, boolean updateSymbolsTableData, boolean updateAllData, 
		   boolean updateResultTable, Float diffperc, boolean isIntraDayData, boolean insertAllDataToResult) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	List DM_PLUS_AVER = new ArrayList<Float>();
	  	List DM_Minus_AVER = new ArrayList<Float>();
	  	List di_plus = new ArrayList<Float>();
	  	List di_minus = new ArrayList<Float>();
	  	List ADX = new ArrayList<Float>();
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
		  		DM_PLUS_AVER.add(rs.getFloat("DM_PLUS_AVERAGE"));
		  		DM_Minus_AVER.add(rs.getFloat("DM_MINUS_AVERAGE"));
		  		ADX.add(rs.getFloat("ADX"));
    	  		di_plus.add(rs.getFloat("di_plus_average"));
    	  		di_minus.add(rs.getFloat("di_minus_average"));
		  	}
		  	
		  	String ADX_Change_DIR="", ADX_bull_entered="", ADX_bear_entered="";
		  	Float profit=0.0f, profitRupees=0.0f;
		  	Float trigger=0.0f;
		  	String tradeDate="";
		  	float filter=0f, filterPerc = 0.1f;
		  	if(updateAllData == true){
		  		for (int i=0; i< date.size(); i++){
		  			if(isIntraDayData==true)
	    	  			tradeDate = (String) date.get(i).toString();
	    	  		else
	    	  			tradeDate = date.get(i)+"";
			  		if(i>1){
			  			
			  			if((float)DM_PLUS_AVER.get(i) - (float)DM_Minus_AVER.get(i) > 0){
			  				if((float)DM_PLUS_AVER.get(i-1) - (float)DM_Minus_AVER.get(i-1) < 0){
			  					ADX_Change_DIR = "Bull";
			  				}
			  			}else{
			  				if((float)DM_PLUS_AVER.get(i-1) - (float)DM_Minus_AVER.get(i-1) > 0){
			  					ADX_Change_DIR = "Bear";
			  				}
			  			}
			  			if(!ADX_Change_DIR.equals("")){
			  				sql = "update "+name+" set ADX_DM_Crossover='"+ADX_Change_DIR+"' WHERE ADX_DM_Crossover='' and TRADEDATE='"+tradeDate+"';";
				  			executeSqlQuery(con, sql);
//				  			System.out.println(sql);
			  			}
			  			
			  			if( (float)DM_PLUS_AVER.get(i) > ((float)DM_Minus_AVER.get(i)*2)  && (float)ADX.get(i) > 25){
			  				ADX_bull_entered="Yes";
			  				sql = "update "+name+" set ADX_Bullish_Enter='"+ADX_bull_entered+"' WHERE ADX_Bullish_Enter='' and TRADEDATE='"+tradeDate+"';";
				  			executeSqlQuery(con, sql);
			  				
			  				if(i < date.size()-1){
			  					if((float) open.get(i+1) < (float) high.get(i) && ((float) high.get(i+1) > (float) high.get(i))){
				  					profit = ((float) high.get(i+1) - (float)high.get(i))*100/(float)high.get(i);
				  					profitRupees = (float) high.get(i+1) - (float)high.get(i);
				  					if(profit==0) profit = -0.0000001f;
				  					sql = "update "+name+" set ADX_Bull_Entered_profit="+profit+","
				  							+ "ADX_BULL_ENTERED_PROFIT_Rs="+profitRupees+" WHERE ADX_Bull_Entered_profit=0 and TRADEDATE='"+date.get(i+1)+"';";
						  			executeSqlQuery(con, sql);
						  			profitRupees=0f;
				  				}
			  					
			  				}
				  			
			  			}
			  			
			  			if( (float)DM_Minus_AVER.get(i) > ((float)DM_PLUS_AVER.get(i)*2)  && (float)ADX.get(i) > 25){
			  				ADX_bear_entered="Yes";
			  				
			  				sql = "update "+name+" set ADX_Bearish_Enter='"+ADX_bear_entered+"' WHERE ADX_Bearish_Enter='' and  TRADEDATE='"+tradeDate+"';";
				  			executeSqlQuery(con, sql);
			  				if(i < date.size()-1){
			  					if((float) open.get(i+1) > (float) low.get(i) && (float) low.get(i+1) < (float) low.get(i)){
				  					profit = ((float) low.get(i) - (float)low.get(i+1))*100/(float)low.get(i);
				  					profitRupees = (float) low.get(i) - (float)low.get(i+1);
				  					if(profit==0) profit = -0.0000001f;
				  					sql = "update "+name+" set ADX_Bear_Entered_profit="+profit+","
				  							+ "ADX_BEAR_ENTERED_PROFIT_Rs="+profitRupees+" WHERE ADX_Bear_Entered_profit=0 and TRADEDATE='"+date.get(i+1)+"';";
						  			executeSqlQuery(con, sql);
						  			profitRupees=0f;
				  				}
			  					
			  				}
			  				
			  			}
//			  			System.out.println(date.size()-1);	
			  			boolean check=false;
			  			
			  			if((!ADX_Change_DIR.equals("") && i < date.size()-1) || insertAllDataToResult == true){
			  				
				  			if(ADX_Change_DIR.equalsIgnoreCase("Bull") || insertAllDataToResult == true){
				  				trigger = ((float)high.get(i) - ((float)high.get(i)*diffperc/100.00f));
//				  				trigger=(float)high.get(i);
				  				if(i <= date.size()){
				  					if(!name.contains("_")){
			  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(),date.get(i).toString(), (float)high.get(i), "Bull", "ADX");
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
//					  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(), (float)high.get(i), "Bull", "ADX");
//					    	  				}
					  						
					  					}
					  					if(((float)high.get(i+1) < (float) open.get(i+1))) {
					  						profit=0.0f;
					  						profitRupees=0.0f;
					  					}
					  					sql = "update "+name+" set ADX_Bull_PROFIT_ON_DM_CROSS="+profit+", "
					  							+ "ADX_Bull_PROFIT_RUPEES="+profitRupees+" WHERE ADX_Bull_PROFIT_ON_DM_CROSS=0 and TRADEDATE='"+date.get(i+1)+"';";
							  			executeSqlQuery(con, sql);
							  			filter = ((float)open.get(i) - (float)low.get(i))*100/(float)open.get(i);
							  			if(check==false){
							  				if(updateResultTable==true  && isIntraDayData==false && filter<filterPerc){
							  					sql = "insert into ADXresults(name, reversal,triggerPrice, profitPerc, profitRupees, date) "
									  					+ " values ('"+name+"', 'Bull', "+trigger+", "+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
							  					if((insertAllDataToResult == true || i==date.size()-2) )
							  					executeSqlQuery(con, sql);
							  				}
							  				
							  			}
					  				}
				  				}
				  			}
				  			
				  			if(ADX_Change_DIR.equalsIgnoreCase("Bear") || insertAllDataToResult == true){
				  				trigger = ((float)low.get(i) + ((float)low.get(i)*diffperc/100.00f));
//				  				trigger=(float)low.get(i);
				  				if(!name.contains("_")){
		  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(),date.get(i).toString(), (float)low.get(i), "Bear", "ADX");
		    	  				}
				  				if(i <= date.size()){
				  					if((float) low.get(i+1) < (float) trigger){
					  					if((float)open.get(i+1) < (float) trigger){
					  						check=true;
					  						profit = ((float) open.get(i+1) - (float)low.get(i+1))*100/(float)open.get(i+1);
					  						profitRupees = (float) open.get(i+1) - (float)low.get(i+1);
					  					}else{
					  						profit = ((float) trigger - (float)low.get(i+1))*100/(float)trigger;
					  						profitRupees = (float) trigger - (float)low.get(i+1);
					  						
//					  						if(!name.contains("_")){
//					  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(), (float)low.get(i), "Bear", "ADX");
//					    	  				}
					  					}
					  					if(((float)low.get(i+1) > (float) open.get(i+1))) {
					  						profit=0.0f;
					  						profitRupees=0.0f;
					  					}
					  					sql = "update "+name+" set ADX_Bear_PROFIT_ON_DM_CROSS="+profit+", "
					  							+ " ADX_Bear_PROFIT_RUPEES = "+profitRupees+" WHERE ADX_Bear_PROFIT_ON_DM_CROSS=0 and TRADEDATE='"+date.get(i+1)+"';";
//					  					System.out.println(sql);
							  			executeSqlQuery(con, sql);
							  			filter = ((float)high.get(i) - (float)open.get(i))*100/(float)open.get(i);
							  			if(check==false){
							  				if(updateResultTable==true  && isIntraDayData==false && filter<filterPerc){
							  					sql = "insert into ADXresults(name, reversal,triggerPrice, profitPerc, profitRupees, date) "
									  					+ " values ('"+name+"', 'Bear', "+trigger+", "+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
							  					if((insertAllDataToResult == true || i==date.size()-2) )
							  					executeSqlQuery(con, sql);
							  				}
							  				
							  			}
					  				}
				  				}
				  			}
//				  			profit=0.0f;
				  			
//				  			System.out.println(sql);
				  			ADX_Change_DIR="";profit=0.0f;profitRupees=0.0f;
			  			}
			  			
			  		}
			  		
			  	}
		  	}
		  	
		  	if(updateSymbolsTableData == true && isIntraDayData==false){
		  		sql = "update symbols set ADXBullProfitOnDMCross= "+
				  		"(select SUM(ADX_BULL_PROFIT_ON_dM_CROSS) "+
				  		" from "+name+" where (ADX_BULL_PROFIT_ON_dM_CROSS<>0 or ADX_DM_Crossover='Bull') and tradedate>='2015-01-01') "+
				  		" where name='"+name+"'";
				  	executeSqlQuery(con, sql);
				sql = "update symbols set ADXBearProfitOnDMCross= "+
					  		"(select SUM(ADX_BEAR_PROFIT_ON_DM_CROSS) "+
					  		" from "+name+" where (ADX_BEAR_PROFIT_ON_DM_CROSS<>0 or ADX_DM_Crossover='Bear') and tradedate>='2015-01-01') "+
					  		" where name='"+name+"'";
					executeSqlQuery(con, sql);
					
				sql = "update symbols set ADX_Bull_Enter_profit= "+
					  		"(select sum(ADX_BULL_ENTERED_PROFIT) from "+name+" where (ADX_BULL_ENTERED_PROFIT<>0 or ADX_Bullish_Enter='yes') and tradedate>'2015-01-01') "+
					  		" where name='"+name+"'";
					executeSqlQuery(con, sql);
				sql = "update symbols set ADX_Bear_Enter_profit= "+
					  		"(select sum(ADX_Bear_ENTERED_PROFIT) from "+name+" where (ADX_Bear_ENTERED_PROFIT<>0 or ADX_Bearish_Enter='yes') and tradedate>'2015-01-01') "+
					  		" where name='"+name+"'";
					executeSqlQuery(con, sql);
					
				//---------update adx previous day data in symbols table for all symbol 
				sql="select coalesce(DM_PLUS_AVERAGE,0) DM_PLUS_AVERAGE from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET ADX_DM_PLUS_AVG=("+sql+") WHERE NAME='"+name+"'";
				executeSqlQuery(con, sql);
				
				sql="select coalesce(DM_MINUS_AVERAGE,0) DM_MINUS_AVERAGE from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET ADX_DM_MINUS_AVG=("+sql+") WHERE NAME='"+name+"'";
				executeSqlQuery(con, sql);
				
				sql="select coalesce(ADX_DM_Crossover,'') ADX_DM_Crossover from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET ADX_DM_Crossovr=("+sql+") WHERE NAME='"+name+"'";
				executeSqlQuery(con, sql);
				
				sql="select coalesce(ADX_Bullish_Enter,'') ADX_Bullish_Enter from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET ADX_Is_Bull_Strong=("+sql+") WHERE NAME='"+name+"'";
				executeSqlQuery(con, sql);
				
				sql="select coalesce(ADX_Bearish_Enter,'') ADX_Bearish_Enter from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET ADX_Is_Bear_Strong=("+sql+") WHERE NAME='"+name+"'";
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
   
  
   
   
	void calculateADX(java.sql.Connection dbConnection, String name, List<Float> open, List<Float> high,  List<Float> low, 
			List<Float> close, List date, int adxRange, boolean updateForTodayAndNextDay, boolean updateForallDays,
			boolean isIntraDayData, String path) throws IOException, SQLException{
		
		
		TemporaryTable tmp = new TemporaryTable();
		Float TR=0.0f, range1=0.0f, range2=0.0f, range3=0.0f, DMPlus=0.0f, DMMinus=0.0f;
		Float TRSum=0.0f, TRAvarage=0.0f, DMPLusSum=0.0f, DMMinusSum=0.0f, DMPlusAvar=0.0f, DMMinusAvar=0.0f;
		Float DIPlusAver=0.0f, DIMinusAver=0.0f, DIDiff=0.0f, DISum=0.0f, DX=0.0f, ADX=0.0f, DX_Sum=0.0f;
		String sql="", tradeDate="";
		BufferedWriter output = null;
		adxRange--;int j=1;
		if(updateForallDays == true || isIntraDayData == true){			
		  	tmp.createTempTable(dbConnection, name);
		  	File file = new File(""+path+name+".txt");
	        output = new BufferedWriter(new FileWriter(file));
		}
		String todaysDate = executeCountQuery(dbConnection, "select date(now())");
        
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
								", DI_DIFF="+DIDiff+", DI_SUM="+DISum+", DX="+DX+", ADX="+ADX+" where  tradedate='"+tradeDate+"'";
//						System.out.println((tradeDate.split(" ")[0]).equalsIgnoreCase(todaysDate));
						executeSqlQuery(dbConnection, sql);
					}else if(updateForallDays == true){
						output.write(tradeDate);	output.write(","+TR);	output.write(","+DMPlus);	output.write(","+DMMinus);
						output.write(","+TRAvarage);	output.write(","+DMPlusAvar);	output.write(","+DMMinusAvar);	output.write(","+DIPlusAver);
						output.write(","+DIMinusAver); output.write(","+DIDiff); output.write(","+DISum); output.write(","+DX); output.write(","+ADX);
                    	output.write("\r\n");
                    	
					}
					if(updateForTodayAndNextDay == true && (tradeDate.split(" ")[0]).equalsIgnoreCase(todaysDate) && isIntraDayData == true){
						output.write(tradeDate);	output.write(","+TR);	output.write(","+DMPlus);	output.write(","+DMMinus);
						output.write(","+TRAvarage);	output.write(","+DMPlusAvar);	output.write(","+DMMinusAvar);	output.write(","+DIPlusAver);
						output.write(","+DIMinusAver); output.write(","+DIDiff); output.write(","+DISum); output.write(","+DX); output.write(","+ADX);
                    	output.write("\r\n");
					}
					
					j++;
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
					 " (tradedate, TR, DM_1_PLUS, DM_1_MINUS, TR_AVARAGE, DM_PLUS_AVERAGE, DM_MINUS_AVERAGE, DI_PLUS_AVERAGE, DI_MINUS_AVERAGE, DI_DIFF, "
					 + " DI_SUM, DX, ADX) ";
           
          		executeSqlQuery(dbConnection, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.TR = b.TR, a.DM_1_PLUS = b.DM_1_PLUS, a.DM_1_MINUS = b.DM_1_MINUS, a.TR_AVARAGE = b.TR_AVARAGE, a.DM_PLUS_AVERAGE=b.DM_PLUS_AVERAGE,"
				+ "  a.DM_MINUS_AVERAGE = b.DM_MINUS_AVERAGE, a.DI_PLUS_AVERAGE = b.DI_PLUS_AVERAGE, a.DI_MINUS_AVERAGE = b.DI_MINUS_AVERAGE, "
				+ " a.DI_DIFF = b.DI_DIFF, a.DI_SUM = b.DI_SUM, a.DX = b.DX, a.ADX = b.ADX  "+
				" WHERE a.tradedate = b.tradedate ";
//          		System.out.println(sql);
          		executeSqlQuery(dbConnection, sql);
          		tmp.dropTempTable(dbConnection, name);
		}
	}
			
}

