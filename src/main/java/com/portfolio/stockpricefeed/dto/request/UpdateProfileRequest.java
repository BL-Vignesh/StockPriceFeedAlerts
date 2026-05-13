package com.portfolio.stockpricefeed.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String displayName;
    private String email;
    private String phone;
    private String address;
}
