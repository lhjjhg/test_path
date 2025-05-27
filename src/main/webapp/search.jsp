<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%@ page import="DB.DBConnection" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CinemaWorld - 영화 검색</title>
    <link rel="stylesheet" href="css/Style.css">
    <link rel="stylesheet" href="css/main.css">
    <script src="https://kit.fontawesome.com/a076d05399.js" crossorigin="anonymous"></script>
    <style>
        .search-header {
            background-color: rgba(0, 0, 0, 0.5);
            padding: 30px 0;
            text-align: center;
            margin-bottom: 30px;
        }
        
        .search-header h1 {
            font-size: 28px;
            margin-bottom: 10px;
        }
        
        .search-results {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
        }
        
        .search-info {
            margin-bottom: 20px;
            color: #bbb;
        }
        
        .search-info strong {
            color: #fff;
        }
        
        .no-results {
            text-align: center;
            padding: 50px 0;
            color: #999;
        }
        
        .no-results i {
            font-size: 48px;
            margin-bottom: 20px;
            color: #666;
        }
        
        .search-suggestions {
            background-color: rgba(255, 255, 255, 0.05);
            padding: 20px;
            border-radius: 10px;
            margin-top: 30px;
        }
        
        .search-suggestions h3 {
            margin-top: 0;
            margin-bottom: 15px;
            color: #ddd;
        }
        
        .suggestion-list {
            list-style: none;
            padding: 0;
            margin: 0;
        }
        
        .suggestion-list li {
            margin-bottom: 10px;
        }
        
        .suggestion-list a {
            color: #e50914;
            text-decoration: none;
        }
        
        .suggestion-list a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body class="main-page">
    <div class="site-wrapper">
        <!-- 헤더 포함 -->
        <jsp:include page="header.jsp" />
        
        <main class="main-content">
            <div class="search-header">
                <h1>검색 결과</h1>
            </div>
            
            <div class="search-results">
                <%
                    String query = request.getParameter("query");
                    
                    if (query != null && !query.trim().isEmpty()) {
                        Connection conn = null;
                        PreparedStatement pstmt = null;
                        ResultSet rs = null;
                        
                        try {
                            conn = DBConnection.getConnection();
                            
                            // 검색 쿼리 실행
                            String sql = "SELECT m.*, md.genre, md.running_time FROM movie m " +
                                        "LEFT JOIN movie_detail md ON m.movie_id = md.movie_id " +
                                        "WHERE m.title LIKE ? " +
                                        "ORDER BY m.movie_rank, m.rating DESC";
                            pstmt = conn.prepareStatement(sql);
                            pstmt.setString(1, "%" + query.trim() + "%");
                            rs = pstmt.executeQuery();
                            
                            // 결과 카운트
                            int resultCount = 0;
                            List<Map<String, String>> results = new ArrayList<>();
                            
                            while (rs.next()) {
                                resultCount++;
                                Map<String, String> movie = new HashMap<>();
                                movie.put("movieId", rs.getString("movie_id"));
                                movie.put("title", rs.getString("title"));
                                movie.put("posterUrl", rs.getString("poster_url"));
                                movie.put("rating", String.format("%.1f", rs.getDouble("rating")));
                                movie.put("genre", rs.getString("genre"));
                                movie.put("runningTime", rs.getString("running_time"));
                                movie.put("status", rs.getString("status"));
                                results.add(movie);
                            }
                %>
                
                <div class="search-info">
                    <p>"<strong><%= query %></strong>" 검색 결과: <strong><%= resultCount %></strong>개의 영화를 찾았습니다.</p>
                </div>
                
                <% if (resultCount > 0) { %>
                <div class="movies-grid">
                    <% 
                        for (Map<String, String> movie : results) {
                            String movieId = movie.get("movieId");
                            String title = movie.get("title");
                            String posterUrl = movie.get("posterUrl");
                            String rating = movie.get("rating");
                            String genre = movie.get("genre");
                            String runningTime = movie.get("runningTime");
                            String status = movie.get("status");
                            
                            if (genre == null) genre = "정보 없음";
                            if (runningTime == null) runningTime = "정보 없음";
                            
                            if (posterUrl == null || posterUrl.isEmpty()) {
                                posterUrl = "image/default-movie.jpg";
                            }
                    %>
                    <div class="movie-card">
                        <div class="movie-poster">
                            <img src="<%= posterUrl %>" alt="<%= title %> 포스터" onerror="this.src='image/default-movie.jpg';">
                            <div class="movie-overlay">
                                <a href="movie-detail.jsp?id=<%= movieId %>" class="detail-btn">상세보기</a>
                                <% if ("current".equals(status)) { %>
                                <a href="booking.jsp?id=<%= movieId %>" class="booking-btn">예매하기</a>
                                <% } %>
                            </div>
                        </div>
                        <div class="movie-info">
                            <h3 class="movie-title"><%= title %></h3>
                            <div class="movie-meta">
                                <span class="genre"><%= genre %></span>
                                <span class="runtime"><%= runningTime %></span>
                            </div>
                            <div class="movie-rating">
                                <i class="fas fa-star"></i>
                                <span><%= rating %></span>
                            </div>
                        </div>
                    </div>
                    <% } %>
                </div>
                <% } else { %>
                <div class="no-results">
                    <i class="fas fa-search"></i>
                    <p>검색 결과가 없습니다.</p>
                    <p>다른 검색어를 입력해보세요.</p>
                    
                    <div class="search-suggestions">
                        <h3>추천 검색어</h3>
                        <ul class="suggestion-list">
                            <li><a href="search.jsp?query=액션">액션</a></li>
                            <li><a href="search.jsp?query=코미디">코미디</a></li>
                            <li><a href="search.jsp?query=로맨스">로맨스</a></li>
                            <li><a href="search.jsp?query=스릴러">스릴러</a></li>
                            <li><a href="search.jsp?query=애니메이션">애니메이션</a></li>
                        </ul>
                    </div>
                </div>
                <% } %>
                
                <%
                        } catch (SQLException e) {
                            e.printStackTrace();
                %>
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>검색 중 오류가 발생했습니다.</p>
                    <p>잠시 후 다시 시도해주세요.</p>
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
                    } else {
                %>
                <div class="search-info">
                    <p>검색어를 입력하지 않았습니다. 헤더의 검색창을 이용해 영화를 검색해보세요.</p>
                </div>
                
                <div class="search-suggestions">
                    <h3>인기 영화</h3>
                    <div class="movies-grid">
                        <%
                            Connection conn = null;
                            PreparedStatement pstmt = null;
                            ResultSet rs = null;
                            
                            try {
                                conn = DBConnection.getConnection();
                                String sql = "SELECT m.*, md.genre, md.running_time FROM movie m " +
                                            "LEFT JOIN movie_detail md ON m.movie_id = md.movie_id " +
                                            "WHERE m.status = 'current' " +
                                            "ORDER BY m.movie_rank, m.rating DESC LIMIT 4";
                                pstmt = conn.prepareStatement(sql);
                                rs = pstmt.executeQuery();
                                
                                while (rs.next()) {
                                    String movieId = rs.getString("movie_id");
                                    String title = rs.getString("title");
                                    String posterUrl = rs.getString("poster_url");
                                    double rating = rs.getDouble("rating");
                                    String genre = rs.getString("genre");
                                    String runningTime = rs.getString("running_time");
                                    
                                    if (genre == null) genre = "정보 없음";
                                    if (runningTime == null) runningTime = "정보 없음";
                                    
                                    if (posterUrl == null || posterUrl.isEmpty()) {
                                        posterUrl = "image/default-movie.jpg";
                                    }
                        %>
                        <div class="movie-card">
                            <div class="movie-poster">
                                <img src="<%= posterUrl %>" alt="<%= title %> 포스터" onerror="this.src='image/default-movie.jpg';">
                                <div class="movie-overlay">
                                    <a href="movie-detail.jsp?id=<%= movieId %>" class="detail-btn">상세보기</a>
                                    <a href="booking.jsp?id=<%= movieId %>" class="booking-btn">예매하기</a>
                                </div>
                            </div>
                            <div class="movie-info">
                                <h3 class="movie-title"><%= title %></h3>
                                <div class="movie-meta">
                                    <span class="genre"><%= genre %></span>
                                    <span class="runtime"><%= runningTime %></span>
                                </div>
                                <div class="movie-rating">
                                    <i class="fas fa-star"></i>
                                    <span><%= String.format("%.1f", rating) %></span>
                                </div>
                            </div>
                        </div>
                        <%
                                }
                            } catch (SQLException e) {
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
                </div>
                <% } %>
            </div>
        </main>
        
        <!-- 푸터 포함 -->
        <jsp:include page="footer.jsp" />
    </div>
    
    <script src="js/main.js"></script>
</body>
</html>
