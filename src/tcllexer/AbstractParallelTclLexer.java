/*
 * Copyright (C) 2017 Ruslan Feshchenko
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
package tcllexer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Basic abstract class for parallelized Tcl lexers
 *
 * @author Ruslan Feshchenko
 * @version 0.2
 */
public abstract class AbstractParallelTclLexer extends AbstractBasicTclLexer {

    /**
     * The max size of token queue
     */
    public static final int MAXTKN = 100;

    /**
     * An executor for a working thread
     */
    private final ExecutorService exc;

    /**
     * A queue for tokens
     */
    private final BlockingQueue<TclToken> tokenQueue;
    /**
     * Flag showing if the end of file has been reached
     */
    private boolean finished;

    /**
     * A general constructor parallel lexers
     *
     * @param script a Tcl script
     * @param skipWhitespace Should whitespace be skipped
     */
    public AbstractParallelTclLexer(String script, boolean skipWhitespace) {
        super(script, skipWhitespace);
        this.exc = Executors.newSingleThreadExecutor();
        this.tokenQueue = new ArrayBlockingQueue<>(MAXTKN);
        exc.submit(() -> {
            TclToken tk;
            //Retriving tokens until the queue is full or interrupted or end of file is reached
            while (true) {
                tk = readToken();
                try {
                    tokenQueue.put(tk);
                } catch (InterruptedException ex) {
                    break;
                }
                //If EOF token, stop lexer
                if (tk.type == TclTokenType.EOF) {
                    break;
                }
            }
        });
    }

    @Override
    public TclToken getToken() {
        TclToken tk;
        //Returning EOF token if the lexer is finished
        if (finished) {
            tk = new TclToken(TclTokenType.EOF);
        } else {
            //Reading the top object from the queue
            try {
                tk = tokenQueue.take();
            } catch (InterruptedException ex) {
                tk = new TclToken(TclTokenType.NULL);
            }
            //If EOF token, finishing thread pool and setting 'finished' flag
            if (tk.type == TclTokenType.EOF) {
                close();
            }
        }
        return tk;
    }

    /**
     * A method that reads a next token
     *
     * @return next token
     */
    private TclToken readToken() {
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
            nexttoken = new TclToken(TclTokenType.EOF);
        } else {
            /*
              Get a custom token according to a subclass
             */
            nexttoken = getCustomToken();
            /*
              Returning UNKNOWN token in all other cases
             */
            if (nexttoken == null) {
                advancePosition();
                nexttoken = new TclToken(TclTokenType.UNKNOWN);
            }
        }
        return nexttoken;
    }

    /**
     * Returning a custom Tcl token or null if unknown
     *
     * @return
     */
    protected abstract TclToken getCustomToken();

    /**
     * If the lexer is finished
     *
     * @return the finished
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Finishing the lexer
     */
    public void close() {
        exc.shutdown();
        finished = true;
    }
}