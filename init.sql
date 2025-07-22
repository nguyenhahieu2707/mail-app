-- init.sql
-- Thay đổi phương thức xác thực cho người dùng 'root' để tương thích với Postfix/Dovecot client cũ hơn
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root';
FLUSH PRIVILEGES;
