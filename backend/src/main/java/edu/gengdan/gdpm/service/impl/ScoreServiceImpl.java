package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import edu.gengdan.gdpm.common.BusinessStatus;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.ScoreSaveRequest;
import edu.gengdan.gdpm.entity.ArchiveRecord;
import edu.gengdan.gdpm.entity.DefenseInfo;
import edu.gengdan.gdpm.entity.ScoreInfo;
import edu.gengdan.gdpm.entity.ThesisReview;
import edu.gengdan.gdpm.mapper.ScoreInfoMapper;
import edu.gengdan.gdpm.service.MaterialService;
import edu.gengdan.gdpm.service.ScoreService;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.ScoreSavedVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 按角色范围查询成绩，学生仅可见本人已发布成绩。 指导教师或管理员锁定学生后录分；引用已完成评阅分，权重和为一，发布归档后禁改。 管理员仅发布未发布成绩，发布后锁定改分并通知学生。 */
@Service
public class ScoreServiceImpl extends DomainSupport<ScoreInfoMapper, ScoreInfo> implements ScoreService {

    @Autowired
    private MaterialService materials;

    /** 按角色范围查询成绩，学生仅可见本人已发布成绩。 */
    public List<Map<String, Object>> scores() {
        auth.permission("defense:read");
        return reports.scores(auth.scope());
    }

    /** 指导教师或管理员锁定学生后录分；引用已完成评阅分，权重和为一，发布归档后禁改。 */
    @OperLog("录入成绩")
    @Transactional
    public ScoreSavedVO score(ScoreSaveRequest body) {
        validate(body);
        auth.roles("TEACHER", "ADMIN");
        auth.permission("score:write");
        long studentId = body.getStudentId();
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
        BigDecimal guidance = body.getGuidanceScore();
        BigDecimal defense = body.getDefenseScore();
        // 评阅分不接受前端录入，统一取自评阅教师已提交且同意答辩的评阅记录
        ThesisReview thesisReview = thesisReviews.selectOne(
            query(ThesisReview.class)
                .eq(ThesisReview::getStudentId, studentId)
                .eq(ThesisReview::getStatus, BusinessStatus.REVIEWED.label())
                .eq(ThesisReview::getAgreeDefense, 1)
        );
        require(thesisReview != null, "该学生论文评阅尚未完成或评阅未同意答辩，不能录入成绩");
        BigDecimal review = thesisReview.getScore();
        BigDecimal guidanceWeight = body.getGuidanceWeight(),
            reviewWeight = body.getReviewWeight(),
            defenseWeight = body.getDefenseWeight();
        require(
            guidanceWeight.add(reviewWeight).add(defenseWeight).compareTo(BigDecimal.ONE) == 0,
            "三项权重之和必须等于1"
        );
        BigDecimal total = guidance
            .multiply(guidanceWeight)
            .add(review.multiply(reviewWeight))
            .add(defense.multiply(defenseWeight))
            .setScale(2, RoundingMode.HALF_UP);
        ScoreInfo score = new ScoreInfo();
        score.setStudentId(studentId);
        score.setGuidanceScore(guidance);
        score.setReviewScore(review);
        score.setDefenseScore(defense);
        score.setGuidanceWeight(guidanceWeight);
        score.setReviewWeight(reviewWeight);
        score.setDefenseWeight(defenseWeight);
        score.setTotalScore(total);
        score.setPublished(0);
        score.setTeacherId(auth.uid());
        score.setUpdatedAt(LocalDateTime.now());
        if (previous != null) {
            score.setId(previous.getId());
        }
        saveOrUpdate(score);
        defenses.update(
            null,
            new LambdaUpdateWrapper<DefenseInfo>()
                .eq(DefenseInfo::getStudentId, studentId)
                .set(DefenseInfo::getStatus, BusinessStatus.COMPLETED.label())
        );
        return new ScoreSavedVO(score.getId(), total);
    }

    /** 管理员仅发布未发布成绩，发布后锁定改分并通知学生。 */
    @OperLog("发布成绩")
    @Transactional
    public MessageVO publish(long scoreId) {
        auth.roles("ADMIN");
        auth.permission("score:write");
        ScoreInfo score = required(getById(scoreId));
        lockStudent(score.getStudentId());
        score = required(lambdaQuery().eq(ScoreInfo::getId, scoreId).last("FOR UPDATE").one());
        require(score.getPublished() == 0, "成绩已发布");
        score.setPublished(1);
        updateById(score);
        messages.send(
            score.getStudentId(),
            "毕业设计成绩已发布",
            "总成绩：" + score.getTotalScore(),
            "/scores",
            null
        );
        return new MessageVO("成绩已发布");
    }
}
