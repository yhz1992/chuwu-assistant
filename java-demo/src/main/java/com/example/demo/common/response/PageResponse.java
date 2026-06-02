package com.example.demo.common.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 分页响应体，继承自 ApiResponse
 *
 * @param <T> 列表数据类型
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class PageResponse<T> extends ApiResponse<List<T>> {

    /** 分页信息 */
    private Pagination pagination;

    private PageResponse() {
    }

    private PageResponse(int code, String message, List<T> data, Pagination pagination) {
        super.setCode(code);
        super.setMessage(message);
        super.setData(data);
        this.pagination = pagination;
    }

    /**
     * 成功分页响应
     *
     * @param data     列表数据
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param total    总记录数
     * @param <T>      数据类型
     * @return PageResponse
     */
    public static <T> PageResponse<T> ok(List<T> data, long page, long pageSize, long total) {
        Pagination pagination = new Pagination(page, pageSize, total, page * pageSize < total);
        return new PageResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, pagination);
    }

    /**
     * 分页信息内部类
     */
    @Data
    public static class Pagination {
        /** 当前页码 */
        private long page;

        /** 每页大小 */
        private long pageSize;

        /** 总记录数 */
        private long total;

        /** 是否有更多数据 */
        private boolean hasMore;

        public Pagination(long page, long pageSize, long total, boolean hasMore) {
            this.page = page;
            this.pageSize = pageSize;
            this.total = total;
            this.hasMore = hasMore;
        }
    }
}
