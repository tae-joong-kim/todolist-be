package com.navercorp.newbie.todolist.domain;

import com.navercorp.newbie.todolist.config.FileSafer;
import com.navercorp.newbie.todolist.dto.TodoCreateForm;
import com.navercorp.newbie.todolist.dto.TodoUpdateForm;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    private String uploadFileName;
    private String storeFileName;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;


    public void create(TodoCreateForm todoCreateForm) throws Exception {
        MultipartFile multipartFile = todoCreateForm.getFile();

        if(multipartFile != null){
            this.uploadFileName = multipartFile.getOriginalFilename();
            this.storeFileName = getStoreFileName(this.uploadFileName);
        }

        this.title = todoCreateForm.getTitle();
        this.description = todoCreateForm.getDescription();
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = this.createdAt;
    }

    public void update(TodoUpdateForm todoUpdateForm) throws Exception {
        MultipartFile multipartFile = todoUpdateForm.getFile();

        if(multipartFile != null){
            this.uploadFileName = multipartFile.getOriginalFilename();
            this.storeFileName = getStoreFileName(this.uploadFileName);
        }

        this.title = todoUpdateForm.getTitle();
        this.description = todoUpdateForm.getDescription();
        this.modifiedAt = LocalDateTime.now();
    }

    public void setFileName() throws Exception {
        this.uploadFileName = null;
        this.storeFileName = null;
    }

    public String getStoreFileName(String uploadFileName) {
        String ext = uploadFileName.substring(uploadFileName.lastIndexOf(".") + 1);
        String uuid = UUID.randomUUID().toString();

        return uuid + "." + ext;
    }
}
