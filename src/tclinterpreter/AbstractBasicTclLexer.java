/*
 * Copyright (C) 2015 Ruslan Feshchenko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tclinterpreter;

/**
 * Basic abstract class for Tcl lexers
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public abstract class AbstractBasicTclLexer {

    /**
     * TCL script
     */
    private String script = null;
    /**
     * Current position in script
     */
    private int pos = 0;
    /**
     * Current symbol at position of pos
     */
    private char currentchar = 0;
    /**
     * Next symbol at position of pos+1
     */
    private char nextchar = 0;
    /**
     * Previous symbol at position of pos-1
     */
    private char previouschar = 0;
    /**
     * A flag indicating if whitespace shall be ignored
     */
    private final boolean skipWhitespace;

    /**
     * A general constructor of parsers
     *
     * @param script a Tcl script
     * @param skipWhitespace Should whitespace be skipped
     */
    public AbstractBasicTclLexer(String script, boolean skipWhitespace) {
        this.script = script;
        //If the string has at least one symbol, read it as the current char
        if (script.length() > 0) {
            currentchar = script.charAt(pos);
            //If the string has at least two symbols, read the second one as the next char
            if (script.length() > 1) {
                nextchar = script.charAt(pos + 1);
                //Skipping any leading escaped end of line
                if (currentchar == '\\' && (nextchar == '\n' || nextchar == '\r')) {
                    advancePosition();
                    skipEOL();
                }
            }
        }
        this.skipWhitespace = skipWhitespace;
    }

    /**
     * Advancing position by one symbol, zero if no next symbol
     */
    protected final void advancePosition() {
        if (++pos < script.length()) {
            previouschar = currentchar;
            currentchar = nextchar;
            if (pos < script.length() - 1) {
                nextchar = script.charAt(pos + 1);
            } else {
                nextchar = 0;
            }
            if (currentchar == '\\' && (nextchar == '\n' || nextchar == '\r')) {
                char pchar = previouschar;
                advancePosition();
                skipEOL();
                //Preserving the previous char
                previouschar = pchar;
            }
        } else {
            currentchar = 0;
        }
    }

    /**
     * What is the next character?
     *
     * @return the next character
     */
    protected char peek() {
        return nextchar;
    }

    /**
     * What was the previous character?
     *
     * @return the previous character
     */
    protected char peekback() {
        return previouschar;
    }

    /**
     * Getting the next Tcl token
     *
     * @return
     */
    public abstract TclToken getToken();

    /**
     * Returning the Tcl script
     *
     * @return
     */
    public String getScript() {
        return script;
    }

    /**
     * Skipping white space
     */
    protected void skipWhitespace() {
        skipEOL();
    }

    /**
     * Backslash substitution
     *
     * @return
     */
    protected String replaceSymbol() {
        StringBuilder subst = new StringBuilder();
        advancePosition();
        switch (currentchar) {
            case 'a':
                subst.append((char) 7);
                break;
            case 'b':
                subst.append((char) 8);
                break;
            case 'f':
                subst.append((char) 12);
                break;
            case 'n':
                subst.append((char) 10);
                break;
            case 'r':
                subst.append((char) 13);
                break;
            case 't':
                subst.append((char) 9);
                break;
            case 'v':
                subst.append((char) 11);
                break;
            case '0':
                subst.append(readOctalNumber());
                break;
            case 'x':
                subst.append(readHexNumber());
                break;
            case 'u':
                subst.append(readUnicode());
                break;
            default:
                subst.append(currentchar);
        }
        advancePosition();
        return subst.toString();
    }

    /**
     * Reading an octal number. The current position is at the last digit at the
     * end
     *
     * @return
     */
    protected String readOctalNumber() {
        StringBuilder oNumber = new StringBuilder("");
        while (Character.isDigit(peek()) && (peek() != '8' && peek() != '9')) {
            advancePosition();
            oNumber.append(currentchar);
        }
        if (oNumber.length() == 0) {
            oNumber.append("0");
        }
        return Integer.valueOf(oNumber.toString(), 8).toString();
    }

    /**
     * Reading a hex number. The current position is at the last digit at the
     * end
     *
     * @return
     */
    protected String readHexNumber() {
        StringBuilder hNumber = new StringBuilder("");
        while (Character.isDigit(peek()) || (Character.toLowerCase(peek()) >= 'a' && Character.toLowerCase(peek()) <= 'f')) {
            advancePosition();
            hNumber.append(currentchar);
        }
        if (hNumber.length() == 0) {
            hNumber.append("0");
        }
        return Integer.valueOf(hNumber.toString(), 16).toString();
    }

    /**
     * Reading character specified by its Unicode. The current position is at
     * the last digit at the end
     *
     * @return
     */
    protected String readUnicode() {
        StringBuilder hNumber = new StringBuilder("");
        while ((Character.isDigit(peek()) || (Character.toLowerCase(peek()) >= 'a' && Character.toLowerCase(peek()) <= 'f')) && hNumber.length() < 4) {
            advancePosition();
            hNumber.append(currentchar);
        }
        return "" + (char) Integer.parseInt(hNumber.toString(), 16);
    }

    /**
     * Skipping end of line and any whitespace after it
     */
    private void skipEOL() {
        while (Character.isWhitespace(currentchar) && currentchar != 0) {
            advancePosition();
        }
    }

    /**
     * Returning the char at the current position
     *
     * @return currentchar
     */
    public char getCurrentchar() {
        return currentchar;
    }

    /**
     * Should any whitespace be skipped?
     *
     * @return skipWhitespace
     */
    public boolean isSkipWhitespace() {
        return skipWhitespace;
    }

}
