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

import tcllexer.TclStringLexer;
import java.util.List;
import tcllexer.AbstractBasicTclLexer;
import tcllexer.TclTokenType;

/**
 * A class for the Tcl parser which converts the token stream into a tree
 *
 * @author Ruslan Feshchenko
 * @version 0.2
 */
public class TclParser extends AbstractTclParser {

    /**
     * Constructor
     *
     * @param lexer
     */
    public TclParser(AbstractBasicTclLexer lexer) {
        super(lexer);
    }

    /**
     * Parsing quote enclosed string and returning a list of Tcl nodes
     *
     * @param str
     * @return
     * @throws tclparser.AbstractTclParser.TclParserError
     */
    protected List<TclNode> parseString(String str) throws TclParserError {
        AbstractTclParser strparser = new TclStringParser(new TclStringLexer(str));
        return strparser.parse().getChildren();
    }

    /**
     * Reading the command and creating the corresponding node
     *
     * @return
     * @throws TclParserError
     */
    protected TclNode getCommand() throws TclParserError {
        TclNode node = new TclNode(TclNodeType.COMMAND);
        node.setValue("eof");
        TclNode operand = null;
        try {
            /*
             Any command should begin with a word with a possible leading whitespace
             */
            advanceToken(TclTokenType.WORD, TclTokenType.WHITESPACE, TclTokenType.EOL, TclTokenType.CMT);
            if (currenttoken.type != TclTokenType.WORD) {
                advanceToken(TclTokenType.WORD, TclTokenType.CMT);
            }
            if (currenttoken.type != TclTokenType.WORD) {
                advanceToken(TclTokenType.WORD);
            }
            node.setValue(currenttoken.getValue());
            /*
             There should be a whitespace after the command name
             */
            advanceToken(TclTokenType.WHITESPACE);
            /**
             * Cycling over arguments
             */

            /*
             Getting operands
             */
            while (true) {
                try {
                    /*
                     Skipping whitespace tokens
                     */
                    advanceToken(TclTokenType.WHITESPACE);
                } catch (TclParserError innererror) {
                    /*
                     Creating a new operand node after whitespace
                     */
                    if (previoustoken.type == TclTokenType.WHITESPACE) {
                        operand = new TclNode(TclNodeType.OPERAND).setValue(currenttoken.getValue());
                        node.getChildren().add(operand);
                    }
                    /*
                     Analysing tokens corresponding to operands
                     */
                    switch (currenttoken.type) {
                        case WORD:
                            /*
                             A variable substitution
                             */
                            operand.getChildren().add(new TclNode(TclNodeType.WORD).
                                    setValue(currenttoken.getValue()));
                            break;
                        case DOLLAR:
                            /*
                             A name as an operand
                             */
                            advanceToken(TclTokenType.NAME);
                            operand.getChildren().add(new TclNode(TclNodeType.NAME).
                                    setValue(currenttoken.getValue()));
                            break;
                        case LEFTCURL:
                            /*
                             A string in curly brackets
                             */
                            advanceToken(TclTokenType.STRING, TclTokenType.RIGHTCURL);
                            if (currenttoken.type == TclTokenType.STRING) {
                                operand.getChildren().add(new TclNode(TclNodeType.STRING).
                                        setValue(currenttoken.getValue()));
                                advanceToken(TclTokenType.RIGHTCURL);
                            } else {
                                operand.getChildren().add(new TclNode(TclNodeType.STRING).
                                        setValue(""));
                            }
                            break;
                        case LEFTBR:
                            /*
                             Commands in brackets
                             */
                            advanceToken(TclTokenType.STRING, TclTokenType.RIGHTBR);
                            if (currenttoken.type == TclTokenType.STRING) {
                                operand.getChildren().add(new TclNode(TclNodeType.PROGRAM).
                                        setValue(currenttoken.getValue()));
                                advanceToken(TclTokenType.RIGHTBR);
                            } else {
                                operand.getChildren().add(new TclNode(TclNodeType.PROGRAM).
                                        setValue(""));
                            }
                            break;
                        case LEFTQ:
                            /*
                             A string in quotes
                             */
                            advanceToken(TclTokenType.STRING, TclTokenType.RIGHTQ);
                            if (currenttoken.type == TclTokenType.STRING) {
                                operand.getChildren().addAll(parseString(currenttoken.getValue()));
                                advanceToken(TclTokenType.RIGHTQ);
                            } else {
                                operand.getChildren().add(new TclNode(TclNodeType.SUBSTRING).
                                        setValue(""));
                            }
                            break;
                        case CMT:
                            break;
                        default:
                            throw innererror;
                    }
                }
            }
        } catch (TclParserError outererror) {
            // Throw an error if not end of line, semi-colon or end of file
            if (currenttoken.type != TclTokenType.EOL
                    && currenttoken.type != TclTokenType.SEMI && currenttoken.type != TclTokenType.EOF) {
                throw outererror;
            }
        }
        return node;
    }

    @Override
    public TclNode parse() throws TclParserError {
        TclNode node = new TclNode(TclNodeType.PROGRAM).setValue("Tcl script");
        //Cyclying over commands until the end of file is reached
        do {
            node.getChildren().add(getCommand());
        } while (currenttoken.type != TclTokenType.EOF);
        return node;
    }
}
