package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Jsp_project";
    private static final String USER = "root";
    private static final String PASS = "123456"; 
    
    public static Connection getConnection() throws SQLException {
        try {
            System.out.println("JDBC 드라이버 로딩 시도: " + JDBC_DRIVER);
            Class.forName(JDBC_DRIVER);
            System.out.println("JDBC 드라이버 로딩 성공");
            System.out.println("데이터베이스 연결 시도: " + DB_URL);
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("데이터베이스 연결 성공");
            return conn;
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC 드라이버를 찾을 수 없습니다: " + e.getMessage());
            throw new SQLException("JDBC Driver not found", e);
        } catch (SQLException e) {
            System.out.println("데이터베이스 연결 실패: " + e.getMessage());
            throw e;
        }
    }
}
