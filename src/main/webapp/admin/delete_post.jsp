<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="admin-check.jsp" %>
<%@ page import="java.sql.*" %>
<%@ page import="DB.DBConnection" %>
<%
    // URL 파라미터에서 게시글 ID 가져오기
    int postId = Integer.parseInt(request.getParameter("id"));
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    try {
        conn = DBConnection.getConnection();
        
        // 게시글 댓글 삭제
        String deleteComments = "DELETE FROM board_comment WHERE board_id=?";
        pstmt = conn.prepareStatement(deleteComments);
        pstmt.setInt(1, postId);
        pstmt.executeUpdate();
        pstmt.close();
        
        // 게시글 삭제
        String deletePost = "DELETE FROM board WHERE id=?";
        pstmt = conn.prepareStatement(deletePost);
        pstmt.setInt(1, postId);
        
        int result = pstmt.executeUpdate();
        
        if(result > 0) {
            // 삭제 성공
            response.sendRedirect("manage-boards.jsp?tab=posts&success=true");
        } else {
            // 삭제 실패
            response.sendRedirect("manage-boards.jsp?tab=posts&error=delete");
        }
    } catch(Exception e) {
        e.printStackTrace();
        response.sendRedirect("manage-boards.jsp?tab=posts&error=exception");
    } finally {
        if(pstmt != null) try { pstmt.close(); } catch(Exception e) {}
        if(conn != null) try { conn.close(); } catch(Exception e) {}
    }
%>
