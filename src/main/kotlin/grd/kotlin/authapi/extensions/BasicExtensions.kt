package grd.kotlin.authapi.extensions

import com.chook.api.enums.*
import java.time.*
import java.util.*
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

class BasicExtensions

/**
 * Check if a nullable value is null.
 * @param this value to check
 * @return True if null
 **/
fun Any?.isNull(): Boolean
{
    return this == null
}

/**
 * Check if a nullable value is not null.
 * @param this value to check
 * @return True if not null
 **/
fun Any?.isNotNull(): Boolean
{
    return this != null
}

/**
 * Check if [this] Class has [annotationClass] given.
 * @param this value to check
 * @param annotationClass Class of annotation
 * @return True if present
 **/
fun Any.hasAnnotation(annotationClass: Class<out Annotation>): Boolean
{
    return this.javaClass.isAnnotationPresent(annotationClass)
}

/**
 * Check if [this] KProperty has [annotationClass] given.
 * @param this value to check
 * @param annotationClass Class of annotation
 * @return True if present
 **/
fun KProperty<*>.hasAnnotation(annotationClass: Class<out Annotation>): Boolean
{
    val annotation = this.javaField!!.getDeclaredAnnotation(annotationClass)
    return !annotation.isNull()
}
