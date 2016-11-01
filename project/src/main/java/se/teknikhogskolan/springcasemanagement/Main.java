package se.teknikhogskolan.springcasemanagement;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan("se.teknikhogskolan.springcasemanagement");
            context.refresh();
        }
    }
}
