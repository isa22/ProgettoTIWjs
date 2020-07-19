package utils;

import java.util.List;

import beans.Image;

public class AlbumData{
	private List<Image> images;
	private String albumTitle;
	private int albumId;
	
	public AlbumData(List<Image> images, String albumTitle, int albumId) {
		this.images = images;
		this.albumTitle = albumTitle;
		this.albumId = albumId;
	}
}
