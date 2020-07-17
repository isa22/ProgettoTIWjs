package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;

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
		// TODO Auto-generated method stub
		String path = "Login.html";
		JSONObject json = new JSONObject();
		json.append("redirect", path);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json.toString());
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
		
		System.out.println("New user credentials: \n username: " + usrn + "\n password1:" + pwd1 + "\n password2:" + pwd2 + "\n email:" + email);
		
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
			response.getWriter().println("Errore interno, riprova più tardi");
			System.out.println("Server Error: SQLException thrown by userDAO.isPresent");
			return;
		}
		if (isPresent) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Email o user già registrati");
			System.out.println("Server Error: request parameter user or email are already present in the DB");
			
		}
		//create a new user and a new album order for the new user
		else {
			User user;
			try {
				user = userDao.insertUser(usrn, pwd1, email);
			}
			catch(SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Errore interno, riprova più tardi");
				System.out.println("Server Error: SQLException thrown by userDAO.insertUser");
				return;
			}
			try {
				AlbumOrderDAO albumOrder = new AlbumOrderDAO(connection);
				AlbumDAO album = new AlbumDAO(connection);
				albumOrder.createAlbumOrder(user.getId(), album.getNumberOfAlbum());
			}
			catch(SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Errore interno, riprova più tardi");
				System.out.println("Server Error: SQLException thrown by albumOrderDAO.createAlbumOrder");
				return;
			}
			
			
			request.getSession().setAttribute("user", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			
			//forward the request toward goToHomePage servlet
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/Home");
	        dispatcher.forward(request, response);
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
