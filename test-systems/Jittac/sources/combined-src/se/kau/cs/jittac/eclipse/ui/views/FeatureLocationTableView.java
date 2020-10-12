package se.kau.cs.jittac.eclipse.ui.views;


import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import se.kau.cs.jittac.model.feature.FeatureLocation;
import se.kau.cs.jittac.model.feature.FeatureLocationRegistry;

public class FeatureLocationTableView extends ViewPart
{
    private static TableViewer viewer;
    
    public void createPartControl( Composite parent )
    {
        //Table Search
    	//GridLayout layout = new GridLayout( 2, false );
        //parent.setLayout( layout );
        //Label searchLabel = new Label( parent, SWT.NONE );
        //searchLabel.setText( "Search: " );
        //final Text searchText = new Text( parent, SWT.BORDER | SWT.SEARCH );
        //searchText.setLayoutData( new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL ) );
        createViewer( parent );
    }

    private void createViewer( Composite parent )
    {
        viewer = new TableViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER );
        createColumns( parent, viewer );
        
        final Table table = viewer.getTable();
        table.setHeaderVisible( true );
        table.setLinesVisible( true );
        
        viewer.setContentProvider( new ArrayContentProvider() );
        viewer.setInput( FeatureLocationRegistry.INSTANCE.getAllFeatureLocations() );
        getSite().setSelectionProvider( viewer );

        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        viewer.getControl().setLayoutData( gridData );
    }

    public TableViewer getViewer()
    {
        return viewer;
    }

    private void createColumns( final Composite parent, final TableViewer viewer )
    {
        TableViewerColumn col = createTableViewerColumn( "Source", 450, 0 );
        col.setLabelProvider( new ColumnLabelProvider()
        {
            @Override
            public String getText( Object element )
            {
            	FeatureLocation fl = ( FeatureLocation ) element;
                return fl.getResource().getPersistentHandle();   
            }
        } );

        col = createTableViewerColumn( "Feature", 200, 1 );
        col.setLabelProvider( new ColumnLabelProvider()
        {
        	@Override
            public String getText( Object element )
            {
            	FeatureLocation fl = ( FeatureLocation ) element;
                return fl.getFeature().getName();
            }
        } );

        col = createTableViewerColumn( "Start-Offset", 100, 2 );
        col.setLabelProvider( new ColumnLabelProvider()
        {
        	@Override
            public String getText( Object element )
            {
            	FeatureLocation fl = ( FeatureLocation ) element;
                return Integer.toString( fl.getOffset() );
            }
        } );

        col = createTableViewerColumn( "Length", 75, 3 );
        col.setLabelProvider( new ColumnLabelProvider()
        {
        	@Override
            public String getText( Object element )
            {
            	FeatureLocation fl = ( FeatureLocation ) element;
                return Integer.toString( fl.getLength() );
            }
        } );
    }

    private TableViewerColumn createTableViewerColumn( String title, int bound, final int colNumber )
    {
        final TableViewerColumn viewerColumn = new TableViewerColumn( viewer, SWT.NONE );
        final TableColumn column = viewerColumn.getColumn();
        column.setText( title );
        column.setWidth( bound );
        column.setResizable( true );
        column.setMoveable( true );
        return viewerColumn;
    }

    public void setFocus()
    {
        viewer.getControl().setFocus();
    }

    public static void refresh()
    {
    	viewer.setInput( FeatureLocationRegistry.INSTANCE.getAllFeatureLocations() );
    	viewer.refresh();
    }

}