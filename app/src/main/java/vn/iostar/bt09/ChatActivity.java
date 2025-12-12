package vn.iostar.bt09;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.net.URISyntaxException;

public class ChatActivity extends AppCompatActivity {

    private Socket mSocket;
    private EditText edtMessage;
    private TextView txtChatLog;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Ánh xạ View (Bạn tự tạo layout nhé)
        edtMessage = findViewById(R.id.edtMessage);
        txtChatLog = findViewById(R.id.txtChatLog);
        btnSend = findViewById(R.id.btnSend);

        // 1. Khởi tạo kết nối Socket
        try {
            // Thay đổi URL phù hợp (Emulator: 10.0.2.2, Device thật: IP máy tính)
            mSocket = IO.socket("http://10.0.2.2:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // 2. Kết nối
        mSocket.connect();

        // 3. Lắng nghe sự kiện "receive_message" từ Server gửi về
        mSocket.on("receive_message", onNewMessage);

        // 4. Xử lý nút Gửi
        btnSend.setOnClickListener(v -> {
            String content = edtMessage.getText().toString().trim();
            if (!content.isEmpty()) {
                sendMessage(content);
                edtMessage.setText(""); // Xóa ô nhập sau khi gửi
            }
        });
    }

    // Hàm gửi tin nhắn lên Server
    private void sendMessage(String message) {
        JSONObject data = new JSONObject();
        try {
            data.put("sender", "Customer"); // Hoặc lấy tên user thật
            data.put("message", message);

            // "send_message" phải trùng tên với sự kiện socket.on bên server Nodejs
            mSocket.emit("send_message", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Listener nhận tin nhắn (Chạy trên background thread)
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            // Vì Socket chạy ngầm, muốn cập nhật UI (TextView) phải dùng runOnUiThread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String sender = data.getString("sender");
                        String message = data.getString("message");

                        // Hiển thị lên màn hình
                        String currentLog = txtChatLog.getText().toString();
                        txtChatLog.setText(currentLog + "\n" + sender + ": " + message);

                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ngắt kết nối khi thoát app để tránh tốn pin/tài nguyên
        mSocket.disconnect();
        mSocket.off("receive_message", onNewMessage);
    }
}
