package io.lostinreality.lir_android_app;

/**
 * Created by jose on 30/05/16.
 */
public class Author {
    private Long numberId;
    private String fullName;
    private String avatarUrl;

    public Author(Long nId, String fname, String avatarurl) {
        this.numberId = nId;
        this.fullName = fname;
        this.avatarUrl = avatarurl;
    }

    public Long getId() {
        return numberId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAvatarUrl() {
        if (this.avatarUrl == null)
            return "/assets/images/lir-logo.png";
        if (Constants.reg_exp_pattern.matcher(this.avatarUrl).find()) {
            return avatarUrl;
        } else {
            return Constants.LIR_SERVER_URL_DOMAIN + avatarUrl;
        }
    }

}
