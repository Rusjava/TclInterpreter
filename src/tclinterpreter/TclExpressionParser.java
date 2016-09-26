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
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class TclExpressionParser extends AbstractTclParser {

    /**
     * The number of folded parentheses
     */
    protected int fnumber = 0;

    /**
     * Constructor
     *
     * @param lexer
     */
    public TclExpressionParser(TclExpressionLexer lexer) {
        super(lexer);
    }

    /**
     * Returning a factor of a binary operation
     *
     * @return
     * @throws tclinterpreter.AbstractTclParser.TclParserError
     * @throws tclinterpreter.TclExpressionParser.UnbalancedParenthesesException
     */
    protected TclNode getFactor() throws TclParserError, UnbalancedParenthesesException {
        TclNode node = null;
        /*
         * If it begins with a number, return it. If it begins with an opening paranthesis get the expression in paranthesis
         */
        advanceToken(TclTokenType.NUMBER, TclTokenType.LEFTPAR,
                TclTokenType.NOT, TclTokenType.BNOT, TclTokenType.PLUS, TclTokenType.MINUS);
        switch (currenttoken.type) {
            case NUMBER:
                node = new TclNode(TclNodeType.NUMBER).setValue(currenttoken.getValue());
                break;
            case LEFTPAR:
                fnumber++; //Increasing number of folded parantheses
                node = getExpression();
                break;
            case MINUS:
                node = new TclNode(TclNodeType.UNARYOP).setValue("-");
                node.getChildren().add(getFactor());
                break;
            case PLUS:
                node = new TclNode(TclNodeType.UNARYOP).setValue("+");
                node.getChildren().add(getFactor());
                break;
            case NOT:
                node = new TclNode(TclNodeType.UNARYOP).setValue("!");
                node.getChildren().add(getFactor());
                break;
            case BNOT:
                node = new TclNode(TclNodeType.UNARYOP).setValue("~");
                node.getChildren().add(getFactor());
        }

        return node;
    }

    /**
     * Returning the expression in parentheses
     *
     * @return
     * @throws tclinterpreter.AbstractTclParser.TclParserError
     * @throws tclinterpreter.TclExpressionParser.UnbalancedParenthesesException
     */
    protected TclNode getExpression() throws TclParserError, UnbalancedParenthesesException {
        TclNode arg;
        TclNode op;
        /*
         Is the first token an argument?
         */
        arg = getArgument();
        /*
         Cycling over the long expression
         */
        while (currenttoken.type != TclTokenType.EOF && currenttoken.type != TclTokenType.RIGHTPAR) {
            op = getAddOperation();
            op.getChildren().add(arg);
            arg = getArgument();
            op.getChildren().add(arg);
            arg = op;
        }
        return arg;
    }

    /**
     * Returning an argument of a binary operation
     *
     * @return
     * @throws tclinterpreter.AbstractTclParser.TclParserError
     * @throws tclinterpreter.TclExpressionParser.UnbalancedParenthesesException
     */
    protected TclNode getArgument() throws TclParserError, UnbalancedParenthesesException {
        TclNode fact;
        TclNode op;
        /*
         Is the first token a factor?
         */
        fact = getFactor();
        /*
         Cycling over the long expression
         */
        try {
            do {
                op = getProdOperation();
                op.getChildren().add(fact);
                fact = getFactor();
                op.getChildren().add(fact);
                fact = op;
            } while (true);
        } catch (TclParserError error) {
            if (currenttoken.type == TclTokenType.RIGHTPAR) {
                fnumber--;
                if (fnumber < 0) {
                    throw new UnbalancedParenthesesException("The number of closing parentheses exceeds the number of opening parentheses");
                }
            }
            if ((currenttoken.type != TclTokenType.EOF
                    && currenttoken.type != TclTokenType.PLUS
                    && currenttoken.type != TclTokenType.MINUS
                    && currenttoken.type != TclTokenType.RIGHTPAR)) {
                throw error;
            }
        }
        return fact;
    }

    /**
     * Returning a binary additive operation node
     *
     * @return
     * @throws tclinterpreter.AbstractTclParser.TclParserError
     */
    protected TclNode getAddOperation() throws TclParserError {
        TclNode node = new TclNode(TclNodeType.BINARYOP);
        switch (currenttoken.type) {
            case PLUS:
                node.setValue("+");
                break;
            case MINUS:
                node.setValue("-");
                break;
        }
        return node;
    }

    /**
     * Returning a binary product operation node
     *
     * @return
     * @throws tclinterpreter.AbstractTclParser.TclParserError
     */
    protected TclNode getProdOperation() throws TclParserError {
        TclNode node = new TclNode(TclNodeType.BINARYOP);
        advanceToken(TclTokenType.MUL, TclTokenType.DIV);
        switch (currenttoken.type) {
            case MUL:
                node.setValue("*");
                break;
            case DIV:
                node.setValue("/");
                break;
        }
        return node;
    }

    @Override
    public TclNode parse() throws TclParserError {
        /*
         * Returning an expression evaluation result
         */
        TclNode result;
        try {
            result = getExpression();
        } catch (TclParserError error) {
            if (error.ctokentype == TclTokenType.EOF) {
                return new TclNode(TclNodeType.QSTRING).setValue("0");
            } else {
                throw error;
            }
        }
        if (fnumber > 0) {
            throw new UnbalancedParenthesesException("The number of openning parentheses exceeds the number of closing parentheses");
        }
        return result;
    }

    /**
     * A class for an exception thrown if the parentheses are unbalanced
     */
    public static class UnbalancedParenthesesException extends AbstractTclParser.TclParserError {

        /**
         * A constructor
         *
         * @param msg
         */
        public UnbalancedParenthesesException(String msg) {
            super(msg, null, null);
        }

        @Override
        public String toString() {
            return message;
        }
    }
}
