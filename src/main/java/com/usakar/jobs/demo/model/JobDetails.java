package com.usakar.jobs.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.usakar.jobs.demo.util.JobUtil;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.quartz.Trigger;

@Builder(toBuilder = true)
public class JobDetails {

    @NotBlank
    @Getter
    private String name;
    @NotEmpty
    @Getter
    @Setter
    private String subject;
    @NotEmpty
    @Getter
    @Setter
    private String messageBody;
    @NotEmpty
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private Map<String, Object> data;
    @JsonProperty("triggers")
    private List<TriggerDetails> triggerDetails;

    public List<String> getTo() {
        return to;
    }

    public List<String> getCc() {
        return cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    @JsonIgnore
    public Set<Trigger> buildTriggers() {
        Set<Trigger> triggers = new LinkedHashSet<>();
        for (TriggerDetails triggerDetails : this.triggerDetails) {
            triggers.add(JobUtil.buildTrigger(triggerDetails));
        }

        return triggers;
    }
}
