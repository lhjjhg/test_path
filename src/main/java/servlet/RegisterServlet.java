package servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import DB.DBConnection;

@WebServlet("/RegisterServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1MB
    maxFileSize = 1024 * 1024 * 10,  // 10MB
    maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String nickname = request.getParameter("nickname");
        String fullAddress = request.getParameter("fullAddress");
        String birthdate = request.getParameter("birthdate");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // 아이디 중복 확인
            String checkSql = "SELECT COUNT(*) FROM user WHERE username = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                request.setAttribute("errorMessage", "이미 사용 중인 아이디입니다.");
                request.getRequestDispatcher("register.jsp").forward(request, response);
                return;
            }
            
            // 프로필 이미지 처리
            String imagePath = "image/default-profile.png"; // 기본 이미지 경로
            Part filePart = request.getPart("profileImage");
            
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = username + "_" + System.currentTimeMillis() + getFileExtension(filePart);
                String uploadDir = getServletContext().getRealPath("/image");
                
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
                
                Path path = Paths.get(uploadDir + File.separator + fileName);
                try (InputStream input = filePart.getInputStream()) {
                    Files.copy(input, path, StandardCopyOption.REPLACE_EXISTING);
                }
                
                imagePath = "image/" + fileName;
            }
            
            // 회원 정보 저장
            String insertSql = "INSERT INTO user (username, password, name, nickname, address, birthdate, profile_image) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setString(1, username);
            pstmt.setString(2, password); // 실제 구현에서는 비밀번호 해싱 필요
            pstmt.setString(3, name);
            pstmt.setString(4, nickname);
            pstmt.setString(5, fullAddress);
            pstmt.setString(6, birthdate);
            pstmt.setString(7, imagePath);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 회원가입 성공
                response.sendRedirect("login.jsp?registered=true");
            } else {
                // 회원가입 실패
                request.setAttribute("errorMessage", "회원가입에 실패했습니다. 다시 시도해주세요.");
                request.getRequestDispatcher("register.jsp").forward(request, response);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "데이터베이스 오류가 발생했습니다: " + e.getMessage());
            request.getRequestDispatcher("register.jsp").forward(request, response);
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
    
    private String getFileExtension(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        
        for (String item : items) {
            if (item.trim().startsWith("filename")) {
                String fileName = item.substring(item.indexOf("=") + 2, item.length() - 1);
                int dotIndex = fileName.lastIndexOf(".");
                if (dotIndex > 0) {
                    return fileName.substring(dotIndex);
                }
            }
        }
        return "";
    }
}
