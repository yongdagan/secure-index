package com.gmail.yongdagan.secure_index.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.gmail.yongdagan.secure_index.dataobject.Index;

public class AnalyzerUtil {
	
	public static Index stemming(int startId, Path path) throws IOException {
		final Index index = new Index(startId);
		if(Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
    			@Override
    			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
    				if(path.getFileName().toString().endsWith(".txt")) {
    					getToken(path, index);
    				}
    				return FileVisitResult.CONTINUE;
    			}
    		});
		} else {
			getToken(path, index);
		}
		return index;
	}
	
	private static void getToken(Path path, Index index) {
		// convert a file to tokens
		try(BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset())) {
			String line;
			while((line = reader.readLine()) != null) {
				line = line.trim().toLowerCase();
				char[] data = line.toCharArray();
				StringBuffer buffer = new StringBuffer();
				for(int i = 0; i < data.length; i ++) {
					if(data[i] == ' ' || data[i] == '\t' || (data[i] >= 0x4e00 && data[i] <= 0x9fbb)) {
						String tmp = buffer.toString();
						if(!tmp.isEmpty()) {
							index.addTerm(tmp);
						}
						// Chinese
						if(data[i] >= 0x4e00 && data[i] <= 0x9fbb) {
							index.addTerm(Character.toString(data[i]));
						}
						// reset
						buffer = new StringBuffer();
					} else {
						buffer.append(data[i]);
					}
				}
				String tmp = buffer.toString();
				if(!tmp.isEmpty()) {
					index.addTerm(tmp);
				}
			}
		} catch (IOException e) {
			// ignore
		}
		
		String fileName = path.getFileName().toString();
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		index.markDocId(fileName);
	}
	
}
