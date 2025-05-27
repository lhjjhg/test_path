<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="DB.DBConnection" %>

<%
    // 로그인 확인
    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null) {
        response.sendRedirect(request.getContextPath() + "/member/login.jsp");
        return;
    }
    
    // POST 요청 확인
    if (!request.getMethod().equalsIgnoreCase("POST")) {
        response.sendRedirect("list.jsp");
        return;
    }
    
    // 요청 파라미터
    request.setCharacterEncoding("UTF-8");
    String commentIdParam = request.getParameter("commentId");
    String boardIdParam = request.getParameter("boardId");
    String content = request.getParameter("content");
    
    // 유효성 검사
    if (commentIdParam == null || commentIdParam.isEmpty() || 
        boardIdParam == null || boardIdParam.isEmpty() || 
        content == null || content.isEmpty()) {
        response.sendRedirect("list.jsp");
        return;
    }
    
    int commentId, boardId;
    try {
        commentId = Integer.parseInt(commentIdParam);
        boardId = Integer.parseInt(boardIdParam);
    } catch (NumberFormatException e) {
        response.sendRedirect("list.jsp");
        return;
    }
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        conn = DBConnection.getConnection();
        
        // 댓글 작성자 확인
        String checkSql = "SELECT user_id FROM board_comment WHERE id = ?";
        pstmt = conn.prepareStatement(checkSql);
        pstmt.setInt(1, commentId);
        rs = pstmt.executeQuery();
        
        if (!rs.next()) {
            response.sendRedirect("view.jsp?id=" + boardId);
            return;
        }
        
        int authorId = rs.getInt("user_id");
        
        // 작성자만 수정 가능
        if (userId != authorId) {
            response.sendRedirect("view.jsp?id=" + boardId);
            return;
        }
        
        rs.close();
        pstmt.close();
        
        // 댓글 수정
        String sql = "UPDATE board_comment SET content = ? WHERE id = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, content);
        pstmt.setInt(2, commentId);
        
        int result = pstmt.executeUpdate();
        
        response.sendRedirect("view.jsp?id=" + boardId);
        
    } catch (Exception e) {
        e.printStackTrace();
        response.sendRedirect("view.jsp?id=" + boardId);
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
