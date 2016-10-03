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
                name.append(replaceSymbol());
            } else {
                name.append(currentchar);
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
        while (currentchar != '[' && currentchar != 0 && currentchar != '$' && currentchar != 0) {
            if (currentchar == '\\') {
                string.append(replaceSymbol());
            } else {
                string.append(currentchar);
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
        while (currentchar != ']' && currentchar != 0) {
            string.append(currentchar);
            advancePosition();
        }
        return string.toString();
    }

    @Override
    public TclToken getToken() {
        /*
         What is the next token
         */
        if (currentchar == '[') {
            /*
             Reading the beginning of the command substitutiion
             */
            advancePosition();
            return new TclToken(TclTokenType.LEFTBR);
        } else if (currentchar == ']') {
            /*
             Reading the end of the command substitutiion
             */
            advancePosition();
            return new TclToken(TclTokenType.RIGHTBR);
        } else if (currentchar == '$') {
            /*
             Reading the beginning of the variable substitutiion
             */
            advancePosition();
            return new TclToken(TclTokenType.DOLLAR);
        } else if ((currentchar == '_' || Character.isLetter(currentchar))
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
        } else if (currentchar == 0) {
            /*
             Reading and returning end of file
             */
            return new TclToken(TclTokenType.EOF);
        } else {
            /*
             Reading and returning EOF
             */
            return new TclToken(TclTokenType.STRING).setValue(readSubString());
        }
    }
}
