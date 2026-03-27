package com.sample.PersonApp.viewModel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;

import com.sample.PersonApp.dao.UserDAO;
import com.sample.PersonApp.dto.UserDTO;

public class LoginViewModel {

    private String username;
    private String password;
    private String usernameError;
    private String passwordError;
    private String loginError;
    private UserDAO userDAO = new UserDAO();

    // getters & setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsernameError() {
        return usernameError;
    }

    public String getPasswordError() {
        return passwordError;
    }

    public String getLoginError() {
        return loginError;
    }

    @Command
    @NotifyChange({"usernameError", "passwordError", "loginError"})
    public void login() {
        usernameError = null;
        passwordError = null;
        loginError = null;

        boolean hasError = false;
        if (username == null || username.trim().isEmpty()) {
            usernameError = "Username is required";
            hasError = true;
        }

        if (password == null || password.trim().isEmpty()) {
            passwordError = "Password is required";
            hasError = true;
        }
        if (hasError) {
            return;
        }
        UserDTO user = userDAO.findByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            // store user info in session
            Sessions.getCurrent().setAttribute("user", user);
            Sessions.getCurrent().setAttribute("role", user.getRole());
            Executions.sendRedirect("home.zul");

        } else {
            loginError = "Invalid username or password";
        }
    }

    @Command
    @NotifyChange("usernameError")
    public void clearUsernameError() {
        usernameError = null;
    }

    @Command
    @NotifyChange("passwordError")
    public void clearPasswordError() {
        passwordError = null;
    }

    @Command
    public void goToRegister() {
        Executions.sendRedirect("register.zul");
    }
}
