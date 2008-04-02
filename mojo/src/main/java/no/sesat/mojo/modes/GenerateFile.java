package no.sesat.mojo.modes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * This class provide methods that makes it easy to write indented text to a
 * file.
 *
 */
public abstract class GenerateFile {

    private PrintStream stream;
    private int depth = 0;
    private boolean indent = true;
    private File file;

    /**
     * Initialize this generator. It will open the file specified.
     *
     * @param name
     *            Filename
     */
    protected void init(final String name) {
        file = new File(name);

        try {
            stream = new PrintStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Indent one level.
     */
    protected void indent() {
        depth++;
    }

    /**
     * Unindent one level.
     */
    protected void unindent() {
        depth--;
        if (depth < 0) {
            throw new RuntimeException("Indenting below zero");
        }
    }

    /**
     * Print text to file with a newline appended and increase indention level
     * by one.
     *
     * @param string
     *            String that will be printed
     */
    protected void printlnI(final String string) {
        println(string);
        depth++;
    }

    /**
     * Decrease indention level by one, and print text to file with a newline
     * appended.
     *
     * @param string
     *            String that will be printed
     */
    protected void printlnU(final String string) {
        depth--;
        println(string);
    }

    /**
     * Print text to file.
     *
     * @param string
     *            String that will be printed
     */
    protected void print(final String string) {
        if (indent) {
            for (int i = 1; i <= depth; i++) {
                stream.print("    ");
            }
        }
        stream.print(string);
        indent = false;
    }

    /**
     * Print text to file and a newline.
     *
     * @param string
     *            String that will be printed
     */
    protected void println(final String string) {
        print(string + "\n");
        indent = true;
    }

    /**
     * Close stream.
     */
    protected void done() {
        stream.close();
    }
}
