package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.BaseDataSaveRequest;
import edu.gengdan.gdpm.entity.Major;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.ReferenceVO;

/** 已登录用户获取专业、班级和届次，教师列表仅管理员可见。 管理员维护专业、班级和届次，班级专业变更须与已有用户一致，届次改名同步用户。 管理员删除未被引用的基础数据，引用冲突由持久化约束拦截。 */
public interface BaseDataService extends IService<Major> {
    /** 已登录用户获取专业、班级和届次，教师列表仅管理员可见。 */
    ReferenceVO reference();

    /** 管理员维护专业、班级和届次，班级专业变更须与已有用户一致，届次改名同步用户。 */
    IdVO base(String type, Long baseId, BaseDataSaveRequest body);

    /** 管理员删除未被引用的基础数据，引用冲突由持久化约束拦截。 */
    MessageVO removeBase(String type, long baseId);
}
