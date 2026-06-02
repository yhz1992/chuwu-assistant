package com.example.demo.modules.feedback.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反馈实体
 * 对应数据库表 feedbacks
 */
@Data
@TableName("feedbacks")
public class Feedback {

    /** 反馈 ID，格式：fb_ + 雪花ID */
    @TableId
    private String id;

    /** 用户 ID（游客可为空） */
    private String userId;

    /** 反馈类型：feature-功能建议，bug-问题反馈，template-模板需求，other-其他 */
    private String type;

    /** 反馈内容 */
    private String content;

    /** 联系方式 */
    private String contact;

    /** 图片列表（JSON 数组） */
    @TableField()
    private String images;

    /** 处理状态：pending-待处理，processing-处理中，done-已处理 */
    private String status;

    /** 处理人 ID */
    private String handlerId;

    /** 处理备注 */
    private String handlerNote;

    /** 逻辑删除时间 */
    private LocalDateTime deletedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
