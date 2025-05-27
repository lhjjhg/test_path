<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="admin-check.jsp" %>
<%@ page import="java.sql.*" %>
<%@ page import="DB.DBConnection" %>
<%
    // 폼에서 전송된 데이터 가져오기
    int userId = Integer.parseInt(request.getParameter("userId"));
    String nickname = request.getParameter("nickname");
    String email = request.getParameter("email");
    String role = request.getParameter("role");
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    try {
        conn = DBConnection.getConnection();
        String sql = "UPDATE user SET nickname=?, email=?, role=? WHERE id=?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, nickname);
        pstmt.setString(2, email);
        pstmt.setString(3, role);
        pstmt.setInt(4, userId);
        
        int result = pstmt.executeUpdate();
        
        if(result > 0) {
            // 업데이트 성공
            response.sendRedirect("manage-users.jsp?success=true");
        } else {
            // 업데이트 실패
            response.sendRedirect("manage-users.jsp?error=update");
        }
    } catch(Exception e) {
        e.printStackTrace();
        response.sendRedirect("manage-users.jsp?error=exception");
    } finally {
        if(pstmt != null) try { pstmt.close(); } catch(Exception e) {}
        if(conn != null) try { conn.close(); } catch(Exception e) {}
    }
%>
