package edu.washington.cs.detector.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.UIAnomalyDetector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestAndroidSwingSWTPluginUtils extends TestCase {
	public static Test suite() {
		return new TestSuite(TestAndroidSwingSWTPluginUtils.class);
	}
	
	public void testGetAllWidget() throws IOException, ClassHierarchyException {
		String jarPath = "D:\\Java\\android-sdk-windows\\platforms\\android-8\\android.jar";
		CGBuilder builder = new CGBuilder(jarPath);
		builder.makeScopeAndClassHierarchy();
		ClassHierarchy cha = builder.getClassHierarchy();
		IClass view = AndroidUtils.getWidgetView(cha);
		int num = 0;
		for(IClass c : cha) {
			if(cha.isAssignableFrom(view, c)) {
				System.out.println(num++ + "  -  " + c);
			}
		}
		assertEquals(63, num);
	}
	
	public void testFindAppActivity() throws IOException, ClassHierarchyException {
		String appPath = "D:\\research\\guierror\\eclipsews\\TestAndroid\\bin\\classes\\test\\android"
			+ Globals.pathSep +
			"D:\\Java\\android-sdk-windows\\platforms\\android-8\\android.jar";
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		
		Collection<IClass> colls = AndroidUtils.getAppActivityClasses(builder.getClassHierarchy());
		System.out.println(colls);
		assertEquals(1, colls.size());
	}
	
	public void testLoadAllListener() throws IOException, ClassHierarchyException {
		String appPath = "D:\\research\\guierror\\eclipsews\\TestAndroid\\bin\\classes\\test\\android"
			+ Globals.pathSep +
			"D:\\Java\\android-sdk-windows\\platforms\\android-8\\android.jar";
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		List<IClass> allListeners = AndroidUtils.getAndroidListenerClasses(builder.getClassHierarchy());
		assertEquals(115, allListeners.size());
		
		Set<String> set = new HashSet<String>();
		for(IClass c : allListeners) {
			for(IMethod m : c.getDeclaredMethods()) {
				if(m.isPrivate()) {
					continue;
				}
				set.add(m.getName().toString());
			}
		}
		System.out.println(set.size());
		assertEquals(170, set.size());
	}
	
	public void testGetCustomizedWidget() throws IOException, ClassHierarchyException {
		String appPath = "D:\\research\\guierror\\eclipsews\\TestAndroid\\bin\\classes\\test\\android"
			+ Globals.pathSep +
			"D:\\Java\\android-sdk-windows\\platforms\\android-8\\android.jar";
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		
		String xmlContent = Files.getFileContents("D:\\research\\guierror\\eclipsews\\TestAndroid\\res\\layout\\main.xml");
		Collection<String> classes = AndroidUtils.extractCustomizedUIs(builder.getClassHierarchy(), xmlContent);
		System.out.println(classes);
		assertEquals(1, classes.size());
	}
	
	public void testGetDeclardWidget() throws ZipException, IOException, ClassHierarchyException {
		String path = //"E:\\market_20101102.tar\\market_20101102\\applications\\Comics\\cat.bcnmultimedia.paraboles.apk";
			"D:\\research\\guierror\\eclipsews\\TestAndroid";
		
		String appPath = "D:\\research\\guierror\\eclipsews\\TestAndroid\\bin\\classes\\test\\android"
			+ Globals.pathSep +
			"D:\\Java\\android-sdk-windows\\platforms\\android-8\\android.jar";
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		
		Collection<String> widgets = AndroidUtils.extractAllUIs(builder.getClassHierarchy(), new File(path));
		System.out.println(widgets);
		assertEquals(4, widgets.size());
	}
	
	public void testGetDeclaredWidgetSchoolCartoon() throws ZipException, IOException, ClassHierarchyException {
		String path = "D:\\develop-tools\\apktool\\tmp";
		List<File> files = Files.getFileListing(new File(path));
		for(File f : files) {
			if(f.getAbsolutePath().indexOf("res" + Globals.fileSep + "layout") != -1
					&& f.getName().endsWith(".xml")) {
				System.out.println(f);
				Collection<String> widgets = AndroidUtils.extractAndroidUIs(Files.getFileContents(f));
				System.out.println("  - no: " +widgets.size());
				System.out.println("  -: " + widgets);
			}
		}
		//for all
		CGBuilder builder = new CGBuilder("D:\\Java\\android-sdk-windows\\platforms\\android-8\\android.jar");
		builder.makeScopeAndClassHierarchy();
		Collection<String> all = AndroidUtils.extractAllUIs(builder.getClassHierarchy(), new File(path));
		System.out.println("No: " + all.size());
		System.out.println(all);
		assertEquals(9, all.size());
	}
	
	/**
	 * This test is fragile, no surprise if it fails.
	 * */
	public void testClassesInAndroidLayout() throws IOException {
		String path = "D:\\research\\guierror\\eclipsews\\TestAndroid\\res\\layout\\main.xml";
		String xmlContent = Files.getFileContents(path);
		Collection<String> declaredClasses  = AndroidUtils.extractAndroidUIs(xmlContent);
		System.out.println(declaredClasses);
		assertEquals("No of declared classes is incorrect", 3, declaredClasses.size() );
	}

	public void testExtractPluginXML() throws IOException {
		String content = Utils
				.getPluginXMLContent("D:\\research\\guierror\\subjects\\site-1.6.18\\plugins\\org.tigris.subversion.subclipse.ui_1.6.18.jar");
		
		Collection<String> clazzList = Utils.extractClasses(content);
		for(String clazz : clazzList) {
			System.out.println(clazz);
		}
		assertEquals(88, clazzList.size());
	}
	
	public void testExtractAllPluginXML() throws IOException {
		String pluginDir = TestCommons.rse_303_dir + Globals.fileSep + "plugins";
		Set<String> allClasses = new LinkedHashSet<String>();
		
		List<String> jarFiles = TestCommons.getNonSourceNonTestsJars(pluginDir);
		for(String jarFile : jarFiles) {
			allClasses.addAll(Utils.extractClassFromPluginXML(jarFile));
		}
		System.out.println("Number of exposed class: " + allClasses.size());
		assertEquals(101, allClasses.size());
	}
	
	public void testExtractAllSwingAWTListeners() throws IOException, ClassHierarchyException {
		String appPath = TestCommons.testfolder + "swingerror";
		CGBuilder builder = new CGBuilder(appPath, UIAnomalyDetector.EXCLUSION_FILE_SWING);
		builder.makeScopeAndClassHierarchy();
		Collection<String> listeners = SwingUtils.getAllPublicSwingAWTListeners(builder.getClassHierarchy());
//		int i = 0;
//		for(String l : listeners) {
//			i++;
//			System.out.println(l);
//		}
		
		assertEquals(218, listeners.size());
		
		//test load all listeners
		Collection<String> methods = new HashSet<String>();
		for(String l : listeners) {
			IClass c = WALAUtils.lookupClass(builder.getClassHierarchy(), l);
			assertNotNull(c);
			
			if(!c.isInterface()) {
				continue;
			}
			
			for(IMethod m : c.getDeclaredMethods()) {
				if(m.isPrivate() || m.isNative() || m.isStatic() || m.isSynthetic()) {
					continue;
				}
				if(m.isInit() || m.isClinit()) {
					continue;
				}
				methods.add(m.getName().toString());
			}
			
		}
		
		assertEquals(98, methods.size());
		for(String m : methods) {
			System.out.println(m);
		}
	}
	
	public void testAllAndroid_2_2_Classes() throws IOException, ClassHierarchyException {
		String appPath = "D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		CGBuilder builder = new CGBuilder(appPath, UIAnomalyDetector.EXCLUSION_FILE_SWING);
		builder.makeScopeAndClassHierarchy();
		StringBuilder sb = new StringBuilder();
		for(IClass c : builder.getClassHierarchy()) {
			sb.append(WALAUtils.getJavaFullClassName(c));
			sb.append(Globals.lineSep);
//			if(c.toString().indexOf("ProgressBar")!=-1) {
//				for(IMethod m : c.getDeclaredMethods()) {
//					String sig = m.getSignature();
//					if(sig.indexOf("refresh") != -1) {
//						System.out.println(sig);
//					}
//				}
//			}
		}
		Files.writeToFile(sb.toString(), "./tests/edu/washington/cs/detector/util/androidclasses.txt");
	}
}