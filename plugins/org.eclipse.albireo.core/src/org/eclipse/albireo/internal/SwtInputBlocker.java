/*******************************************************************************
 * Copyright (c) 2007-2008 SAS Institute Inc., ILOG S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAS Institute Inc. - initial API and implementation
 *     ILOG S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.albireo.internal;

import java.util.Stack;

import org.eclipse.albireo.core.ThreadingHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;


/**
 * This class, together with {@link AwtDialogListener}, ensures the proper
 * modal behavior of Swing dialogs when running within a SWT environment.
 * It allows to block SWT input while the AWT/Swing modal dialog is visible.
 *
 * @see AwtDialogListener
 */
public class SwtInputBlocker {
    static private SwtInputBlocker instance = null;
    static private int blockCount = 0;
    private Shell shell;
    private final AwtDialogListener dialogListener;
    private Shell parentShell;
    private Stack /* of Shell */ shellsWithActivateListener;
    
    private Listener activateListener = new Listener() {
        public void handleEvent(Event event) {
            // Schedule the AWT focus request so that activation is completed first. Otherwise the focus
            // request can happen before the AWT window is deactivated.
            ThreadingHandler.getInstance().asyncExec(shell.getDisplay(), new Runnable() {
                public void run() {
                    // On some platforms (e.g. Linux/GTK), the 0x0 shell still appears as a dot 
                    // on the screen, so make it invisible by moving it below other windows. This
                    // is unnecessary under Windows and causes a flash, so only make the call when necessary.
                    // note: would like to do this too: parentShell.moveBelow(null);, but see bug 170774
                    shell.moveBelow(null);
                    dialogListener.requestFocus();
                }
            });
        }
    };
    
    private FocusListener focusListener = new FocusAdapter() {
        public void focusGained(FocusEvent e) {
            dialogListener.requestFocus();
        }
    };
    

    private SwtInputBlocker(Shell parent, AwtDialogListener dialogListener) {
        this.parentShell = parent;
        this.dialogListener = dialogListener; 
    }
    
    private void open() {
        assert Display.getCurrent() != null;     // On SWT event thread
        
        // TODO: Will SWT.NO_FOCUS help in any way here?
        // TODO: Another shell is not necessary here if AwtEnvironment.createDialogParentFrame is used. 
        // Construct with the current display, rather than parent. This reduces problems where
        // the AWT dialog gets covered or does not have focus when opened. 
        // Use ON_TOP to prevent a Windows task bar button
        shell = new Shell(Display.getCurrent(), SWT.APPLICATION_MODAL | SWT.NO_TRIM | SWT.ON_TOP);
        shell.setSize(0, 0);
        
        // Add listener(s) to force focus back to the AWT dialog if SWT gets control
        if (Platform.isGtk()) {
            // Under GTK, focus events are not available to detect this condition, 
            // so use the activate event. 
            // TODO: is it necessary to do this for all parents?
            shellsWithActivateListener = new Stack();
            Shell shell = parentShell;
            while (shell != null) {
                shell.addListener(SWT.Activate, activateListener);
                shellsWithActivateListener.push(shell);
                Composite composite = shell.getParent();
                shell = (composite != null) ? composite.getShell() : null;
            }
        } else {
            // Otherwise, restore focus to awt if the shell gets focus  
            // TODO: test on MacOS and Motif
            shell.addFocusListener(focusListener);
        }
        shell.open();
        
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        
        // If windows from other applications have been opened while SWT was being blocked, 
        // the original parent shell can get lost under those windows after the blocking
        // is stopped. Force the parent shell back to the front here.
        if (!parentShell.isDisposed())
            parentShell.forceActive();
    }

    private void close() {
        assert shell != null;
        
        if (Platform.isGtk()) {
            while (!shellsWithActivateListener.isEmpty()) {
                Shell shell = (Shell) shellsWithActivateListener.pop();
                if (!shell.isDisposed())
                    shell.removeListener(SWT.Activate, activateListener);
            }
        }
        shell.dispose();
    }

    public static void unblock() {
        assert blockCount >= 0;
        assert Display.getCurrent() != null;  // On SWT event thread

        // System.out.println("Deleting SWT blocker");
        if (blockCount == 0) {
            return;
        }
        if ((blockCount == 1) && (instance != null)) {
            instance.close();
            instance = null;
        }
        blockCount--;
    }
    
    public static void block(AwtDialogListener dialogListener) {
        assert blockCount >= 0;
        
        // System.out.println("Creating SWT blocker");
        final Display display = Display.getCurrent();
        assert display != null;  // On SWT event thread
        
        blockCount++;
        if (blockCount == 1) {
            assert instance == null;  // should be no existing blocker
            
            // get a shell to parent the blocking dialog
            Shell shell = getShell(display);

            // If there is a shell to block, block input now. If there are no shells, 
            // then there is no input to block. In the case of no shells, we are not
            // protecting against a shell that might get created later. This is a rare
            // enough case to skip, at least for now. In the future, a listener could be 
            // added to cover it. 
            // TODO: if (shell==null) add listener to block shells created later?
            //
            // Block is implemented with a hidden modal dialog. Using setEnabled(false) is another option, but 
            // on some platforms that will grey the disabled controls.
            if (shell != null) {
                instance = new SwtInputBlocker(shell, dialogListener);
                instance.open();
            }
        }
    }
    
    // Find a shell to use, giving preference to the active shell.
    static private Shell getShell(Display display) {
        Shell shell = display.getActiveShell();
        if (shell == null) {
            Shell[] allShells = display.getShells();
            if (allShells.length > 0) {
                shell = allShells[0];
            }
        }
        return shell;
    }
}
