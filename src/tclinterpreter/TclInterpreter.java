/*
 * Copyright (C) 2015 Ruslan Feshchenko
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

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class interpretes Tcl scripts
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class TclInterpreter extends AbstractTclInterpreter {

    /**
     * A map for Tcl keywords
     */
    public final Map<String, TclCommand<TclNode, String>> COMMANDS = new HashMap<>();

    /**
     * Constructor, which sets up the interpreter with an attached parser
     *
     * @param parser
     * @param context the upper level context pointer or the current context
     * pointer
     * @param newcontext Should a new context be created
     */
    public TclInterpreter(TclParser parser, TclInterpreterContext context, boolean newcontext) {
        super(parser, context, newcontext);
    }

    /**
     * A full constructor with output stream
     *
     * @param parser a Tcl parser to use
     * @param context the upper level context pointer or the current context
     * pointer
     * @param newcontext Should a new context should be created of a given
     * context used
     * @param out an output stream
     * @param encoding an encoding to be used for output encoding
     */
    public TclInterpreter(AbstractTclParser parser, TclInterpreterContext context, boolean newcontext, OutputStream out, String encoding) {
        super(parser, context, newcontext, out, encoding);
    }

    /**
     * Initializing keywords map
     */
    {
        /*
         Empty command
         */
        COMMANDS.put("eof", node -> {
            return null;
        });
        /*
         'Set' command definition
         */
        COMMANDS.put("set", (TclCommand<TclNode, String>) (TclNode node) -> {
            String value = null;
            //If at least operand is present, then execute the commnd
            if (node.getChildren().size() >= 1) {
                String index = null;
                String name = readOpNode(node.getChildren().get(0));
                //Checking if the name is the variable of array id
                if (name.charAt(name.length() - 1) == ')' && name.indexOf('(') != -1) {
                    index = name.substring(name.lastIndexOf('(') + 1, name.length() - 1);
                    name = name.substring(0, name.lastIndexOf('('));
                }
                if (node.getChildren().size() >= 2) {
                    //If at least two operands, set the variable or array element
                    value = readOpNode(node.getChildren().get(1));
                    if (index == null) {
                        context.setVaribale(name, value);
                        output.append(" ").append(name).append("=").append(value).append(";");
                    } else {
                        context.setArrayElement(name, index, value);
                        output.append(" ").append(name).append("(").append(index).append(")=").append(value).append(";");
                    }
                } else {
                    //If only one opernad, read and return the variable or array element
                    if (index == null) {
                        value = context.getVaribale(name);
                        output.append(" ").append(name).append("=").append(value).append(";");
                    } else {
                        value = context.getArrayElement(name, index);
                        output.append(" ").append(name).append("(").append(index).append(")=").append(value).append(";");
                    }
                }
            } else {
                //In no operands, throw an error
                throw new TclExecutionException("'set' command must have at least one argument!", node);
            }
            return value;
        });

        /*
         'Unset' command definition
         */
        COMMANDS.put("unset", (TclCommand<TclNode, String>) (TclNode node) -> {
            //If at least one operand, remove the variable or array element
            if (node.getChildren().size() >= 1) {
                String index = null;
                String name = readOpNode(node.getChildren().get(0));
                //Checking if the name is the variable of array id
                if (name.charAt(name.length() - 1) == ')' && name.indexOf('(') != -1) {
                    index = name.substring(name.lastIndexOf('(') + 1, name.length() - 1);
                    name = name.substring(0, name.lastIndexOf('('));
                }
                //Checking if a variable of an array element needs to removed
                if (index == null) {
                    context.deleteVaribale(name);
                    output.append(" ").append(name).append("=").append("undefined;");
                    return context.getVaribale(name);
                } else {
                    context.deleteArrayElement(name, index);
                    output.append(" ").append(name).append("(").append(index).append(")=").append("undefined;");
                    return context.getArrayElement(name, index);
                }
            } else {
                //In no operands, throw an error
                throw new TclExecutionException("'unset' command must have at least one argument!", node);
            }
        });

        /*
         'Puts' command definition
         */
        COMMANDS.put("puts", (TclCommand<TclNode, String>) (TclNode node) -> {
            //If at least one operand, print the operand
            if (node.getChildren().size() >= 1) {
                String value = readOpNode(node.getChildren().get(0));
                out.append("Tcl> ")
                        .append(value)
                        .append("\n");
                output.append(" output: ").append(value).append(";");
                return value;
            } else {
                //In no operands, throw an error
                throw new TclExecutionException("'puts' command must have at least one argument!", node);
            }
        });

        /*
         'Expr' command definition
         */
        COMMANDS.put("expr", (TclCommand<TclNode, String>) (TclNode node) -> {
            //If at least one operand, interprert the it as an expression
            if (node.getChildren().size() >= 1) {
                //Reading the expression after all allowed substitutions
                String expr = readOpNode(node.getChildren().get(0));
                //The second round of substitutions
                TclNode exprNode = null;
                try {
                    exprNode = new TclStringParser(new TclStringLexer(expr)).parse();
                } catch (AbstractTclParser.TclParserError ex) {
                    Logger.getLogger(TclInterpreter.class.getName()).log(Level.SEVERE, null, ex);
                }
                //Interpreting the expression
                TclExpressionInterpreter inter = new TclExpressionInterpreter(
                        new TclExpressionParser(new TclExpressionLexer(readOpNode(exprNode))));
                String result = null;
                try {
                    result = inter.run();
                } catch (AbstractTclParser.TclParserError ex) {
                    Logger.getLogger(TclInterpreter.class.getName()).log(Level.SEVERE, null, ex);
                } catch (AbstractTclInterpreter.TclExecutionException ex) {
                    Logger.getLogger(TclInterpreter.class.getName()).log(Level.SEVERE, null, ex);
                }
                output.append(" expression=").append(result).append(";");
                return result;
            } else {
                //In no operands, throw an error
                throw new TclExecutionException("'expr' command must have at least one argument!", node);
            }
        });

        /*
         'if' command definition
         */
        COMMANDS.put("if", (TclCommand<TclNode, String>) (TclNode node) -> {
            String result = null;
            //If at least otwo operand, process the 'if' command
            if (node.getChildren().size() >= 2) {
                //Creating an iterator over the list of arguments
                Iterator<TclNode> iter = node.getChildren().iterator();
                String expression = readOpNode(iter.next());
                //Iterating until an exception is thrown
                try {
                    while (true) {
                        result = readOpNode(iter.next());
                        //If the condition is true return the first expression
                        //In other case read and return the last expression or if 'elseif' go to the next iteration
                        if (readBooleanString(expression) == 1) {
                            //If the next argument is equel to 'then', then go to the next argument
                            if (result.toLowerCase().equals("then")) {
                                result = readOpNode(iter.next());
                            }
                            return result;
                        } else {
                            //If the next argument is equel to 'then', then iterate over to the next argument
                            if (result.toLowerCase().equals("then")) {
                                iter.next();
                            }
                            result = readOpNode(iter.next());
                            switch (result.toLowerCase()) {
                                case "elseif":
                                    expression = readOpNode(iter.next());
                                    break;
                                case "else":
                                    result = readOpNode(iter.next());
                                default:
                                    return result;
                            }
                        }
                    }
                } catch (NoSuchElementException ex) {
                    return result;
                }
            } else {
                //In no operands, throw an error
                throw new TclExecutionException("'if' command must have at least two argument!", node);
            }
        });
    }

    /**
     * Executing a Tcl command
     *
     * @param command
     * @return the result of a command
     * @throws tclinterpreter.AbstractTclInterpreter.TclExecutionException
     */
    protected String executeCommand(TclNode command) throws TclExecutionException {
        output.append("\n");
        return COMMANDS.get(command.getValue()).apply(command);
    }

    /**
     * Evaluating the value of an operand node
     *
     * @param node
     * @return
     */
    protected String readOpNode(TclNode node) {
        StringBuilder str = new StringBuilder("");
        for (TclNode child : node.getChildren()) {
            switch (child.type) {
                case NAME:
                    str.append(readVariable(child.getValue()));
                    break;
                case SUBSTRING:
                case STRING:
                    str.append(child.getValue());
                    break;
                case PROGRAM:
                    AbstractTclInterpreter subinterpreter
                            = new TclInterpreter(new TclParser(new TclLexer(child.getValue())), context, false);
                    String result = null;
                    try {
                        result = subinterpreter.run();
                    } catch (AbstractTclParser.TclParserError ex) {
                        Logger.getLogger(TclInterpreter.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (AbstractTclInterpreter.TclExecutionException ex) {
                        Logger.getLogger(TclInterpreter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    str.append(result);
                    output.append("[").append(subinterpreter.getOutput()).append("\n]\n");
                    break;
                case WORD:
                    str.append(child.getValue());
                    break;
                default:
                    break;
            }
        }
        return str.toString();
    }

    /**
     * Reading a variable or an array element based on the name string
     *
     * @param name
     * @return
     */
    protected String readVariable(String name) {
        String index = null;
        //Checking if the name is a variable id or an array id
        if (name.charAt(name.length() - 1) == ')' && name.indexOf('(') != -1) {
            index = name.substring(name.lastIndexOf('(') + 1, name.length() - 1);
            name = name.substring(0, name.lastIndexOf('('));
        }
        //Reading either the variable of an array element 
        if (index == null) {
            return context.getVaribale(name);
        } else {
            return context.getArrayElement(name, index);
        }
    }

    /**
     * Interpreting a string as a boolean value
     *
     * @param str
     * @return
     */
    protected Long readBooleanString(String str) {
        try {
            double nres = Double.parseDouble(str);
            if (nres == 0) {
                return 0l;
            } else {
                return 1l;
            }
        } catch (NumberFormatException ex) {
            switch (str.toLowerCase()) {
                case "yes":
                case "true":
                    return 1l;
                case "no":
                case "false":
                    return 0l;
                default:
                    return null;
            }
        }
    }

    /**
     * Executing a sequence of commands
     *
     * @param program node
     * @return the result of the last command
     * @throws tclinterpreter.AbstractTclInterpreter.TclExecutionException
     */
    protected String executeProgram(TclNode program) throws TclExecutionException {
        List<TclNode> chld = program.getChildren();
        String lastResult = null;
        for (TclNode node : chld) {
            String res = executeCommand(node);
            lastResult = (res == null) ? lastResult : res;
        }
        return lastResult;
    }

    /**
     * Running the script
     *
     * @return
     * @throws tclinterpreter.TclParser.TclParserError
     * @throws tclinterpreter.AbstractTclInterpreter.TclExecutionException
     */
    @Override
    public String run() throws TclParser.TclParserError, TclExecutionException {
        TclNode root = parser.parse();
        output.append("Executing ").append(root.getValue()).append(": ");
        return executeProgram(root);
    }

}
