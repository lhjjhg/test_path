package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DB.DBConnection;

@WebServlet("/MovieLikeServlet")
public class MovieLikeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        // 로그인 상태가 아니면 오류 응답
        if (userId == null) {
            sendErrorResponse(response, "로그인이 필요합니다.");
            return;
        }
        
        String movieId = request.getParameter("movieId");
        if (movieId == null || movieId.isEmpty()) {
            sendErrorResponse(response, "영화 ID가 필요합니다.");
            return;
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // 이미 좋아요를 눌렀는지 확인
            String checkSql = "SELECT * FROM movie_likes WHERE user_id = ? AND movie_id = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, movieId);
            rs = pstmt.executeQuery();
            
            boolean alreadyLiked = rs.next();
            boolean liked = false;
            
            if (alreadyLiked) {
                // 이미 좋아요를 눌렀으면 삭제 (좋아요 취소)
                String deleteSql = "DELETE FROM movie_likes WHERE user_id = ? AND movie_id = ?";
                pstmt = conn.prepareStatement(deleteSql);
                pstmt.setInt(1, userId);
                pstmt.setString(2, movieId);
                pstmt.executeUpdate();
                liked = false;
                System.out.println("좋아요 취소: 사용자 ID=" + userId + ", 영화 ID=" + movieId);
            } else {
                // 좋아요를 누르지 않았으면 추가
                String insertSql = "INSERT INTO movie_likes (user_id, movie_id, created_at) VALUES (?, ?, NOW())";
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setInt(1, userId);
                pstmt.setString(2, movieId);
                pstmt.executeUpdate();
                liked = true;
                System.out.println("좋아요 추가: 사용자 ID=" + userId + ", 영화 ID=" + movieId);
            }
            
            // JSON 응답 반환
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{\"success\": true, \"liked\": " + liked + "}");
            out.flush();
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL 오류: " + e.getMessage());
            sendErrorResponse(response, "데이터베이스 오류: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print("{\"success\": false, \"message\": \"" + message + "\"}");
        out.flush();
    }
}
