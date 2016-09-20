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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
    public static final Map<String, Consumer<TclNode>> COMMANDS = new HashMap<>();

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
     * Initializing keywords map
     */
    {
        /*
         'Set' command definition
         */
        COMMANDS.put("set", node -> {
            context.setVaribale(readOPNode(node.getChildren().get(0)), readOPNode(node.getChildren().get(1)));
        });

        /*
         'Unset' command definition
         */
        COMMANDS.put("unset", node -> {
            context.deleteVaribale(readOPNode(node.getChildren().get(0)));
        });

        /*
         'Puts' command definition
         */
        COMMANDS.put("puts", node -> {
            output.append("Tcl> ")
                    .append(readOPNode(node.getChildren().get(0)))
                    .append("\n");
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
            try {
                output.append(inter.run());
            } catch (AbstractTclParser.TclParserError ex) {
                Logger.getLogger(TclInterpreter.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    /**
     * Executing a Tcl command
     *
     * @param command
     */
    protected void executeCommand(TclNode command) {
        COMMANDS.get(command.getValue()).accept(command);
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
     * @param program
     */
    protected void executeProgram(TclNode program) {
        List<TclNode> chld = program.getChildren();
        chld.stream().forEach(this::executeCommand);
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
        output.append("Executing ").append(root.getValue()).append("\n");
        executeProgram(root);
        return output.toString();
    }

}
