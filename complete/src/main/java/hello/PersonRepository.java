package hello;

import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, String> {

    Person findByName(String name);

    Iterable<Person> findByAgeGreaterThan(int age);

    Iterable<Person> findByAgeLessThan(int age);

    Iterable<Person> findByAgeGreaterThanAndAgeLessThan(int age1, int age2);
}
