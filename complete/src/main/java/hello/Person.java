package hello;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.gemfire.mapping.annotation.Region;

import lombok.Getter;

@Region(value = "People")
public class Person implements Serializable {

	@Id
	@Getter
	private final String name;

	@Getter
	private final int age;

	@PersistenceConstructor
	public Person(String name, int age) {
		this.name = name;
		this.age = age;
	}

	@Override
	public String toString() {
		return String.format("%s is %d years old", getName(), getAge());
	}
}
