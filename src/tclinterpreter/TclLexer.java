/*
 * Copyright (C) 2016 Ruslan Feshchenko
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

import java.util.HashMap;
import java.util.Map;

/**
 * A class for TCL lexer
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class TclLexer extends AbstractTclLexer {

    /**
     * A map for final string characters
     */
    protected static final Map<Character, Character> MIRRORMAP = new HashMap<>();

    static {
        MIRRORMAP.put('"', '"');
        MIRRORMAP.put('[', ']');
        MIRRORMAP.put('{', '}');
        MIRRORMAP.put('(', ')');
    }

    /**
     * Flag indicating that the lexer is inside quotation
     */
    protected boolean qflag;

    /**
     * Flag indicating that the lexer is inside curly brackets
     */
    protected boolean curlyflag;

    /**
     * Flag indicating that the lexer is inside brackets
     */
    protected boolean brflag;
    
    /**
     * Flag indicating that the lexer is inside parantheses
     */
    protected boolean prflag;

    /**
     * Constructor
     *
     * @param script a TCL script to interpret
     */
    public TclLexer(String script) {
        super(script);
    }

    /**
     * Reading alphanumerical names from the script
     *
     * @return
     */
    protected String readName() {
        StringBuilder name = new StringBuilder("");
        while ((Character.isDigit(currentchar)
                || Character.isLetter(currentchar)
                || currentchar == '_') && currentchar != 0) {
            if (currentchar == '\\') {
                if (peek() == '\n' || peek() == '\r') {
                    advancePosition();
                    skipEOL();
                } else {
                    name.append(replaceSymbol());
                }
            } else {
                name.append(currentchar);
                advancePosition();
            }
        }
        return name.toString();
    }

    /**
     * Reading Tcl words
     *
     * @return
     */
    protected String readWord() {
        StringBuilder name = new StringBuilder("");
        while (!Character.isWhitespace(currentchar) && currentchar != '['
                && currentchar != ';' && currentchar != '$' && currentchar != 0) {
            if (currentchar == '\\') {
                if (peek() == '\n' || peek() == '\r') {
                    advancePosition();
                    skipEOL();
                } else {
                    name.append(replaceSymbol());
                }
            } else {
                name.append(currentchar);
                advancePosition();
            }
        }
        return name.toString();
    }

    /**
     * Skipping end of line and any whitespace after it
     */
    protected void skipEOL() {
        skipWhitespace();
    }

    /**
     * Reading the string between quotes of curly braces with ends of lines
     * skipped
     *
     * @param endchar the end symbol
     * @return
     */
    protected String readString(char endchar) {
        StringBuilder string = new StringBuilder("");
        int counter = 1;
        while (counter > 0 && currentchar != 0) {
            if (currentchar == '\\' && (peek() == '\n' || peek() == '\r')) {
                advancePosition();
                skipEOL();
            } else {
                if (currentchar == endchar && endchar != MIRRORMAP.get(endchar)) {
                    counter++;
                }
                if (currentchar == MIRRORMAP.get(endchar)) {
                    counter--;
                }
                if (counter != 0) {
                    string.append(currentchar);
                    advancePosition();
                }
            }
        }
        return string.toString();
    }

    @Override
    public TclToken getToken() {
        /*
         Skipping any leading escaped end of line
         */
        if (currentchar == '\\' && (peek() == '\n' || peek() == '\r')) {
            advancePosition();
            skipEOL();
        }

        /*
         What is the next token
         */
        if ((peekback() == '"' && qflag)
                || peekback() == '{' || peekback() == '[' || peekback() == '(') {
            /*
             Reading and returning a string of symbols
             */
            return new TclToken(TclTokenType.STRING).setValue(readString(peekback()));
        } else if (currentchar == '{') {
            /*
             Returning a left brace token
             */
            curlyflag = true;
            advancePosition();
            return new TclToken(TclTokenType.LEFTCURL);
        } else if (currentchar == '}') {
            /*
             Returning a right brace token
             */
            curlyflag = false;
            advancePosition();
            return new TclToken(TclTokenType.RIGHTCURL);
        } else if (currentchar == '"' && !qflag) {
            /*
             Returning a left quote token
             */
            qflag = true;
            advancePosition();
            return new TclToken(TclTokenType.LEFTQ);
        } else if (currentchar == '"' && qflag) {
            /*
             Returning a right quote token
             */
            qflag = false;
            advancePosition();
            return new TclToken(TclTokenType.RIGHTQ);
        } else if (currentchar == ';') {
            /*
             Returning a semi-colon token
             */
            advancePosition();
            return new TclToken(TclTokenType.SEMI);
        } else if (currentchar == '\n' || currentchar == '\r') {
            /*
             Returning an end of line token
             */
            skipEOL();
            return new TclToken(TclTokenType.EOL);
        } else if (Character.isWhitespace(currentchar)) {
            /*
             Skipping whitespace and returning a whitespace token
             */
            skipWhitespace();
            return new TclToken(TclTokenType.WHITESPACE);
        } else if (currentchar == '[') {
            /*
             Returning a left bracket token
             */
            brflag = true;
            advancePosition();
            return new TclToken(TclTokenType.LEFTBR);
        } else if (currentchar == ']') {
            /*
             Returning a right bracket token
             */
            brflag = false;
            advancePosition();
            return new TclToken(TclTokenType.RIGHTBR);
        } else if (currentchar == '(') {
            /*
             Returning a left paranthesis token
             */
            prflag = true;
            advancePosition();
            return new TclToken(TclTokenType.LEFTPAR);
        } else if (currentchar == ')') {
            /*
             Returning a right paranthesis token
             */
            prflag = false;
            advancePosition();
            return new TclToken(TclTokenType.RIGHTPAR);
        } else if (currentchar == '$') {
            /*
             Returning a dollar token
             */
            advancePosition();
            return new TclToken(TclTokenType.DOLLAR);
        } else if ((currentchar == '_' || Character.isLetter(currentchar))
                && peekback() == '$') {
            /*
             Returning a name token
             */
            return new TclToken(TclTokenType.NAME).setValue(readName());
        } else if ((currentchar == '_' || currentchar == '\\'
                || Character.isDigit(currentchar) || Character.isLetter(currentchar))) {
            /*
             Returning a Tclword token
             */
            return new TclToken(TclTokenType.WORD).setValue(readWord());
        } else if (currentchar == 0) {
            /*
             Returning an end of file token
             */
            return new TclToken(TclTokenType.EOF);
        } else {
            /*
             Returning UNKNOWN token in all other cases
             */
            return new TclToken(TclTokenType.UNKNOWN);
        }
    }
}
