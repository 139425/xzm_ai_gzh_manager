package com.xzm.xzm_ai_gzh_manager.model.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 微信公众号账号
 * @TableName wx_account
 */
@TableName(value ="wx_account")
public class WxAccount {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * appid
     */
    private String appid;

    /**
     * 公众号名称
     */
    private String name;

    /**
     * 认证状态
     */
    private Boolean verified;

    /**
     * appSecret
     */
    private String secret;

    /**
     * token
     */
    private String token;

    /**
     * aesKey
     */
    private String aeskey;

    /**
     * 创建用户 id
     */
    private Long userid;

    /**
     * 创建时间
     */
    private Date createtime;

    /**
     * 更新时间
     */
    private Date updatetime;

    /**
     * 是否删除
     */
    private Integer isdelete;

    /**
     * id
     */
    public Long getId() {
        return id;
    }

    /**
     * id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * appid
     */
    public String getAppid() {
        return appid;
    }

    /**
     * appid
     */
    public void setAppid(String appid) {
        this.appid = appid;
    }

    /**
     * 公众号名称
     */
    public String getName() {
        return name;
    }

    /**
     * 公众号名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 认证状态
     */
    public Boolean getVerified() {
        return verified;
    }

    /**
     * 认证状态
     */
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    /**
     * appSecret
     */
    public String getSecret() {
        return secret;
    }

    /**
     * appSecret
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * token
     */
    public String getToken() {
        return token;
    }

    /**
     * token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * aesKey
     */
    public String getAeskey() {
        return aeskey;
    }

    /**
     * aesKey
     */
    public void setAeskey(String aeskey) {
        this.aeskey = aeskey;
    }

    /**
     * 创建用户 id
     */
    public Long getUserid() {
        return userid;
    }

    /**
     * 创建用户 id
     */
    public void setUserid(Long userid) {
        this.userid = userid;
    }

    /**
     * 创建时间
     */
    public Date getCreatetime() {
        return createtime;
    }

    /**
     * 创建时间
     */
    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    /**
     * 更新时间
     */
    public Date getUpdatetime() {
        return updatetime;
    }

    /**
     * 更新时间
     */
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    /**
     * 是否删除
     */
    public Integer getIsdelete() {
        return isdelete;
    }

    /**
     * 是否删除
     */
    public void setIsdelete(Integer isdelete) {
        this.isdelete = isdelete;
    }

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
        WxAccount other = (WxAccount) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAppid() == null ? other.getAppid() == null : this.getAppid().equals(other.getAppid()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getVerified() == null ? other.getVerified() == null : this.getVerified().equals(other.getVerified()))
            && (this.getSecret() == null ? other.getSecret() == null : this.getSecret().equals(other.getSecret()))
            && (this.getToken() == null ? other.getToken() == null : this.getToken().equals(other.getToken()))
            && (this.getAeskey() == null ? other.getAeskey() == null : this.getAeskey().equals(other.getAeskey()))
            && (this.getUserid() == null ? other.getUserid() == null : this.getUserid().equals(other.getUserid()))
            && (this.getCreatetime() == null ? other.getCreatetime() == null : this.getCreatetime().equals(other.getCreatetime()))
            && (this.getUpdatetime() == null ? other.getUpdatetime() == null : this.getUpdatetime().equals(other.getUpdatetime()))
            && (this.getIsdelete() == null ? other.getIsdelete() == null : this.getIsdelete().equals(other.getIsdelete()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAppid() == null) ? 0 : getAppid().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getVerified() == null) ? 0 : getVerified().hashCode());
        result = prime * result + ((getSecret() == null) ? 0 : getSecret().hashCode());
        result = prime * result + ((getToken() == null) ? 0 : getToken().hashCode());
        result = prime * result + ((getAeskey() == null) ? 0 : getAeskey().hashCode());
        result = prime * result + ((getUserid() == null) ? 0 : getUserid().hashCode());
        result = prime * result + ((getCreatetime() == null) ? 0 : getCreatetime().hashCode());
        result = prime * result + ((getUpdatetime() == null) ? 0 : getUpdatetime().hashCode());
        result = prime * result + ((getIsdelete() == null) ? 0 : getIsdelete().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", appid=").append(appid);
        sb.append(", name=").append(name);
        sb.append(", verified=").append(verified);
        sb.append(", secret=").append(secret);
        sb.append(", token=").append(token);
        sb.append(", aeskey=").append(aeskey);
        sb.append(", userid=").append(userid);
        sb.append(", createtime=").append(createtime);
        sb.append(", updatetime=").append(updatetime);
        sb.append(", isdelete=").append(isdelete);
        sb.append("]");
        return sb.toString();
    }
}