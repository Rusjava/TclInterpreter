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
    protected double CalculateNode(TclNode node) {
        double result;
        /*
        If the node is number, just get its value
         */
        if (node.getChildren().isEmpty()) {
            if (!node.getValue().isEmpty()) {
                return Double.parseDouble(node.getValue());
            } else {
                return 0;
            }
        }
        result = CalculateNode(node.getChildren().get(0));
        TclNode node2 = node.getChildren().get(1);
        /*
        Operation depends on the node's value
         */
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
                break;
        }
        return result;
    }

    @Override
    public String run() throws AbstractTclParser.TclParserError {
        String result=Double.toString(CalculateNode(parser.parse()));
        return result;
    }

}
