package com.carracing.client.util;

import java.text.Format;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class ColumnFormatter<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {
    private final Format format;

    public ColumnFormatter(Format format) {
        this.format = format;
    }
    
    @Override public TableCell<S, T> call(TableColumn<S, T> arg) {
        return new TableCell<S, T>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new Label(format.format(item)));
                }
            }
        };
    }
}