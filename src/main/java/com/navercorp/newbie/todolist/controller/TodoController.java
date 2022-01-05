package com.navercorp.newbie.todolist.controller;

import com.navercorp.newbie.todolist.domain.Todo;
import com.navercorp.newbie.todolist.dto.TodoCreateForm;
import com.navercorp.newbie.todolist.dto.TodoGetDetailResponseDto;
import com.navercorp.newbie.todolist.dto.TodoGetResponseDto;
import com.navercorp.newbie.todolist.dto.TodoUpdateForm;
import com.navercorp.newbie.todolist.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todo")
public class TodoController {
    
    private final TodoService todoService;
    private final ModelMapper modelMapper;

    @PostMapping
    public TodoGetDetailResponseDto createTodo(TodoCreateForm todoCreateForm) throws Exception {
        Todo todo = todoService.createTodo(todoCreateForm);
        return modelMapper.map(todo, TodoGetDetailResponseDto.class);
    }

    @GetMapping
    public List<TodoGetResponseDto> readTodoAsList(){
        return todoService.readTodoAsList().stream()
                .map(todo -> modelMapper.map(todo, TodoGetResponseDto.class))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TodoGetDetailResponseDto readDetailTodo(@PathVariable Long id){
        Todo todo = todoService.readDetailTodo(id);
        return modelMapper.map(todo, TodoGetDetailResponseDto.class);
    }

    @PutMapping("/{id}")
    public TodoGetResponseDto UpdateTodo(@PathVariable Long id, TodoUpdateForm todoUpdateForm) throws Exception {
        Todo todo = todoService.updateTodo(id, todoUpdateForm);
        return modelMapper.map(todo, TodoGetResponseDto.class);
    }

    @DeleteMapping("/{id}")
    public void deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
    }
}
