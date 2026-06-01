-- 教学任务合班改造：Class_ID 由单值 INT 改为 Class_Ids 逗号分隔字符串
-- 执行前请备份数据库

ALTER TABLE teaching_task DROP FOREIGN KEY fk_task_class;

ALTER TABLE teaching_task
    CHANGE COLUMN Class_ID Class_Ids VARCHAR(255) NOT NULL COMMENT '逗号分隔的班级ID列表，如 1,2,3';
