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
	
	public List<Image> findImagesByAlbum(int albumId) throws SQLException {
		List<Image> images = new ArrayList<Image>();
		String query = "SELECT * from dbtiwexam1920js.image where album = ? order by date desc";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, albumId);
			result = pstatement.executeQuery();
			while (result.next()) {
				Image image = new Image();
				image.setId(result.getInt("id"));
				image.setDate(result.getTimestamp("date"));
				image.setTitle(result.getString("name"));
				image.setPath(result.getString("path"));
				image.setDescription(result.getString("description"));
				image.setAlbumId(albumId);
				images.add(image);
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
		return images;
	}
	
	
	public int getNumberOfImagesByAlbum(int albumId) throws SQLException {
		int count = 0;
		String query = "SELECT COUNT(*) from dbtiwexam1920js.image where album = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, albumId);
			result = pstatement.executeQuery();
			if(result.next())
				count = result.getInt(1);
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
		return count;
	}
	
	
	public Image findImageDescriptionByImage(int imageId) throws SQLException {
		Image image = null;

		String query = "SELECT * from dbtiwexam1920js.image where id = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, imageId);
			result = pstatement.executeQuery();
			if (result.next()) {
				image = new Image();
				image.setId(result.getInt("id"));
				image.setDate(result.getTimestamp("date"));
				image.setTitle(result.getString("name"));
				image.setPath(result.getString("path"));
				image.setDescription(result.getString("description"));
				image.setAlbumId(result.getInt("album"));
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
		return image;
	}
}
