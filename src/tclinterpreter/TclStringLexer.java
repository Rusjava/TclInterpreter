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
     * Backslash substitution
     *
     * @return
     */
    protected String replaceSymbol() {
        StringBuilder subst = new StringBuilder();
        advancePosition();
        switch (currentchar) {
            case 'a':
                subst.append(0x07);
                break;
            case 'b':
                subst.append(0x08);
                break;
            case 'f':
                subst.append(0x0c);
                break;
            case 'n':
                subst.append(0x0a);
                break;
            case 'r':
                subst.append(0x0d);
                break;
            case 't':
                subst.append(0x09);
                break;
            case 'v':
                subst.append(0x0b);
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
     * Reading alphanumerical names from the script
     *
     * @return
     */
    protected String readName() {
        StringBuilder name = new StringBuilder("");
        while (Character.isDigit(currentchar)
                || Character.isLetter(currentchar)
                || currentchar == '_') {
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
        while (currentchar != '[' && currentchar != 0 && currentchar != '$') {
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
    protected String readString() {
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
                && retropeek() == '$') {
            /*
             Returning a name token
             */
            return new TclToken(TclTokenType.NAME).setValue(readName());
        } else if (retropeek() == '[') {
            /*
             Reading and returning a string representing a script
             */
            return new TclToken(TclTokenType.STRING).setValue(readString());
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

    /**
     * Reading an octal number. The current position is at the last digit at the end
     *
     * @return
     */
    protected String readOctalNumber() {
        StringBuilder oNumber = new StringBuilder("");
        while (Character.isDigit(peek()) && (peek() != '8' || peek() != '9')) {
            advancePosition();
            oNumber.append(currentchar);
        }
        return Integer.valueOf(oNumber.toString(), 8).toString();
    }

    /**
     * Reading a hex number. The current position is at the last digit at the end
     *
     * @return
     */
    protected String readHexNumber() {
        StringBuilder hNumber = new StringBuilder("");
        while (Character.isDigit(peek())
                || (Character.toLowerCase(peek()) >= 'a' && Character.toLowerCase(peek()) <= 'f')) {
            advancePosition();
            hNumber.append(currentchar);
        }
        return Integer.valueOf(hNumber.toString(), 16).toString();
    }

    /**
     * Reading character specified by its Unicode. The current position is at the last digit at the end
     *
     * @return
     */
    protected String readUnicode() {
        StringBuilder hNumber = new StringBuilder("");
        while ((Character.isDigit(peek())
                || (Character.toLowerCase(peek()) >= 'a' && Character.toLowerCase(peek()) <= 'f'))
                && hNumber.length() < 4) {
            advancePosition();
            hNumber.append(currentchar);
        }
        return "" + (char) Integer.parseInt(hNumber.toString());
    }
}
