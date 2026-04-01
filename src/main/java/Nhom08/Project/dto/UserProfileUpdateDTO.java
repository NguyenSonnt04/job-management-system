package Nhom08.Project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserProfileUpdateDTO {

    @Size(max = 100, message = "Ho ten khong duoc vuot qua 100 ky tu")
    private String fullName;

    @Size(max = 20, message = "So dien thoai khong duoc vuot qua 20 ky tu")
    @Pattern(regexp = "^[0-9+\\-()\\s]{0,20}$", message = "So dien thoai chi duoc chua so va cac ky tu + - ( )")
    private String phone;

    @Email(message = "Email lien he khong hop le")
    @Size(max = 100)
    private String contactEmail;

    @Size(max = 20)
    private String dob;

    @Size(max = 120, message = "Nghe nghiep khong duoc vuot qua 120 ky tu")
    private String occupation;

    private String skills;
    private String education;
    private String experience;
    private String projects;

    // Getters & Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getProjects() { return projects; }
    public void setProjects(String projects) { this.projects = projects; }
}
