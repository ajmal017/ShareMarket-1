package Indicators;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

public class PMO extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      PMO adx = new PMO();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT name FROM symbols");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		System.out.println(name);	
	    	  		adx.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays);
	    	  		adx.UpdatePMOResults(dbConnection, name, updateSymbolsTableData, updateAllData);
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
   
   public void UpdatePMOResults(java.sql.Connection con, String name, boolean updateSymbolsTableData, boolean updateAllData) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<Date>();
	  	List PMO_Line = new ArrayList<Float>();
	  	List PMO_Signal = new ArrayList<Float>();
	  	String sql="";
	  	try {
	  		sql="select * from "+name+" ";
			rs = executeSelectSqlQuery(con, sql);
		 
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getDate("tradedate"));
		  		PMO_Line.add(rs.getFloat("PMO_CUSTOM_EMA"));
		  		PMO_Signal.add(rs.getFloat("PMO_SIGNAL"));
		  	}
		  	
		  	String PMO_Change_DIR="";
		  	Float profit=0.0f, profitRupees=0.0f;
		  	
		  	if(updateAllData == true){
		  		for (int i=0; i< date.size(); i++){
		  			if(i>1 && i < date.size()-1){
		  				PMO_Change_DIR = "";
		  				if((float)PMO_Line.get(i) > 0 && (float)PMO_Line.get(i) > (float)PMO_Signal.get(i)){
		  					if((float)PMO_Line.get(i) > (float)PMO_Line.get(i-1) && (float)PMO_Line.get(i) > (float)PMO_Line.get(i+1)){
		  						PMO_Change_DIR = "Bear";
		  					}
				  		}
		  				if((float)PMO_Line.get(i) < 0 && (float)PMO_Line.get(i) < (float)PMO_Signal.get(i)){
		  					if((float)PMO_Line.get(i) < (float)PMO_Line.get(i-1) && (float)PMO_Line.get(i) < (float)PMO_Line.get(i+1)){
		  						PMO_Change_DIR = "Bull";
		  					}
				  		}
		  				if(!PMO_Change_DIR.equals("")){
//		  					PMO_Change_DIR="";
		  					sql = "UPDATE "+name+" set PMO_REVERSAL='"+PMO_Change_DIR+"' WHERE TRADEDATE='"+date.get(i+1)+" 00:00:00'";
		  					executeSqlQuery(con, sql);
		  					
		  				}
		  				
//		  				if(PMO_Change_DIR.equalsIgnoreCase("Bull")){
//			  				if(i < date.size()-1){
//			  					if((float) high.get(i+1) > (float) high.get(i)){
//				  					if((float)open.get(i+1) > (float) high.get(i)){
//				  						profit = ((float) high.get(i+1) - (float)open.get(i+1))*100/(float)open.get(i+1);
//				  						profitRupees = (float) high.get(i+1) - (float)open.get(i+1);
//				  					}else{
//				  						profit = ((float) high.get(i+1) - (float)high.get(i))*100/(float)high.get(i);
//				  						profitRupees = (float) high.get(i+1) - (float)high.get(i);
//				  					}
//				  					if(((float)high.get(i+1) < (float) open.get(i+1))) {
//				  						profit=0.0f;
//				  						profitRupees=0.0f;
//				  					}
//				  					sql = "update "+name+" set WILL_BULL_PROFIT="+profit+", "
//				  							+ "WILL_BULL_PROFIT_Rs="+profitRupees+" WHERE WILL_BULL_PROFIT=0 and TRADEDATE='"+date.get(i+1)+" 00:00:00';";
//						  			executeSqlQuery(con, sql);
////						  			System.out.println(sql);
//				  				}
//			  				}
//			  			}
//		  				if(PMO_Change_DIR.equalsIgnoreCase("Bear")){
//			  				if(i < date.size()-1){
//			  					if((float) low.get(i+1) < (float) low.get(i)){
//				  					if((float)open.get(i+1) < (float) low.get(i)){
//				  						profit = ((float) open.get(i+1) - (float)low.get(i+1))*100/(float)open.get(i+1);
//				  						profitRupees = (float) open.get(i+1) - (float)low.get(i+1);
//				  					}else{
//				  						profit = ((float) low.get(i) - (float)low.get(i+1))*100/(float)low.get(i);
//				  						profitRupees = (float) low.get(i) - (float)low.get(i+1);
//				  					}
//				  					if(((float)low.get(i+1) > (float) open.get(i+1))) {
//				  						profit=0.0f;
//				  						profitRupees=0.0f;
//				  					}
//				  					sql = "update "+name+" set WILL_BEAR_PROFIT="+profit+", "
//				  							+ "WILL_BEAR_PROFIT_Rs="+profitRupees+" WHERE WILL_BEAR_PROFIT=0 and TRADEDATE='"+date.get(i+1)+" 00:00:00';";
////				  					System.out.println(sql);
//						  			executeSqlQuery(con, sql);
//				  				}
//			  				}
//			  			}
		  			}
		  		}
		  	}
		  	
		  	if(updateSymbolsTableData == true){
//		  		sql = "update symbols set Williams_Bull_profit= "+
//				  		"(select coalesce(SUM(Will_Bull_profit),0) "+
//				  		" from "+name+" where (Will_Bull_profit<>0 or WILL_REVERSAL='Bull') and tradedate>='2015-01-01 00:00:00') "+
//				  		" where name='"+name+"'";
//				  	executeSqlQuery(con, sql);
//				  	
//				sql = "update symbols set Williams_Bear_profit= "+
//					  		"(select coalesce(SUM(Will_Bear_profit),0) "+
//					  		" from "+name+" where (Will_Bear_profit<>0 or WILL_REVERSAL='Bear') and tradedate>='2015-01-01 00:00:00') "+
//					  		" where name='"+name+"'";
//					  	executeSqlQuery(con, sql);
					  	
			  	sql="select coalesce(PMO_REVERSAL,'') PMO_REVERSAL from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET PMO_REVERSE=("+sql+") WHERE NAME='"+name+"'";
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
   
   public void LoadData(java.sql.Connection con, String name, boolean updateForTodayAndNextDay, boolean updateForallDays) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<Date>();
	  	try {
			rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, tradedate FROM "+name+"  order by tradedate;");
			int smoothingPeriod = 35;
			int doubleSmoothingPeriod = 20;
			int signalPeriod = 10;
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getDate("tradedate"));
		  	}
//		  	System.out.println(date.size());
		  	calculatePMO(con, name,open, high, low, close, date, smoothingPeriod, doubleSmoothingPeriod, signalPeriod, updateForTodayAndNextDay, updateForallDays);
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
   
  
   
   
	void calculatePMO(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, 
			List<Float> close, List date, int smoothingPeriod,int  doubleSmoothingPeriod, int signalPeriod,
			boolean updateForTodayAndNextDay, boolean updateForallDays){
		Float ROC=0.0f, ROCTemp=0f, custEMA_ROC_Multiply=0f, custEMA_ROC_Multiply_Temp=0f, PMOLine=0.00f,
				PMOLineT=0.00f, PMO_Signal=0.00f;
		int k=0,j=0,l=0;String sql="";
		DecimalFormat df = new DecimalFormat("#.##");
		DecimalFormat df2 = new DecimalFormat("#.####");
		Float ROC_EMA=0.0f;
		Float smoothingPeriodConst = 2.00f/smoothingPeriod;
		Float doubleSmoothingPeriodConst = 2.00f/doubleSmoothingPeriod;
		
		smoothingPeriodConst = Float.parseFloat(df2.format(smoothingPeriodConst));
		doubleSmoothingPeriodConst = Float.parseFloat(df2.format(doubleSmoothingPeriodConst));
//		System.out.println(smoothingPeriodConst);
		for (int i=0; i<= date.size()-1; i++){
	
			if(i>=1){
				ROC = (close.get(i)-close.get(i-1))/close.get(i-1)*100;
				ROC = Float.parseFloat(df.format(ROC));
				ROCTemp+=ROC;
//				System.out.println(df.format(ROC));
				k++;
				if(k==smoothingPeriod){
					ROC_EMA = ROCTemp/smoothingPeriod;
					Double toBeTruncated = new Double(ROC_EMA);
					ROC_EMA=(float) new BigDecimal(toBeTruncated ).setScale(2, BigDecimal.ROUND_UP).doubleValue();
//					System.out.println(ROC_EMA);
				}else if(k>smoothingPeriod){
					j++;
					ROC_EMA = (ROC*smoothingPeriodConst)+(ROC_EMA*(1.00f-smoothingPeriodConst));
					if(k==smoothingPeriod+1){
						Double toBeTruncated = new Double(ROC_EMA);
						ROC_EMA=(float) new BigDecimal(toBeTruncated ).setScale(2, BigDecimal.ROUND_UP).doubleValue();
					}
//					System.out.println(ROC_EMA);
					custEMA_ROC_Multiply = ROC_EMA *10;
					ROC_EMA = Float.parseFloat(df.format(ROC_EMA));
					
					custEMA_ROC_Multiply = Float.parseFloat(df.format(custEMA_ROC_Multiply));
					custEMA_ROC_Multiply_Temp+=custEMA_ROC_Multiply;
					if(j==doubleSmoothingPeriod){
						PMOLine = custEMA_ROC_Multiply_Temp/doubleSmoothingPeriod;
						PMOLine = Float.parseFloat(df.format(PMOLine));
						
					}else if(j>doubleSmoothingPeriod){
						l++;
						PMOLine = ((custEMA_ROC_Multiply-PMOLine)*doubleSmoothingPeriodConst)+PMOLine;
						PMOLine = Float.parseFloat(df.format(PMOLine));
//						System.out.println(PMOLine);
						PMOLineT+=PMOLine;
						
						if(l==signalPeriod){
							PMO_Signal = PMOLineT / signalPeriod;
//							System.out.println(PMO_Signal);
						}else if(l>signalPeriod){
							PMO_Signal = (PMOLine*(2.00f/(signalPeriod+1.00f))+PMO_Signal*(1.00f-(2.00f/(signalPeriod+1.00f))));
//							System.out.println(PMO_Signal);
						}
						PMO_Signal = Float.parseFloat(df.format(PMO_Signal));
						
					}
					
				}
				sql = "UPDATE "+name+" SET PMO_ROC="+ROC+", PMO_CUSTOM_ROC_EMA="+ROC_EMA+", PMO_CUSTOM_EMA="+PMOLine+", "
						+ " PMO_SIGNAL="+PMO_Signal+" WHERE TRADEDATE='"+date.get(i)+" 00:00:00'";
				try {
					if(!sql.equals(""))
					executeSqlQuery(con, sql);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
		
	}
			
}

