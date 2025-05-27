<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="DB.DBConnection" %>
<%
    // 세션 확인
    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null) {
        response.sendRedirect(request.getContextPath() + "/member/login.jsp");
        return;
    }
    
    // 파라미터 받기
    String idParam = request.getParameter("id");
    
    // 유효성 검사
    if (idParam == null || idParam.isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/board/list.jsp");
        return;
    }
    
    int boardId;
    try {
        boardId = Integer.parseInt(idParam);
    } catch (NumberFormatException e) {
        response.sendRedirect(request.getContextPath() + "/board/list.jsp");
        return;
    }
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        conn = DBConnection.getConnection();
        
        // 게시글 작성자 확인
        String checkSql = "SELECT user_id, category_id FROM board WHERE id = ?";
        pstmt = conn.prepareStatement(checkSql);
        pstmt.setInt(1, boardId);
        rs = pstmt.executeQuery();
        
        if (rs.next()) {
            int boardUserId = rs.getInt("user_id");
            int categoryId = rs.getInt("category_id");
            
            // 작성자만 삭제 가능
            if (userId != boardUserId) {
                response.sendRedirect(request.getContextPath() + "/board/view.jsp?id=" + boardId);
                return;
            }
            
            rs.close();
            pstmt.close();
            
            // 게시글 삭제
            String deleteSql = "DELETE FROM board WHERE id = ?";
            pstmt = conn.prepareStatement(deleteSql);
            pstmt.setInt(1, boardId);
            
            int result = pstmt.executeUpdate();
            
            response.sendRedirect(request.getContextPath() + "/board/list.jsp?category=" + categoryId);
        } else {
            response.sendRedirect(request.getContextPath() + "/board/list.jsp");
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        response.sendRedirect(request.getContextPath() + "/board/list.jsp");
    } finally {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
%>
