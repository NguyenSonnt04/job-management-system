package Nhom08.Project.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserProfileUpdateDTO {

    @Size(max = 100, message = "Ho ten khong duoc vuot qua 100 ky tu")
    private String fullName;

    @Size(max = 20, message = "So dien thoai khong duoc vuot qua 20 ky tu")
    @Pattern(
        regexp = "^[0-9+\\-()\\s]{0,20}$",
        message = "So dien thoai chi duoc chua so va cac ky tu + - ( )"
    )
    private String phone;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
