package me.ryan_clark.rpg_map_editor;

import java.io.Serializable;

import javafx.scene.image.Image;

public class Map implements Serializable {

	private static final long serialVersionUID = 4718329832053403359L;
	private int width;
	private int height;
	
	private String tileSheet = "/tiles/tileset_01.png";
	
	private Tile[][] tiles;
	
	// Create new blank map
	public Map(int width, int height) {
		this.width = width;
		this.height = height;
		this.tiles = new Tile[height][width];
		
		for(int row = 0; row < height; row++) {
			for(int tile = 0; tile < width; tile++) {
				tiles[row][tile] = new Tile(0, 0);
			}
		}
	}
	
	public Map(int width, int height, Tile[][] tiles) {
		this.width = width;
		this.height = height;
		this.tiles = tiles;
	}
	
	public void setTile(int x, int y, Tile t) {
		if(x < 0 || y < 0 || x > width || y > height) {
			System.out.println("Error in tile set: Tile (" + x + ", " + y + ") out of bounds. Size is [" + width + ", " + height + "]");
		} else {
			tiles[y][x] = t;
		}
	}
	
	public final Tile getTile(int x, int y) {
		if(x < 0 || y < 0 || x > width || y > height) {
			System.out.println("Error in tile get: Tile (" + x + ", " + y + ") out of bounds. Size is [" + width + ", " + height + "]");
			return null;
		}
		return tiles[y][x];
	}
	
	public final int getWidth() {
		return width;
	}
	
	public final int getHeight() {
		return height;
	}
	
	public Image getSheet() {
		return new Image(tileSheet);
	}

}
