package no.sesat.mojo.modes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public abstract class GenerateFile {

	private PrintStream stream;
	private int depth = 0;
	private boolean indent = true;

	private File file;

	protected void init(String name) {
		file = new File(name);

		try {
			stream = new PrintStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected void indent() {
		depth++;
	}

	protected void unindent() {
		depth--;
		if (depth < 0)
			throw new RuntimeException("Indenting below zero");
	}

	protected void printlnI(String string) {
		println(string);
		depth++;
	}

	protected void printlnU(String string) {
		depth--;
		println(string);
	}

	protected void print(String string) {
		if (indent)
			for (int i = 1; i <= depth; i++) {
				stream.print("    ");
			}
		stream.print(string);
		indent = false;
	}

	protected void println(String string) {
		print(string += "\n");
		indent = true;
	}

	protected void done() {
		if (depth != 0)
			throw new RuntimeException("Indenting not balanced.");

		System.out.println("Written file: " + file);
	}
}
