package org.gomicro.jmeter.xml.ext;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.BooleanPropertyEditor;
import org.apache.jmeter.testbeans.gui.TextAreaEditor;

public class XMLAssertBeanInfo extends BeanInfoSupport {

	/**
	 * Constructor which creates property group and creates UI.
	 */
	public XMLAssertBeanInfo() {
		super(XMLAssert.class);
		PropertyDescriptor p;

		p = property("ignoreEmptyNodes");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, Boolean.TRUE);
		p.setPropertyEditorClass(BooleanPropertyEditor.class);

		p = property("ignoreNamespacePrefixes");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, Boolean.TRUE);
		p.setPropertyEditorClass(BooleanPropertyEditor.class);

		p = property("ignoreChildNodeListOrder");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, Boolean.TRUE);
		p.setPropertyEditorClass(BooleanPropertyEditor.class);

		createPropertyGroup("configurations",
				new String[] { "ignoreEmptyNodes", "ignoreNamespacePrefixes", "ignoreChildNodeListOrder" });

		p = property("expectedXMLResult");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");
		p.setPropertyEditorClass(TextAreaEditor.class);

		p = property("currentXMLResult");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");
		p.setPropertyEditorClass(TextAreaEditor.class);

		createPropertyGroup("variables", new String[] { "expectedXMLResult", "currentXMLResult" });

	}

}