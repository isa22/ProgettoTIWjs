package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import beans.Album;
import beans.AlbumOrder;
import beans.User;
import dao.AlbumDAO;
import utils.ConnectionHandler;

@WebServlet("/Home")
public class GoToHomePage extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public GoToHomePage() {
		super();
	}
	
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		
		HttpSession session = request.getSession();

		AlbumDAO albumDAO = new AlbumDAO(connection);
		List<Album> albums = new ArrayList<Album>();
		
		//retrieve the albums from DB
		try {
			albums = albumDAO.findAll();
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Richiesta non valida");
			System.out.println("Server Error: SQLException thrown by albumDAO.findAll");
			return;
		}
		User user = (User) session.getAttribute("user");
		AlbumOrder albumOrder = user.getAlbumOrder();
		List<Album> orderedAlbums = new ArrayList<Album>();
		if (albumOrder !=null) {
			for(Integer n : albumOrder.getOrder()) {
				Album toRemove=null;
				for(Album alb : albums) {
					if(alb.getId() == n) {
						orderedAlbums.add(alb);
						toRemove = alb;
					}
				}
				if(toRemove !=null)
					albums.remove(toRemove);
			}
		}
		else {
			orderedAlbums = albums;
		}
		
		Gson gson = new GsonBuilder()
				   .setDateFormat("yyyy MMM dd").create();
		String json = gson.toJson(orderedAlbums);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	

}
