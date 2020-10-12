package se.kau.cs.jittac.eclipse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.map.HashedMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import se.kau.cs.jittac.eclipse.codesupport.ProblemMarker;
import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.io.ArchitectureModelReader;
import se.kau.cs.jittac.model.im.IImplementationModel;
import se.kau.cs.jittac.model.im.ImplementationModelPartition;
import se.kau.cs.jittac.model.im.events.AbstractBuildEventListener;
import se.kau.cs.jittac.model.im.events.BuildEndEvent;
import se.kau.cs.jittac.model.im.io.ImplementationModelReader;
import se.kau.cs.jittac.model.im.io.ImplementationModelWriter;
import se.kau.cs.jittac.model.mapping.IJittacProject;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacProject;

public class ModelManager {
	
	private static ModelManager instance = new ModelManager();
	private static final String IMPLEMENTATION_FILE_NAME_PREFIX = "jittac-im-";
	private static final String IMPLEMENTATION_FILE_NAME_SUFFIX = ".sav";
	
	
	private BidiMap<IFile, ArchitectureModel> modelsByFile;
	private ModelManagerBuildEventListener buildEventListener;
	private Set<IImplementationModel> loadedImplementationModels;
	
	private class ModelManagerBuildEventListener extends AbstractBuildEventListener {
		
		@Override
		public void onBuildEnd(BuildEndEvent event) {
			Runnable  r = 
				() -> {
					try (OutputStream out = createOutputStream(event.getPartition())) {
						if (out != null) {
							ImplementationModelWriter.writePartition(out, event.getPartition());
							out.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				};
			new Thread(r).start();
		}
	}
	
	private ModelManager() {
		modelsByFile = new DualHashBidiMap<>();
		buildEventListener = new ModelManagerBuildEventListener();
		loadedImplementationModels = new HashSet<>();
	}

	public static ModelManager instance() {
		return instance;
	}

	public ArchitectureModel getArchitectureModel(IFile file) {
		ArchitectureModel model = null;
		if (!file.exists()) {
			modelsByFile.remove(file);
		}
		else {
			model = modelsByFile.get(file);
			if (model == null) {
				try {
					model = ArchitectureModelReader.read(file.getContents());
					model.registerListener(ProblemMarker.getInstance());
					if (model != null) {
						modelsByFile.put(file, model);
						for (IJittacProject proj : model.getManagedProjects()) {
							IImplementationModel im = proj.getImplementationModel();
							im.registerBuildEventListener(buildEventListener);
							if (!loadedImplementationModels.contains(im)) {
								locateAndReadPartitionFiles(im);
								loadedImplementationModels.add(im);
							}
						}
					}
				} catch (CoreException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					
				}
			}
		}
		return model;
	}
	
	public IFile getFile(ArchitectureModel model) {
		return modelsByFile.getKey(model);
	}

	private void locateAndReadPartitionFiles(IImplementationModel im) 
	throws FileNotFoundException {
		File[] partFiles = this.findPartitionFiles(im);
		for (File f : partFiles) {
			InputStream in = new FileInputStream(f);
			ImplementationModelReader.read(in, im);
		}
	}
	
	private File[] findPartitionFiles(IImplementationModel im) {
		IJittacProject jProject = im.getProject();
		if (jProject instanceof EclipseJittacProject) {
			IProject project = ((EclipseJittacProject) jProject).
					getWrappedProject();
			IPath path = project.getWorkingLocation(Activator.PLUGIN_ID);
			File dir = path.toFile();
			if (!(dir.isDirectory() && dir.canRead())) return null;
			File[] matchingFiles =
					dir.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.startsWith(IMPLEMENTATION_FILE_NAME_PREFIX) &&
									name.endsWith(IMPLEMENTATION_FILE_NAME_SUFFIX);
						}
					});
			return matchingFiles;
		}
		return null;
	}
	
	private OutputStream createOutputStream(ImplementationModelPartition part) {
		OutputStream out = null;
		IPath path = getPathToPartition(part);
        File file = path.toFile();
        if (file.exists()) {
        	file.delete();
        }
		try {
			file.createNewFile();
			if (file.canWrite()) {
				out = new FileOutputStream(file);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			//TODO
		}
		return out;
	}

	
	
	private IPath getPathToPartition(ImplementationModelPartition part) {
		IJittacProject jProject = part.getImplementationModel().getProject();
		if (jProject instanceof EclipseJittacProject) {
			IProject project = ((EclipseJittacProject) jProject).
					getWrappedProject();
	        IPath path = project.getWorkingLocation(Activator.PLUGIN_ID);
	        path = path.append("/" + 
	        		IMPLEMENTATION_FILE_NAME_PREFIX +
	        		part.getBuilderType().getName() +
	        		IMPLEMENTATION_FILE_NAME_SUFFIX);
	        return path;
		}
		return null;
	}
}
