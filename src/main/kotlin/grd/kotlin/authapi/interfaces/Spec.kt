package grd.kotlin.authapi.interfaces

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class Spec<T> : Specification<T>
{
    private lateinit var criteria: SearchCriteria

    override fun toPredicate(root: Root<T?>, query: CriteriaQuery<*>?, builder: CriteriaBuilder): Predicate?
    {
        if(criteria.getOperation().equals(">"))
            return builder.greaterThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString())
        else if(criteria.getOperation().equals("<"))
            return builder.lessThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString())
        else if(criteria.getOperation().equals(":"))
            return if(root.get<String>(criteria.getKey()).javaType === String::class.java)
                builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%")
            else
                builder.equal(root.get<String>(criteria.getKey()), criteria.getValue())

        return null
    }
}

class SearchCriteria
{
    private var key: String? = null
    private var operation: String? = null
    private var value: Any? = null

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

enum class SearchOperation
{
    EQUALITY,
    NEGATION,
    GREATER_THAN,
    LESS_THAN,
    LIKE,
    STARTS_WITH,
    ENDS_WITH,
    CONTAINS;

    companion object
    {
        val SIMPLE_OPERATION_SET = arrayOf(":", "!", ">", "<", "~")
        const val OR_PREDICATE_FLAG = "'"
        const val ZERO_OR_MORE_REGEX = "*"
        const val OR_OPERATOR = "OR"
        const val AND_OPERATOR = "AND"
        const val LEFT_PARANTHESIS = "("
        const val RIGHT_PARANTHESIS = ")"
        fun getSimpleOperation(input: Char): SearchOperation?
        {
            return when(input)
            {
                ':' -> EQUALITY
                '!' -> NEGATION
                '>' -> GREATER_THAN
                '<' -> LESS_THAN
                '~' -> LIKE
                else -> null
            }
        }
    }
}