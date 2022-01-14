package com.leyuna.disk.service.file;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.leyuna.disk.DataResponse;
import com.leyuna.disk.co.FileInfoCO;
import com.leyuna.disk.command.CacheExe;
import com.leyuna.disk.constant.ServerCode;
import com.leyuna.disk.domain.FileInfoE;
import com.leyuna.disk.domain.FileUpLogE;
import com.leyuna.disk.dto.file.UpFileDTO;
import com.leyuna.disk.enums.ErrorEnum;
import com.leyuna.disk.enums.FileEnum;
import com.leyuna.disk.enums.SortEnum;
import com.leyuna.disk.util.AssertUtil;
import com.leyuna.disk.util.FileUtil;
import com.leyuna.disk.util.ObjectUtil;
import com.leyuna.disk.validator.FileValidator;
import com.leyuna.disk.validator.UserValidator;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author pengli
 * @create 2021-12-24 16:55
 * 文件相关服务 [非查询]
 */
@Service
@Log4j2
public class FileService {

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private FileValidator fileValidator;

    @Autowired
    private CacheExe cacheExe;

    //默认最大520000
    @Value("${disk.max.memory:520000}")
    private Long maxMemory;

    //默认大小5000
    @Value("${disk.max.file:5000}")
    private Long maxFile;


    /**
     * 校验本次文件上传请求是否合法
     *
     * @param upFileDTO
     * @return
     */
    public DataResponse<Integer> JudgeFile (UpFileDTO upFileDTO) {

        //首先看这个用户是否符合上传文件规则
        userValidator.validator(upFileDTO.getUserId());
        MultipartFile file = upFileDTO.getFile();
        if (ObjectUtils.isEmpty(file)) {
            return DataResponse.buildFailure();
        }
        double fileSize=(double)file.getSize()/1024;
        String fileName = file.getOriginalFilename();
        //名称校验
        fileValidator.validator(fileName, fileSize, null);
        //按文件名和大小 判断是否有数据一样的文件
        List<FileInfoCO> fileInfoCOS = FileInfoE.queryInstance().setName(fileName).setFileSize(fileSize).selectByCon();
        //File theFile = FileUtil.searchFile(fileName, fileSize);

        if(CollectionUtils.isNotEmpty(fileInfoCOS)){
            FileInfoCO fileInfoCO = fileInfoCOS.get(0);
            //如果这个文件是我自己硬盘里的文件则跳过
            if(!fileInfoCO.getUserId().equals(upFileDTO.getUserId())){
                //如果服务器中有一个一模一样的文件，则直接进行拷贝操作
                String path="/"+fileInfoCO.getFileType()+"/"+fileInfoCO.getName();
                File copyFile=new File(ServerCode.FILE_ADDRESS+fileInfoCO.getUserId()
                        +path);

                FileUtil.copyFile(copyFile, upFileDTO.getUserId()+path);
            }
            //返回一个空对象
            return DataResponse.of(0);
        }
        return DataResponse.of(1);
    }

    /**
     * 保存文件
     *
     * @param upFileDTO
     * @return
     */
    public DataResponse savaFile (UpFileDTO upFileDTO) {
        //上传用户编号
        String userId = upFileDTO.getUserId();
        try {
            MultipartFile file = upFileDTO.getFile();
            String name = file.getOriginalFilename();
            String type = name.substring(name.lastIndexOf('.')+1);
            //文件类型
            FileEnum fileEnum = FileEnum.loadType(type);

            //计算K级内存大小
            double fileSize=(double)file.getSize()/1024;
            AssertUtil.isFalse(fileSize>maxMemory,ErrorEnum.FILE_USER_OVER.getName());
            //用户的文件列表
            List<FileInfoCO> fileInfoCOS = FileInfoE.selectByUserIdMaxSize(userId);
            FileInfoCO lastFile = null;
            if (CollectionUtils.isNotEmpty(fileInfoCOS)) {
                lastFile = fileInfoCOS.get(0);
                //大于5G非法
                if (lastFile.getFileSizeTotal() + fileSize > maxMemory) {
                    //用户列表内存已满，无法继续上传文件
                    FileUpLogE.queryInstance()
                            .setUpSign(1).setUserId(userId).save();
                    AssertUtil.isFalse(true, ErrorEnum.FILE_USER_OVER.getName());
                }
            }
            Double sizetotal = ObjectUtils.isEmpty(lastFile) ? fileSize : lastFile.getFileSizeTotal() + fileSize;

            //如果保存的文件非永久，则进行一个度的校验
            if (null != upFileDTO.getSaveTime()) {
                //如果文件大小符合条件存入缓存，则进人redis流程
                if (fileSize <= maxFile) {
                    String base64 = null;
                    base64 = Base64.encode(file.getBytes());

                    if (StringUtils.isNotEmpty(base64)) {
                        //计算存储时间
                        LocalDateTime saveTime = upFileDTO.getSaveTime();
                        Duration duration = Duration.between(saveTime, LocalDateTime.now());
                        long saveSec = duration.getSeconds();
                        //将小量文件存入redis中进行保存
                        cacheExe.setCacheKey("file/" + userId, base64, saveSec);
                    }
                    //TODO 缓存过期事件，更新数据库
                } else {
                    //TODO 走定时任务，到过期时间时，将存储的文件删除
                }
            } else {
                //如果需要的是永久保存 如果小于5G 则进行累计计算，并将文件转入磁盘中
                File saveFile = new File(ServerCode.FILE_ADDRESS + userId+"/"+fileEnum.getName()+"/"+name);
                if(!saveFile.getParentFile().exists()){
                    saveFile.getParentFile().mkdirs();
                }
                file.transferTo(saveFile);
            }

            FileInfoE.queryInstance().setUserId(userId)
                    .setFileSize(fileSize)
                    .setFileSizeTotal(sizetotal)
                    .setName(file.getOriginalFilename())
                    .setFileType(fileEnum.getValue()).save();
        } catch (IOException e) {
            log.error(e);
            AssertUtil.isFalse(true, ErrorEnum.SERVER_ERROR.getName());
        }
        return DataResponse.buildSuccess();
    }

    /**
     * 删除文件
     *
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public DataResponse deleteFile (String id) {

        FileInfoCO fileInfoCO = FileInfoE.queryInstance().setId(id).selectById();
        AssertUtil.isFalse(null == fileInfoCO, ErrorEnum.SELECT_NOT_FOUND.getName());

        //逻辑删除数据条 按照创建时间排序查询 第一条的内存总量就是当前用户的内存总量
        FileInfoE.queryInstance().setId(id).setDeleted(1).update();

        //物理删除文件
        File file = new File(ServerCode.FILE_ADDRESS + fileInfoCO.getUserId() + "/" + fileInfoCO.getName());
        AssertUtil.isFalse(!file.exists(), ErrorEnum.SELECT_NOT_FOUND.getName());
        file.delete();
        return DataResponse.buildSuccess();
    }

    /**
     * 获取文件
     *
     * @param id 文件id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public File getFile (String id) {

        //获取文件数据
        FileInfoCO fileInfoCO = FileInfoE.queryInstance().setId(id).selectById();
        AssertUtil.isFalse(ObjectUtils.isEmpty(fileInfoCO), ErrorEnum.SELECT_NOT_FOUND.getName());

        File file = FileUtil.getFile(fileInfoCO.getName(), fileInfoCO.getUserId(),
                FileEnum.loadName(fileInfoCO.getFileType()).getName());
        return file;

    }
}
