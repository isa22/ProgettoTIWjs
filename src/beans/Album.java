package beans;

import java.sql.Timestamp;

public class Album {
	private int id;
	private String title;
	private Timestamp date;
	private String firstImagePath;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public Timestamp getDate() {
		return date;
	}
	
	public void setDate(Timestamp date) {
		this.date = date;
	}
	
	public String getFirstImagePath() {
		return firstImagePath;
	}
	
	public void setFirstImagePath(String path) {
		this.firstImagePath = path;
	}
}
