package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;

import DB.DBConnection;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String remember = request.getParameter("remember");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM user WHERE username = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password); // 실제 구현에서는 비밀번호 해싱 필요
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // 로그인 성공
                HttpSession session = request.getSession();
                session.setAttribute("username", username);
                session.setAttribute("userId", rs.getInt("id"));
                session.setAttribute("nickname", rs.getString("nickname"));
                session.setAttribute("userRole", rs.getString("role")); // role 정보 추가
                
                // 로그인 상태 유지 처리
                if (remember != null) {
                    session.setMaxInactiveInterval(30 * 24 * 60 * 60); // 30일
                    
                    // 아이디 저장을 위한 쿠키 생성
                    Cookie usernameCookie = new Cookie("savedUsername", username);
                    usernameCookie.setMaxAge(30 * 24 * 60 * 60); // 30일
                    usernameCookie.setPath("/");
                    response.addCookie(usernameCookie);
                } else {
                    // 아이디 저장을 해제한 경우 기존 쿠키 삭제
                    Cookie usernameCookie = new Cookie("savedUsername", "");
                    usernameCookie.setMaxAge(0); // 쿠키 삭제
                    usernameCookie.setPath("/");
                    response.addCookie(usernameCookie);
                }
                
                // 메인 페이지로 리다이렉트 (Profile.jsp 대신 index.jsp로 변경)
                response.sendRedirect("index.jsp");
            } else {
                // 로그인 실패
                request.setAttribute("errorMessage", "아이디 또는 비밀번호가 일치하지 않습니다.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            // 사용자에게 보여줄 오류 메시지 설정
            request.setAttribute("errorMessage", "데이터베이스 연결 오류: " + e.getMessage());
            request.getRequestDispatcher("login.jsp").forward(request, response);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
