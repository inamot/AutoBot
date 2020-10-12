package se.kau.cs.jittac.eclipse.ui.views;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import se.kau.cs.jittac.eclipse.builders.jdt.JDTJavaReference;
import se.kau.cs.jittac.eclipse.ui.editors.ArchitectureModelEditor;
import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.am.Connector;
import se.kau.cs.jittac.model.feature.Feature;
import se.kau.cs.jittac.model.feature.FeatureLocationRegistry;
import se.kau.cs.jittac.model.im.IXReference;
import se.kau.cs.jittac.model.mapping.IJittacResource;

public class FeatureDependencyMatrixView extends ViewPart implements ISelectionListener {

	private TableViewer viewer;
	private String[] columnHeaders = {"Feature Dependency", "#Occurrences"};

	
	@Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
    }
	
	public void dispose() {
		 getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		 super.dispose();
	}
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) return;
        IStructuredSelection selected = (IStructuredSelection) selection;
        if (part instanceof ArchitectureModelEditor) {
        	if (selected.getFirstElement() instanceof Connector) {
        		if (!viewer.getControl().isDisposed()) {
        			viewer.setInput((Connector) selected.getFirstElement());
        		}
        	}
        }
	}

	@Override
	public void createPartControl(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
		for (int i = 0; i < columnHeaders.length; i++) {
			TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
			column.setText(columnHeaders[i]);
			column.pack();
			column.setWidth( 450 );
		}

		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new ReferenceLabelProvider());
		viewer.setComparator(new LocalViewerComparator());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	private class Entry
	{
		public Feature source, target;
		public int occurences;
		
		public Entry( Feature source, Feature target, int occurences )
		{
			this.source = source;
			this.target = target;
			this.occurences = occurences;
		}
	}
	
	private class ContentProvider implements IStructuredContentProvider
	{
		@Override
		public Object[] getElements( Object inputElement )
		{
			Map<String, Entry> resultmap = new HashMap<>();
			
			if( inputElement != null && !( inputElement instanceof Connector) )
				throw new IllegalArgumentException();
		
			if( inputElement != null )
			{
				Connector connector = ( Connector ) inputElement;
				
				for( IXReference<?, ?> ref : connector.getContributingReferences() )
				{
					IJittacResource srcRes = ref.getSource().getResource();
					IJittacResource trgRes = ref.getTarget().getResource();
					
					if( ref instanceof JDTJavaReference )
					{
						JDTJavaReference jref = ( JDTJavaReference ) ref;
						Feature sf = FeatureLocationRegistry.INSTANCE.getFeature( srcRes, jref.getInternalReference().offset );
						
						if( sf != null )
						{
							Set<Feature> trgFeatures = new HashSet<>();
							
							try 
							{
								switch( ref.getType() )
								{ 
//									case TYPEREF:
//									case CONTAINMENT: 	trgFeatures.addAll(FeatureLocationRegistry.INSTANCE.getFeatures(trgRes));
//												  	break;
									case ACCESS:
									case CALL:
									case CREATION:	
										int pos = ( ( IMember ) jref.getTarget().getJavaElement() ).getSourceRange().getOffset();
										Feature tf = FeatureLocationRegistry.INSTANCE.getFeature( trgRes, pos );
													
										if( tf != null )
										{
											trgFeatures.add( tf );
										}
													
										break;
									default: 		
										break;
								}
							}
							catch( JavaModelException e1 )
							{
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							for( Feature tf : trgFeatures )
							{
								if( resultmap.containsKey( sf.getName() + " -> " + tf.getName() ) )
								{
									Entry e = resultmap.get( sf.getName() + " -> " + tf.getName() );
									e.occurences++;
								}
								else
								{
									resultmap.put( sf.getName() + " -> " + tf.getName(), new Entry( sf, tf, 1 ) );
								}
							}
						}
					}
				}
				
				produceJsonData( resultmap );
				produceHeaderB( connector );
				
				return resultmap.values().toArray();
			}
			
			return new Object[ 0 ];
		}
	}
	
	private class ReferenceLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
		   	Entry entry = (Entry) element;
	    	
	    	switch (columnIndex) {
	    	case 0:
	    		return entry.source.getName() + " -> " + entry.target.getName();
	        case 1:
	            return Integer.toString(entry.occurences);
	        }
	        return null;
		}
	}
	
	private class LocalViewerComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			Entry e = (Entry) e1;
			Entry f = (Entry) e2;
			
			return (e.source.getName() + "->" + e.target.getName())
					.compareTo(f.source.getName() + "->" + f.target.getName());
		}
	}

	private static void produceJsonData( Map<String, Entry> featureMap )
	{	
		if( featureMap.isEmpty() )
		{
			try
			{
				URL dataFileUrl = new URL( "platform:/plugin/se.kau.cs.jittac.eclipse/resources/data.json" );
				File dataFile = new File( FileLocator.resolve( dataFileUrl ).toURI() );
				dataFile.delete();
				dataFile.createNewFile();
				BufferedWriter writer = new BufferedWriter( new FileWriter( dataFile, true ) );
				writer.write( "no feature dependencies" );
			    writer.close();
			}
			catch( IOException | URISyntaxException e )
			{ 	
				e.printStackTrace();
			}
		}
		else
		{
			String tab = "\t\t\t";
			String data = tab + "var json_data =\n";
			ArrayList<String> features = new ArrayList<String>();
			
			for( String featureDependency : featureMap.keySet() )
			{
				String sourceFeature = featureMap.get( featureDependency ).source.getName();
				String targetFeature = featureMap.get( featureDependency ).target.getName();
				
				if( !sourceFeature.equals( targetFeature ) )
				{
					if( !features.contains( sourceFeature ) )
					{
						features.add( sourceFeature );
					}
					if( !features.contains( targetFeature ) )
					{
						features.add( targetFeature );
					}
				}
			}
			
			data = data + tab + "{\n";
			data = data + tab + "\t\"nodes\" :\n";
			data = data + tab + "\t[\n";
			
			for( String feature : features )
			{
				data = data + tab + "\t\t{\n ";
				data = data + tab + "\t\t\t\"name\" : \"" + feature + "\",\n";
				data = data + tab + "\t\t\t\"id\" : \"" + feature + "\"\n";
				data = data + tab + "\t\t},\n ";
			}
			
			data = data.substring( 0, data.lastIndexOf( "," ) ) + "\n";	// removes last comma produced by loop
			data = data + tab + "\t],\n\n";
			data = data + tab + "\t\"links\" :\n";
			data = data + tab + "\t[\n";
			
			for( String featureDependency : featureMap.keySet() )
			{
				String sourceFeature = featureMap.get( featureDependency ).source.getName();
				String targetFeature = featureMap.get( featureDependency ).target.getName();
				int source = features.indexOf( sourceFeature );
				int target = features.indexOf( targetFeature );
				int numOfDependencies = featureMap.get( featureDependency ).occurences;
				
				if( !sourceFeature.equals( targetFeature ) )
				{
					data = data + tab + "\t\t{\n ";
					data = data + tab + "\t\t\t\"source\" : " + source + ",\n";
					data = data + tab + "\t\t\t\"value\" : " + numOfDependencies + ",\n";
					data = data + tab + "\t\t\t\"target\" : " + target + "\n" ;
					data = data + tab + "\t\t},\n ";
				}
			}
			
			data = data.substring( 0, data.lastIndexOf( "," ) ) + "\n";	// removes last comma produced by loop
			data = data + tab + "\t]\n";
			data = data + tab + "};\n\n";
			data = data + tab + "var colors =\n";
			data = data + tab + "{\n";
			
			String[] colors = 
			{
	            "#39add1", // light blue
	            "#3079ab", // dark blue
	            "#c25975", // mauve
	            "#e15258", // red
	            "#f9845b", // orange
	            "#838cc7", // lavender
	            "#7d669e", // purple
	            "#53bbb4", // aqua
	            "#51b46d", // green
	            "#e0ab18", // mustard
	            "#637a91", // dark gray
	            "#f092b0", // pink
	            "#b7c0c7"  // light gray
		    };
			
			Random randomGenerator = new Random();
			
			for( String feature : features )
			{
		        String randomColor = colors[ randomGenerator.nextInt( colors.length ) ];
				data = data + tab + "\t'" + feature + "' : '" + randomColor + "',\n";
			}
			
			data = data + tab + "\t'fallback' : '#9f9fa3'\n";
			data = data + tab + "};\n\n";
		
			try
			{
				URL dataFileUrl = new URL( "platform:/plugin/se.kau.cs.jittac.eclipse/resources/data.json" );
				File dataFile = new File( FileLocator.resolve( dataFileUrl ).toURI() );
				dataFile.delete();
				dataFile.createNewFile();
				BufferedWriter writer = new BufferedWriter( new FileWriter( dataFile, true ) );
				writer.write( data );
			    writer.close();
			}
			catch( IOException | URISyntaxException e )
			{ 	
				e.printStackTrace();
			}
		}
	}
	
	private static void produceHeaderB( Connector connector )
	{
		String sourceModule = connector.getSrc().getName();
		String targetModule = connector.getTrg().getName();
		String type = connector.getState().toString();
		String data = "\t\t<h5>Source/Target Modules : <span style=\"color: #3079ab\">" 
				+ sourceModule + "  <img src=\"right-arrow.png\">  " 
				+ targetModule +"</span> &emsp;&emsp;&emsp; Dependency Type : <span style=\"color: #3079ab\">" 
				+ type + "</span></h5>";
		
		try
		{
			URL dataFileUrl = new URL( "platform:/plugin/se.kau.cs.jittac.eclipse/resources/headerB.html" );
			File dataFile = new File( FileLocator.resolve( dataFileUrl ).toURI() );
			dataFile.delete();
			dataFile.createNewFile();
			BufferedWriter writer = new BufferedWriter( new FileWriter( dataFile, true ) );
			writer.write( data );
		    writer.close();
		}
		catch( IOException | URISyntaxException e )
		{ 	
			e.printStackTrace();
		}
	}

}
