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
 * A special lexer class for expressions
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class TclExpressionLexer extends AbstractTclLexer {

    /**
     * Constructor
     *
     * @param script
     */
    public TclExpressionLexer(String script) {
        super(script);
    }

    /**
     * Reading a real number from the script
     *
     * @return
     */
    protected String readNumber() {
        StringBuilder number = new StringBuilder("");
        /*
         This is a number if didgit, dot and exponetial characters are present     
         */
        while (Character.isDigit(currentchar)
                || currentchar == '.'
                || (Character.toLowerCase(currentchar) == 'e'
                && ((peek() == '-') || peek() == '+'))) {
            number.append(currentchar);
            advancePosition();
            if (Character.toLowerCase(retropeek()) == 'e') {
                number.append(currentchar);
                advancePosition();
            }
        }
        return number.toString();
    }

    @Override
    public TclToken getToken() {
        /*
         Skipping any whitespace
         */
        if (Character.isWhitespace(currentchar)) {
            /*
             Returning a real number token
             */
            skipWhitespace();
        }

        if (Character.isDigit(currentchar)) {
            /*
             Returning a real number token
             */
            return new TclToken(TclTokenType.NUMBER).setValue(readNumber());
        } else if (currentchar == '+') {
            /*
             Returning a plus op token
             */
            advancePosition();
            return new TclToken(TclTokenType.PLUS);
        } else if (currentchar == '-') {
            /*
             Returning a minus op token
             */
            advancePosition();
            return new TclToken(TclTokenType.MINUS);
        } else if (currentchar == '~') {
            /*
             Returning a minus op token
             */
            advancePosition();
            return new TclToken(TclTokenType.BNOT);
        } else if (currentchar == '!') {
            /*
             Returning a minus op token
             */
            advancePosition();
            return new TclToken(TclTokenType.NOT);
        } else if (currentchar == '*' && peek() == '*') {
            /*
             Returning an exp op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.EXP);
        } else if (currentchar == '*') {
            /*
             Returning a multiplication op token
             */
            advancePosition();
            return new TclToken(TclTokenType.MUL);
        } else if (currentchar == '<' && peek() == '<') {
            /*
             Returning a left shift op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.LSHIFT);
        } else if (currentchar == '<' && peek() == '=') {
            /*
             Returning a less or equal op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.LEQ);
        }
        else if (currentchar == '<') {
            /*
             Returning a less op token
             */
            advancePosition();
            return new TclToken(TclTokenType.LESS);
        } else if (currentchar == '>' && peek() == '>') {
            /*
             Returning a righr shift op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.RSHIFT);
        } else if (currentchar == '>' && peek() == '=') {
            /*
             Returning a more or equal op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.MEQ);
        } else if (currentchar == '>') {
            /*
             Returning a more op token
             */
            advancePosition();
            return new TclToken(TclTokenType.MORE);
        } else if (currentchar == '/') {
            /*
             Returning a division op token
             */
            advancePosition();
            return new TclToken(TclTokenType.DIV);
        } else if (currentchar == '%') {
            /*
             Returning a remainder op token
             */
            advancePosition();
            return new TclToken(TclTokenType.REM);
        } else if (currentchar == 'e' && peek() == 'q') {
            /*
             Returning a string equality op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.EQ);
        } else if (currentchar == 'n' && peek() == 'e') {
            /*
             Returning a string non-equality op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.NE);
        } else if (currentchar == 'i' && peek() == 'n') {
            /*
             Returning a string in list op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.IN);
        } else if (currentchar == 'n' && peek() == 'i') {
            /*
             Returning a string not in list op token
             */
            advancePosition();
            advancePosition();
            return new TclToken(TclTokenType.NI);
        } else if (currentchar == '&' && peek() == '&') {
            /*
             Returning an AND op token
             */
            advancePosition();
            return new TclToken(TclTokenType.AND);
        } else if (currentchar == '|' && peek() == '|') {
            /*
             Returning an OR op token
             */
            advancePosition();
            return new TclToken(TclTokenType.OR);
        } else if (currentchar == '&') {
            /*
             Returning a bit AND op token
             */
            advancePosition();
            return new TclToken(TclTokenType.BAND);
        } else if (currentchar == '^') {
            /*
             Returning a bit XOR op token
             */
            advancePosition();
            return new TclToken(TclTokenType.BXOR);
        } else if (currentchar == '|') {
            /*
             Returning a bit OR op token
             */
            advancePosition();
            return new TclToken(TclTokenType.BOR);
        } else if (currentchar == '(') {
            /*
             Returning a left paranthesis op token
             */
            advancePosition();
            return new TclToken(TclTokenType.LEFTPAR);
        } else if (currentchar == ')') {
            /*
             Returning a right paranthesis op token
             */
            advancePosition();
            return new TclToken(TclTokenType.RIGHTPAR);
        } else if (currentchar == 0) {
            /*
             Reading and returning end of file
             */
            return new TclToken(TclTokenType.EOF);
        } else {
            /*
             Returning an unknown token
             */
            advancePosition();
            return new TclToken(TclTokenType.UNKNOWN);
        }
    }
}
