package com.app.citysparsh.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileStorageService {

    @Autowired
    private Cloudinary cloudinary;

    public Map<String, String> uploadFile(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder",    "citysparsh/complaints",
                        "resource_type", "auto"
                )
        );

        String url      = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");
        String fileName = file.getOriginalFilename();

        return Map.of(
                "url",      url,
                "publicId", publicId,
                "name",     fileName
        );
    }

    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
