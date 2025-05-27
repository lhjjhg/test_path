package crawler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.DatabaseMetaData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import DB.DBConnection;
import dto.Movie;
import dto.MovieDetail;
import dao.MovieDAO;

public class MovieCrawler {
    
    // CGV 영화 차트 URL
    private static final String CGV_CHART_URL = "http://www.cgv.co.kr/movies/?lt=1&ft=0";
    // CGV 상영 예정작 URL
    private static final String CGV_COMING_URL = "http://www.cgv.co.kr/movies/pre-movies.aspx";
    // CGV 영화 상세 정보 URL 패턴
    private static final String CGV_MOVIE_DETAIL_URL = "http://www.cgv.co.kr/movies/detail-view/?midx=";
    
    // 로그 출력을 위한 StringBuilder
    private StringBuilder logBuilder = new StringBuilder();
    
    public String getLog() {
        return logBuilder.toString();
    }
    
    private void log(String message) {
        System.out.println(message);
        logBuilder.append(message).append("<br>");
    }
    
    /**
     * 현재 상영 중인 영화 목록을 크롤링하여 반환
     */
    public List<Movie> crawlCurrentMovies() {
        List<Movie> movies = new ArrayList<>();
        
        try {
            log("CGV 현재 상영작 URL 접속 시도: " + CGV_CHART_URL);
            
            // 더 많은 헤더 추가 및 타임아웃 증가
            Document doc = Jsoup.connect(CGV_CHART_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Cache-Control", "max-age=0")
                    .timeout(60000) // 타임아웃 60초로 증가
                    .get();
            
            log("CGV 현재 상영작 페이지 접속 성공");
            
            // 영화 목록 선택자 수정 - 더 정확한 선택자 사용
            Elements movieElements = doc.select("div.sect-movie-chart ol > li");
            log("찾은 영화 개수: " + movieElements.size());
            
            if (movieElements.isEmpty()) {
                log("영화 요소를 찾을 수 없습니다. 선택자를 확인하세요.");
                // 대체 선택자 시도
                movieElements = doc.select("div.wrap-movie-chart > div.sect-movie-chart > ol > li");
                log("대체 선택자로 찾은 영화 개수: " + movieElements.size());
                
                if (movieElements.isEmpty()) {
                    log("대체 선택자로도 영화를 찾을 수 없습니다. HTML 구조 확인:");
                    log(doc.select("div.wrap-movie-chart").html().substring(0, 500) + "...");
                }
            }
            
            int rank = 1; // 순위 초기화
            
            for (Element movieElement : movieElements) {
                try {
                    // 디버깅을 위해 현재 처리 중인 영화 요소의 HTML 출력
                    log("현재 처리 중인 영화 요소: " + movieElement.html().substring(0, Math.min(200, movieElement.html().length())) + "...");
                    
                    // 순위 정보 추출
                    String rankText = movieElement.select("strong.rank").text();
                    if (rankText.isEmpty()) {
                        rankText = movieElement.select("span.rank").text();
                    }
                    log("순위 텍스트: " + rankText);
                    
                    // 제목 추출
                    String title = movieElement.select("strong.title").text();
                    if (title.isEmpty()) {
                        title = movieElement.select("div.box-contents > a > strong").text();
                    }
                    log("영화 제목: " + title);
                    
                    // 포스터 URL 추출
                    String posterUrl = movieElement.select("span.thumb-image img").attr("src");
                    if (posterUrl.isEmpty()) {
                        posterUrl = movieElement.select("div.box-image > a > span > img").attr("src");
                    }
                    log("포스터 URL: " + posterUrl);
                    
                    // 평점 추출
                    String ratingText = movieElement.select("span.percent span").text().replace("%", "");
                    if (ratingText.isEmpty()) {
                        ratingText = movieElement.select("div.box-contents > div.score > strong.percent > span").text().replace("%", "");
                    }
                    log("평점 텍스트: " + ratingText);
                    
                    double rating = 0.0;
                    try {
                        if (!ratingText.isEmpty()) {
                            rating = Double.parseDouble(ratingText) / 10.0; // 100% 기준을 10점 만점으로 변환
                        }
                    } catch (NumberFormatException e) {
                        log("평점 파싱 실패: " + ratingText + " - " + e.getMessage());
                    }
                    
                    // 영화 상세 페이지 링크에서 영화 ID 추출
                    String detailLink = movieElement.select("div.box-image > a").attr("href");
                    if (detailLink.isEmpty()) {
                        detailLink = movieElement.select("div.box-image > a").attr("href");
                    }
                    log("상세 링크: " + detailLink);
                    
                    String movieId = "";
                    if (detailLink.contains("midx=")) {
                        movieId = detailLink.substring(detailLink.indexOf("midx=") + 5);
                        if (movieId.contains("&")) {
                            movieId = movieId.substring(0, movieId.indexOf("&"));
                        }
                        log("영화 ID 추출: " + movieId);
                    } else {
                        log("영화 ID 추출 실패. 상세 링크: " + detailLink);
                    }
                    
                    // 개봉일, 장르, 러닝타임 등 추가 정보
                    String info = movieElement.select("span.txt-info").text();
                    if (info.isEmpty()) {
                        info = movieElement.select("div.box-contents > span.txt-info").text();
                    }
                    log("영화 정보 텍스트: " + info);
                    
                    String releaseDate = "";
                    String genre = "";
                    String runningTime = "";
                    
                    if (info.contains("개봉")) {
                        releaseDate = info.substring(0, info.indexOf("개봉")).trim();
                        log("개봉일 추출: " + releaseDate);
                    }
                    
                    // 장르와 러닝타임은 상세 페이지에서 가져오기 위해 일단 비워둠
                    
                    // 영화 객체 생성 및 설정
                    if (!title.isEmpty() && !movieId.isEmpty()) {
                        Movie movie = new Movie();
                        movie.setMovieId(movieId);
                        movie.setTitle(title);
                        movie.setPosterUrl(posterUrl);
                        movie.setRating(rating);
                        
                        // 순위 설정 - 실제 순위 텍스트가 있으면 사용, 없으면 순서대로 부여
                        if (rankText != null && !rankText.isEmpty()) {
                            try {
                                movie.setMovieRank(Integer.parseInt(rankText));
                            } catch (NumberFormatException e) {
                                movie.setMovieRank(rank++); // 파싱 실패 시 순서대로 부여
                            }
                        } else {
                            movie.setMovieRank(rank++); // 순위 정보가 없으면 순서대로 부여
                        }
                        
                        movie.setReleaseDate(releaseDate);
                        movie.setGenre(genre);
                        movie.setRunningTime(runningTime);
                        movie.setStatus("current"); // 현재 상영 중
                        
                        movies.add(movie);
                        log("영화 정보 수집 완료: " + title);
                    } else {
                        log("영화 제목 또는 ID가 비어있어 건너뜁니다.");
                    }
                } catch (Exception e) {
                    log("영화 정보 파싱 중 오류: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // 영화가 하나도 수집되지 않았다면 다른 방법 시도
            if (movies.isEmpty()) {
                log("기본 방법으로 영화를 찾을 수 없습니다. 다른 방법 시도...");
                
                // 다른 페이지에서 시도 (무비차트 페이지)
                String alternativeUrl = "http://www.cgv.co.kr/movies/";
                log("대체 URL 접속 시도: " + alternativeUrl);
                
                Document altDoc = Jsoup.connect(alternativeUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                        .timeout(60000)
                        .get();
                
                Elements altMovieElements = altDoc.select("div.sect-movie-chart ol > li");
                log("대체 URL에서 찾은 영화 개수: " + altMovieElements.size());
                
                rank = 1; // 순위 초기화
                
                for (Element movieElement : altMovieElements) {
                    try {
                        String title = movieElement.select("strong.title").text();
                        String posterUrl = movieElement.select("span.thumb-image img").attr("src");
                        String detailLink = movieElement.select("div.box-image > a").attr("href");
                        
                        log("대체 방법 - 영화 제목: " + title);
                        log("대체 방법 - 포스터 URL: " + posterUrl);
                        log("대체 방법 - 상세 링크: " + detailLink);
                        
                        String movieId = "";
                        if (detailLink.contains("midx=")) {
                            movieId = detailLink.substring(detailLink.indexOf("midx=") + 5);
                            if (movieId.contains("&")) {
                                movieId = movieId.substring(0, movieId.indexOf("&"));
                            }
                        }
                        
                        if (!title.isEmpty() && !movieId.isEmpty()) {
                            Movie movie = new Movie();
                            movie.setMovieId(movieId);
                            movie.setTitle(title);
                            movie.setPosterUrl(posterUrl);
                            movie.setMovieRank(rank++);
                            movie.setStatus("current");
                            
                            movies.add(movie);
                            log("대체 방법 - 영화 정보 수집 완료: " + title);
                        }
                    } catch (Exception e) {
                        log("대체 방법 - 영화 정보 파싱 중 오류: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            log("CGV 영화 차트 크롤링 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        log("현재 상영작 크롤링 완료. 영화 수: " + movies.size());
        return movies;
    }
    
    /**
     * 상영 예정작 목록을 크롤링하여 반환
     */
    public List<Movie> crawlComingSoonMovies() {
        List<Movie> movies = new ArrayList<>();
        
        try {
            log("CGV 상영 예정작 URL 접속 시도: " + CGV_COMING_URL);
            Document doc = Jsoup.connect(CGV_COMING_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .timeout(60000)
                    .get();
            log("CGV 상영 예정작 페이지 접속 성공");
            
            Elements movieElements = doc.select("div.sect-movie-chart ol > li");
            log("찾은 상영 예정작 개수: " + movieElements.size());
            
            if (movieElements.isEmpty()) {
                log("상영 예정작 요소를 찾을 수 없습니다. 선택자를 확인하세요.");
                // 대체 선택자 시도
                movieElements = doc.select("div.wrap-movie-chart > div.sect-movie-chart > ol > li");
                log("대체 선택자로 찾은 상영 예정작 개수: " + movieElements.size());
            }
            
            for (Element movieElement : movieElements) {
                try {
                    String title = movieElement.select("strong.title").text();
                    if (title.isEmpty()) {
                        title = movieElement.select("div.box-contents > a > strong").text();
                    }
                    
                    String posterUrl = movieElement.select("span.thumb-image img").attr("src");
                    if (posterUrl.isEmpty()) {
                        posterUrl = movieElement.select("div.box-image > a > span > img").attr("src");
                    }
                    
                    log("상영 예정작 정보 파싱: " + title);
                    
                    // 영화 상세 페이지 링크에서 영화 ID 추출
                    String detailLink = movieElement.select("div.box-image > a").attr("href");
                    String movieId = "";
                    if (detailLink.contains("midx=")) {
                        movieId = detailLink.substring(detailLink.indexOf("midx=") + 5);
                        if (movieId.contains("&")) {
                            movieId = movieId.substring(0, movieId.indexOf("&"));
                        }
                        log("영화 ID 추출: " + movieId);
                    } else {
                        log("영화 ID 추출 실패. 상세 링크: " + detailLink);
                    }
                    
                    // 개봉일 정보
                    String releaseDate = movieElement.select("div.box-contents > span.txt-info > strong").text();
                    if (releaseDate.isEmpty()) {
                        releaseDate = movieElement.select("span.txt-info strong").text();
                    }
                    
                    if (releaseDate.contains("개봉")) {
                        releaseDate = releaseDate.replace("개봉", "").trim();
                        log("개봉일 추출: " + releaseDate);
                    }
                    
                    if (!title.isEmpty() && !movieId.isEmpty()) {
                        Movie movie = new Movie();
                        movie.setMovieId(movieId);
                        movie.setTitle(title);
                        movie.setPosterUrl(posterUrl);
                        movie.setReleaseDate(releaseDate);
                        movie.setStatus("coming"); // 상영 예정
                        
                        movies.add(movie);
                        log("상영 예정작 정보 수집 완료: " + title);
                    } else {
                        log("영화 제목 또는 ID가 비어있어 건너뜁니다.");
                    }
                } catch (Exception e) {
                    log("상영 예정작 정보 파싱 중 오류: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
        } catch (IOException e) {
            log("CGV 상영 예정작 크롤링 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        log("상영 예정작 크롤링 완료. 영화 수: " + movies.size());
        return movies;
    }
    
    /**
     * 영화 상세 정보를 크롤링하여 반환
     */
    public MovieDetail crawlMovieDetail(String movieId) {
        MovieDetail movieDetail = new MovieDetail();
        
        try {
            String detailUrl = CGV_MOVIE_DETAIL_URL + movieId;
            log("영화 상세 정보 URL 접속 시도: " + detailUrl);
            
            Document doc = Jsoup.connect(detailUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .timeout(60000)
                    .get();
            log("영화 상세 정보 페이지 접속 성공");
            
            // 영화 기본 정보
            Element infoElement = doc.selectFirst("div.sect-base-movie");
            if (infoElement != null) {
                String title = infoElement.select("div.box-contents > div.title > strong").text();
                if (title.isEmpty()) {
                    title = infoElement.select(".title strong").text();
                }
                
                String posterUrl = infoElement.select("div.box-image > a > span > img").attr("src");
                if (posterUrl.isEmpty()) {
                    posterUrl = infoElement.select(".thumb-image img").attr("src");
                }
                
                String englishTitle = infoElement.select("div.box-contents > div.title > p").text();
                if (englishTitle.isEmpty()) {
                    englishTitle = infoElement.select(".title p").text();
                }
                
                log("영화 제목: " + title);
                log("영어 제목: " + englishTitle);
                log("포스터 URL: " + posterUrl);
                
                movieDetail.setMovieId(movieId);
                movieDetail.setTitle(title);
                movieDetail.setEnglishTitle(englishTitle);
                movieDetail.setPosterUrl(posterUrl);
                
                // 영화 상세 정보
                Elements infoItems = infoElement.select(".box-contents .spec dl dt, .box-contents .spec dl dd");
                
                String currentKey = "";
                for (Element item : infoItems) {
                    if (item.is("dt")) {
                        currentKey = item.text().trim();
                        log("정보 타입: " + currentKey);
                    } else if (item.is("dd")) {
                        String value = item.text().trim();
                        log(currentKey + ": " + value);
                        
                        if (currentKey.contains("감독")) {
                            movieDetail.setDirector(value);
                        } else if (currentKey.contains("배우")) {
                            movieDetail.setActors(value);
                        } else if (currentKey.contains("장르")) {
                            // 장르, 러닝타임, 국가 등이 함께 있을 수 있음
                            String[] parts = value.split(",");
                            for (String part : parts) {
                                part = part.trim();
                                if (part.contains("분")) {
                                    movieDetail.setRunningTime(part);
                                } else if (part.contains("개봉") || part.contains("재개봉")) {
                                    movieDetail.setReleaseDate(part);
                                } else {
                                    movieDetail.setGenre(part);
                                }
                            }
                        } else if (currentKey.contains("등급")) {
                            movieDetail.setRating(value);
                        }
                    }
                }
            } else {
                log("영화 정보 요소(.sect-base-movie)를 찾을 수 없습니다.");
                log("HTML 구조 확인: " + doc.select("body").html().substring(0, 500) + "...");
            }
            
            // 영화 줄거리
            Element plotElement = doc.selectFirst(".sect-story-movie");
            if (plotElement != null) {
                String plot = plotElement.text().trim();
                log("줄거리: " + (plot.length() > 100 ? plot.substring(0, 100) + "..." : plot));
                movieDetail.setPlot(plot);
            } else {
                log("줄거리 요소(.sect-story-movie)를 찾을 수 없습니다.");
                // 대체 선택자 시도
                plotElement = doc.selectFirst("div.sect-story-movie");
                if (plotElement != null) {
                    String plot = plotElement.text().trim();
                    log("대체 선택자로 찾은 줄거리: " + (plot.length() > 100 ? plot.substring(0, 100) + "..." : plot));
                    movieDetail.setPlot(plot);
                }
            }
            
            // 스틸컷 이미지
            Elements stillcutElements = doc.select(".sect-stillcut img");
            List<String> stillcuts = new ArrayList<>();
            for (Element img : stillcutElements) {
                String imgUrl = img.attr("src");
                if (imgUrl != null && !imgUrl.isEmpty()) {
                    stillcuts.add(imgUrl);
                    log("스틸컷 이미지: " + imgUrl);
                }
            }
            
            // 스틸컷이 없으면 다른 방법으로 시도
            if (stillcuts.isEmpty()) {
                Elements photoElements = doc.select(".sect-phototicket img");
                for (Element img : photoElements) {
                    String imgUrl = img.attr("src");
                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        stillcuts.add(imgUrl);
                        log("포토티켓 이미지: " + imgUrl);
                    }
                }
                
                // 또 다른 대체 선택자 시도
                if (stillcuts.isEmpty()) {
                    Elements altPhotoElements = doc.select("div.gallery_box img");
                    for (Element img : altPhotoElements) {
                        String imgUrl = img.attr("src");
                        if (imgUrl != null && !imgUrl.isEmpty()) {
                            stillcuts.add(imgUrl);
                            log("갤러리 이미지: " + imgUrl);
                        }
                    }
                }
            }
            
            movieDetail.setStillcuts(stillcuts);
            
        } catch (IOException e) {
            log("영화 상세 정보 크롤링 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        log("영화 상세 정보 크롤링 완료: " + movieId);
        return movieDetail;
    }
    
    /**
     * 크롤링한 영화 정보를 데이터베이스에 저장
     */
    public void saveMoviesToDatabase(List<Movie> movies) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            log("데이터베이스 연결 시도...");
            conn = DBConnection.getConnection();
            log("데이터베이스 연결 성공");
            
            // 테이블이 존재하는지 확인하고 없으면 생성
            checkAndCreateTables(conn);
            
            // 영화 정보 저장 또는 업데이트
            String checkSql = "SELECT COUNT(*) FROM movie WHERE movie_id = ?";
            String insertSql = "INSERT INTO movie (movie_id, title, poster_url, rating, movie_rank, release_date, genre, running_time, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String updateSql = "UPDATE movie SET title = ?, poster_url = ?, rating = ?, movie_rank = ?, release_date = ?, genre = ?, running_time = ?, status = ? WHERE movie_id = ?";
            
            int insertCount = 0;
            int updateCount = 0;
            int errorCount = 0;
            
            for (Movie movie : movies) {
                try {
                    if (movie.getMovieId() == null || movie.getMovieId().isEmpty()) {
                        log("영화 ID가 없어 저장을 건너뜁니다: " + movie.getTitle());
                        errorCount++;
                        continue;
                    }
                    
                    // 이미 존재하는 영화인지 확인
                    pstmt = conn.prepareStatement(checkSql);
                    pstmt.setString(1, movie.getMovieId());
                    rs = pstmt.executeQuery();
                    
                    boolean exists = false;
                    if (rs.next()) {
                        exists = rs.getInt(1) > 0;
                    }
                    
                    if (exists) {
                        // 업데이트
                        log("기존 영화 정보 업데이트: " + movie.getTitle());
                        pstmt = conn.prepareStatement(updateSql);
                        pstmt.setString(1, movie.getTitle());
                        pstmt.setString(2, movie.getPosterUrl());
                        pstmt.setDouble(3, movie.getRating());
                        pstmt.setInt(4, movie.getMovieRank());
                        pstmt.setString(5, movie.getReleaseDate());
                        pstmt.setString(6, movie.getGenre());
                        pstmt.setString(7, movie.getRunningTime());
                        pstmt.setString(8, movie.getStatus());
                        pstmt.setString(9, movie.getMovieId());
                        
                        int result = pstmt.executeUpdate();
                        if (result > 0) {
                            updateCount++;
                            log("영화 정보 업데이트 성공: " + movie.getTitle());
                        } else {
                            log("영화 정보 업데이트 실패: " + movie.getTitle());
                            errorCount++;
                        }
                    } else {
                        // 삽입
                        log("새 영화 정보 삽입: " + movie.getTitle());
                        pstmt = conn.prepareStatement(insertSql);
                        pstmt.setString(1, movie.getMovieId());
                        pstmt.setString(2, movie.getTitle());
                        pstmt.setString(3, movie.getPosterUrl());
                        pstmt.setDouble(4, movie.getRating());
                        pstmt.setInt(5, movie.getMovieRank());
                        pstmt.setString(6, movie.getReleaseDate());
                        pstmt.setString(7, movie.getGenre());
                        pstmt.setString(8, movie.getRunningTime());
                        pstmt.setString(9, movie.getStatus());
                        
                        int result = pstmt.executeUpdate();
                        if (result > 0) {
                            insertCount++;
                            log("영화 정보 삽입 성공: " + movie.getTitle());
                        } else {
                            log("영화 정보 삽입 실패: " + movie.getTitle());
                            errorCount++;
                        }
                    }
                    
                    // 영화 상세 정보 크롤링 및 저장
                    if (!movie.getMovieId().isEmpty()) {
                        log("영화 상세 정보 크롤링 시작: " + movie.getTitle());
                        MovieDetail detail = crawlMovieDetail(movie.getMovieId());
                        
                        // 영화 테이블 업데이트 (장르, 러닝타임 등)
                        if (detail.getGenre() != null && !detail.getGenre().isEmpty()) {
                            String updateMovieSql = "UPDATE movie SET genre = ?, running_time = ? WHERE movie_id = ?";
                            pstmt = conn.prepareStatement(updateMovieSql);
                            pstmt.setString(1, detail.getGenre());
                            pstmt.setString(2, detail.getRunningTime());
                            pstmt.setString(3, movie.getMovieId());
                            pstmt.executeUpdate();
                            log("영화 테이블 추가 정보 업데이트 완료: " + movie.getTitle());
                        }
                        
                        saveMovieDetail(conn, detail);
                    }
                } catch (SQLException e) {
                    log("영화 저장 중 SQL 오류: " + e.getMessage());
                    e.printStackTrace();
                    errorCount++;
                }
            }
            
            log("영화 정보 저장 완료: 새로 삽입 " + insertCount + "개, 업데이트 " + updateCount + "개, 오류 " + errorCount + "개");
            
        } catch (SQLException e) {
            log("영화 정보 저장 중 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
                log("데이터베이스 연결 종료");
            } catch (SQLException e) {
                log("데이터베이스 연결 종료 중 오류: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 필요한 테이블이 존재하는지 확인하고 없으면 생성
     */
    private void checkAndCreateTables(Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            // 테이블이 존재하는지 확인
            DatabaseMetaData dbm = conn.getMetaData();
            
            // movie 테이블 확인
            rs = dbm.getTables(null, null, "movie", null);
            if (!rs.next()) {
                log("movie 테이블이 존재하지 않아 생성합니다.");
                pstmt = conn.prepareStatement(
                    "CREATE TABLE movie (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "movie_id VARCHAR(50) NOT NULL UNIQUE, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "poster_url VARCHAR(500), " +
                    "rating DOUBLE DEFAULT 0, " +
                    "movie_rank INT, " +
                    "release_date VARCHAR(100), " +
                    "genre VARCHAR(100), " +
                    "running_time VARCHAR(50), " +
                    "status VARCHAR(20) DEFAULT 'current', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")"
                );
                pstmt.executeUpdate();
                log("movie 테이블 생성 완료");
            }
            
            // movie_detail 테이블 확인
            rs = dbm.getTables(null, null, "movie_detail", null);
            if (!rs.next()) {
                log("movie_detail 테이블이 존재하지 않아 생성합니다.");
                pstmt = conn.prepareStatement(
                    "CREATE TABLE movie_detail (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "movie_id VARCHAR(50) NOT NULL UNIQUE, " +
                    "english_title VARCHAR(255), " +
                    "director VARCHAR(255), " +
                    "actors TEXT, " +
                    "plot TEXT, " +
                    "rating VARCHAR(50), " +
                    "running_time VARCHAR(50), " +
                    "release_date VARCHAR(100), " +
                    "genre VARCHAR(100), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")"
                );
                pstmt.executeUpdate();
                log("movie_detail 테이블 생성 완료");
                
                // 외래 키 추가
                try {
                    pstmt = conn.prepareStatement(
                        "ALTER TABLE movie_detail " +
                        "ADD CONSTRAINT fk_movie_detail_movie_id " +
                        "FOREIGN KEY (movie_id) REFERENCES movie(movie_id) ON DELETE CASCADE"
                    );
                    pstmt.executeUpdate();
                    log("movie_detail 테이블에 외래 키 추가 완료");
                } catch (SQLException e) {
                    log("movie_detail 테이블 외래 키 추가 중 오류 (이미 존재할 수 있음): " + e.getMessage());
                }
            }
            
            // movie_stillcut 테이블 확인
            rs = dbm.getTables(null, null, "movie_stillcut", null);
            if (!rs.next()) {
                log("movie_stillcut 테이블이 존재하지 않아 생성합니다.");
                pstmt = conn.prepareStatement(
                    "CREATE TABLE movie_stillcut (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "movie_id VARCHAR(50) NOT NULL, " +
                    "image_url VARCHAR(500) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );
                pstmt.executeUpdate();
                log("movie_stillcut 테이블 생성 완료");
                
                // 외래 키 추가
                try {
                    pstmt = conn.prepareStatement(
                        "ALTER TABLE movie_stillcut " +
                        "ADD CONSTRAINT fk_movie_stillcut_movie_id " +
                        "FOREIGN KEY (movie_id) REFERENCES movie(movie_id) ON DELETE CASCADE"
                    );
                    pstmt.executeUpdate();
                    log("movie_stillcut 테이블에 외래 키 추가 완료");
                } catch (SQLException e) {
                    log("movie_stillcut 테이블 외래 키 추가 중 오류 (이미 존재할 수 있음): " + e.getMessage());
                }
            }
            
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }
    
    /**
     * 영화 상세 정보를 데이터베이스에 저장
     */
    private void saveMovieDetail(Connection conn, MovieDetail detail) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            if (detail.getMovieId() == null || detail.getMovieId().isEmpty()) {
                log("영화 ID가 없어 상세 정보 저장을 건너뜁니다.");
                return;
            }
            
            // 영화 상세 정보 저장 또는 업데이트
            String checkSql = "SELECT COUNT(*) FROM movie_detail WHERE movie_id = ?";
            String insertSql = "INSERT INTO movie_detail (movie_id, english_title, director, actors, plot, rating, running_time, release_date, genre) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String updateSql = "UPDATE movie_detail SET english_title = ?, director = ?, actors = ?, plot = ?, rating = ?, running_time = ?, release_date = ?, genre = ? WHERE movie_id = ?";
            
            // 이미 존재하는 영화 상세 정보인지 확인
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, detail.getMovieId());
            rs = pstmt.executeQuery();
            
            boolean exists = false;
            if (rs.next()) {
                exists = rs.getInt(1) > 0;
            }
            
            if (exists) {
                // 업데이트
                log("기존 영화 상세 정보 업데이트: " + detail.getTitle());
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setString(1, detail.getEnglishTitle());
                pstmt.setString(2, detail.getDirector());
                pstmt.setString(3, detail.getActors());
                pstmt.setString(4, detail.getPlot());
                pstmt.setString(5, detail.getRating());
                pstmt.setString(6, detail.getRunningTime());
                pstmt.setString(7, detail.getReleaseDate());
                pstmt.setString(8, detail.getGenre());
                pstmt.setString(9, detail.getMovieId());
                
                int result = pstmt.executeUpdate();
                if (result > 0) {
                    log("영화 상세 정보 업데이트 성공: " + detail.getTitle());
                } else {
                    log("영화 상세 정보 업데이트 실패: " + detail.getTitle());
                }
            } else {
                // 삽입
                log("새 영화 상세 정보 삽입: " + detail.getTitle());
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setString(1, detail.getMovieId());
                pstmt.setString(2, detail.getEnglishTitle());
                pstmt.setString(3, detail.getDirector());
                pstmt.setString(4, detail.getActors());
                pstmt.setString(5, detail.getPlot());
                pstmt.setString(6, detail.getRating());
                pstmt.setString(7, detail.getRunningTime());
                pstmt.setString(8, detail.getReleaseDate());
                pstmt.setString(9, detail.getGenre());
                
                int result = pstmt.executeUpdate();
                if (result > 0) {
                    log("영화 상세 정보 삽입 성공: " + detail.getTitle());
                } else {
                    log("영화 상세 정보 삽입 실패: " + detail.getTitle());
                }
            }
            
            // 스틸컷 이미지 저장
            if (detail.getStillcuts() != null && !detail.getStillcuts().isEmpty()) {
                String deleteSql = "DELETE FROM movie_stillcut WHERE movie_id = ?";
                String insertStillcutSql = "INSERT INTO movie_stillcut (movie_id, image_url) VALUES (?, ?)";
                
                // 기존 스틸컷 삭제
                pstmt = conn.prepareStatement(deleteSql);
                pstmt.setString(1, detail.getMovieId());
                int deleteResult = pstmt.executeUpdate();
                log("기존 스틸컷 삭제: " + deleteResult + "개");
                
                // 새 스틸컷 추가
                int stillcutCount = 0;
                pstmt = conn.prepareStatement(insertStillcutSql);
                for (String imageUrl : detail.getStillcuts()) {
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        pstmt.setString(1, detail.getMovieId());
                        pstmt.setString(2, imageUrl);
                        pstmt.executeUpdate();
                        stillcutCount++;
                    }
                }
                log("새 스틸컷 추가 완료: " + stillcutCount + "개");
            }
            
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }
    
    /**
     * 모든 영화 정보 크롤링 및 저장 실행
     */
    public void crawlAndSaveAllMovies() {
        log("영화 크롤링 프로세스 시작...");
        
        try {
            // 영화 테이블 초기화 (선택적)
            boolean resetTables = true; // 이 값을 파라미터로 받거나 설정에서 관리할 수 있습니다
            
            if (resetTables) {
                log("영화 테이블 초기화 시작...");
                MovieDAO movieDAO = new MovieDAO();
                movieDAO.resetMovieTables();
                log("영화 테이블 초기화 완료");
            }
            
        // 테이블이 존재하는지 확인하고 없으면 생성
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            checkAndCreateTables(conn);
        } finally {
            if (conn != null) conn.close();
        }
        
            List<Movie> currentMovies = crawlCurrentMovies();
            log("현재 상영작 크롤링 완료: " + currentMovies.size() + "개 영화");
            
            List<Movie> comingSoonMovies = crawlComingSoonMovies();
            log("상영 예정작 크롤링 완료: " + comingSoonMovies.size() + "개 영화");
            
            if (!currentMovies.isEmpty()) {
                log("현재 상영작 데이터베이스 저장 시작...");
                saveMoviesToDatabase(currentMovies);
            }
            
            if (!comingSoonMovies.isEmpty()) {
                log("상영 예정작 데이터베이스 저장 시작...");
                saveMoviesToDatabase(comingSoonMovies);
            }
            
            log("모든 영화 크롤링 및 저장 완료!");
        } catch (Exception e) {
            log("크롤링 및 저장 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
