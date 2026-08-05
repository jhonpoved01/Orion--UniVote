package io.github.jhonpoved01.univote;

import io.github.jhonpoved01.univote.config.AppConfig;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.stage.Stage;

public final class UniVoteApplication extends Application {

    private ExecutorService applicationExecutor;

    @Override
    public void start(Stage stage) {
        AppConfig config = AppConfig.load();
        ApplicationServices services = ApplicationServices.create();
        applicationExecutor = Executors.newSingleThreadExecutor(
                Thread.ofPlatform().daemon().name("univote-application-worker").factory());
        SceneNavigator navigator = new SceneNavigator(
                stage, config, services.authenticationService(), services.votingService(),
                applicationExecutor);

        navigator.showLogin();
    }

    @Override
    public void stop() {
        if (applicationExecutor != null) {
            applicationExecutor.shutdownNow();
        }
    }
}
