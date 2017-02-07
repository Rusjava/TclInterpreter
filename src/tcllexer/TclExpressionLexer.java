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
package tcllexer;

/**
 * A special lexer class for expressions
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class TclExpressionLexer extends AbstractTclLexer {

    protected boolean qflag = false;

    /**
     * Constructor
     *
     * @param script
     */
    public TclExpressionLexer(String script) {
        super(script, true);
    }

    /**
     * Reading a real number from the script
     *
     * @return
     */
    protected String readNumber() {
        /*
        If this is an octal number?
         */
        if (getCurrentchar() == '0' && Character.isDigit(peek())) {
            String oNumber = readOctalNumber();
            advancePosition();
            return oNumber;
        }
        /*
        If this is an hex number?
         */
        if (getCurrentchar() == '0' && peek() == 'x') {
            advancePosition();
            String hNumber = readHexNumber();
            advancePosition();
            return hNumber;
        }
        StringBuilder number = new StringBuilder("");
        /*
         This is a number if didgit, dot and exponetial characters are present     
         */
        while (Character.isDigit(getCurrentchar())
                || getCurrentchar() == '.'
                || (Character.toLowerCase(getCurrentchar()) == 'e'
                && ((peek() == '-') || peek() == '+'))) {
            number.append(getCurrentchar());
            advancePosition();
            if (Character.toLowerCase(peekback()) == 'e') {
                number.append(getCurrentchar());
                advancePosition();
            }
        }
        return number.toString();
    }

    /**
     * Reading a string of characters in quotes
     *
     * @return
     */
    protected String readString() {
        StringBuilder string = new StringBuilder("");
        while (getCurrentchar() != '"' && getCurrentchar() != 0) {
            string.append(getCurrentchar());
            advancePosition();
        }
        return string.toString();
    }
    
    /**
     * Reading alphanumerical names of functions from
     * the script
     *
     * @return
     */
    protected String readFunctionName() {
        StringBuilder name = new StringBuilder("");
        while ((Character.isDigit(getCurrentchar())
                || Character.isLetter(getCurrentchar())
                || getCurrentchar() == '_'
                || getCurrentchar() == '\\') && getCurrentchar() != 0) {
            if (getCurrentchar() == '\\') {
                //Doing slash substitutions
                name.append(replaceSymbol());
            } else {
                name.append(getCurrentchar());
                advancePosition();
            }
        }
        return name.toString();
    }

    @Override
    public TclToken getCustomToken() {

        if (peekback() == '"' && qflag) {
            /*
             Reading and returning a string of symbols
             */
            return new TclToken(TclTokenType.STRING).setValue(readString());
        } else if (Character.isDigit(getCurrentchar())) {
            /*
             Returning a real number token
             */
            return new TclToken(TclTokenType.NUMBER).setValue(readNumber());
        } else if (getCurrentchar() == '+') {
            /*
             Returning a plus op token
             */
            advancePosition();
            return new TclToken(TclTokenType.PLUS);
        } else if (getCurrentchar() == '-') {
            /*
             Returning a minus op token
             */
            advancePosition();
            return new TclToken(TclTokenType.MINUS);
        } else if (getCurrentchar() == '~') {
            /*
             Returning a minus op token
             */
            advancePosition();
            return new TclToken(TclTokenType.BNOT);
        } else if (getCurrentchar() == '!') {
            /*
             Returning a minus op token
             */
            advancePosition();
            return new TclToken(TclTokenType.NOT);
        } else if (getCurrentchar() == '*' && peek() == '*') {
            /*
             Returning an exp op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.EXP);
        } else if (getCurrentchar() == '*') {
            /*
             Returning a multiplication op token
             */
            advancePosition();
            return new TclToken(TclTokenType.MUL);
        } else if (getCurrentchar() == '<' && peek() == '<') {
            /*
             Returning a left shift op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.LSHIFT);
        } else if (getCurrentchar() == '<' && peek() == '=') {
            /*
             Returning a less or equal op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.LEQ);
        } else if (getCurrentchar() == '<') {
            /*
             Returning a less op token
             */
            advancePosition();
            return new TclToken(TclTokenType.LESS);
        } else if (getCurrentchar() == '>' && peek() == '>') {
            /*
             Returning a righr shift op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.RSHIFT);
        } else if (getCurrentchar() == '>' && peek() == '=') {
            /*
             Returning a more or equal op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.MEQ);
        } else if (getCurrentchar() == '>') {
            /*
             Returning a more op token
             */
            advancePosition();
            return new TclToken(TclTokenType.MORE);
        } else if (getCurrentchar() == '/') {
            /*
             Returning a division op token
             */
            advancePosition();
            return new TclToken(TclTokenType.DIV);
        } else if (getCurrentchar() == '%') {
            /*
             Returning a remainder op token
             */
            advancePosition();
            return new TclToken(TclTokenType.REM);
        } else if (getCurrentchar() == 'e' && peek() == 'q') {
            /*
             Returning a string equality op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.EQ);
        } else if (getCurrentchar() == 'n' && peek() == 'e') {
            /*
             Returning a string non-equality op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.NE);
        } else if (getCurrentchar() == 'i' && peek() == 'n') {
            /*
             Returning a string in a list op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.IN);
        } else if (getCurrentchar() == 'n' && peek() == 'i') {
            /*
             Returning a string not in a list op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.NI);
        } else if (getCurrentchar() == '&' && peek() == '&') {
            /*
             Returning an AND op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.AND);
        } else if (getCurrentchar() == '|' && peek() == '|') {
            /*
             Returning an OR op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.OR);
        } else if (getCurrentchar() == '&') {
            /*
             Returning a bit AND op token
             */
            advancePosition();
            return new TclToken(TclTokenType.BAND);
        } else if (getCurrentchar() == '^') {
            /*
             Returning a bit XOR op token
             */
            advancePosition();
            return new TclToken(TclTokenType.BXOR);
        } else if (getCurrentchar() == '|') {
            /*
             Returning a bit OR op token
             */
            advancePosition();
            return new TclToken(TclTokenType.BOR);
        } else if (getCurrentchar() == '?') {
            /*
             Returning a question mark token
             */
            advancePosition();
            return new TclToken(TclTokenType.QM);
        } else if (getCurrentchar() == ':') {
            /*
             Returning a colon token
             */
            advancePosition();
            return new TclToken(TclTokenType.COLON);
        } else if (getCurrentchar() == '(') {
            /*
             Returning a left paranthesis op token
             */
            advancePosition();
            return new TclToken(TclTokenType.LEFTPAR);
        } else if (getCurrentchar() == ')') {
            /*
             Returning a right paranthesis op token
             */
            advancePosition();
            return new TclToken(TclTokenType.RIGHTPAR);
        } else if (getCurrentchar() == '"' && !qflag) {
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
        } else if (Character.isLetter(getCurrentchar()) || getCurrentchar() == '_') {
            /*
             Returning a function name token
             */
            return new TclToken(TclTokenType.NAME).setValue(readFunctionName());
        } else {
            return null;
        }
    }
}
