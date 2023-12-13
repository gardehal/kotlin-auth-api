package grd.kotlin.authapi.interfaces

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class Spec<T> : Specification<T>
{
    private val criteria: SearchCriteria? = null

    override fun toPredicate(root: Root<T?>, query: CriteriaQuery<*>?, builder: CriteriaBuilder): Predicate?
    {
        if(criteria.getOperation().equalsIgnoreCase(">"))
        {
            return builder.greaterThanOrEqualTo(
                root.< String > get < kotlin . String ? > criteria.getKey(), criteria.getValue().toString()
            )
        } else if(criteria.getOperation().equalsIgnoreCase("<"))
        {
            return builder.lessThanOrEqualTo(
                root.< String > get < kotlin . String ? > criteria.getKey(), criteria.getValue().toString()
            )
        } else if(criteria.getOperation().equalsIgnoreCase(":"))
        {
            return if(root.get(criteria.getKey()).getJavaType() === String::class.java)
            {
                builder.like(
                    root.< String > get < kotlin . String ? > criteria.getKey(), "%" + criteria.getValue() + "%"
                )
            } else
            {
                builder.equal(root.get(criteria.getKey()), criteria.getValue())
            }
        }
        return null
    }
}

class SearchCriteria
{
    private var key: String? = null
    private var operation: String? = null
    private var value: Any? = null

    fun SearchCriteria(key: String?, operation: String?, value: Any?)
    {
        super()
        this.key = key
        this.operation = operation
        this.value = value
    }

    fun getKey(): String?
    {
        return key
    }

    fun setKey(key: String?)
    {
        this.key = key
    }

    fun getOperation(): String?
    {
        return operation
    }

    fun setOperation(operation: String?)
    {
        this.operation = operation
    }

    fun getValue(): Any?
    {
        return value
    }

    fun setValue(value: Any?)
    {
        this.value = value
    }
}

class SpecSearchCriteria
{
    private val key: String? = null
    private val operation: SearchOperation? = null
    private val value: Any? = null
    val isOrPredicate = false
}