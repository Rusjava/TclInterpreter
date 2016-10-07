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
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
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
    public final Map<String, Function<TclNode, String>> COMMANDS = new HashMap<>();

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
        COMMANDS.put("set", node -> {
            String name = readOPNode(node.getChildren().get(0));
            String value;
            String index = null;
            //Checking if the name is the variable of array id
            if (name.charAt(name.length() - 1) == ')' && name.indexOf('(') != -1) {
                index = name.substring(name.lastIndexOf('(') + 1, name.length() - 1);
                name = name.substring(0, name.lastIndexOf('('));
            }
            //Checking if a variable or an array element needs to be set or retrived
            if (node.getChildren().size() == 2) {
                value = readOPNode(node.getChildren().get(1));
                if (index == null) {
                    context.setVaribale(name, value);
                    output.append(" ").append(name).append("=").append(value).append(";");
                } else {
                    context.setArrayElement(name, index, value);
                    output.append(" ").append(name).append("(").append(index).append(")=").append(value).append(";");
                }
            } else if (index == null) {
                value = context.getVaribale(name);
                output.append(" ").append(name).append("=").append(value).append(";");
            } else {
                value = context.getArrayElement(name, index);
                output.append(" ").append(name).append("(").append(index).append(")=").append(value).append(";");
            }
            return value;
        });

        /*
         'Unset' command definition
         */
        COMMANDS.put("unset", node -> {
            String name = readOPNode(node.getChildren().get(0));
            String index = null;
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
        });

        /*
         'Puts' command definition
         */
        COMMANDS.put("puts", node -> {
            String value = readOPNode(node.getChildren().get(0));
            out.append("Tcl> ")
                    .append(value)
                    .append("\n");
            output.append(" output: ").append(value).append(";");
            return value;
        });

        /*
         'Expr' command definition
         */
        COMMANDS.put("expr", node -> {
            //Reading the expression after all allowed substitutions
            String expr = readOPNode(node.getChildren().get(0));
            //The second round of substitutions
            TclNode exprNode = null;
            try {
                exprNode = new TclStringParser(new TclStringLexer(expr)).parse();
            } catch (AbstractTclParser.TclParserError ex) {
                Logger.getLogger(TclInterpreter.class.getName()).log(Level.SEVERE, null, ex);
            }
            //Interpreting the expression
            TclExpressionInterpreter inter = new TclExpressionInterpreter(
                    new TclExpressionParser(new TclExpressionLexer(readOPNode(exprNode))));
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
        });
    }

    /**
     * Executing a Tcl command
     *
     * @param command
     * @return the result of a command
     */
    protected String executeCommand(TclNode command) {
        output.append("\n");
        return COMMANDS.get(command.getValue()).apply(command);
    }

    /**
     * Evaluating the value of the operand
     *
     * @param node
     * @return
     */
    protected String readOPNode(TclNode node) {
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
     * Executing a sequence of commands
     *
     * @param program node
     * @return the result of the last command
     */
    protected String executeProgram(TclNode program) {
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
     */
    @Override
    public String run() throws TclParser.TclParserError {
        TclNode root = parser.parse();
        output.append("Executing ").append(root.getValue()).append(": ");
        return executeProgram(root);
    }

}
