<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="admin-check.jsp" %>
<%@ page import="java.sql.*" %>
<%@ page import="DB.DBConnection" %>
<%
    // 폼에서 전송된 데이터 가져오기
    String categoryName = request.getParameter("categoryName");
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    try {
        conn = DBConnection.getConnection();
        String sql = "INSERT INTO board_category (name) VALUES (?)";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, categoryName);
        
        int result = pstmt.executeUpdate();
        
        if(result > 0) {
            // 추가 성공
            response.sendRedirect("manage-boards.jsp?tab=categories&success=true");
        } else {
            // 추가 실패
            response.sendRedirect("manage-boards.jsp?tab=categories&error=add");
        }
    } catch(Exception e) {
        e.printStackTrace();
        response.sendRedirect("manage-boards.jsp?tab=categories&error=exception");
    } finally {
        if(pstmt != null) try { pstmt.close(); } catch(Exception e) {}
        if(conn != null) try { conn.close(); } catch(Exception e) {}
    }
%>
