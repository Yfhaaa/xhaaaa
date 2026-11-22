package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.ScoreSaveRequest;
import edu.gengdan.gdpm.entity.ScoreInfo;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.ScoreSavedVO;
import java.util.List;
import java.util.Map;

/** 按角色范围查询成绩，学生仅可见本人已发布成绩。 指导教师或管理员锁定学生后录分；引用已完成评阅分，权重和为一，发布归档后禁改。 管理员仅发布未发布成绩，发布后锁定改分并通知学生。 */
public interface ScoreService extends IService<ScoreInfo> {
    /** 按角色范围查询成绩，学生仅可见本人已发布成绩。 */
    List<Map<String, Object>> scores();

    /** 指导教师或管理员锁定学生后录分；引用已完成评阅分，权重和为一，发布归档后禁改。 */
    ScoreSavedVO score(ScoreSaveRequest body);

    /** 管理员仅发布未发布成绩，发布后锁定改分并通知学生。 */
    MessageVO publish(long scoreId);
}
