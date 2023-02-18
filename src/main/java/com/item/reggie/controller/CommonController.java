package com.item.reggie.controller;

import com.item.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;


/**
 * 文件上传和下载
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    //读取配置文件的设置
    @Value("${reggie.path}")
    private  String  basepath;
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){        //这里的参数名 ，file 必须与前端一致
      log.info("upload: {}",file.getOriginalFilename());
      //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除。
        //file.getOriginalFilename()获取原始文件名

        String originalname= file.getOriginalFilename();
        String suffix =originalname.substring(originalname.lastIndexOf("."));
        //使用uuid重新生成文件名，防止文件名重复造成文件覆盖
        String fileName =UUID.randomUUID().toString()+suffix;

        //创建一个目录结构；
        File dir=new File(basepath);
        if(!dir.exists()){
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(basepath+fileName));

        } catch (IOException e){
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream=new FileInputStream(new File(basepath+name));

            //输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream servletOutputStream= response.getOutputStream();


            response.setContentType("/image/jpeg");

            int len=0;
            byte[] bytes=new byte[1024];
            while((len=fileInputStream.read(bytes))!=-1){
                servletOutputStream.write(bytes,0,len);
                servletOutputStream.flush();
            }


            //关闭资源
            servletOutputStream.close();
            fileInputStream.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
