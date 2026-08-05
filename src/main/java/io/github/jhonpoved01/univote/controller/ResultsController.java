package io.github.jhonpoved01.univote.controller;

import io.github.jhonpoved01.univote.SceneNavigator;
import io.github.jhonpoved01.univote.exception.ElectionResultsException;
import io.github.jhonpoved01.univote.model.Election;
import io.github.jhonpoved01.univote.model.ElectionResult;
import io.github.jhonpoved01.univote.security.UserSession;
import io.github.jhonpoved01.univote.service.ElectionResultsService;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;

public final class ResultsController {
    private static final String SAFE_ERROR = "No fue posible consultar los resultados.";
    private final UserSession session;
    private final ElectionResultsService service;
    private final Executor executor;
    private final SceneNavigator navigator;
    private boolean busy;
    @FXML private ListView<Election> electionList;
    @FXML private ListView<ElectionResult> resultList;
    @FXML private Label selectedElectionLabel;
    @FXML private Label emptyLabel;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button backButton;

    public ResultsController(
            UserSession session, ElectionResultsService service,
            Executor executor, SceneNavigator navigator) {
        this.session = Objects.requireNonNull(session);
        this.service = Objects.requireNonNull(service);
        this.executor = Objects.requireNonNull(executor);
        this.navigator = Objects.requireNonNull(navigator);
    }

    @FXML private void initialize() {
        electionList.setCellFactory(view -> new ElectionCell());
        resultList.setCellFactory(view -> new ResultCell());
        electionList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, election) -> loadResults(election));
        loadElections();
    }

    private void loadElections() {
        run(new Task<List<Election>>() {
            @Override protected List<Election> call() { return service.getPublishableElections(session); }
        }, elections -> {
            electionList.getItems().setAll(elections);
            setEmpty(elections.isEmpty(), "No hay resultados publicados disponibles.");
        });
    }

    private void loadResults(Election election) {
        resultList.getItems().clear();
        if (election == null || busy) return;
        selectedElectionLabel.setText(election.name());
        run(new Task<List<ElectionResult>>() {
            @Override protected List<ElectionResult> call() {
                return service.getPublishedResults(session, election.id());
            }
        }, results -> {
            resultList.getItems().setAll(results);
            setEmpty(results.isEmpty(), "Esta elección no tiene resultados disponibles.");
        });
    }

    private <T> void run(Task<T> task, java.util.function.Consumer<T> success) {
        if (busy) return;
        clearError(); setBusy(true);
        task.setOnSucceeded(event -> { setBusy(false); success.accept(task.getValue()); });
        task.setOnFailed(event -> {
            setBusy(false);
            Throwable error = task.getException();
            showError(error instanceof ElectionResultsException ? error.getMessage() : SAFE_ERROR);
        });
        task.setOnCancelled(event -> { setBusy(false); showError(SAFE_ERROR); });
        try { executor.execute(task); }
        catch (RejectedExecutionException exception) { setBusy(false); showError(SAFE_ERROR); }
    }

    @FXML private void handleBack() { if (!busy) navigator.showDashboard(session); }

    private void setBusy(boolean value) {
        busy = value;
        electionList.setDisable(value); resultList.setDisable(value); backButton.setDisable(value);
        progressIndicator.setVisible(value); progressIndicator.setManaged(value);
    }
    private void setEmpty(boolean visible, String text) {
        emptyLabel.setText(text); emptyLabel.setVisible(visible); emptyLabel.setManaged(visible);
    }
    private void clearError() { errorLabel.setText(""); errorLabel.setVisible(false); errorLabel.setManaged(false); }
    private void showError(String text) { errorLabel.setText(text); errorLabel.setVisible(true); errorLabel.setManaged(true); }

    private static final class ElectionCell extends ListCell<Election> {
        @Override protected void updateItem(Election item, boolean empty) {
            super.updateItem(item, empty); setText(empty || item == null ? null : item.name());
        }
    }
    private static final class ResultCell extends ListCell<ElectionResult> {
        @Override protected void updateItem(ElectionResult item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null
                    : "LISTA " + item.listNumber() + "  ·  " + item.candidateName()
                    + "\n" + item.proposalTitle()
                    + "\n" + item.totalVotes() + " votos  ·  "
                    + item.percentage().setScale(2, RoundingMode.HALF_UP)
                            .stripTrailingZeros().toPlainString() + "%");
        }
    }
}
