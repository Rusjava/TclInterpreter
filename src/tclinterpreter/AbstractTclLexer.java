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
 * Basic abstract class for Tcl lexers without support of multi-threading
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public abstract class AbstractTclLexer extends AbstractBasicTclLexer {

    /**
     * A general constructor of parsers
     *
     * @param script a Tcl script
     * @param skipWhitespace Should whitespace be skipped
     */
    public AbstractTclLexer(String script, boolean skipWhitespace) {
        super(script, skipWhitespace);
    }

    @Override
    public TclToken getToken() {
        TclToken nexttoken;
        /*
         Skipping any leading whitespace (if allowed)
         */
        if (Character.isWhitespace(getCurrentchar()) && isSkipWhitespace()) {
            skipWhitespace();
        }
        /*
         Returning an end of file token if end of file is reached
         */
        if (getCurrentchar() == 0) {
            return new TclToken(TclTokenType.EOF);
        }
        /*
         Get a custom token according to a subclass
         */
        nexttoken = getCustomToken();
        /*
         Returning UNKNOWN token in all other cases
         */
        if (nexttoken == null) {
            advancePosition();
            return new TclToken(TclTokenType.UNKNOWN);
        }
        return nexttoken;
    }

    /**
     * Returning a custom Tcl token or null if unknown
     *
     * @return
     */
    protected abstract TclToken getCustomToken();
}
