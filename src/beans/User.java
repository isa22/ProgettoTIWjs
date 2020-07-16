package beans;

public class User {
    private int id;
    private String username;
    private String email;
    private AlbumOrder albumOrder;


    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public AlbumOrder getAlbumOrder() {
        return albumOrder;
    }
    
    public String getEmail() {
        return email;
    }

    public void setId(int id) {
        this.id = id;
    }



    public void setUsername(String username) {
        this.username = username;
    }

    public void setAlbumOrder(AlbumOrder albumOrder) {
        this.albumOrder = albumOrder;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
