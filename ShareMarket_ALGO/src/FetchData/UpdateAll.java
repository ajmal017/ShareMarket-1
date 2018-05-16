package FetchData;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.transform.TransformerException;

import Indicators.ADX;
import Indicators.Bollinger;
import Indicators.Breakout;
import Indicators.CCI;
import Indicators.Connection;
import Indicators.MACD;
import Indicators.MovingAvgEnvelope;
import Indicators.OnBalanceVolume;
import Indicators.PSAR;
import Indicators.PriceRateOfChange;
import Indicators.RangeCompare;
import Indicators.SimpleMovingAvg;
import Indicators.SuperTrend;
import Indicators.Test;
import Indicators.WILLIAMS;

public class UpdateAll {

	public static void main(String[] args) throws TransformerException, SQLException {
	      Test t = new Test();
	      PSAR psar = new PSAR();ADX adx = new ADX(); MACD macd = new MACD();Bollinger bollinger = new Bollinger();WILLIAMS williams = new WILLIAMS();
	      CCI cci = new CCI();MovingAvgEnvelope ma = new MovingAvgEnvelope(); Breakout brk = new Breakout();
	      SuperTrend superTrend= new SuperTrend();RangeCompare range = new RangeCompare();
	      SimpleMovingAvg sma= new SimpleMovingAvg();
	      PriceRateOfChange roc = new PriceRateOfChange();
	      OnBalanceVolume obv = new OnBalanceVolume();
	      java.sql.Connection dbConnection = null;
	      try{
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	boolean updateForTodayAndNextDay=true; 
	    	  	boolean updateForallDays=true;
	    	  	boolean updateSymbolsTableData=false; boolean updateAllData=true;
	    	  	boolean updateResultTable=false;Float diffPerc=0.00f;
	    	  	boolean isIntraDayData=false; boolean fastProcess=false;
	    	  	boolean insertAllDataToResult = true;
	    	  	String iter="1d";
	    	  	 String name=""; int totalCount=0;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where volume > 100000000 order by id");
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		totalCount++;
	    	  		name= rs.getString("s.name");
	    	  		System.out.println(name);
	    	  		if(!iter.equals("1d"))
	    	  			name =name+"_"+iter+"";
//	    	  		brk.LoadData(dbConnection, name, updateForTodayAndNextDay, true, isIntraDayData, path+"/envelope/"+iter+"/");
////	    	  		//	obv
//	    	  		obv.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/obv/"+iter+"/");
////	    	  		//roc
//	    	  		roc.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/obv/"+iter+"/");
//	    	  		sma.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/envelope/"+iter+"/", 26);
	    	  		
	    	  		
//	    	  		Update psar
//	    	  		psar.getPsar().clear();
//	    	  		psar.getDir().clear();
//	    	  		psar.updatePSAR(dbConnection, name, updateForTodayAndNextDay, true, updateResultTable, isIntraDayData, path+"/cci/"+iter+"/", insertAllDataToResult);
//	    	  	
//	    	  		update adx
//	    	  		adx.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"cci/"+iter+"/");
//	    	  		adx.UpdateADXResults(dbConnection, name, updateSymbolsTableData, updateAllData, updateResultTable, diffPerc,
//	    	  				isIntraDayData, insertAllDataToResult);
//	    	  		update williams
//	    	  		williams.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/cci/"+iter+"/");
//	    	  		williams.UpdateWilliamsResults(dbConnection, name, updateSymbolsTableData, updateAllData, updateResultTable,isIntraDayData, insertAllDataToResult);
////	    	  		update macd
	    	  		macd.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/1d/");
//	    	  		macd.UpdateMACDResults(dbConnection, name, updateSymbolsTableData, updateAllData, updateResultTable, isIntraDayData, insertAllDataToResult);
//	    	  		bollinger.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData, path+"/cci/1d/");
	    	  		//	    	  		update cci
//	    	  		cci.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData,path+"/cci/"+iter+"/");
//	    	  		cci.UpdateCCIResults(dbConnection, name, updateSymbolsTableData, updateAllData, 
//	    	  				updateResultTable, diffPerc, isIntraDayData, insertAllDataToResult, fastProcess);
////	    	  		update ma envelope
//	    	  		ma.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, isIntraDayData,path+"/cci/"+iter+"/");
//	    	  		ma.UpdateENVELOPEResults(dbConnection, name, updateSymbolsTableData, updateAllData, 
//	    	  				updateResultTable, diffPerc, isIntraDayData, insertAllDataToResult);
//	    	  		
//					update range results
//
//	    	  		range.UpdateRangeResults(dbConnection, name, updateSymbolsTableData, updateAllData, updateResultTable, 
//	    	  				diffPerc, isIntraDayData, insertAllDataToResult);
////	    	  		
//	    	  		//supertrend
//	    	  		superTrend.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, 
//	    	  				isIntraDayData, path+"/1d/", 3);
//	    	  		
	    	  		name="";
	    	  	}
	      }
		  	catch(Exception e){
				e.printStackTrace();
			}
	      finally{
	  	  	if(dbConnection!=null)
					dbConnection.close();
	      }
	   }

}
