package rulai.service.impl;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;
import java.util.Map;
@Service
public class OssService {

    private final OSS ossClient;
    private final String bucketName = "paroxbucket";
    private final String endpoint = "https://oss-cn-beijing.aliyuncs.com";

    public OssService() throws Exception {
        String region = "cn-beijing";

        EnvironmentVariableCredentialsProvider credentialsProvider =
                CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        conf.setSignatureVersion(SignVersion.V4);

        this.ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    public Map<String, String> generateUploadUrl(String suffix) {

        // 1️⃣ 生成文件路径
        String objectKey = "uploads/"
                + System.currentTimeMillis()
                + "_" + (int)(Math.random() * 10000)
                + suffix;

        // 2️⃣ 设置过期时间（5分钟）
        Date expiration = new Date(System.currentTimeMillis() + 5 * 60 * 1000);

        // 3️⃣ 生成 PUT 签名 URL
        GeneratePresignedUrlRequest request =
                new GeneratePresignedUrlRequest(bucketName, objectKey, HttpMethod.PUT);

        request.setExpiration(expiration);

        URL signedUrl = ossClient.generatePresignedUrl(request);

        // 4️⃣ 最终访问 URL（公网）
        String finalUrl = "https://" + bucketName + ".oss-cn-beijing.aliyuncs.com/" + objectKey;

        return Map.of(
                "signedUrl", signedUrl.toString(),
                "finalUrl", finalUrl
        );
    }
}
