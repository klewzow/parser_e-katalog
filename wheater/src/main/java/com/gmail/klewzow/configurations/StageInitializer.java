package com.gmail.klewzow.configurations;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StageInitializer implements ApplicationListener<JavaFxApplication.StageReadyEvent> {
    private final int width;
    private final int height;

    {
        width = 600;
        height = 400;
    }


    @Value("${fxml.screen.file.start}")
    private Resource chartResource;
    @Value("${fxml.screen.file.style}")
    private Resource style;
    private ApplicationContext context;
    private String applicationTitle;

    public StageInitializer(@Value("${fxml.screen.title}") String applicationTitle,
                            ApplicationContext context) {
        this.applicationTitle = applicationTitle;
        this.context = context;
    }

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void onApplicationEvent(JavaFxApplication.StageReadyEvent event) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(chartResource.getURL());
            fxmlLoader.setControllerFactory(aClass -> context.getBean(aClass));
            Parent parent = fxmlLoader.load();
            Scene scene = new Scene(parent);
            scene.getStylesheets().add(String.valueOf(style.getURL()));
            scene.setRoot(parent);
            Stage stage = event.getStage();
            stage.initStyle(StageStyle.UNDECORATED);
            parent.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset = stage.getX() - event.getScreenX();
                    yOffset = stage.getY() - event.getScreenY();
                }
            });
            parent.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    stage.setX(event.getScreenX() + xOffset);
                    stage.setY(event.getScreenY() + yOffset);
                }
            });
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setTitle(applicationTitle);
            stage.setScene(scene);
            stage.setTitle(applicationTitle);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
