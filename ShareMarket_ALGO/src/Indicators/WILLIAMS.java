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

public class WILLIAMS extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      WILLIAMS adx = new WILLIAMS();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 1000000000 order by volume desc ");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
	    	  	boolean updateResultTable=false;boolean isIntraDayData=false;
	    	  	boolean insertAllDataToResult = false;
	    	  	String iter="1d";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("s.name");
	    	  		if(!iter.equals("1d"))
	    	  			name =name+"_"+iter+"";
//	    	  		System.out.println(name);	
	    	  		adx.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData,path+"/will/"+iter+"/");
	    	  		adx.UpdateWilliamsResults(dbConnection, name, updateSymbolsTableData, updateAllData, updateResultTable, isIntraDayData, insertAllDataToResult);
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
   
   public void UpdateWilliamsResults(java.sql.Connection con, String name, boolean updateSymbolsTableData, 
		   boolean updateAllData, boolean updateResultTable, boolean isIntraDayData, boolean insertAllDataToResult) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	List Williams = new ArrayList<Float>();
	  	List di_plus = new ArrayList<Float>();
	  	List di_minus = new ArrayList<Float>();
//	  	List volume = new ArrayList<String>();
	  	String sql="";
	  	try {
	  		sql="select * from "+name+" where date(tradedate)>='2016-03-28'";
			rs = executeSelectSqlQuery(con, sql);
		 
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getString("tradedate"));
		  		Williams.add(rs.getFloat("WILL_R"));
//		  		volume.add(rs.getString("volume"));
    	  		di_plus.add(rs.getFloat("di_plus_average"));
    	  		di_minus.add(rs.getFloat("di_minus_average"));
		  	}
		  	date.add("2016-06-01");
		  	
		  	String Will_Change_DIR="";
		  	float filter=0f, filterPerc = 2f;
		  	Float profit=0.0f, profitRupees=0.0f;
		  	String tradeDate="";
		  	if(updateAllData == true){
		  		for (int i=0; i< date.size(); i++){
		  			if(isIntraDayData==true)
	    	  			tradeDate = (String) date.get(i).toString();
	    	  		else
	    	  			tradeDate = date.get(i)+"";
		  			if(i>1){
		  				Will_Change_DIR = "";
		  				if((float)Williams.get(i) > 50 && (float)Williams.get(i-1) <50){
		  					Will_Change_DIR = "Bear";
				  		}
		  				if((float)Williams.get(i) < 50 && (float)Williams.get(i-1) >50){
		  					Will_Change_DIR = "Bull";
				  		}
		  				if(!Will_Change_DIR.equals("")){
//		  					Will_Change_DIR="";
		  					sql = "UPDATE "+name+" set WILL_REVERSAL='"+Will_Change_DIR+"' WHERE WILL_REVERSAL='' and TRADEDATE='"+tradeDate+"'";
		  					executeSqlQuery(con, sql);
		  					
		  				}
		  				
		  				boolean check=false;
		  				if(Will_Change_DIR.equalsIgnoreCase("Bull") || insertAllDataToResult == true){
			  				if(i < date.size()-1){
			  					if(!name.contains("_")){
		  							calculateIntraDayCross(con, name, date.get(i+1).toString(),date.get(i).toString(), (float)high.get(i), "Bull", "Will");
		    	  				}
			  					if((float) high.get(i+1) > (float) high.get(i)){
				  					if((float)open.get(i+1) > (float) high.get(i)){
				  						check =true;
				  						profit = ((float) high.get(i+1) - (float)open.get(i+1))*100/(float)open.get(i+1);
				  						profitRupees = (float) high.get(i+1) - (float)open.get(i+1);
				  					}else{
				  						profit = ((float) high.get(i+1) - (float)high.get(i))*100/(float)high.get(i);
				  						profitRupees = (float) high.get(i+1) - (float)high.get(i);
				  						
//				  						if(!name.contains("_")){
//				  							calculateIntraDayCross(con, name, date.get(i+1).toString(), (float)high.get(i), "Bull", "Will");
//				    	  				}
				  					}
				  					if(((float)high.get(i+1) < (float) open.get(i+1))) {
				  						profit=0.0f;
				  						profitRupees=0.0f;
				  					}
				  					sql = "update "+name+" set WILL_BULL_PROFIT="+profit+", "
				  							+ "WILL_BULL_PROFIT_Rs="+profitRupees+" WHERE WILL_BULL_PROFIT=0 and TRADEDATE='"+date.get(i+1)+"';";
						  			executeSqlQuery(con, sql);
						  			filter = ((float)open.get(i) - (float)low.get(i))*100/(float)open.get(i);
						  			if(check == false){
						  				if(updateResultTable==true  && isIntraDayData==false && filter<filterPerc){
						  					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								  					+ " values ('"+name+"', 'Bull', "+high.get(i)+", "+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
						  					if((insertAllDataToResult == true || i==date.size()-2) )
						  					executeSqlQuery(con, sql);
						  				}
						  				
						  			}
						  			
//						  			System.out.println(sql);
				  				}
			  				}
			  			}
		  				if(Will_Change_DIR.equalsIgnoreCase("Bear") || insertAllDataToResult == true){
			  				if(i < date.size()-1){
			  					if(!name.contains("_")){
		  							calculateIntraDayCross(con, name, date.get(i+1).toString(),date.get(i).toString(), (float)low.get(i), "Bear", "Will");
		    	  				}
			  					if((float) low.get(i+1) < (float) low.get(i)){
				  					if((float)open.get(i+1) < (float) low.get(i)){
				  						check=true;
				  						profit = ((float) open.get(i+1) - (float)low.get(i+1))*100/(float)open.get(i+1);
				  						profitRupees = (float) open.get(i+1) - (float)low.get(i+1);
				  					}else{
				  						profit = ((float) low.get(i) - (float)low.get(i+1))*100/(float)low.get(i);
				  						profitRupees = (float) low.get(i) - (float)low.get(i+1);
				  						
//				  						if(!name.contains("_")){
//				  							calculateIntraDayCross(con, name, date.get(i+1).toString(), (float)high.get(i), "Bull", "Will");
//				    	  				}
				  					}
				  					if(((float)low.get(i+1) > (float) open.get(i+1))) {
				  						profit=0.0f;
				  						profitRupees=0.0f;
				  					}
				  					sql = "update "+name+" set WILL_BEAR_PROFIT="+profit+", "
				  							+ "WILL_BEAR_PROFIT_Rs="+profitRupees+" WHERE WILL_BEAR_PROFIT=0 and TRADEDATE='"+date.get(i+1)+"';";
//				  					System.out.println(sql);
						  			executeSqlQuery(con, sql);
						  			filter = ((float)high.get(i) - (float)open.get(i))*100/(float)open.get(i);
						  			if(check == false){
						  				if(updateResultTable==true  && isIntraDayData==false && filter<filterPerc){
						  					sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
								  					+ " values ('"+name+"', 'Bear', "+low.get(i)+", "+profit+", "+profitRupees+", '"+date.get(i+1)+"')";
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
		  		sql = "update symbols set Williams_Bull_profit= "+
				  		"(select coalesce(SUM(Will_Bull_profit),0) "+
				  		" from "+name+" where (Will_Bull_profit<>0 or WILL_REVERSAL='Bull') and tradedate>='2015-01-01') "+
				  		" where name='"+name+"'";
				  	executeSqlQuery(con, sql);
				  	
				sql = "update symbols set Williams_Bear_profit= "+
					  		"(select coalesce(SUM(Will_Bear_profit),0) "+
					  		" from "+name+" where (Will_Bear_profit<>0 or WILL_REVERSAL='Bear') and tradedate>='2015-01-01') "+
					  		" where name='"+name+"'";
					  	executeSqlQuery(con, sql);
					  	
			  	sql="select coalesce(WILL_REVERSAL,'') WILL_REVERSAL from "+name+" order by tradedate desc limit 1 ";
				sql = "UPDATE SYMBOLS SET Williams_Reversal=("+sql+") WHERE NAME='"+name+"'";
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
			rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, tradedate, supertrend_reversal FROM "+name+"  order by tradedate;");
		 
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getString("tradedate"));
//		  		System.out.println(rs.getString("supertrend_reversal").trim());
		  	}
//		  	System.out.println(date.size());
		  	calculateWilliams(con, name,open, high, low, close, date,  14, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path);
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
   
  
   void calculateIntraDayCross(java.sql.Connection con, String name,  String date, String prevDate, float triggerPrice, String dir, 
		   String indicator) throws IOException, SQLException{
		
		ResultSet rs=null;float intrarange=0f, breakout1=0f, breakout2=0f;;
		String tableName="";
		float trigger = triggerPrice;
		if(indicator.equalsIgnoreCase("Will")){
			tableName = "williamsresults";
		}else if(indicator.equalsIgnoreCase("ADX")){
			tableName = "ADXresults";
		}else if(indicator.equalsIgnoreCase("MACD")){
			tableName = "MACDresults";
		}else if(indicator.equalsIgnoreCase("CCI")){
			tableName = "CCIresults";
		}else if(indicator.equalsIgnoreCase("ENVELOPE")){
			tableName = "maenveloperesults";
		}else if(indicator.equalsIgnoreCase("RANGE")){
			tableName = "RANGEresults";
		}else if(indicator.equalsIgnoreCase("PSAR")){
			tableName = "psarresults";
		}
		List open = new ArrayList<Float>();List macd = new ArrayList<Float>();
		List sig = new ArrayList<Float>();
		List high = new ArrayList<Float>();
		List low = new ArrayList<Float>();
		List close = new ArrayList<Float>();
		List cci = new ArrayList<Float>();
		List roc = new ArrayList<Float>();
		List tradedate = new ArrayList<String>();
		List supertrend = new ArrayList<Integer>();
		List cciReversal = new ArrayList<String>();
		List ema_26 = new ArrayList<Float>();
		List elder = new ArrayList<String>();
		List ema_12 = new ArrayList<Float>();
		List hist = new ArrayList<Float>();
		List obv = new ArrayList<Long>();
		String dayshigh="";String dayslow=""; String daysclose="", daysopen="", tradeddate="";
		dayshigh = executeCountQuery(con, "select high from "+name+" where tradedate=Date('"+date+"')");
		dayslow = executeCountQuery(con, "select low from "+name+" where tradedate=Date('"+date+"')");
		daysclose = executeCountQuery(con, "select close from "+name+" where tradedate=Date('"+date+"')");
		daysopen = executeCountQuery(con, "select open from "+name+" where tradedate=Date('"+date+"')");
		String interval="5";
		String sql="select macd, sig, roc, obv,ema1, histogram, ema2, supertrend_reversal, cci, cci_reversal, (case when (tradedate<concat(Date('"+date+"'),' 9:00:00')) then 0 else 1 end) timecheck, tradedate,open,high,low,close "+
				" from "+name+"_"+interval+" where tradedate >=concat(Date('"+date+" '),' 9:10:00') and tradedate <= concat(Date('"+date+"'),' 15:00:00')";
//       System.out.println(sql);
       rs = executeSelectSqlQuery(con, sql);
       
       float profitRupees=0f,profitPerc=0f;
       int count=0;
       while(rs.next()){
	    	open.add(rs.getFloat("open"));
	       	close.add(rs.getFloat("close"));
	       	high.add(rs.getFloat("high"));
	       	low.add(rs.getFloat("low"));
	       	macd.add(rs.getFloat("macd"));
	       	sig.add(rs.getFloat("sig"));
	       	cci.add(rs.getFloat("cci"));
	       	roc.add(rs.getFloat("roc")); 
	       	tradedate.add(rs.getString("tradedate"));
	       	cciReversal.add(rs.getString("cci_reversal"));
	       	supertrend.add(rs.getInt("supertrend_reversal"));
	       	ema_26.add(rs.getFloat("ema2"));
	       	obv.add(rs.getLong("obv"));
	       	
	       	ema_12.add(rs.getFloat("ema1")); 
	       	hist.add(rs.getFloat("histogram")); 
	       	if(count>=1){
	       		if((float)ema_12.get(count) > (float)ema_12.get(count-1) && (float)hist.get(count) > (float)hist.get(count-1)){
	       			elder.add("Bull");
	       		}else if((float)ema_12.get(count) < (float)ema_12.get(count-1) && (float)hist.get(count) < (float)hist.get(count-1)){
	       			elder.add("Bear");
	       		}else{
	       			elder.add("Neutral");
	       		}
	       	}else{
	       		elder.add("");
	       	}
	       	count++;
       }
       
       String dateexited="";
       long obv_Prevhigh=0,obv_PrevLow=0;
       sql = "select coalesce(max(obv),0) from "+name+"_1 where date(tradedate) =Date('"+prevDate+" ')";
       sql = "select coalesce(min(obv),0) from "+name+"_1 where date(tradedate) =Date('"+prevDate+" ')";

       float checkPercGreater=0f, checkPercGreater2=0f;
       float p=0.5f,l=-2f, diffPerc=0.2f, ema_Cur=0.0f, diff=0.1f;
       float exit=2;boolean gotentered=false;
       for (int i=0; i< tradedate.size()-1; i++){
    	   dateexited="";
    	   if (i==0) continue;
    	   String maxObvIntra="", todaysOpen="", maxHigh="";;
    	   
          	if(dir.equalsIgnoreCase("Bull") ){
          		checkPercGreater = ((float)close.get(i) - (float)open.get(i))*100/(float)open.get(i);
          		checkPercGreater2 = ((float)high.get(i) - (float)close.get(i))*100/(float)close.get(i);
          		if(checkPercGreater > exit && checkPercGreater2> 0.5f && checkPercGreater2 < 1f)
          		{		
              			sql = "select min(low) from "+name+"_5 where tradedate >'"+tradedate.get(i)+"' and tradedate <= concat(Date('"+date+"'),' 15:15:00')";
              			dayslow = executeCountQuery(con, sql);
          				triggerPrice = (float)close.get(i+1);
          				profitPerc = ((float)triggerPrice - Float.parseFloat(daysclose))*100/(float)triggerPrice;
          				profitRupees = ((float) Float.parseFloat(dayshigh) - (float) triggerPrice )*100/(float) triggerPrice;
          				tradeddate = tradedate.get(i).toString();
          				dateexited = tradedate.get(i).toString();
              			sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
             					+ " values ('"+name+"', 'Bull', "+checkPercGreater+", "+(profitPerc-0.05f)+", "+profitRupees+", '"+tradeddate+"', '"+dateexited+"', "+(float)cci.get(i+1)+")";
              			executeSqlQuery(con, sql);
              			profitPerc=0f;
              			break;
              	}
          	}
          	if(dir.equalsIgnoreCase("Bear") ){
          		ema_Cur= ((float)ema_26.get(i) - ((float)ema_26.get(i)*diff/100.00f));
          		checkPercGreater = ((float)open.get(i) - (float)close.get(i))*100/(float)open.get(i);
          		checkPercGreater2 = ((float)close.get(i) - (float)low.get(i))*100/(float)close.get(i);
          		if(checkPercGreater>exit &&  checkPercGreater2> 0.5f && checkPercGreater2 < 1f){
	          			sql = "select max(high) from "+name+"_5 where tradedate >'"+tradedate.get(i)+"' and tradedate <= concat(Date('"+date+"'),' 15:15:00')";
		      			dayshigh = executeCountQuery(con, sql);
		  				triggerPrice = (float)close.get(i+1);
		  				profitPerc = (Float.parseFloat(daysclose)- (float)triggerPrice )*100/(float)triggerPrice;
		  				profitRupees = ((float) Float.parseFloat(dayshigh) - (float) triggerPrice )*100/(float) triggerPrice;
		  				tradeddate = tradedate.get(i).toString();
		  				dateexited = tradedate.get(i).toString();
//          				profitPerc = ((float) Float.parseFloat(dayshigh) - (float) triggerPrice )*100/(float) triggerPrice;
//          				profitRupees = ((float)triggerPrice - Float.parseFloat(dayslow))*100/(float)triggerPrice;
              			sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
             					+ " values ('"+name+"', 'Bear', "+checkPercGreater+", "+(profitPerc-0.05f)+", "+profitRupees+", '"+tradeddate+"', '"+dateexited+"', "+(float)cci.get(i+1)+")";
              			executeSqlQuery(con, sql);
              			profitPerc=0f;
              			break;
              	}
          	}
       }
	}
   
   void calculateIntraDayCrossBackup(java.sql.Connection con, String name,  String date, String prevDate, float triggerPrice, String dir, 
		   String indicator) throws IOException, SQLException{
		
		ResultSet rs=null;float intrarange=0f, breakout1=0f, breakout2=0f;;
		String tableName="";
		float trigger = triggerPrice;
		if(indicator.equalsIgnoreCase("Will")){
			tableName = "williamsresults";
		}else if(indicator.equalsIgnoreCase("ADX")){
			tableName = "ADXresults";
		}else if(indicator.equalsIgnoreCase("MACD")){
			tableName = "MACDresults";
		}else if(indicator.equalsIgnoreCase("CCI")){
			tableName = "CCIresults";
		}else if(indicator.equalsIgnoreCase("ENVELOPE")){
			tableName = "maenveloperesults";
		}else if(indicator.equalsIgnoreCase("RANGE")){
			tableName = "RANGEresults";
		}else if(indicator.equalsIgnoreCase("PSAR")){
			tableName = "psarresults";
		}
		List open = new ArrayList<Float>();
		List high = new ArrayList<Float>();
		List low = new ArrayList<Float>();
		List close = new ArrayList<Float>();
		List cci = new ArrayList<Float>();List adx = new ArrayList<Float>();
		List roc = new ArrayList<Float>();
		List tradedate = new ArrayList<String>();
		List supertrend = new ArrayList<Integer>();
		List cciReversal = new ArrayList<String>();
		List ema_26 = new ArrayList<Float>();
		List elder = new ArrayList<String>();
		List ema_12 = new ArrayList<Float>();
		List hist = new ArrayList<Float>();
		List obv = new ArrayList<Long>();
		String dayshigh="";String dayslow=""; String daysclose="", daysopen="";
		dayshigh = executeCountQuery(con, "select high from "+name+" where tradedate=Date('"+date+"')");
		dayslow = executeCountQuery(con, "select low from "+name+" where tradedate=Date('"+date+"')");
		daysclose = executeCountQuery(con, "select close from "+name+" where tradedate=Date('"+date+"')");
		daysopen = executeCountQuery(con, "select open from "+name+" where tradedate=Date('"+date+"')");
		String interval="15";
		String sql="select adx, roc, obv,ema1, histogram, ema2, supertrend_reversal, cci, cci_reversal, (case when (tradedate<concat(Date('"+date+"'),' 9:00:00')) then 0 else 1 end) timecheck, tradedate,open,high,low,close "+
				" from "+name+"_"+interval+" where tradedate >=concat(Date('"+date+" '),' 9:10:00') and tradedate <= concat(Date('"+date+"'),' 15:00:00')";
//       System.out.println(sql);
       rs = executeSelectSqlQuery(con, sql);
       
       float profitRupees=0f,profitPerc=0f;
       int count=0;
       while(rs.next()){
	    	open.add(rs.getFloat("open"));
	       	close.add(rs.getFloat("close"));
	       	high.add(rs.getFloat("high"));
	       	low.add(rs.getFloat("low"));
	       	cci.add(rs.getFloat("cci"));
	       	roc.add(rs.getFloat("roc")); 
	       	tradedate.add(rs.getString("tradedate"));
	       	cciReversal.add(rs.getString("cci_reversal"));
	       	supertrend.add(rs.getInt("supertrend_reversal"));
	       	ema_26.add(rs.getFloat("ema2"));
	       	obv.add(rs.getLong("obv"));
	       	adx.add(rs.getFloat("adx"));
	       	
	       	ema_12.add(rs.getFloat("ema1")); 
	       	hist.add(rs.getFloat("histogram")); 
	       	if(count>=1){
	       		if((float)ema_12.get(count) > (float)ema_12.get(count-1) && (float)hist.get(count) > (float)hist.get(count-1)){
	       			elder.add("Bull");
	       		}else if((float)ema_12.get(count) < (float)ema_12.get(count-1) && (float)hist.get(count) < (float)hist.get(count-1)){
	       			elder.add("Bear");
	       		}else{
	       			elder.add("Neutral");
	       		}
	       	}else{
	       		elder.add("");
	       	}
	       	count++;
       }
       boolean check=false; float intrahigh=0f, intralow=0f, upbreak=0f, downbreak=0f; boolean donttrade=false;
//       sql = "select (max(high) - min(low)) as intrarange, max(high) as intrahigh, min(low) as intralow from "+name+"_1 where tradedate >=concat(Date('"+date+" '),' 9:10:00') and tradedate <= concat(Date('"+date+"'),' 10:00:00')";
//       rs = executeSelectSqlQuery(con, sql);
//       while (rs.next()){
//    	   intrarange = rs.getFloat("intrarange");
//    	   intrahigh = rs.getFloat("intrahigh");
//    	   intralow = rs.getFloat("intralow");
//       }
//       sql = "select breakout1 from "+name+" where tradedate =Date('"+date+" ')";
//       breakout1 = Float.parseFloat(executeCountQuery(con, sql));
//       sql = "select breakout2 from "+name+" where tradedate =Date('"+date+" ')";
//       breakout2 = Float.parseFloat(executeCountQuery(con, sql));
//       
//       if(intrarange < breakout1){
//    	   upbreak = intralow + breakout1;
//    	   downbreak = intrahigh - breakout1;
//       }else if (intrarange > breakout1 && intrarange < breakout2){
//    	   upbreak = intralow + breakout2;
//    	   downbreak = intrahigh - breakout2;
//       }else if (intrarange > breakout2){
//    	   donttrade=true;
//       }
       String dateexited="";
//       if(dir.equalsIgnoreCase("Bull") )
//    	   triggerPrice = (triggerPrice - (triggerPrice*0.1f/100.00f));
//       else if(dir.equalsIgnoreCase("Bear") )
//    	   triggerPrice = (triggerPrice + (triggerPrice*0.1f/100.00f));
       long obv_Prevhigh=0,obv_PrevLow=0;
       sql = "select coalesce(max(obv),0) from "+name+"_1 where date(tradedate) =Date('"+prevDate+" ')";
//       System.out.println(sql);
//       obv_Prevhigh = Long.parseLong(executeCountQuery(con, sql));
       
       sql = "select coalesce(min(obv),0) from "+name+"_1 where date(tradedate) =Date('"+prevDate+" ')";
//       obv_PrevLow = Long.parseLong(executeCountQuery(con, sql));
       float checkPercGreater=0f;
       float p=1f,l=-0.5f, diffPerc=0.2f, ema_Cur=0.0f, diff=0.1f;
       int exit=10;int adxFilter=30;
       for (int i=0; i< tradedate.size()-1; i++){
    	   dateexited="";
    	   if (i==0) continue;
    	   String maxObvIntra="", todaysOpen="", maxHigh="";;
          	if(dir.equalsIgnoreCase("Bull") ){
//          		&& elder.get(i-1).toString().equalsIgnoreCase("Bull") && elder.get(i-1).toString().equalsIgnoreCase("Bull")
          		ema_Cur= ((float)ema_26.get(i) + ((float)ema_26.get(i)*diff/100.00f));
          		checkPercGreater = ((float)close.get(i) - (float)open.get(i))*100/(float)open.get(i);
          			if( Float.parseFloat(daysopen)  < triggerPrice && (float)high.get(i) > triggerPrice && (float)high.get(i-1) < triggerPrice
          					&& (float)adx.get(i-1) > adxFilter)
//          		if(checkPercGreater > exit)
          		{
          			if(check == false){
          				boolean gotentered=false; 
          				sql = "select max(high) from "+name+"_"+interval+" where tradedate <'"+tradedate.get(i)+"' and tradedate >= concat(Date('"+date+"'),' 9:10:00')";
          				if(Float.parseFloat(executeCountQuery(con, sql)) > triggerPrice )
          					break;
          				
          				
          				sql = "select max(obv) from "+name+"_"+interval+" where tradedate <'"+tradedate.get(i)+"' and tradedate >= concat(Date('"+date+"'),' 9:10:00')";
          				maxObvIntra = executeCountQuery(con, sql);
//          				if(Long.parseLong(maxObvIntra) < (long)obv.get(i) ){
//          					break;
//          				}
          				
          				sql = "select max(high) from "+name+"_"+interval+" where tradedate >='"+tradedate.get(i)+"' and tradedate <= concat(Date('"+date+"'),' 15:15:00')";
          				dayshigh = executeCountQuery(con, sql);
          				
          				sql = "select min(low) from "+name+"_"+interval+" where tradedate >'"+tradedate.get(i)+"' and tradedate <= concat(Date('"+date+"'),' 15:15:00')";
          				dayslow = executeCountQuery(con, sql);
//          				triggerPrice = (float) open.get(i+1);
          				
          				
          				int c=1;
          				for(int j=i; j< tradedate.size()-1; j++){
          					if(c>exit)
          						break;
          					
          					if(((float) high.get(j) - (float) triggerPrice)*100/(float) triggerPrice >= p){
          						profitPerc = ((float) high.get(j) - (float) triggerPrice)*100/(float) triggerPrice;
          						profitPerc = p;
          						dateexited = tradedate.get(j).toString();
          						gotentered = true;
          						break;
          					}else if (((float) low.get(j+1) - (float) triggerPrice)*100/(float) triggerPrice <= l){
          						profitPerc = ((float) low.get(j+1) - (float) triggerPrice)*100/(float) triggerPrice;
          						profitPerc = l;
          						dateexited = tradedate.get(j+1).toString();
          						gotentered = true;
          						break;
          					}
          					
          					c++;
          				}
          				if(dateexited.equals("") || gotentered==false){
          					dateexited =  tradedate.get(i+1+exit).toString();
//          					profitPerc = ((float)open.get(i+1) - Float.parseFloat(daysclose))*100/(float)(float)open.get(i+1);
          					profitPerc = ((float) open.get(i+1+exit) - (float) triggerPrice)*100/(float) triggerPrice;
          				}
//          				System.out.println(sql);
//          				profitPerc = (Math.max((float)high.get(i),  (float)high.get(i+1)) - triggerPrice)*100/triggerPrice;
//          				profitPerc = (Float.parseFloat(dayshigh) - (float) open.get(i+1))*100/(float) open.get(i+1);
//          				profitPerc = (Float.parseFloat(dayshigh) - triggerPrice)*100/(float)triggerPrice;
//          				profitPerc = ((float) high.get(i+1) - (float) high.get(i))*100/(float) high.get(i);
          				
              			if(profitPerc < .5){
//              				profitPerc = (Float.parseFloat(daysclose) - (float)open.get(i+1))*100/(float)open.get(i+1);
//              				profitRupees = (Float.parseFloat(daysclose) - (float)open.get(i+1));
          				}
              			if((float) close.get(i) > triggerPrice ){
          					profitRupees = (float)1;
          					
          				}
          				else{
          					//triggerPrice = (float) open.get(i+1);
//          					if(!elder.get(i).toString().equalsIgnoreCase("Bull"))
//          						profitPerc = (triggerPrice - (float)open.get(i+1))*100/triggerPrice;
//          					profitPerc = ( (float)open.get(i+1) - triggerPrice)*100/triggerPrice;
          					profitRupees = (float)100;
          					c= 1;
          				}
//              			if(elder.get(i-1).toString().equalsIgnoreCase("Bull") && elder.get(i-2).toString().equalsIgnoreCase("Bull") && elder.get(i-3).toString().equalsIgnoreCase("Bull"))
//              				profitPerc = (Math.max((float)high.get(i),  (float)high.get(i)) - triggerPrice)*100/triggerPrice;
              				for (int x=i; x< tradedate.size()-1; x++){
              					if(elder.get(x).toString().equalsIgnoreCase("Bear")){
              						profitPerc = (Math.max((float)close.get(x),  (float)close.get(x)) - triggerPrice)*100/triggerPrice;
              						break;
              					}else if(x==tradedate.size()){
              						profitPerc = (Math.max((float)close.get(x),  (float)close.get(x)) - triggerPrice)*100/triggerPrice;
              						break;
              					}
              				}
              				profitPerc = ((float)high.get(i) - triggerPrice)*100/triggerPrice;
////              			else break;
//          				if(profitPerc<p){
//          					profitPerc = ((float)open.get(i+1)- triggerPrice)*100/triggerPrice;
//          				}else{
//          					profitPerc = p;
//          				}
          				profitPerc = ((float)open.get(i+1) - Float.parseFloat(dayslow))*100/(float)open.get(i+1);
          				profitPerc = ((float)high.get(i) - triggerPrice )*100/(float)triggerPrice;
//          				profitPerc = ((float)open.get(i+1) - (float)low.get(i+1))*100/(float)open.get(i+1);
//          				profitPerc = ((float)high.get(i+1) - (float)open.get(i+1))*100/(float)open.get(i+1);
//          				if(profitPerc<p){
//          					profitPerc = (triggerPrice - (float)open.get(i+1))*100/triggerPrice;
//          					profitPerc = -0.10f;
//          				}else{
//          					profitPerc = p;
//          				}
              			sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
             					+ " values ('"+name+"', 'Bull', "+triggerPrice+", "+(profitPerc-0.05f)+", "+(float)cci.get(i+2)+", '"+tradedate.get(i)+"', '"+dateexited+"', "+(float)cci.get(i+1)+")";
//             			System.out.println(sql);
              			executeSqlQuery(con, sql);
              			profitPerc=0f;
              			break;
          			}
              	}
          	}
          	if(dir.equalsIgnoreCase("Bear") ){
          		ema_Cur= ((float)ema_26.get(i) - ((float)ema_26.get(i)*diff/100.00f));
          		checkPercGreater = ((float)open.get(i) - (float)close.get(i))*100/(float)open.get(i);
//          		&& elder.get(i-1).toString().equalsIgnoreCase("Bear") && elder.get(i-1).toString().equalsIgnoreCase("Bear")
          		if( Float.parseFloat(daysopen)  > triggerPrice &&  (float)low.get(i) < triggerPrice && (float)low.get(i-1) > triggerPrice 
          				&& (float)adx.get(i-1) > adxFilter )
//          		if(checkPercGreater>exit)
          		{
          			if(check == false){
          				sql = "select min(low) from "+name+"_"+interval+" where tradedate <'"+tradedate.get(i)+"' and tradedate >= concat(Date('"+date+"'),' 9:10:00')";
          				if(Float.parseFloat(executeCountQuery(con, sql)) < triggerPrice )
          					break;
          				
          				sql = "select min(obv) from "+name+"_"+interval+" where tradedate <'"+tradedate.get(i)+"' and tradedate >= concat(Date('"+date+"'),' 9:10:00')";
          				maxObvIntra = executeCountQuery(con, sql);
//          				if(Long.parseLong(maxObvIntra) < (long)obv.get(i) ){
//          					break;
//          				}
          				
          				sql = "select min(low) from "+name+"_"+interval+" where tradedate >='"+tradedate.get(i)+"' and tradedate <= concat(Date('"+date+"'),' 15:15:00')";
          				dayslow = executeCountQuery(con, sql);
          				
          				sql = "select max(high) from "+name+"_"+interval+" where tradedate >'"+tradedate.get(i)+"' and tradedate <= concat(Date('"+date+"'),' 15:15:00')";
          				dayshigh = executeCountQuery(con, sql);
          				
          				boolean gotentered=false; 
          				int c=1;
          				for(int j=i; j< tradedate.size()-1; j++){
          					if(c>exit)
          						break;
          					
          					if (((float) triggerPrice - (float) high.get(j+1))*100/(float) triggerPrice <= l){
          						profitPerc = ((float) triggerPrice - (float) high.get(j+1))*100/(float) triggerPrice;
          						profitPerc = l;
          						dateexited = tradedate.get(j+1).toString();
          						gotentered= true;
          						break;
          					}else if (((float) triggerPrice - (float) low.get(j))*100/(float) triggerPrice >= p){
          						profitPerc = ((float) triggerPrice - (float) low.get(j))*100/(float) triggerPrice;
          						profitPerc = p;
          						dateexited = tradedate.get(j).toString();
          						gotentered= true;
          						break;
          					}
          					c++;
          				}
          				if(dateexited.equals("") || gotentered==false){
          					dateexited =  tradedate.get(i+1+exit).toString();
          					profitPerc = ((float)triggerPrice - (float)open.get(i+1+exit))*100/(float)triggerPrice;
          				}
          				
//          				profitPerc = (triggerPrice - Math.min((float)low.get(i),  (float)low.get(i+1)))*100/triggerPrice;
//          				profitRupees = (float)cci.get(i);
              			if(profitPerc < 0.5){
//              				profitPerc = ((float)open.get(i+1) - Float.parseFloat(daysclose))*100/(float)(float)open.get(i+1);
//              				profitRupees = ((float)open.get(i+1) - Float.parseFloat(daysclose));
          				}
              			if((float) close.get(i) < trigger){
          					profitRupees = (float)1;
          				}
          				else{
          					//triggerPrice = (float) open.get(i+1);
//          					if(!elder.get(i).toString().equalsIgnoreCase("Bear"))
//          						profitPerc = (triggerPrice - (float)open.get(i+1))*100/triggerPrice;
          					profitRupees = (float)100;
          					c= 1;
          				}
              			
//              			if(elder.get(i-1).toString().equalsIgnoreCase("Bear") && elder.get(i-2).toString().equalsIgnoreCase("Bear") && elder.get(i-3).toString().equalsIgnoreCase("Bear"))
//              				profitPerc = (triggerPrice - Math.min((float)low.get(i),  (float)low.get(i)))*100/triggerPrice;
              				profitPerc = (triggerPrice - (float)low.get(i))*100/triggerPrice;
//              			else break;
              				profitPerc = ((float)open.get(i+1) - (float)low.get(i+1))*100/(float)open.get(i+1);
              			if(profitPerc<p){
          					profitPerc = (triggerPrice - (float)open.get(i+1))*100/triggerPrice;
          					profitPerc = -0.1f;
          				}else{
          					profitPerc = p;
          				}
//              			profitPerc = (triggerPrice - (float)close.get(i))*100/triggerPrice;
              			profitPerc = ((float)triggerPrice - (float)low.get(i))*100/(float)triggerPrice;
//              				profitPerc = (Float.parseFloat(dayshigh) - (float)open.get(i+1))*100/(float)open.get(i+1);
//              				profitPerc = ((float)high.get(i+1) - (float)open.get(i+1))*100/(float)open.get(i+1);
              				
              			sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
             					+ " values ('"+name+"', 'Bear', "+triggerPrice+", "+(profitPerc-0.05f)+", "+(float)cci.get(i+2)+", '"+tradedate.get(i)+"', '"+dateexited+"', "+(float)cci.get(i+1)+")";
//              			System.out.println(sql);
              			executeSqlQuery(con, sql);
              			break;
          			}
              	}
          	}
       }
	}
   
   void calculateIntraDayCrossBuyAtYest(java.sql.Connection con, String name,  String date, String prevDate, float triggerPrice, String dir, 
		   String indicator) throws IOException, SQLException{
		
		ResultSet rs=null;float intrarange=0f, breakout1=0f, breakout2=0f;;
		String tableName="";
		float trigger = triggerPrice;
		if(indicator.equalsIgnoreCase("Will")){
			tableName = "williamsresults";
		}else if(indicator.equalsIgnoreCase("ADX")){
			tableName = "ADXresults";
		}else if(indicator.equalsIgnoreCase("MACD")){
			tableName = "MACDresults";
		}else if(indicator.equalsIgnoreCase("CCI")){
			tableName = "CCIresults";
		}else if(indicator.equalsIgnoreCase("ENVELOPE")){
			tableName = "maenveloperesults";
		}else if(indicator.equalsIgnoreCase("RANGE")){
			tableName = "RANGEresults";
		}else if(indicator.equalsIgnoreCase("PSAR")){
			tableName = "psarresults";
		}
		List open = new ArrayList<Float>();
		List high = new ArrayList<Float>();
		List low = new ArrayList<Float>();
		List close = new ArrayList<Float>();
		List cci = new ArrayList<Float>();
		List roc = new ArrayList<Float>();
		List tradedate = new ArrayList<String>();
		List supertrend = new ArrayList<Integer>();
		List cciReversal = new ArrayList<String>();
		List timecheck = new ArrayList<Integer>();
		List obv = new ArrayList<Long>();
		String dayshigh="";String dayslow=""; String daysclose="", daysopen="";
		dayshigh = executeCountQuery(con, "select high from "+name+" where tradedate=Date('"+date+"')");
		dayslow = executeCountQuery(con, "select low from "+name+" where tradedate=Date('"+date+"')");
		daysclose = executeCountQuery(con, "select close from "+name+" where tradedate=Date('"+date+"')");
		daysopen = executeCountQuery(con, "select open from "+name+" where tradedate=Date('"+date+"')");
		String interval="1";
		String sql="select roc, obv, supertrend_reversal, cci, cci_reversal, (case when (tradedate<concat(Date('"+date+"'),' 9:00:00')) then 0 else 1 end) timecheck, tradedate,open,high,low,close "+
				" from "+name+"_"+interval+" where tradedate >=concat(Date('"+date+" '),' 9:45:00') and tradedate <= concat(Date('"+date+"'),' 15:00:00')";
//       System.out.println(sql);
       rs = executeSelectSqlQuery(con, sql);
       
       float profitRupees=0f,profitPerc=0f;
       while(rs.next()){
	    	open.add(rs.getFloat("open"));
	       	close.add(rs.getFloat("close"));
	       	high.add(rs.getFloat("high"));
	       	low.add(rs.getFloat("low"));
	       	cci.add(rs.getFloat("cci"));
	       	roc.add(rs.getFloat("roc")); 
	       	tradedate.add(rs.getString("tradedate"));
	       	cciReversal.add(rs.getString("cci_reversal"));
	       	supertrend.add(rs.getInt("supertrend_reversal"));
	       	timecheck.add(rs.getInt("timecheck"));
	       	obv.add(rs.getLong("obv"));
       }
       boolean check=false; float intrahigh=0f, intralow=0f, upbreak=0f, downbreak=0f; boolean donttrade=false;
       sql = "select (max(high) - min(low)) as intrarange, max(high) as intrahigh, min(low) as intralow from "+name+"_1 where tradedate >=concat(Date('"+date+" '),' 9:10:00') and tradedate <= concat(Date('"+date+"'),' 10:00:00')";
       rs = executeSelectSqlQuery(con, sql);
       while (rs.next()){
    	   intrarange = rs.getFloat("intrarange");
    	   intrahigh = rs.getFloat("intrahigh");
    	   intralow = rs.getFloat("intralow");
       }
       sql = "select breakout1 from "+name+" where tradedate =Date('"+date+" ')";
       breakout1 = Float.parseFloat(executeCountQuery(con, sql));
       sql = "select breakout2 from "+name+" where tradedate =Date('"+date+" ')";
       breakout2 = Float.parseFloat(executeCountQuery(con, sql));
       
       if(intrarange < breakout1){
    	   upbreak = intralow + breakout1;
    	   downbreak = intrahigh - breakout1;
       }else if (intrarange > breakout1 && intrarange < breakout2){
    	   upbreak = intralow + breakout2;
    	   downbreak = intrahigh - breakout2;
       }else if (intrarange > breakout2){
    	   donttrade=true;
       }
       String dateexited="";
//       if(dir.equalsIgnoreCase("Bull") )
//    	   triggerPrice = (triggerPrice - (triggerPrice*0.2f/100.00f));
//       else if(dir.equalsIgnoreCase("Bear") )
//    	   triggerPrice = (triggerPrice + (triggerPrice*0.2f/100.00f));
       long obv_Prevhigh=0,obv_PrevLow=0;
       sql = "select coalesce(max(obv),0) from "+name+"_1 where date(tradedate) =Date('"+prevDate+" ')";
//       System.out.println(sql);
       obv_Prevhigh = Long.parseLong(executeCountQuery(con, sql));
       
       sql = "select coalesce(min(obv),0) from "+name+"_1 where date(tradedate) =Date('"+prevDate+" ')";
       obv_PrevLow = Long.parseLong(executeCountQuery(con, sql));
       
       float p=.1f,l=-.15f;int exit=4;
       for (int i=0; i< tradedate.size()-1; i++){
    	   dateexited="";
    	   if (i==0) continue;
          	if(dir.equalsIgnoreCase("Bull") ){
//          		triggerPrice = upbreak;
//          		triggerPrice = (triggerPrice - (triggerPrice*0.2f/100.00f));
          		
          			if( Float.parseFloat(daysopen)  < triggerPrice && 
          					(float)high.get(i) > triggerPrice && (float)high.get(i-1) < triggerPrice ){
          			if(check == false){
          				boolean gotentered=false; 
          				sql = "select max(high) from "+name+"_"+interval+" where tradedate <'"+tradedate.get(i)+"' and tradedate >= concat(Date('"+date+"'),' 9:10:00')";
          				if(Float.parseFloat(executeCountQuery(con, sql)) > triggerPrice )
          					break;
          				sql = "select max(high) from "+name+"_"+interval+" where tradedate >='"+tradedate.get(i)+"' and tradedate <= concat(Date('"+date+"'),' 15:15:00')";
          				dayshigh = executeCountQuery(con, sql);
//          				triggerPrice = (float) open.get(i+1);
          				int c=1;
          				for(int j=i; j< tradedate.size()-1; j++){
          					if(c>2)
          						break;
          					if(((float) high.get(j) - (float) triggerPrice)*100/(float) triggerPrice >= p){
          						profitPerc = ((float) high.get(j) - (float) triggerPrice)*100/(float) triggerPrice;
          						profitPerc = p;
          						dateexited = tradedate.get(j).toString();
          						gotentered = true;
          						break;
          					}else if (((float) low.get(j+1) - (float) triggerPrice)*100/(float) triggerPrice <= l){
          						profitPerc = ((float) low.get(j+1) - (float) triggerPrice)*100/(float) triggerPrice;
          						profitPerc = l;
          						dateexited = tradedate.get(j+1).toString();
          						gotentered = true;
          						break;
          					}
          					
          					c++;
          				}
          				if(dateexited.equals("") || gotentered==false){
          					dateexited =  tradedate.get(i+3).toString();
//          					profitPerc = ((float)open.get(i+1) - Float.parseFloat(daysclose))*100/(float)(float)open.get(i+1);
          					profitPerc = ((float) open.get(i+3) - (float) triggerPrice)*100/(float) triggerPrice;
          				}
//          				System.out.println(sql);
//          				profitPerc = (Math.max((float)high.get(i),  (float)high.get(i+1)) - triggerPrice)*100/triggerPrice;
//          				profitPerc = (Float.parseFloat(dayshigh) - (float) open.get(i+1))*100/(float) open.get(i+1);
//          				profitPerc = (Float.parseFloat(dayshigh) - (float)open.get(i+1))*100/(float)open.get(i+1);
//          				profitPerc = ((float) high.get(i+1) - (float) high.get(i))*100/(float) high.get(i);
          				profitRupees = (float)cci.get(i);
              			if(profitPerc < .5){
//              				profitPerc = (Float.parseFloat(daysclose) - (float)open.get(i+1))*100/(float)open.get(i+1);
//              				profitRupees = (Float.parseFloat(daysclose) - (float)open.get(i+1));
          				}
              			sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
             					+ " values ('"+name+"', 'Bull', "+triggerPrice+", "+profitPerc+", "+(Math.abs(obv_Prevhigh) - Math.abs((long)obv.get(i-1)))+", '"+tradedate.get(i)+"', '"+dateexited+"', "+c+")";
//             			System.out.println(sql);
              			executeSqlQuery(con, sql);
              			break;
          			}
              	}
          	}
          	if(dir.equalsIgnoreCase("Bear") ){
//          		triggerPrice = downbreak;
//          		triggerPrice = (triggerPrice + (triggerPrice*0.2f/100.00f));
          		
          		if( Float.parseFloat(daysopen)  > triggerPrice && 
          				(float)low.get(i) < triggerPrice && (float)low.get(i-1) > triggerPrice ){
          			if(check == false){
          				sql = "select min(low) from "+name+"_"+interval+" where tradedate <'"+tradedate.get(i)+"' and tradedate >= concat(Date('"+date+"'),' 9:10:00')";
          				if(Float.parseFloat(executeCountQuery(con, sql)) < triggerPrice )
          					break;
          				
          				sql = "select min(low) from "+name+"_"+interval+" where tradedate >='"+tradedate.get(i)+"' and tradedate <= concat(Date('"+date+"'),' 15:15:00')";
          				dayslow = executeCountQuery(con, sql);
          				boolean gotentered=false; 
//          				triggerPrice = (float) open.get(i+1);
          				int c=1;
          				for(int j=i; j< tradedate.size()-1; j++){
          					if(c>2)
          						break;
          					if (((float) triggerPrice - (float) high.get(j+1))*100/(float) triggerPrice <= l){
          						profitPerc = ((float) triggerPrice - (float) high.get(j+1))*100/(float) triggerPrice;
          						profitPerc = l;
          						dateexited = tradedate.get(j+1).toString();
          						gotentered= true;
          						break;
          					}else if (((float) triggerPrice - (float) low.get(j))*100/(float) triggerPrice >= p){
          						profitPerc = ((float) triggerPrice - (float) low.get(j))*100/(float) triggerPrice;
          						profitPerc = p;
          						dateexited = tradedate.get(j).toString();
          						gotentered= true;
          						break;
          					}
          					c++;
          				}
          				if(dateexited.equals("") || gotentered==false){
          					dateexited =  tradedate.get(i+3).toString();
          					profitPerc = ((float)triggerPrice - (float)open.get(i+3))*100/(float)triggerPrice;
          				}
//          				profitPerc = ((float)open.get(i+1) - Float.parseFloat(dayslow))*100/(float)open.get(i+1);
//          				profitPerc = (triggerPrice - Math.min((float)low.get(i),  (float)low.get(i+1)))*100/triggerPrice;
          				profitRupees = (float)cci.get(i);
              			if(profitPerc < 0.5){
//              				profitPerc = ((float)open.get(i+1) - Float.parseFloat(daysclose))*100/(float)(float)open.get(i+1);
//              				profitRupees = ((float)open.get(i+1) - Float.parseFloat(daysclose));
          				}
              			sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date, dateexited, noofcandles) "
             					+ " values ('"+name+"', 'Bear', "+triggerPrice+", "+profitPerc+", "+(Math.abs((long)obv.get(i-1)) - Math.abs(obv_PrevLow) )+", '"+tradedate.get(i)+"', '"+dateexited+"', "+c+")";
//              			System.out.println(sql);
              			executeSqlQuery(con, sql);
              			break;
          			}
              	}
          	}
       }
	}
   
   void calculateIntraDayCrossOrig(java.sql.Connection con, String name,  String date, float triggerPrice, String dir, String indicator) throws IOException, SQLException{
		
		ResultSet rs=null;float trig=0f;
		String tableName="";
		if(indicator.equalsIgnoreCase("Will")){
			tableName = "williamsresults";
		}else if(indicator.equalsIgnoreCase("ADX")){
			tableName = "ADXresults";
		}else if(indicator.equalsIgnoreCase("MACD")){
			tableName = "MACDresults";
		}else if(indicator.equalsIgnoreCase("CCI")){
			tableName = "CCIresults";
		}else if(indicator.equalsIgnoreCase("ENVELOPE")){
			tableName = "maenveloperesults";
		}else if(indicator.equalsIgnoreCase("RANGE")){
			tableName = "RANGEresults";
		}else if(indicator.equalsIgnoreCase("PSAR")){
			tableName = "psarresults";
		}
		List open = new ArrayList<Float>();
		List high = new ArrayList<Float>();
		List low = new ArrayList<Float>();
		List close = new ArrayList<Float>();
		List tradedate = new ArrayList<String>();
		List cciReversal = new ArrayList<String>();
		List timecheck = new ArrayList<Integer>();
		String sql="select cci_reversal, (case when (tradedate<concat(Date('"+date+"'),' 10:00:00')) then 0 else 1 end) timecheck, tradedate,open,high,low,close "+
				" from "+name+"_3 where tradedate >=concat(Date('"+date+" '),' 10:00:00') and tradedate <= concat(Date('"+date+"'),' 14:30:00')";
//      System.out.println(sql);
      rs = executeSelectSqlQuery(con, sql);
      
      float profitRupees=0f,profitPerc=0f;
      while(rs.next()){
	    	open.add(rs.getFloat("open"));
	       	close.add(rs.getFloat("close"));
	       	high.add(rs.getFloat("high"));
	       	low.add(rs.getFloat("low")); 
	       	tradedate.add(rs.getString("tradedate"));
	       	cciReversal.add(rs.getString("cci_reversal"));
	       	timecheck.add(rs.getInt("timecheck"));
      }
      boolean check=false;
      for (int i=0; i<= tradedate.size()-1; i++){
   	   if (i==0) continue;
         	if(dir.equalsIgnoreCase("Bull")){
         		if(cciReversal.get(i).equals("Bull") && cciReversal.get(i-1).equals("")){
//         			if((Integer)timecheck.get(i)==0) check=true;
         			if(check == false){
         				
         				profitPerc = ((float)high.get(i) - triggerPrice)*100/triggerPrice;
             			profitRupees = ((float)high.get(i) - triggerPrice);
             			if(profitPerc < 0.1){
         					profitPerc = ((float)close.get(i) - triggerPrice)*100/triggerPrice;
         					profitRupees = ((float)close.get(i) - triggerPrice);
         				}
             			sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
            					+ " values ('"+name+"', 'Bull', "+triggerPrice+", "+profitPerc+", "+profitRupees+", '"+tradedate.get(i)+"')";
            			executeSqlQuery(con, sql);
             			break;
         			}
         			
             	}
         	}
         	if(dir.equalsIgnoreCase("Bear")){
         		if((float)low.get(i) < triggerPrice){
//         			if((Integer)timecheck.get(i)==0) check=true;
         			if(check == false){
         				profitPerc = (triggerPrice - (float)low.get(i))*100/triggerPrice;
             			profitRupees = (triggerPrice - (float)low.get(i));
             			if(profitPerc < 0.1){
             				profitPerc = (triggerPrice - (float)close.get(i))*100/(float)close.get(i);
             				profitRupees = (triggerPrice - (float)close.get(i));
         				}
             			sql = "insert into "+tableName+"(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
            					+ " values ('"+name+"', 'Bear', "+triggerPrice+", "+profitPerc+", "+profitRupees+", '"+tradedate.get(i)+"')";
            			executeSqlQuery(con, sql);
             			break;
         			}
         			
             	}
         	}
      }
	}
	void calculateWilliamsIntra(java.sql.Connection con, String name,  String date, float highT, float lowT,
			float willR) throws IOException, SQLException{
		String dir="";
		ResultSet rs=null;float trig=0f;float close=0f, trigTemp=0f;
		float high=0f, low=0f; String tradedate="", tradedate1="";
		String sql="select supertrend_reversal, tradedate, ("+highT+"-close)/("+highT+"-"+lowT+")*100 as trig,open,high,low,close "+
				" from "+name+"_5 where tradedate >=concat(Date('"+date+" '),' 9:00:00') and tradedate <= concat(Date('"+date+"'),' 15:30:00')";
//        System.out.println(sql);
        rs = executeSelectSqlQuery(con, sql);
        int check=0;float highTemp=0f,highTemp1=0f, lowTemp=0f, lowTemp1=0f;
        int bullCheck = 0;int bearCheck = 0;
        while(rs.next()){
        	trig = rs.getFloat("trig");
        	close = rs.getFloat("close");
        	high = rs.getFloat("high");
        	low = rs.getFloat("low");
        	tradedate = rs.getString("tradedate");
        	dir = rs.getString("supertrend_reversal").trim();
//        	System.out.println(dir);
        	if(bullCheck==1){
        		if(high>highTemp1) highTemp1 = high;
        	}
        	if(bearCheck==1){
        		if(low<lowTemp1) lowTemp1 = low;
        	}
        	highTemp = close;
        	lowTemp = close;
        	if(willR > 50){
        		if(trig <50){
        			if(check==0  && dir.equalsIgnoreCase("1")){
        				highTemp = close;
            			check=1;
            			bullCheck=1;
            			highTemp1 = close;
            			tradedate1 = tradedate;
            			trigTemp = trig;
            			if(high>highTemp1) highTemp1 = high;
            			System.out.println(name+","+trig+", bull,"+tradedate1);
        			}
        		}
        	}
        	else if(willR < 50){
        		if(trig >50){
        			if(check==0  && dir.equalsIgnoreCase("-1")){
        				lowTemp = close;
            			check=1;
            			bearCheck=1;
            			lowTemp1 = close;
            			tradedate1 = tradedate;
            			trigTemp = trig;
            			if(low<lowTemp1) lowTemp1 = low;
            			System.out.println(name+","+trig+", bear,"+tradedate1);
        			}
        		}
        	}
        	sql = "update "+name+"_5 set Will_R_5 = "+trig+" where tradedate = '"+tradedate+"'";
			executeSqlQuery(con, sql);
        }
//        System.out.println(bullCheck+","+dir);
        if(bullCheck==1){
        	sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
  					+ " values ('"+name+"', 'Bull', "+highTemp+", "+(highTemp1-highTemp)*100/highTemp+", "+(highTemp1-highTemp)+", '"+tradedate1+"')";
  			executeSqlQuery(con, sql);
        }
//        System.out.println(tradedate1+","+(highTemp1-highTemp)*100/highTemp+", Bull at "+highTemp+","+highTemp1+",will="+trigTemp+"-----------------");
//        System.out.println((highTemp1-highTemp)*100/highTemp);
        if(bearCheck==1 ){
        	sql = "insert into williamsresults(name, reversal, triggerPrice, profitPerc, profitRupees, date) "
  					+ " values ('"+name+"', 'Bear', "+lowTemp+", "+(lowTemp-lowTemp1)*100/lowTemp+", "+(lowTemp-lowTemp1)+", '"+tradedate1+"')";
  			executeSqlQuery(con, sql);
        }
        
//		System.out.println(tradedate1+","+(lowTemp-lowTemp1)*100/lowTemp+", bear at "+lowTemp+","+lowTemp1+",will="+trigTemp+"-------------------");
//        System.out.println((lowTemp-lowTemp1)*100/lowTemp);
	}
   
	void calculateWilliams(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, List<Float> close, List date, 
			int willRange,
			boolean updateForTodayAndNextDay, boolean updateForallDays, boolean isIntraDayData, String path) throws IOException, SQLException{
		Float highT=0.0f, lowT=0f, will_R=0f, willTemp=0f;
		willRange--;int k=0;String sql="",tradeDate="";
		String todaysDate = executeCountQuery(con, "select date(now())");
		TemporaryTable tmp = new TemporaryTable();
		BufferedWriter output = null;
		if(updateForallDays == true || isIntraDayData == true){			
			tmp.createTempTable(con, name);
		  	File file = new File(""+path+name+".txt");
	        output = new BufferedWriter(new FileWriter(file));	
		}
	  	
        
		for (int i=0; i<= date.size()-1; i++){
			if(isIntraDayData==true)
	  			tradeDate = (String) date.get(i).toString();
	  		else
	  			tradeDate = (String) date.get(i);
			if(i>= willRange){
				highT=0f;lowT=0f;k=0;
				for(int j=i-willRange; k<=willRange; j++){
					k++;
					highT = Math.max(highT, high.get(j));
					if(k==1){
						lowT = low.get(j);
					}else{
						lowT = Math.min(lowT, low.get(j));
					}
				}
				
				will_R = (highT-close.get(i))/(highT-lowT)*100f;
//				System.out.println(willTemp+","+will_R+" date="+tradeDate+","+open.get(i)+","+high.get(i)+","+low.get(i)+","+close.get(i) );
				
				
				if(Float.isNaN(will_R))
					will_R = 0.0f;
//				System.out.println(highT+" date="+date.get(i)+" low="+lowT+" w="+will_R);
				if(updateForTodayAndNextDay == true && i== (date.size()-1)){
					sql = "UPDATE "+name+" SET WILL_HIGH="+highT+", WILL_LOW="+lowT+", WILL_R="+will_R+" "
							+ " where tradedate='"+tradeDate+"'";
//					System.out.println(sql);
					executeSqlQuery(con, sql);
				}else if(updateForallDays == true){
					
					output.write(tradeDate);	output.write(","+highT);	output.write(","+lowT);	output.write(","+will_R);
                	output.write("\r\n");
				}
				if(updateForTodayAndNextDay == true && (tradeDate.split(" ")[0]).equalsIgnoreCase(todaysDate) && isIntraDayData == true){
					output.write(tradeDate);	output.write(","+highT);	output.write(","+lowT);	output.write(","+will_R);
                	output.write("\r\n");
				}
			}
			
			willTemp = will_R;
		}
		
		if ( output != null ) output.close();
		if(updateForallDays == true || isIntraDayData == true){			
			

			sql = "LOAD DATA LOCAL INFILE '"+path+name+".txt' "+
					 " INTO TABLE "+name+"_Temp "+
					 " FIELDS TERMINATED BY ',' "+
					 " LINES TERMINATED BY '\n'" +
					 " "+
					 " (tradedate, WILL_HIGH, WILL_LOW, WILL_R) ";
           
          		executeSqlQuery(con, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET  a.WILL_HIGH = b.WILL_HIGH, a.WILL_LOW=b.WILL_LOW, a.WILL_R = b.WILL_R "+
				" WHERE a.tradedate = b.tradedate ";
//          		System.out.println(sql);
          		executeSqlQuery(con, sql);
          		tmp.dropTempTable(con, name);
		}
	}
			
}

