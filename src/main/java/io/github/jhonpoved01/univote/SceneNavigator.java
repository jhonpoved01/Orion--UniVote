package io.github.jhonpoved01.univote;

import io.github.jhonpoved01.univote.config.AppConfig;
import io.github.jhonpoved01.univote.controller.DashboardController;
import io.github.jhonpoved01.univote.controller.LoginController;
import io.github.jhonpoved01.univote.controller.ElectionsController;
import io.github.jhonpoved01.univote.controller.VoteReceiptController;
import io.github.jhonpoved01.univote.exception.ViewNavigationException;
import io.github.jhonpoved01.univote.security.UserSession;
import io.github.jhonpoved01.univote.service.AuthenticationService;
import io.github.jhonpoved01.univote.service.VotingService;
import io.github.jhonpoved01.univote.model.VoteReceipt;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.Executor;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SceneNavigator {

    private static final String LOGIN_RESOURCE =
            "/io/github/jhonpoved01/univote/fxml/login.fxml";
    private static final String DASHBOARD_RESOURCE =
            "/io/github/jhonpoved01/univote/fxml/app-shell.fxml";
    private static final String ELECTIONS_RESOURCE =
            "/io/github/jhonpoved01/univote/fxml/elections.fxml";
    private static final String RECEIPT_RESOURCE =
            "/io/github/jhonpoved01/univote/fxml/vote-receipt.fxml";

    private final Stage stage;
    private final AppConfig appConfig;
    private final AuthenticationService authenticationService;
    private final VotingService votingService;
    private final Executor applicationExecutor;
    private Scene scene;

    public SceneNavigator(
            Stage stage,
            AppConfig appConfig,
            AuthenticationService authenticationService,
            VotingService votingService,
            Executor applicationExecutor) {
        this.stage = Objects.requireNonNull(stage, "stage no puede ser null");
        this.appConfig = Objects.requireNonNull(appConfig, "appConfig no puede ser null");
        this.authenticationService = Objects.requireNonNull(
                authenticationService, "authenticationService no puede ser null");
        this.votingService = Objects.requireNonNull(votingService, "votingService no puede ser null");
        this.applicationExecutor = Objects.requireNonNull(
                applicationExecutor, "applicationExecutor no puede ser null");
    }

    public void showLogin() {
        Parent root = loadView(LOGIN_RESOURCE, type -> {
            if (type == LoginController.class) {
                return new LoginController(authenticationService, applicationExecutor, this);
            }
            throw unsupportedController(type);
        });
        show(root);
    }

    public void showDashboard(UserSession session) {
        Objects.requireNonNull(session, "session no puede ser null");
        Parent root = loadView(DASHBOARD_RESOURCE, type -> {
            if (type == DashboardController.class) {
                return new DashboardController(session, this);
            }
            throw unsupportedController(type);
        });
        show(root);
    }

    public void showElections(UserSession session) {
        Objects.requireNonNull(session, "session no puede ser null");
        Parent root = loadView(ELECTIONS_RESOURCE, type -> {
            if (type == ElectionsController.class) {
                return new ElectionsController(session, votingService, applicationExecutor, this);
            }
            throw unsupportedController(type);
        });
        show(root);
    }

    public void showVoteReceipt(UserSession session, VoteReceipt receipt) {
        Objects.requireNonNull(session, "session no puede ser null");
        Objects.requireNonNull(receipt, "receipt no puede ser null");
        Parent root = loadView(RECEIPT_RESOURCE, type -> {
            if (type == VoteReceiptController.class) {
                return new VoteReceiptController(session, receipt, this);
            }
            throw unsupportedController(type);
        });
        show(root);
    }

    private Parent loadView(
            String resourcePath, javafx.util.Callback<Class<?>, Object> controllerFactory) {
        URL resource = SceneNavigator.class.getResource(resourcePath);
        if (resource == null) {
            throw new ViewNavigationException("No fue posible cargar la vista solicitada");
        }
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setControllerFactory(controllerFactory);
        try {
            return loader.load();
        } catch (IOException | RuntimeException exception) {
            throw new ViewNavigationException("No fue posible cargar la vista solicitada", exception);
        }
    }

    private void show(Parent root) {
        if (scene == null) {
            scene = new Scene(root, appConfig.initialWidth(), appConfig.initialHeight());
            stage.setTitle(appConfig.windowTitle());
            stage.setMinWidth(appConfig.minimumWidth());
            stage.setMinHeight(appConfig.minimumHeight());
            stage.setScene(scene);
            stage.show();
        } else {
            scene.setRoot(root);
        }
    }

    private static ViewNavigationException unsupportedController(Class<?> type) {
        return new ViewNavigationException("No fue posible crear el controlador de la vista");
    }
}
