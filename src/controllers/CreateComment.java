package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import beans.Comment;
import beans.User;
import dao.CommentDAO;
import utils.ConnectionHandler;

@WebServlet("/CreateComment")
@MultipartConfig
public class CreateComment extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	public CreateComment() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		User user = (User)session.getAttribute("user");
		// Get and parse all parameters from request
		Boolean isBadRequest = null;
		String text = null;
		int imageId = 0;
		try {
			text = StringEscapeUtils.escapeJava(request.getParameter("comment"));
			imageId = Integer.parseInt(request.getParameter("imageId"));
			isBadRequest = text.isEmpty();
		} catch (NullPointerException | NumberFormatException e) {
			isBadRequest = true;
			System.out.println("Server error: request prameters imageId or text haven't been parsed correctly");
			e.printStackTrace();
		}
		if (isBadRequest) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Richiesta non valida");
			if(text.isEmpty()) System.out.println("Server error: empty comment text");
			return;
		}

		int userId = user.getId();
		// Create comment in DB
		CommentDAO commentDAO = new CommentDAO(connection);
		try {
			commentDAO.createComment(userId,imageId,text);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Il commento non può essere creato");
			System.out.println("Server error: SQLException thrown by commentDAO.createComment");
			return;
		}

		List<Comment> comments = new ArrayList<Comment>();
		try {
			comments = commentDAO.findCommentsByImage(imageId);
		}
		catch(SQLException e) {
			
		}
		// return the user to the right view
		Gson gson = new GsonBuilder()
				   .setDateFormat("yyyy MMM dd").create();
		String json = gson.toJson(comments);
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

}
