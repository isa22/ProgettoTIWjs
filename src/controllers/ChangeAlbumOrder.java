package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.mysql.cj.xdevapi.JsonArray;
import com.mysql.cj.xdevapi.JsonParser;

import beans.AlbumOrder;
import beans.User;

import org.json.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dao.AlbumOrderDAO;
import utils.ConnectionHandler;

/**
 * Servlet implementation class ChangeAlbumOrder
 */
@WebServlet("/ChangeAlbumOrder")
@MultipartConfig
public class ChangeAlbumOrder extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ChangeAlbumOrder() {
        super();
    }

    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
		JSONObject resp = new JSONObject();
		if(session == null) {
			
			resp.append("redirect","/Login");
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(resp.toString());
			return;
		}
		//JSONObject data = new JSONObject(request.getReader().readLine());
		
		 StringBuffer jb = new StringBuffer();
		 String line = null;
         JSONObject data;
		  try {
		    BufferedReader reader = request.getReader();
		    while ((line = reader.readLine()) != null)
		      jb.append(line);
		  } catch (Exception e) { /*report an error*/ }

		  try {
		    data =  HTTP.toJSONObject(jb.toString());
		  } catch (JSONException e) {
		    // crash and burn
		    throw new IOException("Error parsing JSON request string");
		  }
		
		User userBean = (User) session.getAttribute("user");
		int userId = (int) userBean.getId();
		System.out.println(data);
		JSONArray newOrder = data.getJSONArray("newOrder");
		List orderList = newOrder.toList();
		AlbumOrderDAO orderDao;
		try {
			orderDao = new AlbumOrderDAO(connection);
			orderDao.changeAlbumOrder(userId, orderList);
		}
		catch(SQLException e) {
			resp.append("error", "Update failed");
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(resp.toString());
		}
		
		//set new album order in user session
		AlbumOrder newOrderBean = new AlbumOrder();
		newOrderBean.setOrder(orderList); 
		userBean.setAlbumOrder(newOrderBean);
		session.setAttribute("user", userBean);
		
	}

}
