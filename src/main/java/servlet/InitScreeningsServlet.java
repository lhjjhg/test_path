package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DB.DBConnection;

@WebServlet("/admin/init-screenings")
public class InitScreeningsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>상영 정보 초기화</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; line-height: 1.6; padding: 20px; background-color: #f4f4f4; }");
        out.println(".container { max-width: 800px; margin: 0 auto; background: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        out.println("h1 { color: #333; }");
        out.println("pre { background: #f9f9f9; padding: 10px; border-radius: 5px; overflow-x: auto; white-space: pre-wrap; }");
        out.println(".log { background: #f0f0f0; padding: 15px; border-radius: 5px; max-height: 400px; overflow-y: auto; margin-top: 20px; }");
        out.println(".success { color: green; }");
        out.println(".error { color: red; }");
        out.println("a { display: inline-block; margin-top: 20px; background: #4CAF50; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>상영 정보 초기화</h1>");
        out.println("<p>상영 정보를 초기화하고 더미 데이터를 생성합니다.</p>");
        
        out.println("<div class='log'>");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작
            
            // 기존 상영 정보 삭제
            out.println("기존 상영 정보 삭제 중...<br>");
            String truncateSql = "TRUNCATE TABLE screening";
            pstmt = conn.prepareStatement(truncateSql);
            pstmt.executeUpdate();
            out.println("기존 상영 정보 삭제 완료<br>");
            
            // 영화 목록 가져오기
            out.println("영화 목록 가져오는 중...<br>");
            String movieSql = "SELECT movie_id, title FROM movie WHERE status = 'current'";
            pstmt = conn.prepareStatement(movieSql);
            rs = pstmt.executeQuery();
            
            // 영화 ID 배열 생성
            java.util.List<String> movieIds = new java.util.ArrayList<>();
            while (rs.next()) {
                movieIds.add(rs.getString("movie_id"));
                out.println("- " + rs.getString("title") + "<br>");
            }
            
            if (movieIds.isEmpty()) {
                out.println("<span class='error'>현재 상영 중인 영화가 없습니다. 먼저 영화를 등록해주세요.</span><br>");
                conn.rollback();
                return;
            }
            
            // 상영관 목록 가져오기
            out.println("상영관 목록 가져오는 중...<br>");
            String screenSql = "SELECT id, name, seats_total FROM screen";
            pstmt = conn.prepareStatement(screenSql);
            rs = pstmt.executeQuery();
            
            // 상영관 ID 배열 생성
            java.util.List<Integer> screenIds = new java.util.ArrayList<>();
            java.util.Map<Integer, Integer> screenSeats = new java.util.HashMap<>();
            while (rs.next()) {
                int screenId = rs.getInt("id");
                screenIds.add(screenId);
                screenSeats.put(screenId, rs.getInt("seats_total"));
                out.println("- " + rs.getString("name") + " (" + rs.getInt("seats_total") + "석)<br>");
            }
            
            if (screenIds.isEmpty()) {
                out.println("<span class='error'>등록된 상영관이 없습니다. 먼저 상영관을 등록해주세요.</span><br>");
                conn.rollback();
                return;
            }
            
            // 상영 시간 배열 생성
            String[] times = {"10:00", "12:30", "15:00", "17:30", "20:00", "22:30"};
            
            // 오늘부터 14일간의 상영 정보 생성
            out.println("상영 정보 생성 중...<br>");
            
            Random random = new Random();
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            String insertSql = "INSERT INTO screening (movie_id, screen_id, screening_date, screening_time, available_seats) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertSql);
            
            int count = 0;
            
            for (int day = 0; day < 14; day++) {
                Date date = cal.getTime();
                String formattedDate = dateFormat.format(date);
                
                for (int screenId : screenIds) {
                    // 각 상영관마다 2-4개의 영화 선택
                    int movieCount = random.nextInt(3) + 2; // 2-4개
                    java.util.List<String> selectedMovies = new java.util.ArrayList<>();
                    
                    for (int i = 0; i < movieCount; i++) {
                        String movieId = movieIds.get(random.nextInt(movieIds.size()));
                        if (!selectedMovies.contains(movieId)) {
                            selectedMovies.add(movieId);
                        }
                    }
                    
                    for (String movieId : selectedMovies) {
                        // 각 영화마다 2-4개의 상영 시간 선택
                        int timeCount = random.nextInt(3) + 2; // 2-4개
                        java.util.List<String> selectedTimes = new java.util.ArrayList<>();
                        
                        for (int i = 0; i < timeCount; i++) {
                            String time = times[random.nextInt(times.length)];
                            if (!selectedTimes.contains(time)) {
                                selectedTimes.add(time);
                            }
                        }
                        
                        // 선택된 시간 정렬
                        java.util.Collections.sort(selectedTimes);
                        
                        for (String time : selectedTimes) {
                            int seatsTotal = screenSeats.get(screenId);
                            int availableSeats = seatsTotal - random.nextInt(seatsTotal / 4); // 25% 이내 랜덤 예약
                            
                            pstmt.setString(1, movieId);
                            pstmt.setInt(2, screenId);
                            pstmt.setString(3, formattedDate);
                            pstmt.setString(4, time);
                            pstmt.setInt(5, availableSeats);
                            pstmt.addBatch();
                            
                            count++;
                        }
                    }
                }
                
                cal.add(Calendar.DATE, 1);
            }
            
            int[] results = pstmt.executeBatch();
            conn.commit();
            
            out.println("상영 정보 생성 완료. 총 " + count + "개의 상영 정보가 생성되었습니다.<br>");
            out.println("<span class='success'>모든 작업이 성공적으로 완료되었습니다.</span>");
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            
            out.println("<span class='error'>오류 발생: " + e.getMessage() + "</span><br>");
            e.printStackTrace(new PrintWriter(out));
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        out.println("</div>");
        
        out.println("<a href='../index.jsp'>메인 페이지로 이동</a>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}
