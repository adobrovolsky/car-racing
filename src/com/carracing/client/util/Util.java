package com.carracing.client.util;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class Util {

	@SuppressWarnings("rawtypes")
	public static <T> void refreshTable(TableView<T> table) {
		for (int i = 0; i < table.getColumns().size(); i++) {
			((TableColumn) (table.getColumns().get(i))).setVisible(false);
			((TableColumn) (table.getColumns().get(i))).setVisible(true);
		}
	}
}