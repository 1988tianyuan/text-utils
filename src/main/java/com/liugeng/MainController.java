package com.liugeng;

import com.liugeng.model.HandleData;
import com.liugeng.model.HandleResult;
import com.liugeng.strategy.FileConvertType;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static com.liugeng.strategy.FileConvertType.FILE_REVERSE;
import static com.liugeng.strategy.FileConvertType.REPIETER_CONVERT;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;

@Slf4j
public class MainController implements Initializable {
    @FXML
    private AnchorPane root;
    @FXML
    private VBox vBox;
    @FXML
    private Label handleInfo;
    @FXML
    private Button openFileButton;
    @FXML
    private RadioButton chooseFileReverse;
    @FXML
    private RadioButton chooseRepetierConvert;
    @FXML
    private AnchorPane repetierInput;
    @FXML
    private TextField falseVELCP;
    @FXML
    private TextField trueVELCP;

    private FileChooser fileChooser;

    private FileConvertType currentType = REPIETER_CONVERT;

    private Map<FileConvertType, Map<String, Object>> dataMap = new HashMap<>();

    private Window currentWindow;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser = new FileChooser();
        ToggleGroup group = new ToggleGroup();
        chooseFileReverse.setToggleGroup(group);
        chooseRepetierConvert.setToggleGroup(group);
        chooseRepetierConvert.setSelected(true);
        chooseRepetierConvert.requestFocus();
        renewRepetierData(null);
        group.selectedToggleProperty().addListener((observable, oldRadio, newRadio) -> {
            if (group.getSelectedToggle() != null) {
                if (newRadio == chooseFileReverse) {
                    repetierInput.setVisible(false);
                    currentType = FILE_REVERSE;
                } else {
                    repetierInput.setVisible(true);
                    renewRepetierData(null);
                    currentType = REPIETER_CONVERT;
                }
            }
        });
    }

    public boolean renewRepetierData(Event event) {
        Map<String, Object> data;
        String falseVELCPValue = falseVELCP.getText();
        String trueVELCPValue = trueVELCP.getText();
        if (!dataMap.containsKey(REPIETER_CONVERT)) {
            data = new HashMap<>();
            dataMap.put(REPIETER_CONVERT, data);
        } else {
            data = dataMap.get(REPIETER_CONVERT);
        }
        try {
            data.put("falseVELCP", NumberUtils.createDouble(falseVELCPValue));
            data.put("trueVELCP", NumberUtils.createDouble(trueVELCPValue));
        } catch (Exception e) {
            popupAnyError("请输入正确格式的数字！", "输入不合法");
            return false;
        }
        return true;
    }

    public void openFile(Event event) {
        if (!renewRepetierData(null)) {
            return;
        }
        currentWindow = root.getScene().getWindow();
        File file = fileChooser.showOpenDialog(currentWindow);
        String newName;
        String newPath;
        if (file != null && file.exists()) {
            handleInfo.setVisible(false);
            newName = currentType + "-" + file.getName();
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
                FileHandleTask task = new FileHandleTask(currentType, openFileButton, file, newFile, dataMap.get(currentType));
                CompletableFuture<HandleResult> future = CompletableFuture.supplyAsync(task);
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

    private void popup(String msg, String title) {
        Alert _alert = new Alert(Alert.AlertType.INFORMATION);
        _alert.setTitle(title);
        _alert.setContentText(msg);
        _alert.setHeaderText(title);
        _alert.initOwner(currentWindow);
        _alert.show();
        log.info(msg);
    }

    private void popupAnyError(String msg, String title) {
        Platform.runLater(() -> popup(msg, title));
    }

    private void popupSuccess(FileHandleProgress progress, String msg, String title) {
        Platform.runLater(() -> {
            try {
                openFileButton.setText("打开文件");
                openFileButton.setDisable(false);
                handleInfo.setVisible(true);
                handleInfo.setText(msg);
                progress.stop();
                popup(msg, title);
            } catch (Exception e) {
                log.error("popup的时候发生错误", e);
            }
        });
    }
}
