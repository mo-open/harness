package org.mds.harness.model.ignite;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;

public class IndexedData implements Serializable {
    @QuerySqlField(index = true)
    private long id;
    @QuerySqlField
    private String name;
    @QuerySqlField
    private String address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
