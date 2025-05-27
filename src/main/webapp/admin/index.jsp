<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="admin-check.jsp" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CinemaWorld - 관리자 페이지</title>
    <link rel="stylesheet" href="../css/Style.css">
    <link rel="stylesheet" href="../css/main.css">
    <style>
        .admin-container {
            max-width: 1000px;
            margin: 50px auto;
            padding: 30px;
            background-color: rgba(31, 31, 31, 0.9);
            border-radius: 10px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
        }
        
        .admin-title {
            text-align: center;
            margin-bottom: 40px;
        }
        
        .admin-title h1 {
            font-size: 32px;
            color: #fff;
            margin-bottom: 10px;
        }
        
        .admin-title p {
            color: #bbb;
            font-size: 16px;
        }
        
        .admin-menu {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 20px;
        }
        
        .menu-item {
            background-color: rgba(255, 255, 255, 0.05);
            border-radius: 10px;
            padding: 20px;
            transition: all 0.3s ease;
            cursor: pointer;
        }
        
        .menu-item:hover {
            background-color: rgba(229, 9, 20, 0.1);
            transform: translateY(-5px);
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
        }
        
        .menu-icon {
            font-size: 36px;
            color: #e50914;
            margin-bottom: 15px;
        }
        
        .menu-title {
            font-size: 20px;
            font-weight: 600;
            color: #fff;
            margin-bottom: 10px;
        }
        
        .menu-description {
            color: #bbb;
            font-size: 14px;
            line-height: 1.5;
        }
        
        .back-link {
            display: block;
            text-align: center;
            margin-top: 40px;
            color: #bbb;
            text-decoration: none;
            transition: color 0.3s ease;
        }
        
        .back-link:hover {
            color: #e50914;
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
                    <h1>관리자 페이지</h1>
                    <p>영화 정보 관리 및 시스템 설정을 위한 관리자 메뉴입니다</p>
                </div>
                
                <div class="admin-menu">
                    <a href="crawl-movies.jsp" class="menu-item">
                        <div class="menu-icon"><i class="fas fa-film"></i></div>
                        <div class="menu-title">현재 상영작 크롤링</div>
                        <div class="menu-description">CGV 웹사이트에서 현재 상영 중인 영화와 상영 예정작 정보를 크롤링합니다.</div>
                    </a>
                    
                    <a href="manage-movies.jsp" class="menu-item">
                        <div class="menu-icon"><i class="fas fa-tasks"></i></div>
                        <div class="menu-title">영화 데이터 관리</div>
                        <div class="menu-description">데이터베이스에 저장된 영화 정보를 조회, 수정, 삭제합니다.</div>
                    </a>
                    
                    <a href="manage-users.jsp" class="menu-item">
                        <div class="menu-icon"><i class="fas fa-users"></i></div>
                        <div class="menu-title">사용자 관리</div>
                        <div class="menu-description">사용자 계정 정보를 조회, 수정, 삭제하고 권한을 관리합니다.</div>
                    </a>
                    
                    <a href="manage-boards.jsp" class="menu-item">
                        <div class="menu-icon"><i class="fas fa-clipboard-list"></i></div>
                        <div class="menu-title">게시판 관리</div>
                        <div class="menu-description">게시판의 게시글과 댓글을 관리하고 카테고리를 설정합니다.</div>
                    </a>
                    
                    <a href="reset-movies.jsp" class="menu-item">
                        <div class="menu-icon"><i class="fas fa-trash-alt"></i></div>
                        <div class="menu-title">영화 데이터 초기화</div>
                        <div class="menu-description">데이터베이스의 모든 영화 정보를 삭제합니다.</div>
                    </a>
                    
                    <a href="crawl-debug.jsp" class="menu-item">
                        <div class="menu-icon"><i class="fas fa-bug"></i></div>
                        <div class="menu-title">크롤링 디버그</div>
                        <div class="menu-description">크롤링 과정에서 발생할 수 있는 문제를 진단하고 해결합니다.</div>
                    </a>
                    
                    <a href="site-statistics.jsp" class="menu-item">
                        <div class="menu-icon"><i class="fas fa-chart-bar"></i></div>
                        <div class="menu-title">사이트 통계</div>
                        <div class="menu-description">사이트 사용 통계와 분석 정보를 확인합니다.</div>
                    </a>
                    
                    <a href="add-movie.jsp" class="menu-item">
                        <div class="menu-icon"><i class="fas fa-plus-circle"></i></div>
                        <div class="menu-title">영화 직접 추가</div>
                        <div class="menu-description">영화 정보를 수동으로 추가합니다. 클래식 영화나 누락된 영화를 추가할 때 유용합니다.</div>
                    </a>
                </div>
                
                <a href="../index.jsp" class="back-link">메인 페이지로 돌아가기</a>
            </div>
        </main>
        
        <!-- 푸터 포함 -->
        <jsp:include page="../footer.jsp" />
    </div>
</body>
</html>
