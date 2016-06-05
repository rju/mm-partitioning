package partitioning

import java.util.ArrayList
import java.util.Collection
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.EClassifier

class Partitioner {
	
	val Resource resource
	val Collection<EClass> classes
	Iterable<EClass> rootClasses
	
	
	new (Resource resource) {
		this.resource = resource
		this.classes = new ArrayList<EClass>
	}
	
	def void collectClasses() {
		this.resource.contents.forEach[it.collectClasses]
	}
	
	def int countNormalClasses() {
		this.classes.filter[!it.interface].size
	}
	
	def Collection<EClass> getClasses() {
		return this.classes
	}
	
	def private void collectClasses(EObject object) {
		switch(object) {
			EPackage: object.eContents.forEach[it.collectClasses]
			EClass: classes.add(object)
		}
	}

	def findRootClasses() {
		this.rootClasses = this.classes.filter[!it.isExternalContained && !it.abstract && !it.interface] 
	}
	
	def private boolean isExternalContained(EClass eClass) {
		this.classes.exists[it.EReferences.exists[it.containment && 
			(eClass.EAllSuperTypes.contains(it.EType) ||
				eClass == it.EType
			) && it != eClass
		]]
	}
	
	def Collection<Collection<EClassifier>> createPartitions() {
		val result = new ArrayList<Collection<EClassifier>>()
		
		this.rootClasses.forEach[
			val partition = new ArrayList<EClassifier>()
			partition.add(it)
			it.EReferences.filter[it.containment].forEach[
				partition.addTransitive(it.EType)
			]
			result.add(partition)
		]
		
		return result
	}
	
	def private void addTransitive(ArrayList<EClassifier> classifiers, EClassifier classifier) {
		if (!classifiers.contains(classifier)) {
			classifiers.add(classifier)
			classifiers.addSubtype(classifier)
			if (classifier instanceof EClass)
				classifier.EReferences.filter[it.containment].forEach[
					classifiers.addTransitive(it.EType)
				]
		}
	}
	
	def private addSubtype(ArrayList<EClassifier> classifiers, EClassifier classifier) {
		classes.filter[it.EAllSuperTypes.contains(classifier)].forEach[
			classifiers.addTransitive(it)
		]
	}
	
	def Iterable<EClass> missingClasses(Collection<Collection<EClassifier>> partitions) {
		return classes.filter[clazz | !partitions.exists[it.exists[it == clazz]]]	
	}
			
}