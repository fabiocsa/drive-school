-- ============================================
-- 驾校报名与学员管理系统 - 初始化数据
-- 用户账号由 DataInitializer 通过 BCrypt 初始化
-- ============================================

USE drive_school;

-- 车型
INSERT IGNORE INTO `vehicle_type` (`name`, `min_age`, `subject_hours_json`) VALUES
('C1', 18, '{"phase1":12,"phase2":16,"phase3":24,"phase4":10}'),
('C2', 18, '{"phase1":12,"phase2":14,"phase3":22,"phase4":10}'),
('B2', 20, '{"phase1":14,"phase2":20,"phase3":30,"phase4":12}');

-- 科目
INSERT IGNORE INTO `subject` (`name`, `sort_order`, `exam_fee`) VALUES
('科目一', 1, 100.00),
('科目二', 2, 200.00),
('科目三', 3, 300.00),
('科目四', 4, 100.00);

-- 考场
INSERT IGNORE INTO `exam_location` (`name`, `address`, `capacity`, `contact`) VALUES
('第一考场', '市区驾考中心A区', 100, '王主任 13811111111'),
('第二考场', '市区驾考中心B区', 80, '刘主任 13822222222');

-- 费用标准
INSERT IGNORE INTO `fee_standard` (`item_name`, `fee_type`, `amount`) VALUES
('报名费', 'REGISTRATION', 3000.00),
('科目一考试费', 'EXAM', 100.00),
('科目二考试费', 'EXAM', 200.00),
('科目三考试费', 'EXAM', 300.00),
('科目四考试费', 'EXAM', 100.00),
('补考费', 'RETAKE', 50.00);
