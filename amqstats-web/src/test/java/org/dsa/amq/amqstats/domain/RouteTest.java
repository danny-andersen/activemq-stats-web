package org.dsa.amq.amqstats.domain;

import javax.management.Attribute;
import javax.management.AttributeList;

import junit.framework.TestCase;

import org.dsa.amq.amqstats.service.UriEnrichmentFactoryService;

public class RouteTest extends TestCase {

	public void testGetAttrsBoolean() {
		Route route = new Route(new UriEnrichmentFactoryService());
		route.addAttribute(new NameValueAttr("uri-dest1", "file:///dest"));
		route.addAttribute(new NameValueAttr("uri-source", "file:///dest1"));
		route.addAttribute(new NameValueAttr("avocado", "file:///dest1"));
		route.addAttribute(new NameValueAttr("zebra", "file:///dest1"));
		NameValueAttr[] nvs = route.getAttrs(true);
		assertEquals(4, nvs.length);
		assertTrue(nvs[0].name.compareTo("uri-source") == 0);
		assertTrue(nvs[1].name.compareTo("uri-dest1") == 0);
		assertTrue(nvs[2].name.compareTo("avocado") == 0);
		assertTrue(nvs[3].name.compareTo("zebra") == 0);
	}

	public void testAddDescAttrs1() {
		Route route = new Route(new UriEnrichmentFactoryService());
		AttributeList attrs = new AttributeList();
		String desc = "EventDrivenConsumerRoute[Endpoint[file:///home/danny/ActiveMQ/fromdir] -> Instrumentation:route[DelegateAsync[UnitOfWork(RouteContextProcessor[Channel[choice{when Simple: ${file:ext} == \"fred\": Channel[sendTo(Endpoint[file:///home/danny/ActiveMQ/filterDir])], when Simple: ${file:onlyname} == ${file:onlyname.noext}: Channel[sendTo(Endpoint[file:///home/danny/ActiveMQ/filterNoExtDir])], ])]}]])]]]";
		// We should get 2 dest-urs
		attrs.add(new Attribute(Route.DESC, desc));
		route.addAttrs(attrs);
		NameValueAttr[] nvs = route.getAttrs();
		assertEquals(2, nvs.length);
		for (NameValueAttr nv : nvs) {
			if (nv.name.startsWith(Route.DEST_URI)) {
				if (nv.value.contains("filterDir")) {
					assertTrue("Didn't contain fred: " + nv.value, nv.value.contains("fred"));
				} else if (nv.value.contains("filterNoExtDir")) {
					assertTrue("Didn't contain expected value: " + nv.value, nv.value
							.compareTo("file:///home/danny/ActiveMQ/filterNoExtDir") == 0);
				} else {
					fail("Unexpected value: " + nv.value);
				}
			}
		}
	}

	public void testAddDescAttrs2() {
		Route route = new Route(new UriEnrichmentFactoryService());
		AttributeList attrs = new AttributeList();
		String desc = "EventDrivenConsumerRoute[Endpoint[file:///home/danny/ActiveMQ/fromdir] -> Instrumentation:route[DelegateAsync[UnitOfWork(RouteContextProcessor[Channel[choice{when Simple: ${file:ext} == \"fred\": Channel[sendTo(Endpoint[file:///home/danny/ActiveMQ/filterDir])], when Simple: ${file:ext} == \"joe\": Channel[sendTo(Endpoint[file:///home/danny/ActiveMQ/filterDir])], when Simple: ${file:ext} == \"jane\": Channel[sendTo(Endpoint[file:///home/danny/ActiveMQ/filterDir])], when Simple: ${file:onlyname} == ${file:onlyname.noext}: Channel[sendTo(Endpoint[file:///home/danny/ActiveMQ/filterNoExtDir])], otherwise: Channel[sendTo(Endpoint[file:///home/danny/ActiveMQ/todir])]}]])]]]";
		// We should get 3 dest-urs
		attrs.add(new Attribute(Route.DESC, desc));
		route.addAttrs(attrs);
		NameValueAttr[] nvs = route.getAttrs();
		assertEquals(3, nvs.length);
		for (NameValueAttr nv : nvs) {
			if (nv.name.startsWith(Route.DEST_URI)) {
				if (nv.value.contains("filterDir")) {
					assertTrue("Didn't contain fred: " + nv.value, nv.value.contains("fred"));
					assertTrue("Didn't contain joe: " + nv.value, nv.value.contains("joe"));
					assertTrue("Didn't contain jane: " + nv.value, nv.value.contains("jane"));
				} else if (nv.value.contains("filterNoExtDir")) {
					assertTrue("Didn't contain expected value: " + nv.value, nv.value
							.compareTo("file:///home/danny/ActiveMQ/filterNoExtDir") == 0);
				} else if (nv.value.contains("todir")) {
					assertTrue("Didn't contain expected value" + nv.value, nv.value
							.compareTo("file:///home/danny/ActiveMQ/todir") == 0);
				} else {
					fail("Unexpected value: " + nv.value);
				}
			}
		}
	}

	public void testAddAttr() {
		Route route = new Route(new UriEnrichmentFactoryService());
		route.addAttribute(new NameValueAttr("uri-dest1", "file:///dest",
				"fred"));
		route.addAttribute(new NameValueAttr("uri-dest2", "file:///dest", "joe"));
		route.addAttribute(new NameValueAttr("uri-dest3", "file:///dest2",
				"jane"));
		route.addAttribute(new NameValueAttr("uri-dest4", "file:///dest3", null));
		NameValueAttr[] nvs = route.getAttrs();
		assertEquals(3, nvs.length);
		for (NameValueAttr nv : nvs) {
			if (nv.name.startsWith(Route.DEST_URI)) {
				if (nv.value.contains("fred")) {
					assertTrue(nv.value,
							nv.value.compareTo("fred,joe:file:///dest") == 0);
				} else if (nv.value.contains("jane")) {
					assertTrue(nv.value,
							nv.value.compareTo("jane:file:///dest2") == 0);
				} else if (nv.value.contains("dest3")) {
					assertTrue(nv.value,
							nv.value.compareTo("file:///dest3") == 0);
				}
			} else {
				fail("Unexpected attribute: " + nv.name);
			}
		}

	}
}
