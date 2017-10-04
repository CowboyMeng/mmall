package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: Chou_meng
 * @Date: 2017/9/26
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
