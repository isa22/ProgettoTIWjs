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
import beans.Comment;
import beans.Image;
import dao.AlbumDAO;
import dao.CommentDAO;
import dao.ImageDAO;
import utils.ConnectionHandler;

@WebServlet("/Album")
public class GetAlbum extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public GetAlbum() {
		super();
	}
	
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		
		//DAO, list of images beans and requested album bean declarations 
		ImageDAO imageDAO = new ImageDAO(connection);
		List<Image> images = new ArrayList<Image>();
		AlbumDAO albumDAO = new AlbumDAO(connection);
		Album album = null;
		
		//Get http request parameters and retrieve data for pagination from DB
		int albumId;
		try {
			albumId = Integer.parseInt(request.getParameter("albumId"));
		} catch(NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("Richiesta non valida");
			System.out.println("Server Error: request parameter albumId hasn't been parsed correctly");
			return;
		}
		//Retrieve images from DB 
		try {
			images = imageDAO.findImagesByAlbum(albumId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("Impossibile recuperare le immagini dell'album");
			System.out.println("Server Error: SQLException thrown by imageDAO.findImagesByAlbum");
			return;
		}
		try {
			album = albumDAO.getTitleOfAlbum(albumId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("Impossibile recuperare le immagini dell'album");
			System.out.println("Server Error: SQLException thrown by albumDAO.getTitleOfAlbum");
			return;
		}
		
		//Prepare response with the requested Album.html page
		CommentDAO commentDao = new CommentDAO(connection);
		
		try {
			for(Image img : images) {
				List<Comment> comments = commentDao.findCommentsByImage(img.getId());
				img.setComments(comments);
			}
		}
		catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("Impossibile recuperare i commenti");
			System.out.println("Server Error: SQLException thrown by commentDao.findCommentsByImage");
			return;
		}
		if(album !=null)
			album.setImages(images);
		else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("L'album richiesto non esiste");
			System.out.println("Server Error: SQLException thrown by albumDAO.getTitleOfAlbum");
			return;
		}
		
		//container for album data to group data in one json
		Gson gson = new GsonBuilder()
				   .setDateFormat("yyyy MMM dd").create();
		String json = gson.toJson(album);
		
		
		response.setStatus(HttpServletResponse.SC_OK);
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
