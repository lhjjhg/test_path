<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="admin-check.jsp" %>
<%@ page import="java.sql.*" %>
<%@ page import="DB.DBConnection" %>
<%
    // URL 파라미터에서 댓글 ID 가져오기
    int commentId = Integer.parseInt(request.getParameter("id"));
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    try {
        conn = DBConnection.getConnection();
        String sql = "DELETE FROM board_comment WHERE id=?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, commentId);
        
        int result = pstmt.executeUpdate();
        
        if(result > 0) {
            // 삭제 성공
            response.sendRedirect("manage-boards.jsp?tab=comments&success=true");
        } else {
            // 삭제 실패
            response.sendRedirect("manage-boards.jsp?tab=comments&error=delete");
        }
    } catch(Exception e) {
        e.printStackTrace();
        response.sendRedirect("manage-boards.jsp?tab=comments&error=exception");
    } finally {
        if(pstmt != null) try { pstmt.close(); } catch(Exception e) {}
        if(conn != null) try { conn.close(); } catch(Exception e) {}
    }
%>
