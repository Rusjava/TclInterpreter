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
     * The pointer to the context of the enclosing Tcl interpreter
     */
    private TclInterpreterContext upperlevelcontext;

    /**
     * Local variables associated with the context
     */
    private final Map<String, String[]> variables;

    /**
     * Local arrays associated with the context
     */
    private final Map<String, Map<String, String>> arrays;

    /**
     * Tcl commands associated with the context
     */
    private final Map<String, TclCommand<String[], String>> commands;

    /**
     * Constructor
     *
     * @param uppercontext the upper level context
     */
    public TclInterpreterContext(TclInterpreterContext uppercontext) {
        variables = new HashMap<>();
        arrays = new HashMap<>();
        commands = new HashMap<>();
        this.upperlevelcontext = uppercontext;
    }

    /**
     * Returning the variables map
     *
     * @return
     */
    public Map<String, String[]> getVariables() {
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
     * Returning the commands map
     *
     * @return
     */
    public Map<String, TclCommand<String[], String>> getCommands() {
        return commands;
    }

    /**
     * Returning the context of the enclosing Tcl interpreter
     *
     * @return
     */
    public TclInterpreterContext getUpperlevelcontext() {
        return upperlevelcontext;
    }

    /**
     * Returning the offset from the this context to the global context
     *
     * @return
     */
    public int getGlobalContextLevelOffset() {
        TclInterpreterContext cnt = this;
        int glc = -1;
        while (cnt != null) {
            cnt = cnt.getUpperlevelcontext();
            glc++;
        }
        return glc;
    }

    /**
     * Returning the context offset by "offset" from this context
     *
     * @param offset - offset from the current context
     * @return
     */
    public TclInterpreterContext getUpperlevelcontext(int offset) {
        TclInterpreterContext cnt = this;
        int global = getGlobalContextLevelOffset();
        //If more than global offset then return the global context
        if (offset > getGlobalContextLevelOffset()) {
            offset = global;
        }
        //Extracting the context with specified offset
        for (int glc = 0; glc < offset; glc++) {
            cnt = cnt.getUpperlevelcontext();
        }
        return cnt;
    }

    /**
     * Getting the value of a particular local variable
     *
     * @param name variable name
     * @return
     */
    public String getVariable(String name) {
        String[] obj = getVariableObject(name);
        return obj == null ? null : obj[0];
    }

    /**
     * Getting the array object of a particular local variable
     *
     * @param name variable name
     * @return
     */
    public String[] getVariableObject(String name) {
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
    public void deleteVariable(String name) {
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
    public void setVariable(String name, String value) {
        if (variables.get(name) == null) {
            variables.put(name, new String[1]);
        }
        variables.get(name)[0] = value;
    }

    /**
     * Setting the object of a particular local variable
     *
     * @param name variable name
     * @param value variable object value
     */
    public void setVariableObject(String name, String[] value) {
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

    /**
     * Returning a Tcl command
     *
     * @param cname
     * @return
     */
    public TclCommand<String[], String> getCommand(String cname) {
        return commands.get(cname);
    }

    /**
     * Adding a new Tcl command
     *
     * @param cname
     * @param cmd
     */
    public void addCommand(String cname, TclCommand<String[], String> cmd) {
        commands.put(cname, cmd);
    }

    /**
     * Getting a variable or an array element
     *
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
     *
     * @param name
     * @param value
     */
    public void setElement(String name, String value) {
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
     *
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
