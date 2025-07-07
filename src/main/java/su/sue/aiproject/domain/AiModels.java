package su.sue.aiproject.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 
 * @TableName ai_models
 */
@TableName(value ="ai_models")
@Data
public class AiModels {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String apiEndpoint;

    /**
     * 
     */
    private BigDecimal costPer1kTokens;

    /**
     * 
     */
    private Boolean isEnabled;

    /**
     * 
     */
    private String modelName;

    /**
     * 
     */
    private String provider;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        AiModels other = (AiModels) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getApiEndpoint() == null ? other.getApiEndpoint() == null : this.getApiEndpoint().equals(other.getApiEndpoint()))
            && (this.getCostPer1kTokens() == null ? other.getCostPer1kTokens() == null : this.getCostPer1kTokens().equals(other.getCostPer1kTokens()))
            && (this.getIsEnabled() == null ? other.getIsEnabled() == null : this.getIsEnabled().equals(other.getIsEnabled()))
            && (this.getModelName() == null ? other.getModelName() == null : this.getModelName().equals(other.getModelName()))
            && (this.getProvider() == null ? other.getProvider() == null : this.getProvider().equals(other.getProvider()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getApiEndpoint() == null) ? 0 : getApiEndpoint().hashCode());
        result = prime * result + ((getCostPer1kTokens() == null) ? 0 : getCostPer1kTokens().hashCode());
        result = prime * result + ((getIsEnabled() == null) ? 0 : getIsEnabled().hashCode());
        result = prime * result + ((getModelName() == null) ? 0 : getModelName().hashCode());
        result = prime * result + ((getProvider() == null) ? 0 : getProvider().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", apiEndpoint=").append(apiEndpoint);
        sb.append(", costPer1kTokens=").append(costPer1kTokens);
        sb.append(", isEnabled=").append(isEnabled);
        sb.append(", modelName=").append(modelName);
        sb.append(", provider=").append(provider);
        sb.append("]");
        return sb.toString();
    }
}