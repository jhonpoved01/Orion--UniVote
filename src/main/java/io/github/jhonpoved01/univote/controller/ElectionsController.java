package io.github.jhonpoved01.univote.controller;

import io.github.jhonpoved01.univote.SceneNavigator;
import io.github.jhonpoved01.univote.model.Candidacy;
import io.github.jhonpoved01.univote.model.Election;
import io.github.jhonpoved01.univote.model.VoteReceipt;
import io.github.jhonpoved01.univote.security.UserSession;
import io.github.jhonpoved01.univote.service.VotingService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;

public final class ElectionsController {
    private static final String SAFE_LOAD_ERROR =
            "No fue posible cargar la información electoral.";
    private static final String SAFE_VOTE_ERROR = "No fue posible registrar el voto.";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UserSession session;
    private final VotingService votingService;
    private final Executor executor;
    private final SceneNavigator navigator;
    private Election selectedElection;
    private boolean operationInProgress;
    private boolean voteCompleted;

    @FXML private ListView<Election> electionList;
    @FXML private ListView<Candidacy> candidacyList;
    @FXML private Label emptyElectionsLabel;
    @FXML private Label candidacyPromptLabel;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button confirmVoteButton;
    @FXML private Button backButton;

    public ElectionsController(
            UserSession session, VotingService votingService,
            Executor executor, SceneNavigator navigator) {
        this.session = Objects.requireNonNull(session);
        this.votingService = Objects.requireNonNull(votingService);
        this.executor = Objects.requireNonNull(executor);
        this.navigator = Objects.requireNonNull(navigator);
    }

    @FXML
    private void initialize() {
        electionList.setCellFactory(view -> new ElectionCell());
        candidacyList.setCellFactory(view -> new CandidacyCell());
        electionList.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, selected) -> selectElection(selected));
        candidacyList.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, selected) ->
                        confirmVoteButton.setDisable(operationInProgress || selected == null));
        clearError();
        loadElections();
    }

    private void loadElections() {
        runTask(new Task<List<Election>>() {
            @Override protected List<Election> call() {
                return votingService.getAvailableElections(session);
            }
        }, elections -> {
            electionList.getItems().setAll(elections);
            boolean empty = elections.isEmpty();
            emptyElectionsLabel.setVisible(empty);
            emptyElectionsLabel.setManaged(empty);
        }, SAFE_LOAD_ERROR);
    }

    private void selectElection(Election election) {
        selectedElection = election;
        candidacyList.getItems().clear();
        confirmVoteButton.setDisable(true);
        if (election == null || operationInProgress) {
            return;
        }
        candidacyPromptLabel.setText("Cargando candidaturas…");
        runTask(new Task<List<Candidacy>>() {
            @Override protected List<Candidacy> call() {
                return votingService.getCandidacies(session, election.id());
            }
        }, candidacies -> {
            candidacyList.getItems().setAll(candidacies);
            candidacyPromptLabel.setText(candidacies.isEmpty()
                    ? "No hay candidaturas disponibles para esta elección."
                    : "Selecciona una candidatura para continuar.");
        }, SAFE_LOAD_ERROR);
    }

    @FXML
    private void handleConfirmVote() {
        Candidacy candidacy = candidacyList.getSelectionModel().getSelectedItem();
        if (operationInProgress || voteCompleted || selectedElection == null || candidacy == null) {
            return;
        }
        if (!confirmVote(selectedElection, candidacy)) {
            return;
        }

        int electionId = selectedElection.id();
        int candidacyId = candidacy.id();
        runTask(new Task<VoteReceipt>() {
            @Override protected VoteReceipt call() {
                return votingService.castVote(session, electionId, candidacyId);
            }
        }, receipt -> {
            voteCompleted = true;
            confirmVoteButton.setDisable(true);
            navigator.showVoteReceipt(session, receipt);
        }, SAFE_VOTE_ERROR);
    }

    private boolean confirmVote(Election election, Candidacy candidacy) {
        ButtonType emitVote = new ButtonType("Emitir voto");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar voto");
        alert.setHeaderText("Esta decisión no podrá modificarse");
        alert.setContentText("Elección: " + election.name()
                + "\nCandidatura: Lista " + candidacy.listNumber()
                + " · " + candidacy.candidateName());
        alert.getButtonTypes().setAll(ButtonType.CANCEL, emitVote);
        alert.initOwner(confirmVoteButton.getScene().getWindow());
        alert.getDialogPane().getStyleClass().add("electoral-dialog");
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(
                ElectionsController.class.getResource(
                        "/io/github/jhonpoved01/univote/css/theme.css")).toExternalForm());
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == emitVote;
    }

    @FXML
    private void handleBack() {
        if (!operationInProgress) {
            navigator.showDashboard(session);
        }
    }

    private <T> void runTask(
            Task<T> task, java.util.function.Consumer<T> onSuccess, String safeError) {
        if (operationInProgress) {
            return;
        }
        clearError();
        setBusy(true);
        task.setOnSucceeded(event -> {
            setBusy(false);
            try {
                onSuccess.accept(task.getValue());
            } catch (RuntimeException exception) {
                showError(safeError);
            }
        });
        task.setOnFailed(event -> {
            setBusy(false);
            showError(safeError);
        });
        task.setOnCancelled(event -> {
            setBusy(false);
            showError(safeError);
        });
        try {
            executor.execute(task);
        } catch (RejectedExecutionException exception) {
            setBusy(false);
            showError(safeError);
        }
    }

    private void setBusy(boolean busy) {
        operationInProgress = busy;
        progressIndicator.setVisible(busy);
        progressIndicator.setManaged(busy);
        electionList.setDisable(busy);
        candidacyList.setDisable(busy);
        backButton.setDisable(busy);
        confirmVoteButton.setDisable(busy
                || voteCompleted
                || candidacyList.getSelectionModel().getSelectedItem() == null);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private static final class ElectionCell extends ListCell<Election> {
        @Override protected void updateItem(Election item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null
                    : item.name() + "\n" + item.description()
                    + "\nPeriodo: " + DATE_FORMAT.format(item.startsAt())
                    + " — " + DATE_FORMAT.format(item.endsAt()));
        }
    }

    private static final class CandidacyCell extends ListCell<Candidacy> {
        @Override protected void updateItem(Candidacy item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null
                    : "LISTA " + item.listNumber() + "  ·  " + item.candidateName()
                    + "\n" + item.proposalTitle() + "\n" + item.proposal());
        }
    }
}
