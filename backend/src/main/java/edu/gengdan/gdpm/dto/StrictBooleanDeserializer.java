package edu.gengdan.gdpm.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import edu.gengdan.gdpm.common.ApiException;
import java.io.IOException;

/** 审核结果必须为 JSON 布尔值，拒绝字符串和数字的隐式转换。 */
public class StrictBooleanDeserializer extends JsonDeserializer<Boolean> {

    /** 只接受 true/false，失败时保留审核接口的中文参数提示。 */
    @Override
    public Boolean deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.hasToken(JsonToken.VALUE_TRUE)) {
            return true;
        }
        if (parser.hasToken(JsonToken.VALUE_FALSE)) {
            return false;
        }
        throw new ApiException(400, "approved必须为布尔值");
    }
}
