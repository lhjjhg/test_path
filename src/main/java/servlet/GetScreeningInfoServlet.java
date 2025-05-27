package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import DB.DBConnection;

@WebServlet("/GetScreeningInfoServlet")
public class GetScreeningInfoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String movieId = request.getParameter("movieId");
        String theaterId = request.getParameter("theaterId");
        String date = request.getParameter("date");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // 상영 정보 조회 쿼리
            String sql = "SELECT id, available_seats FROM screening " +
                         "WHERE movie_id = ? AND theater_id = ? AND screening_date = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, movieId);
            pstmt.setString(2, theaterId);
            pstmt.setString(3, date);
            
            rs = pstmt.executeQuery();
            
            JSONArray screeningsArray = new JSONArray();
            
            while (rs.next()) {
                JSONObject screeningObj = new JSONObject();
                screeningObj.put("id", rs.getInt("id"));
                screeningObj.put("availableSeats", rs.getInt("available_seats"));
                screeningsArray.add(screeningObj);
            }
            
            JSONObject resultObj = new JSONObject();
            resultObj.put("success", true);
            resultObj.put("screenings", screeningsArray);
            
            out.print(resultObj.toJSONString());
            
        } catch (SQLException e) {
            e.printStackTrace();
            
            JSONObject errorObj = new JSONObject();
            errorObj.put("success", false);
            errorObj.put("message", e.getMessage());
            
            out.print(errorObj.toJSONString());
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
