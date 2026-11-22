CREATE DATABASE IF NOT EXISTS gdpm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE gdpm;

CREATE TABLE IF NOT EXISTS major (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS school_class (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    major_id BIGINT NOT NULL,
    FOREIGN KEY (major_id) REFERENCES major (id)
);

CREATE TABLE IF NOT EXISTS cohort (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(80) NOT NULL,
    major_id BIGINT,
    class_id BIGINT,
    cohort VARCHAR(20),
    email VARCHAR(120),
    phone VARCHAR(30),
    enabled INT NOT NULL DEFAULT 1,
    FOREIGN KEY (major_id) REFERENCES major (id),
    FOREIGN KEY (class_id) REFERENCES school_class (id)
);

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user (id),
    FOREIGN KEY (role_id) REFERENCES sys_role (id)
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES sys_role (id),
    FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
);

CREATE TABLE IF NOT EXISTS sys_session (
    token_hash VARCHAR(64) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(80) NOT NULL,
    detail VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workflow_step (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    deadline TIMESTAMP NOT NULL,
    plagiarism_threshold DECIMAL(5, 2) NOT NULL DEFAULT 30,
    sort_order INT NOT NULL
);

CREATE TABLE IF NOT EXISTS topic_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    teacher_id BIGINT NOT NULL,
    major_id BIGINT NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '待审核',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewer_id BIGINT,
    review_comment VARCHAR(2000),
    revision_count INT NOT NULL DEFAULT 0,
    FOREIGN KEY (teacher_id) REFERENCES sys_user (id),
    FOREIGN KEY (major_id) REFERENCES major (id),
    FOREIGN KEY (reviewer_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS topic_selection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '待审核',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewer_id BIGINT,
    review_comment VARCHAR(2000),
    revision_count INT NOT NULL DEFAULT 0,
    UNIQUE (topic_id, student_id),
    FOREIGN KEY (topic_id) REFERENCES topic_info (id),
    FOREIGN KEY (student_id) REFERENCES sys_user (id),
    FOREIGN KEY (reviewer_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS student_assignment (
    student_id BIGINT PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES sys_user (id),
    FOREIGN KEY (topic_id) REFERENCES topic_info (id),
    FOREIGN KEY (teacher_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS file_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS task_book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    stage VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    file_id BIGINT NOT NULL,
    version INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '待审核',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewer_id BIGINT,
    review_comment VARCHAR(2000),
    reviewed_at TIMESTAMP NULL,
    revision_count INT NOT NULL DEFAULT 0,
    UNIQUE (student_id, stage, version),
    FOREIGN KEY (student_id) REFERENCES sys_user (id),
    FOREIGN KEY (teacher_id) REFERENCES sys_user (id),
    FOREIGN KEY (file_id) REFERENCES file_record (id),
    FOREIGN KEY (reviewer_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS proposal_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    stage VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    file_id BIGINT NOT NULL,
    version INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '待审核',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewer_id BIGINT,
    review_comment VARCHAR(2000),
    reviewed_at TIMESTAMP NULL,
    revision_count INT NOT NULL DEFAULT 0,
    UNIQUE (student_id, stage, version),
    FOREIGN KEY (student_id) REFERENCES sys_user (id),
    FOREIGN KEY (teacher_id) REFERENCES sys_user (id),
    FOREIGN KEY (file_id) REFERENCES file_record (id),
    FOREIGN KEY (reviewer_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS midterm_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    stage VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    file_id BIGINT NOT NULL,
    version INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '待审核',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewer_id BIGINT,
    review_comment VARCHAR(2000),
    reviewed_at TIMESTAMP NULL,
    revision_count INT NOT NULL DEFAULT 0,
    UNIQUE (student_id, stage, version),
    FOREIGN KEY (student_id) REFERENCES sys_user (id),
    FOREIGN KEY (teacher_id) REFERENCES sys_user (id),
    FOREIGN KEY (file_id) REFERENCES file_record (id),
    FOREIGN KEY (reviewer_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS thesis_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    stage VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    file_id BIGINT NOT NULL,
    version INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '待审核',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewer_id BIGINT,
    review_comment VARCHAR(2000),
    reviewed_at TIMESTAMP NULL,
    revision_count INT NOT NULL DEFAULT 0,
    UNIQUE (student_id, stage, version),
    FOREIGN KEY (student_id) REFERENCES sys_user (id),
    FOREIGN KEY (teacher_id) REFERENCES sys_user (id),
    FOREIGN KEY (file_id) REFERENCES file_record (id),
    FOREIGN KEY (reviewer_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS guidance_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    guidance_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES sys_user (id),
    FOREIGN KEY (teacher_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS review_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_type VARCHAR(30) NOT NULL,
    business_id BIGINT NOT NULL,
    student_id BIGINT,
    reviewer_id BIGINT NOT NULL,
    result VARCHAR(20) NOT NULL,
    comment VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES sys_user (id),
    FOREIGN KEY (reviewer_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS defense_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    teacher_ids VARCHAR(255) NOT NULL,
    location VARCHAR(100) NOT NULL,
    defense_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS defense_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL UNIQUE,
    group_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '待提交',
    FOREIGN KEY (student_id) REFERENCES sys_user (id),
    FOREIGN KEY (group_id) REFERENCES defense_group (id)
);

CREATE TABLE IF NOT EXISTS score_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL UNIQUE,
    guidance_score DECIMAL(5, 2) NOT NULL,
    review_score DECIMAL(5, 2) NOT NULL,
    defense_score DECIMAL(5, 2) NOT NULL,
    guidance_weight DECIMAL(5, 2) NOT NULL,
    review_weight DECIMAL(5, 2) NOT NULL,
    defense_weight DECIMAL(5, 2) NOT NULL,
    total_score DECIMAL(5, 2) NOT NULL,
    published INT NOT NULL DEFAULT 0,
    teacher_id BIGINT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES sys_user (id),
    FOREIGN KEY (teacher_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS archive_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL UNIQUE,
    manifest TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '已完成',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    publisher_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (publisher_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS topic_change (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    old_topic_id BIGINT NOT NULL,
    new_topic_id BIGINT NOT NULL,
    old_teacher_id BIGINT NOT NULL,
    new_teacher_id BIGINT NOT NULL,
    reason VARCHAR(2000) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT '待原教师审批',
    approval_step INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES sys_user (id),
    FOREIGN KEY (old_topic_id) REFERENCES topic_info (id),
    FOREIGN KEY (new_topic_id) REFERENCES topic_info (id),
    FOREIGN KEY (old_teacher_id) REFERENCES sys_user (id),
    FOREIGN KEY (new_teacher_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS thesis_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL UNIQUE,
    reviewer_id BIGINT NOT NULL,
    thesis_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '待评阅',
    comment VARCHAR(2000),
    score DECIMAL(5, 2),
    agree_defense INT,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    FOREIGN KEY (student_id) REFERENCES sys_user (id),
    FOREIGN KEY (reviewer_id) REFERENCES sys_user (id),
    FOREIGN KEY (thesis_id) REFERENCES thesis_info (id)
);

CREATE TABLE IF NOT EXISTS plagiarism_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    thesis_id BIGINT NOT NULL,
    platform VARCHAR(100) NOT NULL,
    rate DECIMAL(5, 2) NOT NULL,
    file_id BIGINT NOT NULL,
    registrar_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES sys_user (id),
    FOREIGN KEY (thesis_id) REFERENCES thesis_info (id),
    FOREIGN KEY (file_id) REFERENCES file_record (id),
    FOREIGN KEY (registrar_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS sys_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    path VARCHAR(200) NOT NULL,
    read_flag INT NOT NULL DEFAULT 0,
    event_key VARCHAR(200) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user (id)
);

INSERT INTO
    major (id, name)
VALUES
    (1, '计算机科学与技术'),
    (2, '软件工程');

INSERT INTO
    school_class (id, name, major_id)
VALUES
    (1, '计科22-1班', 1),
    (2, '软工22-1班', 2);

INSERT INTO
    cohort (id, name)
VALUES
    (1, '2026'),
    (2, '2027');

INSERT INTO
    sys_role (id, code, name)
VALUES
    (1, 'ADMIN', '院系管理员'),
    (2, 'TEACHER', '指导教师'),
    (3, 'STUDENT', '学生');

INSERT INTO
    sys_permission (id, code, name)
VALUES
    (1, 'dashboard:read', '首页看板'),
    (2, 'topic:read', '课题浏览'),
    (3, 'topic:write', '课题申报与双选处理'),
    (4, 'material:read', '材料查询'),
    (5, 'material:write', '材料提交审核'),
    (6, 'guidance:read', '指导记录'),
    (7, 'guidance:write', '指导记录维护'),
    (8, 'defense:read', '答辩成绩查询'),
    (9, 'score:write', '成绩录入'),
    (10, 'stats:read', '统计归档查询'),
    (11, 'system:manage', '系统管理'),
    (12, 'archive:write', '归档管理'),
    (13, 'defense:write', '答辩安排');

INSERT INTO
    sys_role_permission (role_id, permission_id)
SELECT
    1,
    id
FROM
    sys_permission;

INSERT INTO
    sys_role_permission (role_id, permission_id)
SELECT
    2,
    id
FROM
    sys_permission
WHERE
    id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

INSERT INTO
    sys_role_permission (role_id, permission_id)
SELECT
    3,
    id
FROM
    sys_permission
WHERE
    id IN (1, 2, 4, 5, 6, 8, 10);

INSERT INTO
    sys_user (
        id,
        username,
        password,
        name,
        major_id,
        class_id,
        cohort,
        enabled
    )
VALUES
    (
        1,
        'admin',
        '$2b$12$szI4YTcFuVeWwYy.ml8cCus4szNoOnCqzr1XM07m/Guyp8BR6K7cK',
        '教务管理员',
        1,
        1,
        '2026',
        1
    );

INSERT INTO
    sys_user_role (user_id, role_id)
VALUES
    (1, 1);

INSERT INTO
    sys_user (
        id,
        username,
        password,
        name,
        major_id,
        class_id,
        cohort,
        enabled
    )
VALUES
    (
        2,
        'teacher1',
        '$2b$12$szI4YTcFuVeWwYy.ml8cCus4szNoOnCqzr1XM07m/Guyp8BR6K7cK',
        '程者军',
        1,
        1,
        '2026',
        1
    );

INSERT INTO
    sys_user_role (user_id, role_id)
VALUES
    (2, 2);

INSERT INTO
    sys_user (
        id,
        username,
        password,
        name,
        major_id,
        class_id,
        cohort,
        enabled
    )
VALUES
    (
        3,
        'teacher2',
        '$2b$12$szI4YTcFuVeWwYy.ml8cCus4szNoOnCqzr1XM07m/Guyp8BR6K7cK',
        '王老师',
        2,
        2,
        '2026',
        1
    );

INSERT INTO
    sys_user_role (user_id, role_id)
VALUES
    (3, 2);

INSERT INTO
    sys_user (
        id,
        username,
        password,
        name,
        major_id,
        class_id,
        cohort,
        enabled
    )
VALUES
    (
        4,
        'student1',
        '$2b$12$szI4YTcFuVeWwYy.ml8cCus4szNoOnCqzr1XM07m/Guyp8BR6K7cK',
        '刘冉',
        1,
        1,
        '2026',
        1
    );

INSERT INTO
    sys_user_role (user_id, role_id)
VALUES
    (4, 3);

INSERT INTO
    sys_user (
        id,
        username,
        password,
        name,
        major_id,
        class_id,
        cohort,
        enabled
    )
VALUES
    (
        5,
        'student2',
        '$2b$12$szI4YTcFuVeWwYy.ml8cCus4szNoOnCqzr1XM07m/Guyp8BR6K7cK',
        '李明',
        1,
        1,
        '2026',
        1
    );

INSERT INTO
    sys_user_role (user_id, role_id)
VALUES
    (5, 3);

INSERT INTO
    sys_user (
        id,
        username,
        password,
        name,
        major_id,
        class_id,
        cohort,
        enabled
    )
VALUES
    (
        6,
        'student3',
        '$2b$12$szI4YTcFuVeWwYy.ml8cCus4szNoOnCqzr1XM07m/Guyp8BR6K7cK',
        '王悦',
        2,
        2,
        '2026',
        1
    );

INSERT INTO
    sys_user_role (user_id, role_id)
VALUES
    (6, 3);

INSERT INTO
    sys_user (
        id,
        username,
        password,
        name,
        major_id,
        class_id,
        cohort,
        enabled
    )
VALUES
    (
        7,
        'student4',
        '$2b$12$szI4YTcFuVeWwYy.ml8cCus4szNoOnCqzr1XM07m/Guyp8BR6K7cK',
        '陈晨',
        1,
        1,
        '2026',
        1
    );

INSERT INTO
    sys_user_role (user_id, role_id)
VALUES
    (7, 3);

INSERT INTO
    workflow_step (id, code, name, deadline, sort_order)
VALUES
    (1, 'topic', '课题申报', '2027-01-28 23:59:59', 1);

INSERT INTO
    workflow_step (id, code, name, deadline, sort_order)
VALUES
    (2, 'selection', '双向选题', '2027-02-28 23:59:59', 2);

INSERT INTO
    workflow_step (id, code, name, deadline, sort_order)
VALUES
    (3, 'task', '任务书', '2027-03-28 23:59:59', 3);

INSERT INTO
    workflow_step (id, code, name, deadline, sort_order)
VALUES
    (4, 'proposal', '开题报告', '2027-04-28 23:59:59', 4);

INSERT INTO
    workflow_step (id, code, name, deadline, sort_order)
VALUES
    (5, 'midterm', '中期检查', '2027-05-28 23:59:59', 5);

INSERT INTO
    workflow_step (id, code, name, deadline, sort_order)
VALUES
    (6, 'draft', '论文初稿', '2027-06-28 23:59:59', 6);

INSERT INTO
    workflow_step (id, code, name, deadline, sort_order)
VALUES
    (7, 'final', '论文定稿', '2027-07-28 23:59:59', 7);

INSERT INTO
    workflow_step (id, code, name, deadline, sort_order)
VALUES
    (8, 'defense', '答辩评定', '2027-08-28 23:59:59', 8);

INSERT INTO
    topic_info (
        id,
        title,
        description,
        teacher_id,
        major_id,
        capacity,
        status,
        reviewer_id,
        review_comment
    )
VALUES
    (
        1,
        '基于 Spring Boot 的校园服务平台',
        '面向校园事务构建前后端分离服务，重点研究权限与业务流程。',
        2,
        1,
        2,
        '已通过',
        1,
        '选题符合专业培养要求'
    ),
    (
        2,
        '学习行为数据可视化分析系统',
        '研究学习行为数据分析与可视化表达。',
        3,
        2,
        1,
        '已通过',
        1,
        '同意发布'
    ),
    (3, '智慧图书馆管理系统', '实现借阅、检索与智能统计。', 2, 1, 1, '已通过', 1, '同意发布'),
    (
        4,
        '校园活动协同管理平台',
        '基于角色分工完成活动申报和审核。',
        3,
        2,
        2,
        '待审核',
        NULL,
        NULL
    );

INSERT INTO
    topic_selection (
        id,
        topic_id,
        student_id,
        status,
        reviewer_id,
        review_comment
    )
VALUES
    (1, 1, 5, '已通过', 2, '同意指导'),
    (2, 2, 6, '已通过', 3, '同意指导'),
    (3, 3, 7, '已通过', 2, '同意指导');

INSERT INTO
    student_assignment (student_id, topic_id, teacher_id)
VALUES
    (5, 1, 2),
    (6, 2, 3),
    (7, 3, 2);

INSERT INTO
    file_record (id, owner_id, original_name, stored_name, size_bytes)
VALUES
    (1, 5, '演示材料.txt', 'demo-5.txt', 0),
    (2, 6, '演示材料.txt', 'demo-6.txt', 0),
    (3, 7, '演示材料.txt', 'demo-7.txt', 0);

INSERT INTO
    task_book (
        student_id,
        teacher_id,
        stage,
        title,
        content,
        file_id,
        version,
        status,
        reviewer_id,
        review_comment,
        reviewed_at
    )
VALUES
    (
        5,
        2,
        'task',
        '任务书 · 演示',
        '演示过程材料，展示提交、审核及意见留痕。',
        1,
        1,
        '已通过',
        2,
        '材料完整，同意通过',
        CURRENT_TIMESTAMP
    );

INSERT INTO
    proposal_report (
        student_id,
        teacher_id,
        stage,
        title,
        content,
        file_id,
        version,
        status,
        reviewer_id,
        review_comment,
        reviewed_at
    )
VALUES
    (
        5,
        2,
        'proposal',
        '开题报告 · 演示',
        '演示过程材料，展示提交、审核及意见留痕。',
        1,
        1,
        '已退回',
        2,
        '请补充技术路线和进度计划',
        CURRENT_TIMESTAMP
    );

INSERT INTO
    task_book (
        student_id,
        teacher_id,
        stage,
        title,
        content,
        file_id,
        version,
        status,
        reviewer_id,
        review_comment,
        reviewed_at
    )
VALUES
    (
        6,
        3,
        'task',
        '任务书 · 演示',
        '演示过程材料，展示提交、审核及意见留痕。',
        2,
        1,
        '已通过',
        3,
        '材料完整，同意通过',
        CURRENT_TIMESTAMP
    );

INSERT INTO
    proposal_report (
        student_id,
        teacher_id,
        stage,
        title,
        content,
        file_id,
        version,
        status,
        reviewer_id,
        review_comment,
        reviewed_at
    )
VALUES
    (
        6,
        3,
        'proposal',
        '开题报告 · 演示',
        '演示过程材料，展示提交、审核及意见留痕。',
        2,
        1,
        '已通过',
        3,
        '材料完整，同意通过',
        CURRENT_TIMESTAMP
    );

INSERT INTO
    midterm_check (
        student_id,
        teacher_id,
        stage,
        title,
        content,
        file_id,
        version,
        status,
        reviewer_id,
        review_comment,
        reviewed_at
    )
VALUES
    (
        6,
        3,
        'midterm',
        '中期检查 · 演示',
        '演示过程材料，展示提交、审核及意见留痕。',
        2,
        1,
        '待审核',
        NULL,
        '待教师审核',
        NULL
    );

INSERT INTO
    task_book (
        student_id,
        teacher_id,
        stage,
        title,
        content,
        file_id,
        version,
        status,
        reviewer_id,
        review_comment,
        reviewed_at
    )
VALUES
    (
        7,
        2,
        'task',
        '任务书 · 演示',
        '演示过程材料，展示提交、审核及意见留痕。',
        3,
        1,
        '已通过',
        2,
        '材料完整，同意通过',
        CURRENT_TIMESTAMP
    );

INSERT INTO
    proposal_report (
        student_id,
        teacher_id,
        stage,
        title,
        content,
        file_id,
        version,
        status,
        reviewer_id,
        review_comment,
        reviewed_at
    )
VALUES
    (
        7,
        2,
        'proposal',
        '开题报告 · 演示',
        '演示过程材料，展示提交、审核及意见留痕。',
        3,
        1,
        '已通过',
        2,
        '材料完整，同意通过',
        CURRENT_TIMESTAMP
    );

INSERT INTO
    midterm_check (
        student_id,
        teacher_id,
        stage,
        title,
        content,
        file_id,
        version,
        status,
        reviewer_id,
        review_comment,
        reviewed_at
    )
VALUES
    (
        7,
        2,
        'midterm',
        '中期检查 · 演示',
        '演示过程材料，展示提交、审核及意见留痕。',
        3,
        1,
        '已通过',
        2,
        '材料完整，同意通过',
        CURRENT_TIMESTAMP
    );

INSERT INTO
    thesis_info (
        student_id,
        teacher_id,
        stage,
        title,
        content,
        file_id,
        version,
        status,
        reviewer_id,
        review_comment,
        reviewed_at
    )
VALUES
    (
        7,
        2,
        'draft',
        '论文初稿 · 演示',
        '演示过程材料，展示提交、审核及意见留痕。',
        3,
        1,
        '已通过',
        2,
        '材料完整，同意通过',
        CURRENT_TIMESTAMP
    );

INSERT INTO
    thesis_info (
        student_id,
        teacher_id,
        stage,
        title,
        content,
        file_id,
        version,
        status,
        reviewer_id,
        review_comment,
        reviewed_at
    )
VALUES
    (
        7,
        2,
        'final',
        '论文定稿 · 演示',
        '演示过程材料，展示提交、审核及意见留痕。',
        3,
        1,
        '已通过',
        2,
        '材料完整，同意通过',
        CURRENT_TIMESTAMP
    );

INSERT INTO
    review_record (
        business_type,
        business_id,
        student_id,
        reviewer_id,
        result,
        comment
    )
VALUES
    ('proposal', 1, 5, 2, '已退回', '请补充技术路线和进度计划');

INSERT INTO
    guidance_record (student_id, teacher_id, content, guidance_at)
VALUES
    (5, 2, '明确研究问题，补充系统用例图和数据库设计。', '2026-11-20 14:00:00'),
    (6, 3, '完善中期报告，增加核心功能测试记录。', '2026-11-21 10:00:00');

INSERT INTO
    defense_group (id, name, teacher_ids, location, defense_at)
VALUES
    (1, '计算机专业第一答辩组', '2,3', '教学楼 A302', '2027-06-10 09:00:00');

INSERT INTO
    defense_info (student_id, group_id, status)
VALUES
    (7, 1, '已完成');

INSERT INTO
    score_info (
        student_id,
        guidance_score,
        review_score,
        defense_score,
        guidance_weight,
        review_weight,
        defense_weight,
        total_score,
        published,
        teacher_id
    )
VALUES
    (7, 90, 86, 92, 0.3, 0.3, 0.4, 89.6, 1, 2);

INSERT INTO
    archive_record (student_id, manifest)
VALUES
    (7, '演示归档：任务书、开题报告、中期检查、论文初稿、论文定稿、成绩均已齐全。');

INSERT INTO
    notice (title, content, publisher_id)
VALUES
    (
        '2026届毕业设计工作安排',
        '请按流程完成选题、任务书、开题、中检和论文提交。退回材料请根据指导意见修改后重新提交。',
        1
    ),
    (
        '材料提交与版本管理说明',
        '材料支持 PDF、Word、TXT 及 ZIP，单文件不超过20MB；每次重新提交保留独立版本及审核意见。',
        1
    );

INSERT INTO
    thesis_review (
        student_id,
        reviewer_id,
        thesis_id,
        status,
        comment,
        score,
        agree_defense,
        reviewed_at
    )
SELECT
    7,
    3,
    id,
    '已评阅',
    '结构完整、论证充分，同意答辩。',
    86,
    1,
    CURRENT_TIMESTAMP
FROM
    thesis_info
WHERE
    student_id = 7
    AND stage = 'final';

INSERT INTO
    plagiarism_check (
        student_id,
        thesis_id,
        platform,
        rate,
        file_id,
        registrar_id
    )
SELECT
    7,
    id,
    '演示查重平台',
    12.5,
    3,
    1
FROM
    thesis_info
WHERE
    student_id = 7
    AND stage = 'final';

INSERT INTO
    sys_message (user_id, title, content, path, event_key)
VALUES
    (
        7,
        '成绩已发布',
        '毕业设计成绩89.60分，归档材料已齐全。',
        '/my-project',
        'demo:score:7'
    ),
    (
        5,
        '开题报告已退回',
        '请补充技术路线和进度计划。',
        '/my-project',
        'demo:proposal:5'
    ),
    (
        2,
        '过程管理工作提醒',
        '请及时查看学生申请与材料待办。',
        '/pending-reviews',
        'demo:teacher:2'
    );

INSERT INTO
    topic_info (
        id,
        title,
        description,
        teacher_id,
        major_id,
        capacity,
        status,
        reviewer_id,
        review_comment
    )
VALUES
    (
        5,
        '跨学科校园数据分析平台',
        '面向计算机专业的数据分析与协同设计课题。',
        3,
        1,
        2,
        '已通过',
        1,
        '同意发布'
    );

INSERT INTO
    topic_change (
        id,
        student_id,
        old_topic_id,
        new_topic_id,
        old_teacher_id,
        new_teacher_id,
        reason,
        status,
        approval_step
    )
VALUES
    (1, 5, 1, 5, 2, 3, '希望调整研究方向，现保留原课题继续完善研究计划。', '已退回', 1);

INSERT INTO
    review_record (
        business_type,
        business_id,
        student_id,
        reviewer_id,
        result,
        comment
    )
VALUES
    ('topic_change', 1, 5, 2, '已退回', '第1级：请先完善研究计划后再申请变更。');
