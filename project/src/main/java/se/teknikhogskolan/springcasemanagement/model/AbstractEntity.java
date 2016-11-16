package se.teknikhogskolan.springcasemanagement.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractEntity {

    @Id
    @GeneratedValue
    private Long id;

    @CreatedDate
    @Column(updatable = false)
    private LocalDate created;

    @LastModifiedDate
    private LocalDate lastModified;

    public Long getId() {
        return id;
    }

    public LocalDate getCreated() {
        return created;
    }

    public LocalDate getLastModified() {
        return lastModified;
    }
    
    public String lastModifiedToString() {
        return this.lastModified == null ? "null" : lastModified.toString();
    }
    
    public String createdDateToString() {
        return this.created == null ? "null" : created.toString();
    }
}