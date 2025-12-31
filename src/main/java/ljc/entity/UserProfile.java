package ljc.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "user_profile")
public class UserProfile {
    @Id
    private Integer userId;

    private Integer gold;
    private Integer diamond;
    private String unlockedCountries; // 存储如 "CN,EN" 的字符串
}