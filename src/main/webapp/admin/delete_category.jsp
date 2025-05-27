<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="admin-check.jsp" %>
<%@ page import="java.sql.*" %>
<%@ page import="DB.DBConnection" %>
<%
    // URL 파라미터에서 카테고리 ID 가져오기
    int categoryId = Integer.parseInt(request.getParameter("id"));
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        conn = DBConnection.getConnection();
        
        // 카테고리에 게시글이 있는지 확인
        String checkSql = "SELECT COUNT(*) FROM board WHERE category_id=?";
        pstmt = conn.prepareStatement(checkSql);
        pstmt.setInt(1, categoryId);
        rs = pstmt.executeQuery();
        
        if(rs.next() && rs.getInt(1) > 0) {
            // 게시글이 있으면 삭제 불가
            response.sendRedirect("manage-boards.jsp?tab=categories&error=haspost");
            return;
        }
        
        // 카테고리 삭제
        String deleteSql = "DELETE FROM board_category WHERE id=?";
        pstmt = conn.prepareStatement(deleteSql);
        pstmt.setInt(1, categoryId);
        
        int result = pstmt.executeUpdate();
        
        if(result > 0) {
            // 삭제 성공
            response.sendRedirect("manage-boards.jsp?tab=categories&success=true");
        } else {
            // 삭제 실패
            response.sendRedirect("manage-boards.jsp?tab=categories&error=delete");
        }
    } catch(Exception e) {
        e.printStackTrace();
        response.sendRedirect("manage-boards.jsp?tab=categories&error=exception");
    } finally {
        if(rs != null) try { rs.close(); } catch(Exception e) {}
        if(pstmt != null) try { pstmt.close(); } catch(Exception e) {}
        if(conn != null) try { conn.close(); } catch(Exception e) {}
    }
%>
