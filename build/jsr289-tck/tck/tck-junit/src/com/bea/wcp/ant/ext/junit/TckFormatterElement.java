package com.bea.wcp.ant.ext.junit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.optional.junit.BriefJUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;
import org.apache.tools.ant.types.EnumeratedAttribute;

/**
 * <p> A wrapper for the implementations of <code>JUnitResultFormatter</code>.
 * In particular, used as a nested <code>&lt;formatter&gt;</code> element in
 * a <code>&lt;junit&gt;</code> task.
 * <p> For example,
 * <code><pre>
 *       &lt;junit printsummary="no" haltonfailure="yes" fork="false"&gt;
 *           &lt;formatter type="plain" usefile="false" /&gt;
 *           &lt;test name="org.apache.ecs.InternationalCharTest" /&gt;
 *       &lt;/junit&gt;</pre></code>
 * adds a <code>plain</code> type implementation
 * (<code>PlainJUnitResultFormatter</code>) to display the results of the test.
 *
 * <p> Either the <code>type</code> or the <code>classname</code> attribute
 * must be set.
 *
 * @see JUnitTask
 * @see XMLJUnitResultFormatter
 * @see BriefJUnitResultFormatter
 * @see PlainJUnitResultFormatter
 * @see JUnitResultFormatter
 */
public class TckFormatterElement extends FormatterElement{
	private FormatterElement delegate = null;

    private String classname;
    private String extension;
    private OutputStream out = System.out;
    private File outFile;
    private boolean useFile = true;
    private String ifProperty;
    private String unlessProperty;

    public static final String XML_FORMATTER_CLASS_NAME =
        "org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter";
    public static final String TCKXML_FORMATTER_CLASS_NAME =
        "com.bea.wcp.ant.ext.junit.TckXMLFormatter";
    public static final String BRIEF_FORMATTER_CLASS_NAME =
        "org.apache.tools.ant.taskdefs.optional.junit.BriefJUnitResultFormatter";
    public static final String PLAIN_FORMATTER_CLASS_NAME =
        "org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter";
    
    
    public TckFormatterElement(){
    	
    }
    
    public TckFormatterElement(FormatterElement e){
//    	System.out.println("in TckFormatterElement.....");
    	delegate = e;
    	if(delegate != null){
    		this.setClassname(e.getClassname());
    		this.setUseFile(true);
    	}
    }

    /**
     * <p> Quick way to use a standard formatter.
     *
     * <p> At the moment, there are three supported standard formatters.
     * <ul>
     * <li> The <code>xml</code> type uses a <code>XMLJUnitResultFormatter</code>.
     * <li> The <code>xml_ext</code> type uses a <code>TckXMLFormatter</code>.
     * <li> The <code>brief</code> type uses a <code>BriefJUnitResultFormatter</code>.
     * <li> The <code>plain</code> type (the default) uses a <code>PlainJUnitResultFormatter</code>.
     * </ul>
     *
     * <p> Sets <code>classname</code> attribute - so you can't use that
     * attribute if you use this one.
     */
    public void setType(TypeAttribute type) {
        if ("xml".equals(type.getValue())) {
            setClassname(XML_FORMATTER_CLASS_NAME);
        } else if("xml_ext".equals(type.getValue())){
        	setClassname(TCKXML_FORMATTER_CLASS_NAME);
        }{
            if ("brief".equals(type.getValue())) {
                setClassname(BRIEF_FORMATTER_CLASS_NAME);
            } else { // must be plain, ensured by TypeAttribute
                setClassname(PLAIN_FORMATTER_CLASS_NAME);
            }
        }
    }

    /**
     * <p> Set name of class to be used as the formatter.
     *
     * <p> This class must implement <code>JUnitResultFormatter</code>
     */
    public void setClassname(String classname) {
        this.classname = classname;

        if (XML_FORMATTER_CLASS_NAME.equals(classname)
        		|| TCKXML_FORMATTER_CLASS_NAME.equals(classname)) {
           setExtension(".xml");
        } else if (PLAIN_FORMATTER_CLASS_NAME.equals(classname)) {
           setExtension(".txt");
        } else if (BRIEF_FORMATTER_CLASS_NAME.equals(classname)) {
           setExtension(".txt");
        }
    }

    /**
     * Get name of class to be used as the formatter.
     */
    public String getClassname() {
        return classname;
    }

    public void setExtension(String ext) {
        this.extension = ext;
    }

    public String getExtension() {
//    	System.out.println("in getExtension:"+extension);
        return extension;
    }

    /**
     * <p> Set the file which the formatte should log to.
     *
     * <p> Note that logging to file must be enabled .
     */
    void setOutfile(File out) {
        this.outFile = out;
    }

    /**
     * <p> Set output stream for formatter to use.
     *
     * <p> Defaults to standard out.
     */
    public void setOutput(OutputStream out) {
        this.out = out;
    }

    /**
     * Set whether the formatter should log to file.
     */
    public void setUseFile(boolean useFile) {
        this.useFile = useFile;
    }

    /**
     * Get whether the formatter should log to file.
     */
    boolean getUseFile() {
        return useFile;
    }

    /**
     * Set whether this formatter should be used.  It will be
     * used if the property has been set, otherwise it won't.
     * @param ifProperty name of property
     */
    public void setIf(String ifProperty) {
        this.ifProperty = ifProperty;
    }

    /**
     * Set whether this formatter should NOT be used. It
     * will not be used if the property has been set, orthwise it
     * will be used.
     * @param unlessProperty name of property
     */
    public void setUnless(String unlessProperty) {
        this.unlessProperty = unlessProperty;
    }

    /**
     * Ensures that the selector passes the conditions placed
     * on it with <code>if</code> and <code>unless</code> properties.
     */
    public boolean shouldUse(Task t) {
    	if(delegate != null){
    		return delegate.shouldUse(t);
    	}
        if (ifProperty != null && t.getProject().getProperty(ifProperty) == null) {
            return false;
        } else if (unlessProperty != null
                    && t.getProject().getProperty(unlessProperty) != null) {
            return false;
        }

        return true;
    }

    /**
     * @since Ant 1.2
     */
    JUnitResultFormatter createFormatter() throws BuildException {
        return createFormatter(null);
    }

    /**
     * @since Ant 1.6
     */
    JUnitResultFormatter createFormatter(ClassLoader loader)
        throws BuildException {
//    	if(true){throw new BuildException("-------------");}
        if (classname == null) {
            throw new BuildException("you must specify type or classname");
        }

        Class f = null;
        try {
            if (loader == null) {
                f = Class.forName(classname);
            } else {
                f = Class.forName(classname, true, loader);
            }
        } catch (ClassNotFoundException e) {
            throw new BuildException(e);
        }

        Object o = null;
        try {
            o = f.newInstance();
        } catch (InstantiationException e) {
            throw new BuildException(e);
        } catch (IllegalAccessException e) {
            throw new BuildException(e);
        }

        if (!(o instanceof JUnitResultFormatter)) {
            throw new BuildException(classname
                + " is not a JUnitResultFormatter");
        }

        JUnitResultFormatter r = (JUnitResultFormatter) o;

        if (useFile && outFile != null) {
            try {
                out = new FileOutputStream(outFile);
            } catch (java.io.IOException e) {
                throw new BuildException(e);
            }
        }
        r.setOutput(out);
        
        return r;
    }

    /**
     * <p> Enumerated attribute with the values "plain", "xml" and "brief".
     *
     * <p> Use to enumerate options for <code>type</code> attribute.
     */
    public static class TypeAttribute extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"plain", "xml", "brief","xml_ext"};
        }
    }
}
