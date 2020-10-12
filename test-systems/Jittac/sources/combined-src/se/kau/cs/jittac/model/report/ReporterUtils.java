package se.kau.cs.jittac.model.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.am.Connector;
import se.kau.cs.jittac.model.im.IXReference;

public class ReporterUtils {

	public static List<IXReference<?,?>> sortReferencesBySource(Collection<IXReference<?,?>> refs) {
		List<IXReference<?,?>> sortedRefs = new ArrayList<>(refs);
		Comparator<IXReference<?,?>> c = (IXReference<?,?> r1, IXReference<?,?> r2) -> r1.getSource().toString().compareTo(r2.getSource().toString());
		Collections.sort(sortedRefs, c);
		return sortedRefs;
	}
	
	
	public static Map<String, Set<IXReference<?,?>>> sortViolationsByResource(Collection<Connector> divergences) {
		Map<String, Set<IXReference<?,?>>> violationsPerResource = new HashedMap<>();
		for (Connector d : divergences) {
			for (IXReference<?,?> violation : d.getContributingReferences()) {
				String srcResName = violation.getSource().getResource().toString(); 
				if (violationsPerResource.containsKey(srcResName)) {
					violationsPerResource.get(srcResName).add(violation);
				}
				else {
					Set<IXReference<?,?>> newRefSet = new HashSet<>();
					newRefSet.add(violation);
					violationsPerResource.put(srcResName, newRefSet);
				}
			}
		}
		return violationsPerResource;
	}
	
	public static List<Component> getComponentsSortedByName(ArchitectureModel model) {
		List<Component> comps = model.getComponentsAsList();
		Collections.sort(comps, Comparator.comparing(Component::getName));
		return comps;
	}
}
