package com.bea.wcp.ant.ext.junit;

import java.lang.reflect.Proxy;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestResult;

public class TestProxy implements Test{	
	private Test proxy = null;
	public static Test getInstance(Test t){
		return new TestProxy(t);
	}
	TestProxy(Test t){	
		proxy = (Test)Proxy.newProxyInstance(t.getClass().getClassLoader(),
		        new Class[]{Test.class}, new TestHandler(t));		
	}
	
	public int countTestCases(){
		return proxy.countTestCases();
	}
	
	public void run(TestResult result){
		proxy.run(result);
	}
	
	//just used to output debug information. System.out.println sometimes is not available
	//during executing ant script. It's weird!
	public static void a(String s){
		System.out.println("\n\n\n\n\n\n\nxxxxxxxxxxxxxxx");
    	throw new NullPointerException("\n\n\n\n" + s + "\n\n\n\n");
    }
}
