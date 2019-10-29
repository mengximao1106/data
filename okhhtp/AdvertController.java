/**
 * Copyright (C): 恒大集团版权所有 Evergrande Group
 * FileName: AdvertController
 * Author:   mengximao
 * Date:     2018-04-18 11:06
 * Description: 广告管理控制类
 */
package com.evergrande.smc.controller;

import com.evergrande.smc.anotation.SystemControllerLogPc;
import com.evergrande.smc.common.config.AdvertConfig;
import com.evergrande.smc.common.exception.BusinessException;
import com.evergrande.smc.common.model.ErrorCode;
import com.evergrande.smc.common.model.RestResult;
import com.evergrande.smc.common.util.IdWorkerUtil;
import com.evergrande.smc.config.DfsConfig;
import com.evergrande.smc.service.AdvertService;
import com.evergrande.smc.vo.AdvertVO;
import com.google.common.base.Throwables;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 广告图片管理控制类
 *
 * @author mengximao
 * @since 1.0.0
 */
@RestController
@RequestMapping("/imageMng")
@Api(value = "AdvertController", tags = "advert", description = "广告图片管理")
public class AdvertController extends BaseController{

    private Logger logger = LoggerFactory.getLogger(AdvertController.class);

    @Autowired
    private DfsConfig dfsConfig;

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private AdvertConfig advertConfig;

    @Autowired
    private AdvertService advertService;

    /**
     * 本类的下载方法的url拼接文件路径参数
     */
    private static final String DOWNLOAD_IMAGE_URL = "/imageMng/download?objectName=";

    private final SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyyMM");

    /**
     *  单个上传广告图片
     * @param objectName 为重新命名文件名称，不需要文件后缀名
     * @param mdfObjectNameFlag 图片名称文件名后台不可以修改：ture，不可修改；false或者不传，可修改。
     * @param file 表单文件上传按钮
     * @throws BusinessException
     */
    @PostMapping(value="/uploadOne/nowDate")
    @ApiOperation(value = "uploadImage", notes = "单个文件上传接口：<br>" +
            "1）使用当前日期作为路径；<br>" +
            "2）默认为系统生成的ID为存储文件名称;<br>" +
            "3）mdfObjectNameFlag 为'ture'文件名后台不可以修改,不传或者为false文件名可修改；<br>" +
            "4）mdfObjectNameFlag为true、传入objectName为重新命名文件名称，不需要文件后缀名;<br>" +
            "必填字段：file;<br>" +
            "非必填字段：objectName, mdfObjectNameFlag;", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", name = "Authorization", required=true)
    @SystemControllerLogPc(description = "上传广告图片",module = "广告管理")
    public RestResult uploadImage(@RequestParam(required = false) String objectName,
                                  @RequestParam(required = false) boolean mdfObjectNameFlag,
                                  @RequestPart("file") MultipartFile file) throws BusinessException{
        String objectNamePath = "";
        try {
            //文件后缀类型
            String fileTpye = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            //文件名不可修改
            if(mdfObjectNameFlag){
                if(objectName==null){
                    objectName = file.getOriginalFilename().substring(0,file.getOriginalFilename().lastIndexOf("."));
                }
                objectName = objectName + fileTpye ;
            }else{//可修改,默认文件名，如： 20180423105724988250456872980480.png
                objectName = IdWorkerUtil.generateIdStartWithDate() + fileTpye;
            }

            objectNamePath = sdfMonth.format(new Date()) + "/" +objectName;

            okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/octet-stream"), file.getBytes());
            okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("bucketName", dfsConfig.getBucketName())
                    .addFormDataPart("objectName", objectNamePath)
                    .addFormDataPart("file", file.getOriginalFilename(), fileBody)
                    .build();
            Request request = new Request.Builder()
                    .url(dfsConfig.getUrl() + "/api/dfs/file/")
                    // 需要加上token验证
                    .addHeader(dfsConfig.getHeader(), dfsConfig.getToken())
                    .post(requestBody)
                    .build();
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            if(!response.isSuccessful()){
                return  returnFailed("文件上传失败：" + response.message());
            }
        }catch (Exception e){
            logger.error("AdvertController.uploadImage " + Throwables.getStackTraceAsString(e));
            throw BusinessException.define(ErrorCode.UPLOAD_FILE_EXCEPTION);
        }

        return  returnSuccess(DOWNLOAD_IMAGE_URL + objectNamePath);
    }

    /**
     * 查询该项目下所有图片信息
     * @return
     * @throws BusinessException
     */
    @GetMapping(value="/findAll")
    @ApiOperation(value = "findAllImages", notes = "查询所有广告图片接口", httpMethod = "GET")
    @ApiImplicitParam(paramType = "header", name = "Authorization", required=true)
    public RestResult findAllImages() throws BusinessException {

        return returnSuccess(advertService.saveAndFindList(advertConfig, DOWNLOAD_IMAGE_URL, getLoginUser().getLoginName()));
    }

    /**
     * 保存图片信息
     * @param advertList 广告图片信息列表
     * @return
     * @throws BusinessException
     */
    @PostMapping(value="/save")
    @ApiOperation(value = "saveImage", notes = "保存广告图片接口<br>" +
            "必填字段：sort, imageUrl;", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", name = "Authorization", required=true)
    @SystemControllerLogPc(description="保存广告图片信息" ,module="广告管理")
    public RestResult saveImage(@RequestBody List<AdvertVO> advertList) throws BusinessException{
        advertService.save(advertList, getLoginUser().getLoginName(), advertConfig.getMaxCount());
        return returnSuccess();
    }

    /**
     *   http://dfs-dev.eafservice.evergrande.com/swagger-ui.html 查看dfs开发接口参数说明
     * @param objectName 文件名，包含路径
     * @param req 请求
     * @param res 响应
     * @return
     * @throws BusinessException
     */
    @GetMapping(value="/download")
    @ApiOperation(value = "downloadImage", notes = "下载文件接口<br>" +
            "必填字段：objectName；", httpMethod = "GET")
    @ApiImplicitParam(paramType = "header", name = "Authorization", required=true)
    public RestResult download(@RequestParam("objectName") String objectName, HttpServletRequest req, HttpServletResponse res) throws BusinessException{
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            String downloadFileName = objectName;
            String fileName = objectName;
            fileName = fileName.replace("/", URLEncoder.encode("/", "utf-8"));
            fileName = URLEncoder.encode(fileName, "utf-8");
            String url = dfsConfig.getUrl() + "/api/dfs/file/" + dfsConfig.getBucketName() + "/";
            url = url + fileName + "/download";
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(dfsConfig.getHeader(), dfsConfig.getToken())
                    .get()
                    .build();
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            okhttp3.ResponseBody responseBody = response.body();
            String agent = req.getHeader("USER-AGENT").toLowerCase();
            String headerValue;
            String certFileName = downloadFileName;
            final String firefox ="firefox";
            final String safari = "safari";
            if (agent.contains(firefox) || agent.contains(safari)) {
                headerValue = "attachment;filename=" + new String(certFileName.getBytes("UTF-8"), "ISO-8859-1");
            } else {
                certFileName = URLEncoder.encode(fileName, "UTF-8");
                headerValue = "attachment;filename=" + certFileName;
            }
            res.setContentType("application/x-download");
            String headerKey = "Content-Disposition";
            res.setHeader(headerKey, headerValue);
            inputStream =  responseBody.byteStream();
            outputStream = res.getOutputStream();
            int ch;
            byte[] buffer = new byte[2048];

            while ((ch = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, ch);
            }
        } catch (Exception e) {
            logger.error("AdvertController.download " + Throwables.getStackTraceAsString(e));
            throw BusinessException.define(ErrorCode.DOWNLOAD_FILE_EXCEPTION);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }catch (Exception e){
                logger.error("AdvertController.download " + Throwables.getStackTraceAsString(e));
                throw BusinessException.define(ErrorCode.DOWNLOAD_FILE_EXCEPTION);
            }
        }

        return returnSuccess();
    }

}