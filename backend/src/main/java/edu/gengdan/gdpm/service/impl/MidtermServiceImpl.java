package edu.gengdan.gdpm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.entity.MidtermCheck;
import edu.gengdan.gdpm.mapper.MidtermCheckMapper;
import edu.gengdan.gdpm.service.MidtermService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 持 material:read 权限查询范围内各阶段全部历史版本。 */
@Service
public class MidtermServiceImpl
    extends AbstractMaterialStageService<MidtermCheckMapper, MidtermCheck>
    implements MidtermService
{

    /** 构造当前材料表的学生与阶段查询条件。 */
    protected LambdaQueryWrapper<MidtermCheck> stageQuery(long studentId, String stage) {
        return query(MidtermCheck.class)
            .eq(MidtermCheck::getStudentId, studentId)
            .eq(MidtermCheck::getStage, stage)
            .orderByDesc(MidtermCheck::getVersion);
    }

    /** 创建当前阶段对应的材料实体。 */
    protected MidtermCheck newMaterial() {
        return new MidtermCheck();
    }

    /** 拒绝与当前模块不匹配的材料阶段。 */
    protected void validateStage(String stage) {
        ApiException.require(List.of("midterm").contains(stage), "材料阶段不存在");
    }
}
