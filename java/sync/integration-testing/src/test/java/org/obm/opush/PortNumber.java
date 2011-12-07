package org.obm.opush;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

@Retention(RetentionPolicy.RUNTIME) 
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) 
@BindingAnnotation
public @interface PortNumber {

}
