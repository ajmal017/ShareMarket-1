package FetchData;

import java.sql.ResultSet;
import java.sql.SQLException;

import Indicators.Connection;

public class UpdateReversal extends Connection{
	public static void main(String a[]){
		java.sql.Connection dbConnection =null;
		Connection con = new Connection(); UpdateReversal reversal = new UpdateReversal();
		dbConnection = con.getDbConnection();
		try {
			reversal.updateReversal(dbConnection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void updateReversal(java.sql.Connection dbConnection) throws SQLException{
		String sql = "", name="";
		ResultSet rs = null;
		sql = "Delete from StoreReversal";
		executeSqlQuery(dbConnection, sql);
		rs = executeSelectSqlQuery(dbConnection, "Select s.name from symbols s , margintables m where m.name=s.name and volume > 10000000 order by volume desc");
		while(rs.next()){
			name = rs.getString("s.name");
			System.out.println(name);
			sql = "Insert into StoreReversal(tradedate,name, open, high, low, close, psar_Reversal, MACD_CHANGE_DIR, Hist_Zero_Cross, ADX_DM_Crossover,"
					+ " CCI_Reversal, Williams_REVERSAL, MaEnvelope_Reversal, volume) "
					+ " select tradedate,'"+name+"',open, high, low, close , reversal,MACD_CHANGE_DIR, Hist_Zero_Cross,ADX_DM_Crossover,cci_reversal, will_Reversal, "
					+ " envlp_Cross, (select volume from symbols where name ='"+name+"') from "+name+"  order by tradedate desc limit 1";
//			System.out.println(sql);
			executeSqlQuery(dbConnection, sql);
		}
	}
}
