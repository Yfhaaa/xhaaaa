package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.ArchiveRecord;
import edu.gengdan.gdpm.vo.DashboardVO;
import edu.gengdan.gdpm.vo.StatisticsVO;
import java.util.List;
import java.util.Map;

/**
 * 按当前身份的数据范围汇总九节点进度，学生未发布成绩保持隐藏。 持 dashboard:read 权限查看当前身份的待办、进度、通知与截止提醒。 持 stats:read
 * 权限按专业、班级或教师统计，先做身份范围过滤再应用业务筛选。
 */
public interface StatisticsService extends IService<ArchiveRecord> {
    /** 按当前身份的数据范围汇总九节点进度，学生未发布成绩保持隐藏。 */
    List<Map<String, Object>> progress();

    /** 持 dashboard:read 权限查看当前身份的待办、进度、通知与截止提醒。 */
    DashboardVO dashboard();

    /** 持 stats:read 权限按专业、班级或教师统计，先做身份范围过滤再应用业务筛选。 */
    StatisticsVO statistics(Map<String, String> filters);
}
