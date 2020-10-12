package se.kau.cs.jittac.model.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.am.Connector;
import se.kau.cs.jittac.model.im.IXReference;
import se.kau.cs.jittac.model.im.IXReferenceType;

public class PlainTextViolationReporter {

	public static void write(OutputStream stream, ArchitectureModel model){
        
		try {
			OutputStreamWriter writer = new OutputStreamWriter(stream);

			writeArchitectureOverview(writer, model);
			
			//writeMapping(writer, model)
			
			for (Component c : ReporterUtils.getComponentsSortedByName(model)) {
				writeViolationReport(writer, c, new IXReferenceType[] {IXReferenceType.ACCESS, IXReferenceType.CALL});
			}

			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	private static void writeArchitectureOverview(Writer writer, ArchitectureModel model) {
		
		try {
			writer.write("Architecture Overview\n");
			List<Component> comps = ReporterUtils.getComponentsSortedByName(model);
			for (Component c : comps) {
				StringBuffer sb = new StringBuffer();
				Collection<Connector> envisagedCons = CollectionUtils.select(c.getOutgoingConnectors(), con -> con.isEnvisaged() && !con.isReflexive());
				Collection<Component> targetedComps = CollectionUtils.collect(envisagedCons, Connector::getTrg);
				sb.append("\t" + c.getName() + (envisagedCons.size() > 0 ? " -> " : " "));
				targetedComps.forEach(comp -> sb.append(comp.getName() + ","));
				sb.deleteCharAt(sb.length() - 1);
				sb.append("\n");
				writer.write(sb.toString());
			}
			writer.write("\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void writeViolationReport(Writer writer, Component c, IXReferenceType[] types) throws IOException {
		Collection<Connector> divergences = CollectionUtils.select(c.getOutgoingConnectors(), con -> !con.isEnvisaged());
		Map<String, Set<IXReference<?,?>>> violationsPerResource;
		violationsPerResource = ReporterUtils.sortViolationsByResource(divergences);
					
		writer.write("Component " + c.getName() + "\n");
		for (String s : violationsPerResource.keySet()) {
			Collection<IXReference<?,?>> filteredViolations = 
					CollectionUtils.select(violationsPerResource.get(s), ref -> Arrays.asList(types).contains(ref.getType()));
			if (filteredViolations.size() > 0) {
				writer.write("\tFollowing violations starting in " + s + ":\n");

				for (IXReference<?,?> ref : ReporterUtils.sortReferencesBySource(filteredViolations)) {
					StringBuffer sb = new StringBuffer();
					sb.append("\t\t");
					sb.append(ref.getSource().toString());
					sb.append(" --" + ref.getType() + "-> " );
					sb.append(ref.getTarget().toString());
					sb.append("(" + ref.getTarget().getResource().toString() + ", component ");
					sb.append(c.getModel().getMapping().getComponent(ref.getTarget().getResource()).getName());
					sb.append(")\n");
					
					writer.write(sb.toString());
				}
				writer.write("\n");
			}
		}
	}
}
