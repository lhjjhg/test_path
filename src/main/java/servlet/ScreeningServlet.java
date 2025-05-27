package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import DB.DBConnection;

@WebServlet("/ScreeningServlet")
public class ScreeningServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String movieId = request.getParameter("movieId");
        String theaterId = request.getParameter("theaterId");
        String date = request.getParameter("date");
        
        if (movieId == null || theaterId == null || date == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            conn = DBConnection.getConnection();
            
            // 상영 정보 조회
            String sql = "SELECT s.id, s.screening_time, s.available_seats, " +
                        "sc.id AS screen_id, sc.name AS screen_name, sc.seats_total " +
                        "FROM screening s " +
                        "JOIN screen sc ON s.screen_id = sc.id " +
                        "WHERE s.movie_id = ? AND sc.theater_id = ? AND s.screening_date = ? " +
                        "ORDER BY s.screening_time";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, movieId);
            pstmt.setString(2, theaterId);
            pstmt.setString(3, date);
            rs = pstmt.executeQuery();
            
            // JSON 응답 생성
            JSONArray screeningsArray = new JSONArray();
            
            Map<String, List<JSONObject>> screeningsByScreen = new HashMap<>();
            
            while (rs.next()) {
                int screeningId = rs.getInt("id");
                String screeningTime = rs.getString("screening_time");
                int availableSeats = rs.getInt("available_seats");
                int screenId = rs.getInt("screen_id");
                String screenName = rs.getString("screen_name");
                int seatsTotal = rs.getInt("seats_total");
                
                JSONObject screeningObj = new JSONObject();
                screeningObj.put("id", screeningId);
                screeningObj.put("time", screeningTime);
                screeningObj.put("availableSeats", availableSeats);
                screeningObj.put("seatsTotal", seatsTotal);
                
                // 상영관별로 그룹화
                if (!screeningsByScreen.containsKey(screenName)) {
                    screeningsByScreen.put(screenName, new ArrayList<>());
                }
                screeningsByScreen.get(screenName).add(screeningObj);
            }
            
            // 상영관별로 JSON 배열 생성
            for (Map.Entry<String, List<JSONObject>> entry : screeningsByScreen.entrySet()) {
                JSONObject screenObj = new JSONObject();
                screenObj.put("screenName", entry.getKey());
                screenObj.put("screenings", entry.getValue());
                screeningsArray.add(screenObj);
            }
            
            JSONObject resultObj = new JSONObject();
            resultObj.put("success", true);
            resultObj.put("screens", screeningsArray);
            
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
