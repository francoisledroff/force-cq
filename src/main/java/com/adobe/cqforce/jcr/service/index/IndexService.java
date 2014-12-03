package com.adobe.cqforce.jcr.service.index;

import java.util.List;
import java.util.Map;


public interface IndexService {

    Map<String, byte[]> getIndexedContent(List<? extends Indexable> list, String propertyName);

    Map<String, byte[]> getIndexedContentAll(List<?> list, String idxName);

    String getIndexKey(String value);
}
