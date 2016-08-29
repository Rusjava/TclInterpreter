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
 * A class for the Tcl parser which converts the token stream into a tree
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class TclParser {

    /**
     * The current Tcl token
     */
    protected TclToken currenttoken = null;

    /**
     * The previous Tcl token
     */
    protected TclToken previoustoken = null;

    /**
     * The associated TclLexer
     */
    protected TclLexer lexer;

    /**
     * Constructor
     *
     * @param lexer
     */
    public TclParser(TclLexer lexer) {
        this.lexer = lexer;
    }

    /**
     * Advancing to the next token. Throwing and exception if a wrong token
     *
     * @param type
     * @throws tclinterpreter.TclParser.TclParserError
     */
    protected void advanceToken(TclTokenType type) throws TclParserError {
        previoustoken = currenttoken;
        currenttoken = lexer.getToken();
        if (currenttoken.type != type) {
            throw new TclParserError("Parser error", currenttoken.type, type);
        }
    }

    /**
     * Advancing to the next token. Throwing and exception if a wrong token
     *
     * @param types
     * @throws tclinterpreter.TclParser.TclParserError
     */
    protected void advanceToken(TclTokenType... types) throws TclParserError {
        currenttoken = lexer.getToken();
        boolean flag = true;
        for (TclTokenType type : types) {
            flag = flag && currenttoken.type != type;
        }
        if (flag) {
            throw new TclParserError("Parser error", currenttoken.type, types[0]);
        }
    }

    /**
     * Making necessary substitutions in a quote enclosed string
     *
     * @param str
     * @return
     */
    protected String processstring(String str) {
        return str;
    }

    /**
     * Reading the command and creating the corresponding node
     *
     * @return
     * @throws TclParserError
     */
    protected TclNode getCommand() throws TclParserError {
        TclNode node = new TclNode(TclNodeType.COMMAND);
        advanceToken(new TclTokenType[]{TclTokenType.WORD, TclTokenType.WHITESPACE, TclTokenType.EOL});
        if (currenttoken.type != TclTokenType.WORD) {
            advanceToken(new TclTokenType[]{TclTokenType.WORD, TclTokenType.WHITESPACE});
        }
        if (currenttoken.type != TclTokenType.WORD) {
            advanceToken(TclTokenType.WORD);
        }
        node.setValue(currenttoken.getValue());

        try {
            /*
             Getting operands
             */
            while (true) {
                try {
                    /*
                     Skipping whitespace
                     */
                    advanceToken(TclTokenType.WHITESPACE);
                } catch (TclParserError error) {
                    if (currenttoken.type == TclTokenType.WORD) {
                        /*
                         A name as an operand
                         */
                        node.getChildren().add(new TclNode(TclNodeType.OPERAND).
                                setValue(currenttoken.getValue()));
                    } else if (currenttoken.type == TclTokenType.LEFTCURL) {
                        /*
                         A string in curly brackets
                         */
                        advanceToken(new TclTokenType[]{TclTokenType.STRING, TclTokenType.RIGHTCURL});
                        if (currenttoken.type == TclTokenType.STRING) {
                            node.getChildren().add(new TclNode(TclNodeType.OPERAND).
                                    setValue(currenttoken.getValue()));
                            advanceToken(TclTokenType.RIGHTCURL);
                        } else {
                            node.getChildren().add(new TclNode(TclNodeType.OPERAND).
                                    setValue(""));
                        }
                    } else if (currenttoken.type == TclTokenType.LEFTBR) {
                        /*
                         A command in brackets
                         */
                        advanceToken(TclTokenType.STRING);

                    } else if (currenttoken.type == TclTokenType.LEFTQ) {
                        /*
                         A string in quotes
                         */
                        advanceToken(new TclTokenType[]{TclTokenType.STRING, TclTokenType.RIGHTQ});
                        if (currenttoken.type == TclTokenType.STRING) {
                            node.getChildren().add(new TclNode(TclNodeType.OPERAND).
                                    setValue(processstring(currenttoken.getValue())));
                            advanceToken(TclTokenType.RIGHTQ);
                        } else {
                            node.getChildren().add(new TclNode(TclNodeType.OPERAND).
                                    setValue(""));
                        }
                    } else {
                        throw error;
                    }
                }
            }
        } catch (TclParserError outererror) {
            if (currenttoken.type == TclTokenType.EOL || currenttoken.type == TclTokenType.SEMI) {

            } else {
                throw outererror;
            }
        }
        return node;
    }

    /**
     * Parsing the script and creating the node tree consisting of commands
     *
     * @return
     * @throws tclinterpreter.TclParser.TclParserError
     */
    public TclNode parse() throws TclParserError {
        TclNode node = new TclNode(TclNodeType.PROGRAM).setValue("test script");
        try {
            while (true) {
                node.getChildren().add(getCommand());
            }
        } catch (TclParserError error) {
            if (currenttoken.type == TclTokenType.EOF) {
                return node;
            }
            throw error;
        }
    }

    /**
     * A class for Tcl parser errors
     */
    public static class TclParserError extends Exception {

        String message;
        TclTokenType ctokentype, etokentype;

        /**
         * Constructor
         *
         * @param message
         * @param ctokentype
         * @param etokentype
         */
        public TclParserError(String message, TclTokenType ctokentype, TclTokenType etokentype) {
            super();
            this.message = message;
            this.ctokentype = ctokentype;
            this.etokentype = etokentype;
        }

        @Override
        public String toString() {
            return message + ", Found " + ctokentype + ", Expected " + etokentype;
        }
    }
}
