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

import tclparser.TclNode;
import tclparser.AbstractTclParser;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public abstract class AbstractTclInterpreter {

    /**
     * A print stream for interpreter output and errors
     */
    protected PrintStream out;

    /**
     * A string for the script output
     */
    protected StringBuilder output = new StringBuilder("");

    /**
     * Current parser
     */
    protected AbstractTclParser parser;

    /**
     * The local context, which includes variables, the pointer to the upper
     * level context and attributes
     */
    protected TclInterpreterContext context;
    
    /**
     * The command result
     */
    protected TclList rlist = null;

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
    protected AbstractTclInterpreter(AbstractTclParser parser, TclInterpreterContext context, boolean newcontext, OutputStream out, String encoding) {
        this.parser = parser;
        if (newcontext) {
            this.context = new TclInterpreterContext(context);
        } else {
            this.context = context;
        }
        try {
            this.out = new PrintStream(out, true, encoding);
        } catch (UnsupportedEncodingException ex) {
            this.out = new PrintStream(out, true);
        }
    }

    /**
     * A full constructor with output print stream
     *
     * @param parser a Tcl parser to use
     * @param context the upper level context pointer or the current context
     * pointer
     * @param newcontext Should a new context should be created of a given
     * context used
     * @param out an output stream
     */
    protected AbstractTclInterpreter(AbstractTclParser parser, TclInterpreterContext context, boolean newcontext, PrintStream out) {
        this.parser = parser;
        if (newcontext) {
            this.context = new TclInterpreterContext(context);
        } else {
            this.context = context;
        }
        this.out = out;
    }

    /**
     * Constructor taking a parser and a context as an argument
     *
     * @param parser
     * @param context the upper level context pointer or the current context
     * pointer
     * @param newcontext Should a new context should be created of a given
     * context used
     */
    protected AbstractTclInterpreter(AbstractTclParser parser, TclInterpreterContext context, boolean newcontext) {
        this(parser, context, newcontext, System.out);
    }

    /**
     * Method, which sets up script
     *
     * @param parser
     */
    public void setParser(AbstractTclParser parser) {
        this.parser = parser;
    }

    /**
     * Returning Tcl parser used
     *
     * @return
     */
    public AbstractTclParser getParser() {
        return parser;
    }

    /**
     * Running the script
     *
     * @return
     * @throws tclinterpreter.TclParser.TclParserError
     * @throws tclinterpreter.AbstractTclInterpreter.TclExecutionException
     */
    public abstract String run() throws AbstractTclParser.TclParserError, TclExecutionException;

    /**
     * Getting the script output string
     *
     * @return
     */
    public String getOutput() {
        return output.toString();
    }

    /**
     * @return the rlist
     */
    public TclList getRlist() {
        return rlist;
    }

    /**
     * A general class for execution errors thrown by interpreters
     *
     */
    public static class TclExecutionException extends Exception {

        /**
         * The node being evaluated
         */
        protected TclNode currentnode;

        /**
         * A construtor
         *
         * @param msg
         * @param currentnode the node being evaluated
         */
        public TclExecutionException(String msg, TclNode currentnode) {
            super(msg);
            this.currentnode = currentnode;
        }

        @Override
        public String toString() {
            return super.getMessage() + " (at " + currentnode + " )";
        }
    }
}
