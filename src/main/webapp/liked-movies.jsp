<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="DB.DBConnection" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CinemaWorld - 좋아요한 영화</title>
    <link rel="stylesheet" href="css/Style.css">
    <link rel="stylesheet" href="css/main.css">
    <link rel="stylesheet" href="css/profile.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <style>
        .page-title {
            margin: 20px 0;
            text-align: center;
            font-size: 24px;
            font-weight: bold;
            color: #333;
        }
        
        .liked-movies-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        
        .liked-movies-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }
        
        .movie-card {
            position: relative;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s, box-shadow 0.3s;
            height: 300px;
            background-color: #fff;
        }
        
        .movie-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
        }
        
        .movie-poster {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        
        .movie-info {
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            padding: 15px;
            background: linear-gradient(to top, rgba(0, 0, 0, 0.9), rgba(0, 0, 0, 0));
            color: white;
            transform: translateY(70%);
            transition: transform 0.3s;
        }
        
        .movie-card:hover .movie-info {
            transform: translateY(0);
        }
        
        .movie-title {
            font-size: 16px;
            font-weight: bold;
            margin: 0 0 5px 0;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        
        .movie-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 14px;
            margin-top: 8px;
        }
        
        .movie-rating {
            display: flex;
            align-items: center;
        }
        
        .movie-rating i {
            color: gold;
            margin-right: 5px;
        }
        
        .movie-date {
            font-size: 12px;
            color: #ddd;
        }
        
        .movie-actions {
            margin-top: 10px;
            display: flex;
            justify-content: space-between;
        }
        
        .view-btn, .unlike-btn {
            padding: 5px 10px;
            border-radius: 4px;
            font-size: 12px;
            cursor: pointer;
            transition: background-color 0.3s;
            border: none;
            display: inline-flex;
            align-items: center;
            gap: 5px;
        }
        
        .view-btn {
            background-color: #4285f4;
            color: white;
        }
        
        .view-btn:hover {
            background-color: #3367d6;
        }
        
        .unlike-btn {
            background-color: transparent;
            color: #ff5252;
            border: 1px solid #ff5252;
        }
        
        .unlike-btn:hover {
            background-color: #ffebee;
        }
        
        .no-movies {
            text-align: center;
            padding: 50px 0;
            grid-column: 1 / -1;
        }
        
        .no-movies i {
            font-size: 64px;
            color: #ddd;
            margin-bottom: 20px;
        }
        
        .no-movies h3 {
            font-size: 20px;
            color: #666;
            margin-bottom: 10px;
        }
        
        .no-movies p {
            color: #888;
            margin-bottom: 20px;
        }
        
        .back-to-movies {
            display: inline-block;
            background-color: #4285f4;
            color: white;
            padding: 10px 20px;
            border-radius: 4px;
            text-decoration: none;
            transition: background-color 0.3s;
        }
        
        .back-to-movies:hover {
            background-color: #3367d6;
        }
        
        .pagination {
            display: flex;
            justify-content: center;
            margin-top: 30px;
            gap: 5px;
        }
        
        .pagination a, .pagination span {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 36px;
            height: 36px;
            border-radius: 4px;
            text-decoration: none;
            color: #333;
            transition: all 0.3s;
        }
        
        .pagination a {
            background-color: #f0f0f0;
        }
        
        .pagination a:hover {
            background-color: #e0e0e0;
        }
        
        .pagination .active {
            background-color: #4285f4;
            color: white;
        }
        
        .pagination .disabled {
            color: #aaa;
            cursor: not-allowed;
        }
        
        @media (max-width: 768px) {
            .liked-movies-grid {
                grid-template-columns: repeat(2, 1fr);
            }
        }
        
        @media (max-width: 480px) {
            .liked-movies-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body class="main-page">
    <%
        // 세션에서 사용자 정보 가져오기
        String username = (String) session.getAttribute("username");
        Integer userId = (Integer) session.getAttribute("userId");
        
        // 로그인 상태가 아니면 로그인 페이지로 리다이렉트
        if (username == null || userId == null) {
            response.sendRedirect("member/login.jsp");
            return;
        }
        
        // 페이지네이션 설정
        int currentPage = 1;
        int moviesPerPage = 12;
        int totalMovies = 0;
        
        if (request.getParameter("page") != null) {
            try {
                currentPage = Integer.parseInt(request.getParameter("page"));
                if (currentPage < 1) currentPage = 1;
            } catch (NumberFormatException e) {
                currentPage = 1;
            }
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // 총 좋아요한 영화 수 조회
            String countSql = "SELECT COUNT(*) AS count FROM movie_likes WHERE user_id = ?";
            pstmt = conn.prepareStatement(countSql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                totalMovies = rs.getInt("count");
            }
            
            // 총 페이지 수 계산
            int totalPages = (int) Math.ceil((double) totalMovies / moviesPerPage);
            if (currentPage > totalPages && totalPages > 0) {
                currentPage = totalPages;
            }
            
            // 시작 인덱스 계산
            int startIndex = (currentPage - 1) * moviesPerPage;
    %>

    <div class="site-wrapper">
        <!-- 헤더 포함 -->
        <jsp:include page="header.jsp" />
        
        <main class="main-content">
            <div class="liked-movies-container">
                <h1 class="page-title">
                    <i class="fas fa-heart" style="color: #e50914;"></i> 좋아요한 영화
                </h1>
                
                <%
                    // 좋아요한 영화 목록 조회
                    String likedMoviesSql = 
                        "SELECT m.*, ml.created_at AS liked_at " +
                        "FROM movie_likes ml " +
                        "JOIN movie m ON ml.movie_id = m.movie_id " +
                        "WHERE ml.user_id = ? " +
                        "ORDER BY ml.created_at DESC " +
                        "LIMIT ?, ?";
                    
                    pstmt = conn.prepareStatement(likedMoviesSql);
                    pstmt.setInt(1, userId);
                    pstmt.setInt(2, startIndex);
                    pstmt.setInt(3, moviesPerPage);
                    rs = pstmt.executeQuery();
                    
                    boolean hasLikedMovies = false;
                %>
                
                <div class="liked-movies-grid">
                    <%
                        while (rs.next()) {
                            hasLikedMovies = true;
                            String movieId = rs.getString("movie_id");
                            String title = rs.getString("title");
                            String posterUrl = rs.getString("poster_url");
                            double rating = rs.getDouble("rating");
                            String releaseDate = rs.getString("release_date");
                            Timestamp likedAt = rs.getTimestamp("liked_at");
                            
                            // 포스터 URL이 없는 경우 기본 이미지 사용
                            if (posterUrl == null || posterUrl.isEmpty()) {
                                posterUrl = "image/default-movie.jpg";
                            }
                    %>
                    <div class="movie-card">
                        <img src="<%= posterUrl %>" alt="<%= title %> 포스터" class="movie-poster" onerror="this.src='image/default-movie.jpg';">
                        <div class="movie-info">
                            <h3 class="movie-title"><%= title %></h3>
                            <div class="movie-meta">
                                <div class="movie-rating">
                                    <i class="fas fa-star"></i>
                                    <span><%= String.format("%.1f", rating) %></span>
                                </div>
                                <% if (releaseDate != null && !releaseDate.isEmpty()) { %>
                                    <div class="movie-date"><%= releaseDate %></div>
                                <% } %>
                            </div>
                            <div class="movie-actions">
                                <a href="movie-detail.jsp?id=<%= movieId %>" class="view-btn">
                                    <i class="fas fa-eye"></i> 상세보기
                                </a>
                                <button class="unlike-btn" data-movie-id="<%= movieId %>">
                                    <i class="fas fa-heart-broken"></i> 취소
                                </button>
                            </div>
                        </div>
                    </div>
                    <%
                        }
                        
                        if (!hasLikedMovies) {
                    %>
                    <div class="no-movies">
                        <i class="fas fa-heart-broken"></i>
                        <h3>좋아요한 영화가 없습니다</h3>
                        <p>영화 상세 페이지에서 좋아요 버튼을 눌러 영화를 저장해보세요.</p>
                        <a href="movies.jsp" class="back-to-movies">영화 둘러보기</a>
                    </div>
                    <%
                        }
                    %>
                </div>
                
                <% if (totalPages > 1) { %>
                <div class="pagination">
                    <% if (currentPage > 1) { %>
                        <a href="?page=1"><i class="fas fa-angle-double-left"></i></a>
                        <a href="?page=<%= currentPage - 1 %>"><i class="fas fa-angle-left"></i></a>
                    <% } else { %>
                        <span class="disabled"><i class="fas fa-angle-double-left"></i></span>
                        <span class="disabled"><i class="fas fa-angle-left"></i></span>
                    <% } %>
                    
                    <% 
                        int startPage = Math.max(1, currentPage - 2);
                        int endPage = Math.min(totalPages, startPage + 4);
                        startPage = Math.max(1, endPage - 4);
                        
                        for (int i = startPage; i <= endPage; i++) { 
                    %>
                        <a href="?page=<%= i %>" class="<%= i == currentPage ? "active" : "" %>"><%= i %></a>
                    <% } %>
                    
                    <% if (currentPage < totalPages) { %>
                        <a href="?page=<%= currentPage + 1 %>"><i class="fas fa-angle-right"></i></a>
                        <a href="?page=<%= totalPages %>"><i class="fas fa-angle-double-right"></i></a>
                    <% } else { %>
                        <span class="disabled"><i class="fas fa-angle-right"></i></span>
                        <span class="disabled"><i class="fas fa-angle-double-right"></i></span>
                    <% } %>
                </div>
                <% } %>
            </div>
        </main>
        
        <!-- 푸터 포함 -->
        <jsp:include page="footer.jsp" />
    </div>
    
    <script>
    document.addEventListener('DOMContentLoaded', function() {
        // 좋아요 취소 버튼 이벤트
        const unlikeButtons = document.querySelectorAll('.unlike-btn');
        unlikeButtons.forEach(button => {
            button.addEventListener('click', function() {
                if (confirm('이 영화를 좋아요 목록에서 삭제하시겠습니까?')) {
                    const movieId = this.getAttribute('data-movie-id');
                    
                    // 서버에 요청 보내기
                    fetch('MovieLikeServlet?movieId=' + movieId)
                        .then(response => {
                            if (!response.ok) {
                                throw new Error('서버 응답 오류');
                            }
                            return response.json();
                        })
                        .then(data => {
                            if (data.success) {
                                // 성공적으로 처리되면 페이지 새로고침
                                window.location.reload();
                            } else {
                                alert('좋아요 취소 중 오류가 발생했습니다: ' + data.message);
                            }
                        })
                        .catch(error => {
                            console.error('Error:', error);
                            alert('좋아요 취소 중 오류가 발생했습니다. 다시 시도해주세요.');
                        });
                }
            });
        });
    });
    </script>
    
    <%
        } catch (SQLException e) {
            e.printStackTrace();
    %>
    <div class="error-container">
        <i class="fas fa-exclamation-circle"></i>
        <h2>데이터베이스 오류</h2>
        <p>영화 정보를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.</p>
    </div>
    <%
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
</body>
</html>
