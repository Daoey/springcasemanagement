package se.teknikhogskolan.springcasemanagement.service.wrapper;
import java.util.Iterator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class Piece<T> {
    private Page<?> page;
    
    public Piece(Page<?> page) {
        this.page = page;
    }
    
    @Override
    public String toString() {
        return page.toString();
    }
    
    public int getNumber() {
        return page.getNumber();
    }
    
    public int getSize() {
        return page.getSize();
    }
    
    public int getNumberOfElements() {
        return page.getNumberOfElements();
    }
    
    public List<T> getContent() {
        return (List<T>) page.getContent();
    }
    
    public boolean hasContent() {
        return page.hasContent();
    }
    
    public Sort getSort() {
        return page.getSort();
    }
    
    public boolean isFirst() {
        return page.isFirst();
    }
    
    public boolean isLast() {
        return page.isLast();
    }
    
    public boolean hasNext() {
        return page.hasNext();
    }
    
    public boolean hasPrevious() {
        return page.hasPrevious();
    }
    
    public Pageable nextPageable() {
        return page.nextPageable();
    }
    
    public Pageable previousPageable() {
        return page.previousPageable();
    }

    public Iterator<T> iterator() {
        return (Iterator<T>) page.iterator();
    }
    
    public int getTotalPages() {
        return page.getTotalPages();
    }
    
    public long getTotalElements() {
        return getTotalElements();
    }
}
