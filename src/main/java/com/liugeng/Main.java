package com.liugeng;

import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;

@Slf4j
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL bootLocation = getClass().getClassLoader().getResource("sample.fxml");
        if (bootLocation == null) {
            log.error("找不到启动页面！启动失败");
            System.exit(-1);
        }
        Parent root = FXMLLoader.load(bootLocation);
        primaryStage.setTitle("给舟雄的翻转工具");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
