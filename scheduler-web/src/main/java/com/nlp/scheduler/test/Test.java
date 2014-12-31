package com.nlp.scheduler.test;

import java.sql.Connection;
import java.sql.DriverManager;

public class Test {

	public static void main(String[] args) {
		
		try {
			
			Class.forName("org.postgresql.Driver").newInstance();
			 String url = "jdbc:postgresql://dev.glaucusis.com:5432/newsfeedsite" ;
	         Connection con = DriverManager.getConnection(url, "newsfeedsite" , "Glaucusis12" );
			
	         System.out.println(con);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
