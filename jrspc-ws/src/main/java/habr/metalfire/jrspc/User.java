package habr.metalfire.jrspc;

import java.io.Serializable;

public class User implements Serializable{    
        
    private static final long serialVersionUID = 1L;
    
    public static enum Role { User, Admin, Supervisor }
    
    private Long id;
    private String login;    
    private String password;
    private String city;      
    private String role;
    
    public User() { }
    
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    
    public String getLogin() {return login;}
    public void setLogin(String login) {this.login = login;}
    
    public String getPassword() { return password;}
    public void setPassword(String password) {this.password = password; }
    
    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}

    public String getCity() { return city;}
    public void setCity(String city) {this.city = city;}   
    
}
