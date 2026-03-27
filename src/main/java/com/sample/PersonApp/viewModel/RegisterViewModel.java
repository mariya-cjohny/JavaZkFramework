package com.sample.PersonApp.viewModel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

import com.sample.PersonApp.dao.UserDAO;
import com.sample.PersonApp.dto.UserDTO;

public class RegisterViewModel {

    private String username;
    private String password;
    private String confirmPassword;

    private String usernameError;
    private String passwordError;
    private String confirmPasswordError;
    private String generalError;

    private UserDAO userDAO = new UserDAO();

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getUsernameError() {
        return usernameError;
    }

    public String getPasswordError() {
        return passwordError;
    }

    public String getConfirmPasswordError() {
        return confirmPasswordError;
    }

    public String getGeneralError() {
        return generalError;
    }

    @Command
    @NotifyChange({
        "usernameError",
        "passwordError",
        "confirmPasswordError",
        "generalError"
    })
    public void register() {

        // reset errors
        usernameError = null;
        passwordError = null;
        confirmPasswordError = null;
        generalError = null;

        boolean hasError = false;

        //username validation
        if (username == null || username.trim().isEmpty()) {
            usernameError = "Username required";
            hasError = true;
        } else if (username.length() < 4) {
            usernameError = "Minimum 4 characters required";
            hasError = true;
        } else if (username.length() > 20) {
            usernameError = "Maximum 20 characters allowed";
            hasError = true;
        }

        //password validation
        if (password == null || password.isEmpty()) {
            passwordError = "Password required";
            hasError = true;
        } else if (password.length() < 6) {
            passwordError = "Minimum 6 characters required";
            hasError = true;
        } else if (!password.matches(".*[A-Z].*")) {
            passwordError = "Must contain at least 1 uppercase letter";
            hasError = true;
        } else if (!password.matches(".*[a-z].*")) {
            passwordError = "Must contain at least 1 lowercase letter";
            hasError = true;
        } else if (!password.matches(".*\\d.*")) {
            passwordError = "Must contain at least 1 number";
            hasError = true;
        }

        //confirm password validation
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            confirmPasswordError = "Please confirm your password";
            hasError = true;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordError = "Passwords do not match";
            hasError = true;
        }

        if (hasError) {
            return;
        }

        if (userDAO.findByUsername(username.toLowerCase()) != null) {
            generalError = "Username already exists";
            return;
        }

        UserDTO user = new UserDTO();
        user.setUsername(username.toLowerCase());
        user.setPassword(password);
        user.setRole("USER");

        userDAO.insert(user);

        Messagebox.show(
                "Registration successful!",
                "Success",
                Messagebox.OK,
                Messagebox.INFORMATION,
                event -> {
                    if (Messagebox.ON_OK.equals(event.getName())) {
                        Executions.sendRedirect("login.zul");
                    }
                }
        );
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
    @NotifyChange("confirmPasswordError")
    public void clearConfirmPasswordError() {
        confirmPasswordError = null;
    }

    @Command
    public void goToLogin() {
        Executions.sendRedirect("login.zul");
    }
}
