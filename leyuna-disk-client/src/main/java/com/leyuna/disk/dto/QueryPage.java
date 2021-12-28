package com.leyuna.disk.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author pengli
 * @create 2021-12-09 16:19
 */
@Getter
@Setter
@ToString
@Accessors( chain = true)
public class QueryPage {

    private Integer index=10;

    private Integer size=1;
}
