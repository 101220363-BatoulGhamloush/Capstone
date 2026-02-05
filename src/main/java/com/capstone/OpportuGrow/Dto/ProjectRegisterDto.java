package com.capstone.OpportuGrow.Dto;

import com.capstone.OpportuGrow.model.ProjectType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public class ProjectRegisterDto {
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getFundingGoal() {
        return fundingGoal;
    }

    public void setFundingGoal(Double fundingGoal) {
        this.fundingGoal = fundingGoal;
    }

    public Integer getFundingDuration() {
        return fundingDuration;
    }

    public void setFundingDuration(Integer fundingDuration) {
        this.fundingDuration = fundingDuration;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private String title;
    private String shortDescription;
    private String longDescription;
    private String category;
    private Double fundingGoal;
    private Integer fundingDuration; // بالأيام
    private String imageUrl;
    private MultipartFile imageFile;

    private List<MultipartFile> galleryFiles;
    private String videoUrl;
    private ProjectType type;
    private BigDecimal platformFee;
    private BigDecimal processingFee;
    private BigDecimal amountYouWillReceive;
    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String address;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(BigDecimal platformFee) {
        this.platformFee = platformFee;
    }

    public BigDecimal getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(BigDecimal processingFee) {
        this.processingFee = processingFee;
    }

    public BigDecimal getAmountYouWillReceive() {
        return amountYouWillReceive;
    }

    public void setAmountYouWillReceive(BigDecimal amountYouWillReceive) {
        this.amountYouWillReceive = amountYouWillReceive;
    }

    public List<MultipartFile> getGalleryFiles() {
        return galleryFiles;
    }

    public void setGalleryFiles(List<MultipartFile> galleryFiles) {
        this.galleryFiles = galleryFiles;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public ProjectType getType() {
        return type;
    }

    public void setType(ProjectType type) {
        this.type = type;
    }


    public MultipartFile getImageFile() { return imageFile; }
    public void setImageFile(MultipartFile imageFile) { this.imageFile = imageFile; }// لاحقاً بعد رفع الصور


}

