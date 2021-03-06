// Copyright (c) 2003-2014, Jodd Team (jodd.org). All Rights Reserved.

package jodd.json;

import jodd.json.mock.Address;
import jodd.json.mock.Employee;
import jodd.json.mock.Friend;
import jodd.json.mock.Hill;
import jodd.json.mock.Mountain;
import jodd.json.mock.Network;
import jodd.json.mock.Person;
import jodd.json.mock.Phone;
import jodd.json.mock.Spiderman;
import jodd.json.mock.Surfer;
import jodd.json.mock.TestClass2;
import jodd.json.mock.TestClass3;
import jodd.json.mock.Zipcode;
import jodd.json.model.ListContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JSONSerializationTest {

	private Person jodder;
	private Person modesty;
	private Map colors;
	private List people;
	private Network network;
	private Zipcode pedroZip;
	private Employee dilbert;

	@Before
	@SuppressWarnings({"unchecked"})
	public void setUp() {
		DataCreator dataCreator = new DataCreator();
		pedroZip = new Zipcode("848485");
		Person pedro = dataCreator.createPedro();
		jodder = dataCreator.createJodder();
		modesty = dataCreator.createModesty();
		colors = dataCreator.createColorMap();

		people = new ArrayList();
		people.add(jodder);
		people.add(modesty);
		people.add(pedro);

		dilbert = dataCreator.createDilbert();

		network = dataCreator.createNetwork("My Network", jodder, modesty);
	}

	@After
	public void tearDown() {
		JoddJson.classMetadataName = null;
	}

	@Test
	public void testObject() {
		JoddJson.classMetadataName = "class";
		JsonSerializer serializer = new JsonSerializer();

		String jodderJson = serializer.serialize(jodder);

		assertStringValue(Person.class.getName(), jodderJson);
		assertAttribute("firstname", jodderJson);
		assertStringValue("Igor", jodderJson);
		assertAttribute("lastname", jodderJson);
		assertStringValue("Spasic", jodderJson);
		assertAttribute("work", jodderJson);
		assertAttribute("home", jodderJson);
		assertAttribute("street", jodderJson);
		assertStringValue(Address.class.getName(), jodderJson);
		assertAttribute("zipcode", jodderJson);
		assertStringValue(Zipcode.class.getName(), jodderJson);
		assertAttributeMissing("person", jodderJson);

		assertAttributeMissing("phones", jodderJson);
		assertStringValueMissing(Phone.class.getName(), jodderJson);
		assertAttributeMissing("hobbies", jodderJson);

		JsonSerializer jdrSerializer = new JsonSerializer();
		jdrSerializer.exclude("home", "work");
		String modestyJson = jdrSerializer.serialize(modesty);
		assertStringValue(Person.class.getName(), modestyJson);
		assertAttribute("firstname", modestyJson);
		assertStringValue("Modesty", modestyJson);
		assertAttribute("lastname", modestyJson);
		assertStringValue("Blase", modestyJson);
		assertAttribute("birthdate", modestyJson);

		assertStringValueMissing(Address.class.getName(), modestyJson);
		assertAttributeMissing("work", modestyJson);
		assertAttributeMissing("home", modestyJson);
		assertAttributeMissing("street", modestyJson);
		assertAttributeMissing("city", modestyJson);
		assertAttributeMissing("state", modestyJson);
		assertStringValueMissing(Zipcode.class.getName(), modestyJson);
		assertAttributeMissing("zipcode", modestyJson);
		assertStringValueMissing(Phone.class.getName(), modestyJson);
		assertAttributeMissing("hobbies", modestyJson);
		assertAttributeMissing("person", modestyJson);

		serializer.exclude("home.zipcode", "work.zipcode");

		String json2 = serializer.serialize(jodder);
		assertStringValue(Person.class.getName(), json2);
		assertAttribute("work", json2);
		assertAttribute("home", json2);
		assertAttribute("street", json2);
		assertStringValue(Address.class.getName(), json2);
		assertAttributeMissing("zipcode", json2);
		assertAttributeMissing("phones", json2);
		assertStringValueMissing(Zipcode.class.getName(), json2);
		assertStringValueMissing(Phone.class.getName(), json2);
		assertAttributeMissing("hobbies", json2);
		assertAttributeMissing("type", json2);
		assertStringValueMissing("PAGER", json2);

		serializer.include("hobbies").exclude(true, "phones.areaCode", "phones.exchange", "phones.number");

		String json3 = serializer.serialize(jodder);
		assertStringValue(Person.class.getName(), json3);
		assertAttribute("work", json3);
		assertAttribute("home", json3);
		assertAttribute("street", json3);
		assertStringValue(Address.class.getName(), json3);
		assertAttribute("phones", json3);
		assertAttribute("phoneNumber", json3);
		assertStringValue(Phone.class.getName(), json3);
		assertAttribute("hobbies", json3);

		assertAttributeMissing("zipcode", json3);
		assertAttributeMissing(Zipcode.class.getName(), json3);
		assertAttributeMissing("areaCode", json3);
		assertAttributeMissing("exchange", json3);
		assertAttributeMissing("number", json3);
		assertAttribute("type", json3);
		assertStringValue("PAGER", json3);

		assertTrue(json3.startsWith("{"));
		assertTrue(json3.endsWith("}"));
	}


	@Test
	public void testMap() {
		JsonSerializer serializer = new JsonSerializer();
		String colorsJson = serializer.serialize(colors);
		for (Object o : colors.entrySet()) {
			Map.Entry entry = (Map.Entry) o;
			assertAttribute(entry.getKey().toString(), colorsJson);
			assertStringValue(entry.getValue().toString(), colorsJson);
		}

		assertTrue(colorsJson.startsWith("{"));
		assertTrue(colorsJson.endsWith("}"));

		colors.put(null, "#aaaaaa");
		colors.put("orange", null);

		String json = serializer.serialize(colors);

		assertTrue(json.contains("null:"));
		assertStringValue("#aaaaaa", json);
		assertAttribute("orange", json);
		assertTrue(json.contains(":null"));
	}

	@Test
	public void testArray() {
		int[] array = new int[30];
		for (int i = 0; i < array.length; i++) {
			array[i] = i;
		}

		String json = new JsonSerializer().serialize(array);

		for (int i = 0; i < array.length; i++) {
			assertNumber(i, json);
		}

		assertFalse(json.contains("\""));
		assertFalse(json.contains("\'"));
	}

	@Test
	public void testCollection() {
		JsonSerializer serializer = new JsonSerializer();
		String colorsJson = serializer.serialize(colors.values());
		for (Object o : colors.entrySet()) {
			Map.Entry entry = (Map.Entry) o;
			assertAttributeMissing(entry.getKey().toString(), colorsJson);
			assertStringValue(entry.getValue().toString(), colorsJson);
		}
		assertTrue(colorsJson.startsWith("["));
		assertTrue(colorsJson.endsWith("]"));
	}

	@Test
	public void testString() {
		assertSerializedTo("Hello", "\"Hello\"");
		assertSerializedTo("Hello\nWorld", "\"Hello\\nWorld\"");
		assertSerializedTo("Hello 'Big Boy'", "\"Hello 'Big Boy'\"");
		assertSerializedTo("Fly \"you fools\"", "\"Fly \\\"you fools\\\"\"");
		assertSerializedTo("</script>", "\"<\\/script>\"");
	}


	@Test
	public void testListOfObjects() {
		JoddJson.classMetadataName = "class";

		JsonSerializer serializer = new JsonSerializer();
		String peopleJson = serializer.serialize(people);

		assertStringValue(Person.class.getName(), peopleJson);
		assertAttribute("firstname", peopleJson);
		assertStringValue("Igor", peopleJson);
		assertStringValue("Modesty", peopleJson);
		assertAttribute("lastname", peopleJson);
		assertStringValue("Spasic", peopleJson);
		assertStringValue(Address.class.getName(), peopleJson);
		assertStringValue("Pedro", peopleJson);
		assertStringValue("Sanchez", peopleJson);

		serializer = new JsonSerializer().exclude("home", "work");
		peopleJson = serializer.serialize(people);

		assertStringValue(Person.class.getName(), peopleJson);
		assertAttribute("firstname", peopleJson);
		assertStringValue("Igor", peopleJson);
		assertStringValue("Modesty", peopleJson);
		assertAttribute("lastname", peopleJson);
		assertStringValue("Spasic", peopleJson);
		assertStringValueMissing(Address.class.getName(), peopleJson);
	}


	@Test
	public void testDeepIncludes() {
		JsonSerializer serializer = new JsonSerializer();
		String peopleJson = serializer.include("people.hobbies").serialize(network);

		assertAttribute("name", peopleJson);
		assertStringValue("My Network", peopleJson);
		assertAttribute("firstname", peopleJson);
		assertStringValue("Igor", peopleJson);
		assertStringValue("Modesty", peopleJson);
		assertAttribute("lastname", peopleJson);
		assertStringValue("Spasic", peopleJson);
		assertAttribute("hobbies", peopleJson);
		assertStringValue("read", peopleJson);
	}

	@Test
	public void testDates() {
		JsonSerializer serializer = new JsonSerializer();
		String peopleJson = serializer.exclude("home", "work").serialize(jodder);
		assertAttribute("firstname", peopleJson);
		assertStringValue("Igor", peopleJson);
		assertNumber(jodder.getBirthdate().getTime(), peopleJson);
		assertStringValueMissing("java.util.Date", peopleJson);
	}

	@Test
	public void testSimpleShallowWithListInMap() {
		JsonSerializer serializer = new JsonSerializer();
		Map wrapper = new HashMap();
		wrapper.put("name", "Joe Blow");
		wrapper.put("people", people);
		String peopleJson = serializer.serialize(wrapper);
		assertFalse(peopleJson.contains("["));

		serializer.include("people.*");
		peopleJson = serializer.serialize(wrapper);
		assertTrue(peopleJson.contains("["));
	}

	@Test
	public void testSimpleShallowWithListInObject() {
		JsonSerializer serializer = new JsonSerializer();
		ListContainer wrapper = new ListContainer();
		wrapper.setName("Joe Blow");
		wrapper.setPeople(people);
		String peopleJson = serializer.serialize(wrapper);
		assertFalse(peopleJson.contains("["));
	}

	@Test
	public void testSetIncludes() {
		JsonSerializer serializer = new JsonSerializer();
		serializer.include("people.hobbies", "phones", "home", "people.resume");

		assertEquals(4, serializer.rules.totalRules());
		assertEquals("[people.hobbies]", serializer.rules.getRule(0).toString());
		assertEquals("[phones]", serializer.rules.getRule(1).toString());
		assertEquals("[home]", serializer.rules.getRule(2).toString());
		assertEquals("[people.resume]", serializer.rules.getRule(3).toString());
	}

	@Test
	public void testDeepSerialization() {
		JsonSerializer serializer = new JsonSerializer();
		String peopleJson = serializer.deep(true).serialize(network);

		assertAttribute("name", peopleJson);
		assertStringValue("My Network", peopleJson);
		assertAttribute("firstname", peopleJson);
		assertStringValue("Igor", peopleJson);
		assertStringValue("Modesty", peopleJson);
		assertAttribute("lastname", peopleJson);
		assertStringValue("Spasic", peopleJson);
		assertAttributeMissing("hobbies", peopleJson); // annotation explicitly excludes this
		assertStringValueMissing("read", peopleJson);
	}

	@Test
	public void testDeepSerializationWithIncludeOverrides() {
		JsonSerializer serializer = new JsonSerializer();
		String peopleJson = serializer.include("people.hobbies").deep(true).serialize(network);

		assertAttribute("firstname", peopleJson);
		assertStringValue("Igor", peopleJson);
		assertAttribute("hobbies", peopleJson);
		assertStringValue("read", peopleJson);
		assertStringValue("run", peopleJson);
		assertStringValue("code", peopleJson);
	}

	@Test
	public void testDeepSerializationWithExcludes() {
		JsonSerializer serializer = new JsonSerializer();
		String peopleJson = serializer.exclude("people.work").deep(true).serialize(network);

		assertAttribute("firstname", peopleJson);
		assertStringValue("Igor", peopleJson);
		assertAttributeMissing("work", peopleJson);
		assertStringValue("173 Hackers Drive", peopleJson);
		assertAttribute("home", peopleJson);
		assertAttribute("phones", peopleJson);
	}

	@Test
	public void testDeepSerializationCycles() {
		JsonSerializer serializer = new JsonSerializer();
		String json = serializer.deep(true).serialize(people);

		assertAttribute("zipcode", json);
		assertEquals(2, occurs(pedroZip.getZipcode(), json));
		assertAttributeMissing("person", json);
	}

	@Test
	public void testSerializeSuperClass() {
		JsonSerializer serializer = new JsonSerializer();
		String json = serializer.serialize(dilbert);

		assertAttribute("company", json);
		assertStringValue("Initech", json);
		assertAttribute("firstname", json);
		assertStringValue("Dilbert", json);
	}

	@Test
	public void testSerializePublicFields() {
		Spiderman spiderman = new Spiderman();

		JsonSerializer serializer = new JsonSerializer();
		String json = serializer.serialize(spiderman);

		assertAttribute("spideySense", json);
		assertAttribute("superpower", json);
		assertStringValue("Creates web", json);
	}

	@Test
	public void testExcludingPublicFields() {
		Spiderman spiderman = new Spiderman();

		String json = new JsonSerializer().exclude("superpower").serialize(spiderman);

		assertAttributeMissing("superpower", json);
		assertAttribute("spideySense", json);
	}


	@Test
	public void testWildcards() {
		JoddJson.classMetadataName = "class";
		JsonSerializer serializer = new JsonSerializer();
		String json = serializer.include("phones").exclude("*.class").serialize(jodder);

		assertAttributeMissing("class", json);
		assertAttribute("phones", json);
		assertAttributeMissing("hobbies", json);
	}

	@Test
	public void testExclude() {
		String json = new JsonSerializer().serialize(jodder);

		assertAttribute("firstname", json);
		assertAttributeMissing("number", json);
		assertAttributeMissing("exchange", json);
		assertAttributeMissing("areaCode", json);

		json = new JsonSerializer().include("phones").serialize(jodder);

		assertAttribute("firstname", json);
		assertAttribute("number", json);
		assertAttribute("exchange", json);
		assertAttribute("areaCode", json);

		json = new JsonSerializer().include("phones").exclude("phones.areaCode").serialize(jodder);

		assertAttribute("firstname", json);
		assertAttribute("number", json);
		assertAttribute("exchange", json);
		assertAttributeMissing("areaCode", json);
	}

	@Test
	public void testExcludeAll() {
		JsonSerializer serializer = new JsonSerializer();
		String json = serializer.exclude("*").serialize(jodder);

		assertEquals("{}", json);
	}

	@Test
	public void testMixedWildcards() {
		JsonSerializer serializer = new JsonSerializer();
		serializer.include("firstname", "lastname").exclude("*");
		String json = serializer.serialize(jodder);

		assertAttribute("firstname", json);
		assertStringValue("Igor", json);
		assertAttribute("lastname", json);
		assertStringValue("Spasic", json);
		assertAttributeMissing("class", json);
		assertAttributeMissing("phones", json);
		assertAttributeMissing("birthdate", json);

		serializer = new JsonSerializer();
		serializer.include("firstname", "lastname", "phones.areaCode", "phones.exchange", "phones.number").exclude("*");
		json = serializer.serialize(jodder);

		assertAttribute("firstname", json);
		assertStringValue("Igor", json);
		assertAttribute("lastname", json);
		assertStringValue("Spasic", json);
		assertAttributeMissing("class", json);
		assertAttribute("phones", json);
		assertAttributeMissing("birthdate", json);
	}

	@Test
	public void testCopyOnWriteList() {
		CopyOnWriteArrayList<Person> people = new CopyOnWriteArrayList<Person>();
		people.add(jodder);
		people.add(modesty);

		String json = new JsonSerializer().serialize(people);
		assertAttribute("firstname", json);
		assertStringValue("Igor", json);
		assertStringValue("Modesty", json);
	}

	@Test
	public void testAnnotations() {
		HashMap<String, TestClass3> map = new HashMap<String, TestClass3>();
		map.put("String1", new TestClass3());

		TestClass2 testElement = new TestClass2();
		testElement.setMapOfJustice(map);

		String json = new JsonSerializer().serialize(testElement);
		assertAttributeMissing("mapOfJustice", json);
		assertAttributeMissing("name", json);
		assertEquals(-1, json.indexOf("testName2"));

		json = new JsonSerializer().include("mapOfJustice").serialize(testElement);
		assertAttribute("mapOfJustice", json);

		// make sure the name property value is missing!  assertAttributeMissing( "name", json )
		// conflicts since mapOfJustice contains an object with name in it
		assertEquals(-1, json.indexOf("testName2"));
	}

	@Test
	public void testTransient() {
		TestClass2 testElement = new TestClass2();

		String json = new JsonSerializer().serialize(testElement);
		assertAttributeMissing("description", json);

		json = new JsonSerializer().include("description").serialize(testElement);
		assertAttribute("description", json);
	}

	@Test
	public void testSettersWithoutGettersAreMissing() {
		Friend friend = new Friend("Nugget", "Donkey Rider", "Slim");
		String json = new JsonSerializer().include("*").serialize(friend);
		assertAttribute("nicknames", json);
		assertAttributeMissing("nicknamesAsArray", json);
	}

	@Test
	public void testIncludesExcludes() throws FileNotFoundException {
		Surfer surfer = Surfer.createSurfer();

		String json = new JsonSerializer().serialize(surfer);

		assertAttribute("name", json);
		assertStringValue("jodd", json);
		assertAttribute("id", json);
		assertAttribute("split", json);
		assertAttribute("skill", json);
		assertAttribute("pipe", json);
		assertAttributeMissing("phones", json);

		// exclude pipe
		json = new JsonSerializer().excludeTypes(InputStream.class).serialize(surfer);

		assertAttribute("name", json);
		assertStringValue("jodd", json);
		assertAttribute("id", json);
		assertAttribute("split", json);
		assertAttribute("skill", json);
		assertAttributeMissing("pipe", json);
		assertAttributeMissing("phones", json);

		// exclude pipe (alt)

		json = new JsonSerializer().excludeTypes("*Stream").serialize(surfer);

		assertAttribute("name", json);
		assertStringValue("jodd", json);
		assertAttribute("id", json);
		assertAttribute("split", json);
		assertAttribute("skill", json);
		assertAttributeMissing("pipe", json);
		assertAttributeMissing("phones", json);

		// exclude s*, include phones
		json = new JsonSerializer().exclude("split").include("phones").excludeTypes("*Stream").serialize(surfer);

		assertAttribute("name", json);
		assertStringValue("jodd", json);
		assertAttribute("id", json);
		assertAttributeMissing("split", json);
		assertAttribute("skill", json);
		assertAttributeMissing("pipe", json);
		assertAttribute("phones", json);
		assertAttribute("exchange", json);

		json = new JsonSerializer().exclude("split").include("phones").exclude("phones.exchange").serialize(surfer);

		assertAttribute("phones", json);
		assertAttributeMissing("exchange", json);
	}

	@Test
	public void testSuperclass() {
		Hill hill = new Hill();
		hill.setHeight("qwe");
		hill.setName("aaa");

		String json = new JsonSerializer().serialize(hill);

		assertAttribute("height", json);
		assertAttributeMissing("name", json);

		Mountain mountain = new Mountain();
		mountain.setName("bbb");
		mountain.setHeight("123");
		mountain.setWild(true);

		JoddJson.serializationSubclassAware = false;

		JoddJson.annotationManager.reset();
		json = new JsonSerializer().serialize(mountain);

		assertAttribute("height", json);
		assertAttribute("name", json);
		assertAttribute("wild", json);

		JoddJson.serializationSubclassAware = true;

		JoddJson.annotationManager.reset();
		json = new JsonSerializer().serialize(mountain);

		assertAttribute("height", json);
		assertAttributeMissing("name", json);
		assertAttributeMissing("wild", json);
	}


	// ---------------------------------------------------------------- custom asserts


	private int occurs(String str, String json) {
		int current = 0;
		int count = 0;
		while (current >= 0) {
			current = json.indexOf(str, current);
			if (current > 0) {
				count++;
				current += str.length();
			}
		}
		return count;
	}

	private void assertAttributeMissing(String attribute, String json) {
		assertAttribute(attribute, json, false);
	}

	private void assertAttribute(String attribute, String json) {
		assertAttribute(attribute, json, true);
	}

	private void assertAttribute(String attribute, String json, boolean isPresent) {
		if (isPresent) {
			assertTrue(json.contains("\"" + attribute + "\":"));
		}
		else {
			assertFalse(json.contains("\"" + attribute + "\":"));
		}
	}

	private void assertStringValue(String value, String json, boolean isPresent) {
		if (isPresent) {
			assertTrue(json.contains("\"" + value + "\""));
		}
		else {
			assertFalse(json.contains("\"" + value + "\""));
		}
	}

	private void assertNumber(Number number, String json) {
		assertTrue(json.contains(number.toString()));
	}

	private void assertStringValueMissing(String value, String json) {
		assertStringValue(value, json, false);
	}

	private void assertStringValue(String value, String json) {
		assertStringValue(value, json, true);
	}

	private void assertSerializedTo(String original, String expected) {
		JsonSerializer serializer = new JsonSerializer();
		String json = serializer.serialize(original);
		assertEquals(expected, json);
	}


}