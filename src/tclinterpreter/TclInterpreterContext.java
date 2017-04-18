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
 * Context for Tcl interpreters containing VARIABLES and other attributes
 *
 * @author Ruslan Feshchenko
 * @version
 */
public class TclInterpreterContext {

    /**
     * The pointer to the context of the enclosing Tcl interpreter
     */
    protected TclInterpreterContext upperlevelcontext;

    /**
     * Local VARIABLES associated with the context
     */
    protected final Map<String, String> VARIABLES;

    /**
     * Local ARRAYS associated with the context
     */
    protected final Map<String, Map<String, String>> ARRAYS;

    /**
     * Tcl commands associated with the context
     */
    protected final Map<String, TclCommand<String[], String>> COMMANDS;

    /**
     * Constructor
     *
     * @param uppercontext the upper level context
     */
    public TclInterpreterContext(TclInterpreterContext uppercontext) {
        VARIABLES = new HashMap<>();
        ARRAYS = new HashMap<>();
        COMMANDS = new HashMap<>();
        this.upperlevelcontext = uppercontext;
    }

    /**
     * Returning the VARIABLES map
     *
     * @return
     */
    public Map<String, String> getVariables() {
        return VARIABLES;
    }

    /**
     * Returning the ARRAYS map
     *
     * @return
     */
    public Map<String, Map<String, String>> getArrays() {
        return ARRAYS;
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
    public String getVariable(String name) {
        return VARIABLES.get(name);
    }

    /**
     * Getting value of an element of a particular local array
     *
     * @param name variable name
     * @param index array index
     * @return
     */
    public String getArrayElement(String name, String index) {
        return ARRAYS.get(name) == null ? null : ARRAYS.get(name).get(index);
    }

    /**
     * Deleting a particular local variable
     *
     * @param name variable name
     */
    public void deleteVariable(String name) {
        VARIABLES.remove(name);
    }

    /**
     * Deleting a particular element of an array
     *
     * @param name array name
     * @param index array index
     */
    public void deleteArrayElement(String name, String index) {
        if (ARRAYS.remove(name) != null) {
            ARRAYS.remove(name).remove(index);
            //Removing array if it has become empty
            if (ARRAYS.remove(name).isEmpty()) {
                ARRAYS.remove(name);
            }
        }
    }

    /**
     * Setting the value of a particular local variable
     *
     * @param name variable name
     * @param value variable value
     */
    public void setVariable(String name, String value) {
        VARIABLES.put(name, value);
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
        if (ARRAYS.get(name) == null) {
            ARRAYS.put(name, new HashMap<>());
        }
        ARRAYS.get(name).put(index, value);
    }

    /**
     * Returning a Tcl command
     *
     * @param cname
     * @return
     */
    public TclCommand<String[], String> getCommand(String cname) {
        return COMMANDS.get(cname);
    }

    /**
     * Adding a new Tcl command
     *
     * @param cname
     * @param cmd
     */
    public void addCommand(String cname, TclCommand<String[], String> cmd) {
        COMMANDS.put(cname, cmd);
    }

    /**
     * Getting a variable or an array element
     * @param name
     * @return
     */
    public String getElement(String name) {
        String index;
        //Checking if 'name' is the variable of array id
        if (name.charAt(name.length() - 1) == ')' && name.indexOf('(') != -1) {
            index = name.substring(name.lastIndexOf('(') + 1, name.length() - 1);
            name = name.substring(0, name.lastIndexOf('('));
            return getArrayElement(name, index);
        } else {
            return getVariable(name);
        }
    }
    
    /**
     * Setting the value of a variable of an array element
     * @param name
     * @param value
     */
    public void setElement (String name, String value) {
        String index;
        //Checking if 'name' is the variable of array id
        if (name.charAt(name.length() - 1) == ')' && name.indexOf('(') != -1) {
            index = name.substring(name.lastIndexOf('(') + 1, name.length() - 1);
            name = name.substring(0, name.lastIndexOf('('));
            setArrayElement(name, index, value);
        } else {
            setVariable(name, value);
        }
    }
    
    /**
     * Deleting a variable or an array element
     * @param name
     */
    public void deleteElement(String name) {
        String index;
        //Checking if 'name' is the variable of array id
        if (name.charAt(name.length() - 1) == ')' && name.indexOf('(') != -1) {
            index = name.substring(name.lastIndexOf('(') + 1, name.length() - 1);
            name = name.substring(0, name.lastIndexOf('('));
            deleteArrayElement(name, index);
        } else {
            deleteVariable(name);
        }
    }
}
