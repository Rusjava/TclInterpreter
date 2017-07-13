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
package tclparser;

import tcllexer.AbstractBasicTclLexer;
import tcllexer.TclToken;
import tcllexer.TclTokenType;

/**
 *
 * @author Ruslan Feshchenko
 * @version 0.2
 */
public abstract class AbstractTclParser {

    /**
     * Current token
     */
    protected TclToken currenttoken;
    
    /**
     * Previous token
     */
    protected TclToken previoustoken;
    /**
     * The associated TclLexer
     */
    protected AbstractBasicTclLexer lexer;
    
    /**
     * 
     * @param lexer
     */
    public AbstractTclParser(AbstractBasicTclLexer lexer) {
        super();
        this.previoustoken = new TclToken(TclTokenType.NULL);
        this.currenttoken = new TclToken(TclTokenType.NULL);
        this.lexer = lexer;
    }

    /**
     * Advancing to the next token. Throwing and exception if a wrong token
     *
     * @param type
     * @throws tclparser.AbstractTclParser.TclParserError
     */
    protected void advanceToken(TclTokenType type) throws TclParser.TclParserError {
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
     * @throws tclparser.AbstractTclParser.TclParserError
     */
    protected void advanceToken(TclTokenType... types) throws TclParser.TclParserError {
        previoustoken = currenttoken;
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
     * Parsing the script and creating the node tree consisting of commands 
     * and other node types
     *
     * @return
     * @throws tclparser.AbstractTclParser.TclParserError
     */
    public abstract TclNode parse() throws TclParser.TclParserError;
    
    /**
     * A class for Tcl parser errors
     */
    public static class TclParserError extends Exception {

        /**
         * A message for the exception
         */
        protected String message;
        
        /**
         * Current and previous tokens at the moment the exception happened
         */
        protected TclTokenType ctokentype, etokentype;

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
