package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import crawler.MovieCrawler;

@WebServlet("/admin/crawl-movies")
public class MovieCrawlerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>영화 크롤링 진행 중...</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; line-height: 1.6; padding: 20px; background-color: #f4f4f4; }");
        out.println(".container { max-width: 800px; margin: 0 auto; background: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        out.println("h1 { color: #333; }");
        out.println("pre { background: #f9f9f9; padding: 10px; border-radius: 5px; overflow-x: auto; white-space: pre-wrap; }");
        out.println(".log { background: #f0f0f0; padding: 15px; border-radius: 5px; max-height: 400px; overflow-y: auto; margin-top: 20px; }");
        out.println(".success { color: green; }");
        out.println(".error { color: red; }");
        out.println("a { display: inline-block; margin-top: 20px; background: #4CAF50; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>영화 정보 크롤링 진행 중...</h1>");
        out.println("<p>크롤링이 완료될 때까지 기다려주세요. 이 과정은 몇 분 정도 소요될 수 있습니다.</p>");
        
        out.println("<div class='log' id='log'>");
        // 로그 내용이 여기에 출력될 것입니다
        out.println("</div>");
        
        // 응답 버퍼를 비우고 클라이언트에게 전송
        out.flush();
        
        try {
            // 크롤링 작업 시작
            MovieCrawler crawler = new MovieCrawler();
            crawler.crawlAndSaveAllMovies();
            
            // 결과 출력
            out.println("<h2 class='success'>크롤링 완료!</h2>");
            out.println("<div class='log'>");
            out.println(crawler.getLog());
            out.println("</div>");
            
            out.println("<a href='../index.jsp'>메인 페이지로 이동</a>");
        } catch (Exception e) {
            out.println("<h2 class='error'>크롤링 중 오류 발생</h2>");
            out.println("<p class='error'>오류 메시지: " + e.getMessage() + "</p>");
            
            out.println("<pre>");
            e.printStackTrace(new PrintWriter(out));
            out.println("</pre>");
            
            out.println("<a href='../index.jsp'>메인 페이지로 이동</a>");
        }
        
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}
