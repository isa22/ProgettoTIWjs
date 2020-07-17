package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

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
	private static final JSONObject resp  = null;
	private Connection connection = null;
	private final int imagesPerPage = 5;
	
	public GetAlbum() {
		super();
	}
	
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		if(session == null) {
			
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
		}
		
		//DAO, list of images beans and requested album bean declarations 
		ImageDAO imageDAO = new ImageDAO(connection);
		List<Image> images = new ArrayList<Image>();
		AlbumDAO albumDAO = new AlbumDAO(connection);
		Album album;
		
		//Get http request parameters and retrieve data for pagination from DB
		int albumId;
		try {
			albumId = Integer.parseInt(request.getParameter("albumId"));
		} catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Richiesta non valida");
			System.out.println("Server Error: request parameter albumId hasn't been parsed correctly");
			return;
		}
		int totImages;
		try {
			totImages = imageDAO.getNumberOfImagesByAlbum(albumId); 
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile recuperare le immagini dell'album");
			System.out.println("Server Error: SQLException thrown by imageDAO.getNumberOfImagesByAlbum");
			return;
		}
		int totPages = (int) Math.ceil((double)totImages/imagesPerPage);
		int page; 
		try{page = limitPageInsideRange(Integer.parseInt(request.getParameter("page")),totPages);}catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Richiesta non valida");
			System.out.println("Server Error: request parameter page hasn't been parsed correctly");
			return;
		}
		
		//Retrieve images from DB 
		try {
			//TODO change imageDAO parameters
			images = imageDAO.findImagesByAlbum(albumId,page);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile recuperare le immagini dell'album");
			System.out.println("Server Error: SQLException thrown by imageDAO.findImagesByAlbum");
			return;
		}
		try {
			album = albumDAO.getTitleOfAlbum(albumId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile recuperare le immagini dell'album");
			System.out.println("Server Error: SQLException thrown by albumDAO.getTitleOfAlbum");
			return;
		}
		
		//Prepare response with the requested Album.html page
		CommentDAO commentDao = new CommentDAO(connection);
		
		Image focusedImage = (Image) request.getAttribute("image");
		try {
			for(Image img : images) {
				List<Comment> comments = commentDao.findCommentsByImage(img.getId());
				img.setComments(comments);
			}
		}
		catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile recuperare i commenti");
			System.out.println("Server Error: SQLException thrown by commentDao.findCommentsByImage");
			return;
		}
		
		resp.append("images", images);
		resp.append("albumTitle", album.getTitle());
		resp.append("albumId", album.getId());
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(resp.toString());
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
	
	/**This method returns the page number inside the range of possible pages*/ 
	private int limitPageInsideRange(int page, int totPages) {
		if(page<1) return 1;
		if(page > totPages) return totPages;
		return page;
	}
}
