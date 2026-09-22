package com.gravitycanvas.dto;

public class UserDto {
    private Long id;
    private String username;
    private String fullName;
    private String role;
    private String employeeNo;
    private String department;

    public UserDto() {}

    public UserDto(Long id, String username, String fullName, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public UserDto(Long id, String username, String fullName, String role, String employeeNo, String department) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.employeeNo = employeeNo;
        this.department = department;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
