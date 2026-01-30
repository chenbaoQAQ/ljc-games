package ljc.entity;

import lombok.Data;

@Data
public class UserProfile {

    private Integer userId;

    private Long gold;

    private Long diamond;

    private String unlockedCountries;
}