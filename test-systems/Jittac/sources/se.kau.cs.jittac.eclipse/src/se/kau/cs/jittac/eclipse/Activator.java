package se.kau.cs.jittac.eclipse;

import static org.eclipse.jdt.core.JavaCore.isJavaLikeFileName;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.common.base.Predicate;

import se.kau.cs.jittac.eclipse.builders.jdt.JDTImplementationModelFactory;
import se.kau.cs.jittac.model.im.ImplementationModelFactoryRegistry;
import se.kau.cs.jittac.model.mapping.JittacResourceModelRegistry;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacResourceFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "se.kau.cs.jittac.eclipse"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		JittacResourceModelRegistry.INSTANCE.registerResourceModel(EclipseJittacResourceFactory.INSTANCE);
		JittacResourceModelRegistry.INSTANCE.setDefaultResourceModel(EclipseJittacResourceFactory.INSTANCE);
		
		ImplementationModelFactoryRegistry.instance().register(JDTImplementationModelFactory.INSTANCE);
		URL url = getBundle().getEntry("temp");
		//System.out.println("Activator URL: " + url);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

    /**
     * Return a predicate which returns true when the given resource is supported by this plug-in,
     * that is when the resource represents a java compilation unit.
     * 
     * @return the predicate
     */
    public static Predicate<IResource> supportedResource() {
        return new Predicate<IResource>() {
            @Override
            public boolean apply(IResource resource) {
                return isJavaLikeFileName(resource.getName());
            }
        };
    }
}
