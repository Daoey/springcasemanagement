package se.teknikhogskolan.springcasemanagement.auditing;

import org.springframework.data.domain.AuditorAware;

public final class IssueAuditorAware implements AuditorAware<String> {

    @Override
    public String getCurrentAuditor() {
        return System.getProperty("user.name");
    }
}
