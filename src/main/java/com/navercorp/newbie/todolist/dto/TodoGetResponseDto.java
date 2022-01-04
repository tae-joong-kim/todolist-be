package com.navercorp.newbie.todolist.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoGetResponseDto {
    private Long id;
    private String title;
    private Boolean isDone;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}