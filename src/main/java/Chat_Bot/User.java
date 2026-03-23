package Chat_Bot;

public class User {
    private String username, email, phone, dob;

    public User(String username, String email, String phone, String dob) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.dob = dob;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDob() { return dob; }
}
