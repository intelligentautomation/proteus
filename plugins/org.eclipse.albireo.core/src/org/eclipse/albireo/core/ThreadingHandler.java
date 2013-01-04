/*******************************************************************************
 * Copyright (c) 2005-2008 SAS Institute Inc., ILOG S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAS Institute Inc. - initial API and implementation
 *     ILOG S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.albireo.core;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;

/**
 * This class deals with threading between SWT and AWT.
 * <p>
 * It is customizable through the "replaceable singleton" design pattern.
 * <p>
 * Note: This class does <em>not</em> combine the two event threads.
 */
public class ThreadingHandler {

    // ========================================================================
    // Accessors

    private boolean reportingAsyncExecExceptions = true;

    /**
     * Returns true if exceptions and throwables occurring during asynchronous
     * execution should be logged on <code>System.err</code>.
     */
    public boolean isReportingAsyncExecExceptions() {
        return reportingAsyncExecExceptions;
    }

    /**
     * Specifies whether exceptions and throwables occurring during asynchronous
     * execution should be logged on <code>System.err</code>.
     * If <code>true</code>, the exception will be shown on the console.
     * If <code>false</code>, the only way to see the exception is
     * <ol>
     *   <li>to be in an application that provides the "Error Log" view,</li>
     *   <li>to open the "Error Log" view,</li>
     *   <li>to look at the event details of all "unhandled event loop
     *       exceptions".</li>
     * </ol>
     * The default is <code>true</code>.
     */
    public void setReportingAsyncExecExceptions(boolean doReporting) {
        reportingAsyncExecExceptions = doReporting;
    }

    // ========================================================================
    // Overridable API

    /**
     * Executes the given <code><var>task</var></code> in the given display's
     * thread.
     * <p>Note: <code>Throwable</code>s thrown by <code><var>task</var></code>
     * will not be displayed. If you want them to be displayed, use a try/catch
     * block with <code>printStackTrace()</code> inside
     * <code><var>task</var></code> yourself.
     *
     * @throws SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the display has been disposed</li>
     * </ul>
     */
    public void asyncExec(Display display, final Runnable task) {
        final Runnable final_task;
        if (isReportingAsyncExecExceptions()) {
            final_task =
                new Runnable() {
                    public void run() {
                        try {
                            task.run();
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                            throw e;
                        } catch (Error e) {
                            e.printStackTrace();
                            throw e;
                        }
                    }
                };
        } else {
            final_task = task;
        }
        try {
            display.asyncExec(final_task);
        } catch (NullPointerException e) {
            // Workaround for wrong order of actions inside Display.dispose().
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=216346
            // http://dev.eclipse.org/newslists/news.eclipse.platform.swt/msg30856.html
            StackTraceElement[] stack = e.getStackTrace();
            if (stack.length > 0
                    && "org.eclipse.swt.widgets.Display".equals(stack[0].getClassName())
                    && "asyncExec".equals(stack[0].getMethodName())) {
                SWTException swte = new SWTException(SWT.ERROR_DEVICE_DISPOSED);
                swte.throwable = e;
                throw swte;
            } else
                throw e;
        }
    }

    // ========================================================================
    // Singleton design pattern

    private static ThreadingHandler theHandler = new ThreadingHandler();

    /**
     * Returns the currently active singleton of this class.
     */
    public static ThreadingHandler getInstance() {
        return theHandler;
    }

    /**
     * Replaces the singleton of this class.
     * @param instance An instance of this class or of a customized subclass.
     */
    public static void setInstance(ThreadingHandler instance) {
        theHandler = instance;
    }

}
