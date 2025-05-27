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

@WebServlet("/ReviewServlet")
public class ReviewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        // 로그인 상태가 아니면 로그인 페이지로 리다이렉트
        if (userId == null) {
            response.sendRedirect("member/login.jsp");
            return;
        }
        
        String action = request.getParameter("action");
        String movieId = request.getParameter("movieId");
        
        // 리뷰 추가
        if ("add".equals(action)) {
            String content = request.getParameter("content");
            int rating = Integer.parseInt(request.getParameter("rating"));
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            
            try {
                conn = DBConnection.getConnection();
                
                // 이미 리뷰를 작성했는지 확인
                String checkSql = "SELECT COUNT(*) FROM review WHERE user_id = ? AND movie_id = ?";
                pstmt = conn.prepareStatement(checkSql);
                pstmt.setInt(1, userId);
                pstmt.setString(2, movieId);
                rs = pstmt.executeQuery();
                
                boolean alreadyReviewed = false;
                if (rs.next()) {
                    alreadyReviewed = rs.getInt(1) > 0;
                }
                
                if (alreadyReviewed) {
                    // 이미 리뷰가 있으면 새 리뷰 추가 대신 오류 메시지 표시
                    request.getSession().setAttribute("errorMessage", "이미 이 영화에 리뷰를 작성하셨습니다. 기존 리뷰를 수정해주세요.");
                    response.sendRedirect("movie-detail.jsp?id=" + movieId);
                    return;
                } else {
                    // 새 리뷰 추가
                    String insertSql = "INSERT INTO review (movie_id, user_id, rating, content) VALUES (?, ?, ?, ?)";
                    pstmt = conn.prepareStatement(insertSql);
                    pstmt.setString(1, movieId);
                    pstmt.setInt(2, userId);
                    pstmt.setInt(3, rating); // 사용자가 선택한 별점 그대로 저장
                    pstmt.setString(4, content);
                    pstmt.executeUpdate();
                    
                    // 성공 메시지 설정
                    request.getSession().setAttribute("successMessage", "리뷰가 성공적으로 등록되었습니다.");
                }
                
                // 영화 평점 업데이트
                updateMovieRating(conn, movieId);
                
                // 리다이렉트
                response.sendRedirect("movie-detail.jsp?id=" + movieId);
                
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendRedirect("error.jsp?message=" + e.getMessage());
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
        // 리뷰 수정
        else if ("update".equals(action)) {
            String reviewId = request.getParameter("id");
            String content = request.getParameter("content");
            int rating = Integer.parseInt(request.getParameter("rating"));
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            
            try {
                conn = DBConnection.getConnection();
                
                // 리뷰 수정 (자신의 리뷰만 수정 가능하도록 user_id 조건 추가)
                String updateSql = "UPDATE review SET rating = ?, content = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?";
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, rating);
                pstmt.setString(2, content);
                pstmt.setString(3, reviewId);
                pstmt.setInt(4, userId);
                int result = pstmt.executeUpdate();
                
                if (result > 0) {
                    // 영화 평점 업데이트
                    updateMovieRating(conn, movieId);
                    
                    // 성공 메시지 설정
                    request.getSession().setAttribute("successMessage", "리뷰가 성공적으로 수정되었습니다.");
                } else {
                    // 실패 메시지 설정
                    request.getSession().setAttribute("errorMessage", "리뷰 수정에 실패했습니다. 권한이 없거나 리뷰가 존재하지 않습니다.");
                }
                
                // 리다이렉트
                response.sendRedirect("movie-detail.jsp?id=" + movieId);
                
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendRedirect("error.jsp?message=" + e.getMessage());
            } finally {
                try {
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        // 리뷰 삭제
        else if ("delete".equals(action)) {
            String reviewId = request.getParameter("id");
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            
            try {
                conn = DBConnection.getConnection();
                
                // 먼저 영화 ID 가져오기 (리다이렉트용)
                String selectSql = "SELECT movie_id FROM review WHERE id = ?";
                pstmt = conn.prepareStatement(selectSql);
                pstmt.setString(1, reviewId);
                rs = pstmt.executeQuery();
                
                String movieIdFromReview = null;
                if (rs.next()) {
                    movieIdFromReview = rs.getString("movie_id");
                }
                
                if (movieIdFromReview == null) {
                    // 리뷰를 찾을 수 없음
                    request.getSession().setAttribute("errorMessage", "리뷰를 찾을 수 없습니다.");
                    response.sendRedirect("movie-detail.jsp?id=" + movieId);
                    return;
                }
                
                // 리뷰 좋아요 먼저 삭제 (외래 키 제약조건 때문)
                String deleteLikesSql = "DELETE FROM review_likes WHERE review_id = ?";
                pstmt = conn.prepareStatement(deleteLikesSql);
                pstmt.setString(1, reviewId);
                pstmt.executeUpdate();
                
                // 리뷰 삭제 (자신의 리뷰만 삭제 가능하도록 user_id 조건 추가)
                String deleteSql = "DELETE FROM review WHERE id = ? AND user_id = ?";
                pstmt = conn.prepareStatement(deleteSql);
                pstmt.setString(1, reviewId);
                pstmt.setInt(2, userId);
                int result = pstmt.executeUpdate();
                
                if (result > 0) {
                    // 영화 평점 업데이트
                    updateMovieRating(conn, movieIdFromReview);
                    
                    // 성공 메시지 설정
                    request.getSession().setAttribute("successMessage", "리뷰가 성공적으로 삭제되었습니다.");
                } else {
                    // 실패 메시지 설정
                    request.getSession().setAttribute("errorMessage", "리뷰 삭제에 실패했습니다. 권한이 없거나 리뷰가 존재하지 않습니다.");
                }
                
                // 리다이렉트
                response.sendRedirect("movie-detail.jsp?id=" + (movieId != null ? movieId : movieIdFromReview));
                
            } catch (SQLException e) {
                e.printStackTrace();
                request.getSession().setAttribute("errorMessage", "리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
                response.sendRedirect("movie-detail.jsp?id=" + movieId);
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
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        // 리뷰 좋아요
        if ("like".equals(action)) {
            String reviewId = request.getParameter("id");
            
            // 로그인 상태가 아니면 오류 응답
            if (userId == null) {
                sendErrorResponse(response, "로그인이 필요합니다.");
                return;
            }
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            
            try {
                conn = DBConnection.getConnection();
                
                // 이미 좋아요를 눌렀는지 확인
                String checkSql = "SELECT * FROM review_likes WHERE user_id = ? AND review_id = ?";
                pstmt = conn.prepareStatement(checkSql);
                pstmt.setInt(1, userId);
                pstmt.setString(2, reviewId);
                rs = pstmt.executeQuery();
                
                boolean alreadyLiked = rs.next();
                
                if (alreadyLiked) {
                    // 이미 좋아요를 눌렀으면 삭제 (좋아요 취소)
                    String deleteSql = "DELETE FROM review_likes WHERE user_id = ? AND review_id = ?";
                    pstmt = conn.prepareStatement(deleteSql);
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, reviewId);
                    pstmt.executeUpdate();
                    
                    // 리뷰 좋아요 수 감소
                    String updateSql = "UPDATE review SET likes = likes - 1 WHERE id = ?";
                    pstmt = conn.prepareStatement(updateSql);
                    pstmt.setString(1, reviewId);
                    pstmt.executeUpdate();
                } else {
                    // 좋아요를 누르지 않았으면 추가
                    String insertSql = "INSERT INTO review_likes (user_id, review_id) VALUES (?, ?)";
                    pstmt = conn.prepareStatement(insertSql);
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, reviewId);
                    pstmt.executeUpdate();
                    
                    // 리뷰 좋아요 수 증가
                    String updateSql = "UPDATE review SET likes = likes + 1 WHERE id = ?";
                    pstmt = conn.prepareStatement(updateSql);
                    pstmt.setString(1, reviewId);
                    pstmt.executeUpdate();
                }
                
                // 업데이트된 좋아요 수 조회
                String selectSql = "SELECT likes FROM review WHERE id = ?";
                pstmt = conn.prepareStatement(selectSql);
                pstmt.setString(1, reviewId);
                rs = pstmt.executeQuery();
                
                int likes = 0;
                if (rs.next()) {
                    likes = rs.getInt("likes");
                }
                
                // JSON 응답 반환
                out.print("{\"success\": true, \"likes\": " + likes + ", \"liked\": " + !alreadyLiked + "}");
                
            } catch (SQLException e) {
                e.printStackTrace();
                out.print("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
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
        // 리뷰 정보 가져오기 (수정 폼용)
        else if ("getReview".equals(action)) {
            String reviewId = request.getParameter("id");
            
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            
            try {
                conn = DBConnection.getConnection();
                
                // 리뷰 정보 조회 (자신의 리뷰만 조회 가능하도록 user_id 조건 추가)
                String selectSql = "SELECT * FROM review WHERE id = ? AND user_id = ?";
                pstmt = conn.prepareStatement(selectSql);
                pstmt.setString(1, reviewId);
                pstmt.setInt(2, userId);
                rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    int rating = rs.getInt("rating");
                    String content = rs.getString("content");
                    
                    // JSON 응답 반환
                    out.print("{\"success\": true, \"rating\": " + rating + ", \"content\": \"" + content.replace("\"", "\\\"").replace("\n", "\\n") + "\"}");
                } else {
                    out.print("{\"success\": false, \"message\": \"리뷰를 찾을 수 없거나 권한이 없습니다.\"}");
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
                out.print("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
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
        // 리뷰 좋아요 상태 확인
        else if ("checkLike".equals(action)) {
            String reviewId = request.getParameter("id");
            
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            
            try {
                conn = DBConnection.getConnection();
                
                // 좋아요 상태 확인
                String checkSql = "SELECT * FROM review_likes WHERE user_id = ? AND review_id = ?";
                pstmt = conn.prepareStatement(checkSql);
                pstmt.setInt(1, userId);
                pstmt.setString(2, reviewId);
                rs = pstmt.executeQuery();
                
                boolean liked = rs.next();
                
                // JSON 응답 반환
                out.print("{\"success\": true, \"liked\": " + liked + "}");
                
            } catch (SQLException e) {
                e.printStackTrace();
                out.print("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
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
        // 리뷰 삭제 (GET 방식으로도 처리 가능하도록 추가)
        else if ("delete".equals(action)) {
            String reviewId = request.getParameter("id");
            String movieId = request.getParameter("movieId");
            
            // 로그인 상태가 아니면 로그인 페이지로 리다이렉트
            if (userId == null) {
                response.sendRedirect("member/login.jsp");
                return;
            }
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            
            try {
                conn = DBConnection.getConnection();
                
                // 먼저 영화 ID 가져오기 (리다이렉트용)
                String selectSql = "SELECT movie_id FROM review WHERE id = ?";
                pstmt = conn.prepareStatement(selectSql);
                pstmt.setString(1, reviewId);
                rs = pstmt.executeQuery();
                
                String movieIdFromReview = null;
                if (rs.next()) {
                    movieIdFromReview = rs.getString("movie_id");
                }
                
                if (movieIdFromReview == null) {
                    // 리뷰를 찾을 수 없음
                    request.getSession().setAttribute("errorMessage", "리뷰를 찾을 수 없습니다.");
                    response.sendRedirect("movie-detail.jsp?id=" + movieId);
                    return;
                }
                
                // 리뷰 좋아요 먼저 삭제 (외래 키 제약조건 때문)
                String deleteLikesSql = "DELETE FROM review_likes WHERE review_id = ?";
                pstmt = conn.prepareStatement(deleteLikesSql);
                pstmt.setString(1, reviewId);
                pstmt.executeUpdate();
                
                // 리뷰 삭제 (자신의 리뷰만 삭제 가능하도록 user_id 조건 추가)
                String deleteSql = "DELETE FROM review WHERE id = ? AND user_id = ?";
                pstmt = conn.prepareStatement(deleteSql);
                pstmt.setString(1, reviewId);
                pstmt.setInt(2, userId);
                int result = pstmt.executeUpdate();
                
                if (result > 0) {
                    // 영화 평점 업데이트
                    updateMovieRating(conn, movieIdFromReview);
                    
                    // 성공 메시지 설정
                    request.getSession().setAttribute("successMessage", "리뷰가 성공적으로 삭제되었습니다.");
                } else {
                    // 실패 메시지 설정
                    request.getSession().setAttribute("errorMessage", "리뷰 삭제에 실패했습니다. 권한이 없거나 리뷰가 존재하지 않습니다.");
                }
                
                // 리다이렉트
                response.sendRedirect("movie-detail.jsp?id=" + (movieId != null ? movieId : movieIdFromReview) + "&tab=reviews");
                
            } catch (SQLException e) {
                e.printStackTrace();
                request.getSession().setAttribute("errorMessage", "리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
                response.sendRedirect("movie-detail.jsp?id=" + movieId + "&tab=reviews");
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
    }
    
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print("{\"success\": false, \"message\": \"" + message + "\"}");
        out.flush();
    }
    
    // 영화 평점 업데이트 메소드
    private void updateMovieRating(Connection conn, String movieId) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            // 영화의 모든 리뷰 평균 평점 계산
            String avgSql = "SELECT AVG(rating) as avg_rating FROM review WHERE movie_id = ?";
            pstmt = conn.prepareStatement(avgSql);
            pstmt.setString(1, movieId);
            rs = pstmt.executeQuery();
            
            double avgRating = 0.0;
            if (rs.next()) {
                avgRating = rs.getDouble("avg_rating");
            }
            
            // 영화 테이블 평점 업데이트
            String updateSql = "UPDATE movie SET rating = ? WHERE movie_id = ?";
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setDouble(1, avgRating);
            pstmt.setString(2, movieId);
            pstmt.executeUpdate();
            
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }
}
