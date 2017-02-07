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
package tcllexer;

/**
 * An enumeration for token types
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public enum TclTokenType {

    NUMBER("number"),
    PLUS("plus"),
    MINUS("minus"),
    MUL("product"),
    DIV("division"),
    EXP("exp"),
    REM("remainder"),
    BOR("bitor"),
    BAND("bitand"),
    OR("or"),
    AND("and"),
    BXOR("xor"),
    LEQ("lessorequal"),
    MEQ("moreorequal"),
    LESS("less"),
    MORE("more"),
    BNOT("bitnot"),
    NOT("not"),
    NE("string non-equality"),
    EQ("string equality"),
    IN("string in list"),
    NI("string not in list"),
    QM("question mark"),
    COLON("colon"),
    LSHIFT("leftshift"),
    RSHIFT("rightshift"),
    LEFTPAR("leftparanthesis"),
    RIGHTPAR("rightparanthesis"),
    LEFTBR("leftbracket"),
    RIGHTBR("rightbracket"),
    LEFTQ("leftquote"),
    RIGHTQ("rightquote"),
    LEFTCURL("leftcurlybracket"),
    RIGHTCURL("rightcurlybracket"),
    SEMI("semicolon"),
    NAME("id"),
    WORD("tclword"),
    EOL("\n"),
    DOLLAR("$"),
    STRING("str"),
    WHITESPACE("space"),
    EOF("eof"),
    CMT("comment"),
    UNKNOWN("unknown");
    
    private final String type;
    /*
     Constructor
     */

    private TclTokenType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TokenType: " + type;
    }
}
