package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.Image;

public class ImageDAO {
	private Connection connection;

	public ImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Image> findImagesByAlbum(int albumId, int pageOffset) throws SQLException {
		List<Image> images = new ArrayList<Image>();
		String query = "SELECT * from dbtiwexam1920js.image where album = ? limit ?,5";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, albumId);
			pstatement.setInt(2, (pageOffset-1)*5);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Image image = new Image();
					image.setId(result.getInt("id"));
					image.setDate(result.getTimestamp("date"));
					image.setTitle(result.getString("name"));
					image.setPath(result.getString("path"));
					image.setAlbumId(albumId);
					images.add(image);
				}
			}
		}
		return images;
	}
	
	
	public int getNumberOfImagesByAlbum(int albumId) throws SQLException {
		int count = 0;
		String query = "SELECT COUNT(*) from dbtiwexam1920js.image where album = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, albumId);
			try (ResultSet result = pstatement.executeQuery();) {
				if(result.next())
					count = result.getInt(1);
			}
		}
		return count;
	}
	
	public Image findImageDescriptionByImage(int imageId) throws SQLException {
		Image image = null;

		String query = "SELECT * from dbtiwexam1920js.image where id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, imageId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.next()) {
					image = new Image();
					image.setId(result.getInt("id"));
					image.setDate(result.getTimestamp("date"));
					image.setTitle(result.getString("name"));
					image.setPath(result.getString("path"));
					image.setDescription(result.getString("description"));
					image.setAlbumId(result.getInt("album"));
				}
			}
		}
		return image;
	}
}
