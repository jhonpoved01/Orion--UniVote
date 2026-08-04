package io.github.jhonpoved01.univote;

import io.github.jhonpoved01.univote.config.AppConfig;
import io.github.jhonpoved01.univote.exception.ConfigurationException;
import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class UniVoteApplication extends Application {

    private static final String SHELL_RESOURCE =
            "/io/github/jhonpoved01/univote/fxml/app-shell.fxml";

    @Override
    public void start(Stage stage) throws IOException {
        AppConfig config = AppConfig.load();
        URL shellUrl = requireResource(SHELL_RESOURCE);
        Parent root = FXMLLoader.load(shellUrl);

        Scene scene = new Scene(root, config.initialWidth(), config.initialHeight());
        stage.setTitle(config.windowTitle());
        stage.setMinWidth(config.minimumWidth());
        stage.setMinHeight(config.minimumHeight());
        stage.setScene(scene);
        stage.show();
    }

    private static URL requireResource(String path) {
        URL resource = UniVoteApplication.class.getResource(path);
        if (resource == null) {
            throw new ConfigurationException("No se encontró el recurso requerido: " + path);
        }
        return resource;
    }
}
