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
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import tclinterpreter.GenericTclCommand.TclList;

/**
 * This class interpretes Tcl scripts
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class TclInterpreter extends AbstractTclInterpreter {

    /**
     * A map containing all Tcl commands
     *
     */
    public final Map<String, TclCommand<TclNode, TclList>> COMMANDS = new HashMap<>();

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
        COMMANDS.put("eof", new GenericTclCommand("set", 0, (TclCommand<TclNode, TclList>) (TclNode node) -> {
            TclList list = new TclList();
            list.add("");
            return list;
        }));
        /*
         'Set' command definition
         */
        COMMANDS.put("set", new GenericTclCommand("set", 1, (TclCommand<TclNode, TclList>) (TclNode node) -> {
            String value;
            String index = null;
            TclList list = new TclList();
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
            } else //If only one opernad, read and return the variable or array element
            {
                if (index == null) {
                    value = context.getVaribale(name);
                    output.append(" ").append(name).append("=").append(value).append(";");
                } else {
                    value = context.getArrayElement(name, index);
                    output.append(" ").append(name).append("(").append(index).append(")=").append(value).append(";");
                }
            }
            list.add(value);
            return list;
        }));

        /*
         'Unset' command definition
         */
        COMMANDS.put("unset", new GenericTclCommand("unset", 1, (TclCommand<TclNode, TclList>) (TclNode node) -> {
            String index = null;
            String name = readOpNode(node.getChildren().get(0));
            TclList list = new TclList();
            //Checking if the name is the variable of array id
            if (name.charAt(name.length() - 1) == ')' && name.indexOf('(') != -1) {
                index = name.substring(name.lastIndexOf('(') + 1, name.length() - 1);
                name = name.substring(0, name.lastIndexOf('('));
            }
            //Checking if a variable of an array element needs to removed
            if (index == null) {
                context.deleteVaribale(name);
                output.append(" ").append(name).append("=").append("undefined;");
                list.add(context.getVaribale(name));
            } else {
                context.deleteArrayElement(name, index);
                output.append(" ").append(name).append("(").append(index).append(")=").append("undefined;");
                list.add(context.getArrayElement(name, index));
            }
            return list;
        }));

        /*
         'Puts' command definition
         */
        COMMANDS.put("puts", new GenericTclCommand("puts", 1, (TclCommand<TclNode, TclList>) (TclNode node) -> {
            String value = readOpNode(node.getChildren().get(0));
            out.append("Tcl> ")
                    .append(value)
                    .append("\n");
            output.append(" output: ").append(value).append(";");
            TclList list = new TclList();
            list.add(value);
            return list;
        }));

        /*
         'Expr' command definition
         */
        COMMANDS.put("expr", new GenericTclCommand("expr", 1, (TclCommand<TclNode, TclList>) (TclNode node) -> {
            //The second round of substitutions
            String result = evaluateExpression(readOpNode(node.getChildren().get(0)), node);
            //Creating output
            output.append(" expression=").append(result).append(";");
            TclList list = new TclList();
            list.add(result);
            return list;
        }));
        /*
         'if' command definition
         */
        COMMANDS.put("if", new GenericTclCommand("if", 2, (TclCommand<TclNode, TclList>) (TclNode node) -> {
            String result = null, intresult;
            TclList list = new TclList();
            //Creating an iterator over the list of arguments
            Iterator<TclNode> iter = node.getChildren().iterator();
            String expression = evaluateExpression(readOpNode(iter.next()), node);
            //Iterating until an exception is thrown
            try {
                while (true) {
                    intresult = readOpNode(iter.next());
                    //If the next argument is equel to 'then', then go to the next argument
                    if (intresult.toLowerCase().equals("then")) {
                        intresult = readOpNode(iter.next());
                    }
                    //If the condition is true return the first expression
                    //In other case read and return the last expression or if 'elseif' go to the next iteration
                    if (readBooleanString(expression) == 1) {
                        //Parsing and interprerting the first body
                        result = evaluateScript(intresult);
                        output.append(" if=then: ").append(result).append(";\n");
                        list.add(result);
                        return list;
                    } else {
                        intresult = readOpNode(iter.next());
                        switch (intresult.toLowerCase()) {
                            case "elseif":
                                expression = evaluateExpression(readOpNode(iter.next()), node);
                                break;
                            case "else":
                                //Reading, parsing and interprerting the second body
                                intresult = readOpNode(iter.next());
                                result = evaluateScript(intresult);
                            default:
                                output.append(" if=else: ").append(result).append(";\n");
                                list.add(result);
                                return list;
                        }
                    }
                }
            } catch (NoSuchElementException ex) {
                output.append(" if=").append(result).append(";\n");
                list.add(result);
                return list;
            }
        }));

        /*
         'for' command definition
         */
        COMMANDS.put("for", new GenericTclCommand("for", 4, (TclCommand<TclNode, TclList>) (TclNode node) -> {
            //Reading, parsing and interprerting the first expression
            evaluateScript(readOpNode(node.getChildren().get(0)));
            //Reading the condition string
            String conString = readOpNode(node.getChildren().get(1));
            //Reading the final expression string
            String finalString = readOpNode(node.getChildren().get(2));
            //Reading the cycle body string
            String action = readOpNode(node.getChildren().get(3));
            //Result
            String result = null;
            //The first evaluation of the conditional expression
            String condition = evaluateExpression(conString, node);
            //The main cycle
            while (readBooleanString(condition) == 1) {
                //Evaluating the body of the cycle
                result = evaluateScript(action);
                //Evaluating the final expression of the cycle
                evaluateScript(finalString);
                //Evaluating the conditional expression
                condition = evaluateExpression(conString, node);
            }
            //Writing the body evaluation condition as the output
            output.append(" 'for' expression=").append(result).append(";\n");
            TclList list = new TclList();
            list.add(result);
            return list;
        }));

        /*
         'while' cycle command definition
         */
        COMMANDS.put("while", new GenericTclCommand("while", 2, (TclCommand<TclNode, TclList>) (TclNode node) -> {
            //Reading the conditional string and the cycle body
            String conString = readOpNode(node.getChildren().get(0));
            String action = readOpNode(node.getChildren().get(1));
            //Result
            String result = null;
            //The first evaluation of the conditional expression
            String condition = evaluateExpression(conString, node);
            //The main cycle
            while (readBooleanString(condition) == 1) {
                //Parsing and interprerting the cycle body
                result = evaluateScript(action);
                //Evaluating the first operand as a conditional expression
                condition = evaluateExpression(conString, node);
            }
            //Writing the body evaluation condition as the output
            output.append(" 'while' expression=").append(result).append(";\n");
            TclList list = new TclList();
            list.add(result);
            return list;
        }));

        /*
         'string' command definition
         */
        COMMANDS.put("string", new GenericTclCommand("string", 2, (TclCommand<TclNode, TclList>) (TclNode node) -> {
            //Result
            String result = null, charset, str;
            char[] strarray;
            int i = 0;
            //Executingg different subcommands
            try {
                switch (readOpNode(node.getChildren().get(0))) {
                    case "length":
                        //String length
                        result = Integer.toString(readOpNode(node.getChildren().get(1)).length());
                        break;
                    case "index":
                        //The char at index position
                        result = "" + readOpNode(node.getChildren().get(1))
                                .charAt(Integer.parseInt(readOpNode(node.getChildren().get(2))));
                        break;
                    case "range":
                        //Returng a substring
                        result = readOpNode(node.getChildren().get(1))
                                .substring(Integer.parseInt(readOpNode(node.getChildren().get(2))),
                                        Integer.parseInt(readOpNode(node.getChildren().get(3))));
                        break;
                    case "compare":
                        //Comparing two strings
                        result = Integer.toString(readOpNode(node.getChildren().get(1))
                                .compareTo(readOpNode(node.getChildren().get(2))));
                        break;
                    case "match":
                        //Matching two strings
                        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + readOpNode(node.getChildren().get(1)));
                        result = Integer.toString(matcher.matches(Paths.get(readOpNode(node.getChildren().get(2)))) ? 1 : 0);
                        break;
                    case "first":
                        //Index of the first character of a substring
                        result = Integer.toString(readOpNode(node.getChildren().get(2)).indexOf(readOpNode(node.getChildren().get(1))));
                        break;
                    case "last":
                        //Index of the last character of a substring
                        result = Integer.toString(readOpNode(node.getChildren().get(2)).lastIndexOf(readOpNode(node.getChildren().get(1))));
                        break;
                    case "tolower":
                        //Converting to lower case
                        result = readOpNode(node.getChildren().get(1)).toLowerCase();
                        break;
                    case "toupper":
                        //Converting to upper case
                        result = readOpNode(node.getChildren().get(1)).toUpperCase();
                        break;
                    case "trimleft":
                        //Trimming chars from the left
                        str = readOpNode(node.getChildren().get(1));
                        strarray = str.toCharArray();
                        //Trim spaces if no charset is given
                        try {
                            charset = readOpNode(node.getChildren().get(2));
                        } catch (IndexOutOfBoundsException ex) {
                            charset = null;
                        }
                        //Skipping trimmed chracters
                        while (isInCharset(strarray[i], charset)) {
                            i++;
                        }
                        result = str.substring(i);
                        break;
                    default:
                        throw new TclExecutionException("Unknown string subcommand!", node);
                }
            } catch (NumberFormatException ex) {
                throw new TclExecutionException("String indexes must be ingegers!", node);
            }
            TclList list = new TclList();
            list.add(result);
            return list;
        }));

        /*
        'list' command
         */
        COMMANDS.put("list", new GenericTclCommand("list", 1, (TclCommand<TclNode, TclList>) (TclNode node) -> {
            List<String> list = new ArrayList<>();
            //Adding all 'list' command arguments to the list
            for (TclNode arg : node.getChildren()) {
                list.add(readOpNode(arg));
            }
            TclList tlist = new TclList();
            tlist.add(list.get(0));
            return tlist;
        }));

        /*
        'lindex' command
         */
        COMMANDS.put("lindex", new GenericTclCommand("lindex", 2, (TclCommand<TclNode, TclList>) (TclNode node) -> {
            List<String> list = context.getList(readOpNode(node.getChildren().get(0)));
            String result = null;
            if (list != null) {
                try {
                    result = list.get(Integer.parseInt(readOpNode(node.getChildren().get(1))));
                } catch (NumberFormatException ex) {
                    throw new TclExecutionException("The index of a list element must be an integer number!", node);
                }
            }
            TclList tlist = new TclList();
            tlist.add(result);
            return tlist;
        }));
    }

    /**
     * Executing a Tcl command
     *
     * @param command
     * @return the condition of a command
     * @throws tclinterpreter.AbstractTclInterpreter.TclExecutionException
     */
    protected String executeCommand(TclNode command) throws TclExecutionException {
        output.append("\n");
        return COMMANDS.get(command.getValue()).apply(command).toString();
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
                    str.append(evaluateScript(child.getValue()));
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
     * A method that evaluates Tcl expressions
     *
     * @param expr expression string
     * @param node Tcl node where the expression is found
     * @return
     * @throws TclExecutionException
     */
    protected String evaluateExpression(String expr, TclNode node) throws TclExecutionException {
        TclNode exprNode;
        String result;
        //First submit the expression ot a TclStringParser for substitution
        try {
            exprNode = new TclStringParser(new TclStringLexer(expr)).parse();
        } catch (AbstractTclParser.TclParserError ex) {
            throw new AbstractTclInterpreter.TclExecutionException("Syntax error in Tcl expression!", node);
        }
        //Interpreting the expression
        TclExpressionInterpreter inter = new TclExpressionInterpreter(
                new TclExpressionParser(new TclExpressionLexer(readOpNode(exprNode))));
        try {
            result = inter.run();
        } catch (AbstractTclParser.TclParserError ex) {
            throw new AbstractTclInterpreter.TclExecutionException("Syntax error in Tcl expression!", node);
        }
        return result;
    }

    /**
     * Evaluating a Tcl script
     *
     * @param script
     * @return
     */
    protected String evaluateScript(String script) {
        //Creating a new instance of Tcl interpreter with the same context
        AbstractTclInterpreter subinterpreter
                = new TclInterpreter(new TclParser(new TclLexer(script)), context, false);
        String result = null;
        //Evaluating the script and catch errors that appear
        try {
            result = subinterpreter.run();
        } catch (AbstractTclParser.TclParserError ex) {
            Logger.getLogger(TclInterpreter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AbstractTclInterpreter.TclExecutionException ex) {
            Logger.getLogger(TclInterpreter.class.getName()).log(Level.SEVERE, null, ex);
        }
        output.append("[").append(subinterpreter.getOutput()).append("\n]\n");
        return result;
    }

    /**
     * Executing a sequence of commands
     *
     * @param program node
     * @return the condition of the last command
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

    /**
     * Checking a character belongs to a charset or is a whitespace
     *
     * @param ch
     * @param chset
     * @return
     */
    protected boolean isInCharset(char ch, String chset) {
        if (chset != null) {
            return chset.contains("" + ch);
        } else {
            return Character.isWhitespace(ch);
        }
    }
}
