package org.agora.jac;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;



public class JAgoraComputation {
	
	
	
	
	public static void main(String[] args) throws Exception{
		Class.forName("com.mysql.jdbc.Driver") ;
		Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.8.200:3306/agora-db", "agora-dev", "pythagoras") ;
		Statement stmt = conn.createStatement() ;
		String query = "select * from `test-table`;" ;
		ResultSet rs = stmt.executeQuery(query) ;
		System.out.println(rs.toString());
		System.out.println("Aw yeah");
	}
}
