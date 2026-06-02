package com.example.demo.modules.template.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("templates")
public class Template {
    @TableId
    private String id;
    private String name;
    private String previewImage;
    private String type;
    private Boolean isPremium;
    private Boolean isActive;
    private String config;
    private String tags;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
