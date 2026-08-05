package io.github.jhonpoved01.univote.controller;

import io.github.jhonpoved01.univote.SceneNavigator;
import io.github.jhonpoved01.univote.security.UserSession;
import io.github.jhonpoved01.univote.service.AuthenticationService;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public final class LoginController {

    private static final String SAFE_AUTHENTICATION_ERROR =
            "No fue posible iniciar sesión con las credenciales proporcionadas.";

    private final AuthenticationService authenticationService;
    private final Executor authenticationExecutor;
    private final SceneNavigator navigator;
    private boolean authenticationInProgress;

    @FXML
    private TextField identifierField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label errorLabel;

    public LoginController(
            AuthenticationService authenticationService,
            Executor authenticationExecutor,
            SceneNavigator navigator) {
        this.authenticationService = Objects.requireNonNull(
                authenticationService, "authenticationService no puede ser null");
        this.authenticationExecutor = Objects.requireNonNull(
                authenticationExecutor, "authenticationExecutor no puede ser null");
        this.navigator = Objects.requireNonNull(navigator, "navigator no puede ser null");
    }

    @FXML
    private void initialize() {
        setBusy(false);
        clearError();
        Platform.runLater(identifierField::requestFocus);
    }

    @FXML
    private void handleLogin() {
        if (authenticationInProgress) {
            return;
        }

        clearError();
        String identifier = identifierField.getText() == null
                ? null : identifierField.getText().strip();
        char[] password = passwordField.getText() == null
                ? null : passwordField.getText().toCharArray();
        passwordField.clear();
        setBusy(true);

        Task<UserSession> authenticationTask = new Task<>() {
            @Override
            protected UserSession call() {
                return authenticationService.authenticate(identifier, password);
            }
        };
        authenticationTask.setOnSucceeded(event -> {
            setBusy(false);
            try {
                navigator.showDashboard(authenticationTask.getValue());
            } catch (RuntimeException exception) {
                showSafeError();
            }
        });
        authenticationTask.setOnFailed(event -> {
            setBusy(false);
            showSafeError();
        });
        authenticationTask.setOnCancelled(event -> {
            Arrays.fill(password, '\0');
            setBusy(false);
            showSafeError();
        });

        try {
            authenticationExecutor.execute(authenticationTask);
        } catch (RejectedExecutionException exception) {
            Arrays.fill(password, '\0');
            setBusy(false);
            showSafeError();
        }
    }

    private void setBusy(boolean busy) {
        authenticationInProgress = busy;
        loginButton.setDisable(busy);
        identifierField.setDisable(busy);
        passwordField.setDisable(busy);
        progressIndicator.setVisible(busy);
        progressIndicator.setManaged(busy);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showSafeError() {
        errorLabel.setText(SAFE_AUTHENTICATION_ERROR);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        passwordField.requestFocus();
    }
}
