/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.dialogs;

import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceManager;
import com.iai.proteus.ui.SwtUtil;
import com.iai.proteus.ui.UIUtil;
import com.iai.proteus.wizards.AddServiceWizard;

/**
 * Dialog for managing services 
 * 
 * @author Jakob Henriksson 
 *
 */
public class ManageAllServicesDialog extends TitleAreaDialog {

	private ServiceManager manager; 
	
	private TableViewer tableViewer;
	private Table table;
	
	private Image imgNew;
	private Image imgDelete;

	/**
	 * Constructor
	 * 
	 * @param parentShell
	 * @param manager
	 */
	public ManageAllServicesDialog(Shell parentShell, ServiceManager manager) {
		super(parentShell);
		this.manager = manager;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		
		// add listener to dispose of resources 
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				// dispose of resources 
				if (imgNew != null)
					imgNew.dispose();
				if (imgDelete != null)
					imgDelete.dispose();
			}
		});
		
		imgNew = UIUtil.getImage("icons/fugue/database--plus.png");
		imgDelete = UIUtil.getImage("icons/fugue/database--minus.png");
		
		setTitle("Manage Services");
		setMessage("Add or remove your services");
		Composite area = (Composite) super.createDialogArea(parent);

		ToolBar toolBar = new ToolBar(area, SWT.FLAT | SWT.RIGHT);

		ToolItem tltmNew = new ToolItem(toolBar, SWT.NONE);
		tltmNew.setText("New");
		tltmNew.setImage(imgNew);
		// listener
		tltmNew.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				WizardDialog dialog = 
						new WizardDialog(getShell(), new AddServiceWizard());
				dialog.open(); 
				// update the viewers whose input may have changed 
				tableViewer.refresh();
			}
		});
		
		final ToolItem tltmDelete = new ToolItem(toolBar, SWT.NONE);
		tltmDelete.setText("Delete");
		tltmDelete.setImage(imgDelete);
		// disabled by default 
		tltmDelete.setEnabled(false);
		// listener 
		tltmDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				
				// get the services selected
				Collection<?> selection = SwtUtil.getSelection(tableViewer);
				int count = selection.size();
				
				MessageDialog dialog = 
						UIUtil.getConfirmDialog(getShell(), "Delete services", 
								"Are you sure you want to delete the " +
										count + " selected service" +
										(count > 1 ? "s" : "") + "?");
				
				if (dialog.open() == MessageDialog.OK) {
					 
					for (Object obj : selection) {
						// make sure we are dealing with the right objects
						if (obj instanceof Service) {
							Service service = (Service) obj;
							// remove the service from the service manager
							manager.removeService(service);
						}
					}
					// update the viewers whose input may have changed 
					tableViewer.refresh();
				}
			}
		});		

		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		tableViewer = new TableViewer(container,
				SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		// column sorters
		final ColumnSorterType colSorterType = new ColumnSorterType();
		final ColumnSorterName colSorterName = new ColumnSorterName();
		final ColumnSorterUrl colSorterUrl = new ColumnSorterUrl();
		
		TableViewerColumn colType = new TableViewerColumn(tableViewer, SWT.NONE);
		colType.getColumn().setText("Type");
		colType.getColumn().setWidth(40);
		colType.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Service) {
					Service service = (Service) element;
					return service.getServiceType().toString();
				}
				return "";
			}
		});
		// column sorting 
		colType.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// make sure we have the right sorter
				tableViewer.setSorter(colSorterType);
				// toggle the sort direction 
				colSorterType.changeSortDirection();
				// update the viewers whose input may have changed
				tableViewer.refresh();
			}
		});
		
		TableViewerColumn colName = new TableViewerColumn(tableViewer, SWT.NONE);
		colName.getColumn().setText("Name");
		colName.getColumn().setWidth(200);
		colName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Service) {
					Service service = (Service) element;
					return service.getName();
				}
				return "";
			}
		});
		// column sorting 
		colName.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// make sure we have the right sorter
				tableViewer.setSorter(colSorterName);
				// toggle the sort direction 
				colSorterName.changeSortDirection();
				// update the viewers whose input may have changed
				tableViewer.refresh();
			}
		});		

		TableViewerColumn colUrl = new TableViewerColumn(tableViewer, SWT.NONE);
		colUrl.getColumn().setText("Service URL");
		colUrl.getColumn().setWidth(280);
		colUrl.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Service) {
					Service service = (Service) element;
					return service.getServiceUrl();
				}
				return "";
			}
		});
		// column sorting 
		colUrl.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// make sure we have the right sorter
				tableViewer.setSorter(colSorterUrl);
				// toggle the sort direction 
				colSorterUrl.changeSortDirection();
				// update the viewers whose input may have changed
				tableViewer.refresh();
			}
		});			
		
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer.setContentProvider(new InnerContentProvider());
		tableViewer.setInput(manager);
		
		table.setFocus();
		
		// add listener to set the correct state of the 'delete' toolbar item
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!tableViewer.getSelection().isEmpty()) { 
					tltmDelete.setEnabled(true);
					return;
				} 
				tltmDelete.setEnabled(false);
			}
		});

		return area;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.CLOSE_LABEL,
				true);
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
//				true);
//		createButton(parent, IDialogConstants.CANCEL_ID,
//				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 *
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(550, 529);
	}
	
	
	/**
	 * Content provider for table viewer 
	 * 
	 * @author Jakob Henriksson 
	 *
	 */
	private static class InnerContentProvider implements IStructuredContentProvider {

		Object[] EMPTY_ARRAY = new Object[0];

		@Override
		public Object[] getElements(Object element) {
			if (element instanceof ServiceManager) {
				ServiceManager root = (ServiceManager) element;
				Collection<Service> services = root.getServices(); 
				return services.toArray();
			}
			return EMPTY_ARRAY;
		}
		
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	/**
	 * Abstract sorter, keeping track of direction and sorting 
	 * 
	 * @author Jakob Henriksson
	 *
	 */
	private static abstract class TableSorter extends ViewerSorter {
		
		private boolean desc;
		
		/**
		 * Constructor 
		 */
		public TableSorter() {
			// default
			desc = false;
		}
		
		/**
		 * Change sorting direction 
		 */
		public void changeSortDirection() {
			desc = !desc;
		}
		
		/**
		 * Compare two strings 
		 * 
		 * @param s1
		 * @param s2
		 * @return
		 */
		public int sort(String s1, String s2) {
			if (desc)
				return s1.compareTo(s2);
			else
				return s2.compareTo(s1);			
		}
	}	

	/**
	 * Sorts service types
	 *  
	 * @author Jakob Henriksson 
	 *
	 */
	private static class ColumnSorterType extends TableSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			Service item1 = (Service) e1;
			Service item2 = (Service) e2;
			String s1 = item1.getServiceType().toString();
			String s2 = item2.getServiceType().toString();
			return sort(s1, s2);
		}		
	}
	
	/**
	 * Sorts service names
	 *  
	 * @author Jakob Henriksson 
	 *
	 */
	private static class ColumnSorterName extends TableSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			Service item1 = (Service) e1;
			Service item2 = (Service) e2;
			String s1 = item1.getName().toString();
			String s2 = item2.getName().toString();
			return sort(s1, s2);
		}		
	}	
	
	/**
	 * Sorts service names
	 *  
	 * @author Jakob Henriksson 
	 *
	 */
	private static class ColumnSorterUrl extends TableSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			Service item1 = (Service) e1;
			Service item2 = (Service) e2;
			String s1 = item1.getServiceUrl();
			String s2 = item2.getServiceUrl();
			return sort(s1, s2);
		}		
	}		
}
