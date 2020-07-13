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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import beans.Album;
import beans.Comment;
import beans.Image;
import dao.AlbumDAO;
import dao.ImageDAO;
import utils.ConnectionHandler;

@WebServlet("/Album")
public class GoToAlbumPage extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;
	private final int imagesPerPage = 5;
	
	public GoToAlbumPage() {
		super();
	}
	
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		//DAO, list of images beans and requested album bean declarations 
		ImageDAO imageDAO = new ImageDAO(connection);
		List<Image> images = new ArrayList<Image>();
		AlbumDAO albumDAO = new AlbumDAO(connection);
		Album album;
		
		//Get http request parameters and retrieve data for pagination from DB
		int albumId;
		try {albumId = Integer.parseInt(request.getParameter("albumId"));}catch(NumberFormatException e) {
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
		String path = "/WEB-INF/Album.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		//set response variables
		ctx.setVariable("images", images); 		 								//list of page images
		ctx.setVariable("nextPage", limitPageInsideRange(page+1,totPages));	 	//next page
		ctx.setVariable("currentPage", page);	 								//current page
		ctx.setVariable("previousPage", limitPageInsideRange(page-1,totPages)); //previous page
		ctx.setVariable("isFirstPage", page==1?true:false);						//true if the requested page is the first of the album
		ctx.setVariable("isLastPage", page==totPages?true:false);				//true if the requested page is the last of the album
		ctx.setVariable("albumTitle", album.getTitle());						//requested album title
		ctx.setVariable("albumId", albumId);								//requested album id
		
		Image focusedImage = (Image) request.getAttribute("image");
		List<Comment> comments = (List<Comment>) request.getAttribute("comments");
		
		if(focusedImage!=null) ctx.setVariable("focusedImage", focusedImage);	//forwarded parameter from GetDescriptionByImage servlet
		if(comments!=null) ctx.setVariable("comments", comments);				//forwarded parameter from GetDescriptionByImage servlet
		
		//process Album.html page with thymeleaf
		templateEngine.process(path, ctx, response.getWriter());
		
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
