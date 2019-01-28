package me.ryan_clark.rpg_map_editor.gui;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Gui extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		
		URL location = getClass().getResource("/style/layout.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(location);
		Parent root = fxmlLoader.load();
		
		Scene scene = new Scene(root, 1280, 720);
		
		stage.setScene(scene);
		stage.setTitle("MAP EDITOR | v0.1");
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
		stage.setMinWidth(800);
		stage.setMinHeight(600);
		stage.show();
		
		stage.setOnCloseRequest((e) -> {
			System.exit(0);
		});
	}

}
