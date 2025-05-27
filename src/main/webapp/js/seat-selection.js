document.addEventListener("DOMContentLoaded", () => {
  // URL 파라미터에서 정보 가져오기
  const urlParams = new URLSearchParams(window.location.search)
  const screeningId = urlParams.get("screeningId")

  // 세션 스토리지에서 예매 정보 가져오기
  const bookingData = JSON.parse(sessionStorage.getItem("bookingData") || "{}")

  // 좌석 선택 상태 관리
  const selectedSeats = new Set()
  let adultCount = 0
  let youthCount = 0

  // 좌석 선택 이벤트 처리
  document.querySelectorAll(".seat").forEach((seat) => {
    seat.addEventListener("click", function () {
      if (this.classList.contains("occupied")) {
        return // 이미 예매된 좌석은 선택 불가
      }

      const seatId = this.getAttribute("data-seat")

      if (selectedSeats.has(seatId)) {
        // 이미 선택된 좌석 선택 해제
        selectedSeats.delete(seatId)
        this.classList.remove("selected")
      } else {
        // 인원 수 확인
        const totalSelected = selectedSeats.size
        const totalCount = adultCount + youthCount

        if (totalSelected >= totalCount) {
          alert("선택하신 인원 수만큼 좌석을 선택하셨습니다.")
          return
        }

        // 새 좌석 선택
        selectedSeats.add(seatId)
        this.classList.add("selected")
      }

      // 선택된 좌석 정보 업데이트
      updateSelectedSeatsInfo()
    })
  })

  // 인원 수 변경 이벤트 처리
  document.getElementById("adult-count").addEventListener("change", function () {
    adultCount = Number.parseInt(this.value)
    validateSeatSelection()
  })

  document.getElementById("youth-count").addEventListener("change", function () {
    youthCount = Number.parseInt(this.value)
    validateSeatSelection()
  })

  // 좌석 선택 유효성 검사
  function validateSeatSelection() {
    const totalCount = adultCount + youthCount
    const selectedCount = selectedSeats.size

    // 선택된 좌석이 인원 수보다 많으면 초과분 제거
    if (selectedCount > totalCount) {
      const seatsToRemove = selectedCount - totalCount
      let removed = 0

      // 선택된 좌석 중에서 마지막 seatsToRemove개 제거
      const seatsArray = Array.from(selectedSeats)
      for (let i = seatsArray.length - 1; i >= 0 && removed < seatsToRemove; i--) {
        const seatId = seatsArray[i]
        selectedSeats.delete(seatId)
        document.querySelector(`.seat[data-seat="${seatId}"]`).classList.remove("selected")
        removed++
      }

      alert(`인원 수가 변경되어 ${seatsToRemove}개의 좌석 선택이 취소되었습니다.`)
    }

    // 선택된 좌석 정보 업데이트
    updateSelectedSeatsInfo()

    // 결제 버튼 활성화/비활성화
    const paymentButton = document.getElementById("payment-button")
    if (totalCount > 0 && selectedCount === totalCount) {
      paymentButton.disabled = false
    } else {
      paymentButton.disabled = true
    }
  }

  // 선택된 좌석 정보 업데이트
  function updateSelectedSeatsInfo() {
    const selectedSeatsElement = document.getElementById("selected-seats")
    const seatsArray = Array.from(selectedSeats)

    if (seatsArray.length > 0) {
      selectedSeatsElement.textContent = seatsArray.join(", ")
    } else {
      selectedSeatsElement.textContent = "선택된 좌석이 없습니다."
    }

    // 가격 계산
    const adultPrice = 12000
    const youthPrice = 9000
    const totalPrice = adultCount * adultPrice + youthCount * youthPrice

    document.getElementById("total-price").textContent = totalPrice.toLocaleString() + "원"

    // 결제 버튼 활성화/비활성화
    const paymentButton = document.getElementById("payment-button")
    const totalCount = adultCount + youthCount

    if (totalCount > 0 && selectedSeats.size === totalCount) {
      paymentButton.disabled = false
    } else {
      paymentButton.disabled = true
    }
  }

  // 예매 완료 시 좌석 정보 업데이트
  document.getElementById("booking-form").addEventListener("submit", () => {
    // 선택된 좌석 정보를 hidden input에 설정
    document.getElementById("selected-seats-input").value = Array.from(selectedSeats).join(",")

    // 세션 스토리지에서 해당 상영 정보 업데이트
    if (screeningId) {
      // 모든 키를 순회하며 해당 screeningId를 찾음
      Object.keys(bookingData).forEach((key) => {
        const screens = bookingData[key]
        screens.forEach((screen) => {
          screen.screenings.forEach((screening) => {
            if (screening.id == screeningId) {
              // 선택된 좌석 수만큼 가용 좌석 감소
              screening.availableSeats -= selectedSeats.size
              // 세션 스토리지 업데이트
              sessionStorage.setItem("bookingData", JSON.stringify(bookingData))
            }
          })
        })
      })
    }

    return true // 폼 제출 계속 진행
  })

  // 초기화
  adultCount = Number.parseInt(document.getElementById("adult-count").value)
  youthCount = Number.parseInt(document.getElementById("youth-count").value)
  updateSelectedSeatsInfo()
})
