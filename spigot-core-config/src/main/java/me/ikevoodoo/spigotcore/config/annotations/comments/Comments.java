package me.ikevoodoo.spigotcore.config.annotations.comments;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Comments {

    String[] value() default { "" };

    String prefix() default " ";

}
