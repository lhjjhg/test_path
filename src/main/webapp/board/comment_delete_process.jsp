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
    String commentIdParam = request.getParameter("id");
    String boardIdParam = request.getParameter("boardId");
    
    // 유효성 검사
    if (commentIdParam == null || commentIdParam.isEmpty() || boardIdParam == null || boardIdParam.isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/board/list.jsp");
        return;
    }
    
    int commentId, boardId;
    try {
        commentId = Integer.parseInt(commentIdParam);
        boardId = Integer.parseInt(boardIdParam);
    } catch (NumberFormatException e) {
        response.sendRedirect(request.getContextPath() + "/board/list.jsp");
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
        
        if (rs.next()) {
            int commentUserId = rs.getInt("user_id");
            
            // 작성자만 삭제 가능
            if (userId != commentUserId) {
                response.sendRedirect(request.getContextPath() + "/board/view.jsp?id=" + boardId);
                return;
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/board/view.jsp?id=" + boardId);
            return;
        }
        
        rs.close();
        pstmt.close();
        
        // 댓글 삭제
        String deleteSql = "DELETE FROM board_comment WHERE id = ?";
        pstmt = conn.prepareStatement(deleteSql);
        pstmt.setInt(1, commentId);
        
        int result = pstmt.executeUpdate();
        
        response.sendRedirect(request.getContextPath() + "/board/view.jsp?id=" + boardId);
        
    } catch (Exception e) {
        e.printStackTrace();
        response.sendRedirect(request.getContextPath() + "/board/view.jsp?id=" + boardId);
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
