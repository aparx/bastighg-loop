package io.github.aparx.challenges.looping.command;

import javax.validation.constraints.NotNull;
import java.lang.annotation.*;

/**
 * @author aparx (Vinzent Zeband)
 * @version 18:45 CET, 04.08.2022
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ChallengeCommand {

    @NotNull
    String value();

}
