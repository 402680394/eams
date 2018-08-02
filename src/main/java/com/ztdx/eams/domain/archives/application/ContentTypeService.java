package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.model.ContentType;
import com.ztdx.eams.domain.archives.repository.ContentTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentTypeService {
    private ContentTypeRepository contentTypeRepository;

    public ContentTypeService(ContentTypeRepository contentTypeRepository) {
        this.contentTypeRepository = contentTypeRepository;
    }

    public List<ContentType> list(){
        return contentTypeRepository.findAll();
    }
}
