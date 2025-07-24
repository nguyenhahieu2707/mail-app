echo "[ENTRYPOINT] Cleaning up old sockets..."
rm -f /var/spool/postfix/private/dovecot-lmtp
rm -f /var/run/dovecot/master.pid
rm -rf /run/dovecot
mkdir -p /run/dovecot
chown dovecot:dovecot /run/dovecot
chmod 755 /run/dovecot

# Đảm bảo mailbox có quyền đúng
chown -R 5000:5000 /var/mail/vhosts

# Logging: đảm bảo thư mục log tồn tại
mkdir -p /var/log/mailserver
touch /var/log/mailserver/postfix.log /var/log/mailserver/dovecot.log
chmod 644 /var/log/mailserver/*.log

# Khởi động rsyslog
echo "[ENTRYPOINT] Starting rsyslog..."
rsyslogd

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
