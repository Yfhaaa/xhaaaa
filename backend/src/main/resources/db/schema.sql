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
