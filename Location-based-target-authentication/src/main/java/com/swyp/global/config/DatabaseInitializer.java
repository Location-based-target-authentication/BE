package com.swyp.global.config;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseInitializer {

    private final DataSource dataSource;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            
            String[] sqls = {
                // 컬럼 수정
                "ALTER TABLE goals MODIFY COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;",
                "ALTER TABLE goals MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;"
            };

            // SQL 실행
            for (String sql : sqls) {
                try {
                    statement.executeUpdate(sql);
                    System.out.println("Executed SQL: " + sql);
                } catch (Exception e) {
                    System.out.println("Failed to execute SQL: " + sql + " Error: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
