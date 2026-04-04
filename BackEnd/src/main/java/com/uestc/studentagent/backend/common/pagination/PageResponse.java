package com.uestc.studentagent.backend.common.pagination;

import java.util.List;

public record PageResponse<T>(
        Integer page,
        Integer size,
        Long total,
        List<T> records
) {
}
