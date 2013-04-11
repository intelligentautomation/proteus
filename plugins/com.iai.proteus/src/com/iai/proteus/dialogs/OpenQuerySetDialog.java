/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.dialogs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.iai.proteus.model.services.Service;
import com.iai.proteus.queryset.QuerySetManager;
import com.iai.proteus.queryset.persist.v1.QuerySet;
import com.iai.proteus.queryset.persist.v1.QuerySetPersist;
import com.iai.proteus.ui.SwtUtil;
import com.iai.proteus.ui.UIUtil;

/**
 * Dialog for managing services 
 * 
 * @author Jakob Henriksson 
 *
 */
public class OpenQuerySetDialog extends TitleAreaDialog {
	
	private Collection<QuerySet> querySets;

	private TableViewer tableViewer;
	private Table table;
	
	private Image imgDelete;
	
	private QuerySet selectedQuerySet;

	/**
	 * Constructor
	 * 
	 * @param parentShell
	 * @param manager
	 */
	public OpenQuerySetDialog(Shell parentShell) {
		super(parentShell);
		
		// load stored query sets 
		File folder = QuerySetPersist.getStorageLocation();
		
		querySets = new ArrayList<QuerySet>();

		// find all persisted query sets 
		File[] files = 
				folder.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File file, String name) {
						if (name.endsWith("." + QuerySetPersist.querySetExtension))
							return true;
						return false;
					}
				});

		// read the query sets 
		if (files != null) {
			for (File file : files) {
				QuerySet querySet = QuerySetPersist.read(file);
				if (querySet != null) {
					// exclude already opened query sets 
					if (QuerySetManager.getInstance().isOpen(querySet.getUuid()))
						continue;
					// set the file that this query set came from 
					querySet.setFile(file);
					// add the query set 
					querySets.add(querySet);
				}
			}
		}
		
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
				if (imgDelete != null)
					imgDelete.dispose();
			}
		});
		
		imgDelete = UIUtil.getImage("icons/fugue/database--minus.png");
		
		setTitle("Open Query Set");
		setMessage("Open a saved query set");
		Composite area = (Composite) super.createDialogArea(parent);

		ToolBar toolBar = new ToolBar(area, SWT.FLAT | SWT.RIGHT);
		
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
				Object selection = 
						SwtUtil.getFirstSelectedElement(tableViewer.getSelection());
				
				MessageDialog dialog = 
						UIUtil.getConfirmDialog(getShell(), "Delete query set", 
								"Are you sure you want to delete the " +
										"query set?");
				
				if (dialog.open() == MessageDialog.OK) {

					// make sure we are dealing with the right objects
					if (selection instanceof QuerySet) {
						QuerySet qs = (QuerySet) selection;
						// update model 
						querySets.remove(qs);
						// update manager
						QuerySetManager.getInstance().removeStored(qs.getUuid());
						// delete the file 
						FileUtils.deleteQuietly(qs.getFile());
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
		final ColumnSorterName colSorterName = new ColumnSorterName();
		final ColumnSorterUrl colSorterTimestamp = new ColumnSorterUrl();
		
		TableViewerColumn colName = new TableViewerColumn(tableViewer, SWT.NONE);
		colName.getColumn().setText("Name");
		colName.getColumn().setWidth(400);
		colName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof QuerySet) {
					return ((QuerySet) element).getTitle();
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

		TableViewerColumn colTimestamp = new TableViewerColumn(tableViewer, SWT.NONE);
		colTimestamp.getColumn().setText("Saved");
		colTimestamp.getColumn().setWidth(120);
		colTimestamp.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof QuerySet) {
					return QuerySetPersist.format.format(
							((QuerySet) element).getDateCreated());
				}
				return "";
			}
		});
		// column sorting 
		colTimestamp.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// make sure we have the right sorter
				tableViewer.setSorter(colSorterTimestamp);
				// toggle the sort direction 
				colSorterTimestamp.changeSortDirection();
				// update the viewers whose input may have changed
				tableViewer.refresh();
			}
		});			
		
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setInput(querySets);
		
		table.setFocus();
		
		// add listener to set the correct state of the 'delete' toolbar item
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// update tool item status 
				tltmDelete.setEnabled(!tableViewer.getSelection().isEmpty());

				// update button 
				Button btn = getButton(IDialogConstants.OK_ID);
				btn.setEnabled(!tableViewer.getSelection().isEmpty());
				
				// remember selection (set to null if empty selection) 
				selectedQuerySet = 
						(QuerySet) SwtUtil.getFirstSelectedElement(event.getSelection());
			}
		});
		
		// listen for double clicks 
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (!event.getSelection().isEmpty()) {
					// issue "OK pressed" signal 
					OpenQuerySetDialog.super.okPressed();
				}
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
		// set disabled by default 
		createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OPEN_LABEL, true).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
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
	 * Returns the selected query set 
	 * 
	 * @return
	 */
	public QuerySet getSelectedQuerySet() {
		return selectedQuerySet;
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
			String s1 = item1.getEndpoint();
			String s2 = item2.getEndpoint();
			return sort(s1, s2);
		}		
	}		
}
