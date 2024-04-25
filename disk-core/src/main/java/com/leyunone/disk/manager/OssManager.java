package com.leyunone.disk.manager;

import com.aliyun.oss.model.PartETag;

import java.io.InputStream;
import java.util.List;

/**
 * :)
 *
 * @Author pengli
 * @Date 2024/4/22 16:32
 */
public interface OssManager {

    String completePartUploadFile(String fileName, String uploadId, List<PartETag> partETags);

    PartETag partUploadFile(String fileName, InputStream is, String uploadId, String fileMd5, int partNum, long partSize);

    String getUploadId(String fileName);
}