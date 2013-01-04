package com.iai.proteus.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.iai.proteus.Activator;
import com.iai.proteus.map.wms.WMSUtil;
import com.iai.proteus.model.Model;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceType;
import com.iai.proteus.model.services.WmsMapLayer;
import com.iai.proteus.model.workspace.Project;
import com.iai.proteus.model.workspace.Provenance;
import com.iai.proteus.model.workspace.QueryLayer;
import com.iai.proteus.model.workspace.QueryWmsMap;
import com.iai.proteus.ui.UIUtil;

public class WmsMapLayerViewer extends ViewPart
	implements ISelectionListener, ICheckStateListener
{

	public static final String ID = "com.iai.smt.discovery.views.WmsMapLayerViewer"; //$NON-NLS-1$

	private CheckboxTableViewer tableViewer;

	public WmsMapLayerViewer() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		TableColumnLayout layout = new TableColumnLayout();
		container.setLayout(layout);
		{

			tableViewer =
					CheckboxTableViewer.newCheckList(container, SWT.BORDER);

			Table table = tableViewer.getTable();
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			table.setHeaderVisible(true);
			table.setLinesVisible(false);

			TableViewerColumn colMapName =
					new TableViewerColumn(tableViewer, SWT.NONE);
			layout.setColumnData(colMapName.getColumn(),
					new ColumnWeightData(4, ColumnWeightData.MINIMUM_WIDTH,
							true));
			colMapName.getColumn().setText("Map name");

			// label provider for column
			colMapName.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					WmsMapLayer map = (WmsMapLayer) element;
					return map.getName();
				}

				@Override
				public Image getImage(Object element) {
					return UIUtil.getImage(Activator.PLUGIN_ID,
							"icons/fugue/map.png");
				}
			});

			tableViewer.setContentProvider(ArrayContentProvider.getInstance());
			tableViewer.setUseHashlookup(true);
			// starts without any input
			tableViewer.setInput(new Object[0]);

			// listener
			tableViewer.addCheckStateListener(this);
		}

		// register as listener
		getSite().getPage().addSelectionListener((ISelectionListener) this);

		createActions();
		initializeToolBar();
		initializeMenu();
		initializeContextMenus();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
//		IToolBarManager toolbarManager = getViewSite().getActionBars()
//				.getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
//		IMenuManager menuManager = getViewSite().getActionBars()
//				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	/**
	 * Handle changes in selection
	 *
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

		/*
		 * Before we lose selection, delete all layers (should have been
		 * saved to workspace if they should be kept)
		 */
		Object input = tableViewer.getInput();
		if (input instanceof WmsMapLayer[]) {
			for (WmsMapLayer mapLayer : (WmsMapLayer[]) input) {
				// delete active layers
				if (mapLayer.isActive())
					mapLayer.fireDeleteLayer();
			}
		}

		if (!selection.isEmpty()) {
			if (selection instanceof StructuredSelection) {
				StructuredSelection treeSelection =
						(StructuredSelection) selection;
				Object element = treeSelection.getFirstElement();
				if (element instanceof Service) {

					final Service service = (Service) element;

					// we only handle WMSs
					if (service.getServiceType().equals(ServiceType.WMS)) {

						// create job to load layers
						Job job = new Job("Loading layers from WMS") {
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								try {
									monitor.beginTask("Loading layers from WMS",
											IProgressMonitor.UNKNOWN);

									// update input
									final Object[] layers =
											WMSUtil.loadLayersFromWMS(service);

									UIUtil.update(new Runnable() {
										@Override
										public void run() {
											tableViewer.setInput(layers);
										}
									});

									monitor.worked(1);

									return Status.OK_STATUS;

								} finally {
									monitor.done();
								}
							}
						};
//						job.setUser(true);
						job.schedule();

						return;
					}
				}
			}

		}

		// default: clear table
		tableViewer.getTable().removeAll();
	}

	/**
	 * Handles selection in the view
	 *
	 */
	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {

		Object elmt = event.getElement();
		if (elmt instanceof WmsMapLayer) {
			WmsMapLayer mapLayer = (WmsMapLayer) elmt;

			if (event.getChecked()) {
				mapLayer.activate();
			} else {
				mapLayer.deactivate();
			}

			/*
			 * Notify listeners of change
			 */
			mapLayer.fireToggleLayer();

			System.out.println("Activate layer: " + mapLayer.getName());
		}
	}

	/**
	 * Initialize context menus
	 *
	 */
	private void initializeContextMenus() {

		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);

		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				ISelection selection = tableViewer.getSelection();
				if (selection.isEmpty())
					return;
				if (selection instanceof StructuredSelection) {
					final Object selected =
							((StructuredSelection) selection).getFirstElement();

					/*
					 * Map layer
					 */
					if (selected instanceof WmsMapLayer) {
						WmsMapLayer mapLayer = (WmsMapLayer) selected;
						IAction actionAddMapToLayer =
								createAddMapToLayerAction(mapLayer);
						IAction actionAddMapAsLayer =
								createAddMapAsNewLayerAction(mapLayer);
						manager.add(new Separator("dynamic"));
						manager.add(actionAddMapToLayer);
						manager.add(actionAddMapAsLayer);
					}
				}
			}
		});

		Table tree = tableViewer.getTable();
		Menu menu = menuMgr.createContextMenu(tree);
		tree.setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}

	/**
	 * Returns an action to save a WMS map to an existing layer in the
	 * user's workspace
	 *
	 * @param service
	 * @return
	 */
	private IAction createAddMapToLayerAction(final WmsMapLayer mapLayer) {
		IAction action = new Action("Add map to workspace layer...",
				UIUtil.getImageDescriptor(Activator.PLUGIN_ID,
						"icons/fugue/disk-black.png")) {
			public void run() {

//				Shell shell =
//						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//				AddToWorkspaceDialog dialog =
//						new AddToWorkspaceDialog(getViewSite().getShell(),
//								new DialogLabelProvider(),
//								new ContentProvider(Level.LAYER));
//
//				dialog.setTitle("Where should the map go?");
//				dialog.setMessage("Please select a project and layer");
//
//				String msg =
//						"You must first create a project " +
//								"and a layer to add to an existing project";
//				dialog.setEmptyListMessage(msg);
//
//				dialog.setValidator(new ISelectionStatusValidator() {
//					@Override
//					public IStatus validate(Object[] selection) {
//						if (selection.length == 1) {
//							if (selection[0] instanceof QueryLayer) {
//								return new Status(Status.OK,
//										Activator.PLUGIN_ID, "");
//							}
//						}
//						return new Status(Status.ERROR, Activator.PLUGIN_ID,
//								"A layer must be specified");
//					}
//				});
//
//				dialog.setInput(WorkspaceRoot.getInstance());
//				dialog.open();
//
//				if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
//					Object result = dialog.getFirstResult();
//					if (result != null) {
//						// the validator code should make sure that this is
//						// the case
//						QueryLayer queryLayer = (QueryLayer)result;
//						// add to project
//						addMapLayerToQueryLayer(queryLayer, mapLayer);
//						// notify listeners of the update
//						queryLayer.fireUpdated();
//					}
//				}

			}
		};
		return action;
	}

	/**
	 * Returns an action to save a WMS map as a new layer in the user's
	 * workspace
	 *
	 * @param service
	 * @return
	 */
	private IAction createAddMapAsNewLayerAction(final WmsMapLayer mapLayer) {
		IAction action = new Action("Save map as new layer...",
				UIUtil.getImageDescriptor(Activator.PLUGIN_ID,
						"icons/fugue/disk-black.png")) {
			public void run() {

				Shell shell =
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//				AddToWorkspaceDialog dialog =
//						new AddToWorkspaceDialog(shell,
//								new DialogLabelProvider(),
//								new ContentProvider(Level.PROJECT));
//
//				dialog.setTitle("Where should the map go?");
//				dialog.setMessage("Please select a project");
//
//				String msg =
//						"You must first create a project";
//				dialog.setEmptyListMessage(msg);
//
//				dialog.setValidator(new ISelectionStatusValidator() {
//					@Override
//					public IStatus validate(Object[] selection) {
//						if (selection.length == 1) {
//							if (selection[0] instanceof Project) {
//								return new Status(Status.OK,
//										Activator.PLUGIN_ID, "");
//							}
//						}
//						return new Status(Status.ERROR, Activator.PLUGIN_ID,
//								"A project must be specified");
//					}
//				});
//
//				dialog.setInput(WorkspaceRoot.getInstance());
//				dialog.open();
//
//				if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
//					Object result = dialog.getFirstResult();
//					if (result != null) {
//						// the validator code should make sure that this is
//						// the case
//						Project project = (Project)result;
//						// add to project
//						addMapLayerToProject(project, mapLayer);
//						// notify listeners of the update
//						project.fireUpdated();
//					}
//				}

			}
		};
		return action;
	}

	/**
	 * Adds the given map layer to a project
	 *
	 * @param project
	 * @param mapLayer
	 */
	private void addMapLayerToProject(Project project, WmsMapLayer mapLayer) {

		// we first need to get the service the layer belongs to
		Model parent = mapLayer.getParent();
		if (parent instanceof Service) {
			Service service = (Service) parent;

			/*
			 * Query
			 */
			QueryWmsMap query = new QueryWmsMap();
			query.setName(mapLayer.getName());
			// set the map layer name
			query.setWmsLayerName(mapLayer.getWmsLayerName());
			// set the provenance information for the query
			query.setProvenance(new Provenance((Service)service.clone()));

			/*
			 * Create the query layer and add the query
			 */
			QueryLayer queryLayer = new QueryLayer();
			queryLayer.setName(mapLayer.getName());
			queryLayer.addQuery(query);

			/*
			 * Add the query layer to the project
			 */
			project.addLayer(queryLayer);
		}
	}

	/**
	 * Adds the given map layer to a query layer
	 *
	 * @param queryLayer
	 * @param mapLayer
	 */
	private void addMapLayerToQueryLayer(QueryLayer queryLayer,
			WmsMapLayer mapLayer)
	{

		// we first need to get the service the layer belongs to
		Model parent = mapLayer.getParent();
		if (parent instanceof Service) {
			Service service = (Service) parent;

			/*
			 * Query
			 */
			QueryWmsMap query = new QueryWmsMap();
			query.setName(mapLayer.getName());
			// set the map layer name
			query.setWmsLayerName(mapLayer.getWmsLayerName());
			// set the provenance information for the query
			query.setProvenance(new Provenance((Service)service.clone()));

			/*
			 * Add the query to the query layer
			 */
			queryLayer.addQuery(query);
		}
	}

}
