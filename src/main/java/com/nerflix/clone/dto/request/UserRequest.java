package com.nerflix.clone.dto.request;

import lombok.Data;

@Data
public class UserRequest {

        private String email;
        private String password;
        private String fullname;
        private String role;
        private Boolean active;

}
