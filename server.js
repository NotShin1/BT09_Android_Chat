const app = require('express')();
const http = require('http').createServer(app);
const io = require('socket.io')(http, {
    cors: {
        origin: "*", 
        methods: ["GET", "POST"]
    }
});

io.on('connection', (socket) => {
    console.log('Một user đã kết nối: ' + socket.id);

    // Lắng nghe tin nhắn từ Client gửi lên
    socket.on('send_message', (data) => {
        console.log('Tin nhắn nhận được: ', data);
        
        // Gửi lại tin nhắn cho TẤT CẢ mọi người (bao gồm cả người gửi)
        // Đây là chỗ Manager và Customer sẽ thấy tin nhắn của nhau
        io.emit('receive_message', data);
    });

    socket.on('disconnect', () => {
        console.log('User đã thoát');
    });
});

http.listen(3000, () => {
    console.log('Server đang chạy tại port 3000');
});