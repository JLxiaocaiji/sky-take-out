实现步骤：
直接使用websocket.html页面作为WebSocket客户端
导入WebSocket的maven坐标
导入WebSocket服务端组件WebSocketServer，用于和客户端通信
导入配置类WebSocketConfiguration，注册WebSocket的服务端组件
导入定时任务类WebSocketTask，定时向客户端推送数据

@ServerEndpoint("/ws/{sid}")
@ServerEndpoint 注解用于标记一个类，该类将作为 WebSocket 服务端的端点
"/ws/{sid}" 是端点的 URI 路径。这个路径可以包含路径参数，如 {sid}，它是一个占位符，用于在运行时捕获实际请求中的值
```
@ServerEndpoint("/ws/{sid}")
public class MyWebSocketServer {

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("WebSocket opened with session ID: " + sid);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received message: " + message);
        try {
            session.getBasicRemote().sendText("Echo: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("WebSocket closed");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error: " + throwable.getMessage());
    }
}

@ServerEndpoint("/ws/{sid}") 定义了 WebSocket 服务端的端点路径，其中 {sid} 是一个路径参数。
@OnOpen 注解标记的方法 onOpen 在客户端打开一个新的 WebSocket 连接时被调用。@PathParam("sid") String sid 用于从路径中提取 sid 参数的值。
@OnMessage 注解标记的方法 onMessage 在服务端接收到客户端发送的消息时被调用。
@OnClose 注解标记的方法 onClose 在 WebSocket 连接被关闭时被调用。
@OnError 注解标记的方法 onError 在发生错误时被调用。
```