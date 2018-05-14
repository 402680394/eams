package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.model.Archives;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 档案库服务
 */
@Service
public class ArchivesService {

    private final ArchivesRepository archivesRepository;

    /**
     * 构造函数
     */
    @Autowired
    public ArchivesService(ArchivesRepository archivesRepository) {
        this.archivesRepository = archivesRepository;
    }

    /**
     * 更新档案库
     */
    public void save(Archives archives){
        archivesRepository.save(archives);
    }

    public Archives get(Integer id){
        Optional<Archives> archives = archivesRepository.findById(id);
        return  archives.get();
    }
}