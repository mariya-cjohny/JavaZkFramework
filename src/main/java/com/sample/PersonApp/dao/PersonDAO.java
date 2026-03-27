package com.sample.PersonApp.dao;

import org.jooq.DSLContext;
import java.util.List;

import com.sample.PersonApp.config.JooqConfig;
import com.sample.PersonApp.dto.PersonDTO;
import static com.sample.jooq.generated.tables.Person.PERSON;

public class PersonDAO {

    private DSLContext dsl = JooqConfig.getDSLContext();

    public List<PersonDTO> getAll() {
        return dsl
                .selectFrom(PERSON)
                .fetchInto(PersonDTO.class);
    }

    public void insert(PersonDTO p) {
        dsl.insertInto(PERSON)
                .set(PERSON.NAME, p.getName())
                .set(PERSON.AGE, p.getAge())
                .set(PERSON.SALARY, p.getSalary())
                .execute();
    }

    public void update(PersonDTO p) {
        dsl.update(PERSON)
                .set(PERSON.NAME, p.getName())
                .set(PERSON.AGE, p.getAge())
                .set(PERSON.SALARY, p.getSalary())
                .where(PERSON.ID.eq(p.getId()))
                .execute();
    }

    public void delete(int id) {
        dsl.deleteFrom(PERSON)
                .where(PERSON.ID.eq(id))
                .execute();
    }
}
