<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="admin-check.jsp" %>
<%@ page import="java.sql.*" %>
<%@ page import="DB.DBConnection" %>
<%
    // URL 파라미터에서 사용자 ID 가져오기
    int userId = Integer.parseInt(request.getParameter("id"));
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    try {
        conn = DBConnection.getConnection();
        
        // 사용자 삭제 전에 관련 데이터 삭제 (외래 키 제약조건 때문)
        // 리뷰 삭제
        String deleteReviews = "DELETE FROM review WHERE user_id=?";
        pstmt = conn.prepareStatement(deleteReviews);
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
        pstmt.close();
        
        // 영화 좋아요 삭제
        String deleteMovieLikes = "DELETE FROM movie_likes WHERE user_id=?";
        pstmt = conn.prepareStatement(deleteMovieLikes);
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
        pstmt.close();
        
        // 리뷰 좋아요 삭제
        String deleteReviewLikes = "DELETE FROM review_likes WHERE user_id=?";
        pstmt = conn.prepareStatement(deleteReviewLikes);
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
        pstmt.close();
        
        // 게시글 댓글 삭제
        String deleteBoardComments = "DELETE FROM board_comment WHERE user_id=?";
        pstmt = conn.prepareStatement(deleteBoardComments);
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
        pstmt.close();
        
        // 게시글 삭제
        String deleteBoards = "DELETE FROM board WHERE user_id=?";
        pstmt = conn.prepareStatement(deleteBoards);
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
        pstmt.close();
        
        // 사용자 삭제
        String deleteUser = "DELETE FROM user WHERE id=?";
        pstmt = conn.prepareStatement(deleteUser);
        pstmt.setInt(1, userId);
        
        int result = pstmt.executeUpdate();
        
        if(result > 0) {
            // 삭제 성공
            response.sendRedirect("manage-users.jsp?success=true");
        } else {
            // 삭제 실패
            response.sendRedirect("manage-users.jsp?error=delete");
        }
    } catch(Exception e) {
        e.printStackTrace();
        response.sendRedirect("manage-users.jsp?error=exception");
    } finally {
        if(pstmt != null) try { pstmt.close(); } catch(Exception e) {}
        if(conn != null) try { conn.close(); } catch(Exception e) {}
    }
%>
