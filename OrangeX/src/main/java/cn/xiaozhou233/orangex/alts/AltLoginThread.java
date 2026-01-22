package cn.xiaozhou233.orangex.alts;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.utils.BrowserUtil;
import cn.xiaozhou233.orangex.utils.ClipboardUtil;
import net.lenni0451.commons.httpclient.HttpClient;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.java.JavaAuthManager;
import net.raphimc.minecraftauth.msa.model.MsaDeviceCode;
import net.raphimc.minecraftauth.msa.service.impl.DeviceCodeMsaAuthService;

import java.util.UUID;
import java.util.function.Consumer;

public class AltLoginThread extends Thread {

    private String status = "NONE";
    private final Consumer<String> statusConsumer;

    public AltLoginThread(Consumer<String> statusConsumer) {
        super("Alt Login Thread");
        this.statusConsumer = statusConsumer;
    }

    public String getStatus() {
        return status;
    }

    private void setStatus(String status) {
        this.status = status;
        if (statusConsumer != null) {
            statusConsumer.accept(status);
        }
    }

    @Override
    public void run() {
        try {
            HttpClient httpClient = MinecraftAuth.createHttpClient();
            setStatus("Created HttpClient");

            JavaAuthManager.Builder authManagerBuilder = JavaAuthManager.create(httpClient);
            setStatus("Created AuthManagerBuilder");

            JavaAuthManager authManager = authManagerBuilder.login(DeviceCodeMsaAuthService::new, new Consumer<MsaDeviceCode>() {
                @Override
                public void accept(MsaDeviceCode deviceCode) {
                    System.out.println("Go to " + deviceCode.getVerificationUri());
                    BrowserUtil.openUrl(deviceCode.getDirectVerificationUri());
                    System.out.println("Enter code " + deviceCode.getUserCode());
                    ClipboardUtil.copyToClipboard(deviceCode.getUserCode());

                    setStatus("Waiting Login... code: " + deviceCode.getUserCode());
                }
            });

            String username = authManager.getMinecraftProfile().getUpToDate().getName();
            UUID uuid = authManager.getMinecraftProfile().getUpToDate().getId();
            String accessToken = authManager.getMinecraftToken().getUpToDate().getToken();
            long expireTimeMs = authManager.getMinecraftToken().getUpToDate().getExpireTimeMs();

            Alt alt = new Alt(username, uuid, accessToken, expireTimeMs);
            OrangeX.getInstance().getAltManager().addAlt(alt);
            setStatus("Login Success!");

        } catch (Exception e) {
            setStatus("Login Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
