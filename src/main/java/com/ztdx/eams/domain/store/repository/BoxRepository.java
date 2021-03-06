package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.Box;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.List;

/**
 * Created by li on 2018/7/9.
 */
@Repository
@Table(name = "store_box")
@Qualifier("boxRepository")
public interface BoxRepository extends JpaRepository<Box, Integer> {

    //查询盒号是否存在
    boolean existsByCodeAndArchivesId(String code, int archivesId);

    //通过ID修改信息
    @Modifying
    @Query("update Box b set b.codeRule=:#{#box.codeRule},b.code=:#{#box.code},b.flowNumber=:#{#box.flowNumber},b.width=:#{#box.width},b.maxPagesTotal=:#{#box.maxPagesTotal},b.remark=:#{#box.remark} where b.id=:#{#box.id}")
    void updateById(@Param("box") Box box);

    //通过ID修改上架信息
    @Modifying
    @Query("update Box b set b.onFrame=:onFrame,b.point=:point where b.id=:id")
    void updateOnFrameById(@Param("id") int id, @Param("onFrame") int onFrame, @Param("point") String point);

    Box findByArchivesIdAndCode(int archiveId, String code);

    @Modifying
    @Query("update Box b set b.pagesTotal = :pages, b.filesTotal = :files where b.code = :code and b.archivesId = :archivesId")
    void updateTotal(@Param("code") String code, @Param("archivesId") int archivesId, @Param("pages") int pages, @Param("files") int files);

    List<Box> findByIdIn(List<Integer> ids);

    @Modifying
    @Query("update Box b set b.onFrame = :onFrame, b.point = :beforePoint where b.point = :point")
    void updateOnFrameByPoint(@Param("onFrame") int onFrame, @Param("beforePoint") String beforePoint, @Param("point") String point);
}
