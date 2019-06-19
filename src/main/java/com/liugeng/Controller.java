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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
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
    private Button openFile;
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
        if (file.exists()) {
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
                CompletableFuture<HandleResult> future = CompletableFuture.supplyAsync(new FileHandleTask(file, newFile));
                Timeline timeline = new Timeline();
                timeline.setCycleCount(Timeline.INDEFINITE);
                KeyFrame keyFrame = new KeyFrame(Duration.millis(200), event1 -> {

                });
                timeline.getKeyFrames().add(keyFrame);
                timeline.play();
                future.whenCompleteAsync((handleResult, throwable) -> {
                    timeline.stop();
                    if (handleResult.isSuccess()) {
                        popupSuccess(handleResult.getResult(), "处理成功！");
                    } else {
                        popupSuccess(handleResult.getResult(), "处理失败！");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class FileHandleTask implements Supplier<HandleResult> {
        private File file;
        private File newFile;
        public FileHandleTask(File file, File newFile) {
            this.file = file;
            this.newFile = newFile;
        }
        @Override
        public HandleResult get() {
            String result;
            boolean isSuccess;
            try (RandomAccessFile rdFile = new RandomAccessFile(file, "r")) {
                long len = rdFile.length();
                long start = rdFile.getFilePointer();
                long nextEnd = start + len - 1;
                rdFile.seek(nextEnd);
                int pointer;
                String readStr;
                while (nextEnd > start) {
                    pointer = rdFile.read();
                    if (pointer == '\n' || pointer == '\r') {
                        String readOrigin = rdFile.readLine();
                        if (readOrigin != null) {
                            readStr = new String(readOrigin.getBytes(ISO_8859_1), UTF_8) + LINE_SEPARATOR;
                            FileUtils.write(newFile, readStr, UTF_8, true);
                        }
                        nextEnd--;
                    }
                    nextEnd--;
                    if(nextEnd != -1) {
                        rdFile.seek(nextEnd);
                    }
                    if (nextEnd == 0) {
                        String readOrigin = rdFile.readLine();
                        if (readOrigin != null) {
                            readStr = new String(readOrigin.getBytes(ISO_8859_1), UTF_8) + LINE_SEPARATOR;
                            FileUtils.write(newFile, readStr, UTF_8, true);
                        }
                    }
                }
                result = "你的文件已经处理完了，地址位于：" + newFile.getPath();
                isSuccess = true;
            } catch (Exception e) {
                log.error("读取文件:[{}]失败！", file.getPath(), e);
                result = "处理失败，报错信息：" + e.getMessage();
                isSuccess = false;
            }
            return HandleResult.builder().result(result).success(isSuccess).build();
        }
    }

    private void popupSuccess(String msg, String title) {
        Platform.runLater(() -> {
            try {
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

    @Data
    @Builder
    private static class HandleResult {
        private String result;
        private boolean success;
    }
}
