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

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class CleanResizeListener extends ControlAdapter {
    private Rectangle oldRect = null;
    public void controlResized(ControlEvent e) {
        assert e != null;
        assert Display.getCurrent() != null;     // On SWT event thread
        
        // Prevent garbage from Swing lags during resize. Fill exposed areas 
        // with background color. 
        Composite composite = (Composite)e.widget;
        Rectangle newRect = composite.getClientArea();
        if (oldRect != null) {
            int heightDelta = newRect.height - oldRect.height;
            int widthDelta = newRect.width - oldRect.width;
            if ((heightDelta > 0) || (widthDelta > 0)) {
                GC gc = new GC(composite);
                try {
                    gc.fillRectangle(newRect.x, oldRect.height, newRect.width, heightDelta);
                    gc.fillRectangle(oldRect.width, newRect.y, widthDelta, newRect.height);
                } finally {
                    gc.dispose();
                }
            }
        }
        oldRect = newRect;
    }
}
