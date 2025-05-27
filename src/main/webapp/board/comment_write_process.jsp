<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="DB.DBConnection" %>
<%
    request.setCharacterEncoding("UTF-8");

    // 세션 확인
    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null) {
        response.sendRedirect(request.getContextPath() + "/member/login.jsp");
        return;
    }
    
    // 파라미터 받기
    String boardIdParam = request.getParameter("boardId");
    String content = request.getParameter("content");
    
    // 유효성 검사
    if (boardIdParam == null || boardIdParam.isEmpty() || content == null || content.trim().isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/board/view.jsp?id=" + boardIdParam);
        return;
    }
    
    int boardId = Integer.parseInt(boardIdParam);
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    try {
        conn = DBConnection.getConnection();
        
        // 디버깅을 위한 로그 출력
        System.out.println("댓글 등록 시도: 게시글ID=" + boardId + ", 사용자ID=" + userId + ", 내용=" + content);
        
        String sql = "INSERT INTO board_comment (board_id, user_id, content, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, boardId);
        pstmt.setInt(2, userId);
        pstmt.setString(3, content);
        
        int result = pstmt.executeUpdate();
        
        if (result > 0) {
            System.out.println("댓글 등록 성공");
        } else {
            System.out.println("댓글 등록 실패");
        }
        
        response.sendRedirect(request.getContextPath() + "/board/view.jsp?id=" + boardId);
        
    } catch (Exception e) {
        System.out.println("댓글 등록 중 예외 발생: " + e.getMessage());
        e.printStackTrace();
        response.sendRedirect(request.getContextPath() + "/board/view.jsp?id=" + boardId);
    } finally {
        try {
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
%>
