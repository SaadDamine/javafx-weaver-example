package com.damine.javafxweaverexample.controllers;

import com.damine.javafxweaverexample.models.User;
import com.damine.javafxweaverexample.enums.RoleEnum;
import com.damine.javafxweaverexample.repositories.UserRepository;
import com.damine.javafxweaverexample.services.UserService;
import com.damine.javafxweaverexample.utils.BCryptManagerUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.controlsfx.control.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@Component
@FxmlView("../fxml/login.fxml")
public class LoginController implements Initializable {
    @FXML
    ComboBox<String> role;
    @FXML
    private PasswordField password;
    @FXML
    private TextField username;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        role.getItems().add(RoleEnum.USER.name());
        role.getItems().add(RoleEnum.ADMINISTRATOR.name());
        role.getSelectionModel().select(RoleEnum.USER.name());
        if (userService.testUser()) {
            // Ceci n'est pas à recopier en production
            List<RoleEnum> userRole = Arrays.asList(RoleEnum.USER);
            List<RoleEnum> adminRole = Arrays.asList(RoleEnum.USER, RoleEnum.ADMINISTRATOR);
            User user = new User("user", "user", "User", "USER", userRole);
            User adminUser = new User("admin", "admin", "Admin", "ADMIN", adminRole);
            userRepository.save(adminUser);
            userRepository.save(user);
        }
    }

    @FXML
    public void login(ActionEvent event) throws Exception {
        if (username.getText().isEmpty()) {
            Notifications notifications = Notifications.create().title("Error").text("insert user name").graphic(null).position(Pos.CENTER);
            notifications.showWarning();
            return;
        }
        if (password.getText().isEmpty()) {
            Notifications notifications = Notifications.create().title("Error").text("insert user password").graphic(null).position(Pos.CENTER);
            notifications.showWarning();
            return;
        }
        if (userService.loadUserByUsername(username.getText()) == null) {
            Notifications notifications = Notifications.create().title("Error").text("user not found").graphic(null).position(Pos.CENTER);
            notifications.showWarning();
            return;
        }
        if (BCryptManagerUtil.passwordencoder().matches(password.getText(), userService.loadUserByUsername(username.getText()).getPassword())) {
            if (role.getSelectionModel().getSelectedItem().equals(RoleEnum.USER.name())) {
                if (userService.userHasAuthority((User) userService.loadUserByUsername(username.getText()), RoleEnum.USER.name())) {
                    Node node = (Node) event.getSource();
                    Stage stage = (Stage) node.getScene().getWindow();
                    Scene scene = stage.getScene();
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/user.fxml"));
                    Parent root = (Parent) fxmlLoader.load();
                    scene.setRoot(root);
                }
            } else {
                Notifications notifications = Notifications.create().title("Error").text("permission denied").graphic(null).position(Pos.CENTER);
                notifications.showWarning();
                return;
            }
            if (role.getSelectionModel().getSelectedItem().equals(RoleEnum.ADMINISTRATOR.name())) {
                if (userService.userHasAuthority((User) userService.loadUserByUsername(username.getText()), RoleEnum.ADMINISTRATOR.name())) {
                    Node node = (Node) event.getSource();
                    Stage stage = (Stage) node.getScene().getWindow();
                    Scene scene = stage.getScene();
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/admin.fxml"));
                    Parent root = (Parent) fxmlLoader.load();
                    scene.setRoot(root);
                } else {
                    Notifications notifications = Notifications.create().title("Error").text("permission denied").graphic(null).position(Pos.CENTER);
                    notifications.showWarning();
                }
            }
        } else {
            Notifications notifications = Notifications.create().title("Error").text("wrong password or username").graphic(null).position(Pos.CENTER);
            notifications.showWarning();
        }
    }
}
