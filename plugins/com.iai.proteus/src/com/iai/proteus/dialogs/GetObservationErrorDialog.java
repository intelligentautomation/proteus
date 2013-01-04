package com.iai.proteus.dialogs;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.iai.proteus.common.sos.model.SosCapabilities;
import com.iai.proteus.ui.UIUtil;

public class GetObservationErrorDialog extends TitleAreaDialog {
	
	private String exception;
	private SosCapabilities capabilities; 
	
	private Text text;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public GetObservationErrorDialog(Shell parentShell, String exception, 
			SosCapabilities capabilities) 
	{
		super(parentShell);
		this.exception = exception;
		this.capabilities = capabilities;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("There was an error issuing the GetObservation request");
		setTitle("Error retrieving sensor data");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		text = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		text.setText(exception);

		Button btnViewServiceContact = new Button(container, SWT.NONE);
		btnViewServiceContact.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ContactDialog dialog = 
					new ContactDialog(UIUtil.getShell(), capabilities);
				dialog.open();
			}
		});
		btnViewServiceContact.setText("View service contact information");

		return area;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
}
