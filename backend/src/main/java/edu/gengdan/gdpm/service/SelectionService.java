package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.ReviewRequest;
import edu.gengdan.gdpm.entity.TopicSelection;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.MessageVO;
import java.util.List;
import java.util.Map;

/** 按学生本人或教师指导范围查询双选申请。 学生在选题截止前申请本专业有名额的已通过课题，拒绝重复和多课题确定。 课题教师在事务内确认待审核申请，锁学生与课题保证唯一分配且不超名额。 */
public interface SelectionService extends IService<TopicSelection> {
    /** 按学生本人或教师指导范围查询双选申请。 */
    List<Map<String, Object>> selections();

    /** 学生在选题截止前申请本专业有名额的已通过课题，拒绝重复和多课题确定。 */
    IdVO apply(long topicId);

    /** 课题教师在事务内确认待审核申请，锁学生与课题保证唯一分配且不超名额。 */
    MessageVO confirm(long selectionId, ReviewRequest body);
}
