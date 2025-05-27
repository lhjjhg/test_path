<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="DB.DBConnection" %>
<%@ page import="dto.User" %>
<%@ page import="dao.UserDAO" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CinemaWorld - 프로필 수정</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/Style.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/profile.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
</head>
<body class="main-page">
    <%
        // 세션에서 사용자 정보 가져오기
        String username = (String) session.getAttribute("username");
        Integer userId = (Integer) session.getAttribute("userId");
        
        // 로그인 상태가 아니면 로그인 페이지로 리다이렉트
        if (username == null || userId == null) {
            response.sendRedirect(request.getContextPath() + "/member/login.jsp");
            return;
        }
        
        // 사용자 정보 조회
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserById(userId);
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/member/login.jsp");
            return;
        }
        
        // 프로필 이미지 경로 설정 (절대 경로 사용)
        String profileImage = request.getContextPath() + "/image/default-profile.png";
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            // 이미 절대 경로인 경우 (http로 시작하는 경우)
            if (user.getProfileImage().startsWith("http")) {
                profileImage = user.getProfileImage();
            } 
            // 상대 경로인 경우 컨텍스트 경로 추가
            else {
                profileImage = request.getContextPath() + "/" + user.getProfileImage();
            }
        }
        
        // 디버깅 출력
        System.out.println("프로필 이미지 경로: " + profileImage);
        
        // 에러 메시지 가져오기
        String errorMsg = (String) request.getAttribute("errorMsg");
        // 성공 메시지 가져오기 (request 또는 session에서)
        String successMsg = (String) request.getAttribute("successMsg");
        if (successMsg == null) {
            successMsg = (String) session.getAttribute("successMsg");
            if (successMsg != null) {
                // 세션에서 메시지를 가져왔으면 세션에서 제거
                session.removeAttribute("successMsg");
            }
        }
    %>

    <div class="site-wrapper">
        <!-- 헤더 포함 -->
        <jsp:include page="../header.jsp" />
        
        <main class="main-content">
            <div class="profile-container">
                <h1 class="page-title">프로필 수정</h1>
                
                <% if (errorMsg != null) { %>
                    <div class="alert alert-danger">
                        <%= errorMsg %>
                    </div>
                <% } %>
                
                <% if (successMsg != null) { %>
                    <div class="alert alert-success">
                        <%= successMsg %>
                    </div>
                <% } %>
                
                <div class="edit-profile-form-container">
                    <form action="<%= request.getContextPath() %>/ProfileUpdateServlet" method="post" enctype="multipart/form-data" class="edit-profile-form">
                        <div class="form-group profile-image-upload">
                            <div class="current-profile-image">
                                <img src="<%= profileImage %>" alt="현재 프로필 이미지" id="profile-preview" onerror="this.src='<%= request.getContextPath() %>/image/default-profile.png'">
                            </div>
                            <div class="profile-image-controls">
                                <label for="profile-image" class="custom-file-upload">
                                    <i class="fas fa-camera"></i> 프로필 이미지 변경
                                </label>
                                <input type="file" id="profile-image" name="profileImage" accept="image/*">
                            </div>
                        </div>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="username">아이디</label>
                                <input type="text" id="username" value="<%= user.getUsername() %>" disabled class="form-control">
                                <small class="form-text text-muted">아이디는 변경할 수 없습니다.</small>
                            </div>
                            
                            <div class="form-group">
                                <label for="password">비밀번호</label>
                                <input type="password" id="password" name="password" placeholder="변경할 비밀번호 입력" class="form-control">
                                <small class="form-text text-muted">변경하지 않으려면 비워두세요.</small>
                            </div>
                        </div>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="name">이름</label>
                                <input type="text" id="name" name="name" value="<%= user.getName() %>" class="form-control" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="nickname">닉네임</label>
                                <input type="text" id="nickname" name="nickname" value="<%= user.getNickname() %>" class="form-control" required>
                            </div>
                        </div>
                        
                        <div class="form-group">
                            <label for="address">주소</label>
                            <input type="text" id="address" name="address" value="<%= user.getAddress() != null ? user.getAddress() : "" %>" class="form-control">
                        </div>
                        
                        <div class="form-group">
                            <label for="birthdate">생년월일</label>
                            <input type="date" id="birthdate" name="birthdate" value="<%= user.getBirthdate() != null ? user.getBirthdate() : "" %>" class="form-control">
                        </div>
                        
                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary">변경사항 저장</button>
                            <a href="<%= request.getContextPath() %>/member/profile.jsp" class="btn btn-secondary">취소</a>
                        </div>
                    </form>
                </div>
            </div>
        </main>
        
        <!-- 푸터 포함 -->
        <jsp:include page="../footer.jsp" />
    </div>
    
    <script>
        // 프로필 이미지 미리보기
        document.getElementById('profile-image').addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    document.getElementById('profile-preview').src = e.target.result;
                }
                reader.readAsDataURL(file);
            }
        });
        
        // 이미지 크기 제한 및 조정
        window.addEventListener('DOMContentLoaded', function() {
            const profilePreview = document.getElementById('profile-preview');
            if (profilePreview) {
                profilePreview.style.maxWidth = '100%';
                profilePreview.style.maxHeight = '100%';
                profilePreview.style.objectFit = 'cover';
            }
            
            // 성공 메시지가 있으면 3초 후에 자동으로 사라지게 함
            const alertSuccess = document.querySelector('.alert-success');
            if (alertSuccess) {
                setTimeout(function() {
                    alertSuccess.style.transition = 'opacity 1s';
                    alertSuccess.style.opacity = '0';
                    setTimeout(function() {
                        alertSuccess.style.display = 'none';
                    }, 1000);
                }, 3000);
            }
        });
    </script>
</body>
</html>
