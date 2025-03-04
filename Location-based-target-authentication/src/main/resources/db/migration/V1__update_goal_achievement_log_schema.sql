-- Rename user_id column to auth_user_id in goal_achievement_log table
ALTER TABLE goal_achievement_log 
CHANGE COLUMN user_id auth_user_id BIGINT NOT NULL;

-- Add foreign key constraint
ALTER TABLE goal_achievement_log
ADD CONSTRAINT fk_goal_achievement_log_auth_user
FOREIGN KEY (auth_user_id) REFERENCES users(id);
