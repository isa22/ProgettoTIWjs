package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import beans.Comment;

public class CommentDAO {
	private Connection connection;
	
	public CommentDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Comment> findCommentsByImage(int imageId) throws SQLException {
		List<Comment> comments = new ArrayList<Comment>();
		String query = "SELECT c.*, u.username from dbtiwexam1920js.comment c inner join dbtiwexam1920js.user u on c.user = u.id where c.image = ?";
		
		ResultSet result = null;
		PreparedStatement pstatement = null;
		
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, imageId);
			result = pstatement.executeQuery();
			while (result.next()) {
				Comment comment = new Comment();
				comment.setTimestamp(result.getTimestamp("time"));
				comment.setText(StringEscapeUtils.unescapeJava(result.getString("content")));
				comment.setUserId(result.getInt("user"));
				comment.setUsername(StringEscapeUtils.unescapeJava(result.getString("username")));
				comment.setImageId(imageId);
				comments.add(comment);
			}
		} catch (SQLException e) {
		    e.printStackTrace();
			throw new SQLException(e);

		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}		
		return comments;
	}
	

	public void createComment(int userId, int imageId, String text)
			throws SQLException {

		String query = "INSERT into dbtiwexam1920js.comment (content, image, user) VALUES(?, ?, ?)";
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, text);
			pstatement.setInt(2, imageId);
			pstatement.setInt(3, userId);
			pstatement.executeUpdate();
		} catch (SQLException e) {
		    e.printStackTrace();
			throw new SQLException(e);

		} finally {
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}		
	}

}