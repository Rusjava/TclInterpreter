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

import java.util.ArrayList;
import java.util.Collection;

/**
 * A class for Tcl lists
 *
 * @author Ruslan Feshchenko
 * @version 0.2
 */
public class TclList extends ArrayList<String> {

    /**
     * Simple constructor
     */
    public TclList() {
        super();
    }

    /**
     * Creating a Tcl list with a fixed capacity
     *
     * @param cap
     */
    public TclList(int cap) {
        super(cap);
    }

    /**
     * Creating a Tcl list from a collection
     *
     * @param col
     */
    public TclList(Collection<? extends String> col) {
        super(col);
    }

    /**
     * Printing the Tcl list as space separated string of elements enclosed in
     * braces
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        //If empty list return null
        if (this.isEmpty()) {
            return "";
        }
        //Building the string as a sum of all elements
        str.append("{").append(this.get(0)).append("}");
        for (int i = 1; i < this.size(); i++) {
            str.append(" ").append("{").append(this.get(i)).append("}");
        }
        return str.toString();
    }

    /**
     * Reading string as a TclList
     *
     * @param sList
     * @return
     */
    public static TclList getList(String sList) {
        return splitList(sList, null);
    }

    /**
     * Reading string as a TclList using specified split characters
     *
     * @param sList
     * @param splCh
     * @return
     */
    public static TclList splitList(String sList, String splCh) {
        TclList list = new TclList();
        if (sList != null && !sList.isEmpty()) {
            StringBuilder el = new StringBuilder();
            int cnt = 0, brcnt;
            //Cycling over all characters
            while (cnt < sList.length()) {
                //If new element begins with '{', ignore whitespace until '}' is reached 
                //(taking into account enclosed braces)
                if (sList.charAt(cnt) == '{') {
                    brcnt = 1;
                    cnt++;
                    while (brcnt != 0 && cnt < sList.length()) {
                        if (sList.charAt(cnt) == '}' && cnt + 1 == sList.length()) {
                            brcnt--;
                        } else if (sList.charAt(cnt) == '}' && isSplitCh(sList.charAt(cnt + 1), splCh)) {
                            brcnt--;
                        } else if (sList.charAt(cnt) == '{' && isSplitCh(sList.charAt(cnt - 1), splCh)) {
                            brcnt++;
                        }
                        if (brcnt != 0) {
                            el.append(sList.charAt(cnt));
                        }
                        cnt++;
                    }
                    //If whitespace, add an element to the list, skip whitespace and start a new element
                } else if (isSplitCh(sList.charAt(cnt), splCh)) {
                    list.add(el.toString());
                    el = new StringBuilder();
                    while (isSplitCh(sList.charAt(cnt), splCh) && cnt < sList.length()) {
                        cnt++;
                    }
                    //In any other case just add the character to the current element
                } else {
                    el.append(sList.charAt(cnt));
                    cnt++;
                }
            }
            list.add(el.toString());
        }
        return list;
    }
    
    /**
     * Testing is a character is a splitting character
     * @param ch
     * @param splCh
     * @return 
     */
    private static Boolean isSplitCh(char ch, String splCh) {
        if (splCh == null) {
            return Character.isWhitespace(ch);
        } else {
            return splCh.contains(Character.toString(ch));
        }
    }
}
