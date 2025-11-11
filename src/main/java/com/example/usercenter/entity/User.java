package  com.example.usercenter.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class User {
    private String username;
    private Long id;
    private String userAccount;
    private String avatarUrl;
    private Integer gender;
    private String userPassword;
    private String phone;
    private String email;
    private Integer userStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isDelete;
    private Integer userRole;
    private String planetCode;
}