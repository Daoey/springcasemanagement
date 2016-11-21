package se.teknikhogskolan.springcasemanagement.auditing;

import org.springframework.data.domain.AuditorAware;

public class IssueAuditorAware implements AuditorAware<String> {

    @Override
    public String getCurrentAuditor() {
        return System.getProperty("user.name");
    }
}
