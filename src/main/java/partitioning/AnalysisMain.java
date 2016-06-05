package partitioning;

import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import partitioning.Partitioner;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

public class AnalysisMain {

	public static void main(String[] args) throws IOException {
		/** load ecore model */
		Resource resource = readModel(args[0]);
		/** run analysis */
		executeAnalysis(resource);
	}

	
	private static void executeAnalysis(Resource resource) {
		Partitioner partitioner = new Partitioner(resource);
		
		partitioner.collectClasses();
		int i = 0;
		for (EClass clazz : partitioner.findRootClasses()) {
			System.out.println("root " + i + " " + clazz);
			i++;
		}
		
		System.out.println("total " + i + 
				" " + partitioner.getClasses().size() + 
				" " + partitioner.countNormalClasses());
		
		Collection<Collection<EClassifier>> partitions = partitioner.createPartitions();
		int j = 0;
		for (Collection<EClassifier> partition : partitions) {
			System.out.println("P " + partitionName(partition.iterator()) + 
					" " + partition.size());
			j = j + partition.size();
		}
		
		System.out.println("collected " + j);
		
		Iterable<EClass> missing = partitioner.missingClasses(partitions);
		int k = 0;
		for (EClass clazz : missing) {
			System.out.println("M " + clazz.getName() + " " + (clazz.isInterface()?"interface":"class"));
			k++;
		}
		
		System.out.println("missing " + k);
	}

	private static String partitionName(Iterator<EClassifier> partition) {
		String result = "R " + partition.next().getName() + " [";
		
		while (partition.hasNext()) {
			result = result + partition.next().getName() + " ";
		}
		return result + "]";
		
	}

	private static Resource readModel(String uri) throws IOException {
		XMIResource resource = new XMIResourceImpl(URI.createURI(uri));
		resource.load(null);
		return resource;
	}
}
