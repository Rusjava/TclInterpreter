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
 * A class for Tcl tokens
 *
 * Ruslan Feshchenko
 *
 * @version 0.1
 */
public class TclToken {

    /**
     * Token type
     */
    public final TclTokenType type;

    /**
     * The string value of the token
     */
    protected String value;

    /**
     * Constructor
     *
     * @param type
     */
    public TclToken(TclTokenType type) {
        this.type = type;
        this.value = type.name();
    }

    /**
     * Setting the string value of the token
     *
     * @param value
     * @return 
     */
    public TclToken setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Returning the string value of the token
     *
     * @return
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "" + type + ", TokenValue: " + value;
    }
}
