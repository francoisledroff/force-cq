package com.adobe.cqforce.jcr.service.index;


import com.adobe.cqforce.util.CommonUtils;
import com.adobe.cqforce.util.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IndexServiceImpl implements IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);

    private Index index;

    public IndexServiceImpl(Index index) {
        this.index = index;
    }

    @Override
    public Map<String, byte[]> getIndexedContent(List<? extends Indexable> list, String propertyName) {
        long t1 = System.currentTimeMillis();
        Map<String, byte[]> data = new HashMap<String, byte[]>();

        byte[] separator = {','};
        byte[] arrayStart = {'['};
        byte[] arrayEnd = {']'};

        for (Indexable entity : list) {
            String propertyValue = entity.getPropertyValue(propertyName);

            if (propertyValue != null) {
                String key = index.getIndexKey(propertyValue);

                if (index.isValidIndexKey(key)) {
                    byte[] content = data.get(key);
                    byte[] userContent = entity.getContent();

                    if (content == null) {
                        content = CommonUtils.concatenateArrays(arrayStart, userContent, arrayEnd);

                    } else {
                        content[content.length - 1] = ' ';
                        content = CommonUtils.concatenateArrays(content, separator, userContent, arrayEnd);
                    }

                    data.put(key, content);
                }

            }
        }
        logger.debug("Indexing for property [" + propertyName + "] took [" + (System.currentTimeMillis() - t1) + "]ms");

        return data;
    }

    @Override
    public Map<String, byte[]> getIndexedContentAll(List<?> list, String idxName) {
        Map<String, byte[]> data = new HashMap<String, byte[]>();

        byte[] idxContent = GsonUtil.toJson(list).getBytes();
        data.put(idxName, idxContent);

        return data;
    }

    @Override
    public String getIndexKey(String value) {
        return index.getIndexKey(value);
    }

}
