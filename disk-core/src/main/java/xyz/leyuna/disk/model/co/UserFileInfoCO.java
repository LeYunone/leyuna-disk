package xyz.leyuna.disk.model.co;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author pengli
 * @create 2022-03-22 09:33
 */
@Getter
@Setter
@ToString
public class UserFileInfoCO {

    /**
     * 用户内存总量
     */
    private Double fileTotal;

    /**
     * 用户文件
     */
    private Page<FileInfoCO> fileinfos;
}
