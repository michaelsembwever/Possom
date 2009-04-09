/*
 * Copyright (2008) Schibsted ASA
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    protected void writelnI(final String string) {
        writeln(string);
        depth++;
    }

    /**
     * Decrease indention level by one, and write text to file with a newline
     * appended.
     *
     * @param string
     *            String that will be printed
     */
    protected void writelnU(final String string) {
        depth--;
        writeln(string);
    }

    /**
     * Print text to file.
     *
     * @param string
     *            String that will be printed
     */
    protected void write(final String string) {
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
    protected void writeln(final String string) {
        write(string + "\n");
        indent = true;
    }

    /**
     * Close stream.
     */
    protected void done() {
        stream.close();
    }
}
