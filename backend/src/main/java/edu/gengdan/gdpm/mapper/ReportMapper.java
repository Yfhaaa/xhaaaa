package edu.gengdan.gdpm.mapper;

import edu.gengdan.gdpm.dto.DataScope;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** 跨表关联和统计所需的投影视图，SQL 统一维护在 mapper XML。 */
public interface ReportMapper {
    /** 不依赖登录上下文的截止提醒节点完成状态。 */
    Map<String, Object> progressState(@Param("studentId") long studentId);

    /** roles 的关联查询视图。 */
    List<String> roles(@Param("userId") long userId);

    /** permissions 的关联查询视图。 */
    List<String> permissions(@Param("userId") long userId);

    /** users 的关联查询视图。 */
    List<Map<String, Object>> users();

    /** teachers 的关联查询视图。 */
    List<Map<String, Object>> teachers();

    /** students 的关联查询视图。 */
    List<Map<String, Object>> students(@Param("scope") DataScope scope);

    /** topics 的关联查询视图。 */
    List<Map<String, Object>> topics(@Param("scope") DataScope scope);

    /** selections 的关联查询视图。 */
    List<Map<String, Object>> selections(@Param("scope") DataScope scope);

    /** guidance 的关联查询视图。 */
    List<Map<String, Object>> guidance(@Param("scope") DataScope scope);

    /** defenses 的关联查询视图。 */
    List<Map<String, Object>> defenses(@Param("scope") DataScope scope);

    /** groups 的关联查询视图。 */
    List<Map<String, Object>> groups(@Param("scope") DataScope scope);

    /** scores 的关联查询视图。 */
    List<Map<String, Object>> scores(@Param("scope") DataScope scope);

    /** archives 的关联查询视图。 */
    List<Map<String, Object>> archives(@Param("scope") DataScope scope);

    /** reviews 的关联查询视图。 */
    List<Map<String, Object>> reviews(@Param("scope") DataScope scope);

    /** logs 的关联查询视图。 */
    List<Map<String, Object>> logs();

    /** notices 的关联查询视图。 */
    List<Map<String, Object>> notices();

    /** pendingSelections 的关联查询视图。 */
    List<Map<String, Object>> pendingSelections(@Param("teacherId") long teacherId);

    /** materialsTask 的关联查询视图。 */
    List<Map<String, Object>> materialsTask(@Param("scope") DataScope scope, @Param("stage") String stage);

    /** materialsProposal 的关联查询视图。 */
    List<Map<String, Object>> materialsProposal(
        @Param("scope") DataScope scope,
        @Param("stage") String stage
    );

    /** materialsMidterm 的关联查询视图。 */
    List<Map<String, Object>> materialsMidterm(@Param("scope") DataScope scope, @Param("stage") String stage);

    /** materialsThesis 的关联查询视图。 */
    List<Map<String, Object>> materialsThesis(@Param("scope") DataScope scope, @Param("stage") String stage);

    /** 按专业、班级或教师聚合九节点进度及范围内可见成绩。 */
    List<Map<String, Object>> statistics(
        @Param("scope") DataScope scope,
        @Param("groupBy") String groupBy,
        @Param("filters") Map<String, String> filters
    );
}
