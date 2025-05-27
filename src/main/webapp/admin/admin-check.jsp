<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 관리자 권한 체크
    String userRole = (String) session.getAttribute("userRole");
    if (userRole == null || !userRole.equals("ADMIN")) {
        // 관리자가 아닌 경우 메인 페이지로 리다이렉트
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }
%>
