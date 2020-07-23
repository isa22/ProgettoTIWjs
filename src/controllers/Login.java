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
import utils.ConnectionHandler;
import dao.AlbumOrderDAO;
import dao.UserDAO;
import beans.AlbumOrder;
import beans.User;


@WebServlet("/Login")
@MultipartConfig
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
  
    public Login() {
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
		String usrn = null;
		String pwd = null;
		usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
		pwd = StringEscapeUtils.escapeJava(request.getParameter("password"));
		if (usrn == null || pwd == null || usrn.isEmpty() || pwd.isEmpty() ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Richiesta non valida");
			System.out.println("Server Error: request parameters user or password are null");
			return;
		}
		UserDAO userDao = new UserDAO(connection);
		User user = null;

		MessageDigest digest = null;
		byte[] hash = pwd.getBytes();
		//hashing of the password
		try {
			digest = MessageDigest.getInstance("SHA-256");
			hash = digest.digest(pwd.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		try {
			user = userDao.authenticateUser(usrn, Arrays.toString(hash));
		}
		catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore interno, riprova più tardi");
			System.out.println("Server Error: SQLException thrown by userDAO.authenticateUser");
			return;
		}
		//user not found or wrong password
		if(user== null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("User o password non validi");
			System.out.println("User or password not valid");
			return;
		}
		else {
			AlbumOrderDAO orderDao = new AlbumOrderDAO(connection);
			AlbumOrder order = null;
			try {
				order = orderDao.getAlbumOrder(user.getId());
			}
			catch(SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Errore interno, riprova più tardi");
				System.out.println("Server Error: SQLException thrown by AlbumOrderDao.getAlbumOrder");
				return;
			}
			if(order!=null) {
				user.setAlbumOrder(order);
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
