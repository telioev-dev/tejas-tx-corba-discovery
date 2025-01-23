package com.teliolabs.corba.data.repository;

import com.teliolabs.corba.config.DataSourceConfig;
import com.teliolabs.corba.data.dto.Circle;
import com.teliolabs.corba.data.exception.DataAccessException;
import com.teliolabs.corba.data.mapper.CircleEntityMapper;
import com.teliolabs.corba.data.queries.CircleQueries;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Log4j2
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CircleRepository {

    private static CircleRepository instance;

    // Public method to get the singleton instance
    public static CircleRepository getInstance() {
        if (instance == null) {
            synchronized (CircleRepository.class) {
                if (instance == null) {
                    instance = new CircleRepository();
                }
            }
        }
        return instance;
    }

    public List<Circle> findAllCircles() {
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(CircleQueries.SELECT_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            List<Circle> circles = new ArrayList<>();

            while (resultSet.next()) {
                circles.add(mapResultSetToManagedElementEntity(resultSet));
            }
            return circles;
        } catch (SQLException e) {
            log.error("Error fetching all managed elements", e);
            throw new DataAccessException("Error fetching all managed elements", e);
        }
    }


    public Optional<Circle> findCircleByNameAndVendor(String name, String vendor) {
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(CircleQueries.SELECT_BY_NAME_AND_VENDOR_SQL)) {

            statement.setString(1, name);
            statement.setString(2, vendor);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(CircleEntityMapper.getInstance().mapToEntity(resultSet));
                }
            }

        } catch (SQLException e) {
            log.error("Error finding circle by name: " + name + " and vendor: " + vendor, e);
            e.printStackTrace(); // Handle exception (log or rethrow as needed)
        }

        return Optional.empty();
    }

    private Circle mapResultSetToManagedElementEntity(ResultSet resultSet) throws SQLException {
        return CircleEntityMapper.getInstance().mapToEntity(resultSet);
    }
}