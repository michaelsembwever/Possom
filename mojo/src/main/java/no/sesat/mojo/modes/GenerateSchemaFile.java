/*
 * Copyright (2008) Schibsted Søk AS
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

/**
 * Common class for Schema Generators.
 *
 */
public abstract class GenerateSchemaFile extends GenerateFile implements Runnable {
    protected final ConfigElement root;
    protected final String id;

    private final String fileName;

    /**
     * @param element
     *            Root element
     * @param name
     *            File name
     * @param idString
     *            Id for this schema
     */
    public GenerateSchemaFile(final ConfigElement element, final String name, final String idString) {
        fileName = name;
        id = idString;
        root = element;
    }

    /**
     *
     * @see java.lang.Runnable#run()
     */
    public final void run() {
        init(fileName);
        runImpl();
        done();
    }

    /**
     * This will be called when this runnable is run, and should generate the
     * expected schema file.
     */
    protected abstract void runImpl();
}
