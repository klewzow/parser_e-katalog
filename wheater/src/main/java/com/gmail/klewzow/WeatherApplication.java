package com.gmail.klewzow;

import com.gmail.klewzow.configurations.JavaFxApplication;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static javafx.application.Application.launch;

@SpringBootApplication
public class WeatherApplication  {

    public static void main(String[] args) {

         Application.launch(JavaFxApplication.class, args);


//       SpringApplication.run(WeatherApplication.class, args);
    }

}
