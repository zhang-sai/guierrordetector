package edu.washington.cs.detector.experiments.eclipseplugin;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGEntryManager;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.FilterStrategy;
import edu.washington.cs.detector.SWTAppUIErrorMain;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.MergeSamePrefixStrategy;
import edu.washington.cs.detector.experiments.filters.MergeSameTailStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveContainingNodeStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveDoubleThreadStartStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSameEntryStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseNoSystemCalls;
import edu.washington.cs.detector.guider.CGTraverseSWTGuider;
import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import edu.washington.cs.detector.walaextension.ParamTypeCustomizedEntrypoint;

public class TestRSESDKUI extends AbstractEclipsePluginTest {
	public static String PLUGIN_DIR = "D:\\research\\guierror\\subjects\\RSE-SDK-3.0.3\\eclipse" + Globals.fileSep
			+ "plugins";
	
	private boolean init_all_arg_objs = false;
	
	@Override
	protected String getAppPath() {
		return PLUGIN_DIR;
	}

	@Override
	protected String getDependentJars() {
		return EclipsePluginCommons.DEPENDENT_JARS;
	}

	@Override
	protected Iterable<Entrypoint> getEntrypoints(ClassHierarchy cha) {
		
		//initialize classes to be created
		if(this.init_all_arg_objs) {
			Set<String> appClasses = WALAUtils.getAllAppClassNames(cha);
			ParamTypeCustomizedEntrypoint.setUserClasses(appClasses);
		}
		

//		return kclass.toString().indexOf("org/eclipse/dstore/internal/core/client/ClientUpdateHandler") != -1
//	    || kclass.toString().indexOf("org/eclipse/dstore/core/client/ClientConnection") != -1;
		String className1 = "org.eclipse.dstore.internal.core.client.ClientUpdateHandler";
		String className2 = "org.eclipse.dstore.core.client.ClientConnection";
		String className3 = "org.eclipse.rse.internal.services.dstore.files.DStoreFileService";
		
		final HashSet<Entrypoint> result = HashSetFactory.make();
		
		String package1 = "org.eclipse.rse.ui";
		String package2 = "org.eclipse.rse.internal.ui";
		
		Set<IClass> allAppClasses = WALAUtils.getAllAppClasses(cha);
		
		for(IClass c : allAppClasses) {
			if(c.isAbstract() || c.isInterface()) {
				continue;
			}
			String packageName = WALAUtils.getJavaPackageName(c);
//			if(packageName.startsWith(package1) || packageName.startsWith(package2)) 
			{
				for(IMethod m : c.getDeclaredMethods()) {
					String methodFullName = WALAUtils.getFullMethodName(m);
					
					if(m.isAbstract() || m.isClinit() || m.isNative()) {
						continue;
					}
//					if(m.isProtected() || m.isPublic()) {
//						Entrypoint ep = new ParamTypeCustomizedEntrypoint(m, cha);
//						result.add(ep);
//					}
//					if(m.isInit()) {
//						Entrypoint ep = new ParamTypeCustomizedEntrypoint(m, cha);
//						result.add(ep);
//					}
					
					if(methodFullName.startsWith(className1) || methodFullName.startsWith(className2)
							|| methodFullName.startsWith(className3)) 
					{
						if (m.isInit()) {
							Entrypoint ep = new ParamTypeCustomizedEntrypoint(m, cha);
						    result.add(ep);
						} else {
						    Entrypoint ep = new DefaultEntrypoint(m, cha);
						    result.add(ep);
						}
					} else {
						if (m.isInit()) {
							Entrypoint ep = new ParamTypeCustomizedEntrypoint(m, cha);
						    result.add(ep);
						}
					}
					
				}
			}
		}
		
		return result;
	}

	@Override
	protected Collection<CGNode> getStartNodes(Iterable<CGNode> allNodes,
			ClassHierarchy cha) {
		String package1 = "org.eclipse.rse.ui";
		String package2 = "org.eclipse.rse.internal.ui";
		
		String name = "org.eclipse.dstore.internal.core.client.ClientUpdateHandler";
		
		
		Collection<CGNode> nodes = new HashSet<CGNode>();
		
		for(CGNode node : allNodes) {
			IMethod m = node.getMethod();
			String str = WALAUtils.getFullMethodName(m);
			if(str.startsWith(package1) || str.startsWith(package2)) {
				nodes.add(node);
			}
			
			if(nodes.size() > 100) {
				break;
			}
			
//			if(str.startsWith(name)) {
//				nodes.add(node);
//			}
		}
			
		return nodes;
	} 
	
	public void testRSEDKUI() throws ClassHierarchyException, IOException {
		Log.logConfig("./log.txt");
		UIAnomalyDetector.DEBUG = true;
		
		this.init_all_arg_objs = true;
		
		super.setCGType(CG.RTA);
		super.setThreadStartGuider(new CGTraverseSWTGuider());
		super.setUIAnomalyGuider(new CGTraverseSWTGuider());
		
		Collection<AnomalyCallChain> chains = super.reportUIErrors();
		
		int count = 0;
		for(AnomalyCallChain chain : chains) {
			Log.logln("The " + (count++) + "-th chain");
			Log.logln(chain.getFullCallChainAsString());
		}
	}
	
//	@Override
//	protected boolean isUIClass(IClass kclass) {
//		return TestCommons.isConcreteAccessibleClass(kclass)
//				&& kclass.toString().indexOf("/ui") != -1
//				&& kclass.toString().indexOf("/rse/") != -1;
//	}
//
//	public void testGetAppJars() {
//		super.checkAppJarNumber(46);
//	}

//	public void testDetectUIErrors() throws IOException,
//			ClassHierarchyException {
//		List<AnomalyCallChain> chains = super.reportUIErrors(SWTAppUIErrorMain.default_log, CG.ZeroOneContainerCFA);
//		assertEquals(308, chains.size());
//	}
	
	/**
	 * No errors can be found, since the entry methods must be correctly specified.
	 * */
//	public void testKnownBug267478() throws ClassHierarchyException, IOException {
//		AbstractEclipsePluginTest test = new AbstractEclipsePluginTest(){
//			@Override
//			protected boolean isUIClass(IClass kclass) {
//				return kclass.toString().indexOf("org/eclipse/dstore/internal/core/client/ClientUpdateHandler") != -1;
//			}
//			@Override
//			protected String getAppPath() {
//				return PLUGIN_DIR;
//			}
//			@Override
//			protected String getDependentJars() {
//				return EclipsePluginCommons.DEPENDENT_JARS;
//			}
//		};
//		AbstractEclipsePluginTest.DEBUG = true;
//		List<AnomalyCallChain> chains  = test.reportUIErrors(SWTAppUIErrorMain.default_log, CG.ZeroCFA);
//		assertEquals(0, chains.size());
//	}
	
//	public void testKnownBug267478ByAllEntries() throws ClassHierarchyException, IOException {
//		//org.eclipse.rse.services.dstore.util.DownloadListener
//		AbstractEclipsePluginTest test = new AbstractEclipsePluginTest(){
//			final Collection<String> exposedClasses = TestCommons.getPluginExposedClasses(getAppPath(), "./logs/plugin_xml_classes.txt");
//			
//			@Override
//			protected CGTraverseGuider getThreadStartGuider() {return new CGTraverseSWTGuider(); }
//			
//			@Override
//			protected CGTraverseGuider getUIAnomalyGuider() {return new CGTraverseSWTGuider(); }
//			
//			@Override
//			protected boolean isUIClass(IClass kclass) {
//				return kclass.toString().indexOf("org/eclipse/dstore/internal/core/client/ClientUpdateHandler") != -1
//				    || kclass.toString().indexOf("org/eclipse/dstore/core/client/ClientConnection") != -1;
//			}
//			@Override
//			protected boolean isEntryClass(IClass kclass) {
//				if(isUIClass(kclass)) {
//					return true;
//				}
//				if(kclass.toString().indexOf("DStoreFileService") != -1) {
//					return true;
//				}
//				String javaClass = WALAUtils.getJavaFullClassName(kclass);
//				return exposedClasses.contains(javaClass);
//			}
//			@Override
//			protected String getAppPath() {
//				return PLUGIN_DIR;
//			}
//			@Override
//			protected String getDependentJars() {
//				return EclipsePluginCommons.DEPENDENT_JARS;
//			}
//		};
//		
//		List<FilterStrategy> filters = new LinkedList<FilterStrategy>();
//		filters.add(new RemoveSystemCallStrategy());
//		filters.add(new MergeSameTailStrategy());
//		filters.add(new RemoveDoubleThreadStartStrategy());
//		filters.add(new MergeSamePrefixStrategy(6));
//		
//		AbstractEclipsePluginTest.DEBUG = true;
//		List<AnomalyCallChain> chains 
//		    = test.reportUIErrorsWithEntries(SWTAppUIErrorMain.default_log, CG.OneCFA, filters); //can change the CG type here
//		
//		System.out.println(chains.size());
//	}
	
//	public void testCheckKnownBug26747() throws IOException, ClassHierarchyException {
//		AbstractEclipsePluginTest test = new AbstractEclipsePluginTest(){
//			final Collection<String> exposedClasses = TestCommons.getPluginExposedClasses(getAppPath(), "./logs/plugin_xml_classes.txt");
//			@Override
//			protected boolean isUIClass(IClass kclass) {
//				return kclass.toString().indexOf("org/eclipse/dstore/internal/core/client/ClientUpdateHandler") != -1
//				    || kclass.toString().indexOf("org/eclipse/dstore/core/client/ClientConnection") != -1;
//			}
//			@Override
//			protected boolean isEntryClass(IClass kclass) {
//				if(isUIClass(kclass)) {
//					return true;
//				}
//				String javaClass = WALAUtils.getJavaFullClassName(kclass);
//				return exposedClasses.contains(javaClass);
//			}
//			@Override
//			protected String getAppPath() {
//				return PLUGIN_DIR;
//			}
//			@Override
//			protected String getDependentJars() {
//				return EclipsePluginCommons.DEPENDENT_JARS;
//			}
//		};
//		String startSig = "Lorg/eclipse/dstore/core/client/ClientConnection, localConnect()";
//		
//		String[] pathNodesSigs = new String[] {
//				"Ljava/lang/Thread, start()V",
//				"Lorg/eclipse/dstore/core/model/Handler, run()V >",
//				"Lorg/eclipse/dstore/core/model/UpdateHandler, handle()V >",
//				"Lorg/eclipse/dstore/internal/core/client/ClientUpdateHandler, sendUpdates()V",
//				"Lorg/eclipse/dstore/internal/core/client/ClientUpdateHandler, notify",
//				"Lorg/eclipse/dstore/internal/extra/DomainNotifier, fireDomainChanged(Lorg/eclipse/dstore/extra/DomainEvent;)V >",
//				"Lorg/eclipse/rse/services/dstore/util/DownloadListener, domainChanged(Lorg/eclipse/dstore/extra/DomainEvent;)V >",
//				"Lorg/eclipse/rse/services/dstore/util/DownloadListener, setDone(Z)V",
//				"Lorg/eclipse/rse/services/dstore/util/DownloadListener, updateDownloadState()V",
//				"Lorg/eclipse/jface/dialogs/ProgressMonitorDialog$ProgressMonitor, worked(I)V",
//				"Lorg/eclipse/jface/dialogs/ProgressMonitorDialog$ProgressMonitor, internalWorked(D)V",
//				"Lorg/eclipse/jface/dialogs/ProgressIndicator, worked(D)V",
//				"Application, Lorg/eclipse/swt/widgets/ProgressBar, getSelection()I",
//				"Application, Lorg/eclipse/swt/widgets/Widget, checkWidget()V"
//		};
//		List<AnomalyCallChain> chains = test.checkPathValidity(null, CG.RTA, startSig, pathNodesSigs);
//		System.out.println("Number of chains: " + chains.size());
//		
//		for(AnomalyCallChain chain : chains) {
//			System.out.println(chain.getFullCallChainAsString());
//			System.out.println(Globals.lineSep);
//		}
//		
//		assertEquals(1, chains.size());
//	}
	
//	public void testEntryPointInCallGraph() throws IOException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException {
//		String myClass = "org.eclipse.dstore.internal.core.client.ClientUpdateHandler";
//		
//		String appPath =  TestCommons.assemblyAppPath(getAppPath(), getDependentJars());
//		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appPath,
//				FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
//		ClassHierarchy cha = ClassHierarchy.make(scope);
//
//		ClassLoaderReference clr = scope.getApplicationLoader();
//		final HashSet<Entrypoint> result = HashSetFactory.make();
//		for (IClass klass : cha) {
//			if (klass.getClassLoader().getReference().equals(clr)) {
//				Collection<IMethod> allMethods = klass.getDeclaredMethods();
//				for(IMethod m : allMethods) {
//					if(!m.isPublic() || m.isAbstract()) {
//						continue;
//					}
//					TypeName tn = m.getDeclaringClass().getName();
//					String fullClassName = (tn.getPackage() != null ? Utils.translateSlashToDot(tn.getPackage().toString()) + "." : "")
//					    + tn.getClassName().toString();
//					if(!fullClassName.equals(myClass)) {
//						continue;
//					}
//					result.add(new DefaultEntrypoint(m, cha));
//				}
//			}
//		}
//
//		Iterable<Entrypoint> entrypoints = new Iterable<Entrypoint>() {
//			public Iterator<Entrypoint> iterator() {
//				return result.iterator();
//			}
//		}; 
//
//		System.out.println("Number of entry points: " + Utils.countIterable(entrypoints));
//		Utils.dumpCollection(entrypoints, System.out);
//		
//		/* Explicitly set entrypoints in the AnalysisOptions */
//		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
//		CallGraphBuilder builder = Util.makeZeroCFABuilder(options, new AnalysisCache(), cha, scope);
//
//		System.out.println("Type of the call graph builder: " + builder.getClass());
//		System.out.println("Before making call graph, the number of entry points: "
//				+ Utils.countIterable(options.getEntrypoints()));
//		CallGraph callgraph = builder.makeCallGraph(options, null);
//
//		/*the size of entryNodes is DIFFERENT than the size of entrypoints*/
//		Collection<CGNode> entryNodes = callgraph.getEntrypointNodes();
//		System.out.println("Number of entry nodes: " + entryNodes.size());
//		System.out.println(entryNodes);
//		
//		assertEquals("The entry size is not equal.", entryNodes.size(), Utils.countIterable(entrypoints));
//	}
}