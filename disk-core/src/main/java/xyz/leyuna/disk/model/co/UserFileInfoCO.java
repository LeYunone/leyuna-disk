package xyz.leyuna.disk.model.co;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author LeYuna
 * @email 365627310@qq.com
 * @create 2022-03-22 09:33
 */
@Getter
@Setter
@ToString
public class UserFileInfoCO {

    /**
     * 用户内存总量
     */
    private Long fileTotal;

    /**
     * 用户文件
     */
    private List<FileInfoCO> fileinfos;
}
