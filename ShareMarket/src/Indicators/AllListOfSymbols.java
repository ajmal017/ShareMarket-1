package Indicators;

import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import com.mysql.jdbc.DatabaseMetaData;

public class AllListOfSymbols extends Connection{

	public static void main(String[] args) throws SQLException {
		java.sql.Connection dbConnection = null;
		try
        {
			Connection con=null;
            con = new Connection();
            
            FileInputStream file = new FileInputStream(new File("C:/Puneeth/SHARE_MARKET/EQUITY_L2.xls"));
 
            
            org.apache.poi.ss.usermodel.Workbook workbook = WorkbookFactory.create(file);
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
 
            //Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();
            List symbolList = new ArrayList<String>();
            while (rowIterator.hasNext()) 
            {
                Row row = rowIterator.next();
                //For each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();
                 
                while (cellIterator.hasNext()) 
                {
                    Cell cell = cellIterator.next();
                    //Check the cell type and format accordingly
                    switch (cell.getCellType()) 
                    {
                        case Cell.CELL_TYPE_NUMERIC:
                            System.out.print(cell.getNumericCellValue());
                            break;
                        case Cell.CELL_TYPE_STRING:
//                            System.out.print(cell.getStringCellValue());
                        	symbolList.add(cell.getStringCellValue());
                            break;
                    }
                }
//                System.out.println("");
            }
            file.close();
//            System.out.println(symbolList.size());
            String sql="";
            String s="";
        	dbConnection = con.getDbConnection();
        	int tableExist=0;
        	DatabaseMetaData metadata = (DatabaseMetaData) dbConnection.getMetaData();
        	String[] types = {"TABLE"};
        	ResultSet resultSet = metadata.getTables(null, null, "%", types);
        	while (resultSet.next()) {
        		String tableName = resultSet.getString(3);
//        		System.out.println(tableName);
        		if(tableName.equalsIgnoreCase("symbols")){
        			tableExist = 1;
        			break;
        		}
        	}
        	
        	if (tableExist!=1){

            	sql =sql + "CREATE TABLE symbols"
            			+ "(name varchar(50) NOT NULL, accuracy varchar(45) NOT NULL, next_day_psar varchar(45) DEFAULT '', PRIMARY KEY (name)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
//            	System.out.println(sql);
            	con.executeSqlQuery(dbConnection, sql);
        	}
            for (int i=0; i<symbolList.size(); i++){
            	
            	try{
            		s = (String) symbolList.get(i);
                	sql = "insert into symbols(name, accuracy) values ('"+s+"','')";
                	con.executeSqlQuery(dbConnection, sql);
            	}catch(Exception e){
            		e.printStackTrace();
            		continue;
            	}
            	
//            	System.out.println(s);
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
		finally{

            if(dbConnection!=null){
				dbConnection.close();
			}
		}
		
    }

	}


