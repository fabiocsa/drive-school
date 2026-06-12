-- ============================================
-- 驾校报名与学员管理系统 - 数据库初始化脚本
-- 数据库 drive_school 由 JDBC URL 中 createDatabaseIfNotExist=true 自动创建
-- 以下全部使用 CREATE TABLE IF NOT EXISTS（幂等，重复启动不丢数据）
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(200) NOT NULL,
    `role` VARCHAR(20) NOT NULL COMMENT 'ROLE_STUDENT, ROLE_COACH, ROLE_ADMIN',
    `real_name` VARCHAR(50) DEFAULT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `enabled` TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 车型表
CREATE TABLE IF NOT EXISTS `vehicle_type` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(20) NOT NULL COMMENT 'C1, C2, B2等',
    `min_age` INT NOT NULL DEFAULT 18,
    `subject_hours_json` VARCHAR(500) DEFAULT NULL COMMENT '各科目最低学时JSON',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 科目表
CREATE TABLE IF NOT EXISTS `subject` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '科目一~科目四',
    `sort_order` INT NOT NULL,
    `exam_fee` DECIMAL(10,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 考场表
CREATE TABLE IF NOT EXISTS `exam_location` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `address` VARCHAR(200) DEFAULT NULL,
    `capacity` INT NOT NULL DEFAULT 50,
    `contact` VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 费用标准表
CREATE TABLE IF NOT EXISTS `fee_standard` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `item_name` VARCHAR(50) NOT NULL COMMENT '报名费/考试费/补考费',
    `fee_type` VARCHAR(30) NOT NULL COMMENT 'REGISTRATION/EXAM/RETAKE',
    `amount` DECIMAL(10,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 学员信息表
CREATE TABLE IF NOT EXISTS `student_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `id_card` VARCHAR(18) DEFAULT NULL,
    `address` VARCHAR(200) DEFAULT NULL,
    `vehicle_type_id` BIGINT DEFAULT NULL,
    `id_card_front_photo` VARCHAR(500) DEFAULT NULL,
    `id_card_back_photo` VARCHAR(500) DEFAULT NULL,
    `health_report_photo` VARCHAR(500) DEFAULT NULL,
    `photo` VARCHAR(500) DEFAULT NULL,
    `audit_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    `audit_remark` VARCHAR(500) DEFAULT NULL,
    `medical_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PASSED/FAILED',
    `coach_id` BIGINT DEFAULT NULL,
    `assign_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ASSIGNED',
    `cert_status` VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT 'NONE/WAITING/ISSUED',
    `audited_by` VARCHAR(50) DEFAULT NULL COMMENT '审核人用户名',
    `audited_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `registration_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_audit_status` (`audit_status`),
    KEY `idx_coach_id` (`coach_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 教练表
CREATE TABLE IF NOT EXISTS `coach` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `gender` VARCHAR(10) DEFAULT NULL,
    `vehicle_type_id` BIGINT DEFAULT NULL,
    `rating` INT NOT NULL DEFAULT 3 COMMENT '1-5',
    `schedule_json` VARCHAR(500) DEFAULT NULL COMMENT '档期JSON',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 学时记录表
CREATE TABLE IF NOT EXISTS `training_hour` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `student_id` BIGINT NOT NULL,
    `coach_id` BIGINT NOT NULL,
    `duration` DECIMAL(5,1) NOT NULL DEFAULT 0,
    `content` VARCHAR(500) DEFAULT NULL,
    `record_date` DATE NOT NULL,
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_coach_id` (`coach_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 约课表
CREATE TABLE IF NOT EXISTS `appointment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `student_id` BIGINT NOT NULL,
    `coach_id` BIGINT NOT NULL,
    `appointment_time` VARCHAR(50) NOT NULL COMMENT '如: 周一上午',
    `appointment_date` DATE NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/CANCELLED/COMPLETED',
    `cancel_reason` VARCHAR(500) DEFAULT NULL,
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_coach_id` (`coach_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 学习阶段表
CREATE TABLE IF NOT EXISTS `learning_phase` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `student_id` BIGINT NOT NULL,
    `current_phase` VARCHAR(20) NOT NULL DEFAULT 'PHASE1' COMMENT 'PHASE1/PHASE2/PHASE3/PHASE4/COMPLETED',
    `phase1_hours` DECIMAL(5,1) NOT NULL DEFAULT 0,
    `phase2_hours` DECIMAL(5,1) NOT NULL DEFAULT 0,
    `phase3_hours` DECIMAL(5,1) NOT NULL DEFAULT 0,
    `phase4_hours` DECIMAL(5,1) NOT NULL DEFAULT 0,
    `phase1_completed` TINYINT(1) NOT NULL DEFAULT 0,
    `phase2_completed` TINYINT(1) NOT NULL DEFAULT 0,
    `phase3_completed` TINYINT(1) NOT NULL DEFAULT 0,
    `phase4_completed` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 考试报名表
CREATE TABLE IF NOT EXISTS `exam_registration` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `student_id` BIGINT NOT NULL,
    `subject_id` BIGINT NOT NULL,
    `exam_location_id` BIGINT NOT NULL,
    `exam_date` DATE NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/COMPLETED',
    `score` DECIMAL(5,1) DEFAULT NULL,
    `is_passed` TINYINT(1) DEFAULT NULL,
    `retry_count` INT NOT NULL DEFAULT 0,
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 增量迁移（已有数据库升级用）
-- 重复启动时列已存在会报错，但 application-dev.yml 设了 continue-on-error: true，自动跳过
-- ============================================
ALTER TABLE `student_info` ADD COLUMN `audited_by` VARCHAR(50) DEFAULT NULL COMMENT '审核人用户名' AFTER `cert_status`;
ALTER TABLE `student_info` ADD COLUMN `audited_time` DATETIME DEFAULT NULL COMMENT '审核时间' AFTER `audited_by`;

-- 约课表：复合索引，加速"按教练+日期查询可用时间槽"并辅助防并发冲突
-- 覆盖 coach_id + appointment_date + status，避免全表扫描
ALTER TABLE `appointment` ADD INDEX `idx_coach_date_status` (`coach_id`, `appointment_date`, `status`);

-- 扩展 schedule_json 列以容纳含容量的新格式 (VARCHAR 500 → 1000)
ALTER TABLE `coach` MODIFY COLUMN `schedule_json` VARCHAR(1000) DEFAULT NULL COMMENT '档期JSON(含容量)';
