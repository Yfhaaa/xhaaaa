package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.ProcessMaterial;
import java.util.Map;

/** 单类材料的版本化提交与审核契约。 */
public interface MaterialStageService<T extends ProcessMaterial> extends IService<T> {
    /** 查询指定阶段的最新版本。 */
    T latest(long studentId, String stage);
    /** 校验阶段顺序后保存新材料版本。 */
    Object submit(String stage, Map<String, Object> body);
    /** 审核当前待审核的最新材料。 */
    Object review(String stage, long materialId, Map<String, Object> body);
}
