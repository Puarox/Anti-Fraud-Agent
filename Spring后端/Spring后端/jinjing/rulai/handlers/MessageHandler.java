package rulai.handlers;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessageHandler {

    @OnConnect
    public void onConnect(SocketIOClient client) {
        // 可以在这里获取 sessionId，例如从 url 参数里: client.getHandshakeData().getSingleUrlParam("sessionId")
        System.out.println("客户端连接成功: " + client.getSessionId());
    }

    // 前端执行 socket.emit('join', {sessionId: 'xxx'}) 时触发
    @OnEvent("join")
    public void onJoin(SocketIOClient client, Map<String, String> data) {
        String sessionId = data.get("sessionId");
        // 加入房间，方便后续局部广播
        client.joinRoom(sessionId);
        System.out.println("用户加入会话房间: " + sessionId);
    }
}
