document.addEventListener("DOMContentLoaded", () => {
  // 게시글 삭제 확인
  const deleteButtons = document.querySelectorAll(".btn-delete")
  if (deleteButtons) {
    deleteButtons.forEach((button) => {
      button.addEventListener("click", (e) => {
        if (!confirm("정말로 삭제하시겠습니까?")) {
          e.preventDefault()
        }
      })
    })
  }

  // 댓글 삭제 확인
  const commentDeleteButtons = document.querySelectorAll(".comment-delete")
  if (commentDeleteButtons) {
    commentDeleteButtons.forEach((button) => {
      button.addEventListener("click", (e) => {
        if (!confirm("댓글을 삭제하시겠습니까?")) {
          e.preventDefault()
        }
      })
    })
  }

  // 게시글 작성 폼 유효성 검사
  const boardForm = document.getElementById("board-form")
  if (boardForm) {
    boardForm.addEventListener("submit", (e) => {
      const title = document.getElementById("title").value.trim()
      const content = document.getElementById("content").value.trim()
      const category = document.getElementById("category").value

      if (!category) {
        alert("카테고리를 선택해주세요.")
        e.preventDefault()
        return false
      }

      if (!title) {
        alert("제목을 입력해주세요.")
        document.getElementById("title").focus()
        e.preventDefault()
        return false
      }

      if (!content) {
        alert("내용을 입력해주세요.")
        document.getElementById("content").focus()
        e.preventDefault()
        return false
      }

      return true
    })
  }

  // 댓글 작성 폼 유효성 검사
  const commentForm = document.getElementById("comment-form")
  if (commentForm) {
    commentForm.addEventListener("submit", (e) => {
      const content = document.getElementById("comment-content").value.trim()

      if (!content) {
        alert("댓글 내용을 입력해주세요.")
        document.getElementById("comment-content").focus()
        e.preventDefault()
        return false
      }

      return true
    })
  }
})
