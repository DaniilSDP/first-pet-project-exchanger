package org.example.firstpetprojectexchanger.dto;

import org.springframework.data.domain.Page;



import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(

        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last

) {

	public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    
    }
}

/*
«record»
PageResponse

T
+content: List<T>
+page: int
+size: int
+totalElements: long
+totalPages: int
+first: boolean
+last: boolean
+<E,T> of(Page<E>, Function<E,T>): PageResponse<T>
 */