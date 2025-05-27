<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CinemaWorld - 회원가입</title>
    <link rel="stylesheet" href="../css/Style.css">
    <script src="https://kit.fontawesome.com/a076d05399.js" crossorigin="anonymous"></script>
    <!-- 다음 우편번호 API -->
    <script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
</head>
<body>
    <div class="container">
        <div class="register-container">
            <div class="register-form">
                <div class="logo">
                    <i class="fas fa-film"></i>
                    <h1>CinemaWorld</h1>
                </div>
                <h2>회원가입</h2>
                <p class="welcome-text">영화 커뮤니티에 가입하고 다양한 혜택을 누리세요</p>
                
                <% if (request.getAttribute("errorMessage") != null) { %>
                    <div class="error-message">
                        <%= request.getAttribute("errorMessage") %>
                    </div>
                <% } %>
                
                <form action="RegisterServlet" method="post" enctype="multipart/form-data" id="registerForm">
                    <div class="input-group">
                        <label for="username">아이디 <span class="required">*</span></label>
                        <input type="text" id="username" name="username" required>
                        <span id="usernameError" class="error-message"></span>
                    </div>
                    
                    <div class="input-group">
                        <label for="password">비밀번호 <span class="required">*</span></label>
                        <input type="password" id="password" name="password" required>
                        <span id="passwordError" class="error-message"></span>
                        <p class="password-hint">8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다</p>
                    </div>
                    
                    <div class="input-group">
                        <label for="confirmPassword">비밀번호 확인 <span class="required">*</span></label>
                        <input type="password" id="confirmPassword" name="confirmPassword" required>
                        <span id="confirmPasswordError" class="error-message"></span>
                    </div>
                    
                    <div class="input-group">
                        <label for="name">이름 <span class="required">*</span></label>
                        <input type="text" id="name" name="name" required>
                    </div>
                    
                    <div class="input-group">
                        <label for="nickname">닉네임 <span class="required">*</span></label>
                        <input type="text" id="nickname" name="nickname" required>
                    </div>
                    
                    <div class="input-group">
                        <label for="postcode">주소</label>
                        <div class="address-search">
                            <input type="text" id="postcode" name="postcode" placeholder="우편번호" readonly>
                            <button type="button" id="postcodeBtn">우편번호 찾기</button>
                        </div>
                        <input type="text" id="address" name="address" placeholder="기본주소" readonly>
                        <div class="address-detail">
                            <input type="text" id="detailAddress" name="detailAddress" placeholder="상세주소">
                            <input type="hidden" id="fullAddress" name="fullAddress">
                        </div>
                    </div>
                    
                    <div class="input-group">
                        <label for="birthdate">생년월일</label>
                        <input type="date" id="birthdate" name="birthdate">
                    </div>
                    
                    <div class="input-group">
                        <label for="profileImage">프로필 이미지</label>
                        <input type="file" id="profileImage" name="profileImage" accept="image/*">
                        <div class="image-preview" id="imagePreview">
                            <img src="image/default-profile.png" alt="프로필 이미지 미리보기" id="previewImg">
                        </div>
                    </div>
                    
                    <div class="terms-agreement">
                        <input type="checkbox" id="agreeTerms" name="agreeTerms" required>
                        <label for="agreeTerms">이용약관 및 개인정보 처리방침에 동의합니다 <span class="required">*</span></label>
                    </div>
                    
                    <button type="submit" class="register-btn" id="registerBtn">회원가입</button>
                </form>
                
                <div class="login-link">
                    <p>이미 계정이 있으신가요? <a href="login.jsp">로그인</a></p>
                </div>
            </div>
        </div>
    </div>
    
    <script>
    // 다음 우편번호 API 함수
    function execDaumPostcode() {
        new daum.Postcode({
            oncomplete: function(data) {
                // 팝업에서 검색결과 항목을 클릭했을때 실행할 코드를 작성하는 부분.

                // 각 주소의 노출 규칙에 따라 주소를 조합한다.
                // 내려오는 변수가 값이 없는 경우엔 공백('')값을 가지므로, 이를 참고하여 분기 한다.
                let addr = ''; // 주소 변수

                // 사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
                if (data.userSelectedType === 'R') { // 사용자가 도로명 주소를 선택했을 경우
                    addr = data.roadAddress;
                } else { // 사용자가 지번 주소를 선택했을 경우(J)
                    addr = data.jibunAddress;
                }

                // 우편번호와 주소 정보를 해당 필드에 넣는다.
                document.getElementById('postcode').value = data.zonecode;
                document.getElementById('address').value = addr;
                
                // 커서를 상세주소 필드로 이동한다.
                document.getElementById('detailAddress').focus();
                
                // 전체 주소 업데이트
                updateFullAddress();
            }
        }).open();
    }
    
    // 페이지 로드 후 실행
    document.addEventListener('DOMContentLoaded', function() {
        // 우편번호 찾기 버튼 이벤트 리스너
        document.getElementById('postcodeBtn').addEventListener('click', function() {
            execDaumPostcode();
        });
        
        // 상세주소 입력 시 전체 주소 업데이트
        document.getElementById('detailAddress').addEventListener('input', function() {
            updateFullAddress();
        });
        
        // 전체 주소 업데이트 함수
        function updateFullAddress() {
            const addressEl = document.getElementById('address');
            const detailAddressEl = document.getElementById('detailAddress');
            const fullAddressEl = document.getElementById('fullAddress');
            
            if (addressEl.value) {
                fullAddressEl.value = addressEl.value;
                if (detailAddressEl.value) {
                    fullAddressEl.value += ' ' + detailAddressEl.value;
                }
            }
        }
    });
    </script>
    
    <script src="js/register.js"></script>
</body>
</html>
