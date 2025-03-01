package com.swyp.global.config;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseInitializer {

    private final DataSource dataSource;

    // DataSource를 주입받습니다.
    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Spring 애플리케이션 시작 시 SQL을 실행합니다.
    @PostConstruct
    public void init() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // 외래 키 제약 조건 및 컬럼 수정하는 SQL 쿼리들
            String[] sqls = {
                "ALTER TABLE goal_achievements " +
                    "ADD CONSTRAINT fk_goal_id FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL;",
                
                "ALTER TABLE goal_days " +
                    "ADD CONSTRAINT fk_goal_days_goal_id FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE;",
                
                "ALTER TABLE goal_achievement_log " +
                    "ADD CONSTRAINT fk_goal_achievement_log_goal_id FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE;",
                
                "ALTER TABLE point_history " +
                    "ADD CONSTRAINT fk_point_history_goal_id FOREIGN KEY (related_goal_id) REFERENCES goals(id) ON DELETE SET NULL;",
                
                "ALTER TABLE goals " +
                    "MODIFY COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;",
                
                "ALTER TABLE goals " +
                    "MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;"
            };

            // SQL 실행
            for (String sql : sqls) {
                statement.executeUpdate(sql);
                System.out.println("Executed SQL: " + sql);
            }

        } catch (Exception e) {
            e.printStackTrace(); // 오류가 발생하면 스택 트레이스를 출력합니다.
        }
    }
}
