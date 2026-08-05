package io.github.jhonpoved01.univote.controller;

import io.github.jhonpoved01.univote.SceneNavigator;
import io.github.jhonpoved01.univote.model.AuthenticatedUser;
import io.github.jhonpoved01.univote.security.UserSession;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public final class DashboardController {

    private final SceneNavigator navigator;
    private UserSession session;

    @FXML
    private Label profileNameLabel;
    @FXML
    private Label profileRoleLabel;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label avatarLabel;
    @FXML
    private Button voteButton;
    @FXML
    private Button exploreElectionsButton;
    @FXML
    private Button processDetailsButton;
    @FXML private Button verificationButton;
    @FXML private Button resultsButton;

    public DashboardController(UserSession session, SceneNavigator navigator) {
        this.session = Objects.requireNonNull(session, "session no puede ser null");
        this.navigator = Objects.requireNonNull(navigator, "navigator no puede ser null");
    }

    @FXML
    private void initialize() {
        AuthenticatedUser user = session.user();
        String fullName = user.firstNames().strip() + " " + user.lastNames().strip();
        profileNameLabel.setText(fullName);
        profileRoleLabel.setText(user.roleName());
        welcomeLabel.setText("Bienvenido, " + user.firstNames().strip());
        avatarLabel.setText(initials(user));
        boolean canVote = session.hasPermission("EMITIR_VOTO");
        voteButton.setVisible(canVote);
        voteButton.setManaged(canVote);
        exploreElectionsButton.setVisible(canVote);
        exploreElectionsButton.setManaged(canVote);
        processDetailsButton.setVisible(canVote);
        processDetailsButton.setManaged(canVote);
        verificationButton.setVisible(true);
        verificationButton.setManaged(true);
        boolean canViewResults = session.hasPermission("CONSULTAR_RESULTADOS");
        resultsButton.setVisible(canViewResults);
        resultsButton.setManaged(canViewResults);
    }

    @FXML
    private void handleElections() {
        navigator.showElections(session);
    }

    @FXML private void handleVerification() { navigator.showVoteVerification(session); }

    @FXML private void handleResults() { navigator.showResults(session); }

    @FXML
    private void handleLogout() {
        session = null;
        navigator.showLogin();
    }

    private static String initials(AuthenticatedUser user) {
        return firstInitial(user.firstNames()) + firstInitial(user.lastNames());
    }

    private static String firstInitial(String value) {
        return value.strip().substring(0, 1).toUpperCase(java.util.Locale.ROOT);
    }
}
