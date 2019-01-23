package me.ryan_clark.rpg_map_editor.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import me.ryan_clark.rpg_map_editor.Map;
import me.ryan_clark.rpg_map_editor.SubImage;
import me.ryan_clark.rpg_map_editor.Tile;

public class GuiController implements Initializable {
	
	// private File openedMap = null;
	private BooleanProperty openedFile = new SimpleBooleanProperty();
	private Map currentMap = null;

	private Canvas mapCanvas;
	private Canvas tilesetCanvas;
	
	private Rectangle mapHover;
	private Rectangle tilesetHover;
	
	private int selectedSprite = -1;
	
	private int scaleIndex = 12;
	private final double[] scales = new double[]{0.05, 0.06, 0.07, 0.08, 0.1, 0.13, 0.17, 0.2, 0.25, 0.33, 0.5, 0.67, 1.0, 1.5, 2.0};
		
	@FXML
	private Button buttonNew;
	
	@FXML
	private Button buttonOpen;
	
	@FXML
	private Button buttonSave;
	
	@FXML
	private Label zoomLabel;
	
	@FXML
	private Button zoomIn;
	
	@FXML
	private Button zoomOut;
	
	@FXML
	private ScrollPane mapPane;
	
	@FXML
	private Pane tiles;
	
	@FXML
	private Pane sheet;
	


	@Override
	public void initialize(URL location, ResourceBundle resources) {
				
		buttonNew.setGraphic(new ImageView(new Image("/style/icons/pencil.png")));
		buttonOpen.setGraphic(new ImageView(new Image("/style/icons/sign-out.png")));
		buttonSave.setGraphic(new ImageView(new Image("/style/icons/sign-in.png")));
		zoomOut.setGraphic(new ImageView(new Image("/style/icons/zoom-out.png")));
		zoomIn.setGraphic(new ImageView(new Image("/style/icons/zoom-in.png")));
		
		mapPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		mapPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		
		openedFile.set(false);
		buttonSave.disableProperty().bind(openedFile.not());
		
		zoomIn.setDisable(true);
		zoomOut.setDisable(true);
		
		buttonNew.setOnAction(e -> {
			newMap();
		});
		
		FileChooser.ExtensionFilter genFilter = new FileChooser.ExtensionFilter("All map files (*.ser, *.map)", "*.ser", "*.map");
		FileChooser.ExtensionFilter serFilter = new FileChooser.ExtensionFilter("Serialized map files (*.ser)", "*.ser");
		FileChooser.ExtensionFilter mapFilter = new FileChooser.ExtensionFilter("Text map files (*.map)", "*.map");
		
		buttonOpen.setOnAction(e -> {

			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().addAll(genFilter, serFilter, mapFilter);
			fc.setTitle("Open File");
			fc.setInitialDirectory(new File(System.getProperty("user.dir")));
			File f = fc.showOpenDialog(buttonOpen.getScene().getWindow());
			if (f != null) {
				String extension = "";
				int i = f.getAbsolutePath().lastIndexOf('.');
				if (i > 0) {
					extension = f.getAbsolutePath().substring(i + 1);
				}
				if(extension.equals("map")) {
					openMapFromFile(f);
				} else if(extension.equals("ser")) {
					openMapFromFileSerializable(f);
				}
			}
			
		});
		
		buttonSave.setOnAction(e -> {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().addAll(serFilter, mapFilter);
			fc.setTitle("Save As");
			fc.setInitialDirectory(new File(System.getProperty("user.dir")));
			File f = fc.showSaveDialog(buttonOpen.getScene().getWindow());
			if (f != null) {
				String extension = "";
				int i = f.getAbsolutePath().lastIndexOf('.');
				if (i > 0) {
					extension = f.getAbsolutePath().substring(i + 1);
				}
				if(extension.equals("ser")) {
					currentMap.saveMapSerialized(f);
				} else {
					currentMap.saveMap(f);
				}
			}
			
		});
		
		zoomOut.setOnAction(e -> {
			scaleIndex--;
			if(scaleIndex <= 0) {
				zoomOut.setDisable(true);
			} else {
				zoomOut.setDisable(false);
				zoomIn.setDisable(false);
			}
			applyZoom();
		});
		
		zoomIn.setOnAction(e -> {
			scaleIndex++;
			if(scaleIndex >= scales.length - 1) {
				zoomIn.setDisable(true);
			} else {
				zoomIn.setDisable(false);
				zoomOut.setDisable(false);
			}
			applyZoom();
		});
		
	}
		
	private void applyZoom() {
		mapCanvas.setScaleX(scales[scaleIndex]);
		mapCanvas.setScaleY(scales[scaleIndex]);
		mapCanvas.setTranslateX(- ((52 * currentMap.getWidth()) - (52 * currentMap.getWidth() * scales[scaleIndex])) / 2);
		mapCanvas.setTranslateY(-((52 * currentMap.getHeight()) - (52 * currentMap.getHeight() * scales[scaleIndex])) / 2);
		tiles.setPrefSize(52 * currentMap.getWidth() * scales[scaleIndex], 52 * currentMap.getHeight() * scales[scaleIndex]);
		
		mapHover.setWidth(50 * (scales[scaleIndex]));
		mapHover.setHeight(50 * (scales[scaleIndex]));
		mapHover.setLayoutX(-100);
		
		zoomLabel.setText("Zoom: " + (int) (100 * scales[scaleIndex]) + "%");
	}
	
	// TODO: Convert popup window to FXML
	private void newMap() {
		
		UnaryOperator<Change> integerFilter = change -> {
			String newText = change.getControlNewText();
			if (newText.matches("[0-9]*")) {
				return change;
			}
			return null;
		};
		
		Pane parentPane = new Pane();
		
		Pane widthPane = new Pane();
		Label promptWidth = new Label("Width: ");
		TextField inputWidth = new TextField();
		inputWidth.setLayoutX(75);
		inputWidth.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));
		inputWidth.setText("10");
		inputWidth.setPrefWidth(75);
		widthPane.getChildren().addAll(promptWidth, inputWidth);
		
		Pane heightPane = new Pane();
		heightPane.setLayoutY(30);
		Label promptHeight = new Label("Height: ");
		TextField inputHeight = new TextField();
		inputHeight.setLayoutX(75);
		inputHeight.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));
		inputHeight.setText("10");
		inputHeight.setPrefWidth(75);
		heightPane.getChildren().addAll(promptHeight, inputHeight);
		
		Pane buttonPane = new Pane();
		buttonPane.setLayoutY(60);
		Button okButton = new Button("OK");
		okButton.setPrefWidth(75);
		Button cancelButton = new Button("Cancel");
		cancelButton.setPrefWidth(75);
		cancelButton.setLayoutX(75);
		buttonPane.getChildren().addAll(okButton, cancelButton);
		

		parentPane.getChildren().addAll(widthPane, heightPane, buttonPane);
		parentPane.setLayoutX(10);
		parentPane.setLayoutY(10);
		parentPane.setBackground(null);
		
		Stage popup = new Stage();
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.initOwner(buttonNew.getScene().getWindow());
		
		Scene popupScene = new Scene(parentPane);
		popup.setScene(popupScene);
		popup.setResizable(false);
		popup.setTitle("New Map");
		popup.show();

		cancelButton.setOnAction(e -> {
			popup.close();
		});
		
		okButton.setOnAction(e -> {
			int width = Integer.parseInt(inputWidth.getText());
			int height = Integer.parseInt(inputHeight.getText());
			if(width > 100 || height > 100) {
				System.out.println("Error, max dimensions are 100 x 100");
			} else if(width < 10 || height < 10) {
				System.out.println("Error, min dimensions are 10 x 10");
			} else {
				openMapFromInput(width, height);
				popup.close();
			}
		});
	}
	
	
	private void openMapFromFile(File f) {
		try {
			currentMap = new Map(f);
			setupTiles();
			openedFile.set(true);
			zoomIn.setDisable(false);
			zoomOut.setDisable(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void openMapFromFileSerializable(File f) {
		try {
			FileInputStream fileIn = new FileInputStream(f);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			currentMap = (Map) in.readObject();
			setupTiles();
			openedFile.set(true);
			zoomIn.setDisable(false);
			zoomOut.setDisable(false);
			in.close();
			fileIn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void openMapFromInput(int width, int height) {
		currentMap = new Map(width, height);
		setupTiles();
		openedFile.set(true);
		zoomIn.setDisable(false);
		zoomOut.setDisable(false);
	}
	
	private void setupTiles() {
		
		Image tileSheet = currentMap.getSheet();
		
		// FIRST, work on the map canvas
		mapCanvas = new Canvas(52 * currentMap.getWidth(), 52 * currentMap.getHeight());
		mapCanvas.setLayoutX(5);
		mapCanvas.setLayoutY(5);
		
		// Set up hover rectangle for map canvas
		mapHover = new Rectangle(50, 50);
		mapHover.setLayoutX(-100);
		mapHover.setOpacity(0.5);
		mapHover.setMouseTransparent(true);
		
		// Draw all the tiles from the current map
		final GraphicsContext ctxMap = mapCanvas.getGraphicsContext2D();
		for(int y = 0; y < currentMap.getHeight(); y++) {
			for(int x = 0; x < currentMap.getWidth(); x++) {
				SubImage si = SubImage.getSprite(currentMap.getTile(x, y).spriteId);
				ctxMap.drawImage(tileSheet, si.x, si.y, 50, 50, x * 50, y * 50, 50, 50);
			}
		}
		applyZoom();
		
		// Mouse events to draw tiles (On click or drag)
		mapCanvas.setOnMouseClicked(m -> { drawTile(m, ctxMap);	});
		mapCanvas.setOnMouseDragged(m -> { drawTile(m, ctxMap);	});
		
		// Hovering over a square moves the mapHover rectangle to that location
		mapCanvas.setOnMouseMoved(m -> {
			double x = m.getX();
			double y = m.getY();
			if(x < 50 * currentMap.getWidth() && y < 50 * currentMap.getHeight()) {
				int posX = (int) (x / 50) * 50;
				int posY = (int) (y / 50) * 50;
				mapHover.setLayoutX(posX * scales[scaleIndex] + 5);
				mapHover.setLayoutY(posY * scales[scaleIndex] + 5);
			} else {
				mapHover.setLayoutX(-100);
			}
		});
		
		// Clear tiles pane, add canvas and hover
		tiles.getChildren().removeAll(tiles.getChildren());
		tiles.getChildren().addAll(mapCanvas, mapHover);
		

		
		// SECOND, work on the tileset canvas
		tilesetCanvas = new Canvas(500, 500);
		
		// Set up hover rectangle for tileset canvas
		tilesetHover = new Rectangle(50, 50);
		tilesetHover.setLayoutX(-100);
		tilesetHover.setStroke(Color.CYAN);
		tilesetHover.setStrokeWidth(2.0);
		tilesetHover.setStrokeType(StrokeType.OUTSIDE);
		tilesetHover.setFill(Color.TRANSPARENT);
		
		// Draw all possible tiles in the tile set
		final GraphicsContext ctxTileset = tilesetCanvas.getGraphicsContext2D();
		int sid = 0;
		for(int y = 0; y < 10; y++) {
			for(int x = 0; x < 10; x++) {
				SubImage si = SubImage.getSprite(sid++);
				ctxTileset.drawImage(tileSheet, si.x, si.y, 50, 50, x * 50, y * 50, 50, 50);
			}
		}
		
		// Clicking on a square selects it
		tilesetCanvas.setOnMouseClicked(m -> {
			double x = m.getX();
			double y = m.getY();
			
			int posX = (int) x/50;
			int posY = (int) y/50;
			
			selectedSprite = 10 * posY + posX;

			tilesetHover.setLayoutX(posX * 50);
			tilesetHover.setLayoutY(posY * 50);	
		});
		
		// Clear sheet pane, add canvas and hover
		sheet.getChildren().removeAll(sheet.getChildren());
		sheet.getChildren().addAll(tilesetCanvas, tilesetHover);
		

	}
	
	
	// TODO: Different drawing methods (eg fill, rectangle, line)
	public void drawTile(MouseEvent m, GraphicsContext ctx) {
		double x = m.getX();
		double y = m.getY();
		int posX = (int) x/50;
		int posY = (int) y/50;
		if(selectedSprite != -1) {
			currentMap.setTile(posX, posY, new Tile(selectedSprite, currentMap.getTile(posX, posY).collisionId));
			
			SubImage si = SubImage.getSprite(selectedSprite);
			ctx.drawImage(new Image("/tiles/tileset_01.png"), si.x, si.y, 50, 50, posX * 50, posY * 50, 50, 50);
			
			mapHover.setLayoutX(posX * 50 * scales[scaleIndex] + 5);
			mapHover.setLayoutY(posY * 50 * scales[scaleIndex] + 5);
			
		}
	}
}
