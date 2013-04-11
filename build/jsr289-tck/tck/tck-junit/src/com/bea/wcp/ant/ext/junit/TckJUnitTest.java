package com.bea.wcp.ant.ext.junit;

import java.util.Properties;
import java.util.Vector;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;

public class TckJUnitTest extends JUnitTest{
//	private JUnitTest delegate = null;
    /** the name of the test case */
    private String name = null;
//
//    /** the name of the result file */
//    private String outfile = null;
//
//    // @todo this is duplicating TestResult information. Only the time is not
//    // part of the result. So we'd better derive a new class from TestResult
//    // and deal with it. (SB)
//    private long runs, failures, errors;
//    private long runTime;

    // Snapshot of the system properties
    private Properties props = null;
    
    public TckJUnitTest(){
    	
    }
    
    public TckJUnitTest(String name){
//    	this( new JUnitTest(name));
    	this.name = name;
    }
    
//    public TckJUnitTest(JUnitTest t){
//    	delegate = t;
//    	name = t.getName();
//    	outfile = t.getOutfile();
//    	runs = t.runCount();
//    	failures = t.failureCount();
//    	errors = t.errorCount();
//    	runTime = t.getRunTime();
//    	props = t.getProperties();
//    }
    
    public TckJUnitTest(String name, boolean haltOnError, boolean haltOnFailure,
            boolean filtertrace){
//    	this(new JUnitTest(name,haltOnError,haltOnFailure,filtertrace));
        this.name  = name;
        this.haltOnError = haltOnError;
        this.haltOnFail = haltOnFailure;
        this.filtertrace = filtertrace;
    }
    
    
    void addFormattersTo(Vector v) {
        final int count = formatters.size();
        for (int i = 0; i < count; i++) {
            v.addElement(formatters.elementAt(i));
        }
    }
    
    public Object clone(){
    	TckJUnitTest t = (TckJUnitTest)super.clone();
        t.props = props == null ? null : (Properties) props.clone();
        t.formatters = (Vector) formatters.clone();
        return t;
    }

}
