package io.github.jhonpoved01.univote.controller;

import io.github.jhonpoved01.univote.SceneNavigator;
import io.github.jhonpoved01.univote.exception.VoteVerificationException;
import io.github.jhonpoved01.univote.model.VoteVerification;
import io.github.jhonpoved01.univote.security.UserSession;
import io.github.jhonpoved01.univote.service.VoteVerificationService;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public final class VoteVerificationController {
    private static final String SAFE_ERROR = "No fue posible verificar el comprobante.";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final UserSession session;
    private final VoteVerificationService service;
    private final Executor executor;
    private final SceneNavigator navigator;
    private boolean busy;
    @FXML private TextField codeField;
    @FXML private Button verifyButton;
    @FXML private Button backButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label messageLabel;

    public VoteVerificationController(
            UserSession session, VoteVerificationService service,
            Executor executor, SceneNavigator navigator) {
        this.session = Objects.requireNonNull(session);
        this.service = Objects.requireNonNull(service);
        this.executor = Objects.requireNonNull(executor);
        this.navigator = Objects.requireNonNull(navigator);
    }

    @FXML private void handleVerify() {
        if (busy) return;
        clearMessage();
        String code = codeField.getText();
        Task<VoteVerification> task = new Task<>() {
            @Override protected VoteVerification call() { return service.verify(session, code); }
        };
        task.setOnSucceeded(event -> {
            setBusy(false);
            VoteVerification verification = task.getValue();
            messageLabel.getStyleClass().setAll("verification-success");
            showMessage("Voto registrado el " + DATE_FORMAT.format(verification.registeredAt()) + ".");
        });
        task.setOnFailed(event -> {
            setBusy(false);
            Throwable error = task.getException();
            showMessage(error instanceof VoteVerificationException
                    ? error.getMessage() : SAFE_ERROR);
        });
        task.setOnCancelled(event -> { setBusy(false); showMessage(SAFE_ERROR); });
        setBusy(true);
        try { executor.execute(task); }
        catch (RejectedExecutionException exception) { setBusy(false); showMessage(SAFE_ERROR); }
    }

    @FXML private void handleBack() {
        if (!busy) { codeField.clear(); navigator.showDashboard(session); }
    }

    private void setBusy(boolean value) {
        busy = value;
        codeField.setDisable(value);
        verifyButton.setDisable(value);
        backButton.setDisable(value);
        progressIndicator.setVisible(value);
        progressIndicator.setManaged(value);
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        messageLabel.getStyleClass().setAll("verification-message");
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
