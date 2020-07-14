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
		String query = "SELECT * from dbtiwexam1920js.comment where image = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, imageId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Comment comment = new Comment();
					comment.setTimestamp(result.getTimestamp("time"));
					comment.setText(StringEscapeUtils.unescapeJava(result.getString("content")));
					comment.setUserId(result.getInt("user"));
					comment.setImageId(imageId);
					comments.add(comment);
				}
			}
		}
		return comments;
	}
	

	public void createComment(int userId, int imageId, String text)
			throws SQLException {

		String query = "INSERT into dbtiwexam1920js.comment (content, image, user) VALUES(?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, text);
			pstatement.setInt(2, imageId);
			pstatement.setInt(3, userId);
			pstatement.executeUpdate();
		}
	}

}