package com.eiffelbikerental.model;

public class User {

    private int id;
    private String username;
    private String email;
    private String password;
    private UserType userType;
    private String phone;
    private String address;
    private String country;

    public enum UserType {
        STUDENT, EMPLOYEE,CUSTOMER
    }


    public User() {

	}

	public User(int id, String username) {
		super();
		this.id = id;
		this.username = username;
	}

	public User(int id, String username, String email, String password, UserType type) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userType = type;
    }

    public User(String username, String email, String password, UserType type) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.userType = type;
    }
	public User(int id, String username, String email, String password, UserType type, String phone ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userType = type;
        this.phone = phone;

    }
	public User(int id, String username, String email,UserType type, String phone ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userType = type;
        this.phone = phone;

    }


    public User(int int1, String string, String string2, Object object, UserType valueOf, int int2) {
		// TODO Auto-generated constructor stub
	}

	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}


	public String getPhone() {
		return phone;
	}

	public void setPhone(String phoneNumber) {
		phone = phoneNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", userType=" + userType +
                '}';
    }
}
