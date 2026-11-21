package edu.gengdan.gdpm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.entity.TaskBook;
import edu.gengdan.gdpm.mapper.TaskBookMapper;
import edu.gengdan.gdpm.service.TaskBookService;
import org.springframework.stereotype.Service;

/** TaskBook 的类型化阶段服务。 */
@Service
public class TaskBookServiceImpl
    extends AbstractMaterialStageService<TaskBookMapper, TaskBook>
    implements TaskBookService
{

    /** 构造当前材料表的学生与阶段查询条件。 */
    protected LambdaQueryWrapper<TaskBook> stageQuery(long studentId, String stage) {
        return query(TaskBook.class)
            .eq(TaskBook::getStudentId, studentId)
            .eq(TaskBook::getStage, stage)
            .orderByDesc(TaskBook::getVersion);
    }

    /** 创建当前阶段对应的材料实体。 */
    protected TaskBook newMaterial() {
        return new TaskBook();
    }

    /** 拒绝与当前模块不匹配的材料阶段。 */
    protected void validateStage(String stage) {
        ApiException.require(java.util.List.of("task").contains(stage), "材料阶段不存在");
    }
}
