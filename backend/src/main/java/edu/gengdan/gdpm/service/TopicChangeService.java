package edu.gengdan.gdpm.service;

import edu.gengdan.gdpm.dto.ReviewRequest;
import edu.gengdan.gdpm.dto.TopicChangeRequest;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.StatusVO;
import java.util.List;
import java.util.Map;

/** 仅变更参与学生、原教师、新教师和管理员可读变更及审批历史。 学生仅为未进入答辩项目申请本专业有名额的新课题，同一学生只允许一条在途申请。 原教师、新教师、管理员依次审批；终审锁定学生和两课题原子迁移关系，拒绝则保留历史。 */
public interface TopicChangeService {
    /** 仅变更参与学生、原教师、新教师和管理员可读变更及审批历史。 */
    List<Map<String, Object>> records();

    /** 学生仅为未进入答辩项目申请本专业有名额的新课题，同一学生只允许一条在途申请。 */
    IdVO apply(TopicChangeRequest body);

    /** 原教师、新教师、管理员依次审批；终审锁定学生和两课题原子迁移关系，拒绝则保留历史。 */
    StatusVO approve(long id, ReviewRequest body);
}
