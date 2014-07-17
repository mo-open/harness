package org.mds.harness.common.http;

import org.apache.http.entity.ContentType;

/**
 * Created by Randall.mo on 14-6-9.
 */
public enum ContentTypeEnum {
    APPLICATION_ATOM_XML(ContentType.APPLICATION_ATOM_XML),
    APPLICATION_JSON(ContentType.APPLICATION_JSON),
    APPLICATION_SVG_XML(ContentType.APPLICATION_SVG_XML),
    APPLICATION_XHTML_XML(ContentType.APPLICATION_XHTML_XML),
    APPLICATION_XML(ContentType.APPLICATION_XML),
    TEXT_HTML(ContentType.TEXT_HTML),
    TEXT_PLAIN(ContentType.TEXT_PLAIN),
    TEXT_XML(ContentType.TEXT_XML);
    private String value;

    private ContentTypeEnum(ContentType contentType) {
        this.value = contentType.getMimeType();
    }

    public String value() {
        return this.value;
    }
}
