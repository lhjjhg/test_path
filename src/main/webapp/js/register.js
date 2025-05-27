document.addEventListener("DOMContentLoaded", () => {
  const registerForm = document.getElementById("registerForm")
  const username = document.getElementById("username")
  const password = document.getElementById("password")
  const confirmPassword = document.getElementById("confirmPassword")
  const profileImage = document.getElementById("profileImage")
  const previewImg = document.getElementById("previewImg")

  // 이미지 미리보기
  profileImage.addEventListener("change", (e) => {
    const file = e.target.files[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (e) => {
        previewImg.src = e.target.result
      }
      reader.readAsDataURL(file)
    } else {
      previewImg.src = "image/default-profile.png"
    }
  })

  // 폼 제출 시 유효성 검사
  registerForm.addEventListener("submit", (e) => {
    let isValid = true

    // 아이디 검사 (영문, 숫자 조합 4-20자)
    const usernameRegex = /^[a-zA-Z0-9]{4,20}$/
    if (!usernameRegex.test(username.value)) {
      document.getElementById("usernameError").textContent = "아이디는 영문, 숫자 조합 4-20자로 입력해주세요."
      isValid = false
    } else {
      document.getElementById("usernameError").textContent = ""
    }

    // 비밀번호 검사 (8자 이상, 영문, 숫자, 특수문자 포함)
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/
    if (!passwordRegex.test(password.value)) {
      document.getElementById("passwordError").textContent =
        "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다."
      isValid = false
    } else {
      document.getElementById("passwordError").textContent = ""
    }

    // 비밀번호 확인
    if (password.value !== confirmPassword.value) {
      document.getElementById("confirmPasswordError").textContent = "비밀번호가 일치하지 않습니다."
      isValid = false
    } else {
      document.getElementById("confirmPasswordError").textContent = ""
    }

    if (!isValid) {
      e.preventDefault()
    }
  })
})
