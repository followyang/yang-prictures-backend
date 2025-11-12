package com.yang.yangpicturesbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 空间
 * @TableName space
 */
@TableName(value ="space")
@Data
public class Space {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 空间名称
     */
    @TableField(value ="spaceName")
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    @TableField(value ="spaceLevel")
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    @TableField(value ="maxSize")
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    @TableField(value ="maxCount")
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    @TableField(value ="totalSize")
    private Long totalSize;

    /**
     * 空间类型：0-私有 1-团队
     */
    private Integer spaceType;


    /**
     * 当前空间下的图片数量
     */
    @TableField(value ="totalCount")
    private Long totalCount;

    /**
     * 创建用户 id
     */
    @TableField(value ="userId")
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(value ="createTime")
    private Date createTime;

    /**
     * 编辑时间
     */
    @TableField(value ="editTime")
    private Date editTime;

    /**
     * 更新时间
     */
    @TableField(value ="updateTime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField(value ="isDelete")
    private Integer isDelete;

}