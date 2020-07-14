package beans;

import java.sql.Timestamp;

public class Comment {
	private Timestamp timestamp;
	private int userId;
	private int imageId;
	private String text;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	
	public int getImageId() {
		return imageId;
	}
	
	public void setImageId(int imageId) {
		this.imageId = imageId;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
}