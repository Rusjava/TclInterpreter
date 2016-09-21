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
         'Set' command definition
         */
        COMMANDS.put("set", node -> {
            String varname=readOPNode(node.getChildren().get(0));
            String varvalue = readOPNode(node.getChildren().get(1));
            context.setVaribale(varname, varvalue);
            output.append(" ").append(varname).append("=").append(varvalue).append(";");
            return varvalue;
        });

        /*
         'Unset' command definition
         */
        COMMANDS.put("unset", node -> {
            String varname = readOPNode(node.getChildren().get(0));
            context.deleteVaribale(varname);
            output.append(" ").append(varname).append("=").append("undefined").append(";");
            return varname;
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
                    str.append(context.getVaribale(child.getValue()));
                    break;
                case QSTRING:
                    str.append(child.getValue());
                    break;
                case CURLYSTRING:
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
     * Executing a sequence of commands
     *
     * @param program node
     * @return the result of the last command
     */
    protected String executeProgram(TclNode program) {
        List<TclNode> chld = program.getChildren();
        String lastResult = null;
        for (TclNode node : chld) {
            lastResult = executeCommand(node);
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
