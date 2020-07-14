package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.AlbumOrder;

public class AlbumOrderDAO {
	private Connection connection;
	
	public AlbumOrderDAO(Connection connection){
		this.connection = connection;
	}
	
	//create a new album order for new users
	public void createAlbumOrder(int userId, int albumNum) throws SQLException {

		String query = "INSERT into dbtiwexam1920js.albumorder (album, user, order) VALUES(?, ?, ?)";
		for (int i = 1; i <= albumNum; i++) {
			try (PreparedStatement pstatement = connection.prepareStatement(query);) {
				pstatement.setInt(1, i);
				pstatement.setInt(2, userId);
				pstatement.setInt(3, i);
				pstatement.executeUpdate();
			}
		}
	}
	
	//update album order after a submit of the client
	public void changeAlbumOrder(int userId, List order) throws SQLException{
		
		String query = "UPDATE dbtiwexam1920js.albumorder SET aulbumorder = ? WHERE user = ? and album = ?";
		for (int i = 1 ; i<=order.size(); i++) {
			try (PreparedStatement pstatement = connection.prepareStatement(query);) {
				pstatement.setInt(1, i);
				pstatement.setInt(2, userId);
				pstatement.setInt(2, (int) order.get(i-1));
				pstatement.executeUpdate();
			}
		}
	}
	
	//retrieve album order give id of the user
	public  AlbumOrder getAlbumOrder(int userId) throws SQLException{
		
		AlbumOrder order = new AlbumOrder();
		List<Integer> orders = new ArrayList<Integer>();
		
		String query = "SELECT album from dbtiwexam1920js.albumorder WHERE user = ? order by albumorder";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					orders.add(result.getInt(1));
				}
			}
		}
		order.setOrder(orders);
		return order;
	}
}
