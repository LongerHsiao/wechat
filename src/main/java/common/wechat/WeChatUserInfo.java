package common.wechat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * @author LongerHsiao
 * @date 2017-10-23
 */
public class WeChatUserInfo implements Serializable {
    private static final long serialVersionUID = -6385045918828076924L;
    private String openid;
    private String nickname;
    private String sex;
    private String province;
    private String city;
    private String country;
    private String headImgUrl;
    private String[] privilege;
    private String unionid;
    private Date subscribeTime;

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    public String[] getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String[] privilege) {
        this.privilege = privilege;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public Date getSubscribeTime() {
        return subscribeTime;
    }

    public void setSubscribeTime(Date subscribeTime) {
        this.subscribeTime = subscribeTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeChatUserInfo that = (WeChatUserInfo) o;

        if (openid != null ? !openid.equals(that.openid) : that.openid != null) return false;
        if (nickname != null ? !nickname.equals(that.nickname) : that.nickname != null) return false;
        if (sex != null ? !sex.equals(that.sex) : that.sex != null) return false;
        if (province != null ? !province.equals(that.province) : that.province != null) return false;
        if (city != null ? !city.equals(that.city) : that.city != null) return false;
        if (country != null ? !country.equals(that.country) : that.country != null) return false;
        if (headImgUrl != null ? !headImgUrl.equals(that.headImgUrl) : that.headImgUrl != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(privilege, that.privilege)) return false;
        if (unionid != null ? !unionid.equals(that.unionid) : that.unionid != null) return false;
        return subscribeTime != null ? subscribeTime.equals(that.subscribeTime) : that.subscribeTime == null;
    }

    @Override
    public int hashCode() {
        int result = openid != null ? openid.hashCode() : 0;
        result = 31 * result + (nickname != null ? nickname.hashCode() : 0);
        result = 31 * result + (sex != null ? sex.hashCode() : 0);
        result = 31 * result + (province != null ? province.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (headImgUrl != null ? headImgUrl.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(privilege);
        result = 31 * result + (unionid != null ? unionid.hashCode() : 0);
        result = 31 * result + (subscribeTime != null ? subscribeTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WeChatUserInfo{" +
                "openid='" + openid + '\'' +
                ", nickname='" + nickname + '\'' +
                ", sex='" + sex + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", headImgUrl='" + headImgUrl + '\'' +
                ", privilege=" + Arrays.toString(privilege) +
                ", unionid='" + unionid + '\'' +
                ", subscribeTime=" + subscribeTime +
                '}';
    }
}
