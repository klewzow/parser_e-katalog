package com.gmail.klewzow;

import com.gmail.klewzow.interfaces.LoggerInterface;
import javafx.scene.control.TextArea;
import org.springframework.stereotype.Component;

@Component
public class Logger implements LoggerInterface {

    private TextArea textArea;
    private String log;

    public Logger() {
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    @Override
    public void setField(Object field) {
        this.textArea = (TextArea) field;
    }

    @Override
    public void burn(String str) {
        this.textArea.appendText(str +"\n");
    }

    @Override
    public String toString() {
        return "Logger{" +
                "textArea=" + textArea +
                ", log='" + log + '\'' +
                '}';
    }
}
