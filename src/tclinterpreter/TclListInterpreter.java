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

import tclparser.TclNode;
import tclparser.TclExpressionParser;
import tclparser.AbstractTclParser;
import tclparser.TclStringParser;
import tclparser.TclParser;
import tcllexer.TclLexer;
import tcllexer.TclExpressionLexer;
import tcllexer.TclStringLexer;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class interpretes Tcl scripts as collections of lists
 *
 * @author Ruslan Feshchenko
 * @version 0.2
 */
public class TclListInterpreter extends AbstractTclInterpreter {

    /**
     * A map containing all Tcl commands
     *
     */
    public final Map<String, TclCommand<String[], String>> COMMANDS = new HashMap<>();

    /**
     * Constructor, which sets up the interpreter with an attached parser
     *
     * @param parser
     * @param context the upper level context pointer or the current context
     * pointer
     * @param newcontext Should a new context be created
     */
    public TclListInterpreter(TclParser parser, TclInterpreterContext context, boolean newcontext) {
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
    public TclListInterpreter(AbstractTclParser parser, TclInterpreterContext context, boolean newcontext, OutputStream out, String encoding) {
        super(parser, context, newcontext, out, encoding);
    }

    /**
     * Initializing keywords map
     */
    {
        /*
         'set' command definition
         */
        COMMANDS.put("set", new GenericTclCommand("set", 1, (TclCommand<String[], String>) (String... args) -> {
            String value;
            String index = null;
            String name = args[0];
            //Checking if 'name' is the variable of array id
            if (name.charAt(name.length() - 1) == ')' && name.indexOf('(') != -1) {
                index = name.substring(name.lastIndexOf('(') + 1, name.length() - 1);
                name = name.substring(0, name.lastIndexOf('('));
            }
            if (args.length >= 2) {
                //If at least two operands, set the variable or array element
                value = args[1];
                if (index == null) {
                    context.setVaribale(name, value);
                    output.append(" ").append(name).append("=").append(value).append(";\n");
                } else {
                    context.setArrayElement(name, index, value);
                    output.append(" ").append(name).append("(").append(index).append(")=").append(value).append(";\n");
                }
            } else if (index == null) {
                //If only one operand, read and return the variable or array element
                value = context.getVaribale(name);
                output.append(" ").append(name).append("=").append(value).append(";\n");
            } else {
                value = context.getArrayElement(name, index);
                output.append(" ").append(name).append("(").append(index).append(")=").append(value).append(";\n");
            }
            return value;
        }));

        /*
         'append' command definition
         */
        COMMANDS.put("append", new GenericTclCommand("append", 2, (TclCommand<String[], String>) (String... args) -> {
            String value;
            String index = null;
            String name = args[0];
            //Checking if 'name' is the variable or array id
            if (name.charAt(name.length() - 1) == ')' && name.indexOf('(') != -1) {
                index = name.substring(name.lastIndexOf('(') + 1, name.length() - 1);
                name = name.substring(0, name.lastIndexOf('('));
            }
            //Getting variable or array element value
            if (index == null) {
                value = context.getVaribale(name);
                output.append("Intinial value: ").append(name).append("=").append(value).append(";\n");
            } else {
                value = context.getArrayElement(name, index);
                output.append("Intinial value: ").append(name).append("(").append(index).append(")=").append(value).append(";\n");
            }
            //If there are arguments then append them
            if (args.length >= 2) {
                StringBuilder appStr = new StringBuilder();
                //If the variable or array element does not exist assign null length string
                if (value != null) {
                    appStr.append(value);
                }
                //Appending arguments
                for (int i = 1; i < args.length; i++) {
                    appStr.append(args[i]);
                }
                value = appStr.toString();
                //Setting new variable or array element value
                if (index == null) {
                    context.setVaribale(name, value);
                } else {
                    context.setArrayElement(name, index, value);
                }
            }
            //Outputting the final value of the variable
            if (index == null) {
                output.append("Final value: ").append(name).append("=").append(value).append(";\n");
            } else {
                output.append("Final value: ").append(name).append("(").append(index).append(")=").append(value).append(";\n");
            }

            return value;
        }));

        /*
         'unset' command definition
         */
        COMMANDS.put("unset", new GenericTclCommand("unset", 1, (TclCommand<String[], String>) (String... args) -> {
            String index = null, value;
            String name = args[0];
            //Checking if the tlist is the variable of array id
            if (name.charAt(name.length() - 1) == ')' && name.indexOf('(') != -1) {
                index = name.substring(name.lastIndexOf('(') + 1, name.length() - 1);
                name = name.substring(0, name.lastIndexOf('('));
            }
            //Checking if a variable or an array element needs to removed
            if (index == null) {
                value = context.getVaribale(name);
                context.deleteVaribale(name);
                output.append(" ").append(name).append("=").append("undefined;");
            } else {
                value = context.getArrayElement(name, index);
                context.deleteArrayElement(name, index);
                output.append(" ").append(name).append("(").append(index).append(")=").append("undefined;");
            }
            return value;
        }));

        /*
         'Puts' command definition
         */
        COMMANDS.put("puts", new GenericTclCommand("puts", 1, (TclCommand<String[], String>) (String... args) -> {
            out.append("Tcl> ")
                    .append(args[0])
                    .append("\n");
            output.append(" output: ").append(args[0]).append(";\n");
            return args[0];
        }));

        /*
         'Expr' command definition
         */
        COMMANDS.put("expr", new GenericTclCommand("expr", 1, (TclCommand<String[], String>) (String... args) -> {
            //The second round of substitutions
            String result = evaluateExpression(args[0]);
            //Creating output
            output.append(" expression=").append(result).append(";\n");
            return result;
        }));
        /*
         'if' command definition
         */
        COMMANDS.put("if", new GenericTclCommand("if", 2, (TclCommand<String[], String>) (String... args) -> {
            String result = null;
            List<String> argList = Arrays.asList(args);
            String intresult;
            //Creating an iterator over the list of arguments
            Iterator<String> iter = argList.iterator();
            String expression = evaluateExpression(iter.next());
            //Iterating until an exception is thrown
            try {
                while (true) {
                    intresult = iter.next();
                    //If the next argument is equel to 'then', then go to the next argument
                    if (intresult.toLowerCase().equals("then")) {
                        intresult = iter.next();
                    }
                    //If the condition is true return the first expression
                    //In other case read and return the last expression or if 'elseif' go to the next iteration
                    if (readBooleanString(expression) == 1) {
                        //Parsing and interprerting the first body
                        result = evaluateScript(intresult);
                        output.append(" if=then: ").append(result).append(";\n");
                        return result;
                    } else {
                        intresult = iter.next();
                        switch (intresult.toLowerCase()) {
                            case "elseif":
                                expression = evaluateExpression(iter.next());
                                break;
                            case "else":
                                //Reading, parsing and interprerting the second body
                                intresult = iter.next();
                                result = evaluateScript(intresult);
                            default:
                                output.append(" if=else: ").append(result).append(";\n");
                                return result;
                        }
                    }
                }
            } catch (NoSuchElementException ex) {
                output.append(" if=").append(result).append(";\n");
                return result;
            }
        }));

        /*
         'for' command definition
         */
        COMMANDS.put("for", new GenericTclCommand("for", 4, (TclCommand<String[], String>) (String... args) -> {
            //Reading, parsing and interprerting the first expression
            evaluateScript(args[0]);
            //Reading the condition string
            String conString = args[1];
            //Reading the final expression string
            String finalString = args[2];
            //Reading the cycle body string
            String action = args[3];
            //Result
            String result = null;
            //The first evaluation of the conditional expression
            String condition = evaluateExpression(conString);
            //The main cycle
            while (readBooleanString(condition) == 1) {
                //Evaluating the body of the cycle
                result = evaluateScript(action);
                //Evaluating the final expression of the cycle
                evaluateScript(finalString);
                //Evaluating the conditional expression
                condition = evaluateExpression(conString);
            }
            //Writing the body evaluation condition as the output
            output.append(" 'for' expression=").append(result).append(";\n");
            return result;
        }));

        /*
         'while' cycle command definition
         */
        COMMANDS.put("while", new GenericTclCommand("while", 2, (TclCommand<String[], String>) (String... args) -> {
            //Reading the conditional string and the cycle body
            String conString = args[0];
            String action = args[1];
            //Result
            String result = null;
            //The first evaluation of the conditional expression
            String condition = evaluateExpression(conString);
            //The main cycle
            while (readBooleanString(condition) == 1) {
                //Parsing and interprerting the cycle body
                result = evaluateScript(action);
                //Evaluating the first operand as a conditional expression
                condition = evaluateExpression(conString);
            }
            //Writing the body evaluation condition as the output
            output.append(" 'while' expression=").append(result).append(";\n");
            return result;
        }));

        /*
         'string' command definition
         */
        COMMANDS.put("string", new GenericTclCommand("string", 2, (TclCommand<String[], String>) (String... args) -> {
            //Variable for the result
            String result = null;
            int i = 0, k;
            //Executingg different subcommands
            try {
                switch (args[0]) {
                    case "length":
                        //String length
                        result = Integer.toString(args[1].length());
                        break;
                    case "index":
                        //The char at index position
                        try {
                            result = "" + args[1].charAt(Integer.parseInt(args[2]));
                        } catch (NumberFormatException ex) {
                            throw new AbstractTclInterpreter.TclExecutionException("The index of a string must be an integer number!", null);
                        }
                        break;
                    case "range":
                        //Returng a substring
                        result = args[1].substring(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                        break;
                    case "compare":
                        //Comparing two strings
                        result = Integer.toString(args[1].compareTo(args[2]));
                        break;
                    case "match":
                        //Matching two strings
                        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + args[1]);
                        result = Integer.toString(matcher.matches(Paths.get(args[2])) ? 1 : 0);
                        break;
                    case "first":
                        //Index of the first character of a substring
                        result = Integer.toString(args[2].indexOf(args[1]));
                        break;
                    case "last":
                        //Index of the last character of a substring
                        result = Integer.toString(args[2].lastIndexOf(args[1]));
                        break;
                    case "wordstart":
                        //The index of the first character of the word contating the index character
                        result = args[1];
                        //Reading the character's index
                        try {
                            i = Integer.parseInt(args[2]);
                        } catch (NumberFormatException ex) {
                            throw new AbstractTclInterpreter.TclExecutionException("The index of a string must be an integer number!", null);
                        }
                        k = i;
                        while ((Character.isLetterOrDigit(result.charAt(k)) || result.charAt(k) == '_')) {
                            k--;
                            if (k == -1) {
                                break;
                            }
                        }
                        result = "" + (Character.isLetterOrDigit(result.charAt(i)) || result.charAt(i) == '_' ? k + 1 : i);
                        break;
                    case "wordend":
                        //The index of the last+1 character of the word contating the index character
                        result = args[1];
                        //Reading the character's index
                        try {
                            i = Integer.parseInt(args[2]);
                        } catch (NumberFormatException ex) {
                            throw new AbstractTclInterpreter.TclExecutionException("The index of a string must be an integer number!", null);
                        }
                        k = i;
                        while ((Character.isLetterOrDigit(result.charAt(k)) || result.charAt(k) == '_')) {
                            k++;
                            if (k == result.length()) {
                                break;
                            }
                        }
                        result = "" + (Character.isLetterOrDigit(result.charAt(i)) || result.charAt(i) == '_' ? k : i + 1);
                        break;
                    case "tolower":
                        //Converting to lower case
                        result = args[1].toLowerCase();
                        break;
                    case "toupper":
                        //Converting to upper case
                        result = args[1].toUpperCase();
                        break;
                    case "trimleft":
                        //Trimming chars from the left
                        result = trimString(args, -1);
                        break;
                    case "trimright":
                        //Trimming chars from the right
                        result = trimString(args, 1);
                        break;
                    case "trim":
                        //Trimming chars from the left
                        result = trimString(args, 0);
                        break;
                    default:
                        throw new AbstractTclInterpreter.TclExecutionException("Unknown string subcommand!", null);
                }
            } catch (NumberFormatException ex) {
                throw new AbstractTclInterpreter.TclExecutionException("String indexes must be integer numbers!", null);
            }
            output.append(" string=").append(result).append(";\n");
            return result;
        }));

        /*
         'format' command definition - formatted output to a string
         */
        COMMANDS.put("format", new GenericTclCommand("format", 2, (TclCommand<String[], String>) (String... args) -> {
            //Variable for the result
            String result = null;
            int i;
            //Format string
            String fmStr;
            Object[] fargs = new Object[args.length - 1];
            List<String> fmts = null;
            //Extracting formatters
            try {
                fmts = getStringFormatters(args[0]);
            } catch (IllegalFormatException ex) {
                throw new AbstractTclInterpreter.TclExecutionException("Illegal format string! - " + ex.getMessage(), null);
            }
            //Extracting values to print
            try {
                for (i = 1; i < args.length; i++) {
                    fmStr = args[i];
                    //Converting arguments according to extracted format strings
                    switch (fmts.get(i - 1).charAt(fmts.get(i - 1).length() - 1)) {
                        case 's':
                            fargs[i - 1] = fmStr;
                            break;
                        case 'g':
                        case 'G':
                        case 'e':
                        case 'E':
                        case 'f':
                            fargs[i - 1] = Double.valueOf((String) fmStr);
                            break;
                        case 'i':
                        case 'd':
                        case 'u':
                        case 'o':
                        case 'X':
                        case 'x':
                            fargs[i - 1] = Long.valueOf((String) fmStr);
                            break;
                        case 'c':
                            fargs[i - 1] = fmStr.charAt(0);
                            break;
                        default:
                            throw new AbstractTclInterpreter.TclExecutionException("Unsupported formatter! - " + fmStr, null);
                    }
                }
            } catch (NumberFormatException ex) {
                throw new AbstractTclInterpreter.TclExecutionException("An argument does not match the formatter! - "
                        + ex.getMessage(), null);
            }
            //Creating and applying string formatter
            Formatter fmt = new Formatter();
            try {
                result = fmt.format(args[0], fargs).toString();
            } catch (MissingFormatArgumentException ex) {
                throw new AbstractTclInterpreter.TclExecutionException("The number of formatters exceed the number of arguments!", null);
            }
            output.append(" formatted string=").append(result).append(";\n");
            return result;
        }));
        /*
        'list' command - creating a Tcl list
         */
        COMMANDS.put("list", new GenericTclCommand("list", 1, (TclCommand<String[], String>) (String... args) -> {
            StringBuilder result = new StringBuilder();
            //Adding all 'list' command arguments to the list
            Arrays.asList(args).stream().forEach((arg) -> {
                result.append(" {").append(arg).append("}");
            });
            output.append("List: ").append(result).append(";\n");
            //Removing leading space
            if (result.length() != 0) {
                result.deleteCharAt(0);
            }
            return result.toString();
        }));

        /*
        'lindex' command - an element of the list at 'index' position
         */
        COMMANDS.put("lindex", new GenericTclCommand("lindex", 2, (TclCommand<String[], String>) (String... args) -> {
            //List's content
            TclList list = TclList.getList(args[0]);
            String result = null;
            if (list != null) {
                //Reading indexes from all arguments interpreting them as lists
                TclList indexes = TclList.getList(args[1]);
                for (int i = 2; i < args.length; i++) {
                    indexes.addAll(TclList.getList(args[i]));
                }
                //Itterating over enclosed lists
                try {
                    for (int i = 0; i < indexes.size() - 1; i++) {
                        list = TclList.getList(list.get(Integer.parseInt(indexes.get(i))));
                    }
                    //The result is always at the zero element
                    result = list.get(Integer.parseInt(indexes.get(indexes.size() - 1)));
                } catch (NumberFormatException ex) {
                    throw new AbstractTclInterpreter.TclExecutionException("The index of a list element must be an integer number!", null);
                } catch (IndexOutOfBoundsException ex) {
                    throw new AbstractTclInterpreter.TclExecutionException("The index of a list exceeded list's dimensions!", null);
                }
            }
            output.append("Element of the '").append(args[0]).append("' list = ").append(result).append(";\n");
            return result;
        }));

        /*
        'llength' command - the lenght of a list
         */
        COMMANDS.put("llength", new GenericTclCommand("llength", 1, (TclCommand<String[], String>) (String... args) -> {
            //List's content
            TclList list = TclList.getList(args[0]);
            String result = null;
            if (list != null) {
                result = Integer.toString(list.size());
            }
            output.append("The lenght of the '").append(args[0]).append("' list = ").append(result).append(";\n");
            return result;
        }));

        /*
        'split' command - splitting a string into a space separated list using specified characters
         */
        COMMANDS.put("split", new GenericTclCommand("split", 2, (TclCommand<String[], String>) (String... args) -> {
            //List's content
            TclList list = TclList.splitList(args[0], args[1]);
            output.append("The string '").append(args[0]).append("' was split into ").append(list).append(";\n");
            return list.toString();
        }));

        /*
         'lappend' command definition
         */
        COMMANDS.put("lappend", new GenericTclCommand("lappend", 2, (TclCommand<String[], String>) (String... args) -> {
            String value;
            String index = null;
            String name = args[0];
            //Checking if 'name' is the variable or array id
            if (name.charAt(name.length() - 1) == ')' && name.indexOf('(') != -1) {
                index = name.substring(name.lastIndexOf('(') + 1, name.length() - 1);
                name = name.substring(0, name.lastIndexOf('('));
            }
            //Getting the list variable or array element value
            if (index == null) {
                value = context.getVaribale(name);
                output.append("Intinial value: ").append(name).append("=").append(value).append(";\n");
            } else {
                value = context.getArrayElement(name, index);
                output.append("Intinial value: ").append(name).append("(").append(index).append(")=").append(value).append(";\n");
            }
            //If at least two operands, append them to the list variable or array element (with a space)
            if (args.length >= 2) {
                //If the list variable or array element does not exist assign null length string
                StringBuilder appStr = new StringBuilder();
                if (value != null) {
                    appStr.append(value);
                }
                for (int i = 1; i < args.length; i++) {
                    appStr.append(" ").append(args[i]);
                }
                value = appStr.toString();
                //Setting new list variable or array element value
                if (index == null) {
                    context.setVaribale(name, value);
                } else {
                    context.setArrayElement(name, index, value);
                }
            }
            //Outputting the final value of the varibale or array element
            if (index == null) {
                output.append("Final value: ").append(name).append("=").append(value).append(";\n");
            } else {
                output.append("Final value: ").append(name).append("(").append(index).append(")=").append(value).append(";\n");
            }
            return value;
        }));

    }

    /**
     * Executing a Tcl list as a listNode
     *
     * @param listNode
     * @return the result of a listNode
     * @throws tclinterpreter.AbstractTclInterpreter.TclExecutionException
     * @throws tclinterpreter.AbstractTclInterpreter.TclCommandException
     */
    protected String interpretList(TclNode listNode) throws AbstractTclInterpreter.TclExecutionException, AbstractTclInterpreter.TclCommandException {
        //Calling the Tcl listNode or throwing an error if it is not defined
        TclCommand cmd = COMMANDS.get(readOpNode(listNode.getChildren().get(0)));
        if (cmd == null) {
            throw new AbstractTclInterpreter.TclExecutionException("The command " + listNode.getValue() + " is not defined!", listNode);
        }
        //Reading the list of oprerands
        String[] operands = new String[listNode.getChildren().size() - 1];
        for (int i = 0; i < operands.length; i++) {
            operands[i] = readOpNode(listNode.getChildren().get(i + 1));
        }
        return COMMANDS.get(listNode.getValue()).apply(operands);
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
     * Reading a variable or an array element based on the tlist string
     *
     * @param name
     * @return
     */
    protected String readVariable(String name) {
        String index = null;
        //Checking if the tlist is a variable id or an array id
        if (name.charAt(name.length() - 1) == ')' && name.indexOf('(') != -1) {
            index = name.substring(name.lastIndexOf('(') + 1, name.length() - 1);
            name = name.substring(0, name.lastIndexOf('('));
        }
        //Reading either the variable or an array element 
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
     * @return
     * @throws TclExecutionException
     */
    protected String evaluateExpression(String expr) throws AbstractTclInterpreter.TclExecutionException {
        TclNode exprNode = null;
        String result;
        //First submit the expression ot a TclStringParser for substitution
        try {
            exprNode = new TclStringParser(new TclStringLexer(expr)).parse();
        } catch (AbstractTclParser.TclParserError ex) {
            throw new AbstractTclInterpreter.TclExecutionException("Syntax error in Tcl expression!", exprNode);
        }
        //Interpreting the expression
        TclExpressionInterpreter inter = new TclExpressionInterpreter(
                new TclExpressionParser(new TclExpressionLexer(readOpNode(exprNode))));
        try {
            result = inter.run();
        } catch (AbstractTclParser.TclParserError ex) {
            throw new AbstractTclInterpreter.TclExecutionException("Syntax error in Tcl expression!", exprNode);
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
        String result = null;
        AbstractTclInterpreter subinterpreter
                = new TclInterpreter(new TclParser(new TclLexer(script)), context, false);
        //Evaluating the script and catch errors that appear
        try {
            result = subinterpreter.run();
        } catch (AbstractTclParser.TclParserError | AbstractTclInterpreter.TclExecutionException | AbstractTclInterpreter.TclCommandException ex) {
            Logger.getLogger(TclInterpreter.class.getName()).log(Level.SEVERE, null, ex);
        }
        output.append("[").append(subinterpreter.getOutput()).append("]\n");
        return result;
    }

    /**
     * Interpreting the script
     *
     * @return
     * @throws tclparser.AbstractTclParser.TclParserError
     * @throws tclinterpreter.AbstractTclInterpreter.TclExecutionException
     * @throws tclinterpreter.AbstractTclInterpreter.TclCommandException
     */
    @Override
    public String run() throws TclParser.TclParserError, AbstractTclInterpreter.TclExecutionException, AbstractTclInterpreter.TclCommandException {
        TclNode node = parser.parse();
        output.append("Executing test script. First list node: ").append(node.getValue()).append(":\n");
        String result = null;
        while (!node.getValue().equals("EOF")) {
            if (!node.getValue().equals("EOL") && !node.getValue().equals("SEMI")) {
                result = interpretList(node);     
            }
            node = parser.parse();
        }
        return result;
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

    /**
     * A general function for string trimming
     *
     * @param args
     * @param flag 1 - trim left, -1 - trim right and 0 - trim both
     * @return
     */
    protected String trimString(String[] args, int flag) {
        String result = args[1];
        String charset;
        int i = 0, k;
        //Trim spaces if no charset is given
        try {
            charset = args[2];
        } catch (IndexOutOfBoundsException ex) {
            charset = null;
        }
        //Skipping trimmed characters from the left
        if (flag <= 0) {
            while (isInCharset(result.charAt(i), charset)) {
                i++;
                if (i == result.length()) {
                    break;
                }
            }
        }
        k = result.length() - 1;
        //Skipping trimmed characters from the right
        if (flag >= 0) {
            while (isInCharset(result.charAt(k), charset)) {
                k--;
                if (k == -1) {
                    break;
                }
            }
        }
        result = (k != -1 && i != result.length()) ? result.substring(i, k + 1) : "";
        return result;
    }

    /**
     * Getting all formatters in a format string
     *
     * @param fstr - format string
     * @return - list of format specifiers
     */
    protected List<String> getStringFormatters(String fstr) {
        //New formatter
        Formatter fmt = new Formatter();
        Object[] args;
        List<String> lst = new ArrayList<>();
        int fnum = 0;
        boolean flag = true;
        while (flag) {
            flag = false;
            args = new Object[fnum];
            try {
                fmt.format(fstr, args).toString();
            } catch (MissingFormatArgumentException ex) {
                lst.add(ex.getFormatSpecifier());
                flag = true;
                fnum++;
            } catch (IllegalFormatException ex) {
                throw ex;
            }
        }
        return lst;
    }
}
