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

import java.util.ArrayList;
import java.util.List;

/**
 *A class for Tcl nodes
 * 
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class TclNode {
    
    /**
     * The node type
     */
    public TclNodeType type;

    /**
     * The string value for the node
     */
    protected String value;
    
    /**
     * The list of child nodes
     */
    protected final List<TclNode> children;
    
    /**
     * Constructor
     * @param type
     */
    public TclNode (TclNodeType type) {
        this.type=type;
        this.children = new ArrayList<>();
    }

    /**
     * Setting the string value of the node
     *
     * @param value
     * @return 
     */
    public TclNode setValue(String value) {
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
    
    /**
     * Returning a list with node's children
     *
     * @return
     */
    public List<TclNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("");
        str
                .append(type)
                .append(", nodeValue: ")
                .append(getValue())
                .append(" (");
        List<TclNode> chld = getChildren();
        chld.stream().forEach((node) -> {
            str.append(node).append("; ");
        });
        return str.append(")").toString();
    }
}
