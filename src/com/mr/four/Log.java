package com.mr.four;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class Log {

	private File file;
	private FileOutputStream fos;
	private BufferedOutputStream bos;

	public void openFile() throws Exception {
		if (file != null)
			throw new Exception();
		int highest = 1;
		File dir = new File(".");
		if (dir.exists() && dir.isDirectory()) {
			File[] children = dir.listFiles();
			if (children != null)
				for (File f : children)
					if (f.isFile()) {
						String name = f.getName();
						if (name.matches("debug\\d+\\.xml")) {
							int number = Integer.parseInt(name.replaceFirst("^debug(\\d+)\\.xml$", "$1"));
							if (number >= highest)
								highest = number + 1;
						}
					}
		}
		openFile(String.format("debug%03d.xml", highest));
	}

	public void openFile(String fileName) throws Exception {
		if (file != null)
			throw new Exception();
		try {
			file = new File(fileName);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
		} catch(Exception e) {
			_closeFile();
			throw e;
		}
		writeFile("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
	}

	public void closeFile() throws Exception {
		if (file != null) {
			for (;;) {
				String tos = path.peek();
				if (tos == null)
					break;
				closeNode();
			}
			_closeFile();
		}
	}

	private void _closeFile() throws Exception {
		if (bos != null)
			bos.close();
		bos = null;
		if (fos != null)
			fos.close();
		fos = null;
		file = null;
	}

	private void writeFile(String s) throws Exception {
		if (bos == null)
			throw new Exception();
		byte[] b = s.getBytes(StandardCharsets.UTF_8);
		bos.write(b, 0, b.length);
	}

	private LinkedList<String> path = new LinkedList<>();

	private String tabs() {
		return new String(new char[path.size()]).replace("\0", "\t");
	}

	private void makeNode(String element, boolean closed, String... attributes) throws Exception {
		if (path.contains(element))
			for (;;) {
				String tos = path.peek();
				if (tos == null)
					break; // should be impossible
				closeNode();
				if (tos.equals(element))
					break;
			}
		StringBuilder sb = new StringBuilder(tabs());
		sb.append("<");
		sb.append(element);
		for (int i = 0; i + 1 < attributes.length; i += 2) {
			sb.append(" ");
			sb.append(attributes[i]);
			sb.append("=\"");
			sb.append(attributes[i + 1]);
			sb.append("\"");
		}
		sb.append(closed ? "/>" : ">");
		sb.append("\n");
		writeFile(sb.toString());
		if (!closed)
			path.push(element);
	}

	public void openNode(String element, String... attributes) throws Exception {
		makeNode(element, false, attributes);
	}

	public void logNode(String element, String... attributes) throws Exception {
		makeNode(element, true, attributes);
	}

	public void openNode(byte color, byte level, String... attributes) throws Exception {
		makeNode((color == 1 ? "w" : "b") + level, false, attributes);
	}

	public void logNode(byte color, byte level, String... attributes) throws Exception {
		makeNode((color == 1 ? "w" : "b") + level, true, attributes);
	}

	public void closeNode() throws Exception {
		String element = path.pop();
		if (element == null)
			throw new Exception();
		writeFile(tabs() + "</" + element + ">\n");
	}

}
