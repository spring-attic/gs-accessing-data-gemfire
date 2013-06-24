package hello;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.gemfire.mapping.Region;

@Region("hello")
public class Person {

	@Id public String name;
	public int age;

	@PersistenceConstructor
	public Person() { 
		this.name = "";
	}
	
	public Person(String name, int age) {
		this();
		this.name = name; 
		this.age = age;
	}
	
	@Override
	public String toString() {
		return name + " is " + age + " years old.";
	}

}
