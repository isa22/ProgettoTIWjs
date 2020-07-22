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
		StringBuffer jb = new StringBuffer();
		String line = null;
        JsonObject data = null;
        //Putting the request in a string
		try {
		    BufferedReader reader = request.getReader();
		    while ((line = reader.readLine()) != null)
		      jb.append(line);
		  } catch (Exception e) { 
			  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			  response.getWriter().write("Errore interno al server");
		  }
		data =  JsonParser.parseString(jb.toString()).getAsJsonObject();  
		User userBean = (User) session.getAttribute("user");
		int userId = (int) userBean.getId();
		JsonElement newOrderArray = data.get("newOrder");
		Type listType = new TypeToken<ArrayList<Integer>>() {}.getType();
		List<Integer> orderList = new Gson().fromJson(newOrderArray, listType);
		AlbumOrderDAO orderDao;
		try {
			orderDao = new AlbumOrderDAO(connection);
			orderDao.changeAlbumOrder(userId, orderList);
		}
		catch(SQLException e) {
			  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			  response.getWriter().write("Errore interno al server");
		}
		//set new album order in user session
		AlbumOrder newOrderBean = new AlbumOrder();
		newOrderBean.setOrder(orderList); 
		userBean.setAlbumOrder(newOrderBean);
		session.setAttribute("user", userBean);
		
	}

}
