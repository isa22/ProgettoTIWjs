package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import beans.User;
import dao.UserDAO;
import utils.ConnectionHandler;

/**
 * Servlet implementation class Register
 */
@WebServlet("/Register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private TemplateEngine templateEngine;
	private Connection connection = null;
      
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
        super();
        // TODO Auto-generated constructor stub
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
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		String path = "/WEB-INF/Login.html";
		templateEngine.process(path, ctx, response.getWriter());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String usrn = null;
		String pwd1 = null;
		String pwd2 = null;
		String email = null;
		usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
		pwd1 = StringEscapeUtils.escapeJava(request.getParameter("password1"));
		pwd2 = StringEscapeUtils.escapeJava(request.getParameter("password2"));
		email = StringEscapeUtils.escapeJava(request.getParameter("email"));
		if (email == null || pwd1 == null || usrn == null || pwd2 == null || usrn.isEmpty() || pwd1.isEmpty() || email.isEmpty() || pwd2.isEmpty() ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Le credenziali non possono essere nulle");
			return;
		}
		if(!pwd1.equals(pwd2)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Le due password sono diverse");
			return;
		}
		UserDAO userDao = new UserDAO(connection);
		boolean isPresent;
		try {
			isPresent = userDao.isPresent(usrn, email);
		}
		catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error, retry later");
			return;
		}
		if (isPresent) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Email già registrata");
		}
		else {
			User user;
			try {
				user = userDao.insertUser(usrn, pwd1, email);
			}
			catch(SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Internal server error, retry later");
				return;
			}
			request.getSession().setAttribute("user", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(usrn);
		}
	}

}
