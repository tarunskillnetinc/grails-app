package uk.co.wonderlane.wlpos.helpers;

import grails.gorm.PagedResultList;
import org.grails.datastore.mapping.query.Query;

import java.util.List;

public class TestPagedResultList<T> extends PagedResultList<T> {
    public TestPagedResultList(Query query) {
        super(query);
    }

    public TestPagedResultList(List resultSet) {
        super(null);
        this.resultList = resultSet;
    }

    protected void initialize() {
        if (totalCount == Integer.MIN_VALUE) {
            totalCount = this.resultList.size();
        }
    }
}
