/* Copyright (2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */

package no.sesat.search.result;

import java.util.ArrayDeque;
import java.util.Deque;

public class StringChopper {

    private enum State {
        NONE, TAG, STARTTAG, ENDTAG, CDATA, COMMENT, DECLARATION
    };

    /**
     * Truncate s to the given length at closest space or xml tag. Any xml tags
     * will be closed/balanced.
     *
     * @param input
     *            The string that should be truncated.
     * @param length
     * @return The truncated string
     */
    public static String chop(final String input, final int length) {
        return chop(input, length, false);
    }

    /**
     * Truncate s to the given length or to closest space/tag depending on chop.
     * Any xml tags will be closed/balanced.
     *
     * @param input
     *            The string that should be truncated.
     * @param length
     *            max length of string (if choped the string will be '...'
     *            longer then max.)
     * @param chop
     *            If words should be choped, or if we chop inbetween spaces.
     * @return The truncated string
     */
    public static String chop(final String input, final int length, final boolean chop) {

        if (input != null) {
            final Deque<Integer> stack = new ArrayDeque<Integer>();
            final char[] s = input.toCharArray();
            final StringBuilder res = new StringBuilder(s.length);
            State state = State.NONE;
            int count = 0;
            int i = 0;

            main: for (; i < s.length; i++) {
                char c = s[i];
                switch (state) {
                case NONE:
                    if (c == '<') {
                        state = State.TAG;
                    } else {
                        count++;
                        if (count == length) {
                            res.append(c);
                            break main;
                        }
                    }
                    break;

                case TAG:
                    if (c == '/') {
                        state = State.ENDTAG;
                    } else if (c == '!') {
                        // ![CDATA[
                        if (s.length > (i + 7) && s[i + 1] == '[' && (s[i + 2] == 'C' || s[i + 2] == 'c')
                                && (s[i + 3] == 'D' || s[i + 3] == 'd') && (s[i + 4] == 'A' || s[i + 4] == 'a')
                                && (s[i + 5] == 'T' || s[i + 5] == 't') && (s[i + 6] == 'A' || s[i + 6] == 'a')
                                && s[i + 7] == '[') {
                            state = State.CDATA;
                            res.append("![CDATA[");
                            i += 7;
                            continue;
                        }
                        // !--
                        else if (s.length > (i + 2) && s[i + 1] == '-' && s[i + 2] == '-') {
                            state = State.COMMENT;
                            res.append("!--");
                            i += 2;
                            continue;
                        }
                    } else if (c == '?') {
                        state = State.DECLARATION;
                    } else {
                        stack.push(i);
                        state = State.STARTTAG;
                    }
                    break;

                case STARTTAG:
                    if (c == '/') {
                        if (s.length > (i + 1) && s[i + 1] == '>') {
                            state = State.NONE;
                            res.append("/>");
                            i += 1;
                            if (!stack.isEmpty()) {
                                stack.pop();
                            }
                            continue;
                        }
                    } else if (c == '>') {
                        state = State.NONE;
                    }
                    break;

                case ENDTAG:
                    if (c == '>') {
                        state = State.NONE;
                        if (!stack.isEmpty()) {
                            stack.pop();
                        }
                    }
                    break;

                case CDATA:

                    if (c == ']') {// ]]>
                        if (s.length > (i + 2) && s[i + 1] == ']' && s[i + 2] == '>') {
                            state = State.NONE;
                            res.append("]]>");
                            i += 2;
                            continue;
                        }
                    } else {
                        count++;
                        if (count == length) {
                            res.append(c);
                            break main;
                        }
                    }
                    break;

                case COMMENT:
                    if (c == '-') {
                        // -->
                        if (s.length > (i + 2) && s[i + 1] == '-' && s[i + 2] == '>') {
                            state = State.NONE;
                            res.append("-->");
                            i += 2;
                            continue;
                        }
                    }
                    break;

                case DECLARATION:
                    if (c == '?') {
                        if (s.length > (i + 1) && s[i + 1] == '>') {
                            state = State.NONE;
                            res.append("?>");
                            i += 1;
                            continue;
                        }
                    }
                    break;
                }
                res.append(c);
            }

            // remove unclosed tag
            if (state == State.TAG || state == State.STARTTAG || state == State.ENDTAG) {
                int pos = res.lastIndexOf("<");
                res.setLength(pos);
                if (state == State.STARTTAG) {
                    stack.pop();
                }
            }

            // append dots
            if (i < s.length - 1) {
                if (!chop) {
                    for (int k = i; k > 0 && count > 0; k--) {
                        if (s[k] == ' ' || s[k] == ((state == State.CDATA) ? '[' : '>')) {
                            res.setLength(k + 1);
                            k = 0;
                        }
                        count--;
                    }
                    res.append("...");
                }
            }

            // close CDATA if we are in one
            if (state == State.CDATA) {
                res.append("]]>");
            }

            // close all other open tags
            while (!stack.isEmpty()) {
                int j = stack.pop();
                res.append("</");
                while (s.length > j && (s[j] != '>' && s[j] != ' ')) {
                    res.append(s[j]);
                    j++;
                }
                res.append('>');
            }

            return res.toString();
        }
        return null;
    }
}
