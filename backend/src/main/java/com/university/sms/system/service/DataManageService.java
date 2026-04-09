package com.university.sms.system.service;

import com.university.sms.system.dto.RestoreStatusDTO;
import com.university.sms.system.entity.DictItem;
import com.university.sms.system.entity.DictType;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface DataManageService {

    // ==================== 备份管理 ====================

    List<Map<String, Object>> getBackupList();

    Map<String, Object> createBackup();

    /**
     * 从指定备份文件恢复数据库（同步，供内部使用）
     */
    void restoreBackup(String fileName);

    /**
     * 异步启动恢复任务，立即返回；实际恢复在后台执行。
     */
    void startRestoreAsync(String fileName);

    /**
     * 获取当前恢复任务状态（用于前端轮询）
     */
    RestoreStatusDTO getRestoreStatus();

    byte[] getBackupFileContent(String fileName);

    String getBackupFileName(String fileName);

    void deleteBackup(String fileName);

    // ==================== 数据导出 ====================

    void exportData(String type, OutputStream outputStream);

    // ==================== 数据字典 ====================

    List<Map<String, Object>> getDataDictionary();

    List<DictType> getDictTypes();

    DictType createDictType(DictType dictType);

    void updateDictType(Long id, DictType dictType);

    void deleteDictType(Long id);

    List<DictItem> getDictItems(String dictCode);

    DictItem createDictItem(DictItem dictItem);

    void updateDictItem(Long id, DictItem dictItem);

    void deleteDictItem(Long id);
}
