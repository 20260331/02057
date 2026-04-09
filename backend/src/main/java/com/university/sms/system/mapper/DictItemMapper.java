package com.university.sms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.system.entity.DictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DictItemMapper extends BaseMapper<DictItem> {
    
    @Select("SELECT * FROM sys_dict_item WHERE dict_code = #{dictCode} AND status = 1 ORDER BY sort_order")
    List<DictItem> selectByDictCode(@Param("dictCode") String dictCode);
}
