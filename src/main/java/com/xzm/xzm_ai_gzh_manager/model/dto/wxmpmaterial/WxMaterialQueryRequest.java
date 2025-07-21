package com.xzm.xzm_ai_gzh_manager.model.dto.wxmpmaterial;


import com.xzm.xzm_ai_gzh_manager.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WxMaterialQueryRequest extends PageRequest {
    private String materialType;
}
