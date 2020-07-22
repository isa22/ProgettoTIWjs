package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import beans.AlbumOrder;
import beans.User;

import org.json.HTTP;
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
       
    
    public ChangeAlbumOrder() {
        super();
    }

    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		JSONObject resp = new JSONObject();
		StringBuffer jb = new StringBuffer();
		String line = null;
        JSONObject data = null;
        //Putting the request in a string
		try {
		    BufferedReader reader = request.getReader();
		    while ((line = reader.readLine()) != null)
		      jb.append(line);
		  } catch (Exception e) { /*report an error*/ }

		  try {
		    data =  HTTP.toJSONObject(jb.toString());
		  } catch (JSONException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno al server");
		  }
		//Parsing the request string in a JsonObject
		User userBean = (User) session.getAttribute("user");
		int userId = (int) userBean.getId();
		String orderString = (String) data.get("Method");
		JsonObject newOrder  = JsonParser.parseString(orderString).getAsJsonObject();
		JsonElement newOrderArray = newOrder.get("newOrder");
		Type listType = new TypeToken<ArrayList<Integer>>() {}.getType();
		List<Integer> orderList = new Gson().fromJson(newOrderArray, listType);
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
