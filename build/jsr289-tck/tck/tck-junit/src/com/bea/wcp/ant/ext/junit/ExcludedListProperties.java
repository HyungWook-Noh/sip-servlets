package com.bea.wcp.ant.ext.junit;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * this class is provided to read and parse the excludedList.properties file.
 * the file's format is like the followings:
 * 
 * exclude.class.1=com.bea.wcp.Class1
 * exclude.class.2=com.bea.wcp.wlss.Class2
 * exclude.method.1=com.bea.wcp.Class2.method1
 * 
 * @author hujin
 *
 */
class ExcludedListProperties {
	private static final String FILENAME = "excludeList.properties";
	                                        	
	private static final String packagePrefix = "exclude.package";
	private static final String classPrefix = "exclude.class";
	private static final String methodPrefix = "exclude.method";
	
	private static boolean loadSuccessfully = true;
	private static Properties properties = new Properties();
	private List excludedPackages = new ArrayList();
	private List excludedClasses = new ArrayList();
	private List excludedMethods = new ArrayList();
	
	private static ExcludedListProperties excludes = new ExcludedListProperties();
	private ExcludedListProperties(){
		try{
//			URL url = Thread.currentThread().getContextClassLoader().getResource(FILENAME);
//			System.out.println("uri of excludedList is: " + url);
//			StackTraceElement[] sts = Thread.currentThread().getStackTrace();
//			for(StackTraceElement st: sts){
//				System.out.println(st.getFileName() + ": " + st.getClassName()+"."+st.getMethodName()+" - "+st.getLineNumber());
//			}
			InputStream inputStream = 
					Thread.currentThread().getContextClassLoader().getResourceAsStream(FILENAME);
//			System.out.println(Thread.currentThread().getContextClassLoader().getClass().getName());
			
			if(inputStream == null){
				inputStream = getClass().getClassLoader().getResourceAsStream(FILENAME);
			}
//			System.out.println(getClass().getClassLoader().getClass().getName());
			if(inputStream == null){
				loadSuccessfully = false;
				System.out.println("****************************************************************************");
				System.out.println("******         Can't find the excludeList.properties file!            ******");
				System.out.println("******Please make sure the file's directory is set into the CLASSPATH.******");
				System.out.println("****************************************************************************");
				return;
			}
			properties.load(inputStream);
			inputStream.close();
			classifyExcludedElements();
		}catch(Exception e){
			//just ignore the exception, and assume there is no exclusion.
			e.printStackTrace();
		}
	}
	
	private void classifyExcludedElements(){
		Enumeration names = properties.propertyNames();
		while(names.hasMoreElements()){
			String name = (String)names.nextElement();
			if(name.startsWith(packagePrefix)){
				excludedPackages.add(properties.get(name));
			}else if(name.startsWith(classPrefix)){
				excludedClasses.add(properties.get(name));
			}else if(name.startsWith(methodPrefix)){
				excludedMethods.add(properties.get(name));
			}
		}
	}
	
	public static boolean isLoadSuccessfully(){
		return loadSuccessfully;
	}
	
	public static boolean isExcluded(TestCase tc){
		String className = tc.getClass().getName();
		String caseName = className + "." + tc.getName();
		String packageName = tc.getClass().getPackage().getName();
		boolean r =  excludes.excludedMethods.contains(caseName)
		     || excludes.excludedClasses.contains(className) 
		     || excludes.isPackageExcluded(packageName);
		
//		TestProxy.a(className + " " + r + " " + loadSuccessfully);
		
		return r;
	}
	
	private boolean isPackageExcluded(String s){		
	    Iterator itr = excludedPackages.iterator();
	    while(itr.hasNext()){
	    	String p = (String)itr.next();
	    	if(s.startsWith(p)){
	    		return true;
	    	}
	    }
	    return false;
	}
	
	public static void main(String[] args){
		ExcludedListProperties o = ExcludedListProperties.excludes;
		Properties p = o.properties;
		Enumeration names = p.propertyNames();
		while(names.hasMoreElements()){
			String name = (String )names.nextElement();
			System.out.println( name + "=" + p.getProperty(name));
		}
		Iterator itr = o.excludedClasses.iterator();
		while(itr.hasNext()){
			String name = (String)itr.next();
			System.out.println(name);
		}
	}
	
}
