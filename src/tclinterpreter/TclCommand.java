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
 * An interface for Tcl command objects
 * 
 * @author Ruslan Feshchenko
 * @version 0.2
 * @param <T> the class for the argument
 * @param <R> the class of the return value
 */
public interface TclCommand<T, R> {
    
    /**
     * The action of the Tcl command
     * 
     * @param node
     * @return
     * @throws tclinterpreter.AbstractTclInterpreter.TclExecutionException
     * @throws tclinterpreter.AbstractTclInterpreter.TclCommandException
     */
    public R apply(T node) throws AbstractTclInterpreter.TclExecutionException, AbstractTclInterpreter.TclCommandException;
    
}
