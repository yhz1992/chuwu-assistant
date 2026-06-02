package com.example.demo.modules.template.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.modules.template.entity.Template;
import com.example.demo.modules.template.mapper.TemplateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TemplateService {

    private final TemplateMapper templateMapper;

    public TemplateService(TemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    public List<Template> getList(String type, Boolean isPremium) {
        LambdaQueryWrapper<Template> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Template::getIsActive, true);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Template::getType, type);
        }
        if (isPremium != null) {
            wrapper.eq(Template::getIsPremium, isPremium);
        }
        wrapper.orderByAsc(Template::getSortOrder);
        return templateMapper.selectList(wrapper);
    }

    public Template getDetail(String id) {
        return templateMapper.selectById(id);
    }
}
