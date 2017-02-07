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
     * Constructor
     *
     * @param script a TCL script to interpret
     */
    public TclLexer(String script) {
        super(script, false);
    }

    /**
     * Reading alphanumerical names (with possible index in parentheses) from
     * the script
     *
     * @return
     */
    protected String readName() {
        StringBuilder name = new StringBuilder("");
        int counter = 0; //Parentheses counter
        while ((Character.isDigit(getCurrentchar())
                || Character.isLetter(getCurrentchar())
                || getCurrentchar() == '_'
                || getCurrentchar() == '('
                || (getCurrentchar() == ')' && counter > 0))
                && getCurrentchar() != 0) {
            if (getCurrentchar() == '\\') {
                name.append(replaceSymbol());
            } else {
                //Incrementing or decrementing parentheses counter
                if (getCurrentchar() == '(') {
                    counter++;
                } else if (getCurrentchar() == ')') {
                    counter--;
                }
                name.append(getCurrentchar());
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
        while (!Character.isWhitespace(getCurrentchar()) && getCurrentchar() != '['
                && getCurrentchar() != ';' && getCurrentchar() != '$' && getCurrentchar() != 0) {
            if (getCurrentchar() == '\\') {
                name.append(replaceSymbol());
            } else {
                name.append(getCurrentchar());
                advancePosition();
            }
        }
        return name.toString();
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
        while (counter > 0 && getCurrentchar() != 0) {
            if (getCurrentchar() == endchar && endchar != MIRRORMAP.get(endchar)) {
                counter++;
            }
            if (getCurrentchar() == MIRRORMAP.get(endchar)) {
                counter--;
            }
            if (counter != 0) {
                string.append(getCurrentchar());
                advancePosition();
            }
        }
        return string.toString();
    }
    
    /**
     * Reading a Tcl comment
     * 
     * @return
     */
    protected String readComment () {
        StringBuilder string = new StringBuilder("");
        while (getCurrentchar() != '\n' && getCurrentchar() != '\r' && getCurrentchar() != 0) {
            string.append(getCurrentchar());
            advancePosition();
        }
        return string.toString();
    }

    @Override
    public TclToken getCustomToken() {
        /*
         What is the next token
         */
        if (getCurrentchar() == '#' ) {
            /*
            Returning a comment token
            */
            advancePosition();
            return new TclToken(TclTokenType.CMT).setValue(readComment());
        } else if ((peekback() == '"' && qflag)
                || peekback() == '{' || peekback() == '[') {
            /*
             Reading and returning a string of symbols
             */
            return new TclToken(TclTokenType.STRING).setValue(readString(peekback()));
        } else if (getCurrentchar() == '{' && Character.isWhitespace(peekback())) {
            /*
             Returning a left brace token
             */
            curlyflag = true;
            advancePosition();
            return new TclToken(TclTokenType.LEFTCURL);
        } else if (getCurrentchar() == '}' && curlyflag) {
            /*
             Returning a right brace token
             */
            curlyflag = false;
            advancePosition();
            return new TclToken(TclTokenType.RIGHTCURL);
        } else if (getCurrentchar() == '"' && !qflag && Character.isWhitespace(peekback())) {
            /*
             Returning a left quote token
             */
            qflag = true;
            advancePosition();
            return new TclToken(TclTokenType.LEFTQ);
        } else if (getCurrentchar() == '"' && qflag) {
            /*
             Returning a right quote token
             */
            qflag = false;
            advancePosition();
            return new TclToken(TclTokenType.RIGHTQ);
        } else if (getCurrentchar() == ';') {
            /*
             Returning a semi-colon token
             */
            advancePosition();
            return new TclToken(TclTokenType.SEMI);
        } else if (getCurrentchar() == '\n' || getCurrentchar() == '\r') {
            /*
             Returning an end of line token
             */
            skipWhitespace();
            return new TclToken(TclTokenType.EOL);
        } else if (Character.isWhitespace(getCurrentchar())) {
            /*
             Skipping whitespace and returning a whitespace token
             */
            skipWhitespace();
            return new TclToken(TclTokenType.WHITESPACE);
        } else if (getCurrentchar() == '[') {
            /*
             Returning a left bracket token
             */
            brflag = true;
            advancePosition();
            return new TclToken(TclTokenType.LEFTBR);
        } else if (getCurrentchar() == ']') {
            /*
             Returning a right bracket token
             */
            brflag = false;
            advancePosition();
            return new TclToken(TclTokenType.RIGHTBR);
        } else if (getCurrentchar() == '$') {
            /*
             Returning a dollar token
             */
            advancePosition();
            return new TclToken(TclTokenType.DOLLAR);
        } else if ((getCurrentchar() == '_' || Character.isLetter(getCurrentchar()))
                && peekback() == '$') {
            /*
             Returning a name token
             */
            return new TclToken(TclTokenType.NAME).setValue(readName());
        } else if ((getCurrentchar() == '_' || getCurrentchar() == '\\'
                || Character.isDigit(getCurrentchar()) || Character.isLetter(getCurrentchar()))) {
            /*
             Returning a Tclword token
             */
            return new TclToken(TclTokenType.WORD).setValue(readWord());
        } else {
            return null;
        }
    }
}
