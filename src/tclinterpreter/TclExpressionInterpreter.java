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
     * @throws tclinterpreter.AbstractTclInterpreter.TclExecutionException
     */
    protected OpResult CalculateNode(TclNode node) throws TclExecutionException {
        OpResult n1, n2, n3;
        /*
         Switching based on the node type
         */
        switch (node.type) {
            /*
             If the node is a number, just get its value if it is parsable
             */
            case NUMBER:
                if (!node.getValue().isEmpty()) {
                    try {
                        return new OpResult(Long.parseLong(node.getValue()));
                    } catch (NumberFormatException ex) {
                    }
                    try {
                        return new OpResult(Double.parseDouble(node.getValue()));
                    } catch (NumberFormatException ex) {
                        return new OpResult(node.getValue());
                    }
                } else {
                    return new OpResult(0l);
                }
                
            /*
             If the node is a string, just get its value
             */
            case STRING:
                return new OpResult(node.getValue());
            /*
             If the node is an unary operation, apply it to the argument
             */
            case UNARYOP:
                n1 = CalculateNode(node.getChildren().get(0));
                switch (node.getValue()) {
                    case "+":
                        if (n1.isDouble()) {
                            return n1;
                        } else {
                            throw new TclExecutionException("Operation + is only applicable to numeric types", node);
                        }
                    case "-":
                        if (n1.isLong()) {
                            return new OpResult(-n1.getLong());
                        } else if (n1.isDouble()) {
                            return new OpResult(-n1.getDouble());
                        } else {
                            throw new TclExecutionException("Operation - is only applicable to numeric types", node);
                        }
                    case "!":
                        if (n1.isDouble()) {
                            return new OpResult((n1.getDouble() == 0) ? 1l : 0l);
                        } else {
                            throw new TclExecutionException("Operation ! is only applicable to numeric types", node);
                        }
                    case "~":
                        if (n1.isLong()) {
                            return new OpResult(~n1.getLong());
                        } else {
                            throw new TclExecutionException("Operation ~ is only applicable to integer types", node);
                        }
                }
                break;
            /*
             If the node is a binary operation, apply it to its two arguments
             */
            case BINARYOP:
                n1 = CalculateNode(node.getChildren().get(0));
                n2 = CalculateNode(node.getChildren().get(1));
                switch (node.getValue()) {
                    case "+":
                        if (n1.isLong() && n2.isLong()) {
                            return new OpResult(n1.getLong() + n2.getLong());
                        } else if (n1.isDouble() && n2.isDouble()) {
                            return new OpResult(n1.getDouble() + n2.getDouble());
                        } else {
                            return new OpResult(n1.toString() + n2.toString());
                        }
                    case "-":
                        if (n1.isLong() && n2.isLong()) {
                            return new OpResult(n1.getLong() - n2.getLong());
                        } else if (n1.isDouble() && n2.isDouble()) {
                            return new OpResult(n1.getDouble() - n2.getDouble());
                        } else {
                            throw new TclExecutionException("Operation - is only applicable to numeric types", node);
                        }
                    case "*":
                        if (n1.isLong() && n2.isLong()) {
                            return new OpResult(n1.getLong() * n2.getLong());
                        } else if (n1.isDouble() && n2.isDouble()) {
                            return new OpResult(n1.getDouble() * n2.getDouble());
                        } else {
                            throw new TclExecutionException("Operation * is only applicable to numeric types", node);
                        }
                    case "/":
                        if (n1.isLong() && n2.isLong()) {
                            return new OpResult(n1.getLong() / n2.getLong());
                        } else if (n1.isDouble() && n2.isDouble()) {
                            return new OpResult(n1.getDouble() / n2.getDouble());
                        } else {
                            throw new TclExecutionException("Operation / is only applicable to numeric types", node);
                        }
                    case "%":
                        if (n1.isLong() && n2.isLong()) {
                            return new OpResult(n1.getLong() % n2.getLong());
                        } else {
                            throw new TclExecutionException("Operation % is only applicable to integer types", node);
                        }
                    case "**":
                        if (n1.isLong() && n2.isLong()) {
                            return new OpResult(Math.pow(n1.getLong(), n2.getLong()));
                        } else if (n1.isDouble() && n2.isDouble()) {
                            return new OpResult(Math.pow(n1.getDouble(), n2.getDouble()));
                        } else {
                            throw new TclExecutionException("Operation ** is only applicable to numeric types", node);
                        }
                    case "<<":
                        if (n1.isLong() && n2.isLong()) {
                            return new OpResult(n1.getLong() << n2.getLong());
                        } else {
                            throw new TclExecutionException("Operation << is only applicable to integer types", node);
                        }
                    case ">>":
                        if (n1.isLong() && n2.isLong()) {
                            return new OpResult(n1.getLong() >> n2.getLong());
                        } else {
                            throw new TclExecutionException("Operation >> is only applicable to integer types", node);
                        }
                    case ">":
                        if (n1.isDouble() && n2.isDouble()) {
                            return new OpResult((long) (n1.getDouble() > n2.getDouble() ? 1 : 0));
                        } else {
                            throw new TclExecutionException("Operation > is only applicable to numeric types", node);
                        }
                    case "<":
                        if (n1.isDouble() && n2.isDouble()) {
                            return new OpResult((long) (n1.getDouble() < n2.getDouble() ? 1 : 0));
                        } else {
                            throw new TclExecutionException("Operation < is only applicable to numeric types", node);
                        }
                    case ">=":
                        if (n1.isDouble() && n2.isDouble()) {
                            return new OpResult((long) (n1.getDouble() >= n2.getDouble() ? 1 : 0));
                        } else {
                            throw new TclExecutionException("Operation >= is only applicable to numeric types", node);
                        }
                    case "<=":
                        if (n1.isDouble() && n2.isDouble()) {
                            return new OpResult((long) (n1.getDouble() <= n2.getDouble() ? 1 : 0));
                        } else {
                            throw new TclExecutionException("Operation <= is only applicable to numeric types", node);
                        }
                    case "eq":
                        if (n1.isDouble() && n2.isDouble()) {
                            return new OpResult(n1.getDouble() == n2.getDouble() ? 1l : 0l);
                        } else {
                            return new OpResult(n1.toString().equals(n2.toString()) ? 1l : 0l);
                        }
                    case "ne":
                        if (n1.isDouble() && n2.isDouble()) {
                            return new OpResult(n1.getDouble() != n2.getDouble() ? 1l : 0l);
                        } else {
                            return new OpResult(!n1.toString().equals(n2.toString()) ? 1l : 0l);
                        }
                    case "&":
                        if (n1.isLong() && n2.isLong()) {
                            return new OpResult(n1.getLong() & n2.getLong());
                        } else {
                            throw new TclExecutionException("Operation & is only applicable to integer types", node);
                        }
                    case "^":
                        if (n1.isLong() && n2.isLong()) {
                            return new OpResult(n1.getLong() ^ n2.getLong());
                        } else {
                            throw new TclExecutionException("Operation ^ is only applicable to integer types", node);
                        }
                    case "|":
                        if (n1.isLong() && n2.isLong()) {
                            return new OpResult(n1.getLong() | n2.getLong());
                        } else {
                            throw new TclExecutionException("Operation | is only applicable to integer types", node);
                        }
                    case "&&":
                        if (n1.isDouble() && n2.isDouble()) {
                            return new OpResult((n1.getDouble() != 0 && n2.getDouble() != 0) ? 1l : 0l);
                        } else {
                            throw new TclExecutionException("Operation && is only applicable to numeric types", node);
                        }
                    case "||":
                        if (n1.isDouble() && n2.isDouble()) {
                            return new OpResult((n1.getDouble() != 0 || n2.getDouble() != 0) ? 1l : 0l);
                        } else {
                            throw new TclExecutionException("Operation || is only applicable to numeric types", node);
                        }
                }
                break;
            /*
             If the node is a ternary operation, apply it to its three arguments
             */
            case TERNARYOP:
                n1 = CalculateNode(node.getChildren().get(0));
                n2 = CalculateNode(node.getChildren().get(1));
                n3 = CalculateNode(node.getChildren().get(2));
                if (n1.isDouble()) {
                    return n1.getDouble() != 0
                            ? (n2.isLong() ? new OpResult(n2.getLong()) : (n2.isDouble() ? new OpResult(n2.getDouble()) : new OpResult(n2.toString())))
                            : (n3.isLong() ? new OpResult(n3.getLong()) : (n3.isDouble() ? new OpResult(n3.getDouble()) : new OpResult(n3.toString())));
                } else {
                    throw new TclExecutionException("The first argument of a ternary operation must be a number!", node);
                }
            default:
                throw new TclExecutionException("Unknown node type", node);
        }
        return n1;
    }

    @Override
    public String run() throws AbstractTclParser.TclParserError, TclExecutionException {
        String result = CalculateNode(parser.parse()).toString();
        return result;
    }

    /**
     * An inner class for operation results
     */
    protected class OpResult {

        private final Long intvalue;
        private final Double doublevalue;
        private final String svalue;

        /**
         * Constructor for double numbers
         *
         * @param value
         */
        public OpResult(Double value) {
            this.doublevalue = value;
            this.intvalue = null;
            this.svalue = value.toString();
        }

        /**
         * Constructor for long numbers
         *
         * @param value
         */
        public OpResult(Long value) {
            this.doublevalue = value.doubleValue();
            this.intvalue = value;
            this.svalue = value.toString();
        }

        /**
         * Constructor for strings
         *
         * @param value
         */
        public OpResult(String value) {
            this.doublevalue = null;
            this.intvalue = null;
            this.svalue = value;
        }

        /**
         * Returnning true if of Double type
         *
         * @return
         */
        public boolean isDouble() {
            return doublevalue != null;
        }

        /**
         * Returnning true if of Long type
         *
         * @return
         */
        public boolean isLong() {
            return intvalue != null;
        }

        /**
         * Returnning true if of String type
         *
         * @return
         */
        public boolean isString() {
            return doublevalue == null && intvalue == null;
        }

        /**
         * Returnning double value if of Double value
         *
         * @return
         */
        public double getDouble() {
            return doublevalue;
        }

        /**
         * Returnning long value if of Long value
         *
         * @return
         */
        public long getLong() {
            return intvalue;
        }

        /**
         * Returnning String value
         *
         * @return
         */
        @Override
        public String toString() {
            return svalue;
        }
    }
}
