package uk.co.wonderlane.wlpos.helpers

import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.grails.datastore.mapping.query.api.Criteria
import org.grails.datastore.mapping.query.api.QueryableCriteria

import javax.persistence.criteria.JoinType

class HibernateTestMockCriteria implements BuildableCriteria {

    class FakeSearchResultList<T> extends ArrayList<T> {
        public int totalCount = 0
    }

    List responses = new FakeSearchResultList()

    @Override
    Class getTargetClass() {
        return null
    }

    @Override
    Criteria exists(QueryableCriteria<?> subquery) {
        return null
    }

    @Override
    Criteria notExists(QueryableCriteria<?> subquery) {
        return null
    }

    @Override
    Criteria idEquals(Object value) {
        return null
    }

    @Override
    Criteria isEmpty(String propertyName) {
        return null
    }

    @Override
    Criteria isNotEmpty(String propertyName) {
        return null
    }

    @Override
    Criteria isNull(String propertyName) {
        return null
    }

    @Override
    Criteria isNotNull(String propertyName) {
        return null
    }

    @Override
    Criteria eq(String propertyName, Object propertyValue) {
        return null
    }

    @Override
    Criteria idEq(Object propertyValue) {
        return null
    }

    @Override
    Criteria ne(String propertyName, Object propertyValue) {
        return null
    }

    @Override
    Criteria between(String propertyName, Object start, Object finish) {
        return null
    }

    @Override
    Criteria gte(String property, Object value) {
        return null
    }

    @Override
    Criteria ge(String property, Object value) {
        return null
    }

    @Override
    Criteria gt(String property, Object value) {
        return null
    }

    @Override
    Criteria lte(String property, Object value) {
        return null
    }

    @Override
    Criteria le(String property, Object value) {
        return null
    }

    @Override
    Criteria lt(String property, Object value) {
        return null
    }

    @Override
    Criteria like(String propertyName, Object propertyValue) {
        return null
    }

    @Override
    Criteria ilike(String propertyName, Object propertyValue) {
        return null
    }

    @Override
    Criteria rlike(String propertyName, Object propertyValue) {
        return null
    }

    @Override
    Criteria and(@DelegatesTo(Criteria.class) Closure callable) {
        return null
    }

    @Override
    Criteria or(@DelegatesTo(Criteria.class) Closure callable) {
        return null
    }

    @Override
    Criteria not(@DelegatesTo(Criteria.class) Closure callable) {
        return null
    }

    @Override
    Criteria "in"(String propertyName, Collection values) {
        return null
    }

    @Override
    Criteria "in"(String propertyName, QueryableCriteria<?> subquery) {
        return null
    }

    @Override
    Criteria inList(String propertyName, QueryableCriteria<?> subquery) {
        return null
    }

    @Override
    Criteria "in"(String propertyName, Closure<?> subquery) {
        return null
    }

    @Override
    Criteria inList(String propertyName, Closure<?> subquery) {
        return null
    }

    @Override
    Criteria inList(String propertyName, Collection values) {
        return null
    }

    @Override
    Criteria inList(String propertyName, Object[] values) {
        return null
    }

    @Override
    Criteria "in"(String propertyName, Object[] values) {
        return null
    }

    @Override
    Criteria notIn(String propertyName, QueryableCriteria<?> subquery) {
        return null
    }

    @Override
    Criteria notIn(String propertyName, Closure<?> subquery) {
        return null
    }

    @Override
    Criteria order(String propertyName) {
        return null
    }

    @Override
    Criteria order(Query.Order o) {
        return null
    }

    @Override
    Criteria order(String propertyName, String direction) {
        return null
    }

    @Override
    Criteria sizeEq(String propertyName, int size) {
        return null
    }

    @Override
    Criteria sizeGt(String propertyName, int size) {
        return null
    }

    @Override
    Criteria sizeGe(String propertyName, int size) {
        return null
    }

    @Override
    Criteria sizeLe(String propertyName, int size) {
        return null
    }

    @Override
    Criteria sizeLt(String propertyName, int size) {
        return null
    }

    @Override
    Criteria sizeNe(String propertyName, int size) {
        return null
    }

    @Override
    Criteria eqProperty(String propertyName, String otherPropertyName) {
        return null
    }

    @Override
    Criteria neProperty(String propertyName, String otherPropertyName) {
        return null
    }

    @Override
    Criteria gtProperty(String propertyName, String otherPropertyName) {
        return null
    }

    @Override
    Criteria geProperty(String propertyName, String otherPropertyName) {
        return null
    }

    @Override
    Criteria ltProperty(String propertyName, String otherPropertyName) {
        return null
    }

    @Override
    Criteria leProperty(String propertyName, String otherPropertyName) {
        return null
    }

    @Override
    Criteria allEq(Map<String, Object> propertyValues) {
        return null
    }

    @Override
    Criteria eqAll(String propertyName, Closure<?> propertyValue) {
        return null
    }

    @Override
    Criteria gtAll(String propertyName, Closure<?> propertyValue) {
        return null
    }

    @Override
    Criteria ltAll(String propertyName, Closure<?> propertyValue) {
        return null
    }

    @Override
    Criteria geAll(String propertyName, Closure<?> propertyValue) {
        return null
    }

    @Override
    Criteria leAll(String propertyName, Closure<?> propertyValue) {
        return null
    }

    @Override
    Criteria eqAll(String propertyName, QueryableCriteria propertyValue) {
        return null
    }

    @Override
    Criteria gtAll(String propertyName, QueryableCriteria propertyValue) {
        return null
    }

    @Override
    Criteria ltAll(String propertyName, QueryableCriteria propertyValue) {
        return null
    }

    @Override
    Criteria geAll(String propertyName, QueryableCriteria propertyValue) {
        return null
    }

    @Override
    Criteria leAll(String propertyName, QueryableCriteria propertyValue) {
        return null
    }

    @Override
    Criteria gtSome(String propertyName, QueryableCriteria propertyValue) {
        return null
    }

    @Override
    Criteria gtSome(String propertyName, Closure<?> propertyValue) {
        return null
    }

    @Override
    Criteria geSome(String propertyName, QueryableCriteria propertyValue) {
        return null
    }

    @Override
    Criteria geSome(String propertyName, Closure<?> propertyValue) {
        return null
    }

    @Override
    Criteria ltSome(String propertyName, QueryableCriteria propertyValue) {
        return null
    }

    @Override
    Criteria ltSome(String propertyName, Closure<?> propertyValue) {
        return null
    }

    @Override
    Criteria leSome(String propertyName, QueryableCriteria propertyValue) {
        return null
    }

    @Override
    Criteria leSome(String propertyName, Closure<?> propertyValue) {
        return null
    }

    @Override
    BuildableCriteria cache(boolean cache) {
        return null
    }

    @Override
    BuildableCriteria readOnly(boolean readOnly) {
        return null
    }

    @Override
    BuildableCriteria join(String property) {
        return null
    }

    @Override
    BuildableCriteria join(String property, JoinType joinType) {
        return null
    }

    @Override
    BuildableCriteria select(String property) {
        return null
    }

    @Override
    Object list(@DelegatesTo(Criteria.class) Closure closure) {
        closure.setDelegate(this)
        closure.call()
        return responses
    }

    @Override
    Object list(Map params, @DelegatesTo(Criteria.class) Closure closure) {
        closure.setDelegate(this)
        closure.call()
        return responses
    }

    @Override
    Object listDistinct(@DelegatesTo(Criteria.class) Closure closure) {
        return null
    }

    @Override
    Object scroll(@DelegatesTo(Criteria.class) Closure closure) {
        return null
    }

    @Override
    Object get(@DelegatesTo(Criteria.class) Closure closure) {
        closure.setDelegate(this)
        closure.call()
        return responses
    }
}
