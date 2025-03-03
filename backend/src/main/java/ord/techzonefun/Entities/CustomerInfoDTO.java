package ord.techzonefun.Entities;

public class CustomerInfoDTO {
    private String id;
    private String username;
    private String password;
    private String phoneNumber;

    public CustomerInfoDTO(String id, String username, String password, String phoneNumber) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPhoneNumber() { return phoneNumber; }
}