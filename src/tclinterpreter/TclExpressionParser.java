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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class TclExpressionParser extends AbstractTclParser {

    /**
     * List of sets of operations by priority
     */
    protected static final List<Set<TclTokenType>> OPLEVELLIST;

    /**
     * A set of all binary operation types
     */
    protected static final Set<TclTokenType> OPSET;

    static {
        OPLEVELLIST = new ArrayList<>();
        OPLEVELLIST.add(Stream.of(TclTokenType.MUL, TclTokenType.DIV, TclTokenType.REM).collect(Collectors.toSet()));
        OPLEVELLIST.add(Stream.of(TclTokenType.PLUS, TclTokenType.MINUS).collect(Collectors.toSet()));
        OPLEVELLIST.add(Stream.of(TclTokenType.LSHIFT, TclTokenType.RSHIFT).collect(Collectors.toSet()));
        OPLEVELLIST.add(Stream.of(TclTokenType.LESS, TclTokenType.MORE, TclTokenType.LEQ, TclTokenType.MEQ).collect(Collectors.toSet()));
        OPLEVELLIST.add(Stream.of(TclTokenType.IN, TclTokenType.NI, TclTokenType.NE, TclTokenType.EQ).collect(Collectors.toSet()));
        OPLEVELLIST.add(Stream.of(TclTokenType.BAND).collect(Collectors.toSet()));
        OPLEVELLIST.add(Stream.of(TclTokenType.BXOR).collect(Collectors.toSet()));
        OPLEVELLIST.add(Stream.of(TclTokenType.BOR).collect(Collectors.toSet()));
        OPLEVELLIST.add(Stream.of(TclTokenType.AND).collect(Collectors.toSet()));
        OPLEVELLIST.add(Stream.of(TclTokenType.OR).collect(Collectors.toSet()));

        OPSET = new HashSet<>();
        OPLEVELLIST.stream().forEach(set -> {
            OPSET.addAll(set);
        });
    }

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
     * Returning an argument of an exponetial operation
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
                TclTokenType.NOT, TclTokenType.BNOT, TclTokenType.PLUS, TclTokenType.MINUS, TclTokenType.LEFTQ);
        switch (currenttoken.type) {
            case LEFTQ:
                /*
                A string in quotes
                 */
                advanceToken(TclTokenType.STRING, TclTokenType.RIGHTQ);
                if (currenttoken.type == TclTokenType.STRING) {
                    node = new TclNode(TclNodeType.QSTRING).setValue(currenttoken.getValue());
                    advanceToken(TclTokenType.RIGHTQ);
                } else {
                    node = new TclNode(TclNodeType.QSTRING).setValue("");
                }
                checkRightParenthesis();
                break;
            case NUMBER:
                node = new TclNode(TclNodeType.NUMBER).setValue(currenttoken.getValue());
                checkRightParenthesis();
                break;
            case LEFTPAR:
                fnumber++; //Increasing number of folded parantheses
                node = getExpression();
                checkRightParenthesis();
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
     * Checking that a factor is followed by a delimeter and closing parentheses
     *
     * @throws TclParserError
     */
    protected void checkRightParenthesis() throws TclParserError {
        try {
            advanceToken(TclTokenType.RIGHTPAR);
        } catch (TclParserError error) {
            if ((currenttoken.type != TclTokenType.EOF
                    && currenttoken.type != TclTokenType.EXP
                    && currenttoken.type != TclTokenType.QM
                    && currenttoken.type != TclTokenType.COLON
                    && !OPSET.contains(currenttoken.type)
                    && currenttoken.type != TclTokenType.RIGHTPAR)) {
                throw error;
            }
        }
        if (currenttoken.type == TclTokenType.RIGHTPAR) {
            fnumber--;
            if (fnumber < 0) {
                throw new UnbalancedParenthesesException("The number of closing parentheses exceeds the number of opening parentheses");
            }
        }
    }

    /**
     * Returning a factor of a multiplicative operation
     *
     * @return
     * @throws TclParserError
     */
    protected TclNode getFactor2() throws TclParserError {
        TclNode fact;
        TclNode op;
        /*
         Is the first token a factor?
         */
        fact = getFactor();
        if (currenttoken.type == TclTokenType.EXP) {
            op = getBinaryOperation();
            op.getChildren().add(fact);
            fact = getFactor();
            op.getChildren().add(fact);
            fact = op;
        }

        return fact;
    }

    /**
     * Returning the expression with operations of the next priority level
     *
     * @param level array with current expression level
     * @return
     * @throws tclinterpreter.AbstractTclParser.TclParserError
     * @throws tclinterpreter.TclExpressionParser.UnbalancedParenthesesException
     */
    protected TclNode getExpressionLevel(int level) throws TclParserError, UnbalancedParenthesesException {
        //If the last level, go to the exponent
        if (level == -1) {
            return getFactor2();
        }
        //Temporal node variables
        TclNode arg;
        TclNode op;
        /*
         Is the first token an argument?
         */
        arg = getExpressionLevel(level - 1);
        /*
         Cycling over the long expression
         */
        while (OPLEVELLIST.get(level).contains(currenttoken.type)) {
            op = getBinaryOperation();
            op.getChildren().add(arg);
            arg = getExpressionLevel(level - 1);
            op.getChildren().add(arg);
            arg = op;
        }
        return arg;
    }

    /**
     * Returning a binary operation node
     *
     * @return
     */
    protected TclNode getBinaryOperation() {
        TclNode node = new TclNode(TclNodeType.BINARYOP);
        switch (currenttoken.type) {
            case PLUS:
                node.setValue("+");
                break;
            case MINUS:
                node.setValue("-");
                break;
            case MUL:
                node.setValue("*");
                break;
            case DIV:
                node.setValue("/");
                break;
            case REM:
                node.setValue("%");
                break;
            case EXP:
                node.setValue("**");
                break;
            case LSHIFT:
                node.setValue("<<");
                break;
            case RSHIFT:
                node.setValue(">>");
                break;
            case LEQ:
                node.setValue("<=");
                break;
            case MEQ:
                node.setValue(">=");
                break;
            case LESS:
                node.setValue("<");
                break;
            case MORE:
                node.setValue(">");
                break;
            case EQ:
                node.setValue("eq");
                break;
            case NE:
                node.setValue("ne");
                break;
            case IN:
                node.setValue("in");
                break;
            case NI:
                node.setValue("ni");
                break;
            case BAND:
                node.setValue("&");
                break;
            case BXOR:
                node.setValue("^");
                break;
            case BOR:
                node.setValue("|");
                break;
            case AND:
                node.setValue("&&");
                break;
            case OR:
                node.setValue("||");
                break;
        }
        return node;
    }

    /**
     * Returning the highest expression with ternary operation
     *
     * @return
     * @throws tclinterpreter.AbstractTclParser.TclParserError
     * @throws tclinterpreter.TclExpressionParser.UnbalancedParenthesesException
     */
    protected TclNode getExpression() throws TclParserError, UnbalancedParenthesesException {
        //Temporal node variables
        TclNode arg;
        TclNode op;
        /*
         Is the first token an argument?
         */
        arg = getExpressionLevel(OPLEVELLIST.size() - 1);
        /*
         Cycling over the long expression
         */
        if (currenttoken.type == TclTokenType.QM) {
            op = new TclNode(TclNodeType.TERNARYOP);
            op.setValue("?");
            //Logical expression
            op.getChildren().add(arg);
            //The first choice
            arg = getExpressionLevel(OPLEVELLIST.size() - 1);
            op.getChildren().add(arg);
            //If still tenary operation, get the second choice
            if (currenttoken.type == TclTokenType.COLON) {
                arg = getExpressionLevel(OPLEVELLIST.size() - 1);
                op.getChildren().add(arg);
                arg = op;
            } else {
                throw new TclParserError("Broken ternary operation!", currenttoken.type, TclTokenType.COLON);
            }
        }
        return arg;
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
