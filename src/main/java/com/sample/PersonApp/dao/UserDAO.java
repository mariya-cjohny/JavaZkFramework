package com.sample.PersonApp.dao;

import org.jooq.DSLContext;
import com.sample.PersonApp.config.JooqConfig;
import com.sample.PersonApp.dto.UserDTO;

import static com.sample.jooq.generated.tables.Users.USERS;

public class UserDAO {

    private DSLContext dsl = JooqConfig.getDSLContext();

    public UserDTO findByUsername(String username) {
        return dsl.selectFrom(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOneInto(UserDTO.class);
    }

    public void insert(UserDTO user) {
        dsl.insertInto(USERS)
                .set(USERS.USERNAME, user.getUsername())
                .set(USERS.PASSWORD, user.getPassword())
                .set(USERS.ROLE, user.getRole())
                .execute();
    }
}
