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
 * A class for an interpreter of arithmetic expressions
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class TclExpressionInterpreter extends AbstractTclInterpreter {

    /**
     * Constructor
     *
     * @param parser an expression parser
     */
    public TclExpressionInterpreter(TclExpressionParser parser) {
        super(parser, null, false);
    }

    /**
     * Calculating node value recursively
     *
     * @param node
     * @return
     */
    protected double CalculateNode(TclNode node) throws TclExecutionException {
        double result = 0;
        /*
        Switching based on the node type
         */
        switch (node.type) {
            /*
             If the node is number, just get its value
             */
            case NUMBER:
                if (!node.getValue().isEmpty()) {
                    result = Double.parseDouble(node.getValue());
                } else {
                    result = 0;
                }
                break;
            /*
             If the node is an unary operation, apply it to the argument
             */
            case UNARYOP:
                result = CalculateNode(node.getChildren().get(0));
                switch (node.getValue()) {
                    case "+":
                        break;
                    case "-":
                        result = -result;
                        break;
                    case "!":
                        result = (result == 0) ? 1 : 0;
                        break;
                    case "~":
                        if (isInteger(result)) {
                            result = ~(long) result;
                        } else {
                            throw new TclExecutionException("Operation ~ is only applicable to integer types", node);
                        }
                }
                break;
            /*
             If the node is a binary operation, apply it to its two arguments
             */
            case BINARYOP:
                result = CalculateNode(node.getChildren().get(0));
                TclNode node2 = node.getChildren().get(1);
                switch (node.getValue()) {
                    case "+":
                        result += CalculateNode(node2);
                        break;
                    case "-":
                        result -= CalculateNode(node2);
                        break;
                    case "*":
                        result *= CalculateNode(node2);
                        break;
                    case "/":
                        result /= CalculateNode(node2);
                }
                break;
            default:
                throw new TclExecutionException("Unknown node type", node);
        }
        return result;
    }

    /**
     * Checking if the double number is really a long number
     *
     * @param number
     * @return
     */
    protected boolean isInteger(double number) {
        return ((long) number == number) ? true : false;
    }

    @Override
    public String run() throws AbstractTclParser.TclParserError, TclExecutionException {
        String result = Double.toString(CalculateNode(parser.parse()));
        return result;
    }
}
