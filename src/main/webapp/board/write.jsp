<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%@ page import="DB.DBConnection" %>
<%@ page import="dto.BoardCategory" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>게시글 작성 - CinemaWorld</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css">
</head>
<body>
    <jsp:include page="../header.jsp" />
    
    <div class="container">
        <h1>게시글 작성</h1>
        
        <%
            // 로그인 체크
            if (session.getAttribute("userId") == null) {
                response.sendRedirect(request.getContextPath() + "/member/login.jsp");
                return;
            }
            
            // 카테고리 파라미터
            int categoryId = 0;
            String categoryParam = request.getParameter("category");
            if (categoryParam != null && !categoryParam.isEmpty()) {
                try {
                    categoryId = Integer.parseInt(categoryParam);
                } catch (NumberFormatException e) {
                    // 무시
                }
            }
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            
            try {
                conn = DBConnection.getConnection();
                
                // 카테고리 목록 가져오기
                String categorySql = "SELECT * FROM board_category ORDER BY id";
                pstmt = conn.prepareStatement(categorySql);
                rs = pstmt.executeQuery();
                
                List<BoardCategory> categories = new ArrayList<>();
                while (rs.next()) {
                    BoardCategory category = new BoardCategory();
                    category.setId(rs.getInt("id"));
                    category.setName(rs.getString("name"));
                    category.setDescription(rs.getString("description"));
                    categories.add(category);
                    
                    if (categoryId == 0 && category.getId() == 1) {
                        categoryId = category.getId(); // 기본 카테고리 설정
                    }
                }
                
                // 관리자 여부 확인 (userRole이 'ADMIN'인지 확인)
                String userRole = (String) session.getAttribute("userRole");
                boolean isAdmin = "ADMIN".equals(userRole);
        %>
        
        <div class="board-form">
            <form action="write_process.jsp" method="post">
                <div class="form-group">
                    <label for="category">카테고리</label>
                    <select name="category" id="category" required>
                        <% for (BoardCategory category : categories) { %>
                            <option value="<%= category.getId() %>" <%= categoryId == category.getId() ? "selected" : "" %>>
                                <%= category.getName() %>
                            </option>
                        <% } %>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="title">제목</label>
                    <input type="text" name="title" id="title" required maxlength="100" placeholder="제목을 입력하세요">
                </div>
                
                <div class="form-group">
                    <label for="content">내용</label>
                    <textarea name="content" id="content" required placeholder="내용을 입력하세요"></textarea>
                </div>
                
                <% if (isAdmin) { %>
                    <div class="checkbox-group">
                        <input type="checkbox" name="notice" id="notice" value="1">
                        <label for="notice">공지사항으로 등록</label>
                    </div>
                <% } %>
                
                <div class="form-actions">
                    <a href="list.jsp<%= categoryId > 0 ? "?category=" + categoryId : "" %>" class="btn-cancel">취소</a>
                    <button type="submit" class="btn-submit">등록</button>
                </div>
            </form>
        </div>
        
        <%
            } catch (Exception e) {
                e.printStackTrace();
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
    </div>
    
    <jsp:include page="../footer.jsp" />
</body>
</html>
