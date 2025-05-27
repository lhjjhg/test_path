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
import java.sql.SQLException;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import DB.DBConnection;
import dao.UserDAO;
import dto.User;

@WebServlet("/ProfileUpdateServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1MB
    maxFileSize = 1024 * 1024 * 10,  // 10MB
    maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class ProfileUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // 이미지 저장 경로
    private static final String UPLOAD_DIR = "image/profile";
       
    public ProfileUpdateServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        
        // 세션에서 사용자 정보 가져오기
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/member/login.jsp");
            return;
        }
        
        try {
            UserDAO userDAO = new UserDAO();
            User user = null;
            
            try {
                user = userDAO.getUserById(userId);
            } catch (SQLException e) {
                e.printStackTrace();
                request.setAttribute("errorMsg", "사용자 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
                request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                return;
            }
            
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/member/login.jsp");
                return;
            }
            
            // 폼 데이터 가져오기
            String password = request.getParameter("password");
            String name = request.getParameter("name");
            String nickname = request.getParameter("nickname");
            String address = request.getParameter("address");
            String birthdate = request.getParameter("birthdate");
            
            // 사용자 정보 업데이트
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(password);
            }
            
            user.setName(name);
            user.setNickname(nickname);
            user.setAddress(address);
            user.setBirthdate(birthdate);
            
            // 프로필 이미지 처리
            Part filePart = request.getPart("profileImage");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = getSubmittedFileName(filePart);
                
                // 파일 확장자 확인
                String fileExtension = fileName.substring(fileName.lastIndexOf("."));
                if (!isImageFile(fileExtension)) {
                    request.setAttribute("errorMsg", "이미지 파일만 업로드 가능합니다 (jpg, jpeg, png, gif)");
                    request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                    return;
                }
                
                // 고유한 파일명 생성
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                
                // 업로드 디렉토리 경로
                String applicationPath = request.getServletContext().getRealPath("");
                String uploadPath = applicationPath + File.separator + UPLOAD_DIR;
                
                // 디렉토리가 없으면 생성
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                
                // 파일 저장
                String filePath = uploadPath + File.separator + uniqueFileName;
                filePart.write(filePath);
                
                // 사용자 프로필 이미지 경로 업데이트
                user.setProfileImage(UPLOAD_DIR + "/" + uniqueFileName);
            }
            
            // 데이터베이스에 사용자 정보 업데이트
            boolean updated = false;
            try {
                updated = userDAO.updateUser(user);
            } catch (SQLException e) {
                e.printStackTrace();
                request.setAttribute("errorMsg", "프로필 업데이트 중 오류가 발생했습니다: " + e.getMessage());
                request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                return;
            }
            
            if (updated) {
                // 세션 정보 업데이트
                session.setAttribute("nickname", user.getNickname());
                
                // 성공 메시지 설정 (세션에 저장)
                session.setAttribute("successMsg", "프로필이 성공적으로 업데이트되었습니다.");
                
                // 프로필 수정 페이지로 리다이렉트
                response.sendRedirect(request.getContextPath() + "/member/edit-profile.jsp");
            } else {
                request.setAttribute("errorMsg", "프로필 업데이트에 실패했습니다. 다시 시도해주세요.");
                request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "예기치 않은 오류가 발생했습니다: " + e.getMessage());
            request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
        }
    }
    
    // Part에서 파일명 추출
    private String getSubmittedFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        
        for (String item : items) {
            if (item.trim().startsWith("filename")) {
                return item.substring(item.indexOf("=") + 2, item.length() - 1);
            }
        }
        return "";
    }
    
    // 이미지 파일 확장자 확인
    private boolean isImageFile(String fileExtension) {
        fileExtension = fileExtension.toLowerCase();
        return fileExtension.equals(".jpg") || fileExtension.equals(".jpeg") || 
               fileExtension.equals(".png") || fileExtension.equals(".gif");
    }
}
