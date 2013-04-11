package com.bea.wcp.ant.ext.junit;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitVersionHelper;
import org.apache.tools.ant.taskdefs.optional.junit.XMLConstants;
import org.apache.tools.ant.util.DOMElementWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.bea.wcp.ant.ext.annotations.AssertionIds;

/**
 * Prints XML output of the test to a specified Writer.
 * 
 * 
 * @see FormatterElement
 */

public class TckXMLFormatter implements JUnitResultFormatter, XMLConstants {

	/** constant for unnnamed testsuites/cases */
	private static final String UNKNOWN = "unknown";

	/** value attribute for test case elements */
	String ATTR_ASSERTION = "assertions";

	private static DocumentBuilder getDocumentBuilder() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (Exception exc) {
			throw new ExceptionInInitializerError(exc);
		}
	}

	/**
	 * The XML document.
	 */
	private Document doc;

	/**
	 * The wrapper for the whole testsuite.
	 */
	private Element rootElement;

	/**
	 * Element for the current test.
	 */
	private Map testElements = new HashMap();

	/**
	 * tests that failed.
	 */
	private Map failedTests = new HashMap();

	/**
	 * Timing helper.
	 */
	private Map testStarts = new HashMap();

	/**
	 * Where to write the log to.
	 */
	private OutputStream out;

	private FileOutputStream fileOut;

	public TckXMLFormatter() {
	}

	public void setOutput(OutputStream out) {
		this.out = out;		
	}

	public void setSystemOutput(String out) {
		formatOutput(SYSTEM_OUT, out);
	}

	public void setSystemError(String out) {
		formatOutput(SYSTEM_ERR, out);
	}

	/**
	 * The whole testsuite started.
	 */
	public void startTestSuite(JUnitTest suite) {
		doc = getDocumentBuilder().newDocument();
		rootElement = doc.createElement(TESTSUITE);
		String n = suite.getName();
		rootElement.setAttribute(ATTR_NAME, n == null ? UNKNOWN : n);

		// Output properties
		Element propsElement = doc.createElement(PROPERTIES);
		rootElement.appendChild(propsElement);
		Properties props = suite.getProperties();
		if (props != null) {
			Enumeration e = props.propertyNames();
			while (e.hasMoreElements()) {
				String name = (String) e.nextElement();
				Element propElement = doc.createElement(PROPERTY);
				propElement.setAttribute(ATTR_NAME, name);
				propElement.setAttribute(ATTR_VALUE, props.getProperty(name));
				propsElement.appendChild(propElement);
			}
		}
	}

	/**
	 * The whole testsuite ended.
	 */
	public void endTestSuite(JUnitTest suite) throws BuildException {
		rootElement.setAttribute(ATTR_TESTS, "" + suite.runCount());
		rootElement.setAttribute(ATTR_FAILURES, "" + suite.failureCount());
		rootElement.setAttribute(ATTR_ERRORS, "" + suite.errorCount());
		rootElement.setAttribute(ATTR_TIME, "" + (suite.getRunTime() / 1000.0));
		if (out != null) {
			outputResult(out, rootElement);
		}
//		}else if(fileOut != null){
//			try{
//				fileOut.close();
//			}catch(IOException ex){
//				// ignore
//			}
//			try{
//				File destFile =
//	                new File(suite.getTodir(),
//	                		suite.getOutfile() + ".xml");
//				System.out.println("============FileName="+destFile.getName());
//				fileOut = new FileOutputStream(destFile);
//				outputResult(fileOut,rootElement);
//			}catch(IOException ex){
//				throw new BuildException(ex);
//			}
//		}
	}

	private void outputResult(OutputStream s, Element e) throws BuildException {

		Writer wri = null;
		try {
			wri = new BufferedWriter(new OutputStreamWriter(s, "UTF8"));
			wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			(new DOMElementWriter()).write(e, wri, 0, "  ");
			wri.flush();
		} catch (IOException exc) {
			throw new BuildException("Unable to write log file", exc);
		} finally {
			if (s != System.out && s != System.err) {
				if (wri != null) {
					try {
						wri.close();
					} catch (IOException ex) {
						// ignore
					}
				}
			}
		}

	}

	/**
	 * Interface TestListener.
	 * 
	 * <p>
	 * A new Test is started.
	 */
	public void startTest(Test t) {
		testStarts.put(t, new Long(System.currentTimeMillis()));
	}

	/**
	 * Interface TestListener.
	 * 
	 * <p>
	 * A Test is finished.
	 */
	public void endTest(Test test) {
		// Fix for bug #5637 - if a junit.extensions.TestSetup is
		// used and throws an exception during setUp then startTest
		// would never have been called
		if (!testStarts.containsKey(test)) {
			startTest(test);
		}

		Element currentTest = null;
		if (!failedTests.containsKey(test)) {
			currentTest = doc.createElement(TESTCASE);
			String n = JUnitVersionHelper.getTestCaseName(test);
			currentTest.setAttribute(ATTR_NAME, n == null ? UNKNOWN : n);
			// a TestSuite can contain Tests from multiple classes,
			// even tests with the same name - disambiguate them.
			currentTest.setAttribute(ATTR_CLASSNAME, test.getClass().getName());
			/**
			 * added by hujin add assertion ids attributes 2008-01-07
			 */
			Method testMethod = getTestMethod(test, n);
			String assertion = "";
			if (testMethod != null) {
				assertion = getAssertionString(testMethod);
			}
			currentTest.setAttribute(ATTR_ASSERTION, assertion);

			/** end of assertion */

			rootElement.appendChild(currentTest);
			testElements.put(test, currentTest);
		} else {
			currentTest = (Element) testElements.get(test);
		}

		Long l = (Long) testStarts.get(test);
		currentTest.setAttribute(ATTR_TIME, ""
				+ ((System.currentTimeMillis() - l.longValue()) / 1000.0));
	}

	private Method getTestMethod(Test t, String methodName) {
		try {
			return t.getClass().getMethod(methodName, new Class[0]);

		} catch (Exception e) {
			// return null, the caller take the responsibility to check it
			return null;
		}
	}

	private String getAssertionString(Method t) {
		AssertionIds asserAnno = t.getAnnotation(AssertionIds.class);
		StringBuffer sb = new StringBuffer("");

		if (asserAnno != null) {
			String[] ids = asserAnno.ids();
			for (String id : ids) {
				sb.append("[" + id + "]");
			}
			String desc = asserAnno.desc();
			if (desc != null && desc.length() > 0) {
				sb.append(" - " + asserAnno.desc());
			}
		}
		return sb.toString();
	}

	/**
	 * Interface TestListener for JUnit &lt;= 3.4.
	 * 
	 * <p>
	 * A Test failed.
	 */
	public void addFailure(Test test, Throwable t) {
		formatError(FAILURE, test, t);
	}

	/**
	 * Interface TestListener for JUnit &gt; 3.4.
	 * 
	 * <p>
	 * A Test failed.
	 */
	public void addFailure(Test test, AssertionFailedError t) {
		addFailure(test, (Throwable) t);
	}

	/**
	 * Interface TestListener.
	 * 
	 * <p>
	 * An error occurred while running the test.
	 */
	public void addError(Test test, Throwable t) {
		formatError(ERROR, test, t);
	}

	private void formatError(String type, Test test, Throwable t) {
		if (test != null) {
			endTest(test);
			failedTests.put(test, test);
		}

		Element nested = doc.createElement(type);
		Element currentTest = null;
		if (test != null) {
			currentTest = (Element) testElements.get(test);
		} else {
			currentTest = rootElement;
		}

		currentTest.appendChild(nested);

		String message = t.getMessage();
		if (message != null && message.length() > 0) {
			nested.setAttribute(ATTR_MESSAGE, t.getMessage());
		}
		nested.setAttribute(ATTR_TYPE, t.getClass().getName());

		String strace = JUnitTestRunner.getFilteredTrace(t);
		Text trace = doc.createTextNode(strace);
		nested.appendChild(trace);
	}

	private void formatOutput(String type, String output) {
		Element nested = doc.createElement(type);
		rootElement.appendChild(nested);
		nested.appendChild(doc.createCDATASection(output));
	}

} // XMLJUnitResultFormatter

