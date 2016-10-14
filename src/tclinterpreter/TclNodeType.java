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
 * An enumeration for Tcl node types
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public enum TclNodeType {

    BINARYOP("BO"),
    UNARYOP("UO"),
    TERNARYOP("TO"),
    FUNC("function"),
    NUMBER("number"),
    PROGRAM("PROG"),
    COMMAND("CMD"),
    SUBSTRING("QS"),
    STRING("CS"),
    OPERAND("OP"),
    NAME("id"),
    WORD("word");

    private final String type;

    /**
     * Constructor
     *
     * @param value
     */
    private TclNodeType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "NodeType: " + type;
    }
}
