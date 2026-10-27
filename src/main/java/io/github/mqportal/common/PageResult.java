package io.github.mqportal.common;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long page;
    private long size;

    public static <T> PageResult<T> of(List<T> records, long total, long page, long size) {
        PageResult<T> result = new PageResult<>();
        result.records = records;
        result.total = total;
        result.page = page;
        result.size = size;
        return result;
    }
}
