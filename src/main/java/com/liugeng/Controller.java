package com.liugeng;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

@Slf4j
public class Controller implements Initializable {
    @FXML
    private AnchorPane root;
    @FXML
    private VBox vBox;
    @FXML
    private Label handleInfo;
    @FXML
    private Button openFileButton;
    private FileChooser fileChooser;

    private static final String NEW_FILE_PREFIX = "new-";
    private Window currentWindow;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser = new FileChooser();
    }

    public void openFile(Event event) {
        currentWindow = root.getScene().getWindow();
        File file = fileChooser.showOpenDialog(currentWindow);
        String newName;
        String newPath;
        if (file != null && file.exists()) {
            handleInfo.setVisible(false);
            newName = NEW_FILE_PREFIX + file.getName();
            newPath = file.getParent() + DIR_SEPARATOR + newName;
            File newFile = new File(newPath);
            try {
                if (newFile.exists()) {
                    if (newFile.delete()) {
                        log.info("已经存在文件：{} 了，先删掉！", newFile.getName());
                    }
                }
                if (newFile.createNewFile()) {
                    log.info("新建文件：{}", newFile.getName());
                }
                CompletableFuture<FileHandleTask.HandleResult> future = CompletableFuture.supplyAsync(new FileHandleTask(openFileButton, file, newFile));
                FileHandleProgress progress = new FileHandleProgress(vBox);
                progress.start();
                future.whenCompleteAsync((handleResult, throwable) -> {
                    if (handleResult.isSuccess()) {
                        popupSuccess(progress, handleResult.getResult(), "处理成功！");
                    } else {
                        popupSuccess(progress, handleResult.getResult(), "处理失败！");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void popupSuccess(FileHandleProgress progress, String msg, String title) {
        Platform.runLater(() -> {
            try {
                openFileButton.setText("打开文件");
                openFileButton.setDisable(false);
                handleInfo.setVisible(true);
                handleInfo.setText(msg);
                progress.stop();
                Alert _alert = new Alert(Alert.AlertType.INFORMATION);
                _alert.setTitle(title);
                _alert.setContentText(msg);
                _alert.setHeaderText(title);
                _alert.initOwner(currentWindow);
                _alert.show();
                log.info(msg);
            } catch (Exception e) {
                log.error("popup的时候发生错误", e);
            }
        });
    }
}
