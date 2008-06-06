/**
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
 *
 */

package no.sesat;

import java.io.Console;
import java.util.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;

/**
 * Simple interpreter that will start if a console is available. You can add
 * functions to it by using addFunction(String, Function).
 *
 */
public class Interpreter {
    private static Map<String, Function> functions = new HashMap<String, Function>();
    private static boolean run = true;

    /**
     * Add a function to this Interpreter.
     *
     * @param name
     *            Name of function
     * @param fun
     *            The function
     */
    public static void addFunction(final String name, final Function fun) {
        if (functions != null) {
            functions.put(name, fun);
        }
    }

    /**
     * Remove function from interpreter.
     *
     * @param name
     *            Named function
     */
    public static void removeFunction(final String name) {
        if (functions != null) {
            functions.remove(name);
        }
    }

    private static interface FunctionInterface {
        String execute(Context ctx);
    }

    /**
     * Function that you can implement if you want to add a function to this
     * Interpreter.
     */
    public abstract static class Function implements FunctionInterface {
        /**
         * One line that describes the function.
         *
         * @return Description
         */
        protected String describe() {
            return "";
        }

        /**
         * If there is need for a longer description then what you can write in
         * description then write it here.
         *
         * @return Help text
         */
        protected String help() {
            return describe();
        }
    }

    /**
     * Context for a function.
     */
    public static final class Context {
        /**
         * The original argument array.
         */
        protected String[] args;

        private Context(final String[] args) {
            this.args = args;
        }

        /**
         * @return Number of arguments.
         */
        protected int length() {
            return args.length;
        }
    }

    static {
        addFunction("help", new Function() {
            public String execute(final Context ctx) {
                if (ctx.length() == 1) {
                    String res = "Help for " + ctx.args[0] + "\n";
                    final Function fun = functions.get(ctx.args[0].trim());
                    if (fun != null) {
                        res += "    " + fun.help();
                    } else {
                        res += "    Not found!\n";
                    }
                    return res;
                } else {
                    String res = "Functions available: \n";
                    for (String name : functions.keySet()) {
                        res += "    " + name + "\n";
                        res += "        " + functions.get(name).describe() + "\n";
                    }
                    return res;
                }
            }

            public String describe() {
                return "Print this help message";
            }
        });

        addFunction("loggers", new Function() {
            public String execute(final Context ctx) {
                String res = "Active loggers:\n";
                final LoggerRepository repo = Logger.getRootLogger().getLoggerRepository();

                final Enumeration e = repo.getCurrentCategories();
                while (e.hasMoreElements()) {
                    final Logger logger = (Logger) e.nextElement();
                    if (ctx.length() == 0 || (ctx.length() > 0 && logger.getName().matches(ctx.args[0]))) {
                        res += logger.getName() + " " + logger.getLevel();
                        if (ctx.length() == 2) {
                            final Level level = Level.toLevel(ctx.args[1]);
                            if (level.toString().equalsIgnoreCase(ctx.args[1])) {
                                res += " (Setting level to " + level + ")";
                                logger.setLevel(level);
                            } else {
                                res += " (unknown debug level: " + ctx.args[1] + ")";
                            }
                        }
                        res += "\n";
                    }
                }
                return res;
            }

            public String describe() {
                return "Print active loggers, and set level if specified. 'loggers [regexp] [level]'";
            }
        });

        addFunction("quit", new Function() {
            public String execute(final Context ctx) {
                run = false;
                return "Bye!";
            }

            public String describe() {
                return "Stop this interpreter.";
            }
        });

        final Thread replThread = new Thread("REPL") {
            private Console console = System.console();

            public void run() {
                if (console != null) {
                    while (run) {
                        console.printf("$ ");
                        final String line = console.readLine();
                        if (line != null) { // check for null, this will happen on shutdown
                            final String[] input = line.split("\\s");
                            if (input.length > 0) {
                                final String name = input[0].trim();
                                if (name.length() > 0) {
                                    if (functions.containsKey(input[0])) {
                                        try {
                                            console.printf("%s\n", functions.get(name).execute(
                                                    new Context(Arrays.copyOfRange(input, 1, input.length))));
                                        } catch (Throwable e) {
                                            console.printf("Error: %s\n", e.toString());
                                        }

                                    } else {
                                        console.printf("Unknown function: %s\n", name);
                                    }
                                }
                            }
                        }
                    }
                }
                functions = null;
            }
        };
        replThread.setDaemon(true);
        replThread.start();
    }
}
