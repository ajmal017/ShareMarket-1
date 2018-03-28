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

public class MACD extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      MACD macd = new MACD();
	      java.sql.Connection dbConnection = null;
	      try{
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT name FROM symbols where volume>10000000 ");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
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
	    	  		macd.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/macd/"+iter+"/");
//	    	  		macd.UpdateMACDResults(dbConnection, name, updateSymbolsTableData, updateAllData, 
//	    	  				updateResultTable, isIntraDayData, insertAllDataToResult);
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
   
   public void UpdateMACDResults(java.sql.Connection con, String name, boolean updateSymbolsTableData, 
		   boolean updateAllData, boolean updateResultTable, boolean isIntraDayData, boolean insertAllDataToResult) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	List macd = new ArrayList<Float>();
	  	List dir = new ArrayList<String>();List histogram = new ArrayList<Float>();
	  	List di_plus = new ArrayList<Float>();
	  	List di_minus = new ArrayList<Float>();
	  	WILLIAMS will = new WILLIAMS();
	  	String sql="";
	  	Float profit=0.0f, profitRupees=0.0f;
	  	try {
	  		sql="select histogram, case when macd>sig then 'MACDUP' else (case when macd<sig then 'MACDDOWN' else '' end ) end as Direction, tradedate, open, high, low, close, macd "+
	  				", di_plus_average, di_minus_average from "+name+" ";
			rs = executeSelectSqlQuery(con, sql);
		 
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		macd.add(rs.getFloat("macd"));
		  		date.add(rs.getString("tradedate"));
		  		dir.add(rs.getString("Direction"));
		  		histogram.add(rs.getFloat("histogram"));
    	  		di_plus.add(rs.getFloat("di_plus_average"));
    	  		di_minus.add(rs.getFloat("di_minus_average"));
		  	}
		  	String tradeDate="";
		  	String macd_change_dir="", macd_cross_zero="", hist_dir="";
		  	float filter=0f, filterPerc = 2f;
		  	if(updateAllData == true){
		  		for (int i=0; i< date.size(); i++){
		  			if(isIntraDayData==true)
	    	  			tradeDate = (String) date.get(i).toString();
	    	  		else
	    	  			tradeDate = date.get(i)+"";
			  		if(i>1){
			  			macd_change_dir="";
			  			macd_cross_zero="";
			  			if(dir.get(i).equals("MACDUP") && dir.get(i-1).equals("MACDDOWN")){
			  				macd_change_dir = "UP";
				  		}
			  			if(dir.get(i).equals("MACDDOWN") && dir.get(i-1).equals("MACDUP")){
			  				macd_change_dir = "DOWN";
				  		}
			  			if((float)histogram.get(i) > (float)histogram.get(i-1)){
			  				hist_dir = "Green";
				  		}else hist_dir = "Red";
			  			
			  			if((float)macd.get(i) > 0 && (float)macd.get(i-1) <0){
			  				macd_cross_zero="Bullish Cross";
			  			}
			  			if((float)macd.get(i) < 0 && (float)macd.get(i-1) >0){
			  				macd_cross_zero="Bearish Cross";
			  			}
//			  			sql="UPDATE "+name+" SET MACD_CHANGE_DIR='' WHERE MACD_CHANGE_DIR<>'' and TRADEDATE='"+tradeDate+"';";
//			  			executeSqlQuery(con, sql);
//			  			
//			  			sql="UPDATE "+name+" SET Hist_Zero_Cross='' WHERE Hist_Zero_Cross<>'' and TRADEDATE='"+tradeDate+"';";
//			  			executeSqlQuery(con, sql);
			  			if(!macd_change_dir.equals("")){
			  				sql="UPDATE "+name+" SET MACD_CHANGE_DIR='"+macd_change_dir+"' WHERE  TRADEDATE='"+tradeDate+"';";
			  				executeSqlQuery(con, sql);
			  			}
			  			if(!macd_cross_zero.equals("")){
			  				sql="UPDATE "+name+" SET Hist_Zero_Cross='"+macd_cross_zero+"' WHERE  TRADEDATE='"+tradeDate+"';";
			  				executeSqlQuery(con, sql);
			  			}
			  			if(!hist_dir.equals("")){
			  				sql="UPDATE "+name+" SET hist_dir='"+hist_dir+"' WHERE  TRADEDATE='"+tradeDate+"';";
			  				executeSqlQuery(con, sql);
			  			}
			  			boolean check=false;
			  			if(macd_change_dir.equalsIgnoreCase("UP") || insertAllDataToResult == true){
			  				if(i < date.size()-1){
			  					if(!name.contains("_")){
		  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(),date.get(i).toString(), (float)high.get(i), "Bull","MACD");
		    	  				}
			  					if((float) high.get(i+1) > (float) high.get(i)){
				  					if((float)open.get(i+1) > (float) high.get(i)){
				  						check=true;
//				  						profit = ((float) high.get(i+1) - (float)open.get(i+1))*100/(float)open.get(i+1);
//				  						profitRupees = (float) high.get(i+1) - (float)open.get(i+1);
				  					}else{
				  						profit = ((float) high.get(i+1) - (float)high.get(i))*100/(float)high.get(i);
				  						profitRupees = (float) high.get(i+1) - (float)high.get(i);
				  						
//				  						if(!name.contains("_")){
//				  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(), (float)high.get(i), "Bull","MACD");
//				    	  				}
				  					}
				  					if(((float)high.get(i+1) < (float) open.get(i+1))) {
				  						profit=0.0f;
				  						profitRupees=0.0f;
				  					}
				  					sql = "update "+name+" set MACD_BULL_PROFIT="+profit+", "
				  							+ "MACD_BULL_PROFIT_Rs="+profitRupees+" WHERE  TRADEDATE='"+tradeDate+"';";
						  			executeSqlQuery(con, sql);
						  			filter = ((float)open.get(i) - (float)low.get(i))*100/(float)open.get(i);
						  			if(check == false){
						  				if(updateResultTable==true  && isIntraDayData==false && filter<filterPerc){
						  					sql = "insert into macdresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								  					+ " values ('"+name+"', 'UP', "+high.get(i)+","+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
						  					if((insertAllDataToResult == true || i==date.size()-2) )
						  						executeSqlQuery(con, sql);
						  				}

						  			}
//						  			System.out.println(sql);
				  				}
			  				}
			  			}
			  			
			  			if(macd_change_dir.equalsIgnoreCase("DOWN") || insertAllDataToResult == true){
			  				if(i < date.size()-1){
			  					if(!name.contains("_")){
		  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(),date.get(i).toString(), (float)low.get(i), "Bear","MACD");
		    	  				}
			  					if((float) low.get(i+1) < (float) low.get(i)){
				  					if((float)open.get(i+1) < (float) low.get(i)){
				  						check=true;
//				  						profit = ((float) open.get(i+1) - (float)low.get(i+1))*100/(float)open.get(i+1);
//				  						profitRupees = (float) open.get(i+1) - (float)low.get(i+1);
				  					}else{
				  						profit = ((float) low.get(i) - (float)low.get(i+1))*100/(float)low.get(i);
				  						profitRupees = (float) low.get(i) - (float)low.get(i+1);
				  						
//				  						if(!name.contains("_")){
//				  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(), (float)low.get(i), "Bear","MACD");
//				    	  				}
				  					}
				  					if(((float)low.get(i+1) > (float) open.get(i+1))) {
				  						profit=0.0f;
				  						profitRupees=0.0f;
				  					}
				  					sql = "update "+name+" set MACD_BEAR_PROFIT="+profit+", "
				  							+ "MACD_BEAR_PROFIT_Rs="+profitRupees+" WHERE  TRADEDATE='"+tradeDate+"';";
//				  					System.out.println(sql);
						  			executeSqlQuery(con, sql);
						  			filter = ((float)high.get(i) - (float)open.get(i))*100/(float)open.get(i);
						  			if(check == false){
						  				if(updateResultTable==true  && isIntraDayData==false && filter<filterPerc){
						  					sql = "insert into macdresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								  					+ " values ('"+name+"', 'DOWN', "+low.get(i)+","+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
						  					if((insertAllDataToResult == true || i==date.size()-2) )
						  						executeSqlQuery(con, sql);
						  				}
						  				
						  			}
				  				}
			  				}
			  			}
			  			
			  			if(macd_cross_zero.equalsIgnoreCase("Bullish Cross") || insertAllDataToResult == true){
			  				if(i < date.size()-1){
			  					if(!name.contains("_")){
		  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(), date.get(i).toString(),(float)high.get(i), "Bull","MACD");
		    	  				}
			  					if((float) high.get(i+1) > (float) high.get(i)){
				  					if((float)open.get(i+1) > (float) high.get(i)){
				  						check=true;
//				  						profit = ((float) high.get(i+1) - (float)open.get(i+1))*100/(float)open.get(i+1);
//				  						profitRupees = (float) high.get(i+1) - (float)open.get(i+1);
				  					}else{
				  						profit = ((float) high.get(i+1) - (float)high.get(i))*100/(float)high.get(i);
				  						profitRupees = (float) high.get(i+1) - (float)high.get(i);
				  						
//				  						if(!name.contains("_")){
//				  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(), (float)high.get(i), "Bull","MACD");
//				    	  				}
				  					}
				  					if(((float)high.get(i+1) < (float) open.get(i+1))) {
				  						profit=0.0f;
				  						profitRupees=0.0f;
				  					}
				  					sql = "update "+name+" set MACD_BULL_PROFIT="+profit+", "
				  							+ "MACD_BULL_PROFIT_Rs="+profitRupees+" WHERE  TRADEDATE='"+tradeDate+"';";
						  			executeSqlQuery(con, sql);
//						  			System.out.println(sql);
						  			filter = ((float)open.get(i) - (float)low.get(i))*100/(float)open.get(i);
						  			if(check == false){
						  				if(updateResultTable==true  && isIntraDayData==false && filter<filterPerc){
						  					sql = "insert into macdresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								  					+ " values ('"+name+"', 'Bullish Cross', "+high.get(i)+","+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
						  					if((insertAllDataToResult == true || i==date.size()-2) )
						  						executeSqlQuery(con, sql);
						  				}
						  				
						  			}
				  				}
			  				}
			  			}
			  			
			  			if(macd_cross_zero.equalsIgnoreCase("Bearish Cross") || insertAllDataToResult == true){
			  				if(i < date.size()-1){
			  					if(!name.contains("_")){
		  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(), date.get(i).toString(),(float)low.get(i), "Bear","MACD");
		    	  				}
			  					if((float) low.get(i+1) < (float) low.get(i)){
				  					if((float)open.get(i+1) < (float) low.get(i)){
				  						check=true;
//				  						profit = ((float) open.get(i+1) - (float)low.get(i+1))*100/(float)open.get(i+1);
//				  						profitRupees = (float) open.get(i+1) - (float)low.get(i+1);
				  					}else{
				  						profit = ((float) low.get(i) - (float)low.get(i+1))*100/(float)low.get(i);
				  						profitRupees = (float) low.get(i) - (float)low.get(i+1);
				  						
//				  						if(!name.contains("_")){
//				  							will.calculateIntraDayCross(con, name, date.get(i+1).toString(), (float)low.get(i), "Bear","MACD");
//				    	  				}
				  					}
				  					if(((float)low.get(i+1) > (float) open.get(i+1))) {
				  						profit=0.0f;
				  						profitRupees=0.0f;
				  					}
				  					sql = "update "+name+" set MACD_BEAR_PROFIT="+profit+", "
				  							+ "MACD_BEAR_PROFIT_Rs="+profitRupees+" WHERE  TRADEDATE='"+tradeDate+"';";
//				  					System.out.println(sql);
						  			executeSqlQuery(con, sql);
						  			filter = ((float)high.get(i) - (float)open.get(i))*100/(float)open.get(i);
						  			if(check == false){
						  				if(updateResultTable==true  && isIntraDayData==false && filter<filterPerc){
						  					sql = "insert into macdresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								  					+ " values ('"+name+"', 'Bearish Cross', "+low.get(i)+","+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
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
		  	
		  	
		  	if(updateSymbolsTableData == true){
		  		sql = "update symbols set MACDBullResultOnChangeInDir= "+
				  		"(select coalesce(SUM(MACD_Bull_profit),0) "+
				  		" from "+name+" where (MACD_Bull_profit<>0 and MACD_CHANGE_DIR='UP') and tradedate>='2015-01-01') "+
				  		" where name='"+name+"'";
				  	executeSqlQuery(con, sql);
				sql = "update symbols set MACDBearResultOnChangeInDir= "+
					  		"(select coalesce(SUM(MACD_Bear_profit),0) "+
					  		" from "+name+" where (MACD_Bear_profit<>0 and MACD_CHANGE_DIR='DOWN') and tradedate>='2015-01-01') "+
					  		" where name='"+name+"'";
					  	executeSqlQuery(con, sql);
					  	
				sql = "update symbols set MACDBullResultOnHistogram= "+
						  		"(select coalesce(SUM(MACD_Bull_profit),0) "+
						  		" from "+name+" where (MACD_Bull_profit<>0 and Hist_zero_cross='Bullish Cross') and tradedate>='2015-01-01') "+
						  		" where name='"+name+"'";
						  	executeSqlQuery(con, sql);
			    sql = "update symbols set MACDBearResultOnHistogram= "+
							  		"(select coalesce(SUM(MACD_Bear_profit),0) "+
							  		" from "+name+" where (MACD_Bear_profit<>0 and Hist_zero_cross='Bearish Cross') and tradedate>='2015-01-01') "+
							  		" where name='"+name+"'";
							  	executeSqlQuery(con, sql);
				sql="select coalesce(macd_change_dir,'') macd_change_dir from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET MACD_DIR=("+sql+") WHERE NAME='"+name+"'";
				executeSqlQuery(con, sql);
				
				sql="select coalesce(hist_Zero_cross,'') hist_Zero_cross from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET MACD_HIST_CROSS=("+sql+") WHERE NAME='"+name+"'";
				executeSqlQuery(con, sql);
				
				sql="select coalesce(high,0) high from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET LAST_HIGH=("+sql+") WHERE NAME='"+name+"'";
				executeSqlQuery(con, sql);
				
				sql="select coalesce(low,0) low from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET LAST_LOW=("+sql+") WHERE NAME='"+name+"'";
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
	  	String sqlJoin="";
	  	try {
	  		String todaysDate = executeCountQuery(con, "select date(now())");
	  		if(updateForTodayAndNextDay==true && updateForallDays==false){
	  			sqlJoin = " where date(tradedate) = '"+todaysDate+"'";
	  		}
			rs = executeSelectSqlQuery(con, "SELECT * FROM "+name+"  "+sqlJoin);
		 
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getString("tradedate"));
		  	}
		  	calculateMACD(con, name,open, high, low, close, date, 12, 26, 9, updateForTodayAndNextDay, 
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
   
   void calcCrossoverPrice(float open, float high, float low, float close, String bullorBear, float FirstEmaTemp,
		   float SecEmaTemp, float signal){
	   Float ema_1_sum=0.0f,ema_2_sum=0.0f, macdTemp=0.0f;
		Float macdSumForSignal=0.0f,  histogram=0.0f, FirstEmaTemp2=FirstEmaTemp, SecEmaTemp2=SecEmaTemp, signalTemp=signal, tmp=0.0f;
		tmp=low;
		for(float i=.05f; tmp < high;){
			FirstEmaTemp= FirstEmaTemp2*(1.00f-(2.00f/(12.00f+1.00f))) + (((Float)tmp)*(2.00f/(12.00f+1.00f)));
			
			SecEmaTemp = (Float)tmp*(2.00f/(26.00f+1.00f)) + SecEmaTemp2*(1.00f-(2.00f/(26.00f+1.00f)));
					
			macdTemp = FirstEmaTemp-SecEmaTemp;

			signal = ((float)macdTemp*(2.00f/(9.00f+1.00f))+(float)signalTemp*(1.00f-(2.00f/(9.00f+1.00f))));
			
			if(bullorBear.equalsIgnoreCase("Bull")){
				if(macdTemp < signal){
					System.out.println("changed to Bear at "+tmp+" open="+open+" high="+high+" low="+low+" close="+close);
					break;
				}
			}
			if(bullorBear.equalsIgnoreCase("Bear")){
				if(macdTemp > signal){
					System.out.println("changed to Bull at "+tmp+" open="+open+" high="+high+" low="+low+" close="+close);
					break;
				}
			}
			tmp = tmp + i;
			histogram = macdTemp - signal;

		}
	}
	void calculateMACD(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, 
			List<Float> close, List date, int EMA1, int EMA2, int signalValue,
			boolean updateForTodayAndNextDay, boolean updateForallDays, boolean isIntraDayData, String path) throws IOException, SQLException{
		float ema1 = EMA1, ema2=EMA2;
		List macdCalcColumn = new ArrayList<Float>();
		List FirstEma = new ArrayList<Float>();
		List SecEma = new ArrayList<Float>();
		List MACD = new ArrayList<Float>();
		List Signal = new ArrayList<>();
		List Histogram = new ArrayList<>();
		Float ema_1_sum=0.0f,ema_2_sum=0.0f, FirstEmaTemp=0.0f, SecEmaTemp=0.0f, macdTemp=0.0f;
		Float FirstEmaTemp2=0.0f, SecEmaTemp2=0.0f, signalTemp=0.0f;
		Float macdSumForSignal=0.0f, signal=0.0f, histogram=0.0f;
		String sql="",tradeDate="";;
		int index1=0;
		macdCalcColumn = close;
		EMA1--; EMA2--;
		
		int checkForFirstEntry=0;
		String bullorBear="";
		
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
			if(i>=EMA1){
				if(i==EMA1){
					for (int ema_1=i-EMA1; ema_1<= EMA1; ema_1++){
						ema_1_sum = ema_1_sum + (Float)macdCalcColumn.get(ema_1);
					}
					FirstEmaTemp = ema_1_sum/ema1;
					FirstEmaTemp2 = FirstEmaTemp; 
//					System.out.println(FirstEmaTemp);
				}else{
					FirstEmaTemp2 = FirstEmaTemp; 
					FirstEmaTemp= FirstEmaTemp*(1.00f-(2.00f/(ema1+1.00f))) + (((Float)macdCalcColumn.get(i))*(2.00f/(ema1+1.00f)));
//					System.out.println(FirstEmaTemp);
				}
			}
			if(i>=EMA2){
				if(i==EMA2){
					for (int ema_2=i-EMA2; ema_2<= EMA2; ema_2++){
						ema_2_sum = ema_2_sum + (Float)macdCalcColumn.get(ema_2);
					}
					SecEmaTemp = ema_2_sum/ema2;
					SecEmaTemp2 = SecEmaTemp;
//					System.out.println(SecEmaTemp);
				}else{
					SecEmaTemp2 = SecEmaTemp;
					SecEmaTemp = (Float)macdCalcColumn.get(i)*(2.00f/(ema2+1.00f)) + SecEmaTemp*(1.00f-(2.00f/(ema2+1.00f)));
//					System.out.println(SecEmaTemp);
				}
				macdTemp = FirstEmaTemp-SecEmaTemp;
				if((i-EMA2) < signalValue){
					macdSumForSignal+= macdTemp;
					signal =macdSumForSignal/((i-EMA2)+1); 
					signalTemp = signal;
//					System.out.println(signal);
				}else{
					signalTemp = signal;
					signal = ((float)macdTemp*(2.00f/(signalValue+1.00f))+(float)signal*(1.00f-(2.00f/(signalValue+1.00f))));
					
				}
				if(macdTemp > signal){
					bullorBear="Bull";
				}else {
					bullorBear="Bear";
				}
//				System.out.println(date.get(i)+", Outside="+bullorBear);
//				calcCrossoverPrice(open.get(i), high.get(i), low.get(i), close.get(i), bullorBear, FirstEmaTemp2,
//						SecEmaTemp2, signalTemp);
				
				
				histogram = macdTemp - signal;
				
				if(updateForTodayAndNextDay == true && i== (date.size()-1)){
					sql = "UPDATE "+name+" set EMA1="+FirstEmaTemp+", EMA2="+SecEmaTemp+", MACD="+macdTemp+""
							+ ", Histogram="+histogram+", sig="+signal+" where  tradedate='"+tradeDate+"'";
//					System.out.println(sql);
					executeSqlQuery(con, sql);
				}else if(updateForallDays == true){
					output.write(tradeDate);	output.write(","+FirstEmaTemp);	output.write(","+SecEmaTemp);	output.write(","+macdTemp);
					output.write(","+histogram); output.write(","+signal);
                	output.write("\r\n");
				}
				if(updateForTodayAndNextDay == true && (tradeDate.split(" ")[0]).equalsIgnoreCase(todaysDate) && isIntraDayData == true){
					output.write(tradeDate);	output.write(","+FirstEmaTemp);	output.write(","+SecEmaTemp);	output.write(","+macdTemp);
					output.write(","+histogram); output.write(","+signal);
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
					 " (tradedate, EMA1, EMA2, MACD, Histogram, sig) ";
           
          		executeSqlQuery(con, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.EMA1 = b.EMA1, a.EMA2 = b.EMA2, a.MACD = b.MACD, a.Histogram = b.Histogram, a.sig = b.sig  "+
				" WHERE a.tradedate = b.tradedate ";
//          		System.out.println(sql);
          		executeSqlQuery(con, sql);
          		tmp.dropTempTable(con, name);
		}
	}
			
}

