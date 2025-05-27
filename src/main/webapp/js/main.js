document.addEventListener("DOMContentLoaded", () => {
  // 슬라이더 초기화
  const boxofficeSwiper = new Swiper(".boxoffice-slider", {
    slidesPerView: 1,
    spaceBetween: 20,
    loop: true,
    pagination: {
      el: ".swiper-pagination",
      clickable: true,
    },
    navigation: {
      nextEl: ".swiper-button-next",
      prevEl: ".swiper-button-prev",
    },
    autoplay: {
      delay: 5000,
      disableOnInteraction: false,
    },
    breakpoints: {
      // 576px 이상일 때
      576: {
        slidesPerView: 2,
        spaceBetween: 20,
      },
      // 768px 이상일 때
      768: {
        slidesPerView: 3,
        spaceBetween: 30,
      },
      // 992px 이상일 때
      992: {
        slidesPerView: 4,
        spaceBetween: 30,
      },
      // 1200px 이상일 때
      1200: {
        slidesPerView: 5,
        spaceBetween: 30,
      },
    },
  })

  // 모바일 메뉴 토글
  const menuToggle = document.querySelector(".mobile-menu-toggle")
  const mainNav = document.querySelector(".main-nav")

  if (menuToggle && mainNav) {
    menuToggle.addEventListener("click", () => {
      mainNav.classList.toggle("active")
    })
  }
})
