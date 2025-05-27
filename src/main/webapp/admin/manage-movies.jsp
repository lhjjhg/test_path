<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="DB.DBConnection" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>영화 데이터 관리</title>
    <link rel="stylesheet" href="../Style.css">
    <link rel="stylesheet" href="../main.css">
    <style>
        .admin-container {
            max-width: 1200px;
            margin: 50px auto;
            padding: 30px;
            background-color: rgba(31, 31, 31, 0.9);
            border-radius: 10px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
        }
        
        .admin-title {
            text-align: center;
            margin-bottom: 30px;
        }
        
        .admin-title h1 {
            font-size: 28px;
            color: #fff;
        }
        
        .admin-title p {
            color: #bbb;
        }
        
        .admin-actions {
            display: flex;
            justify-content: center;
            gap: 20px;
            margin-top: 40px;
            flex-wrap: wrap;
        }
        
        .admin-btn {
            background-color: #e50914;
            color: white;
            padding: 12px 25px;
            border-radius: 5px;
            border: none;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            text-decoration: none;
            display: inline-block;
        }
        
        .admin-btn:hover {
            background-color: #f40612;
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
        }
        
        .admin-btn.secondary {
            background-color: #333;
        }
        
        .admin-btn.secondary:hover {
            background-color: #444;
        }
        
        .filter-bar {
            display: flex;
            gap: 15px;
            margin-bottom: 20px;
            flex-wrap: wrap;
        }
        
        .filter-item {
            flex: 1;
            min-width: 200px;
        }
        
        .filter-label {
            display: block;
            margin-bottom: 5px;
            color: #ddd;
        }
        
        .filter-select {
            width: 100%;
            padding: 10px;
            background-color: #333;
            color: #fff;
            border: none;
            border-radius: 5px;
        }
        
        .search-box {
            flex: 2;
            min-width: 300px;
        }
        
        .search-input {
            width: 100%;
            padding: 10px;
            background-color: #333;
            color: #fff;
            border: none;
            border-radius: 5px;
        }
        
        .movie-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            color: #ddd;
        }
        
        .movie-table th, .movie-table td {
            padding: 12px 15px;
            text-align: left;
            border-bottom: 1px solid #444;
        }
        
        .movie-table th {
            background-color: #222;
            color: #fff;
            font-weight: 600;
        }
        
        .movie-table tr:hover {
            background-color: rgba(255, 255, 255, 0.05);
        }
        
        .movie-poster {
            width: 60px;
            height: 90px;
            object-fit: cover;
            border-radius: 3px;
        }
        
        .movie-title {
            font-weight: 600;
            color: #fff;
        }
        
        .movie-status {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 3px;
            font-size: 12px;
            font-weight: 600;
        }
        
        .status-current {
            background-color: #4CAF50;
            color: white;
        }
        
        .status-coming {
            background-color: #2196F3;
            color: white;
        }
        
        .action-btn {
            padding: 5px 10px;
            border-radius: 3px;
            border: none;
            cursor: pointer;
            font-size: 12px;
            margin-right: 5px;
        }
        
        .edit-btn {
            background-color: #2196F3;
            color: white;
        }
        
        .delete-btn {
            background-color: #f44336;
            color: white;
        }
        
        .pagination {
            display: flex;
            justify-content: center;
            margin-top: 30px;
            gap: 10px;
        }
        
        .page-btn {
            padding: 8px 12px;
            background-color: #333;
            color: #fff;
            border: none;
            border-radius: 3px;
            cursor: pointer;
            transition: all 0.3s ease;
        }
        
        .page-btn:hover, .page-btn.active {
            background-color: #e50914;
        }
        
        .no-movies {
            text-align: center;
            padding: 50px 0;
            color: #999;
        }
    </style>
</head>
<body class="main-page">
    <div class="site-wrapper">
        <!-- 헤더 포함 -->
        <jsp:include page="../header.jsp" />
        
        <main class="main-content">
            <div class="admin-container">
                <div class="admin-title">
                    <h1>영화 데이터 관리</h1>
                    <p>데이터베이스에 저장된 영화 정보를 관리합니다</p>
                </div>
                
                <form method="get" action="manage-movies.jsp">
                    <div class="filter-bar">
                        <div class="filter-item">
                            <label class="filter-label">상태</label>
                            <select name="status" class="filter-select">
                                <option value="all" <%= request.getParameter("status") == null || "all".equals(request.getParameter("status")) ? "selected" : "" %>>전체</option>
                                <option value="current" <%= "current".equals(request.getParameter("status")) ? "selected" : "" %>>현재 상영작</option>
                                <option value="coming" <%= "coming".equals(request.getParameter("status")) ? "selected" : "" %>>상영 예정작</option>
                            </select>
                        </div>
                        <div class="filter-item">
                            <label class="filter-label">정렬</label>
                            <select name="sort" class="filter-select">
                                <option value="rank" <%= request.getParameter("sort") == null || "rank".equals(request.getParameter("sort")) ? "selected" : "" %>>순위순</option>
                                <option value="title" <%= "title".equals(request.getParameter("sort")) ? "selected" : "" %>>제목순</option>
                                <option value="rating" <%= "rating".equals(request.getParameter("sort")) ? "selected" : "" %>>평점순</option>
                                <option value="date" <%= "date".equals(request.getParameter("sort")) ? "selected" : "" %>>등록일순</option>
                            </select>
                        </div>
                        <div class="search-box">
                            <label class="filter-label">검색</label>
                            <input type="text" name="search" class="search-input" placeholder="영화 제목 검색..." value="<%= request.getParameter("search") != null ? request.getParameter("search") : "" %>">
                        </div>
                    </div>
                    <div class="admin-actions">
                        <button type="submit" class="admin-btn">필터 적용</button>
                        <a href="manage-movies.jsp" class="admin-btn secondary">필터 초기화</a>
                    </div>
                </form>
                
                <%
                    Connection conn = null;
                    PreparedStatement pstmt = null;
                    ResultSet rs = null;
                    
                    try {
                        conn = DBConnection.getConnection();
                        
                        // 페이지네이션 설정
                        int currentPage = 1;
                        if (request.getParameter("page") != null) {
                            currentPage = Integer.parseInt(request.getParameter("page"));
                        }
                        int recordsPerPage = 10;
                        int start = (currentPage - 1) * recordsPerPage;
                        
                        // 필터 설정
                        String status = request.getParameter("status");
                        String sort = request.getParameter("sort");
                        String search = request.getParameter("search");
                        
                        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM movie WHERE 1=1");
                        
                        if (status != null && !status.equals("all")) {
                            sqlBuilder.append(" AND status = ?");
                        }
                        
                        if (search != null && !search.trim().isEmpty()) {
                            sqlBuilder.append(" AND title LIKE ?");
                        }
                        
                        if (sort != null) {
                            switch (sort) {
                                case "title":
                                    sqlBuilder.append(" ORDER BY title ASC");
                                    break;
                                case "rating":
                                    sqlBuilder.append(" ORDER BY rating DESC");
                                    break;
                                case "date":
                                    sqlBuilder.append(" ORDER BY created_at DESC");
                                    break;
                                default:
                                    sqlBuilder.append(" ORDER BY movie_rank, rating DESC");
                                    break;
                            }
                        } else {
                            sqlBuilder.append(" ORDER BY movie_rank, rating DESC");
                        }
                        
                        sqlBuilder.append(" LIMIT ?, ?");
                        
                        pstmt = conn.prepareStatement(sqlBuilder.toString());
                        
                        int paramIndex = 1;
                        
                        if (status != null && !status.equals("all")) {
                            pstmt.setString(paramIndex++, status);
                        }
                        
                        if (search != null && !search.trim().isEmpty()) {
                            pstmt.setString(paramIndex++, "%" + search.trim() + "%");
                        }
                        
                        pstmt.setInt(paramIndex++, start);
                        pstmt.setInt(paramIndex, recordsPerPage);
                        
                        rs = pstmt.executeQuery();
                        
                        // 전체 레코드 수 계산
                        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM movie WHERE 1=1");
                        
                        if (status != null && !status.equals("all")) {
                            countSqlBuilder.append(" AND status = ?");
                        }
                        
                        if (search != null && !search.trim().isEmpty()) {
                            countSqlBuilder.append(" AND title LIKE ?");
                        }
                        
                        PreparedStatement countPstmt = conn.prepareStatement(countSqlBuilder.toString());
                        
                        paramIndex = 1;
                        
                        if (status != null && !status.equals("all")) {
                            countPstmt.setString(paramIndex++, status);
                        }
                        
                        if (search != null && !search.trim().isEmpty()) {
                            countPstmt.setString(paramIndex, "%" + search.trim() + "%");
                        }
                        
                        ResultSet countRs = countPstmt.executeQuery();
                        int totalRecords = 0;
                        
                        if (countRs.next()) {
                            totalRecords = countRs.getInt(1);
                        }
                        
                        int totalPages = (int) Math.ceil(totalRecords * 1.0 / recordsPerPage);
                        
                        countRs.close();
                        countPstmt.close();
                %>
                
                <table class="movie-table">
                    <thead>
                        <tr>
                            <th>포스터</th>
                            <th>제목</th>
                            <th>순위</th>
                            <th>평점</th>
                            <th>개봉일</th>
                            <th>상태</th>
                            <th>액션</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            boolean hasMovies = false;
                            
                            while (rs.next()) {
                                hasMovies = true;
                                String movieId = rs.getString("movie_id");
                                String title = rs.getString("title");
                                String posterUrl = rs.getString("poster_url");
                                int movieRank = rs.getInt("movie_rank");
                                double rating = rs.getDouble("rating");
                                String releaseDate = rs.getString("release_date");
                                String movieStatus = rs.getString("status");
                                
                                if (posterUrl == null || posterUrl.isEmpty()) {
                                    posterUrl = "../image/default-movie.jpg";
                                }
                        %>
                        <tr>
                            <td><img src="<%= posterUrl %>" alt="<%= title %>" class="movie-poster" onerror="this.src='../image/default-movie.jpg';"></td>
                            <td class="movie-title"><%= title %></td>
                            <td><%= movieRank > 0 ? movieRank : "-" %></td>
                            <td><%= String.format("%.1f", rating) %></td>
                            <td><%= releaseDate != null && !releaseDate.isEmpty() ? releaseDate : "-" %></td>
                            <td>
                                <span class="movie-status <%= "current".equals(movieStatus) ? "status-current" : "status-coming" %>">
                                    <%= "current".equals(movieStatus) ? "상영 중" : "상영 예정" %>
                                </span>
                            </td>
                            <td>
                                <a href="../movie-detail.jsp?id=<%= movieId %>" class="action-btn edit-btn">보기</a>
                                <button class="action-btn delete-btn" onclick="deleteMovie('<%= movieId %>', '<%= title %>')">삭제</button>
                            </td>
                        </tr>
                        <%
                            }
                            
                            if (!hasMovies) {
                        %>
                        <tr>
                            <td colspan="7" class="no-movies">
                                <p>영화 정보가 없습니다.</p>
                            </td>
                        </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
                
                <% if (totalPages > 1) { %>
                <div class="pagination">
                    <% if (currentPage > 1) { %>
                    <a href="?page=<%= currentPage - 1 %><%= status != null ? "&status=" + status : "" %><%= sort != null ? "&sort=" + sort : "" %><%= search != null && !search.trim().isEmpty() ? "&search=" + search : "" %>" class="page-btn">&laquo;</a>
                    <% } %>
                    
                    <% 
                        int startPage = Math.max(1, currentPage - 2);
                        int endPage = Math.min(totalPages, currentPage + 2);
                        
                        for (int i = startPage; i <= endPage; i++) { 
                    %>
                    <a href="?page=<%= i %><%= status != null ? "&status=" + status : "" %><%= sort != null ? "&sort=" + sort : "" %><%= search != null && !search.trim().isEmpty() ? "&search=" + search : "" %>" class="page-btn <%= i == currentPage ? "active" : "" %>"><%= i %></a>
                    <% } %>
                    
                    <% if (currentPage < totalPages) { %>
                    <a href="?page=<%= currentPage + 1 %><%= status != null ? "&status=" + status : "" %><%= sort != null ? "&sort=" + sort : "" %><%= search != null && !search.trim().isEmpty() ? "&search=" + search : "" %>" class="page-btn">&raquo;</a>
                    <% } %>
                </div>
                <% } %>
                
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
                
                <div class="admin-actions">
                    <a href="crawl-movies.jsp" class="admin-btn">크롤링 페이지로 이동</a>
                    <a href="reset-movies.jsp" class="admin-btn secondary">영화 데이터 초기화</a>
                    <a href="../index.jsp" class="admin-btn secondary">메인 페이지로 이동</a>
                </div>
            </div>
        </main>
        
        <!-- 푸터 포함 -->
        <jsp:include page="../footer.jsp" />
    </div>
    
    <script>
    function deleteMovie(movieId, title) {
        if (confirm('정말로 "' + title + '" 영화를 삭제하시겠습니까?')) {
            // 삭제 요청 처리
            window.location.href = 'delete-movie.jsp?id=' + movieId;
        }
    }
    </script>
</body>
</html>
