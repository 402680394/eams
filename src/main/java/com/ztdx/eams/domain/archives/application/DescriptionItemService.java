package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.utils.DateUtils;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DescriptionItemService {

    private final DescriptionItemRepository descriptionItemRepository;

    @Autowired
    public DescriptionItemService(DescriptionItemRepository descriptionItemRepository) {
        this.descriptionItemRepository = descriptionItemRepository;
    }

    public <R> Map<String, R> list(int catalogueId, Function<DescriptionItem, R> map) {
        return descriptionItemRepository.findByCatalogueId(catalogueId).stream()
                .collect(
                        Collectors.toMap(
                                DescriptionItem::getMetadataName
                                , map
                                , (d1, d2) -> d2));
    }

    //新增条目数据验证
    public Entry addVerification(Entry entry, UserCredential userCredential) {
        //获取目录著录项
        List<DescriptionItem> descriptionItemList = descriptionItemRepository.findByCatalogueId(entry.getCatalogueId());
        //著录项数据
        Map<String, Object> dataMap = entry.getItems();
        //著录项验证
        for (DescriptionItem descriptionItem : descriptionItemList) {
            //获取著录项名称
            String metadataName = descriptionItem.getMetadataName();
            //获取数据
            Object value = entry.getItems().get(metadataName);
            //是否可空
            //如果著录项不能为空，并且数据也为空
            if ((descriptionItem.getIsNull() == 0 && "" == value) || (descriptionItem.getIsNull() == 0 && null == value)) {
                throw new InvalidArgumentException(descriptionItem.getDisplayName() + "不能为空");
                //如果著录项可为空，并且数据也为空且有默认值
            } else if ((descriptionItem.getIsNull() == 1 && "" == value) || (descriptionItem.getIsNull() == 1 && null == value)) {

                switch (descriptionItem.getDefaultValue()) {
                    //当前登录人姓名
                    case LoginUserName: {
                        dataMap.put(metadataName, userCredential.getName());
                    }
                    //当前系统年度
                    case SystemYear: {
                        dataMap.put(metadataName, DateUtils.getCurrentYear());
                    }
                    case SystemDate_yyyy_MM_dd: {
                        dataMap.put(metadataName, DateUtils.getCurrentDateTime("yyyy-MM-dd"));
                    }
                    case SystemDate_yyyyMMdd: {
                        dataMap.put(metadataName, DateUtils.getCurrentDateTime("yyyyMMdd"));
                    }
                    case SystemDateTime: {
                        dataMap.put(metadataName, DateUtils.getCurrentDateTime("yyyy-MM-dd HH:mm:ss"));
                    }
                    case SystemTime: {
                        dataMap.put(metadataName, DateUtils.getCurrentDateTime("HH:mm:ss"));
                    }
                }
                //数据不为空时
            } else if ("" != value && null != value) {
                //著录项类型为数值型
                if (descriptionItem.getDataType() == 1) {
                    try {
                        Integer.parseInt(String.valueOf(value));
                    } catch (NumberFormatException e) {
                        throw new InvalidArgumentException(descriptionItem.getDisplayName() + "必须为数值");
                    }
                }
                //著录项类型为日期型
                if (descriptionItem.getDataType() == 3) {
                    if (!checkString("((((19|20)\\d{2})-(0?(1|[3-9])|1[012])-(0?[1-9]|[12]\\d|30))|(((19|20)\\d{2})-(0?[13578]|1[02])-31)|(((19|20)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|((((19|20)([13579][26]|[2468][048]|0[48]))|(2000))-0?2-29))$", (String) value)) {
                        throw new InvalidArgumentException(descriptionItem.getDisplayName() + "必须为yyyy-MM-dd日期格式");
                    }
                }
                //著录项类型为数值型
                if (descriptionItem.getDataType() == 4) {
                    try {
                        Double.parseDouble(String.valueOf(value));
                    } catch (NumberFormatException e) {
                        throw new InvalidArgumentException(descriptionItem.getDisplayName() + "必须为浮点数值");
                    }
                }
            }
        }
        entry.setItems(dataMap);
        return entry;
    }

    //修改条目数据验证
    public Entry updateVerification(Entry entry, UserCredential userCredential) {
        //获取目录著录项
        List<DescriptionItem> descriptionItemList = descriptionItemRepository.findByCatalogueId(entry.getCatalogueId());
        //著录项数据
        Map<String, Object> dataMap = entry.getItems();
        //著录项验证
        for (DescriptionItem descriptionItem : descriptionItemList) {
            //获取著录项名称
            String metadataName = descriptionItem.getMetadataName();
            //获取值
            Object value = dataMap.get(metadataName);
            //是否只读
            if (descriptionItem.getIsRead() == 0) {
                dataMap.put(metadataName, null);
            } else {
                //如果著录项不能为空，并且数据也为空
                if ((descriptionItem.getIsNull() == 1 && "" == value) || (descriptionItem.getIsNull() == 1 && null == value)) {
                    throw new InvalidArgumentException(descriptionItem.getDisplayName() + "不能为空");
                    //如果著录项可为空，并且数据也为空且有默认值
                } else if ((descriptionItem.getIsNull() == 0 && "" == value) || (descriptionItem.getIsNull() == 0 && null == value)) {

                    switch (descriptionItem.getDefaultValue()) {
                        //当前登录人姓名
                        case LoginUserName: {
                            dataMap.put(metadataName, userCredential.getName());
                        }
                        //当前系统年度
                        case SystemYear: {
                            dataMap.put(metadataName, DateUtils.getCurrentYear());
                        }
                        case SystemDate_yyyy_MM_dd: {
                            dataMap.put(metadataName, DateUtils.getCurrentDateTime("yyyy-MM-dd"));
                        }
                        case SystemDate_yyyyMMdd: {
                            dataMap.put(metadataName, DateUtils.getCurrentDateTime("yyyyMMdd"));
                        }
                        case SystemDateTime: {
                            dataMap.put(metadataName, DateUtils.getCurrentDateTime("yyyy-MM-dd HH:mm:ss"));
                        }
                        case SystemTime: {
                            dataMap.put(metadataName, DateUtils.getCurrentDateTime("HH:mm:ss"));
                        }
                    }
                    //数据不为空时
                } else if ("" != value && null != value) {
                    //著录项类型为数值型
                    if (descriptionItem.getDataType() == 1) {
                        try {
                            Integer.parseInt(String.valueOf(value));
                        } catch (NumberFormatException e) {
                            throw new InvalidArgumentException(descriptionItem.getDisplayName() + "必须为数值");
                        }
                    }
                    //著录项类型为日期型
                    if (descriptionItem.getDataType() == 3) {
                        if (!checkString("((((19|20)\\d{2})-(0?(1|[3-9])|1[012])-(0?[1-9]|[12]\\d|30))|(((19|20)\\d{2})-(0?[13578]|1[02])-31)|(((19|20)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|((((19|20)([13579][26]|[2468][048]|0[48]))|(2000))-0?2-29))$", (String) value)) {
                            throw new InvalidArgumentException(descriptionItem.getDisplayName() + "必须为yyyy-MM-dd日期格式");
                        }
                    }
                    //著录项类型为数值型
                    if (descriptionItem.getDataType() == 4) {
                        try {
                            Double.parseDouble(String.valueOf(value));
                        } catch (NumberFormatException e) {
                            throw new InvalidArgumentException(descriptionItem.getDisplayName() + "必须为浮点数值");
                        }
                    }
                }
            }

        }
        entry.setItems(dataMap);
        return entry;
    }

    public boolean checkString(String pattern, String string) {
        return string.matches(pattern);
    }
}
