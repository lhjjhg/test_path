<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="DB.DBConnection" %>
<%@ page import="dto.Board" %>
<%@ page import="dto.BoardComment" %>
<%@ page import="dto.BoardCategory" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>게시글 보기 - CinemaWorld</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css">
</head>
<body>
    <jsp:include page="../header.jsp" />
    
    <div class="container">
        <%
            // 게시글 ID 파라미터 처리
            int boardId = 0;
            String boardIdParam = request.getParameter("id");
            if (boardIdParam != null && !boardIdParam.isEmpty()) {
                try {
                    boardId = Integer.parseInt(boardIdParam);
                } catch (NumberFormatException e) {
                    // 무시
                }
            }
            
            if (boardId <= 0) {
        %>
            <div class="error-message">
                잘못된 접근입니다. <a href="list.jsp">게시판으로 돌아가기</a>
            </div>
        <%
                return;
            }
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            
            try {
                conn = DBConnection.getConnection();
                
                // 조회수 증가
                String updateViewsSql = "UPDATE board SET views = views + 1 WHERE id = ?";
                pstmt = conn.prepareStatement(updateViewsSql);
                pstmt.setInt(1, boardId);
                pstmt.executeUpdate();
                pstmt.close();
                
                // 게시글 정보 가져오기
                String sql = "SELECT b.*, u.username, u.nickname, c.name AS category_name " +
                             "FROM board b " +
                             "LEFT JOIN user u ON b.user_id = u.id " +
                             "LEFT JOIN board_category c ON b.category_id = c.id " +
                             "WHERE b.id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, boardId);
                rs = pstmt.executeQuery();
                
                if (!rs.next()) {
        %>
            <div class="error-message">
                존재하지 않는 게시글입니다. <a href="list.jsp">게시판으로 돌아가기</a>
            </div>
        <%
                    return;
                }
                
                Board board = new Board();
                board.setId(rs.getInt("id"));
                board.setCategoryId(rs.getInt("category_id"));
                board.setUserId(rs.getInt("user_id"));
                board.setTitle(rs.getString("title"));
                board.setContent(rs.getString("content"));
                board.setViews(rs.getInt("views"));
                board.setNotice(rs.getBoolean("is_notice"));
                board.setCreatedAt(rs.getTimestamp("created_at"));
                board.setUpdatedAt(rs.getTimestamp("updated_at"));
                
                // 사용자 정보
                String username = rs.getString("username");
                String nickname = rs.getString("nickname");
                board.setUsername(username);
                board.setNickname(nickname);
                
                // 카테고리 이름
                board.setCategoryName(rs.getString("category_name"));
                
                rs.close();
                pstmt.close();
                
                // 댓글 목록 가져오기
                String commentSql = "SELECT c.*, u.username, u.nickname " +
                                   "FROM board_comment c " +
                                   "LEFT JOIN user u ON c.user_id = u.id " +
                                   "WHERE c.board_id = ? " +
                                   "ORDER BY c.created_at ASC";
                pstmt = conn.prepareStatement(commentSql);
                pstmt.setInt(1, boardId);
                rs = pstmt.executeQuery();
                
                List<BoardComment> commentList = new ArrayList<>();
                while (rs.next()) {
                    BoardComment comment = new BoardComment();
                    comment.setId(rs.getInt("id"));
                    comment.setBoardId(rs.getInt("board_id"));
                    comment.setUserId(rs.getInt("user_id"));
                    comment.setContent(rs.getString("content"));
                    comment.setCreatedAt(rs.getTimestamp("created_at"));
                    comment.setUpdatedAt(rs.getTimestamp("updated_at"));
                    
                    // 사용자 정보 설정
                    String commentUsername = rs.getString("username");
                    String commentNickname = rs.getString("nickname");
                    
                    // BoardComment 클래스에 username과 nickname 필드가 없는 경우 대체 방법
                    comment.setUserName(commentNickname != null && !commentNickname.isEmpty() ? commentNickname : commentUsername);
                    
                    commentList.add(comment);
                }
                
                // 현재 로그인한 사용자 정보
                Integer userId = (Integer) session.getAttribute("userId");
                boolean isAuthor = userId != null && userId.equals(board.getUserId());
                boolean isAdmin = session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin");
        %>
        
        <h1>게시글 보기</h1>
        
        <div class="board-view">
            <div class="board-header">
                <h2><%= board.getTitle() %></h2>
                <div class="board-info">
                    <div>
                        <span class="category"><%= board.getCategoryName() %></span>
                        <span class="author">
                            <% if (board.getNickname() != null && !board.getNickname().isEmpty()) { %>
                                <%= board.getNickname() %>
                            <% } else { %>
                                <%= board.getUsername() %>
                            <% } %>
                        </span>
                        <span class="date"><%= new SimpleDateFormat("yyyy-MM-dd HH:mm").format(board.getCreatedAt()) %></span>
                    </div>
                    <div>
                        <span class="views">조회 <%= board.getViews() %></span>
                    </div>
                </div>
            </div>
            
            <div class="board-content">
                <%= board.getContent().replace("\n", "<br>") %>
            </div>
            
            <div class="board-actions">
                <div class="left">
                    <a href="list.jsp" class="btn btn-list">목록</a>
                </div>
                <div class="right">
                    <% if (isAuthor || isAdmin) { %>
                        <a href="edit.jsp?id=<%= board.getId() %>" class="btn btn-edit">수정</a>
                        <a href="javascript:void(0);" onclick="confirmDelete(<%= board.getId() %>)" class="btn btn-delete">삭제</a>
                    <% } %>
                </div>
            </div>
        </div>
        
        <!-- 댓글 섹션 -->
        <div class="comment-section">
            <h3>댓글 (<%= commentList.size() %>)</h3>
            
            <% if (commentList.isEmpty()) { %>
                <div class="no-comments">
                    첫 댓글을 작성해보세요!
                </div>
            <% } else { %>
                <ul class="comment-list">
                    <% for (BoardComment comment : commentList) { %>
                        <li class="comment-item" id="comment-<%= comment.getId() %>">
                            <div class="comment-info">
                                <div class="comment-writer"><%= comment.getUserName() %></div>
                                <div class="comment-date"><%= new SimpleDateFormat("yyyy-MM-dd HH:mm").format(comment.getCreatedAt()) %></div>
                            </div>
                            <div class="comment-content">
                                <%= comment.getContent().replace("\n", "<br>") %>
                            </div>
                            <% if (userId != null && (userId.equals(comment.getUserId()) || isAdmin)) { %>
                                <div class="comment-actions">
                                    <a href="javascript:void(0);" onclick="showEditCommentForm(<%= comment.getId() %>)" class="btn-edit-comment">수정</a>
                                    <a href="javascript:void(0);" onclick="confirmDeleteComment(<%= comment.getId() %>)" class="btn-delete-comment">삭제</a>
                                </div>
                                <div id="edit-form-<%= comment.getId() %>" class="edit-comment-form" style="display: none;">
                                    <form action="comment_edit_process.jsp" method="post">
                                        <input type="hidden" name="commentId" value="<%= comment.getId() %>">
                                        <input type="hidden" name="boardId" value="<%= board.getId() %>">
                                        <textarea name="content"><%= comment.getContent() %></textarea>
                                        <div class="form-actions">
                                            <button type="button" onclick="hideEditCommentForm(<%= comment.getId() %>)" class="btn-cancel">취소</button>
                                            <button type="submit" class="btn-submit">수정</button>
                                        </div>
                                    </form>
                                </div>
                            <% } %>
                        </li>
                    <% } %>
                </ul>
            <% } %>
            
            <% if (userId != null) { %>
                <div class="comment-form">
                    <form action="comment_write_process.jsp" method="post">
                        <input type="hidden" name="boardId" value="<%= board.getId() %>">
                        <textarea name="content" placeholder="댓글을 작성해주세요." required></textarea>
                        <button type="submit" class="btn-submit">댓글 작성</button>
                    </form>
                </div>
            <% } else { %>
                <div class="comment-login-message">
                    댓글을 작성하려면 <a href="${pageContext.request.contextPath}/member/login.jsp">로그인</a>이 필요합니다.
                </div>
            <% } %>
        </div>
        
        <%
            } catch (Exception e) {
                e.printStackTrace();
        %>
            <div class="error-message">
                오류가 발생했습니다: <%= e.getMessage() %><br>
                <a href="list.jsp">게시판으로 돌아가기</a>
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
    </div>
    
    <jsp:include page="../footer.jsp" />
    
    <script>
        function confirmDelete(boardId) {
            if (confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
                location.href = 'delete_process.jsp?id=' + boardId;
            }
        }
        
        function showEditCommentForm(commentId) {
            document.getElementById('edit-form-' + commentId).style.display = 'block';
        }
        
        function hideEditCommentForm(commentId) {
            document.getElementById('edit-form-' + commentId).style.display = 'none';
        }
        
        function confirmDeleteComment(commentId) {
            if (confirm('정말로 이 댓글을 삭제하시겠습니까?')) {
                location.href = 'comment_delete_process.jsp?id=' + commentId + '&boardId=<%= boardId %>';
            }
        }
    </script>
</body>
</html>
