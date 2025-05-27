<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 현재 경로에 따라 CSS 경로 설정
    String footerCssPath = request.getRequestURI().contains("/admin/") ? "../css/main.css" : "css/main.css";
%>
<link rel="stylesheet" href="<%= footerCssPath %>">
<footer class="site-footer">
    <div class="footer-container">
        <div class="footer-content">
            <div class="footer-logo">
                <i class="fas fa-film"></i>
                <span>CinemaWorld</span>
            </div>
            <div class="footer-links">
                <a href="<%= request.getRequestURI().contains("/admin/") ? "../terms.jsp" : "terms.jsp" %>">서비스 이용약관</a>
                <a href="<%= request.getRequestURI().contains("/admin/") ? "../privacy.jsp" : "privacy.jsp" %>">개인정보 처리방침</a>
                <a href="<%= request.getRequestURI().contains("/admin/") ? "../contact.jsp" : "contact.jsp" %>">고객센터</a>
            </div>
            <div class="footer-copyright">
                &copy; <%= new java.text.SimpleDateFormat("yyyy").format(new java.util.Date()) %> CinemaWorld v10. All rights reserved.
            </div>
        </div>
    </div>
</footer>
