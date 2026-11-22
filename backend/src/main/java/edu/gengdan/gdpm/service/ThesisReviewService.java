package edu.gengdan.gdpm.service;

import edu.gengdan.gdpm.dto.ThesisReviewAssignRequest;
import edu.gengdan.gdpm.dto.ThesisReviewRequest;
import edu.gengdan.gdpm.vo.CountVO;
import edu.gengdan.gdpm.vo.IdVO;
import java.util.List;
import java.util.Map;

/** 仅指定评阅教师、学生本人及管理员可查询评阅详情。 管理员为定稿通过学生批量或单独分配非指导教师，任一无效项使整批回滚。 指定评阅教师仅一次提交百分制分数、意见与答辩资格，状态从待评阅改为已评阅。 */
public interface ThesisReviewService {
    /** 仅指定评阅教师、学生本人及管理员可查询评阅详情。 */
    List<Map<String, Object>> records();

    /** 管理员为定稿通过学生批量或单独分配非指导教师，任一无效项使整批回滚。 */
    CountVO assign(ThesisReviewAssignRequest body);

    /** 指定评阅教师仅一次提交百分制分数、意见与答辩资格，状态从待评阅改为已评阅。 */
    IdVO review(long id, ThesisReviewRequest body);
}
