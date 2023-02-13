package com.gmail.klewzow.controller;

import com.gmail.klewzow.interfaces.LoggerInterface;
import com.gmail.klewzow.parser.ECommerceParser;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Component
public class ParseController {

    private ECommerceParser eCommerceParser;
    private String url;
    private LoggerInterface loger;

    public ParseController(ECommerceParser eCommerceParser, LoggerInterface loger) {
        this.eCommerceParser = eCommerceParser;
        this.loger = loger;
    }

    private FileChooser fileChooser;
    private boolean run = false;
    private String[] parameterFile = {"txt file", "*.txt"};
    Properties properties = new Properties();

    private File file;
    private File folder;

    {
        folder = new File("e_comerce_img");
    }


    @FXML
    private FontAwesomeIcon btnClose;

    @FXML
    private Button btnExit;


    @FXML
    private Button btnStart;


    @FXML
    private Button btnStop;
    @FXML
    private Button btnSelectFile;

    @FXML
    private Text filePath;

    @FXML
    private TextArea logField;

    @FXML
    private TextField urlField;

    @FXML
    void selectFile(ActionEvent event) {
        this.fileChooser = new FileChooser();
        this.fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(parameterFile[0], parameterFile[1]));
        List<File> f = this.fileChooser.showOpenMultipleDialog(null);
        if (f != null) {
            filePathWriter(f.stream().findFirst().get().getAbsolutePath());
            buttonsDisable(false, btnStart);


            setProperties("log4j.appender.file.File",f.stream().findFirst().get().getAbsolutePath());

            String p = properties.getProperty("log4j.appender.file.File");
            System.out.println("-------------------------------------------------------------------"+p );
            loger.burn("File: " + f.stream().findFirst().get().getName());
        } else {
            loger.burn("File not selected");
            filePathWriter("none:");
            buttonsDisable(true, btnStart);
        }
    }

    @FXML
    void handlerClose(MouseEvent event) {
        if (event.getSource() == btnClose || event.getSource() == btnExit) {
            System.exit(0);
        }
    }

    @FXML
    void pauseProgram(ActionEvent event) {
        loger.burn("pauseProgram");
    }

    @FXML
    void startProgram(ActionEvent event) {
        Thread tr = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    if (eCommerceParser.go(url, loger, file, folder)) {
                        loger.burn("stopProgram");
                        runProgramUnblockButton();
                    }
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }, "ThreadKatusha");
        tr.start();

        tr.isDaemon();
        runProgramBlockButton();
    }

    @FXML
    void stopProgram(ActionEvent event) {
        this.url = urlField.getText();
        loger.burn("stopProgram");
        runProgramUnblockButton();
    }


    @FXML
    void initialize() {
        loger.setField(logField);
        filePathWriter(filePath.getText());
        buttonsDisable(true, btnStart);
        loger.burn("Start program:");
    }


    private void setFile(File f) {
        this.file = f;
    }

    public void handlerClicks(ActionEvent actionEvent) {
    }

    @FXML
    void handlerUrl(ActionEvent event) {
        buttonsDisable(false, btnStart);
    }

    @FXML
    void handlerOnKeyPressedUrl(KeyEvent event) {
        buttonsDisable(false, btnStart);
    }

    @FXML
    void handlerOnMousePressedUrl(MouseEvent event) {
        buttonsDisable(false, btnStart);
    }

    private void isFile(File file) {
        if (file != null && file.isFile()) {
            setFile(file);
            loger.burn("File: " + file.getPath());
        } else if (!file.getName().equals("")) {
            createTextFile(file);
            createFolder(file);
        } else {
            buttonsDisable(true, btnStart);
            loger.burn("File not found SE : " + file.getName());
        }
    }

    private void filePathWriter(String url) {
        if (!filePath.getText().startsWith("none") && filePath.getText() != null) {
            filePath.setText(url);
            this.file = new File(url);
            isFile(this.file);
            createFolder(this.file);
        }
    }

    private void buttonsDisable(boolean value, Button... args) {
        for (Button btn : args) {
            btn.setDisable(value);
        }
    }

    private void buttonsDisable(boolean value, Button btn) {
        if (btn == btnStart) {
            if (urlField.getText().startsWith("URL") || urlField.getText().equals("") || urlField.getText().length() < 7 || !urlField.getText().startsWith("http") || filePath.getText().startsWith("none") || filePath.getText().length() < 7 || run) {
                btn.setDisable(true);
                return;
            } else {
                btn.setDisable(value);
                this.url = urlField.getText();
            }
        } else {
            btn.setDisable(value);
        }
    }

    public void runProgramBlockButton() {
        run = true;
        buttonsDisable(true, btnStart, btnSelectFile);
        urlField.setDisable(true);
    }

    public void runProgramUnblockButton() {
        run = false;
        buttonsDisable(false, btnStart, btnSelectFile);
        urlField.setDisable(false);
    }

    private void createTextFile(File file) {
        try {
            file.createNewFile();
            loger.burn("File create : " + file.getAbsolutePath());
        } catch (IOException e) {
            loger.burn("File ERROR");
        }
    }

    private void createFolder(File file) {
        this.folder = new File(file.getAbsolutePath().replace(file.getName(), "" + folder.getName()));
        if (!folder.isDirectory()) {
            folder.mkdir();
            loger.burn("Folder create " + folder.getAbsolutePath());
        }
    }

    private static int res = 0;

    public static void tick() {
        res += 1;
        System.out.println(res);
    }

    public void setProperties(String key, String value) {
        properties.setProperty(key, value);
    }
}


