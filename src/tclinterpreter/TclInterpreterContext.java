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

import java.util.HashMap;
import java.util.Map;

/**
 * Context for Tcl interpreters containing variables and other attributes
 *
 * @author Ruslan Feshchenko
 * @version
 */
public class TclInterpreterContext {

    /**
     * The poiunter to the context of the enclosing Tcl interpreter
     */
    protected TclInterpreterContext upperlevelcontext;

    /**
     * Local variables associated with the context
     */
    protected Map<String, String> variables;

    /**
     * Constructor
     * @param uppercontext the upper level context
     */
    public TclInterpreterContext(TclInterpreterContext uppercontext) {
        variables = new HashMap<>();
        this.upperlevelcontext=uppercontext;
    }

    /**
     * Returning the variables map
     *
     * @return
     */
    public Map<String, String> getVariables() {
        return variables;
    }

    /**
     * Returning the context of the enclosing Tcl interpreter
     *
     * @return
     */
    public TclInterpreterContext getUpperLevelContext() {
        return upperlevelcontext;
    }

    /**
     * Getting value of a particular local variable
     *
     * @param name variable name
     * @return
     */
    public String getVaribale(String name) {
        return variables.get(name);
    }

    /**
     * Deleting a particular local variable
     *
     * @param name variable name
     */
    public void deleteVaribale(String name) {
        variables.remove(name);
    }

    /**
     * Setting value of a particular local variable
     *
     * @param name variable name
     * @param value variable value
     */
    public void setVaribale(String name, String value) {
        variables.put(name, value);
    }
}
