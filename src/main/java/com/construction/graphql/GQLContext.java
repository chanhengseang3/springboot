package com.construction.graphql;

import com.construction.user.authentication.domain.AppUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class GQLContext {

    public enum AccessLevel {
        ANONYMOUS, PARTIAL, FULL
    }

    private AppUser user;
    private Integer queryDepthLimit;
    private Integer querySizeLimit;
    private Integer queryTimeout;
    private String otpToken;
    private AccessLevel accessLevel;

}
