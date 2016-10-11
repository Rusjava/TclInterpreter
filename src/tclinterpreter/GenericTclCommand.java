/*
 * Copyright (C) 2015 Ruslan Feshchenko
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
 * General class for Tcl commands
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class GenericTclCommand {

    /**
     * Tcl command body
     */
    protected TclCommand command;

    /**
     * Minimal number of argument
     */
    protected int argNumber = 1;
    
    /**
     * Command name
     */
    protected String name;
}
