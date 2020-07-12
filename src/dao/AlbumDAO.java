package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.Album;

public class AlbumDAO {
	private Connection connection;

	public AlbumDAO(Connection connection) {
		this.connection = connection;
	}

	public List<Album> findAll() throws SQLException {
		List<Album> albums = new ArrayList<Album>();
		String query = "SELECT * from dbtiwexam1920.album";
		String query2 = "SELECT path from dbtiwexam1920.image group by album";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Album album = new Album();
					album.setId(result.getInt("id"));
					album.setDate(result.getTimestamp("date"));
					album.setTitle(result.getString("title"));
					albums.add(album);
				}
			}
		}
		try (PreparedStatement pstatement = connection.prepareStatement(query2);) {
			try (ResultSet result = pstatement.executeQuery();) {
				int i = 0;
				while (result.next()) {
					albums.get(i).setFirstImagePath(result.getString("path"));
					i++;	
				}
			}
		}
		return albums;
	}
	
	
	public Album getTitleOfAlbum(int albumId) throws SQLException {
		Album album = new Album();
		String query = "SELECT title from dbtiwexam1920.album where id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, albumId);
			try (ResultSet result = pstatement.executeQuery();) {
				if(result.next())
					album.setTitle(result.getString("title"));
			}
		}
		return album;
	}
	
	
	
	
}
