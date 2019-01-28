package me.ryan_clark.rpg_map_editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

public class MapIO {
	
	public static void saveMap(File f, Map map) {
		try {
			int width = map.getWidth();
			int height = map.getHeight();
			
			PrintWriter writer = new PrintWriter(f);
			writer.println(width + " " + height);
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					if(x != 0) {
						writer.print(", ");
					}
					Tile t = map.getTile(x, y);
					writer.print(t.spriteId + " " + t.collisionId);
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
	
	public static void saveMapSerialized(File f, Map map) {
		try {
			FileOutputStream fileOut = new FileOutputStream(f);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(map);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Map openMap(File f) throws FileNotFoundException, IOException {
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
						newTiles[row][i] = new Tile(Integer.parseInt(split2[0]), Integer.parseInt(split2[1]));
					}
					row++;
				}
			}
		}
		
		// Only return data if successful parse
		return new Map(w, h, newTiles);
	}
}
