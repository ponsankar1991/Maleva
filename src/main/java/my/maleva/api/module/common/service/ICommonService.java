package my.maleva.api.module.common.service;

import my.maleva.api.common.dto.ResponseViewModel;

/**
 * Common Service Interface
 * Equivalent to .NET ICommonServices
 */
public interface ICommonService {

    ResponseViewModel uploadFile(int id, int comid, String tableName, String paths);

    ResponseViewModel fetchFiles(String imageDirectory);

    ResponseViewModel checkFiles(String imageDirectory);
}
