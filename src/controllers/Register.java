package controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.tribes.util.Arrays;
import org.apache.commons.lang.StringEscapeUtils;

import beans.User;
import dao.AlbumDAO;
import dao.AlbumOrderDAO;
import dao.UserDAO;
import utils.ConnectionHandler;


@WebServlet("/Register")
@MultipartConfig
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Connection connection = null;
      
       
    public Register() {
        super();
    }
    
    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String loginpath = request.getServletContext().getContextPath() + "/Login.html";
		response.sendRedirect(loginpath);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//retrieve request parameters
		String usrn = null;
		String pwd1 = null;
		String pwd2 = null;
		String email = null;
		usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
		pwd1 = StringEscapeUtils.escapeJava(request.getParameter("password1"));
		pwd2 = StringEscapeUtils.escapeJava(request.getParameter("password2"));
		email = StringEscapeUtils.escapeJava(request.getParameter("email"));
		
		//check if parameters are empty
		if (email == null || pwd1 == null || usrn == null || pwd2 == null || usrn.isEmpty() || pwd1.isEmpty() || email.isEmpty() || pwd2.isEmpty() ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Le credenziali non possono essere nulle");
			System.out.println("Server Error: request parameter username, password1, password2 or email haven't been parsed correctly");
			return;
		}
		//check if the passwords match
		if(!pwd1.equals(pwd2)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Le due password sono diverse");
			System.out.println("Server Error: request parameter password1 and password2 do not match");
			return;
		}
		//check if user is already present in the DB
		UserDAO userDao = new UserDAO(connection);
		boolean isPresent;
		try {
			isPresent = userDao.isPresent(usrn, email);
		}
		catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore interno, riprova pi� tardi");
			System.out.println("Server Error: SQLException thrown by userDAO.isPresent");
			return;
		}
		if (isPresent) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Email o user gi� registrati");
			System.out.println("Server Error: request parameter user or email are already present in the DB");
			
		}
		//create a new user and a new album order for the new user
		else {
			User user;
			MessageDigest digest = null;
			byte[] hash = pwd1.getBytes();
			//hashing of the password
			try {
				digest = MessageDigest.getInstance("SHA-256");
				hash = digest.digest(pwd1.getBytes(StandardCharsets.UTF_8));
			} catch (NoSuchAlgorithmException e1) {
				e1.printStackTrace();
			}
			
			try {
				user = userDao.insertUser(usrn, Arrays.toString(hash), email);
			}
			catch(SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Errore interno, riprova pi� tardi");
				System.out.println("Server Error: SQLException thrown by userDAO.insertUser");
				return;
			}
			try {
				AlbumOrderDAO albumOrder = new AlbumOrderDAO(connection);
				AlbumDAO album = new AlbumDAO(connection);
				albumOrder.createAlbumOrder(user.getId(), album.getAlbumIds());
			}
			catch(SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Errore interno, riprova pi� tardi");
				System.out.println("Server Error: SQLException thrown by albumOrderDAO.createAlbumOrder");
				return;
			}
			
			
			request.getSession().setAttribute("user", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(user.getUsername());
			
		}
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
