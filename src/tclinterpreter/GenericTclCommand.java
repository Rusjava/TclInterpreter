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
 * General class for string based Tcl commands implementing TclCommand interface
 *
 * @author Ruslan Feshchenko
 * @version 0.2
 */
public class GenericTclCommand implements TclCommand<String[], String> {

    /**
     * Tcl command body
     */
    protected TclCommand<String[], String> command;

    /**
     * Minimal number of argument
     */
    protected int argNumber = 1;

    /**
     * Command name
     */
    protected String name;

    /**
     * Constructor
     *
     * @param name
     * @param argNumber
     * @param command
     */
    public GenericTclCommand(String name, int argNumber, TclCommand<String[], String> command) {
        this.name = name;
        this.argNumber = argNumber;
        this.command = command;
    }

    /**
     * Returning name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " command";
    }

    /**
     * Applying command and checking that the correct number of arguments is
     * present
     *
     * @param args
     * @return the result of the command's execution
     */
    @Override
    public String apply(String... args) throws AbstractTclInterpreter.TclCommandException, AbstractTclInterpreter.TclExecutionException {
        if (command == null) {
            return null;
        }
        //If at least argNumber operand, process the 'while' cycle
        if (args.length >= argNumber) {
            try {
                return command.apply(args);
            } catch (IndexOutOfBoundsException ex) {
                throw new AbstractTclInterpreter.TclCommandException("Insufficient number of operands in '"
                        + name + "' command!", command);
            }
        } else {
            //If less than argNumber operands, throw an error
            throw new AbstractTclInterpreter.TclCommandException(name
                    + " command must have at least " + argNumber + " argument" + (argNumber > 1 ? "s" : "") + "!", command);
        }
    }
}
