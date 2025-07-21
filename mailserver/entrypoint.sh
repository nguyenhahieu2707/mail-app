
#!/bin/bash

#echo "[ENTRYPOINT] Cleaning up old sockets..."
## ... (các lệnh dọn dẹp giữ nguyên) ...
#chown -R 5000:5000 /var/mail/vhosts
#
## --- PHẦN THAY ĐỔI QUAN TRỌNG ---
#echo "[ENTRYPOINT] Resolving MySQL IP address..."
#MYSQL_IP=""
#while [ -z "$MYSQL_IP" ]; do
#    MYSQL_IP=$(dig +short mysql)
#    if [ -z "$MYSQL_IP" ]; then
#        echo "[ENTRYPOINT] Could not resolve mysql, retrying in 2s..."
#        sleep 2
#    fi
#done
#echo "[ENTRYPOINT] ✅ MySQL resolved to IP: $MYSQL_IP"
#
#echo "[ENTRYPOINT] Creating config files from templates..."
#
## Tạo file cấu hình cho Dovecot
#sed "s/__MYSQL_HOST__/$MYSQL_IP/g" /etc/dovecot/dovecot-sql.conf.ext.template > /etc/dovecot/dovecot-sql.conf.ext
#
## Tạo các file cấu hình cho Postfix
#sed "s/__MYSQL_HOST__/$MYSQL_IP/g" /etc/postfix/mysql-virtual-domains.cf.template > /etc/postfix/mysql-virtual-domains.cf
#sed "s/__MYSQL_HOST__/$MYSQL_IP/g" /etc/postfix/mysql-virtual-mailboxes.cf.template > /etc/postfix/mysql-virtual-mailboxes.cf
## Thêm dòng sed cho file mysql-virtual-alias-maps.cf nếu bạn có
## sed "s/__MYSQL_HOST__/$MYSQL_IP/g" /etc/postfix/mysql-virtual-alias-maps.cf.template > /etc/postfix/mysql-virtual-alias-maps.cf
#
#echo "[ENTRYPOINT] ✅ All config files created."
## --- KẾT THÚC PHẦN THAY ĐỔI ---
#
#
## ... (các lệnh khởi động Dovecot và Postfix giữ nguyên) ...
#echo "[ENTRYPOINT] Starting Dovecot (background)..."
#/usr/sbin/dovecot &
#
#echo "[ENTRYPOINT] Waiting for dovecot-lmtp socket to be ready..."
#while [ ! -S /var/spool/postfix/private/dovecot-lmtp ]; do
#    sleep 0.5
#done
#echo "[ENTRYPOINT] ✅ Dovecot LMTP socket is ready."
#
#echo "[ENTRYPOINT] Starting Postfix (foreground)..."
#/usr/sbin/postfix start-fg

#!/bin/bash

echo "[ENTRYPOINT] Cleaning up old sockets..."
rm -f /var/spool/postfix/private/dovecot-lmtp
rm -f /var/run/dovecot/master.pid
rm -rf /run/dovecot
mkdir -p /run/dovecot
chown dovecot:dovecot /run/dovecot
chmod 755 /run/dovecot

# Đảm bảo mailbox có quyền đúng
chown -R 5000:5000 /var/mail/vhosts

# --- Thay thế __MYSQL_HOST__ bằng IP thật ---
echo "[ENTRYPOINT] Resolving MySQL IP address..."
MYSQL_IP=""
while [ -z "$MYSQL_IP" ]; do
    MYSQL_IP=$(dig +short mysql)
    if [ -z "$MYSQL_IP" ]; then
        echo "[ENTRYPOINT] Could not resolve mysql, retrying in 2s..."
        sleep 2
    fi
done
echo "[ENTRYPOINT] ✅ MySQL resolved to IP: $MYSQL_IP"

echo "[ENTRYPOINT] Creating config files from templates..."
sed "s/__MYSQL_HOST__/$MYSQL_IP/g" /etc/dovecot/dovecot-sql.conf.ext.template > /etc/dovecot/dovecot-sql.conf.ext
sed "s/__MYSQL_HOST__/$MYSQL_IP/g" /etc/postfix/mysql-virtual-domains.cf.template > /etc/postfix/mysql-virtual-domains.cf
sed "s/__MYSQL_HOST__/$MYSQL_IP/g" /etc/postfix/mysql-virtual-mailboxes.cf.template > /etc/postfix/mysql-virtual-mailboxes.cf
# Nếu có alias map thì thêm:
# sed "s/__MYSQL_HOST__/$MYSQL_IP/g" /etc/postfix/mysql-virtual-alias-maps.cf.template > /etc/postfix/mysql-virtual-alias-maps.cf
echo "[ENTRYPOINT] ✅ All config files created."

# Chờ MySQL sẵn sàng
echo "[ENTRYPOINT] Waiting for MySQL to be ready..."
until mysql -hmysql -uroot -proot -e "SELECT 1;" &> /dev/null
do
    echo "[ENTRYPOINT] MySQL is not ready yet. Retrying in 5s..."
    sleep 5
done
echo "[ENTRYPOINT] ✅ MySQL is ready!"

# Chờ socket LMTP được tạo sau khi Dovecot khởi động
echo "[ENTRYPOINT] Starting Postfix (background)..."
/usr/sbin/postfix start
echo "[ENTRYPOINT] ✅ Postfix started."

echo "[ENTRYPOINT] ✅ Handing off to Dovecot (foreground)"
exec /usr/sbin/dovecot -F
