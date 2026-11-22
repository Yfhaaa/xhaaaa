package edu.gengdan.gdpm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.gengdan.gdpm.entity.PlagiarismCheck;
import org.apache.ibatis.annotations.Mapper;

/** plagiarism_check 实体持久化接口。 */
@Mapper
public interface PlagiarismCheckMapper extends BaseMapper<PlagiarismCheck> {}
