package se.kau.cs.jittac.eclipse.ui.policies;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.gef.mvc.fx.handlers.AbstractHandler;
import org.eclipse.gef.mvc.fx.handlers.IOnClickHandler;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import se.kau.cs.jittac.eclipse.ModelManager;
import se.kau.cs.jittac.eclipse.ui.parts.ArchitectureModelPart;
import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.report.PlainTextViolationReporter;

public class ShowArchitectureModelContextMenuOnClickHandler extends AbstractHandler implements IOnClickHandler {

	private ContextMenu openContextMenu;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	
	@Override
	public void click(MouseEvent event) {
		if (openContextMenu != null) openContextMenu.hide();
		MenuItem reportItem = new MenuItem("Generate Report");
		ContextMenu contextMenu = new ContextMenu(reportItem);
		openContextMenu = contextMenu;
		
		if (!event.isSecondaryButtonDown()) {
			return; // only listen to secondary buttons
		}
		
		reportItem.setOnAction((e) -> {
			if (getHost() == getHost().getRoot()) {
				ArchitectureModel model = ((ArchitectureModelPart) getHost().getChildrenUnmodifiable().get(0)).getContent();
				IFile modelFile = ModelManager.instance().getFile(model); 
				IPath modelDir = modelFile.getLocation();
				modelDir = modelDir.uptoSegment(modelDir.segmentCount() - 1);
				String reportFileName = modelFile.getName() + "-" + sdf.format(new Date(System.currentTimeMillis())) + ".txt";
				IPath reportPath = modelDir.append(reportFileName);
		        File file = reportPath.toFile();
		        if (file.exists()) {
		        	file.delete();
		        }
				try {
					file.createNewFile();
					if (file.canWrite()) {
						OutputStream out = new FileOutputStream(file);
						PlainTextViolationReporter.write(out, model);
						out.flush();
						out.close();
					}
				} catch (FileNotFoundException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				} catch (IOException ex) {
				}
			}
		});
	
		contextMenu.show((Node) event.getTarget(), event.getScreenX(), event.getScreenY());
	}

	
}
