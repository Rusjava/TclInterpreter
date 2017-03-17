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
     * Local arrays associated with the context
     */
    protected Map<String, Map<String, String>> arrays;

    /**
     * Constructor
     *
     * @param uppercontext the upper level context
     */
    public TclInterpreterContext(TclInterpreterContext uppercontext) {
        variables = new HashMap<>();
        arrays = new HashMap<>();
        this.upperlevelcontext = uppercontext;
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
     * Returning the arrays map
     *
     * @return
     */
    public Map<String, Map<String, String>> getArrays() {
        return arrays;
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
     * Getting the value of a particular local variable
     *
     * @param name variable name
     * @return
     */
    public String getVaribale(String name) {
        return variables.get(name);
    }

    /**
     * Getting value of an element of a particular local array
     *
     * @param name variable name
     * @param index array index
     * @return
     */
    public String getArrayElement(String name, String index) {
        return arrays.get(name) == null ? null : arrays.get(name).get(index);
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
     * Deleting a particular element of an array
     *
     * @param name array name
     * @param index array index
     */
    public void deleteArrayElement(String name, String index) {
        if (arrays.remove(name) != null) {
            arrays.remove(name).remove(index);
            //Removing array if it has become empty
            if (arrays.remove(name).isEmpty()) {
                arrays.remove(name);
            }
        }
    }

    /**
     * Setting the value of a particular local variable
     *
     * @param name variable name
     * @param value variable value
     */
    public void setVaribale(String name, String value) {
        variables.put(name, value);
    }
    
    /**
     * Setting the value of a particular element of a local array
     *
     * @param name array name
     * @param index array index
     * @param value array element value
     */
    public void setArrayElement(String name, String index, String value) {
        //Creating the array if it does not exist
        if (arrays.get(name) == null) {
            arrays.put(name, new HashMap<>());
        }
        arrays.get(name).put(index, value);
    }
}
