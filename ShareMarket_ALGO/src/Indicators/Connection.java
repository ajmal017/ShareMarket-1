package Indicators;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Connection {
	public java.sql.Connection getDbConnection(){
	        String URL = "jdbc:mysql://localhost/sharemarketNew";
	        String USER = "root";
	        String PASS = "admin";
	        java.sql.Connection dbConnection=null;
	        
	        try{
	               Class.forName("com.mysql.jdbc.Driver");
	               dbConnection = java.sql.DriverManager.getConnection(URL, USER, PASS);
	               return dbConnection;
	        }
	        catch(ClassNotFoundException e){
	        	e.printStackTrace();
	               System.out.println(e.getMessage());
	        }
	        catch(Exception e){
	        	e.printStackTrace();
	               System.out.println(e.getMessage());
	        }
			return null;
	        
		}
	
	public String executeCountQuery(java.sql.Connection con, String strSQL) {
        ResultSet rs = null;
        String res = "";
        Statement stmt=null;
        try {
            
            stmt = con.createStatement();
//            System.out.println("strSQL in ExecuteQuery--"+strSQL);
            rs = stmt.executeQuery(strSQL);
            while (rs.next()) {
                res = rs.getString(1);
            }
            return res;
        } catch (Exception ex) {
            System.out.println(this.getClass().getName());
            System.out.println("ExecuteQuery Error : " + strSQL + "\n" + ex);
            return "0";
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (rs != null) {
                    rs.close();
                }
                if(con!=null) con=null;
            } catch (Exception ex) {
            }
        }
    }
	public void executeSqlQuery(java.sql.Connection dbConnection, String sqlQuery) throws SQLException{
		java.sql.Statement createStatement = dbConnection.createStatement();
        try {
        	
		               
		               int res = createStatement.executeUpdate(sqlQuery);
		        } catch (SQLException e) {
		               // TODO Auto-generated catch block
		               e.printStackTrace();
		        }
        finally{
//        	if(con !=null) con.close();
//        	if(createStatement!=null) createStatement.close();
        }
		 }
	public ResultSet executeSelectSqlQuery(java.sql.Connection dbConnection, String sqlQuery) throws SQLException{
		java.sql.Statement createStatement = dbConnection.createStatement();
        try {
		               return createStatement.executeQuery(sqlQuery);
	        } catch (SQLException e) {
	               // TODO Auto-generated catch block
	               e.printStackTrace();
	        }
	        finally{
//	        	if(con !=null) con.close();
//	        	if(createStatement!=null) createStatement.close();
	        }
		return null;
		 }
	
	public String TextMonth(String id) {
        switch (id) {
            case "JAN":
                return "01";

            case "FEB":
                return "02";

            case "MAR":
                return "03";

            case "APR":
                return "04";

            case "MAY":
                return "05";

            case "JUN":
                return "06";

            case "JUL":
                return "07";

            case "AUG":
                return "08";

            case "SEP":
                return "09";

            case "OCT":
                return "10";

            case "NOV":
                return "11";

            case "DEC":
                return "12";

            default:
                return null;
        }
    }
	
	public String MonthName(int id) {
        switch (id) {
            case 1:
                return "JAN";

            case 2:
                return "FEB";

            case 3:
                return "MAR";

            case 4:
                return "APR";

            case 5:
                return "MAY";

            case 6:
                return "JUN";

            case 7:
                return "JUL";

            case 8:
                return "AUG";

            case 9:
                return "SEP";

            case 10:
                return "OCT";

            case 11:
                return "NOV";

            case 12:
                return "DEC";

            default:
                return null;
        }
    }
	
	public int getMonthNumber(String name) {
        switch (name) {
            case "JAN":
                return 1;

            case "FEB":
                return 2;

            case "MAR":
                return 3;

            case "APR":
                return 4;

            case "MAY":
                return 5;

            case "JUN":
                return 6;

            case "JUL":
                return 7;

            case "AUG":
                return 8;

            case "SEP":
                return 9;

            case "OCT":
                return 10;

            case "NOV":
                return 11;

            case "DEC":
                return 12;

            default:
                return 0;
        }
    }
	
}
