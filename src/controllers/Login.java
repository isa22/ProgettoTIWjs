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
  
       
    /**
     * @see HttpServlet#HttpServlet()
     */
  
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String path = "Login.html";
		JSONObject json = new JSONObject();
		json.append("redirect", path);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    

	
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
		try {
			user = userDao.authenticateUser(usrn, pwd);
		}
		catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore interno, riprova più tardi");
			System.out.println("Server Error: SQLException thrown by userDAO.authenticateUser");
			return;
		}
		if(user== null) {
			//TODO credo che in caso di mancato match il db ritorni semplicemente zero risultati e non un'eccezione.
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
		}
		
			
		//forward the request toward goToHomePage servlet
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/Home");
	    dispatcher.forward(request, response);
		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
