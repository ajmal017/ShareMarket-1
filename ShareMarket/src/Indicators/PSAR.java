package Indicators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PSAR extends Connection{

	public List psar = new ArrayList<Float>();
	public List dir = new ArrayList();
	
	
	public List getPsar() {
		return psar;
	}
	
	public void setPsar(List psar) {
		this.psar = psar;
	}
	
	public List getDir() {
		return dir;
	}
	
	public void setDir(List dir) {
		this.dir = dir;
	}
	
	public static void main(String[] args)  {
		PSAR psar = new PSAR();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      boolean updateResultTable=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT name FROM symbols  where volume>100000000");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
	    	  	boolean isIntraDayData=false;
	    	  	boolean insertAllDataToResult = false;
	    	  	String iter="1d";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		System.out.println(name);	
	    	  		psar.getPsar().clear();
	    	  		psar.getDir().clear();
	    	  		psar.updatePSAR(dbConnection, name, updateForTodayAndNextDay, updateForallDays, updateResultTable, 
	    	  				isIntraDayData, path+"/"+iter+"/", insertAllDataToResult);
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
	void calculatePSAR(List<Float> open, List<Float> high,  List<Float> low, List<Float> close, List date, float min, float max){
//		double  ep, diff, af, af_into_Diff;
		
		List ep = new ArrayList<Float>();
		List diff = new ArrayList<Float>();
		List af = new ArrayList<Float>();
		List af_into_Diff = new ArrayList<>();
		
		int direction;
		float af_temp, sar_temp, ep_temp;
		
		psar.add(0,0.0f);
		ep.add(0,0.0f);
		diff.add(0,0.0f);
		af_into_Diff.add(0,0.0f);
		af.add(0,0.0f);
		dir.add(0, 1);
//		String psar;
		int checkForFirstEntry=0;
		for (int i=0; i<= date.size()-1; i++){
			if (i>0){
				if (checkForFirstEntry==0){
					psar.add(i, low.get(i-1));
					ep.add(i, high.get(i));
					diff.add(i, (float)ep.get(i)- (float)psar.get(i));
					
					direction = ((int)dir.get(i-1) == 1) ? (((float)low.get(i) > (float)psar.get(i))?1:-1) 
							: (((float)high.get(i) < (float)psar.get(i))? -1 :1);
					dir.add(i, direction);
					af_temp = ((int)dir.get(i)==(int)dir.get(i-1)
								?
								(((int)dir.get(i)==1
									?
										(((float)ep.get(i) > (float)ep.get(i-1) ? (((float)af.get(i-1)==max ? (float)af.get(i-1) : (float)min+(float)af.get(i-1))):(float)af.get(i-1)))
									:
										(((float)ep.get(i) < (float)ep.get(i-1) ? (((float)af.get(i-1)==max ? (float)af.get(i-1) : (float)min+(float)af.get(i-1))):(float)af.get(i-1)))
								))
								:
					min);

					af.add(i, (float)af_temp);
					af_into_Diff.add(i, (float)diff.get(i) * (float)af.get(i));
					
					sar_temp = ((int)dir.get(i)==(int)dir.get(i-1)
									?
									(((int)dir.get(i)==1
										?
											((((float)psar.get(i) + (float)af_into_Diff.get(i)) <	Math.min((float)low.get(i), (float)low.get(i-1))
												?	((float)psar.get(i) + (float)af_into_Diff.get(i)) : Math.min((float)low.get(i), (float)low.get(i-1))))
										:
											
											((((float)psar.get(i) + (float)af_into_Diff.get(i)) >	Math.max((float)high.get(i), (float)high.get(i-1))
													?	((float)psar.get(i) + (float)af_into_Diff.get(i)) : Math.max((float)high.get(i), (float)high.get(i-1))))
									)):
								(float)ep.get(i));
					psar.add(i+1, (float)sar_temp);
//					System.out.println(psar);
//					System.out.println(ep);
//					System.out.println(diff);
//					System.out.println(af_into_Diff);
//					System.out.println(dir);
//					System.out.println(af_temp);
//					System.out.println(sar_temp);
				}else{
					ep_temp = ((int)dir.get(i-1)==1
								?
									((float)high.get(i) > (float)ep.get(i-1) ? (float)high.get(i) : (float)ep.get(i-1))
								:
									((float)low.get(i) < (float)ep.get(i-1) ? (float)low.get(i) : (float)ep.get(i-1))
							);
					ep.add(i, ep_temp);
					diff.add(i, (float)ep.get(i)- (float)psar.get(i));
					direction = ((int)dir.get(i-1) == 1) ? (((float)low.get(i) > (float)psar.get(i))?1:-1) 
							: (((float)high.get(i) < (float)psar.get(i))? -1 :1);
					dir.add(i, direction);
					af_temp = ((int)dir.get(i)==(int)dir.get(i-1)
								?
								(((int)dir.get(i)==1
									?
										(((float)ep.get(i) > (float)ep.get(i-1) ? (((float)af.get(i-1)==max ? (float)af.get(i-1) : (float)min+(float)af.get(i-1))):(float)af.get(i-1)))
									:
										(((float)ep.get(i) < (float)ep.get(i-1) ? (((float)af.get(i-1)==max ? (float)af.get(i-1) : (float)min+(float)af.get(i-1))):(float)af.get(i-1)))
								))
								:
					min);

					af.add(i, (float)af_temp);
					af_into_Diff.add(i, (float)diff.get(i) * (float)af.get(i));
					
					sar_temp = ((int)dir.get(i)==(int)dir.get(i-1)
									?
									(((int)dir.get(i)==1
										?
											((((float)psar.get(i) + (float)af_into_Diff.get(i)) <	Math.min((float)low.get(i), (float)low.get(i-1))
												?	((float)psar.get(i) + (float)af_into_Diff.get(i)) : Math.min((float)low.get(i), (float)low.get(i-1))))
										:
											
											((((float)psar.get(i) + (float)af_into_Diff.get(i)) >	Math.max((float)high.get(i), (float)high.get(i-1))
													?	((float)psar.get(i) + (float)af_into_Diff.get(i)) : Math.max((float)high.get(i), (float)high.get(i-1))))
									)):
								(float)ep.get(i));
					psar.add(i+1, (float)sar_temp);				}
				checkForFirstEntry=1;
			}
		}
//		System.out.println(psar);
//		System.out.println(dir);
//		System.out.println(date);
	}
	public void updatePSAR(java.sql.Connection dbConnection, String symbol, boolean updateForTodayAndNextDay, 
			boolean updateForallDays, boolean updateResultTable, boolean isIntraDayData, String path,boolean insertAllDataToResult) {
		Test t = new Test();
		String reversal="";
		String sql;
	      try{
	    	  Connection con = new Connection();
	    	  TemporaryTable tmp = new TemporaryTable();
	    	  	ResultSet rs=null;
	    	  	List open = new ArrayList<Float>();
	    	  	List high = new ArrayList<Float>();
	    	  	List low = new ArrayList<Float>();
	    	  	List close = new ArrayList<Float>();
	    	  	List date = new ArrayList<String>();
	    	  	List di_plus = new ArrayList<Float>();
	    	  	List di_minus = new ArrayList<Float>();
	    	  	Float profitOnNxtDaySell;
	    	  	WILLIAMS will = new WILLIAMS();
//	    	  	System.out.println("sssssssssssss");
	    	  	 rs = con.executeSelectSqlQuery(dbConnection, "SELECT * FROM "+symbol+"  ");
	    	  	while (rs.next()){
	    	  		open.add(rs.getFloat("open"));
	    	  		high.add(rs.getFloat("high"));
	    	  		low.add(rs.getFloat("low"));
	    	  		close.add(rs.getFloat("close"));
	    	  		date.add(rs.getString("tradedate"));
	    	  		di_plus.add(rs.getFloat("di_plus_average"));
	    	  		di_minus.add(rs.getFloat("di_minus_average"));
	    	  	}
	    	  	if(rs!=null) rs.close();
	    	  	calculatePSAR(open, high, low, close, date, 0.02F, 0.2F);
	    	  	boolean check=false;String tradeDate="";
	    	  	
	    	  	BufferedWriter output = null;
	    	  	if(updateForallDays == true || isIntraDayData == true){
	    	  		tmp.createTempTable(dbConnection, symbol);
		    	  	File file = new File(""+path+symbol+".txt");
		            output = new BufferedWriter(new FileWriter(file));
	    	  	}
	    	  	String todaysDate = executeCountQuery(dbConnection, "select date(now())");
	            
	    	  	for (int i=0; i< date.size(); i++){
	    	  		if(isIntraDayData==true)
	    	  			tradeDate = (String) date.get(i).toString();
	    	  		else
	    	  			tradeDate = date.get(i)+"";
	    	  		if (updateForallDays== true || (updateForTodayAndNextDay == true && (tradeDate.split(" ")[0]).equalsIgnoreCase(todaysDate) && isIntraDayData == true)){
	    	  			output.write(tradeDate);
                    	output.write(","+dir.get(i));
                    	output.write(","+psar.get(i));
//                    	System.out.println(dir.get(i));
                    	output.write("\r\n");
                    	
                    	
	    	  			if(i>0){
	    		    	  		if((int)dir.get(i-1) == 1 && (int)dir.get(i) == -1 && ((float)open.get(i) > (float)psar.get(i))){
			    	  				reversal = "BearToday";
//			    	  				System.out.println(i==date.size()-1);
			    	  				if(!symbol.contains("_")){
			    	  					//will.calculateIntraDayCross(dbConnection, symbol, tradeDate, tradedate,(float)psar.get(i), "Bear", "PSAR");
			    	  				}
			    	  				
			    	  				if(updateResultTable==true && isIntraDayData==false){
			    	  					sql = "insert into psarresults(name, reversal, profitPerc, profitRupees, triggerPrice, date) "
							  					+ " values ('"+symbol+"', 'Bear', "+((float)psar.get(i)-(float)low.get(i))*100.00f/(float)psar.get(i)+", "
							  							+ ""+((float)psar.get(i)-(float)close.get(i))*100.00f/(float)psar.get(i)+", "
							  							+ (float)psar.get(i)+", '"+tradeDate+"')";
			    	  						con.executeSqlQuery(dbConnection, sql);
			    	  				}
						  				

			    	  			}else if((int)dir.get(i-1) == -1 && (int)dir.get(i) == 1 && ((float)open.get(i) < (float)psar.get(i))){
			    	  				reversal = "BullToday";
			    	  				if(!symbol.contains("_")){
			    	  					//will.calculateIntraDayCross(dbConnection, symbol, tradeDate, (float)psar.get(i), "Bull", "PSAR");
			    	  				}
			    	  				
			    	  				if(updateResultTable==true && isIntraDayData==false){
			    	  					sql = "insert into psarresults(name, reversal, profitPerc, profitRupees, triggerPrice, date) "
							  					+ " values ('"+symbol+"', 'Bull', "+((float)high.get(i)-(float)psar.get(i))*100.00f/(float)psar.get(i)+", "
							  							+ ""+((float)close.get(i)-(float)psar.get(i))*100.00f/(float)psar.get(i)+", "+(float)psar.get(i)+", '"+tradeDate+"')";
			    	  						con.executeSqlQuery(dbConnection, sql);
			    	  				}
			    	  				
			    	  				if(i!=date.size()-1){
			    	  					profitOnNxtDaySell = ((float)high.get(i+1) - (float)psar.get(i)) * 100/(float)psar.get(i);
			    	  					sql = "UPDATE "+symbol+ " SET ProfitOnNextDaySell = "+profitOnNxtDaySell+" where ProfitOnNextDaySell=0 and tradedate = '"+date.get(i+1)+" 00:00:00'";
				    	  				con.executeSqlQuery(dbConnection, sql);
			    	  				}
			    	  			}
	    		    	  		if(!reversal.equals("")){
			    	  				sql = "UPDATE "+symbol+ " SET reversal = '"+reversal+"' where  tradedate = '"+tradeDate+"'";
			    	  				con.executeSqlQuery(dbConnection, sql);
//			    	  				System.out.println(sql);
			    	  				reversal = "";
			    	  			}
	    	  			}
	    	  			
	    	  		}
	    	  		
	    	  		
	    	  		if(updateForTodayAndNextDay==true && isIntraDayData==false){
	    	  			if(i==date.size()-1){
	    	  				sql = "UPDATE "+symbol+ " SET PSAR = "+psar.get(i)+", dir = "+dir.get(i)+" where tradedate = '"+tradeDate+"'";
	    	  				con.executeSqlQuery(dbConnection, sql);
//	    	  				System.out.println(sql);
		    	  			sql = "UPDATE symbols SET next_day_psar = "+psar.get(i+1)+" where name = '"+symbol+"'";
//		    	  			System.out.println(sql);
		    	  			con.executeSqlQuery(dbConnection, sql);
		    	  			
		    	  			sql = "UPDATE symbols SET PrevDirection = "+dir.get(i)+" where name = '"+symbol+"'";
//		    	  			System.out.println(sql);
		    	  			con.executeSqlQuery(dbConnection, sql);
		    	  			
		    	  			sql = "UPDATE symbols SET LastPrice = "+close.get(i)+" where name = '"+symbol+"'";
		    	  			con.executeSqlQuery(dbConnection, sql);
		    	  			
		    	  			
		    	  			sql = "UPDATE SYMBOLS SET LastUpdated = '"+tradeDate+"' where name='"+symbol+"'";
		    	  			con.executeSqlQuery(dbConnection, sql);
		    	  		}
	    	  		}
	    	  	}
	    	  	if ( output != null ) output.close();
	    	  	if(updateForallDays == true || isIntraDayData == true){
	    	  		sql = "LOAD DATA LOCAL INFILE '"+path+symbol+".txt' "+
							 " INTO TABLE "+symbol+"_Temp "+
							 " FIELDS TERMINATED BY ',' "+
							 " LINES TERMINATED BY '\n'" +
							 " "+
							 " (tradedate, dir, psar) ";
		            
		           		executeSqlQuery(dbConnection, sql);
		           		
		           		sql = "UPDATE "+symbol+"_Temp b, "+symbol+" a"+
						" SET a.psar = b.psar*1, a.dir = b.dir "+
						" WHERE a.tradedate = b.tradedate ";
//		           		System.out.println(sql);
		           		executeSqlQuery(dbConnection, sql);
		           		tmp.dropTempTable(dbConnection, symbol);
	    	  	}
	    	  	
	    	  	if(isIntraDayData==false){
	    	  		sql="SELECT coalesce(sum((high-psar)*100/psar), 0.0) FROM "+symbol+" where reversal in ('BullToday') and tradedate >='2015-01-01 00:00:00'";
		    	  	sql = "UPDATE SYMBOLS SET BullishResults=("+sql+") where name='"+symbol+"'";
		    	  	con.executeSqlQuery(dbConnection, sql);
//		    	  	System.out.println(sql);
		    	  	
		    	  	sql="SELECT coalesce(sum((psar-low)*100/psar),0.0) FROM "+symbol+" where reversal in ('BearToday') and tradedate >='2015-01-01 00:00:00'";
		    	  	sql = "UPDATE SYMBOLS SET BearishResults=("+sql+") where name='"+symbol+"'";
		    	  	con.executeSqlQuery(dbConnection, sql);
		    	  	
		    	  	sql="SELECT coalesce(sum(ProfitonNextDaySell),0.0) FROM "+symbol+" where tradedate >='2015-01-01 00:00:00'";
		    	  	sql = "UPDATE SYMBOLS SET BullishResultsOnNextDaySell=("+sql+") where name='"+symbol+"'";
		    	  	con.executeSqlQuery(dbConnection, sql);
		    	  	
		    	  	sql="select avg(volume) from "+symbol+" where tradedate>=  DATE(DATE_SUB(NOW(), INTERVAL 50 DAY))";
		    	  	sql = "UPDATE SYMBOLS SET volume=("+sql+") where name='"+symbol+"'";
		    	  	con.executeSqlQuery(dbConnection, sql);
	    	  	}
	    	  	
	    	  	
  	  		
	      }
		  	catch(SQLException e){
		  		e.printStackTrace();
			}
	      catch(Exception e){
	    	  e.printStackTrace();
	      }
//	      t.importDataToTable("ucobank");
	   }
		
	}

