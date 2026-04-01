-- ================================================================
-- Contact Widget Chatbot - Database Migration Script
-- ================================================================
-- This script creates the necessary tables for the contact widget
-- chatbot feature with AI integration.
-- ================================================================

-- Create chat_sessions table
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    session_id VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create chat_messages table
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'USER, ASSISTANT, SYSTEM',
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role (role),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Sample Data (Optional - for testing)
-- ================================================================

-- Note: Sessions and messages are created dynamically through the API
-- No sample data is needed for production use

-- ================================================================
-- Verification Queries
-- ================================================================

-- Check if tables were created successfully
-- SELECT TABLE_NAME, TABLE_ROWS FROM information_schema.TABLES
-- WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN ('chat_sessions', 'chat_messages');

-- View table structures
-- DESCRIBE chat_sessions;
-- DESCRIBE chat_messages;

-- ================================================================
-- Rollback Commands (Use with caution!)
-- ================================================================

-- DROP TABLE IF EXISTS chat_messages;
-- DROP TABLE IF EXISTS chat_sessions;
