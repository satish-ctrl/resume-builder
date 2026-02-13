package com.satish.resumebuilderapi.service;

import com.satish.resumebuilderapi.document.Resume;
import com.satish.resumebuilderapi.dto.AuthResponse;
import com.satish.resumebuilderapi.dto.CreateResumeRequest;
import com.satish.resumebuilderapi.repository.ResumeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final AuthService authService;

    public Resume createResume(CreateResumeRequest request, Object principalObject) {
//        step 1: create resume object
        Resume newResume = new Resume();

//        step 2: Get the current profile
        AuthResponse response = authService.getProfile(principalObject);

//        step 3: update the resume object
        newResume.setUserId(response.getId());
        newResume.setTitle(request.getTitle());

//        step 4: set default data for resume
        setDefaultResumeData(newResume);

//        step 5: save the resume data
        return resumeRepository.save(newResume);
    }

    private void setDefaultResumeData(Resume newResume) {
        newResume.setProfileInfo(new Resume.ProfileInfo());
        newResume.setContactInfo(new Resume.ContactInfo());
        newResume.setWorkExperiences(new ArrayList<>());
        newResume.setEducations(new ArrayList<>());
        newResume.setSkills(new ArrayList<>());
        newResume.setProjects(new ArrayList<>());
        newResume.setCertifications(new ArrayList<>());
        newResume.setLanguages(new ArrayList<>());
        newResume.setInterests(new ArrayList<>());
    }

    public List<Resume> getUserResumes(Object principal) {

//        step 1 : get the current profile
        AuthResponse response = authService.getProfile(principal);

//        Step 2: call the repository
        List<Resume> resumes = resumeRepository.findByUserIdOrderByUpdatedAtDesc(response.getId());

//        step 3: return result
        return resumes;
    }

    public Resume getResumeById(String resumeId, Object principal) {

//        step 1: get the current profile
        AuthResponse response = authService.getProfile(principal);

//        step 2: call the  repo finder method
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(),resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

//        step 3: return the result
        return existingResume;
    }

    public Resume updateResume(String resumeId, Resume updatedData, Object principal) {

//        step 1: get the current profile
        AuthResponse response = authService.getProfile(principal);

//        step 2: call the repository finder method
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(),resumeId)
                .orElseThrow(()-> new RuntimeException("Resume not found"));

//        step 3: update the new data
        existingResume.setTitle(updatedData.getTitle());
        existingResume.setThumbnailLink(updatedData.getThumbnailLink());
        existingResume.setTemplate(updatedData.getTemplate());
        existingResume.setProfileInfo(updatedData.getProfileInfo());
        existingResume.setContactInfo(updatedData.getContactInfo());
        existingResume.setWorkExperiences(updatedData.getWorkExperiences());
        existingResume.setEducations(updatedData.getEducations());
        existingResume.setSkills(updatedData.getSkills());
        existingResume.setProjects(updatedData.getProjects());
        existingResume.setLanguages(updatedData.getLanguages());
        existingResume.setCertifications(updatedData.getCertifications());
        existingResume.setInterests(updatedData.getInterests());

//        step 4: save the details into database
        resumeRepository.save(existingResume);

//        step 5: return result
        return existingResume;

    }

    public void deleteResume(String resumeId, Object principal) {

//        step 1: get the current profile
        AuthResponse response = authService.getProfile(principal);

//        step 2: call the repository finder method
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(), resumeId)
                .orElseThrow(()-> new RuntimeException("Resume not found"));

        resumeRepository.delete(existingResume);
    }
}
