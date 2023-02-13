package com.gmail.klewzow.interfaces;

import javafx.scene.control.TextArea;

public interface LoggerInterface<T> {
    public void setField(T field);
    public void burn(String str);
}
