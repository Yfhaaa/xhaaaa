package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;
import static edu.gengdan.gdpm.common.Values.*;

import edu.gengdan.gdpm.common.*;
import edu.gengdan.gdpm.entity.*;
import edu.gengdan.gdpm.mapper.*;
import edu.gengdan.gdpm.service.*;
import java.math.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** ScoreService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class ScoreServiceImpl extends DomainSupport<ScoreInfoMapper, ScoreInfo> implements ScoreService {

    @Autowired
    private MaterialService materials;

    /** 查询范围内可见成绩并隐藏未发布成绩。 */
    public Object scores() {
        auth.permission("defense:read");
        return reports.scores(auth.scope());
    }

    /** 使用十进制定点计算并锁定学生行，发布后不允许改分。 */
    @OperLog("录入成绩")
    @Transactional
    public Object score(Map<String, Object> body) {
        auth.roles("TEACHER", "ADMIN");
        auth.permission("score:write");
        long studentId = Params.number(body, "studentId");
        auth.student(studentId);
        lockStudent(studentId);
        require(materials.passed(studentId, "final"), "请先完成论文定稿审核");
        require(
            defenses.selectCount(query(DefenseInfo.class).eq(DefenseInfo::getStudentId, studentId)) == 1,
            "请先安排答辩"
        );
        require(
            archives.selectCount(query(ArchiveRecord.class).eq(ArchiveRecord::getStudentId, studentId)) == 0,
            "已归档项目不能改分"
        );
        ScoreInfo previous = lambdaQuery().eq(ScoreInfo::getStudentId, studentId).one();
        require(previous == null || previous.getPublished() == 0, "成绩已发布，不能修改");
        BigDecimal guidance = Params.decimal(body, "guidanceScore", new BigDecimal("100")),
            review = Params.decimal(body, "reviewScore", new BigDecimal("100")),
            defense = Params.decimal(body, "defenseScore", new BigDecimal("100"));
        BigDecimal guidanceWeight = Params.decimal(body, "guidanceWeight", BigDecimal.ONE),
            reviewWeight = Params.decimal(body, "reviewWeight", BigDecimal.ONE),
            defenseWeight = Params.decimal(body, "defenseWeight", BigDecimal.ONE);
        require(
            guidanceWeight.add(reviewWeight).add(defenseWeight).compareTo(BigDecimal.ONE) == 0,
            "三项权重之和必须等于1"
        );
        BigDecimal total = guidance
            .multiply(guidanceWeight)
            .add(review.multiply(reviewWeight))
            .add(defense.multiply(defenseWeight))
            .setScale(2, RoundingMode.HALF_UP);
        ScoreInfo score = entity(
            ScoreInfo.class,
            "student_id",
            studentId,
            "guidance_score",
            guidance,
            "review_score",
            review,
            "defense_score",
            defense,
            "guidance_weight",
            guidanceWeight,
            "review_weight",
            reviewWeight,
            "defense_weight",
            defenseWeight,
            "total_score",
            total,
            "published",
            0,
            "teacher_id",
            auth.uid(),
            "updated_at",
            LocalDateTime.now()
        );
        if (previous != null) {
            score.setId(previous.getId());
        }
        saveOrUpdate(score);
        defenses.update(
            null,
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<DefenseInfo>()
                .eq(DefenseInfo::getStudentId, studentId)
                .set(DefenseInfo::getStatus, BusinessStatus.COMPLETED.label())
        );
        return Map.of("id", score.getId(), "totalScore", total);
    }

    /** 发布成绩并锁定后续修改。 */
    @OperLog("发布成绩")
    @Transactional
    public Object publish(long scoreId) {
        auth.roles("ADMIN");
        auth.permission("score:write");
        ScoreInfo score = required(getById(scoreId));
        lockStudent(score.getStudentId());
        score = required(lambdaQuery().eq(ScoreInfo::getId, scoreId).last("FOR UPDATE").one());
        require(score.getPublished() == 0, "成绩已发布");
        score.setPublished(1);
        updateById(score);
        return Map.of("message", "成绩已发布");
    }
}
