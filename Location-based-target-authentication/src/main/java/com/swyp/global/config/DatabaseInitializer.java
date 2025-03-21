package com.swyp.global.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        // 데이터베이스 존재 확인 및 생성
        try {
            // 기본 MySQL 서버에 연결 (데이터베이스 지정 없이)
            String url = "jdbc:mysql://158.180.87.205:3306?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
            String username = "root";
            String password = "rootroot";
            
            // MySQL 드라이버 로드
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC 드라이버를 찾을 수 없습니다: " + e.getMessage());
            }
            
            // MySQL 서버에 직접 연결
            try (Connection rootConnection = java.sql.DriverManager.getConnection(url, username, password);
                 Statement rootStatement = rootConnection.createStatement()) {
                
                // 데이터베이스 존재 여부 확인
                boolean dbExists = false;
                try (ResultSet rs = rootStatement.executeQuery("SHOW DATABASES LIKE 'SWYP8'")) {
                    dbExists = rs.next();
                }
                
                // 데이터베이스가 없으면 생성
                if (!dbExists) {
                    System.out.println("데이터베이스 SWYP8이 존재하지 않습니다. 새로 생성합니다...");
                    rootStatement.executeUpdate("CREATE DATABASE SWYP8 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                    rootStatement.executeUpdate("GRANT ALL PRIVILEGES ON SWYP8.* TO 'root'@'%'");
                    rootStatement.executeUpdate("FLUSH PRIVILEGES");
                    System.out.println("데이터베이스 SWYP8이 성공적으로 생성되었습니다.");
                } else {
                    System.out.println("데이터베이스 SWYP8이 이미 존재합니다.");
                }
            } catch (SQLException e) {
                System.err.println("데이터베이스 생성 중 오류: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("데이터베이스 초기화 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }

        // 기존 테이블 컬럼 수정 작업
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
