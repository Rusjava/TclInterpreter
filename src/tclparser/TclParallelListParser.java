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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tcllexer.AbstractBasicTclLexer;
import tcllexer.TclTokenType;

/**
 * A class for a parallelized parser of Tcl lists which converts a token stream
 * into a stream of list type Tcl nodes
 *
 * @author Ruslan Feshchenko
 * @version 0.2
 */
public class TclParallelListParser extends AbstractTclParser {

    /**
     * The flag indicating if a new list began
     */
    private boolean newList = true;

    /**
     * An executor for a working thread
     */
    private final ExecutorService exc;

    /**
     * A queue for tokens
     */
    private final BlockingQueue<TclNode> tclNodeQueue;

    /**
     * The maximum number of tcl nodes in the blocking queue
     */
    public static final int MAXNODE = 100;

    /**
     * Constructor
     *
     * @param lexer
     */
    public TclParallelListParser(AbstractBasicTclLexer lexer) {
        super(lexer);
        this.exc = Executors.newSingleThreadExecutor();
        this.tclNodeQueue = new ArrayBlockingQueue<>(MAXNODE);
    }

    /**
     * Parsing quote enclosed string and returning a list of Tcl nodes
     *
     * @param str
     * @return
     * @throws tclparser.AbstractTclParser.TclParserError
     */
    protected List<TclNode> parseString(String str) throws AbstractTclParser.TclParserError {
        AbstractTclParser strparser = new TclStringParser(new TclStringLexer(str));
        return strparser.parse().getChildren();
    }

    /**
     * Reading the command and creating the corresponding node
     *
     * @return
     * @throws TclParserError
     */
    protected TclNode getList() throws AbstractTclParser.TclParserError {
        //Beginng a new list
        newList = true;
        TclNode node = new TclNode(TclNodeType.LIST);
        node.setValue("EOF");
        TclNode operand = null;
        try {
            /*
             Getting list members by cycling over space separated arguments
             */
            while (true) {
                try {
                    /*
                     Skipping whitespace tokens
                     */
                    advanceToken(TclTokenType.WHITESPACE, TclTokenType.CMT);
                } catch (AbstractTclParser.TclParserError innererror) {
                    /*
                     Creating a new operand node at the beginning of a list or after whitespace, semi-colons and line ends
                     */
                    if (previoustoken.type == TclTokenType.WHITESPACE || newList == true) {
                        newList = false;
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
                        default:
                            throw innererror;
                    }
                }
            }
        } catch (AbstractTclParser.TclParserError outererror) {
            // Throw an error if not end of line, semi-colon or end of file
            if (currenttoken.type != TclTokenType.EOL
                    && currenttoken.type != TclTokenType.SEMI && currenttoken.type != TclTokenType.EOF) {
                throw outererror;
            }
        }
        return node;
    }

    @Override
    public TclNode parse() throws AbstractTclParser.TclParserError {
        //Get the next list node
        TclNode node = getList();
        //Assigning the first child's value as the list node's value
        if (!node.getChildren().isEmpty()) {
            node.setValue(node.getChildren().get(0).getValue());
        }
        return node;
    }
}
