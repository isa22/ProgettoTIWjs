package beans;


import java.sql.Timestamp;

public class Image {
	private int id;
	private int albumId;
	private String title;
	private Timestamp date;
	private String description;
	private String path;


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
}