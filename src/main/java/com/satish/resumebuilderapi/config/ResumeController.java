package com.satish.resumebuilderapi.config;

import com.satish.resumebuilderapi.document.Resume;
import com.satish.resumebuilderapi.dto.CreateResumeRequest;
import com.satish.resumebuilderapi.service.FileUploadService;
import com.satish.resumebuilderapi.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.satish.resumebuilderapi.util.AppConstants.*;

@RestController
@RequestMapping(RESUME)
@RequiredArgsConstructor
@Slf4j
public class ResumeController {
    private final ResumeService resumeService;
    private final FileUploadService fileUploadService;

    @PostMapping
    public ResponseEntity<?> createResume(@Valid @RequestBody CreateResumeRequest request, Authentication authentication){

//        step 1 : call the service method
       Resume newResume = resumeService.createResume(request, authentication.getPrincipal());

//   step 2: return response
        return ResponseEntity.status(HttpStatus.CREATED).body(newResume);
    }

    @GetMapping
    public ResponseEntity<?> getUserResumes(Authentication authentication){
//        step 1 call the service method
       List<Resume> resumes = resumeService.getUserResumes(authentication.getPrincipal());

//        step 2 return the response
        return ResponseEntity.ok(resumes);
    }

    @GetMapping(ID)
    public ResponseEntity<?> getResumeById(@PathVariable String id,
                                           Authentication authentication){
//        step 1 : call the service methode
        Resume existingResume = resumeService.getResumeById(id, authentication.getPrincipal());

//        step 2 : return the response
        return ResponseEntity.ok(existingResume);
    }

    @PutMapping(ID)
    public ResponseEntity<?> updateResume(@PathVariable String id ,
                                          @RequestBody Resume updatedData,
                                          Authentication authentication){
//        step 1: call the service method
        Resume updateResume = resumeService.updateResume(id, updatedData, authentication.getPrincipal());

//        step 2: return the response
        return ResponseEntity.ok(updateResume);
    }

    @PutMapping(UPLOAD_IMAGE)
    public ResponseEntity<?> uploadResumeImages(@PathVariable String id,
                                                @RequestPart(value = "thumbnail", required = true) MultipartFile thumbnail,
                                                @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
                                                Authentication authentication) throws IOException {
//        step 1: call the service method
       Map<String, String> response = fileUploadService.uploadResumeImages(id, authentication.getPrincipal(), thumbnail,profileImage);

//        ste 2: return the response
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(ID)
    public ResponseEntity<?> deleteResume(@PathVariable String id,
                                          Authentication authentication){

//        step 1: call the service method
        resumeService.deleteResume(id, authentication.getPrincipal());

//        step 2: return response
        return ResponseEntity.ok(Map.of("message", "Resume deleted successfull"));
    }
}
