package com.bea.wcp.ant.ext.junit;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
/**
 * run unit test, but exclude some packages, classes and methods which are specified
 * in the excludedList.properties.
 * because of the license constraints, we can only make use of ant but not JUnit to do 
 * the exclusion, so what we can do is to check if the case is excluded before every case 
 * is invoked, which of course will bring some negative influence to the efficiency
 * @author hujin
 *
 */
public class TestHandler implements InvocationHandler{
	private Test del = null;	
	
	public TestHandler(Test t){
		del = t;
	}
	public Object invoke(Object proxy, Method method, Object[] args){
//		TestProxy.a("in handler.........\n\n");
		if(TestSuite.class.isAssignableFrom(del.getClass())){			
			TestSuite suite = (TestSuite)del;			
			if(method.getName().equals("run")){
				//the first args must be TestResult
				run((TestResult)args[0],suite.tests());
				return Void.TYPE;
			}else{
				//countTestCases
				return countTestSuite();
			}
		}else if(TestCase.class.isAssignableFrom(del.getClass())){
			if(method.getName().equals("run")){
				runTest(del, (TestResult)args[0]);
				return Void.TYPE;
			}else{
				//countTestCases
				return countTestCase();
			}
		}
		//should not be here
		return null;
	}
	
	private void run(TestResult result, Enumeration tests) {
		for (Enumeration e = tests; e.hasMoreElements(); ) {
	  		if (result.shouldStop() )
	  			break;
			Test test= (Test)e.nextElement();			
			runTest(test, result);
		}
	}

	private void runTest(Test test, TestResult result) {
//		TestProxy.a("in runTest().........\n\n");
		if(TestSuite.class.isAssignableFrom(test.getClass())){
			TestProxy.getInstance(test).run(result);			
		}else if(TestCase.class.isAssignableFrom(test.getClass())){
			TestCase tc = (TestCase)test;
//			TestProxy.a("in TestCase.run(),className="+ tc.getClass().getName()
//					+ "; name="+tc.getName()+"\n\n\n");
			if(ExcludedListProperties.isExcluded(tc)){				
				return;
			}
			test.run(result);
		}
	}
	//just for TestCase
	private int countTestCase(){
		if(ExcludedListProperties.isExcluded((TestCase)del)){
			return 0;
		}
		return 1;
	}
	//just for TestSuite
	private int countTestSuite(){
		int count= 0;
		TestSuite suite = (TestSuite)del;
		for (Enumeration e= suite.tests(); e.hasMoreElements(); ) {
			Test test= (Test)e.nextElement();
			Test proxy = (Test)TestProxy.getInstance(test);
			count= count + proxy.countTestCases();
		}
		return count;
	}
}
