package beans;


import java.sql.Timestamp;

import java.util.List;

public class Image {
	private int id;
	private int albumId;
	private String title;
	private Timestamp date;
	private String description;
	private String path;
	private List<Comment> comments;


	public int getId() {
		return id;
	}

	public int getAlbumId() {
		return albumId;
	}

	public String getTitle() {
		return title;
	}

	public Timestamp getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public String getPath() {
		return path;
	}
	public List<Comment> getComments(){
		return comments;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setAlbumId(int albumId) {
		this.albumId = albumId;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
}