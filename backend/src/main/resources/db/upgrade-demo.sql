-- 增量演示只补充已发布演示学生，且不覆盖已有记录。
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
    t.id,
    '已评阅',
    '结构完整、论证充分，同意答辩。',
    86,
    1,
    CURRENT_TIMESTAMP
FROM
    thesis_info t
    JOIN sys_user u ON u.id = t.student_id
WHERE
    t.student_id = 7
    AND t.stage = 'final'
    AND t.status = '已通过'
    AND u.username = 'student4'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            thesis_review r
        WHERE
            r.student_id = 7
    );

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
    t.id,
    '演示查重平台',
    12.5,
    t.file_id,
    1
FROM
    thesis_info t
    JOIN sys_user u ON u.id = t.student_id
WHERE
    t.student_id = 7
    AND t.stage = 'final'
    AND t.status = '已通过'
    AND u.username = 'student4'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            plagiarism_check p
        WHERE
            p.thesis_id = t.id
    );

INSERT INTO
    sys_message (user_id, title, content, path, event_key)
SELECT
    7,
    '毕业设计工作提醒',
    '可在我的毕设查看全过程档案与已发布成绩。',
    '/my-project',
    'upgrade:student4'
FROM
    sys_user u
WHERE
    u.id = 7
    AND u.username = 'student4'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            sys_message m
        WHERE
            m.event_key = 'upgrade:student4'
    );
