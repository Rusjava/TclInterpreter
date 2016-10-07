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
    protected String script = null;
    /**
     * Current position in script
     */
    protected int pos = 0;
    /**
     * Current symbol at position of pos
     */
    protected char currentchar = 0;
    /**
     * A flag indicating if whitespace shall be ignored
     */
    protected boolean skipWhitespace;

    /**
     * A general constructor of parsers
     *
     * @param script a Tcl script
     * @param skipWhitespace Should whitespace be skipped
     */
    public AbstractBasicTclLexer(String script, boolean skipWhitespace) {
        this.script = script;
        if (script.length() > 0) {
            currentchar = script.charAt(pos);
        } else {
            currentchar = 0;
        }
        this.skipWhitespace = skipWhitespace;
    }

    /**
     * Advance position by one
     */
    protected void advancePosition() {
        if (++pos < script.length()) {
            currentchar = script.charAt(pos);
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
        if (pos < script.length() - 1) {
            return script.charAt(pos + 1);
        } else {
            return 0;
        }
    }

    /**
     * What was the previous character?
     *
     * @return the previous character
     */
    protected char peekback() {
        if (pos > 0) {
            return script.charAt(pos - 1);
        } else {
            return 0;
        }
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
        while (Character.isWhitespace(currentchar) && currentchar != 0) {
            advancePosition();
        }
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
    protected void skipEOL() {
        skipWhitespace();
    }
    
}
