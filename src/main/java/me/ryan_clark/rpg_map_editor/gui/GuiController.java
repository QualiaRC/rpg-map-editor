package me.ryan_clark.rpg_map_editor.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.function.UnaryOperator;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import me.ryan_clark.rpg_map_editor.Action;
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
	
	private Stack<ArrayList<Action>> undoStack = new Stack<ArrayList<Action>>();
	private Stack<ArrayList<Action>> redoStack = new Stack<ArrayList<Action>>();
	
	private ArrayList<Action> currentAction = new ArrayList<Action>();
	
	// layer 1, layer 2, collision
	private int layerMode = 0;
	
	// pencil, fill, rectangle
	private int drawMode = 0;
	
	private boolean ctrlPressed = false;
		
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
	
	@FXML
	private Button undoButton;
	
	@FXML
	private Button redoButton;
	
	@FXML
	private Button layer1Button;
	
	@FXML
	private Button layer2Button;
	
	@FXML
	private Button collisionButton;
	
	@FXML
	private Button paintPencil;
	
	@FXML
	private Button paintFill;
	
	@FXML
	private Button paintRectangle;
	
	
	
	private Button[] layerButtons;
	private Button[] drawButtons;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		layerButtons = new Button[] {layer1Button, layer2Button, collisionButton};
		for(Button b : layerButtons) {
			b.setDisable(true);
		}
		
		drawButtons = new Button[] {paintPencil, paintFill, paintRectangle};
		for(Button b : drawButtons) {
			b.setDisable(true);
		}
		
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
			applyZoom(false);
		});
		
		zoomIn.setOnAction(e -> {
			applyZoom(true);
		});
		
		undoButton.setOnAction(e -> {
			undo();
		});
		
		redoButton.setOnAction(e -> {
			redo();
		});
				
		updateUndoRedo();
		
		layer1Button.setOnAction(e -> {
			changeLayer(0);
		});
		
		layer2Button.setOnAction(e -> {
			changeLayer(1);
		});

		collisionButton.setOnAction(e -> {
			changeLayer(2);
		});
		
		paintPencil.setOnAction(e -> {
			changeDraw(0);
		});
		
		paintFill.setOnAction(e -> {
			changeDraw(1);
		});
		
		paintRectangle.setOnAction(e -> {
			changeDraw(2);
		});
	}
	
	// Sets zoom level back to 100%
	private void resetZoom() {
		scaleIndex = 11;
		applyZoom(true);
	}
		
	// Zooms map canvas in or out
	//  True represents zoom in, false represents zoom out
	private void applyZoom(boolean in) {
		
		// Shift scale index right if zooming in or left if zooming out
		if(in) {
			scaleIndex++;
		} else {
			scaleIndex--;
		}
		
		// If all the way left, disable zoom out
		// Else if all the way right, disable zoom in
		// Else enable both
		if(scaleIndex <= 0) {
			zoomOut.setDisable(true);
		} else if(scaleIndex >= scales.length - 1) {
			zoomIn.setDisable(true);
		} else {
			zoomIn.setDisable(false);
			zoomOut.setDisable(false);
		}
		
		// Scaling calculations on both the canvas and the hover indicator
		mapCanvas.setScaleX(scales[scaleIndex]);
		mapCanvas.setScaleY(scales[scaleIndex]);
		mapCanvas.setTranslateX(- ((52 * currentMap.getWidth()) - (52 * currentMap.getWidth() * scales[scaleIndex])) / 2);
		mapCanvas.setTranslateY(-((52 * currentMap.getHeight()) - (52 * currentMap.getHeight() * scales[scaleIndex])) / 2);
		tiles.setPrefSize(52 * currentMap.getWidth() * scales[scaleIndex], 52 * currentMap.getHeight() * scales[scaleIndex]);
		
		mapHover.setWidth(50 * (scales[scaleIndex]));
		mapHover.setHeight(50 * (scales[scaleIndex]));
		mapHover.setLayoutX(-100);
		
		// Finally, update the zoom label to represent updated zoom level
		zoomLabel.setText("Zoom: " + (int) (100 * scales[scaleIndex]) + "%");
	}
	
	// TODO: Convert pop-up window to FXML
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
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Size Error");
				alert.setHeaderText(null);
				alert.setContentText("Error, max dimensions are 100 x 100");
				alert.showAndWait();
			} else if(width < 10 || height < 10) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Size Error");
				alert.setHeaderText(null);
				alert.setContentText("Error, minimum dimensions are 10 x 10");
				alert.showAndWait();
			} else {
				openMapFromInput(width, height);
				popup.close();
			}
		});
		
		for(TextField t : new TextField[] {inputWidth, inputHeight}) {
			t.setOnKeyPressed(e -> {
				if(e.getCode().equals(KeyCode.ENTER)) {
					int width = Integer.parseInt(inputWidth.getText());
					int height = Integer.parseInt(inputHeight.getText());
					if(width > 100 || height > 100) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Size Error");
						alert.setHeaderText(null);
						alert.setContentText("Error, max dimensions are 100 x 100");
						alert.showAndWait();
					} else if(width < 10 || height < 10) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Size Error");
						alert.setHeaderText(null);
						alert.setContentText("Error, minimum dimensions are 10 x 10");
						alert.showAndWait();
					} else {
						openMapFromInput(width, height);
						popup.close();
					}
				}
			});
		}
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
		
		undoStack.removeAllElements();
		redoStack.removeAllElements();
		updateUndoRedo();
		
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
		resetZoom();
		
		// Mouse events to draw tiles (On click or drag)
		mapCanvas.setOnMousePressed(m -> { 
			redoStack.removeAllElements();
			if(drawMode == 0) {
				drawTile(m, ctxMap);
			} else if(drawMode == 1) {
				fillTile(m, ctxMap);
			}
		});
		mapCanvas.setOnMouseDragged(m -> {
			if(drawMode != 1) {
				redoStack.removeAllElements();
				drawTile(m, ctxMap); 
			}
		});
		
		mapCanvas.setOnMouseDragReleased(m -> {
			if(currentAction.size() != 0) {
				undoStack.push(currentAction);
				currentAction = new ArrayList<Action>();
				updateUndoRedo();
			}
		});
		mapCanvas.setOnMouseReleased(m -> {
			if(currentAction.size() != 0) {
				System.out.println("Adding to undo stack");
				undoStack.push(currentAction);
				currentAction = new ArrayList<Action>();
				updateUndoRedo();
			}
		});
		
		// Hovering over a square moves the mapHover rectangle to that location
		mapCanvas.setOnMouseMoved(m -> {
			double x = m.getX();
			double y = m.getY();
			if(x < 50 * currentMap.getWidth() && y < 50 * currentMap.getHeight() && x > 0 && y > 0) {
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
		
		changeLayer(0);
		
		changeDraw(0);
		
		buttonNew.getScene().setOnKeyPressed(e -> {
			if(e.getCode().equals(KeyCode.CONTROL)) {
				ctrlPressed = true;
			} else if(e.getCode().equals(KeyCode.Z) && ctrlPressed) {
				if(!undoStack.isEmpty()) {
					undo();
				}
			} else if(e.getCode().equals(KeyCode.Y) && ctrlPressed) {
				if(!redoStack.isEmpty()) {
					redo();
				}
			}
		});
		
		buttonNew.getScene().setOnKeyReleased(e -> {
			if(e.getCode().equals(KeyCode.CONTROL)) {
				ctrlPressed = false;
			}
		});
	}
	
	
	// TODO: Different drawing methods (eg fill, rectangle, line)
	private void drawTile(MouseEvent m, GraphicsContext ctx) {		
		mapPane.setPannable(true);
		if(m.getButton() == MouseButton.PRIMARY) {
			mapPane.setPannable(false);
			double x = m.getX();
			double y = m.getY();
			int posX = (int) x/50;
			int posY = (int) y/50;
			if(selectedSprite != -1 && 
					posX < currentMap.getWidth() && posX >= 0 &&
					posY < currentMap.getHeight() && posY >= 0 &&
					currentMap.getTile(posX, posY).spriteId != selectedSprite) {
				Tile newTile = new Tile(selectedSprite, currentMap.getTile(posX, posY).collisionId);
				Action newAction = new Action(new Tile(currentMap.getTile(posX, posY)), newTile, posX, posY); 
				currentAction.add(newAction);
				currentMap.setTile(posX, posY, newTile);
				
				SubImage si = SubImage.getSprite(selectedSprite);
				
				// TODO: use the current map's tile sheet
				ctx.drawImage(new Image("/tiles/tileset_01.png"), si.x, si.y, 50, 50, posX * 50, posY * 50, 50, 50);
			}
			
			mapHover.setLayoutX(posX * 50 * scales[scaleIndex] + 5);
			mapHover.setLayoutY(posY * 50 * scales[scaleIndex] + 5);
		}
	}
	
	private void fillTile(MouseEvent m, GraphicsContext ctx) {
		mapPane.setPannable(true);
		if(m.getButton() == MouseButton.PRIMARY) {
			mapPane.setPannable(false);
			double x = m.getX();
			double y = m.getY();
			int posX = (int) x/50;
			int posY = (int) y/50;
			if(selectedSprite != -1 && 
					posX < currentMap.getWidth() && posX >= 0 &&
					posY < currentMap.getHeight() && posY >= 0 &&
					currentMap.getTile(posX, posY).spriteId != selectedSprite) {
				Tile newTile = new Tile(selectedSprite, currentMap.getTile(posX, posY).collisionId);
				
				Image tileImage = new Image("/tiles/tileset_01.png");
								
				fill(ctx, posX, posY, SubImage.getSprite(selectedSprite), currentMap.getTile(posX, posY), newTile, tileImage);
				
				currentMap.setTile(posX, posY, newTile);
			}
			
		}
	}
	
	private void fill(GraphicsContext ctx, int posX, int posY, SubImage si, Tile oldTile, Tile newTile, Image tileImage) {
		Action newAction = new Action(new Tile(currentMap.getTile(posX, posY)), newTile, posX, posY); 
		currentAction.add(newAction);
		currentMap.setTile(posX, posY, newTile);
		
		ctx.drawImage(tileImage, si.x, si.y, 50, 50, posX * 50, posY * 50, 50, 50);
		
		
		int[] neighborX = new int[] {posX - 1, posX + 1, posX, posX};
		int[] neighborY = new int[] {posY, posY, posY - 1, posY + 1};
		
		for(int i = 0; i < 4; i++) {
			if(neighborX[i] >= 0 && neighborY[i] >= 0 && 
					neighborX[i] < currentMap.getWidth() && neighborY[i] < currentMap.getHeight() &&
					currentMap.getTile(neighborX[i], neighborY[i]).spriteId == oldTile.spriteId) {
				fill(ctx, neighborX[i], neighborY[i], si, oldTile, newTile, tileImage);
			}
		}
	}
	
	private void undo() {
		ArrayList<Action> actions = undoStack.pop();
		
		Image i = new Image("/tiles/tileset_01.png");
		
		for(Action action : actions) {
			currentMap.setTile(action.posX, action.posY, action.oldTile);	
			SubImage si = SubImage.getSprite(action.oldTile.spriteId);
			mapCanvas.getGraphicsContext2D().drawImage(i, si.x, si.y, 50, 50, action.posX * 50, action.posY * 50, 50, 50);
		}
		
		redoStack.push(actions);

		updateUndoRedo();
	}
	
	private void redo() {
		ArrayList<Action> actions = redoStack.pop();
		
		Image i = new Image("/tiles/tileset_01.png");
		
		for(Action action : actions) {
			currentMap.setTile(action.posX, action.posY, action.newTile);	
			SubImage si = SubImage.getSprite(action.newTile.spriteId);
			mapCanvas.getGraphicsContext2D().drawImage(i, si.x, si.y, 50, 50, action.posX * 50, action.posY * 50, 50, 50);			
		}
		
		undoStack.push(actions);
		
		updateUndoRedo();
	}
	
	private void updateUndoRedo() {
		undoButton.setDisable(undoStack.isEmpty());
		redoButton.setDisable(redoStack.isEmpty());
	}
	
	private void changeLayer(int mode) {
		
		layerMode = mode;
		
		for(Button b : layerButtons) {
			b.getStyleClass().remove(1);
			b.setDisable(false);
		}
		
		switch(layerMode) {
			case 1:
				layer1Button.getStyleClass().add("controlButton");
				layer2Button.getStyleClass().add("controlButtonSelected");
				collisionButton.getStyleClass().add("controlButton");
				layer2Button.setDisable(true);
				break;
			case 2:
				layer1Button.getStyleClass().add("controlButton");
				layer2Button.getStyleClass().add("controlButton");
				collisionButton.getStyleClass().add("controlButtonSelected");
				collisionButton.setDisable(true);
				break;
			case 0:
			default:
				layer1Button.getStyleClass().add("controlButtonSelected");
				layer2Button.getStyleClass().add("controlButton");
				collisionButton.getStyleClass().add("controlButton");
				layer1Button.setDisable(true);
				break;
		}
	}
	
	private void changeDraw(int mode) {
		
		drawMode = mode;
		
		for(int i = 0; i < drawButtons.length; i++) {
			drawButtons[i].getStyleClass().remove(1);
			if(i == drawMode) {
				drawButtons[i].setDisable(true);
				drawButtons[i].getStyleClass().add("controlButtonSelected");
			} else {
				drawButtons[i].setDisable(false);
				drawButtons[i].getStyleClass().add("controlButton");
			}
		}
	}
}
