package io.github.jhonpoved01.univote.controller;

import io.github.jhonpoved01.univote.SceneNavigator;
import io.github.jhonpoved01.univote.model.VoteReceipt;
import io.github.jhonpoved01.univote.security.UserSession;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public final class VoteReceiptController {
    private final UserSession session;
    private final VoteReceipt receipt;
    private final SceneNavigator navigator;
    @FXML private Label verificationCodeLabel;
    @FXML private Label copyStatusLabel;

    public VoteReceiptController(
            UserSession session, VoteReceipt receipt, SceneNavigator navigator) {
        this.session = Objects.requireNonNull(session);
        this.receipt = Objects.requireNonNull(receipt);
        this.navigator = Objects.requireNonNull(navigator);
    }

    @FXML private void initialize() {
        verificationCodeLabel.setText(receipt.verificationCode());
    }

    @FXML private void handleCopyCode() {
        ClipboardContent content = new ClipboardContent();
        content.putString(receipt.verificationCode());
        Clipboard.getSystemClipboard().setContent(content);
        copyStatusLabel.setText("Código copiado al portapapeles");
    }

    @FXML private void handleDashboard() {
        navigator.showDashboard(session);
    }
}
