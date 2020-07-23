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
		List<String> paths = new ArrayList<String>();
		String query = "SELECT * from dbtiwexam1920js.album order by date desc";
		String query2 = "SELECT path from dbtiwexam1920js.image group by album";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		
		try {
			pstatement = connection.prepareStatement(query);
			result = pstatement.executeQuery();
			while (result.next()) {
				Album album = new Album();
				album.setId(result.getInt("id"));
				album.setDate(result.getTimestamp("date"));
				album.setTitle(result.getString("title"));
				albums.add(album);
				
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
		
		ResultSet result2 = null;
		PreparedStatement pstatement2 = null;
		try {
			pstatement2 = connection.prepareStatement(query2);
			result2 = pstatement2.executeQuery();
			while (result2.next()) {
				paths.add(result2.getString("path"));
			}
			
		} catch (SQLException e3) {
		    e3.printStackTrace();
			throw new SQLException(e3);
		} finally {
			try {
				result2.close();
			} catch (Exception e4) {
				throw new SQLException(e4);
			}
			try {
				pstatement2.close();
			} catch (Exception e5) {
				throw new SQLException(e5);
			}
		}
		for(int i = 0; i<albums.size(); i++) {
			albums.get(i).setFirstImagePath(paths.get(albums.get(i).getId()-1));
			
		}
		return albums;
	}
	

	public Album getTitleOfAlbum(int albumId) throws SQLException {
		Album album = new Album();
		String query = "SELECT title from dbtiwexam1920js.album where id = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, albumId);
			result = pstatement.executeQuery();
			if(result.next())
				album.setTitle(result.getString("title"));
			
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
		return album;
	}

	
	public List<Integer> getAlbumIds() throws SQLException {
		List<Integer> ids = new ArrayList<Integer>();
		String query = "SELECT id FROM dbtiwexam1920js.album";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			result = pstatement.executeQuery();
			if(result.next())
				ids.add(result.getInt(1));
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
		
		return ids;
	}
	
	
	
	
}
