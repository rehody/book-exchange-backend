package org.prod.bookexchangebackend.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.prod.bookexchangebackend.model.PlatformUser;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class PlatformUserRowMapper implements RowMapper<PlatformUser> {

    @Override
    public PlatformUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PlatformUser(
                rs.getObject("id", UUID.class),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("phone_number"),
                rs.getString("location"),
                rs.getString("bio"),
                List.of(),
                rs.getBoolean("active"));
    }
}
