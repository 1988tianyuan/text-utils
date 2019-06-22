package com.liugeng;

import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FileHandleProgress {
    private final ProgressIndicator progressIndicator;
    private final VBox vBox;

    public FileHandleProgress(VBox vBox) {
        this.vBox = vBox;
        this.progressIndicator = (ProgressIndicator) vBox.getChildren().get(0);
        this.progressIndicator.setDisable(false);
        this.vBox.setVisible(false);
    }

    public void start() {
        progressIndicator.setDisable(true);
        progressIndicator.setProgress(-1);
        vBox.setVisible(true);
    }

    public void stop() {
        progressIndicator.setDisable(false);
        vBox.setVisible(false);
    }
}
