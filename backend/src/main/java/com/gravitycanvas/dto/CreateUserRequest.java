package com.gravitycanvas.dto;

public class CreateUserRequest {
    private String username;
    private String password;
    private String fullName;
    private String role; // "ROLE_MANAGER" or "ROLE_TEAM_MEMBER"
    private String employeeNo;
    private String department;

    public CreateUserRequest() {}

    public CreateUserRequest(String username, String password, String fullName, String role, String employeeNo, String department) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.employeeNo = employeeNo;
        this.department = department;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
