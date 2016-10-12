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

/**
 * A special lexer class for quoted enclosed strings
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class TclStringLexer extends AbstractTclLexer {

    /**
     * Constructor
     *
     * @param script
     */
    public TclStringLexer(String script) {
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
                || (getCurrentchar() == ')' && counter != 0)
                || getCurrentchar() == '\\') && getCurrentchar() != 0) {
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
     * Reading the substring that is not a name or command
     *
     * @return
     */
    protected String readSubString() {
        StringBuilder string = new StringBuilder();
        while (getCurrentchar() != '[' && getCurrentchar() != 0 && getCurrentchar() != '$' && getCurrentchar() != 0) {
            if (getCurrentchar() == '\\') {
                string.append(replaceSymbol());
            } else {
                string.append(getCurrentchar());
                advancePosition();
            }
        }
        return string.toString();
    }

    /**
     * Reading a command string
     *
     * @return
     */
    protected String readCommandString() {
        StringBuilder string = new StringBuilder("");
        //Counter of nested brackets
        int counter = 1;
        do {
            string.append(getCurrentchar());
            advancePosition();
            //Icreamenting or decreamenting the nested bracket counter
            if (getCurrentchar() == '[') {
                counter++;
            } else if (getCurrentchar() == ']') {
                counter--;
            }
        } while (counter > 0 && getCurrentchar() != 0);
        return string.toString();
    }

    @Override
    public TclToken getCustomToken() {
        /*
         What is the next token
         */
        if (getCurrentchar() == '[') {
            /*
             Reading the beginning of the command substitutiion
             */
            advancePosition();
            return new TclToken(TclTokenType.LEFTBR);
        } else if (getCurrentchar() == ']') {
            /*
             Reading the end of the command substitutiion
             */
            advancePosition();
            return new TclToken(TclTokenType.RIGHTBR);
        } else if (getCurrentchar() == '$') {
            /*
             Reading the beginning of the variable substitutiion
             */
            advancePosition();
            return new TclToken(TclTokenType.DOLLAR);
        } else if ((getCurrentchar() == '_' || Character.isLetter(getCurrentchar()))
                && peekback() == '$') {
            /*
             Returning a name token
             */
            return new TclToken(TclTokenType.NAME).setValue(readName());
        } else if (peekback() == '[') {
            /*
             Reading and returning a string representing a script
             */
            return new TclToken(TclTokenType.STRING).setValue(readCommandString());
        } else {
            /*
             Reading and returning EOF
             */
            return new TclToken(TclTokenType.STRING).setValue(readSubString());
        }
    }
}
