package me.ryan_clark.rpg_map_editor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
				tiles[row][tile] = new Tile(0, Tile.CollisionType.NONE);
			}
		}
	}
	
	// Parse map from file
	public Map(File f) throws Exception {
		
		int w = 0, h = 0;
		Tile[][] newTiles = null;
		
		// Attempt to read from input file
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			boolean first = true;
			int row = 0;
			for(String line; (line = br.readLine()) != null; ) {
				if(first) {
					first = false;
					String[] split = line.split("\\s+");
					w= Integer.parseInt(split[0]);
					h = Integer.parseInt(split[1]);
					newTiles = new Tile[h][w];
				} else {
					String[] split = line.split(", ");
					for(int i = 0; i < split.length; i++) {
						String[] split2 = split[i].split("\\s+");
						newTiles[row][i] = new Tile(Integer.parseInt(split2[0]), Tile.collisionTypeFromId(Integer.parseInt(split2[1])));
					}
					row++;
				}
			}
		}
		
		// Only change local data if successful parse
		this.width = w;
		this.height = h;
		this.tiles = newTiles;
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
	
	
	public void saveMap(File f) {
		try {
			PrintWriter writer = new PrintWriter(f);
			writer.println(width + " " + height);
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					if(x != 0) {
						writer.print(", ");
					}
					Tile t = tiles[y][x];
					writer.print(t.spriteId + " " + Tile.collisionIdFromType(t.collisionId));
					if(x == height - 1) {
						writer.println();
						
					}
				}
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void saveMapSerialized(File f) {
		try {
			FileOutputStream fileOut = new FileOutputStream(f);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Image getSheet() {
		return new Image(tileSheet);
	}

}
