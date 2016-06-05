package partitioning;

import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@SuppressWarnings("all")
public class Partitioner {
  private final Resource resource;
  
  private final Collection<EClass> classes;
  
  private Iterable<EClass> rootClasses;
  
  public Partitioner(final Resource resource) {
    this.resource = resource;
    ArrayList<EClass> _arrayList = new ArrayList<EClass>();
    this.classes = _arrayList;
  }
  
  public void collectClasses() {
    EList<EObject> _contents = this.resource.getContents();
    final Consumer<EObject> _function = new Consumer<EObject>() {
      public void accept(final EObject it) {
        Partitioner.this.collectClasses(it);
      }
    };
    _contents.forEach(_function);
  }
  
  public int countNormalClasses() {
    final Function1<EClass, Boolean> _function = new Function1<EClass, Boolean>() {
      public Boolean apply(final EClass it) {
        boolean _isInterface = it.isInterface();
        return Boolean.valueOf((!_isInterface));
      }
    };
    Iterable<EClass> _filter = IterableExtensions.<EClass>filter(this.classes, _function);
    return IterableExtensions.size(_filter);
  }
  
  public Collection<EClass> getClasses() {
    return this.classes;
  }
  
  private void collectClasses(final EObject object) {
    boolean _matched = false;
    if (!_matched) {
      if (object instanceof EPackage) {
        _matched=true;
        EList<EObject> _eContents = ((EPackage)object).eContents();
        final Consumer<EObject> _function = new Consumer<EObject>() {
          public void accept(final EObject it) {
            Partitioner.this.collectClasses(it);
          }
        };
        _eContents.forEach(_function);
      }
    }
    if (!_matched) {
      if (object instanceof EClass) {
        _matched=true;
        this.classes.add(((EClass)object));
      }
    }
  }
  
  public Iterable<EClass> findRootClasses() {
    final Function1<EClass, Boolean> _function = new Function1<EClass, Boolean>() {
      public Boolean apply(final EClass it) {
        boolean _and = false;
        boolean _and_1 = false;
        boolean _isExternalContained = Partitioner.this.isExternalContained(it);
        boolean _not = (!_isExternalContained);
        if (!_not) {
          _and_1 = false;
        } else {
          boolean _isAbstract = it.isAbstract();
          boolean _not_1 = (!_isAbstract);
          _and_1 = _not_1;
        }
        if (!_and_1) {
          _and = false;
        } else {
          boolean _isInterface = it.isInterface();
          boolean _not_2 = (!_isInterface);
          _and = _not_2;
        }
        return Boolean.valueOf(_and);
      }
    };
    Iterable<EClass> _filter = IterableExtensions.<EClass>filter(this.classes, _function);
    return this.rootClasses = _filter;
  }
  
  private boolean isExternalContained(final EClass eClass) {
    final Function1<EClass, Boolean> _function = new Function1<EClass, Boolean>() {
      public Boolean apply(final EClass it) {
        EList<EReference> _eReferences = it.getEReferences();
        final Function1<EReference, Boolean> _function = new Function1<EReference, Boolean>() {
          public Boolean apply(final EReference it) {
            boolean _and = false;
            boolean _and_1 = false;
            boolean _isContainment = it.isContainment();
            if (!_isContainment) {
              _and_1 = false;
            } else {
              boolean _or = false;
              EList<EClass> _eAllSuperTypes = eClass.getEAllSuperTypes();
              EClassifier _eType = it.getEType();
              boolean _contains = _eAllSuperTypes.contains(_eType);
              if (_contains) {
                _or = true;
              } else {
                EClassifier _eType_1 = it.getEType();
                boolean _equals = Objects.equal(eClass, _eType_1);
                _or = _equals;
              }
              _and_1 = _or;
            }
            if (!_and_1) {
              _and = false;
            } else {
              boolean _notEquals = (!Objects.equal(it, eClass));
              _and = _notEquals;
            }
            return Boolean.valueOf(_and);
          }
        };
        return Boolean.valueOf(IterableExtensions.<EReference>exists(_eReferences, _function));
      }
    };
    return IterableExtensions.<EClass>exists(this.classes, _function);
  }
  
  public Collection<Collection<EClassifier>> createPartitions() {
    final ArrayList<Collection<EClassifier>> result = new ArrayList<Collection<EClassifier>>();
    final Consumer<EClass> _function = new Consumer<EClass>() {
      public void accept(final EClass it) {
        final ArrayList<EClassifier> partition = new ArrayList<EClassifier>();
        partition.add(it);
        EList<EReference> _eReferences = it.getEReferences();
        final Function1<EReference, Boolean> _function = new Function1<EReference, Boolean>() {
          public Boolean apply(final EReference it) {
            return Boolean.valueOf(it.isContainment());
          }
        };
        Iterable<EReference> _filter = IterableExtensions.<EReference>filter(_eReferences, _function);
        final Consumer<EReference> _function_1 = new Consumer<EReference>() {
          public void accept(final EReference it) {
            EClassifier _eType = it.getEType();
            Partitioner.this.addTransitive(partition, _eType);
          }
        };
        _filter.forEach(_function_1);
        result.add(partition);
      }
    };
    this.rootClasses.forEach(_function);
    return result;
  }
  
  private void addTransitive(final ArrayList<EClassifier> classifiers, final EClassifier classifier) {
    boolean _contains = classifiers.contains(classifier);
    boolean _not = (!_contains);
    if (_not) {
      classifiers.add(classifier);
      this.addSubtype(classifiers, classifier);
      if ((classifier instanceof EClass)) {
        EList<EReference> _eReferences = ((EClass)classifier).getEReferences();
        final Function1<EReference, Boolean> _function = new Function1<EReference, Boolean>() {
          public Boolean apply(final EReference it) {
            return Boolean.valueOf(it.isContainment());
          }
        };
        Iterable<EReference> _filter = IterableExtensions.<EReference>filter(_eReferences, _function);
        final Consumer<EReference> _function_1 = new Consumer<EReference>() {
          public void accept(final EReference it) {
            EClassifier _eType = it.getEType();
            Partitioner.this.addTransitive(classifiers, _eType);
          }
        };
        _filter.forEach(_function_1);
      }
    }
  }
  
  private void addSubtype(final ArrayList<EClassifier> classifiers, final EClassifier classifier) {
    final Function1<EClass, Boolean> _function = new Function1<EClass, Boolean>() {
      public Boolean apply(final EClass it) {
        EList<EClass> _eAllSuperTypes = it.getEAllSuperTypes();
        return Boolean.valueOf(_eAllSuperTypes.contains(classifier));
      }
    };
    Iterable<EClass> _filter = IterableExtensions.<EClass>filter(this.classes, _function);
    final Consumer<EClass> _function_1 = new Consumer<EClass>() {
      public void accept(final EClass it) {
        Partitioner.this.addTransitive(classifiers, it);
      }
    };
    _filter.forEach(_function_1);
  }
  
  public Iterable<EClass> missingClasses(final Collection<Collection<EClassifier>> partitions) {
    final Function1<EClass, Boolean> _function = new Function1<EClass, Boolean>() {
      public Boolean apply(final EClass clazz) {
        final Function1<Collection<EClassifier>, Boolean> _function = new Function1<Collection<EClassifier>, Boolean>() {
          public Boolean apply(final Collection<EClassifier> it) {
            final Function1<EClassifier, Boolean> _function = new Function1<EClassifier, Boolean>() {
              public Boolean apply(final EClassifier it) {
                return Boolean.valueOf(Objects.equal(it, clazz));
              }
            };
            return Boolean.valueOf(IterableExtensions.<EClassifier>exists(it, _function));
          }
        };
        boolean _exists = IterableExtensions.<Collection<EClassifier>>exists(partitions, _function);
        return Boolean.valueOf((!_exists));
      }
    };
    return IterableExtensions.<EClass>filter(this.classes, _function);
  }
}
