/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceManager;
import com.iai.proteus.model.services.ServiceRoot;
import com.iai.proteus.model.services.ServiceType;
import com.iai.proteus.ui.SwtUtil;
import com.iai.proteus.ui.UIUtil;

/**
 * @author Jakob Henriksson
 *
 */
public class ManageQuerySetServicesDialog extends TitleAreaDialog {

	// the service manager to add services to
	private ServiceManager manager;
	// the service type we are concerned with 
	private ServiceType serviceType;
	
	/*
	 * UI elements 
	 */
	private TableViewer tableViewerServicesQuerySet; 
	private TableViewer tableViewerServicesAvailable;
	
	private Table tableServicesQuerySet;
	private Table tableServicesAvailable;
	
	private Image imgAdd; 
	private Image imgRemove;
	private Image imgDatabase;
	

	/**
	 * Dialog constructor 
	 * 
	 * @param parentShell
	 * @param manager service manager to add services to 
	 * @param serviceType the service type we are concerned with  
	 */
	public ManageQuerySetServicesDialog(Shell parentShell, ServiceManager manager, 
			ServiceType serviceType) 
	{
		super(parentShell);
		
		this.manager = manager;
		this.serviceType = serviceType;
		
		imgAdd = UIUtil.getImage("icons/fugue/plus-button.png");
		imgRemove = UIUtil.getImage("icons/fugue/minus-button.png");
		imgDatabase = UIUtil.getImage("icons/fugue/database.png");
	}

	/**
	 * Create contents of the dialog
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		
		// add dispose listener 
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				// dispose of resources 
				if (imgAdd != null)
					imgAdd.dispose();
				if (imgRemove != null)
					imgRemove.dispose();
				if (imgDatabase != null)
					imgDatabase.dispose();
			}
		});
		
		setMessage("Add or remove services from the query set");
		setTitle("Services");
		Composite area = (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		container.setLayout(new GridLayout(1, true));
		
		/*
		 * Group for query set services
		 */
		Group groupQuerySet = new Group(container, SWT.NONE);
		groupQuerySet.setLayout(new GridLayout(1, false));
		groupQuerySet.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		groupQuerySet.setText("Query set services");
		
		Composite compositeToolbar1 = new Composite(groupQuerySet, SWT.NONE);
		compositeToolbar1.setLayout(new GridLayout(2, false));
		compositeToolbar1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		ToolBar toolBarQuerySet = new ToolBar(compositeToolbar1, SWT.FLAT | SWT.RIGHT);
		toolBarQuerySet.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		final ToolItem tltmRemove = new ToolItem(toolBarQuerySet, SWT.NONE);
		tltmRemove.setText("Remove");
		tltmRemove.setImage(imgRemove);
		// default 
		tltmRemove.setEnabled(false);
		// listener 
		tltmRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// remove the services to the provided service manager
				for (Object elmt : 
					SwtUtil.getSelection(tableViewerServicesQuerySet)) {
					if (elmt instanceof Service) {
						manager.removeService((Service) elmt);
					}
				}
				// update the viewers whose input may have changed 
				tableViewerServicesQuerySet.refresh();
				tableViewerServicesAvailable.refresh();
			}
		});	
		
		ToolBar toolBarSelection1 = new ToolBar(compositeToolbar1, SWT.FLAT | SWT.RIGHT);
		toolBarSelection1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		final ToolItem tltmAll1 = new ToolItem(toolBarSelection1, SWT.NONE);
		tltmAll1.setText("Select all");
		// listener
		tltmAll1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				StructuredSelection ss = 
						new StructuredSelection(manager.getServices(serviceType).toArray());
				// has to be an array or a java.util.List
				tableViewerServicesQuerySet.setSelection(ss);
			}
		});			
		
		final ToolItem tltmNone1 = new ToolItem(toolBarSelection1, SWT.NONE);
		tltmNone1.setText("Select none");
		// listener
		tltmNone1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				tableViewerServicesQuerySet.setSelection(StructuredSelection.EMPTY);
			}
		});		

		
		/*
		 * Services for the query set 
		 */
		tableViewerServicesQuerySet = new TableViewer(groupQuerySet, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tableServicesQuerySet = tableViewerServicesQuerySet.getTable();
		tableServicesQuerySet.setHeaderVisible(true);
		tableServicesQuerySet.setLinesVisible(true);
		tableServicesQuerySet.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		TableViewerColumn tblColViewerName1 = new TableViewerColumn(tableViewerServicesQuerySet, SWT.NONE);
		TableColumn tblclmnName1 = tblColViewerName1.getColumn();
		tblclmnName1.setWidth(250);
		tblclmnName1.setText("Name");
		tblColViewerName1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Service) {
					Service service = (Service) element;
					return service.getName();
				}
				return "";
			}
		});
		
		TableViewerColumn tblColViewerUrl1 = new TableViewerColumn(tableViewerServicesQuerySet, SWT.NONE);
		TableColumn tblclmnUrl1 = tblColViewerUrl1.getColumn();
		tblclmnUrl1.setWidth(250);
		tblclmnUrl1.setText("Url");
		tblColViewerUrl1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Service) {
					Service service = (Service) element;
					return service.getEndpoint();
				}
				return "";
			}
		});			
		
		// listener to update tool item status
		tableViewerServicesQuerySet.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				tltmRemove.setEnabled(!event.getSelection().isEmpty());
			}
		});
		
		// input has to be set after the @{link TableViewerColumns} are defined 
		tableViewerServicesQuerySet.setContentProvider(new InnerContentProvider(manager, serviceType));
		tableViewerServicesQuerySet.setInput(manager);

		
		/*
		 * Group for available services
		 */
		Group groupAvailable = new Group(container, SWT.NONE);
		groupAvailable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		groupAvailable.setLayout(new GridLayout(1, false));
		groupAvailable.setText("All available services");
		
		Composite compositeToolbar2 = new Composite(groupAvailable, SWT.NONE);
		compositeToolbar2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeToolbar2.setLayout(new GridLayout(2, false));
		
		ToolBar toolBarAvailable = new ToolBar(compositeToolbar2, SWT.FLAT | SWT.RIGHT);
		toolBarAvailable.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		
		final ToolItem tltmAddService = new ToolItem(toolBarAvailable, SWT.NONE);
		tltmAddService.setText("Add to query set services");
		tltmAddService.setImage(imgAdd);
		// default 
		tltmAddService.setEnabled(false);
		// listener 
		tltmAddService.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// adds the services to the provided service manager
				for (Object elmt : 
					SwtUtil.getSelection(tableViewerServicesAvailable)) {
					if (elmt instanceof Service) {
						Service service = (Service) elmt;
						// make a copy of the service 
						Service copy = (Service)service.clone();
						// make sure that the service is deactivated by default
						service.deactivate();
						// add the service to the manager
						manager.addService(copy);
					}
				}
				// update the viewers whose input may have changed 
				tableViewerServicesQuerySet.refresh();
				tableViewerServicesAvailable.refresh();
			}
		});
		
		ToolItem tltmManageServices = new ToolItem(toolBarAvailable, SWT.NONE);
		tltmManageServices.setText("Manage services...");
		tltmManageServices.setImage(imgDatabase);
		// listener
		tltmManageServices.addSelectionListener(new SelectionAdapter() {
			@Override 
			public void widgetSelected(SelectionEvent event) {
				// create and open the dialog to manage services 
				ManageAllServicesDialog dialog =
						new ManageAllServicesDialog(UIUtil.getShell(), 
								ServiceRoot.getInstance());
				dialog.open();
				dialog.close();
				// update the viewers whose input may have changed
				tableViewerServicesAvailable.refresh();
			}
		});
		
		ToolBar toolBarSelection2 = new ToolBar(compositeToolbar2, SWT.FLAT | SWT.RIGHT);
		toolBarSelection2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		ToolItem tltmAll2 = new ToolItem(toolBarSelection2, SWT.NONE);
		tltmAll2.setText("Select all");
		// listener
		tltmAll2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				List<Service> all = ServiceRoot.getInstance().getServices(serviceType);
				StructuredSelection ss = new StructuredSelection(all);
				// has to be an array or a java.util.List
				tableViewerServicesAvailable.setSelection(ss);
			}
		});		
		
		ToolItem tltmNone2 = new ToolItem(toolBarSelection2, SWT.NONE);
		tltmNone2.setText("Select none");
		// listener
		tltmNone2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				tableViewerServicesAvailable.setSelection(StructuredSelection.EMPTY);
			}
		});		

		
		/*
		 * Available services 
		 */
		tableViewerServicesAvailable = new TableViewer(groupAvailable, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tableServicesAvailable = tableViewerServicesAvailable.getTable();
		tableServicesAvailable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableServicesAvailable.setHeaderVisible(true);
		tableServicesAvailable.setLinesVisible(true);
		
		TableViewerColumn tblColViewerName2 = new TableViewerColumn(tableViewerServicesAvailable, SWT.NONE);
		TableColumn tblclmnName2 = tblColViewerName2.getColumn();
		tblclmnName2.setWidth(250);
		tblclmnName2.setText("Name");
		tblColViewerName2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Service) {
					Service service = (Service) element;
					return service.getName();
				}
				return "";
			}
		});
		
		TableViewerColumn tblColViewerUrl2 = new TableViewerColumn(tableViewerServicesAvailable, SWT.NONE);
		TableColumn tblclmnUrl2 = tblColViewerUrl2.getColumn();
		tblclmnUrl2.setWidth(250);
		tblclmnUrl2.setText("Url");
		tblColViewerUrl2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Service) {
					Service service = (Service) element;
					return service.getEndpoint();
				}
				return "";
			}
		});	
		
		// listener to update tool item status
		tableViewerServicesAvailable.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				tltmAddService.setEnabled(!event.getSelection().isEmpty());
			}
		});

		// input has to be set after the @{link TableViewerColumns} are defined 
		tableViewerServicesAvailable.setContentProvider(new InnerContentProvider(manager, serviceType));
		tableViewerServicesAvailable.setInput(ServiceRoot.getInstance());
		
		return area;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, 
				IDialogConstants.CLOSE_LABEL, true);
//		createButton(parent, IDialogConstants.CANCEL_ID,
//				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(540, 600);
	}

	
	/**
	 * Content provider for table viewer 
	 * 
	 * @author Jakob Henriksson 
	 *
	 */
	private static class InnerContentProvider implements IStructuredContentProvider {

		Object[] EMPTY_ARRAY = new Object[0];
		
		private ServiceManager manager;
		private ServiceType serviceType;

		/**
		 * Constructor 
		 * 
		 * @param manager
		 * @param serviceType
		 */
		public InnerContentProvider(ServiceManager manager, ServiceType serviceType) {
			this.manager = manager;
			this.serviceType = serviceType;
		}

		@Override
		public Object[] getElements(Object element) {
			// first handle the specific case of a service root
			if (element instanceof ServiceRoot) {
				ServiceRoot root = (ServiceRoot) element;
				Collection<Service> result = new ArrayList<Service>();
				// get the existing services, so we can exclude them
				Collection<Service> existing = manager.getServices(serviceType);
				for (Service service : root.getServices(serviceType)) {
					// check if the service does not already exists
					if (!existing.contains(service))
						result.add(service);
				}
				return result.toArray();
			} 
			// then handle the more generic case of a @{link ServiceManager}
			// implementation 
			else if (element instanceof ServiceManager) {
				return ((ServiceManager) element).getServices(serviceType).toArray();
			}
			// most generic, an array 
			else if (element instanceof ArrayList) {
				return ((ArrayList<?>) element).toArray();
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
}
