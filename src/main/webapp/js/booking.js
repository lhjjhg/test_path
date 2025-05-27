document.addEventListener("DOMContentLoaded", () => {
  // 선택된 요소들을 저장할 변수
  let selectedMovie = null
  let selectedTheater = null
  let selectedDate = null

  // 영화 선택 이벤트
  const movieItems = document.querySelectorAll(".movie-item")
  movieItems.forEach((item) => {
    item.addEventListener("click", function () {
      // 이미 선택된 영화 선택 해제
      document.querySelectorAll(".movie-item.selected").forEach((selected) => {
        selected.classList.remove("selected")
      })

      // 현재 영화 선택
      this.classList.add("selected")
      selectedMovie = this.getAttribute("data-movie-id")

      // 상영 시간표 업데이트
      updateScreenings()
    })
  })

  // 지역 선택 이벤트
  const regionItems = document.querySelectorAll(".region-item")
  regionItems.forEach((item) => {
    item.addEventListener("click", function () {
      // 이미 선택된 지역 선택 해제
      document.querySelectorAll(".region-item.selected").forEach((selected) => {
        selected.classList.remove("selected")
      })

      // 현재 지역 선택
      this.classList.add("selected")
      const selectedRegion = this.getAttribute("data-region")

      // 극장 필터링
      filterTheaters(selectedRegion)
    })
  })

  // 극장 필터링 함수
  function filterTheaters(region) {
    const theaterItems = document.querySelectorAll(".theater-item")

    theaterItems.forEach((item) => {
      const theaterRegion = item.getAttribute("data-region")

      if (region === "all" || theaterRegion === region) {
        item.style.display = "block"
      } else {
        item.style.display = "none"

        // 숨겨진 극장이 선택되어 있었다면 선택 해제
        if (item.classList.contains("selected")) {
          item.classList.remove("selected")
          selectedTheater = null
          updateScreenings()
        }
      }
    })
  }

  // 극장 선택 이벤트
  const theaterItems = document.querySelectorAll(".theater-item")
  theaterItems.forEach((item) => {
    item.addEventListener("click", function () {
      // 이미 선택된 극장 선택 해제
      document.querySelectorAll(".theater-item.selected").forEach((selected) => {
        selected.classList.remove("selected")
      })

      // 현재 극장 선택
      this.classList.add("selected")
      selectedTheater = this.getAttribute("data-theater-id")

      // 상영 시간표 업데이트
      updateScreenings()
    })
  })

  // 날짜 선택 이벤트
  const dateItems = document.querySelectorAll(".date-item")
  dateItems.forEach((item) => {
    item.addEventListener("click", function () {
      // 이미 선택된 날짜 선택 해제
      document.querySelectorAll(".date-item.selected").forEach((selected) => {
        selected.classList.remove("selected")
      })

      // 현재 날짜 선택
      this.classList.add("selected")
      selectedDate = this.getAttribute("data-date")

      // 상영 시간표 업데이트
      updateScreenings()
    })
  })

  // 상영 시간표 업데이트 함수
  function updateScreenings() {
    const timeList = document.querySelector(".time-list")
    const noScheduleMessage = document.querySelector(".no-schedule-message")

    // 영화, 극장, 날짜가 모두 선택되었는지 확인
    if (selectedMovie && selectedTheater && selectedDate) {
      // 로딩 메시지 표시
      timeList.innerHTML =
        '<div class="loading-message"><i class="fas fa-spinner fa-spin"></i> 상영 시간표를 불러오는 중입니다...</div>'
      timeList.style.display = "block"
      noScheduleMessage.style.display = "none"

      // 고정된 더미 데이터 사용
      setTimeout(() => {
        const screenings = getFixedScreenings(selectedMovie, selectedTheater, selectedDate)
        renderScreenings(screenings)
      }, 500) // 로딩 효과를 위해 0.5초 지연
    } else {
      // 선택되지 않은 항목이 있으면 메시지 표시
      timeList.style.display = "none"
      noScheduleMessage.style.display = "block"

      let message = "영화, 극장, 날짜를 선택해주세요."
      if (selectedMovie) {
        if (!selectedTheater && !selectedDate) {
          message = "극장과 날짜를 선택해주세요."
        } else if (!selectedTheater) {
          message = "극장을 선택해주세요."
        } else if (!selectedDate) {
          message = "날짜를 선택해주세요."
        }
      } else {
        message = "영화를 먼저 선택해주세요."
      }

      noScheduleMessage.innerHTML = `<i class="fas fa-film"></i><p>${message}</p>`
    }
  }

  // 고정된 상영 정보 생성 함수
  function getFixedScreenings(movieId, theaterId, date) {
    // 상영관 정보 (고정)
    const screens = [
      {
        screenName: "1관 (일반)",
        screenings: [
          { id: 101, time: "10:30", availableSeats: 150, seatsTotal: 150 },
          { id: 102, time: "13:00", availableSeats: 150, seatsTotal: 150 },
          { id: 103, time: "15:30", availableSeats: 150, seatsTotal: 150 },
          { id: 104, time: "18:00", availableSeats: 150, seatsTotal: 150 },
          { id: 105, time: "20:30", availableSeats: 150, seatsTotal: 150 },
        ],
      },
      {
        screenName: "2관 (일반)",
        screenings: [
          { id: 201, time: "11:00", availableSeats: 120, seatsTotal: 120 },
          { id: 202, time: "13:30", availableSeats: 120, seatsTotal: 120 },
          { id: 203, time: "16:00", availableSeats: 120, seatsTotal: 120 },
          { id: 204, time: "18:30", availableSeats: 120, seatsTotal: 120 },
          { id: 205, time: "21:00", availableSeats: 120, seatsTotal: 120 },
        ],
      },
      {
        screenName: "3관 (IMAX)",
        screenings: [
          { id: 301, time: "09:30", availableSeats: 50, seatsTotal: 50 },
          { id: 302, time: "12:30", availableSeats: 50, seatsTotal: 50 },
          { id: 303, time: "15:30", availableSeats: 50, seatsTotal: 50 },
          { id: 304, time: "18:30", availableSeats: 50, seatsTotal: 50 },
          { id: 305, time: "21:30", availableSeats: 50, seatsTotal: 50 },
        ],
      },
    ]

    // 데이터베이스에서 예매 정보 가져오기
    fetch(`GetScreeningInfoServlet?movieId=${movieId}&theaterId=${theaterId}&date=${date}`)
      .then((response) => response.json())
      .then((data) => {
        if (data.success) {
          // 예매된 좌석 수 반영
          data.screenings.forEach((dbScreening) => {
            screens.forEach((screen) => {
              screen.screenings.forEach((screening) => {
                if (screening.id == dbScreening.id) {
                  screening.availableSeats = dbScreening.availableSeats
                }
              })
            })
          })
        }
      })
      .catch((error) => {
        console.error("Error fetching screening info:", error)
      })

    return screens
  }

  // 상영 시간표 렌더링 함수
  function renderScreenings(screens) {
    const timeList = document.querySelector(".time-list")

    if (!screens || screens.length === 0) {
      timeList.innerHTML =
        '<div class="no-screenings"><i class="fas fa-calendar-times"></i><br>선택하신 조건에 맞는 상영 일정이 없습니다.</div>'
      return
    }

    let html = ""

    screens.forEach((screen) => {
      html += `
        <div class="screen-group">
          <div class="screen-name"><i class="fas fa-tv"></i> ${screen.screenName}</div>
          <div class="time-buttons">
      `

      screen.screenings.forEach((screening) => {
        const time = screening.time
        const availableSeats = screening.availableSeats
        const seatsTotal = screening.seatsTotal

        // 좌석 상태에 따른 스타일 클래스
        let seatClass = ""
        if (availableSeats === 0) {
          seatClass = "sold-out"
        } else if (availableSeats < seatsTotal * 0.2) {
          seatClass = "few-seats"
        }

        html += `
          <div class="time-button ${seatClass}" data-screening-id="${screening.id}">
            <div class="time-info">${time}</div>
            <div class="seat-info">${availableSeats}/${seatsTotal}석</div>
          </div>
        `
      })

      html += `
          </div>
        </div>
      `
    })

    timeList.innerHTML = html

    // 시간 버튼 클릭 이벤트 추가
    const timeButtons = document.querySelectorAll(".time-button:not(.sold-out)")
    timeButtons.forEach((button) => {
      button.addEventListener("click", function () {
        const screeningId = this.getAttribute("data-screening-id")

        // 선택된 영화 정보 가져오기
        const selectedMovieElement = document.querySelector(".movie-item.selected")
        const movieTitle = selectedMovieElement ? selectedMovieElement.textContent.trim() : ""
        const movieId = selectedMovieElement ? selectedMovieElement.getAttribute("data-movie-id") : ""

        // 선택된 극장 정보 가져오기
        const selectedTheaterElement = document.querySelector(".theater-item.selected")
        const theaterName = selectedTheaterElement ? selectedTheaterElement.textContent.trim() : ""

        // 선택된 날짜 정보 가져오기
        const selectedDateElement = document.querySelector(".date-item.selected")
        const selectedDate = selectedDateElement ? selectedDateElement.getAttribute("data-date") : ""

        // 시간 정보 가져오기
        const timeInfo = this.querySelector(".time-info").textContent

        // 좌석 선택 페이지로 이동 (영화 정보 포함)
        const params = new URLSearchParams({
          screeningId: screeningId,
          movieId: movieId,
          movieTitle: movieTitle,
          theaterName: theaterName,
          date: selectedDate,
          time: timeInfo,
        })

        window.location.href = `seat-selection.jsp?${params.toString()}`
      })
    })

    // 매진된 시간 버튼에 대한 처리
    const soldOutButtons = document.querySelectorAll(".time-button.sold-out")
    soldOutButtons.forEach((button) => {
      button.addEventListener("click", (e) => {
        e.preventDefault()
        alert("해당 시간은 매진되었습니다.")
      })
    })
  }

  // 페이지 로드 시 URL 파라미터에 따라 초기 선택 설정
  function initSelectionFromUrl() {
    const urlParams = new URLSearchParams(window.location.search)
    const movieId = urlParams.get("id")

    if (movieId) {
      // 해당 영화 선택
      const movieItem = document.querySelector(`.movie-item[data-movie-id="${movieId}"]`)
      if (movieItem) {
        movieItem.click()
      }
    } else {
      // 첫 번째 영화 자동 선택
      const firstMovieItem = document.querySelector(".movie-item")
      if (firstMovieItem) {
        firstMovieItem.click()
      }
    }
  }

  // 초기화 함수 호출
  initSelectionFromUrl()

  // 첫 번째 지역 선택 (전체)
  const firstRegionItem = document.querySelector(".region-item")
  if (firstRegionItem) {
    firstRegionItem.click()
  }

  // 첫 번째 날짜 자동 선택 (오늘)
  const firstDateItem = document.querySelector(".date-item")
  if (firstDateItem) {
    firstDateItem.click()
  }

  // 첫 번째 극장 자동 선택
  setTimeout(() => {
    const firstTheaterItem = document.querySelector(".theater-item")
    if (firstTheaterItem) {
      firstTheaterItem.click()
    }
  }, 100)
})
