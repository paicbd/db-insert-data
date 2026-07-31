package org.paic.insertdata.config;

import com.paicbd.smsc.utils.Generated;
import org.paic.insertdata.util.ApplicationMode;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Generated
public class LogsCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String mode = context.getEnvironment().getProperty("application.mode");
        return ApplicationMode.LOGS.getValue().equalsIgnoreCase(mode) ||
                ApplicationMode.LOGS_DATABASE.getValue().equalsIgnoreCase(mode);
    }
}
