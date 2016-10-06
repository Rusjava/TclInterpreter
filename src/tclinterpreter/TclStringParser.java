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
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class TclStringParser extends AbstractTclParser {

    /**
     * Constructor
     *
     * @param lexer a lexer to be used
     */
    public TclStringParser(TclStringLexer lexer) {
        super(lexer);
    }

    @Override
    public TclNode parse() throws TclParserError {
        TclNode node = new TclNode(TclNodeType.STRING).setValue(lexer.getScript());
        do {
            try {
                advanceToken(TclTokenType.EOF);
            } catch (TclParserError error) {
                switch (currenttoken.type) {
                    case LEFTBR:
                        /*
                         Commands in brackets
                         */
                        advanceToken(TclTokenType.STRING, TclTokenType.RIGHTBR);
                        if (currenttoken.type == TclTokenType.STRING) {
                            node.getChildren().add(new TclNode(TclNodeType.PROGRAM).
                                    setValue(currenttoken.getValue()));
                            advanceToken(TclTokenType.RIGHTBR);
                        } else {
                            node.getChildren().add(new TclNode(TclNodeType.PROGRAM).
                                    setValue(""));
                        }
                        break;
                    case DOLLAR:
                        /*
                         A name substitution
                         */
                        advanceToken(TclTokenType.NAME);
                        node.getChildren().add(new TclNode(TclNodeType.NAME).
                                setValue(currenttoken.getValue()));
                        break;
                    case STRING:
                        /*
                         A string without substitutions
                         */
                        node.getChildren().add(new TclNode(TclNodeType.SUBSTRING).
                                setValue(currenttoken.getValue()));
                        break;
                    default:
                        throw new TclParserError("Unknown token within a string",
                                currenttoken.type, TclTokenType.STRING);
                }
            }

        } while (currenttoken.type != TclTokenType.EOF);
        return node;
    }
}
