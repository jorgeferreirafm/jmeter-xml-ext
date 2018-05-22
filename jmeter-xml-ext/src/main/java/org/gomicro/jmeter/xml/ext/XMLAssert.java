package org.gomicro.jmeter.xml.ext;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLAssert extends AbstractTestElement implements Serializable, Assertion, TestBean {

	private static final long serialVersionUID = -8194996628237287220L;

	private Boolean ignoreEmptyNodes;

	private Boolean ignoreNamespacePrefixes;

	private Boolean ignoreChildNodeListOrder;

	private String expectedXMLResult;

	private String currentXMLResult;

	public String getExpectedXMLResult() {
		return expectedXMLResult;
	}

	public void setIgnoreChildNodeListOrder(Boolean ignoreChildNodeListOrder) {
		this.ignoreChildNodeListOrder = ignoreChildNodeListOrder;
	}

	public Boolean getIgnoreChildNodeListOrder() {
		return ignoreChildNodeListOrder;
	}

	public void setExpectedXMLResult(String expectedXMLResult) {
		this.expectedXMLResult = expectedXMLResult;
	}

	public String getCurrentXMLResult() {
		return currentXMLResult;
	}

	public void setCurrentXMLResult(String currentXMLResult) {
		this.currentXMLResult = currentXMLResult;
	}

	public void setIgnoreEmptyNodes(Boolean ignoreEmptyNodes) {
		this.ignoreEmptyNodes = ignoreEmptyNodes;
	}

	public Boolean getIgnoreEmptyNodes() {
		return ignoreEmptyNodes;
	}

	public void setIgnoreNamespacePrefixes(Boolean ignoreNamespacePrefixes) {
		this.ignoreNamespacePrefixes = ignoreNamespacePrefixes;
	}

	public Boolean getIgnoreNamespacePrefixes() {
		return ignoreNamespacePrefixes;
	}

	/**
	 * getResult
	 * 
	 */
	@Override
	public AssertionResult getResult(SampleResult response) {

		AssertionResult result = new AssertionResult(getName());
		// Note: initialised with error = failure = false
		try {
			String expectedResult = getExpectedXMLResult();

			String currentResult = getCurrentXMLResult();

			if (expectedResult.length() == 0) {
				return result.setResultForNull();
			}

			if (currentResult.length() == 0) {
				return result.setResultForNull();
			}

			String resultMessage = getResult(expectedResult, currentResult);
			if (resultMessage != null) {
				result.setFailure(true);
				result.setFailureMessage(resultMessage);
			}
		} catch (Exception ex) {
			result.setFailure(true);
			result.setFailureMessage(ex.getMessage());
		}
		return result;
	}

	public void normalizeDocument(Document document) {
		document.normalizeDocument();
		if (ignoreEmptyNodes) {
			removeEmptyNodes(document.getDocumentElement());
		}
	}

	public String getResult(String expectedResult, String currentResult)
			throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setCoalescing(true);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setIgnoringComments(true);
		DocumentBuilder db = dbf.newDocumentBuilder();

		Document doc1 = db.parse(new InputSource(new StringReader(expectedResult)));
		normalizeDocument(doc1);

		Document doc2 = db.parse(new InputSource(new StringReader(currentResult)));
		normalizeDocument(doc2);

		Diff diff = new Diff(doc1, doc2);
		List<Integer> filters = new ArrayList<>();
		if (ignoreNamespacePrefixes) {
			filters.add(DifferenceConstants.NAMESPACE_PREFIX_ID);
		}

		if (ignoreChildNodeListOrder) {
			filters.add(DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID);
		}

		if (filters.size() > 0) {
			diff.overrideDifferenceListener(new ExpectedDifferenceListener(filters));
		}

		DetailedDiff detDiff = new DetailedDiff(diff);
		List<?> differences = detDiff.getAllDifferences();
		if (differences.isEmpty()) {
			return null;
		}

		StringBuilder builder = new StringBuilder();
		for (Object difference : differences) {
			builder.append(difference);
			builder.append("\n");
		}

		return builder.toString();
	}

	public boolean isNodeEmpty(Node node) {
		return (node.getNodeType() == Node.ELEMENT_NODE) && (node.getChildNodes().getLength() == 0);
	}

	public boolean removeEmptyNodes(Node node) {
		NodeList nodeList = node.getChildNodes();
		if (isNodeEmpty(node)) {
			return true;
		}

		int total = nodeList.getLength();
		for (int i = 0; i < total; i++) {
			Node nd = nodeList.item(i);
			if (removeEmptyNodes(nd)) {
				node.removeChild(nd);
				i--;
				total--;
			}
		}

		if (isNodeEmpty(node)) {
			return true;
		}
		return false;
	}

	private class ExpectedDifferenceListener implements DifferenceListener {
		private final Set<Integer> expectedIds;

		private ExpectedDifferenceListener(int expectedIdValue) {
			this(new int[] { expectedIdValue });
		}

		private ExpectedDifferenceListener(int[] expectedIdValues) {
			this.expectedIds = new HashSet<Integer>(expectedIdValues.length);
			for (int expectedIdValue : expectedIdValues) {
				expectedIds.add(new Integer(expectedIdValue));
			}
		}

		private ExpectedDifferenceListener(List<Integer> expectedIdValues) {
			this.expectedIds = new HashSet<Integer>(expectedIdValues.size());
			for (int expectedIdValue : expectedIdValues) {
				expectedIds.add(new Integer(expectedIdValue));
			}
		}

		public int differenceFound(Difference difference) {
			if (expectedIds.contains(new Integer(difference.getId()))) {
				return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
			}
			return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;

		}

		public void skippedComparison(Node control, Node test) {

		}

	}

}