package com.navercorp.newbie.todolist.service;

import com.navercorp.newbie.todolist.domain.Todo;
import com.navercorp.newbie.todolist.dto.TodoCreateForm;
import com.navercorp.newbie.todolist.dto.TodoUpdateForm;
import com.navercorp.newbie.todolist.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class TodoServiceTest {

    @Autowired TodoService todoService;
    @Autowired TodoRepository todoRepository;

    TodoCreateForm getCreateForm(String title, String description, MultipartFile multipartFile) {
        TodoCreateForm todoCreateForm = new TodoCreateForm();
        todoCreateForm.setTitle(title);
        todoCreateForm.setDescription(description);
        todoCreateForm.setFile(multipartFile);
        return todoCreateForm;
    }

    TodoUpdateForm getUpdateForm(String title, String description, MultipartFile file, Boolean isDone) {
        TodoUpdateForm todoUpdateForm = new TodoUpdateForm();
        todoUpdateForm.setTitle(title);
        todoUpdateForm.setDescription(description);
        todoUpdateForm.setFile(file);
        todoUpdateForm.setIsDone(isDone);
        return todoUpdateForm;
    }

    @BeforeEach
    void beforeEach() throws Exception {
        // given
        String title1 = "테스트 제목 - 1";
        String description1 = "테스트 본문 - 1";
        MockMultipartFile mockMultipartFile1 = new MockMultipartFile("테스트-1.png", "테스트 데이터 - 1".getBytes(StandardCharsets.UTF_8));

        TodoCreateForm todoCreateForm1 = getCreateForm(title1, description1, mockMultipartFile1);

        todoService.createTodo(todoCreateForm1);
        System.out.println("todoCreateForm1 = " + todoCreateForm1);
    }

    @Test
    @Transactional
    void createTodo() throws Exception {

        // given
        String title = "테스트 제목";
        String description = "테스트 본문";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("테스트.png", "테스트 데이터".getBytes(StandardCharsets.UTF_8));

        TodoCreateForm todoCreateForm = getCreateForm(title, description, mockMultipartFile);

        // when
        Todo todo = todoService.createTodo(todoCreateForm);

        // then
        assertThat(todo.getId()).isNotNull();
        assertThat(todo.getTitle()).isEqualTo(title);
        assertThat(todo.getDescription()).isEqualTo(description);
        assertThat(todo.getUploadFileName()).isEqualTo(mockMultipartFile.getOriginalFilename());
        assertThat(todo.getStoreFileName()).isNotNull();
        assertThat(todo.getIsDone()).isEqualTo(Boolean.FALSE);
        assertThat(todo.getCreatedAt()).isNotNull();
        assertThat(todo.getModifiedAt()).isNotNull();
    }

    @Test
    @Transactional
    void readTodoAsList() throws Exception {

        // given
        String title = "테스트 제목";
        String description = "테스트 본문";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("테스트.png", "테스트 데이터".getBytes(StandardCharsets.UTF_8));

        TodoCreateForm todoCreateForm = getCreateForm(title, description, mockMultipartFile);

        List<Todo> beforeTodos = todoService.readTodoAsList();

        // when
        Todo todo = todoService.createTodo(todoCreateForm);
        List<Todo> afterTodos = todoService.readTodoAsList();

        // then
        assertThat(afterTodos.size()).isEqualTo(beforeTodos.size() + 1);
        assertThat(afterTodos.get(afterTodos.size()-1)).isEqualTo(todo);
    }

    @Test
    @Transactional
    void readDetailTodo() {

        // given
        Todo repositoryTodo = todoRepository.findById(1L).get();

        // when
        Todo serviceTodo = todoService.readDetailTodo(1L);

        // then
        assertThat(serviceTodo.getId()).isEqualTo(repositoryTodo.getId());
        assertThat(serviceTodo.getTitle()).isEqualTo(repositoryTodo.getTitle());
        assertThat(serviceTodo.getDescription()).isEqualTo(repositoryTodo.getDescription());
        assertThat(serviceTodo.getUploadFileName()).isEqualTo(repositoryTodo.getUploadFileName());
        assertThat(serviceTodo.getStoreFileName()).isEqualTo(repositoryTodo.getStoreFileName());
        assertThat(serviceTodo.getIsDone()).isEqualTo(repositoryTodo.getIsDone());
        assertThat(serviceTodo.getCreatedAt()).isEqualTo(repositoryTodo.getCreatedAt());
        assertThat(serviceTodo.getModifiedAt()).isEqualTo(repositoryTodo.getModifiedAt());
    }

    @Test
    void deleteTodo() {

        // given
        List<Todo> beforeTodos = todoService.readTodoAsList();

        // when
        todoService.deleteTodo(1L);

        List<Todo> afterTodos = todoService.readTodoAsList();

        // then
        assertThat(afterTodos.size()).isEqualTo(beforeTodos.size() - 1);
    }

    @Test
    void readTodoByStoreFileName() throws Exception {

        // given
        String title = "테스트 제목";
        String description = "테스트 본문";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("테스트.png", "테스트 데이터".getBytes(StandardCharsets.UTF_8));

        TodoCreateForm todoCreateForm = getCreateForm(title, description, mockMultipartFile);

        // when
        Todo createdTodo = todoService.createTodo(todoCreateForm);
        Todo foundTodo = todoService.readTodoByStoreFileName(createdTodo.getStoreFileName());

        // then
        assertThat(foundTodo.getId()).isEqualTo(createdTodo.getId());
        assertThat(foundTodo.getTitle()).isEqualTo(createdTodo.getTitle());
        assertThat(foundTodo.getDescription()).isEqualTo(createdTodo.getDescription());
        assertThat(foundTodo.getUploadFileName()).isEqualTo(createdTodo.getUploadFileName());
        assertThat(foundTodo.getStoreFileName()).isEqualTo(createdTodo.getStoreFileName());
        assertThat(foundTodo.getIsDone()).isEqualTo(createdTodo.getIsDone());
        assertThat(foundTodo.getCreatedAt()).isEqualTo(createdTodo.getCreatedAt());
        assertThat(foundTodo.getModifiedAt()).isEqualTo(createdTodo.getModifiedAt());
    }
}